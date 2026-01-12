package com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * 飞剑运行态附件（不 copyOnDeath）。
 * <p>
 * 用途：保存飞剑系统的短期运行态标记（例如：本次登录是否启用、临时调试开关等）。
 * </p>
 * <p>
 * 说明：运行态通常不应在死亡/克隆后继承，因此 AttachmentType 不启用 copyOnDeath。
 * </p>
 */
public class FlyingSwordRuntimeAttachment implements INBTSerializable<CompoundTag> {

    private static final String TAG_DEBUG_FORCE_ENABLED = "DebugForceEnabled";

    private boolean debugForceEnabled = false;

    public boolean isDebugForceEnabled() {
        return debugForceEnabled;
    }

    public void setDebugForceEnabled(final boolean debugForceEnabled) {
        this.debugForceEnabled = debugForceEnabled;
    }

    @Override
    public CompoundTag serializeNBT(final HolderLookup.Provider provider) {
        final CompoundTag tag = new CompoundTag();
        tag.putBoolean(TAG_DEBUG_FORCE_ENABLED, debugForceEnabled);
        return tag;
    }

    @Override
    public void deserializeNBT(final HolderLookup.Provider provider, final CompoundTag tag) {
        if (tag == null) {
            debugForceEnabled = false;
            return;
        }
        debugForceEnabled = tag.getBoolean(TAG_DEBUG_FORCE_ENABLED);
    }
}
