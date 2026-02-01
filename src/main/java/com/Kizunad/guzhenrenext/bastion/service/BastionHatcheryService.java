package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionBlocks;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.block.BastionAnchorBlock;
import com.Kizunad.guzhenrenext.bastion.block.BastionGuardianHatcheryBlock;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeConfig;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeManager;
import com.Kizunad.guzhenrenext.bastion.entity.BastionGuardianData;
import com.Kizunad.guzhenrenext.bastion.guardian.BastionGuardianEntities;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

/**
 * 守卫孵化巢（GuardianHatchery）服务。
 * <p>
 * Round 4.2 的最小目标：
 * <ul>
 *     <li>在资源池足够时，按配置周期性产出守卫（minion/ranged/support 按比例抽取）</li>
 *     <li>资源不足时停机/延后，不刷屏（仅更新下一次尝试 tick）</li>
 *     <li>遵守每基地最大存活数（maxAlive）</li>
 *     <li>冷却为“基地级”（同基地多个孵化巢共用节流）</li>
 * </ul>
 * </p>
 * <p>
 * 注意：本回合优先“不引入 BlockEntity”，冷却/下一次尝试时间保存在 {@link BastionSavedData} 的运行时缓存中。
 * 这意味着服务器重启后冷却会重置，但不会破坏长期存档结构；后续回合可迁移到持久化 SavedData。
 * </p>
 */
public final class BastionHatcheryService {

    private BastionHatcheryService() {
    }

    /** 非配置化技术常量（计数/范围/回退策略）。 */
    private static final class Constants {
        /** 守卫计数垂直搜索半径（与 SpawnService/UpkeepService 保持一致）。 */
        static final int GUARDIAN_SEARCH_HEIGHT = 16;

        /**
         * 查找孵化巢方块的水平半径。
         * <p>
         * Round 4.2 先做“最小可运行逻辑”：直接以核心为中心做范围扫描。
         * 后续回合可把孵化巢挂载到 Anchor 缓存/连通网络，以避免 O(r^3) 扫描。</p>
         */
        static final int HATCHERY_SCAN_RADIUS = 24;

        /**
         * 当资源不足时的回退延后 tick（避免每秒都尝试导致日志/性能抖动）。
         */
        static final long INSUFFICIENT_RESOURCE_BACKOFF_TICKS = 40L;

        /**
         * 当未找到任何孵化巢时的回退延后 tick（避免空扫描每秒重复）。
         */
        static final long NO_HATCHERY_BACKOFF_TICKS = 100L;

        /** 孵化巢扫描时的垂直窗口（核心上下各 N 格）。 */
        static final int HATCHERY_SCAN_VERTICAL = 8;

        /** 查找孵化巢所属基地的最大搜索半径（与 EnergyBuildService 对齐）。 */
        static final int MAX_OWNER_SEARCH_RADIUS = 128;

        private Constants() {
        }
    }

