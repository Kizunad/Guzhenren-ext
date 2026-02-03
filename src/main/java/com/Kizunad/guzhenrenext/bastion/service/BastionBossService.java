package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeConfig;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeManager;
import com.Kizunad.guzhenrenext.bastion.entity.BastionGuardianData;
import com.Kizunad.guzhenrenext.bastion.entity.BossRewardData;
import com.Kizunad.guzhenrenext.bastion.guardian.BastionGuardianEntities;
import com.Kizunad.guzhenrenext.bastion.guardian.BastionGuardianStatsService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;

/**
 * 基地 Boss 服务 - 负责根据配置在攻击时触发 Boss 生成。
 * <p>
 * 仅在基地核心被攻击且满足配置要求时尝试生成 Boss，
 * 包含冷却、资源消耗、存活检测与属性应用。
 * </p>
 */
public final class BastionBossService {

    private BastionBossService() {
        // 工具类
    }

    /** 生成相关常量，集中避免 MagicNumber。 */
    private static final class BossConstants {
        /** 核心上方搜索高度。 */
        private static final int SPAWN_HEIGHT_CHECK = 4;
        /** 检查位置上方是否有空间的高度。 */
        private static final int HEADROOM_CHECK = 1;
        /** 搜索现存 Boss 的垂直半径。 */
        private static final int SEARCH_VERTICAL_RADIUS = 16;
        /** 搜索现存 Boss 的最小水平半径，避免半径过小。 */
        private static final int MIN_SEARCH_RADIUS = 12;
        /** 生成时的中心偏移。 */
        private static final double BLOCK_CENTER_OFFSET = 0.5d;
        /** 生成时的随机旋转角度范围（360度）。 */
        private static final float FULL_ROTATION_DEGREES = 360.0f;
        /** 威胁等级：无。 */
        private static final int THREAT_LEVEL_NONE = 0;
        /** 威胁等级：低。 */
        private static final int THREAT_LEVEL_LOW = 1;
        /** 威胁等级：中。 */
        private static final int THREAT_LEVEL_MEDIUM = 2;
        /** 威胁等级：高。 */
        private static final int THREAT_LEVEL_HIGH = 3;

        private BossConstants() {
        }
    }

    /**
     * 尝试触发 Boss 生成。
     *
     * @param level    服务端世界
     * @param bastion  基地数据
     * @param attacker 攻击者
     */
    public static void tryTriggerBoss(ServerLevel level, BastionData bastion, ServerPlayer attacker) {
        BastionTypeConfig.BossConfig bossConfig = BastionTypeManager
            .getOrDefault(bastion.bastionType())
            .boss();

        if (bossConfig == null || !bossConfig.enabled()) {
            return;
        }
        if (bastion.tier() < bossConfig.minTier()) {
            return;
        }

        long gameTime = level.getGameTime();
        long lastSpawnTick = bastion.lastBossSpawnGameTime();
        if (lastSpawnTick > 0 && (gameTime - lastSpawnTick) < bossConfig.cooldownTicks()) {
            return;
        }

        if (bastion.resourcePool() < bossConfig.spawnCost()) {
            return;
        }

        if (hasAliveBoss(level, bastion)) {
            return;
        }

        double newPool = Math.max(0.0d, bastion.resourcePool() - bossConfig.spawnCost());
        BastionData updated = bastion
            .withResourcePool(newPool)
            .withLastBossSpawnGameTime(gameTime);

        Mob boss = spawnBoss(level, updated, attacker, bossConfig);
        if (boss == null) {
            return;
        }

        BastionSavedData savedData = BastionSavedData.get(level);
        savedData.updateBastion(updated);
    }

    /**
     * 检查是否已有存活的 Boss。
     */
    private static boolean hasAliveBoss(ServerLevel level, BastionData bastion) {
        BlockPos core = bastion.corePos();
        int horizontal = Math.max(bastion.growthRadius(), BossConstants.MIN_SEARCH_RADIUS);
        AABB box = new AABB(
            core.getX() - horizontal,
            core.getY() - BossConstants.SEARCH_VERTICAL_RADIUS,
            core.getZ() - horizontal,
            core.getX() + horizontal,
            core.getY() + BossConstants.SEARCH_VERTICAL_RADIUS,
            core.getZ() + horizontal
        );

        return !level.getEntitiesOfClass(Mob.class, box, mob ->
            mob.isAlive()
                && mob.getType() == BastionGuardianEntities.BASTION_RAVAGER.get()
                && BastionGuardianData.belongsToBastion(mob, bastion.id())
                && BossRewardData.isBoss(mob)
        ).isEmpty();
    }

