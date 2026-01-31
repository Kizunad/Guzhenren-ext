package com.Kizunad.guzhenrenext.bastion;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * 基地词缀/变异标记。
 * <p>
 * 第一阶段仅提供数据结构与序列化占位，用于后续生态演化玩法扩展。
 * </p>
 */
public enum BastionModifier implements StringRepresentable {

    /** 硬化：节点/核心更难被破坏（后续实现）。 */
    HARDENED("hardened"),

    /** 易爆：拆节点可能触发爆炸（后续实现）。 */
    VOLATILE("volatile"),

    /** 隐匿：边界不易被感知（后续实现）。 */
    CLOAKED("cloaked"),

    /** 增殖：扩张速度提升但更脆弱（后续实现）。 */
    PROLIFERATING("proliferating");

    public static final Codec<BastionModifier> CODEC =
        StringRepresentable.fromEnum(BastionModifier::values);

    private final String name;

    BastionModifier(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
