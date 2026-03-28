package com.Kizunad.guzhenrenext.xianqiao.entry;

import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionAttemptStage;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.InitPhase;
import java.util.List;
import java.util.Objects;

public final class XianqiaoUiProjection {

    private static final int SCORE_MIN = 0;
    private static final int SCORE_MAX = 100;
    private static final int READY_QI_THRESHOLD = 60;
    private static final int READY_BALANCE_THRESHOLD = 75;

    private static final String SURFACE_HOME_BANNER = "home_banner";
    private static final String SURFACE_HOME_CARD = "home_card";
    private static final String SURFACE_RIGHT_RAIL = "right_rail";
    private static final String SURFACE_ASCENSION_PREP = "ascension_preparation";
    private static final String SURFACE_SPIRIT = "spirit_incubation";
    private static final String SURFACE_TRIBULATION = "tribulation_risk";
    private static final String SURFACE_DAO_ENVIRONMENT = "dao_environment";

    private static final String TITLE_HOME_BANNER = "首页 Banner";
    private static final String TITLE_HOME_CARD = "首页卡";
    private static final String TITLE_RIGHT_RAIL = "右 Rail";
    private static final String TITLE_ASCENSION_PREP = "升仙准备";
    private static final String TITLE_SPIRIT = "灵性 / 地灵孕育";
    private static final String TITLE_TRIBULATION = "灾劫 / 险兆";
    private static final String TITLE_DAO_ENVIRONMENT = "道痕 / 环境";

    private static final String STAGE_CULTIVATION = "修炼推进";
    private static final String STAGE_PREPARATION = "升仙筹备";
    private static final String STAGE_QI_HARMONIZATION = "观气调和";
    private static final String STAGE_READY = "冲关在即";
    private static final String STAGE_CONFIRMED = "劫机已定";
    private static final String STAGE_TRIBULATION = "原地渡劫";
    private static final String STAGE_FORMING = "仙窍成形";
    private static final String STAGE_FAILED_INJURY = "冲关失败（重伤）";
    private static final String STAGE_FAILED_DEATH = "冲关失败（身死）";

    private static final String RISK_INIT_IN_PROGRESS = "仙窍尚在开辟";
    private static final String RISK_NOT_UNLOCKED = "火候未足";
    private static final String RISK_PREPARATION_OPEN = "升仙气机渐起";
    private static final String RISK_QI_HARMONIZING = "三气仍在调和";
    private static final String RISK_WAITING_CONFIRM = "心念尚待一决";
    private static final String RISK_CONFIRMED = "冲关契机已成";
    private static final String RISK_FAILED_AFTERMATH = "冲关余波未平";

    private static final String BLOCKER_INIT_IN_PROGRESS = "仙窍尚在开辟";
    private static final String BLOCKER_FIVE_TURN = "五转巅峰未满足";
    private static final String BLOCKER_THREE_QI = "三气尚未充足";
    private static final String BLOCKER_BALANCE = "三气尚未平衡";
    private static final String BLOCKER_FROZEN_SNAPSHOT = "升仙气机未定";
    private static final String BLOCKER_WAITING_CONFIRM = "尚待心念一定";
    private static final String BLOCKER_FAILED_RECOVERY = "失败后恢复未完成";
    private static final String BLOCKER_NONE = "无显式阻塞";

    private static final String BUTTON_KEEP_CULTIVATING = "继续修炼";
    private static final String BUTTON_OPEN_PREP = "前往升仙准备";
    private static final String BUTTON_HARMONIZE_QI = "继续调和三气";
    private static final String BUTTON_START_ASCENSION = "发起升仙冲关";

    private XianqiaoUiProjection() {
    }

    public static ProjectionSnapshot project(ProjectionInput input) {
        Objects.requireNonNull(input, "input");
        StageSemantics semantics = resolveStageSemantics(input);
        SurfaceSummary homeBanner = buildSurface(
            SURFACE_HOME_BANNER,
            TITLE_HOME_BANNER,
            semantics,
            "当前阶段：" + semantics.stageLabel() + "；主阻塞：" + semantics.primaryBlocker()
        );
        SurfaceSummary homeCard = buildSurface(
            SURFACE_HOME_CARD,
            TITLE_HOME_CARD,
            semantics,
            "下一步：" + resolveEntryButtonLabel(input) + "；气机：" + resolveSnapshotDetail(input)
        );
        SurfaceSummary rightRail = buildSurface(
            SURFACE_RIGHT_RAIL,
            TITLE_RIGHT_RAIL,
            semantics,
            "三气：天"
                + input.heavenQiScorePercent()
                + "% / 地"
                + input.earthQiScorePercent()
                + "% / 人"
                + input.humanQiScorePercent()
                + "%"
        );
        SurfaceSummary ascensionPrep = buildSurface(
            SURFACE_ASCENSION_PREP,
            TITLE_ASCENSION_PREP,
            semantics,
            "平衡度："
                + input.balanceScorePercent()
                + "%；"
                + resolveSnapshotDetail(input)
        );
        SurfaceSummary spirit = buildSurface(
            SURFACE_SPIRIT,
            TITLE_SPIRIT,
            semantics,
            "灵性孕育页沿用同一阶段语义，当前仍以“"
                + semantics.primaryBlocker()
                + "”决定下一步。"
        );
        SurfaceSummary tribulation = buildSurface(
            SURFACE_TRIBULATION,
            TITLE_TRIBULATION,
            semantics,
            "灾劫页只展示与当前阶段一致的险兆，不把未确认冲关伪装成已开始渡劫。"
        );
        SurfaceSummary daoEnvironment = buildSurface(
            SURFACE_DAO_ENVIRONMENT,
            TITLE_DAO_ENVIRONMENT,
            semantics,
            "主脉 / 次脉 / 候选地形与升仙准备共享同一阶段判断，道痕深层入口统一为占位态。"
        );
        return new ProjectionSnapshot(
            homeBanner,
            homeCard,
            rightRail,
            ascensionPrep,
            spirit,
            tribulation,
            daoEnvironment,
            XianqiaoModuleRoutePlaceholders.daoDetector(),
            XianqiaoModuleRoutePlaceholders.daomarkDistribution(),
            resolveEntryButtonLabel(input),
            isGameplayEntryAvailable(input)
        );
    }

