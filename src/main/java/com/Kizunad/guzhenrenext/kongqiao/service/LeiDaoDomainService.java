package com.Kizunad.guzhenrenext.kongqiao.service;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.domain.network.ClientboundDomainRemovePayload;
import com.Kizunad.guzhenrenext.kongqiao.domain.network.DomainNetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class LeiDaoDomainService {

    public static final String KEY_DOMAIN_UNTIL_TICK = "GuzhenrenExtLeiDaoDomainUntilTick";
    public static final String KEY_DOMAIN_RADIUS = "GuzhenrenExtLeiDaoDomainRadius";
    public static final String KEY_DOMAIN_LIGHTNING_INTERVAL =
        "GuzhenrenExtLeiDaoDomainLightningInterval";
    public static final String KEY_DOMAIN_STUN_DURATION = "GuzhenrenExtLeiDaoDomainStunDuration";
    public static final String KEY_DOMAIN_COST_PER_SECOND = "GuzhenrenExtLeiDaoDomainCostPerSecond";

    private static final int TICKS_PER_SECOND = 20;

    private static final int DEFAULT_LIGHTNING_INTERVAL_TICKS = 60;

    private static final int PARALYZE_DURATION_TICKS = 40;
    private static final int PARALYZE_AMPLIFIER = 2;

    private LeiDaoDomainService() {}

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
        final int configuredInterval = player
            .getPersistentData()
            .getInt(KEY_DOMAIN_LIGHTNING_INTERVAL);
        final int interval = configuredInterval <= 0
            ? DEFAULT_LIGHTNING_INTERVAL_TICKS
            : configuredInterval;
        final int stunDuration = player.getPersistentData().getInt(KEY_DOMAIN_STUN_DURATION);

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

            if (stunDuration > 0) {
                target.setDeltaMovement(Vec3.ZERO);
                target.hasImpulse = true;
            }

            target.addEffect(
                new MobEffectInstance(
                    MobEffects.DIG_SLOWDOWN,
                    PARALYZE_DURATION_TICKS,
                    PARALYZE_AMPLIFIER,
                    false,
                    false,
                    true
                )
            );

            if (player.tickCount % interval == 0) {
                LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(player.level());
                if (lightning != null) {
                    lightning.moveTo(target.position());
                    lightning.setVisualOnly(false);
                    player.level().addFreshEntity(lightning);
                }
            }
        }
    }

    public static void clearDomain(final ServerPlayer player) {
        player.getPersistentData().remove(KEY_DOMAIN_UNTIL_TICK);
        player.getPersistentData().remove(KEY_DOMAIN_RADIUS);
        player.getPersistentData().remove(KEY_DOMAIN_LIGHTNING_INTERVAL);
        player.getPersistentData().remove(KEY_DOMAIN_STUN_DURATION);
        player.getPersistentData().remove(KEY_DOMAIN_COST_PER_SECOND);

        ClientboundDomainRemovePayload payload = new ClientboundDomainRemovePayload(
            player.getUUID()
        );
        DomainNetworkHandler.sendDomainRemove(payload, player.position(), player.serverLevel());
    }
}
