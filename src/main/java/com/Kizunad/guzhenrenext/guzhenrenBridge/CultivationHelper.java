package com.Kizunad.guzhenrenext.guzhenrenBridge;

import net.guzhenren.network.GuzhenrenModVariables;
import net.minecraft.world.entity.LivingEntity;

/**
 * 蛊师修为数据操作工具类。
 */
public final class CultivationHelper {

    private CultivationHelper() {}

    /**
     * 获取当前修炼进度。
     */
    public static double getProgress(LivingEntity entity) {
        if (entity == null) {
            return 0.0;
        }
        try {
            return getVariables(entity).gushi_xiulian_dangqian;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 修改修炼进度。
     * @param amount 变化量
     * @return 修改后的进度
     */
    public static double modifyProgress(LivingEntity entity, double amount) {
        if (entity == null) {
            return 0.0;
        }
        try {
            var vars = getVariables(entity);
            double original = vars.gushi_xiulian_dangqian;
            
            // 假设进度没有严格上限（或者上限由进阶逻辑控制），这里只保证不为负
            // 如果需要上限（比如一转升二转的阈值），可能需要查阅更多逻辑，这里暂时只管加
            double newValue = Math.max(0, original + amount);

            if (Double.compare(original, newValue) != 0) {
                vars.gushi_xiulian_dangqian = newValue;
                PlayerVariablesSyncHelper.markSyncDirty(vars);
            }
            return newValue;
        } catch (Exception e) {
            return 0.0;
        }
    }

    private static GuzhenrenModVariables.PlayerVariables getVariables(LivingEntity entity) {
        return entity.getData(GuzhenrenModVariables.PLAYER_VARIABLES);
    }
}
