package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.guangdao.common;

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
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 光道通用主动：指向性控制/削弱（尽量不造成法术伤害）。
 * <p>
 * 通过 metadata 配置：<br>
 * - cooldown_ticks<br>
 * - range<br>
 * - 标准一次性消耗（由 {@link GuEffectCostHelper#tryConsumeOnce} 读取）<br>
 * - 具体 debuff 由注册时注入 EffectSpec 并从 metadata 读取 duration/amplifier<br>
 * </p>
 */
public class GuangDaoActiveTargetDebuffEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_RANGE = "range";

    private static final int DEFAULT_COOLDOWN_TICKS = 0;
    private static final double DEFAULT_RANGE = 0.0;

    private static final int TICKS_PER_SECOND = 20;
    private static final int MAX_DURATION_SECONDS = 10 * 60;
    private static final int MIN_DURATION_TICKS = 1;
    private static final int MAX_DURATION_TICKS =
        TICKS_PER_SECOND * MAX_DURATION_SECONDS;

    private final String usageId;
    private final String cooldownKey;
    private final List<EffectSpec> enemyEffects;

    public GuangDaoActiveTargetDebuffEffect(
        final String usageId,
        final String cooldownKey,
        final List<EffectSpec> enemyEffects
    ) {
        this.usageId = usageId;
        this.cooldownKey = cooldownKey;
        this.enemyEffects = enemyEffects == null ? List.of() : List.copyOf(enemyEffects);
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
        if (range <= 0.0) {
            return false;
        }

        final LivingEntity target = LineOfSightTargetHelper.findTarget(player, range);
        if (target == null || target.isAlliedTo(user)) {
            player.displayClientMessage(Component.literal("未锁定有效目标。"), true);
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final double multiplier = DaoHenCalculator.calculateMultiplier(
            user,
            target,
            DaoHenHelper.DaoType.GUANG_DAO
        );
        applyEnemyEffects(target, usageInfo, multiplier);

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

    private void applyEnemyEffects(
        final LivingEntity target,
        final NianTouData.Usage usageInfo,
        final double multiplier
    ) {
        if (enemyEffects.isEmpty()) {
            return;
        }
        for (EffectSpec spec : enemyEffects) {
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
