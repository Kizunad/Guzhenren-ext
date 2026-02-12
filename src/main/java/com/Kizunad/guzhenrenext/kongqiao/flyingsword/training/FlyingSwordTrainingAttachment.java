package com.Kizunad.guzhenrenext.kongqiao.flyingsword.training;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class FlyingSwordTrainingAttachment
    implements INBTSerializable<CompoundTag> {

    private static final String TAG_INPUT_SLOTS = "InputSlots";
    private static final String TAG_FUEL_TIME = "FuelTime";
    private static final String TAG_MAX_FUEL_TIME = "MaxFuelTime";
    private static final String TAG_ACCUMULATED_EXP = "AccumulatedExp";

    private static final int SLOT_COUNT = 2;
    private static final int SLOT_SWORD = 0;

    private final net.neoforged.neoforge.items.ItemStackHandler inputSlots =
        new net.neoforged.neoforge.items.ItemStackHandler(SLOT_COUNT) {
            @Override
            protected int getStackLimit(int slot, net.minecraft.world.item.ItemStack stack) {
                if (slot == SLOT_SWORD) {
                    return 1;
                }
                return super.getStackLimit(slot, stack);
            }
        };

    private int fuelTime;

    private int maxFuelTime;

    private int accumulatedExp;

    public net.neoforged.neoforge.items.ItemStackHandler getInputSlots() {
        return inputSlots;
    }

    public int getFuelTime() {
        return fuelTime;
    }

    public void setFuelTime(int fuelTime) {
        this.fuelTime = Math.max(0, fuelTime);
    }

    public int getMaxFuelTime() {
        return maxFuelTime;
    }

    public void setMaxFuelTime(int maxFuelTime) {
        this.maxFuelTime = Math.max(0, maxFuelTime);
    }

    public int getAccumulatedExp() {
        return accumulatedExp;
    }

    public void setAccumulatedExp(int accumulatedExp) {
        this.accumulatedExp = Math.max(0, accumulatedExp);
    }

    public void addAccumulatedExp(int delta) {
        if (delta <= 0) {
            return;
        }
        accumulatedExp += delta;
    }

    public void clearFuelState() {
        fuelTime = 0;
        maxFuelTime = 0;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.put(TAG_INPUT_SLOTS, inputSlots.serializeNBT(provider));
        tag.putInt(TAG_FUEL_TIME, fuelTime);
        tag.putInt(TAG_MAX_FUEL_TIME, maxFuelTime);
        tag.putInt(TAG_ACCUMULATED_EXP, accumulatedExp);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        inputSlots.deserializeNBT(provider, tag.getCompound(TAG_INPUT_SLOTS));

        if (tag.contains(TAG_FUEL_TIME, Tag.TAG_INT)) {
            fuelTime = Math.max(0, tag.getInt(TAG_FUEL_TIME));
        } else {
            fuelTime = 0;
        }

        if (tag.contains(TAG_MAX_FUEL_TIME, Tag.TAG_INT)) {
            maxFuelTime = Math.max(0, tag.getInt(TAG_MAX_FUEL_TIME));
        } else {
            maxFuelTime = 0;
        }

        if (tag.contains(TAG_ACCUMULATED_EXP, Tag.TAG_INT)) {
            accumulatedExp = Math.max(0, tag.getInt(TAG_ACCUMULATED_EXP));
        } else {
            accumulatedExp = 0;
        }
    }
}
