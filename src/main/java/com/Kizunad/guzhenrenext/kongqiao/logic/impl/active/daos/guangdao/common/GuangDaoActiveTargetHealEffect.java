package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.guangdao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.LineOfSightTargetHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 光道通用主动：指向性治疗/净化（支持治疗自己或视线目标）。
 * <p>
 * 通过 metadata 配置：<br>
 * - cooldown_ticks<br>
 * - range（视线距离）<br>
 * - heal_amount<br>
 * - zhenyuan_restore / jingli_restore / hunpo_restore<br>
 * - cleanse_harmful（是否净化负面效果）<br>
 * - 可选：额外给予目标的 Buff（由注册时注入 EffectSpec，并从 metadata 读取 duration/amplifier）<br>
 * - 标准一次性消耗（由 {@link GuEffectCostHelper#tryConsumeOnce} 读取）<br>
 * </p>
 */
public class GuangDaoActiveTargetHealEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_RANGE = "range";
    private static final String META_HEAL_AMOUNT = "heal_amount";
    private static final String META_ZHENYUAN_RESTORE = "zhenyuan_restore";
    private static final String META_JINGLI_RESTORE = "jingli_restore";
    private static final String META_HUNPO_RESTORE = "hunpo_restore";
    private static final String META_CLEANSE_HARMFUL = "cleanse_harmful";

    private static final int DEFAULT_COOLDOWN_TICKS = 0;
    private static final double DEFAULT_RANGE = 0.0;
    private static final double DEFAULT_AMOUNT = 0.0;

    private static final int TICKS_PER_SECOND = 20;
    private static final int MAX_DURATION_SECONDS = 10 * 60;
    private static final int MIN_DURATION_TICKS = 1;
    private static final int MAX_DURATION_TICKS =
        TICKS_PER_SECOND * MAX_DURATION_SECONDS;

    private final String usageId;
    private final String cooldownKey;
    private final List<EffectSpec> extraEffects;

    public GuangDaoActiveTargetHealEffect(
        final String usageId,
        final String cooldownKey,
        final List<EffectSpec> extraEffects
    ) {
        this.usageId = usageId;
        this.cooldownKey = cooldownKey;
        this.extraEffects = extraEffects == null ? List.of() : List.copyOf(extraEffects);
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
        if (user.level().isClientSide()) {
            return false;
        }
        if (!(user instanceof ServerPlayer player)) {
            return false;
        }

        final int remain = GuEffectCooldownHelper.getRemainingTicks(user, cooldownKey);
        if (remain > 0) {
            player.displayClientMessage(
                Component.literal("冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        final double baseRange = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RANGE, DEFAULT_RANGE)
        );
        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.GUANG_DAO
        );
        final double range = baseRange * Math.max(0.0, selfMultiplier);

        LivingEntity target = null;
        if (range > 0.0) {
            target = LineOfSightTargetHelper.findTarget(player, range);
        }
        if (target == null) {
            target = user;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final double multiplier = user == target
            ? selfMultiplier
            : DaoHenCalculator.calculateMultiplier(
                user,
                target,
                DaoHenHelper.DaoType.GUANG_DAO
            );

        final double heal = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HEAL_AMOUNT, DEFAULT_AMOUNT)
        ) * Math.max(0.0, multiplier);
        if (Double.compare(heal, 0.0) > 0) {
            target.heal((float) heal);
        }

        final double zhenyuan = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ZHENYUAN_RESTORE,
                DEFAULT_AMOUNT
            )
        ) * Math.max(0.0, multiplier);
        if (Double.compare(zhenyuan, 0.0) > 0) {
            ZhenYuanHelper.modify(target, zhenyuan);
        }

        final double jingli = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_JINGLI_RESTORE,
                DEFAULT_AMOUNT
            )
        ) * Math.max(0.0, multiplier);
        if (Double.compare(jingli, 0.0) > 0) {
            JingLiHelper.modify(target, jingli);
        }

        final double hunpo = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_HUNPO_RESTORE,
                DEFAULT_AMOUNT
            )
        ) * Math.max(0.0, multiplier);
        if (Double.compare(hunpo, 0.0) > 0) {
            HunPoHelper.modify(target, hunpo);
        }

        if (UsageMetadataHelper.getBoolean(usageInfo, META_CLEANSE_HARMFUL, false)) {
            cleanseHarmful(target);
        }

        applyExtraEffects(target, usageInfo, multiplier);

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
                cooldownKey,
                user.tickCount + cooldownTicks
            );
        }

        return true;
    }

    private void applyExtraEffects(
        final LivingEntity target,
        final NianTouData.Usage usageInfo,
        final double multiplier
    ) {
        if (extraEffects.isEmpty()) {
            return;
        }
        for (EffectSpec spec : extraEffects) {
            final int baseDuration = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    spec.durationKey(),
                    spec.defaultDurationTicks()
                )
            );
            if (baseDuration <= 0) {
                continue;
            }
            final int duration = scaleDuration(baseDuration, multiplier);
            final int amplifier = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    spec.amplifierKey(),
                    spec.defaultAmplifier()
                )
            );
            target.addEffect(
                new MobEffectInstance(spec.effect(), duration, amplifier, true, true)
            );
        }
    }

    private static void cleanseHarmful(final LivingEntity target) {
        if (target == null) {
            return;
        }
        final List<Holder<net.minecraft.world.effect.MobEffect>> toRemove =
            new ArrayList<>();
        for (MobEffectInstance inst : target.getActiveEffects()) {
            if (inst.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                toRemove.add(inst.getEffect());
            }
        }
        for (Holder<net.minecraft.world.effect.MobEffect> effect : toRemove) {
            target.removeEffect(effect);
        }
    }

    private static int scaleDuration(final int baseTicks, final double multiplier) {
        final double scaled = baseTicks * Math.max(0.0, multiplier);
        return (int) UsageMetadataHelper.clamp(
            Math.round(scaled),
            MIN_DURATION_TICKS,
            MAX_DURATION_TICKS
        );
    }

    public record EffectSpec(
        Holder<net.minecraft.world.effect.MobEffect> effect,
        String durationKey,
        int defaultDurationTicks,
        String amplifierKey,
        int defaultAmplifier
    ) {}
}
