package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.block.BastionAnchorBlock;
import com.Kizunad.guzhenrenext.bastion.block.BastionEnergyNodeBlock;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeConfig;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeManager;
import com.Kizunad.guzhenrenext.bastion.energy.BastionEnergyType;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 能源节点建造服务（Round 3.1/3.2）。
 * <p>
 * 目标：把“能源挂载”从纯扫描缓存推进到可建造方块，同时仍保持与扫描缓存语义兼容。
 * </p>
 * <p>
 * 核心约束：
 * <ul>
 *     <li>只能放置在 Anchor 上方（放置位置 below 必须是 {@link BastionAnchorBlock}）</li>
 *     <li>必须能判定该 Anchor 归属某个基地（优先 SavedData 索引）</li>
 *     <li>
 *         Round 3.2：能源类型不再由玩家预先写 NBT 指定，而是由服务端根据环境判定与
 *         {@code bastion_type.energy.priority_order} 的配置顺序自动选择最终类型。
 *     </li>
 *     <li>扣除基地 resourcePool：成本来自 bastionType.energy.&lt;chosenType&gt;.buildCost</li>
 *     <li>遵守每类能源 maxCount 上限（按基地维度统计已挂载数量，类型为 chosenType）</li>
 *     <li>放置成功后立即写入 anchorEnergyTypes 运行时缓存（让加成立刻生效）</li>
 * </ul>
 * </p>
 */
public final class BastionEnergyBuildService {

    private BastionEnergyBuildService() {
    }

    /** 查找归属基地的最大搜索半径（与 Anchor/交互逻辑对齐）。 */
    private static final int MAX_OWNER_SEARCH_RADIUS = 128;

    /**
     * 尝试在指定位置放置能源节点。
     * <p>
     * 该方法只做 server-side 权威校验：调用者负责在客户端侧显示预览等。
     * </p>
     *
     * @return true 表示已成功放置并扣费；false 表示拒绝。
     */
    public static boolean tryBuildEnergyNode(
            ServerLevel level,
            BastionSavedData savedData,
            ServerPlayer player,
            BlockPos placePos,
            BlockState placeState) {

        if (level == null || savedData == null || player == null || placePos == null || placeState == null) {
            return false;
        }

        if (!isValidEnergyNodePlacement(level, placePos)) {
            player.sendSystemMessage(Component.literal("§c能源节点只能放在 Anchor 上方"));
            return false;
        }

        BlockPos anchorPos = placePos.below();
        BastionData owner = savedData.findOwnerBastion(anchorPos, MAX_OWNER_SEARCH_RADIUS);
        if (owner == null) {
            player.sendSystemMessage(Component.literal("§c该 Anchor 未归属于任何基地，无法建造能源节点"));
            return false;
        }

        // 确保能在运行时缓存中找到（或至少初始化）该基地的 Anchor 集合。
        // 解释：能源扫描/连通性均依赖 savedData.getAnchors(bastionId)。
        // 扩张生成的 Anchor 会写入，但“世界已存在的 Anchor + 玩家建造能源节点”未必写入。
        // Round 3.1 先做最小修复：把当前 anchorPos 加入 anchorCache，以保证后续扫描预算可覆盖它。
        if (!savedData.hasAnchorCache(owner.id())) {
            savedData.initializeAnchorCacheFromCore(owner.id(), owner.corePos());
        }
        savedData.addAnchorToCache(owner.id(), anchorPos);

        BastionTypeConfig typeConfig = BastionTypeManager.getOrDefault(owner.bastionType());
        BastionTypeConfig.EnergyConfig energyConfig = typeConfig.energy();

        BastionEnergyType chosenType = chooseEnergyType(
            energyConfig,
            level,
            anchorPos
        );
        if (chosenType == null) {
            player.sendSystemMessage(Component.literal(
                "§c该 Anchor 周边环境不满足任何能源条件，无法挂载能源节点（需要：露天/水源/岩浆）"
            ));
            return false;
        }

        BastionTypeConfig.EnergyNodeConfig nodeConfig = getEnergyNodeConfig(energyConfig, chosenType);

        int maxCount = Math.max(0, nodeConfig.maxCount());
        double buildCost = Math.max(0.0, nodeConfig.buildCost());

        int currentCount = countEnergyNodesForBastion(savedData, owner.id(), chosenType);
        if (currentCount >= maxCount) {
            player.sendSystemMessage(Component.literal(
                "§c该基地的能源节点已达到上限：" + maxCount + "（类型=" + chosenType.getSerializedName() + "）"
            ));
            return false;
        }

        if (owner.resourcePool() < buildCost) {
            player.sendSystemMessage(Component.literal(
                "§c基地资源池不足，无法建造能源节点：需要=" + buildCost + "，当前=" + owner.resourcePool()
            ));
            return false;
        }

        // 放置方块前先确定最终要放置的 BlockState（必须把 energy_type 写入方块状态）。
        BlockState desiredState = placeState;
        if (desiredState.getBlock() instanceof BastionEnergyNodeBlock) {
            desiredState = desiredState.setValue(BastionEnergyNodeBlock.ENERGY_TYPE, chosenType);
        }

        // 扣费：以 SavedData 中的 BastionData 为权威来源。
        BastionData updated = owner.withResourcePool(owner.resourcePool() - buildCost);
        savedData.updateBastion(updated);

        // 放置方块：放置成功才写入缓存。
        boolean placed = level.setBlock(placePos, desiredState, Block.UPDATE_ALL);
        if (!placed) {
            // 放置失败：回滚资源池。
            // 说明：该失败一般极少发生（例如保护/不可替换），但为了保持一致性这里回滚。
            savedData.updateBastion(owner);
            player.sendSystemMessage(Component.literal("§c放置失败，已取消建造"));
            return false;
        }

        // 立即生效：把 AnchorPos -> energyType 写入运行时缓存。
        // Round 3.2：选中的类型以“环境判定 + priority_order”得出，写入后立刻生效。
        // 后续预算扫描会根据环境变动进行纠正（不满足则移除）。
        savedData.getOrCreateAnchorEnergyMap(owner.id()).put(anchorPos, chosenType);
        return true;
    }

