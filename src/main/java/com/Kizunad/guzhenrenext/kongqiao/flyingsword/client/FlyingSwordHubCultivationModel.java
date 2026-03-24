package com.Kizunad.guzhenrenext.kongqiao.flyingsword.client;

import com.Kizunad.guzhenrenext.kongqiao.KongqiaoI18n;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.tactical.TacticalTone;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.forge.FlyingSwordForgeAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.training.FlyingSwordTrainingAttachment;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;

final class FlyingSwordHubCultivationModel {

    private static final int DEFAULT_ROUTE_INDEX = 0;
    private static final int GROWTH_ROUTE_INDEX = 3;
    private static final int MIN_REQUIRED_SWORDS = 1;
    private static final int PERCENT_100 = 100;
    private static final float ZERO_RATIO = 0.0F;
    private static final float FULL_RATIO = 1.0F;

    private static final List<String> SUBTAB_TITLES = List.of(
        localizedText(KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_TAB_FORGE, "锻造"),
        localizedText(KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_TAB_TRAINING, "修炼"),
        localizedText(KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_TAB_CLUSTER, "集群"),
        localizedText(KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_TAB_GROWTH, "成长")
    );

    private FlyingSwordHubCultivationModel() {}

    static List<String> subTabTitles() {
        return SUBTAB_TITLES;
    }

    static CultivationPanelState fromSources(
        @Nullable final FlyingSwordTacticalStateService.TacticalStateSnapshot snapshot,
        @Nullable final FlyingSwordForgeAttachment forgeAttachment,
        @Nullable final FlyingSwordTrainingAttachment trainingAttachment,
        final int clusterLoad,
        final int clusterCapacity,
        final int clusterActiveCount
    ) {
        return fromSummaries(
            snapshot,
            summarizeForge(forgeAttachment),
            summarizeTraining(trainingAttachment),
            clusterLoad,
            clusterCapacity,
            clusterActiveCount
        );
    }

    static CultivationPanelState fromSummaries(
        @Nullable final FlyingSwordTacticalStateService.TacticalStateSnapshot snapshot,
        @Nullable final ForgeSummary forgeSummary,
        @Nullable final TrainingSummary trainingSummary,
        final int clusterLoad,
        final int clusterCapacity,
        final int clusterActiveCount
    ) {
        final FlyingSwordTacticalStateService.TacticalStateSnapshot safeSnapshot =
            snapshot != null
                ? snapshot
                : FlyingSwordTacticalStateService.snapshotFromRoster(List.of(), null);
        final FlyingSwordTacticalStateService.FocusSword focusSword = safeFocusSword(safeSnapshot);
        final FlyingSwordTacticalStateService.SquadSummary squadSummary = safeSquadSummary(
            safeSnapshot
        );
        final List<CultivationRoute> routes = List.of(
            buildForgeRoute(forgeSummary),
            buildTrainingRoute(trainingSummary, focusSword, squadSummary),
            buildClusterRoute(safeSnapshot, squadSummary, clusterLoad, clusterCapacity, clusterActiveCount),
            buildGrowthRoute(focusSword, squadSummary)
        );
        return new CultivationPanelState(routes);
    }

