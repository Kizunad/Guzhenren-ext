package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.huodao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoActiveEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

/**
 * 火/炎道主动杀招【天火坠击】：锁定目标，造成高额普通伤害，并对周围敌对单位溅射。
 * <p>
 * 数值定位：高阶大招，普通伤害为主，避免法术伤害穿甲。
 * </p>
 */
public class HuoDaoShazhaoHeavenfireMeteorEffect implements IShazhaoActiveEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_huo_dao_heavenfire_meteor";

    private static final String META_RANGE = "range";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_PRIMARY_PHYSICAL_DAMAGE =
        "primary_physical_damage";
    private static final String META_SPLASH_PHYSICAL_DAMAGE =
        "splash_physical_damage";
    private static final String META_SPLASH_RADIUS = "splash_radius";
    private static final String META_KNOCKBACK_STRENGTH = "knockback_strength";

    private static final double DEFAULT_RANGE = 24.0;
    private static final int DEFAULT_COOLDOWN_TICKS = 20 * 30;
    private static final double MIN_KNOCKBACK_DISTANCE = 0.0001;
    private static final double KNOCKBACK_UPWARD_PUSH = 0.2;

    private static final String NBT_COOLDOWN_KEY =
        "guzhenrenext_shazhao_active_huo_dao_heavenfire_meteor_cd_until";

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
            1.0,
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

        final double primaryBase = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(data, META_PRIMARY_PHYSICAL_DAMAGE, 0.0)
        );
        final double splashBase = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(data, META_SPLASH_PHYSICAL_DAMAGE, 0.0)
        );
        final double splashRadius = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(data, META_SPLASH_RADIUS, 0.0)
        );
        final double knockbackStrength = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(data, META_KNOCKBACK_STRENGTH, 0.0)
        );

        final DamageSource source = PhysicalDamageSourceHelper.buildPhysicalDamageSource(
            player
        );

        final double primaryMultiplier = DaoHenCalculator.calculateMultiplier(
            player,
            target,
            DaoHenHelper.DaoType.HUO_DAO
        );
        if (primaryBase > 0.0) {
            target.hurt(source, (float) (primaryBase * primaryMultiplier));
        }
        if (knockbackStrength > 0.0) {
            applyKnockback(player, target, knockbackStrength);
        }

        if (splashBase > 0.0 && splashRadius > 0.0) {
            final AABB box = target.getBoundingBox().inflate(splashRadius);
            final List<LivingEntity> others = player.level()
                .getEntitiesOfClass(
                    LivingEntity.class,
                    box,
                    e -> e != null && e != player && e != target
                );
            for (LivingEntity other : others) {
                if (other == null || isAlly(player, other)) {
                    continue;
                }
                final double multiplier = DaoHenCalculator.calculateMultiplier(
                    player,
                    other,
                    DaoHenHelper.DaoType.HUO_DAO
                );
                other.hurt(source, (float) (splashBase * multiplier));
                if (knockbackStrength > 0.0) {
                    applyKnockback(player, other, knockbackStrength);
                }
            }
        }

        final int cooldownTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, META_COOLDOWN_TICKS, DEFAULT_COOLDOWN_TICKS)
        );
        if (cooldownTicks > 0) {
            GuEffectCooldownHelper.setCooldownUntilTick(
                player,
                NBT_COOLDOWN_KEY,
                player.tickCount + cooldownTicks
            );
        }

        return true;
    }

    private static void applyKnockback(
        final LivingEntity user,
        final LivingEntity target,
        final double strength
    ) {
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

    private static boolean isAlly(final LivingEntity user, final LivingEntity other) {
        if (other == user) {
            return true;
        }
        return user.isAlliedTo(other);
    }
}
