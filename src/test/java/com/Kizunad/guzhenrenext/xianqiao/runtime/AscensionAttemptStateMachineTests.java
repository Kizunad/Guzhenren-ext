package com.Kizunad.guzhenrenext.xianqiao.runtime;

import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionAttemptStage;
import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionAttemptStateContract;
import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionInterruptionDecision;
import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionInterruptionPolicy;
import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionInterruptionReason;
import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionOpeningArchitectureContract;
import java.nio.file.Files;
import java.nio.file.Path;
import com.Kizunad.guzhenrenext.xianqiao.service.FragmentPlacementService;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AscensionAttemptStateMachineTests {

    @Test
    void playerVisibleMainChainOrderIsFrozen() {
        assertEquals(
            List.of(
                AscensionAttemptStage.CULTIVATION_PROGRESS,
                AscensionAttemptStage.ASCENSION_PREPARATION_UNLOCKED,
                AscensionAttemptStage.QI_OBSERVATION_AND_HARMONIZATION,
                AscensionAttemptStage.READY_TO_CONFIRM,
                AscensionAttemptStage.CONFIRMED,
                AscensionAttemptStage.WORLD_TRIBULATION_IN_PLACE,
                AscensionAttemptStage.APERTURE_FORMING
            ),
            AscensionAttemptStateContract.PLAYER_VISIBLE_MAIN_CHAIN
        );
    }

    @Test
    void confirmedMeansAttemptAlreadyStartedAndInputFrozen() {
        assertTrue(AscensionAttemptStateContract.isInputFrozen(AscensionAttemptStage.CONFIRMED));
        assertFalse(AscensionAttemptStateContract.isInputFrozen(AscensionAttemptStage.READY_TO_CONFIRM));
    }

    @Test
    void twoFailureOutcomesArePinnedToSevereInjuryAndDeath() {
        assertTrue(AscensionAttemptStateContract.isInputFrozen(AscensionAttemptStage.FAILED_SEVERE_INJURY));
        assertTrue(AscensionAttemptStateContract.isInputFrozen(AscensionAttemptStage.FAILED_DEATH));
    }

    @Test
    void interruptionPolicyIsSingleAndDeterministicAcrossDuplicateDisconnectReloginReenter() {
        assertEquals(
            AscensionInterruptionDecision.KEEP_SINGLE_ATTEMPT_AND_FREEZE_INPUT,
            AscensionInterruptionPolicy.decide(AscensionInterruptionReason.DUPLICATE_TRIGGER)
        );
        assertEquals(
            AscensionInterruptionDecision.KEEP_SINGLE_ATTEMPT_AND_FREEZE_INPUT,
            AscensionInterruptionPolicy.decide(AscensionInterruptionReason.DISCONNECT)
        );
        assertEquals(
            AscensionInterruptionDecision.KEEP_SINGLE_ATTEMPT_AND_FREEZE_INPUT,
            AscensionInterruptionPolicy.decide(AscensionInterruptionReason.RELOGIN)
        );
        assertEquals(
            AscensionInterruptionDecision.KEEP_SINGLE_ATTEMPT_AND_FREEZE_INPUT,
            AscensionInterruptionPolicy.decide(AscensionInterruptionReason.REENTER)
        );
    }

    @Test
    void openingArchitecturePinsChunkBoundaryTruthSeamCenterAndSymmetricFragmentExpansion() throws Exception {
        assertEquals("MIN_MAX_CHUNK_CLOSED_RANGE", AscensionOpeningArchitectureContract.BOUNDARY_TRUTH_SOURCE);
        assertEquals(
            FragmentPlacementService.BOUNDARY_CHUNK_INCREMENT,
            AscensionOpeningArchitectureContract.FRAGMENT_V1_SYMMETRIC_CHUNK_DELTA
        );

        String strategySource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/xianqiao/service/ApertureInitialTerrainStrategy.java")
        );
        assertTrue(strategySource.contains("new SlotTemplate(\"西北\", -SAMPLE_OFFSET, -SAMPLE_OFFSET)"));
        assertTrue(strategySource.contains("new SlotTemplate(\"东北\", 0, -SAMPLE_OFFSET)"));
        assertTrue(strategySource.contains("new SlotTemplate(\"西南\", -SAMPLE_OFFSET, 0)"));
        assertTrue(strategySource.contains("new SlotTemplate(\"东南\", 0, 0)"));

        String openingContractSource = Files.readString(
            Path.of(
                "src/main/java/com/Kizunad/guzhenrenext/xianqiao/ascension/contract/"
                    + "AscensionOpeningArchitectureContract.java"
            )
        );
        assertTrue(openingContractSource.contains("DOUBLE_CHUNK_SIZE"));
        assertTrue(openingContractSource.contains("center.offset(-DOUBLE_CHUNK_SIZE, 0, -DOUBLE_CHUNK_SIZE)"));
        assertTrue(openingContractSource.contains("center.offset(CHUNK_SIZE, 0, CHUNK_SIZE)"));
        assertTrue(openingContractSource.contains("center.offset(0, 0, 0)"));
        assertTrue(openingContractSource.contains("return List.of("));
    }
}