    private static CultivationRoute buildForgeRoute(@Nullable final ForgeSummary forgeSummary) {
        final int requiredSwordCount = Math.max(
            MIN_REQUIRED_SWORDS,
            forgeSummary != null
                ? forgeSummary.requiredSwordCount()
                : FlyingSwordForgeAttachment.DEFAULT_REQUIRED_SWORD_COUNT
        );
        final int fedSwordCount = Math.min(
            Math.max(0, forgeSummary != null ? forgeSummary.fedSwordCount() : 0),
            requiredSwordCount
        );
        final int daoTypeCount = forgeSummary != null ? forgeSummary.daoTypeCount() : 0;
        final int daoTotalScore = forgeSummary != null
            ? forgeSummary.daoTotalScore()
            : 0;
        final String summaryText;
        if (forgeSummary == null || !forgeSummary.active()) {
            summaryText = localizedText(
                KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FORGE_SUMMARY_IDLE,
                "未检测到进行中的培养工程，Hub 只保留培养态势摘要与原锻造界面跳转。"
            );
        } else if (fedSwordCount >= requiredSwordCount) {
            summaryText = localizedText(
                KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FORGE_SUMMARY_READY,
                "当前培养工程已满足收取条件，可回原锻造界面查看成品与最终道痕。"
            );
        } else {
            summaryText = localizedText(
                KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FORGE_SUMMARY_ACTIVE,
                "当前工程正在累计材料剑与蛊虫，Hub 只回显进度，不复制任何输入槽。"
            );
        }
        final String statusText = localizedText(
            KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FORGE_STATUS,
            "最近反馈：%s",
            fallbackText(
                forgeSummary != null ? forgeSummary.lastMessage() : null,
                localizedText(
                    KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FORGE_STATUS_FALLBACK,
                    "等待回原界面投入核心剑"
                )
            )
        );
        final String progressText = localizedText(
            KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_PROGRESS_TEMPLATE,
            "进度 %s/%s（%s）",
            fedSwordCount,
            requiredSwordCount,
            formatPercent(ratioFromCounts(fedSwordCount, requiredSwordCount))
        );
        final String progressResource = forgeSummary != null && fedSwordCount >= requiredSwordCount
            ? localizedText(
                KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FORGE_BADGE_READY,
                "可收取"
            )
            : localizedText(
                KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FORGE_BADGE_FEED,
                "需继续投喂"
            );
        return new CultivationRoute(
            SUBTAB_TITLES.get(0),
            TacticalTone.BENMING,
            summaryText,
            statusText,
            progressText,
            ratioFromCounts(fedSwordCount, requiredSwordCount),
            List.of(
                localizedText(
                    KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FORGE_BADGE_SCORE,
                    "道痕总分 %s",
                    daoTotalScore
                ),
                localizedText(
                    KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FORGE_BADGE_TYPE,
                    "道类 %s",
                    daoTypeCount
                ),
                progressResource
            ),
            localizedText(
                KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_ACTION_OPEN_FORGE,
                "打开原锻造界面"
            ),
            RouteActionType.OPEN_FORGE,
            true,
            true
        );
    }

    private static CultivationRoute buildTrainingRoute(
        @Nullable final TrainingSummary trainingSummary,
        final FlyingSwordTacticalStateService.FocusSword focusSword,
        final FlyingSwordTacticalStateService.SquadSummary squadSummary
    ) {
        final int fuelTime = Math.max(0, trainingSummary != null ? trainingSummary.fuelTime() : 0);
        final int maxFuelTime = Math.max(
            0,
            trainingSummary != null ? trainingSummary.maxFuelTime() : 0
        );
        final int accumulatedExp = Math.max(
            0,
            trainingSummary != null ? trainingSummary.accumulatedExp() : 0
        );
        final String summaryText = fuelTime > 0
            ? localizedText(
                KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_TRAINING_SUMMARY_ACTIVE,
                "燃料正在转化为挂机经验，Hub 只回显燃料与经验，不嵌入原修炼界面的输入槽。"
            )
            : localizedText(
                KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_TRAINING_SUMMARY_IDLE,
                "暂无燃料或尚未开始修炼，需要回原修炼界面补充元石。"
            );
        final String statusText = localizedText(
            KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_TRAINING_STATUS,
            "焦点锚点：%s。",
            describeFocusSword(focusSword)
        );
        final float fuelRatio = ratioFromCounts(fuelTime, maxFuelTime);
        final String progressText = localizedText(
            KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_TRAINING_PROGRESS,
            "燃料 %s/%s（%s）",
            fuelTime,
            maxFuelTime,
            formatPercent(fuelRatio)
        );
        return new CultivationRoute(
            SUBTAB_TITLES.get(1),
            TacticalTone.INFO,
            summaryText,
            statusText,
            progressText,
            fuelRatio,
            List.of(
                localizedText(
                    KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_TRAINING_BADGE_EXP,
                    "累计经验 %s",
                    accumulatedExp
                ),
                localizedText(
                    KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_BADGE_PRESENT,
                    "在位 %s",
                    squadSummary.totalCount()
                ),
                localizedText(
                    KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_BADGE_BENMING,
                    "本命 %s",
                    squadSummary.benmingCount()
                )
            ),
            localizedText(
                KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_ACTION_OPEN_TRAINING,
                "打开原修炼界面"
            ),
            RouteActionType.OPEN_TRAINING,
            true,
            true
        );
    }

