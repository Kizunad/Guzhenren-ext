package com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * 飞剑选择附件。
 * <p>
 * 用途：记录玩家当前“选中的”飞剑（通常为实体 UUID）。
 * </p>
 */
public class FlyingSwordSelectionAttachment implements INBTSerializable<CompoundTag> {

    private static final String TAG_SELECTED = "Selected";

    private UUID selectedSword;

    public Optional<UUID> getSelectedSword() {
        return Optional.ofNullable(selectedSword);
    }

    public void setSelectedSword(final UUID id) {
        this.selectedSword = id;
    }

    public void clear() {
        this.selectedSword = null;
    }

    @Override
    public CompoundTag serializeNBT(final HolderLookup.Provider provider) {
        final CompoundTag tag = new CompoundTag();
        if (selectedSword != null) {
            tag.putUUID(TAG_SELECTED, selectedSword);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(final HolderLookup.Provider provider, final CompoundTag tag) {
        if (tag != null && tag.hasUUID(TAG_SELECTED)) {
            selectedSword = tag.getUUID(TAG_SELECTED);
        } else {
            selectedSword = null;
        }
    }
}
