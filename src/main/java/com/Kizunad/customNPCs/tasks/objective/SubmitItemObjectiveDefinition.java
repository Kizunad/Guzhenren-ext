package com.Kizunad.customNPCs.tasks.objective;

import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

/**
 * “提交物品”目标定义。
 */
public record SubmitItemObjectiveDefinition(
    Item item,
    @Nullable CompoundTag requiredNbt,
    @Nullable Holder<Potion> requiredPotion,
    int requiredCount,
    float baseValueSnapshot
) implements TaskObjectiveDefinition {

    public SubmitItemObjectiveDefinition {
        requiredCount = Math.max(1, requiredCount);
        baseValueSnapshot = Math.max(0.0F, baseValueSnapshot);
        if (requiredNbt != null && requiredNbt.isEmpty()) {
            requiredNbt = null;
        }
    }

    @Override
    public TaskObjectiveType getType() {
        return TaskObjectiveType.SUBMIT_ITEM;
    }

    /**
     * 判断一个物品堆是否满足需求。
     */
    public boolean matches(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != item) {
            return false;
        }
        if (requiredPotion != null) {
            PotionContents contents = stack.getOrDefault(
                DataComponents.POTION_CONTENTS,
                PotionContents.EMPTY
            );
            if (!contents.is(requiredPotion)) {
                return false;
            }
        }
        if (requiredNbt == null) {
            return true;
        }
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return false;
        }
        return containsAllTags(data.copyTag());
    }

    private boolean containsAllTags(CompoundTag stackTag) {
        Set<String> keys = requiredNbt.getAllKeys();
        for (String key : keys) {
            if (!stackTag.contains(key)) {
                return false;
            }
            Tag target = requiredNbt.get(key);
            Tag actual = stackTag.get(key);
            if (!NbtUtils.compareNbt(target, actual, true)) {
                return false;
            }
        }
        return true;
    }
}
