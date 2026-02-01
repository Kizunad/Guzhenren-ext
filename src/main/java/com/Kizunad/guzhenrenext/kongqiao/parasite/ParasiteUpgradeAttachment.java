package com.Kizunad.guzhenrenext.kongqiao.parasite;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * 玩家寄生升级附件。
 * <p>
 * 负责持久化玩家已解锁的寄生升级节点与等级。
 * </p>
 */
public class ParasiteUpgradeAttachment implements INBTSerializable<CompoundTag> {

    private final ParasiteUpgradeData data = new ParasiteUpgradeData();

    public ParasiteUpgradeAttachment() {}

    public ParasiteUpgradeData getData() {
        return data;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return data.serializeNBT(provider);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        data.deserializeNBT(provider, nbt);
    }
}
