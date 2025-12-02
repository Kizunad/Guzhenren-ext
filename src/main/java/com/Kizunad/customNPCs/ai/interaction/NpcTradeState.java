package com.Kizunad.customNPCs.ai.interaction;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

/**
 * 交易数据占位组件：后续可扩展自定义交易、补货、定价。
 */
public class NpcTradeState {

    private static final int DEFAULT_RESTOCK_DELAY_TICKS = 1200;

    private boolean tradeEnabled = true;
    private int restockDelayTicks = DEFAULT_RESTOCK_DELAY_TICKS;

    public boolean isTradeEnabled() {
        return tradeEnabled;
    }

    public void setTradeEnabled(boolean tradeEnabled) {
        this.tradeEnabled = tradeEnabled;
    }

    public int getRestockDelayTicks() {
        return restockDelayTicks;
    }

    public void setRestockDelayTicks(int restockDelayTicks) {
        this.restockDelayTicks = Math.max(0, restockDelayTicks);
    }

    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("enabled", tradeEnabled);
        tag.putInt("restockDelay", restockDelayTicks);
        return tag;
    }

    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("enabled")) {
            tradeEnabled = tag.getBoolean("enabled");
        }
        if (tag.contains("restockDelay")) {
            restockDelayTicks = Math.max(0, tag.getInt("restockDelay"));
        }
    }
}