    private static CultivationRoute buildClusterRoute(
        final FlyingSwordTacticalStateService.TacticalStateSnapshot snapshot,
        final FlyingSwordTacticalStateService.SquadSummary squadSummary,
        final int clusterLoad,
        final int clusterCapacity,
        final int clusterActiveCount
    ) {
        final int safeClusterCapacity = Math.max(0, clusterCapacity);
        final int safeClusterLoad = safeClusterCapacity > 0
            ? Math.min(Math.max(0, clusterLoad), safeClusterCapacity)
            : 0;
        final int safeClusterActiveCount = Math.max(0, clusterActiveCount);
        final boolean benmingInsideWindow = snapshot.hubOverview() != null
            && snapshot.hubOverview().benmingInsideWindow();
        final String summaryText = safeClusterCapacity > 0
            ? localizedText(
                KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_CLUSTER_SUMMARY_ACTIVE,
                "当前集群摘要只显示算力与活跃飞剑，Deploy / Recall 仍回原集群界面执行。"
            )
            : localizedText(
                KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_CLUSTER_SUMMARY_IDLE,
                "暂无客户端集群算力缓存，Hub 只保留打开原集群界面动作。"
            );
        final String statusText = localizedText(
            KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_CLUSTER_STATUS,
            "活跃 %s / 在位 %s / 可爆发 %s",
            safeClusterActiveCount,
            squadSummary.totalCount(),
            squadSummary.burstReadyCount()
        );
        final float clusterRatio = ratioFromCounts(safeClusterLoad, safeClusterCapacity);
        final String progressText = localizedText(
            KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_CLUSTER_PROGRESS,
            "算力 %s/%s（%s）",
            safeClusterLoad,
            safeClusterCapacity,
            formatPercent(clusterRatio)
        );
        return new CultivationRoute(
            SUBTAB_TITLES.get(2),
            TacticalTone.INFO,
            summaryText,
            statusText,
            progressText,
            clusterRatio,
            List.of(
                localizedText(
                    KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_CLUSTER_BADGE_ACTIVE,
                    "活跃 %s",
                    safeClusterActiveCount
                ),
                localizedText(
                    KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_CLUSTER_BADGE_SELECTED,
                    "选中 %s",
                    squadSummary.selectedCount()
                ),
                benmingInsideWindow
                    ? localizedText(
                        KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_CLUSTER_BADGE_BENMING_IN,
                        "窗口含本命"
                    )
                    : localizedText(
                        KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_CLUSTER_BADGE_BENMING_OUT,
                        "窗口无本命"
                    )
            ),
            localizedText(
                KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_ACTION_OPEN_CLUSTER,
                "打开原集群界面"
            ),
            RouteActionType.OPEN_CLUSTER,
            true,
            true
        );
    }

    private static CultivationRoute buildGrowthRoute(
        final FlyingSwordTacticalStateService.FocusSword focusSword,
        final FlyingSwordTacticalStateService.SquadSummary squadSummary
    ) {
        final FlyingSwordTacticalStateService.FlyingSwordViewModel focusView = focusSword.sword();
        if (focusView == null) {
            return new CultivationRoute(
                SUBTAB_TITLES.get(GROWTH_ROUTE_INDEX),
                TacticalTone.BENMING,
                localizedText(
                    KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_GROWTH_SUMMARY_IDLE,
                    "暂无成长锚点；本页只汇总当前编队成长态势，并路由到原成长帮助。"
                ),
                localizedText(
                    KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_GROWTH_STATUS_IDLE,
                    "焦点来源：无；建议先回总览或战斗 HUD 锁定观察对象。"
                ),
                localizedText(
                    KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_GROWTH_PROGRESS_IDLE,
                    "经验进度 0%"
                ),
                ZERO_RATIO,
                List.of(
                    localizedText(
                        KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_BADGE_PRESENT,
                        "在位 %s",
                        squadSummary.totalCount()
                    ),
                    localizedText(
                        KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_BADGE_BENMING,
                        "本命 %s",
                        squadSummary.benmingCount()
                    ),
                    localizedText(
                        KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_BADGE_BURST_READY,
                        "可爆发 %s",
                        squadSummary.burstReadyCount()
                    )
                ),
                localizedText(
                    KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_ACTION_OPEN_GROWTH_HELP,
                    "打开成长帮助"
                ),
                RouteActionType.OPEN_GROWTH_HELP,
                true,
                false
            );
        }

        final float growthRatio = clampRatio(focusView.expProgress());
        final String summaryText = localizedText(
            KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_GROWTH_SUMMARY_ACTIVE,
            "当前成长锚点：%s，来源：%s。",
            focusSwordLabel(focusView),
            focusSourceLabel(focusSword.source())
        );
        final String statusText = localizedText(
            KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_GROWTH_STATUS_ACTIVE,
            "经验 %s / 耐久 %s/%s",
            focusView.experience(),
            Math.round(focusView.health()),
            Math.round(focusView.maxHealth())
        );
        final String progressText = localizedText(
            KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_GROWTH_PROGRESS_ACTIVE,
            "经验进度 %s（Lv.%s）",
            formatPercent(growthRatio),
            focusView.level()
        );
        final String growthAnchorBadge = focusView.benmingSword()
            ? localizedText(
                KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_GROWTH_BADGE_BENMING,
                "本命锚点"
            )
            : localizedText(
                KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_GROWTH_BADGE_NORMAL,
                "普通锚点"
            );
        return new CultivationRoute(
            SUBTAB_TITLES.get(GROWTH_ROUTE_INDEX),
            TacticalTone.BENMING,
            summaryText,
            statusText,
            progressText,
            growthRatio,
            List.of(
                localizedText(
                    KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_GROWTH_BADGE_EXP,
                    "经验 %s",
                    focusView.experience()
                ),
                localizedText(
                    KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_BADGE_PRESENT,
                    "在位 %s",
                    squadSummary.totalCount()
                ),
                growthAnchorBadge
            ),
            localizedText(
                KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_ACTION_OPEN_GROWTH_HELP,
                "打开成长帮助"
            ),
            RouteActionType.OPEN_GROWTH_HELP,
            true,
            false
        );
    }

