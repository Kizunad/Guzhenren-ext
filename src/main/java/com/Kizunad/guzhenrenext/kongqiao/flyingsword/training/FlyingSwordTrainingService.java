package com.Kizunad.guzhenrenext.kongqiao.flyingsword.training;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.calculator.FlyingSwordAttributes;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ItemStackCustomDataHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

public final class FlyingSwordTrainingService {

    private FlyingSwordTrainingService() {}

    private static final int SLOT_SWORD = 0;
    private static final int SLOT_FUEL = 1;
    private static final int EXP_PER_TICK = 1;
    private static final int AFFINITY_PROC_DENOMINATOR = 20;
    private static final int AFFINITY_GAIN = 1;

    public static void tick(ServerPlayer player) {
        if (player == null) {
            return;
        }
        FlyingSwordTrainingAttachment training =
            KongqiaoAttachments.getFlyingSwordTraining(player);
        if (training == null) {
            return;
        }
        tickInternal(training, player);
    }

    public static void tickInternal(
        FlyingSwordTrainingAttachment training,
        ServerPlayer player
    ) {
        if (training == null) {
            return;
        }

        ItemStack swordStack = training.getInputSlots().getStackInSlot(SLOT_SWORD);
        if (!isTrainingSword(swordStack)) {
            return;
        }

        int fuelTime = training.getFuelTime();
        if (fuelTime <= 0 && !tryRefillFuel(training)) {
            return;
        }

        if (training.getFuelTime() <= 0) {
            return;
        }

        training.setFuelTime(training.getFuelTime() - 1);

        CompoundTag root = ItemStackCustomDataHelper.copyCustomDataTag(swordStack);
        FlyingSwordAttributes attributes = FlyingSwordAttributes.fromNBT(
            root.getCompound("Attributes")
        );
        attributes.addExperience(EXP_PER_TICK);
        training.addAccumulatedExp(EXP_PER_TICK);

        if (
            player != null &&
            player.getRandom().nextInt(AFFINITY_PROC_DENOMINATOR) == 0
        ) {
            attributes.getSpiritData().addAffinity(AFFINITY_GAIN);
        }

        root.put("Attributes", attributes.toNBT());
        ItemStackCustomDataHelper.setCustomDataTag(swordStack, root);
        training.getInputSlots().setStackInSlot(SLOT_SWORD, swordStack);
    }

    private static boolean tryRefillFuel(FlyingSwordTrainingAttachment training) {
        ItemStack fuelStack = training.getInputSlots().getStackInSlot(SLOT_FUEL);
        int fuelTicks = FuelHelper.getFuelTime(fuelStack);
        if (fuelTicks <= 0) {
            training.clearFuelState();
            return false;
        }

        fuelStack.shrink(1);
        training.getInputSlots().setStackInSlot(SLOT_FUEL, fuelStack);
        training.setFuelTime(fuelTicks);
        training.setMaxFuelTime(fuelTicks);
        return true;
    }

    private static boolean isTrainingSword(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() instanceof SwordItem;
    }
}
