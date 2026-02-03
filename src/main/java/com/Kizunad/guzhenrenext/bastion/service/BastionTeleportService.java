package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * 基地传送网络服务 - 管理已占领基地之间的传送。
 */
public final class BastionTeleportService {

    private static final Map<UUID, Long> TELEPORT_COOLDOWNS = new ConcurrentHashMap<>();

    private BastionTeleportService() {
    }

    /** 传送相关常量。 */
    private static final class TeleportConstants {
        /** 传送冷却时间（tick）- 5分钟。 */
        static final long COOLDOWN_TICKS = 6000L;
        /** 传送消耗的真元。 */
        static final double ZHENYUAN_COST = 50.0;
        /** 游戏每秒 tick 数。 */
        static final long TICKS_PER_SECOND = 20L;
        /** 传送时水平偏移。 */
        static final double POSITION_OFFSET = 0.5D;
        /** 传送时抬升高度。 */
        static final double HEIGHT_OFFSET = 1.0D;
        /** UUID 显示长度。 */
        static final int UUID_DISPLAY_LENGTH = 8;

        private TeleportConstants() {
        }
    }

    /**
     * 尝试传送玩家到指定基地。
     *
     * @param level   服务端世界
     * @param player  玩家
     * @param bastionId 目标基地 UUID
     * @return 是否成功传送
     */
    public static boolean tryTeleport(ServerLevel level, ServerPlayer player, UUID bastionId) {
        BastionSavedData savedData = BastionSavedData.get(level);
        BastionData bastion = savedData.getBastion(bastionId);
        if (bastion == null) {
            player.sendSystemMessage(Component.literal("§c基地不存在"));
            return false;
        }

        if (bastion.captureState() == null || !bastion.captureState().isCapturedBy(player.getUUID())) {
            player.sendSystemMessage(Component.literal("§c你尚未占领该基地"));
            return false;
        }

        if (!bastion.dimension().equals(level.dimension())) {
            player.sendSystemMessage(Component.literal("§c暂不支持跨维度传送"));
            return false;
        }

        long gameTime = level.getGameTime();
        Long lastTeleport = TELEPORT_COOLDOWNS.get(player.getUUID());
        if (lastTeleport != null && (gameTime - lastTeleport) < TeleportConstants.COOLDOWN_TICKS) {
            long remaining = TeleportConstants.COOLDOWN_TICKS - (gameTime - lastTeleport);
            long remainingSeconds = remaining / TeleportConstants.TICKS_PER_SECOND;
            player.sendSystemMessage(Component.literal(
                String.format("§c传送冷却中，还需 %d 秒", remainingSeconds)
            ));
            return false;
        }

        double currentZhenyuan = ZhenYuanHelper.getAmount(player);
        if (currentZhenyuan < TeleportConstants.ZHENYUAN_COST) {
            player.sendSystemMessage(Component.literal("§c真元不足"));
            return false;
        }
        ZhenYuanHelper.modify(player, -TeleportConstants.ZHENYUAN_COST);

        BlockPos corePos = bastion.corePos();
        player.teleportTo(
            corePos.getX() + TeleportConstants.POSITION_OFFSET,
            corePos.getY() + TeleportConstants.HEIGHT_OFFSET,
            corePos.getZ() + TeleportConstants.POSITION_OFFSET
        );

        TELEPORT_COOLDOWNS.put(player.getUUID(), gameTime);

        player.sendSystemMessage(Component.literal(
            String.format(
                "§a已传送到基地 %s",
                bastionId.toString().substring(0, TeleportConstants.UUID_DISPLAY_LENGTH)
            )
        ));
        return true;
    }

    /**
     * 获取玩家已占领的所有基地。
     */
    public static List<BastionData> getCapturedBastions(ServerLevel level, UUID playerId) {
        return BastionSavedData.get(level).getAllBastions().stream()
            .filter(b -> b.captureState() != null && b.captureState().isCapturedBy(playerId))
            .toList();
    }
}
