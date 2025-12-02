package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.common.AttackAction;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.util.EntityRelationUtil;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.UUID;

/**
 * 猎杀弱小生物的 Goal。
 * <p>
 * 逻辑：在安全状态下寻找比自身弱小的非友方实体，使用 {@link AttackAction} 主动猎杀。
 * 优先级：中等，低于逃跑/防御等保命类目标。
 */
public class HuntGoal implements IGoal {

    private static final Logger LOGGER = LoggerFactory.getLogger(HuntGoal.class);
    private static final double SEARCH_RADIUS = 12.0D;
    private static final double WEAKNESS_THRESHOLD = 0.8D; // 目标综合评分需低于此比例
    private static final float BASE_PRIORITY = 0.35f; // 明确低于 Flee/Defend
    private static final float PRIORITY_GAIN = 0.25f;
    private static final float SAFE_HEALTH_RATIO = 0.6f; // 血量低于此值不主动出击
    private static final double DISTANCE_WEIGHT = 0.05D; // 轻度偏好近目标

    private AttackAction attackAction;
    private UUID targetUuid;
    private double cachedAdvantageRatio = 0.0D;

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
            attackAction = new AttackAction(targetUuid);
            attackAction.start(mind, entity);
            LOGGER.info(
                "[HuntGoal] {} 开始猎杀目标 {}",
                entity.getName().getString(),
                target.getName().getString()
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

        if (attackAction == null) {
            attackAction = new AttackAction(target.getUUID());
            attackAction.start(mind, entity);
        }

        ActionStatus status = attackAction.tick(mind, entity);
        if (status == ActionStatus.SUCCESS) {
            // 攻击命中后，若目标仍存活则准备下一次攻击
            if (target.isAlive()) {
                attackAction.stop(mind, entity);
                attackAction = null;
            }
        } else if (status == ActionStatus.FAILURE) {
            LOGGER.debug("[HuntGoal] 攻击失败，重新选择目标");
            attackAction = null;
            targetUuid = null;
        }
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
