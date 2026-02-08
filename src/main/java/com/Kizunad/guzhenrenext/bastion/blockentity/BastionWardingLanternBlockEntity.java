package com.Kizunad.guzhenrenext.bastion.blockentity;

import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.BastionBlockEntities;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 镇地灯方块实体。
 * <p>
 * 用于持久化该镇地灯绑定的基地唯一标识（{@code bastionId}）。
 * </p>
 */
public class BastionWardingLanternBlockEntity extends BlockEntity {

    /**
     * NBT 中存储基地 UUID 的键名。
     */
    private static final String KEY_BASTION_ID = "bastionId";

    @Nullable
    private UUID bastionId;

    public BastionWardingLanternBlockEntity(BlockPos pos, BlockState blockState) {
        super(BastionBlockEntities.BASTION_WARDING_LANTERN.get(), pos, blockState);
    }

    @Nullable
    public UUID getBastionId() {
        return bastionId;
    }

    public void setBastionId(@Nullable UUID newBastionId) {
        UUID oldBastionId = bastionId;

        BastionSavedData savedData = getSavedDataIfServer();
        if (savedData != null && oldBastionId != null) {
            savedData.removeLanternFromCache(oldBastionId, getBlockPos());
        }

        bastionId = newBastionId;

        if (savedData != null && bastionId != null) {
            savedData.addLanternToCache(bastionId, getBlockPos());
        }
        setChanged();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        BastionSavedData savedData = getSavedDataIfServer();
        if (savedData != null && bastionId != null) {
            savedData.addLanternToCache(bastionId, getBlockPos());
        }
    }

    @Override
    public void setRemoved() {
        BastionSavedData savedData = getSavedDataIfServer();
        if (savedData != null && bastionId != null) {
            savedData.removeLanternFromCache(bastionId, getBlockPos());
        }
        super.setRemoved();
    }

    @Nullable
    private BastionSavedData getSavedDataIfServer() {
        if (level instanceof ServerLevel serverLevel) {
            return BastionSavedData.get(serverLevel);
        }
        return null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (bastionId != null) {
            tag.putUUID(KEY_BASTION_ID, bastionId);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.hasUUID(KEY_BASTION_ID)) {
            bastionId = tag.getUUID(KEY_BASTION_ID);
            return;
        }
        bastionId = null;
    }
}
