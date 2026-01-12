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

    private boolean initialized = false;

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(final boolean initialized) {
        this.initialized = initialized;
    }

    @Override
    public CompoundTag serializeNBT(final HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(TAG_INITIALIZED, initialized);
        return tag;
    }

    @Override
    public void deserializeNBT(final HolderLookup.Provider provider, final CompoundTag tag) {
        if (tag == null) {
            initialized = false;
            return;
        }
        initialized = tag.getBoolean(TAG_INITIALIZED);
    }
}
