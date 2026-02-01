package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeConfig;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeManager;
import net.minecraft.server.level.ServerLevel;

/**
 * 威胁值服务。
 * <p>
 * Round 19：提供威胁值累积、衰减与等级划分，供孵化巢/事件等模块使用。
 * </p>
 */
public final class BastionThreatService {

    private BastionThreatService() {
    }

    /** 威胁等级枚举（0-3）。 */
    public enum ThreatTier {
        NONE,
        LOW,
        MEDIUM,
        HIGH;
    }

    /**
     * 增加威胁值并落盘。
     *
     * @param level   服务端世界
     * @param bastion 基地数据
     * @param amount  增量（<0 将视为 0）
     */
    public static void addThreat(ServerLevel level, BastionData bastion, int amount) {
        if (level == null || bastion == null) {
            return;
        }
        int delta = Math.max(0, amount);
        BastionTypeConfig typeConfig = BastionTypeManager.getOrDefault(bastion.bastionType());
        BastionTypeConfig.ThreatConfig threatConfig = typeConfig.threat();
        if (threatConfig == null || !threatConfig.enabled()) {
            return;
        }

        int current = bastion.threatMeter();
        int next = Math.min(threatConfig.maxThreat(), current + delta);
        if (next == current) {
            return;
        }

        BastionSavedData savedData = BastionSavedData.get(level);
        savedData.updateBastion(bastion.withThreatMeter(next));
    }

    /**
     * 对威胁值进行衰减（按配置间隔与步长）。
     *
     * @param level   服务端世界
     * @param bastion 基地数据
     * @param gameTime 当前游戏时间（用于判断衰减节流）
     */
    public static void decayThreat(ServerLevel level, BastionData bastion, long gameTime) {
        if (level == null || bastion == null) {
            return;
        }
        BastionTypeConfig typeConfig = BastionTypeManager.getOrDefault(bastion.bastionType());
        BastionTypeConfig.ThreatConfig threatConfig = typeConfig.threat();
        if (threatConfig == null || !threatConfig.enabled()) {
            return;
        }

        int interval = Math.max(1, threatConfig.decayInterval());
        if (gameTime % interval != 0) {
            return;
        }

        int current = bastion.threatMeter();
        if (current <= 0) {
            return;
        }

        int decayed = Math.max(0, current - Math.max(0, threatConfig.decayPerTick()));
        if (decayed == current) {
            return;
        }

        BastionSavedData savedData = BastionSavedData.get(level);
        savedData.updateBastion(bastion.withThreatMeter(decayed));
    }

    /**
     * 计算威胁等级（0-3）。
     *
     * @param bastion 基地数据
     * @return 威胁等级
     */
    public static ThreatTier getThreatTier(BastionData bastion) {
        if (bastion == null) {
            return ThreatTier.NONE;
        }
        BastionTypeConfig typeConfig = BastionTypeManager.getOrDefault(bastion.bastionType());
        BastionTypeConfig.ThreatConfig threatConfig = typeConfig.threat();
        if (threatConfig == null || !threatConfig.enabled()) {
            return ThreatTier.NONE;
        }

        int value = bastion.threatMeter();
        if (value >= threatConfig.highThreshold()) {
            return ThreatTier.HIGH;
        }
        if (value >= threatConfig.mediumThreshold()) {
            return ThreatTier.MEDIUM;
        }
        if (value >= threatConfig.lowThreshold()) {
            return ThreatTier.LOW;
        }
        return ThreatTier.NONE;
    }
}
