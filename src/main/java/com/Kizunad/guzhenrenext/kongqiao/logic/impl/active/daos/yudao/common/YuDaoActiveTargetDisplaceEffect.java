package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.LineOfSightTargetHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * 宇道通用主动：牵引/推斥。
 * <p>
 * 视线锁定目标，将其拉向施法者或推离施法者，可附带法术伤害与 debuff。
 * </p>
 */
public class YuDaoActiveTargetDisplaceEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_RANGE = "range";
    private static final String META_FORCE = "force";
    private static final String META_EXTRA_MAGIC_DAMAGE = "extra_magic_damage";
    private static final String META_EFFECT_DURATION_TICKS = "effect_duration_ticks";
    private static final String META_EFFECT_AMPLIFIER = "effect_amplifier";

    private static final int DEFAULT_COOLDOWN_TICKS = 260;
    private static final double DEFAULT_RANGE = 10.0;
    private static final double DEFAULT_FORCE = 0.8;
    private static final int DEFAULT_EFFECT_DURATION_TICKS = 60;
    private static final int DEFAULT_EFFECT_AMPLIFIER = 0;
    private static final double MIN_HORIZONTAL_VECTOR_SQR = 0.0001;
    private static final double VERTICAL_PUSH = 0.05;

    private final String usageId;
    private final DaoHenHelper.DaoType daoType;
    private final String nbtCooldownKey;
    private final boolean pull;
    private final Holder<MobEffect> debuff;

    public YuDaoActiveTargetDisplaceEffect(
        final String usageId,
        final DaoHenHelper.DaoType daoType,
        final String nbtCooldownKey,
        final boolean pull,
        final Holder<MobEffect> debuff
    ) {
        this.usageId = usageId;
        this.daoType = daoType;
        this.nbtCooldownKey = nbtCooldownKey;
        this.pull = pull;
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

        final double selfMultiplier = daoType == null
            ? 1.0
            : DaoHenCalculator.calculateSelfMultiplier(user, daoType);

        final double baseRange = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RANGE, DEFAULT_RANGE)
        );
        final double range = baseRange * Math.max(0.0, selfMultiplier);
        final LivingEntity target = LineOfSightTargetHelper.findTarget(
            player,
            range
        );
        if (target == null) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("未找到可作用目标。"),
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

        final double baseForce = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_FORCE, DEFAULT_FORCE)
        );
        final double force = baseForce * Math.max(0.0, multiplier);
        if (force > 0.0) {
            displace(user, target, force, pull);
        }

        final double baseDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_EXTRA_MAGIC_DAMAGE, 0.0)
        );
        final double damage = baseDamage * Math.max(0.0, multiplier);
        if (damage > 0.0) {
            target.hurt(user.damageSources().playerAttack(player), (float) damage);
        }

        final int duration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_EFFECT_DURATION_TICKS,
                DEFAULT_EFFECT_DURATION_TICKS
            )
        );
        final int amplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_EFFECT_AMPLIFIER,
                DEFAULT_EFFECT_AMPLIFIER
            )
        );
        final int scaledDuration = scaleDuration(duration, multiplier);
        if (debuff != null && scaledDuration > 0) {
            target.addEffect(
                new MobEffectInstance(
                    debuff,
                    scaledDuration,
                    amplifier,
                    true,
                    true
                )
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

    private static void displace(
        final LivingEntity user,
        final LivingEntity target,
        final double force,
        final boolean pull
    ) {
        final Vec3 delta = pull
            ? user.position().subtract(target.position())
            : target.position().subtract(user.position());
        final Vec3 horizontal = new Vec3(delta.x, 0.0, delta.z);
        if (horizontal.lengthSqr() <= MIN_HORIZONTAL_VECTOR_SQR) {
            return;
        }
        final Vec3 dir = horizontal.normalize();
        target.push(dir.x * force, VERTICAL_PUSH, dir.z * force);
        target.hurtMarked = true;
    }

    private static int scaleDuration(final int baseDuration, final double multiplier) {
        if (baseDuration <= 0) {
            return 0;
        }
        final double scaled = baseDuration * Math.max(0.0, multiplier);
        if (scaled <= 0.0) {
            return 0;
        }
        return Math.max(1, (int) Math.round(scaled));
    }
}
