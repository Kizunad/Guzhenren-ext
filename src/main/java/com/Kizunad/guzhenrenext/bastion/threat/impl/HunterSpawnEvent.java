package com.Kizunad.guzhenrenext.bastion.threat.impl;

import com.Kizunad.guzhenrenext.bastion.BastionSoundPlayer;
import com.Kizunad.guzhenrenext.bastion.entity.BastionGuardianData;
import com.Kizunad.guzhenrenext.bastion.entity.BastionGuardianFactory;
import com.Kizunad.guzhenrenext.bastion.threat.IThreatEvent;
import com.Kizunad.guzhenrenext.bastion.threat.ThreatEventContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 猎手生成威胁事件。
 * <p>
 * 效果：在被拆除节点附近生成追踪玩家的守卫怪物。
 * 生成数量随基地转数提升。
 * </p>
 */
public final class HunterSpawnEvent implements IThreatEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(HunterSpawnEvent.class);

    /** 事件 ID。 */
    private static final String ID = "hunter_spawn";

    /** 基础权重。 */
    private static final int BASE_WEIGHT = 35;

    /** 基础生成数量。 */
    private static final int BASE_SPAWN_COUNT = 1;

    /** 每 2 转增加的生成数量。 */
    private static final int SPAWN_PER_2_TIERS = 1;

    /** 最大生成数量。 */
    private static final int MAX_SPAWN_COUNT = 5;

    /** 生成位置偏移范围。 */
    private static final int SPAWN_OFFSET_RANGE = 5;

    /** 生成位置尝试次数上限。 */
    private static final int MAX_SPAWN_ATTEMPTS = 10;

    /** 向上/向下搜索可站立位置的范围。 */
    private static final int SPAWN_Y_SEARCH_MIN = -3;
    private static final int SPAWN_Y_SEARCH_MAX = 5;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public int getBaseWeight() {
        return BASE_WEIGHT;
    }

    @Override
    public boolean canTrigger(ThreatEventContext context) {
        // 需要有附近玩家作为追踪目标
        return context.hasNearbyPlayers();
    }

    @Override
    public void execute(ThreatEventContext context) {
        int tier = context.bastion().tier();
        int spawnCount = Math.min(MAX_SPAWN_COUNT, BASE_SPAWN_COUNT + (tier - 1) / 2 * SPAWN_PER_2_TIERS);

        LOGGER.debug("执行猎手生成: tier={}, count={}", tier, spawnCount);

        // 播放音效
        BastionSoundPlayer.playThreat(context.level(), context.triggerPos());

        // 选择追踪目标（随机选择一个附近玩家）
        ServerPlayer target = context.nearbyPlayers().get(
            context.random().nextInt(context.nearbyPlayers().size()));

        // 生成追猎者（守卫实体），并锁定目标玩家
        for (int i = 0; i < spawnCount; i++) {
            BlockPos spawnPos = findSpawnPosition(context);
            if (spawnPos == null) {
                LOGGER.trace("无法找到有效生成位置，跳过第 {} 个猎手", i + 1);
                continue;
            }

            Mob guardian = BastionGuardianFactory.createGuardian(
                context.level(),
                context.bastion(),
                spawnPos
            );
            if (guardian == null) {
                LOGGER.trace("猎手创建失败，跳过：{}", spawnPos);
                continue;
            }

            // 标记为基地守卫并设置仇恨目标
            BastionGuardianData.markAsGuardian(
                guardian,
                context.bastion().id(),
                context.bastion().tier()
            );
            guardian.setTarget(target);

            if (context.level().addFreshEntity(guardian)) {
                LOGGER.trace("生成猎手于 {} 追踪玩家 {}",
                    spawnPos, target.getName().getString());
            }
        }
    }

    /**
     * 在触发位置附近找到有效的生成位置。
     */
    private BlockPos findSpawnPosition(ThreatEventContext context) {
        BlockPos basePos = context.triggerPos();

        for (int attempt = 0; attempt < MAX_SPAWN_ATTEMPTS; attempt++) {
            int offsetX = context.random().nextInt(SPAWN_OFFSET_RANGE * 2 + 1) - SPAWN_OFFSET_RANGE;
            int offsetZ = context.random().nextInt(SPAWN_OFFSET_RANGE * 2 + 1) - SPAWN_OFFSET_RANGE;
            BlockPos candidate = basePos.offset(offsetX, 0, offsetZ);

            // 向上搜索可站立位置
            for (int dy = SPAWN_Y_SEARCH_MIN; dy <= SPAWN_Y_SEARCH_MAX; dy++) {
                BlockPos checkPos = candidate.above(dy);
                if (isValidSpawnPosition(context, checkPos)) {
                    return checkPos;
                }
            }
        }

        return null;
    }

    /**
     * 检查位置是否适合生成怪物。
     */
    private boolean isValidSpawnPosition(ThreatEventContext context, BlockPos pos) {
        // 脚下必须是固体方块
        if (!context.level().getBlockState(pos.below()).isSolid()) {
            return false;
        }
        // 当前位置和头部位置必须可通过
        if (!context.level().getBlockState(pos).isAir()) {
            return false;
        }
        if (!context.level().getBlockState(pos.above()).isAir()) {
            return false;
        }
        return true;
    }
}
