package com.Kizunad.guzhenrenext.xianqiao.runtime;

import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionOpeningArchitectureContract;
import com.Kizunad.guzhenrenext.xianqiao.runtime.FragmentExpansionPolicy.ExpandedChunkBoundary;
import com.Kizunad.guzhenrenext.xianqiao.runtime.FragmentExpansionPolicy.HorizontalDirection;
import com.Kizunad.guzhenrenext.xianqiao.service.FragmentPlacementService;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FragmentExpansionCompatibilityTests {

    private static final int CENTER_CHUNK_X = 100;

    private static final int CENTER_CHUNK_Z = 200;

    private static final int MIN_CHUNK_X = 96;

    private static final int MAX_CHUNK_X = 104;

    private static final int MIN_CHUNK_Z = 196;

    private static final int MAX_CHUNK_Z = 204;

    private static final int ASYMMETRIC_CENTER_CHUNK_X = 50;

    private static final int ASYMMETRIC_CENTER_CHUNK_Z = 70;

    private static final int ASYMMETRIC_MIN_CHUNK_X = 47;

    private static final int ASYMMETRIC_MAX_CHUNK_X = 52;

    private static final int ASYMMETRIC_MIN_CHUNK_Z = 68;

    private static final int ASYMMETRIC_MAX_CHUNK_Z = 74;

    private static final Path FRAGMENT_SERVICE_SOURCE = Path.of(
        "src/main/java/com/Kizunad/guzhenrenext/xianqiao/service/FragmentPlacementService.java"
    );

    private static final Path TRIBULATION_MANAGER_SOURCE = Path.of(
        "src/main/java/com/Kizunad/guzhenrenext/xianqiao/tribulation/TribulationManager.java"
    );

    private static final Path HEAVENLY_FRAGMENT_ITEM_SOURCE = Path.of(
        "src/main/java/com/Kizunad/guzhenrenext/xianqiao/item/HeavenlyFragmentItem.java"
    );

    @Test
    void fragmentFlowAndTribulationFlowShareSingleV1Policy() throws Exception {
        assertEquals(
            AscensionOpeningArchitectureContract.FRAGMENT_V1_SYMMETRIC_CHUNK_DELTA,
            FragmentExpansionPolicy.V1_SYMMETRIC_CHUNK_DELTA
        );
        assertEquals(
            FragmentExpansionPolicy.V1_SYMMETRIC_CHUNK_DELTA,
            FragmentPlacementService.BOUNDARY_CHUNK_INCREMENT
        );

        String fragmentServiceSource = Files.readString(FRAGMENT_SERVICE_SOURCE);
        assertTrue(fragmentServiceSource.contains("FragmentExpansionPolicy.applySymmetricExpansion"));
        assertTrue(fragmentServiceSource.contains("FragmentExpansionPolicy.resolvePlacementDistanceChunks"));

        String tribulationManagerSource = Files.readString(TRIBULATION_MANAGER_SOURCE);
        assertTrue(tribulationManagerSource.contains("FragmentExpansionPolicy.applySymmetricExpansion"));
        assertFalse(tribulationManagerSource.contains("REWARD_BOUNDARY_CHUNK_DELTA"));
    }

    @Test
    void previewFacingSemanticsAgreeWithActualBoundaryResults() throws Exception {
        ExpandedChunkBoundary expandedBoundary = FragmentExpansionPolicy.resolveExpandedBoundary(
            MIN_CHUNK_X,
            MAX_CHUNK_X,
            MIN_CHUNK_Z,
            MAX_CHUNK_Z
        );

        assertEquals("本次边界四向各 +1 区块", FragmentExpansionPolicy.previewSummary());
        assertEquals("落点为扩张后当前朝向外缘", FragmentExpansionPolicy.previewTargetSemantics());
        assertEquals(
            expandedBoundary.maxChunkX() - CENTER_CHUNK_X,
            FragmentExpansionPolicy.resolvePlacementDistanceChunks(
                CENTER_CHUNK_X,
                CENTER_CHUNK_Z,
                MIN_CHUNK_X,
                MAX_CHUNK_X,
                MIN_CHUNK_Z,
                MAX_CHUNK_Z,
                HorizontalDirection.EAST
            )
        );
        assertEquals(
            CENTER_CHUNK_X - expandedBoundary.minChunkX(),
            FragmentExpansionPolicy.resolvePlacementDistanceChunks(
                CENTER_CHUNK_X,
                CENTER_CHUNK_Z,
                MIN_CHUNK_X,
                MAX_CHUNK_X,
                MIN_CHUNK_Z,
                MAX_CHUNK_Z,
                HorizontalDirection.WEST
            )
        );
        assertEquals(
            expandedBoundary.maxChunkZ() - CENTER_CHUNK_Z,
            FragmentExpansionPolicy.resolvePlacementDistanceChunks(
                CENTER_CHUNK_X,
                CENTER_CHUNK_Z,
                MIN_CHUNK_X,
                MAX_CHUNK_X,
                MIN_CHUNK_Z,
                MAX_CHUNK_Z,
                HorizontalDirection.SOUTH
            )
        );
        assertEquals(
            CENTER_CHUNK_Z - expandedBoundary.minChunkZ(),
            FragmentExpansionPolicy.resolvePlacementDistanceChunks(
                CENTER_CHUNK_X,
                CENTER_CHUNK_Z,
                MIN_CHUNK_X,
                MAX_CHUNK_X,
                MIN_CHUNK_Z,
                MAX_CHUNK_Z,
                HorizontalDirection.NORTH
            )
        );

        String heavenlyFragmentItemSource = Files.readString(HEAVENLY_FRAGMENT_ITEM_SOURCE);
        assertTrue(heavenlyFragmentItemSource.contains("FragmentExpansionPolicy.previewSummary()"));
        assertTrue(heavenlyFragmentItemSource.contains("FragmentExpansionPolicy.previewTargetSemantics()"));
    }

    @Test
    void v1PolicyRemainsSymmetricRectangleInsteadOfDirectionalOnly() {
        ExpandedChunkBoundary expandedBoundary = FragmentExpansionPolicy.resolveExpandedBoundary(
            ASYMMETRIC_MIN_CHUNK_X,
            ASYMMETRIC_MAX_CHUNK_X,
            ASYMMETRIC_MIN_CHUNK_Z,
            ASYMMETRIC_MAX_CHUNK_Z
        );

        assertEquals(ASYMMETRIC_MIN_CHUNK_X - FragmentExpansionPolicy.chunkDelta(), expandedBoundary.minChunkX());
        assertEquals(ASYMMETRIC_MAX_CHUNK_X + FragmentExpansionPolicy.chunkDelta(), expandedBoundary.maxChunkX());
        assertEquals(ASYMMETRIC_MIN_CHUNK_Z - FragmentExpansionPolicy.chunkDelta(), expandedBoundary.minChunkZ());
        assertEquals(ASYMMETRIC_MAX_CHUNK_Z + FragmentExpansionPolicy.chunkDelta(), expandedBoundary.maxChunkZ());

        assertTrue(expandedBoundary.minChunkX() < ASYMMETRIC_MIN_CHUNK_X);
        assertTrue(expandedBoundary.maxChunkX() > ASYMMETRIC_MAX_CHUNK_X);
        assertTrue(expandedBoundary.minChunkZ() < ASYMMETRIC_MIN_CHUNK_Z);
        assertTrue(expandedBoundary.maxChunkZ() > ASYMMETRIC_MAX_CHUNK_Z);
        assertEquals(
            expandedBoundary.maxChunkX() - ASYMMETRIC_CENTER_CHUNK_X,
            FragmentExpansionPolicy.resolvePlacementDistanceChunks(
                ASYMMETRIC_CENTER_CHUNK_X,
                ASYMMETRIC_CENTER_CHUNK_Z,
                ASYMMETRIC_MIN_CHUNK_X,
                ASYMMETRIC_MAX_CHUNK_X,
                ASYMMETRIC_MIN_CHUNK_Z,
                ASYMMETRIC_MAX_CHUNK_Z,
                HorizontalDirection.EAST
            )
        );
    }
}
