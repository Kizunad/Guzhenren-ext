package com.Kizunad.guzhenrenext.bastion;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.bastion.block.BastionAnchorBlock;
import com.Kizunad.guzhenrenext.bastion.block.BastionCoreBlock;
import com.Kizunad.guzhenrenext.bastion.block.BastionMyceliumBlock;
import com.Kizunad.guzhenrenext.bastion.block.BastionReversalArrayBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
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
        /** 菌毯方块硬度。 */
        static final float MYCELIUM_HARDNESS = 0.3f;

        /** 菌毯方块爆炸抗性。 */
        static final float MYCELIUM_BLAST_RESISTANCE = 0.2f;

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
     * Anchor 方块属性：中等硬度，类石材行为。
     */
    private static final BlockBehaviour.Properties ANCHOR_PROPERTIES = BlockBehaviour.Properties.of()
        .strength(BlockProperties.NODE_HARDNESS, BlockProperties.NODE_BLAST_RESISTANCE)
        .sound(SoundType.STONE)
        .requiresCorrectToolForDrops()
        .lightLevel(state -> state.getValue(BastionAnchorBlock.TIER));

    /**
     * 核心方块属性：高硬度，类黑曜石抗性。
     */
    private static final BlockBehaviour.Properties CORE_PROPERTIES = BlockBehaviour.Properties.of()
        .strength(BlockProperties.CORE_HARDNESS, BlockProperties.CORE_BLAST_RESISTANCE)
        .sound(SoundType.METAL)
        .requiresCorrectToolForDrops()
        .lightLevel(state -> BlockProperties.CORE_LIGHT_LEVEL);  // 最大光照

    /**
     * 逆转阵法属性：中等硬度，类石材行为。
     */
    private static final BlockBehaviour.Properties REVERSAL_ARRAY_PROPERTIES = BlockBehaviour.Properties.of()
        .strength(BlockProperties.NODE_HARDNESS, BlockProperties.NODE_BLAST_RESISTANCE)
        .sound(SoundType.STONE)
        .requiresCorrectToolForDrops();

    /**
     * 菌毯方块属性：低硬度、覆盖物定位。
     */
    private static final BlockBehaviour.Properties MYCELIUM_PROPERTIES = BlockBehaviour.Properties.of()
        .strength(BlockProperties.MYCELIUM_HARDNESS, BlockProperties.MYCELIUM_BLAST_RESISTANCE)
        .sound(SoundType.GRASS);

    // ===== 方块注册 =====

    /**
     * 基地菌毯方块 - 贴地扩张主网。
     */
    public static final DeferredHolder<Block, BastionMyceliumBlock> BASTION_NODE = BLOCKS.register(
        "bastion_node",
        () -> new BastionMyceliumBlock(MYCELIUM_PROPERTIES)
    );

    /**
     * 基地 Anchor 方块 - 子核心/支撑节点。
     */
    public static final DeferredHolder<Block, BastionAnchorBlock> BASTION_ANCHOR = BLOCKS.register(
        "bastion_anchor",
        () -> new BastionAnchorBlock(ANCHOR_PROPERTIES)
    );

    /**
     * 基地核心方块 - 基地的核心。
     */
    public static final DeferredHolder<Block, BastionCoreBlock> BASTION_CORE = BLOCKS.register(
        "bastion_core",
        () -> new BastionCoreBlock(CORE_PROPERTIES)
    );

    /**
     * 逆转阵法方块 - 偷家玩法。
     */
    public static final DeferredHolder<Block, BastionReversalArrayBlock> BASTION_REVERSAL_ARRAY =
        BLOCKS.register(
            "bastion_reversal_array",
            () -> new BastionReversalArrayBlock(REVERSAL_ARRAY_PROPERTIES)
        );

    // ===== 物品注册（方块物品） =====

    /**
     * 基地菌毯方块物品（沿用 bastion_node 的物品 id）。
     */
    public static final DeferredHolder<Item, BlockItem> BASTION_NODE_ITEM = ITEMS.register(
        "bastion_node",
        () -> new BlockItem(BASTION_NODE.get(), new Item.Properties())
    );

    /**
     * 基地 Anchor 方块物品。
     */
    public static final DeferredHolder<Item, BlockItem> BASTION_ANCHOR_ITEM = ITEMS.register(
        "bastion_anchor",
        () -> new BlockItem(BASTION_ANCHOR.get(), new Item.Properties())
    );

    /**
     * 基地核心的方块物品。
     */
    public static final DeferredHolder<Item, BlockItem> BASTION_CORE_ITEM = ITEMS.register(
        "bastion_core",
        () ->
            new BlockItem(BASTION_CORE.get(), new Item.Properties()) {
                @Override
                public void appendHoverText(
                    ItemStack stack,
                    Item.TooltipContext context,
                    java.util.List<Component> tooltip,
                    TooltipFlag flag
                ) {
                    tooltip.add(Component.translatable(
                        "tooltip.guzhenrenext.bastion_core.hint"
                    ));
                    super.appendHoverText(stack, context, tooltip, flag);

                    if (isShiftDownSafe()) {
                        tooltip.add(Component.translatable(
                            "tooltip.guzhenrenext.bastion_core.desc.1"
                        ));
                        tooltip.add(Component.translatable(
                            "tooltip.guzhenrenext.bastion_core.desc.2"
                        ));
                        tooltip.add(Component.translatable(
                            "tooltip.guzhenrenext.bastion_core.desc.3"
                        ));
                    }
                }

                private boolean isShiftDownSafe() {
                    if (FMLEnvironment.dist != Dist.CLIENT) {
                        return false;
                    }
                    try {
                        Class<?> screenClass = Class.forName(
                            "net.minecraft.client.gui.screens.Screen"
                        );
                        java.lang.reflect.Method method = screenClass.getDeclaredMethod(
                            "hasShiftDown"
                        );
                        Object result = method.invoke(null);
                        return result instanceof Boolean bool && bool;
                    } catch (ReflectiveOperationException e) {
                        return false;
                    }
                }
            }
    );

    /**
     * 逆转阵法的方块物品。
     */
    public static final DeferredHolder<Item, BlockItem> BASTION_REVERSAL_ARRAY_ITEM =
        ITEMS.register(
            "bastion_reversal_array",
            () ->
                new BlockItem(BASTION_REVERSAL_ARRAY.get(), new Item.Properties()) {
                    @Override
                    public void appendHoverText(
                        ItemStack stack,
                        Item.TooltipContext context,
                        java.util.List<Component> tooltip,
                        TooltipFlag flag
                    ) {
                        tooltip.add(Component.translatable(
                            "tooltip.guzhenrenext.bastion_reversal_array.hint"));
                        super.appendHoverText(stack, context, tooltip, flag);

                        // Shift 展示详细说明（客户端才会有按键状态，服务端无需判断）
                        if (isShiftDownSafe()) {
                            tooltip.add(Component.translatable(
                                "tooltip.guzhenrenext.bastion_reversal_array.desc.1"));
                            tooltip.add(Component.translatable(
                                "tooltip.guzhenrenext.bastion_reversal_array.desc.2"));
                            tooltip.add(Component.translatable(
                                "tooltip.guzhenrenext.bastion_reversal_array.desc.3"));
                            tooltip.add(Component.translatable(
                                "tooltip.guzhenrenext.bastion_reversal_array.desc.4"));
                            tooltip.add(Component.translatable(
                                "tooltip.guzhenrenext.bastion_reversal_array.desc.5"));
                        }
                    }

                    private boolean isShiftDownSafe() {
                        if (FMLEnvironment.dist != Dist.CLIENT) {
                            return false;
                        }
                        try {
                            Class<?> screenClass = Class.forName(
                                "net.minecraft.client.gui.screens.Screen"
                            );
                            java.lang.reflect.Method method = screenClass.getDeclaredMethod(
                                "hasShiftDown"
                            );
                            Object result = method.invoke(null);
                            return result instanceof Boolean bool && bool;
                        } catch (ReflectiveOperationException e) {
                            return false;
                        }
                    }
                }
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
