package com.Kizunad.guzhenrenext.kongqiao.service;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.domain.network.ClientboundDomainRemovePayload;
import com.Kizunad.guzhenrenext.kongqiao.domain.network.DomainNetworkHandler;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class BianHuaDaoDomainService {

    public static final String KEY_DOMAIN_UNTIL_TICK = "GuzhenrenExtBianHuaDaoDomainUntilTick";
    public static final String KEY_DOMAIN_RADIUS = "GuzhenrenExtBianHuaDaoDomainRadius";
    public static final String KEY_DOMAIN_MAX_HEALTH_BONUS = "GuzhenrenExtBianHuaDaoDomainMaxHealthBonus";
    public static final String KEY_DOMAIN_ARMOR_BONUS = "GuzhenrenExtBianHuaDaoDomainArmorBonus";
    public static final String KEY_DOMAIN_HEAL_RATIO = "GuzhenrenExtBianHuaDaoDomainHealRatio";
    public static final String KEY_DOMAIN_COST_PER_SECOND = "GuzhenrenExtBianHuaDaoDomainCostPerSecond";

    private static final int TICKS_PER_SECOND = 20;

    private static final ResourceLocation HEALTH_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(GuzhenrenExt.MODID, "bian_hua_dao_health");
    private static final ResourceLocation ARMOR_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(GuzhenrenExt.MODID, "bian_hua_dao_armor");

    private BianHuaDaoDomainService() {}

    @SubscribeEvent
    public static void onPlayerTick(final PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            tick(player);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(final LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            final int until = player.getPersistentData().getInt(KEY_DOMAIN_UNTIL_TICK);
            if (until > player.tickCount) {
                final double healRatio = player.getPersistentData().getDouble(KEY_DOMAIN_HEAL_RATIO);
                if (healRatio > 0.0) {
                    final float healAmount = event.getNewDamage() * (float) healRatio;
                    if (healAmount > 0.0F) {
                        player.heal(healAmount);
                    }
                }
            }
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

        final double healthBonus = player
            .getPersistentData()
            .getDouble(KEY_DOMAIN_MAX_HEALTH_BONUS);
        final double armorBonus = player
            .getPersistentData()
            .getDouble(KEY_DOMAIN_ARMOR_BONUS);

        updateAttribute(player, Attributes.MAX_HEALTH, HEALTH_MODIFIER_ID, healthBonus);
        updateAttribute(player, Attributes.ARMOR, ARMOR_MODIFIER_ID, armorBonus);
    }
    
    private static void updateAttribute(
        final ServerPlayer player,
        final Holder<Attribute> attribute,
        final ResourceLocation id,
        final double value
    ) {
        final AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }

        if (value > 0) {
            if (instance.getModifier(id) == null) {
                instance.addTransientModifier(
                    new AttributeModifier(id, value, AttributeModifier.Operation.ADD_VALUE)
                );
            }
            return;
        }

        instance.removeModifier(id);
    }

    public static void clearDomain(final ServerPlayer player) {
        player.getPersistentData().remove(KEY_DOMAIN_UNTIL_TICK);
        player.getPersistentData().remove(KEY_DOMAIN_RADIUS);
        player.getPersistentData().remove(KEY_DOMAIN_MAX_HEALTH_BONUS);
        player.getPersistentData().remove(KEY_DOMAIN_ARMOR_BONUS);
        player.getPersistentData().remove(KEY_DOMAIN_HEAL_RATIO);
        player.getPersistentData().remove(KEY_DOMAIN_COST_PER_SECOND);

        final AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.removeModifier(HEALTH_MODIFIER_ID);
        }

        final AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            armor.removeModifier(ARMOR_MODIFIER_ID);
        }

        ClientboundDomainRemovePayload payload = new ClientboundDomainRemovePayload(
            player.getUUID()
        );
        DomainNetworkHandler.sendDomainRemove(payload, player.position(), player.serverLevel());
    }
}
