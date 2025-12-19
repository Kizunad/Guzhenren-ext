package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoEffect;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 护甲型杀招通用实现：按固定值提供护甲加成，并支持每秒魂魄消耗。
 * <p>
 * 通过独立的 AttributeModifier ID 控制叠加，避免重复叠加或无法移除的问题。
 * </p>
 */
public abstract class AbstractArmorShazhaoEffect implements IShazhaoEffect {

    private final ResourceLocation armorModifierId;
    private final double defaultArmorBonus;
    private final double defaultSoulCostPerSecond;

    protected AbstractArmorShazhaoEffect(
        ResourceLocation armorModifierId,
        double armorBonus,
        double soulCostPerSecond
    ) {
        this.armorModifierId = armorModifierId;
        this.defaultArmorBonus = armorBonus;
        this.defaultSoulCostPerSecond = soulCostPerSecond;
    }

    @Override
    public void onSecond(LivingEntity user, ShazhaoData data) {
        double soulCost = Math.max(0.0, getSoulCost(data));
        if (!consumeSoul(user, soulCost)) {
            removeArmor(user);
            return;
        }
        double armorBonus = Math.max(0.0, getArmorBonus(data));
        applyArmor(user, armorBonus);
    }

    @Override
    public void onInactive(LivingEntity user) {
        removeArmor(user);
    }

    private void applyArmor(LivingEntity user, double armorBonus) {
        AttributeInstance attr = user.getAttribute(Attributes.ARMOR);
        if (attr == null) {
            return;
        }

        AttributeModifier modifier = attr.getModifier(armorModifierId);
        if (modifier == null || modifier.amount() != armorBonus) {
            if (modifier != null) {
                attr.removeModifier(armorModifierId);
            }
            if (armorBonus > 0.0) {
                attr.addTransientModifier(
                    new AttributeModifier(
                        armorModifierId,
                        armorBonus,
                        AttributeModifier.Operation.ADD_VALUE
                    )
                );
            }
        }
    }

    private boolean consumeSoul(LivingEntity user, double soulCostPerSecond) {
        if (soulCostPerSecond <= 0.0) {
            return true;
        }
        double current = HunPoHelper.getAmount(user);
        if (current < soulCostPerSecond) {
            return false;
        }
        HunPoHelper.modify(user, -soulCostPerSecond);
        return true;
    }

    private double getArmorBonus(ShazhaoData data) {
        return getMetaDouble(data, "armor_bonus", defaultArmorBonus);
    }

    private double getSoulCost(ShazhaoData data) {
        return getMetaDouble(
            data,
            "soul_cost_per_second",
            defaultSoulCostPerSecond
        );
    }

    private static double getMetaDouble(
        ShazhaoData data,
        String key,
        double defaultValue
    ) {
        if (data == null || data.metadata() == null) {
            return defaultValue;
        }
        String value = data.metadata().get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private void removeArmor(LivingEntity user) {
        AttributeInstance attr = user.getAttribute(Attributes.ARMOR);
        if (attr != null && attr.getModifier(armorModifierId) != null) {
            attr.removeModifier(armorModifierId);
        }
    }
}
