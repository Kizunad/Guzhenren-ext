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
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class XueDaoDomainService {

    public static final String KEY_DOMAIN_UNTIL_TICK = "GuzhenrenExtXueDaoDomainUntilTick";
    public static final String KEY_DOMAIN_RADIUS = "GuzhenrenExtXueDaoDomainRadius";
    public static final String KEY_DOMAIN_ENEMY_DAMAGE = "GuzhenrenExtXueDaoDomainEnemyDamage";
    public static final String KEY_DOMAIN_WITHER_AMP = "GuzhenrenExtXueDaoDomainWitherAmp";
    public static final String KEY_DOMAIN_ABSORPTION_PER_KILL =
        "GuzhenrenExtXueDaoDomainAbsorptionPerKill";
    public static final String KEY_DOMAIN_COST_PER_SECOND = "GuzhenrenExtXueDaoDomainCostPerSecond";

    private static final int TICKS_PER_SECOND = 20;

    private static final int WITHER_DURATION_TICKS = 60;
    private static final float LIFESTEAL_RATIO = 0.3F;

    private XueDaoDomainService() {}

    @SubscribeEvent
    public static void onPlayerTick(final PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            tick(player);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(final LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        final int until = player.getPersistentData().getInt(KEY_DOMAIN_UNTIL_TICK);
        if (until <= player.tickCount) {
            return;
        }

        final double absorptionPerKill = player
            .getPersistentData()
            .getDouble(KEY_DOMAIN_ABSORPTION_PER_KILL);
        if (absorptionPerKill > 0.0) {
            player.setAbsorptionAmount(
                player.getAbsorptionAmount() + (float) absorptionPerKill
            );
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
        final int witherAmp = player.getPersistentData().getInt(KEY_DOMAIN_WITHER_AMP);

        if (radius <= 0.0) {
            return;
        }

        final AABB area = player.getBoundingBox().inflate(radius);
        float totalHeal = 0.0F;

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

            target.addEffect(
                new MobEffectInstance(MobEffects.WITHER, WITHER_DURATION_TICKS, witherAmp)
            );

            if (damage <= 0.0) {
                continue;
            }
            if (!target.hurt(player.damageSources().magic(), (float) damage)) {
                continue;
            }

            totalHeal += (float) damage * LIFESTEAL_RATIO;
        }

        if (totalHeal > 0.0F) {
            player.heal(totalHeal);
        }
    }

    public static void clearDomain(final ServerPlayer player) {
        player.getPersistentData().remove(KEY_DOMAIN_UNTIL_TICK);
        player.getPersistentData().remove(KEY_DOMAIN_RADIUS);
        player.getPersistentData().remove(KEY_DOMAIN_ENEMY_DAMAGE);
        player.getPersistentData().remove(KEY_DOMAIN_WITHER_AMP);
        player.getPersistentData().remove(KEY_DOMAIN_ABSORPTION_PER_KILL);
        player.getPersistentData().remove(KEY_DOMAIN_COST_PER_SECOND);

        ClientboundDomainRemovePayload payload = new ClientboundDomainRemovePayload(
            player.getUUID()
        );
        DomainNetworkHandler.sendDomainRemove(payload, player.position(), player.serverLevel());
    }
}
