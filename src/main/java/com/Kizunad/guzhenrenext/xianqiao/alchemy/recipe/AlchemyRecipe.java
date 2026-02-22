package com.Kizunad.guzhenrenext.xianqiao.alchemy.recipe;

import com.Kizunad.guzhenrenext.xianqiao.alchemy.material.MaterialProperty;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 炼丹配方结果数据。
 * <p>
 * 本记录类型用于承载一次“主材匹配成功”后的可执行配方结果，
 * 其中包含：
 * 1) 主材属性（决定丹药类型）；
 * 2) 目标丹药物品；
 * 3) 本次参与计算的有效辅材槽位数量；
 * 4) 配方增强值占位字段（当前阶段仅做基础透传，不参与成功率/品质计算）。
 * </p>
 */
public record AlchemyRecipe(
    MaterialProperty mainProperty,
    Item outputItem,
    int auxiliaryCount,
    int enhancementValue
) {

    /** 有效辅材槽位的最小值。 */
    private static final int MIN_AUXILIARY_COUNT = 0;

    /** 有效辅材槽位的最大值（固定 4 个辅材槽）。 */
    private static final int MAX_AUXILIARY_COUNT = 4;

    public AlchemyRecipe {
        if (mainProperty == null) {
            throw new IllegalArgumentException("mainProperty cannot be null");
        }
        if (outputItem == null) {
            throw new IllegalArgumentException("outputItem cannot be null");
        }
        if (auxiliaryCount < MIN_AUXILIARY_COUNT || auxiliaryCount > MAX_AUXILIARY_COUNT) {
            throw new IllegalArgumentException("auxiliaryCount out of range: " + auxiliaryCount);
        }
        if (enhancementValue < 0) {
            throw new IllegalArgumentException("enhancementValue cannot be negative");
        }
    }

    /**
     * 基于配方生成一次产出堆。
     *
     * @return 对应丹药 1 个
     */
    public ItemStack createOutputStack() {
        return new ItemStack(outputItem, 1);
    }
}
