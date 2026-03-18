package com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * 飞剑状态附件（运行期短期状态）。
 * <p>
 * 用途：记录是否已初始化、是否需要强制刷新等标记，避免在每 tick 反复做昂贵的注册/扫描。
 * </p>
 */
public class FlyingSwordStateAttachment implements INBTSerializable<CompoundTag> {

    private static final String TAG_INITIALIZED = "Initialized";
    private static final String TAG_BONDED_SWORD_ID = "BondedSwordId";
    private static final String TAG_BOND_CACHE_DIRTY = "BondCacheDirty";
    private static final String TAG_LAST_RESOLVED_TICK = "LastResolvedTick";
    private static final String TAG_RESONANCE_TYPE = "ResonanceType";
    private static final String TAG_OVERLOAD = "Overload";
    private static final String TAG_BURST_COOLDOWN_UNTIL_TICK = "BurstCooldownUntilTick";
    private static final String TAG_BURST_ACTIVE_UNTIL_TICK = "BurstActiveUntilTick";
    private static final String TAG_BURST_AFTERSHOCK_UNTIL_TICK =
        "BurstAftershockUntilTick";
    private static final String TAG_RITUAL_LOCK_UNTIL_TICK = "RitualLockUntilTick";
    private static final String TAG_RESONANCE_LEVEL = "ResonanceLevel";
    private static final String TAG_LAST_OVERLOAD_TICK = "LastOverloadTick";
    private static final String TAG_OVERLOAD_BACKLASH_UNTIL_TICK =
        "OverloadBacklashUntilTick";
    private static final String TAG_OVERLOAD_RECOVERY_UNTIL_TICK =
        "OverloadRecoveryUntilTick";
    private static final String TAG_LAST_COMBAT_TICK = "LastCombatTick";
    private static final long UNRESOLVED_TICK = -1L;

    private boolean initialized = false;
    private String bondedSwordId = "";

    private boolean bondCacheDirty = true;

    private long lastResolvedTick = UNRESOLVED_TICK;

    private String resonanceType = "";
    private double overload = 0.0D;
    private long burstCooldownUntilTick = 0L;
    private long burstActiveUntilTick = 0L;
    private long burstAftershockUntilTick = 0L;
    private long ritualLockUntilTick = 0L;
    private int resonanceLevel = 0;
    private long lastOverloadTick = 0L;
    private long overloadBacklashUntilTick = 0L;
    private long overloadRecoveryUntilTick = 0L;
    private long lastCombatTick = 0L;

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(final boolean initialized) {
        this.initialized = initialized;
    }

    public String getBondedSwordId() {
        return bondedSwordId;
    }

    public boolean isBondCacheDirty() {
        return bondCacheDirty;
    }

    public long getLastResolvedTick() {
        return lastResolvedTick;
    }

    public String getResonanceType() {
        return resonanceType;
    }

    public void setResonanceType(final String resonanceType) {
        this.resonanceType = normalizeSwordId(resonanceType);
    }

    public double getOverload() {
        return overload;
    }

    public void setOverload(final double overload) {
        this.overload = normalizeNonNegativeDouble(overload);
    }

    public long getBurstCooldownUntilTick() {
        return burstCooldownUntilTick;
    }

    public void setBurstCooldownUntilTick(final long burstCooldownUntilTick) {
        this.burstCooldownUntilTick = normalizeNonNegativeLong(burstCooldownUntilTick);
    }

    public long getBurstActiveUntilTick() {
        return burstActiveUntilTick;
    }

    public void setBurstActiveUntilTick(final long burstActiveUntilTick) {
        this.burstActiveUntilTick = normalizeNonNegativeLong(burstActiveUntilTick);
    }

    public long getBurstAftershockUntilTick() {
        return burstAftershockUntilTick;
    }

    public void setBurstAftershockUntilTick(final long burstAftershockUntilTick) {
        this.burstAftershockUntilTick = normalizeNonNegativeLong(
            burstAftershockUntilTick
        );
    }

