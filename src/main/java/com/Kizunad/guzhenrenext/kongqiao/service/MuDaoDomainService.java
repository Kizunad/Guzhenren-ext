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
import java.util.Comparator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class MuDaoDomainService {

    public static final String KEY_DOMAIN_UNTIL_TICK = "GuzhenrenExtMuDaoDomainUntilTick";
    public static final String KEY_DOMAIN_RADIUS = "GuzhenrenExtMuDaoDomainRadius";
    public static final String KEY_DOMAIN_ENEMY_DAMAGE = "GuzhenrenExtMuDaoDomainEnemyDamage";
    public static final String KEY_DOMAIN_LIFESTEAL = "GuzhenrenExtMuDaoDomainLifesteal";
    public static final String KEY_DOMAIN_DAMAGE_TRANSFER_RATIO =
        "GuzhenrenExtMuDaoDomainDamageTransferRatio";
    public static final String KEY_DOMAIN_REGEN_AMP = "GuzhenrenExtMuDaoDomainRegenAmp";
    public static final String KEY_DOMAIN_COST_PER_SECOND = "GuzhenrenExtMuDaoDomainCostPerSecond";

    private static final int TICKS_PER_SECOND = 20;

    private static final int ROOT_DURATION_TICKS = 40;
    private static final int ROOT_SLOW_AMPLIFIER = 6;

    private MuDaoDomainService() {}

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

        final double transferRatio = player
            .getPersistentData()
            .getDouble(KEY_DOMAIN_DAMAGE_TRANSFER_RATIO);
        if (transferRatio <= 0.0) {
            return;
        }

        final double radius = player.getPersistentData().getDouble(KEY_DOMAIN_RADIUS);
        if (radius <= 0.0) {
            return;
        }

        final AABB area = player.getBoundingBox().inflate(radius);
        final LivingEntity target = player
            .level()
            .getEntitiesOfClass(LivingEntity.class, area, e -> !e.isAlliedTo(player) && e != player)
            .stream()
            .max(Comparator.comparingDouble(LivingEntity::getHealth))
            .orElse(null);

        if (target != null) {
            final float originalDamage = event.getOriginalDamage();
            final float transferred = (float) (originalDamage * transferRatio);
            target.hurt(player.damageSources().magic(), transferred);
            event.setNewDamage(originalDamage - transferred);
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
        final double lifesteal = player.getPersistentData().getDouble(KEY_DOMAIN_LIFESTEAL);
        final int regenAmp = player.getPersistentData().getInt(KEY_DOMAIN_REGEN_AMP);

        if (regenAmp > 0) {
            player.addEffect(
                new MobEffectInstance(
                    MobEffects.REGENERATION,
                    ROOT_DURATION_TICKS,
                    regenAmp - 1,
                    false,
                    false,
                    true
                )
            );
        }

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
                new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    ROOT_DURATION_TICKS,
                    ROOT_SLOW_AMPLIFIER,
                    false,
                    false,
                    true
                )
            );

            if (damage <= 0.0) {
                continue;
            }
            if (!target.hurt(player.damageSources().magic(), (float) damage)) {
                continue;
            }

            totalHeal += (float) (damage * lifesteal);
        }

        if (totalHeal > 0.0F) {
            player.heal(totalHeal);
        }
    }

    public static void clearDomain(final ServerPlayer player) {
        player.getPersistentData().remove(KEY_DOMAIN_UNTIL_TICK);
        player.getPersistentData().remove(KEY_DOMAIN_RADIUS);
        player.getPersistentData().remove(KEY_DOMAIN_ENEMY_DAMAGE);
        player.getPersistentData().remove(KEY_DOMAIN_LIFESTEAL);
        player.getPersistentData().remove(KEY_DOMAIN_DAMAGE_TRANSFER_RATIO);
        player.getPersistentData().remove(KEY_DOMAIN_REGEN_AMP);
        player.getPersistentData().remove(KEY_DOMAIN_COST_PER_SECOND);

        ClientboundDomainRemovePayload payload = new ClientboundDomainRemovePayload(
            player.getUUID()
        );
        DomainNetworkHandler.sendDomainRemove(payload, player.position(), player.serverLevel());
    }
}
