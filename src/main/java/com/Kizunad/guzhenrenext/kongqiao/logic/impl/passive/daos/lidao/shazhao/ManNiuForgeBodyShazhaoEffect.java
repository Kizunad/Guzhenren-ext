package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 力道被动杀招：蛮牛锻体。
 * <p>
 * 每秒维持扣费：提供攻击与气血上限加成，并缓慢恢复念头/精力/魂魄；数值随【力】道道痕动态变化。
 * </p>
 */
public class ManNiuForgeBodyShazhaoEffect implements IShazhaoEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_li_dao_man_niu_forge_body";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.LI_DAO;

    private static final String META_ATTACK_DAMAGE_BONUS = "attack_damage_bonus";
    private static final String META_MAX_HEALTH_BONUS = "max_health_bonus";
    private static final String META_NIANTOU_RESTORE_PER_SECOND =
        "niantou_restore_per_second";
    private static final String META_JINGLI_RESTORE_PER_SECOND =
        "jingli_restore_per_second";
    private static final String META_HUNPO_RESTORE_PER_SECOND =
        "hunpo_restore_per_second";

    private static final double DEFAULT_ATTACK_DAMAGE_BONUS = 3.0;
    private static final double DEFAULT_MAX_HEALTH_BONUS = 2.0;
    private static final double DEFAULT_RESTORE_PER_SECOND = 0.0;

    private static final double MIN_VALUE = 0.0;
    private static final double MAX_ATTACK_DAMAGE_BONUS = 80.0;
    private static final double MAX_MAX_HEALTH_BONUS = 80.0;
    private static final double MAX_RESTORE_PER_SECOND = 30.0;

    private static final ResourceLocation ATTACK_DAMAGE_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_li_dao_man_niu_forge_body_attack_damage"
        );
    private static final ResourceLocation MAX_HEALTH_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_li_dao_man_niu_forge_body_max_health"
        );

    @Override
    public String getShazhaoId() {
        return SHAZHAO_ID;
    }

    @Override
    public void onSecond(final LivingEntity user, final ShazhaoData data) {
        if (user == null || data == null) {
            return;
        }
        if (user.level().isClientSide()) {
            return;
        }

        if (!ShazhaoCostHelper.tryConsumeSustain(user, data)) {
            onInactive(user);
            return;
        }

        final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(user, DAO_TYPE)
        );

        applyDamageAndHealth(user, data, selfMultiplier);
        applyResourceRestore(user, data, selfMultiplier);
    }

    @Override
    public void onInactive(final LivingEntity user) {
        removeAttributeModifier(user, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_MODIFIER_ID);
        removeAttributeModifier(user, Attributes.MAX_HEALTH, MAX_HEALTH_MODIFIER_ID);
    }

    private static void applyDamageAndHealth(
        final LivingEntity user,
        final ShazhaoData data,
        final double multiplier
    ) {
        final double baseDamage = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_ATTACK_DAMAGE_BONUS,
                DEFAULT_ATTACK_DAMAGE_BONUS
            )
        );
        final double damageBonus = ShazhaoMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(baseDamage, multiplier),
            MIN_VALUE,
            MAX_ATTACK_DAMAGE_BONUS
        );
        applyAttributeModifier(
            user,
            Attributes.ATTACK_DAMAGE,
            ATTACK_DAMAGE_MODIFIER_ID,
            damageBonus
        );

        final double baseHealth = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_MAX_HEALTH_BONUS,
                DEFAULT_MAX_HEALTH_BONUS
            )
        );
        final double maxHealthBonus = ShazhaoMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(baseHealth, multiplier),
            MIN_VALUE,
            MAX_MAX_HEALTH_BONUS
        );
        applyAttributeModifier(
            user,
            Attributes.MAX_HEALTH,
            MAX_HEALTH_MODIFIER_ID,
            maxHealthBonus
        );
    }

    private static void applyResourceRestore(
        final LivingEntity user,
        final ShazhaoData data,
        final double multiplier
    ) {
        final double baseNianTou = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_NIANTOU_RESTORE_PER_SECOND,
                DEFAULT_RESTORE_PER_SECOND
            )
        );
        final double nianTou = ShazhaoMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(baseNianTou, multiplier),
            MIN_VALUE,
            MAX_RESTORE_PER_SECOND
        );
        if (nianTou > MIN_VALUE) {
            NianTouHelper.modify(user, nianTou);
        }

        final double baseJingLi = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_JINGLI_RESTORE_PER_SECOND,
                DEFAULT_RESTORE_PER_SECOND
            )
        );
        final double jingLi = ShazhaoMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(baseJingLi, multiplier),
            MIN_VALUE,
            MAX_RESTORE_PER_SECOND
        );
        if (jingLi > MIN_VALUE) {
            JingLiHelper.modify(user, jingLi);
        }

        final double baseHunPo = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_HUNPO_RESTORE_PER_SECOND,
                DEFAULT_RESTORE_PER_SECOND
            )
        );
        final double hunPo = ShazhaoMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(baseHunPo, multiplier),
            MIN_VALUE,
            MAX_RESTORE_PER_SECOND
        );
        if (hunPo > MIN_VALUE) {
            HunPoHelper.modify(user, hunPo);
        }
    }

    private static void applyAttributeModifier(
        final LivingEntity user,
        final Holder<Attribute> attrHolder,
        final ResourceLocation modifierId,
        final double amount
    ) {
        if (user == null || attrHolder == null || modifierId == null) {
            return;
        }
        final AttributeInstance attr = user.getAttribute(attrHolder);
        if (attr == null) {
            return;
        }

        final double clamped = Math.max(MIN_VALUE, amount);
        final AttributeModifier existing = attr.getModifier(modifierId);
        if (existing != null && Double.compare(existing.amount(), clamped) == 0) {
            return;
        }
        if (existing != null) {
            attr.removeModifier(modifierId);
        }
        if (clamped > MIN_VALUE) {
            attr.addTransientModifier(
                new AttributeModifier(
                    modifierId,
                    clamped,
                    AttributeModifier.Operation.ADD_VALUE
                )
            );
        }
    }

    private static void removeAttributeModifier(
        final LivingEntity user,
        final Holder<Attribute> attrHolder,
        final ResourceLocation modifierId
    ) {
        if (user == null || attrHolder == null || modifierId == null) {
            return;
        }
        final AttributeInstance attr = user.getAttribute(attrHolder);
        if (attr != null && attr.getModifier(modifierId) != null) {
            attr.removeModifier(modifierId);
        }
    }
}

