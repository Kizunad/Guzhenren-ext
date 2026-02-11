package com.Kizunad.guzhenrenext.xianqiao.command;

import com.Kizunad.guzhenrenext.xianqiao.block.XianqiaoBlocks;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * 仙窍进出传送命令。
 */
public final class ApertureCommand {

    /** 仙窍维度键。 */
    private static final ResourceKey<Level> APERTURE_DIMENSION = ResourceKey.create(
        Registries.DIMENSION,
        ResourceLocation.fromNamespaceAndPath("guzhenrenext", "aperture_world")
    );

    /** 平台半径：3x3。 */
    private static final int PLATFORM_RADIUS = 1;

    /** 方块中心偏移（用于传送到格子中心）。 */
    private static final double BLOCK_CENTER_OFFSET = 0.5D;

    /** 出生点安全抬升高度。 */
    private static final int SPAWN_Y_OFFSET = 1;

    /** 箱子相对核心的 X 偏移。 */
    private static final int CHEST_OFFSET_X = 1;

    /** 箱子相对核心的 Y 偏移。 */
    private static final int CHEST_OFFSET_Y = 0;

    /** 箱子相对核心的 Z 偏移。 */
    private static final int CHEST_OFFSET_Z = 0;

    private ApertureCommand() {
    }

    /**
     * 注册仙窍命令。
     *
     * @param dispatcher Brigadier 分发器
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("guzhenren")
                .then(Commands.literal("enter_aperture").executes(ApertureCommand::enterAperture))
                .then(Commands.literal("leave_aperture").executes(ApertureCommand::leaveAperture))
        );
    }

    /**
     * 执行进入仙窍逻辑。
     */
    private static int enterAperture(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("需要玩家执行。"));
            return 0;
        }
        ServerLevel apertureLevel = player.server.getLevel(APERTURE_DIMENSION);
        if (apertureLevel == null) {
            context.getSource().sendFailure(Component.literal("仙窍维度未加载。"));
            return 0;
        }

        ApertureWorldData worldData = ApertureWorldData.get(apertureLevel);
        ApertureInfo apertureInfo = worldData.getOrAllocate(player.getUUID());
        initializeApertureIfNeeded(apertureLevel, worldData, player, apertureInfo);

        BlockPos center = apertureInfo.center();
        player.teleportTo(
            apertureLevel,
            center.getX() + BLOCK_CENTER_OFFSET,
            center.getY(),
            center.getZ() + BLOCK_CENTER_OFFSET,
            player.getYRot(),
            player.getXRot()
        );
        return Command.SINGLE_SUCCESS;
    }

    /**
     * 执行离开仙窍逻辑，返回主世界出生点。
     */
    private static int leaveAperture(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("需要玩家执行。"));
            return 0;
        }
        ServerLevel overworld = player.server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            context.getSource().sendFailure(Component.literal("主世界未加载。"));
            return 0;
        }

        BlockPos spawnPos = overworld.getSharedSpawnPos();
        player.teleportTo(
            overworld,
            spawnPos.getX() + BLOCK_CENTER_OFFSET,
            spawnPos.getY() + SPAWN_Y_OFFSET,
            spawnPos.getZ() + BLOCK_CENTER_OFFSET,
            player.getYRot(),
            player.getXRot()
        );
        return Command.SINGLE_SUCCESS;
    }

    /**
     * 首次进入时初始化仙窍平台、核心与箱子。
     */
    private static void initializeApertureIfNeeded(
        ServerLevel level,
        ApertureWorldData worldData,
        ServerPlayer player,
        ApertureInfo apertureInfo
    ) {
        if (worldData.isApertureInitialized(player.getUUID())) {
            return;
        }

        BlockPos center = apertureInfo.center();
        createInitialPlatform(level, center);
        worldData.markApertureInitialized(player.getUUID());
    }

    /**
     * 生成初始平台：3x3 基岩 + 核心方块 + 偏移一格箱子。
     */
    private static void createInitialPlatform(ServerLevel level, BlockPos center) {
        int baseY = center.getY() - 1;
        for (int offsetX = -PLATFORM_RADIUS; offsetX <= PLATFORM_RADIUS; offsetX++) {
            for (int offsetZ = -PLATFORM_RADIUS; offsetZ <= PLATFORM_RADIUS; offsetZ++) {
                BlockPos bedrockPos = new BlockPos(center.getX() + offsetX, baseY, center.getZ() + offsetZ);
                level.setBlock(bedrockPos, Blocks.BEDROCK.defaultBlockState(), Block.UPDATE_ALL);
            }
        }

        level.setBlock(center, XianqiaoBlocks.apertureCoreBlock().defaultBlockState(), Block.UPDATE_ALL);

        BlockPos chestPos = center.offset(CHEST_OFFSET_X, CHEST_OFFSET_Y, CHEST_OFFSET_Z);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
    }
}
