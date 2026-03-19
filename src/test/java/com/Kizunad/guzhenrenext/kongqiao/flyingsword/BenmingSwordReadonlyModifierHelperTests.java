package com.Kizunad.guzhenrenext.kongqiao.flyingsword;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.BenmingSwordReadonlyModifierHelper;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.CultivationSnapshot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BenmingSwordReadonlyModifierHelperTests {

    private static final double DOUBLE_DELTA = 1.0E-9;

    private static final double TEST_MAGIC_999999_0 = 999999.0;
    private static final int TEST_MAGIC_99 = 99;
    private static final double TEST_MAGIC_1_2D = 1.2D;
    private static final double TEST_MAGIC_120_0D = 120.0D;
    private static final double TEST_MAGIC_100_0D = 100.0D;
    private static final double TEST_MAGIC_5000_0 = 5000.0;
    private static final int TEST_MAGIC_5 = 5;
    private static final double TEST_MAGIC_0_5D = 0.5D;
    private static final double TEST_MAGIC_0_08D = 0.08D;
    private static final double TEST_MAGIC_5_0D = 5.0D;
    private static final double TEST_MAGIC_9_0D = 9.0D;
    private static final double TEST_MAGIC_0_12D = 0.12D;
    private static final double TEST_MAGIC_40_0D = 40.0D;


    @Test
    void higherQiyunAndRealmYieldMildPositiveAndCappedRewardMultiplier() {
        BenmingSwordReadonlyModifierHelper.ReadonlyModifier lowModifier =
            BenmingSwordReadonlyModifierHelper.fromSnapshot(
                CultivationSnapshot.of(0.0, 0.0, 0.0, 0.0, 0)
            );
        BenmingSwordReadonlyModifierHelper.ReadonlyModifier highModifier =
            BenmingSwordReadonlyModifierHelper.fromSnapshot(
                CultivationSnapshot.of(0.0, 0.0, 0.0, TEST_MAGIC_999999_0, TEST_MAGIC_99)
            );

        assertEquals(1.0D, lowModifier.finalMultiplier(), DOUBLE_DELTA);
        assertTrue(highModifier.finalMultiplier() > lowModifier.finalMultiplier());
        assertEquals(TEST_MAGIC_1_2D, highModifier.finalMultiplier(), DOUBLE_DELTA);
        assertEquals(TEST_MAGIC_120_0D, highModifier.applyToReward(TEST_MAGIC_100_0D), DOUBLE_DELTA);
    }

    @Test
    void physiqueSeamRemainsSafeNoopAndDoesNotAffectFinalModifier() {
        BenmingSwordReadonlyModifierHelper.ReadonlyModifier modifier =
            BenmingSwordReadonlyModifierHelper.fromSnapshot(
                CultivationSnapshot.of(0.0, 0.0, 0.0, TEST_MAGIC_5000_0, TEST_MAGIC_5)
            );

        double expectedQiyunBonus = TEST_MAGIC_0_5D * TEST_MAGIC_0_08D;
        double expectedRealmBonus = (TEST_MAGIC_5_0D / TEST_MAGIC_9_0D) * TEST_MAGIC_0_12D;
        double expectedFinal = 1.0D + expectedQiyunBonus + expectedRealmBonus;

        assertEquals(1.0D, modifier.physiqueMultiplier(), DOUBLE_DELTA);
        assertEquals(expectedFinal, modifier.finalMultiplier(), DOUBLE_DELTA);
        assertEquals(TEST_MAGIC_40_0D, modifier.applyToCost(TEST_MAGIC_40_0D), DOUBLE_DELTA);
    }
}
