package com.Kizunad.guzhenrenext.bastion.guardian.entity;

import com.Kizunad.guzhenrenext.bastion.guardian.BastionGuardianCombatRules;
import com.Kizunad.guzhenrenext.bastion.entity.BastionGuardianData;
import java.util.EnumSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

/**
 * 盾兵守卫（外观：卫道士 + 盾牌定位）。
 * <p>
 * 设计目标：作为高护甲、较高血量、较低攻击的近战小兵，主打前排承伤。
 * 增强：提供格挡减伤、投射物反弹与嘲讽吸引火力。
 * </p>
 */
public class BastionShieldGuardian extends Vindicator {

    private static final class Config {
        private static final String NBT_ROOT = "BastionShieldGuardian";
        private static final String NBT_NEXT_TAUNT = "NextTaunt";
        private static final String NBT_NEXT_BLOCK = "NextBlock";

        private static final float BLOCK_DAMAGE_MULTIPLIER = 0.5f;
        private static final int BLOCK_MIN_DURATION_TICKS = 40;
        private static final int BLOCK_MAX_DURATION_TICKS = 80;
        private static final int BLOCK_COOLDOWN_TICKS = 40;
        private static final double BLOCK_REFLECT_CHANCE = 0.5d;
        private static final float BLOCK_REFLECT_VELOCITY = 1.4f;
        private static final float BLOCK_REFLECT_INACCURACY = 0.1f;
        private static final double BLOCK_REFLECT_TARGET_HEIGHT = 0.6d;

        private static final double BLOCK_MIN_REFLECT_LEN_SQR = 1.0e-4;
    
        private static final int TAUNT_COOLDOWN_TICKS = 600;
        private static final double TAUNT_RANGE = 8.0d;
        private static final int TAUNT_CHECK_INTERVAL_TICKS = 20;
        private static final int ALLY_HURT_WINDOW_TICKS = 10;

        private Config() {
        }
    }

    private static final EntityDataAccessor<Boolean> DATA_BLOCKING = SynchedEntityData.defineId(
        BastionShieldGuardian.class,
        EntityDataSerializers.BOOLEAN
    );

    private long nextTauntGameTime;
    private long nextBlockGameTime;
    private int blockingTicks;

