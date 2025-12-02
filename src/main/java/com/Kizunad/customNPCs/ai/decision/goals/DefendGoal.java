package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.common.AttackAction;
import com.Kizunad.customNPCs.ai.actions.common.BlockWithShieldAction;
import com.Kizunad.customNPCs.ai.actions.common.RangedAttackItemAction;
import com.Kizunad.customNPCs.ai.config.NpcCombatDefaults;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 防御Goal - 当被攻击且有能力时进行防御反击
 * <p>
 * 使用标准动作: {@link AttackAction}
 * <p>
 * 优先级: 中高
 * 触发条件: 受到攻击 且 攻击力 > 0
 */
public class DefendGoal implements IGoal {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        DefendGoal.class
    );

    private static final int DEFEND_MEMORY_DURATION = 60; // 记忆最近一次受击的持续时间（以 tick 为单位），60 tick ≈ 3 秒
    private static final double MIN_ATTACK_DAMAGE = 1.0; // 触发防御逻辑所需的最小攻击伤害阈值
    private static final float PRIORITY_RECENT_HIT = 0.7f; // 最近一次受击在防御优先级计算中的权重
    private static final float PRIORITY_MEMORY = 0.65f; // 记忆中受击事件在防御优先级计算中的权重
    private static final int DEFEND_COOLDOWN_TICKS = 80; // 防御动作后的冷却时间（80 tick），防止频繁重复进入防御状态
    private static final double RANGED_TRIGGER_MIN = 10.0d; // 进入远程模式的最小距离（格）
    private static final double BLOCK_RANGE_MAX = 3.5d; // NPC 进行格挡的最大有效距离（以方块为单位）

    private AttackAction attackAction = null; // 当前正在执行的近战攻击动作
    private RangedAttackItemAction rangedAction = null; // 当前正在执行的远程攻击动作
    private BlockWithShieldAction blockAction = null; // 当前正在执行的格挡动作
    private UUID targetUuid = null; // 当前目标实体的 UUID
    private Mode activeMode = Mode.NONE; // 当前活动的防御模式

    private enum Mode {
        NONE,
        MELEE,
        RANGED,
        BLOCK,
    }

    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        // 如果最近受到攻击且有反击能力
        if (entity.hurtTime > 0 && canDefend(entity)) {
            // 中高优先级，但低于逃跑
            return PRIORITY_RECENT_HIT;
        }

        // 如果Memory中记录了攻击者
        if (
            mind.getMemory().hasMemory("last_attacker") ||
            mind.getMemory().hasMemory("current_threat_id")
        ) {
            return PRIORITY_MEMORY;
        }

        return 0.0f;
    }

    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        // 最近受到攻击或Memory中有攻击者，且有反击能力，且未处于防御冷却
        boolean wasHurt =
            entity.hurtTime > 0 ||
            mind.getMemory().hasMemory("last_attacker") ||
            mind.getMemory().hasMemory("current_threat_id");
        boolean onCooldown = mind.getMemory().hasMemory("defend_cooldown");
        return wasHurt && canDefend(entity) && !onCooldown;
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        // 设置防御状态为true
        mind
            .getMemory()
            .rememberShortTerm("is_defending", true, DEFEND_MEMORY_DURATION);

        // 尝试从记忆/最近受击获取攻击者
        targetUuid = resolveTargetUuid(mind, entity);

        LOGGER.info(
            "[DefendGoal] {} 开始防御 | 目标: {}",
            entity.getName().getString(),
            targetUuid != null ? targetUuid : "未知"
        );
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        if (targetUuid == null) {
            targetUuid = resolveTargetUuid(mind, entity);
            if (targetUuid == null) {
                return;
            }
        }

        double distance = getTargetDistance(entity);
        if (distance < 0) {
            stopAllActions(mind, entity);
            targetUuid = null;
            return;
        }

        if (shouldUseRanged(mind, entity, distance)) {
            switchToRanged(mind, entity);
        } else if (shouldBlock(mind, entity, distance)) {
            switchToBlock(mind, entity);
        } else {
            switchToMelee(mind, entity);
        }

        tickActiveAction(mind, entity);
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        mind.getMemory().forget("is_defending");
        mind
            .getMemory()
            .rememberShortTerm("defend_cooldown", true, DEFEND_COOLDOWN_TICKS);

        stopAllActions(mind, entity);
        targetUuid = null;

        // 若已脱战，则清理威胁短期记忆
        if (!isUnderThreat(mind, entity)) {
            clearThreatMemory(mind);
        }

        LOGGER.info("[DefendGoal] {} 停止防御", entity.getName().getString());
    }

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        // 不再受到威胁或目标已失效
        boolean noThreat =
            entity.hurtTime == 0 &&
            !mind.getMemory().hasMemory("last_attacker") &&
            !mind.getMemory().hasMemory("current_threat_id");
        boolean targetGone = targetUuid == null;

        return noThreat || targetGone;
    }

    @Override
    public String getName() {
        return "defend";
    }

    /**
     * 检查实体是否有防御能力（攻击力 > 0）
     */
    private boolean canDefend(LivingEntity entity) {
        // 部分实体（如村民）缺少攻击属性，需空值保护
        var attr = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attr == null) {
            LOGGER.warn(
                "[DefendGoal] {} 缺少 attack_damage 属性，视为不可防御",
                entity.getName().getString()
            );
            return false;
        }
        double attackDamage = attr.getValue();
        return attackDamage >= MIN_ATTACK_DAMAGE;
    }

    /**
     * 根据 NPC 的记忆与实体的状态解析并返回目标 UUID。
     *
     * <p>优先级顺序如下：</p>
     * <ol>
     *   <li>若记忆中存在 {@code current_threat_id} 并且是 {@link java.util.UUID}，则返回该值。</li>
     *   <li>若记忆中存在 {@code last_attacker} 并且是 {@link java.util.UUID}，则返回该值。</li>
     *   <li>若实体最近受到攻击，返回攻击者的 UUID。</li>
     *   <li>若以上情况均不存在，则返回 {@code null}。</li>
     * </ol>
     *
     * @param mind NPC 的思维模块，用于读取记忆。
     * @param entity 当前目标实体，用于获取最近攻击者信息。
     * @return 解析得到的目标 UUID，若无可用信息则返回 {@code null}。
     */
    private UUID resolveTargetUuid(INpcMind mind, LivingEntity entity) {
        Object threatId = mind.getMemory().getMemory("current_threat_id");
        if (threatId instanceof UUID uuid) {
            return uuid;
        }

        Object attackerUuid = mind.getMemory().getMemory("last_attacker");
        if (attackerUuid instanceof UUID uuid) {
            return uuid;
        }

        if (entity.getLastHurtByMob() != null) {
            return entity.getLastHurtByMob().getUUID();
        }
        return null;
    }

    private double getTargetDistance(LivingEntity entity) {
        if (targetUuid == null) {
            return -1.0d;
        }
        if (entity.level() instanceof ServerLevel serverLevel) {
            Entity target = serverLevel.getEntity(targetUuid);
            if (target instanceof LivingEntity living) {
                return entity.distanceTo(living);
            }
        }
        return -1.0d;
    }

    private boolean hasRangedWeapon(INpcMind mind, LivingEntity entity) {
        Object hasRanged = mind
            .getCurrentWorldState(entity)
            .getState(WorldStateKeys.HAS_RANGED_WEAPON);
        return Boolean.TRUE.equals(hasRanged);
    }

    private boolean shouldUseRanged(
        INpcMind mind,
        LivingEntity entity,
        double distance
    ) {
        return distance >= RANGED_TRIGGER_MIN && hasRangedWeapon(mind, entity);
    }

    private boolean shouldBlock(
        INpcMind mind,
        LivingEntity entity,
        double distance
    ) {
        boolean inCooldown = mind.getMemory().hasMemory("block_cooldown");
        return (
            !inCooldown &&
            distance > 0 &&
            distance <= BLOCK_RANGE_MAX &&
            hasShield(entity)
        );
    }

    private boolean hasShield(LivingEntity entity) {
        return (
            entity.getMainHandItem().is(Items.SHIELD) ||
            entity.getOffhandItem().is(Items.SHIELD)
        );
    }

    private void switchToRanged(INpcMind mind, LivingEntity entity) {
        if (activeMode == Mode.RANGED) {
            return;
        }
        stopAllActions(mind, entity);
        rangedAction = new RangedAttackItemAction(targetUuid);
        rangedAction.start(mind, entity);
        activeMode = Mode.RANGED;
        LOGGER.debug("[DefendGoal] 切换为远程反击");
    }

    private void switchToBlock(INpcMind mind, LivingEntity entity) {
        if (activeMode == Mode.BLOCK) {
            return;
        }
        stopAllActions(mind, entity);
        blockAction = new BlockWithShieldAction(
            NpcCombatDefaults.SHIELD_MIN_RAISE_TICKS
        );
        blockAction.start(mind, entity);
        activeMode = Mode.BLOCK;
        LOGGER.debug("[DefendGoal] 切换为格挡");
    }

    private void switchToMelee(INpcMind mind, LivingEntity entity) {
        if (activeMode == Mode.MELEE) {
            return;
        }
        stopAllActions(mind, entity);
        attackAction = new AttackAction(targetUuid);
        attackAction.start(mind, entity);
        activeMode = Mode.MELEE;
        LOGGER.debug("[DefendGoal] 切换为近战");
    }

    private void tickActiveAction(INpcMind mind, LivingEntity entity) {
        if (activeMode == Mode.MELEE && attackAction != null) {
            ActionStatus status = attackAction.tick(mind, entity);
            if (status == ActionStatus.FAILURE) {
                attackAction = null;
                activeMode = Mode.NONE;
            }
        } else if (activeMode == Mode.RANGED && rangedAction != null) {
            ActionStatus status = rangedAction.tick(mind, entity);
            if (status == ActionStatus.FAILURE) {
                rangedAction = null;
                activeMode = Mode.NONE;
            }
        } else if (activeMode == Mode.BLOCK && blockAction != null) {
            ActionStatus status = blockAction.tick(mind, entity);
            if (status != ActionStatus.RUNNING) {
                blockAction = null;
                activeMode = Mode.NONE;
                mind
                    .getMemory()
                    .rememberShortTerm(
                        "block_cooldown",
                        true,
                        NpcCombatDefaults.SHIELD_COOLDOWN_TICKS
                    );
            }
        }
    }

    private void stopAllActions(INpcMind mind, LivingEntity entity) {
        if (attackAction != null) {
            attackAction.stop(mind, entity);
            attackAction = null;
        }
        if (rangedAction != null) {
            rangedAction.stop(mind, entity);
            rangedAction = null;
        }
        if (blockAction != null) {
            blockAction.stop(mind, entity);
            blockAction = null;
        }
        activeMode = Mode.NONE;
    }

    private boolean isUnderThreat(INpcMind mind, LivingEntity entity) {
        return (
            entity.hurtTime > 0 ||
            mind.getMemory().hasMemory("last_attacker") ||
            mind.getMemory().hasMemory("current_threat_id")
        );
    }

    private void clearThreatMemory(INpcMind mind) {
        mind.getMemory().forget("threat_detected");
        mind.getMemory().forget("current_threat_id");
        mind.getMemory().forget("last_attacker");
        mind.getMemory().forget(WorldStateKeys.TARGET_VISIBLE);
        mind.getMemory().forget(WorldStateKeys.TARGET_IN_RANGE);
        mind.getMemory().forget(WorldStateKeys.DISTANCE_TO_TARGET);
        mind.getMemory().forget(WorldStateKeys.HOSTILE_NEARBY);
    }
}
