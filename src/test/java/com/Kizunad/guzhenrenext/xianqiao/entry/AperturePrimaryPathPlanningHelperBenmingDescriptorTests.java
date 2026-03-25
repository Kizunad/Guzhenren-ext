package com.Kizunad.guzhenrenext.xianqiao.entry;

import com.Kizunad.guzhenrenext.xianqiao.opening.BiomeInferenceService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AperturePrimaryPathPlanningHelperBenmingDescriptorTests {

    private static final String HELPER_SOURCE_PATH =
        "src/main/java/com/Kizunad/guzhenrenext/xianqiao/entry/AperturePrimaryPathPlanningHelper.java";

    private static final String BIOME_OCEAN = "minecraft:ocean";

    private static final String BIOME_PLAINS = "minecraft:plains";

    private static final String BIOME_FOREST = "minecraft:forest";

    private static final String BIOME_SAVANNA = "minecraft:savanna";

    private static final Set<String> TAG_OCEAN = Set.of(
        "minecraft:is_ocean",
        "c:is_ocean",
        "c:is_aquatic",
        "c:is_wet"
    );

    private static final Set<String> TAG_PLAINS = Set.of(
        "c:is_plains",
        "minecraft:is_overworld",
        "c:is_overworld"
    );

    private static final Set<String> TAG_FOREST = Set.of(
        "c:is_forest",
        "minecraft:is_forest",
        "c:is_overworld"
    );

    private static final Set<String> TAG_SAVANNA = Set.of(
        "c:is_savanna",
        "minecraft:is_savanna",
        "c:is_dry"
    );

    private static final double TEMPERATURE_MILD = 0.7D;

    private static final double TEMPERATURE_HOT = 2.0D;

    private static final String UNKNOWN_DESCRIPTOR = "unknown_benming|state:unknown_fallback|benming_code:0";

    private static final String RESOLVED_OCEAN_DESCRIPTOR =
        "benming_code:8|state:resolved|semantic:ocean|海|water|dao:fire";

    private final BiomeInferenceService biomeInferenceService = new BiomeInferenceService();

    @Test
    void sourceWiringMustUseResolvedBenmingDescriptorInsteadOfBlankPlaceholder() throws IOException {
        String source = readHelperSource();

        assertTrue(source.contains("resolveBenmingDescriptor(resolvedOpeningProfile),"));
        assertTrue(source.contains("|state:resolved"));
        assertTrue(source.contains("|semantic:"));
        assertFalse(source.contains("return \"\";"));
    }

    @Test
    void resolvedDescriptorSemanticMustProduceMeaningfulBenmingScoring() {
        assertFalse(RESOLVED_OCEAN_DESCRIPTOR.isBlank());
        assertTrue(RESOLVED_OCEAN_DESCRIPTOR.contains("state:resolved"));
        assertTrue(RESOLVED_OCEAN_DESCRIPTOR.contains("semantic:ocean"));

        List<BiomeInferenceService.VanillaBiomeDescriptor> vanillaBiomes = List.of(
            new BiomeInferenceService.VanillaBiomeDescriptor(BIOME_OCEAN, TAG_OCEAN, TEMPERATURE_MILD, true),
            new BiomeInferenceService.VanillaBiomeDescriptor(BIOME_PLAINS, TAG_PLAINS, TEMPERATURE_MILD, true)
        );
        BiomeInferenceService.BiomeInferenceInput input = new BiomeInferenceService.BiomeInferenceInput(
            RESOLVED_OCEAN_DESCRIPTOR,
            Set.of(),
            Map.of(),
            23L
        );

        BiomeInferenceService.BiomeInferenceResult result = biomeInferenceService.infer(input, vanillaBiomes);

        assertEquals(BIOME_OCEAN, result.rankedPreferences().get(0).biomeId());
        assertTrue(result.rankedPreferences().get(0).benmingScore() > 0.0D);
    }

    @Test
    void unknownFallbackDescriptorMustBeExplicitAndKeepInferenceDeterministic() throws IOException {
        String source = readHelperSource();

        assertTrue(source.contains("BENMING_UNKNOWN_DESCRIPTOR"));
        assertTrue(source.contains(UNKNOWN_DESCRIPTOR));
        assertFalse(UNKNOWN_DESCRIPTOR.isBlank());

        List<BiomeInferenceService.VanillaBiomeDescriptor> vanillaBiomes = List.of(
            new BiomeInferenceService.VanillaBiomeDescriptor(BIOME_PLAINS, TAG_PLAINS, TEMPERATURE_MILD, true),
            new BiomeInferenceService.VanillaBiomeDescriptor(BIOME_FOREST, TAG_FOREST, TEMPERATURE_MILD, true),
            new BiomeInferenceService.VanillaBiomeDescriptor(BIOME_SAVANNA, TAG_SAVANNA, TEMPERATURE_HOT, false)
        );
        BiomeInferenceService.BiomeInferenceInput input = new BiomeInferenceService.BiomeInferenceInput(
            UNKNOWN_DESCRIPTOR,
            Set.of(),
            Map.of(),
            42L
        );

        BiomeInferenceService.BiomeInferenceResult result = biomeInferenceService.infer(input, vanillaBiomes);

        assertEquals(BiomeInferenceService.BiomeFallbackPolicy.STABLE_HASH_POOL, result.fallbackPolicy());
        assertTrue(result.fallbackBiomeId().isPresent());
    }

    private static String readHelperSource() throws IOException {
        return Files.readString(Path.of(HELPER_SOURCE_PATH));
    }
}