    private static FlyingSwordTacticalStateService.FocusSword safeFocusSword(
        final FlyingSwordTacticalStateService.TacticalStateSnapshot snapshot
    ) {
        if (snapshot.focusSword() != null) {
            return snapshot.focusSword();
        }
        return new FlyingSwordTacticalStateService.FocusSword(
            FlyingSwordTacticalStateService.FocusSource.NONE,
            null
        );
    }

    private static FlyingSwordTacticalStateService.SquadSummary safeSquadSummary(
        final FlyingSwordTacticalStateService.TacticalStateSnapshot snapshot
    ) {
        if (snapshot.squadSummary() != null) {
            return snapshot.squadSummary();
        }
        return new FlyingSwordTacticalStateService.SquadSummary(0, 0, 0, 0, 0, 0, null);
    }

    private static ForgeSummary summarizeForge(
        @Nullable final FlyingSwordForgeAttachment forgeAttachment
    ) {
        if (forgeAttachment == null) {
            return null;
        }
        final int daoTypeCount = forgeAttachment.getDaoMarks().size();
        final int daoTotalScore = forgeAttachment.getDaoMarks().values().stream()
            .mapToInt(Integer::intValue)
            .sum();
        return new ForgeSummary(
            forgeAttachment.isActive(),
            forgeAttachment.getRequiredSwordCount(),
            forgeAttachment.getFedSwordCount(),
            daoTypeCount,
            daoTotalScore,
            forgeAttachment.getLastMessage()
        );
    }

    private static TrainingSummary summarizeTraining(
        @Nullable final FlyingSwordTrainingAttachment trainingAttachment
    ) {
        if (trainingAttachment == null) {
            return null;
        }
        return new TrainingSummary(
            trainingAttachment.getFuelTime(),
            trainingAttachment.getMaxFuelTime(),
            trainingAttachment.getAccumulatedExp()
        );
    }

    private static float ratioFromCounts(final int current, final int max) {
        if (max <= 0) {
            return ZERO_RATIO;
        }
        return clampRatio((float) current / (float) max);
    }

    private static float clampRatio(final float ratio) {
        if (ratio < ZERO_RATIO) {
            return ZERO_RATIO;
        }
        if (ratio > FULL_RATIO) {
            return FULL_RATIO;
        }
        return ratio;
    }

    private static String formatPercent(final float ratio) {
        return Math.round(clampRatio(ratio) * PERCENT_100) + "%";
    }

    private static String localizedText(
        final String key,
        final String fallback,
        final Object... args
    ) {
        final String localized = KongqiaoI18n.localizedTextWithBundledFallback(
            key,
            fallback,
            args
        );
        if (key != null && key.equals(localized)) {
            if (args == null || args.length == 0) {
                return fallback;
            }
            try {
                return String.format(Locale.ROOT, fallback, args);
            } catch (IllegalFormatException exception) {
                return fallback;
            }
        }
        return localized;
    }

    private static String fallbackText(
        @Nullable final String text,
        final String fallback
    ) {
        if (text == null || text.isBlank()) {
            return fallback;
        }
        return text.trim();
    }

