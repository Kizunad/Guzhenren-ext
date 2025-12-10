package com.Kizunad.guzhenrenext.kongqiao.inventory;

import com.Kizunad.guzhenrenext.kongqiao.KongqiaoConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

/**
 * 攻击背包容器。
 * <p>
 * 该容器保持与玩家背包相同的列数，方便与玩家背包互换。
 * </p>
 */
public class AttackInventory extends SimpleContainer {

    private final int rows;
    private final int columns;
    private Runnable changeListener;

    public AttackInventory() {
        this(KongqiaoConstants.ATTACK_ROWS, KongqiaoConstants.ATTACK_COLUMNS);
    }

    public AttackInventory(int rows, int columns) {
        super(rows * columns);
        this.rows = rows;
        this.columns = columns;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (changeListener != null) {
            changeListener.run();
        }
    }

    public void setChangeListener(Runnable listener) {
        this.changeListener = listener;
    }

    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("rows", rows);
        NonNullList<ItemStack> items = NonNullList.withSize(
            getContainerSize(),
            ItemStack.EMPTY
        );
        for (int i = 0; i < getContainerSize(); i++) {
            items.set(i, getItem(i).copy());
        }
        ContainerHelper.saveAllItems(tag, items, provider);
        return tag;
    }

    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        NonNullList<ItemStack> items = NonNullList.withSize(
            getContainerSize(),
            ItemStack.EMPTY
        );
        ContainerHelper.loadAllItems(tag, items, provider);
        for (int i = 0; i < items.size(); i++) {
            setItem(i, items.get(i));
        }
    }
}
