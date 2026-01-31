package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.block.BastionAnchorBlock;
import com.Kizunad.guzhenrenext.bastion.block.BastionEnergyNodeBlock;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeConfig;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeManager;
import com.Kizunad.guzhenrenext.bastion.energy.BastionEnergyType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/**
 * 基地能源挂载扫描服务（回合3：能源挂载 v1）。
 * <p>
 * 职责：
 * <ul>
 *     <li>从 {@link BastionSavedData#getAnchors(java.util.UUID)} 获取 Anchor 坐标（运行时缓存）</li>
 *     <li>在 <b>预算限制</b> 下扫描 Anchor 周边环境并判定能源类型</li>
 *     <li>将结果写入 {@link BastionSavedData#getOrCreateAnchorEnergyMap(java.util.UUID)}（运行时缓存）</li>
 * </ul>
 * </p>
 * <p>
 * 设计说明（必须读）：
 * <ul>
 *     <li>
 *         为什么要预算化：Anchor 可能很多，且水/岩浆扫描是立方体遍历，属于“高常数”操作。
 *         若每 tick 全量扫描会造成服务端卡顿，因此按 N 个/次、每 M tick 扫一次的方式分片执行。
 *     </li>
 *     <li>
 *         为什么不持久化：能源类型是可从世界状态推导的派生信息（方块/液体/天空可见性）。
 *         写入存档会引入 schema 变化与一致性问题；重启后由后续“预算化重建流程”重新填充即可。
 *     </li>
 *     <li>
 *         光合判定为什么用 canSeeSky 简化：完整判定通常需要考虑光照衰减、遮挡、时间/天气等。
 *         MVP 先用 {@link ServerLevel#canSeeSky(BlockPos)} 做“是否能直视天空”的近似，保证 server-safe，
 *         后续再按需要精细化（例如引入 skylight 阈值/更复杂采样）。
 *     </li>
 * </ul>
 * </p>
 * <p>
 * 注意：本类仅提供静态入口，不在本任务中接入 BastionTicker。
 * </p>
 */
public final class BastionEnergyService {

    private BastionEnergyService() {
    }

    /**
     * 扫描配置常量（避免 MagicNumber）。
     */
    private static final class ScanConfig {
        /** 每隔多少 tick 才允许进行一次能源扫描。 */
        static final long ENERGY_SCAN_INTERVAL_TICKS = 40L;
        /** 单次扫描最多处理多少个 Anchor（预算）。 */
        static final int ENERGY_SCAN_BUDGET_ANCHORS = 4;

        /** 光合 skylight 阈值（0~15）。MVP 主要用 canSeeSky 判定，此阈值作为后备采样判断。 */
        static final int PHOTOSYNTHESIS_SKY_LIGHT_THRESHOLD = 12;

        private ScanConfig() {
        }
    }

    /**
     * 在基地 tick 中调用：预算化扫描 Anchor 周边环境并填充能源挂载缓存。
     * <p>
     * 约束：
     * <ul>
     *     <li>只扫描 {@code savedData.getAnchors(bastion.id())} 内的 AnchorPos</li>
     *     <li>要求 chunk loaded（未加载直接跳过，不做强制清理）</li>
     * </ul>
     * </p>
     */
    public static void tick(ServerLevel level, BastionSavedData savedData, BastionData bastion, long gameTime) {
        // 未到扫描间隔：直接跳过（避免每 tick 做重扫描）。
        if (gameTime % ScanConfig.ENERGY_SCAN_INTERVAL_TICKS != 0L) {
            return;
        }

        Set<BlockPos> anchors = savedData.getAnchors(bastion.id());
        if (anchors.isEmpty()) {
            return;
        }

        BastionTypeConfig typeConfig = BastionTypeManager.getOrDefault(bastion.bastionType());
        BastionTypeConfig.EnergyConfig energyConfig = typeConfig.energy();

        int photoRadius = Math.max(1, energyConfig.photosynthesis().scanRadius());
        int waterRadius = Math.max(1, energyConfig.waterIntake().scanRadius());
        int geothermalRadius = Math.max(1, energyConfig.geothermal().scanRadius());

        // 运行时缓存写入入口。
        Map<BlockPos, BastionEnergyType> energyMap = savedData.getOrCreateAnchorEnergyMap(bastion.id());

        // 为了在“无持久游标”的前提下仍能轮转扫描目标：
        // 1) 对 AnchorPos 做稳定排序；2) 用 gameTime 计算起点进行循环扫描。
        // 这样不会每次都只扫描同一批前 N 个。
        List<BlockPos> sortedAnchors = new ArrayList<>(anchors);
        // BlockPos 同时存在 static asLong(x,y,z) 与实例 asLong()，
        // 这里用 lambda 避免方法引用在不同映射下产生歧义。
        sortedAnchors.sort(Comparator.comparingLong(pos -> pos.asLong()));

        int size = sortedAnchors.size();
        int budget = Math.max(1, ScanConfig.ENERGY_SCAN_BUDGET_ANCHORS);
        int startIndex = (int) ((gameTime / ScanConfig.ENERGY_SCAN_INTERVAL_TICKS) % (long) size);

        for (int i = 0; i < budget && i < size; i++) {
            BlockPos anchorPos = sortedAnchors.get((startIndex + i) % size);

            // chunk 未加载：不扫描，不强行清理（允许留给外部流程/后续 tick）。
            if (!level.isLoaded(anchorPos)) {
                continue;
            }

            // 缓存可能“撒谎”（例如方块被破坏但缓存未更新）。
            // 只有当我们实际扫描到该位置不是 Anchor 时，才清理能源挂载记录作为兜底。
            BlockState state = level.getBlockState(anchorPos);
            if (!(state.getBlock() instanceof BastionAnchorBlock)) {
                savedData.clearAnchorEnergy(bastion.id(), anchorPos);
                continue;
            }

            // Round 3.1：能源节点成为可建造方块，必须“挂载在 Anchor 上”。
            // 规则：仅当 Anchor 上方存在 BastionEnergyNodeBlock 时才允许该 Anchor 产生能源挂载。
            BlockPos nodePos = anchorPos.above();
            if (!level.isLoaded(nodePos)) {
                continue;
            }
            BlockState nodeState = level.getBlockState(nodePos);
            if (!(nodeState.getBlock() instanceof BastionEnergyNodeBlock)) {
                // 该 Anchor 未挂载能源节点：清理旧记录（避免拆除后长期保留加成）。
                energyMap.remove(anchorPos);
                continue;
            }

            // Round 3.1：能源类型由“节点方块的类型属性”指定；扫描只负责校验环境是否满足该类型。
            BastionEnergyType desiredType = nodeState.getValue(BastionEnergyNodeBlock.ENERGY_TYPE);

            boolean valid = isEnergyTypeSatisfied(
                desiredType,
                level,
                anchorPos,
                photoRadius,
                waterRadius,
                geothermalRadius
            );

            if (!valid) {
                // 环境不满足：移除旧记录，避免长期保留过期加成。
                energyMap.remove(anchorPos);
                continue;
            }

            energyMap.put(anchorPos, desiredType);
        }

        // 清理失效条目：如果能源缓存里存在“已不在 anchorCache 的位置”，则移除。
        // 说明：anchorCache 是运行时缓存，可能在方块破坏/重建后短暂不同步。
        // 这里做“弱一致性”清理：
        // - 不扫描未加载区块（成本太高）
        // - 不强行加载区块
        // - 仅移除明显不再属于该基地 anchorCache 的条目
        java.util.Set<BlockPos> anchorSet = anchors;
        energyMap.keySet().removeIf(pos -> !anchorSet.contains(pos));
    }

