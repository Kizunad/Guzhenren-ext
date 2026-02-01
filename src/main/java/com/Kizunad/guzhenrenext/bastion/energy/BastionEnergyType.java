package com.Kizunad.guzhenrenext.bastion.energy;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

/**
 * 挂载在 Anchor 上的能源类别枚举（回合 3 用）。
 * <p>
 * 该枚举用于 BastionTypeConfig.energy 的稳定序列化：
 * <ul>
 *   <li>photosynthesis：光合作用，偏向“获取光照/日照并转化为能量”</li>
 *   <li>water_intake：摄取水分，偏向“吸收液体/湿度并转化为能量”</li>
 *   <li>geothermal：地热，偏向“从地表/地下热量获取能量”</li>
 * </ul>
 * <p>
 * 注意：序列化名（snake_case）必须与配置 JSON 中的 energy 字段取值完全一致，
 * 以避免未来配置/网络/调试时产生兼容性问题。
 */
public enum BastionEnergyType implements StringRepresentable {

    /**
     * 光合作用：通过光照/日照获取能量。
     */
    PHOTOSYNTHESIS("photosynthesis"),
    /**
     * 摄取水分：通过吸收水分/液体获取能量。
     */
    WATER_INTAKE("water_intake"),
    /**
     * 地热：通过地表/地下热量获取能量。
     */
    GEOTHERMAL("geothermal"),

    /**
     * 风能：通过高空风力获取能量。
     */
    WIND("wind"),

    /**
     * 夜能：通过夜间/低光照环境获取能量。
     */
    NIGHT("night");

    /**
     * 便于后续配置/网络/调试复用的通用 Codec。
     */
    public static final Codec<BastionEnergyType> CODEC = StringRepresentable.fromEnum(BastionEnergyType::values);

    private final String serializedName;

    BastionEnergyType(String serializedName) {
        this.serializedName = serializedName;
    }

    /**
     * 获取稳定的序列化名（snake_case）。
     */
    @Override
    public String getSerializedName() {
        return this.serializedName;
    }
}
