package com.Kizunad.guzhenrenext.guzhenrenBridge;

import net.guzhenren.network.GuzhenrenModVariables;
import net.minecraft.world.entity.LivingEntity;

/**
 * 流派（liupai_*）数据读取与写入工具。
 * <p>
 * 权威数据源：蛊真人本体 {@link GuzhenrenModVariables.PlayerVariables}（即 PLAYER_VARIABLES 附件）。
 * Guzhenren-ext 不需要猜 NBT Key；只需直接读写字段并标记同步即可。
 * </p>
 * <p>
 * 兼容策略：
 * <ul>
 *     <li>读取失败时返回 0，避免因版本差异导致崩服。</li>
 *     <li>写入后使用 {@link PlayerVariablesSyncHelper#markSyncDirty} 触发原模组同步逻辑。</li>
 * </ul>
 * </p>
 */
public final class LiuPaiHelper {

    private LiuPaiHelper() {}

    /**
     * 这里的枚举不是“道痕类型”，而是对应 GuzhenrenModVariables.PlayerVariables 的 liupai_* 字段。
     * <p>
     * 注意：原模组字段存在个别命名不一致/大小写异常（例如 {@code Liupai_gudao}）。
     * 本处显式映射，避免后续调用方误写字符串。
     * </p>
     */
    public enum LiuPaiType {
        HUN_DAO,
        GU_DAO,
        MU_DAO,
        HUO_DAO,
        JIAN_DAO,
        DAO_DAO,
        XUE_DAO,
        LIAN_DAO,
        LV_DAO,
        TIAN_DAO,
        QI_DAO,
        NU_DAO,
        TOU_DAO,
        XIN_DAO,
        YING_DAO,
        XING_DAO,
        YUE_DAO,
        YUN_DAO,
        ZHI_DAO,
        ZHEN_DAO,
        ZHOU_DAO,
        JIN_DAO,
        REN_DAO,
        LI_DAO,
        LEI_DAO,
        DU_DAO,
        SHUI_DAO,
        TU_DAO,
        YU_DAO,
        SHI_DAO,
        DAN_DAO,
        HUA_DAO,
        AN_DAO,
        HUAN_DAO,
        MENG_DAO,
        BING_DAO,
        BING_XUE_DAO,
        BIAN_HUA_DAO,
        FENG_DAO,
        GUANG_DAO,
        FEI_XING_DAO,
        QING_MEI_DAO,
        XU_DAO,
        YUN_DAO_2,
        JIN_DAO_2,
        YIN_DAO,
        GENERIC,
    }

