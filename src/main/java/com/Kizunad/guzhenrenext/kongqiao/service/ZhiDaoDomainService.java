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
public final class ZhiDaoDomainService {

    public static final String KEY_DOMAIN_UNTIL_TICK =
        "GuzhenrenExtZhiDaoDomainUntilTick";
    public static final String KEY_DOMAIN_RADIUS = "GuzhenrenExtZhiDaoDomainRadius";
    public static final String KEY_DOMAIN_DODGE_CHANCE = "GuzhenrenExtZhiDaoDomainDodgeChance";
    public static final String KEY_DOMAIN_COUNTER_DAMAGE = "GuzhenrenExtZhiDaoDomainCounterDamage";
    public static final String KEY_DOMAIN_COST_PER_SECOND =
        "GuzhenrenExtZhiDaoDomainCostPerSecond";

    private static final int TICKS_PER_SECOND = 20;

    private static final int EFFECT_DURATION_TICKS = 40;

    private static final int BUFF_AMPLIFIER = 1;
    private static final int DEBUFF_AMPLIFIER = 1;
    private static final int RESISTANCE_AMPLIFIER = 0;

    private static final int COUNTER_WEAKNESS_DURATION_TICKS = 60;

    private ZhiDaoDomainService() {}

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

        final double chance = player.getPersistentData().getDouble(KEY_DOMAIN_DODGE_CHANCE);
        if (chance > 0.0 && player.getRandom().nextDouble() < chance) {
            event.setNewDamage(0.0F);
            
            final double counter = player.getPersistentData().getDouble(KEY_DOMAIN_COUNTER_DAMAGE);
            if (counter > 0.0 && event.getSource().getEntity() instanceof LivingEntity attacker) {
                attacker.hurt(player.damageSources().magic(), (float) counter);
                attacker.addEffect(
                    new MobEffectInstance(
                        MobEffects.WEAKNESS,
                        COUNTER_WEAKNESS_DURATION_TICKS,
                        2
                    )
                );
            }
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

            if (target.isAlliedTo(player) || target == player) {
                target.addEffect(
                    new MobEffectInstance(
                        MobEffects.DAMAGE_BOOST,
                        EFFECT_DURATION_TICKS,
                        BUFF_AMPLIFIER,
                        false,
                        false,
                        true
                    )
                );
                target.addEffect(
                    new MobEffectInstance(
                        MobEffects.DAMAGE_RESISTANCE,
                        EFFECT_DURATION_TICKS,
                        RESISTANCE_AMPLIFIER,
                        false,
                        false,
                        true
                    )
                );
            } else {
                target.addEffect(
                    new MobEffectInstance(
                        MobEffects.WEAKNESS,
                        EFFECT_DURATION_TICKS,
                        DEBUFF_AMPLIFIER,
                        false,
                        false,
                        true
                    )
                );
                target.addEffect(
                    new MobEffectInstance(
                        MobEffects.DIG_SLOWDOWN,
                        EFFECT_DURATION_TICKS,
                        DEBUFF_AMPLIFIER,
                        false,
                        false,
                        true
                    )
                );
            }
        }
    }

    public static void clearDomain(final ServerPlayer player) {
        player.getPersistentData().remove(KEY_DOMAIN_UNTIL_TICK);
        player.getPersistentData().remove(KEY_DOMAIN_RADIUS);
        player.getPersistentData().remove(KEY_DOMAIN_DODGE_CHANCE);
        player.getPersistentData().remove(KEY_DOMAIN_COUNTER_DAMAGE);
        player.getPersistentData().remove(KEY_DOMAIN_COST_PER_SECOND);

        ClientboundDomainRemovePayload payload = new ClientboundDomainRemovePayload(
            player.getUUID()
        );
        DomainNetworkHandler.sendDomainRemove(payload, player.position(), player.serverLevel());
    }
}
