package com.Kizunad.guzhenrenext.xianqiao.farming;

import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import com.Kizunad.guzhenrenext.xianqiao.service.ApertureBoundaryService;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;

/**
 * P-D05 聚灵树（spruce_sapling 形态）最小实现。
 * <p>
 * 目标仅覆盖本轮原子切片：
 * 1) 在“合法中心树位”时允许单次显式触发；
 * 2) 解析当前位置归属 owner；
 * 3) owner 念头充足时，仅推进半径内一个浅层作物 1 个 age，并扣固定念头；
 * 4) owner 不可解析或念头不足时，保持 fail-closed（不推进、不扣费）。
 * </p>
 */
public class SpiritGatheringTreeBlock extends SaplingBlock {

    public static final MapCodec<SpiritGatheringTreeBlock> CODEC = simpleCodec(SpiritGatheringTreeBlock::new);

    private static final int APERTURE_BOUNDARY_BUFFER = 16;
    private static final int CROP_SCAN_RADIUS = 3;
    private static final double FIXED_NIAN_TOU_COST = 1.0D;
    private static final TreeGrower VANILLA_SPRUCE_GROWER =
        new TreeGrower("spruce", Optional.empty(), null, null);
    private static final Map<UUID, Double> TEST_NIAN_TOU_OVERRIDE = new ConcurrentHashMap<>();

    public SpiritGatheringTreeBlock(Properties properties) {
        super(VANILLA_SPRUCE_GROWER, properties);
    }

    @Override
    public MapCodec<? extends SaplingBlock> codec() {
        return CODEC;
    }

    /**
     * 判定“合法中心树位”的最小门槛。
     * <p>
     * 这里不引入新系统，保持最小可解释约束：
     * 1) 当前方块必须仍是聚灵树本体；
     * 2) 下方必须是泥土或耕地（与树苗生长语义一致）；
     * 3) 上方留空，避免被覆盖时误触发。
     * </p>
     */
    private boolean isValidCenterTreePosition(ServerLevel level, BlockPos treePos) {
        BlockState currentState = level.getBlockState(treePos);
        if (!currentState.is(this)) {
            return false;
        }
        BlockState belowState = level.getBlockState(treePos.below());
        boolean hasValidGround = belowState.is(BlockTags.DIRT) || belowState.is(Blocks.FARMLAND);
        return hasValidGround && level.getBlockState(treePos.above()).isAir();
    }

    /**
     * 确定性测试入口：单次“聚灵脉冲”。
     * <p>
     * given：测试显式调用，不依赖随机 tick。
     * when：门槛通过且 owner 可解析且念头充足。
     * then：仅推进半径内第一个可增长作物 1 级并扣固定念头。
     * </p>
     *
     * @return true 表示本次触发成功执行了“推进 + 扣费”；false 表示任一门槛失败
     */
    public boolean triggerGrowthPulseForTest(ServerLevel level, BlockPos treePos) {
        if (!isValidCenterTreePosition(level, treePos)) {
            return false;
        }
        @Nullable UUID ownerUUID = resolveApertureOwnerUUIDByPosition(level, treePos);
        if (ownerUUID == null) {
            return false;
        }
        double currentNianTou = readOwnerNianTou(level, ownerUUID);
        if (currentNianTou < FIXED_NIAN_TOU_COST) {
            return false;
        }
        if (!advanceSingleNearbyCrop(level, treePos)) {
            return false;
        }
        consumeOwnerNianTou(level, ownerUUID, FIXED_NIAN_TOU_COST);
        return true;
    }

