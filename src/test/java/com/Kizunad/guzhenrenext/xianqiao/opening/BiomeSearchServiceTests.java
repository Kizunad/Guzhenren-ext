package com.Kizunad.guzhenrenext.xianqiao.opening;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BiomeSearchServiceTests {

    private static final String TARGET_BIOME = "minecraft:plains";
    private static final int ORIGIN_X = 0;
    private static final int ORIGIN_Z = 0;
    private static final int TASK_SIX_RADIUS = 1_000;
    private static final int CHUNK_SIZE = 16;
    private static final int MAX_FALLBACK_ATTEMPTS = 20;
    private static final int FIXED_ANCHOR_Y = 64;
    private static final int SCORE_SCALE = 10_000;
    private static final long DETERMINISTIC_SEED = 20260326L;

    @Test
    void taskSixDefaultMustUseRadiusOneThousand() {
        BiomeSearchService<String> service = BiomeSearchService.taskSixDefault();
        BiomeSearchService.SearchPolicy[] capturedPolicy = new BiomeSearchService.SearchPolicy[1];

        BiomeSearchService.AnchorCandidate result = service.search(
            TARGET_BIOME,
            new BiomeSearchService.SearchRequest(ORIGIN_X, ORIGIN_Z),
            (minInclusive, maxInclusive) -> minInclusive,
            new BiomeSearchService.SearchContext<>() {
                @Override
                public BiomeSearchService.AnchorCandidate locateClosest(
                    String targetBiome,
                    BiomeSearchService.SearchRequest request,
                    BiomeSearchService.SearchPolicy policy
                ) {
                    capturedPolicy[0] = policy;
                    return null;
                }

                @Override
                public boolean chunkCenterMatchesBiome(int chunkMinX, int chunkMinZ, String targetBiome) {
                    return false;
                }

                @Override
                public BiomeSearchService.AnchorCandidate evaluateChunk(int chunkMinX, int chunkMinZ) {
                    return null;
                }
            }
        );

        assertNull(result);
        assertNotNull(capturedPolicy[0]);
        assertEquals(TASK_SIX_RADIUS, capturedPolicy[0].locateSearchRadiusBlocks());
    }

    @Test
    void fallbackSearchMustStayInsideRadius() {
        BiomeSearchService<String> service = BiomeSearchService.taskSixDefault();
        List<BiomeSearchService.ChunkAnchor> checkedChunks = new ArrayList<>();

        BiomeSearchService.AnchorCandidate result = service.search(
            TARGET_BIOME,
            new BiomeSearchService.SearchRequest(ORIGIN_X, ORIGIN_Z),
            createScriptedRandom(62, 0, -62, 0, 0, 62, 0, -62),
            new BiomeSearchService.SearchContext<>() {
                @Override
                public BiomeSearchService.AnchorCandidate locateClosest(
                    String targetBiome,
                    BiomeSearchService.SearchRequest request,
                    BiomeSearchService.SearchPolicy policy
                ) {
                    return null;
                }

                @Override
                public boolean chunkCenterMatchesBiome(int chunkMinX, int chunkMinZ, String targetBiome) {
                    checkedChunks.add(new BiomeSearchService.ChunkAnchor(chunkMinX, chunkMinZ, FIXED_ANCHOR_Y));
                    return true;
                }

                @Override
                public BiomeSearchService.AnchorCandidate evaluateChunk(int chunkMinX, int chunkMinZ) {
                    return new BiomeSearchService.AnchorCandidate(
                        new BiomeSearchService.ChunkAnchor(chunkMinX, chunkMinZ, FIXED_ANCHOR_Y),
                        new BiomeSearchService.AnchorQuality(false, 1.0D)
                    );
                }
            }
        );

        assertNotNull(result);
        assertFalse(checkedChunks.isEmpty());
        for (BiomeSearchService.ChunkAnchor anchor : checkedChunks) {
            assertTrue(isInsideRadius(anchor.chunkMinX(), anchor.chunkMinZ(), TASK_SIX_RADIUS));
        }
    }

    @Test
    void sameSeedAndInputMustYieldDeterministicFallbackAnchor() {
        BiomeSearchService<String> service = BiomeSearchService.taskSixDefault();
        BiomeSearchService.SearchRequest request = new BiomeSearchService.SearchRequest(ORIGIN_X, ORIGIN_Z);

        BiomeSearchService.AnchorCandidate first = service.search(
            TARGET_BIOME,
            request,
            createSeededRandom(DETERMINISTIC_SEED),
            createDeterministicFallbackContext()
        );
        BiomeSearchService.AnchorCandidate second = service.search(
            TARGET_BIOME,
            request,
            createSeededRandom(DETERMINISTIC_SEED),
            createDeterministicFallbackContext()
        );

        assertNotNull(first);
        assertNotNull(second);
        assertEquals(first.anchor(), second.anchor());
        assertEquals(first.quality(), second.quality());
    }

    private static BiomeSearchService.SearchContext<String> createDeterministicFallbackContext() {
        return new BiomeSearchService.SearchContext<>() {
            @Override
            public BiomeSearchService.AnchorCandidate locateClosest(
                String targetBiome,
                BiomeSearchService.SearchRequest request,
                BiomeSearchService.SearchPolicy policy
            ) {
                return null;
            }

            @Override
            public boolean chunkCenterMatchesBiome(int chunkMinX, int chunkMinZ, String targetBiome) {
                return true;
            }

            @Override
            public BiomeSearchService.AnchorCandidate evaluateChunk(int chunkMinX, int chunkMinZ) {
                int score = SCORE_SCALE - Math.abs(chunkMinX) - Math.abs(chunkMinZ);
                return new BiomeSearchService.AnchorCandidate(
                    new BiomeSearchService.ChunkAnchor(chunkMinX, chunkMinZ, FIXED_ANCHOR_Y),
                    new BiomeSearchService.AnchorQuality(false, score)
                );
            }
        };
    }

    private static BiomeSearchService.IntInRangeRandom createSeededRandom(long seed) {
        Random random = new Random(seed);
        return (minInclusive, maxInclusive) -> {
            int bound = maxInclusive - minInclusive + 1;
            return minInclusive + random.nextInt(bound);
        };
    }

    private static BiomeSearchService.IntInRangeRandom createScriptedRandom(int... values) {
        return new BiomeSearchService.IntInRangeRandom() {
            private int cursor;

            @Override
            public int nextIntBetweenInclusive(int minInclusive, int maxInclusive) {
                int index = cursor < values.length ? cursor : values.length - 1;
                cursor++;
                int scripted = values[index];
                if (scripted < minInclusive) {
                    return minInclusive;
                }
                if (scripted > maxInclusive) {
                    return maxInclusive;
                }
                return scripted;
            }
        };
    }

    private static boolean isInsideRadius(int chunkMinX, int chunkMinZ, int radiusBlocks) {
        int centerX = chunkMinX + CHUNK_SIZE / 2;
        int centerZ = chunkMinZ + CHUNK_SIZE / 2;
        long dx = (long) centerX - ORIGIN_X;
        long dz = (long) centerZ - ORIGIN_Z;
        long radiusSquared = (long) radiusBlocks * radiusBlocks;
        return dx * dx + dz * dz <= radiusSquared;
    }
}