    private static SurfaceSummary buildSurface(
        String surfaceId,
        String title,
        StageSemantics semantics,
        String detail
    ) {
        return new SurfaceSummary(
            surfaceId,
            title,
            semantics.stageLabel(),
            semantics.riskLabel(),
            semantics.primaryBlocker(),
            detail
        );
    }

    private static StageSemantics resolveStageSemantics(ProjectionInput input) {
        if (input.initPhase() == InitPhase.PLANNED || input.initPhase() == InitPhase.EXECUTING) {
            return new StageSemantics(
                resolveStageLabel(input.attemptStage()),
                RISK_INIT_IN_PROGRESS,
                BLOCKER_INIT_IN_PROGRESS
            );
        }
        if (input.attemptStage() == AscensionAttemptStage.FAILED_SEVERE_INJURY
            || input.attemptStage() == AscensionAttemptStage.FAILED_DEATH) {
            return new StageSemantics(
                resolveStageLabel(input.attemptStage()),
                RISK_FAILED_AFTERMATH,
                BLOCKER_FAILED_RECOVERY
            );
        }
        if (input.canEnterConfirmed()) {
            return new StageSemantics(resolveStageLabel(input.attemptStage()), RISK_CONFIRMED, BLOCKER_NONE);
        }
        if (input.readyToConfirm()) {
            return new StageSemantics(
                resolveStageLabel(input.attemptStage()),
                RISK_WAITING_CONFIRM,
                BLOCKER_WAITING_CONFIRM
            );
        }
        if (!input.fiveTurnPeak()) {
            return new StageSemantics(resolveStageLabel(input.attemptStage()), RISK_NOT_UNLOCKED, BLOCKER_FIVE_TURN);
        }
        if (minimumQiScore(input) < READY_QI_THRESHOLD) {
            return new StageSemantics(resolveStageLabel(input.attemptStage()), RISK_QI_HARMONIZING, BLOCKER_THREE_QI);
        }
        if (input.balanceScorePercent() < READY_BALANCE_THRESHOLD) {
            return new StageSemantics(resolveStageLabel(input.attemptStage()), RISK_QI_HARMONIZING, BLOCKER_BALANCE);
        }
        if (!input.snapshotFrozen()) {
            return new StageSemantics(
                resolveStageLabel(input.attemptStage()),
                RISK_PREPARATION_OPEN,
                BLOCKER_FROZEN_SNAPSHOT
            );
        }
        return new StageSemantics(
            resolveStageLabel(input.attemptStage()),
            RISK_PREPARATION_OPEN,
            BLOCKER_WAITING_CONFIRM
        );
    }

    private static int minimumQiScore(ProjectionInput input) {
        return Math.min(
            input.heavenQiScorePercent(),
            Math.min(input.humanQiScorePercent(), input.earthQiScorePercent())
        );
    }

    private static String resolveStageLabel(AscensionAttemptStage stage) {
        return switch (stage) {
            case CULTIVATION_PROGRESS -> STAGE_CULTIVATION;
            case ASCENSION_PREPARATION_UNLOCKED -> STAGE_PREPARATION;
            case QI_OBSERVATION_AND_HARMONIZATION -> STAGE_QI_HARMONIZATION;
            case READY_TO_CONFIRM -> STAGE_READY;
            case CONFIRMED -> STAGE_CONFIRMED;
            case WORLD_TRIBULATION_IN_PLACE -> STAGE_TRIBULATION;
            case APERTURE_FORMING -> STAGE_FORMING;
            case FAILED_SEVERE_INJURY -> STAGE_FAILED_INJURY;
            case FAILED_DEATH -> STAGE_FAILED_DEATH;
        };
    }

