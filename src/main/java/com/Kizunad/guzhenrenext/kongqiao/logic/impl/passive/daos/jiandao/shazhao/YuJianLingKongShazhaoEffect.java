package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 剑道被动杀招：御剑凌空。
 * <p>
 * 每秒维持扣费，提升身法（移速）并赋予缓降，模拟“御剑腾挪”的持续状态；
 * 数值随【剑】道痕动态变化。
 * </p>
 */
public class YuJianLingKongShazhaoEffect implements IShazhaoEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_yu_jian_ling_kong";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.JIAN_DAO;

    private static final String META_MOVE_SPEED_BONUS = "move_speed_bonus";
    private static final String META_SLOW_FALLING_DURATION_TICKS =
        "slow_falling_duration_ticks";

    private static final double MIN_VALUE = 0.0;
    private static final double DEFAULT_MOVE_SPEED_BONUS = 0.18;
    private static final double MAX_MOVE_SPEED_BONUS = 1.2;

    private static final int DEFAULT_SLOW_FALLING_DURATION_TICKS = 80;

    private static final ResourceLocation MOVE_SPEED_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_yu_jian_ling_kong_move_speed"
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

        applyMoveSpeedModifier(user, data, selfMultiplier);
        applySlowFalling(user, data, selfMultiplier);
    }

    @Override
    public void onInactive(final LivingEntity user) {
        removeMoveSpeedModifier(user);
    }

    private static void applyMoveSpeedModifier(
        final LivingEntity user,
        final ShazhaoData data,
        final double selfMultiplier
    ) {
        final double base = ShazhaoMetadataHelper.clamp(
            Math.max(
                MIN_VALUE,
                ShazhaoMetadataHelper.getDouble(
                    data,
                    META_MOVE_SPEED_BONUS,
                    DEFAULT_MOVE_SPEED_BONUS
                )
            ),
            MIN_VALUE,
            MAX_MOVE_SPEED_BONUS
        );
        final double scaled = ShazhaoMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(base, selfMultiplier),
            MIN_VALUE,
            MAX_MOVE_SPEED_BONUS
        );

        final AttributeInstance attr = user.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr == null) {
            return;
        }
        final AttributeModifier existing = attr.getModifier(MOVE_SPEED_MODIFIER_ID);
        if (existing != null && Double.compare(existing.amount(), scaled) == 0) {
            return;
        }
        if (existing != null) {
            attr.removeModifier(MOVE_SPEED_MODIFIER_ID);
        }
        if (scaled > MIN_VALUE) {
            attr.addTransientModifier(
                new AttributeModifier(
                    MOVE_SPEED_MODIFIER_ID,
                    scaled,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                )
            );
        }
    }

    private static void removeMoveSpeedModifier(final LivingEntity user) {
        if (user == null) {
            return;
        }
        final AttributeInstance attr = user.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr != null && attr.getModifier(MOVE_SPEED_MODIFIER_ID) != null) {
            attr.removeModifier(MOVE_SPEED_MODIFIER_ID);
        }
    }

    private static void applySlowFalling(
        final LivingEntity user,
        final ShazhaoData data,
        final double selfMultiplier
    ) {
        final int baseTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_SLOW_FALLING_DURATION_TICKS,
                DEFAULT_SLOW_FALLING_DURATION_TICKS
            )
        );
        if (baseTicks <= 0) {
            return;
        }
        final int ticks = DaoHenEffectScalingHelper.scaleDurationTicks(
            baseTicks,
            selfMultiplier
        );
        if (ticks > 0) {
            user.addEffect(
                new MobEffectInstance(MobEffects.SLOW_FALLING, ticks, 0, true, true)
            );
        }
    }
}

