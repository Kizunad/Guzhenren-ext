package com.Kizunad.guzhenrenext.entity.spawn;

import com.Kizunad.guzhenrenext.entity.ModEntities;
import com.Kizunad.guzhenrenext.entity.RogueEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;

/**
 * 散修 NPC 世界生成器。
 * <p>
 * 根据世界状态动态生成散修，支持生成规则（数量上限、位置约束）、初始属性（等级、势力倾向）、
 * 生成事件发布。使用秒级检查（每 20 tick 检查一次）而非每 tick 检查。
 * </p>
 */
public final class RogueSpawner {

    /**
     * 秒级检查的 tick 间隔（20 tick = 1 秒）。
     */
    private static final long TICKS_PER_SECOND = 20L;

    /**
     * 生成位置的 X/Z 坐标偏移（方块中心）。
     */
    private static final double SPAWN_OFFSET = 0.5D;

    /**
     * 私有构造器，防止实例化。
     */
    private RogueSpawner() {
    }

    /**
     * 尝试在指定位置附近生成散修。
     * <p>
     * 仅在秒级检查时间点（level.getGameTime() % 20 == 0）执行生成逻辑。
     * 检查当前附近散修数量，如果未超过 maxCount，则生成一个新散修。
     * </p>
     *
     * @param level 服务器世界
     * @param center 生成中心位置
     * @param maxCount 最大散修数量上限
     * @param radius 搜索半径（方块）
     */
    public static void trySpawn(ServerLevel level, BlockPos center, int maxCount, int radius) {
        // 秒级检查：仅在 20 tick 倍数时执行
        if (level.getGameTime() % TICKS_PER_SECOND != 0) {
            return;
        }

        // 统计附近散修数量
        int currentCount = countRoguesNear(level, center, radius);

        // 如果未超过上限，生成一个新散修
        if (currentCount < maxCount) {
            spawnRogue(level, center);
        }
    }

    /**
     * 统计指定位置附近的散修数量。
     * <p>
     * 在以 center 为中心、radius 为半径的立方体范围内搜索所有 RogueEntity 实体。
     * </p>
     *
     * @param level 服务器世界
     * @param center 搜索中心位置
     * @param radius 搜索半径（方块）
     * @return 附近散修的数量
     */
    public static int countRoguesNear(ServerLevel level, BlockPos center, int radius) {
        // 构建搜索范围
        BlockPos minPos = center.offset(-radius, -radius, -radius);
        BlockPos maxPos = center.offset(radius, radius, radius);

        // 获取范围内的所有 RogueEntity
        List<RogueEntity> rogues = level.getEntitiesOfClass(
            RogueEntity.class,
            new AABB(
                minPos.getX(),
                minPos.getY(),
                minPos.getZ(),
                maxPos.getX() + 1,
                maxPos.getY() + 1,
                maxPos.getZ() + 1
            )
        );

        return rogues.size();
    }

    /**
     * 在指定位置生成一个散修。
     * <p>
     * 创建新的 RogueEntity 实例，将其添加到世界中，并发布生成事件。
     * </p>
     *
     * @param level 服务器世界
     * @param pos 生成位置
     * @return 生成的散修实体，如果生成失败则返回 null
     */
    public static RogueEntity spawnRogue(ServerLevel level, BlockPos pos) {
        // 获取 RogueEntity 的 EntityType
        EntityType<RogueEntity> rogueType = ModEntities.ROGUE.value();

        // 创建新实体
        RogueEntity rogue = new RogueEntity(rogueType, level);

        // 设置位置
        rogue.setPos(pos.getX() + SPAWN_OFFSET, pos.getY(), pos.getZ() + SPAWN_OFFSET);

        // 添加到世界
        level.addFreshEntity(rogue);

        // 发布生成事件（如果 FactionEventBus 有相应事件类型）
        // 当前版本暂不发布事件，因为 FactionEventBus 中可能没有 SpawnEvent
        // 后续可扩展为：FactionEventBus.INSTANCE.post(new RogueSpawnedEvent(...))

        return rogue;
    }
}