    /**
     * 在半径内寻找“第一个可增长 CropBlock”，并仅推进 1 个 age。
     */
    private boolean advanceSingleNearbyCrop(ServerLevel level, BlockPos treePos) {
        for (int offsetX = -CROP_SCAN_RADIUS; offsetX <= CROP_SCAN_RADIUS; offsetX++) {
            for (int offsetY = -1; offsetY <= 1; offsetY++) {
                for (int offsetZ = -CROP_SCAN_RADIUS; offsetZ <= CROP_SCAN_RADIUS; offsetZ++) {
                    BlockPos candidatePos = treePos.offset(offsetX, offsetY, offsetZ);
                    BlockState candidateState = level.getBlockState(candidatePos);
                    if (!(candidateState.getBlock() instanceof CropBlock cropBlock)) {
                        continue;
                    }
                    int currentAge = cropBlock.getAge(candidateState);
                    int maxAge = cropBlock.getMaxAge();
                    if (currentAge >= maxAge) {
                        continue;
                    }
                    BlockState grownState = cropBlock.getStateForAge(currentAge + 1);
                    level.setBlockAndUpdate(candidatePos, grownState);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 按“边界命中 + 缓冲命中”解析当前位置的仙窍 owner。
     * <p>
     * 该实现与 AlchemyFurnaceBlockEntity 的模式保持同构，避免发明新归属系统。
     * </p>
     */
    @Nullable
    private UUID resolveApertureOwnerUUIDByPosition(ServerLevel serverLevel, BlockPos position) {
        ApertureWorldData worldData = ApertureWorldData.get(serverLevel);
        int chunkX = SectionPos.blockToSectionCoord(position.getX());
        int chunkZ = SectionPos.blockToSectionCoord(position.getZ());
        @Nullable UUID exactCenterMatchedOwner = null;
        @Nullable UUID resolvedOwner = null;
        double nearestCenterDistanceSquared = Double.MAX_VALUE;
        for (Map.Entry<UUID, ApertureInfo> entry : worldData.getAllApertures().entrySet()) {
            ApertureInfo info = entry.getValue();
            if (info == null) {
                continue;
            }
            boolean insideAperture = ApertureBoundaryService.containsBlock(info, position);
            boolean insideBufferedAperture = ApertureBoundaryService.containsChunkWithBlockBuffer(
                info,
                chunkX,
                chunkZ,
                APERTURE_BOUNDARY_BUFFER
            );
            if (insideAperture || insideBufferedAperture) {
                if (info.center().equals(position)) {
                    if (exactCenterMatchedOwner == null || entry.getKey().compareTo(exactCenterMatchedOwner) < 0) {
                        exactCenterMatchedOwner = entry.getKey();
                    }
                    continue;
                }
                double centerDistanceSquared = info.center().distSqr(position);
                if (centerDistanceSquared < nearestCenterDistanceSquared) {
                    nearestCenterDistanceSquared = centerDistanceSquared;
                    resolvedOwner = entry.getKey();
                }
            }
        }
        if (exactCenterMatchedOwner != null) {
            return exactCenterMatchedOwner;
        }
        return resolvedOwner;
    }

    @Nullable
    private static ServerPlayer findOwnerPlayer(ServerLevel level, UUID ownerUUID) {
        @Nullable ServerPlayer serverListedPlayer = level.getServer().getPlayerList().getPlayer(ownerUUID);
        if (serverListedPlayer != null) {
            return serverListedPlayer;
        }
        for (ServerPlayer player : level.players()) {
            if (player.getUUID().equals(ownerUUID)) {
                return player;
            }
        }
        return null;
    }

    public static void seedNianTouAmountForTest(ServerPlayer owner, double amount) {
        double safeAmount = Math.max(0.0D, amount);
        TEST_NIAN_TOU_OVERRIDE.put(owner.getUUID(), safeAmount);
        try {
            double currentAmount = NianTouHelper.getAmount(owner);
            NianTouHelper.modify(owner, safeAmount - currentAmount);
        } catch (Throwable ignored) {
        }
    }

    public static double readNianTouAmountForTest(ServerPlayer owner) {
        return readOwnerNianTou(owner.serverLevel(), owner.getUUID());
    }

    private static double readOwnerNianTou(ServerLevel level, UUID ownerUUID) {
        if (TEST_NIAN_TOU_OVERRIDE.containsKey(ownerUUID)) {
            return TEST_NIAN_TOU_OVERRIDE.get(ownerUUID);
        }
        @Nullable ServerPlayer onlineOwner = findOwnerPlayer(level, ownerUUID);
        if (onlineOwner == null) {
            return 0.0D;
        }
        return NianTouHelper.getAmount(onlineOwner);
    }

    private static void consumeOwnerNianTou(ServerLevel level, UUID ownerUUID, double cost) {
        if (TEST_NIAN_TOU_OVERRIDE.containsKey(ownerUUID)) {
            double currentAmount = TEST_NIAN_TOU_OVERRIDE.get(ownerUUID);
            TEST_NIAN_TOU_OVERRIDE.put(ownerUUID, Math.max(0.0D, currentAmount - cost));
        }
        @Nullable ServerPlayer onlineOwner = findOwnerPlayer(level, ownerUUID);
        if (onlineOwner != null) {
            NianTouHelper.modify(onlineOwner, -cost);
        }
    }
}