    public BastionShieldGuardian(EntityType<? extends Vindicator> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BLOCKING, false);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new ShieldBlockGoal());
        this.goalSelector.addGoal(3, new TauntGoal());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            return;
        }
        if (this.isBlocking() && !this.hasAliveTarget()) {
            this.setBlocking(false);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isBlocking() && !source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            if (source.is(DamageTypeTags.IS_PROJECTILE)) {
                this.tryReflectProjectile(source);
            }
            amount *= Config.BLOCK_DAMAGE_MULTIPLIER;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        CompoundTag root = tag.getCompound(Config.NBT_ROOT);
        root.putLong(Config.NBT_NEXT_TAUNT, this.nextTauntGameTime);
        root.putLong(Config.NBT_NEXT_BLOCK, this.nextBlockGameTime);
        tag.put(Config.NBT_ROOT, root);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        CompoundTag root = tag.getCompound(Config.NBT_ROOT);
        this.nextTauntGameTime = root.getLong(Config.NBT_NEXT_TAUNT);
        this.nextBlockGameTime = root.getLong(Config.NBT_NEXT_BLOCK);
    }

    /**
     * 是否正在格挡（服务器与客户端同步）。
     */
    public boolean isBlocking() {
        return this.entityData.get(DATA_BLOCKING);
    }

    private void setBlocking(boolean blocking) {
        this.entityData.set(DATA_BLOCKING, blocking);
        if (!blocking) {
            this.blockingTicks = 0;
        }
    }

    private boolean hasAliveTarget() {
        LivingEntity target = this.getTarget();
        return target != null && target.isAlive() && !target.isRemoved();
    }

    private void tryReflectProjectile(DamageSource source) {
        if (this.level().isClientSide()) {
            return;
        }
        if (this.random.nextDouble() > Config.BLOCK_REFLECT_CHANCE) {
            return;
        }
        Entity direct = source.getDirectEntity();
        if (!(direct instanceof Projectile projectile)) {
            return;
        }
        Entity attacker = BastionGuardianCombatRules.resolveAttacker(source);
        if (attacker != null && BastionGuardianCombatRules.isSameBastion(this, attacker)) {
            return;
        }
        Vec3 fallbackDir = projectile.getDeltaMovement().scale(-1.0d);
        Vec3 shootDir = fallbackDir;
        if (attacker != null) {
            Vec3 targetPos = new Vec3(
                attacker.getX(),
                attacker.getY() + Config.BLOCK_REFLECT_TARGET_HEIGHT,
                attacker.getZ()
            );
            shootDir = targetPos.subtract(this.position());
        }
        if (shootDir.lengthSqr() < Config.BLOCK_MIN_REFLECT_LEN_SQR) {
            shootDir = fallbackDir;
        }
        projectile.setOwner(this);
        projectile.moveTo(
            this.getX(),
            this.getEyeY(),
            this.getZ(),
            projectile.getYRot(),
            projectile.getXRot()
        );
        projectile.shoot(
            shootDir.x,
            shootDir.y,
            shootDir.z,
            Config.BLOCK_REFLECT_VELOCITY,
            Config.BLOCK_REFLECT_INACCURACY
        );
    }

    private class ShieldBlockGoal extends Goal {

        private int maxDuration;

        ShieldBlockGoal() {
            this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (!BastionShieldGuardian.this.hasAliveTarget()) {
                return false;
            }
            long now = BastionShieldGuardian.this.level().getGameTime();
            if (now < BastionShieldGuardian.this.nextBlockGameTime) {
                return false;
            }
            return !BastionShieldGuardian.this.isBlocking();
        }

        @Override
        public void start() {
            this.maxDuration = BastionShieldGuardian.this.random.nextInt(
                Config.BLOCK_MAX_DURATION_TICKS - Config.BLOCK_MIN_DURATION_TICKS + 1
            ) + Config.BLOCK_MIN_DURATION_TICKS;
            BastionShieldGuardian.this.setBlocking(true);
        }

        @Override
        public boolean canContinueToUse() {
            return BastionShieldGuardian.this.hasAliveTarget()
                && BastionShieldGuardian.this.blockingTicks < this.maxDuration;
        }

        @Override
        public void tick() {
            BastionShieldGuardian.this.blockingTicks++;
        }

        @Override
        public void stop() {
            BastionShieldGuardian.this.setBlocking(false);
            long now = BastionShieldGuardian.this.level().getGameTime();
            BastionShieldGuardian.this.nextBlockGameTime = now + Config.BLOCK_COOLDOWN_TICKS;
        }
    }

    private class TauntGoal extends Goal {

        private long lastCheckTick;
        private LivingEntity allyUnderAttack;

        TauntGoal() {
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (BastionShieldGuardian.this.level().isClientSide()) {
                return false;
            }
            long now = BastionShieldGuardian.this.level().getGameTime();
            if (now < this.lastCheckTick + Config.TAUNT_CHECK_INTERVAL_TICKS) {
                return false;
            }
            this.lastCheckTick = now;
            if (now < BastionShieldGuardian.this.nextTauntGameTime) {
                return false;
            }
            this.allyUnderAttack = this.findAllyUnderAttack();
            return this.allyUnderAttack != null;
        }

        @Override
        public void start() {
            this.performTaunt();
            long now = BastionShieldGuardian.this.level().getGameTime();
            BastionShieldGuardian.this.nextTauntGameTime = now + Config.TAUNT_COOLDOWN_TICKS;
            this.allyUnderAttack = null;
        }

        private LivingEntity findAllyUnderAttack() {
            if (!(BastionShieldGuardian.this.level() instanceof ServerLevel level)) {
                return null;
            }
            AABB box = BastionShieldGuardian.this.getBoundingBox().inflate(Config.TAUNT_RANGE);
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {
                if (entity == BastionShieldGuardian.this) {
                    continue;
                }
                if (!entity.isAlive() || entity.isRemoved()) {
                    continue;
                }
                if (!BastionGuardianCombatRules.isGuardian(entity)) {
                    continue;
                }
                if (!BastionGuardianCombatRules.isSameBastion(BastionShieldGuardian.this, entity)) {
                    continue;
                }
                if (entity.hurtTime <= 0) {
                    continue;
                }
                if (entity.hurtTime > Config.ALLY_HURT_WINDOW_TICKS) {
                    continue;
                }
                return entity;
            }
            return null;
        }

        private void performTaunt() {
            if (!(BastionShieldGuardian.this.level() instanceof ServerLevel level)) {
                return;
            }
            AABB box = BastionShieldGuardian.this.getBoundingBox().inflate(Config.TAUNT_RANGE);
            for (Mob mob : level.getEntitiesOfClass(Mob.class, box)) {
                if (mob == BastionShieldGuardian.this) {
                    continue;
                }
                if (!mob.isAlive() || mob.isRemoved()) {
                    continue;
                }
                if (BastionGuardianCombatRules.isSameBastion(BastionShieldGuardian.this, mob)) {
                    continue;
                }
                if (!canAggro(mob)) {
                    continue;
                }
                mob.setTarget(BastionShieldGuardian.this);
            }
        }

        private boolean canAggro(Mob mob) {
            if (!(BastionShieldGuardian.this.level() instanceof ServerLevel level)) {
                return false;
            }
            if (!BastionGuardianCombatRules.canGuardianDamage(level, BastionShieldGuardian.this, mob)) {
                return true;
            }
            if (mob.getAttribute(Attributes.FOLLOW_RANGE) == null) {
                return true;
            }
            double distSq = BastionShieldGuardian.this.distanceToSqr(mob);
            double followRange = mob.getAttributeValue(Attributes.FOLLOW_RANGE);
            return distSq <= followRange * followRange;
        }
    }
}
