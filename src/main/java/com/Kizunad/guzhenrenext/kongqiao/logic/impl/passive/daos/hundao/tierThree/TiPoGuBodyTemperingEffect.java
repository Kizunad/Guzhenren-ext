package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierThree;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

/**
 * 体魄蛊：被动【炼体强魄】。
 * <p>
 * 体魄蛊本质是“以体养魂、以魂炼体”的底子蛊：在空窍内持续温养肉身，让蛊师更能打、更能扛。
 * <ul>
 *   <li>维持：每秒消耗少量魂魄（受魂道道痕自增幅）。魂魄不足时被动暂停。</li>
 *   <li>增益：提升最大生命、基础攻击力与击退抗性（受力道道痕自增幅）。</li>
 * </ul>
 * </p>
 */
public class TiPoGuBodyTemperingEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:tipogu_passive_body_tempering";

    private static final String NBT_ACTIVE = "TiPoGuBodyTemperingActive";

    private static final ResourceLocation MAX_HEALTH_MODIFIER_ID =
        ResourceLocation.parse("guzhenrenext:tipogu_max_health");
    private static final ResourceLocation ATTACK_DAMAGE_MODIFIER_ID =
        ResourceLocation.parse("guzhenrenext:tipogu_attack_damage");
    private static final ResourceLocation KNOCKBACK_RESISTANCE_MODIFIER_ID =
        ResourceLocation.parse("guzhenrenext:tipogu_knockback_resistance");

    private static final double DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND = 180.0;
    private static final double DEFAULT_MAX_HEALTH_BONUS_PERCENT = 0.12;
    private static final double DEFAULT_ATTACK_DAMAGE_BONUS_PERCENT = 0.10;
    private static final double DEFAULT_KNOCKBACK_RESISTANCE_BONUS = 0.10;

    private static final double MAX_PERCENT_BONUS = 1.0;

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public void onTick(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user.level().isClientSide()) {
            return;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(user);
        if (config != null && !config.isPassiveEnabled(USAGE_ID)) {
            deactivate(user);
            return;
        }

        if (!isActive(user)) {
            removeAllModifiers(user);
            return;
        }

        final double liDaoMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.LI_DAO
        );

        final double maxHealthBonus = clamp(
            getMetaDouble(
                usageInfo,
                "max_health_bonus_percent",
                DEFAULT_MAX_HEALTH_BONUS_PERCENT
            ) * liDaoMultiplier,
            0.0,
            MAX_PERCENT_BONUS
        );
        final double attackDamageBonus = clamp(
            getMetaDouble(
                usageInfo,
                "attack_damage_bonus_percent",
                DEFAULT_ATTACK_DAMAGE_BONUS_PERCENT
            ) * liDaoMultiplier,
            0.0,
            MAX_PERCENT_BONUS
        );
        final double knockbackResistanceBonus = clamp(
            getMetaDouble(
                usageInfo,
                "knockback_resistance_bonus",
                DEFAULT_KNOCKBACK_RESISTANCE_BONUS
            ) * liDaoMultiplier,
            0.0,
            1.0
        );

        applyPercentModifier(
            user,
            Attributes.MAX_HEALTH,
            MAX_HEALTH_MODIFIER_ID,
            maxHealthBonus
        );
        applyPercentModifier(
            user,
            Attributes.ATTACK_DAMAGE,
            ATTACK_DAMAGE_MODIFIER_ID,
            attackDamageBonus
        );
        applyAddModifier(
            user,
            Attributes.KNOCKBACK_RESISTANCE,
            KNOCKBACK_RESISTANCE_MODIFIER_ID,
            knockbackResistanceBonus
        );
    }

    @Override
    public void onSecond(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user.level().isClientSide()) {
            return;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(user);
        if (config != null && !config.isPassiveEnabled(USAGE_ID)) {
            deactivate(user);
            return;
        }

        final double hunDaoMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.HUN_DAO
        );

        final double niantouCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_NIANTOU_COST_PER_SECOND,
                0.0
            )
        );
        final double jingliCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_JINGLI_COST_PER_SECOND,
                0.0
            )
        );
        final double hunpoCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_HUNPO_COST_PER_SECOND,
                0.0
            ) * hunDaoMultiplier
        );
        final double zhenyuanBaseCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_ZHENYUAN_BASE_COST_PER_SECOND,
                DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND
            )
        );

        if (
            !GuEffectCostHelper.tryConsumeSustain(
                user,
                niantouCostPerSecond,
                jingliCostPerSecond,
                hunpoCostPerSecond,
                zhenyuanBaseCostPerSecond
            )
        ) {
            setActive(user, false);
            removeAllModifiers(user);
            return;
        }

        setActive(user, true);
    }

    @Override
    public void onUnequip(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        deactivate(user);
    }

    private static boolean isActive(final LivingEntity user) {
        return user.getPersistentData().getBoolean(NBT_ACTIVE);
    }

    private static void setActive(final LivingEntity user, final boolean active) {
        user.getPersistentData().putBoolean(NBT_ACTIVE, active);
    }

    private static void deactivate(final LivingEntity user) {
        setActive(user, false);
        removeAllModifiers(user);
    }

    private static void removeAllModifiers(final LivingEntity user) {
        removeModifier(user, Attributes.MAX_HEALTH, MAX_HEALTH_MODIFIER_ID);
        removeModifier(user, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_MODIFIER_ID);
        removeModifier(
            user,
            Attributes.KNOCKBACK_RESISTANCE,
            KNOCKBACK_RESISTANCE_MODIFIER_ID
        );
    }

    private static void applyPercentModifier(
        final LivingEntity user,
        final Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
        final ResourceLocation id,
        final double amount
    ) {
        applyModifier(
            user,
            attribute,
            id,
            amount,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    private static void applyAddModifier(
        final LivingEntity user,
        final Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
        final ResourceLocation id,
        final double amount
    ) {
        applyModifier(
            user,
            attribute,
            id,
            amount,
            AttributeModifier.Operation.ADD_VALUE
        );
    }

    private static void applyModifier(
        final LivingEntity user,
        final Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
        final ResourceLocation id,
        final double amount,
        final AttributeModifier.Operation operation
    ) {
        final AttributeInstance attr = user.getAttribute(attribute);
        if (attr == null) {
            return;
        }
        final AttributeModifier existing = attr.getModifier(id);
        if (existing == null || Double.compare(existing.amount(), amount) != 0) {
            if (existing != null) {
                attr.removeModifier(id);
            }
            if (amount > 0.0) {
                attr.addTransientModifier(
                    new AttributeModifier(id, amount, operation)
                );
            }
        }
    }

    private static void removeModifier(
        final LivingEntity user,
        final Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
        final ResourceLocation id
    ) {
        final AttributeInstance attr = user.getAttribute(attribute);
        if (attr != null && attr.getModifier(id) != null) {
            attr.removeModifier(id);
        }
    }

    private static double clamp(final double value, final double min, final double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private static double getMetaDouble(
        final NianTouData.Usage usage,
        final String key,
        final double defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Double.parseDouble(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }
}
