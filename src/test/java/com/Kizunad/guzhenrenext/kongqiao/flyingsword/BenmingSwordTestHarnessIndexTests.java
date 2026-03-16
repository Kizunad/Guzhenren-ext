package com.Kizunad.guzhenrenext.kongqiao.flyingsword;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class BenmingSwordTestHarnessIndexTests {

    private static final String GAME_TEST_CLASS_NAME =
        "com.Kizunad.guzhenrenext_test.flyingsword.BenmingSwordRecallRestoreTests";
    private static final String GAME_TEST_CLASS_RESOURCE =
        "com/Kizunad/guzhenrenext_test/flyingsword/BenmingSwordRecallRestoreTests.class";

    @Test
    void pureLogicLayerShouldRemainDiscoverable() {
        assertDoesNotThrow(() -> Class.forName(BenmingSwordBondSerializationTests.class.getName()));
        assertDoesNotThrow(
            () -> Class.forName(FlyingSwordStateAttachmentBenmingCacheTests.class.getName())
        );
        assertDoesNotThrow(() -> Class.forName(BenmingSwordResourceTransactionTests.class.getName()));
    }

    @Test
    void gameTestLayerEntryShouldRemainDiscoverable() {
        assertDoesNotThrow(() -> Class.forName(GAME_TEST_CLASS_NAME, false, getClass().getClassLoader()));
        assertNotNull(getClass().getClassLoader().getResource(GAME_TEST_CLASS_RESOURCE));
    }
}
