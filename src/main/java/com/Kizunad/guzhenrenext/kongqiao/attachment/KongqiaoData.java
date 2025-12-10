package com.Kizunad.guzhenrenext.kongqiao.attachment;

import com.Kizunad.guzhenrenext.kongqiao.KongqiaoOwner;
import com.Kizunad.guzhenrenext.kongqiao.inventory.AttackInventory;
import com.Kizunad.guzhenrenext.kongqiao.inventory.GuchongFeedInventory;
import com.Kizunad.guzhenrenext.kongqiao.inventory.KongqiaoInventory;
import com.Kizunad.guzhenrenext.kongqiao.service.KongqiaoService;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * 附着在实体上的空窍数据。
 */
public class KongqiaoData
    implements KongqiaoOwner, INBTSerializable<CompoundTag> {

    private final KongqiaoInventory kongqiaoInventory;
    private final AttackInventory attackInventory;
    private final GuchongFeedInventory feedInventory;
    private UUID ownerId = Util.NIL_UUID;
    private boolean clientSide;
    private boolean dirty;

    public KongqiaoData() {
        this.kongqiaoInventory = KongqiaoService.createInventory();
        this.attackInventory = new AttackInventory();
        this.feedInventory = new GuchongFeedInventory();
        this.kongqiaoInventory.setChangeListener(() -> dirty = true);
        this.attackInventory.setChangeListener(() -> dirty = true);
        this.feedInventory.setChangeListener(() -> dirty = true);
    }

    public void bind(Entity entity) {
        if (entity != null) {
            this.ownerId = entity.getUUID();
            this.clientSide = entity.level().isClientSide();
        } else {
            this.ownerId = Util.NIL_UUID;
            this.clientSide = false;
        }
    }

    @Override
    public KongqiaoInventory getKongqiaoInventory() {
        return kongqiaoInventory;
    }

    @Override
    public AttackInventory getAttackInventory() {
        return attackInventory;
    }

    @Override
    public GuchongFeedInventory getFeedInventory() {
        return feedInventory;
    }

    @Override
    public void markKongqiaoDirty() {
        dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void clearDirty() {
        dirty = false;
    }

    @Override
    public UUID getKongqiaoId() {
        return ownerId;
    }

    @Override
    public boolean isClientSide() {
        return clientSide;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.put("kongqiao", kongqiaoInventory.serializeNBT(provider));
        tag.put("attack", attackInventory.serializeNBT(provider));
        tag.put("feed", feedInventory.serializeNBT(provider));
        return tag;
    }

    @Override
    public void deserializeNBT(
        HolderLookup.Provider provider,
        CompoundTag tag
    ) {
        if (tag.contains("kongqiao")) {
            kongqiaoInventory.deserializeNBT(
                provider,
                tag.getCompound("kongqiao")
            );
        }
        if (tag.contains("attack")) {
            attackInventory.deserializeNBT(provider, tag.getCompound("attack"));
        }
        if (tag.contains("feed")) {
            feedInventory.deserializeNBT(provider, tag.getCompound("feed"));
        }
    }
}
