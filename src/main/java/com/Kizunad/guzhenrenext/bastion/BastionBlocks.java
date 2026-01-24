package com.Kizunad.guzhenrenext.bastion;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.bastion.block.BastionCoreBlock;
import com.Kizunad.guzhenrenext.bastion.block.BastionNodeBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 基地系统方块与物品注册表。
 * <p>
 * 注册核心方块和节点方块及其对应的方块物品。
 * </p>
 */
public final class BastionBlocks {

    private BastionBlocks() {
        // 工具类
    }

    // ===== 延迟注册器 =====

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(
        Registries.BLOCK,
        GuzhenrenExt.MODID
    );

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
        Registries.ITEM,
        GuzhenrenExt.MODID
    );

    // ===== 方块属性常量 =====

    /**
     * 方块属性常量，避免 MagicNumber checkstyle 错误。
     */
    private static final class BlockProperties {
        /** 节点方块硬度。 */
        static final float NODE_HARDNESS = 3.0f;
        /** 节点方块爆炸抗性。 */
        static final float NODE_BLAST_RESISTANCE = 6.0f;
        /** 核心方块硬度（类似黑曜石）。 */
        static final float CORE_HARDNESS = 50.0f;
        /** 核心方块爆炸抗性（类似黑曜石）。 */
        static final float CORE_BLAST_RESISTANCE = 1200.0f;
        /** 核心方块光照等级（最大值）。 */
        static final int CORE_LIGHT_LEVEL = 15;

        private BlockProperties() {
            // 工具类
        }
    }

    // ===== 方块属性配置 =====

    /**
     * 节点方块属性：中等硬度，类石材行为。
     */
    private static final BlockBehaviour.Properties NODE_PROPERTIES = BlockBehaviour.Properties.of()
        .strength(BlockProperties.NODE_HARDNESS, BlockProperties.NODE_BLAST_RESISTANCE)
        .sound(SoundType.STONE)
        .requiresCorrectToolForDrops()
        .lightLevel(state -> state.getValue(BastionNodeBlock.TIER));  // 根据转数发光

    /**
     * 核心方块属性：高硬度，类黑曜石抗性。
     */
    private static final BlockBehaviour.Properties CORE_PROPERTIES = BlockBehaviour.Properties.of()
        .strength(BlockProperties.CORE_HARDNESS, BlockProperties.CORE_BLAST_RESISTANCE)
        .sound(SoundType.METAL)
        .requiresCorrectToolForDrops()
        .lightLevel(state -> BlockProperties.CORE_LIGHT_LEVEL);  // 最大光照

    // ===== 方块注册 =====

    /**
     * 基地节点方块 - 领地扩张方块。
     */
    public static final DeferredHolder<Block, BastionNodeBlock> BASTION_NODE = BLOCKS.register(
        "bastion_node",
        () -> new BastionNodeBlock(NODE_PROPERTIES)
    );

    /**
     * 基地核心方块 - 基地的核心。
     */
    public static final DeferredHolder<Block, BastionCoreBlock> BASTION_CORE = BLOCKS.register(
        "bastion_core",
        () -> new BastionCoreBlock(CORE_PROPERTIES)
    );

    // ===== 物品注册（方块物品） =====

    /**
     * 基地节点的方块物品。
     */
    public static final DeferredHolder<Item, BlockItem> BASTION_NODE_ITEM = ITEMS.register(
        "bastion_node",
        () -> new BlockItem(BASTION_NODE.get(), new Item.Properties())
    );

    /**
     * 基地核心的方块物品。
     */
    public static final DeferredHolder<Item, BlockItem> BASTION_CORE_ITEM = ITEMS.register(
        "bastion_core",
        () -> new BlockItem(BASTION_CORE.get(), new Item.Properties())
    );

    // ===== 注册方法 =====

    /**
     * 注册所有基地方块和物品。
     *
     * @param eventBus 模组事件总线
     */
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
    }
}
