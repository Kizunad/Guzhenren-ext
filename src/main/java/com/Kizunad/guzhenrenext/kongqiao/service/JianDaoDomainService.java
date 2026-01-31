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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class JianDaoDomainService {

    public static final String KEY_DOMAIN_UNTIL_TICK = "GuzhenrenExtJianDaoDomainUntilTick";
    public static final String KEY_DOMAIN_RADIUS = "GuzhenrenExtJianDaoDomainRadius";
    public static final String KEY_DOMAIN_ENEMY_DAMAGE = "GuzhenrenExtJianDaoDomainEnemyDamage";
    public static final String KEY_DOMAIN_DISARM_CHANCE = "GuzhenrenExtJianDaoDomainDisarmChance";
    public static final String KEY_DOMAIN_EXECUTE_THRESHOLD =
        "GuzhenrenExtJianDaoDomainExecuteThreshold";
    public static final String KEY_DOMAIN_DAMAGE_REDUCTION_MELEE =
        "GuzhenrenExtJianDaoDomainDamageReductionMelee";
    public static final String KEY_DOMAIN_COST_PER_SECOND = "GuzhenrenExtJianDaoDomainCostPerSecond";

    private static final int TICKS_PER_SECOND = 20;

    private static final int WEAKNESS_DURATION_TICKS = 60;
    private static final int WEAKNESS_AMPLIFIER = 2;

    private JianDaoDomainService() {}

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

        final double reduction = player
            .getPersistentData()
            .getDouble(KEY_DOMAIN_DAMAGE_REDUCTION_MELEE);
        if (reduction <= 0.0) {
            return;
        }

        if (isMeleeDamage(event.getSource())) {
            final float original = event.getOriginalDamage();
            final float reduced = Math.max(0.0F, original * (1.0F - (float) reduction));
            event.setNewDamage(reduced);
        }
    }

    private static boolean isMeleeDamage(final net.minecraft.world.damagesource.DamageSource source) {
        return (
            !source.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE) &&
            !source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION) &&
            !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)
        );
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
        final double chance = player.getPersistentData().getDouble(KEY_DOMAIN_DISARM_CHANCE);
        final double executeThreshold = player
            .getPersistentData()
            .getDouble(KEY_DOMAIN_EXECUTE_THRESHOLD);

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

            if (executeThreshold > 0.0) {
                if (target.getHealth() < target.getMaxHealth() * executeThreshold) {
                    target.hurt(player.damageSources().mobAttack(player), Float.MAX_VALUE);
                    continue;
                }
            }

            if (damage > 0.0) {
                target.hurt(player.damageSources().mobAttack(player), (float) damage);
            }

            if (target.getMainHandItem().isEmpty()) {
                continue;
            }

            if (target instanceof Player) {
                target.addEffect(
                    new MobEffectInstance(
                        MobEffects.WEAKNESS,
                        WEAKNESS_DURATION_TICKS,
                        WEAKNESS_AMPLIFIER
                    )
                );
                continue;
            }

            if (target instanceof Mob mob) {
                if (player.getRandom().nextDouble() < chance) {
                    mob.spawnAtLocation(mob.getMainHandItem());
                    mob.setItemInHand(
                        net.minecraft.world.InteractionHand.MAIN_HAND,
                        net.minecraft.world.item.ItemStack.EMPTY
                    );
                }
            }
        }
    }

    public static void clearDomain(final ServerPlayer player) {
        player.getPersistentData().remove(KEY_DOMAIN_UNTIL_TICK);
        player.getPersistentData().remove(KEY_DOMAIN_RADIUS);
        player.getPersistentData().remove(KEY_DOMAIN_ENEMY_DAMAGE);
        player.getPersistentData().remove(KEY_DOMAIN_DISARM_CHANCE);
        player.getPersistentData().remove(KEY_DOMAIN_EXECUTE_THRESHOLD);
        player.getPersistentData().remove(KEY_DOMAIN_DAMAGE_REDUCTION_MELEE);
        player.getPersistentData().remove(KEY_DOMAIN_COST_PER_SECOND);

        ClientboundDomainRemovePayload payload = new ClientboundDomainRemovePayload(
            player.getUUID()
        );
        DomainNetworkHandler.sendDomainRemove(payload, player.position(), player.serverLevel());
    }
}
