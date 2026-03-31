package com.Kizunad.guzhenrenext.faction.integration;

import com.Kizunad.guzhenrenext.faction.core.FactionCore;
import com.Kizunad.guzhenrenext.faction.core.FactionMembership;
import com.Kizunad.guzhenrenext.faction.service.FactionService;
import com.Kizunad.guzhenrenext.xianqiao.tribulation.TribulationManager;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;

public final class FactionAscensionModifier {

    private static final int RELATION_HOSTILE_THRESHOLD = -50;
    private static final int RESOURCE_TIER_UNIT = 100;
    private static final int POWER_TIER_UNIT = 100;
    private static final int RESOURCE_SCORE_BONUS_STEP = 2;
    private static final int POWER_SCORE_BONUS_STEP = 1;
    private static final int RESOURCE_READY_THRESHOLD_REDUCTION_STEP = 1;
    private static final int RESOURCE_BALANCE_THRESHOLD_REDUCTION_STEP = 1;
    private static final int MAX_READINESS_SCORE_BONUS = 18;
    private static final int MAX_READY_THRESHOLD_REDUCTION = 8;
    private static final int MAX_BALANCE_THRESHOLD_REDUCTION = 6;
    private static final double BASE_MULTIPLIER = 1.0D;
    private static final double SECT_PROTECTION_INTENSITY_MULTIPLIER = 0.85D;
    private static final double CLAN_PROTECTION_INTENSITY_MULTIPLIER = 0.90D;
    private static final double HOSTILE_INTENSITY_PER_FACTION = 0.08D;
    private static final double HOSTILE_INTENSITY_PER_SEVERITY_POINT = 0.001D;
    private static final double HOSTILE_INVASION_PER_FACTION = 0.15D;
    private static final double HOSTILE_INVASION_PER_SEVERITY_POINT = 0.002D;
    private static final double MIN_INTENSITY_MULTIPLIER = 0.50D;
    private static final double MAX_INTENSITY_MULTIPLIER = 2.50D;
    private static final double MIN_INVASION_MULTIPLIER = 0.75D;
    private static final double MAX_INVASION_MULTIPLIER = 3.00D;

    public ReadinessModifier resolveReadinessModifier(ServerLevel level, UUID playerId) {
        return evaluateSnapshot(resolveSnapshot(level, playerId)).readinessModifier();
    }

    public TribulationManager.ExternalTribulationModifier resolveTribulationModifier(
        ServerLevel level,
        UUID ownerId
    ) {
        TribulationModifier modifier = evaluateSnapshot(resolveSnapshot(level, ownerId)).tribulationModifier();
        return new TribulationManager.ExternalTribulationModifier(
            modifier.intensityMultiplier(),
            modifier.invasionSpawnMultiplier()
        );
    }

    public static ModifierBundle evaluateSnapshot(FactionInfluenceSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");

        ReadinessModifier readinessModifier = resolveReadinessModifier(snapshot);
        TribulationModifier tribulationModifier = resolveTribulationModifier(snapshot);
        return new ModifierBundle(readinessModifier, tribulationModifier);
    }

    private static ReadinessModifier resolveReadinessModifier(FactionInfluenceSnapshot snapshot) {
        if (!snapshot.affiliated()) {
            return ReadinessModifier.neutral();
        }
        int resourceTier = Math.max(0, snapshot.factionResources() / RESOURCE_TIER_UNIT);
        int powerTier = Math.max(0, snapshot.factionPower() / POWER_TIER_UNIT);

        int scoreBonus = clampInt(
            resourceTier * RESOURCE_SCORE_BONUS_STEP + powerTier * POWER_SCORE_BONUS_STEP,
            0,
            MAX_READINESS_SCORE_BONUS
        );
        int readyThresholdReduction = clampInt(
            resourceTier * RESOURCE_READY_THRESHOLD_REDUCTION_STEP,
            0,
            MAX_READY_THRESHOLD_REDUCTION
        );
        int balanceThresholdReduction = clampInt(
            resourceTier * RESOURCE_BALANCE_THRESHOLD_REDUCTION_STEP,
            0,
            MAX_BALANCE_THRESHOLD_REDUCTION
        );
        return new ReadinessModifier(scoreBonus, readyThresholdReduction, balanceThresholdReduction);
    }

    private static TribulationModifier resolveTribulationModifier(FactionInfluenceSnapshot snapshot) {
        double intensityMultiplier = BASE_MULTIPLIER;
        double invasionMultiplier = BASE_MULTIPLIER;

        if (snapshot.affiliated()) {
            if (snapshot.factionType() == FactionCore.FactionType.SECT) {
                intensityMultiplier *= SECT_PROTECTION_INTENSITY_MULTIPLIER;
            } else if (snapshot.factionType() == FactionCore.FactionType.CLAN) {
                intensityMultiplier *= CLAN_PROTECTION_INTENSITY_MULTIPLIER;
            }

            if (snapshot.hostileFactionCount() > 0 || snapshot.hostilitySeverity() > 0) {
                double hostileIntensityFactor = BASE_MULTIPLIER
                    + snapshot.hostileFactionCount() * HOSTILE_INTENSITY_PER_FACTION
                    + snapshot.hostilitySeverity() * HOSTILE_INTENSITY_PER_SEVERITY_POINT;
                double hostileInvasionFactor = BASE_MULTIPLIER
                    + snapshot.hostileFactionCount() * HOSTILE_INVASION_PER_FACTION
                    + snapshot.hostilitySeverity() * HOSTILE_INVASION_PER_SEVERITY_POINT;
                intensityMultiplier *= hostileIntensityFactor;
                invasionMultiplier *= hostileInvasionFactor;
            }
        }

        return new TribulationModifier(
            clampDouble(intensityMultiplier, MIN_INTENSITY_MULTIPLIER, MAX_INTENSITY_MULTIPLIER),
            clampDouble(invasionMultiplier, MIN_INVASION_MULTIPLIER, MAX_INVASION_MULTIPLIER)
        );
    }

