package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierTwo;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * 二转鬼叫蛊：被动【惊啸魂震】。
 * <p>
 * 原著描述倾向于“进攻蛊虫，发出惊啸声，造成魂魄震荡”。这里做成一次性触发的被动：
 * - 当持有者攻击命中时，若冷却结束且魂魄足够，则对目标附近敌人施加魂魄冲击并短暂迟滞。
 * - 通过魂道道痕增幅震荡强度（伤害倍率）与冲击手感（击退强度）。
 * </p>
 */
public class GuiJiaoGuSoulShriekEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:guijiaogu_passive_soul_shriek";

    private static final String NBT_LAST_TICK = "GuijiaoLastShriekTick";

    private static final double DEFAULT_BASE_SOUL_DAMAGE = 20.0;
    private static final double DEFAULT_RADIUS = 3.5;
    private static final int DEFAULT_COOLDOWN_TICKS = 60;
    private static final int DEFAULT_SLOW_DURATION_TICKS = 20;
    private static final int DEFAULT_SLOW_AMPLIFIER = 3;
    private static final double DEFAULT_KNOCKBACK_STRENGTH = 1.0;

    private static final double PARTICLE_Y_OFFSET = 0.8;
    private static final int SONIC_PARTICLE_COUNT = 1;
    private static final int SOUL_PARTICLE_COUNT = 6;
    private static final double SOUL_PARTICLE_SPREAD = 0.25;
    private static final double SOUL_PARTICLE_SPEED = 0.02;
    private static final float SHRIEK_SOUND_VOLUME = 0.6F;
    private static final float SHRIEK_SOUND_PITCH = 1.2F;

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public float onAttack(
        LivingEntity attacker,
        LivingEntity target,
        float damage,
        ItemStack stack,
        NianTouData.Usage usageInfo
    ) {
        if (attacker.level().isClientSide()) {
            return damage;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(attacker);
        if (config != null && !config.isPassiveEnabled(USAGE_ID)) {
            return damage;
        }

        int cooldownTicks = getMetaInt(
            usageInfo,
            "cooldown_ticks",
            DEFAULT_COOLDOWN_TICKS
        );
        CompoundTag nbt = attacker.getPersistentData();
        int lastTick = nbt.getInt(NBT_LAST_TICK);
        if (cooldownTicks > 0 && (attacker.tickCount - lastTick) < cooldownTicks) {
            return damage;
        }

        if (!(attacker.level() instanceof ServerLevel serverLevel)) {
            return damage;
        }

        // 扣除消耗并写入冷却（先扣费再效果，避免因异常导致“白嫖”）
        if (!GuEffectCostHelper.tryConsumeOnce(null, attacker, usageInfo)) {
            return damage;
        }
        nbt.putInt(NBT_LAST_TICK, attacker.tickCount);

        double radius = UsageMetadataHelper.getDouble(
            usageInfo,
            "radius",
            DEFAULT_RADIUS
        );
        double baseSoulDamage = UsageMetadataHelper.getDouble(
            usageInfo,
            "base_soul_damage",
            DEFAULT_BASE_SOUL_DAMAGE
        );
        int slowDuration = UsageMetadataHelper.getInt(
            usageInfo,
            "slow_duration_ticks",
            DEFAULT_SLOW_DURATION_TICKS
        );
        int slowAmplifier = UsageMetadataHelper.getInt(
            usageInfo,
            "slow_amplifier",
            DEFAULT_SLOW_AMPLIFIER
        );
        double knockbackStrength = UsageMetadataHelper.getDouble(
            usageInfo,
            "knockback_strength",
            DEFAULT_KNOCKBACK_STRENGTH
        );

        // 震荡范围以目标为中心，表现“惊啸声”向外扩散。
        AABB area = target.getBoundingBox().inflate(radius);
        List<LivingEntity> victims = serverLevel.getEntitiesOfClass(
            LivingEntity.class,
            area,
            e -> e.isAlive() && e != attacker && !isAlly(attacker, e)
        );

        double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            attacker,
            DaoHenHelper.DaoType.HUN_DAO
        );
        double finalKnockback = Math.max(0.0, knockbackStrength) * selfMultiplier;

        for (LivingEntity victim : victims) {
            double multiplier = DaoHenCalculator.calculateMultiplier(
                attacker,
                victim,
                DaoHenHelper.DaoType.HUN_DAO
            );
            double finalSoulDamage = baseSoulDamage * multiplier;
            applySoulShock(attacker, victim, finalSoulDamage);

            if (slowDuration > 0) {
                victim.addEffect(
                    new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        slowDuration,
                        Math.max(0, slowAmplifier),
                        true,
                        true
                    )
                );
            }

            if (finalKnockback > 0.0) {
                double dx = victim.getX() - attacker.getX();
                double dz = victim.getZ() - attacker.getZ();
                victim.knockback(finalKnockback, dx, dz);
            }

            serverLevel.sendParticles(
                ParticleTypes.SONIC_BOOM,
                victim.getX(),
                victim.getY() + victim.getBbHeight() * PARTICLE_Y_OFFSET,
                victim.getZ(),
                SONIC_PARTICLE_COUNT,
                0,
                0,
                0,
                0
            );
            serverLevel.sendParticles(
                ParticleTypes.SOUL,
                victim.getX(),
                victim.getY() + victim.getBbHeight() * PARTICLE_Y_OFFSET,
                victim.getZ(),
                SOUL_PARTICLE_COUNT,
                SOUL_PARTICLE_SPREAD,
                SOUL_PARTICLE_SPREAD,
                SOUL_PARTICLE_SPREAD,
                SOUL_PARTICLE_SPEED
            );
        }

        serverLevel.playSound(
            null,
            attacker.getX(),
            attacker.getY(),
            attacker.getZ(),
            SoundEvents.GHAST_SCREAM,
            SoundSource.PLAYERS,
            SHRIEK_SOUND_VOLUME,
            SHRIEK_SOUND_PITCH
        );

        return damage;
    }

    private static void applySoulShock(
        LivingEntity attacker,
        LivingEntity victim,
        double soulDamage
    ) {
        if (soulDamage <= 0) {
            return;
        }

        double targetSoul = HunPoHelper.getAmount(victim);
        if (targetSoul > 0) {
            HunPoHelper.modify(victim, -soulDamage);
            HunPoHelper.checkAndKill(victim);
            return;
        }

        DamageSource src = attacker.damageSources().mobAttack(attacker);
        victim.hurt(src, (float) soulDamage);
    }

    private static boolean isAlly(LivingEntity owner, LivingEntity target) {
        if (target.isAlliedTo(owner)) {
            return true;
        }
        if (target instanceof TamableAnimal pet && pet.getOwner() == owner) {
            return true;
        }
        return false;
    }

    private static int getMetaInt(
        NianTouData.Usage usage,
        String key,
        int defaultValue
    ) {
        return UsageMetadataHelper.getInt(usage, key, defaultValue);
    }
}
