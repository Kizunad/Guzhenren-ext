package com.Kizunad.guzhenrenext.kongqiao.service;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoData;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouDataManager;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouUnlockChecker;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouUsageId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class KongqiaoPressureProjectionService {

    static final String BLOCKED_REASON_PASSIVE_OVERLOAD = "passive_overload";

    private static final double BONUS_ROW_PRESSURE_CAP = 6.0D;
    private static final double PASSIVE_DURATION_PRESSURE_DIVISOR = 200.0D;
    private static final double PASSIVE_TOTAL_NIANTOU_PRESSURE_DIVISOR = 50.0D;
    private static final double PRESSURE_CAP_CANCI = 18.0D;
    private static final double PRESSURE_CAP_XIADENG = 28.0D;
    private static final double PRESSURE_CAP_ZHONGDENG = 40.0D;
    private static final double PRESSURE_CAP_SHANGDENG = 56.0D;
    private static final double PRESSURE_CAP_JUEPIN = 72.0D;
    private static final double STABLE_RATIO_UPPER_BOUND = 0.80D;
    private static final double TENSE_RATIO_UPPER_BOUND = 1.00D;
    private static final double OVERLOADED_RATIO_UPPER_BOUND = 1.20D;
    private static final double UNCONTROLLED_RATIO_UPPER_BOUND = 1.50D;
    private static final int OVERLOAD_TIER_STABLE = 0;
    private static final int OVERLOAD_TIER_TENSE = 1;
    private static final int OVERLOAD_TIER_OVERLOADED = 2;
    private static final int OVERLOAD_TIER_UNCONTROLLED = 3;
    private static final int OVERLOAD_TIER_COLLAPSE_EDGE = 4;
    private static final Comparator<PassiveRuntimeCandidate> FORCE_DISABLE_ORDER =
        Comparator.comparingInt(PassiveRuntimeCandidate::passivePressure)
            .reversed()
            .thenComparing(PassiveRuntimeCandidate::usageId);

    private KongqiaoPressureProjectionService() {}

    /**
     * 组装投影（包含容量信息）。
     * <p>
     * 使用 {@link KongqiaoCapacityBridge} 计算容量信息。
     * </p>
     *
     * @param data 空窍数据
     * @param entity 玩家实体，用于计算容量
     * @return 压力与容量投影
     */
    public static KongqiaoPressureProjection assembleProjection(
        final KongqiaoData data,
        final LivingEntity entity
    ) {
        // 先组装压力投影
        KongqiaoPressureProjection pressureProjection = assemblePressureProjection(
            data,
            entity
        );
        // 再计算容量投影
        KongqiaoCapacityProfile capacityProfile = KongqiaoCapacityBridge.resolveFromEntity(entity);

        return new KongqiaoPressureProjection(
            // 压力字段
            pressureProjection.totalPressure(),
            pressureProjection.effectivePressure(),
            pressureProjection.pressureCap(),
            pressureProjection.residentPressure(),
            pressureProjection.passivePressure(),
            pressureProjection.wheelReservePressure(),
            pressureProjection.burstPressure(),
            pressureProjection.fatigueDebt(),
            pressureProjection.overloadTier(),
            pressureProjection.blockedReason(),
            pressureProjection.sealedSlotCount(),
            pressureProjection.forcedDisabledCount(),
            // 容量字段（Task 3）
            capacityProfile.aptitudeTier().displayName(),
            capacityProfile.apertureRank(),
            capacityProfile.apertureStage(),
            capacityProfile.baseRows(),
            capacityProfile.bonusRows(),
            capacityProfile.totalRows()
        );
    }

    /**
     * 组装投影（仅压力信息，不含容量）。
     * <p>
     * 此方法保留用于向后兼容。
     * </p>
     *
     * @param data 空窍数据
     * @return 压力与容量投影
     * @deprecated 请使用 {@link #assembleProjection(KongqiaoData, LivingEntity)} 以获取完整容量信息
     */
    @Deprecated
    public static KongqiaoPressureProjection assembleProjection(final KongqiaoData data) {
        return assemblePressureProjection(data);
    }

    /**
     * 组装压力投影（不包含容量信息）。
     * <p>
     * 此方法返回仅包含压力字段的投影，容量字段为空。
     * </p>
     *
     * @param data 空窍数据
     * @return 压力投影
     */
    public static KongqiaoPressureProjection assemblePressureProjection(
        final KongqiaoData data
    ) {
        if (data == null) {
            return KongqiaoPressureProjection.empty();
        }
        final KongqiaoData.StabilityState stabilityState =
            data.getStabilityState();
        final double residentPressure = 0.0D;
        final double passivePressure = 0.0D;
        final double wheelReservePressure = 0.0D;
        final double burstPressure = stabilityState.getBurstPressure();
        final double fatigueDebt = stabilityState.getFatigueDebt();
        final double effectivePressure =
            residentPressure
                + passivePressure
                + wheelReservePressure
                + burstPressure
                + fatigueDebt;

        // Task 3 已落地，但此重载不包含 entity，故返回空容量字段
        return new KongqiaoPressureProjection(
            effectivePressure,
            effectivePressure,
            0.0D,
            residentPressure,
            passivePressure,
            wheelReservePressure,
            burstPressure,
            fatigueDebt,
            stabilityState.getOverloadTier(),
            "",
            stabilityState.getSealedSlots().size(),
            stabilityState.getForcedDisabledUsageIds().size(),
            // 容量字段默认为空
            "",
            0,
            0,
            0,
            0,
            0
        );
    }

    public static KongqiaoPressureProjection assemblePressureProjection(
        final KongqiaoData data,
        final LivingEntity entity
    ) {
        if (data == null) {
            return KongqiaoPressureProjection.empty();
        }
        if (entity == null || data.getKongqiaoInventory() == null) {
            return assemblePressureProjection(data);
        }

        final PassiveRuntimeSnapshot passiveRuntimeSnapshot =
            resolvePassiveRuntimeSnapshot(
                entity,
                data.getKongqiaoInventory(),
                data.getKongqiaoInventory().getSettings().getUnlockedSlots()
            );
        syncPassiveRuntimeState(data, passiveRuntimeSnapshot);

        final KongqiaoData.StabilityState stabilityState = data.getStabilityState();
        final double residentPressure = 0.0D;
        final double passivePressure = passiveRuntimeSnapshot.passivePressure();
        final double wheelReservePressure = 0.0D;
        final double burstPressure = stabilityState.getBurstPressure();
        final double fatigueDebt = stabilityState.getFatigueDebt();
        final double effectivePressure =
            residentPressure
                + passivePressure
                + wheelReservePressure
                + burstPressure
                + fatigueDebt;

        return new KongqiaoPressureProjection(
            effectivePressure,
            effectivePressure,
            passiveRuntimeSnapshot.pressureCap(),
            residentPressure,
            passivePressure,
            wheelReservePressure,
            burstPressure,
            fatigueDebt,
            stabilityState.getOverloadTier(),
            passiveRuntimeSnapshot.blockedReason(),
            stabilityState.getSealedSlots().size(),
            stabilityState.getForcedDisabledUsageIds().size(),
            "",
            0,
            0,
            0,
            0,
            0
        );
    }

    static void syncPassiveRuntimeState(
        final KongqiaoData data,
        final PassiveRuntimeSnapshot snapshot
    ) {
        if (data == null || snapshot == null) {
            return;
        }
        final KongqiaoData.StabilityState stabilityState = data.getStabilityState();
        if (stabilityState == null) {
            return;
        }
        stabilityState.setOverloadTier(snapshot.overloadTier());
        stabilityState.setForcedDisabledUsageIds(snapshot.forcedDisabledUsageIds());
    }

    static PassiveRuntimeSnapshot resolvePassiveRuntimeSnapshot(
        final LivingEntity user,
        final Container container,
        final int slotCount
    ) {
        if (user == null || container == null || slotCount <= 0) {
            return PassiveRuntimeSnapshot.empty();
        }
        final KongqiaoData data = KongqiaoAttachments.getData(user);
        final KongqiaoData.StabilityState stabilityState = data == null
            ? null
            : data.getStabilityState();
        final Set<Integer> sealedSlots = stabilityState == null
            ? Set.of()
            : stabilityState.getSealedSlots();
        return evaluatePassiveRuntimeSnapshot(
            collectPassiveRuntimeCandidates(user, container, slotCount, sealedSlots),
            KongqiaoCapacityBridge.resolveFromEntity(user),
            stabilityState == null ? 0.0D : stabilityState.getBurstPressure(),
            stabilityState == null ? 0.0D : stabilityState.getFatigueDebt()
        );
    }

    static PassiveRuntimeSnapshot evaluatePassiveRuntimeSnapshot(
        final Collection<PassiveRuntimeCandidate> candidates,
        final KongqiaoCapacityProfile capacityProfile,
        final double burstPressure,
        final double fatigueDebt
    ) {
        final List<PassiveRuntimeCandidate> normalizedCandidates = new ArrayList<>();
        if (candidates != null) {
            normalizedCandidates.addAll(candidates);
        }

        double passivePressure = 0.0D;
        final LinkedHashSet<String> runnableUsageIds = new LinkedHashSet<>();
        for (PassiveRuntimeCandidate candidate : normalizedCandidates) {
            if (candidate == null) {
                continue;
            }
            passivePressure += candidate.passivePressure();
            runnableUsageIds.add(candidate.usageId());
        }

        final double normalizedBurstPressure = normalizeNonNegativeDouble(
            burstPressure
        );
        final double normalizedFatigueDebt = normalizeNonNegativeDouble(
            fatigueDebt
        );
        final double pressureCap = computeTask5PressureCap(capacityProfile);
        final double effectivePressure =
            passivePressure + normalizedBurstPressure + normalizedFatigueDebt;
        final int overloadTier = determineOverloadTier(
            effectivePressure,
            pressureCap
        );

        final LinkedHashSet<String> forcedDisabledUsageIds = new LinkedHashSet<>();
        if (overloadTier >= OVERLOAD_TIER_OVERLOADED) {
            final List<PassiveRuntimeCandidate> orderedCandidates = new ArrayList<>(
                normalizedCandidates
            );
            orderedCandidates.sort(FORCE_DISABLE_ORDER);

            double runnablePassivePressure = passivePressure;
            for (PassiveRuntimeCandidate candidate : orderedCandidates) {
                if (
                    determineOverloadTier(
                        runnablePassivePressure
                            + normalizedBurstPressure
                            + normalizedFatigueDebt,
                        pressureCap
                    ) < OVERLOAD_TIER_OVERLOADED
                ) {
                    break;
                }
                runnablePassivePressure -= candidate.passivePressure();
                forcedDisabledUsageIds.add(candidate.usageId());
                runnableUsageIds.remove(candidate.usageId());
            }
        }

        return new PassiveRuntimeSnapshot(
            passivePressure,
            pressureCap,
            effectivePressure,
            overloadTier,
            forcedDisabledUsageIds.isEmpty()
                ? ""
                : BLOCKED_REASON_PASSIVE_OVERLOAD,
            forcedDisabledUsageIds,
            runnableUsageIds
        );
    }

    static int computePassivePressureCore(final NianTouData.Usage usage) {
        if (usage == null) {
            return 0;
        }
        final int durationCost = Math.max(0, usage.costDuration());
        final int totalNiantouCost = Math.max(0, usage.costTotalNiantou());
        return 1
            + (int) Math.ceil(durationCost / PASSIVE_DURATION_PRESSURE_DIVISOR)
            + (int) Math.ceil(
                totalNiantouCost / PASSIVE_TOTAL_NIANTOU_PRESSURE_DIVISOR
            );
    }

    private static List<PassiveRuntimeCandidate> collectPassiveRuntimeCandidates(
        final LivingEntity user,
        final Container container,
        final int slotCount,
        final Set<Integer> sealedSlots
    ) {
        return collapseSlotRuntimeCandidatesByUsage(
            collectSlotPassiveRuntimeCandidates(user, container, slotCount),
            sealedSlots
        );
    }

    static List<SlotPassiveRuntimeCandidate> collectSlotPassiveRuntimeCandidates(
        final LivingEntity user,
        final Container container,
        final int slotCount
    ) {
        final TweakConfig config = KongqiaoAttachments.getTweakConfig(user);
        final List<SlotPassiveRuntimeCandidate> slotCandidates = new ArrayList<>();
        if (user == null || container == null || slotCount <= 0) {
            return slotCandidates;
        }
        final int maxSlots = Math.min(slotCount, container.getContainerSize());
        for (int slot = 0; slot < maxSlots; slot++) {
            final ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            final NianTouData niantouData = NianTouDataManager.getData(stack);
            if (niantouData == null || niantouData.usages() == null) {
                continue;
            }
            for (NianTouData.Usage usage : niantouData.usages()) {
                if (usage == null) {
                    continue;
                }
                final String usageId = usage.usageID();
                if (!NianTouUsageId.isPassive(usageId)) {
                    continue;
                }
                if (config != null && !config.isPassiveEnabled(usageId)) {
                    continue;
                }
                if (!NianTouUnlockChecker.isUsageUnlocked(user, stack, usageId)) {
                    continue;
                }
                slotCandidates.add(
                    new SlotPassiveRuntimeCandidate(
                        slot,
                        usageId,
                        computePassivePressureCore(usage)
                    )
                );
            }
        }
        return slotCandidates;
    }

    static List<PassiveRuntimeCandidate> collapseSlotRuntimeCandidatesByUsage(
        final Collection<SlotPassiveRuntimeCandidate> slotCandidates,
        final Set<Integer> sealedSlots
    ) {
        final LinkedHashMap<String, Integer> pressureByUsageId = new LinkedHashMap<>();
        final Set<Integer> normalizedSealedSlots = sealedSlots == null
            ? Set.of()
            : sealedSlots;
        if (slotCandidates != null) {
            for (SlotPassiveRuntimeCandidate candidate : slotCandidates) {
                if (candidate == null || normalizedSealedSlots.contains(candidate.slot())) {
                    continue;
                }
                pressureByUsageId.merge(
                    candidate.usageId(),
                    candidate.passivePressure(),
                    Integer::sum
                );
            }
        }

        final List<PassiveRuntimeCandidate> candidates = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : pressureByUsageId.entrySet()) {
            candidates.add(
                new PassiveRuntimeCandidate(entry.getKey(), entry.getValue())
            );
        }
        return candidates;
    }

    private static double computeTask5PressureCap(
        final KongqiaoCapacityProfile capacityProfile
    ) {
        if (capacityProfile == null || capacityProfile.aptitudeTier() == null) {
            return 0.0D;
        }
        return basePressureCapByAptitude(capacityProfile.aptitudeTier())
            + Math.max(0, capacityProfile.bonusRows()) * BONUS_ROW_PRESSURE_CAP;
    }

    private static double basePressureCapByAptitude(
        final KongqiaoAptitudeTier aptitudeTier
    ) {
        if (aptitudeTier == null) {
            return 0.0D;
        }
        return switch (aptitudeTier) {
            case CANCI -> PRESSURE_CAP_CANCI;
            case XIADENG -> PRESSURE_CAP_XIADENG;
            case ZHONGDENG -> PRESSURE_CAP_ZHONGDENG;
            case SHANGDENG -> PRESSURE_CAP_SHANGDENG;
            case JUEPIN -> PRESSURE_CAP_JUEPIN;
        };
    }

    static int determineOverloadTier(
        final double effectivePressure,
        final double pressureCap
    ) {
        final double normalizedEffectivePressure = normalizeNonNegativeDouble(
            effectivePressure
        );
        final double normalizedPressureCap = normalizeNonNegativeDouble(pressureCap);
        if (normalizedPressureCap <= 0.0D) {
            return normalizedEffectivePressure <= 0.0D
                ? OVERLOAD_TIER_STABLE
                : OVERLOAD_TIER_COLLAPSE_EDGE;
        }
        final double ratio = normalizedEffectivePressure / normalizedPressureCap;
        if (ratio <= STABLE_RATIO_UPPER_BOUND) {
            return OVERLOAD_TIER_STABLE;
        }
        if (ratio <= TENSE_RATIO_UPPER_BOUND) {
            return OVERLOAD_TIER_TENSE;
        }
        if (ratio <= OVERLOADED_RATIO_UPPER_BOUND) {
            return OVERLOAD_TIER_OVERLOADED;
        }
        if (ratio <= UNCONTROLLED_RATIO_UPPER_BOUND) {
            return OVERLOAD_TIER_UNCONTROLLED;
        }
        return OVERLOAD_TIER_COLLAPSE_EDGE;
    }

    static boolean isTenseOrBetter(final int overloadTier) {
        return Math.max(0, overloadTier) <= OVERLOAD_TIER_TENSE;
    }

    static boolean isCollapseEdgeOrWorse(final int overloadTier) {
        return Math.max(0, overloadTier) >= OVERLOAD_TIER_COLLAPSE_EDGE;
    }

    public static boolean isOverloadedOrWorse(final int overloadTier) {
        return Math.max(0, overloadTier) >= OVERLOAD_TIER_OVERLOADED;
    }

    static boolean wouldProjectedPressureReachOverload(
        final double currentEffectivePressure,
        final double pressureCap,
        final double additionalPressure
    ) {
        return isOverloadedOrWorse(
            determineOverloadTier(
                normalizeNonNegativeDouble(currentEffectivePressure)
                    + normalizeNonNegativeDouble(additionalPressure),
                pressureCap
            )
        );
    }

    private static double normalizeNonNegativeDouble(final double value) {
        return value < 0.0D ? 0.0D : value;
    }

    static record PassiveRuntimeCandidate(String usageId, int passivePressure) {

        PassiveRuntimeCandidate {
            usageId = usageId == null ? "" : usageId;
            passivePressure = Math.max(0, passivePressure);
        }
    }

    static record SlotPassiveRuntimeCandidate(int slot, String usageId, int passivePressure) {

        SlotPassiveRuntimeCandidate {
            slot = Math.max(0, slot);
            usageId = usageId == null ? "" : usageId;
            passivePressure = Math.max(0, passivePressure);
        }
    }

    static record PassiveRuntimeSnapshot(
        double passivePressure,
        double pressureCap,
        double effectivePressure,
        int overloadTier,
        String blockedReason,
        Set<String> forcedDisabledUsageIds,
        Set<String> runnableUsageIds
    ) {

        PassiveRuntimeSnapshot {
            passivePressure = normalizeNonNegativeDouble(passivePressure);
            pressureCap = normalizeNonNegativeDouble(pressureCap);
            effectivePressure = normalizeNonNegativeDouble(effectivePressure);
            overloadTier = Math.max(0, overloadTier);
            blockedReason = blockedReason == null ? "" : blockedReason;
            forcedDisabledUsageIds = immutableUsageIdSet(forcedDisabledUsageIds);
            runnableUsageIds = immutableUsageIdSet(runnableUsageIds);
        }

        static PassiveRuntimeSnapshot empty() {
            return new PassiveRuntimeSnapshot(
                0.0D,
                0.0D,
                0.0D,
                OVERLOAD_TIER_STABLE,
                "",
                Set.of(),
                Set.of()
            );
        }

        boolean isForcedDisabled(final String usageId) {
            return forcedDisabledUsageIds.contains(usageId);
        }

        private static Set<String> immutableUsageIdSet(final Set<String> usageIds) {
            if (usageIds == null || usageIds.isEmpty()) {
                return Set.of();
            }
            final LinkedHashSet<String> normalized = new LinkedHashSet<>();
            for (String usageId : usageIds) {
                if (usageId == null || usageId.isBlank()) {
                    continue;
                }
                normalized.add(usageId);
            }
            return Collections.unmodifiableSet(normalized);
        }
    }
}
