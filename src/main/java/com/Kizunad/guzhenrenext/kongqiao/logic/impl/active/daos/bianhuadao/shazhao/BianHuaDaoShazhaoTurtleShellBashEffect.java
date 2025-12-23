package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoActiveEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.LineOfSightTargetHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.PhysicalDamageSourceHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * 变化道主动杀招【龟甲铁槌】：锁定单体，以龟甲厚势撞击并令其虚弱。
 * <p>
 * 说明：伤害为普通伤害源；虚弱持续时间按道痕倍率裁剪，避免过度控制。
 * </p>
 */
public class BianHuaDaoShazhaoTurtleShellBashEffect implements IShazhaoActiveEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_bian_hua_turtle_shell_bash";

    private static final DaoHenHelper.DaoType DAO_TYPE =
        DaoHenHelper.DaoType.BIAN_HUA_DAO;

    private static final String META_RANGE = "range";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_PHYSICAL_DAMAGE = "physical_damage";
    private static final String META_WEAKNESS_DURATION_TICKS =
        "weakness_duration_ticks";
    private static final String META_WEAKNESS_AMPLIFIER = "weakness_amplifier";
    private static final String META_KNOCKBACK_STRENGTH = "knockback_strength";

    private static final double DEFAULT_RANGE = 12.0;
    private static final int DEFAULT_COOLDOWN_TICKS = 20 * 8;

    private static final double MIN_RANGE = 1.0;
    private static final double MIN_KNOCKBACK_DISTANCE = 0.001;
    private static final double KNOCKBACK_UPWARD_PUSH = 0.1;

    private static final String NBT_COOLDOWN_KEY =
        "guzhenrenext_shazhao_active_bian_hua_turtle_shell_bash_cd_until";

    @Override
    public String getShazhaoId() {
        return SHAZHAO_ID;
    }

    @Override
    public boolean onActivate(final ServerPlayer player, final ShazhaoData data) {
        if (player == null || data == null || player.level().isClientSide()) {
            return false;
        }

        final int remain = GuEffectCooldownHelper.getRemainingTicks(
            player,
            NBT_COOLDOWN_KEY
        );
        if (remain > 0) {
            player.displayClientMessage(
                Component.literal("冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        final double range = Math.max(
            MIN_RANGE,
            ShazhaoMetadataHelper.getDouble(data, META_RANGE, DEFAULT_RANGE)
        );
        final LivingEntity target = LineOfSightTargetHelper.findTarget(player, range);
        if (target == null) {
            player.displayClientMessage(Component.literal("未找到可锁定目标。"), true);
            return false;
        }

        if (!ShazhaoCostHelper.tryConsumeOnce(player, player, data)) {
            return false;
        }

        final double multiplier = DaoHenCalculator.calculateMultiplier(
            player,
            target,
            DAO_TYPE
        );

        final double baseDamage = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(data, META_PHYSICAL_DAMAGE, 0.0)
        );
        if (baseDamage > 0.0) {
            final DamageSource source =
                PhysicalDamageSourceHelper.buildPhysicalDamageSource(player);
            target.hurt(source, (float) (baseDamage * multiplier));
        }

        applyWeakness(target, data, multiplier);
        applyKnockback(player, target, data, multiplier);
        applyCooldown(player, data);
        return true;
    }

    private static void applyWeakness(
        final LivingEntity target,
        final ShazhaoData data,
        final double multiplier
    ) {
        final int durationTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, META_WEAKNESS_DURATION_TICKS, 0)
        );
        if (durationTicks <= 0) {
            return;
        }
        final int scaledDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
            durationTicks,
            multiplier
        );
        if (scaledDuration <= 0) {
            return;
        }
        final int amplifier = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, META_WEAKNESS_AMPLIFIER, 0)
        );
        target.addEffect(
            new MobEffectInstance(
                MobEffects.WEAKNESS,
                scaledDuration,
                amplifier,
                true,
                true
            )
        );
    }

    private static void applyKnockback(
        final LivingEntity user,
        final LivingEntity target,
        final ShazhaoData data,
        final double multiplier
    ) {
        final double baseStrength = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(data, META_KNOCKBACK_STRENGTH, 0.0)
        );
        if (baseStrength <= 0.0) {
            return;
        }
        final double strength = DaoHenEffectScalingHelper.scaleValue(
            baseStrength,
            multiplier
        );
        if (strength <= 0.0) {
            return;
        }
        final double dx = target.getX() - user.getX();
        final double dz = target.getZ() - user.getZ();
        final double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist <= MIN_KNOCKBACK_DISTANCE) {
            return;
        }
        target.push(
            (dx / dist) * strength,
            KNOCKBACK_UPWARD_PUSH,
            (dz / dist) * strength
        );
        target.hurtMarked = true;
    }

    private static void applyCooldown(
        final ServerPlayer player,
        final ShazhaoData data
    ) {
        final int cooldownTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_COOLDOWN_TICKS,
                DEFAULT_COOLDOWN_TICKS
            )
        );
        if (cooldownTicks <= 0) {
            return;
        }
        GuEffectCooldownHelper.setCooldownUntilTick(
            player,
            NBT_COOLDOWN_KEY,
            player.tickCount + cooldownTicks
        );
    }
}

