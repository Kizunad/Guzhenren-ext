package com.Kizunad.customNPCs.ai.actions.common;

import com.Kizunad.customNPCs.ai.actions.AbstractStandardAction;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.interfaces.IAttackAction;
import com.Kizunad.customNPCs.ai.llm.LlmPromptRegistry;
import com.Kizunad.customNPCs.ai.status.config.NpcStatusConfig;
import com.Kizunad.customNPCs.ai.util.EntityRelationUtil;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import java.util.UUID;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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
        "AttackAction: melee strike target within ~3.5 blocks; backs off if too close; "
            + "requires target UUID set; uses mob swing/doHurtTarget.";

    static {
        LlmPromptRegistry.register(LLM_USAGE_DESC);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(
        AttackAction.class
    );
    private static final double DESIRED_RANGE_FACTOR = 0.9D;
    private static final double BACKOFF_RANGE_FACTOR = 0.6D;
    private static final double REPOSITION_SPEED = 1.2D;
    private static final double RETREAT_EXTRA = 0.8D;
    private static final double RANGE_HYSTERESIS = 0.5D;
    private static final double MIN_LOWER_THRESHOLD = 0.1D;

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
    }

    @Override
    protected ActionStatus tickInternal(INpcMind mind, Mob mob) {
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
            EntityRelationUtil.isAlly(mob, livingTarget)
        ) {
            LOGGER.info(
                "[AttackAction] 目标 {} 被识别为友方，放弃攻击",
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

        // 检查距离
        Vec3 mobPos = mob.position();
        Vec3 targetPos = targetEntity.position();
        double distance = mobPos.distanceTo(targetPos);

        // 主动保持近战理想距离窗口，加入滞后避免抖动
        double upperThreshold = Math.max(
            attackRange,
            attackRange * DESIRED_RANGE_FACTOR + RANGE_HYSTERESIS
        );
        double lowerThreshold = Math.max(
            MIN_LOWER_THRESHOLD,
            attackRange * BACKOFF_RANGE_FACTOR - RANGE_HYSTERESIS
        );

        if (distance > upperThreshold) {
            navigateTowards(mob, targetPos);
            return ActionStatus.RUNNING;
        }
        if (distance < lowerThreshold) {
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

        // 造成伤害
        if (target instanceof LivingEntity livingTarget) {
            mob.doHurtTarget(livingTarget);
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

    @Override
    protected void onStart(INpcMind mind, LivingEntity entity) {
        this.cooldownCounter = 0;
        this.hasHit = false;
        this.attemptTicks = 0;
        LOGGER.info("[AttackAction] 开始攻击目标 {}", targetUuid);
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
