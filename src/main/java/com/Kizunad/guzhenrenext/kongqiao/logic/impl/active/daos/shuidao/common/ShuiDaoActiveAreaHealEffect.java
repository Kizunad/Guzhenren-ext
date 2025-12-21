package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shuidao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * 水道主动：范围回春 - 群体治疗/回复（可选净化）+ 可选增益。
 * <p>
 * 用于“春雨/水生花/眼泪”等偏辅助蛊虫：不依赖法术伤害，价值集中在治疗与状态管理。
 * </p>
 *
 * <p>metadata:</p>
 * <ul>
 *   <li>cooldown_ticks</li>
 *   <li>radius</li>
 *   <li>heal_amount</li>
 *   <li>restore_zhenyuan / restore_jingli / restore_hunpo / restore_niantou</li>
 *   <li>cleanse_harmful（0/1）</li>
 *   <li>（效果）effects 由构造器注入，使用 durationKey/amplifierKey 从 metadata 读取</li>
 * </ul>
 */
public class ShuiDaoActiveAreaHealEffect implements IGuEffect {

    public static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    public static final String META_RADIUS = "radius";
    public static final String META_HEAL_AMOUNT = "heal_amount";

    public static final String META_RESTORE_ZHENYUAN = "restore_zhenyuan";
    public static final String META_RESTORE_JINGLI = "restore_jingli";
    public static final String META_RESTORE_HUNPO = "restore_hunpo";
    public static final String META_RESTORE_NIANTOU = "restore_niantou";

    public static final String META_CLEANSE_HARMFUL = "cleanse_harmful";

    private static final int DEFAULT_COOLDOWN_TICKS = 220;
    private static final double DEFAULT_RADIUS = 4.0;
    private static final double MIN_RADIUS = 0.0;
    private static final double MAX_RADIUS = 32.0;

    private static final double MAX_HEAL = 200.0;
    private static final double MAX_RESTORE = 100.0;

    private final String usageId;
    private final String nbtCooldownKey;
    private final List<EffectSpec> effects;

    public ShuiDaoActiveAreaHealEffect(
        final String usageId,
        final String nbtCooldownKey,
        final List<EffectSpec> effects
    ) {
        this.usageId = usageId;
        this.nbtCooldownKey = nbtCooldownKey;
        this.effects = effects == null ? List.of() : List.copyOf(effects);
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

        final int remain = GuEffectCooldownHelper.getRemainingTicks(user, nbtCooldownKey);
        if (remain > 0) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.SHUI_DAO
        );

        final double radius = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(usageInfo, META_RADIUS, DEFAULT_RADIUS),
            MIN_RADIUS,
            MAX_RADIUS
        );
        final List<LivingEntity> targets = listTargets(user, radius);

        final double healBase = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HEAL_AMOUNT, 0.0)
        );
        final double heal = Math.min(
            MAX_HEAL,
            DaoHenEffectScalingHelper.scaleValue(healBase, selfMultiplier)
        );

        final double restoreZhenyuan = clampRestore(
            UsageMetadataHelper.getDouble(usageInfo, META_RESTORE_ZHENYUAN, 0.0),
            selfMultiplier
        );
        final double restoreJingli = clampRestore(
            UsageMetadataHelper.getDouble(usageInfo, META_RESTORE_JINGLI, 0.0),
            selfMultiplier
        );
        final double restoreHunpo = clampRestore(
            UsageMetadataHelper.getDouble(usageInfo, META_RESTORE_HUNPO, 0.0),
            selfMultiplier
        );
        final double restoreNianTou = clampRestore(
            UsageMetadataHelper.getDouble(usageInfo, META_RESTORE_NIANTOU, 0.0),
            selfMultiplier
        );

        final boolean cleanse = UsageMetadataHelper.getInt(usageInfo, META_CLEANSE_HARMFUL, 0) > 0;

        for (LivingEntity target : targets) {
            if (cleanse) {
                cleanseHarmful(target);
            }
            if (heal > 0.0) {
                target.heal((float) heal);
            }
            if (restoreZhenyuan > 0.0) {
                ZhenYuanHelper.modify(target, restoreZhenyuan);
            }
            if (restoreJingli > 0.0) {
                JingLiHelper.modify(target, restoreJingli);
            }
            if (restoreHunpo > 0.0) {
                HunPoHelper.modify(target, restoreHunpo);
            }
            if (restoreNianTou > 0.0) {
                NianTouHelper.modify(target, restoreNianTou);
            }

            applyEffects(target, usageInfo, selfMultiplier);
        }

        final int cooldownTicks = Math.max(
            0,
            UsageMetadataHelper.getInt(usageInfo, META_COOLDOWN_TICKS, DEFAULT_COOLDOWN_TICKS)
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

    private void applyEffects(
        final LivingEntity target,
        final NianTouData.Usage usageInfo,
        final double selfMultiplier
    ) {
        for (EffectSpec spec : effects) {
            if (spec == null || spec.effect() == null) {
                continue;
            }
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
            final int duration = DaoHenEffectScalingHelper.scaleDurationTicks(
                baseDuration,
                selfMultiplier
            );
            if (duration <= 0) {
                continue;
            }
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

    private static double clampRestore(final double base, final double selfMultiplier) {
        if (base <= 0.0) {
            return 0.0;
        }
        return Math.min(
            MAX_RESTORE,
            DaoHenEffectScalingHelper.scaleValue(base, selfMultiplier)
        );
    }

    private static List<LivingEntity> listTargets(
        final LivingEntity user,
        final double radius
    ) {
        if (radius <= 0.0) {
            return List.of(user);
        }
        final AABB box = user.getBoundingBox().inflate(radius);
        return user.level().getEntitiesOfClass(
            LivingEntity.class,
            box,
            e -> e.isAlive() && (e == user || e.isAlliedTo(user))
        );
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

    public record EffectSpec(
        Holder<MobEffect> effect,
        String durationKey,
        int defaultDurationTicks,
        String amplifierKey,
        int defaultAmplifier
    ) {}
}

