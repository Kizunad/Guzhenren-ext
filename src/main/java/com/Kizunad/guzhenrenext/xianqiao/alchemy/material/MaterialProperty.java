package com.Kizunad.guzhenrenext.xianqiao.alchemy.material;

/**
 * 炼丹主材属性。
 * <p>
 * 该枚举用于描述炼丹主材的基础药性，供炼丹服务进行产物映射。
 * 当前保持最小集合，直接对应基础四类丹药。
 * </p>
 */
public enum MaterialProperty {

    /** 木性：偏生发，映射催生相关丹药。 */
    WOOD,

    /** 软性：偏护持，映射护体相关丹药。 */
    SOFT,

    /** 生机性：偏恢复，映射回春相关丹药。 */
    VITAL,

    /** 润泽性：偏滋养，映射润泽相关丹药。 */
    MOIST
}