    /**
     * 由 BastionTicker 在 ACTIVE + FULL tick 中驱动。
     *
     * @param level    服务端世界
     * @param savedData 存储数据（用于读取/写入基地数据与运行时冷却）
     * @param bastion  基地数据
     * @param gameTime 当前游戏时间
     */
    public static void tick(ServerLevel level, BastionSavedData savedData, BastionData bastion, long gameTime) {
        if (level == null || savedData == null || bastion == null) {
            return;
        }

        BastionTypeConfig typeConfig = BastionTypeManager.getOrDefault(bastion.bastionType());
        BastionTypeConfig.HatcheryConfig hatchery = typeConfig.hatchery();

        // 兼容策略：缺省 hatchery 为未启用，旧 JSON 不改变行为。
        if (hatchery == null || !hatchery.enabled()) {
            return;
        }

        // Gate 0：冷却（基地级）。
        long nextAllowed = savedData.getNextHatcheryTryTick(bastion.id());
        if (gameTime < nextAllowed) {
            return;
        }

        // 先把“下一次允许尝试”的 tick 设为冷却结束（避免本 tick 内多次进入）。
        long cooldown = Math.max(1L, hatchery.cooldownTicks());
        savedData.setNextHatcheryTryTick(bastion.id(), gameTime + cooldown);

        // Gate 1：maxAlive（<=0 表示不产出）。
        int maxAlive = Math.max(0, hatchery.maxAlive());
        if (maxAlive <= 0) {
            return;
        }

        int currentAlive = countBastionGuardians(level, bastion);
        if (currentAlive >= maxAlive) {
            return;
        }

        // Gate 2：确保当前基地附近存在至少一个孵化巢。
        // Round 4.2：用范围扫描做最小实现。
        BlockPos hatcheryPos = findAnyHatcheryNearCore(level, savedData, bastion);
        if (hatcheryPos == null) {
            // 空扫描回退：避免每秒都做范围扫描。
            savedData.setNextHatcheryTryTick(bastion.id(), gameTime + Constants.NO_HATCHERY_BACKOFF_TICKS);
            return;
        }

        // Gate 3：资源池检查。
        // 注意：本回合不复用 upkeep.shutdown_threshold，而是使用 hatchery 自己的 costPerSpawn。
        double costPerSpawn = Math.max(0.0, hatchery.costPerSpawn());
        if (bastion.resourcePool() < costPerSpawn) {
            // 资源不足：延后，不刷屏。
            savedData.setNextHatcheryTryTick(bastion.id(), gameTime + Constants.INSUFFICIENT_RESOURCE_BACKOFF_TICKS);
            return;
        }

        // 计算本周期最多产出数量（受 spawnPerCycle + maxAlive 上限约束）。
        int spawnPerCycle = Math.max(0, hatchery.spawnPerCycle());
        int remainingSlots = maxAlive - currentAlive;
        int toSpawn = Math.min(spawnPerCycle, remainingSlots);
        if (toSpawn <= 0) {
            return;
        }

        // 为避免“扣费成功但生成失败”导致资源白扣，这里按每次成功生成后再扣费。
        // 但为了让扣费能持久写入，我们每次成功后 updateBastion。
        BastionData current = bastion;
        Random random = new Random(bastion.id().hashCode() ^ gameTime);

        for (int i = 0; i < toSpawn; i++) {
            if (current.resourcePool() < costPerSpawn) {
                // 本周期中途资源用尽：停机，延后。
                savedData.setNextHatcheryTryTick(
                    bastion.id(),
                    gameTime + Constants.INSUFFICIENT_RESOURCE_BACKOFF_TICKS
                );
                break;
            }

            EntityType<? extends Mob> type = chooseGuardianType(hatchery.weights(), random);
            if (type == null) {
                // 权重全为 0：视为未启用。
                break;
            }

            // 生成位置：优先孵化巢上方 1 格（保证可见/可理解）；如果不适合，则回退为核心上方。
            BlockPos spawnPos = hatcheryPos.above();
            if (!isAirTwoBlocks(level, spawnPos)) {
                spawnPos = current.corePos().above();
            }
            if (!isAirTwoBlocks(level, spawnPos)) {
                // 找不到安全位置：本周期跳过，不刷屏。
                continue;
            }

            Mob mob = type.create(level);
            if (mob == null) {
                continue;
            }

            mob.moveTo(
                spawnPos.getX() + SpawnConstants.BLOCK_CENTER_OFFSET,
                spawnPos.getY(),
                spawnPos.getZ() + SpawnConstants.BLOCK_CENTER_OFFSET,
                level.random.nextFloat() * SpawnConstants.FULL_ROTATION_DEGREES,
                0.0f
            );
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.SPAWNER, null);
            mob.setPersistenceRequired();

            // Round 4.2 的“机制地基”要求：孵化巢产出的守卫也应立即拥有基地强度，而不是等到首次 tick。
            // BastionGuardianFactory 在 createCustomGuardian 内部会做一次 applyGuardianStats，但这里我们直接创建 EntityType。
            // 因此需要在生成后主动补一遍，避免瞬间弱鸡。
            applyBaseGuardianStatsIfPossible(mob, current);

            // 标记归属（用于计数/规则/技能等）。
            BastionGuardianData.markAsGuardian(mob, current.id(), current.tier());

            boolean added = level.addFreshEntity(mob);
            if (!added) {
                continue;
            }

            // 生成成功后扣费并落盘。
            current = current.withResourcePool(Math.max(0.0, current.resourcePool() - costPerSpawn));
            savedData.updateBastion(current);

            // 基于成功生成次数进行“局部”上限收敛：避免同 tick 内多次全量计数。
            currentAlive++;
            if (currentAlive >= maxAlive) {
                break;
            }
        }
    }

    /**
     * Round 4.2.1：孵化巢自动生成 v1（基于菌毯数量阈值）。
     * <p>
     * 当菌毯数量达到配置阈值后，若基地仍未拥有任何孵化巢，则尝试在一个“属于该基地”的 Anchor 上方
     * 放置 {@link BastionBlocks#BASTION_GUARDIAN_HATCHERY}。
     * </p>
     * <p>
     * 说明：
     * <ul>
     *     <li>该方法只负责“生成方块”，不负责刷怪/扣费/冷却；后者由 {@link #tick} 统一驱动。</li>
     *     <li>为保证 tick 能在 v1 的“核心附近扫描”范围内找到孵化巢，这里优先选择核心附近的 Anchor。</li>
     *     <li>若暂时找不到合适 Anchor/位置不可替换，则静默返回，下次 FULL tick 继续尝试。</li>
     * </ul>
     * </p>
     */
    public static void tryAutoSpawnHatchery(
            ServerLevel level,
            BastionSavedData savedData,
            BastionData bastion,
            long gameTime) {
        if (level == null || savedData == null || bastion == null) {
            return;
        }

        BastionTypeConfig typeConfig = BastionTypeManager.getOrDefault(bastion.bastionType());
        BastionTypeConfig.HatcheryConfig hatchery = typeConfig.hatchery();

        // Gate 0：必须启用孵化巢系统。
        if (hatchery == null || !hatchery.enabled()) {
            return;
        }

        // Gate 1：菌毯数量阈值。
        int threshold = Math.max(0, hatchery.autoSpawnThreshold());
        if (bastion.totalMycelium() < threshold) {
            return;
        }

        // Gate 2：基地已经存在孵化巢则不重复生成。
        if (findAnyHatcheryNearCore(level, savedData, bastion) != null) {
            return;
        }

        // Gate 3：选择一个合适的 Anchor 位置。
        BlockPos anchorPos = findAnchorForAutoSpawn(level, savedData, bastion);
        if (anchorPos == null) {
            return;
        }

        BlockPos hatcheryPos = anchorPos.above();
        if (!level.isLoaded(hatcheryPos)) {
            return;
        }

        BlockState currentState = level.getBlockState(hatcheryPos);
        if (!currentState.canBeReplaced()) {
            return;
        }

        // 放置孵化巢方块。
        BlockState hatcheryState = BastionBlocks.BASTION_GUARDIAN_HATCHERY.get().defaultBlockState();
        level.setBlock(hatcheryPos, hatcheryState, Block.UPDATE_ALL);
    }

    /**
     * 为孵化巢自动生成选择一个 Anchor。
     * <p>
     * v1 约束：孵化巢在 tick 中采用“以核心为中心的范围扫描”查找，因此这里尽量选择核心附近的 Anchor。
     * </p>
     */
    private static BlockPos findAnchorForAutoSpawn(ServerLevel level, BastionSavedData savedData, BastionData bastion) {
        if (level == null || savedData == null || bastion == null) {
            return null;
        }

        BlockPos corePos = bastion.corePos();

        // 1) 优先使用运行时 anchorCache（成本最低），但要校验方块仍存在且归属正确。
        BlockPos fromCache = chooseBestAnchorFromIterable(
            level,
            savedData,
            bastion,
            savedData.getAnchors(bastion.id()),
            corePos
        );
        if (fromCache != null) {
            return fromCache;
        }

        // 2) 后备：在核心附近做范围扫描。
        // 说明：用于服务器重启后 anchorCache 为空、但世界中仍存在 Anchor 的情况。
        int r = Math.max(1, Constants.HATCHERY_SCAN_RADIUS);
        int vertical = Math.max(0, Constants.HATCHERY_SCAN_VERTICAL);

        BlockPos best = null;
        double bestDistSq = Double.MAX_VALUE;
        boolean bestHasSpawnSpace = false;

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                for (int dy = -vertical; dy <= vertical; dy++) {
                    BlockPos pos = corePos.offset(dx, dy, dz);
                    if (!level.isLoaded(pos)) {
                        continue;
                    }

                    BlockState state = level.getBlockState(pos);
                    if (!(state.getBlock() instanceof BastionAnchorBlock)) {
                        continue;
                    }

                    // 重要：孵化巢会放在 Anchor 上方 1 格，因此必须保证“孵化巢方块位置”能被后续
                    // findAnyHatcheryNearCore 的扫描窗口覆盖，否则会导致“放了但找不到”。
                    if (!isWithinCoreScanWindow(corePos, pos, r, vertical)) {
                        continue;
                    }

                    if (!isAnchorOwnedByBastion(savedData, bastion, pos)) {
                        continue;
                    }

                    if (!canPlaceHatcheryAbove(level, pos)) {
                        continue;
                    }

                    boolean hasSpawnSpace = isAirTwoBlocks(level, pos.above(2));
                    double distSq = corePos.distSqr(pos);

                    // 优先级：可生成空间（孵化巢上方两格空气）优先，其次选离核心更近的。
                    if (best == null
                        || (hasSpawnSpace && !bestHasSpawnSpace)
                        || (hasSpawnSpace == bestHasSpawnSpace && distSq < bestDistSq)) {
                        best = pos;
                        bestDistSq = distSq;
                        bestHasSpawnSpace = hasSpawnSpace;
                    }
                }
            }
        }

        return best;
    }

    private static BlockPos chooseBestAnchorFromIterable(
            ServerLevel level,
            BastionSavedData savedData,
            BastionData bastion,
            Iterable<BlockPos> anchors,
            BlockPos corePos) {
        if (anchors == null) {
            return null;
        }

        BlockPos best = null;
        double bestDistSq = Double.MAX_VALUE;
        boolean bestHasSpawnSpace = false;

        int r = Math.max(1, Constants.HATCHERY_SCAN_RADIUS);
        int vertical = Math.max(0, Constants.HATCHERY_SCAN_VERTICAL);

        for (BlockPos pos : anchors) {
            if (pos == null) {
                continue;
            }

            // 为保证 tick 的扫描能命中孵化巢，这里把候选限制在“核心附近窗口”。
            if (!isWithinCoreScanWindow(corePos, pos, r, vertical)) {
                continue;
            }

            if (!level.isLoaded(pos)) {
                continue;
            }

            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof BastionAnchorBlock)) {
                continue;
            }

            if (!isAnchorOwnedByBastion(savedData, bastion, pos)) {
                continue;
            }

            if (!canPlaceHatcheryAbove(level, pos)) {
                continue;
            }

            boolean hasSpawnSpace = isAirTwoBlocks(level, pos.above(2));
            double distSq = corePos.distSqr(pos);

            if (best == null
                || (hasSpawnSpace && !bestHasSpawnSpace)
                || (hasSpawnSpace == bestHasSpawnSpace && distSq < bestDistSq)) {
                best = pos;
                bestDistSq = distSq;
                bestHasSpawnSpace = hasSpawnSpace;
            }
        }

        return best;
    }

    /**
     * 判断 Anchor 是否归属于指定基地。
     * <p>
     * 注意：这里使用 {@link BastionSavedData#findOwnerBastion}，它会优先走持久化索引，
     * 并在索引缺失时 fallback 到“最近核心”规则。
     * </p>
     */
    private static boolean isAnchorOwnedByBastion(BastionSavedData savedData, BastionData bastion, BlockPos anchorPos) {
        BastionData owner = savedData.findOwnerBastion(anchorPos, Constants.MAX_OWNER_SEARCH_RADIUS);
        return owner != null && owner.id().equals(bastion.id());
    }

    private static boolean canPlaceHatcheryAbove(ServerLevel level, BlockPos anchorPos) {
        BlockPos hatcheryPos = anchorPos.above();
        if (!level.isLoaded(hatcheryPos)) {
            return false;
        }
        BlockState above = level.getBlockState(hatcheryPos);
        return above.canBeReplaced();
    }

    private static boolean isWithinCoreScanWindow(BlockPos corePos, BlockPos pos, int radius, int vertical) {
        int dx = Math.abs(pos.getX() - corePos.getX());
        int dz = Math.abs(pos.getZ() - corePos.getZ());
        if (dx > radius || dz > radius) {
            return false;
        }

        // y 维度用孵化巢方块的位置（Anchor 上方 1 格）做窗口约束。
        int hatcheryY = pos.getY() + 1;
        int dy = Math.abs(hatcheryY - corePos.getY());
        return dy <= vertical;
    }

    /**
     * 统计指定基地范围内属于该基地的守卫数量。
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
            mob -> BastionGuardianData.isGuardian(mob)
                && BastionGuardianData.belongsToBastion(mob, bastion.id())
        ).size();
    }

    /**
     * 在核心附近扫描任意一个孵化巢方块位置。
     */
    private static BlockPos findAnyHatcheryNearCore(
            ServerLevel level,
            BastionSavedData savedData,
            BastionData bastion) {
        if (level == null || savedData == null || bastion == null) {
            return null;
        }

        BlockPos corePos = bastion.corePos();
        int r = Math.max(1, Constants.HATCHERY_SCAN_RADIUS);
        int vertical = Math.max(0, Constants.HATCHERY_SCAN_VERTICAL);

        BastionTypeConfig typeConfig = BastionTypeManager.getOrDefault(bastion.bastionType());
        BastionTypeConfig.HatcheryConfig hatcheryConfig = typeConfig.hatchery();
        int proximityRadius = 0;
        if (hatcheryConfig != null) {
            proximityRadius = Math.max(0, hatcheryConfig.anchorProximityRadius());
        }

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                // y 方向只检查一个较小窗口：核心上下 N 格。
                for (int dy = -vertical; dy <= vertical; dy++) {
                    BlockPos pos = corePos.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (state.getBlock() instanceof BastionGuardianHatcheryBlock) {
                        // 归属约束：孵化巢必须挂在“属于该基地”的 Anchor 上。
                        // 解释：避免把邻近基地的 Anchor 结构误当作本基地孵化点。
                        BastionData owner = savedData.findOwnerBastion(pos.below(), Constants.MAX_OWNER_SEARCH_RADIUS);
                        if (owner != null && owner.id().equals(bastion.id())) {
                            // 连通性约束：孵化巢附近必须存在属于同基地的 Anchor（包含下方 Anchor）。
                            boolean hasNearbyAnchor = hasAnchorWithinRadius(
                                level,
                                savedData,
                                bastion,
                                pos,
                                proximityRadius
                            );
                            if (!hasNearbyAnchor) {
                                continue;
                            }
                            return pos;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 判断孵化巢附近是否存在“属于该基地”的 Anchor。
     * <p>
     * 说明：遍历以孵化巢为中心的立方体窗口，半径由配置给定，包含孵化巢下方的 Anchor。
     * </p>
     */
    private static boolean hasAnchorWithinRadius(
            ServerLevel level,
            BastionSavedData savedData,
            BastionData bastion,
            BlockPos hatcheryPos,
            int radius) {
        if (level == null || savedData == null || bastion == null || hatcheryPos == null) {
            return false;
        }

        // 需要包含“孵化巢下方的 Anchor”，因此最小半径取 1，避免半径为 0 时漏掉 dy=-1 的位置。
        int clampedRadius = Math.max(1, radius);
        for (int dx = -clampedRadius; dx <= clampedRadius; dx++) {
            for (int dz = -clampedRadius; dz <= clampedRadius; dz++) {
                for (int dy = -clampedRadius; dy <= clampedRadius; dy++) {
                    BlockPos candidate = hatcheryPos.offset(dx, dy, dz);
                    if (!level.isLoaded(candidate)) {
                        continue;
                    }

                    BlockState state = level.getBlockState(candidate);
                    if (!(state.getBlock() instanceof BastionAnchorBlock)) {
                        continue;
                    }

                    if (isAnchorOwnedByBastion(savedData, bastion, candidate)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isAirTwoBlocks(ServerLevel level, BlockPos pos) {
        if (level == null || pos == null) {
            return false;
        }
        return level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir();
    }

    /**
     * 对孵化巢产出的守卫，尽可能套用“基地强度”属性。
     * <p>
     * 说明：当前守卫强度系统依赖自定义守卫实体类拥有对应属性实例。
     * 若某些实体未注册某属性，这里必须容错并静默跳过。</p>
     */
    private static void applyBaseGuardianStatsIfPossible(Mob guardian, BastionData bastion) {
        if (guardian == null || bastion == null) {
            return;
        }

        int tier = Math.max(1, bastion.tier());

        // 与 BastionGuardianStatsService 的核心数值保持一致（Round 4.2 只做“最小一致性”）。
        double health = Stats.BASE_HEALTH + (tier - 1) * Stats.HEALTH_PER_TIER;
        double attack = Stats.BASE_ATTACK_TIER_1 * Math.pow(Stats.ATTACK_MULT_PER_TIER, tier - 1);
        double armor = Stats.BASE_ARMOR + (tier - 1) * Stats.ARMOR_PER_TIER;
        double toughness = Stats.BASE_TOUGHNESS + (tier - 1) * Stats.TOUGHNESS_PER_TIER;
        double range = Stats.BASE_FOLLOW_RANGE + (tier - 1) * Stats.FOLLOW_RANGE_PER_TIER;
        double speed = Math.min(Stats.MAX_SPEED, Stats.BASE_SPEED + (tier - 1) * Stats.SPEED_PER_TIER);
        double knock = Math.min(Stats.MAX_KNOCKBACK_RESIST, Stats.BASE_KNOCKBACK_RESIST
            + (tier - 1) * Stats.KNOCKBACK_RESIST_PER_TIER);

        // 注意：此处不做“道途/精英”细分，因为孵化巢只控制类别（minion/ranged/support），不控制 dao。
        // 后续回合若需要按 bastion.primaryDao 做特化，可迁移到 BastionGuardianStatsService 公共 API。

        setBaseValueIfPresent(guardian, Attributes.MAX_HEALTH, health);
        setBaseValueIfPresent(guardian, Attributes.ATTACK_DAMAGE, attack);
        setBaseValueIfPresent(guardian, Attributes.ARMOR, armor);
        setBaseValueIfPresent(guardian, Attributes.ARMOR_TOUGHNESS, toughness);
        setBaseValueIfPresent(guardian, Attributes.FOLLOW_RANGE, range);
        setBaseValueIfPresent(guardian, Attributes.MOVEMENT_SPEED, speed);
        setBaseValueIfPresent(guardian, Attributes.KNOCKBACK_RESISTANCE, knock);

        guardian.setHealth(guardian.getMaxHealth());
    }

    private static void setBaseValueIfPresent(
            Mob mob,
            net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr,
            double v) {
        AttributeInstance instance = mob.getAttribute(attr);
        if (instance == null) {
            return;
        }
        instance.setBaseValue(v);
    }

    /**
     * 按权重选择守卫类别，并映射到当前已有的“守卫实体类型”。
     * <p>
     * Round 4.2 只要求“类别权重随机”，并不要求配置到具体 entity id。
     * 因此这里使用固定映射：
     * <ul>
     *     <li>minion：Vindicator</li>
     *     <li>ranged：Pillager</li>
     *     <li>support：Witch</li>
     *     <li>shield_minion：BastionShieldGuardian</li>
     *     <li>berserker：BastionBerserkerGuardian</li>
     *     <li>archer：BastionArcherGuardian</li>
     *     <li>elite：Warden（默认权重为 0，不会出现）</li>
     *     <li>boss：Ravager（默认权重为 0，不会出现）</li>
     * </ul>
     * </p>
     */
    private static EntityType<? extends Mob> chooseGuardianType(
            BastionTypeConfig.GuardianWeights weights,
            Random random) {
        if (weights == null || random == null) {
            return null;
        }

        int wMinion = Math.max(0, weights.minion());
        int wRanged = Math.max(0, weights.ranged());
        int wSupport = Math.max(0, weights.support());
        int wElite = Math.max(0, weights.elite());
        int wBoss = Math.max(0, weights.boss());
        int wShieldMinion = Math.max(0, weights.shieldMinion());
        int wBerserker = Math.max(0, weights.berserker());
        int wArcher = Math.max(0, weights.archer());

        int total = wMinion + wRanged + wSupport + wElite + wBoss + wShieldMinion + wBerserker + wArcher;
        if (total <= 0) {
            return null;
        }

        int roll = random.nextInt(total);
        int cumulative = 0;

        cumulative += wMinion;
        if (roll < cumulative) {
            return BastionGuardianEntities.BASTION_VINDICATOR.get();
        }
        cumulative += wRanged;
        if (roll < cumulative) {
            return BastionGuardianEntities.BASTION_PILLAGER.get();
        }
        cumulative += wSupport;
        if (roll < cumulative) {
            return BastionGuardianEntities.BASTION_WITCH.get();
        }
        cumulative += wShieldMinion;
        if (roll < cumulative) {
            return BastionGuardianEntities.BASTION_SHIELD_GUARDIAN.get();
        }
        cumulative += wBerserker;
        if (roll < cumulative) {
            return BastionGuardianEntities.BASTION_BERSERKER_GUARDIAN.get();
        }
        cumulative += wArcher;
        if (roll < cumulative) {
            return BastionGuardianEntities.BASTION_ARCHER_GUARDIAN.get();
        }
        cumulative += wElite;
        if (roll < cumulative) {
            return BastionGuardianEntities.BASTION_WARDEN.get();
        }
        return BastionGuardianEntities.BASTION_RAVAGER.get();
    }

    /**
     * 生成时使用的基础常量（避免 MagicNumber checkstyle）。
     */
    private static final class SpawnConstants {
        static final double BLOCK_CENTER_OFFSET = 0.5;
        static final float FULL_ROTATION_DEGREES = 360.0f;

        private SpawnConstants() {
        }
    }

    /**
     * 属性配置常量（避免 MagicNumber）。
     * <p>
     * 说明：当前数值与 BastionGuardianStatsService 对齐，目的是保证孵化巢产物不会短暂变弱。
     * 后续若数值体系迁移，应把这块改为调用统一服务。</p>
     */
    private static final class Stats {
        static final double BASE_HEALTH = 80.0;
        static final double HEALTH_PER_TIER = 60.0;

        static final double BASE_ATTACK_TIER_1 = 6.0;
        static final double ATTACK_MULT_PER_TIER = 10.0;

        static final double BASE_ARMOR = 8.0;
        static final double ARMOR_PER_TIER = 3.0;

        static final double BASE_TOUGHNESS = 2.0;
        static final double TOUGHNESS_PER_TIER = 1.0;

        static final double BASE_FOLLOW_RANGE = 28.0;
        static final double FOLLOW_RANGE_PER_TIER = 4.0;

        static final double BASE_SPEED = 0.28;
        static final double SPEED_PER_TIER = 0.01;
        static final double MAX_SPEED = 0.36;

        static final double BASE_KNOCKBACK_RESIST = 0.2;
        static final double KNOCKBACK_RESIST_PER_TIER = 0.05;
        static final double MAX_KNOCKBACK_RESIST = 0.9;

        private Stats() {
        }
    }
}
