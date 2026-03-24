package com.Kizunad.guzhenrenext.kongqiao.flyingsword.client;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ai.SwordAIMode;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.quality.SwordQuality;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.resonance.FlyingSwordResonanceType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

public final class FlyingSwordTacticalStateService {

    private FlyingSwordTacticalStateService() {}

    public static TacticalStateSnapshot snapshotFromRoster(
        List<FlyingSwordHudState.SwordDisplayData> distanceOrderedRoster,
        @Nullable UUID selectedSwordId
    ) {
        final List<FlyingSwordHudState.SwordDisplayData> safeRoster =
            distanceOrderedRoster == null ? List.of() : distanceOrderedRoster;
        final List<FlyingSwordViewModel> distanceOrderedViews = toViewList(safeRoster, selectedSwordId);
        final List<FlyingSwordViewModel> visibleWindow = toVisibleWindow(safeRoster, selectedSwordId);

        final FocusSword focusSword = resolveFocusSword(distanceOrderedViews);
        final SquadSummary squadSummary = buildSquadSummary(distanceOrderedViews);
        final HubOverview hubOverview = new HubOverview(
            visibleWindow,
            !visibleWindow.isEmpty() && visibleWindow.stream().anyMatch(FlyingSwordViewModel::benmingSword)
        );
        final HelpSignals helpSignals = buildHelpSignals(squadSummary.benmingSummary());
        return new TacticalStateSnapshot(focusSword, squadSummary, hubOverview, helpSignals);
    }

    private static List<FlyingSwordViewModel> toVisibleWindow(
        List<FlyingSwordHudState.SwordDisplayData> distanceOrderedRoster,
        @Nullable UUID selectedSwordId
    ) {
        final List<FlyingSwordHudState.SwordDisplayData> window =
            FlyingSwordHudState.buildVisibleDisplayWindow(distanceOrderedRoster);
        return toViewList(window, selectedSwordId);
    }

    private static List<FlyingSwordViewModel> toViewList(
        List<FlyingSwordHudState.SwordDisplayData> rows,
        @Nullable UUID selectedSwordId
    ) {
        final List<FlyingSwordViewModel> views = new ArrayList<>(rows.size());
        for (FlyingSwordHudState.SwordDisplayData row : rows) {
            views.add(FlyingSwordViewModel.from(row, selectedSwordId));
        }
        return List.copyOf(views);
    }

    private static FocusSword resolveFocusSword(List<FlyingSwordViewModel> distanceOrderedViews) {
        final FlyingSwordViewModel benmingSword = findFirstBenming(distanceOrderedViews);
        if (benmingSword != null) {
            return new FocusSword(FocusSource.BENMING, benmingSword);
        }

        final FlyingSwordViewModel selectedSword = findFirstSelected(distanceOrderedViews);
        if (selectedSword != null) {
            return new FocusSword(FocusSource.SELECTED, selectedSword);
        }

        if (!distanceOrderedViews.isEmpty()) {
            return new FocusSword(FocusSource.RECENT, distanceOrderedViews.get(0));
        }
        return new FocusSword(FocusSource.NONE, null);
    }

    @Nullable
    private static FlyingSwordViewModel findFirstBenming(List<FlyingSwordViewModel> views) {
        for (FlyingSwordViewModel view : views) {
            if (view.benmingSword()) {
                return view;
            }
        }
        return null;
    }

    @Nullable
    private static FlyingSwordViewModel findFirstSelected(List<FlyingSwordViewModel> views) {
        for (FlyingSwordViewModel view : views) {
            if (view.selected()) {
                return view;
            }
        }
        return null;
    }

    private static SquadSummary buildSquadSummary(List<FlyingSwordViewModel> distanceOrderedViews) {
        int benmingCount = 0;
        int selectedCount = 0;
        int warningCount = 0;
        int dangerCount = 0;
        int burstReadyCount = 0;

        for (FlyingSwordViewModel view : distanceOrderedViews) {
            if (view.benmingSword()) {
                benmingCount++;
            }
            if (view.selected()) {
                selectedCount++;
            }
            if (view.highlightWarning()) {
                warningCount++;
            }
            if (view.overloadDanger()) {
                dangerCount++;
            }
            if (view.burstReady()) {
                burstReadyCount++;
            }
        }

        return new SquadSummary(
            distanceOrderedViews.size(),
            benmingCount,
            selectedCount,
            warningCount,
            dangerCount,
            burstReadyCount,
            toBenmingSummary(findFirstBenming(distanceOrderedViews))
        );
    }

