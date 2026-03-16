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
    private static final long UNRESOLVED_TICK = -1L;

    private boolean initialized = false;
    private String bondedSwordId = "";

    private boolean bondCacheDirty = true;

    private long lastResolvedTick = UNRESOLVED_TICK;

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
        return tag;
    }

    @Override
    public void deserializeNBT(final HolderLookup.Provider provider, final CompoundTag tag) {
        if (tag == null) {
            initialized = false;
            bondedSwordId = "";
            bondCacheDirty = true;
            lastResolvedTick = UNRESOLVED_TICK;
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
    }

    private static String normalizeSwordId(final String swordId) {
        return swordId == null ? "" : swordId;
    }
}