    /**
     * 根据环境与配置优先级，选择最终能源类型。
     * <p>
     * Round 3.2：当水/岩浆/天空条件同时满足时，必须按 bastion_type.energy.priority_order 选择。
     * </p>
     */
    private static BastionEnergyType chooseEnergyType(
            BastionTypeConfig.EnergyConfig energyConfig,
            ServerLevel level,
            BlockPos anchorPos) {
        if (energyConfig == null || level == null || anchorPos == null) {
            return null;
        }

        int photoRadius = Math.max(1, energyConfig.photosynthesis().scanRadius());
        int waterRadius = Math.max(1, energyConfig.waterIntake().scanRadius());
        int geothermalRadius = Math.max(1, energyConfig.geothermal().scanRadius());

        for (BastionEnergyType type : energyConfig.normalizedPriorityOrder()) {
            boolean ok = BastionEnergyService.isEnergyTypeSatisfied(
                type,
                level,
                anchorPos,
                photoRadius,
                waterRadius,
                geothermalRadius
            );
            if (ok) {
                return type;
            }
        }
        return null;
    }

    /**
     * 放置约束：能源节点必须放在 Anchor 上方一格（placePos.below() 是 BastionAnchorBlock）。
     */
    public static boolean isValidEnergyNodePlacement(ServerLevel level, BlockPos placePos) {
        if (level == null || placePos == null) {
            return false;
        }
        BlockState below = level.getBlockState(placePos.below());
        return below.getBlock() instanceof BastionAnchorBlock;
    }

    private static BastionTypeConfig.EnergyNodeConfig getEnergyNodeConfig(
            BastionTypeConfig.EnergyConfig energyConfig,
            BastionEnergyType energyType) {
        if (energyConfig == null || energyType == null) {
            return BastionTypeConfig.EnergyNodeConfig.DEFAULT;
        }

        return switch (energyType) {
            case PHOTOSYNTHESIS -> energyConfig.photosynthesis();
            case WATER_INTAKE -> energyConfig.waterIntake();
            case GEOTHERMAL -> energyConfig.geothermal();
        };
    }

    /**
     * 统计某个基地已挂载的指定能源类型数量。
     * <p>
     * 数据源：SavedData 的 anchorEnergyTypes 运行时缓存。
     * <br>
     * 说明：该缓存是“弱一致性”，由预算扫描推进与世界状态纠正；Round 3.1 的建造上限也以它为主，
     * 以保证建造成功后“立即可见”。
     * </p>
     */
    private static int countEnergyNodesForBastion(
            BastionSavedData savedData,
            UUID bastionId,
            BastionEnergyType type) {
        Map<BlockPos, BastionEnergyType> map = savedData.getOrCreateAnchorEnergyMap(bastionId);
        int count = 0;
        for (BastionEnergyType v : map.values()) {
            if (v == type) {
                count++;
            }
        }
        return count;
    }
}
