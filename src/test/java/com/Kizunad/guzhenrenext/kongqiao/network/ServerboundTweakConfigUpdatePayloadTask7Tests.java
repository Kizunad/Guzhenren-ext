package com.Kizunad.guzhenrenext.kongqiao.network;

import com.Kizunad.guzhenrenext.testsupport.Task4RuntimeHarness;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ServerboundTweakConfigUpdatePayloadTask7Tests {

    @Test
    void overloadedWheelAddRejectsAndKeepsWheelConfigurationUnchanged() throws Exception {
        final Task4RuntimeHarness.RuntimeApi api = Task4RuntimeHarness.create();
        final Object config = api.newTweakConfig();

        assertTrue(
            api.tryAddWheelSkillWithOverloadGate(
                config,
                "guzhenren:test_task7_existing",
                0,
                8
            )
        );
        final List<String> before = api.wheelSkills(config);

        final boolean added = api.tryAddWheelSkillWithOverloadGate(
            config,
            "guzhenren:test_task7_blocked",
            2,
            8
        );
        final List<String> after = api.wheelSkills(config);

        assertFalse(added);
        assertEquals(List.of("guzhenren:test_task7_existing"), before);
        assertEquals(before, after);
    }
}
