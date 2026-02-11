package com.Kizunad.guzhenrenext.xianqiao.daomark;

/**
 * 道类型枚举。
 * <p>
 * 每种道对应一个 ordinal 索引，用于 {@link DaoSectionData} 中 short[] 数组的下标。
 * </p>
 */
public enum DaoType {

    /** 火道。 */
    FIRE("火道"),
    /** 水道。 */
    WATER("水道"),
    /** 土道。 */
    EARTH("土道"),
    /** 木道。 */
    WOOD("木道"),
    /** 金道。 */
    METAL("金道"),
    /** 雷道。 */
    LIGHTNING("雷道"),
    /** 风道。 */
    WIND("风道"),
    /** 冰道。 */
    ICE("冰道"),
    /** 死道。 */
    DEATH("死道"),
    /** 生道。 */
    LIFE("生道"),
    /** 时道。 */
    TIME("时道"),
    /** 空道。 */
    SPACE("空道"),
    /** 毒道。 */
    POISON("毒道"),
    /** 魂道。 */
    SOUL("魂道"),
    /** 剑道。 */
    SWORD("剑道"),
    /** 血道。 */
    BLOOD("血道"),
    /** 力道。 */
    STRENGTH("力道"),
    /** 规则道。 */
    RULE("规则道"),
    /** 智道。 */
    WISDOM("智道"),
    /** 暗道。 */
    DARK("暗道"),
    /** 光道。 */
    LIGHT("光道"),
    /** 云道。 */
    CLOUD("云道"),
    /** 星道。 */
    STAR("星道"),
    /** 月道。 */
    MOON("月道"),
    /** 变化道。 */
    TRANSFORMATION("变化道"),
    /** 梦道。 */
    DREAM("梦道"),
    /** 情道。 */
    EMOTION("情道"),
    /** 运道。 */
    LUCK("运道"),
    /** 命道。 */
    FATE("命道");

    /** 预缓存所有枚举值，避免重复调用 values()。 */
    private static final DaoType[] VALUES = values();

    /** 道类型的中文显示名称。 */
    private final String displayName;

    DaoType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 获取此道类型的数组索引（等同于 ordinal）。
     *
     * @return 数组索引
     */
    public int getIndex() {
        return ordinal();
    }

    /**
     * 获取此道类型的中文显示名称。
     *
     * @return 中文名
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 根据索引获取道类型。
     *
     * @param index 数组索引
     * @return 对应的道类型
     * @throws IllegalArgumentException 如果索引越界
     */
    public static DaoType byIndex(int index) {
        if (index < 0 || index >= VALUES.length) {
            throw new IllegalArgumentException("无效的道类型索引: " + index);
        }
        return VALUES[index];
    }

    /**
     * 获取道类型总数。
     *
     * @return 道类型数量
     */
    public static int count() {
        return VALUES.length;
    }
}