    public long getRitualLockUntilTick() {
        return ritualLockUntilTick;
    }

    public void setRitualLockUntilTick(final long ritualLockUntilTick) {
        this.ritualLockUntilTick = normalizeNonNegativeLong(ritualLockUntilTick);
    }

    public int getResonanceLevel() {
        return resonanceLevel;
    }

    public void setResonanceLevel(final int resonanceLevel) {
        this.resonanceLevel = normalizeNonNegativeInt(resonanceLevel);
    }

    public long getLastOverloadTick() {
        return lastOverloadTick;
    }

    public void setLastOverloadTick(final long lastOverloadTick) {
        this.lastOverloadTick = normalizeNonNegativeLong(lastOverloadTick);
    }

    public long getOverloadBacklashUntilTick() {
        return overloadBacklashUntilTick;
    }

    public void setOverloadBacklashUntilTick(final long overloadBacklashUntilTick) {
        this.overloadBacklashUntilTick =
            normalizeNonNegativeLong(overloadBacklashUntilTick);
    }

    public long getOverloadRecoveryUntilTick() {
        return overloadRecoveryUntilTick;
    }

    public void setOverloadRecoveryUntilTick(final long overloadRecoveryUntilTick) {
        this.overloadRecoveryUntilTick =
            normalizeNonNegativeLong(overloadRecoveryUntilTick);
    }

    public long getLastCombatTick() {
        return lastCombatTick;
    }

    public void setLastCombatTick(final long lastCombatTick) {
        this.lastCombatTick = normalizeNonNegativeLong(lastCombatTick);
    }

    public void updateBondCache(final String stableSwordId, final long resolvedTick) {
        bondedSwordId = normalizeSwordId(stableSwordId);
        lastResolvedTick = resolvedTick;
        bondCacheDirty = false;
    }

    public void markBondCacheDirty() {
        bondCacheDirty = true;
    }

    public void clearBondCache() {
        bondedSwordId = "";
        lastResolvedTick = UNRESOLVED_TICK;
        bondCacheDirty = true;
    }

    @Override
    public CompoundTag serializeNBT(final HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(TAG_INITIALIZED, initialized);
        tag.putString(TAG_BONDED_SWORD_ID, bondedSwordId);
        tag.putBoolean(TAG_BOND_CACHE_DIRTY, bondCacheDirty);
        tag.putLong(TAG_LAST_RESOLVED_TICK, lastResolvedTick);
        tag.putString(TAG_RESONANCE_TYPE, resonanceType);
        tag.putDouble(TAG_OVERLOAD, overload);
        tag.putLong(TAG_BURST_COOLDOWN_UNTIL_TICK, burstCooldownUntilTick);
        tag.putLong(TAG_BURST_ACTIVE_UNTIL_TICK, burstActiveUntilTick);
        tag.putLong(TAG_BURST_AFTERSHOCK_UNTIL_TICK, burstAftershockUntilTick);
        tag.putLong(TAG_RITUAL_LOCK_UNTIL_TICK, ritualLockUntilTick);
        tag.putInt(TAG_RESONANCE_LEVEL, resonanceLevel);
        tag.putLong(TAG_LAST_OVERLOAD_TICK, lastOverloadTick);
        tag.putLong(TAG_OVERLOAD_BACKLASH_UNTIL_TICK, overloadBacklashUntilTick);
        tag.putLong(TAG_OVERLOAD_RECOVERY_UNTIL_TICK, overloadRecoveryUntilTick);
        tag.putLong(TAG_LAST_COMBAT_TICK, lastCombatTick);
        return tag;
    }

