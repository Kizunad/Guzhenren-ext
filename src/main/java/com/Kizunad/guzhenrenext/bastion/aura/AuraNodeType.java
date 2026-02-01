package com.Kizunad.guzhenrenext.bastion.aura;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

/**
 * 光环节点类型枚举。
 * <p>
 * Round 5.2：用于标识光环节点的增益/减益极性。
 * </p>
 */
public enum AuraNodeType implements StringRepresentable {

    /**
     * 增益型（增幅守卫）。
     */
    BUFF("buff"),

    /**
     * 减益型（压制玩家）。
     */
    DEBUFF("debuff"),

    /**
     * 隐匿型（为守卫提供短时隐身）。
     */
    STEALTH("stealth"),

    /**
     * 侦测型（为范围内玩家施加发光效果，便于守卫发现）。
     */
    DETECTION("detection");

    /**
     * 便于配置/网络/调试复用的通用 Codec。
     */
    public static final Codec<AuraNodeType> CODEC = StringRepresentable.fromEnum(AuraNodeType::values);

    private final String serializedName;

    AuraNodeType(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }
}
