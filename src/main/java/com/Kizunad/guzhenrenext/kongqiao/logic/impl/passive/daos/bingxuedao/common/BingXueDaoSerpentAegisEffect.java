package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.common;

import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 冰雪道被动：仙蛇护体。
 * <p>
 * 每秒维持冰寒护体（抗性），并在受击时对攻击者施加冻结与虚弱。
 * </p>
 */
public class BingXueDaoSerpentAegisEffect implements IGuEffect {

    private static final String META_ZHENYUAN_BASE_COST_PER_SECOND =
        "zhenyuan_base_cost_per_second";
    private static final String META_NIANTOU_COST_PER_SECOND =
        "niantou_cost_per_second";
    private static final String META_JINGLI_COST_PER_SECOND =
        "jingli_cost_per_second";
    private static final String META_HUNPO_COST_PER_SECOND =
        "hunpo_cost_per_second";
    private static final String META_RESISTANCE_DURATION_TICKS =
        "resistance_duration_ticks";
    private static final String META_RESISTANCE_AMPLIFIER = "resistance_amplifier";
    private static final String META_RETALIATE_FREEZE_TICKS =
        "retaliate_freeze_ticks";
    private static final String META_RETALIATE_WEAKNESS_DURATION_TICKS =
        "retaliate_weakness_duration_ticks";
    private static final String META_RETALIATE_WEAKNESS_AMPLIFIER =
        "retaliate_weakness_amplifier";

    private static final double DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND = 0.0;
    private static final int DEFAULT_RESISTANCE_DURATION_TICKS = 40;
    private static final int DEFAULT_RESISTANCE_AMPLIFIER = 0;
    private static final int DEFAULT_RETALIATE_FREEZE_TICKS = 120;
    private static final int DEFAULT_RETALIATE_WEAKNESS_DURATION_TICKS = 60;
    private static final int DEFAULT_RETALIATE_WEAKNESS_AMPLIFIER = 0;

    private final String usageId;

    public BingXueDaoSerpentAegisEffect(final String usageId) {
        this.usageId = usageId;
    }

    @Override
    public String getUsageId() {
        return usageId;
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
        if (config != null && !config.isPassiveEnabled(usageId)) {
            setActive(user, false);
            return;
        }

        final double zhenyuanBaseCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ZHENYUAN_BASE_COST_PER_SECOND,
                DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND
            )
        );
        final double niantouCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_NIANTOU_COST_PER_SECOND, 0.0)
        );
        final double jingliCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_JINGLI_COST_PER_SECOND, 0.0)
        );
        final double hunpoCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HUNPO_COST_PER_SECOND, 0.0)
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
            return;
        }

        final Holder<MobEffect> resistance = MobEffects.DAMAGE_RESISTANCE;
        final int duration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_RESISTANCE_DURATION_TICKS,
                DEFAULT_RESISTANCE_DURATION_TICKS
            )
        );
        final int amplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_RESISTANCE_AMPLIFIER,
                DEFAULT_RESISTANCE_AMPLIFIER
            )
        );
        if (resistance != null && duration > 0) {
            user.addEffect(
                new MobEffectInstance(resistance, duration, amplifier, true, true)
            );
            setActive(user, true);
            return;
        }

        setActive(user, false);
    }

    @Override
    public float onHurt(
        final LivingEntity victim,
        final DamageSource source,
        final float damage,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (victim.level().isClientSide()) {
            return damage;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(victim);
        if (config != null && !config.isPassiveEnabled(usageId)) {
            return damage;
        }

        if (!(source.getEntity() instanceof LivingEntity attacker)) {
            return damage;
        }
        if (!attacker.isAlive() || attacker.isAlliedTo(victim)) {
            return damage;
        }

        final int freezeTicks = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_RETALIATE_FREEZE_TICKS,
                DEFAULT_RETALIATE_FREEZE_TICKS
            )
        );
        if (freezeTicks > 0) {
            attacker.setTicksFrozen(attacker.getTicksFrozen() + freezeTicks);
        }

        final Holder<MobEffect> weakness = MobEffects.WEAKNESS;
        final int duration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_RETALIATE_WEAKNESS_DURATION_TICKS,
                DEFAULT_RETALIATE_WEAKNESS_DURATION_TICKS
            )
        );
        final int amplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_RETALIATE_WEAKNESS_AMPLIFIER,
                DEFAULT_RETALIATE_WEAKNESS_AMPLIFIER
            )
        );
        if (weakness != null && duration > 0) {
            attacker.addEffect(
                new MobEffectInstance(weakness, duration, amplifier, true, true)
            );
        }

        return damage;
    }

    private void setActive(final LivingEntity user, final boolean active) {
        final ActivePassives actives = KongqiaoAttachments.getActivePassives(user);
        if (actives == null) {
            return;
        }
        if (active) {
            actives.add(usageId);
            return;
        }
        actives.remove(usageId);
    }
}
