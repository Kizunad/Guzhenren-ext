package com.Kizunad.guzhenrenext.bastion.threat.impl;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionParticles;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.BastionSoundPlayer;
import com.Kizunad.guzhenrenext.bastion.threat.IThreatEvent;
import com.Kizunad.guzhenrenext.bastion.threat.ThreatEventContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 扩张涌动威胁事件。
 * <p>
 * 效果：临时大幅增加基地的资源池，加速节点扩张。
 * 这是一种"反击"机制，让玩家拆除节点后基地反而更快扩张。
 * </p>
 */
public final class ExpansionSurgeEvent implements IThreatEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpansionSurgeEvent.class);

    /** 事件 ID。 */
    private static final String ID = "expansion_surge";

    /** 基础权重。 */
    private static final int BASE_WEIGHT = 25;

    /** 基础资源池注入量。 */
    private static final double BASE_POOL_INJECTION = 50.0;

    /** 每转增加的资源池注入量。 */
    private static final double POOL_PER_TIER = 25.0;

    /** 节点损失比例对注入量的加成系数。 */
    private static final double LOSS_RATIO_MULTIPLIER = 2.0;

    /** 最低触发转数（低转基地不触发此事件）。 */
    private static final int MIN_TIER = 2;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public int getBaseWeight() {
        return BASE_WEIGHT;
    }

    @Override
    public boolean canTrigger(ThreatEventContext context) {
        // 最低 2 转才触发
        if (context.bastion().tier() < MIN_TIER) {
            return false;
        }
        // 需要有节点损失
        return context.nodeCountAfter() < context.nodeCountBefore();
    }

    @Override
    public void execute(ThreatEventContext context) {
        int tier = context.bastion().tier();
        double lossRatio = context.getNodeLossRatio();

        // 计算注入量：基础 + 转数加成 + 损失比例加成
        double injection = BASE_POOL_INJECTION + (tier - 1) * POOL_PER_TIER;
        injection *= (1.0 + lossRatio * LOSS_RATIO_MULTIPLIER);

        LOGGER.debug("执行扩张涌动: tier={}, lossRatio={}, injection={}",
            tier, lossRatio, injection);

        // 播放音效和粒子
        BastionSoundPlayer.playThreat(context.level(), context.getCorePos());
        BastionParticles.spawnExpansionSurgeParticles(
            context.level(), context.getCorePos(), context.bastion().primaryDao());

        // 更新资源池
        BastionSavedData savedData = BastionSavedData.get(context.level());
        BastionData current = savedData.getBastion(context.bastion().id());

        if (current != null) {
            double newPool = current.resourcePool() + injection;
            BastionData updated = current.withResourcePool(newPool);
            savedData.updateBastion(updated);

            LOGGER.info("扩张涌动: 基地 {} 资源池 {} -> {} (+{})",
                current.id(), current.resourcePool(), newPool, injection);
        }
    }
}
