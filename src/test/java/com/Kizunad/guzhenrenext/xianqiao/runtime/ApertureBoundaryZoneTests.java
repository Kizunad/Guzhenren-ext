package com.Kizunad.guzhenrenext.xianqiao.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ApertureBoundaryZoneTests {

    private static final int MIN_CHUNK = 0;

    private static final int MAX_CHUNK = 0;

    private static final int MIN_BLOCK = 0;

    private static final int MAX_BLOCK = 15;

    private static ChaosZoneModel createDefaultZoneModel() {
        return ApertureRuntimeBoundaryService.resolveModel(
            MIN_CHUNK,
            MAX_CHUNK,
            MIN_CHUNK,
            MAX_CHUNK
        );
    }

    @Test
    void thresholdSemanticsAtExactZeroEightSixteenSeventeenAreStable() {
        ChaosZoneModel zoneModel = createDefaultZoneModel();

        assertEquals(ChaosZoneModel.ChaosBand.PLAYABLE_OR_SAFE, zoneModel.resolveChaosBand(MAX_BLOCK, MAX_BLOCK));
        assertEquals(0L, zoneModel.getOutsideDistanceSquaredToPlayable(MAX_BLOCK, MAX_BLOCK));

        int distanceEightX = MAX_BLOCK + 8;
        assertEquals(64L, zoneModel.getOutsideDistanceSquaredToPlayable(distanceEightX, MIN_BLOCK));
        assertEquals(ChaosZoneModel.ChaosBand.PLAYABLE_OR_SAFE, zoneModel.resolveChaosBand(distanceEightX, MIN_BLOCK));

        int distanceSixteenX = MAX_BLOCK + 16;
        assertEquals(256L, zoneModel.getOutsideDistanceSquaredToPlayable(distanceSixteenX, MIN_BLOCK));
        assertEquals(ChaosZoneModel.ChaosBand.WARNING, zoneModel.resolveChaosBand(distanceSixteenX, MIN_BLOCK));
        assertTrue(zoneModel.isInWarningBand(distanceSixteenX, MIN_BLOCK));
        assertFalse(zoneModel.isInLethalChaosBand(distanceSixteenX, MIN_BLOCK));

        int distanceSeventeenX = MAX_BLOCK + 17;
        assertEquals(289L, zoneModel.getOutsideDistanceSquaredToPlayable(distanceSeventeenX, MIN_BLOCK));
        assertEquals(ChaosZoneModel.ChaosBand.LETHAL, zoneModel.resolveChaosBand(distanceSeventeenX, MIN_BLOCK));
        assertFalse(zoneModel.isInWarningBand(distanceSeventeenX, MIN_BLOCK));
        assertTrue(zoneModel.isInLethalChaosBand(distanceSeventeenX, MIN_BLOCK));
    }

    @Test
    void reserveSemanticsStayRuntimeOnlyAndUseSixteenChunks() {
        ChaosZoneModel zoneModel = createDefaultZoneModel();

        assertEquals(ApertureRuntimeBoundaryService.DEFAULT_RESERVED_CHAOS_CHUNKS, zoneModel.reservedChaosChunks());

        int reserveInnerBlock = -256;
        assertTrue(zoneModel.isInsideReserveZone(reserveInnerBlock, MIN_BLOCK));
        assertTrue(zoneModel.isInReserveChaosBand(reserveInnerBlock, MIN_BLOCK));
        assertFalse(zoneModel.isInsideTruthBoundary(reserveInnerBlock, MIN_BLOCK));

        int reserveOutsideBlock = -257;
        assertFalse(zoneModel.isInsideReserveZone(reserveOutsideBlock, MIN_BLOCK));
        assertFalse(zoneModel.isInReserveChaosBand(reserveOutsideBlock, MIN_BLOCK));
        assertEquals(1L, zoneModel.getOutsideDistanceSquaredToReserve(reserveOutsideBlock, MIN_BLOCK));
    }

    @Test
    void defaultThresholdsArePinnedToOpeningPlannerContract() {
        ChaosZoneModel zoneModel = createDefaultZoneModel();

        assertEquals(0, zoneModel.safezoneInsetChunks());
        assertEquals(8, zoneModel.warningBufferBlocks());
        assertEquals(16, zoneModel.lethalBufferBlocks());
        assertEquals(16, zoneModel.reservedChaosChunks());
    }
}
