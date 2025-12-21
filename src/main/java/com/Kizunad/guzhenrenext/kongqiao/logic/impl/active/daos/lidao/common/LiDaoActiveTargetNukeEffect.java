package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.lidao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.LineOfSightTargetHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 力道主动：单体重击（优先普通伤害），并可附带一个或多个负面效果。
 */
public class LiDaoActiveTargetNukeEffect implements IGuEffect {

    public record EffectSpec(
        Holder<MobEffect> effect,
        String durationKey,
        int defaultDurationTicks,
        String amplifierKey,
        int defaultAmplifier
    ) {}

    private static final String META_RANGE = "range";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_EXTRA_PHYSICAL_DAMAGE =
        "extra_physical_damage";
    private static final String META_EXTRA_MAGIC_DAMAGE = "extra_magic_damage";
    private static final String META_HEAL_AMOUNT = "self_heal_amount";

    private static final double DEFAULT_RANGE = 6.0;
    private static final int DEFAULT_COOLDOWN_TICKS = 20 * 8;
    private static final double MAX_HEAL = 100.0;

    private final String usageId;
    private final String nbtCooldownKey;
    private final List<EffectSpec> effects;

    public LiDaoActiveTargetNukeEffect(
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

        final double range = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RANGE, DEFAULT_RANGE)
        );
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

        final double multiplier = DaoHenCalculator.calculateMultiplier(
            user,
            target,
            DaoHenHelper.DaoType.LI_DAO
        );

        applyDebuffs(target, usageInfo, multiplier);
        applyExtraDamage(user, target, usageInfo, multiplier);
        applySelfHeal(user, usageInfo);

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
            if (duration <= 0) {
                continue;
            }
            final int scaledDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
                duration,
                multiplier
            );
            if (scaledDuration <= 0) {
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

    private static void applyExtraDamage(
        final LivingEntity user,
        final LivingEntity target,
        final NianTouData.Usage usageInfo,
        final double multiplier
    ) {
        final double extraPhysicalDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_EXTRA_PHYSICAL_DAMAGE,
                0.0
            )
        );
        final double extraMagicDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_EXTRA_MAGIC_DAMAGE,
                0.0
            )
        );
        if (extraPhysicalDamage > 0.0) {
            final DamageSource source = buildPhysicalDamageSource(user);
            target.hurt(source, (float) (extraPhysicalDamage * multiplier));
            return;
        }
        if (extraMagicDamage > 0.0) {
            target.hurt(
                user.damageSources().magic(),
                (float) (extraMagicDamage * multiplier)
            );
        }
    }

    private static void applySelfHeal(
        final LivingEntity user,
        final NianTouData.Usage usageInfo
    ) {
        final double healBase = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HEAL_AMOUNT, 0.0)
        );
        if (healBase <= 0.0) {
            return;
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.LI_DAO
        );
        final double heal = Math.min(
            MAX_HEAL,
            DaoHenEffectScalingHelper.scaleValue(healBase, selfMultiplier)
        );
        if (heal > 0.0) {
            user.heal((float) heal);
        }
    }

    private static DamageSource buildPhysicalDamageSource(final LivingEntity user) {
        if (user instanceof Player player) {
            return user.damageSources().playerAttack(player);
        }
        return user.damageSources().mobAttack(user);
    }
}
