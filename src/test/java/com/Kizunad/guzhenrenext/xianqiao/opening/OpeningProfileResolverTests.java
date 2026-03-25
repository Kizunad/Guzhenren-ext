package com.Kizunad.guzhenrenext.xianqiao.opening;

import java.util.Map;
import java.util.OptionalDouble;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class OpeningProfileResolverTests {

    private static final double VALUE_ZERO = 0.0;

    private static final double VALUE_TEN = 10.0;

    private static final double VALUE_TWELVE = 12.0;

    private static final double VALUE_TWENTY_FOUR = 24.0;

    private static final double VALUE_THIRTY = 30.0;

    private static final double VALUE_THIRTY_SIX = 36.0;

    private static final double VALUE_FIFTY = 50.0;

    private static final double VALUE_SIXTY_SIX = 66.0;

    private static final double VALUE_SEVENTY = 70.0;

    private static final double VALUE_EIGHTY_EIGHT = 88.0;

    private static final double VALUE_NINETY_TWO = 92.0;

    private static final double VALUE_NINETY_NINE = 99.0;

    private static final double VALUE_FORTY = 40.0;

    private static final double VALUE_HUNDRED = 100.0;

    private static final double VALUE_EIGHT_HUNDRED = 800.0;

    private static final double VALUE_NINE_HUNDRED = 900.0;

    private static final double VALUE_ONE_THOUSAND = 1000.0;

    private static final double VALUE_TWO = 2.0;

    private static final double VALUE_FIVE = 5.0;

    private static final String DAO_JIANDAO = "jiandao";

    private static final String DAO_TUDAO = "tudao";

    private static final String DAO_JINDAO = "jindao";

    private static final String DAO_YUNDAO = "yundao";

    private final OpeningProfileResolver resolver = new OpeningProfileResolver();

    private static OpeningProfileResolver.BasicResolverInput.Builder baseBuilder() {
        return OpeningProfileResolver.BasicResolverInput.builder()
            .zhuanshu(VALUE_FIVE)
            .jieduan(VALUE_FIVE)
            .kongqiao(VALUE_FIVE)
            .benminggu(VALUE_TWO)
            .zuidaZhenyuan(VALUE_ONE_THOUSAND)
            .shouyuan(VALUE_ONE_THOUSAND)
            .jingli(VALUE_ONE_THOUSAND)
            .zuidaJingli(VALUE_ONE_THOUSAND)
            .hunpo(VALUE_ONE_THOUSAND)
            .zuidaHunpo(VALUE_ONE_THOUSAND)
            .tizhi(VALUE_HUNDRED)
            .qiyun(VALUE_THIRTY)
            .qiyunShangxian(VALUE_THIRTY)
            .renqi(OptionalDouble.empty())
            .humanQi(OptionalDouble.empty())
            .earthQi(OptionalDouble.empty())
            .daoHen(Map.of())
            .ascensionAttemptInitiated(false)
            .snapshotFrozen(false);
    }

    @Test
    void sameInputMustResolveDeterministicFrozenProfile() {
        OpeningProfileResolver.BasicResolverInput input = baseBuilder()
            .shouyuan(VALUE_NINE_HUNDRED)
            .jingli(VALUE_EIGHT_HUNDRED)
            .qiyun(VALUE_THIRTY_SIX)
            .qiyunShangxian(VALUE_FORTY)
            .renqi(OptionalDouble.of(VALUE_EIGHTY_EIGHT))
            .humanQi(OptionalDouble.of(VALUE_NINETY_NINE))
            .earthQi(OptionalDouble.of(VALUE_NINETY_TWO))
            .daoHen(Map.of(DAO_JIANDAO, VALUE_TWELVE, DAO_TUDAO, VALUE_TEN))
            .ascensionAttemptInitiated(true)
            .snapshotFrozen(true)
            .build();

        ResolvedOpeningProfile first = resolver.resolve(input);
        ResolvedOpeningProfile second = resolver.resolve(input);

        assertEquals(first, second, "相同输入必须得到完全一致的冻结画像，保证后续规划可复算");
        assertEquals(
            AscensionReadinessStage.CONFIRMED,
            first.threeQiEvaluation().stage(),
            "达到阈值且尝试+冻结成立时必须进入 CONFIRMED"
        );
        assertTrue(first.threeQiEvaluation().highQualityWindow());
    }

    @Test
    void missingUnknownAndAllZeroCasesMustUseExplicitFallbackStates() {
        OpeningProfileResolver.BasicResolverInput input = OpeningProfileResolver.BasicResolverInput.builder()
            .zhuanshu(VALUE_ZERO)
            .jieduan(VALUE_ZERO)
            .kongqiao(VALUE_ZERO)
            .benminggu(VALUE_ZERO)
            .zuidaZhenyuan(VALUE_ZERO)
            .shouyuan(VALUE_ZERO)
            .jingli(VALUE_ZERO)
            .zuidaJingli(VALUE_ZERO)
            .hunpo(VALUE_ZERO)
            .zuidaHunpo(VALUE_ZERO)
            .tizhi(VALUE_ZERO)
            .qiyun(VALUE_ZERO)
            .qiyunShangxian(VALUE_ZERO)
            .build();

        ResolvedOpeningProfile profile = resolver.resolve(input);

        assertEquals(ResolvedOpeningProfile.BenmingGuState.UNKNOWN_FALLBACK, profile.benmingGuState());
        assertEquals(ResolvedOpeningProfile.DaoMarkState.SPARSE_FALLBACK, profile.daoMarkState());
        assertEquals(ResolvedOpeningProfile.AptitudeState.ALL_ZERO_FALLBACK, profile.aptitudeState());
        assertEquals(ResolvedOpeningProfile.HumanQiSource.MISSING_FALLBACK, profile.humanQiSource());
        assertTrue(profile.earthQiFallbackApplied());
        assertEquals(0, profile.aptitudeScore());
        assertEquals("generic", profile.dominantDaoMark());
        assertEquals(AscensionReadinessStage.NOT_READY, profile.threeQiEvaluation().stage());
    }

    @Test
    void renqiShouldHavePriorityOverHumanQiFallback() {
        OpeningProfileResolver.BasicResolverInput input = baseBuilder()
            .renqi(OptionalDouble.of(VALUE_FIFTY))
            .humanQi(OptionalDouble.of(VALUE_NINETY_NINE))
            .earthQi(OptionalDouble.of(VALUE_SEVENTY))
            .daoHen(Map.of(DAO_JIANDAO, VALUE_TWELVE))
            .build();

        ResolvedOpeningProfile profile = resolver.resolve(input);

        assertEquals(ResolvedOpeningProfile.HumanQiSource.REN_QI, profile.humanQiSource());
        assertEquals(50, profile.ascensionConditionSnapshot().humanScore());
    }

    @Test
    void humanQiFallbackShouldBeUsedWhenRenqiAbsent() {
        OpeningProfileResolver.BasicResolverInput input = baseBuilder()
            .qiyun(VALUE_TWENTY_FOUR)
            .renqi(OptionalDouble.empty())
            .humanQi(OptionalDouble.of(VALUE_SIXTY_SIX))
            .earthQi(OptionalDouble.of(VALUE_SEVENTY))
            .daoHen(Map.of(DAO_JIANDAO, VALUE_TWELVE))
            .build();

        ResolvedOpeningProfile profile = resolver.resolve(input);

        assertEquals(ResolvedOpeningProfile.HumanQiSource.HUMAN_QI_FALLBACK, profile.humanQiSource());
        assertEquals(66, profile.ascensionConditionSnapshot().humanScore());
    }

    @Test
    void sparseDaoMarkAndTieShouldBeResolvedDeterministically() {
        OpeningProfileResolver.BasicResolverInput sparseInput = baseBuilder()
            .renqi(OptionalDouble.of(VALUE_FIFTY))
            .earthQi(OptionalDouble.of(VALUE_SEVENTY))
            .daoHen(Map.of(DAO_JIANDAO, VALUE_ZERO, DAO_TUDAO, -VALUE_TEN))
            .build();
        OpeningProfileResolver.BasicResolverInput tieInput = baseBuilder()
            .renqi(OptionalDouble.of(VALUE_FIFTY))
            .earthQi(OptionalDouble.of(VALUE_SEVENTY))
            .daoHen(Map.of(DAO_JINDAO, VALUE_TEN, DAO_YUNDAO, VALUE_TEN))
            .build();

        ResolvedOpeningProfile sparse = resolver.resolve(sparseInput);
        ResolvedOpeningProfile tie = resolver.resolve(tieInput);

        assertEquals(ResolvedOpeningProfile.DaoMarkState.SPARSE_FALLBACK, sparse.daoMarkState());
        assertEquals(ResolvedOpeningProfile.DaoMarkState.RESOLVED, tie.daoMarkState());
        assertEquals(DAO_JINDAO, tie.dominantDaoMark(), "同值道痕必须按稳定字典序择主，避免结果抖动");
    }

    @Test
    void nullPlayerVariablesFactoryPathShouldStillProduceUsableInput() {
        OpeningProfileResolver.OpeningProfileResolverInput input = OpeningProfileResolver.BasicResolverInput.empty(
            OptionalDouble.of(VALUE_SEVENTY),
            OptionalDouble.of(VALUE_SIXTY_SIX),
            false,
            false
        );

        ResolvedOpeningProfile profile = resolver.resolve(input);

        assertEquals(ResolvedOpeningProfile.HumanQiSource.HUMAN_QI_FALLBACK, profile.humanQiSource());
        assertFalse(profile.earthQiFallbackApplied());
    }
}
