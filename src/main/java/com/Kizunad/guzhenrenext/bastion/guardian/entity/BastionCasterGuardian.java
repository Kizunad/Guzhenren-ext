package com.Kizunad.guzhenrenext.bastion.guardian.entity;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.entity.BastionGuardianData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 术士守卫（外观：烈焰人）。
 * <p>
 * 设计目标：作为远程法系守卫，复用烈焰人的火球射击与模型，并扩展多种法术。
 * </p>
 */
public class BastionCasterGuardian extends Blaze implements RangedAttackMob {

    private static final class Config {

        private static final String NBT_ROOT = "BastionCasterGuardian";
        private static final String NBT_NEXT_FIREBALL = "NextFireball";
        private static final String NBT_NEXT_WITHER_SKULL = "NextWitherSkull";

        private static final int FIREBALL_COOLDOWN_TICKS = 40;
        private static final int WITHER_SKULL_COOLDOWN_TICKS = 200;

        private static final double AIM_EPSILON = 1.0e-4d;
        private static final double FIREBALL_SPEED = 0.6d;
        private static final double FIREBALL_INACCURACY = 0.1d;
        private static final double WITHER_SKULL_SPEED = 0.7d;
        private static final double CAST_PARTICLE_SPREAD = 0.4d;
        private static final double CAST_PARTICLE_VERTICAL_SPREAD = 0.2d;
        private static final double CAST_PARTICLE_SPEED = 0.0d;
        private static final int CAST_PARTICLE_COUNT = 12;
        private static final double GOAL_MOVE_SPEED = 1.0d;
        private static final int GOAL_ATTACK_INTERVAL = 40;
        private static final float GOAL_MAX_RANGE = 32.0f;
        private static final int GOAL_PRIORITY = 4;

        private Config() {
        }
    }

    private enum SpellType {
        FIREBALL,
        WITHER_SKULL
    }

    private long nextFireballGameTime;
    private long nextWitherSkullGameTime;

    public BastionCasterGuardian(EntityType<? extends Blaze> type, Level level) {
        super(type, level);
        // 守卫需要长期驻守，禁止被动消失。
        this.setPersistenceRequired();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(Config.GOAL_PRIORITY, new RangedAttackGoal(this, Config.GOAL_MOVE_SPEED,
            Config.GOAL_ATTACK_INTERVAL, Config.GOAL_MAX_RANGE));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        // 远程入口 owner 过滤：
        // 即便目标已进入原版 RangedAttackGoal，本处仍需基于 bastion.isFriendlyTo(playerId)
        // 做最终短路，防止友方玩家在火球/凋零头颅路径上承受负面效果。
        if (target instanceof Player player) {
            var bastionId = BastionGuardianData.getBastionId(this);
            if (bastionId != null) {
                BastionData bastion = BastionSavedData.get(serverLevel).getBastion(bastionId);
                if (bastion != null && bastion.isFriendlyTo(player.getUUID())) {
                    return;
                }
            }
        }
        long now = this.level().getGameTime();
        SpellType spell = this.chooseSpell(now);
        if (spell == null) {
            return;
        }
        Vec3 eyePos = this.getEyePosition();
        Vec3 targetPos = target.getEyePosition();
        Vec3 dir = targetPos.subtract(eyePos);
        if (dir.lengthSqr() < Config.AIM_EPSILON) {
            return;
        }
        switch (spell) {
            case WITHER_SKULL -> {
                this.castWitherSkull(serverLevel, dir, eyePos);
                this.nextWitherSkullGameTime = now + Config.WITHER_SKULL_COOLDOWN_TICKS;
            }
            case FIREBALL -> {
                this.castFireball(serverLevel, dir, eyePos);
                this.nextFireballGameTime = now + Config.FIREBALL_COOLDOWN_TICKS;
            }
            default -> {
                return;
            }
        }
        this.spawnCastParticles(serverLevel);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        CompoundTag root = tag.getCompound(Config.NBT_ROOT);
        root.putLong(Config.NBT_NEXT_FIREBALL, this.nextFireballGameTime);
        root.putLong(Config.NBT_NEXT_WITHER_SKULL, this.nextWitherSkullGameTime);
        tag.put(Config.NBT_ROOT, root);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        CompoundTag root = tag.getCompound(Config.NBT_ROOT);
        this.nextFireballGameTime = root.getLong(Config.NBT_NEXT_FIREBALL);
        this.nextWitherSkullGameTime = root.getLong(Config.NBT_NEXT_WITHER_SKULL);
    }

    /**
     * 根据当前冷却选择可用法术。
     */
    private SpellType chooseSpell(long now) {
        if (now >= this.nextWitherSkullGameTime) {
            return SpellType.WITHER_SKULL;
        }
        if (now >= this.nextFireballGameTime) {
            return SpellType.FIREBALL;
        }
        return null;
    }

    private void castFireball(ServerLevel level, Vec3 dir, Vec3 eyePos) {
        Vec3 normalized = dir.normalize();
        Vec3 inaccuracy = new Vec3(
            this.random.triangle(0.0d, Config.FIREBALL_INACCURACY),
            this.random.triangle(0.0d, Config.FIREBALL_INACCURACY),
            this.random.triangle(0.0d, Config.FIREBALL_INACCURACY)
        );
        Vec3 velocity = normalized.add(inaccuracy).scale(Config.FIREBALL_SPEED);
        SmallFireball fireball = new SmallFireball(level, this, velocity);
        level.addFreshEntity(fireball);
    }

    private void castWitherSkull(ServerLevel level, Vec3 dir, Vec3 eyePos) {
        Vec3 velocity = dir.normalize().scale(Config.WITHER_SKULL_SPEED);
        WitherSkull skull = new WitherSkull(level, this, velocity);
        skull.setPos(eyePos);
        level.addFreshEntity(skull);
    }

    /**
     * 施法时生成魔法粒子，用于提示释放动作。
     */
    private void spawnCastParticles(ServerLevel level) {
        level.sendParticles(
            ParticleTypes.ENCHANT,
            this.getX(),
            this.getEyeY(),
            this.getZ(),
            Config.CAST_PARTICLE_COUNT,
            Config.CAST_PARTICLE_SPREAD,
            Config.CAST_PARTICLE_VERTICAL_SPREAD,
            Config.CAST_PARTICLE_SPREAD,
            Config.CAST_PARTICLE_SPEED
        );
    }
}
