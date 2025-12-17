package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierThree;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 三转鬼脸蛊：被动【狰狞噬魂】。
 * <p>
 * 原著描述倾向于“攻击类蛊虫，对魂魄造成巨大的冲击”。这里落地为“命中触发的魂魄撕裂”：
 * - 每次攻击命中，在冷却间隔内对目标追加一段魂道魂魄伤害。
 * - 魂魄伤害受魂道道痕交互倍率增幅（攻方加成、受方共鸣、异道抵消）。
 * - 同时回收少量“魂渣”滋养自身魂魄（吸取比例可配），符合鬼脸吞噬魂魄的意象。
 * </p>
 */
public class GuiLianGuSoulImpactEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:guiliangu_passive_soul_impact";

    private static final String NBT_LAST_TICK = "GuilianLastImpactTick";

    private static final double DEFAULT_SOUL_COST = 3.0;
    private static final double DEFAULT_BASE_SOUL_DAMAGE = 40.0;
    private static final int DEFAULT_TRIGGER_INTERVAL_TICKS = 20;
    private static final double DEFAULT_LEECH_PERCENT = 0.15;
    private static final int DEFAULT_WEAKNESS_DURATION_TICKS = 20;
    private static final int DEFAULT_WEAKNESS_AMPLIFIER = 0;

    private static final double PARTICLE_Y_OFFSET = 0.75;
    private static final int PARTICLE_SOUL_COUNT = 10;
    private static final double PARTICLE_SOUL_SPREAD = 0.35;
    private static final double PARTICLE_SOUL_SPEED = 0.02;
    private static final int PARTICLE_SMOKE_COUNT = 6;
    private static final double PARTICLE_SMOKE_SPREAD = 0.25;
    private static final double PARTICLE_SMOKE_SPEED = 0.01;
    private static final float IMPACT_SOUND_VOLUME = 0.6F;
    private static final float IMPACT_SOUND_PITCH = 1.0F;

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

        int interval = getMetaInt(
            usageInfo,
            "trigger_interval_ticks",
            DEFAULT_TRIGGER_INTERVAL_TICKS
        );
        CompoundTag nbt = attacker.getPersistentData();
        int lastTick = nbt.getInt(NBT_LAST_TICK);
        if (interval > 0 && (attacker.tickCount - lastTick) < interval) {
            return damage;
        }

        double soulCost = getMetaDouble(
            usageInfo,
            "soul_cost",
            DEFAULT_SOUL_COST
        );
        if (HunPoHelper.getAmount(attacker) < soulCost) {
            return damage;
        }

        if (!(attacker.level() instanceof ServerLevel serverLevel)) {
            return damage;
        }

        HunPoHelper.modify(attacker, -soulCost);
        nbt.putInt(NBT_LAST_TICK, attacker.tickCount);

        double baseSoulDamage = getMetaDouble(
            usageInfo,
            "base_soul_damage",
            DEFAULT_BASE_SOUL_DAMAGE
        );
        double multiplier = DaoHenCalculator.calculateMultiplier(
            attacker,
            target,
            DaoHenHelper.DaoType.HUN_DAO
        );
        double finalSoulDamage = baseSoulDamage * multiplier;
        applySoulImpact(attacker, target, finalSoulDamage);

        double leechPercent = getMetaDouble(
            usageInfo,
            "leech_percent",
            DEFAULT_LEECH_PERCENT
        );
        if (leechPercent > 0) {
            HunPoHelper.modify(attacker, finalSoulDamage * leechPercent);
        }

        int weaknessDuration = getMetaInt(
            usageInfo,
            "weakness_duration_ticks",
            DEFAULT_WEAKNESS_DURATION_TICKS
        );
        int weaknessAmplifier = getMetaInt(
            usageInfo,
            "weakness_amplifier",
            DEFAULT_WEAKNESS_AMPLIFIER
        );
        if (weaknessDuration > 0) {
            target.addEffect(
                new MobEffectInstance(
                    MobEffects.WEAKNESS,
                    weaknessDuration,
                    Math.max(0, weaknessAmplifier),
                    true,
                    true
                )
            );
        }

        serverLevel.sendParticles(
            ParticleTypes.SOUL,
            target.getX(),
            target.getY() + target.getBbHeight() * PARTICLE_Y_OFFSET,
            target.getZ(),
            PARTICLE_SOUL_COUNT,
            PARTICLE_SOUL_SPREAD,
            PARTICLE_SOUL_SPREAD,
            PARTICLE_SOUL_SPREAD,
            PARTICLE_SOUL_SPEED
        );
        serverLevel.sendParticles(
            ParticleTypes.SMOKE,
            target.getX(),
            target.getY() + target.getBbHeight() * PARTICLE_Y_OFFSET,
            target.getZ(),
            PARTICLE_SMOKE_COUNT,
            PARTICLE_SMOKE_SPREAD,
            PARTICLE_SMOKE_SPREAD,
            PARTICLE_SMOKE_SPREAD,
            PARTICLE_SMOKE_SPEED
        );
        serverLevel.playSound(
            null,
            target.getX(),
            target.getY(),
            target.getZ(),
            SoundEvents.WITHER_HURT,
            SoundSource.PLAYERS,
            IMPACT_SOUND_VOLUME,
            IMPACT_SOUND_PITCH
        );

        return damage;
    }

    private static void applySoulImpact(
        LivingEntity attacker,
        LivingEntity target,
        double soulDamage
    ) {
        if (soulDamage <= 0) {
            return;
        }

        double targetSoul = HunPoHelper.getAmount(target);
        if (targetSoul > 0) {
            HunPoHelper.modify(target, -soulDamage);
            HunPoHelper.checkAndKill(target);
            return;
        }

        DamageSource src = attacker.damageSources().magic();
        target.hurt(src, (float) soulDamage);
    }

    private static double getMetaDouble(
        NianTouData.Usage usage,
        String key,
        double defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Double.parseDouble(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    private static int getMetaInt(
        NianTouData.Usage usage,
        String key,
        int defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Integer.parseInt(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }
}
