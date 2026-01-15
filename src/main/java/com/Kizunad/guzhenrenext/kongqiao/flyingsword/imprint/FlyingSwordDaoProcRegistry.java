package com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint.FlyingSwordImprintProcTuning.AMP_0;
import static com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint.FlyingSwordImprintProcTuning.AMP_1;
import static com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint.FlyingSwordImprintProcTuning.DURATION_TICKS_DEFAULT;
import static com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint.FlyingSwordImprintProcTuning.DURATION_TICKS_LONG;
import static com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint.FlyingSwordImprintProcTuning.DURATION_TICKS_NONE;
import static com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint.FlyingSwordImprintProcTuning.DURATION_TICKS_SHORT;
import static com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint.FlyingSwordImprintProcTuning.POWER_AOE_DAMAGE_LIGHT;
import static com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint.FlyingSwordImprintProcTuning.POWER_AOE_DAMAGE_MEDIUM;
import static com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint.FlyingSwordImprintProcTuning.POWER_BONUS_DAMAGE_HEAVY;
import static com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint.FlyingSwordImprintProcTuning.POWER_BONUS_DAMAGE_LIGHT;
import static com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint.FlyingSwordImprintProcTuning.POWER_BONUS_DAMAGE_MEDIUM;
import static com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint.FlyingSwordImprintProcTuning.POWER_CHAIN_DAMAGE_RATIO;
import static com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint.FlyingSwordImprintProcTuning.POWER_DURABILITY_RESTORE_HEAVY;
import static com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint.FlyingSwordImprintProcTuning.POWER_DURABILITY_RESTORE_LIGHT;
import static com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint.FlyingSwordImprintProcTuning.POWER_KNOCKBACK_HEAVY;
import static com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint.FlyingSwordImprintProcTuning.POWER_KNOCKBACK_LIGHT;
import static com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint.FlyingSwordImprintProcTuning.POWER_ONE;
import static com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint.FlyingSwordImprintProcTuning.RANGE_AOE_LARGE;
import static com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint.FlyingSwordImprintProcTuning.RANGE_AOE_MEDIUM;
import static com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint.FlyingSwordImprintProcTuning.RANGE_CHAIN;
import static com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint.FlyingSwordImprintProcTuning.RANGE_ZERO;

public final class FlyingSwordDaoProcRegistry {

    private static final Map<String, ProcSpec> PROC_BY_DAO_KEY =
        createRegistry();

    private FlyingSwordDaoProcRegistry() {}

    public static ProcSpec get(String daoKey) {
        if (daoKey == null || daoKey.isBlank()) {
            return null;
        }
        return PROC_BY_DAO_KEY.get(daoKey);
    }

