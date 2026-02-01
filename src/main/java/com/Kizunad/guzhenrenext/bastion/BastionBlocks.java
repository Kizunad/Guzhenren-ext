package com.Kizunad.guzhenrenext.bastion;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.bastion.block.BastionAnchorBlock;
import com.Kizunad.guzhenrenext.bastion.block.BastionAuraNodeBlock;
import com.Kizunad.guzhenrenext.bastion.block.BastionAntiExplosionShellBlock;
import com.Kizunad.guzhenrenext.bastion.block.BastionAntiFireShellBlock;
import com.Kizunad.guzhenrenext.bastion.block.BastionChitinShellBlock;
import com.Kizunad.guzhenrenext.bastion.block.BastionCoreBlock;
import com.Kizunad.guzhenrenext.bastion.block.BastionEnergyNodeBlock;
import com.Kizunad.guzhenrenext.bastion.block.BastionGuardianHatcheryBlock;
import com.Kizunad.guzhenrenext.bastion.block.BastionMyceliumBlock;
import com.Kizunad.guzhenrenext.bastion.block.BastionReversalArrayBlock;
import com.Kizunad.guzhenrenext.bastion.block.BastionTurretBlock;
import com.Kizunad.guzhenrenext.bastion.block.BastionTrapBlock;
import com.Kizunad.guzhenrenext.bastion.service.BastionEnergyBuildService;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.Kizunad.guzhenrenext.bastion.item.BastionScoutItem;

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

        /** 外壳方块硬度。 */
        static final float CHITIN_SHELL_HARDNESS = 1.5f;
        /** 外壳方块爆炸抗性。 */
        static final float CHITIN_SHELL_BLAST_RESISTANCE = 3.0f;

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

    /**
     * 外壳方块属性：低硬度、轻量防护。
     */
    private static final BlockBehaviour.Properties CHITIN_SHELL_PROPERTIES = BlockBehaviour.Properties.of()
        .mapColor(MapColor.TERRACOTTA_BROWN)
        .strength(BlockProperties.CHITIN_SHELL_HARDNESS, BlockProperties.CHITIN_SHELL_BLAST_RESISTANCE)
        .requiresCorrectToolForDrops();

    /**
     * 能源节点属性：中等硬度，类石材行为。
     */
    private static final BlockBehaviour.Properties ENERGY_NODE_PROPERTIES = BlockBehaviour.Properties.of()
        .strength(BlockProperties.NODE_HARDNESS, BlockProperties.NODE_BLAST_RESISTANCE)
        .sound(SoundType.STONE)
        .requiresCorrectToolForDrops();

    /**
     * 守卫孵化巢属性：中等硬度，类石材行为。
     */
    private static final BlockBehaviour.Properties HATCHERY_PROPERTIES = BlockBehaviour.Properties.of()
        .strength(BlockProperties.NODE_HARDNESS, BlockProperties.NODE_BLAST_RESISTANCE)
        .sound(SoundType.STONE)
        .requiresCorrectToolForDrops();

    /**
     * 炮台节点属性：中等硬度，类石材行为。
     */
    private static final BlockBehaviour.Properties TURRET_PROPERTIES = BlockBehaviour.Properties.of()
        .strength(BlockProperties.NODE_HARDNESS, BlockProperties.NODE_BLAST_RESISTANCE)
        .sound(SoundType.STONE)
        .requiresCorrectToolForDrops();

    /**
     * 陷阱节点属性：中等硬度，类石材行为。
     */
    private static final BlockBehaviour.Properties TRAP_PROPERTIES = BlockBehaviour.Properties.of()
        .strength(BlockProperties.NODE_HARDNESS, BlockProperties.NODE_BLAST_RESISTANCE)
        .sound(SoundType.STONE)
        .requiresCorrectToolForDrops();

    /**
     * 光环节点属性：中等硬度，类石材行为。
     */
    private static final BlockBehaviour.Properties AURA_NODE_PROPERTIES = BlockBehaviour.Properties.of()
        .strength(BlockProperties.NODE_HARDNESS, BlockProperties.NODE_BLAST_RESISTANCE)
        .sound(SoundType.STONE)
        .requiresCorrectToolForDrops();

    /**
     * 反爆外壳节点属性：中等硬度，类石材行为。
     */
    private static final BlockBehaviour.Properties ANTI_EXPLOSION_SHELL_PROPERTIES = BlockBehaviour.Properties.of()
        .strength(BlockProperties.NODE_HARDNESS, BlockProperties.NODE_BLAST_RESISTANCE)
        .sound(SoundType.STONE)
        .requiresCorrectToolForDrops();

    /**
     * 反火外壳节点属性：中等硬度，类石材行为。
     */
    private static final BlockBehaviour.Properties ANTI_FIRE_SHELL_PROPERTIES = BlockBehaviour.Properties.of()
        .strength(BlockProperties.NODE_HARDNESS, BlockProperties.NODE_BLAST_RESISTANCE)
        .sound(SoundType.STONE)
        .requiresCorrectToolForDrops();

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

    /**
     * 能源节点方块（带 energy_type 属性）。
     */
    public static final DeferredHolder<Block, BastionEnergyNodeBlock> BASTION_ENERGY_NODE = BLOCKS.register(
        "bastion_energy_node",
        () -> new BastionEnergyNodeBlock(ENERGY_NODE_PROPERTIES)
    );

    /**
     * 守卫孵化巢方块（挂载在 Anchor 上）。
     */
    public static final DeferredHolder<Block, BastionGuardianHatcheryBlock> BASTION_GUARDIAN_HATCHERY =
        BLOCKS.register(
            "bastion_guardian_hatchery",
            () -> new BastionGuardianHatcheryBlock(HATCHERY_PROPERTIES)
        );

    /**
     * 光环节点方块（带 aura_type 属性）。
     */
    public static final DeferredHolder<Block, BastionAuraNodeBlock> BASTION_AURA_NODE = BLOCKS.register(
        "bastion_aura_node",
        () -> new BastionAuraNodeBlock(AURA_NODE_PROPERTIES)
    );

    /**
     * 基地外壳方块（甲壳） - 轻量保护。
     */
    public static final DeferredHolder<Block, BastionChitinShellBlock> BASTION_CHITIN_SHELL = BLOCKS.register(
        "bastion_chitin_shell",
        () -> new BastionChitinShellBlock(CHITIN_SHELL_PROPERTIES)
    );

    /**
     * 炮台节点方块（挂载在 Anchor 上）。
     */
    public static final DeferredHolder<Block, BastionTurretBlock> BASTION_TURRET = BLOCKS.register(
        "bastion_turret",
        () -> new BastionTurretBlock(TURRET_PROPERTIES)
    );

    /**
     * 陷阱节点方块（挂载在 Anchor 上）。
     */
    public static final DeferredHolder<Block, BastionTrapBlock> BASTION_TRAP = BLOCKS.register(
        "bastion_trap",
        () -> new BastionTrapBlock(TRAP_PROPERTIES)
    );

    /**
     * 反爆外壳节点方块（挂载在 Anchor 上）。
     */
    public static final DeferredHolder<Block, BastionAntiExplosionShellBlock> BASTION_ANTI_EXPLOSION_SHELL =
        BLOCKS.register(
            "bastion_anti_explosion_shell",
            () -> new BastionAntiExplosionShellBlock(ANTI_EXPLOSION_SHELL_PROPERTIES)
        );

    /**
     * 反火外壳节点方块（挂载在 Anchor 上）。
     */
    public static final DeferredHolder<Block, BastionAntiFireShellBlock> BASTION_ANTI_FIRE_SHELL = BLOCKS.register(
        "bastion_anti_fire_shell",
        () -> new BastionAntiFireShellBlock(ANTI_FIRE_SHELL_PROPERTIES)
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
     * 能源节点方块物品（可建造）。
     * <p>
     * Round 3.1：放置时必须满足 server-side 约束：
     * <ul>
     *     <li>只能放在 Anchor 上方</li>
     *     <li>扣除 resourcePool（buildCost）</li>
     *     <li>遵守 maxCount 上限</li>
     * </ul>
     * </p>
     * <p>
     * 能源类型通过物品 CustomData 写入（避免拆成 3 个独立方块）。
     * </p>
     */
    public static final DeferredHolder<Item, BlockItem> BASTION_ENERGY_NODE_ITEM = ITEMS.register(
        "bastion_energy_node",
        () -> new BastionEnergyNodeItem(BASTION_ENERGY_NODE.get(), new Item.Properties())
    );

    /**
     * 守卫孵化巢方块物品。
     * <p>
     * Round 4.2：本回合仅做“结构约束 + FULL tick 驱动”，不在放置时扣费。
     * 扣费与冷却由 {@code BastionHatcheryService} 统一在服务端 tick 中处理，避免玩家放置时刷屏。
     * </p>
     */
    public static final DeferredHolder<Item, BlockItem> BASTION_GUARDIAN_HATCHERY_ITEM = ITEMS.register(
        "bastion_guardian_hatchery",
        () -> new BlockItem(BASTION_GUARDIAN_HATCHERY.get(), new Item.Properties())
    );

    /**
     * 光环节点方块物品：放置时做“归属/成本/上限”校验。
     */
    public static final DeferredHolder<Item, BlockItem> BASTION_AURA_NODE_ITEM = ITEMS.register(
        "bastion_aura_node",
        () -> new BastionAuraNodeBlock.BastionAuraNodeItem(BASTION_AURA_NODE.get(), new Item.Properties())
    );

    /**
     * 基地外壳方块物品（甲壳）。
     */
    public static final DeferredHolder<Item, BlockItem> BASTION_CHITIN_SHELL_ITEM = ITEMS.register(
        "bastion_chitin_shell",
        () -> new BlockItem(BASTION_CHITIN_SHELL.get(), new Item.Properties())
    );

    /** 炮台节点方块物品。 */
    public static final DeferredHolder<Item, BlockItem> BASTION_TURRET_ITEM = ITEMS.register(
        "bastion_turret",
        () -> new BlockItem(BASTION_TURRET.get(), new Item.Properties())
    );

    /** 陷阱节点方块物品。 */
    public static final DeferredHolder<Item, BlockItem> BASTION_TRAP_ITEM = ITEMS.register(
        "bastion_trap",
        () -> new BlockItem(BASTION_TRAP.get(), new Item.Properties())
    );

    /** 反爆外壳节点方块物品。 */
    public static final DeferredHolder<Item, BlockItem> BASTION_ANTI_EXPLOSION_SHELL_ITEM = ITEMS.register(
        "bastion_anti_explosion_shell",
        () -> new BlockItem(BASTION_ANTI_EXPLOSION_SHELL.get(), new Item.Properties())
    );

    /** 反火外壳节点方块物品。 */
    public static final DeferredHolder<Item, BlockItem> BASTION_ANTI_FIRE_SHELL_ITEM = ITEMS.register(
        "bastion_anti_fire_shell",
        () -> new BlockItem(BASTION_ANTI_FIRE_SHELL.get(), new Item.Properties())
    );

    /** 侦查道具，用于侦测附近基地信息。 */
    public static final DeferredHolder<Item, BastionScoutItem> BASTION_SCOUT_ITEM = ITEMS.register(
        "bastion_scout",
        () -> new BastionScoutItem(new Item.Properties())
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

    /**
     * 能源节点物品：在服务端放置时做“扣费/上限/归属”校验。
     */
    private static final class BastionEnergyNodeItem extends BlockItem {

        private BastionEnergyNodeItem(Block block, Properties properties) {
            super(block, properties);
        }

        @Override
        protected boolean placeBlock(
                BlockPlaceContext context,
                net.minecraft.world.level.block.state.BlockState state) {
            if (context == null) {
                return false;
            }

            // 客户端：保持原始行为（让客户端先行放置预览），最终以服务端为权威。
            if (context.getLevel().isClientSide()) {
                return super.placeBlock(context, state);
            }

            if (!(context.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel)
                || !(context.getPlayer() instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
                return false;
            }

            BastionSavedData savedData = BastionSavedData.get(serverLevel);

            // Round 3.2：能源节点类型不再要求玩家预先写入 CustomData。
            // 服务端会在 tryBuildEnergyNode 内部根据环境判定 + bastion_type.energy.priority_order
            // 自动选择最终类型，并将其写入方块属性与运行时缓存。
            net.minecraft.world.level.block.state.BlockState desired = state;

            boolean ok = BastionEnergyBuildService.tryBuildEnergyNode(
                serverLevel,
                savedData,
                serverPlayer,
                context.getClickedPos(),
                desired
            );

            // 说明：tryBuildEnergyNode 内部已执行 setBlock。
            // 这里返回 true 仅用于告诉上层“放置成功”，由 BlockItem 流程处理消耗/统计。
            return ok;
     }

    /**
     * 光环节点物品：在服务端放置时做“扣费/上限/归属”校验。
     */
        private static final class BastionAuraNodeItem extends BlockItem {

        private BastionAuraNodeItem(Block block, Properties properties) {
            super(block, properties);
        }

        @Override
        protected boolean placeBlock(
                BlockPlaceContext context,
                net.minecraft.world.level.block.state.BlockState state) {
            if (context == null) {
                return false;
            }

            if (context.getLevel().isClientSide()) {
                return super.placeBlock(context, state);
            }

            if (!(context.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel)
                || !(context.getPlayer() instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
                return false;
            }

            BastionSavedData savedData = BastionSavedData.get(serverLevel);

            boolean ok = BastionAuraNodeBlock.tryBuildAuraNode(
                serverLevel,
                savedData,
                serverPlayer,
                context.getClickedPos(),
                state
            );

            return ok;
        }
    }
}
}