    private static String resolveSnapshotDetail(ProjectionInput input) {
        if (!input.snapshotFrozen()) {
            return "升仙气机未定";
        }
        if (input.frozenSnapshotPlayerInitiated()) {
            return "升仙气机已定（亲自引动）";
        }
        return "升仙气机已定（静候引动）";
    }

    private static String resolveEntryButtonLabel(ProjectionInput input) {
        if (!isGameplayEntryAvailable(input)) {
            return BUTTON_KEEP_CULTIVATING;
        }
        if (input.canEnterConfirmed() || input.readyToConfirm()) {
            return BUTTON_START_ASCENSION;
        }
        if (input.fiveTurnPeak()) {
            return BUTTON_HARMONIZE_QI;
        }
        return BUTTON_OPEN_PREP;
    }

    private static boolean isGameplayEntryAvailable(ProjectionInput input) {
        return input.attemptStage() != AscensionAttemptStage.CULTIVATION_PROGRESS
            && input.attemptStage() != AscensionAttemptStage.FAILED_SEVERE_INJURY
            && input.attemptStage() != AscensionAttemptStage.FAILED_DEATH;
    }

    public record ProjectionInput(
        InitPhase initPhase,
        AscensionAttemptStage attemptStage,
        int heavenQiScorePercent,
        int humanQiScorePercent,
        int earthQiScorePercent,
        int balanceScorePercent,
        boolean fiveTurnPeak,
        boolean readyToConfirm,
        boolean confirmedThresholdMet,
        boolean canEnterConfirmed,
        boolean snapshotFrozen,
        boolean frozenSnapshotPlayerInitiated
    ) {

        public ProjectionInput {
            initPhase = Objects.requireNonNull(initPhase, "initPhase");
            attemptStage = Objects.requireNonNull(attemptStage, "attemptStage");
            heavenQiScorePercent = clampPercent(heavenQiScorePercent);
            humanQiScorePercent = clampPercent(humanQiScorePercent);
            earthQiScorePercent = clampPercent(earthQiScorePercent);
            balanceScorePercent = clampPercent(balanceScorePercent);
        }

        private static int clampPercent(int value) {
            if (value < SCORE_MIN) {
                return SCORE_MIN;
            }
            if (value > SCORE_MAX) {
                return SCORE_MAX;
            }
            return value;
        }
    }

    public record ProjectionSnapshot(
        SurfaceSummary homeBanner,
        SurfaceSummary homeCard,
        SurfaceSummary rightRail,
        SurfaceSummary ascensionPreparation,
        SurfaceSummary spiritIncubation,
        SurfaceSummary tribulationRisk,
        SurfaceSummary daoEnvironment,
        XianqiaoModuleRoutePlaceholders.PlaceholderRoute detectorRoute,
        XianqiaoModuleRoutePlaceholders.PlaceholderRoute daomarkDistributionRoute,
        String entryButtonLabel,
        boolean gameplayEntryAvailable
    ) {

        public ProjectionSnapshot {
            homeBanner = Objects.requireNonNull(homeBanner, "homeBanner");
            homeCard = Objects.requireNonNull(homeCard, "homeCard");
            rightRail = Objects.requireNonNull(rightRail, "rightRail");
            ascensionPreparation = Objects.requireNonNull(ascensionPreparation, "ascensionPreparation");
            spiritIncubation = Objects.requireNonNull(spiritIncubation, "spiritIncubation");
            tribulationRisk = Objects.requireNonNull(tribulationRisk, "tribulationRisk");
            daoEnvironment = Objects.requireNonNull(daoEnvironment, "daoEnvironment");
            detectorRoute = Objects.requireNonNull(detectorRoute, "detectorRoute");
            daomarkDistributionRoute = Objects.requireNonNull(daomarkDistributionRoute, "daomarkDistributionRoute");
            entryButtonLabel = Objects.requireNonNull(entryButtonLabel, "entryButtonLabel");
        }

        public List<SurfaceSummary> allSurfaceSummaries() {
            return List.of(
                homeBanner,
                homeCard,
                rightRail,
                ascensionPreparation,
                spiritIncubation,
                tribulationRisk,
                daoEnvironment
            );
        }
    }

    public record SurfaceSummary(
        String surfaceId,
        String title,
        String stageLabel,
        String riskLabel,
        String primaryBlocker,
        String detail
    ) {

        public SurfaceSummary {
            surfaceId = Objects.requireNonNull(surfaceId, "surfaceId");
            title = Objects.requireNonNull(title, "title");
            stageLabel = Objects.requireNonNull(stageLabel, "stageLabel");
            riskLabel = Objects.requireNonNull(riskLabel, "riskLabel");
            primaryBlocker = Objects.requireNonNull(primaryBlocker, "primaryBlocker");
            detail = Objects.requireNonNull(detail, "detail");
        }
    }

    private record StageSemantics(String stageLabel, String riskLabel, String primaryBlocker) {

        private StageSemantics {
            stageLabel = Objects.requireNonNull(stageLabel, "stageLabel");
            riskLabel = Objects.requireNonNull(riskLabel, "riskLabel");
            primaryBlocker = Objects.requireNonNull(primaryBlocker, "primaryBlocker");
        }
    }
}
