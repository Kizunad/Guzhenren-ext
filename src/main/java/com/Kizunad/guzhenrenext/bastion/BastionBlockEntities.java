package com.Kizunad.guzhenrenext.bastion;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.bastion.blockentity.BastionReversalArrayBlockEntity;
import com.Kizunad.guzhenrenext.bastion.blockentity.BastionWardingLanternBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 基地系统 BlockEntity 注册表。
 */
public final class BastionBlockEntities {

    private BastionBlockEntities() {
    }

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, GuzhenrenExt.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BastionReversalArrayBlockEntity>>
        BASTION_REVERSAL_ARRAY = BLOCK_ENTITY_TYPES.register(
            "bastion_reversal_array",
            () -> BlockEntityType.Builder.of(
                BastionReversalArrayBlockEntity::new,
                BastionBlocks.BASTION_REVERSAL_ARRAY.get()
            ).build(null)
        );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BastionWardingLanternBlockEntity>>
        BASTION_WARDING_LANTERN = BLOCK_ENTITY_TYPES.register(
            "bastion_warding_lantern",
            () -> BlockEntityType.Builder.of(
                BastionWardingLanternBlockEntity::new,
                BastionBlocks.BASTION_WARDING_LANTERN.get()
            ).build(null)
        );

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
