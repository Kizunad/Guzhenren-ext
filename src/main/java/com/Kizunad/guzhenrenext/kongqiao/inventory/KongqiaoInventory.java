package com.Kizunad.guzhenrenext.kongqiao.inventory;

import com.Kizunad.guzhenrenext.kongqiao.KongqiaoConstants;
import com.Kizunad.guzhenrenext.kongqiao.validator.KongqiaoSlotValidator;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

/**
 * 空窍物品槽容器。
 * <p>
 * 实际容量固定为最大可拓展容量，通过 {@link KongqiaoSettings}
 * 控制可见/可交互的行数，避免频繁重建容器。
 * </p>
 */
public class KongqiaoInventory extends SimpleContainer {

    private final KongqiaoSettings settings;
    private final KongqiaoSlotValidator validator;
    private final int columns;
    private Runnable changeListener;

    public KongqiaoInventory(
        KongqiaoSettings settings,
        KongqiaoSlotValidator validator
    ) {
        super(KongqiaoConstants.MAX_ROWS * settings.getColumns());
        this.settings = settings;
        this.validator = validator;
        this.columns = settings.getColumns();
    }

    public KongqiaoSettings getSettings() {
        return settings;
    }

    public int getColumns() {
        return columns;
    }

    public int getVisibleRows() {
        return settings.getUnlockedRows();
    }

    public boolean isSlotUnlocked(int slot) {
        return settings.isSlotUnlocked(slot);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        if (!settings.isSlotUnlocked(index)) {
            return false;
        }
        return validator.canPlace(stack);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (changeListener != null) {
            changeListener.run();
        }
    }

    /**
     * 注册内容变化回调，用于触发标记与同步。
     */
    public void setChangeListener(Runnable listener) {
        this.changeListener = listener;
    }

    /**
     * 序列化空窍内容。
     */
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("rows", settings.getUnlockedRows());
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

    /**
     * 反序列化空窍内容。
     */
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("rows")) {
            settings.setUnlockedRows(tag.getInt("rows"));
        }
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
