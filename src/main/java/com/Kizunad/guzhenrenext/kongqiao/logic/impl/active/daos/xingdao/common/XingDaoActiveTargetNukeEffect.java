package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xingdao.common;

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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 星道通用主动：视线锁定单体打击。
 * <p>
 * - 资源消耗：真元折算 +（念头/精力/魂魄任选其一即可）。
 * - 伤害：优先使用物理伤害（受护甲影响），法术伤害仅作为兼容兜底。
 * - debuff 持续时间：随星道道痕动态变化（倍率裁剪防止失控）。
 * </p>
 */
public class XingDaoActiveTargetNukeEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_RANGE = "range";
    private static final String META_EXTRA_PHYSICAL_DAMAGE = "extra_physical_damage";
    private static final String META_EXTRA_MAGIC_DAMAGE = "extra_magic_damage";

    private static final int DEFAULT_COOLDOWN_TICKS = 360;
    private static final double DEFAULT_RANGE = 14.0;

    public record EffectSpec(
        Holder<MobEffect> effect,
        String durationKey,
        int defaultDurationTicks,
        String amplifierKey,
        int defaultAmplifier
    ) {}

    private final String usageId;
    private final String nbtCooldownKey;
    private final List<EffectSpec> effects;

    public XingDaoActiveTargetNukeEffect(
        final String usageId,
        final String nbtCooldownKey,
        final List<EffectSpec> effects
    ) {
        this.usageId = usageId;
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
            DaoHenHelper.DaoType.XING_DAO
        );

        applyDebuffs(target, usageInfo, multiplier);

        final double extraPhysicalDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_EXTRA_PHYSICAL_DAMAGE, 0.0)
        );
        final double extraMagicDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_EXTRA_MAGIC_DAMAGE, 0.0)
        );
        if (extraPhysicalDamage > 0.0) {
            final DamageSource source = buildPhysicalDamageSource(user);
            target.hurt(
                source,
                (float) (extraPhysicalDamage * multiplier)
            );
        } else if (extraMagicDamage > 0.0) {
            target.hurt(
                user.damageSources().magic(),
                (float) (extraMagicDamage * multiplier)
            );
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
            final int scaledDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
                duration,
                multiplier
            );
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

    private static DamageSource buildPhysicalDamageSource(final LivingEntity user) {
        if (user instanceof Player player) {
            return user.damageSources().playerAttack(player);
        }
        return user.damageSources().mobAttack(user);
    }
}
