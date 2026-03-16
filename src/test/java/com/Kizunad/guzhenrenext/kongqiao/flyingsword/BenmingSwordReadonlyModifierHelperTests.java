package com.Kizunad.guzhenrenext.kongqiao.flyingsword;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.BenmingSwordReadonlyModifierHelper;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.CultivationSnapshot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BenmingSwordReadonlyModifierHelperTests {

    private static final double DOUBLE_DELTA = 1.0E-9;

    @Test
    void higherQiyunAndRealmYieldMildPositiveAndCappedRewardMultiplier() {
        BenmingSwordReadonlyModifierHelper.ReadonlyModifier lowModifier =
            BenmingSwordReadonlyModifierHelper.fromSnapshot(
                CultivationSnapshot.of(0.0, 0.0, 0.0, 0.0, 0)
            );
        BenmingSwordReadonlyModifierHelper.ReadonlyModifier highModifier =
            BenmingSwordReadonlyModifierHelper.fromSnapshot(
                CultivationSnapshot.of(0.0, 0.0, 0.0, 999999.0, 99)
            );

        assertEquals(1.0D, lowModifier.finalMultiplier(), DOUBLE_DELTA);
        assertTrue(highModifier.finalMultiplier() > lowModifier.finalMultiplier());
        assertEquals(1.2D, highModifier.finalMultiplier(), DOUBLE_DELTA);
        assertEquals(120.0D, highModifier.applyToReward(100.0D), DOUBLE_DELTA);
    }

    @Test
    void physiqueSeamRemainsSafeNoopAndDoesNotAffectFinalModifier() {
        BenmingSwordReadonlyModifierHelper.ReadonlyModifier modifier =
            BenmingSwordReadonlyModifierHelper.fromSnapshot(
                CultivationSnapshot.of(0.0, 0.0, 0.0, 5000.0, 5)
            );

        double expectedQiyunBonus = 0.5D * 0.08D;
        double expectedRealmBonus = (5.0D / 9.0D) * 0.12D;
        double expectedFinal = 1.0D + expectedQiyunBonus + expectedRealmBonus;

        assertEquals(1.0D, modifier.physiqueMultiplier(), DOUBLE_DELTA);
        assertEquals(expectedFinal, modifier.finalMultiplier(), DOUBLE_DELTA);
        assertEquals(40.0D, modifier.applyToCost(40.0D), DOUBLE_DELTA);
    }
}
