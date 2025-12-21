package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.rendao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
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
import net.minecraft.world.phys.AABB;

/**
 * 人道通用主动：范围治疗/净化（偏辅助）。
 * <p>
 * 通过 metadata 配置：<br>
 * - cooldown_ticks<br>
 * - radius / ignore_walls<br>
 * - heal_amount / zhenyuan_restore / jingli_restore / hunpo_restore<br>
 * - cleanse_harmful（是否净化负面效果）<br>
 * - 可选：额外给予友方的 Buff（由注册时注入 EffectSpec，并从 metadata 读取 duration/amplifier）<br>
 * - 标准一次性消耗（由 {@link GuEffectCostHelper#tryConsumeOnce} 读取）<br>
 * </p>
 */
public class RenDaoActiveAreaHealEffect implements IGuEffect {

    public record EffectSpec(
        Holder<net.minecraft.world.effect.MobEffect> effect,
        String durationKey,
        int defaultDurationTicks,
        String amplifierKey,
        int defaultAmplifier
    ) {}

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_RADIUS = "radius";
    private static final String META_IGNORE_WALLS = "ignore_walls";
    private static final String META_HEAL_AMOUNT = "heal_amount";
    private static final String META_ZHENYUAN_RESTORE = "zhenyuan_restore";
    private static final String META_JINGLI_RESTORE = "jingli_restore";
    private static final String META_HUNPO_RESTORE = "hunpo_restore";
    private static final String META_CLEANSE_HARMFUL = "cleanse_harmful";

    private static final int DEFAULT_COOLDOWN_TICKS = 0;
    private static final double DEFAULT_RADIUS = 0.0;
    private static final double DEFAULT_AMOUNT = 0.0;

    private static final int TICKS_PER_SECOND = 20;
    private static final int MAX_DURATION_SECONDS = 10 * 60;
    private static final int MIN_DURATION_TICKS = 1;
    private static final int MAX_DURATION_TICKS =
        TICKS_PER_SECOND * MAX_DURATION_SECONDS;

    private final String usageId;
    private final String cooldownKey;
    private final List<EffectSpec> allyEffects;

    public RenDaoActiveAreaHealEffect(
        final String usageId,
        final String cooldownKey,
        final List<EffectSpec> allyEffects
    ) {
        this.usageId = usageId;
        this.cooldownKey = cooldownKey;
        this.allyEffects = allyEffects == null ? List.of() : List.copyOf(allyEffects);
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

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final double baseRadius = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RADIUS, DEFAULT_RADIUS)
        );
        final boolean ignoreWalls = UsageMetadataHelper.getBoolean(
            usageInfo,
            META_IGNORE_WALLS,
            false
        );
        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.REN_DAO
        );
        final double radius = baseRadius * Math.max(0.0, selfMultiplier);
        if (radius <= 0.0) {
            return false;
        }

        final double baseHeal = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HEAL_AMOUNT, DEFAULT_AMOUNT)
        );
        final double baseZhenyuan = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ZHENYUAN_RESTORE,
                DEFAULT_AMOUNT
            )
        );
        final double baseJingli = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_JINGLI_RESTORE,
                DEFAULT_AMOUNT
            )
        );
        final double baseHunpo = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_HUNPO_RESTORE,
                DEFAULT_AMOUNT
            )
        );
        final boolean cleanse = UsageMetadataHelper.getBoolean(
            usageInfo,
            META_CLEANSE_HARMFUL,
            false
        );

        final AABB box = user.getBoundingBox().inflate(radius);
        final List<LivingEntity> allies = user.level().getEntitiesOfClass(
            LivingEntity.class,
            box,
            e ->
                e.isAlive()
                    && (e == user || e.isAlliedTo(user))
                    && (ignoreWalls || user.hasLineOfSight(e))
        );
        for (LivingEntity ally : allies) {
            final double multiplier = ally == user
                ? selfMultiplier
                : DaoHenCalculator.calculateMultiplier(
                    user,
                    ally,
                    DaoHenHelper.DaoType.REN_DAO
                );

            final double heal = baseHeal * Math.max(0.0, multiplier);
            if (Double.compare(heal, 0.0) > 0) {
                ally.heal((float) heal);
            }

            final double zhenyuan = baseZhenyuan * Math.max(0.0, multiplier);
            if (Double.compare(zhenyuan, 0.0) > 0) {
                ZhenYuanHelper.modify(ally, zhenyuan);
            }

            final double jingli = baseJingli * Math.max(0.0, multiplier);
            if (Double.compare(jingli, 0.0) > 0) {
                JingLiHelper.modify(ally, jingli);
            }

            final double hunpo = baseHunpo * Math.max(0.0, multiplier);
            if (Double.compare(hunpo, 0.0) > 0) {
                HunPoHelper.modify(ally, hunpo);
            }

            if (cleanse) {
                cleanseHarmful(ally);
            }

            applyAllyEffects(ally, usageInfo, multiplier);
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
                cooldownKey,
                user.tickCount + cooldownTicks
            );
        }

        return true;
    }

    private void applyAllyEffects(
        final LivingEntity ally,
        final NianTouData.Usage usageInfo,
        final double multiplier
    ) {
        if (allyEffects.isEmpty()) {
            return;
        }
        for (EffectSpec spec : allyEffects) {
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
            ally.addEffect(
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
        for (Holder<net.minecraft.world.effect.MobEffect> holder : toRemove) {
            target.removeEffect(holder);
        }
    }

    private static int scaleDuration(final int baseDuration, final double multiplier) {
        final int duration = (int) Math.round(baseDuration * Math.max(0.0, multiplier));
        if (duration <= 0) {
            return 0;
        }
        return (int) UsageMetadataHelper.clamp(duration, MIN_DURATION_TICKS, MAX_DURATION_TICKS);
    }
}
