package com.Kizunad.guzhenrenext.bastion.entity;

import com.Kizunad.guzhenrenext.bastion.BastionDao;
import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeConfig;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeConfig.BossConfig;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeManager;
import com.Kizunad.guzhenrenext.bastion.guardian.BastionGuardianEntities;
import com.Kizunad.guzhenrenext.bastion.guardian.BastionGuardianStatsService;
import com.Kizunad.guzhenrenext.bastion.service.BastionThreatService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基地守卫工厂 - 根据基地类型配置创建守卫实体。
 * <p>
 * 当前版本：守卫生成完全切换为“自定义守卫实体”（复用原版模型/动画），
 * 不再走 CustomNPC 反射路径，也不再生成原版实体作为基地守卫。</p>
 */
public final class BastionGuardianFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(BastionGuardianFactory.class);

    private BastionGuardianFactory() {
        // 工具类
    }

    // ===== 属性配置常量 =====

    /**
     * 守卫属性配置。
     */
    private static final class GuardianStats {
        // 生成参数
        static final double BLOCK_CENTER_OFFSET = 0.5;
        static final float FULL_ROTATION_DEGREES = 360.0f;

        static final int MIN_TIER = 1;

        private GuardianStats() {
        }
    }

    // ===== 公开 API =====

    /**
     * 为指定基地创建一个守卫实体。
     * <p>
     * 当前版本固定创建“自定义守卫实体”。
     * </p>
     *
     * @param level    服务端世界
     * @param bastion  基地数据
     * @param spawnPos 生成位置
     * @return 已配置的守卫实体，如果创建失败则返回 null
     */
    public static Mob createGuardian(
            ServerLevel level,
            BastionData bastion,
            BlockPos spawnPos) {
        // 需求变更：守卫生成完全切换为“自定义守卫实体”（复用原版模型/动画）。
        // CustomNPC/原版回退逻辑仅保留历史兼容代码，不再作为基地守卫来源。
        return createCustomGuardian(level, bastion, spawnPos);
    }

    // ===== 自定义守卫创建（主路径） =====

    private static Mob createCustomGuardian(
            ServerLevel level,
            BastionData bastion,
            BlockPos spawnPos) {
        // Warden 精英上限：每基地最多 1 只
        if (hasEliteWarden(level, bastion.id(), bastion.corePos())) {
            if (isWardenOnlyCandidate(bastion.bastionType(), bastion.tier())) {
                return null;
            }
        }

        int tier = Math.max(GuardianStats.MIN_TIER, bastion.tier());
        EntityType<? extends Mob> entityType = selectGuardianTypeFromConfig(
            level, bastion.bastionType(), tier, bastion.primaryDao(), bastion.id(), bastion.corePos()
        );
        Mob entity = entityType.create(level);
        if (entity == null) {
            return null;
        }

        entity.moveTo(
            spawnPos.getX() + GuardianStats.BLOCK_CENTER_OFFSET,
            spawnPos.getY(),
            spawnPos.getZ() + GuardianStats.BLOCK_CENTER_OFFSET,
            level.random.nextFloat() * GuardianStats.FULL_ROTATION_DEGREES,
            0.0f
        );

        entity.finalizeSpawn(
            level,
            level.getCurrentDifficultyAt(spawnPos),
            MobSpawnType.SPAWNER,
            null
        );
        entity.setPersistenceRequired();

        // 立即应用高强度属性，避免首次 tick 前仍使用原版数值。
        boolean elite = entity.getType() == BastionGuardianEntities.BASTION_WARDEN.get();
        boolean boss = entity.getType() == BastionGuardianEntities.BASTION_RAVAGER.get();
        double attributeMultiplier = 1.0;
        double rewardMultiplier = 1.0;
        if (boss) {
            attributeMultiplier = resolveBossAttributeMultiplier(bastion);
            rewardMultiplier = resolveBossRewardMultiplier(bastion);
            markBoss(entity, rewardMultiplier);
        }

        BastionGuardianStatsService.applyGuardianStats(entity, bastion.primaryDao(), tier, elite);
        if (boss && attributeMultiplier != 1.0d) {
            applyBossAttributeMultiplier(entity, attributeMultiplier);
        }
        entity.setHealth(entity.getMaxHealth());
        return entity;
    }

    /**
     * 根据威胁等级和 Boss 配置确定属性倍率。
     * <p>
     * 规则：找到 threat_multipliers 中 threatLevel 小于等于当前等级的最大项，
     * 若无匹配则回退 1.0。
     * </p>
     */
    private static double resolveBossAttributeMultiplier(BastionData bastion) {
        BossConfig bossConfig = BastionTypeManager.getOrDefault(bastion.bastionType()).boss();
        if (bossConfig == null || bossConfig.threatMultipliers() == null || bossConfig.threatMultipliers().isEmpty()) {
            return 1.0d;
        }
        int level = mapThreatTier(BastionThreatService.getThreatTier(bastion));
        double best = 1.0d;
        for (BossConfig.ThreatMultiplier multiplier : bossConfig.threatMultipliers()) {
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
     * 根据威胁等级和 Boss 配置确定奖励倍率（掉落）。
     * <p>
     * 规则同属性倍率：取满足阈值的最大 rewardMultiplier，默认 1.0。
     * </p>
     */
    private static double resolveBossRewardMultiplier(BastionData bastion) {
        BossConfig bossConfig = BastionTypeManager.getOrDefault(bastion.bastionType()).boss();
        if (bossConfig == null || bossConfig.threatMultipliers() == null || bossConfig.threatMultipliers().isEmpty()) {
            return 1.0d;
        }
        int level = mapThreatTier(BastionThreatService.getThreatTier(bastion));
        double best = 1.0d;
        for (BossConfig.ThreatMultiplier multiplier : bossConfig.threatMultipliers()) {
            if (multiplier == null) {
                continue;
            }
            if (level >= multiplier.threatLevel()) {
                best = Math.max(best, multiplier.rewardMultiplier());
            }
        }
        return best;
    }

    /**
     * 把威胁等级映射为整数等级，方便比较。
     */
    private static int mapThreatTier(BastionThreatService.ThreatTier tier) {
        return switch (tier) {
            case HIGH -> ThreatLevelConstants.THREAT_LEVEL_HIGH;
            case MEDIUM -> ThreatLevelConstants.THREAT_LEVEL_MEDIUM;
            case LOW -> ThreatLevelConstants.THREAT_LEVEL_LOW;
            default -> ThreatLevelConstants.THREAT_LEVEL_NONE;
        };
    }

    /**
     * 威胁等级常量，避免 MagicNumber。
     */
    private static final class ThreatLevelConstants {
        private static final int THREAT_LEVEL_NONE = 0;
        private static final int THREAT_LEVEL_LOW = 1;
        private static final int THREAT_LEVEL_MEDIUM = 2;
        private static final int THREAT_LEVEL_HIGH = 3;

        private ThreatLevelConstants() {
        }
    }

    /**
     * 为 Boss 标记奖励倍率，供掉落事件读取。
     */
    private static void markBoss(Mob boss, double rewardMultiplier) {
        CompoundTag data = boss.getPersistentData();
        CompoundTag bossTag = new CompoundTag();
        bossTag.putBoolean(BossRewardData.IS_BOSS_KEY, true);
        bossTag.putDouble(BossRewardData.REWARD_MULTIPLIER_KEY, rewardMultiplier);
        data.put(BossRewardData.ROOT_KEY, bossTag);
    }

    /**
     * 按倍率增强 Boss 的关键属性，保持健康上限同步。
     */
    private static void applyBossAttributeMultiplier(Mob boss, double multiplier) {
        scaleAttribute(boss, Attributes.MAX_HEALTH, multiplier);
        scaleAttribute(boss, Attributes.ATTACK_DAMAGE, multiplier);
        scaleAttribute(boss, Attributes.ARMOR, multiplier);
        scaleAttribute(boss, Attributes.ARMOR_TOUGHNESS, multiplier);
        if (boss.getHealth() > boss.getMaxHealth()) {
            boss.setHealth(boss.getMaxHealth());
        }
    }

    private static void scaleAttribute(
            Mob mob,
            net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
            double multiplier) {
        AttributeInstance instance = mob.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        instance.setBaseValue(instance.getBaseValue() * multiplier);
    }

    private static boolean isWardenOnlyCandidate(String bastionType, int tier) {
        BastionTypeConfig config = BastionTypeManager.getOrDefault(bastionType);
        List<BastionTypeConfig.EntityWeight> eligible = config.spawning().entityWeights().stream()
            .filter(w -> w.minTier() <= tier)
            .filter(w -> w.weight() > GuardianSpawnConstants.MIN_WEIGHT)
            .toList();

        // 如果配置为空或没有正权重候选，视为不是“warden-only”。
        if (eligible.isEmpty()) {
            return false;
        }

        long nonWardenCount = eligible.stream()
            .filter(w -> !GuardianEntityIds.MINECRAFT_WARDEN.equals(w.entityType()))
            .count();
        return nonWardenCount == 0;
    }

    // ===== 配置驱动的自定义实体选择 =====

    /**
     * 从 bastion_type 配置中按权重选择“自定义守卫实体类型”。
     */
    private static EntityType<? extends Mob> selectGuardianTypeFromConfig(
            ServerLevel level,
            String bastionType,
            int tier,
            BastionDao fallbackDao,
            UUID bastionId,
            BlockPos corePos) {
        BastionTypeConfig config = BastionTypeManager.getOrDefault(bastionType);
        List<BastionTypeConfig.EntityWeight> weights = config.spawning().entityWeights();

        List<BastionTypeConfig.EntityWeight> eligibleWeights = weights.stream()
            .filter(w -> w.minTier() <= tier)
            .toList();

        // Warden 精英上限（每基地最多 1 只）：如果已存在，则把 warden 从候选里移除。
        if (hasEliteWarden(level, bastionId, corePos)) {
            eligibleWeights = eligibleWeights.stream()
                .filter(w -> !GuardianEntityIds.MINECRAFT_WARDEN.equals(w.entityType()))
                .toList();
        }

        if (eligibleWeights.isEmpty()) {
            return selectFallbackGuardianType(fallbackDao);
        }

        int totalWeight = eligibleWeights.stream()
            .mapToInt(BastionTypeConfig.EntityWeight::weight)
            .sum();
        if (totalWeight <= 0) {
            return selectFallbackGuardianType(fallbackDao);
        }

        int roll = level.random.nextInt(totalWeight);
        int cumulative = 0;
        for (BastionTypeConfig.EntityWeight entry : eligibleWeights) {
            cumulative += entry.weight();
            if (roll < cumulative) {
                Optional<EntityType<? extends Mob>> resolved = resolveCustomGuardianEntityType(entry.entityType());
                if (resolved.isPresent()) {
                    return resolved.get();
                }
                LOGGER.warn("无法解析守卫外观 {}，使用回退映射", entry.entityType());
                return selectFallbackGuardianType(fallbackDao);
            }
        }

        return selectFallbackGuardianType(fallbackDao);
    }

    private static Optional<EntityType<? extends Mob>> resolveCustomGuardianEntityType(String entityTypeId) {
        if (entityTypeId == null || entityTypeId.isBlank()) {
            return Optional.empty();
        }

        return switch (entityTypeId) {
            case GuardianEntityIds.MINECRAFT_WITCH ->
                Optional.of(BastionGuardianEntities.BASTION_WITCH.get());
            case GuardianEntityIds.MINECRAFT_EVOKER ->
                Optional.of(BastionGuardianEntities.BASTION_EVOKER.get());
            case GuardianEntityIds.MINECRAFT_ILLUSIONER ->
                Optional.of(BastionGuardianEntities.BASTION_ILLUSIONER.get());

            case GuardianEntityIds.MINECRAFT_PHANTOM ->
                Optional.of(BastionGuardianEntities.BASTION_PHANTOM.get());
            case GuardianEntityIds.MINECRAFT_VEX ->
                Optional.of(BastionGuardianEntities.BASTION_VEX.get());
            case GuardianEntityIds.MINECRAFT_WITHER_SKELETON ->
                Optional.of(BastionGuardianEntities.BASTION_WITHER_SKELETON.get());

            case GuardianEntityIds.MINECRAFT_VINDICATOR ->
                Optional.of(BastionGuardianEntities.BASTION_VINDICATOR.get());
            case GuardianEntityIds.MINECRAFT_PILLAGER ->
                Optional.of(BastionGuardianEntities.BASTION_PILLAGER.get());
            case GuardianEntityIds.MINECRAFT_IRON_GOLEM ->
                Optional.of(BastionGuardianEntities.BASTION_IRON_GOLEM.get());

            case GuardianEntityIds.MINECRAFT_RAVAGER ->
                Optional.of(BastionGuardianEntities.BASTION_RAVAGER.get());
            case GuardianEntityIds.MINECRAFT_HOGLIN ->
                Optional.of(BastionGuardianEntities.BASTION_HOGLIN.get());
            case GuardianEntityIds.MINECRAFT_WARDEN ->
                Optional.of(BastionGuardianEntities.BASTION_WARDEN.get());

            default -> Optional.empty();
        };
    }

    private static EntityType<? extends Mob> selectFallbackGuardianType(BastionDao dao) {
        return switch (dao) {
            case ZHI_DAO -> BastionGuardianEntities.BASTION_WITCH.get();
            case HUN_DAO -> BastionGuardianEntities.BASTION_PHANTOM.get();
            case MU_DAO -> BastionGuardianEntities.BASTION_VINDICATOR.get();
            case LI_DAO -> BastionGuardianEntities.BASTION_RAVAGER.get();
        };
    }

    private static boolean hasEliteWarden(ServerLevel level, UUID bastionId, BlockPos corePos) {
        int radius = GuardianSpawnConstants.ELITE_SEARCH_RADIUS;
        AABB box = new AABB(
            corePos.getX() - radius,
            corePos.getY() - GuardianSpawnConstants.ELITE_SEARCH_HEIGHT,
            corePos.getZ() - radius,
            corePos.getX() + radius,
            corePos.getY() + GuardianSpawnConstants.ELITE_SEARCH_HEIGHT,
            corePos.getZ() + radius
        );

        return !level.getEntitiesOfClass(
            Mob.class,
            box,
            mob -> mob.getType() == BastionGuardianEntities.BASTION_WARDEN.get()
                && BastionGuardianData.belongsToBastion(mob, bastionId)
                && mob.isAlive()
        ).isEmpty();
    }

    private static final class GuardianEntityIds {
        static final String MINECRAFT_WITCH = "minecraft:witch";
        static final String MINECRAFT_EVOKER = "minecraft:evoker";
        static final String MINECRAFT_ILLUSIONER = "minecraft:illusioner";

        static final String MINECRAFT_PHANTOM = "minecraft:phantom";
        static final String MINECRAFT_VEX = "minecraft:vex";
        static final String MINECRAFT_WITHER_SKELETON = "minecraft:wither_skeleton";

        static final String MINECRAFT_VINDICATOR = "minecraft:vindicator";
        static final String MINECRAFT_PILLAGER = "minecraft:pillager";
        static final String MINECRAFT_IRON_GOLEM = "minecraft:iron_golem";

        static final String MINECRAFT_RAVAGER = "minecraft:ravager";
        static final String MINECRAFT_HOGLIN = "minecraft:hoglin";
        static final String MINECRAFT_WARDEN = "minecraft:warden";

        private GuardianEntityIds() {
        }
    }

    private static final class GuardianSpawnConstants {
        static final int ELITE_SEARCH_RADIUS = 96;
        static final int ELITE_SEARCH_HEIGHT = 24;

        static final int MIN_WEIGHT = 0;

        private GuardianSpawnConstants() {
        }
    }
}
