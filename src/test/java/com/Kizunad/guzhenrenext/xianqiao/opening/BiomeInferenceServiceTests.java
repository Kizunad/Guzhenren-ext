package com.Kizunad.guzhenrenext.xianqiao.opening;

import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BiomeInferenceServiceTests {

    private static final double TEMPERATURE_HOT = 2.0D;

    private static final double TEMPERATURE_MILD = 0.7D;

    private static final double TEMPERATURE_COLD = -0.6D;

    private static final String BIOME_DESERT = "minecraft:desert";

    private static final String BIOME_SWAMP = "minecraft:swamp";

    private static final String BIOME_OCEAN = "minecraft:ocean";

    private static final String BIOME_PLAINS = "minecraft:plains";

    private static final String BIOME_MEADOW = "minecraft:meadow";

    private static final String BIOME_FOREST = "minecraft:forest";

    private static final String BIOME_SAVANNA = "minecraft:savanna";

    private static final String BIOME_SNOWY_PLAINS = "minecraft:snowy_plains";

    private static final Set<String> TAG_DESERT_HOT_DRY = Set.of("c:is_desert", "c:is_hot", "c:is_dry");

    private static final Set<String> TAG_SWAMP_WET = Set.of("c:is_swamp", "c:is_wet", "c:is_aquatic");

    private static final Set<String> TAG_OCEAN = Set.of("minecraft:is_ocean", "c:is_ocean", "c:is_aquatic", "c:is_wet");

    private static final Set<String> TAG_PLAINS = Set.of("c:is_plains", "minecraft:is_overworld", "c:is_overworld");

    private static final Set<String> TAG_FOREST = Set.of("c:is_forest", "minecraft:is_forest", "c:is_overworld");

    private static final Set<String> TAG_SAVANNA = Set.of("c:is_savanna", "minecraft:is_savanna", "c:is_dry");

    private static final Set<String> TAG_SNOWY = Set.of("c:is_snowy", "c:is_cold", "c:is_overworld");

    private final BiomeInferenceService service = new BiomeInferenceService();

    @Test
    void benmingPriorityMustOverrideConflictingDaoMarkTrend() {
        List<BiomeInferenceService.VanillaBiomeDescriptor> vanillaBiomes = List.of(
            new BiomeInferenceService.VanillaBiomeDescriptor(BIOME_DESERT, TAG_DESERT_HOT_DRY, TEMPERATURE_HOT, false),
            new BiomeInferenceService.VanillaBiomeDescriptor(BIOME_SWAMP, TAG_SWAMP_WET, TEMPERATURE_MILD, true)
        );

        Map<DaoType, Double> daoMarks = new HashMap<>();
        daoMarks.put(DaoType.WATER, 140.0D);
        daoMarks.put(DaoType.FIRE, 8.0D);

        BiomeInferenceService.BiomeInferenceInput input = new BiomeInferenceService.BiomeInferenceInput(
            "沙漠炎蛊",
            Set.of(DaoType.FIRE),
            daoMarks,
            11L
        );

        BiomeInferenceService.BiomeInferenceResult result = service.infer(input, vanillaBiomes);

        assertEquals(
            BIOME_DESERT,
            result.rankedPreferences().get(0).biomeId(),
            "本命蛊直连语义应压过冲突道痕，保证本命优先级稳定可测"
        );
        assertTrue(
            result.rankedPreferences().get(0).benmingScore() > result.rankedPreferences().get(1).benmingScore(),
            "冲突场景下 benmingScore 必须显著区分，以便后续层明确解释"
        );
    }

    @Test
    void daoMarkWeightingMustPreferTagMetadataMatchedBiome() {
        List<BiomeInferenceService.VanillaBiomeDescriptor> vanillaBiomes = List.of(
            new BiomeInferenceService.VanillaBiomeDescriptor(BIOME_OCEAN, TAG_OCEAN, TEMPERATURE_MILD, true),
            new BiomeInferenceService.VanillaBiomeDescriptor(BIOME_PLAINS, TAG_PLAINS, TEMPERATURE_MILD, true),
            new BiomeInferenceService.VanillaBiomeDescriptor(BIOME_DESERT, TAG_DESERT_HOT_DRY, TEMPERATURE_HOT, false)
        );

        Map<DaoType, Double> daoMarks = new HashMap<>();
        daoMarks.put(DaoType.WATER, 120.0D);
        daoMarks.put(DaoType.FIRE, 10.0D);

        BiomeInferenceService.BiomeInferenceInput input = new BiomeInferenceService.BiomeInferenceInput(
            "",
            Set.of(),
            daoMarks,
            19L
        );

        BiomeInferenceService.BiomeInferenceResult result = service.infer(input, vanillaBiomes);

        assertEquals(BIOME_OCEAN, result.rankedPreferences().get(0).biomeId(), "道痕主导时应命中标签/元数据最匹配 biome");
        assertTrue(
            result.rankedPreferences().get(0).daoMarkScore() > result.rankedPreferences().get(1).daoMarkScore(),
            "首位候选的道痕分应高于其它候选，便于验证权重生效"
        );
    }

    @Test
    void tieBreakOrderMustBeDeterministicForSameInput() {
        List<BiomeInferenceService.VanillaBiomeDescriptor> vanillaBiomes = List.of(
            new BiomeInferenceService.VanillaBiomeDescriptor(BIOME_PLAINS, TAG_PLAINS, TEMPERATURE_MILD, true),
            new BiomeInferenceService.VanillaBiomeDescriptor(BIOME_MEADOW, TAG_PLAINS, TEMPERATURE_MILD, true)
        );

        BiomeInferenceService.BiomeInferenceInput input = new BiomeInferenceService.BiomeInferenceInput(
            "",
            Set.of(),
            Map.of(),
            7L
        );

        BiomeInferenceService.BiomeInferenceResult first = service.infer(input, vanillaBiomes);
        BiomeInferenceService.BiomeInferenceResult second = service.infer(input, vanillaBiomes);

        assertEquals(first.rankedPreferences(), second.rankedPreferences(), "同输入的平分排序必须完全稳定");
        assertEquals(BIOME_MEADOW, first.rankedPreferences().get(0).biomeId(), "同分时应使用固定 biomeId 作为最终 tie-break");
    }

    @Test
    void fallbackMustPreferBenmingRelatedBeforeStablePool() {
        List<BiomeInferenceService.VanillaBiomeDescriptor> vanillaBiomes = List.of(
            new BiomeInferenceService.VanillaBiomeDescriptor(BIOME_PLAINS, TAG_PLAINS, TEMPERATURE_MILD, true),
            new BiomeInferenceService.VanillaBiomeDescriptor(BIOME_FOREST, TAG_FOREST, TEMPERATURE_MILD, true),
            new BiomeInferenceService.VanillaBiomeDescriptor(BIOME_SNOWY_PLAINS, TAG_SNOWY, TEMPERATURE_COLD, true)
        );

        BiomeInferenceService.BiomeInferenceInput input = new BiomeInferenceService.BiomeInferenceInput(
            "玄魂蛊",
            Set.of(),
            Map.of(),
            1L
        );

        BiomeInferenceService.BiomeInferenceResult result = service.infer(input, vanillaBiomes);

        assertEquals(BiomeInferenceService.BiomeFallbackPolicy.BENMING_RELATED, result.fallbackPolicy());
        assertEquals(Optional.of(BIOME_SNOWY_PLAINS), result.fallbackBiomeId(), "无正分时应先走本命相关 fallback");
    }

    @Test
    void stableHashFallbackMustBeDeterministicWhenNoBenmingFallbackExists() {
        List<BiomeInferenceService.VanillaBiomeDescriptor> vanillaBiomes = List.of(
            new BiomeInferenceService.VanillaBiomeDescriptor(BIOME_PLAINS, TAG_PLAINS, TEMPERATURE_MILD, true),
            new BiomeInferenceService.VanillaBiomeDescriptor(BIOME_FOREST, TAG_FOREST, TEMPERATURE_MILD, true),
            new BiomeInferenceService.VanillaBiomeDescriptor(BIOME_SAVANNA, TAG_SAVANNA, TEMPERATURE_HOT, false)
        );

        BiomeInferenceService.BiomeInferenceInput input = new BiomeInferenceService.BiomeInferenceInput(
            "未知本命",
            Set.of(),
            Map.of(),
            42L
        );

        BiomeInferenceService.BiomeInferenceResult first = service.infer(input, vanillaBiomes);
        BiomeInferenceService.BiomeInferenceResult second = service.infer(input, vanillaBiomes);

        assertEquals(BiomeInferenceService.BiomeFallbackPolicy.STABLE_HASH_POOL, first.fallbackPolicy());
        assertEquals(first.fallbackBiomeId(), second.fallbackBiomeId(), "稳定哈希 fallback 必须可复现");
        assertTrue(first.fallbackBiomeId().isPresent());
    }

    @Test
    void serviceMustBeTagAndMetadataAwareNotPureStaticLookup() {
        List<BiomeInferenceService.VanillaBiomeDescriptor> vanillaBiomes = List.of(
            new BiomeInferenceService.VanillaBiomeDescriptor(BIOME_DESERT, TAG_DESERT_HOT_DRY, TEMPERATURE_HOT, false),
            new BiomeInferenceService.VanillaBiomeDescriptor(BIOME_SNOWY_PLAINS, TAG_SNOWY, TEMPERATURE_COLD, true),
            new BiomeInferenceService.VanillaBiomeDescriptor(BIOME_PLAINS, TAG_PLAINS, TEMPERATURE_MILD, true)
        );

        BiomeInferenceService.BiomeInferenceInput fireInput = new BiomeInferenceService.BiomeInferenceInput(
            "",
            Set.of(DaoType.FIRE),
            Map.of(DaoType.FIRE, 10.0D),
            3L
        );
        BiomeInferenceService.BiomeInferenceInput iceInput = new BiomeInferenceService.BiomeInferenceInput(
            "",
            Set.of(DaoType.ICE),
            Map.of(DaoType.ICE, 10.0D),
            3L
        );

        BiomeInferenceService.BiomeInferenceResult fireResult = service.infer(fireInput, vanillaBiomes);
        BiomeInferenceService.BiomeInferenceResult iceResult = service.infer(iceInput, vanillaBiomes);

        assertEquals(BIOME_DESERT, fireResult.rankedPreferences().get(0).biomeId(), "火道应受热/干标签与温度共同影响");
        assertEquals(BIOME_SNOWY_PLAINS, iceResult.rankedPreferences().get(0).biomeId(), "冰道应受冷/雪标签与温度共同影响");
    }
}
