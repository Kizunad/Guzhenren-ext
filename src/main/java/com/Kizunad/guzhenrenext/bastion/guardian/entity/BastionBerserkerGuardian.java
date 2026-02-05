package com.Kizunad.guzhenrenext.bastion.guardian.entity;

import java.util.EnumSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.level.Level;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

/**
 * 狂战守卫（外观：卫道士）。
 * <p>
 * 设计目标：作为高攻击、较快移动、低护甲的近战小兵，主打冲锋与破甲倾向。
 * 扩展：实现低血狂暴与可冷却冲锋。
 * </p>
 */
public class BastionBerserkerGuardian extends Vindicator {

    private static final class Config {

        private static final String NBT_ROOT = "BastionBerserkerGuardian";
        private static final String NBT_NEXT_CHARGE = "NextCharge";

        private static final float BERSERK_HEALTH_RATIO = 0.3f;
        private static final double BERSERK_DAMAGE_BONUS = 0.5d;
        private static final int BERSERK_PARTICLE_INTERVAL_TICKS = 5;
        private static final double BERSERK_PARTICLE_SPREAD = 0.25d;

        private static final int CHARGE_COOLDOWN_TICKS = 300;
        private static final int CHARGE_DURATION_TICKS = 20;
        private static final double CHARGE_SPEED_MULTIPLIER = 1.6d;
        private static final double CHARGE_HIT_RANGE = 2.5d;
        private static final float CHARGE_BONUS_DAMAGE = 3.0f;

        private Config() {
        }
    }

    private static final ResourceLocation BERSERK_DAMAGE_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(
        "guzhenrenext",
        "berserk_damage"
    );
    private static final ResourceLocation CHARGE_SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(
        "guzhenrenext",
        "charge_speed"
    );
    private static final double CHARGE_NAV_SPEED = 1.4d;

    private static final EntityDataAccessor<Boolean> DATA_BERSERK = SynchedEntityData.defineId(
        BastionBerserkerGuardian.class,
        EntityDataSerializers.BOOLEAN
    );

    private long nextChargeGameTime;
    private int chargingTicks;
    private boolean hasChargeHit;

