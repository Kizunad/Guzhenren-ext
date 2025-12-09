package com.Kizunad.customNPCs.ai.actions.common;

import com.Kizunad.customNPCs.ai.actions.AbstractStandardAction;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.interfaces.IAttackAction;
import com.Kizunad.customNPCs.ai.actions.registry.AttackCompatRegistry;
import com.Kizunad.customNPCs.ai.actions.util.NavigationUtil;
import com.Kizunad.customNPCs.ai.inventory.NpcInventory;
import com.Kizunad.customNPCs.ai.llm.LlmPromptRegistry;
import com.Kizunad.customNPCs.ai.status.config.NpcStatusConfig;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import java.util.UUID;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 攻击动作 - 攻击指定目标实体
 * <p>
 * 功能：
 * - 检查目标距离
 * - 执行攻击动画（swing）
 * - 造成伤害（doHurtTarget）
 * - 处理攻击冷却
 * <p>
 * 参数：
 * - targetUuid: 目标实体的 UUID
 * - attackRange: 攻击距离阈值（默认 3.0 blocks）
 * - cooldownTicks: 攻击冷却时长（默认 20 ticks）
 * - maxAttempts: 最大攻击尝试次数
 */
public class AttackAction
    extends AbstractStandardAction
    implements IAttackAction {

    public static final String LLM_USAGE_DESC =
        "AttackAction: melee strike target within ~3.5 blocks; backs off if too close; " +
        "requires target UUID set; uses mob swing/doHurtTarget.";

    static {
        LlmPromptRegistry.register(LLM_USAGE_DESC);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(
        AttackAction.class
    );
    private static final double DESIRED_RANGE_FACTOR = 0.75D;
    private static final double BACKOFF_RANGE_FACTOR = 0.55D;
    private static final double REPOSITION_SPEED = 1.2D;
    private static final double RETREAT_EXTRA = 0.8D;
    private static final double RANGE_HYSTERESIS = 0.35D;
    private static final double ATTACK_RANGE_BUFFER = 0.4D;
    private static final double MIN_LOWER_THRESHOLD = 0.1D;
    private static final float LOOK_MAX_ROTATION = 30.0F;
    private static final int UNREACHABLE_TICKS_THRESHOLD = 40;
    private static final double STUCK_DISTANCE_EPSILON = 0.25D;
    private static final int STUCK_NO_HIT_TICKS = 80;

    // 从配置获取默认值

    // ==================== 参数 ====================
    /**
     * 攻击距离阈值（blocks）
     */
    private final double attackRange;

    /**
     * 攻击冷却时长（ticks）
     */
    private final int cooldownTicks;

    /**
     * 最大攻击尝试时长（ticks）
     */
    private final int maxAttemptTicks;

    // ==================== 状态 ====================
    /**
     * 攻击冷却计数器
     */
    private int cooldownCounter;

    /**
     * 是否已成功命中
     */
    private boolean hasHit;

    /**
     * 尝试攻击的 tick 数
     */
    private int attemptTicks;

    /**
     * 连续不可达计数
     */
    private int unreachableTicks;
    /**
     * 最近一次产生位移的位置与 tick，用于检测卡位
     */
    private Vec3 lastProgressPos;
    private int lastProgressTick;

    /**
     * 创建攻击动作（使用默认值）
     * @param targetUuid 目标实体 UUID
     */
    public AttackAction(UUID targetUuid) {
        this(
            targetUuid,
            CONFIG.getAttackRange(),
            CONFIG.getAttackCooldownTicks(),
            CONFIG.getMaxAttackAttemptTicks()
        );
    }

    /**
     * 创建攻击动作（完整参数）
     * @param targetUuid 目标实体 UUID
     * @param attackRange 攻击距离
     * @param cooldownTicks 攻击冷却时长
     * @param maxAttemptTicks 最大尝试时长
     */
    public AttackAction(
        UUID targetUuid,
        double attackRange,
        int cooldownTicks,
        int maxAttemptTicks
    ) {
        super("AttackAction", targetUuid, maxAttemptTicks, 0, attackRange);
        this.attackRange = attackRange;
        this.cooldownTicks = cooldownTicks;
        this.maxAttemptTicks = maxAttemptTicks;
        this.cooldownCounter = 0;
        this.hasHit = false;
        this.attemptTicks = 0;
        this.unreachableTicks = 0;
        this.lastProgressPos = null;
        this.lastProgressTick = 0;
    }

    @Override
    protected ActionStatus tickInternal(INpcMind mind, Mob mob) {
        attemptTicks++;
        // 获取目标实体
        Entity targetEntity = resolveEntity(mob.level());
        if (targetEntity == null) {
            LOGGER.warn("[AttackAction] 目标实体 {} 不存在", targetUuid);
            return ActionStatus.FAILURE;
        }

        // NOTE: 可能需要一个缓存EntityUUID
        // 场景：周围过多友方实体导致的性能问题
        // 现在暂时不考虑
        if (
            targetEntity instanceof LivingEntity livingTarget &&
            isProtectedAlly(mob, livingTarget)
        ) {
            LOGGER.info(
                "[AttackAction] 目标 {} 属于同队伍或自身，放弃攻击",
                targetEntity.getName().getString()
            );
            return ActionStatus.FAILURE;
        }

        // 检查目标是否存活
        if (!targetEntity.isAlive()) {
            LOGGER.info("[AttackAction] 目标已死亡");
            return ActionStatus.SUCCESS; // 目标已死亡视为成功
        }

        // 检查目标是否切换维度
        if (!targetEntity.level().equals(mob.level())) {
            LOGGER.warn("[AttackAction] 目标切换维度");
            return ActionStatus.FAILURE;
        }

        if (!NavigationUtil.canReachEntity(mob, targetEntity, 0.0D)) {
            unreachableTicks++;
            if (unreachableTicks >= UNREACHABLE_TICKS_THRESHOLD) {
                LOGGER.debug(
                    "[AttackAction] 连续 {} ticks 无法到达目标，放弃",
                    unreachableTicks
                );
                return ActionStatus.FAILURE;
            }
        } else {
            unreachableTicks = 0;
        }

        // 始终面向目标，防止因朝向问题导致攻击判定失败或卡住
        mob
            .getLookControl()
            .setLookAt(targetEntity, LOOK_MAX_ROTATION, LOOK_MAX_ROTATION);

        // 检查距离
        Vec3 mobPos = mob.position();
        Vec3 targetPos = targetEntity.position();

        // 卡位检测：位置长时间几乎不变且未命中，认为卡住，提前失败让目标重评估
        if (
            lastProgressPos == null ||
            mobPos.distanceToSqr(lastProgressPos) >
            STUCK_DISTANCE_EPSILON * STUCK_DISTANCE_EPSILON
        ) {
            lastProgressPos = mobPos;
            lastProgressTick = attemptTicks;
        } else if (
            !hasHit && attemptTicks - lastProgressTick >= STUCK_NO_HIT_TICKS
        ) {
            LOGGER.warn(
                "[AttackAction] 持续 {} ticks 位置未变化且未命中，判定卡住，触发重评估",
                attemptTicks - lastProgressTick
            );
            return ActionStatus.FAILURE;
        }

        double distance = mobPos.distanceTo(targetPos);

        // 主动靠近：靠前一些减少脱手；攻击窗口增加缓冲避免轻微拉开就判定失败
        double approachRange = attackRange * DESIRED_RANGE_FACTOR;
        double attackWindowUpper = attackRange + ATTACK_RANGE_BUFFER;
        double approachUpper = approachRange + RANGE_HYSTERESIS;
        double lowerThreshold = Math.max(
            MIN_LOWER_THRESHOLD,
            approachRange * BACKOFF_RANGE_FACTOR - RANGE_HYSTERESIS
        );

        if (distance > attackWindowUpper) {
            navigateTowards(mob, targetPos);
            return ActionStatus.RUNNING;
        }

        if (distance > approachUpper) {
            navigateTowards(mob, targetPos);
        } else if (distance < lowerThreshold) {
            navigateAway(mob, mobPos, targetPos);
            return ActionStatus.RUNNING;
        }

        // 处理冷却
        if (cooldownCounter > 0) {
            cooldownCounter--;
            return ActionStatus.RUNNING;
        }

        // 执行攻击
        performAttack(mind, mob, targetEntity);

        // 设置冷却
        cooldownCounter = cooldownTicks;
        hasHit = true;

        // 成功条件：命中一次即为成功
        LOGGER.info(
            "[AttackAction] 攻击成功，目标: {}",
            targetEntity.getName().getString()
        );
        return ActionStatus.SUCCESS;
    }

    /**
     * 执行攻击逻辑
     * @param mob 攻击者
     * @param target 目标
     */
    private void performAttack(INpcMind mind, Mob mob, Entity target) {
        // 播放挥手动画
        mob.swing(InteractionHand.MAIN_HAND);

        if (target instanceof LivingEntity livingTarget) {
            AttackCompatRegistry.AttackContext context =
                new AttackCompatRegistry.AttackContext(
                    mind,
                    mob,
                    livingTarget,
                    mob.getMainHandItem()
                );
            AttackCompatRegistry.AttackDecision decision =
                AttackCompatRegistry.dispatch(context);

            if (decision != AttackCompatRegistry.AttackDecision.HANDLED) {
                mob.doHurtTarget(livingTarget);
            }

            mind
                .getStatus()
                .addExhaustion(
                    NpcStatusConfig.getInstance().getAttackExhaustion()
                );
            LOGGER.debug(
                "[AttackAction] 攻击 {} | 剩余生命值: {}",
                target.getName().getString(),
                livingTarget.getHealth()
            );
        }
    }

    /**
     * 判定是否需要保护的友军（仅保留队伍/自身保护，允许攻击同类）。
     */
    private boolean isProtectedAlly(Mob mob, LivingEntity target) {
        if (mob == target) {
            return true;
        }
        Entity vehicle = mob.getVehicle();
        if (vehicle == target) {
            return true;
        }
        return mob.isAlliedTo(target);
    }

    @Override
    protected void onStart(INpcMind mind, LivingEntity entity) {
        this.cooldownCounter = 0;
        this.hasHit = false;
        this.attemptTicks = 0;
        this.unreachableTicks = 0;
        this.lastProgressPos = null;
        this.lastProgressTick = 0;

        if (entity instanceof Mob mob) {
            equipBestMeleeWeapon(mind, mob);
        }

        LOGGER.info("[AttackAction] 开始攻击目标 {}", targetUuid);
    }

    /**
     * 尝试从背包中装备攻击力最高的近战武器。
     */
    private void equipBestMeleeWeapon(INpcMind mind, Mob mob) {
        ItemStack currentStack = mob.getMainHandItem();
        double bestDamage = getAttackDamage(currentStack);
        int bestSlot = -1;

        NpcInventory inventory = mind.getInventory();

        // 遍历主背包寻找更好的武器
        for (int i = 0; i < inventory.getMainSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            double damage = getAttackDamage(stack);
            if (damage > bestDamage) {
                bestDamage = damage;
                bestSlot = i;
            }
        }

        // 如果找到了更好的武器，执行交换
        if (bestSlot != -1) {
            ItemStack bestWeapon = inventory.getItem(bestSlot);
            ItemStack toInventory = currentStack.copy();

            // 1. 装备新武器
            mob.setItemInHand(InteractionHand.MAIN_HAND, bestWeapon);

            // 2. 旧武器（如果有）放回原槽位
            inventory.setItem(bestSlot, toInventory);

            LOGGER.info(
                "[AttackAction] 自动切换更强武器: {} (攻: {})",
                bestWeapon.getHoverName().getString(),
                bestDamage
            );
        }
    }

    private double getAttackDamage(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0.0; // 空手（实际上基础攻击是1，但这用于比较增量）
        }
        // 获取主手属性修饰符中的攻击伤害总和
        ItemAttributeModifiers modifiers = stack.getAttributeModifiers();
        final double[] damage = {0.0d};
        modifiers.forEach(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute.equals(Attributes.ATTACK_DAMAGE)) {
                damage[0] += modifier.amount();
            }
        });
        return damage[0];
    }

    @Override
    protected void onStop(INpcMind mind, LivingEntity entity) {
        LOGGER.info(
            "[AttackAction] 停止攻击 | 已命中: {} | 尝试 {} ticks",
            hasHit,
            attemptTicks
        );
    }

    @Override
    public boolean canInterrupt() {
        // 攻击动作不可中断（优先级高）
        return false;
    }

    @Override
    public UUID getTargetUuid() {
        return targetUuid;
    }

    @Override
    public boolean hasHit() {
        return hasHit;
    }

    /**
     * 朝目标推进到理想距离。
     */
    private void navigateTowards(Mob mob, Vec3 targetPos) {
        double desiredDistance = attackRange * DESIRED_RANGE_FACTOR;
        Vec3 dir = targetPos.subtract(mob.position());
        Vec3 dest = targetPos.subtract(dir.normalize().scale(desiredDistance));
        mob
            .getNavigation()
            .moveTo(dest.x(), dest.y(), dest.z(), REPOSITION_SPEED);
    }

    /**
     * 当过近时，朝远离目标方向后撤。
     */
    private void navigateAway(Mob mob, Vec3 mobPos, Vec3 targetPos) {
        Vec3 dir = mobPos.subtract(targetPos);
        Vec3 dest = mobPos.add(
            dir.normalize().scale(attackRange + RETREAT_EXTRA)
        );
        mob
            .getNavigation()
            .moveTo(dest.x(), dest.y(), dest.z(), REPOSITION_SPEED);
    }
}