    private static String describeFocusSword(
        final FlyingSwordTacticalStateService.FocusSword focusSword
    ) {
        return localizedText(
            KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FOCUS_DESC,
            "%s（%s）",
            focusSwordLabel(focusSword.sword()),
            focusSourceLabel(focusSword.source())
        );
    }

    private static String focusSwordLabel(
        @Nullable final FlyingSwordTacticalStateService.FlyingSwordViewModel focusView
    ) {
        if (focusView == null) {
            return localizedText(
                KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FOCUS_UNLOCKED,
                "未锁定"
            );
        }
        final String qualityName = focusView.quality() != null
            ? focusView.quality().getDisplayName()
            : localizedText(
                KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FOCUS_UNKNOWN_QUALITY,
                "未知品质"
            );
        return localizedText(
            KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FOCUS_LABEL,
            "%s Lv.%s",
            qualityName,
            focusView.level()
        );
    }

    private static String focusSourceLabel(
        @Nullable final FlyingSwordTacticalStateService.FocusSource source
    ) {
        final FlyingSwordTacticalStateService.FocusSource safeSource = source != null
            ? source
            : FlyingSwordTacticalStateService.FocusSource.NONE;
        return switch (safeSource) {
            case BENMING -> localizedText(
                KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FOCUS_SOURCE_BENMING,
                "本命"
            );
            case SELECTED -> localizedText(
                KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FOCUS_SOURCE_SELECTED,
                "选中飞剑"
            );
            case RECENT -> localizedText(
                KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FOCUS_SOURCE_RECENT,
                "最近飞剑"
            );
            case NONE -> localizedText(
                KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FOCUS_SOURCE_NONE,
                "无"
            );
        };
    }

    enum RouteActionType {
        OPEN_FORGE,
        OPEN_TRAINING,
        OPEN_CLUSTER,
        OPEN_GROWTH_HELP
    }

    record ForgeSummary(
        boolean active,
        int requiredSwordCount,
        int fedSwordCount,
        int daoTypeCount,
        int daoTotalScore,
        @Nullable String lastMessage
    ) {
    }

    record TrainingSummary(int fuelTime, int maxFuelTime, int accumulatedExp) {
    }

    record CultivationRoute(
        String title,
        TacticalTone tone,
        String summaryText,
        String statusText,
        String progressText,
        float progressRatio,
        List<String> resourceBadges,
        String actionText,
        RouteActionType actionType,
        boolean routeOnly,
        boolean opensOriginalScreen
    ) {

        CultivationRoute {
            title = fallbackText(title, SUBTAB_TITLES.get(DEFAULT_ROUTE_INDEX));
            tone = tone != null ? tone : TacticalTone.INFO;
            summaryText = fallbackText(
                summaryText,
                localizedText(KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FALLBACK_SUMMARY, "暂无摘要")
            );
            statusText = fallbackText(
                statusText,
                localizedText(KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FALLBACK_STATUS, "暂无状态")
            );
            progressText = fallbackText(
                progressText,
                localizedText(KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FALLBACK_PROGRESS, "进度 0%")
            );
            progressRatio = clampRatio(progressRatio);
            resourceBadges = sanitizeBadges(resourceBadges);
            actionText = fallbackText(
                actionText,
                localizedText(KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FALLBACK_ACTION, "打开原界面")
            );
            actionType = actionType != null ? actionType : RouteActionType.OPEN_GROWTH_HELP;
        }
    }

    record CultivationPanelState(List<CultivationRoute> routes) {

        CultivationPanelState {
            routes = List.copyOf(routes != null ? routes : List.of());
        }

        CultivationRoute routeAt(final int index) {
            if (routes.isEmpty()) {
                throw new IllegalStateException(
                    localizedText(
                        KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_ROUTES_REQUIRED,
                        "培养页路由不能为空"
                    )
                );
            }
            final int safeIndex = index < 0 || index >= routes.size()
                ? DEFAULT_ROUTE_INDEX
                : index;
            return routes.get(safeIndex);
        }
    }

    private static List<String> sanitizeBadges(@Nullable final List<String> badges) {
        if (badges == null || badges.isEmpty()) {
            return List.of(
                localizedText(
                    KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FALLBACK_BADGE,
                    "无资源摘要"
                )
            );
        }
        final List<String> sanitized = new ArrayList<>(badges.size());
        for (final String badge : badges) {
            sanitized.add(
                fallbackText(
                    badge,
                    localizedText(
                        KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FALLBACK_BADGE,
                        "无资源摘要"
                    )
                )
            );
        }
        return List.copyOf(sanitized);
    }
}
