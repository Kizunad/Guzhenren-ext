package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.guangdao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * 光道通用主动：范围物理打击（尽量避免法术伤害，走护甲结算）。
 * <p>
 * 通过 metadata 配置：<br>
 * - cooldown_ticks<br>
 * - radius / ignore_walls<br>
 * - damage（基础物理伤害）<br>
 * - burn_seconds（点燃秒数，可为 0）<br>
 * - blind_duration_ticks（可为 0）<br>
 * - 标准一次性消耗（由 {@link GuEffectCostHelper#tryConsumeOnce} 读取）<br>
 * </p>
 */
public class GuangDaoActiveAreaPhysicalStrikeEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_RADIUS = "radius";
    private static final String META_IGNORE_WALLS = "ignore_walls";
    private static final String META_DAMAGE = "damage";
    private static final String META_BURN_SECONDS = "burn_seconds";
    private static final String META_BLIND_DURATION_TICKS = "blind_duration_ticks";

    private static final int DEFAULT_COOLDOWN_TICKS = 0;
    private static final double DEFAULT_RADIUS = 0.0;
    private static final double DEFAULT_DAMAGE = 0.0;

    private static final int TICKS_PER_SECOND = 20;
    private static final int MAX_DURATION_SECONDS = 10 * 60;
    private static final int MIN_DURATION_TICKS = 1;
    private static final int MAX_DURATION_TICKS =
        TICKS_PER_SECOND * MAX_DURATION_SECONDS;

    private final String usageId;
    private final String cooldownKey;

    public GuangDaoActiveAreaPhysicalStrikeEffect(
        final String usageId,
        final String cooldownKey
    ) {
        this.usageId = usageId;
        this.cooldownKey = cooldownKey;
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

        final double radius = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RADIUS, DEFAULT_RADIUS)
        );
        if (radius <= 0.0) {
            return false;
        }
        final boolean ignoreWalls = UsageMetadataHelper.getBoolean(
            usageInfo,
            META_IGNORE_WALLS,
            false
        );

        final double baseDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_DAMAGE, DEFAULT_DAMAGE)
        );
        final int burnSeconds = Math.max(
            0,
            UsageMetadataHelper.getInt(usageInfo, META_BURN_SECONDS, 0)
        );
        final int blindDurationBase = Math.max(
            0,
            UsageMetadataHelper.getInt(usageInfo, META_BLIND_DURATION_TICKS, 0)
        );

        final DamageSource damageSource = player.damageSources().playerAttack(player);
        final AABB box = user.getBoundingBox().inflate(radius);
        final List<LivingEntity> enemies = user.level().getEntitiesOfClass(
            LivingEntity.class,
            box,
            e ->
                e.isAlive()
                    && e != user
                    && !e.isAlliedTo(user)
                    && (ignoreWalls || user.hasLineOfSight(e))
        );

        for (LivingEntity enemy : enemies) {
            final double multiplier = DaoHenCalculator.calculateMultiplier(
                user,
                enemy,
                DaoHenHelper.DaoType.GUANG_DAO
            );
            final float finalDamage = (float) (baseDamage * multiplier);
            if (finalDamage > 0.0F) {
                enemy.hurt(damageSource, finalDamage);
            }

            if (burnSeconds > 0) {
                enemy.igniteForSeconds(burnSeconds);
            }
            if (blindDurationBase > 0) {
                enemy.addEffect(
                    new MobEffectInstance(
                        MobEffects.BLINDNESS,
                        scaleDuration(blindDurationBase, multiplier),
                        0,
                        true,
                        true
                    )
                );
            }
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

    private static int scaleDuration(final int baseTicks, final double multiplier) {
        final double scaled = baseTicks * Math.max(0.0, multiplier);
        return (int) UsageMetadataHelper.clamp(
            Math.round(scaled),
            MIN_DURATION_TICKS,
            MAX_DURATION_TICKS
        );
    }
}
