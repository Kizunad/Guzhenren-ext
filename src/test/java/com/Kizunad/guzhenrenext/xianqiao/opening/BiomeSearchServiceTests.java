package com.Kizunad.guzhenrenext.xianqiao.opening;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BiomeSearchServiceTests {

    private static final long DETERMINISTIC_SEED = 20260325L;

    private static final int ORIGIN_X = 64;

    private static final int ORIGIN_Z = 64;

    private final BiomeSearchService service = new BiomeSearchService();

    @Test
    void planLocateAttemptsMustAlwaysUseBoundedRadiusR1000() {
        List<BiomeSearchService.LocateAttempt> attempts = service.planLocateAttempts(
            List.of("minecraft:desert"),
            "minecraft:plains",
            DETERMINISTIC_SEED,
            ORIGIN_X,
            ORIGIN_Z
        );

        assertEquals(2, attempts.size(), "应先尝试优先候选，再尝试 fallback biome");
        assertTrue(
            attempts.stream().allMatch(
                attempt -> attempt.radiusBlocks() == BiomeSearchService.BOUNDED_SEARCH_RADIUS_BLOCKS
            ),
            "所有 locate 计划的半径必须严格等于 r=1000"
        );
        assertTrue(attempts.stream().allMatch(attempt -> attempt.horizontalStep() == 16), "水平步长应保持 16");
        assertTrue(attempts.stream().allMatch(attempt -> attempt.verticalStep() == 16), "垂直步长应保持 16");
    }

    @Test
    void fallbackSelectionMustBeDeterministicForSameInputs() {
        String first = service.resolveDeterministicFallbackBiomeId(
            List.of("minecraft:desert", "minecraft:forest"),
            null,
            DETERMINISTIC_SEED,
            ORIGIN_X,
            ORIGIN_Z
        );
        String second = service.resolveDeterministicFallbackBiomeId(
            List.of("minecraft:desert", "minecraft:forest"),
            null,
            DETERMINISTIC_SEED,
            ORIGIN_X,
            ORIGIN_Z
        );

        assertEquals(first, second, "相同输入与 seed 下 fallback biome 必须稳定可复现");
    }

    @Test
    void planLocateAttemptsMustPreferProvidedRelatedOrDefaultFallback() {
        List<BiomeSearchService.LocateAttempt> attempts = service.planLocateAttempts(
            List.of("minecraft:desert"),
            "minecraft:meadow",
            DETERMINISTIC_SEED,
            ORIGIN_X,
            ORIGIN_Z
        );

        assertEquals(2, attempts.size(), "提供相关 fallback 时应保留两步查询计划");
        assertEquals("minecraft:desert", attempts.get(0).biomeId(), "第一步必须是优先候选 biome");
        assertEquals("minecraft:meadow", attempts.get(1).biomeId(), "第二步必须使用推断层给出的 fallback biome");
        assertTrue(attempts.get(1).fallbackAttempt(), "第二步应标记为 fallback 查询");
    }
}