    private static Map<String, ProcSpec> createRegistry() {
        Map<String, ProcSpec> map = new HashMap<>();

        map.put(DaoHenHelper.DaoType.JIAN_DAO.getKey(), ProcSpec.jianDao());
        map.put(DaoHenHelper.DaoType.HUO_DAO.getKey(), ProcSpec.yanDao());
        map.put(DaoHenHelper.DaoType.LEI_DAO.getKey(), ProcSpec.leiDao());

        map.put(DaoHenHelper.DaoType.SHUI_DAO.getKey(), ProcSpec.shuiDao());
        map.put(DaoHenHelper.DaoType.BING_XUE_DAO.getKey(), ProcSpec.bingXueDao());
        map.put(DaoHenHelper.DaoType.FENG_DAO.getKey(), ProcSpec.fengDao());
        map.put(DaoHenHelper.DaoType.TU_DAO.getKey(), ProcSpec.tuDao());
        map.put(DaoHenHelper.DaoType.MU_DAO.getKey(), ProcSpec.muDao());
        map.put(DaoHenHelper.DaoType.JIN_DAO.getKey(), ProcSpec.jinDao());
        map.put(DaoHenHelper.DaoType.DU_DAO.getKey(), ProcSpec.duDao());
        map.put(DaoHenHelper.DaoType.XUE_DAO.getKey(), ProcSpec.xueDao());
        map.put(DaoHenHelper.DaoType.HUN_DAO.getKey(), ProcSpec.hunDao());
        map.put(DaoHenHelper.DaoType.GU_DAO.getKey(), ProcSpec.guDao());
        map.put(DaoHenHelper.DaoType.GUANG_DAO.getKey(), ProcSpec.guangDao());
        map.put(DaoHenHelper.DaoType.YING_DAO.getKey(), ProcSpec.yingDao());
        map.put(DaoHenHelper.DaoType.XING_DAO.getKey(), ProcSpec.xingDao());
        map.put(DaoHenHelper.DaoType.YUE_DAO.getKey(), ProcSpec.yueDao());
        map.put(DaoHenHelper.DaoType.YUN_DAO.getKey(), ProcSpec.yunDao());
        map.put(DaoHenHelper.DaoType.ZHI_DAO.getKey(), ProcSpec.zhiDao());
        map.put(DaoHenHelper.DaoType.ZHOU_DAO.getKey(), ProcSpec.zhouDao());
        map.put(DaoHenHelper.DaoType.DAO_DAO.getKey(), ProcSpec.daoDao());
        map.put(DaoHenHelper.DaoType.LI_DAO.getKey(), ProcSpec.liDao());
        map.put(DaoHenHelper.DaoType.REN_DAO.getKey(), ProcSpec.renDao());
        map.put(DaoHenHelper.DaoType.LV_DAO.getKey(), ProcSpec.lvDao());
        map.put(DaoHenHelper.DaoType.TIAN_DAO.getKey(), ProcSpec.tianDao());
        map.put(DaoHenHelper.DaoType.TOU_DAO.getKey(), ProcSpec.touDao());
        map.put(DaoHenHelper.DaoType.YU_DAO.getKey(), ProcSpec.yuDao());
        map.put(DaoHenHelper.DaoType.SHI_DAO.getKey(), ProcSpec.shiDao());
        map.put(DaoHenHelper.DaoType.BIAN_HUA_DAO.getKey(), ProcSpec.bianHuaDao());

        return Collections.unmodifiableMap(map);
    }


