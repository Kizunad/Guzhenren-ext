package com.Kizunad.guzhenrenext.xianqiao.spirit.deep;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SacrificialSheepEntity extends Sheep {

    private static final int PRECIOUS_NUTRITION_STEP = 5;

    private int spiritReserve;

    public SacrificialSheepEntity(EntityType<? extends Sheep> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (isValuableFeed(itemStack)) {
            if (!level().isClientSide()) {
                spiritReserve += PRECIOUS_NUTRITION_STEP;
                setCustomName(Component.literal("献祭羊·灵蕴" + spiritReserve));
                setCustomNameVisible(true);
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    public int getSpiritReserve() {
        return spiritReserve;
    }

    public int consumeTribulationOffset() {
        int value = spiritReserve;
        spiritReserve = 0;
        return value;
    }

    private boolean isValuableFeed(ItemStack itemStack) {
        return itemStack.getMaxStackSize() == 1 || itemStack.getRarity() != net.minecraft.world.item.Rarity.COMMON;
    }
}
