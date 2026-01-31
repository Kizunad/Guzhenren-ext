package com.Kizunad.guzhenrenext.kongqiao.service;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.domain.network.ClientboundDomainRemovePayload;
import com.Kizunad.guzhenrenext.kongqiao.domain.network.DomainNetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class FengDaoDomainService {

    public static final String KEY_DOMAIN_UNTIL_TICK =
        "GuzhenrenExtFengDaoDomainUntilTick";
    public static final String KEY_DOMAIN_RADIUS = "GuzhenrenExtFengDaoDomainRadius";
    public static final String KEY_DOMAIN_KNOCKBACK =
        "GuzhenrenExtFengDaoDomainKnockback";
    public static final String KEY_DOMAIN_COST_PER_SECOND =
        "GuzhenrenExtFengDaoDomainCostPerSecond";

    private static final int TICKS_PER_SECOND = 20;
    private static final double MIN_DIR_SQR = 0.0001;

    private static final double DEFAULT_VERTICAL_REPEL = 1.0;

    private FengDaoDomainService() {}

    @SubscribeEvent
    public static void onPlayerTick(final PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            tick(player);
        }
    }

    public static void tick(final ServerPlayer player) {
        final int until = player.getPersistentData().getInt(KEY_DOMAIN_UNTIL_TICK);
        if (until <= 0 || player.tickCount >= until) {
            if (until > 0) {
                clearDomain(player);
            }
            return;
        }

        if (player.tickCount % TICKS_PER_SECOND == 0) {
            final double costBase = player
                .getPersistentData()
                .getDouble(KEY_DOMAIN_COST_PER_SECOND);
            final double cost = ZhenYuanHelper.calculateGuCost(player, costBase);
            if (!ZhenYuanHelper.hasEnough(player, cost)) {
                clearDomain(player);
                return;
            }
            ZhenYuanHelper.modify(player, -cost);
        }

        final double radius = player.getPersistentData().getDouble(KEY_DOMAIN_RADIUS);
        final double strength = player.getPersistentData().getDouble(KEY_DOMAIN_KNOCKBACK);
        if (radius <= 0.0 || strength <= 0.0) {
            return;
        }

        final AABB area = player.getBoundingBox().inflate(radius);
        for (Entity e : player.level().getEntities(player, area)) {
            if (!shouldRepel(player, e)) {
                continue;
            }

            final double distSqr = e.distanceToSqr(player);
            if (distSqr > radius * radius) {
                continue;
            }

            Vec3 dir = e.position().subtract(player.position()).normalize();
            if (dir.lengthSqr() < MIN_DIR_SQR) {
                dir = new Vec3(0.0, DEFAULT_VERTICAL_REPEL, 0.0);
            }

            final double distance = Math.sqrt(distSqr);
            final double distanceFactor = (radius - distance) / radius;
            final double force = strength * (1.0 + Math.max(0.0, distanceFactor));
            e.setDeltaMovement(e.getDeltaMovement().add(dir.scale(force)));
            e.hurtMarked = true;
        }
    }

    private static boolean shouldRepel(final ServerPlayer player, final Entity e) {
        if (e == null || player == null) {
            return false;
        }
        if (e instanceof Projectile proj) {
            return proj.getOwner() != player;
        }
        if (e instanceof LivingEntity living) {
            return !living.isAlliedTo(player);
        }
        return false;
    }

    public static void clearDomain(final ServerPlayer player) {
        player.getPersistentData().remove(KEY_DOMAIN_UNTIL_TICK);
        player.getPersistentData().remove(KEY_DOMAIN_RADIUS);
        player.getPersistentData().remove(KEY_DOMAIN_KNOCKBACK);
        player.getPersistentData().remove(KEY_DOMAIN_COST_PER_SECOND);

        ClientboundDomainRemovePayload payload = new ClientboundDomainRemovePayload(
            player.getUUID()
        );
        DomainNetworkHandler.sendDomainRemove(payload, player.position(), player.serverLevel());
    }
}
