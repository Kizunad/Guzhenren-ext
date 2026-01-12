package com.Kizunad.guzhenrenext.guzhenrenBridge;

import net.guzhenren.network.GuzhenrenModVariables;
import net.minecraft.world.entity.LivingEntity;


/**
 * 道痕数据读取与写入工具。
 * <p>
 * 负责从 GuzhenrenModVariables 中读取各流派道痕数量并提供写入接口。
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
        QI_DAO("qidao"), // 气道（库变量：dahen_qidao，少了一个 o）
        NU_DAO("nudao"), // 奴道（库变量：dahen_nudao，少了一个 o）
        TOU_DAO("toudao"), // 偷道（库变量：daohen_toudao）
        XIN_DAO("xindao"), // 信道（库变量：daohen_xindao）
        YING_DAO("yingdao"), // 影道（库变量：daohen_yingdao）
        XING_DAO("xingdao"), // 星道（库变量：dahen_xingdao，少了一个 o）
        YUE_DAO("yuedao"), // 月道（库变量：daohen_yuedao）
        YUN_DAO("yundao"), // 云道（库变量：daohen_yundao）
        YUN_DAO_2("yundao2"), // 云道2（库变量：daohen_yundao2）
        ZHI_DAO("zhidao"), // 智道（库变量：dahen_zhidao，少了一个 o）
        ZHEN_DAO("zhendao"), // 阵道（库变量：dahen_zhendao，少了一个 o）
        ZHOU_DAO("zhoudao"), // 宙道（库变量：dahen_zhoudao，少了一个 o）
        JIN_DAO("jindao"), // 金道（库变量：daohen_jindao）
        JIN_DAO_2("jindao2"), // 金道2（库变量：daohen_jindao2）
        YIN_DAO("yindao"), // 阴道（库变量：daohen_yindao）
        REN_DAO("rendao"), // 人道（库变量：dahen_rendao，少了一个 o）
        LI_DAO("lidao"), // 力道（库变量：daohen_lidao）
        LEI_DAO("leidao"), // 雷道（库变量：daohen_leidao）
        DU_DAO("dudao"), // 毒道（库变量：daohen_dudao）
        SHUI_DAO("shuidao"), // 水道（库变量：daohen_shuidao）
        TU_DAO("tudao"), // 土道（库变量：daohen_tudao）
        YU_DAO("yudao"), // 宇道（库变量：daohen_yudao）
        SHI_DAO("shidao"), // 食道（库变量：daohen_shidao）
        DAN_DAO("dandao"), // 丹道（库变量：daohen_dandao）
        HUA_DAO("huadao"), // 画道（库变量：daohen_huadao）
        AN_DAO("andao"), // 暗道（库变量：daohen_andao）
        HUAN_DAO("huandao"), // 幻道（库变量：daohen_huandao）
        MENG_DAO("mengdao"), // 梦道（库变量：daohen_mengdao）
        BING_DAO("bingdao"), // 冰道（库变量：daohen_bingdao）
        BING_XUE_DAO("bingxuedao"), // 冰雪道（库变量：daohen_bingxuedao）
        BIAN_HUA_DAO("bianhuadao"), // 变化道（库变量：daohen_bianhuadao）
        XU_DAO("xudao"), // 虚道（库变量：daohen_xudao）
        FENG_DAO("fengdao"), // 风道（库变量：daohen_fengdao）
        GUANG_DAO("guangdao"), // 光道（库变量：daohen_guangdao）
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
        if (entity == null || type == null) {
            return 0.0;
        }
        try {
            var vars = entity.getData(GuzhenrenModVariables.PLAYER_VARIABLES);

            return switch (type) {
                case HUN_DAO -> vars.daohen_hundao;
                case GU_DAO -> vars.daohen_gudao;
                case MU_DAO -> vars.daohen_mudao;
                case HUO_DAO -> vars.daohen_yandao;
                case JIAN_DAO -> vars.daohen_jiandao;
                case DAO_DAO -> vars.daohen_daodao;
                case XUE_DAO -> vars.daohen_xuedao;
                case LIAN_DAO -> vars.dahen_liandao; // 少一个 o
                case LV_DAO -> vars.daohen_lvdao;
                case TIAN_DAO -> vars.dahen_tiandao; // 少一个 o
                case QI_DAO -> vars.dahen_qidao; // 少一个 o
                case NU_DAO -> vars.dahen_nudao; // 少一个 o
                case TOU_DAO -> vars.daohen_toudao;
                case XIN_DAO -> vars.daohen_xindao;
                case YING_DAO -> vars.daohen_yingdao;
                case XING_DAO -> vars.dahen_xingdao; // 少一个 o
                case YUE_DAO -> vars.daohen_yuedao;
                case YUN_DAO -> vars.daohen_yundao;
                case YUN_DAO_2 -> vars.daohen_yundao2;
                case ZHI_DAO -> vars.dahen_zhidao; // 少一个 o
                case ZHEN_DAO -> vars.dahen_zhendao; // 少一个 o
                case ZHOU_DAO -> vars.dahen_zhoudao; // 少一个 o
                case JIN_DAO -> vars.daohen_jindao;
                case JIN_DAO_2 -> vars.daohen_jindao2;
                case YIN_DAO -> vars.daohen_yindao;
                case REN_DAO -> vars.dahen_rendao; // 少一个 o
                case LI_DAO -> vars.daohen_lidao;
                case LEI_DAO -> vars.daohen_leidao;
                case DU_DAO -> vars.daohen_dudao;
                case SHUI_DAO -> vars.daohen_shuidao;
                case TU_DAO -> vars.daohen_tudao;
                case YU_DAO -> vars.daohen_yudao;
                case SHI_DAO -> vars.daohen_shidao;
                case DAN_DAO -> vars.daohen_dandao;
                case HUA_DAO -> vars.daohen_huadao;
                case AN_DAO -> vars.daohen_andao;
                case HUAN_DAO -> vars.daohen_huandao;
                case MENG_DAO -> vars.daohen_mengdao;
                case BING_DAO -> vars.daohen_bingdao;
                case BING_XUE_DAO -> vars.daohen_bingxuedao;
                case BIAN_HUA_DAO -> vars.daohen_bianhuadao;
                case XU_DAO -> vars.daohen_xudao;
                case FENG_DAO -> vars.daohen_fengdao;
                case GUANG_DAO -> vars.daohen_guangdao;
                case GENERIC -> 0.0;
                default -> 0.0;
            };
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

    /**
     * 设置指定流派道痕数量，并触发原模组同步。
     */
    public static void setDaoHen(LivingEntity entity, DaoType type, double value) {
        if (entity == null || type == null) {
            return;
        }
        try {
            GuzhenrenModVariables.PlayerVariables vars = entity.getData(
                GuzhenrenModVariables.PLAYER_VARIABLES
            );
            final double clamped = Math.max(0.0, value);
            switch (type) {
                case HUN_DAO -> vars.daohen_hundao = clamped;
                case GU_DAO -> vars.daohen_gudao = clamped;
                case MU_DAO -> vars.daohen_mudao = clamped;
                case HUO_DAO -> vars.daohen_yandao = clamped;
                case JIAN_DAO -> vars.daohen_jiandao = clamped;
                case DAO_DAO -> vars.daohen_daodao = clamped;
                case XUE_DAO -> vars.daohen_xuedao = clamped;
                case LIAN_DAO -> vars.dahen_liandao = clamped; // 少一个 o
                case LV_DAO -> vars.daohen_lvdao = clamped;
                case TIAN_DAO -> vars.dahen_tiandao = clamped; // 少一个 o
                case QI_DAO -> vars.dahen_qidao = clamped; // 少一个 o
                case NU_DAO -> vars.dahen_nudao = clamped; // 少一个 o
                case TOU_DAO -> vars.daohen_toudao = clamped;
                case XIN_DAO -> vars.daohen_xindao = clamped;
                case YING_DAO -> vars.daohen_yingdao = clamped;
                case XING_DAO -> vars.dahen_xingdao = clamped; // 少一个 o
                case YUE_DAO -> vars.daohen_yuedao = clamped;
                case YUN_DAO -> vars.daohen_yundao = clamped;
                case YUN_DAO_2 -> vars.daohen_yundao2 = clamped;
                case ZHI_DAO -> vars.dahen_zhidao = clamped; // 少一个 o
                case ZHEN_DAO -> vars.dahen_zhendao = clamped; // 少一个 o
                case ZHOU_DAO -> vars.dahen_zhoudao = clamped; // 少一个 o
                case JIN_DAO -> vars.daohen_jindao = clamped;
                case JIN_DAO_2 -> vars.daohen_jindao2 = clamped;
                case YIN_DAO -> vars.daohen_yindao = clamped;
                case REN_DAO -> vars.dahen_rendao = clamped; // 少一个 o
                case LI_DAO -> vars.daohen_lidao = clamped;
                case LEI_DAO -> vars.daohen_leidao = clamped;
                case DU_DAO -> vars.daohen_dudao = clamped;
                case SHUI_DAO -> vars.daohen_shuidao = clamped;
                case TU_DAO -> vars.daohen_tudao = clamped;
                case YU_DAO -> vars.daohen_yudao = clamped;
                case SHI_DAO -> vars.daohen_shidao = clamped;
                case DAN_DAO -> vars.daohen_dandao = clamped;
                case HUA_DAO -> vars.daohen_huadao = clamped;
                case AN_DAO -> vars.daohen_andao = clamped;
                case HUAN_DAO -> vars.daohen_huandao = clamped;
                case MENG_DAO -> vars.daohen_mengdao = clamped;
                case BING_DAO -> vars.daohen_bingdao = clamped;
                case BING_XUE_DAO -> vars.daohen_bingxuedao = clamped;
                case BIAN_HUA_DAO -> vars.daohen_bianhuadao = clamped;
                case XU_DAO -> vars.daohen_xudao = clamped;
                case FENG_DAO -> vars.daohen_fengdao = clamped;
                case GUANG_DAO -> vars.daohen_guangdao = clamped;
                case GENERIC -> {
                    return;
                }
                default -> {
                    return;
                }
            }
            PlayerVariablesSyncHelper.markSyncDirty(vars);
        } catch (Exception ignored) {
            // 兼容性：未找到变量或字段异常直接吞掉，避免崩服
        }
    }

    /**
     * 增量修改指定流派道痕。
     */
    public static void addDaoHen(LivingEntity entity, DaoType type, double delta) {
        if (delta == 0.0) {
            return;
        }
        final double current = getDaoHen(entity, type);
        setDaoHen(entity, type, current + delta);
    }
}
