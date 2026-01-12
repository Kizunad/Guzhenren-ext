package com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * 飞剑偏好附件（copyOnDeath）。
 * <p>
 * 用途：保存玩家对飞剑系统的 UI/控制偏好（不属于运行态）。
 * 当前阶段先提供最小骨架，后续再扩展具体字段。
 * </p>
 */
public class FlyingSwordPreferencesAttachment implements INBTSerializable<CompoundTag> {

    private static final String TAG_ENABLED = "Enabled";

    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public CompoundTag serializeNBT(final HolderLookup.Provider provider) {
        final CompoundTag tag = new CompoundTag();
        tag.putBoolean(TAG_ENABLED, enabled);
        return tag;
    }

    @Override
    public void deserializeNBT(final HolderLookup.Provider provider, final CompoundTag tag) {
        if (tag == null) {
            enabled = true;
            return;
        }
        enabled = tag.getBoolean(TAG_ENABLED);
    }
}
