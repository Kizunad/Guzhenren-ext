package com.Kizunad.guzhenrenext.kongqiao.service;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.domain.network.ClientboundDomainRemovePayload;
import com.Kizunad.guzhenrenext.kongqiao.domain.network.DomainNetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class YanDaoDomainService {

    public static final String KEY_DOMAIN_UNTIL_TICK = "GuzhenrenExtYanDaoDomainUntilTick";
    public static final String KEY_DOMAIN_RADIUS = "GuzhenrenExtYanDaoDomainRadius";
    public static final String KEY_DOMAIN_ENEMY_DAMAGE = "GuzhenrenExtYanDaoDomainEnemyDamage";
    public static final String KEY_DOMAIN_EXPLOSION_RADIUS =
        "GuzhenrenExtYanDaoDomainExplosionRadius";
    public static final String KEY_DOMAIN_COST_PER_SECOND = "GuzhenrenExtYanDaoDomainCostPerSecond";

    private static final int TICKS_PER_SECOND = 20;

    private static final int FIRE_SECONDS = 3;

    private YanDaoDomainService() {}

    @SubscribeEvent
    public static void onPlayerTick(final PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            tick(player);
        }
    }

    public static void tick(final ServerPlayer player) {
        if (player.tickCount % TICKS_PER_SECOND != 0) {
            return;
        }

        final int until = player.getPersistentData().getInt(KEY_DOMAIN_UNTIL_TICK);
        if (until <= 0 || player.tickCount >= until) {
            if (until > 0) {
                clearDomain(player);
            }
            return;
        }

        final double costBase = player
            .getPersistentData()
            .getDouble(KEY_DOMAIN_COST_PER_SECOND);
        final double cost = ZhenYuanHelper.calculateGuCost(player, costBase);
        if (!ZhenYuanHelper.hasEnough(player, cost)) {
            clearDomain(player);
            return;
        }
        ZhenYuanHelper.modify(player, -cost);

        final double radius = player.getPersistentData().getDouble(KEY_DOMAIN_RADIUS);
        final double damage = player.getPersistentData().getDouble(KEY_DOMAIN_ENEMY_DAMAGE);
        if (radius <= 0.0) {
            return;
        }

        final AABB area = player.getBoundingBox().inflate(radius);
        for (Entity e : player.level().getEntities(player, area)) {
            if (!(e instanceof LivingEntity target)) {
                continue;
            }
            if (target.isAlliedTo(player)) {
                continue;
            }
            if (e.distanceToSqr(player) > radius * radius) {
                continue;
            }

            if (damage > 0.0) {
                target.hurt(player.damageSources().inFire(), (float) damage);
            }
            target.igniteForSeconds(FIRE_SECONDS);
        }
    }

    public static void clearDomain(final ServerPlayer player) {
        final double explosionRadius = player
            .getPersistentData()
            .getDouble(KEY_DOMAIN_EXPLOSION_RADIUS);
        if (explosionRadius > 0.0) {
            player.level().explode(
                player,
                player.getX(),
                player.getY(),
                player.getZ(),
                (float) explosionRadius,
                Level.ExplosionInteraction.NONE
            );
        }

        player.getPersistentData().remove(KEY_DOMAIN_UNTIL_TICK);
        player.getPersistentData().remove(KEY_DOMAIN_RADIUS);
        player.getPersistentData().remove(KEY_DOMAIN_ENEMY_DAMAGE);
        player.getPersistentData().remove(KEY_DOMAIN_EXPLOSION_RADIUS);
        player.getPersistentData().remove(KEY_DOMAIN_COST_PER_SECOND);

        ClientboundDomainRemovePayload payload = new ClientboundDomainRemovePayload(
            player.getUUID()
        );
        DomainNetworkHandler.sendDomainRemove(payload, player.position(), player.serverLevel());
    }
}
