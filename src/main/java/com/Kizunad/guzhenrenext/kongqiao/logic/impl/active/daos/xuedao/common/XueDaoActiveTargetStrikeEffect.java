package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.LineOfSightTargetHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.PhysicalDamageSourceHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 血道通用主动：视线锁定单体的“血击/血滴/血锥”。
 * <p>
 * 伤害走普通伤害通道（受护甲影响），并接入血道道痕倍率。
 * 可选附带：自愈与单体 debuff（如流血/迟缓的简化替代）。
 * </p>
 */
public class XueDaoActiveTargetStrikeEffect implements IGuEffect {

    private static final String META_RANGE = "range";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_DAMAGE = "damage";
    private static final String META_SELF_HEAL_AMOUNT = "self_heal_amount";
    private static final String META_DEBUFF_DURATION_TICKS = "debuff_duration_ticks";
    private static final String META_DEBUFF_AMPLIFIER = "debuff_amplifier";

    private static final double DEFAULT_RANGE = 10.0;
    private static final int DEFAULT_COOLDOWN_TICKS = 200;
    private static final double DEFAULT_DAMAGE = 8.0;
    private static final double MAX_SELF_HEAL = 100.0;

    private final String usageId;
    private final String nbtCooldownKey;
    private final Holder<MobEffect> debuff;

    public XueDaoActiveTargetStrikeEffect(
        final String usageId,
        final String nbtCooldownKey,
        final Holder<MobEffect> debuff
    ) {
        this.usageId = usageId;
        this.nbtCooldownKey = nbtCooldownKey;
        this.debuff = debuff;
    }

    @Override
    public String getUsageId() {
        return usageId;
    }

    @Override
    public boolean onActivate(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user == null || usageInfo == null) {
            return false;
        }
        if (user.level().isClientSide()) {
            return false;
        }
        if (!(user instanceof ServerPlayer player)) {
            return false;
        }

        final int remain = GuEffectCooldownHelper.getRemainingTicks(
            user,
            nbtCooldownKey
        );
        if (remain > 0) {
            player.displayClientMessage(
                Component.literal("冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        final double baseRange = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RANGE, DEFAULT_RANGE)
        );
        final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(user, DaoHenHelper.DaoType.XUE_DAO)
        );
        final double range = baseRange * Math.max(0.0, selfMultiplier);
        final LivingEntity target = LineOfSightTargetHelper.findTarget(player, range);
        if (target == null) {
            player.displayClientMessage(Component.literal("未找到可锁定目标。"), true);
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final double multiplier = DaoHenCalculator.calculateMultiplier(
            user,
            target,
            DaoHenHelper.DaoType.XUE_DAO
        );

        applyDebuff(target, usageInfo, selfMultiplier);

        final double baseDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_DAMAGE, DEFAULT_DAMAGE)
        );
        final double finalDamage = baseDamage * Math.max(0.0, multiplier);
        if (finalDamage > 0.0) {
            target.hurt(
                PhysicalDamageSourceHelper.buildPhysicalDamageSource(user),
                (float) finalDamage
            );
        }

        applySelfHeal(user, usageInfo, selfMultiplier);

        final int cooldownTicks = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_COOLDOWN_TICKS,
                DEFAULT_COOLDOWN_TICKS
            )
        );
        if (cooldownTicks > 0) {
            GuEffectCooldownHelper.setCooldownUntilTick(
                user,
                nbtCooldownKey,
                user.tickCount + cooldownTicks
            );
        }

        return true;
    }

    private void applyDebuff(
        final LivingEntity target,
        final NianTouData.Usage usageInfo,
        final double selfMultiplier
    ) {
        if (debuff == null) {
            return;
        }
        final int baseDuration = Math.max(
            0,
            UsageMetadataHelper.getInt(usageInfo, META_DEBUFF_DURATION_TICKS, 0)
        );
        final int duration = DaoHenEffectScalingHelper.scaleDurationTicks(
            baseDuration,
            selfMultiplier
        );
        if (duration <= 0) {
            return;
        }
        final int amplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(usageInfo, META_DEBUFF_AMPLIFIER, 0)
        );
        target.addEffect(new MobEffectInstance(debuff, duration, amplifier, true, true));
    }

    private static void applySelfHeal(
        final LivingEntity user,
        final NianTouData.Usage usageInfo,
        final double selfMultiplier
    ) {
        final double baseHeal = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_SELF_HEAL_AMOUNT, 0.0)
        );
        if (baseHeal <= 0.0) {
            return;
        }
        final double heal = UsageMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(baseHeal, selfMultiplier),
            0.0,
            MAX_SELF_HEAL
        );
        if (heal > 0.0) {
            user.heal((float) heal);
        }
    }
}

