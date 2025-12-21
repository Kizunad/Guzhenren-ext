package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 智道通用被动：攻击触发（概率）对目标施加 debuff，并可附带额外法术伤害。
 */
public class ZhiDaoAttackProcDebuffEffect implements IGuEffect {

    private static final String META_PROC_CHANCE = "proc_chance";
    private static final String META_EFFECT_DURATION_TICKS = "effect_duration_ticks";
    private static final String META_EFFECT_AMPLIFIER = "effect_amplifier";
    private static final String META_ENABLE_MAGIC_DAMAGE = "enable_magic_damage";
    private static final String META_EXTRA_MAGIC_DAMAGE = "extra_magic_damage";

    private static final double DEFAULT_PROC_CHANCE = 0.15;
    private static final int DEFAULT_EFFECT_DURATION_TICKS = 60;
    private static final int DEFAULT_EFFECT_AMPLIFIER = 0;
    private static final double DEFAULT_EXTRA_MAGIC_DAMAGE = 0.0;
    private static final double MAX_EXTRA_MAGIC_DAMAGE = 12.0;

    private final String usageId;
    private final Holder<MobEffect> debuff;

    public ZhiDaoAttackProcDebuffEffect(
        final String usageId,
        final Holder<MobEffect> debuff
    ) {
        this.usageId = usageId;
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

        final int durationBase = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_EFFECT_DURATION_TICKS,
                DEFAULT_EFFECT_DURATION_TICKS
            )
        );
        final int amplifierBase = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_EFFECT_AMPLIFIER,
                DEFAULT_EFFECT_AMPLIFIER
            )
        );

        final double multiplier = DaoHenCalculator.calculateMultiplier(
            attacker,
            target,
            DaoHenHelper.DaoType.ZHI_DAO
        );
        final int duration = Math.max(0, (int) Math.round(durationBase * multiplier));
        final int amplifier = Math.max(0, amplifierBase);

        if (debuff != null && duration > 0) {
            target.addEffect(
                new MobEffectInstance(debuff, duration, amplifier, true, true)
            );
        }

        final boolean enableMagicDamage = UsageMetadataHelper.getBoolean(
            usageInfo,
            META_ENABLE_MAGIC_DAMAGE,
            false
        );
        final double extraMagicDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_EXTRA_MAGIC_DAMAGE,
                DEFAULT_EXTRA_MAGIC_DAMAGE
            )
        );
        if (enableMagicDamage && extraMagicDamage > 0.0) {
            final double finalDamage = Math.min(MAX_EXTRA_MAGIC_DAMAGE, extraMagicDamage);
            target.hurt(
                attacker.damageSources().magic(),
                (float) (finalDamage * multiplier)
            );
        }

        return damage;
    }
}
