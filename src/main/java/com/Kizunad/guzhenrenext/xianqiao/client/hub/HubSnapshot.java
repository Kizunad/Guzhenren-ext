package com.Kizunad.guzhenrenext.xianqiao.client.hub;

import com.Kizunad.guzhenrenext.xianqiao.block.ApertureHubMenu;
import com.Kizunad.guzhenrenext.xianqiao.resource.ResourceControllerMenu;
import com.Kizunad.guzhenrenext.xianqiao.spirit.LandSpiritMenu;
import com.Kizunad.guzhenrenext.xianqiao.service.SpiritUnlockService;
import java.util.Objects;

public record HubSnapshot(
    CoreSnapshot core,
    LandSpiritSnapshot landSpirit,
    ResourceSnapshot resource
) {

    public static final String RESOURCE_ROUTE_FALLBACK_TEXT = "待前往资源分台查看";
    public static final String SPIRIT_ROUTE_FALLBACK_TEXT = "待前往地灵分台查看";
    public static final String RESOURCE_LOCAL_SUMMARY_NOTICE = "资源为局部采样，需前往资源分台核验";

    public HubSnapshot {
        core = Objects.requireNonNull(core, "core");
        landSpirit = Objects.requireNonNull(landSpirit, "landSpirit");
        resource = Objects.requireNonNull(resource, "resource");
    }

    public static HubSnapshot fromApertureMenu(ApertureHubMenu apertureMenu) {
        final CoreSnapshot coreSnapshot = CoreSnapshot.fromApertureMenu(apertureMenu);
        return new HubSnapshot(
            coreSnapshot,
            LandSpiritSnapshot.fromCoreSnapshot(coreSnapshot),
            ResourceSnapshot.routeFallback(RESOURCE_ROUTE_FALLBACK_TEXT)
        );
    }

    public static HubSnapshot fromCore(CoreSnapshot coreSnapshot) {
        return new HubSnapshot(
            coreSnapshot,
            LandSpiritSnapshot.missing(SPIRIT_ROUTE_FALLBACK_TEXT),
            ResourceSnapshot.routeFallback(RESOURCE_ROUTE_FALLBACK_TEXT)
        );
    }

    public HubSnapshot withLandSpiritMenu(LandSpiritMenu landSpiritMenu) {
        return withLandSpirit(LandSpiritSnapshot.fromLandSpiritMenu(landSpiritMenu));
    }

    public HubSnapshot withLandSpirit(LandSpiritSnapshot landSpiritSnapshot) {
        Objects.requireNonNull(landSpiritSnapshot, "landSpiritSnapshot");
        return new HubSnapshot(core, landSpiritSnapshot, resource);
    }

    public HubSnapshot withResourceMenu(ResourceControllerMenu resourceMenu) {
        return withResource(ResourceSnapshot.conservativeFromResourceMenu(resourceMenu));
    }

    public HubSnapshot withResource(ResourceSnapshot resourceSnapshot) {
        Objects.requireNonNull(resourceSnapshot, "resourceSnapshot");
        return new HubSnapshot(core, landSpirit, resourceSnapshot);
    }

    public enum DataClass {
        REAL_CORE,
        REAL_SUMMARY,
        SUMMARY_ROUTE
    }

    public record CoreSnapshot(
        DataClass dataClass,
        int minChunkX,
        int maxChunkX,
        int minChunkZ,
        int maxChunkZ,
        int chunkSpanX,
        int chunkSpanZ,
        int timeSpeedPercent,
        int favorabilityPercent,
        int tier,
        boolean frozen,
        long tribulationTick
    ) {

        public CoreSnapshot {
            if (dataClass != DataClass.REAL_CORE) {
                throw new IllegalArgumentException("CoreSnapshot 必须标记为 REAL_CORE");
            }
        }

        public static CoreSnapshot fromApertureMenu(ApertureHubMenu apertureMenu) {
            Objects.requireNonNull(apertureMenu, "apertureMenu");
            return new CoreSnapshot(
                DataClass.REAL_CORE,
                apertureMenu.getMinChunkX(),
                apertureMenu.getMaxChunkX(),
                apertureMenu.getMinChunkZ(),
                apertureMenu.getMaxChunkZ(),
                apertureMenu.getChunkSpanX(),
                apertureMenu.getChunkSpanZ(),
                apertureMenu.getTimeSpeedPercent(),
                apertureMenu.getFavorabilityPercent(),
                apertureMenu.getTier(),
                apertureMenu.isFrozen(),
                apertureMenu.getTribulationTick()
            );
        }

        public boolean hasValidBoundary() {
            return maxChunkX >= minChunkX && maxChunkZ >= minChunkZ;
        }
    }

    public enum RealSummaryState {
        AVAILABLE,
        MISSING
    }

    public record LandSpiritSnapshot(
        DataClass dataClass,
        RealSummaryState state,
        int favorabilityPermille,
        int tier,
        int stage,
        int nextStageMinTier,
        int nextStageMinFavorabilityPermille,
        String fallbackText
    ) {

        public LandSpiritSnapshot {
            Objects.requireNonNull(state, "state");
            Objects.requireNonNull(fallbackText, "fallbackText");
            if (dataClass != DataClass.REAL_SUMMARY) {
                throw new IllegalArgumentException("LandSpiritSnapshot 必须标记为 REAL_SUMMARY");
            }
            if (fallbackText.isBlank()) {
                throw new IllegalArgumentException("fallbackText 不能为空");
            }
        }

        public static LandSpiritSnapshot missing(String fallbackText) {
            return new LandSpiritSnapshot(
                DataClass.REAL_SUMMARY,
                RealSummaryState.MISSING,
                0,
                0,
                0,
                0,
                0,
                fallbackText
            );
        }

        public static LandSpiritSnapshot fromLandSpiritMenu(LandSpiritMenu landSpiritMenu) {
            Objects.requireNonNull(landSpiritMenu, "landSpiritMenu");
            return new LandSpiritSnapshot(
                DataClass.REAL_SUMMARY,
                RealSummaryState.AVAILABLE,
                landSpiritMenu.getFavorabilityPermille(),
                landSpiritMenu.getTier(),
                landSpiritMenu.getCurrentStage(),
                landSpiritMenu.getNextStageMinTier(),
                landSpiritMenu.getNextStageMinFavorabilityPermille(),
                SPIRIT_ROUTE_FALLBACK_TEXT
            );
        }

        public static LandSpiritSnapshot fromCoreSnapshot(CoreSnapshot coreSnapshot) {
            Objects.requireNonNull(coreSnapshot, "coreSnapshot");
            final int favorabilityPermille = Math.round(
                coreSnapshot.favorabilityPercent() / FAVORABILITY_PERCENT_DIVISOR
            );
            final int currentStage = SpiritUnlockService.computeStage(
                coreSnapshot.tier(),
                coreSnapshot.favorabilityPercent() / PERCENT_BASE
            );
            final int nextStage = SpiritUnlockService.getNextStage(currentStage);
            return new LandSpiritSnapshot(
                DataClass.REAL_SUMMARY,
                RealSummaryState.AVAILABLE,
                favorabilityPermille,
                coreSnapshot.tier(),
                currentStage,
                SpiritUnlockService.getMinTierForStage(nextStage),
                Math.round(
                    SpiritUnlockService.getMinFavorabilityForStage(nextStage)
                        * FAVORABILITY_PERCENT_TO_PERMILLE_FACTOR
                ),
                SPIRIT_ROUTE_FALLBACK_TEXT
            );
        }

        public boolean isAvailable() {
            return state == RealSummaryState.AVAILABLE;
        }
    }

    private static final float PERCENT_BASE = 100.0F;

    private static final float FAVORABILITY_PERCENT_DIVISOR = 10.0F;

    private static final float FAVORABILITY_PERCENT_TO_PERMILLE_FACTOR = 10.0F;

    public enum SummaryRouteState {
        ROUTE_FALLBACK,
        CONSERVATIVE_LOCAL_SUMMARY
    }

    public record ResourceSnapshot(
        DataClass dataClass,
        SummaryRouteState state,
        boolean formed,
        int progressPermille,
        int efficiencyPercent,
        int auraValue,
        int remainingTicks,
        String fallbackText
    ) {

        public ResourceSnapshot {
            Objects.requireNonNull(state, "state");
            Objects.requireNonNull(fallbackText, "fallbackText");
            if (dataClass != DataClass.SUMMARY_ROUTE) {
                throw new IllegalArgumentException("ResourceSnapshot 必须标记为 SUMMARY_ROUTE");
            }
            if (fallbackText.isBlank()) {
                throw new IllegalArgumentException("fallbackText 不能为空");
            }
        }

        public static ResourceSnapshot routeFallback(String fallbackText) {
            return new ResourceSnapshot(
                DataClass.SUMMARY_ROUTE,
                SummaryRouteState.ROUTE_FALLBACK,
                false,
                0,
                0,
                0,
                0,
                fallbackText
            );
        }

        public static ResourceSnapshot conservativeFromResourceMenu(ResourceControllerMenu resourceMenu) {
            Objects.requireNonNull(resourceMenu, "resourceMenu");
            return new ResourceSnapshot(
                DataClass.SUMMARY_ROUTE,
                SummaryRouteState.CONSERVATIVE_LOCAL_SUMMARY,
                resourceMenu.isFormed(),
                resourceMenu.getProgressPermille(),
                resourceMenu.getEfficiencyPercent(),
                resourceMenu.getAuraValue(),
                resourceMenu.getRemainingTicks(),
                RESOURCE_LOCAL_SUMMARY_NOTICE
            );
        }

        public boolean isRouteFallback() {
            return state == SummaryRouteState.ROUTE_FALLBACK;
        }
    }
}
