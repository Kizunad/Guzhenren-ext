package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
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
 * 剑道被动杀招：剑意凝神。
 * <p>
 * 每秒维持扣费，提供攻击速度加成，并缓慢恢复念头/精力；数值随【剑】道痕动态变化。
 * </p>
 */
public class JianYiFocusShazhaoEffect implements IShazhaoEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_jian_yi_focus";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.JIAN_DAO;

    private static final String META_ATTACK_SPEED_BONUS = "attack_speed_bonus";
    private static final String META_NIANTOU_RESTORE_PER_SECOND =
        "niantou_restore_per_second";
    private static final String META_JINGLI_RESTORE_PER_SECOND =
        "jingli_restore_per_second";

    private static final double DEFAULT_ATTACK_SPEED_BONUS = 0.15;
    private static final double DEFAULT_RESTORE_PER_SECOND = 0.0;

    private static final double MIN_VALUE = 0.0;
    private static final double MAX_ATTACK_SPEED_BONUS = 2.0;
    private static final double MAX_RESTORE_PER_SECOND = 80.0;

    private static final ResourceLocation ATTACK_SPEED_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_jian_yi_focus_attack_speed"
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

        final double baseAttackSpeed = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_ATTACK_SPEED_BONUS,
                DEFAULT_ATTACK_SPEED_BONUS
            )
        );
        final double attackSpeedBonus = ShazhaoMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(baseAttackSpeed, selfMultiplier),
            MIN_VALUE,
            MAX_ATTACK_SPEED_BONUS
        );
        applyAttributeModifier(
            user,
            Attributes.ATTACK_SPEED,
            ATTACK_SPEED_MODIFIER_ID,
            attackSpeedBonus
        );

        final double baseNianTouRestore = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_NIANTOU_RESTORE_PER_SECOND,
                DEFAULT_RESTORE_PER_SECOND
            )
        );
        final double nianTouRestore = ShazhaoMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(baseNianTouRestore, selfMultiplier),
            MIN_VALUE,
            MAX_RESTORE_PER_SECOND
        );
        if (nianTouRestore > MIN_VALUE) {
            NianTouHelper.modify(user, nianTouRestore);
        }

        final double baseJingLiRestore = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_JINGLI_RESTORE_PER_SECOND,
                DEFAULT_RESTORE_PER_SECOND
            )
        );
        final double jingLiRestore = ShazhaoMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(baseJingLiRestore, selfMultiplier),
            MIN_VALUE,
            MAX_RESTORE_PER_SECOND
        );
        if (jingLiRestore > MIN_VALUE) {
            JingLiHelper.modify(user, jingLiRestore);
        }
    }

    @Override
    public void onInactive(final LivingEntity user) {
        removeAttributeModifier(user, Attributes.ATTACK_SPEED, ATTACK_SPEED_MODIFIER_ID);
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

