package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bianhuadao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
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
 * 变化道被动杀招【兽血回潮】：以兽血养身，持续小幅回复生命并强化身法（持续维持）。
 * <p>
 * 说明：移动速度使用专属 transient modifier，避免与其他药水/来源互相覆盖。
 * </p>
 */
public class BianHuaDaoShazhaoBeastBloodRegenEffect implements IShazhaoEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_bian_hua_beast_blood_regen";

    private static final DaoHenHelper.DaoType DAO_TYPE =
        DaoHenHelper.DaoType.BIAN_HUA_DAO;

    private static final String META_HEAL_PER_SECOND = "heal_per_second";
    private static final String META_MOVE_SPEED_BONUS = "move_speed_bonus";

    private static final double DEFAULT_HEAL_PER_SECOND = 0.6;
    private static final double DEFAULT_MOVE_SPEED_BONUS = 0.08;

    private static final ResourceLocation MOVE_SPEED_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_bian_hua_beast_blood_regen_move_speed"
        );

    @Override
    public String getShazhaoId() {
        return SHAZHAO_ID;
    }

    @Override
    public void onSecond(final LivingEntity user, final ShazhaoData data) {
        if (user == null || data == null || user.level().isClientSide()) {
            return;
        }

        if (!ShazhaoCostHelper.tryConsumeSustain(user, data)) {
            onInactive(user);
            return;
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DAO_TYPE
        );

        applyMoveSpeedModifier(user, data, selfMultiplier);
        healUser(user, data, selfMultiplier);
    }

    @Override
    public void onInactive(final LivingEntity user) {
        if (user == null || user.level().isClientSide()) {
            return;
        }
        removeMoveSpeedModifier(user);
    }

    private static void healUser(
        final LivingEntity user,
        final ShazhaoData data,
        final double selfMultiplier
    ) {
        final double baseHeal = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_HEAL_PER_SECOND,
                DEFAULT_HEAL_PER_SECOND
            )
        );
        if (baseHeal <= 0.0) {
            return;
        }
        if (user.getHealth() >= user.getMaxHealth()) {
            return;
        }
        final double healAmount = DaoHenEffectScalingHelper.scaleValue(
            baseHeal,
            selfMultiplier
        );
        if (healAmount <= 0.0) {
            return;
        }
        user.heal((float) healAmount);
    }

    private static void applyMoveSpeedModifier(
        final LivingEntity user,
        final ShazhaoData data,
        final double selfMultiplier
    ) {
        final double baseBonus = ShazhaoMetadataHelper.clamp(
            ShazhaoMetadataHelper.getDouble(
                data,
                META_MOVE_SPEED_BONUS,
                DEFAULT_MOVE_SPEED_BONUS
            ),
            0.0,
            1.0
        );
        final double bonus = DaoHenEffectScalingHelper.scaleValue(
            baseBonus,
            selfMultiplier
        );
        final AttributeInstance attr = user.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr == null) {
            return;
        }
        final AttributeModifier existing = attr.getModifier(MOVE_SPEED_MODIFIER_ID);
        if (existing != null && Double.compare(existing.amount(), bonus) == 0) {
            return;
        }
        if (existing != null) {
            attr.removeModifier(MOVE_SPEED_MODIFIER_ID);
        }
        if (bonus > 0.0) {
            attr.addTransientModifier(
                new AttributeModifier(
                    MOVE_SPEED_MODIFIER_ID,
                    bonus,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                )
            );
        }
    }

    private static void removeMoveSpeedModifier(final LivingEntity user) {
        final AttributeInstance attr = user.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr != null && attr.getModifier(MOVE_SPEED_MODIFIER_ID) != null) {
            attr.removeModifier(MOVE_SPEED_MODIFIER_ID);
        }
    }
}

