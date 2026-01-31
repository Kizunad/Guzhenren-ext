package com.Kizunad.guzhenrenext.kongqiao.service;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.domain.network.ClientboundDomainRemovePayload;
import com.Kizunad.guzhenrenext.kongqiao.domain.network.DomainNetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class LvDaoDomainService {

    public static final String KEY_DOMAIN_UNTIL_TICK = "GuzhenrenExtLvDaoDomainUntilTick";
    public static final String KEY_DOMAIN_RADIUS = "GuzhenrenExtLvDaoDomainRadius";
    public static final String KEY_DOMAIN_REFLECT_RATIO = "GuzhenrenExtLvDaoDomainReflectRatio";
    public static final String KEY_DOMAIN_COST_PER_SECOND = "GuzhenrenExtLvDaoDomainCostPerSecond";

    private static final int TICKS_PER_SECOND = 20;

    private LvDaoDomainService() {}

    @SubscribeEvent
    public static void onPlayerTick(final PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            tick(player);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(final LivingDamageEvent.Pre event) {
        final Entity sourceEntity = event.getSource().getEntity();
        if (sourceEntity instanceof ServerPlayer player) {
            // 如果攻击者是开着律道领域的玩家，禁止其造成物理伤害
            if (hasActiveDomain(player) && isPhysicalDamage(event.getSource())) {
                event.setNewDamage(0.0F);
            }
        }

        if (event.getEntity() instanceof ServerPlayer player) {
            // 如果受击者是开着律道领域的玩家
            if (hasActiveDomain(player) && isPhysicalDamage(event.getSource())) {
                event.setNewDamage(0.0F);
                
                final double reflectRatio = player.getPersistentData().getDouble(KEY_DOMAIN_REFLECT_RATIO);
                if (reflectRatio > 0.0 && event.getSource().getEntity() instanceof LivingEntity attacker) {
                    attacker.invulnerableTime = 0;
                    attacker.hurt(player.damageSources().magic(), event.getOriginalDamage() * (float) reflectRatio);
                }
            }
        }
    }

    private static boolean hasActiveDomain(final ServerPlayer player) {
        final int until = player.getPersistentData().getInt(KEY_DOMAIN_UNTIL_TICK);
        return until > player.tickCount;
    }

    private static boolean isPhysicalDamage(final DamageSource source) {
        return !source.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE) &&
               !source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION) &&
               !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR);
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
        if (radius > 0.0) {
             // 律道领域可能有额外的 tick 逻辑，比如强行推开非领域内实体？
             // 暂时保持空，主要逻辑在事件监听中
        }
    }

    public static void clearDomain(final ServerPlayer player) {
        player.getPersistentData().remove(KEY_DOMAIN_UNTIL_TICK);
        player.getPersistentData().remove(KEY_DOMAIN_RADIUS);
        player.getPersistentData().remove(KEY_DOMAIN_REFLECT_RATIO);
        player.getPersistentData().remove(KEY_DOMAIN_COST_PER_SECOND);

        ClientboundDomainRemovePayload payload = new ClientboundDomainRemovePayload(
            player.getUUID()
        );
        DomainNetworkHandler.sendDomainRemove(payload, player.position(), player.serverLevel());
    }
}
