package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.rendao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * 人道主动：自毁殉爆（范围普通伤害 + 自身反噬）。
 * <p>
 * 适用于“玉碎”等以极端心念引爆血肉魂魄的设计：伤人亦伤己。
 * </p>
 */
public class RenDaoActiveSelfSacrificeExplosionEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_RADIUS = "radius";
    private static final String META_EXTRA_PHYSICAL_DAMAGE = "extra_physical_damage";
    private static final String META_SELF_DAMAGE_AMOUNT = "self_damage_amount";
    private static final String META_PREVENT_SELF_DEATH = "prevent_self_death";

    private static final int DEFAULT_COOLDOWN_TICKS = 20 * 60;
    private static final double DEFAULT_RADIUS = 6.0;

    private static final double MAX_RADIUS = 64.0;
    private static final double MAX_SELF_DAMAGE = 200.0;

    private final String usageId;
    private final String cooldownKey;

    public RenDaoActiveSelfSacrificeExplosionEffect(
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
                net.minecraft.network.chat.Component.literal(
                    "冷却中，剩余 " + remain + "t"
                ),
                true
            );
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.REN_DAO
        );
        final double rawRadius = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RADIUS, DEFAULT_RADIUS)
        );
        final double radius = Math.min(rawRadius * Math.max(0.0, selfMultiplier), MAX_RADIUS);

        final double baseDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_EXTRA_PHYSICAL_DAMAGE, 0.0)
        );
        if (radius > 0.0 && baseDamage > 0.0) {
            final AABB box = user.getBoundingBox().inflate(radius);
            for (LivingEntity target : user.level().getEntitiesOfClass(
                LivingEntity.class,
                box,
                e -> e.isAlive() && e != user
            )) {
                final double multiplier = DaoHenCalculator.calculateMultiplier(
                    user,
                    target,
                    DaoHenHelper.DaoType.REN_DAO
                );
                target.hurt(
                    buildPhysicalDamageSource(user),
                    (float) (baseDamage * Math.max(0.0, multiplier))
                );
            }
        }

        applySelfBacklash(user, usageInfo, selfMultiplier);

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

    private static void applySelfBacklash(
        final LivingEntity user,
        final NianTouData.Usage usageInfo,
        final double selfMultiplier
    ) {
        final double baseSelfDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_SELF_DAMAGE_AMOUNT, 0.0)
        );
        if (baseSelfDamage <= 0.0) {
            return;
        }

        double selfDamage = Math.min(
            MAX_SELF_DAMAGE,
            DaoHenEffectScalingHelper.scaleValue(baseSelfDamage, selfMultiplier)
        );
        if (selfDamage <= 0.0) {
            return;
        }

        if (UsageMetadataHelper.getBoolean(usageInfo, META_PREVENT_SELF_DEATH, false)) {
            final double effectiveHealth =
                user.getHealth() + user.getAbsorptionAmount();
            final double maxAllowed = Math.max(0.0, effectiveHealth - 1.0);
            selfDamage = Math.min(selfDamage, maxAllowed);
            if (selfDamage <= 0.0) {
                return;
            }
        }

        user.hurt(buildSelfDamageSource(user), (float) selfDamage);
    }

    private static DamageSource buildPhysicalDamageSource(final LivingEntity user) {
        if (user instanceof Player player) {
            return user.damageSources().playerAttack(player);
        }
        return user.damageSources().mobAttack(user);
    }

    private static DamageSource buildSelfDamageSource(final LivingEntity user) {
        return user.damageSources().generic();
    }
}

