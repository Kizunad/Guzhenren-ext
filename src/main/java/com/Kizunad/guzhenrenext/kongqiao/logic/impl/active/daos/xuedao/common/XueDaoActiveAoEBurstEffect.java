package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.PhysicalDamageSourceHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 血道通用主动：范围爆发（血网缠绕/血爆/喷血冲击波等）。
 * <p>
 * 以普通伤害为主；可选附带控制 debuff 与击退/牵制感。
 * </p>
 */
public class XueDaoActiveAoEBurstEffect implements IGuEffect {

    private static final String META_RADIUS = "radius";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_DAMAGE = "damage";
    private static final String META_SELF_HEAL_PER_HIT = "self_heal_per_hit";
    private static final String META_DEBUFF_DURATION_TICKS = "debuff_duration_ticks";
    private static final String META_DEBUFF_AMPLIFIER = "debuff_amplifier";
    private static final String META_KNOCKBACK_STRENGTH = "knockback_strength";

    private static final double DEFAULT_RADIUS = 6.0;
    private static final int DEFAULT_COOLDOWN_TICKS = 260;
    private static final double DEFAULT_DAMAGE = 10.0;
    private static final double DEFAULT_KNOCKBACK_STRENGTH = 0.0;
    private static final double MAX_SELF_HEAL = 100.0;

    private static final double MIN_KNOCKBACK_DISTANCE_SQ = 0.0001;

    private final String usageId;
    private final String nbtCooldownKey;
    private final Holder<MobEffect> debuff;

    public XueDaoActiveAoEBurstEffect(
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

        final double radius = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RADIUS, DEFAULT_RADIUS)
        );
        final double baseDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_DAMAGE, DEFAULT_DAMAGE)
        );

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(user, DaoHenHelper.DaoType.XUE_DAO)
        );
        final int debuffDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
            Math.max(0, UsageMetadataHelper.getInt(usageInfo, META_DEBUFF_DURATION_TICKS, 0)),
            selfMultiplier
        );
        final int debuffAmplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(usageInfo, META_DEBUFF_AMPLIFIER, 0)
        );

        final double baseHealPerHit = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_SELF_HEAL_PER_HIT, 0.0)
        );
        final double healPerHit = DaoHenEffectScalingHelper.scaleValue(
            baseHealPerHit,
            selfMultiplier
        );

        final double knockbackStrength = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_KNOCKBACK_STRENGTH,
                DEFAULT_KNOCKBACK_STRENGTH
            )
        );

        final AABB area = user.getBoundingBox().inflate(radius);
        final List<LivingEntity> targets = user.level().getEntitiesOfClass(
            LivingEntity.class,
            area,
            e -> e.isAlive() && e != user && !e.isAlliedTo(user)
        );

        double totalHeal = 0.0;
        for (LivingEntity t : targets) {
            if (debuff != null && debuffDuration > 0) {
                t.addEffect(
                    new MobEffectInstance(debuff, debuffDuration, debuffAmplifier, true, true)
                );
            }
            if (baseDamage > 0.0) {
                final double multiplier = DaoHenCalculator.calculateMultiplier(
                    user,
                    t,
                    DaoHenHelper.DaoType.XUE_DAO
                );
                final double finalDamage = baseDamage * Math.max(0.0, multiplier);
                if (finalDamage > 0.0) {
                    t.hurt(
                        PhysicalDamageSourceHelper.buildPhysicalDamageSource(user),
                        (float) finalDamage
                    );
                    if (healPerHit > 0.0) {
                        totalHeal += healPerHit;
                    }
                }
            }
            applyKnockback(user, t, knockbackStrength);
        }

        if (totalHeal > 0.0) {
            user.heal((float) UsageMetadataHelper.clamp(totalHeal, 0.0, MAX_SELF_HEAL));
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

    private static void applyKnockback(
        final LivingEntity user,
        final LivingEntity target,
        final double strength
    ) {
        if (strength <= 0.0) {
            return;
        }
        final Vec3 delta = target.position().subtract(user.position());
        final double d2 = delta.lengthSqr();
        if (d2 <= MIN_KNOCKBACK_DISTANCE_SQ) {
            return;
        }
        final Vec3 dir = delta.normalize();
        target.push(dir.x * strength, 0.0, dir.z * strength);
        target.hurtMarked = true;
    }
}

