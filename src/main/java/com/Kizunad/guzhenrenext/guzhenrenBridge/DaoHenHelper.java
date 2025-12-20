package com.Kizunad.guzhenrenext.guzhenrenBridge;

import net.guzhenren.network.GuzhenrenModVariables;
import net.minecraft.world.entity.LivingEntity;

/**
 * 道痕数据读取工具。
 * <p>
 * 负责从 GuzhenrenModVariables 中读取各流派道痕数量。
 * </p>
 */
public final class DaoHenHelper {

    private DaoHenHelper() {}

    public enum DaoType {
        HUN_DAO("hundao"), // 魂道
        GU_DAO("gudao"), // 骨道
        ZHI_DAO("zhidao"), // 智道
        JIN_DAO("jindao"), // 金道
        LI_DAO("lidao"), // 力道
        TU_DAO("tudao"), // 土道
        BIAN_HUA_DAO("bianhuadao"), // 变化道
        FENG_DAO("fengdao"), // 风道
        NU_DAO("nudao"), // 奴道
        // ... 根据需要添加其他流派
        GENERIC("generic"); // 通用/无属性

        private final String key;

        DaoType(String key) {
            this.key = key;
        }
    }

    /**
     * 获取指定流派的道痕数量。
     */
    public static double getDaoHen(LivingEntity entity, DaoType type) {
        if (entity == null) {
            return 0.0;
        }
        try {
            var vars = entity.getData(GuzhenrenModVariables.PLAYER_VARIABLES);

            // 目前只针对已知变量名做映射，需根据实际 Mod 变量名完善
            switch (type) {
                case HUN_DAO:
                    return vars.daohen_hundao;
                case GU_DAO:
                    return vars.daohen_gudao;
                case ZHI_DAO:
                    // 注意：库中变量名为 dahen_zhidao（少了一个 o）
                    return vars.dahen_zhidao;
                case JIN_DAO:
                    return vars.daohen_jindao;
                case LI_DAO:
                    return vars.daohen_lidao;
                case TU_DAO:
                    return vars.daohen_tudao;
                case BIAN_HUA_DAO:
                    return vars.daohen_bianhuadao;
                case FENG_DAO: return vars.daohen_fengdao;
                case NU_DAO: return vars.dahen_nudao; // 注意：库中变量名为 dahen_nudao (少了个o)
                default: return 0.0;
            }
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 获取总道痕数量。
     */
    public static double getTotalDaoHen(LivingEntity entity) {
        if (entity == null) {
            return 0.0;
        }
        try {
            return entity.getData(
                GuzhenrenModVariables.PLAYER_VARIABLES
            ).daohen_zong;
        } catch (Exception e) {
            return 0.0;
        }
    }
}
