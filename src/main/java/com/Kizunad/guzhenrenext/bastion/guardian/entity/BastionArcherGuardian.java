package com.Kizunad.guzhenrenext.bastion.guardian.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 弓手守卫（外观：骷髅）。
 * <p>
 * 设计目标：作为专职远程小兵，复用骷髅的弓箭行为与模型。
 * Round 13 最小实现：仅保证常驻、不自发消失，行为/数值后续由守卫服务统一覆盖。
 * </p>
 */
public class BastionArcherGuardian extends AbstractSkeleton {

    private static final class Config {

        private static final float MULTISHOT_ANGLE_DEG = 10.0f;
        private static final int MULTISHOT_COUNT = 3;
        private static final int MULTISHOT_COOLDOWN_TICKS = 40;
        private static final float BASE_ARROW_VELOCITY = 1.6f;
        private static final float BASE_ARROW_INACCURACY = 0.0f;
        private static final double LEAD_PREDICT_TICKS = 10.0d;
        private static final double FIRE_ARROW_CHANCE = 0.2d;
        private static final int FIRE_TICKS = 100;
        private static final double HEIGHT_LEAD_MAX = 0.4d;
        private static final double HEIGHT_LEAD_DISTANCE_FACTOR = 0.02d;
        private static final double HEIGHT_LEAD_SPEED_FACTOR = 0.1d;

        private Config() {
        }
    }

    private long nextMultiShotGameTime;

    public BastionArcherGuardian(EntityType<? extends AbstractSkeleton> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!this.canUseMultiShot()) {
            super.performRangedAttack(target, distanceFactor);
            return;
        }
        double targetSpeed = target.getDeltaMovement().horizontalDistance();
        // 20 tick = 1s，用常量避免 MagicNumber。
        final double ticksPerSecond = 20.0d;
        double leadSeconds = Config.LEAD_PREDICT_TICKS / ticksPerSecond;
        Vec3 futurePos = target.position().add(target.getDeltaMovement().scale(leadSeconds));
        Vec3 eyePos = this.getEyePosition();
        Vec3 toPredicted = futurePos.subtract(eyePos);
        ItemStack arrowStack = new ItemStack(Items.ARROW);
        ItemStack weaponStack = this.getMainHandItem();
        for (int i = 0; i < Config.MULTISHOT_COUNT; i++) {
            AbstractArrow arrow = this.getArrow(arrowStack, distanceFactor, weaponStack);
            if (this.random.nextDouble() < Config.FIRE_ARROW_CHANCE) {
                arrow.setRemainingFireTicks(Config.FIRE_TICKS);
            }
            float angleOffsetDeg = (i - 1) * Config.MULTISHOT_ANGLE_DEG;
            Vec3 dir = rotateYaw(toPredicted, angleOffsetDeg).normalize();
            arrow.shoot(dir.x, dir.y + computeHeightLead(target, targetSpeed), dir.z,
                Config.BASE_ARROW_VELOCITY, Config.BASE_ARROW_INACCURACY);
            serverLevel.addFreshEntity(arrow);
        }
        this.resetMultiShotCooldown();
    }

    private boolean canUseMultiShot() {
        return this.level().getGameTime() >= this.nextMultiShotGameTime;
    }

    private void resetMultiShotCooldown() {
        this.nextMultiShotGameTime = this.level().getGameTime() + Config.MULTISHOT_COOLDOWN_TICKS;
    }

    @Override
    protected SoundEvent getStepSound() {
        // 复用骷髅脚步声，保持与原版一致的听感。
        return SoundEvents.SKELETON_STEP;
    }

    /**
     * 通过偏航旋转向量，实现扇形多重射击。
     */
    private Vec3 rotateYaw(Vec3 vec, float degrees) {
        float radians = degrees * Mth.DEG_TO_RAD;
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double x = vec.x * cos - vec.z * sin;
        double z = vec.x * sin + vec.z * cos;
        return new Vec3(x, vec.y, z);
    }

    /**
     * 简易抛物线高度补偿：根据目标水平速度和射击者距离，略微上抬箭矢。
     */
    private float computeHeightLead(LivingEntity target, double targetSpeed) {
        double distance = this.distanceTo(target);
        // 距离越远、目标速度越快，抬高越多，避免平射落地。
        double gravityComp = Math.min(Config.HEIGHT_LEAD_MAX,
            Config.HEIGHT_LEAD_DISTANCE_FACTOR * distance + Config.HEIGHT_LEAD_SPEED_FACTOR * targetSpeed);
        return (float) gravityComp;
    }
}
