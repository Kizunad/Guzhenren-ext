package com.Kizunad.guzhenrenext.damage;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public final class GuzhenrenExtDamageTypes {

    public static final ResourceKey<DamageType> AN_DAO = key("an_dao");
    public static final ResourceKey<DamageType> BIAN_HUA_DAO = key("bian_hua_dao");
    public static final ResourceKey<DamageType> BING_DAO = key("bing_dao");
    public static final ResourceKey<DamageType> BING_XUE_DAO = key("bing_xue_dao");
    public static final ResourceKey<DamageType> CHAOS_EROSION = key("chaos_erosion");
    public static final ResourceKey<DamageType> DAN_DAO = key("dan_dao");
    public static final ResourceKey<DamageType> DAO_DAO = key("dao_dao");
    public static final ResourceKey<DamageType> DU_DAO = key("du_dao");
    public static final ResourceKey<DamageType> FEI_XING_DAO = key("fei_xing_dao");
    public static final ResourceKey<DamageType> FENG_DAO = key("feng_dao");
    public static final ResourceKey<DamageType> GU_DAO = key("gu_dao");
    public static final ResourceKey<DamageType> GUANG_DAO = key("guang_dao");
    public static final ResourceKey<DamageType> HUA_DAO = key("hua_dao");
    public static final ResourceKey<DamageType> HUAN_DAO = key("huan_dao");
    public static final ResourceKey<DamageType> HUN_DAO = key("hun_dao");
    public static final ResourceKey<DamageType> JIAN_DAO = key("jian_dao");
    public static final ResourceKey<DamageType> JIN_DAO = key("jin_dao");
    public static final ResourceKey<DamageType> JIN_DAO_FORBIDDEN = key("jin_dao_forbidden");
    public static final ResourceKey<DamageType> LEI_DAO = key("lei_dao");
    public static final ResourceKey<DamageType> LI_DAO = key("li_dao");
    public static final ResourceKey<DamageType> LIAN_DAO = key("lian_dao");
    public static final ResourceKey<DamageType> LU_DAO = key("lu_dao");
    public static final ResourceKey<DamageType> MENG_DAO = key("meng_dao");
    public static final ResourceKey<DamageType> MU_DAO = key("mu_dao");
    public static final ResourceKey<DamageType> NU_DAO = key("nu_dao");
    public static final ResourceKey<DamageType> QI_DAO = key("qi_dao");
    public static final ResourceKey<DamageType> QING_MEI_DAO = key("qing_mei_dao");
    public static final ResourceKey<DamageType> REN_DAO = key("ren_dao");
    public static final ResourceKey<DamageType> SHI_DAO = key("shi_dao");
    public static final ResourceKey<DamageType> SHUI_DAO = key("shui_dao");
    public static final ResourceKey<DamageType> TIAN_DAO = key("tian_dao");
    public static final ResourceKey<DamageType> TOU_DAO = key("tou_dao");
    public static final ResourceKey<DamageType> TU_DAO = key("tu_dao");
    public static final ResourceKey<DamageType> XIE_DAO = key("xie_dao");
    public static final ResourceKey<DamageType> XIN_DAO = key("xin_dao");
    public static final ResourceKey<DamageType> XING_DAO = key("xing_dao");
    public static final ResourceKey<DamageType> XU_DAO = key("xu_dao");
    public static final ResourceKey<DamageType> YAN_DAO = key("yan_dao");
    public static final ResourceKey<DamageType> YIN_DAO = key("yin_dao");
    public static final ResourceKey<DamageType> YING_DAO = key("ying_dao");
    public static final ResourceKey<DamageType> YUE_DAO = key("yue_dao");
    public static final ResourceKey<DamageType> YUN_DAO = key("yun_dao");
    public static final ResourceKey<DamageType> YUN_DAO_CLOUD = key("yun_dao_cloud");
    public static final ResourceKey<DamageType> ZHEN_DAO = key("zhen_dao");
    public static final ResourceKey<DamageType> ZHI_DAO = key("zhi_dao");
    public static final ResourceKey<DamageType> ZHOU_DAO = key("zhou_dao");
    public static final ResourceKey<DamageType> YU_DAO = key("yu_dao");

    private GuzhenrenExtDamageTypes() {}

    private static ResourceKey<DamageType> key(final String path) {
        return ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(GuzhenrenExt.MODID, path)
        );
    }
}
