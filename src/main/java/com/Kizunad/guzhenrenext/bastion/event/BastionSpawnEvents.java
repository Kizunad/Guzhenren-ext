package com.Kizunad.guzhenrenext.bastion.event;

import com.Kizunad.guzhenrenext.bastion.service.BastionSpawnService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

/**
 * 基地自然刷怪控制事件。
 * <p>
 * 拦截自然刷怪，确保符合领地道途规则。
 * </p>
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
public final class BastionSpawnEvents {

    private BastionSpawnEvents() {
    }

    /**
     * 检查自然刷怪是否允许。
     *
     * @param event 刷怪事件
     */
    @SubscribeEvent
    public static void onCheckSpawn(FinalizeSpawnEvent event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            Mob mob = event.getEntity();
            
            // 忽略非自然生成（如刷怪笼、指令、繁殖等），只干预自然生成和区块生成
            MobSpawnType spawnType = event.getSpawnType();
            if (spawnType == MobSpawnType.NATURAL
                    || spawnType == MobSpawnType.CHUNK_GENERATION
                    || spawnType == MobSpawnType.PATROL) {
                // 如果是有 owner 的领地，进行过滤
                if (!BastionSpawnService.isNaturalSpawnAllowed(serverLevel, mob.blockPosition(), mob)) {
                    event.setSpawnCancelled(true);
                }
            }
        }
    }
}