    private static FactionInfluenceSnapshot resolveSnapshot(ServerLevel level, UUID playerId) {
        if (level == null || playerId == null) {
            return FactionInfluenceSnapshot.unaffiliated();
        }
        ServerLevel authorityLevel = resolveFactionAuthorityLevel(level);
        FactionMembership membership = findPlayerMembership(authorityLevel, playerId);
        if (membership == null) {
            return FactionInfluenceSnapshot.unaffiliated();
        }
        FactionCore faction = FactionService.getFaction(authorityLevel, membership.factionId());
        if (faction == null) {
            return FactionInfluenceSnapshot.unaffiliated();
        }

        int hostileFactionCount = 0;
        int hostilitySeverity = 0;
        for (FactionCore otherFaction : FactionService.getAllFactions(authorityLevel)) {
            if (otherFaction.id().equals(faction.id())) {
                continue;
            }
            int relation = FactionService.getRelation(authorityLevel, faction.id(), otherFaction.id());
            if (relation < RELATION_HOSTILE_THRESHOLD) {
                hostileFactionCount++;
                hostilitySeverity += Math.abs(relation);
            }
        }

        return new FactionInfluenceSnapshot(
            true,
            faction.type(),
            faction.power(),
            faction.resources(),
            hostileFactionCount,
            hostilitySeverity
        );
    }

    private static FactionMembership findPlayerMembership(ServerLevel level, UUID playerId) {
        for (FactionCore faction : FactionService.getAllFactions(level)) {
            for (FactionMembership membership : FactionService.getMembers(level, faction.id())) {
                if (playerId.equals(membership.memberId())) {
                    return membership;
                }
            }
        }
        return null;
    }

    private static ServerLevel resolveFactionAuthorityLevel(ServerLevel currentLevel) {
        ServerLevel overworld = currentLevel.getServer().overworld();
        if (overworld != null) {
            return overworld;
        }
        return currentLevel;
    }

    private static int clampInt(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }

    private static double clampDouble(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }

    public record ModifierBundle(
        ReadinessModifier readinessModifier,
        TribulationModifier tribulationModifier
    ) {

        public ModifierBundle {
            readinessModifier = Objects.requireNonNull(readinessModifier, "readinessModifier");
            tribulationModifier = Objects.requireNonNull(tribulationModifier, "tribulationModifier");
        }
    }

    public record ReadinessModifier(
        int scoreBonus,
        int readyThresholdReduction,
        int balanceThresholdReduction
    ) {

        public ReadinessModifier {
            scoreBonus = Math.max(0, scoreBonus);
            readyThresholdReduction = Math.max(0, readyThresholdReduction);
            balanceThresholdReduction = Math.max(0, balanceThresholdReduction);
        }

        public static ReadinessModifier neutral() {
            return new ReadinessModifier(0, 0, 0);
        }
    }

    public record TribulationModifier(
        double intensityMultiplier,
        double invasionSpawnMultiplier
    ) {

        public TribulationModifier {
            intensityMultiplier = clampDouble(
                intensityMultiplier,
                MIN_INTENSITY_MULTIPLIER,
                MAX_INTENSITY_MULTIPLIER
            );
            invasionSpawnMultiplier = clampDouble(
                invasionSpawnMultiplier,
                MIN_INVASION_MULTIPLIER,
                MAX_INVASION_MULTIPLIER
            );
        }
    }

    public record FactionInfluenceSnapshot(
        boolean affiliated,
        FactionCore.FactionType factionType,
        int factionPower,
        int factionResources,
        int hostileFactionCount,
        int hostilitySeverity
    ) {

        public FactionInfluenceSnapshot {
            factionType = Objects.requireNonNull(factionType, "factionType");
            factionPower = Math.max(0, factionPower);
            factionResources = Math.max(0, factionResources);
            hostileFactionCount = Math.max(0, hostileFactionCount);
            hostilitySeverity = Math.max(0, hostilitySeverity);
            if (!affiliated) {
                factionPower = 0;
                factionResources = 0;
                hostileFactionCount = 0;
                hostilitySeverity = 0;
            }
        }

        public static FactionInfluenceSnapshot unaffiliated() {
            return new FactionInfluenceSnapshot(false, FactionCore.FactionType.ROGUE_GROUP, 0, 0, 0, 0);
        }
    }
}
