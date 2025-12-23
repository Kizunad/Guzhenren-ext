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
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

/**
 * 变化道主动杀招【分身幻围杀】：锁定主目标重创，并以幻身余势溅射周围。
 * <p>
 * 说明：伤害为普通伤害源；控制类持续时间按倍率裁剪；范围存在上限避免 AABB 扫描拖垮服务器。
 * </p>
 */
public class BianHuaDaoShazhaoPhantomCloneAssaultEffect implements IShazhaoActiveEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_bian_hua_phantom_clone_assault";

    private static final DaoHenHelper.DaoType DAO_TYPE =
        DaoHenHelper.DaoType.BIAN_HUA_DAO;

    private static final String META_RANGE = "range";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_PRIMARY_PHYSICAL_DAMAGE =
        "primary_physical_damage";
    private static final String META_SPLASH_PHYSICAL_DAMAGE =
        "splash_physical_damage";
    private static final String META_SPLASH_RADIUS = "splash_radius";
    private static final String META_SLOW_DURATION_TICKS = "slow_duration_ticks";
    private static final String META_SLOW_AMPLIFIER = "slow_amplifier";

    private static final double DEFAULT_RANGE = 18.0;
    private static final double DEFAULT_SPLASH_RADIUS = 4.0;
    private static final int DEFAULT_COOLDOWN_TICKS = 20 * 18;

    private static final double MIN_RANGE = 1.0;
    private static final double MIN_RADIUS = 0.1;
    private static final double MAX_SPLASH_RADIUS = 32.0;

    private static final String NBT_COOLDOWN_KEY =
        "guzhenrenext_shazhao_active_bian_hua_phantom_clone_assault_cd_until";

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

        final DamageSource source =
            PhysicalDamageSourceHelper.buildPhysicalDamageSource(player);
        final double primaryBase = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_PRIMARY_PHYSICAL_DAMAGE,
                0.0
            )
        );
        final double splashBase = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_SPLASH_PHYSICAL_DAMAGE,
                0.0
            )
        );

        final double primaryMultiplier = DaoHenCalculator.calculateMultiplier(
            player,
            target,
            DAO_TYPE
        );
        if (primaryBase > 0.0) {
            target.hurt(source, (float) (primaryBase * primaryMultiplier));
        }
        applySlow(target, data, primaryMultiplier);

        final double radius = clampRadius(
            ShazhaoMetadataHelper.getDouble(
                data,
                META_SPLASH_RADIUS,
                DEFAULT_SPLASH_RADIUS
            )
        );
        if (splashBase > 0.0 && radius > MIN_RADIUS) {
            applySplashDamage(player, target, data, source, splashBase, radius);
        }

        applyCooldown(player, data);
        return true;
    }

    private static void applySplashDamage(
        final ServerPlayer player,
        final LivingEntity mainTarget,
        final ShazhaoData data,
        final DamageSource source,
        final double splashBase,
        final double radius
    ) {
        final AABB box = mainTarget.getBoundingBox().inflate(radius);
        final List<LivingEntity> targets = player.level()
            .getEntitiesOfClass(LivingEntity.class, box, e -> e != null && e != player);
        for (LivingEntity target : targets) {
            if (target == null || target == mainTarget || isAlly(player, target)) {
                continue;
            }
            final double multiplier = DaoHenCalculator.calculateMultiplier(
                player,
                target,
                DAO_TYPE
            );
            target.hurt(source, (float) (splashBase * multiplier));
            applySlow(target, data, multiplier);
        }
    }

    private static void applySlow(
        final LivingEntity target,
        final ShazhaoData data,
        final double multiplier
    ) {
        final int durationTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, META_SLOW_DURATION_TICKS, 0)
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
            ShazhaoMetadataHelper.getInt(data, META_SLOW_AMPLIFIER, 0)
        );
        target.addEffect(
            new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                scaledDuration,
                amplifier,
                true,
                true
            )
        );
    }

    private static boolean isAlly(final LivingEntity user, final LivingEntity other) {
        if (other == user) {
            return true;
        }
        return user.isAlliedTo(other);
    }

    private static double clampRadius(final double radius) {
        final double r = Math.max(MIN_RADIUS, radius);
        return Math.min(r, MAX_SPLASH_RADIUS);
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