    /**
     * 判断“指定能源类型”在当前环境中是否满足。
     * <p>
     * Round 3.1：能源类型不再由扫描“推断”，而是由能源节点方块指定；
     * 扫描只负责校验环境条件。
     * </p>
     */
    private static boolean isEnergyTypeSatisfied(
            BastionEnergyType energyType,
            ServerLevel level,
            BlockPos anchorPos,
            int photoRadius,
            int waterRadius,
            int geothermalRadius) {

        if (energyType == null) {
            return false;
        }

        return switch (energyType) {
            case PHOTOSYNTHESIS -> isPhotosynthesis(level, anchorPos, photoRadius);
            case WATER_INTAKE -> isWaterIntake(level, anchorPos, waterRadius);
            case GEOTHERMAL -> isGeothermal(level, anchorPos, geothermalRadius);
        };
    }

    /**
     * 光合判定（MVP）：Anchor 上方在 scanRadius 范围内存在天空可见或 skylight >= 阈值。
     * <p>
     * MVP 简化：优先用 canSeeSky 判断直视天空（常见“露天”场景），
     * 并在 scanRadius 内做少量垂直采样作为后备（避免极端情况下 canSeeSky 过严）。
     * </p>
     */
    private static boolean isPhotosynthesis(ServerLevel level, BlockPos anchorPos, int scanRadius) {
        // Round 3.1：能源节点本身位于 Anchor 上方一格。
        // 因此这里从“能源节点上方”开始做垂直采样：避免节点方块本身阻挡 canSeeSky 判断。
        for (int dy = 1; dy <= scanRadius; dy++) {
            BlockPos pos = anchorPos.above(dy + 1);
            if (!level.isLoaded(pos)) {
                continue;
            }
            if (level.canSeeSky(pos)) {
                return true;
            }
            if (level.getBrightness(LightLayer.SKY, pos) >= ScanConfig.PHOTOSYNTHESIS_SKY_LIGHT_THRESHOLD) {
                return true;
            }
        }
        return false;
    }

    /**
     * 汲水判定：在 scanRadius 立方体内存在水流体。
     */
    private static boolean isWaterIntake(ServerLevel level, BlockPos anchorPos, int scanRadius) {
        return containsFluid(level, anchorPos, scanRadius, Fluids.WATER);
    }

    /**
     * 地热判定：在 scanRadius 立方体内存在岩浆流体。
     */
    private static boolean isGeothermal(ServerLevel level, BlockPos anchorPos, int scanRadius) {
        return containsFluid(level, anchorPos, scanRadius, Fluids.LAVA);
    }

    /**
     * 在以 center 为中心的立方体范围内查找指定流体。
     * <p>
     * 重要：不会触发区块加载；遇到未加载位置直接跳过。
     * </p>
     */
    private static boolean containsFluid(ServerLevel level, BlockPos center, int scanRadius, Fluid fluid) {
        int r = Math.max(1, scanRadius);
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (!level.isLoaded(pos)) {
                        continue;
                    }
                    FluidState fluidState = level.getFluidState(pos);
                    if (!fluidState.isEmpty() && fluidState.is(fluid)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