    public BastionBerserkerGuardian(EntityType<? extends Vindicator> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BERSERK, false);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new ChargeAttackGoal());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.updateBerserkState();
        this.spawnBerserkParticles();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        CompoundTag root = tag.getCompound(Config.NBT_ROOT);
        root.putLong(Config.NBT_NEXT_CHARGE, this.nextChargeGameTime);
        tag.put(Config.NBT_ROOT, root);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        CompoundTag root = tag.getCompound(Config.NBT_ROOT);
        this.nextChargeGameTime = root.getLong(Config.NBT_NEXT_CHARGE);
    }

    /**
     * 是否处于狂暴状态（服务器同步给客户端）。
     */
    public boolean isBerserk() {
        return this.entityData.get(DATA_BERSERK);
    }

    private void setBerserk(boolean berserk) {
        this.entityData.set(DATA_BERSERK, berserk);
        this.setGlowingTag(berserk);
        if (!this.level().isClientSide()) {
            this.updateBerserkDamageModifier(berserk);
        }
    }

    private void updateBerserkState() {
        if (this.level().isClientSide()) {
            return;
        }
        boolean shouldBerserk = this.getHealth() <= this.getMaxHealth() * Config.BERSERK_HEALTH_RATIO;
        if (shouldBerserk != this.isBerserk()) {
            this.setBerserk(shouldBerserk);
        }
    }

    private void spawnBerserkParticles() {
        if (!this.isBerserk()) {
            return;
        }
        if (this.level().isClientSide() && this.tickCount % Config.BERSERK_PARTICLE_INTERVAL_TICKS == 0) {
            double spread = Config.BERSERK_PARTICLE_SPREAD;
            this.level().addParticle(
                ParticleTypes.ANGRY_VILLAGER,
                this.getRandomX(spread),
                this.getRandomY(),
                this.getRandomZ(spread),
                0.0d,
                0.02d,
                0.0d
            );
        }
    }

    private void updateBerserkDamageModifier(boolean berserk) {
        AttributeInstance attack = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack == null) {
            return;
        }
        attack.removeModifier(BERSERK_DAMAGE_MODIFIER_ID);
        if (berserk) {
            attack.addTransientModifier(new AttributeModifier(
                BERSERK_DAMAGE_MODIFIER_ID,
                Config.BERSERK_DAMAGE_BONUS,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
    }

    private void applyChargeSpeedModifier() {
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        if (speed.getModifier(CHARGE_SPEED_MODIFIER_ID) != null) {
            return;
        }
        speed.addTransientModifier(new AttributeModifier(
            CHARGE_SPEED_MODIFIER_ID,
            Config.CHARGE_SPEED_MULTIPLIER - 1.0d,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        ));
    }

    private void clearChargeSpeedModifier() {
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(CHARGE_SPEED_MODIFIER_ID);
        }
    }

    private class ChargeAttackGoal extends Goal {

        private LivingEntity chargeTarget;

        ChargeAttackGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = BastionBerserkerGuardian.this.getTarget();
            if (target == null || !target.isAlive() || target.isRemoved()) {
                return false;
            }
            long now = BastionBerserkerGuardian.this.level().getGameTime();
            if (now < BastionBerserkerGuardian.this.nextChargeGameTime) {
                return false;
            }
            return BastionBerserkerGuardian.this.distanceToSqr(target) > 1.5d;
        }

        @Override
        public void start() {
            this.chargeTarget = BastionBerserkerGuardian.this.getTarget();
            BastionBerserkerGuardian.this.chargingTicks = 0;
            BastionBerserkerGuardian.this.hasChargeHit = false;
            BastionBerserkerGuardian.this.applyChargeSpeedModifier();
            BastionBerserkerGuardian.this.getNavigation().moveTo(this.chargeTarget, CHARGE_NAV_SPEED);
            long now = BastionBerserkerGuardian.this.level().getGameTime();
            BastionBerserkerGuardian.this.nextChargeGameTime = now + Config.CHARGE_COOLDOWN_TICKS;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.chargeTarget == null || !this.chargeTarget.isAlive() || this.chargeTarget.isRemoved()) {
                return false;
            }
            return BastionBerserkerGuardian.this.chargingTicks < Config.CHARGE_DURATION_TICKS;
        }

        @Override
        public void tick() {
            BastionBerserkerGuardian.this.chargingTicks++;
            BastionBerserkerGuardian.this.getLookControl().setLookAt(this.chargeTarget, 30.0f, 30.0f);
            BastionBerserkerGuardian.this.getNavigation().moveTo(this.chargeTarget, CHARGE_NAV_SPEED);
            double distSq = BastionBerserkerGuardian.this.distanceToSqr(this.chargeTarget);
            if (!BastionBerserkerGuardian.this.hasChargeHit
                && distSq <= Config.CHARGE_HIT_RANGE * Config.CHARGE_HIT_RANGE) {
                this.performChargeHit();
            }
        }

        @Override
        public void stop() {
            BastionBerserkerGuardian.this.clearChargeSpeedModifier();
            this.chargeTarget = null;
        }

        private void performChargeHit() {
            BastionBerserkerGuardian.this.hasChargeHit = true;
            LivingEntity target = this.chargeTarget;
            if (target == null) {
                return;
            }
            float baseDamage = (float) BastionBerserkerGuardian.this.getAttributeValue(Attributes.ATTACK_DAMAGE);
            float extra = Config.CHARGE_BONUS_DAMAGE;
            DamageSource source = BastionBerserkerGuardian.this.damageSources().mobAttack(BastionBerserkerGuardian.this);
            target.hurt(source, baseDamage + extra);
            target.knockback(0.4d, BastionBerserkerGuardian.this.getX() - target.getX(),
                BastionBerserkerGuardian.this.getZ() - target.getZ());
        }
    }
}
