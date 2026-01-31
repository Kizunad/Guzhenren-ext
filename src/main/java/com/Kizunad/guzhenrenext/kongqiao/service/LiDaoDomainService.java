package com.Kizunad.guzhenrenext.kongqiao.service;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.domain.network.ClientboundDomainRemovePayload;
import com.Kizunad.guzhenrenext.kongqiao.domain.network.DomainNetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class LiDaoDomainService {

    public static final String KEY_DOMAIN_UNTIL_TICK = "GuzhenrenExtLiDaoDomainUntilTick";
    public static final String KEY_DOMAIN_RADIUS = "GuzhenrenExtLiDaoDomainRadius";
    public static final String KEY_DOMAIN_REPEL_STRENGTH = "GuzhenrenExtLiDaoDomainRepelStrength";
    public static final String KEY_DOMAIN_GRAVITY_MULTIPLIER =
        "GuzhenrenExtLiDaoDomainGravityMultiplier";
    public static final String KEY_DOMAIN_COST_PER_SECOND = "GuzhenrenExtLiDaoDomainCostPerSecond";

    private static final int TICKS_PER_SECOND = 20;
    private static final double MIN_SPEED = 0.1;
    private static final double DAMAGE_PER_SPEED = 10.0;

    private static final double GRAVITY_FORCE = 0.05;

    private LiDaoDomainService() {}

    @SubscribeEvent
    public static void onPlayerTick(final PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            tick(player);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(final LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        final int until = player.getPersistentData().getInt(KEY_DOMAIN_UNTIL_TICK);
        if (until <= player.tickCount) {
            return;
        }

        final Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof LivingEntity livingAttacker)) {
            return;
        }
        if (attacker == player || attacker.isAlliedTo(player)) {
            return;
        }

        final double radius = player.getPersistentData().getDouble(KEY_DOMAIN_RADIUS);
        if (radius <= 0.0 || player.distanceToSqr(attacker) > radius * radius) {
            return;
        }

        // 真实反伤：基于玩家攻击力
        final float strength = (float) player.getAttributeValue(
            net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE
        );
        livingAttacker.hurt(player.damageSources().mobAttack(player), strength);
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
        final double strength = player.getPersistentData().getDouble(KEY_DOMAIN_REPEL_STRENGTH);
        final double gravityMult = player
            .getPersistentData()
            .getDouble(KEY_DOMAIN_GRAVITY_MULTIPLIER);

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

            final double distSqr = e.distanceToSqr(player);
            if (distSqr > radius * radius) {
                continue;
            }

            if (gravityMult > 0.0) {
                e.setDeltaMovement(e.getDeltaMovement().add(0.0, -GRAVITY_FORCE * gravityMult, 0.0));
                e.hurtMarked = true;
            }

            if (strength <= 0.0) {
                continue;
            }

            final double motionX = e.getDeltaMovement().x;
            final double motionY = e.getDeltaMovement().y;
            final double motionZ = e.getDeltaMovement().z;
            final double speed = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);

            if (speed <= MIN_SPEED) {
                continue;
            }

            final double dx = e.getX() - player.getX();
            final double dz = e.getZ() - player.getZ();
            final double horizontalSqr = dx * dx + dz * dz;
            if (horizontalSqr <= 0.0) {
                continue;
            }

            final double invLen = 1.0 / Math.sqrt(horizontalSqr);
            final double dirX = dx * invLen;
            final double dirZ = dz * invLen;

            final double distance = Math.sqrt(distSqr);
            final double distanceFactor = (radius - distance) / radius;
            final double force = strength * (1.0 + Math.max(0.0, distanceFactor));

            e.setDeltaMovement(
                e.getDeltaMovement().add(dirX * force * (1.0 + speed), 0.0, dirZ * force * (1.0 + speed))
            );
            e.hurtMarked = true;

            target.hurt(
                player.damageSources().flyIntoWall(),
                (float) (speed * DAMAGE_PER_SPEED)
            );
        }
    }

    public static void clearDomain(final ServerPlayer player) {
        player.getPersistentData().remove(KEY_DOMAIN_UNTIL_TICK);
        player.getPersistentData().remove(KEY_DOMAIN_RADIUS);
        player.getPersistentData().remove(KEY_DOMAIN_REPEL_STRENGTH);
        player.getPersistentData().remove(KEY_DOMAIN_GRAVITY_MULTIPLIER);
        player.getPersistentData().remove(KEY_DOMAIN_COST_PER_SECOND);

        ClientboundDomainRemovePayload payload = new ClientboundDomainRemovePayload(
            player.getUUID()
        );
        DomainNetworkHandler.sendDomainRemove(payload, player.position(), player.serverLevel());
    }
}