    /**
     * 获取指定流派数量。
     */
    public static double getLiuPai(LivingEntity entity, LiuPaiType type) {
        final GuzhenrenModVariables.PlayerVariables vars = getVariables(entity);
        if (vars == null || type == null) {
            return 0.0;
        }
        try {
            return switch (type) {
                case HUN_DAO -> vars.liupai_hundao;
                case GU_DAO -> vars.Liupai_gudao; // 注意：原模组字段名首字母大写
                case MU_DAO -> vars.liupai_mudao;
                case HUO_DAO -> vars.liupai_yandao;
                case JIAN_DAO -> vars.liupai_jiandao;
                case DAO_DAO -> vars.liupai_daodao;
                case XUE_DAO -> vars.liupai_xuedao;
                case LIAN_DAO -> vars.liupai_liandao;
                case LV_DAO -> vars.liupai_lvdao;
                case TIAN_DAO -> vars.liupai_tiandao;
                case QI_DAO -> vars.liupai_qidao;
                case NU_DAO -> vars.liupai_nudao;
                case TOU_DAO -> vars.liupai_toudao;
                case XIN_DAO -> vars.liupai_xindao;
                case YING_DAO -> vars.liupai_yingdao;
                case XING_DAO -> vars.liupai_xingdao;
                case YUE_DAO -> vars.liupai_yuedao;
                case YUN_DAO -> vars.liupai_yundao;
                case ZHI_DAO -> vars.liupai_zhidao;
                case JIN_DAO -> vars.liupai_jindao;
                case REN_DAO -> vars.liupai_rendao;
                case LI_DAO -> vars.liupai_lidao;
                case LEI_DAO -> vars.liupai_leidao;
                case DU_DAO -> vars.liupai_dudao;
                case SHUI_DAO -> vars.liupai_shuidao;
                case TU_DAO -> vars.liupai_tudao;
                case YU_DAO -> vars.liupai_yudao;
                case BING_XUE_DAO -> vars.liupai_bingxuedao;
                case BIAN_HUA_DAO -> vars.liupai_bianhuadao;
                case FENG_DAO -> vars.liupai_fengdao;
                case GUANG_DAO -> vars.liupai_guangdao;
                case AN_DAO -> vars.liupai_andao;
                case ZHEN_DAO -> vars.liupai_zhendao;
                case ZHOU_DAO -> vars.liupai_zhoudao;
                case MENG_DAO -> vars.liupai_mengdao;
                case HUAN_DAO -> vars.liupai_huandao;
                case BING_DAO -> vars.liupai_bingdao;
                case FEI_XING_DAO -> vars.liupai_feixingdao;
                case QING_MEI_DAO -> vars.liupai_qingmeidao;
                case XU_DAO -> vars.liupai_xudao;
                case YUN_DAO_2 -> vars.liupai_yundao2;
                case JIN_DAO_2 -> vars.liupai_jindao2;
                case YIN_DAO -> vars.liupai_yindao;
                case DAN_DAO -> vars.liupai_dandao;
                case HUA_DAO -> vars.liupai_huadao;
                case SHI_DAO -> vars.liupai_shidao;
                default -> 0.0;
            };
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 设置指定流派数量。
     * <p>
     * 说明：该方法会将值钳制为非负，并触发原模组同步（_syncDirty）。
     * </p>
     */
    public static void setLiuPai(LivingEntity entity, LiuPaiType type, double value) {
        final GuzhenrenModVariables.PlayerVariables vars = getVariables(entity);
        if (vars == null || type == null) {
            return;
        }
        final double clamped = Math.max(0.0, value);
        try {
            switch (type) {
                case HUN_DAO -> vars.liupai_hundao = clamped;
                case GU_DAO -> vars.Liupai_gudao = clamped;
                case MU_DAO -> vars.liupai_mudao = clamped;
                case HUO_DAO -> vars.liupai_yandao = clamped;
                case JIAN_DAO -> vars.liupai_jiandao = clamped;
                case DAO_DAO -> vars.liupai_daodao = clamped;
                case XUE_DAO -> vars.liupai_xuedao = clamped;
                case LIAN_DAO -> vars.liupai_liandao = clamped;
                case LV_DAO -> vars.liupai_lvdao = clamped;
                case TIAN_DAO -> vars.liupai_tiandao = clamped;
                case QI_DAO -> vars.liupai_qidao = clamped;
                case NU_DAO -> vars.liupai_nudao = clamped;
                case TOU_DAO -> vars.liupai_toudao = clamped;
                case XIN_DAO -> vars.liupai_xindao = clamped;
                case YING_DAO -> vars.liupai_yingdao = clamped;
                case XING_DAO -> vars.liupai_xingdao = clamped;
                case YUE_DAO -> vars.liupai_yuedao = clamped;
                case YUN_DAO -> vars.liupai_yundao = clamped;
                case ZHI_DAO -> vars.liupai_zhidao = clamped;
                case JIN_DAO -> vars.liupai_jindao = clamped;
                case REN_DAO -> vars.liupai_rendao = clamped;
                case LI_DAO -> vars.liupai_lidao = clamped;
                case LEI_DAO -> vars.liupai_leidao = clamped;
                case DU_DAO -> vars.liupai_dudao = clamped;
                case SHUI_DAO -> vars.liupai_shuidao = clamped;
                case TU_DAO -> vars.liupai_tudao = clamped;
                case YU_DAO -> vars.liupai_yudao = clamped;
                case BING_XUE_DAO -> vars.liupai_bingxuedao = clamped;
                case BIAN_HUA_DAO -> vars.liupai_bianhuadao = clamped;
                case FENG_DAO -> vars.liupai_fengdao = clamped;
                case GUANG_DAO -> vars.liupai_guangdao = clamped;
                case AN_DAO -> vars.liupai_andao = clamped;
                case ZHEN_DAO -> vars.liupai_zhendao = clamped;
                case ZHOU_DAO -> vars.liupai_zhoudao = clamped;
                case MENG_DAO -> vars.liupai_mengdao = clamped;
                case HUAN_DAO -> vars.liupai_huandao = clamped;
                case BING_DAO -> vars.liupai_bingdao = clamped;
                case FEI_XING_DAO -> vars.liupai_feixingdao = clamped;
                case QING_MEI_DAO -> vars.liupai_qingmeidao = clamped;
                case XU_DAO -> vars.liupai_xudao = clamped;
                case YUN_DAO_2 -> vars.liupai_yundao2 = clamped;
                case JIN_DAO_2 -> vars.liupai_jindao2 = clamped;
                case YIN_DAO -> vars.liupai_yindao = clamped;
                case DAN_DAO -> vars.liupai_dandao = clamped;
                case HUA_DAO -> vars.liupai_huadao = clamped;
                case SHI_DAO -> vars.liupai_shidao = clamped;
                default -> {
                    // 未支持的字段，忽略
                    return;
                }
            }
        } catch (Exception e) {
            return;
        }
        PlayerVariablesSyncHelper.markSyncDirty(vars);
    }

    /**
     * 增加指定流派数量。
     */
    public static void addLiuPai(LivingEntity entity, LiuPaiType type, double delta) {
        if (delta == 0.0) {
            return;
        }
        final double current = getLiuPai(entity, type);
        setLiuPai(entity, type, current + delta);
    }

    private static GuzhenrenModVariables.PlayerVariables getVariables(LivingEntity entity) {
        if (entity == null) {
            return null;
        }
        try {
            return entity.getData(GuzhenrenModVariables.PLAYER_VARIABLES);
        } catch (Exception e) {
            return null;
        }
    }
}
