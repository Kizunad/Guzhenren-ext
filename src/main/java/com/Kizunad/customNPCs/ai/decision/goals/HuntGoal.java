package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.actions.AbstractStandardAction;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.common.AttackAction;
import com.Kizunad.customNPCs.ai.actions.common.RangedAttackItemAction;
import com.Kizunad.customNPCs.ai.actions.config.ActionConfig;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.llm.LlmPromptRegistry;
import com.Kizunad.customNPCs.ai.util.EntityRelationUtil;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import java.util.Comparator;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 猎杀弱小生物的 Goal。
 * <p>
 * 逻辑：在安全状态下寻找比自身弱小的非友方实体，根据距离和装备智能选择近战或远程攻击。
 * 优先级：中等，低于逃跑/防御等保命类目标。
 */
public class HuntGoal implements IGoal {

    public static final String LLM_USAGE_DESC =
        "HuntGoal: when healthy/safe, seek weaker non-friendly targets; switches between Melee and Ranged based on distance and equipment.";

    static {
        LlmPromptRegistry.register(LLM_USAGE_DESC);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(
        HuntGoal.class
    );
    private static final double SEARCH_RADIUS = 12.0D;
    private static final double WEAKNESS_THRESHOLD = 0.8D; // 目标综合评分需低于此比例
    private static final float BASE_PRIORITY = 0.35f; // 明确低于 Flee/Defend
    private static final float PRIORITY_GAIN = 0.25f;
    private static final float SAFE_HEALTH_RATIO = 0.6f; // 血量低于此值不主动出击
    private static final double DISTANCE_WEIGHT = 0.05D; // 轻度偏好近目标
    private static final int ATTACK_TIMEOUT_TICKS = 200; // 近战最长尝试时间（10s）
    private static final double RANGED_MIN_DIST = 4.0D; // 远程切换阈值

    private final double attackRange;
    private final int attackCooldownTicks;
    private AbstractStandardAction attackAction;
    private UUID targetUuid;
    private double cachedAdvantageRatio = 0.0D;

    public HuntGoal() {
        ActionConfig config = ActionConfig.getInstance();
        this.attackRange = config.getAttackRange();
        this.attackCooldownTicks = config.getAttackCooldownTicks();
    }

    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        if (!canEngage(mind, entity)) {
            return 0.0f;
        }
        LivingEntity target = ensureTarget(entity);
        if (target == null) {
            return 0.0f;
        }
        // 优先级随优势提升，但上限控制在中等水平，避免压过保命目标
        double clamped = Math.min(1.0D, Math.max(0.0D, cachedAdvantageRatio));
        return BASE_PRIORITY + (float) (clamped * PRIORITY_GAIN);
    }

    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        return canEngage(mind, entity) && ensureTarget(entity) != null;
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        LivingEntity target = ensureTarget(entity);
        if (target != null) {
            targetUuid = target.getUUID();
            createAppropriateAction(mind, (Mob) entity, target);
            attackAction.start(mind, entity);
            LOGGER.info(
                "[HuntGoal] {} 开始猎杀目标 {} (策略: {})",
                entity.getName().getString(),
                target.getName().getString(),
                attackAction.getClass().getSimpleName()
            );
        }
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        if (!canEngage(mind, entity)) {
            return;
        }
        LivingEntity target = ensureTarget(entity);
        if (target == null) {
            stop(mind, entity);
            return;
        }
        Mob mob = (Mob) entity;

        // 动态策略检查与切换
        if (attackAction != null) {
            boolean isRangedAction =
                attackAction instanceof RangedAttackItemAction;
            boolean shouldBeRanged = shouldUseRanged(mob, target);

            if (isRangedAction != shouldBeRanged) {
                LOGGER.debug(
                    "[HuntGoal] 策略切换: {} -> {}",
                    isRangedAction ? "远程" : "近战",
                    shouldBeRanged ? "远程" : "近战"
                );
                attackAction.stop(mind, entity);
                attackAction = null;
            }
        }

        if (attackAction == null) {
            createAppropriateAction(mind, mob, target);
            attackAction.start(mind, entity);
        }

