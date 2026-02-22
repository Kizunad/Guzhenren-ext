package com.Kizunad.guzhenrenext.xianqiao.alchemy.item;

/**
 * 丹药品质。
 * <p>
 * 该枚举承载三个直接用于玩法与界面展示的元数据：
 * 1. 效果倍率：用于后续效果强度计算。
 * 2. 中文显示名：用于 tooltip 与文案展示。
 * 3. 显示颜色：用于 tooltip/UI 文本着色（int RGB）。
 * </p>
 */
public enum PillQuality {
    /** 黄阶：基础品质，效果倍率 1.0。 */
    HUANG(1.0D, "黄", 0xE3B341),

    /** 地阶：进阶品质，效果倍率 1.5。 */
    DI(1.5D, "地", 0xA67C52),

    /** 玄阶：高阶品质，效果倍率 2.0。 */
    XUAN(2.0D, "玄", 0x6C5CE7),

    /** 天阶：顶阶品质，效果倍率 3.0。 */
    TIAN(3.0D, "天", 0x5DADE2);

    /** 品质效果倍率。 */
    private final double effectMultiplier;

    /** 品质中文显示名。 */
    private final String displayName;

    /** 品质显示颜色（int RGB，可直接用于 tooltip/UI）。 */
    private final int displayColor;

    /**
     * 品质默认值。
     * <p>
     * 当堆栈中缺失品质字段或反序列化失败时，统一回落到黄品，保证读链路稳定。
     * </p>
     */
    private static final PillQuality DEFAULT_QUALITY = HUANG;

    PillQuality(double effectMultiplier, String displayName, int displayColor) {
        this.effectMultiplier = effectMultiplier;
        this.displayName = displayName;
        this.displayColor = displayColor;
    }

    /**
     * 获取该品质的效果倍率。
     *
     * @return 效果倍率
     */
    public double getEffectMultiplier() {
        return effectMultiplier;
    }

    /**
     * 获取该品质的中文显示名。
     *
     * @return 中文显示名
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 获取该品质的显示颜色（int RGB）。
     *
     * @return 显示颜色
     */
    public int getDisplayColor() {
        return displayColor;
    }

    /**
     * 获取用于 NBT 序列化的字符串。
     *
     * @return 序列化字符串
     */
    public String getSerializedName() {
        return this.name();
    }

    /**
     * 从序列化字符串反序列化品质。
     * <p>
     * 该方法用于统一处理丹药品质读取，避免字符串魔法散落在业务代码中。
     * </p>
     *
     * @param serializedName 序列化字符串
     * @return 解析得到的品质；非法输入返回默认品质
     */
    public static PillQuality fromSerializedName(String serializedName) {
        if (serializedName == null || serializedName.isBlank()) {
            return DEFAULT_QUALITY;
        }
        for (PillQuality quality : values()) {
            if (quality.name().equals(serializedName)) {
                return quality;
            }
        }
        return DEFAULT_QUALITY;
    }
}