    /**
     * 生成并配置 Boss。
     */
    private static Mob spawnBoss(
            ServerLevel level,
            BastionData bastion,
            ServerPlayer attacker,
            BastionTypeConfig.BossConfig bossConfig) {
        BlockPos spawnPos = resolveSpawnPosition(level, bastion, attacker);
        Mob boss = BastionGuardianEntities.BASTION_RAVAGER.get().create(level);
        if (boss == null) {
            return null;
        }

        boss.moveTo(
            spawnPos.getX() + BossConstants.BLOCK_CENTER_OFFSET,
            spawnPos.getY(),
            spawnPos.getZ() + BossConstants.BLOCK_CENTER_OFFSET,
            level.random.nextFloat() * BossConstants.FULL_ROTATION_DEGREES,
            0.0f
        );

        boss.finalizeSpawn(
            level,
            level.getCurrentDifficultyAt(spawnPos),
            MobSpawnType.TRIGGERED,
            null
        );
        boss.setPersistenceRequired();

        // 标记归属与 Boss 身份
        BastionGuardianData.markAsGuardian(boss, bastion.id(), bastion.tier());
        double rewardMultiplier = resolveRewardMultiplier(bossConfig, bastion);
        markBoss(boss, rewardMultiplier);

        // 应用属性：基础守卫属性 + Boss 配置倍率 + 威胁倍率
        BastionGuardianStatsService.applyGuardianStats(
            boss,
            bastion.primaryDao(),
            bastion.tier(),
            false
        );
        double attributeMultiplier = resolveAttributeMultiplier(bossConfig, bastion);
        applyBossAttributeMultiplier(boss, bossConfig, attributeMultiplier);
        boss.setHealth(boss.getMaxHealth());

        // 将 Boss 添加到世界
        level.addFreshEntity(boss);

        return boss;
    }

    /**
     * 计算属性倍率：Boss 配置倍率乘以威胁等级倍率。
     */
    private static double resolveAttributeMultiplier(
            BastionTypeConfig.BossConfig bossConfig,
            BastionData bastion) {
        double baseMultiplier = 1.0d;
        if (bossConfig != null) {
            baseMultiplier = Math.max(1.0d, bossConfig.healthMultiplier());
        }
        return baseMultiplier * resolveThreatAttributeMultiplier(bossConfig, bastion);
    }

    /**
     * 计算奖励倍率：默认 1.0，受威胁等级影响。
     */
    private static double resolveRewardMultiplier(
            BastionTypeConfig.BossConfig bossConfig,
            BastionData bastion) {
        double baseMultiplier = 1.0d;
        if (bossConfig != null) {
            baseMultiplier = Math.max(1.0d, bossConfig.damageMultiplier());
        }
        return baseMultiplier * resolveThreatRewardMultiplier(bossConfig, bastion);
    }

    /**
     * 应用 Boss 属性倍率（生命/伤害/护甲）。
     */
    private static void applyBossAttributeMultiplier(
            Mob boss,
            BastionTypeConfig.BossConfig bossConfig,
            double threatAttrMultiplier) {
        double healthMult = Math.max(1.0d, bossConfig.healthMultiplier()) * threatAttrMultiplier;
        double damageMult = Math.max(1.0d, bossConfig.damageMultiplier()) * threatAttrMultiplier;
        double armorMult = Math.max(1.0d, bossConfig.armorMultiplier()) * threatAttrMultiplier;

        scaleAttribute(boss, Attributes.MAX_HEALTH, healthMult);
        scaleAttribute(boss, Attributes.ATTACK_DAMAGE, damageMult);
        scaleAttribute(boss, Attributes.ARMOR, armorMult);
        scaleAttribute(boss, Attributes.ARMOR_TOUGHNESS, armorMult);
    }

    private static void scaleAttribute(Mob mob, Holder<Attribute> attribute, double multiplier) {
        AttributeInstance instance = mob.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        instance.setBaseValue(instance.getBaseValue() * multiplier);
    }

