package com.Kizunad.guzhenrenext.kongqiao.service;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 血道运行时服务。
 * <p>
 * 当前职责：清理血河蠎召唤物，避免召唤物永久滞留造成堆积与性能风险。
 * </p>
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class XueDaoRuntimeService {

    public static final String KEY_PYTHON_UUID =
        "GuzhenrenExtXueDao_PythonUuid";
    public static final String KEY_PYTHON_UNTIL_TICK =
        "GuzhenrenExtXueDao_PythonUntilTick";

    private XueDaoRuntimeService() {}

    @SubscribeEvent
    public static void onPlayerTick(final PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        cleanupPython(player);
    }

    public static void bindPython(
        final ServerPlayer player,
        final UUID uuid,
        final int untilTick
    ) {
        if (player == null || uuid == null) {
            return;
        }
        player.getPersistentData().putUUID(KEY_PYTHON_UUID, uuid);
        player.getPersistentData().putInt(KEY_PYTHON_UNTIL_TICK, untilTick);
    }

    public static void despawnExistingPython(final ServerPlayer player) {
        if (player == null) {
            return;
        }
        if (!player.getPersistentData().hasUUID(KEY_PYTHON_UUID)) {
            return;
        }
        final ServerLevel level = player.serverLevel();
        final Entity existing = level.getEntity(
            player.getPersistentData().getUUID(KEY_PYTHON_UUID)
        );
        if (existing != null && existing.isAlive()) {
            existing.discard();
        }
        player.getPersistentData().remove(KEY_PYTHON_UUID);
        player.getPersistentData().remove(KEY_PYTHON_UNTIL_TICK);
    }

    private static void cleanupPython(final ServerPlayer player) {
        if (player == null) {
            return;
        }
        if (!player.getPersistentData().hasUUID(KEY_PYTHON_UUID)) {
            return;
        }
        final int until = player.getPersistentData().getInt(KEY_PYTHON_UNTIL_TICK);
        if (until > 0 && player.tickCount <= until) {
            return;
        }
        despawnExistingPython(player);
    }
}

