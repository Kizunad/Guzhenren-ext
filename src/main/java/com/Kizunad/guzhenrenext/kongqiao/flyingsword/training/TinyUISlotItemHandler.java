package com.Kizunad.guzhenrenext.kongqiao.flyingsword.training;

import com.Kizunad.tinyUI.demo.TinyUISlot;
import net.minecraft.world.SimpleContainer;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * 适配 IItemHandler 的 TinyUISlot。
 * 能够同时支持 TinyUI 的动态定位和 NeoForge 的 ItemHandler 能力。
 */
public class TinyUISlotItemHandler extends TinyUISlot {

    private final SlotItemHandler delegate;

    public TinyUISlotItemHandler(IItemHandler itemHandler, int index, int x, int y) {
        super(new SimpleContainer(0), index, x, y);
        this.delegate = new SlotItemHandler(itemHandler, index, x, y);
    }

    @Override
    public boolean mayPlace(net.minecraft.world.item.ItemStack stack) {
        return delegate.mayPlace(stack);
    }

    @Override
    public net.minecraft.world.item.ItemStack getItem() {
        return delegate.getItem();
    }

    @Override
    public void set(net.minecraft.world.item.ItemStack stack) {
        delegate.set(stack);
    }

    @Override
    public void setChanged() {
        delegate.setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return delegate.getMaxStackSize();
    }

    @Override
    public int getMaxStackSize(net.minecraft.world.item.ItemStack stack) {
        return delegate.getMaxStackSize(stack);
    }

    @Override
    public net.minecraft.world.item.ItemStack remove(int amount) {
        return delegate.remove(amount);
    }
}
