package com.Kizunad.guzhenrenext.xianqiao.alchemy.material;

import java.util.Optional;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 炼丹主材属性解析器。
 */
public final class MaterialPropertyResolver {

    private MaterialPropertyResolver() {
    }

    /**
     * 解析主材对应的药性。
     *
     * @param stack 主材堆栈
     * @return 药性，无法识别时返回空
     */
    public static Optional<MaterialProperty> resolve(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        Item item = stack.getItem();
        if (item == Items.OAK_SAPLING || item == Items.BAMBOO) {
            return Optional.of(MaterialProperty.WOOD);
        }
        if (item == Items.SLIME_BALL || item == Items.HONEY_BOTTLE) {
            return Optional.of(MaterialProperty.SOFT);
        }
        if (item == Items.WHEAT || item == Items.CARROT) {
            return Optional.of(MaterialProperty.VITAL);
        }
        if (item == Items.KELP || item == Items.SEAGRASS) {
            return Optional.of(MaterialProperty.MOIST);
        }
        return Optional.empty();
    }
}
