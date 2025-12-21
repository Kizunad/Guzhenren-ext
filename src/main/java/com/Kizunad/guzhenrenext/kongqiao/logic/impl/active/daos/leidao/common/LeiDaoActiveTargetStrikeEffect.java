package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.leidao.common;

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
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * 雷道主动：指向打击 - 单体/小范围的普通伤害 + 可选减益。
 * <p>
 * 统一实现多个雷道主动技能：通过 metadata 配置伤害、范围与 AoE。
 * </p>
 */
public class LeiDaoActiveTargetStrikeEffect implements IGuEffect {

    public static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    public static final String META_RANGE = "range";
    public static final String META_EXTRA_PHYSICAL_DAMAGE = "extra_physical_damage";
    public static final String META_AOE_RADIUS = "aoe_radius";
    public static final String META_AOE_EXTRA_PHYSICAL_DAMAGE = "aoe_extra_physical_damage";

    private static final int DEFAULT_COOLDOWN_TICKS = 220;
    private static final double DEFAULT_RANGE = 12.0;
    private static final double MIN_RANGE = 1.0;
    private static final double MAX_RANGE = 64.0;

    private static final double MIN_RADIUS = 0.0;
    private static final double MAX_RADIUS = 16.0;

    private final String usageId;
    private final String nbtCooldownKey;
    private final List<EffectSpec> debuffs;

    public LeiDaoActiveTargetStrikeEffect(
        final String usageId,
        final String nbtCooldownKey,
        final List<EffectSpec> debuffs
    ) {
        this.usageId = usageId;
        this.nbtCooldownKey = nbtCooldownKey;
        this.debuffs = debuffs == null ? List.of() : List.copyOf(debuffs);
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

        final double range = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(usageInfo, META_RANGE, DEFAULT_RANGE),
            MIN_RANGE,
            MAX_RANGE
        );
        final LivingEntity target = LineOfSightTargetHelper.findTarget(player, range);
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

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.LEI_DAO
        );
        applyDebuffs(target, usageInfo, selfMultiplier);

        final DamageSource source = PhysicalDamageSourceHelper.buildPhysicalDamageSource(user);

        final double baseDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_EXTRA_PHYSICAL_DAMAGE, 0.0)
        );
        if (baseDamage > 0.0) {
            final double multiplier = DaoHenCalculator.calculateMultiplier(
                user,
                target,
                DaoHenHelper.DaoType.LEI_DAO
            );
            target.hurt(source, (float) (baseDamage * multiplier));
        }

        final double radius = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(usageInfo, META_AOE_RADIUS, 0.0),
            MIN_RADIUS,
            MAX_RADIUS
        );
        final double aoeBaseDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_AOE_EXTRA_PHYSICAL_DAMAGE, 0.0)
        );
        if (radius > 0.0 && aoeBaseDamage > 0.0) {
            final AABB box = target.getBoundingBox().inflate(radius);
            for (LivingEntity entity : user.level().getEntitiesOfClass(
                LivingEntity.class,
                box,
                e -> e.isAlive() && e != user && e != target
            )) {
                final double m = DaoHenCalculator.calculateMultiplier(
                    user,
                    entity,
                    DaoHenHelper.DaoType.LEI_DAO
                );
                entity.hurt(source, (float) (aoeBaseDamage * m));
            }
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

    private void applyDebuffs(
        final LivingEntity target,
        final NianTouData.Usage usageInfo,
        final double selfMultiplier
    ) {
        for (EffectSpec spec : debuffs) {
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
                selfMultiplier
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
                new MobEffectInstance(spec.effect(), scaledDuration, amplifier, true, true)
            );
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
