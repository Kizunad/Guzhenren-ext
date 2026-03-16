package com.Kizunad.guzhenrenext.xianqiao.spirit.deep;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class MutatedSpiritFoxEntity extends Fox {

    private static final int LIGHTNING_BOLT_COUNT = 2;

    private boolean thunderForm;

    public MutatedSpiritFoxEntity(EntityType<? extends Fox> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!thunderForm && isThunderCatalyst(itemStack)) {
            if (!level().isClientSide()) {
                transformToThunderForm();
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    public boolean isThunderForm() {
        return thunderForm;
    }

    private boolean isThunderCatalyst(ItemStack itemStack) {
        return itemStack.is(Blocks.FERN.asItem()) || itemStack.is(net.minecraft.world.item.Items.LIGHTNING_ROD);
    }

    private void transformToThunderForm() {
        thunderForm = true;
        setCustomName(net.minecraft.network.chat.Component.literal("雷霆灵狐"));
        setCustomNameVisible(true);
        setGlowingTag(true);
        if (level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < LIGHTNING_BOLT_COUNT; i++) {
                LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
                if (lightningBolt != null) {
                    lightningBolt.moveTo(getX(), getY(), getZ());
                    lightningBolt.setVisualOnly(true);
                    serverLevel.addFreshEntity(lightningBolt);
                }
            }
        }
    }
}
