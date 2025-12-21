package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.LineOfSightTargetHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 宇道通用主动：破空打击/异界镇压。
 * <p>
 * 视线锁定单体目标，造成法术伤害并施加若干 debuff。
 * </p>
 */
public class YuDaoActiveTargetNukeEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_RANGE = "range";
    private static final String META_EXTRA_MAGIC_DAMAGE = "extra_magic_damage";

    private static final int DEFAULT_COOLDOWN_TICKS = 360;
    private static final double DEFAULT_RANGE = 14.0;

    private static final int TICKS_PER_SECOND = 20;
    private static final int MAX_DURATION_SECONDS = 10 * 60;
    private static final int MAX_DURATION_TICKS =
        TICKS_PER_SECOND * MAX_DURATION_SECONDS;

    public record EffectSpec(
        Holder<MobEffect> effect,
        String durationKey,
        int defaultDurationTicks,
        String amplifierKey,
        int defaultAmplifier
    ) {}

    private final String usageId;
    private final DaoHenHelper.DaoType daoType;
    private final String nbtCooldownKey;
    private final List<EffectSpec> effects;

    public YuDaoActiveTargetNukeEffect(
        final String usageId,
        final DaoHenHelper.DaoType daoType,
        final String nbtCooldownKey,
        final List<EffectSpec> effects
    ) {
        this.usageId = usageId;
        this.daoType = daoType;
        this.nbtCooldownKey = nbtCooldownKey;
        this.effects = effects;
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

        final int remain = GuEffectCooldownHelper.getRemainingTicks(
            user,
            nbtCooldownKey
        );
        if (remain > 0) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                    "冷却中，剩余 " + remain + "t"
                ),
                true
            );
            return false;
        }

        final double baseRange = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RANGE, DEFAULT_RANGE)
        );
        final double selfMultiplier = daoType == null
            ? 1.0
            : DaoHenCalculator.calculateSelfMultiplier(user, daoType);
        final double range = baseRange * Math.max(0.0, selfMultiplier);
        final LivingEntity target = LineOfSightTargetHelper.findTarget(
            player,
            range
        );
        if (target == null) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("未找到可锁定目标。"),
                true
            );
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final double multiplier = daoType == null
            ? 1.0
            : DaoHenCalculator.calculateMultiplier(user, target, daoType);

        applyDebuffs(target, usageInfo, multiplier);

        final double damage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_EXTRA_MAGIC_DAMAGE, 0.0)
        );
        final double scaledDamage = damage * Math.max(0.0, multiplier);
        if (scaledDamage > 0.0) {
            target.hurt(user.damageSources().playerAttack(player), (float) scaledDamage);
        }

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

    private void applyDebuffs(
        final LivingEntity target,
        final NianTouData.Usage usageInfo,
        final double multiplier
    ) {
        if (effects == null || effects.isEmpty()) {
            return;
        }
        for (EffectSpec spec : effects) {
            if (spec == null || spec.effect() == null) {
                continue;
            }
            final int duration = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    spec.durationKey(),
                    spec.defaultDurationTicks()
                )
            );
            final int scaledDuration = scaleDuration(duration, multiplier);
            final int amplifier = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    spec.amplifierKey(),
                    spec.defaultAmplifier()
                )
            );
            if (scaledDuration > 0) {
                target.addEffect(
                    new MobEffectInstance(
                        spec.effect(),
                        scaledDuration,
                        amplifier,
                        true,
                        true
                    )
                );
            }
        }
    }

    private static int scaleDuration(final int baseDuration, final double multiplier) {
        if (baseDuration <= 0) {
            return 0;
        }
        final double scaled = baseDuration * Math.max(0.0, multiplier);
        if (scaled <= 0.0) {
            return 0;
        }
        return Math.min(Math.max(1, (int) Math.round(scaled)), MAX_DURATION_TICKS);
    }
}
