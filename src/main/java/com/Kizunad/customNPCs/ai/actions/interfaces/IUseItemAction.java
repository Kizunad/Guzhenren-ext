package com.Kizunad.customNPCs.ai.actions.interfaces;

import com.Kizunad.customNPCs.ai.actions.IAction;
import net.minecraft.world.item.ItemStack;

/**
 * 使用物品类动作接口 - 用于描述使用物品的动作
 */
public interface IUseItemAction extends IAction {
    /**
     * 获取正在使用的物品堆
     * @return 物品堆
     */
    ItemStack getItemStack();

    /**
     * 检查物品使用是否完成
     * @return 完成返回 true
     */
    boolean isUsageComplete();
}
