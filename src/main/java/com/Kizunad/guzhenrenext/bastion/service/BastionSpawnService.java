package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.bastion.BastionDao;
import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.BastionState;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeConfig;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeManager;
import com.Kizunad.guzhenrenext.bastion.entity.BastionGuardianData;
import com.Kizunad.guzhenrenext.bastion.entity.BastionGuardianFactory;
import java.util.Random;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基地刷怪服务 - 处理基地领地内的怪物生成。
 * <p>
 * 基于设计文档的刷怪机制：
 * <ul>
 *   <li>仅在 ACTIVE 状态且有玩家附近时刷怪</li>
 *   <li>刷怪类型和强度基于道途类型和转数</li>
 *   <li>刷怪位置限制在基地节点范围内</li>
 * </ul>
 * </p>
 */
public final class BastionSpawnService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BastionSpawnService.class);

    private BastionSpawnService() {
    }

    // ===== 刷怪常量 =====

    /**
     * 非配置化的刷怪相关常量。
     */
    private static final class Constants {
        /** 刷怪位置搜索尝试次数。 */
        static final int SPAWN_POSITION_ATTEMPTS = 5;
        /** 刷怪搜索半径。 */
        static final int SPAWN_SEARCH_RADIUS = 16;
        /** 刷怪位置上方检查高度。 */
        static final int SPAWN_HEIGHT_CHECK = 2;
        /** 刷怪位置下方搜索深度。 */
        static final int SPAWN_DEPTH_SEARCH = 4;
        /** 守卫搜索范围垂直半径。 */
        static final int GUARDIAN_SEARCH_HEIGHT = 16;

        // ===== 刷怪上限配置 =====
        /** 单个基地最大守卫数量（基于转数缩放）。 */
        static final int BASE_PER_BASTION_CAP = 4;
        /** 每转数额外守卫上限。 */
        static final int PER_TIER_CAP_BONUS = 2;
        /** 全局最大守卫数量（整个维度）。 */
        static final int GLOBAL_GUARDIAN_CAP = 100;

        private Constants() {
        }
    }

    // ===== 主刷怪逻辑 =====

    /**
     * 尝试为指定基地执行刷怪。
     * <p>
     * 仅在基地处于 ACTIVE 状态且通过概率检查时执行。
     * 刷怪参数从 BastionTypeConfig 读取。
     * 包含单基地上限和全局上限检查，防止实体膨胀。
     * </p>
     *
     * @param level     服务端世界
     * @param savedData 基地存储数据（当前未使用，预留扩展）
     * @param bastion   基地数据
     * @param gameTime  当前游戏时间
     * @return 本次刷新的怪物数量
     */
    public static int trySpawn(
            ServerLevel level,
            BastionSavedData savedData,
            BastionData bastion,
            long gameTime) {
        // 仅 ACTIVE 状态可刷怪
        if (bastion.getEffectiveState(gameTime) != BastionState.ACTIVE) {
            return 0;
        }

        // 从配置读取刷怪参数 + upkeep（停机阈值）。
        BastionTypeConfig typeConfig = BastionTypeManager.getOrDefault(bastion.bastionType());
        BastionTypeConfig.SpawningConfig spawningConfig = typeConfig.spawning();

        // Round 4.1：停机门禁不再硬编码 resourcePool<=0，而是从 bastionType.upkeep.shutdown_threshold 读取。
        // 兼容策略：旧 JSON 缺失 upkeep 字段时，默认阈值为 0.0，可复现旧行为。
        double shutdownThreshold = typeConfig.upkeep().shutdownThreshold();
        if (bastion.resourcePool() <= shutdownThreshold) {
            return 0;
        }

        // ===== 上限检查 =====
        // 1. 全局上限检查
        int globalGuardianCount = countGlobalGuardians(level);
        if (globalGuardianCount >= Constants.GLOBAL_GUARDIAN_CAP) {
            LOGGER.debug("维度 {} 守卫数量已达全局上限 {}",
                level.dimension().location(), Constants.GLOBAL_GUARDIAN_CAP);
            return 0;
        }

        // 2. 单基地上限检查
        int bastionCap = calculateBastionCap(bastion);
        int bastionGuardianCount = countBastionGuardians(level, bastion);
        if (bastionGuardianCount >= bastionCap) {
            LOGGER.debug("基地 {} 守卫数量已达上限 {}", bastion.id(), bastionCap);
            return 0;
        }

        // 概率检查（使用配置参数）
        double spawnChance = calculateSpawnChance(bastion, spawningConfig);
        Random random = new Random(bastion.id().hashCode() ^ gameTime);
        if (random.nextDouble() > spawnChance) {
            return 0;
        }

        // 计算本次可刷新的数量（受上限约束）
        int remainingSlots = Math.min(
            bastionCap - bastionGuardianCount,
            Constants.GLOBAL_GUARDIAN_CAP - globalGuardianCount
        );
        int maxSpawns = Math.min(spawningConfig.maxSpawnsPerTick(), remainingSlots);

        int spawnedCount = 0;
        for (int i = 0; i < maxSpawns; i++) {
            BlockPos spawnPos = findSpawnPosition(level, savedData, bastion, random);
            if (spawnPos == null) {
                continue;
            }

            if (spawnGuardian(level, bastion, spawnPos)) {
                spawnedCount++;
            }
        }

        if (spawnedCount > 0) {
            LOGGER.debug("基地 {} 刷新了 {} 个怪物（当前: {}/{}）",
                bastion.id(), spawnedCount, bastionGuardianCount + spawnedCount, bastionCap);
        }

        return spawnedCount;
    }

    /**
     * 计算单个基地的守卫上限。
     *
     * @param bastion 基地数据
     * @return 守卫上限
     */
    private static int calculateBastionCap(BastionData bastion) {
        return Constants.BASE_PER_BASTION_CAP
            + (bastion.tier() - 1) * Constants.PER_TIER_CAP_BONUS;
    }

    /**
     * 统计指定基地范围内的守卫数量。
     * <p>
     * 使用 PersistentData 精确匹配基地 UUID，避免 tag 截断碰撞。
     * </p>
     *
     * @param level   服务端世界
     * @param bastion 基地数据
     * @return 守卫数量
     */
    private static int countBastionGuardians(ServerLevel level, BastionData bastion) {
        BlockPos core = bastion.corePos();
        int radius = bastion.growthRadius();

        AABB searchBox = new AABB(
            core.getX() - radius,
            core.getY() - Constants.GUARDIAN_SEARCH_HEIGHT,
            core.getZ() - radius,
            core.getX() + radius,
            core.getY() + Constants.GUARDIAN_SEARCH_HEIGHT,
            core.getZ() + radius
        );

        return (int) level.getEntitiesOfClass(Mob.class, searchBox,
            mob -> BastionGuardianData.belongsToBastion(mob, bastion.id())).size();
    }

    /**
     * 统计整个维度的守卫数量。
     * <p>
     * 使用通用标签快速过滤，避免遍历所有实体的 PersistentData。
     * 注意：此操作在大量实体时有性能开销，但每次刷怪检查只调用一次。
     * </p>
     *
     * @param level 服务端世界
     * @return 守卫数量
     */
    private static int countGlobalGuardians(ServerLevel level) {
        int count = 0;
        for (var entity : level.getAllEntities()) {
            if (BastionGuardianData.isGuardian(entity)) {
                count++;
                // 早期退出：如果已经达到上限，不需要继续计数
                if (count >= Constants.GLOBAL_GUARDIAN_CAP) {
                    break;
                }
            }
        }
        return count;
    }

    /**
     * 计算刷怪概率。
     *
     * @param bastion        基地数据
     * @param spawningConfig 刷怪配置
     * @return 刷怪概率（0.0 到 1.0）
     */
    private static double calculateSpawnChance(
            BastionData bastion,
            BastionTypeConfig.SpawningConfig spawningConfig) {
        double chance = spawningConfig.baseSpawnChance()
            + (bastion.tier() - 1) * spawningConfig.tierSpawnBonus();
        return Math.min(1.0, chance);
    }

    // ===== 刷怪位置选择 =====

    /**
     * 查找合适的刷怪位置。
     * <p>
     * 从菌毯节点缓存中采样位置，在菌毯上方寻找可站立空间。
     * 这确保怪物只在基地实际控制的区域（菌毯覆盖范围）刷新。
     * </p>
     *
     * @param level     服务端世界
     * @param savedData 基地存储数据
     * @param bastion   基地数据
     * @param random    随机源
     * @return 刷怪位置，如果没有合适位置则返回 null
     */
    private static BlockPos findSpawnPosition(
            ServerLevel level,
            BastionSavedData savedData,
            BastionData bastion,
            Random random) {
        // 从缓存中采样菌毯节点
        java.util.List<BlockPos> sampledNodes = savedData.sampleNodesFromCache(
            bastion.id(), Constants.SPAWN_POSITION_ATTEMPTS, random);

        if (sampledNodes.isEmpty()) {
            // 如果没有缓存节点，回退到核心位置
            BlockPos validPos = findValidSpawnHeightAboveNode(level, bastion.corePos(), bastion);
            return validPos;
        }

        // 对每个节点，向上搜索可站立空间
        for (BlockPos nodePos : sampledNodes) {
            BlockPos validPos = findValidSpawnHeightAboveNode(level, nodePos, bastion);
            if (validPos != null) {
                return validPos;
            }
        }
        return null;
    }

    /**
     * 在菌毯节点上方查找可站立的位置。
     * <p>
     * 节点本身是实心方块，从节点上方开始搜索。
     * </p>
     *
     * @param level   服务端世界
     * @param nodePos 菌毯节点位置
     * @param bastion 基地数据
     * @return 可站立位置，如果没有合适位置则返回 null
     */
    private static BlockPos findValidSpawnHeightAboveNode(ServerLevel level, BlockPos nodePos, BastionData bastion) {
        // 从节点上方 1 格开始向上搜索
        for (int dy = 1; dy <= Constants.SPAWN_HEIGHT_CHECK + 1; dy++) {
            BlockPos checkPos = nodePos.above(dy);
            if (isValidSpawnLocation(level, checkPos, bastion)) {
                return checkPos;
            }
        }
        return null;
    }

    /**
     * 检查给定的自然刷怪是否被领地道途允许。
     * <p>
     * 此方法应由自然刷怪事件调用（如 checkSpawn）。
     * </p>
     *
     * @param level 服务端世界
     * @param pos   刷怪位置
     * @param mob   尝试生成的实体
     * @return 如果允许生成返回 true，否则返回 false
     */
    public static boolean isNaturalSpawnAllowed(ServerLevel level, BlockPos pos, Mob mob) {
        BastionSavedData savedData = BastionSavedData.get(level);
        // 获取所在区块的领地归属
        long chunkKey = new ChunkPos(pos).toLong();
        UUID ownerId = savedData.getTerritoryOwner(chunkKey);

        if (ownerId == null) {
            // 无领地，不干预
            return true;
        }

        BastionData bastion = savedData.getBastion(ownerId);
        if (bastion == null) {
            return true;
        }

        // 仅在 ACTIVE 状态下生效？或者一直生效？
        // 假设领地影响一直存在（只要领地还在）。
        
        return isMobTypeAllowedByDao(bastion.primaryDao(), mob.getType().getCategory());
    }

    /**
     * 根据道途判断实体类型是否允许。
     */
    private static boolean isMobTypeAllowedByDao(BastionDao dao, MobCategory category) {
        switch (dao) {
            case ZHI_DAO -> {
                // 智道：允许水生，拒绝怪物
                // ZHI (Wisdom/Water): Allow Water/Ambient. Deny Monster.
                if (category == MobCategory.MONSTER) {
                    return false;
                }
                return true;
            }
            case HUN_DAO -> {
                // 魂道：允许怪物，拒绝动物
                // HUN (Soul): Allow Monster. Deny Animal.
                if (category == MobCategory.CREATURE || category == MobCategory.AMBIENT
                        || category == MobCategory.WATER_CREATURE) {
                    return false;
                }
                return true;
            }
            case MU_DAO -> {
                // 木道：允许动物，拒绝怪物
                // MU (Wood/Life): Allow Animal. Deny Monster.
                if (category == MobCategory.MONSTER) {
                    return false;
                }
                return true;
            }
            case LI_DAO -> {
                // 力道：允许怪物
                // LI (Strength): Allow Monster.
                // 暂不拒绝其他类型，或者也可以像魂道一样倾向于战斗。
                // 保持默认行为。
                return true;
            }
            default -> {
                return true;
            }
        }
    }

    /**
     * 检查位置是否适合刷怪。
     * <p>
     * 加入道途特化过滤逻辑：
     * <ul>
     *   <li>智道：允许在水中刷怪。</li>
     * </ul>
     * </p>
     */
    private static boolean isValidSpawnLocation(ServerLevel level, BlockPos pos, BastionData bastion) {
        BlockState feetState = level.getBlockState(pos);
        BlockState groundState = level.getBlockState(pos.below());
        BlockState headState = level.getBlockState(pos.above());

        // 智道特化：允许在水中生成
        if (bastion.primaryDao() == BastionDao.ZHI_DAO) {
            FluidState fluid = feetState.getFluidState();
            // 如果脚下是水（且头部是水或空气），且地面是固体（或海底）
            if (fluid.is(net.minecraft.tags.FluidTags.WATER)) {
                // 简单放宽：只要是在水里就算合法（假设生成的守卫能适应水）
                return groundState.isSolid() || groundState.getFluidState().is(net.minecraft.tags.FluidTags.WATER);
            }
        }

        // 默认逻辑：实心地面 + 两格空气
        return groundState.isSolid()
            && feetState.isAir()
            && headState.isAir();
    }

    // ===== 守卫生成 =====

    /**
     * 使用 BastionGuardianFactory 生成守卫实体。
     * <p>
     * 如果 CustomNPC 系统可用，生成 NPC；否则生成原版怪物。
     * 生成的守卫通过 PersistentData 存储完整的基地 UUID，确保精确归属。
     * </p>
     *
     * @param level   服务端世界
     * @param bastion 基地数据
     * @param pos     生成位置
     * @return 是否成功生成
     */
    private static boolean spawnGuardian(
            ServerLevel level,
            BastionData bastion,
            BlockPos pos) {
        Mob guardian = BastionGuardianFactory.createGuardian(level, bastion, pos);
        if (guardian == null) {
            return false;
        }

        // 使用 BastionGuardianData 标记守卫归属（通用标签 + PersistentData 完整 UUID + 转数）
        BastionGuardianData.markAsGuardian(guardian, bastion.id(), bastion.tier());

        // 如果基地已被占领，将守卫也标记为属于占领者
        if (bastion.isCaptured() && bastion.captureState() != null
            && bastion.captureState().capturedBy() != null) {
            BastionGuardianData.markAsCaptured(guardian, bastion.captureState().capturedBy());
        }

        return level.addFreshEntity(guardian);
    }
}
