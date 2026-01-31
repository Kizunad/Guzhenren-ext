package com.Kizunad.guzhenrenext.damage;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public final class GuzhenrenDamageTypeTags {

    public static final TagKey<DamageType> DAMAGE_FROM_GUZHENRENEXT = tag("damage_from_guzhenrenext");

    public static final TagKey<DamageType> AN_DAO = tag("an_dao");
    public static final TagKey<DamageType> BIAN_HUA_DAO = tag("bian_hua_dao");
    public static final TagKey<DamageType> BING_DAO = tag("bing_dao");
    public static final TagKey<DamageType> BING_XUE_DAO = tag("bing_xue_dao");
    public static final TagKey<DamageType> DAN_DAO = tag("dan_dao");
    public static final TagKey<DamageType> DAO_DAO = tag("dao_dao");
    public static final TagKey<DamageType> DU_DAO = tag("du_dao");
    public static final TagKey<DamageType> FEI_XING_DAO = tag("fei_xing_dao");
    public static final TagKey<DamageType> FENG_DAO = tag("feng_dao");
    public static final TagKey<DamageType> GU_DAO = tag("gu_dao");
    public static final TagKey<DamageType> GUANG_DAO = tag("guang_dao");
    public static final TagKey<DamageType> HUA_DAO = tag("hua_dao");
    public static final TagKey<DamageType> HUAN_DAO = tag("huan_dao");
    public static final TagKey<DamageType> HUN_DAO = tag("hun_dao");
    public static final TagKey<DamageType> JIAN_DAO = tag("jian_dao");
    public static final TagKey<DamageType> JIN_DAO = tag("jin_dao");
    public static final TagKey<DamageType> JIN_DAO_FORBIDDEN = tag("jin_dao_forbidden");
    public static final TagKey<DamageType> LEI_DAO = tag("lei_dao");
    public static final TagKey<DamageType> LI_DAO = tag("li_dao");
    public static final TagKey<DamageType> LIAN_DAO = tag("lian_dao");
    public static final TagKey<DamageType> LU_DAO = tag("lu_dao");
    public static final TagKey<DamageType> MENG_DAO = tag("meng_dao");
    public static final TagKey<DamageType> MU_DAO = tag("mu_dao");
    public static final TagKey<DamageType> NU_DAO = tag("nu_dao");
    public static final TagKey<DamageType> QI_DAO = tag("qi_dao");
    public static final TagKey<DamageType> QING_MEI_DAO = tag("qing_mei_dao");
    public static final TagKey<DamageType> REN_DAO = tag("ren_dao");
    public static final TagKey<DamageType> SHI_DAO = tag("shi_dao");
    public static final TagKey<DamageType> SHUI_DAO = tag("shui_dao");
    public static final TagKey<DamageType> TIAN_DAO = tag("tian_dao");
    public static final TagKey<DamageType> TOU_DAO = tag("tou_dao");
    public static final TagKey<DamageType> TU_DAO = tag("tu_dao");
    public static final TagKey<DamageType> XIE_DAO = tag("xie_dao");
    public static final TagKey<DamageType> XIN_DAO = tag("xin_dao");
    public static final TagKey<DamageType> XING_DAO = tag("xing_dao");
    public static final TagKey<DamageType> XU_DAO = tag("xu_dao");
    public static final TagKey<DamageType> YAN_DAO = tag("yan_dao");
    public static final TagKey<DamageType> YIN_DAO = tag("yin_dao");
    public static final TagKey<DamageType> YING_DAO = tag("ying_dao");
    public static final TagKey<DamageType> YUE_DAO = tag("yue_dao");
    public static final TagKey<DamageType> YUN_DAO = tag("yun_dao");
    public static final TagKey<DamageType> YUN_DAO_CLOUD = tag("yun_dao_cloud");
    public static final TagKey<DamageType> ZHEN_DAO = tag("zhen_dao");
    public static final TagKey<DamageType> ZHI_DAO = tag("zhi_dao");
    public static final TagKey<DamageType> ZHOU_DAO = tag("zhou_dao");
    public static final TagKey<DamageType> YU_DAO = tag("yu_dao");

    public static final TagKey<DamageType> DAMAGE_KIND_DIRECT = tag("damage_kind_direct");
    public static final TagKey<DamageType> DAMAGE_KIND_DOT = tag("damage_kind_dot");
    public static final TagKey<DamageType> DAMAGE_KIND_PIERCE = tag("damage_kind_pierce");

    private GuzhenrenDamageTypeTags() {}

    private static TagKey<DamageType> tag(final String id) {
        return TagKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath("guzhenren", id)
        );
    }
}
