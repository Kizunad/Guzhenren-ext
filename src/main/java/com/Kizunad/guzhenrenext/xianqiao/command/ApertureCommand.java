package com.Kizunad.guzhenrenext.xianqiao.command;

import com.Kizunad.guzhenrenext.xianqiao.entry.ApertureEntryChannel;
import com.Kizunad.guzhenrenext.xianqiao.entry.ApertureEntryRuntime;
import com.Kizunad.guzhenrenext.xianqiao.entry.ApertureInitializationResult;
import com.Kizunad.guzhenrenext.xianqiao.block.ApertureCoreBlockEntity;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ReturnPosition;
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
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
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

    /** 方块中心偏移（用于传送到格子中心）。 */
    private static final double BLOCK_CENTER_OFFSET = 0.5D;

    /** 出生点安全抬升高度。 */
    private static final int SPAWN_Y_OFFSET = 1;

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

        ApertureInitializationResult result = ApertureEntryRuntime.trigger(
            player,
            ApertureEntryChannel.LEGACY_COMMAND
        );
        if (!result.accepted()) {
            context.getSource().sendFailure(Component.literal(result.message()));
            return 0;
        }
        context.getSource().sendSuccess(() -> Component.literal(result.message()), false);
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
     * 保证玩家仙窍中存在地灵。
     * <p>
     * 该方法用于补偿历史数据：旧版本只建平台不生成地灵。
     * 当玩家再次进入仙窍时，如果核心附近范围内未检索到地灵，则自动补生成一只并认主。
     * </p>
     *
     * @param level 仙窍维度
     * @param player 当前玩家
     * @param center 当前玩家仙窍核心坐标
     */
    private static void ensureLandSpiritExists(ServerLevel level, ServerPlayer player, BlockPos center) {
        spawnLandSpirit(level, player, center);
    }

    public static void ensureLandSpiritExistsFromSharedEntry(
        ServerLevel level,
        ServerPlayer player,
        BlockPos center
    ) {
        ensureLandSpiritExists(level, player, center);
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
