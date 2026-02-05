package com.Kizunad.guzhenrenext.bastion.guardian.entity;

import com.Kizunad.guzhenrenext.bastion.guardian.BastionGuardianCombatRules;
import java.util.EnumSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.syncher.SynchedEntityData;

/**
 * 治疗守卫（外观：女巫）。
 * <p>
 * 设计目标：作为专职治疗/辅助的小兵，复用女巫的模型与动画。
 * </p>
 */
public class BastionHealerGuardian extends Witch {

    /**
     * 常量集中于内部类，避免 MagicNumber 警告。
     */
    private static final class Config {
        private static final String NBT_ROOT = "BastionHealerGuardian";
        private static final String NBT_NEXT_HEAL = "NextHeal";

        private static final double HEAL_RANGE = 8.0d;
        private static final int HEAL_COOLDOWN_TICKS = 60;
        private static final float HEAL_BASE = 4.0f;
        private static final int HEAL_RANDOM_BOUND = 3;
        private static final double HEALTH_RATIO_EPSILON = 1.0e-4d;
        private static final float LOOK_AT_ANGLE = 30.0f;

        private static final int PARTICLE_SEGMENTS = 8;
        private static final int PARTICLE_PER_SEGMENT = 1;
        private static final double PARTICLE_OFFSET = 0.0d;
        private static final double PARTICLE_SPEED = 0.0d;
        private static final double PARTICLE_STEP_DENOMINATOR = PARTICLE_SEGMENTS;

        private Config() {
        }
    }

    private long nextHealGameTime;

    public BastionHealerGuardian(EntityType<? extends Witch> type, Level level) {
        super(type, level);
        // 守卫需要长期驻守，禁止被动消失。
        this.setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new HealAlliesGoal());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        CompoundTag root = tag.getCompound(Config.NBT_ROOT);
        root.putLong(Config.NBT_NEXT_HEAL, this.nextHealGameTime);
        tag.put(Config.NBT_ROOT, root);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        CompoundTag root = tag.getCompound(Config.NBT_ROOT);
        this.nextHealGameTime = root.getLong(Config.NBT_NEXT_HEAL);
    }

    private class HealAlliesGoal extends Goal {

        private LivingEntity healTarget;

        HealAlliesGoal() {
            this.setFlags(EnumSet.of(Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (BastionHealerGuardian.this.level().isClientSide()) {
                return false;
            }
            long now = BastionHealerGuardian.this.level().getGameTime();
            if (now < BastionHealerGuardian.this.nextHealGameTime) {
                return false;
            }
            this.healTarget = this.findHealTarget();
            return this.healTarget != null;
        }

        @Override
        public void start() {
            if (!(BastionHealerGuardian.this.level() instanceof ServerLevel level)) {
                return;
            }
            if (this.healTarget == null) {
                return;
            }
            BastionHealerGuardian.this.getLookControl().setLookAt(
                this.healTarget,
                Config.LOOK_AT_ANGLE,
                Config.LOOK_AT_ANGLE
            );
            float healAmount = Config.HEAL_BASE
                + (float) BastionHealerGuardian.this.random.nextInt(Config.HEAL_RANDOM_BOUND);
            this.healTarget.heal(healAmount);
            this.spawnHealParticles(level, this.healTarget);
            long now = level.getGameTime();
            BastionHealerGuardian.this.nextHealGameTime = now + Config.HEAL_COOLDOWN_TICKS;
            this.healTarget = null;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return false;
        }

        private LivingEntity findHealTarget() {
            if (!(BastionHealerGuardian.this.level() instanceof ServerLevel level)) {
                return null;
            }
            AABB box = BastionHealerGuardian.this.getBoundingBox().inflate(Config.HEAL_RANGE);
            double bestRatio = Double.MAX_VALUE;
            double bestDistance = Double.MAX_VALUE;
            LivingEntity best = null;
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {
                if (entity == BastionHealerGuardian.this) {
                    continue;
                }
                if (!entity.isAlive() || entity.isRemoved()) {
                    continue;
                }
                if (!BastionGuardianCombatRules.isGuardian(entity)) {
                    continue;
                }
                if (!BastionGuardianCombatRules.isSameBastion(BastionHealerGuardian.this, entity)) {
                    continue;
                }
                float maxHealth = entity.getMaxHealth();
                if (maxHealth <= 0.0f) {
                    continue;
                }
                float currentHealth = entity.getHealth();
                if (currentHealth >= maxHealth) {
                    continue;
                }
                double ratio = currentHealth / maxHealth;
                double distance = BastionHealerGuardian.this.distanceToSqr(entity);
                if (ratio + Config.HEALTH_RATIO_EPSILON < bestRatio) {
                    bestRatio = ratio;
                    bestDistance = distance;
                    best = entity;
                    continue;
                }
                if (Math.abs(ratio - bestRatio) > Config.HEALTH_RATIO_EPSILON) {
                    continue;
                }
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = entity;
                }
            }
            return best;
        }

        private void spawnHealParticles(ServerLevel level, LivingEntity target) {
            Vec3 from = BastionHealerGuardian.this.getEyePosition();
            Vec3 to = target.getEyePosition();
            Vec3 step = to.subtract(from).scale(1.0d / Config.PARTICLE_STEP_DENOMINATOR);
            for (int i = 0; i < Config.PARTICLE_SEGMENTS; i++) {
                Vec3 pos = from.add(step.scale(i));
                level.sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    pos.x,
                    pos.y,
                    pos.z,
                    Config.PARTICLE_PER_SEGMENT,
                    Config.PARTICLE_OFFSET,
                    Config.PARTICLE_OFFSET,
                    Config.PARTICLE_OFFSET,
                    Config.PARTICLE_SPEED
                );
            }
        }
    }
}
