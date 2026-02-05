package com.Kizunad.guzhenrenext.bastion.guardian.entity;

import com.Kizunad.guzhenrenext.bastion.guardian.BastionGuardianCombatRules;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * 增幅守卫（外观：幻术师）。
 * <p>
 * 设计目标：持续为附近同基地守卫提供攻击/防御/速度增益，并附带可见的施法粒子。
 * </p>
 */
public class BastionBufferGuardian extends Illusioner {

    private static final class Config {

        private static final String NBT_ROOT = "BastionBufferGuardian";
        private static final String NBT_NEXT_BUFF = "NextBuff";

        private static final double AURA_RANGE = 10.0d;
        private static final int BUFF_DURATION_TICKS = 200;
        private static final int BUFF_INTERVAL_TICKS = 100;
        private static final int BUFF_AMPLIFIER = 0;

        private static final int PARTICLE_COUNT = 12;
        private static final double PARTICLE_SPREAD = 0.5d;
        private static final double PARTICLE_Y_OFFSET = 0.5d;
        private static final double PARTICLE_SPEED = 0.01d;

        private Config() {
        }
    }

    private long nextBuffGameTime;

    public BastionBufferGuardian(EntityType<? extends Illusioner> type, Level level) {
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
        this.goalSelector.addGoal(2, new AuraBuffGoal());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        CompoundTag root = tag.getCompound(Config.NBT_ROOT);
        root.putLong(Config.NBT_NEXT_BUFF, this.nextBuffGameTime);
        tag.put(Config.NBT_ROOT, root);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        CompoundTag root = tag.getCompound(Config.NBT_ROOT);
        this.nextBuffGameTime = root.getLong(Config.NBT_NEXT_BUFF);
    }

    private class AuraBuffGoal extends Goal {

        private long lastScanTick;

        AuraBuffGoal() {
            this.setFlags(EnumSet.of(Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!(BastionBufferGuardian.this.level() instanceof ServerLevel level)) {
                return false;
            }
            long now = level.getGameTime();
            if (now < BastionBufferGuardian.this.nextBuffGameTime) {
                return false;
            }
            if (now == this.lastScanTick) {
                return false;
            }
            this.lastScanTick = now;
            return !this.findTargets(level).isEmpty();
        }

        @Override
        public void start() {
            if (!(BastionBufferGuardian.this.level() instanceof ServerLevel level)) {
                return;
            }
            List<LivingEntity> targets = this.findTargets(level);
            if (targets.isEmpty()) {
                return;
            }
            this.applyBuffs(level, targets);
            long now = level.getGameTime();
            BastionBufferGuardian.this.nextBuffGameTime = now + Config.BUFF_INTERVAL_TICKS;
        }

        private List<LivingEntity> findTargets(ServerLevel level) {
            List<LivingEntity> result = new ArrayList<>();
            AABB box = BastionBufferGuardian.this.getBoundingBox().inflate(Config.AURA_RANGE);
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {
                if (!entity.isAlive() || entity.isRemoved()) {
                    continue;
                }
                if (!BastionGuardianCombatRules.isGuardian(entity)) {
                    continue;
                }
                if (!BastionGuardianCombatRules.isSameBastion(BastionBufferGuardian.this, entity)) {
                    continue;
                }
                result.add(entity);
            }
            return result;
        }

        private void applyBuffs(ServerLevel level, List<LivingEntity> targets) {
            for (LivingEntity target : targets) {
                this.applyBuffToTarget(target);
                this.spawnParticles(level, target);
            }
        }

        private void applyBuffToTarget(LivingEntity target) {
            target.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_BOOST,
                Config.BUFF_DURATION_TICKS,
                Config.BUFF_AMPLIFIER
            ));
            target.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE,
                Config.BUFF_DURATION_TICKS,
                Config.BUFF_AMPLIFIER
            ));
            target.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SPEED,
                Config.BUFF_DURATION_TICKS,
                Config.BUFF_AMPLIFIER
            ));
        }

        private void spawnParticles(ServerLevel level, LivingEntity target) {
            level.sendParticles(
                ParticleTypes.ENCHANT,
                target.getX(),
                target.getY() + Config.PARTICLE_Y_OFFSET,
                target.getZ(),
                Config.PARTICLE_COUNT,
                Config.PARTICLE_SPREAD,
                Config.PARTICLE_SPREAD,
                Config.PARTICLE_SPREAD,
                Config.PARTICLE_SPEED
            );
        }
    }
}
