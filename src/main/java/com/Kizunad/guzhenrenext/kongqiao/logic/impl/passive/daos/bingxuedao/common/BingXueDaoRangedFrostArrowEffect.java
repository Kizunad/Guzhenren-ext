package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.common;

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
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 冰雪道被动：霜箭加持（仅对远程武器攻击生效）。
 */
public class BingXueDaoRangedFrostArrowEffect implements IGuEffect {

    private static final String META_PROC_CHANCE = "proc_chance";
    private static final String META_DAMAGE_BONUS = "damage_bonus";
    private static final String META_FREEZE_TICKS = "freeze_ticks";
    private static final String META_SLOW_DURATION_TICKS = "slow_duration_ticks";
    private static final String META_SLOW_AMPLIFIER = "slow_amplifier";

    private static final double DEFAULT_PROC_CHANCE = 1.0;
    private static final double DEFAULT_DAMAGE_BONUS = 0.0;
    private static final int DEFAULT_FREEZE_TICKS = 0;
    private static final int DEFAULT_SLOW_DURATION_TICKS = 40;
    private static final int DEFAULT_SLOW_AMPLIFIER = 0;

    private final String usageId;

    public BingXueDaoRangedFrostArrowEffect(final String usageId) {
        this.usageId = usageId;
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

        if (!isRangedWeapon(attacker.getMainHandItem())) {
            return damage;
        }

        final double procChance = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_PROC_CHANCE,
                DEFAULT_PROC_CHANCE
            ),
            0.0,
            1.0
        );
        if (attacker.getRandom().nextDouble() > procChance) {
            return damage;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(null, attacker, usageInfo)) {
            return damage;
        }

        float currentDamage = damage;
        final double bonus = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_DAMAGE_BONUS,
                DEFAULT_DAMAGE_BONUS
            ),
            0.0,
            3.0
        );
        if (bonus > 0.0) {
            final double multiplier = DaoHenCalculator.calculateMultiplier(
                attacker,
                target,
                DaoHenHelper.DaoType.BING_XUE_DAO
            );
            currentDamage = (float) (currentDamage * (1.0 + bonus * multiplier));
        }

        final int freezeTicks = Math.max(
            0,
            UsageMetadataHelper.getInt(usageInfo, META_FREEZE_TICKS, DEFAULT_FREEZE_TICKS)
        );
        if (freezeTicks > 0) {
            target.setTicksFrozen(target.getTicksFrozen() + freezeTicks);
        }

        final Holder<MobEffect> slow = MobEffects.MOVEMENT_SLOWDOWN;
        final int duration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_SLOW_DURATION_TICKS,
                DEFAULT_SLOW_DURATION_TICKS
            )
        );
        final int amplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_SLOW_AMPLIFIER,
                DEFAULT_SLOW_AMPLIFIER
            )
        );
        if (slow != null && duration > 0) {
            target.addEffect(
                new MobEffectInstance(slow, duration, amplifier, true, true)
            );
        }

        return currentDamage;
    }

    private static boolean isRangedWeapon(final ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        final Item item = stack.getItem();
        return item instanceof BowItem || item instanceof CrossbowItem;
    }
}
