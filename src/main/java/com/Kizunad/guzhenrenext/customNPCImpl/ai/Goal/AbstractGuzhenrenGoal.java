package com.Kizunad.guzhenrenext.customNPCImpl.ai.Goal;

import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.guzhenrenext.guzhenrenBridge.EntityHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 蛊真人模组目标基类。
 * <p>
 * 提供与蛊真人模组相关的通用目标辅助方法和框架。
 */
public abstract class AbstractGuzhenrenGoal implements IGoal {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        AbstractGuzhenrenGoal.class
    );
    private final String name;

    protected AbstractGuzhenrenGoal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    // --- 蛊真人特定辅助方法 ---
    /**
     * 检查实体是否为蛊师（例如，是否拥有空窍Capability）。
     */
    protected boolean isGuMaster(LivingEntity entity) {
        return EntityHelper.isGuMaster(entity);
    }

    /**
     * 获取实体的真元百分比。
     */
    protected float getPrimevalEssencePercentage(LivingEntity entity) {
        return ZhenYuanHelper.getPercentage(entity);
    }

    // --- IGoal 默认实现 (子类可覆盖) ---
    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        return 0.0f; // 默认低优先级，子类实现具体逻辑
    }

    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        return isGuMaster(entity); // 默认只有蛊师才能运行蛊真人目标
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        LOGGER.debug("[{}] Goal 启动。", getName());
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        // 默认无操作，子类实现具体行为
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        LOGGER.debug("[{}] Goal 停止。", getName());
    }

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        return false; // 默认目标不会自动结束，由子类控制完成条件
    }
}
