package com.Kizunad.guzhenrenext.bastion.config;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.util.Mth;

/**
 * 基地破坏时间配置（按转数）。
 * <p>
 * 目标：让基地核心/节点的“可破坏时间”可由数据包配置，而不是只能靠硬度与工具体验。
 * </p>
 * <p>
 * 设计：
 * <ul>
 *   <li>使用 tier->seconds 映射（秒）。</li>
 *   <li>运行时按转数查表：若未配置，则取最接近的低转条目；仍无则回退默认。</li>
 * </ul>
 * </p>
 */
public final class BastionBreakingConfig {

    private BastionBreakingConfig() {
    }

    private static final class Defaults {
        static final int TIER_1 = 1;
        static final int TIER_4 = 4;
        static final int TIER_9 = 9;

        // 默认目标：1转 3s；4转 30s；9转 180s
        static final int DEFAULT_CORE_SECONDS_TIER_1 = 3;
        static final int DEFAULT_NODE_SECONDS_TIER_1 = 1;

        static final int DEFAULT_CORE_SECONDS_TIER_4 = 30;
        static final int DEFAULT_NODE_SECONDS_TIER_4 = 8;

        static final int DEFAULT_CORE_SECONDS_TIER_9 = 180;
        static final int DEFAULT_NODE_SECONDS_TIER_9 = 45;

        static final int MIN_SECONDS = 1;
        static final int MAX_SECONDS = 60 * 60;

        private Defaults() {
        }
    }

    /** 核心破坏秒数：tier -> seconds。 */
    private static final Map<Integer, Integer> CORE_SECONDS = new ConcurrentHashMap<>();
    /** 节点破坏秒数：tier -> seconds。 */
    private static final Map<Integer, Integer> NODE_SECONDS = new ConcurrentHashMap<>();

    public static void clear() {
        CORE_SECONDS.clear();
        NODE_SECONDS.clear();
        loadDefaults();
    }

    public static void apply(BastionBreakingConfigData data) {
        clear();
        if (data == null) {
            return;
        }

        applyEntries(CORE_SECONDS, data.coreSecondsByTier());
        applyEntries(NODE_SECONDS, data.nodeSecondsByTier());
    }

    public static int getCoreSeconds(int tier) {
        return getSeconds(CORE_SECONDS, tier, Defaults.DEFAULT_CORE_SECONDS_TIER_1);
    }

    public static int getNodeSeconds(int tier) {
        return getSeconds(NODE_SECONDS, tier, Defaults.DEFAULT_NODE_SECONDS_TIER_1);
    }

    private static int getSeconds(Map<Integer, Integer> map, int tier, int fallback) {
        int safeTier = Math.max(1, tier);
        Integer direct = map.get(safeTier);
        if (direct != null) {
            return direct;
        }

        // 回退：取小于等于 safeTier 的最大 tier 条目
        int bestTier = -1;
        int bestValue = fallback;
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            int key = entry.getKey();
            if (key <= safeTier && key > bestTier) {
                bestTier = key;
                bestValue = entry.getValue();
            }
        }
        return bestValue;
    }

    private static void applyEntries(
        Map<Integer, Integer> target,
        List<BastionBreakingConfigData.TierSeconds> entries
    ) {
        if (entries == null) {
            return;
        }
        for (BastionBreakingConfigData.TierSeconds entry : entries) {
            if (entry == null) {
                continue;
            }
            int tier = Math.max(1, entry.tier());
            int seconds = Mth.clamp(entry.seconds(), Defaults.MIN_SECONDS, Defaults.MAX_SECONDS);
            target.put(tier, seconds);
        }
    }

    private static void loadDefaults() {
        CORE_SECONDS.put(Defaults.TIER_1, Defaults.DEFAULT_CORE_SECONDS_TIER_1);
        CORE_SECONDS.put(Defaults.TIER_4, Defaults.DEFAULT_CORE_SECONDS_TIER_4);
        CORE_SECONDS.put(Defaults.TIER_9, Defaults.DEFAULT_CORE_SECONDS_TIER_9);

        NODE_SECONDS.put(Defaults.TIER_1, Defaults.DEFAULT_NODE_SECONDS_TIER_1);
        NODE_SECONDS.put(Defaults.TIER_4, Defaults.DEFAULT_NODE_SECONDS_TIER_4);
        NODE_SECONDS.put(Defaults.TIER_9, Defaults.DEFAULT_NODE_SECONDS_TIER_9);
    }
}
