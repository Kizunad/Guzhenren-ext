package com.Kizunad.guzhenrenext.xianqiao.command;

import com.Kizunad.guzhenrenext.xianqiao.block.XianqiaoBlocks;
import com.Kizunad.guzhenrenext.xianqiao.block.ApertureCoreBlockEntity;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ReturnPosition;
import com.Kizunad.guzhenrenext.xianqiao.service.OverworldTerrainSampler;
import com.Kizunad.guzhenrenext.xianqiao.spirit.LandSpiritEntity;
import com.Kizunad.guzhenrenext.xianqiao.spirit.XianqiaoEntities;
import com.mojang.logging.LogUtils;
import java.util.UUID;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import org.slf4j.Logger;

/**
 * 仙窍进出传送命令。
 */
public final class ApertureCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

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

    private static final int TERRAIN_SAMPLE_OFFSET = 16;

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
        ensureLandSpiritExists(apertureLevel, player, apertureInfo.center());

        ServerLevel currentLevel = player.serverLevel();
        worldData.setReturnPosition(
            player.getUUID(),
            new ReturnPosition(
                currentLevel.dimension().location().toString(),
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYRot(),
                player.getXRot()
            )
        );

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
     * 执行离开仙窍逻辑，优先返回进入前位置。
     */
    private static int leaveAperture(CommandContext<CommandSourceStack> context) {
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
        ReturnPosition returnPosition = worldData.getReturnPosition(player.getUUID());
        if (returnPosition != null) {
            ServerLevel targetLevel = resolveReturnLevel(player, returnPosition);
            if (targetLevel != null) {
                player.teleportTo(
                    targetLevel,
                    returnPosition.x(),
                    returnPosition.y(),
                    returnPosition.z(),
                    returnPosition.yRot(),
                    returnPosition.xRot()
                );
                return Command.SINGLE_SUCCESS;
            }
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

    private static ServerLevel resolveReturnLevel(ServerPlayer player, ReturnPosition returnPosition) {
        try {
            ResourceLocation targetDimensionLocation = ResourceLocation.parse(returnPosition.dimensionKey());
            ResourceKey<Level> targetDimension = ResourceKey.create(Registries.DIMENSION, targetDimensionLocation);
            return player.server.getLevel(targetDimension);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
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
        spawnLandSpirit(level, player, center);
        sampleInitialTerrains(level, player, center);
        worldData.markApertureInitialized(player.getUUID());
    }

    private static void sampleInitialTerrains(ServerLevel apertureLevel, ServerPlayer player, BlockPos center) {
        ServerLevel overworldLevel = player.server.getLevel(Level.OVERWORLD);
        if (overworldLevel == null) {
            LOGGER.warn("[ApertureCommand] 主世界未加载，跳过仙窍四向地形采样。玩家={}", player.getName().getString());
            return;
        }

        Holder<Biome> eastBiome = overworldLevel.registryAccess()
            .lookupOrThrow(Registries.BIOME)
            .getOrThrow(Biomes.PLAINS);
        Holder<Biome> southBiome = overworldLevel.registryAccess()
            .lookupOrThrow(Registries.BIOME)
            .getOrThrow(Biomes.FOREST);
        Holder<Biome> westBiome = overworldLevel.registryAccess()
            .lookupOrThrow(Registries.BIOME)
            .getOrThrow(Biomes.DESERT);
        Holder<Biome> northBiome = overworldLevel.registryAccess()
            .lookupOrThrow(Registries.BIOME)
            .getOrThrow(Biomes.TAIGA);
        RandomSource random = player.getRandom();

        sampleTerrainWithWarn(
            overworldLevel,
            apertureLevel,
            center.offset(TERRAIN_SAMPLE_OFFSET, 0, 0),
            eastBiome,
            random,
            "东"
        );
        sampleTerrainWithWarn(
            overworldLevel,
            apertureLevel,
            center.offset(0, 0, TERRAIN_SAMPLE_OFFSET),
            southBiome,
            random,
            "南"
        );
        sampleTerrainWithWarn(
            overworldLevel,
            apertureLevel,
            center.offset(-TERRAIN_SAMPLE_OFFSET, 0, 0),
            westBiome,
            random,
            "西"
        );
        sampleTerrainWithWarn(
            overworldLevel,
            apertureLevel,
            center.offset(0, 0, -TERRAIN_SAMPLE_OFFSET),
            northBiome,
            random,
            "北"
        );
    }

    private static void sampleTerrainWithWarn(
        ServerLevel overworldLevel,
        ServerLevel apertureLevel,
        BlockPos targetAnchor,
        Holder<Biome> targetBiome,
        RandomSource random,
        String directionName
    ) {
        boolean sampled = OverworldTerrainSampler.sampleAndPlace(
            overworldLevel,
            apertureLevel,
            targetAnchor,
            targetBiome,
            null,
            random
        );
        if (!sampled) {
            LOGGER.warn(
                "[ApertureCommand] 仙窍{}向地形采样失败，已降级跳过。目标锚点={}。",
                directionName,
                targetAnchor
            );
        }
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

    /**
     * 保证玩家仙窍中存在地灵。
     * <p>
     * 该方法用于补偿历史数据：旧版本只建平台不生成地灵。
     * 当玩家再次进入仙窍时，如果核心附近半径范围内未检索到地灵，则自动补生成一只并认主。
     * </p>
     *
     * @param level 仙窍维度
     * @param player 当前玩家
     * @param center 当前玩家仙窍核心坐标
     */
    private static void ensureLandSpiritExists(ServerLevel level, ServerPlayer player, BlockPos center) {
        spawnLandSpirit(level, player, center);
    }

    /**
     * 在核心上方生成并认主一只地灵。
     *
     * @param level 仙窍维度
     * @param player 地灵主人
     * @param center 仙窍核心坐标
     */
    private static void spawnLandSpirit(ServerLevel level, ServerPlayer player, BlockPos center) {
        BlockEntity blockEntity = level.getBlockEntity(center);
        if (!(blockEntity instanceof ApertureCoreBlockEntity coreBlockEntity)) {
            return;
        }

        UUID boundSpiritUUID = coreBlockEntity.getBoundSpiritUUID();
        if (boundSpiritUUID != null) {
            if (level.getEntity(boundSpiritUUID) instanceof LandSpiritEntity) {
                player.sendSystemMessage(Component.literal("地灵已存在，无需重复生成。"));
                return;
            }
            coreBlockEntity.setBoundSpiritUUID(null);
        }

        LandSpiritEntity entity = XianqiaoEntities.LAND_SPIRIT.get().create(level);
        if (entity == null) {
            return;
        }
        entity.setOwnerUUID(player.getUUID());
        entity.setBoundCorePos(center);
        entity.moveTo(
            center.getX() + BLOCK_CENTER_OFFSET,
            center.getY() + SPAWN_Y_OFFSET,
            center.getZ() + BLOCK_CENTER_OFFSET,
            player.getYRot(),
            player.getXRot()
        );
        if (level.addFreshEntity(entity)) {
            coreBlockEntity.setBoundSpiritUUID(entity.getUUID());
        }
    }
}