    @Override
    public void deserializeNBT(final HolderLookup.Provider provider, final CompoundTag tag) {
        if (tag == null) {
            initialized = false;
            bondedSwordId = "";
            bondCacheDirty = true;
            lastResolvedTick = UNRESOLVED_TICK;
            resonanceType = "";
            overload = 0.0D;
            burstCooldownUntilTick = 0L;
            burstActiveUntilTick = 0L;
            burstAftershockUntilTick = 0L;
            ritualLockUntilTick = 0L;
            resonanceLevel = 0;
            lastOverloadTick = 0L;
            overloadBacklashUntilTick = 0L;
            overloadRecoveryUntilTick = 0L;
            lastCombatTick = 0L;
            return;
        }
        initialized = tag.getBoolean(TAG_INITIALIZED);
        bondedSwordId = tag.contains(TAG_BONDED_SWORD_ID)
            ? normalizeSwordId(tag.getString(TAG_BONDED_SWORD_ID))
            : "";
        bondCacheDirty = tag.contains(TAG_BOND_CACHE_DIRTY)
            ? tag.getBoolean(TAG_BOND_CACHE_DIRTY)
            : true;
        lastResolvedTick = tag.contains(TAG_LAST_RESOLVED_TICK)
            ? tag.getLong(TAG_LAST_RESOLVED_TICK)
            : UNRESOLVED_TICK;
        resonanceType = tag.contains(TAG_RESONANCE_TYPE)
            ? normalizeSwordId(tag.getString(TAG_RESONANCE_TYPE))
            : "";
        overload = tag.contains(TAG_OVERLOAD)
            ? normalizeNonNegativeDouble(tag.getDouble(TAG_OVERLOAD))
            : 0.0D;
        burstCooldownUntilTick = tag.contains(TAG_BURST_COOLDOWN_UNTIL_TICK)
            ? normalizeNonNegativeLong(tag.getLong(TAG_BURST_COOLDOWN_UNTIL_TICK))
            : 0L;
        burstActiveUntilTick = tag.contains(TAG_BURST_ACTIVE_UNTIL_TICK)
            ? normalizeNonNegativeLong(tag.getLong(TAG_BURST_ACTIVE_UNTIL_TICK))
            : 0L;
        burstAftershockUntilTick = tag.contains(TAG_BURST_AFTERSHOCK_UNTIL_TICK)
            ? normalizeNonNegativeLong(tag.getLong(TAG_BURST_AFTERSHOCK_UNTIL_TICK))
            : 0L;
        ritualLockUntilTick = tag.contains(TAG_RITUAL_LOCK_UNTIL_TICK)
            ? normalizeNonNegativeLong(tag.getLong(TAG_RITUAL_LOCK_UNTIL_TICK))
            : 0L;
        resonanceLevel = tag.contains(TAG_RESONANCE_LEVEL)
            ? normalizeNonNegativeInt(tag.getInt(TAG_RESONANCE_LEVEL))
            : 0;
        lastOverloadTick = tag.contains(TAG_LAST_OVERLOAD_TICK)
            ? normalizeNonNegativeLong(tag.getLong(TAG_LAST_OVERLOAD_TICK))
            : 0L;
        overloadBacklashUntilTick = tag.contains(TAG_OVERLOAD_BACKLASH_UNTIL_TICK)
            ? normalizeNonNegativeLong(tag.getLong(TAG_OVERLOAD_BACKLASH_UNTIL_TICK))
            : 0L;
        overloadRecoveryUntilTick = tag.contains(TAG_OVERLOAD_RECOVERY_UNTIL_TICK)
            ? normalizeNonNegativeLong(tag.getLong(TAG_OVERLOAD_RECOVERY_UNTIL_TICK))
            : 0L;
        lastCombatTick = tag.contains(TAG_LAST_COMBAT_TICK)
            ? normalizeNonNegativeLong(tag.getLong(TAG_LAST_COMBAT_TICK))
            : 0L;
    }

    private static String normalizeSwordId(final String swordId) {
        return swordId == null ? "" : swordId;
    }

    private static double normalizeNonNegativeDouble(final double value) {
        return value < 0.0D ? 0.0D : value;
    }

    private static long normalizeNonNegativeLong(final long value) {
        return value < 0L ? 0L : value;
    }

    private static int normalizeNonNegativeInt(final int value) {
        return value < 0 ? 0 : value;
    }
}
