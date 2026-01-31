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
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class HunDaoDomainService {

    public static final String KEY_DOMAIN_UNTIL_TICK = "GuzhenrenExtHunDaoDomainUntilTick";
    public static final String KEY_DOMAIN_RADIUS = "GuzhenrenExtHunDaoDomainRadius";
    public static final String KEY_DOMAIN_ENEMY_DAMAGE = "GuzhenrenExtHunDaoDomainEnemyDamage";
    public static final String KEY_DOMAIN_SOUL_DAMAGE = "GuzhenrenExtHunDaoDomainSoulDamage";
    public static final String KEY_DOMAIN_WEAKNESS_AMP = "GuzhenrenExtHunDaoDomainWeaknessAmp";
    public static final String KEY_DOMAIN_COST_PER_SECOND = "GuzhenrenExtHunDaoDomainCostPerSecond";

    private static final int TICKS_PER_SECOND = 20;

    private static final float FEAR_TRIGGER_CHANCE = 0.2F;
    private static final double RANDOM_DIR_RANGE = 0.5;
    private static final double RANDOM_MOVE_SCALE = 0.3;

    private static final int BLINDNESS_DURATION_TICKS = 60;
    private static final int CONFUSION_DURATION_TICKS = 100;
    private static final int WEAKNESS_DURATION_TICKS = 60;

    private static final double MIN_RADIUS = 0.0;

    private HunDaoDomainService() {}

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
        final double damage = player.getPersistentData().getDouble(KEY_DOMAIN_ENEMY_DAMAGE);
        final double soulDamage = player.getPersistentData().getDouble(KEY_DOMAIN_SOUL_DAMAGE);
        final int weaknessAmp = player.getPersistentData().getInt(KEY_DOMAIN_WEAKNESS_AMP);

        if (radius <= MIN_RADIUS) {
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

            if (player.getRandom().nextFloat() < FEAR_TRIGGER_CHANCE) {
                applyFearMovement(target, player);
            }

            if (player.tickCount % TICKS_PER_SECOND == 0) {
                target.addEffect(
                    new MobEffectInstance(
                        MobEffects.BLINDNESS,
                        BLINDNESS_DURATION_TICKS,
                        0
                    )
                );
                target.addEffect(
                    new MobEffectInstance(
                        MobEffects.CONFUSION,
                        CONFUSION_DURATION_TICKS,
                        0
                    )
                );
                if (weaknessAmp > 0) {
                    target.addEffect(
                        new MobEffectInstance(
                            MobEffects.WEAKNESS,
                            WEAKNESS_DURATION_TICKS,
                            weaknessAmp
                        )
                    );
                }
                
                if (damage > 0.0) {
                    target.hurt(player.damageSources().magic(), (float) damage);
                }
                if (soulDamage > 0.0) {
                    target.hurt(player.damageSources().magic(), (float) soulDamage);
                }
            }
        }
    }

    private static void applyFearMovement(final LivingEntity target, final ServerPlayer player) {
        final double dx = player.getRandom().nextDouble() - RANDOM_DIR_RANGE;
        final double dz = player.getRandom().nextDouble() - RANDOM_DIR_RANGE;
        final double lenSqr = dx * dx + dz * dz;
        if (lenSqr <= 0.0) {
            return;
        }

        final double invLen = 1.0 / Math.sqrt(lenSqr);
        target.addDeltaMovement(
            new Vec3(
                dx * invLen * RANDOM_MOVE_SCALE,
                0.0,
                dz * invLen * RANDOM_MOVE_SCALE
            )
        );
        target.hurtMarked = true;
    }

    public static void clearDomain(final ServerPlayer player) {
        player.getPersistentData().remove(KEY_DOMAIN_UNTIL_TICK);
        player.getPersistentData().remove(KEY_DOMAIN_RADIUS);
        player.getPersistentData().remove(KEY_DOMAIN_ENEMY_DAMAGE);
        player.getPersistentData().remove(KEY_DOMAIN_SOUL_DAMAGE);
        player.getPersistentData().remove(KEY_DOMAIN_WEAKNESS_AMP);
        player.getPersistentData().remove(KEY_DOMAIN_COST_PER_SECOND);

        ClientboundDomainRemovePayload payload = new ClientboundDomainRemovePayload(
            player.getUUID()
        );
        DomainNetworkHandler.sendDomainRemove(payload, player.position(), player.serverLevel());
    }
}
