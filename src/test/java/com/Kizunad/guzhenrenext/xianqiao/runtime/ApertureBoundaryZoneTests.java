package com.Kizunad.guzhenrenext.xianqiao.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ApertureBoundaryZoneTests {

    private static final int MIN_CHUNK_X = 0;

    private static final int MAX_CHUNK_X = 0;

    private static final int MIN_CHUNK_Z = 0;

    private static final int MAX_CHUNK_Z = 0;

    @Test
    void shouldClassifyPlayableZoneWhenOutsideDistanceIsZero() {
        ChaosZoneModel atBoundary = ApertureRuntimeBoundaryService.resolveZone(
            MIN_CHUNK_X,
            MAX_CHUNK_X,
            MIN_CHUNK_Z,
            MAX_CHUNK_Z,
            15,
            15
        );

        assertEquals(0, atBoundary.outsideDistanceBlocks());
        assertEquals(0L, atBoundary.outsideDistanceSquared());
        assertEquals(ChaosZoneModel.ZoneType.PLAYABLE_ZONE, atBoundary.zoneType());
        assertTrue(atBoundary.isPlayableZone());
        assertFalse(atBoundary.isWithinReservedChaosBand());
    }

    @Test
    void shouldClassifySafeZoneWhenOutsideDistanceIsExactlyEight() {
        ChaosZoneModel zone = ApertureRuntimeBoundaryService.resolveZone(
            MIN_CHUNK_X,
            MAX_CHUNK_X,
            MIN_CHUNK_Z,
            MAX_CHUNK_Z,
            23,
            15
        );

        assertEquals(8, zone.outsideDistanceBlocks());
        assertEquals(64L, zone.outsideDistanceSquared());
        assertEquals(ChaosZoneModel.ZoneType.SAFEZONE, zone.zoneType());
        assertTrue(zone.isSafeZone());
        assertTrue(zone.isWithinReservedChaosBand());
    }

    @Test
    void shouldClassifyWarningBandWhenOutsideDistanceIsExactlySixteen() {
        ChaosZoneModel zone = ApertureRuntimeBoundaryService.resolveZone(
            MIN_CHUNK_X,
            MAX_CHUNK_X,
            MIN_CHUNK_Z,
            MAX_CHUNK_Z,
            31,
            15
        );

        assertEquals(16, zone.outsideDistanceBlocks());
        assertEquals(256L, zone.outsideDistanceSquared());
        assertEquals(ChaosZoneModel.ZoneType.WARNING_BAND, zone.zoneType());
        assertTrue(zone.isWarningBand());
        assertTrue(zone.isWithinReservedChaosBand());
    }

    @Test
    void shouldClassifyLethalBandWhenOutsideDistanceIsExactlySeventeen() {
        ChaosZoneModel zone = ApertureRuntimeBoundaryService.resolveZone(
            MIN_CHUNK_X,
            MAX_CHUNK_X,
            MIN_CHUNK_Z,
            MAX_CHUNK_Z,
            32,
            15
        );

        assertEquals(17, zone.outsideDistanceBlocks());
        assertEquals(289L, zone.outsideDistanceSquared());
        assertEquals(ChaosZoneModel.ZoneType.LETHAL_CHAOS_BAND, zone.zoneType());
        assertTrue(zone.isLethalChaosBand());
        assertFalse(zone.isWithinReservedChaosBand());
    }

    @Test
    void shouldUseDistanceSquaredThresholdsAsEightSixteenSeventeen() {
        ChaosZoneModel safe = ChaosZoneModel.fromOutsideDistanceSquared(64L);
        ChaosZoneModel warning = ChaosZoneModel.fromOutsideDistanceSquared(256L);
        ChaosZoneModel lethal = ChaosZoneModel.fromOutsideDistanceSquared(257L);

        assertEquals(8, safe.outsideDistanceBlocks());
        assertEquals(ChaosZoneModel.ZoneType.SAFEZONE, safe.zoneType());

        assertEquals(16, warning.outsideDistanceBlocks());
        assertEquals(ChaosZoneModel.ZoneType.WARNING_BAND, warning.zoneType());

        assertEquals(17, lethal.outsideDistanceBlocks());
        assertEquals(ChaosZoneModel.ZoneType.LETHAL_CHAOS_BAND, lethal.zoneType());
    }
}
