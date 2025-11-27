package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.common.AttackAction;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * 防御Goal - 当被攻击且有能力时进行防御反击
 * <p>
 * 使用标准动作: {@link AttackAction}
 * <p>
 * 优先级: 中高
 * 触发条件: 受到攻击 且 攻击力 > 0
 */
public class DefendGoal implements IGoal {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefendGoal.class);

    private static final int DEFEND_MEMORY_DURATION = 60; // 3秒记忆
    private static final double MIN_ATTACK_DAMAGE = 1.0; // 最小攻击力要求
    private static final float PRIORITY_RECENT_HIT = 0.7f;
    private static final float PRIORITY_MEMORY = 0.65f;

    private AttackAction attackAction = null;
    private UUID targetUuid = null;

    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        // 如果最近受到攻击且有反击能力
        if (entity.hurtTime > 0 && canDefend(entity)) {
            // 中高优先级，但低于逃跑
            return PRIORITY_RECENT_HIT;
        }
        
        // 如果Memory中记录了攻击者
        if (mind.getMemory().hasMemory("last_attacker")) {
            return PRIORITY_MEMORY;
        }

        return 0.0f;
    }

    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        // 最近受到攻击或Memory中有攻击者，且有反击能力
        boolean wasHurt = entity.hurtTime > 0 || mind.getMemory().hasMemory("last_attacker");
        return wasHurt && canDefend(entity);
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        mind.getMemory().rememberShortTerm(
            "is_defending",
            true,
            DEFEND_MEMORY_DURATION
        );

        // 尝试从Memory获取攻击者
        Object attackerUuid = mind.getMemory().getMemory("last_attacker");
        if (attackerUuid instanceof UUID uuid) {
            targetUuid = uuid;
        } else if (entity.getLastHurtByMob() != null) {
            targetUuid = entity.getLastHurtByMob().getUUID();
        }

        LOGGER.info(
            "[DefendGoal] {} 开始防御 | 目标: {}",
            entity.getName().getString(),
            targetUuid != null ? targetUuid : "未知"
        );
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        // 如果没有攻击动作，创建一个
        if (attackAction == null && targetUuid != null) {
            attackAction = new AttackAction(targetUuid);
            attackAction.start(mind, entity);
            LOGGER.debug("[DefendGoal] 创建攻击动作");
        }

        // 执行攻击
        if (attackAction != null) {
            ActionStatus status = attackAction.tick(mind, entity);
            if (status == ActionStatus.SUCCESS) {
                LOGGER.info("[DefendGoal] 攻击成功");
                // 继续攻击，直到目标不再威胁或Goal结束
            } else if (status == ActionStatus.FAILURE) {
                LOGGER.warn("[DefendGoal] 攻击失败");
                attackAction = null;
            }
        }
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        mind.getMemory().forget("is_defending");
        
        if (attackAction != null) {
            attackAction.stop(mind, entity);
            attackAction = null;
        }
        targetUuid = null;

        LOGGER.info("[DefendGoal] {} 停止防御", entity.getName().getString());
    }

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        // 不再受到威胁或目标已失效
        boolean noThreat = entity.hurtTime == 0 && !mind.getMemory().hasMemory("last_attacker");
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
        double attackDamage = entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
        return attackDamage >= MIN_ATTACK_DAMAGE;
    }
}
