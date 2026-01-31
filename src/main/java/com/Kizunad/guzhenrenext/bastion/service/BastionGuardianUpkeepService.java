package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.entity.BastionGuardianData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

/**
 * 基地守卫 upkeep（维护费）服务。
 * <p>
 * 回合 4 的“供养/停机”最小落点：
 * <ul>
 *   <li>资源池（resourcePool）不仅用于扩张/产出，也作为守卫的维护费来源。</li>
 *   <li>每个 tickInterval（由外部 ticker 驱动的时间粒度）按守卫数量扣除 resourcePool。</li>
 *   <li>当 resourcePool 归零时，基地进入停机态（上层逻辑应禁止继续刷出新守卫）。</li>
 * </ul>
 * </p>
 * <p>
 * 注意：本服务 <b>只做扣费</b>，不负责刷怪、销毁实体或其它副作用。
 * 这样可以把“经济结算”和“生成/清理”拆开，便于 GameTest/调参/回归。
 * </p>
 */
public final class BastionGuardianUpkeepService {

    private BastionGuardianUpkeepService() {
        // 工具类
    }

    /** 非配置化的常量，先用于实现回合 4 的确定性扣费语义。 */
    private static final class Constants {
        /**
         * 每个守卫在一个维护间隔（tickInterval）内的维护费用。
         * <p>
         * 这里刻意做成常量而非魔法数字，便于后续回合做平衡/配置化。
         * </p>
         */
        static final double UPKEEP_COST_PER_GUARDIAN_PER_INTERVAL = 1.0;

        /**
         * 守卫计数时的垂直搜索半径。
         * <p>
         * 与 {@link BastionSpawnService} 中的守卫计数策略保持一致：
         * 以核心为中心，按基地当前扩张半径做水平范围，垂直范围给一个固定余量，
         * 避免守卫上下小范围移动导致计数波动。
         * </p>
         */
        static final int GUARDIAN_SEARCH_HEIGHT = 16;

        private Constants() {
        }
    }

    /**
     * 对指定基地执行一次维护费结算（单次扣费）。
     * <p>
     * 语义上等价于“经过了 1 个 tickInterval”，但本方法不依赖具体 tick 值，
     * 只执行：统计 -> 计算成本 -> 扣 resourcePool（不为负）。
     * </p>
     *
     * @param level   服务端世界（用于在当前维度内统计守卫实体）
     * @param bastion 基地数据
     * @return 扣费后的 BastionData（使用 {@link BastionData#withResourcePool(double)}）
     */
    public static BastionData applyUpkeep(ServerLevel level, BastionData bastion) {
        int guardianCount = countBastionGuardians(level, bastion);
        double upkeepCost = guardianCount * Constants.UPKEEP_COST_PER_GUARDIAN_PER_INTERVAL;

        double oldPool = bastion.resourcePool();
        double newPool = Math.max(0.0, oldPool - upkeepCost);

        // 维持“纯函数”语义：只返回新对象，不做任何实体侧副作用。
        if (newPool == oldPool) {
            return bastion;
        }
        return bastion.withResourcePool(newPool);
    }

    /**
     * 统计指定基地范围内属于该基地的守卫数量。
     * <p>
     * 复用 {@link BastionGuardianData#belongsToBastion(net.minecraft.world.entity.Entity, java.util.UUID)}
     * 的精确 UUID 判断（PersistentData），并结合通用 tag 做快速过滤。
     * </p>
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
}