    public record ProcSpec(
        String procId,
        EffectType type,
        double power,
        int durationTicks,
        int amplifier,
        double range,
        TargetEffect targetEffect
    ) {

        public enum EffectType {
            BONUS_DAMAGE,
            IGNITE,
            CHAIN_DAMAGE,
            APPLY_EFFECT,
            KNOCKBACK,
            DURABILITY_RESTORE,
            AOE_DAMAGE,
            CLEANSE_TARGET
        }

        public enum TargetEffect {
            SLOW,
            WEAKNESS,
            POISON,
            WITHER,
            GLOWING,
            DARKNESS,
            BLINDNESS,
            HUNGER,
            LEVITATION
        }

        public static ProcSpec jianDao() {
            return new ProcSpec(
                "jiandao",
                EffectType.BONUS_DAMAGE,
                POWER_ONE,
                DURATION_TICKS_NONE,
                AMP_0,
                RANGE_ZERO,
                null
            );
        }

        public static ProcSpec yanDao() {
            return new ProcSpec(
                "yandao",
                EffectType.IGNITE,
                POWER_ONE,
                DURATION_TICKS_NONE,
                AMP_0,
                RANGE_ZERO,
                null
            );
        }

        public static ProcSpec leiDao() {
            return new ProcSpec(
                "leidao",
                EffectType.CHAIN_DAMAGE,
                POWER_CHAIN_DAMAGE_RATIO,
                DURATION_TICKS_NONE,
                AMP_0,
                RANGE_CHAIN,
                null
            );
        }

        public static ProcSpec shuiDao() {
            return new ProcSpec(
                "shuidao",
                EffectType.APPLY_EFFECT,
                POWER_ONE,
                DURATION_TICKS_SHORT,
                AMP_0,
                RANGE_ZERO,
                ProcSpec.TargetEffect.SLOW
            );
        }

        public static ProcSpec bingXueDao() {
            return new ProcSpec(
                "bingxuedao",
                EffectType.APPLY_EFFECT,
                POWER_ONE,
                DURATION_TICKS_DEFAULT,
                AMP_1,
                RANGE_ZERO,
                ProcSpec.TargetEffect.SLOW
            );
        }

        public static ProcSpec fengDao() {
            return new ProcSpec(
                "fengdao",
                EffectType.KNOCKBACK,
                POWER_KNOCKBACK_LIGHT,
                DURATION_TICKS_NONE,
                AMP_0,
                RANGE_ZERO,
                null
            );
        }

        public static ProcSpec tuDao() {
            return new ProcSpec(
                "tudao",
                EffectType.APPLY_EFFECT,
                POWER_ONE,
                DURATION_TICKS_DEFAULT,
                AMP_1,
                RANGE_ZERO,
                ProcSpec.TargetEffect.SLOW
            );
        }

        public static ProcSpec muDao() {
            return new ProcSpec(
                "mudao",
                EffectType.APPLY_EFFECT,
                POWER_ONE,
                DURATION_TICKS_DEFAULT,
                AMP_0,
                RANGE_ZERO,
                ProcSpec.TargetEffect.WEAKNESS
            );
        }

        public static ProcSpec jinDao() {
            return new ProcSpec(
                "jindao",
                EffectType.BONUS_DAMAGE,
                POWER_BONUS_DAMAGE_HEAVY,
                DURATION_TICKS_NONE,
                AMP_0,
                RANGE_ZERO,
                null
            );
        }

        public static ProcSpec duDao() {
            return new ProcSpec(
                "dudao",
                EffectType.APPLY_EFFECT,
                POWER_ONE,
                DURATION_TICKS_DEFAULT,
                AMP_0,
                RANGE_ZERO,
                ProcSpec.TargetEffect.POISON
            );
        }

        public static ProcSpec xueDao() {
            return new ProcSpec(
                "xuedao",
                EffectType.APPLY_EFFECT,
                POWER_ONE,
                DURATION_TICKS_DEFAULT,
                AMP_0,
                RANGE_ZERO,
                ProcSpec.TargetEffect.WITHER
            );
        }

        public static ProcSpec hunDao() {
            return new ProcSpec(
                "hundao",
                EffectType.DURABILITY_RESTORE,
                POWER_DURABILITY_RESTORE_HEAVY,
                DURATION_TICKS_NONE,
                AMP_0,
                RANGE_ZERO,
                null
            );
        }

        public static ProcSpec guDao() {
            return new ProcSpec(
                "gudao",
                EffectType.APPLY_EFFECT,
                POWER_ONE,
                DURATION_TICKS_DEFAULT,
                AMP_0,
                RANGE_ZERO,
                ProcSpec.TargetEffect.WEAKNESS
            );
        }

        public static ProcSpec guangDao() {
            return new ProcSpec(
                "guangdao",
                EffectType.APPLY_EFFECT,
                POWER_ONE,
                DURATION_TICKS_LONG,
                AMP_0,
                RANGE_ZERO,
                ProcSpec.TargetEffect.GLOWING
            );
        }

        public static ProcSpec yingDao() {
            return new ProcSpec(
                "yingdao",
                EffectType.APPLY_EFFECT,
                POWER_ONE,
                DURATION_TICKS_DEFAULT,
                AMP_0,
                RANGE_ZERO,
                ProcSpec.TargetEffect.DARKNESS
            );
        }

        public static ProcSpec xingDao() {
            return new ProcSpec(
                "xingdao",
                EffectType.AOE_DAMAGE,
                POWER_AOE_DAMAGE_MEDIUM,
                DURATION_TICKS_NONE,
                AMP_0,
                RANGE_AOE_MEDIUM,
                null
            );
        }

        public static ProcSpec yueDao() {
            return new ProcSpec(
                "yuedao",
                EffectType.APPLY_EFFECT,
                POWER_ONE,
                DURATION_TICKS_LONG,
                AMP_0,
                RANGE_ZERO,
                ProcSpec.TargetEffect.BLINDNESS
            );
        }

        public static ProcSpec yunDao() {
            return new ProcSpec(
                "yundao",
                EffectType.APPLY_EFFECT,
                POWER_ONE,
                DURATION_TICKS_DEFAULT,
                AMP_0,
                RANGE_ZERO,
                ProcSpec.TargetEffect.BLINDNESS
            );
        }

        public static ProcSpec zhiDao() {
            return new ProcSpec(
                "zhidao",
                EffectType.APPLY_EFFECT,
                POWER_ONE,
                DURATION_TICKS_LONG,
                AMP_0,
                RANGE_ZERO,
                ProcSpec.TargetEffect.GLOWING
            );
        }

        public static ProcSpec zhouDao() {
            return new ProcSpec(
                "zhoudao",
                EffectType.CLEANSE_TARGET,
                POWER_ONE,
                DURATION_TICKS_NONE,
                AMP_0,
                RANGE_ZERO,
                null
            );
        }

        public static ProcSpec daoDao() {
            return new ProcSpec(
                "daodao",
                EffectType.AOE_DAMAGE,
                POWER_AOE_DAMAGE_LIGHT,
                DURATION_TICKS_NONE,
                AMP_0,
                RANGE_AOE_LARGE,
                null
            );
        }

        public static ProcSpec liDao() {
            return new ProcSpec(
                "lidao",
                EffectType.KNOCKBACK,
                POWER_KNOCKBACK_HEAVY,
                DURATION_TICKS_NONE,
                AMP_0,
                RANGE_ZERO,
                null
            );
        }

        public static ProcSpec renDao() {
            return new ProcSpec(
                "rendao",
                EffectType.APPLY_EFFECT,
                POWER_ONE,
                DURATION_TICKS_DEFAULT,
                AMP_0,
                RANGE_ZERO,
                ProcSpec.TargetEffect.WEAKNESS
            );
        }

        public static ProcSpec lvDao() {
            return new ProcSpec(
                "lvdao",
                EffectType.CLEANSE_TARGET,
                POWER_ONE,
                DURATION_TICKS_NONE,
                AMP_0,
                RANGE_ZERO,
                null
            );
        }

        public static ProcSpec tianDao() {
            return new ProcSpec(
                "tiandao",
                EffectType.BONUS_DAMAGE,
                POWER_BONUS_DAMAGE_LIGHT,
                DURATION_TICKS_NONE,
                AMP_0,
                RANGE_ZERO,
                null
            );
        }

        public static ProcSpec touDao() {
            return new ProcSpec(
                "toudao",
                EffectType.DURABILITY_RESTORE,
                POWER_DURABILITY_RESTORE_LIGHT,
                DURATION_TICKS_NONE,
                AMP_0,
                RANGE_ZERO,
                null
            );
        }

        public static ProcSpec yuDao() {
            return new ProcSpec(
                "yudao",
                EffectType.BONUS_DAMAGE,
                POWER_BONUS_DAMAGE_MEDIUM,
                DURATION_TICKS_NONE,
                AMP_0,
                RANGE_ZERO,
                null
            );
        }

        public static ProcSpec shiDao() {
            return new ProcSpec(
                "shidao",
                EffectType.APPLY_EFFECT,
                POWER_ONE,
                DURATION_TICKS_DEFAULT,
                AMP_0,
                RANGE_ZERO,
                ProcSpec.TargetEffect.HUNGER
            );
        }

        public static ProcSpec bianHuaDao() {
            return new ProcSpec(
                "bianhuadao",
                EffectType.APPLY_EFFECT,
                POWER_ONE,
                DURATION_TICKS_DEFAULT,
                AMP_0,
                RANGE_ZERO,
                ProcSpec.TargetEffect.WEAKNESS
            );
        }
    }
}
