package com.Kizunad.guzhenrenext.kongqiao.service;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.domain.network.ClientboundDomainRemovePayload;
import com.Kizunad.guzhenrenext.kongqiao.domain.network.DomainNetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class ShuiDaoDomainService {

    public static final String KEY_DOMAIN_UNTIL_TICK =
        "GuzhenrenExtShuiDaoDomainUntilTick";
    public static final String KEY_DOMAIN_RADIUS = "GuzhenrenExtShuiDaoDomainRadius";
    public static final String KEY_DOMAIN_REGEN_AMP = "GuzhenrenExtShuiDaoDomainRegenAmp";
    public static final String KEY_DOMAIN_WEAKNESS_AMP =
        "GuzhenrenExtShuiDaoDomainWeaknessAmp";
    public static final String KEY_DOMAIN_MANA_SHIELD_RATIO =
        "GuzhenrenExtShuiDaoDomainManaShieldRatio";
    public static final String KEY_DOMAIN_COST_PER_SECOND =
        "GuzhenrenExtShuiDaoDomainCostPerSecond";

    private static final int TICKS_PER_SECOND = 20;

    private static final int EFFECT_DURATION_TICKS = 40;
    private static final int SLOWNESS_AMPLIFIER = 1;

    private ShuiDaoDomainService() {}

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

        final double ratio = player
            .getPersistentData()
            .getDouble(KEY_DOMAIN_MANA_SHIELD_RATIO);
        if (ratio <= 0.0) {
            return;
        }

        final float damage = event.getOriginalDamage();
        if (damage <= 0.0F) {
            return;
        }

        final double cost = damage * ratio;
        if (ZhenYuanHelper.hasEnough(player, cost)) {
            ZhenYuanHelper.modify(player, -cost);
            event.setNewDamage(0.0F);
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
        final int regenAmp = player.getPersistentData().getInt(KEY_DOMAIN_REGEN_AMP);
        final int weakAmp = player.getPersistentData().getInt(KEY_DOMAIN_WEAKNESS_AMP);

        player.addEffect(
            new MobEffectInstance(
                MobEffects.REGENERATION,
                EFFECT_DURATION_TICKS,
                regenAmp,
                false,
                false,
                true
            )
        );

        if (radius <= 0.0) {
            return;
        }

        final AABB area = player.getBoundingBox().inflate(radius);
        for (Entity e : player.level().getEntities(player, area)) {
            if (!(e instanceof LivingEntity target)) {
                continue;
            }
            if (e.distanceToSqr(player) > radius * radius) {
                continue;
            }

            if (target.isAlliedTo(player)) {
                target.addEffect(
                    new MobEffectInstance(
                        MobEffects.REGENERATION,
                        EFFECT_DURATION_TICKS,
                        regenAmp,
                        false,
                        false,
                        true
                    )
                );
                continue;
            }

            target.addEffect(
                new MobEffectInstance(
                    MobEffects.WEAKNESS,
                    EFFECT_DURATION_TICKS,
                    weakAmp,
                    false,
                    false,
                    true
                )
            );
            target.addEffect(
                new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    EFFECT_DURATION_TICKS,
                    SLOWNESS_AMPLIFIER,
                    false,
                    false,
                    true
                )
            );
        }
    }

    public static void clearDomain(final ServerPlayer player) {
        player.getPersistentData().remove(KEY_DOMAIN_UNTIL_TICK);
        player.getPersistentData().remove(KEY_DOMAIN_RADIUS);
        player.getPersistentData().remove(KEY_DOMAIN_REGEN_AMP);
        player.getPersistentData().remove(KEY_DOMAIN_WEAKNESS_AMP);
        player.getPersistentData().remove(KEY_DOMAIN_MANA_SHIELD_RATIO);
        player.getPersistentData().remove(KEY_DOMAIN_COST_PER_SECOND);

        ClientboundDomainRemovePayload payload = new ClientboundDomainRemovePayload(
            player.getUUID()
        );
        DomainNetworkHandler.sendDomainRemove(payload, player.position(), player.serverLevel());
    }
}
