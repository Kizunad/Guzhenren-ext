package com.Kizunad.guzhenrenext.kongqiao.service;

import net.minecraft.nbt.CompoundTag;

/**
 * 空窍压力与容量投影。
 * <p>
 * 该记录承载服务端权威的压力状态和容量状态，供客户端只读消费。
 * 容量字段来自 Task 3 的权威桥接层 {@link KongqiaoCapacityBridge}。
 * </p>
 */
public record KongqiaoPressureProjection(
    // 压力相关字段
    double totalPressure,
    double effectivePressure,
    double pressureCap,
    double residentPressure,
    double passivePressure,
    double wheelReservePressure,
    double burstPressure,
    double fatigueDebt,
    int overloadTier,
    String blockedReason,
    int sealedSlotCount,
    int forcedDisabledCount,
    // 容量相关字段（Task 3）
    String aptitudeTier,
    int apertureRank,
    int apertureStage,
    int baseRows,
    int bonusRows,
    int totalRows
) {

    private static final String TAG_TOTAL_PRESSURE = "totalPressure";
    private static final String TAG_EFFECTIVE_PRESSURE = "effectivePressure";
    private static final String TAG_PRESSURE_CAP = "pressureCap";
    private static final String TAG_RESIDENT_PRESSURE = "residentPressure";
    private static final String TAG_PASSIVE_PRESSURE = "passivePressure";
    private static final String TAG_WHEEL_RESERVE_PRESSURE =
        "wheelReservePressure";
    private static final String TAG_BURST_PRESSURE = "burstPressure";
    private static final String TAG_FATIGUE_DEBT = "fatigueDebt";
    private static final String TAG_OVERLOAD_TIER = "overloadTier";
    private static final String TAG_BLOCKED_REASON = "blockedReason";
    private static final String TAG_SEALED_SLOT_COUNT = "sealedSlotCount";
    private static final String TAG_FORCED_DISABLED_COUNT =
        "forcedDisabledCount";
    // Task 3 容量字段标签
    private static final String TAG_APTITUDE_TIER = "aptitudeTier";
    private static final String TAG_APERTURE_RANK = "apertureRank";
    private static final String TAG_APERTURE_STAGE = "apertureStage";
    private static final String TAG_BASE_ROWS = "baseRows";
    private static final String TAG_BONUS_ROWS = "bonusRows";
    private static final String TAG_TOTAL_ROWS = "totalRows";

    public KongqiaoPressureProjection {
        // 压力字段归一化
        totalPressure = normalizeNonNegativeDouble(totalPressure);
        effectivePressure = normalizeNonNegativeDouble(effectivePressure);
        pressureCap = normalizeNonNegativeDouble(pressureCap);
        residentPressure = normalizeNonNegativeDouble(residentPressure);
        passivePressure = normalizeNonNegativeDouble(passivePressure);
        wheelReservePressure = normalizeNonNegativeDouble(wheelReservePressure);
        burstPressure = normalizeNonNegativeDouble(burstPressure);
        fatigueDebt = normalizeNonNegativeDouble(fatigueDebt);
        overloadTier = normalizeNonNegativeInt(overloadTier);
        blockedReason = blockedReason == null ? "" : blockedReason;
        sealedSlotCount = normalizeNonNegativeInt(sealedSlotCount);
        forcedDisabledCount = normalizeNonNegativeInt(
            forcedDisabledCount
        );
        // 容量字段归一化
        aptitudeTier = aptitudeTier == null ? "" : aptitudeTier;
        apertureRank = normalizeNonNegativeInt(apertureRank);
        apertureStage = normalizeNonNegativeInt(apertureStage);
        baseRows = normalizeNonNegativeInt(baseRows);
        bonusRows = normalizeNonNegativeInt(bonusRows);
        totalRows = normalizeNonNegativeInt(totalRows);
    }

    public static KongqiaoPressureProjection empty() {
        return new KongqiaoPressureProjection(
            // 压力字段
            0.0D,
            0.0D,
            0.0D,
            0.0D,
            0.0D,
            0.0D,
            0.0D,
            0.0D,
            0,
            "",
            0,
            0,
            // 容量字段（Task 3）
            "",
            0,
            0,
            0,
            0,
            0
        );
    }

    public CompoundTag toTag() {
        final CompoundTag tag = new CompoundTag();
        // 压力字段
        tag.putDouble(TAG_TOTAL_PRESSURE, totalPressure);
        tag.putDouble(TAG_EFFECTIVE_PRESSURE, effectivePressure);
        tag.putDouble(TAG_PRESSURE_CAP, pressureCap);
        tag.putDouble(TAG_RESIDENT_PRESSURE, residentPressure);
        tag.putDouble(TAG_PASSIVE_PRESSURE, passivePressure);
        tag.putDouble(TAG_WHEEL_RESERVE_PRESSURE, wheelReservePressure);
        tag.putDouble(TAG_BURST_PRESSURE, burstPressure);
        tag.putDouble(TAG_FATIGUE_DEBT, fatigueDebt);
        tag.putInt(TAG_OVERLOAD_TIER, overloadTier);
        tag.putString(TAG_BLOCKED_REASON, blockedReason);
        tag.putInt(TAG_SEALED_SLOT_COUNT, sealedSlotCount);
        tag.putInt(TAG_FORCED_DISABLED_COUNT, forcedDisabledCount);
        // 容量字段（Task 3）
        tag.putString(TAG_APTITUDE_TIER, aptitudeTier);
        tag.putInt(TAG_APERTURE_RANK, apertureRank);
        tag.putInt(TAG_APERTURE_STAGE, apertureStage);
        tag.putInt(TAG_BASE_ROWS, baseRows);
        tag.putInt(TAG_BONUS_ROWS, bonusRows);
        tag.putInt(TAG_TOTAL_ROWS, totalRows);
        return tag;
    }

    public static KongqiaoPressureProjection fromTag(final CompoundTag tag) {
        if (tag == null) {
            return empty();
        }
        return new KongqiaoPressureProjection(
            // 压力字段
            tag.contains(TAG_TOTAL_PRESSURE)
                ? tag.getDouble(TAG_TOTAL_PRESSURE)
                : 0.0D,
            tag.contains(TAG_EFFECTIVE_PRESSURE)
                ? tag.getDouble(TAG_EFFECTIVE_PRESSURE)
                : 0.0D,
            tag.contains(TAG_PRESSURE_CAP)
                ? tag.getDouble(TAG_PRESSURE_CAP)
                : 0.0D,
            tag.contains(TAG_RESIDENT_PRESSURE)
                ? tag.getDouble(TAG_RESIDENT_PRESSURE)
                : 0.0D,
            tag.contains(TAG_PASSIVE_PRESSURE)
                ? tag.getDouble(TAG_PASSIVE_PRESSURE)
                : 0.0D,
            tag.contains(TAG_WHEEL_RESERVE_PRESSURE)
                ? tag.getDouble(TAG_WHEEL_RESERVE_PRESSURE)
                : 0.0D,
            tag.contains(TAG_BURST_PRESSURE)
                ? tag.getDouble(TAG_BURST_PRESSURE)
                : 0.0D,
            tag.contains(TAG_FATIGUE_DEBT)
                ? tag.getDouble(TAG_FATIGUE_DEBT)
                : 0.0D,
            tag.contains(TAG_OVERLOAD_TIER)
                ? tag.getInt(TAG_OVERLOAD_TIER)
                : 0,
            tag.contains(TAG_BLOCKED_REASON)
                ? tag.getString(TAG_BLOCKED_REASON)
                : "",
            tag.contains(TAG_SEALED_SLOT_COUNT)
                ? tag.getInt(TAG_SEALED_SLOT_COUNT)
                : 0,
            tag.contains(TAG_FORCED_DISABLED_COUNT)
                ? tag.getInt(TAG_FORCED_DISABLED_COUNT)
                : 0,
            // 容量字段（Task 3）
            tag.contains(TAG_APTITUDE_TIER)
                ? tag.getString(TAG_APTITUDE_TIER)
                : "",
            tag.contains(TAG_APERTURE_RANK)
                ? tag.getInt(TAG_APERTURE_RANK)
                : 0,
            tag.contains(TAG_APERTURE_STAGE)
                ? tag.getInt(TAG_APERTURE_STAGE)
                : 0,
            tag.contains(TAG_BASE_ROWS)
                ? tag.getInt(TAG_BASE_ROWS)
                : 0,
            tag.contains(TAG_BONUS_ROWS)
                ? tag.getInt(TAG_BONUS_ROWS)
                : 0,
            tag.contains(TAG_TOTAL_ROWS)
                ? tag.getInt(TAG_TOTAL_ROWS)
                : 0
        );
    }

    private static double normalizeNonNegativeDouble(final double value) {
        return value < 0.0D ? 0.0D : value;
    }

    private static int normalizeNonNegativeInt(final int value) {
        return value < 0 ? 0 : value;
    }
}
