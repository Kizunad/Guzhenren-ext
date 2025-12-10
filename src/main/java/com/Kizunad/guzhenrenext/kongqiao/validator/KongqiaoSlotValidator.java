package com.Kizunad.guzhenrenext.kongqiao.validator;

import net.minecraft.world.item.ItemStack;

/**
 * 空窍槽位合法性校验器。
 * <p>
 * 所有空窍/攻击背包的物品放置规则都应通过该接口完成，方便后续加入新的策略或开放自定义。
 * </p>
 */
public interface KongqiaoSlotValidator {
    /**
     * 判断指定物品是否允许放入。
     *
     * @param stack 目标物品
     * @return true 表示允许放入
     */
    boolean canPlace(ItemStack stack);
}