    /**
     * 威胁等级 -> 属性倍率。
     */
    private static double resolveThreatAttributeMultiplier(
            BastionTypeConfig.BossConfig bossConfig,
            BastionData bastion) {
        if (bossConfig == null || bossConfig.threatMultipliers() == null) {
            return 1.0d;
        }
        int level = mapThreatTier(BastionThreatService.getThreatTier(bastion));
        double best = 1.0d;
        for (BastionTypeConfig.BossConfig.ThreatMultiplier multiplier : bossConfig.threatMultipliers()) {
            if (multiplier == null) {
                continue;
            }
            if (level >= multiplier.threatLevel()) {
                best = Math.max(best, multiplier.attributeMultiplier());
            }
        }
        return best;
    }

    /**
     * 威胁等级 -> 掉落倍率。
     */
    private static double resolveThreatRewardMultiplier(
            BastionTypeConfig.BossConfig bossConfig,
            BastionData bastion) {
        if (bossConfig == null || bossConfig.threatMultipliers() == null) {
            return 1.0d;
        }
        int level = mapThreatTier(BastionThreatService.getThreatTier(bastion));
        double best = 1.0d;
        for (BastionTypeConfig.BossConfig.ThreatMultiplier multiplier : bossConfig.threatMultipliers()) {
            if (multiplier == null) {
                continue;
            }
            if (level >= multiplier.threatLevel()) {
                best = Math.max(best, multiplier.rewardMultiplier());
            }
        }
        return best;
    }

    private static int mapThreatTier(BastionThreatService.ThreatTier tier) {
        return switch (tier) {
            case HIGH -> BossConstants.THREAT_LEVEL_HIGH;
            case MEDIUM -> BossConstants.THREAT_LEVEL_MEDIUM;
            case LOW -> BossConstants.THREAT_LEVEL_LOW;
            default -> BossConstants.THREAT_LEVEL_NONE;
        };
    }

    /**
     * 标记 Boss 身份与奖励倍率。
     */
    private static void markBoss(Mob boss, double rewardMultiplier) {
        CompoundTag data = boss.getPersistentData();
        CompoundTag bossTag = new CompoundTag();
        bossTag.putBoolean(BossRewardData.IS_BOSS_KEY, true);
        bossTag.putDouble(BossRewardData.REWARD_MULTIPLIER_KEY, Math.max(1.0d, rewardMultiplier));
        data.put(BossRewardData.ROOT_KEY, bossTag);
    }

    /**
     * 计算合适的生成位置，优先攻击者附近，其次核心上方。
     */
    private static BlockPos resolveSpawnPosition(ServerLevel level, BastionData bastion, ServerPlayer attacker) {
        BlockPos attackerPos = attacker.blockPosition();
        BlockPos attackerSpawn = findGroundedPos(level, attackerPos);
        if (attackerSpawn != null && isWithinBastionRange(bastion, attackerSpawn)) {
            return attackerSpawn;
        }

        BlockPos corePos = bastion.corePos();
        BlockPos coreSpawn = findGroundedPos(level, corePos.above());
        if (coreSpawn != null) {
            return coreSpawn;
        }
        return corePos.above();
    }

    private static boolean isWithinBastionRange(BastionData bastion, BlockPos pos) {
        int radius = bastion.growthRadius();
        BlockPos core = bastion.corePos();
        return Math.abs(pos.getX() - core.getX()) <= radius
            && Math.abs(pos.getZ() - core.getZ()) <= radius;
    }

    /**
     * 寻找带地面的可生成位置：要求脚下为实心方块，脚/头位置为空气。
     */
    private static BlockPos findGroundedPos(ServerLevel level, BlockPos origin) {
        for (int dy = 0; dy <= BossConstants.SPAWN_HEIGHT_CHECK; dy++) {
            BlockPos check = origin.above(dy);
            BlockPos below = check.below();
            if (!level.getBlockState(below).isSolid()) {
                continue;
            }
            if (!level.getBlockState(check).isAir()) {
                continue;
            }
            if (!level.getBlockState(check.above(BossConstants.HEADROOM_CHECK)).isAir()) {
                continue;
            }
            return check;
        }
        return null;
    }
}
