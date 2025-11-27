package com.Kizunad.customNPCs.ai.actions.goap;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.common.AttackAction;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * GOAP 攻击动作 - 包装 AttackAction 添加GOAP规划信息
 * <p>
 * 前置条件:
 * - target_visible: true (目标可见)
 * - target_in_range: true (目标在攻击范围内)
 * <p>
 * 效果:
 * - target_damaged: true (目标受到伤害)
 * - attack_cooldown_active: true (攻击冷却激活)
 * <p>
 * 代价: 2.0 (需要接近目标，有一定风险)
 */
public class GoapAttackAction implements IGoapAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoapAttackAction.class);

    private static final int COOLDOWN_MEMORY_DURATION_TICKS = 40;
    private static final int ATTACK_TIME_MEMORY_DURATION_TICKS = 100;

    private final WorldState preconditions;
    private final WorldState effects;
    private final AttackAction wrappedAction;
    private final float cost;

    /**
     * 创建GOAP攻击动作（使用默认参数）
     * @param targetUuid 目标实体UUID
     */
    public GoapAttackAction(UUID targetUuid) {
        this(targetUuid, 2.0f);
    }

    /**
     * 创建GOAP攻击动作（指定代价）
     * @param targetUuid 目标实体UUID
     * @param cost 动作代价
     */
    public GoapAttackAction(UUID targetUuid, float cost) {
        this.wrappedAction = new AttackAction(targetUuid);
        this.cost = cost;

        // 前置条件：目标可见且在范围内
        this.preconditions = new WorldState();
        this.preconditions.setState(WorldStateKeys.TARGET_VISIBLE, true);
        this.preconditions.setState(WorldStateKeys.TARGET_IN_RANGE, true);

        // 效果：目标受到伤害，冷却激活
        this.effects = new WorldState();
        this.effects.setState(WorldStateKeys.TARGET_DAMAGED, true);
        this.effects.setState(WorldStateKeys.ATTACK_COOLDOWN_ACTIVE, true);
    }

    @Override
    public WorldState getPreconditions() {
        return preconditions;
    }

    @Override
    public WorldState getEffects() {
        return effects;
    }

    @Override
    public float getCost() {
        return cost;
    }

    @Override
    public ActionStatus tick(INpcMind mind, LivingEntity entity) {
        ActionStatus status = wrappedAction.tick(mind, entity);

        // 成功时更新Memory状态
        if (status == ActionStatus.SUCCESS) {
            mind.getMemory().rememberLongTerm(WorldStateKeys.TARGET_DAMAGED, true);
            mind.getMemory().rememberShortTerm(
                WorldStateKeys.ATTACK_COOLDOWN_ACTIVE,
                true,
                COOLDOWN_MEMORY_DURATION_TICKS
            );
            mind.getMemory().rememberShortTerm(
                "last_attack_time",
                System.currentTimeMillis(),
                ATTACK_TIME_MEMORY_DURATION_TICKS
            );

            LOGGER.info("[GoapAttackAction] 攻击成功，已更新Memory状态");
        }

        return status;
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        wrappedAction.start(mind, entity);
        LOGGER.debug("[GoapAttackAction] 开始执行");
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        wrappedAction.stop(mind, entity);
        LOGGER.debug("[GoapAttackAction] 停止执行");
    }

    @Override
    public boolean canInterrupt() {
        return wrappedAction.canInterrupt();
    }

    @Override
    public String getName() {
        return "goap_attack";
    }
}
