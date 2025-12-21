package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 宇道通用被动：攻击触发（概率）对目标施加 debuff，并可附带额外法术伤害。
 */
public class YuDaoAttackProcDebuffEffect implements IGuEffect {

    private static final String META_PROC_CHANCE = "proc_chance";
    private static final String META_EFFECT_DURATION_TICKS = "effect_duration_ticks";
    private static final String META_EFFECT_AMPLIFIER = "effect_amplifier";
    private static final String META_EXTRA_MAGIC_DAMAGE = "extra_magic_damage";

    private static final double DEFAULT_PROC_CHANCE = 0.15;
    private static final int DEFAULT_EFFECT_DURATION_TICKS = 60;
    private static final int DEFAULT_EFFECT_AMPLIFIER = 0;
    private static final double DEFAULT_EXTRA_MAGIC_DAMAGE = 0.0;

    private final String usageId;
    private final DaoHenHelper.DaoType daoType;
    private final Holder<MobEffect> debuff;

    public YuDaoAttackProcDebuffEffect(
        final String usageId,
        final DaoHenHelper.DaoType daoType,
        final Holder<MobEffect> debuff
    ) {
        this.usageId = usageId;
        this.daoType = daoType;
        this.debuff = debuff;
    }

    @Override
    public String getUsageId() {
        return usageId;
    }

    @Override
    public float onAttack(
        final LivingEntity attacker,
        final LivingEntity target,
        final float damage,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (attacker.level().isClientSide()) {
            return damage;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(attacker);
        if (config != null && !config.isPassiveEnabled(usageId)) {
            return damage;
        }

        final double chance = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_PROC_CHANCE,
                DEFAULT_PROC_CHANCE
            ),
            0.0,
            1.0
        );
        if (attacker.getRandom().nextDouble() > chance) {
            return damage;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(null, attacker, usageInfo)) {
            return damage;
        }

        final double multiplier = daoType == null
            ? 1.0
            : DaoHenCalculator.calculateMultiplier(attacker, target, daoType);

        final int duration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_EFFECT_DURATION_TICKS,
                DEFAULT_EFFECT_DURATION_TICKS
            )
        );
        final int amplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_EFFECT_AMPLIFIER,
                DEFAULT_EFFECT_AMPLIFIER
            )
        );
        final int scaledDuration = scaleDuration(duration, multiplier);
        if (debuff != null && scaledDuration > 0) {
            target.addEffect(
                new MobEffectInstance(
                    debuff,
                    scaledDuration,
                    amplifier,
                    true,
                    true
                )
            );
        }

        final double extraMagicDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_EXTRA_MAGIC_DAMAGE,
                DEFAULT_EXTRA_MAGIC_DAMAGE
            )
        );
        final double extraDamage = extraMagicDamage * Math.max(0.0, multiplier);
        if (extraDamage > 0.0) {
            if (attacker instanceof ServerPlayer player) {
                target.hurt(attacker.damageSources().playerAttack(player), (float) extraDamage);
            } else {
                target.hurt(attacker.damageSources().mobAttack(attacker), (float) extraDamage);
            }
        }

        return damage;
    }

    private static int scaleDuration(final int baseDuration, final double multiplier) {
        if (baseDuration <= 0) {
            return 0;
        }
        final double scaled = baseDuration * Math.max(0.0, multiplier);
        if (scaled <= 0.0) {
            return 0;
        }
        return Math.max(1, (int) Math.round(scaled));
    }
}
