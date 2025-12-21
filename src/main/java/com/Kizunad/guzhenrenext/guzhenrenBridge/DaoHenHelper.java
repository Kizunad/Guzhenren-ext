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
        MU_DAO("mudao"), // 木道
        HUO_DAO("yandao"), // 火/炎道（库变量：daohen_yandao）
        JIAN_DAO("jiandao"), // 剑道（库变量：daohen_jiandao）
        DAO_DAO("daodao"), // 刀道（库变量：daohen_daodao）
        XUE_DAO("xuedao"), // 血道（库变量：daohen_xuedao）
        LIAN_DAO("liandao"), // 炼道（库变量：dahen_liandao，少了一个 o）
        LV_DAO("lvdao"), // 律道（库变量：daohen_lvdao）
        TIAN_DAO("tiandao"), // 天道（库变量：dahen_tiandao，少了一个 o）
        TOU_DAO("toudao"), // 偷道（库变量：daohen_toudao）
        XIN_DAO("xindao"), // 信道（库变量：daohen_xindao）
        YING_DAO("yingdao"), // 影道（库变量：daohen_yingdao）
        XING_DAO("xingdao"), // 星道
        YUE_DAO("yuedao"), // 月道
        YUN_DAO("yundao"), // 云道（库变量：daohen_yundao）
        ZHI_DAO("zhidao"), // 智道
        JIN_DAO("jindao"), // 金道
        REN_DAO("rendao"), // 人道（库变量：dahen_rendao，少了一个 o）
        LI_DAO("lidao"), // 力道
        LEI_DAO("leidao"), // 雷道
        DU_DAO("dudao"), // 毒道
        SHUI_DAO("shuidao"), // 水道
        TU_DAO("tudao"), // 土道
        YU_DAO("yudao"), // 宇道
        SHI_DAO("shidao"), // 食道
        BING_XUE_DAO("bingxuedao"), // 冰雪道
        BIAN_HUA_DAO("bianhuadao"), // 变化道
        FENG_DAO("fengdao"), // 风道
        GUANG_DAO("guangdao"), // 光道
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
                case MU_DAO:
                    return vars.daohen_mudao;
                case HUO_DAO:
                    return vars.daohen_yandao;
                case JIAN_DAO:
                    return vars.daohen_jiandao;
                case DAO_DAO:
                    return vars.daohen_daodao;
                case XUE_DAO:
                    return vars.daohen_xuedao;
                case LIAN_DAO:
                    // 注意：库中变量名为 dahen_liandao（少了一个 o）
                    return vars.dahen_liandao;
                case LV_DAO:
                    return vars.daohen_lvdao;
                case TIAN_DAO:
                    // 注意：库中变量名为 dahen_tiandao（少了一个 o）
                    return vars.dahen_tiandao;
                case TOU_DAO:
                    return vars.daohen_toudao;
                case XIN_DAO:
                    return vars.daohen_xindao;
                case YING_DAO:
                    return vars.daohen_yingdao;
                case XING_DAO:
                    // 注意：库中变量名为 dahen_xingdao（少了一个 o）
                    return vars.dahen_xingdao;
                case YUE_DAO:
                    return vars.daohen_yuedao;
                case YUN_DAO:
                    return vars.daohen_yundao;
                case ZHI_DAO:
                    // 注意：库中变量名为 dahen_zhidao（少了一个 o）
                    return vars.dahen_zhidao;
                case JIN_DAO:
                    return vars.daohen_jindao;
                case REN_DAO:
                    // 注意：库中变量名为 dahen_rendao（少了一个 o）
                    return vars.dahen_rendao;
                case LI_DAO:
                    return vars.daohen_lidao;
                case LEI_DAO:
                    return vars.daohen_leidao;
                case DU_DAO:
                    return vars.daohen_dudao;
                case SHUI_DAO:
                    return vars.daohen_shuidao;
                case TU_DAO:
                    return vars.daohen_tudao;
                case YU_DAO:
                    return vars.daohen_yudao;
                case BING_XUE_DAO:
                    return vars.daohen_bingxuedao;
                case BIAN_HUA_DAO:
                    return vars.daohen_bianhuadao;
                case FENG_DAO: return vars.daohen_fengdao;
                case GUANG_DAO: return vars.daohen_guangdao;
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