    private static HelpSignals buildHelpSignals(@Nullable BenmingSummary benmingSummary) {
        if (benmingSummary == null) {
            return HelpSignals.EMPTY;
        }
        return new HelpSignals(
            true,
            benmingSummary.highlightWarning(),
            benmingSummary.overloadDanger(),
            benmingSummary.overloadBacklashActive(),
            benmingSummary.overloadRecoveryActive(),
            benmingSummary.aftershockPeriod(),
            benmingSummary.burstReady()
        );
    }

    @Nullable
    private static BenmingSummary toBenmingSummary(@Nullable FlyingSwordViewModel benmingSword) {
        if (benmingSword == null) {
            return null;
        }
        return new BenmingSummary(
            benmingSword.uuid(),
            benmingSword.resonanceType(),
            benmingSword.overloadPercent(),
            benmingSword.highlightWarning(),
            benmingSword.overloadDanger(),
            benmingSword.overloadBacklashActive(),
            benmingSword.overloadRecoveryActive(),
            benmingSword.aftershockPeriod(),
            benmingSword.burstReady()
        );
    }

    public enum FocusSource {
        NONE,
        RECENT,
        SELECTED,
        BENMING
    }

    public record TacticalStateSnapshot(
        FocusSword focusSword,
        SquadSummary squadSummary,
        HubOverview hubOverview,
        HelpSignals helpSignals
    ) {}

    public record FocusSword(
        FocusSource source,
        @Nullable FlyingSwordViewModel sword
    ) {}

    public record SquadSummary(
        int totalCount,
        int benmingCount,
        int selectedCount,
        int warningCount,
        int dangerCount,
        int burstReadyCount,
        @Nullable BenmingSummary benmingSummary
    ) {}

    public record BenmingSummary(
        UUID swordUuid,
        @Nullable FlyingSwordResonanceType resonanceType,
        float overloadPercent,
        boolean highlightWarning,
        boolean overloadDanger,
        boolean overloadBacklashActive,
        boolean overloadRecoveryActive,
        boolean aftershockPeriod,
        boolean burstReady
    ) {}

    public record HubOverview(
        List<FlyingSwordViewModel> visibleDisplayWindow,
        boolean benmingInsideWindow
    ) {}

    public record HelpSignals(
        boolean hasBenmingSword,
        boolean hasOverloadWarning,
        boolean hasOverloadDanger,
        boolean hasOverloadBacklash,
        boolean hasOverloadRecovery,
        boolean hasAftershock,
        boolean hasBurstReady
    ) {

        private static final HelpSignals EMPTY = new HelpSignals(
            false,
            false,
            false,
            false,
            false,
            false,
            false
        );
    }

    public record FlyingSwordViewModel(
        int entityId,
        UUID uuid,
        SwordQuality quality,
        int level,
        int experience,
        float expProgress,
        SwordAIMode aiMode,
        float distance,
        float health,
        float maxHealth,
        float healthPercent,
        boolean selected,
        boolean benmingSword,
        @Nullable FlyingSwordResonanceType resonanceType,
        float overloadPercent,
        boolean burstReady,
        boolean aftershockPeriod,
        boolean overloadBacklashActive,
        boolean overloadRecoveryActive,
        boolean highlightWarning,
        boolean overloadDanger
    ) {

        private static FlyingSwordViewModel from(
            FlyingSwordHudState.SwordDisplayData data,
            @Nullable UUID selectedSwordId
        ) {
            final boolean selectedByAuthority =
                selectedSwordId != null && selectedSwordId.equals(data.uuid);
            final boolean selected = selectedByAuthority || data.isSelected;
            return new FlyingSwordViewModel(
                data.entityId,
                data.uuid,
                data.quality,
                data.level,
                data.experience,
                data.expProgress,
                data.aiMode,
                data.distance,
                data.health,
                data.maxHealth,
                data.getHealthPercent(),
                selected,
                data.isBenmingSword,
                data.benmingResonanceType,
                data.overloadPercent,
                data.isBurstReady,
                data.isAftershockPeriod,
                data.isOverloadBacklashActive,
                data.isOverloadRecoveryActive,
                data.shouldHighlightWarning,
                data.isOverloadDanger
            );
        }
    }
}