        ActionStatus status = attackAction.tick(mind, entity);
        if (status == ActionStatus.SUCCESS) {
            // 攻击命中后，若目标仍存活且策略未变，可选择继续攻击（这里简化为停止让下一tick重建，或保持）
            // RangedAttackItemAction 有后摇，SUCCESS 表示已完成一次射击循环
            if (target.isAlive()) {
                attackAction.stop(mind, entity);
                attackAction = null;
            }
        } else if (status == ActionStatus.FAILURE) {
            LOGGER.info("[HuntGoal] 动作失败，重新评估策略");
            attackAction.stop(mind, entity);
            attackAction = null;
            // 下一 tick 会重建，如果 Ranged 失败（如卡住），可能距离变近了，就会切近战
        }
    }

    private void createAppropriateAction(
        INpcMind mind,
        Mob mob,
        LivingEntity target
    ) {
        if (shouldUseRanged(mob, target)) {
            attackAction = new RangedAttackItemAction(target.getUUID());
        } else {
            attackAction = new AttackAction(
                target.getUUID(),
                attackRange,
                attackCooldownTicks,
                ATTACK_TIMEOUT_TICKS
            );
        }
    }

    private boolean shouldUseRanged(Mob mob, LivingEntity target) {
        double distSqr = mob.distanceToSqr(target);
        if (distSqr < RANGED_MIN_DIST * RANGED_MIN_DIST) {
            return false;
        }

        ItemStack main = mob.getMainHandItem();
        ItemStack off = mob.getOffhandItem();
        boolean hasRangedWeapon =
            isProjectileWeapon(main) || isProjectileWeapon(off);

        if (!hasRangedWeapon) {
            return false;
        }

        // 简单的弹药检查（更严谨的在 Action 内部，这里做快速预判）
        // 如果两手都没有弹药且背包也没有，就不切远程
        // 为简化性能，这里只假设有武器就有弹药，让 Action 去处理由失败触发的切换
        // 或者做一次基础检查
        return hasAmmoForWeapon(mob, main) || hasAmmoForWeapon(mob, off);
    }

    private boolean isProjectileWeapon(ItemStack stack) {
        return (
            !stack.isEmpty() && stack.getItem() instanceof ProjectileWeaponItem
        );
    }

    private boolean hasAmmoForWeapon(Mob mob, ItemStack weapon) {
        if (!isProjectileWeapon(weapon)) return false;
        ProjectileWeaponItem gun = (ProjectileWeaponItem) weapon.getItem();
        Predicate<ItemStack> ammoPredicate = gun.getAllSupportedProjectiles();
        // 检查另一只手
        ItemStack other = weapon == mob.getMainHandItem()
            ? mob.getOffhandItem()
            : mob.getMainHandItem();
        if (ammoPredicate.test(other)) return true;
        // 简单检查背包（不遍历整个 inventory，只依赖原版 getProjectile 逻辑可能不够，
        // 但这里为了不引入过多依赖，假设 Action 会处理失败）
        return !mob.getProjectile(weapon).isEmpty();
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        if (attackAction != null) {
            attackAction.stop(mind, entity);
            attackAction = null;
        }
        targetUuid = null;
    }

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        if (!canEngage(mind, entity)) {
            return true;
        }
        LivingEntity target = resolveTarget(entity);
        return (
            target == null ||
            !target.isAlive() ||
            !isTargetWeak(entity, target) ||
            entity.distanceTo(target) > SEARCH_RADIUS
        );
    }

    @Override
    public String getName() {
        return "hunt";
    }

    /**
     * 判断当前是否处于安全且适合主动狩猎的状态。
     */
    private boolean canEngage(INpcMind mind, LivingEntity entity) {
        if (entity.getHealth() <= 0 || entity.isDeadOrDying()) {
            return false;
        }
        float healthRatio = entity.getHealth() / entity.getMaxHealth();
        if (healthRatio < SAFE_HEALTH_RATIO) {
            return false;
        }
        // 若存在威胁记忆或正在逃跑，避免进入猎杀状态
        return !mind.getMemory().hasMemory("threat_detected") &&
            !mind.getMemory().hasMemory(WorldStateKeys.IN_DANGER) &&
            !mind.getMemory().hasMemory("is_fleeing");
    }

    /**
     * 保障有有效的目标，并缓存优势比。
     */
    private LivingEntity ensureTarget(LivingEntity hunter) {
        LivingEntity current = resolveTarget(hunter);
        if (
            current != null &&
            isTargetWeak(hunter, current) &&
            hunter.distanceTo(current) <= SEARCH_RADIUS
        ) {
            cachedAdvantageRatio = computeAdvantageRatio(hunter, current);
            return current;
        }
        LivingEntity selected = selectWeakTarget(hunter);
        targetUuid = selected == null ? null : selected.getUUID();
        if (selected != null) {
            cachedAdvantageRatio = computeAdvantageRatio(hunter, selected);
        } else {
            cachedAdvantageRatio = 0.0D;
        }
        return selected;
    }

    private LivingEntity resolveTarget(LivingEntity hunter) {
        if (targetUuid == null || !(hunter.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        Entity resolved = serverLevel.getEntity(targetUuid);
        return resolved instanceof LivingEntity living ? living : null;
    }

    /**
     * 在搜索范围内选择最弱且最近的合法目标。
     */
    private LivingEntity selectWeakTarget(LivingEntity hunter) {
        if (!(hunter.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        double hunterScore = computeStrengthScore(hunter);
        AABB box = hunter.getBoundingBox().inflate(SEARCH_RADIUS);

        return serverLevel
            .getEntitiesOfClass(LivingEntity.class, box, target -> isCandidate(hunter, target))
            .stream()
            .map(target ->
                new TargetCandidate(
                    target,
                    hunterScore,
                    computeStrengthScore(target),
                    hunter.distanceToSqr(target)
                )
            )
            .filter(candidate -> candidate.isWeakerThanHunter())
            .max(Comparator.comparingDouble(TargetCandidate::score))
            .map(TargetCandidate::entity)
            .orElse(null);
    }

    private boolean isCandidate(LivingEntity hunter, LivingEntity target) {
        if (hunter == target || !target.isAlive()) {
            return false;
        }
        if (EntityRelationUtil.isAlly(hunter, target)) {
            return false;
        }
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }
        if (target.isInvisible()) {
            return false;
        }
        return hunter.distanceTo(target) <= SEARCH_RADIUS;
    }

    /**
     * 目标强度需明显低于猎手。
     */
    private boolean isTargetWeak(LivingEntity hunter, LivingEntity target) {
        double hunterScore = computeStrengthScore(hunter);
        double targetScore = computeStrengthScore(target);
        return targetScore > 0 && targetScore <= hunterScore * WEAKNESS_THRESHOLD;
    }

    private double computeStrengthScore(LivingEntity entity) {
        double attack = getAttributeValue(entity, Attributes.ATTACK_DAMAGE);
        double health = entity.getMaxHealth();
        double armor = getAttributeValue(entity, Attributes.ARMOR);
        return health + attack * 2.0D + armor;
    }

    private double computeAdvantageRatio(LivingEntity hunter, LivingEntity target) {
        double hunterScore = computeStrengthScore(hunter);
        double targetScore = computeStrengthScore(target);
        if (hunterScore <= 0) {
            return 0.0D;
        }
        double gap = hunterScore - targetScore;
        return Math.max(0.0D, gap / hunterScore);
    }

    private double getAttributeValue(
        LivingEntity entity,
        Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute
    ) {
        var instance = entity.getAttribute(attribute);
        return instance == null ? 0.0D : instance.getValue();
    }

    /**
     * 目标候选包装，便于按优势与距离综合排序。
     */
    private record TargetCandidate(
        LivingEntity entity,
        double hunterScore,
        double targetScore,
        double distanceSqr
    ) {
        boolean isWeakerThanHunter() {
            return targetScore > 0 && targetScore <= hunterScore * WEAKNESS_THRESHOLD;
        }

        double score() {
            double advantageRatio = Math.max(0.0D, (hunterScore - targetScore) / hunterScore);
            double distancePenalty = Math.sqrt(distanceSqr) * DISTANCE_WEIGHT;
            return advantageRatio - distancePenalty;
        }
    }
}
