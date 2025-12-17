package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.hundao.tierFour;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * 虎魄蛊：主动·【虎煞波】(Tiger Evil Wave)。
 * <p>
 * 设计目标：不依赖“实质性近战挥砍”，而是以咆哮冲击波打散包围圈。
 * <ul>
 *     <li>施放方式：按下触发进入短暂“蓄力”，到点自动释放</li>
 *     <li>效果：前方扇形强击退 + 200 魂魄伤害 + 缓慢 IV（1 秒）</li>
 *     <li>代价：20 魂魄 + 20 点“四转一阶真元”</li>
 * </ul>
 * </p>
 */
public class HuPoGuTigerEvilWaveEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:hupogu_active_tigerevilwave";

    private static final String TAG_CHARGE_UNTIL_TICK =
        "guzhenrenext_hupogu_wave_charge_until";

    private static final double ZHENYUAN_SIZHUAN_YIJIE_DENOMINATOR = 16384.0;

    private static final int DEFAULT_CHARGE_TICKS = 18;
    private static final double DEFAULT_ACTIVATE_SOUL_COST = 20.0;
    private static final double DEFAULT_ACTIVATE_ZHENYUAN_COST_SIZHUAN_YIJIE =
        20.0;

    private static final double DEFAULT_RANGE = 7.0;
    private static final double DEFAULT_ANGLE_DEGREES = 70.0;
    private static final double DEFAULT_BASE_KNOCKBACK = 1.6;
    private static final double DEFAULT_SOUL_DAMAGE = 200.0;

    private static final int DEFAULT_SLOW_DURATION_TICKS = 20;
    private static final int DEFAULT_SLOW_AMPLIFIER = 3;

    private static final Vector3f WAVE_COLOR_OUTER = new Vector3f(
        0.95f,
        0.85f,
        0.25f
    );
    private static final Vector3f WAVE_COLOR_INNER = new Vector3f(
        1.0f,
        0.55f,
        0.15f
    );

    private static final double VECTOR_EPSILON = 0.0001;
    private static final double HALF = 0.5;
    private static final double MAX_KNOCKBACK_STRENGTH = 4.0;
    private static final double SONIC_HEIGHT_FACTOR = 0.7;

    private static final float WAVE_DUST_SCALE_OUTER = 1.4f;
    private static final float WAVE_DUST_SCALE_INNER = 0.9f;
    private static final int WAVE_RAY_COUNT = 18;
    private static final int WAVE_STEP_COUNT = 14;
    private static final double WAVE_BASE_HEIGHT_FACTOR = 0.3;
    private static final double WAVE_ARC_HEIGHT = 0.25;

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public boolean onActivate(
        LivingEntity user,
        ItemStack stack,
        NianTouData.Usage usageInfo
    ) {
        if (user.level().isClientSide()) {
            return false;
        }

        int currentTick = user.tickCount;
        int chargeUntil = getChargeUntilTick(user);
        if (chargeUntil > currentTick) {
            if (user instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(
                    Component.literal("虎煞波蓄力中..."),
                    true
                );
            }
            return true;
        }

        int chargeTicks = getMetaInt(
            usageInfo,
            "charge_ticks",
            DEFAULT_CHARGE_TICKS
        );
        if (chargeTicks <= 0) {
            return releaseWave(user, stack, usageInfo);
        }
        setChargeUntilTick(user, currentTick + chargeTicks);
        if (user instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(
                Component.literal("虎魄咆哮蓄势，待势而发"),
                true
            );
        }
        return true;
    }

    @Override
    public void onTick(
        LivingEntity user,
        ItemStack stack,
        NianTouData.Usage usageInfo
    ) {
        if (user.level().isClientSide()) {
            return;
        }
        int chargeUntil = getChargeUntilTick(user);
        if (chargeUntil <= 0) {
            return;
        }
        if (user.tickCount < chargeUntil) {
            return;
        }
        setChargeUntilTick(user, 0);
        releaseWave(user, stack, usageInfo);
    }

    private boolean releaseWave(
        LivingEntity user,
        ItemStack stack,
        NianTouData.Usage usageInfo
    ) {
        if (!(user.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        double soulCost = getMetaDouble(
            usageInfo,
            "activate_soul_cost",
            DEFAULT_ACTIVATE_SOUL_COST
        );
        double zhenyuanCostSizhuanYijie = getMetaDouble(
            usageInfo,
            "activate_zhenyuan_cost_sizhuan_yijie",
            DEFAULT_ACTIVATE_ZHENYUAN_COST_SIZHUAN_YIJIE
        );
        double baseCost =
            zhenyuanCostSizhuanYijie * ZHENYUAN_SIZHUAN_YIJIE_DENOMINATOR;
        double realZhenyuanCost = ZhenYuanHelper.calculateGuCost(
            user,
            baseCost
        );

        if (!hasEnoughCost(user, soulCost, realZhenyuanCost)) {
            if (user instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(
                    Component.literal(
                        "消耗不足：需要 " +
                            soulCost +
                            " 魂魄 + " +
                            zhenyuanCostSizhuanYijie +
                            " 四转一阶真元"
                    ),
                    true
                );
            }
            return false;
        }
        consumeCost(user, soulCost, realZhenyuanCost);

        double range = getMetaDouble(usageInfo, "range", DEFAULT_RANGE);
        double angle = getMetaDouble(
            usageInfo,
            "angle_degrees",
            DEFAULT_ANGLE_DEGREES
        );
        double baseKnockback = getMetaDouble(
            usageInfo,
            "base_knockback",
            DEFAULT_BASE_KNOCKBACK
        );
        double soulDamage = getMetaDouble(
            usageInfo,
            "soul_damage",
            DEFAULT_SOUL_DAMAGE
        );
        int slowDuration = getMetaInt(
            usageInfo,
            "slow_duration_ticks",
            DEFAULT_SLOW_DURATION_TICKS
        );
        int slowAmplifier = getMetaInt(
            usageInfo,
            "slow_amplifier",
            DEFAULT_SLOW_AMPLIFIER
        );

        double knockbackStrength =
            baseKnockback *
            DaoHenCalculator.calculateSelfMultiplier(
                user,
                DaoHenHelper.DaoType.LI_DAO
            );
        knockbackStrength = Mth.clamp(
            knockbackStrength,
            0.0,
            MAX_KNOCKBACK_STRENGTH
        );

        Vec3 look = user.getLookAngle();
        Vec3 forward = new Vec3(look.x, 0, look.z);
        if (forward.lengthSqr() < VECTOR_EPSILON) {
            forward = new Vec3(0, 0, 1);
        }
        forward = forward.normalize();
        double cosThreshold = Math.cos(Math.toRadians(angle * HALF));

        AABB area = user.getBoundingBox().inflate(range, 2.0, range);
        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(
            LivingEntity.class,
            area
        );
        for (LivingEntity target : targets) {
            if (target == user) {
                continue;
            }
            if (!target.isAlive()) {
                continue;
            }

            Vec3 toTarget = target.position().subtract(user.position());
            double distance = toTarget.length();
            if (distance <= VECTOR_EPSILON || distance > range) {
                continue;
            }

            Vec3 horizontal = new Vec3(toTarget.x, 0, toTarget.z).normalize();
            double dot = forward.dot(horizontal);
            if (dot < cosThreshold) {
                continue;
            }

            target.addEffect(
                new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    slowDuration,
                    slowAmplifier
                )
            );

            applySoulDamage(user, target, soulDamage);
            applyKnockback(user, target, knockbackStrength);
        }

        spawnWaveVisuals(serverLevel, user, range, angle);
        serverLevel.sendParticles(
            ParticleTypes.SONIC_BOOM,
            user.getX(),
            user.getY() + user.getBbHeight() * SONIC_HEIGHT_FACTOR,
            user.getZ(),
            1,
            0,
            0,
            0,
            0
        );
        return true;
    }

    private static boolean hasEnoughCost(
        LivingEntity user,
        double soulCost,
        double zhenyuanCost
    ) {
        if (soulCost > 0 && HunPoHelper.getAmount(user) < soulCost) {
            return false;
        }
        return !(
            zhenyuanCost > 0 && !ZhenYuanHelper.hasEnough(user, zhenyuanCost)
        );
    }

    private static void consumeCost(
        LivingEntity user,
        double soulCost,
        double zhenyuanCost
    ) {
        if (soulCost > 0) {
            HunPoHelper.modify(user, -soulCost);
        }
        if (zhenyuanCost > 0) {
            ZhenYuanHelper.modify(user, -zhenyuanCost);
        }
    }

    private static void applySoulDamage(
        LivingEntity attacker,
        LivingEntity target,
        double baseSoulDamage
    ) {
        if (baseSoulDamage <= 0) {
            return;
        }
        double multiplier = DaoHenCalculator.calculateMultiplier(
            attacker,
            target,
            DaoHenHelper.DaoType.HUN_DAO
        );
        double finalDamage = baseSoulDamage * multiplier;
        if (finalDamage <= 0) {
            return;
        }
        HunPoHelper.modify(target, -finalDamage);
        HunPoHelper.checkAndKill(target);
    }

    private static void applyKnockback(
        LivingEntity user,
        LivingEntity target,
        double strength
    ) {
        Vec3 delta = target.position().subtract(user.position());
        double dx = delta.x;
        double dz = delta.z;
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len <= VECTOR_EPSILON) {
            return;
        }
        target.knockback(strength, dx / len, dz / len);
    }

    private void spawnWaveVisuals(
        ServerLevel level,
        LivingEntity user,
        double range,
        double angleDegrees
    ) {
        Vec3 look = user.getLookAngle();
        Vec3 forward = new Vec3(look.x, 0, look.z);
        if (forward.lengthSqr() < VECTOR_EPSILON) {
            forward = new Vec3(0, 0, 1);
        }
        forward = forward.normalize();
        Vec3 right = forward.cross(new Vec3(0, 1, 0)).normalize();

        DustParticleOptions outer = new DustParticleOptions(
            WAVE_COLOR_OUTER,
            WAVE_DUST_SCALE_OUTER
        );
        DustParticleOptions inner = new DustParticleOptions(
            WAVE_COLOR_INNER,
            WAVE_DUST_SCALE_INNER
        );

        double halfAngle = angleDegrees * HALF;
        double baseHeight =
            user.getY() + user.getBbHeight() * WAVE_BASE_HEIGHT_FACTOR;
        for (int r = 0; r < WAVE_RAY_COUNT; r++) {
            double t = WAVE_RAY_COUNT == 1
                ? 0.0
                : (double) r / (WAVE_RAY_COUNT - 1);
            double rayAngle = Math.toRadians(-halfAngle + angleDegrees * t);
            Vec3 dir = forward
                .scale(Math.cos(rayAngle))
                .add(right.scale(Math.sin(rayAngle)));
            for (int i = 0; i < WAVE_STEP_COUNT; i++) {
                double d = (double) (i + 1) / WAVE_STEP_COUNT;
                Vec3 pos = user.position().add(dir.scale(range * d));
                double y = baseHeight + Math.sin(d * Math.PI) * WAVE_ARC_HEIGHT;
                level.sendParticles(outer, pos.x, y, pos.z, 1, 0, 0, 0, 0);
                level.sendParticles(inner, pos.x, y, pos.z, 1, 0, 0, 0, 0);
            }
        }
    }

    private static int getChargeUntilTick(LivingEntity user) {
        CompoundTag tag = user.getPersistentData();
        return tag.getInt(TAG_CHARGE_UNTIL_TICK);
    }

    private static void setChargeUntilTick(LivingEntity user, int tick) {
        CompoundTag tag = user.getPersistentData();
        if (tick <= 0) {
            tag.remove(TAG_CHARGE_UNTIL_TICK);
            return;
        }
        tag.putInt(TAG_CHARGE_UNTIL_TICK, tick);
    }

    private static int getMetaInt(
        NianTouData.Usage usage,
        String key,
        int defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Integer.parseInt(
                    Objects.requireNonNull(usage.metadata().get(key))
                );
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    private static double getMetaDouble(
        NianTouData.Usage usage,
        String key,
        double defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Double.parseDouble(
                    Objects.requireNonNull(usage.metadata().get(key))
                );
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }
}
