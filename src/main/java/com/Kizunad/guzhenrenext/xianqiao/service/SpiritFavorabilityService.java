package com.Kizunad.guzhenrenext.xianqiao.service;

import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;

/**
 * 灵性好感度服务。
 * <p>
 * 本服务定位为“服务端权威”的无状态工具类，仅负责：
 * 1) 基于玩家在仙窍维度中的存档数据读取当前好感度；
 * 2) 以增量方式尝试更新好感度；
 * 3) 对写入值执行统一限幅，保证区间稳定在 [0, 100]。
 * </p>
 * <p>
 * 说明：
 * 由于 {@link ApertureInfo} 为不可变记录类型，最终回写通过
 * {@link ApertureWorldData#updateFavorability(UUID, float)} 完成。
 * </p>
 */
public final class SpiritFavorabilityService {

    /** 好感度最小值。 */
    private static final float MIN_FAVORABILITY = 0.0F;

    /** 好感度最大值。 */
    private static final float MAX_FAVORABILITY = 100.0F;

    private SpiritFavorabilityService() {
    }

    /**
     * 好感度变化原因。
     * <p>
     * 该枚举用于标识业务来源，便于后续扩展日志、埋点或差异化策略。
     * 当前方法签名保留该参数，确保调用方在入口层显式传入语义。
     * </p>
     */
    public enum Reason {
        /** 炼化贡献导致的好感度变化。 */
        REFINING_CONTRIBUTION,

        /** 手动交互导致的好感度变化。 */
        MANUAL_INTERACTION,

        /** 危机守护行为导致的好感度变化。 */
        CRISIS_GUARD,

        /** 调试命令导致的好感度变化。 */
        DEBUG_COMMAND
    }

    /**
     * 按增量尝试更新指定玩家的灵性好感度。
     * <p>
     * 更新流程：
     * 1) 从 {@link ApertureWorldData} 读取当前 {@link ApertureInfo}；
     * 2) 若玩家尚无仙窍信息，直接返回 false；
     * 3) 计算 {@code current + delta} 并限幅到 [0, 100]；
     * 4) 若限幅后与旧值一致，视为无实际变化，返回 false；
     * 5) 调用 worldData.updateFavorability 回写并返回 true。
     * </p>
     *
     * @param apertureLevel 仙窍维度服务端世界
     * @param ownerUUID 玩家 UUID
     * @param delta 本次增量（可正可负）
     * @param reason 变化原因（用于业务语义标记）
     * @return 实际发生更新返回 true，否则返回 false
     */
    public static boolean tryAddFavorability(ServerLevel apertureLevel, UUID ownerUUID, float delta, Reason reason) {
        ApertureWorldData worldData = ApertureWorldData.get(apertureLevel);
        ApertureInfo info = worldData.getAperture(ownerUUID);
        if (info == null) {
            return false;
        }

        float currentFavorability = info.favorability();
        float nextFavorability = clampFavorability(currentFavorability + delta);
        if (Float.compare(nextFavorability, currentFavorability) == 0) {
            return false;
        }

        worldData.updateFavorability(ownerUUID, nextFavorability);
        return true;
    }

    /**
     * 读取指定玩家当前灵性好感度。
     *
     * @param apertureLevel 仙窍维度服务端世界
     * @param ownerUUID 玩家 UUID
     * @return 当前好感度；若玩家尚无仙窍信息，返回 0
     */
    public static float getFavorability(ServerLevel apertureLevel, UUID ownerUUID) {
        ApertureWorldData worldData = ApertureWorldData.get(apertureLevel);
        ApertureInfo info = worldData.getAperture(ownerUUID);
        if (info == null) {
            return MIN_FAVORABILITY;
        }
        return info.favorability();
    }

    /**
     * 限制好感度到合法区间 [0, 100]。
     *
     * @param value 原始值
     * @return 限幅后的值
     */
    private static float clampFavorability(float value) {
        return Math.max(MIN_FAVORABILITY, Math.min(MAX_FAVORABILITY, value));
    }
}
