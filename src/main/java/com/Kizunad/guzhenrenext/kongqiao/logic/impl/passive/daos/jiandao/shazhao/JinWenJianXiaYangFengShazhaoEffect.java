package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 剑道被动杀招：金纹匣养锋。
 * <p>
 * 每秒维持扣费，提升攻击伤害并缓慢滋养魂魄；数值随【剑】道痕动态变化。
 * </p>
 */
public class JinWenJianXiaYangFengShazhaoEffect implements IShazhaoEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_jin_wen_jian_xia_yang_feng";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.JIAN_DAO;

    private static final String META_ATTACK_DAMAGE_BONUS = "attack_damage_bonus";
    private static final String META_HUNPO_RESTORE_PER_SECOND =
        "hunpo_restore_per_second";

    private static final double MIN_VALUE = 0.0;
    private static final double DEFAULT_ATTACK_DAMAGE_BONUS = 0.10;
    private static final double MAX_ATTACK_DAMAGE_BONUS = 1.0;

    private static final double DEFAULT_HUNPO_RESTORE_PER_SECOND = 0.10;
    private static final double MAX_HUNPO_RESTORE_PER_SECOND = 80.0;

    private static final ResourceLocation ATTACK_DAMAGE_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_jin_wen_jian_xia_yang_feng_attack_damage"
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

        applyAttackDamageModifier(user, data, selfMultiplier);
        restoreHunpo(user, data, selfMultiplier);
    }

    @Override
    public void onInactive(final LivingEntity user) {
        removeAttackDamageModifier(user);
    }

    private static void applyAttackDamageModifier(
        final LivingEntity user,
        final ShazhaoData data,
        final double selfMultiplier
    ) {
        final double base = ShazhaoMetadataHelper.clamp(
            Math.max(
                MIN_VALUE,
                ShazhaoMetadataHelper.getDouble(
                    data,
                    META_ATTACK_DAMAGE_BONUS,
                    DEFAULT_ATTACK_DAMAGE_BONUS
                )
            ),
            MIN_VALUE,
            MAX_ATTACK_DAMAGE_BONUS
        );
        final double scaled = ShazhaoMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(base, selfMultiplier),
            MIN_VALUE,
            MAX_ATTACK_DAMAGE_BONUS
        );

        final AttributeInstance attr = user.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attr == null) {
            return;
        }
        final AttributeModifier existing = attr.getModifier(ATTACK_DAMAGE_MODIFIER_ID);
        if (existing != null && Double.compare(existing.amount(), scaled) == 0) {
            return;
        }
        if (existing != null) {
            attr.removeModifier(ATTACK_DAMAGE_MODIFIER_ID);
        }
        if (scaled > MIN_VALUE) {
            attr.addTransientModifier(
                new AttributeModifier(
                    ATTACK_DAMAGE_MODIFIER_ID,
                    scaled,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                )
            );
        }
    }

    private static void removeAttackDamageModifier(final LivingEntity user) {
        if (user == null) {
            return;
        }
        final AttributeInstance attr = user.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attr != null && attr.getModifier(ATTACK_DAMAGE_MODIFIER_ID) != null) {
            attr.removeModifier(ATTACK_DAMAGE_MODIFIER_ID);
        }
    }

    private static void restoreHunpo(
        final LivingEntity user,
        final ShazhaoData data,
        final double selfMultiplier
    ) {
        final double base = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_HUNPO_RESTORE_PER_SECOND,
                DEFAULT_HUNPO_RESTORE_PER_SECOND
            )
        );
        if (base <= MIN_VALUE) {
            return;
        }
        final double restore = ShazhaoMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(base, selfMultiplier),
            MIN_VALUE,
            MAX_HUNPO_RESTORE_PER_SECOND
        );
        if (restore > MIN_VALUE) {
            HunPoHelper.modify(user, restore);
        }
    }
}

