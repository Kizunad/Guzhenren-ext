package com.Kizunad.guzhenrenext.bastion.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

/**
 * 数据包配置：基地破坏时间。
 */
public record BastionBreakingConfigData(
    List<TierSeconds> coreSecondsByTier,
    List<TierSeconds> nodeSecondsByTier
) {

    public static final Codec<BastionBreakingConfigData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            TierSeconds.CODEC.listOf().optionalFieldOf("core_seconds_by_tier", List.of())
                .forGetter(BastionBreakingConfigData::coreSecondsByTier),
            TierSeconds.CODEC.listOf().optionalFieldOf("node_seconds_by_tier", List.of())
                .forGetter(BastionBreakingConfigData::nodeSecondsByTier)
        ).apply(instance, BastionBreakingConfigData::new)
    );

    public record TierSeconds(int tier, int seconds) {
        public static final Codec<TierSeconds> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.INT.fieldOf("tier").forGetter(TierSeconds::tier),
                Codec.INT.fieldOf("seconds").forGetter(TierSeconds::seconds)
            ).apply(instance, TierSeconds::new)
        );
    }
}
