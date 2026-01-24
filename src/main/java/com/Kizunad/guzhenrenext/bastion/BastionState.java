package com.Kizunad.guzhenrenext.bastion;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * 基地状态枚举。
 * <p>
 * 优先级（从高到低）：DESTROYED > SEALED > ACTIVE
 * </p>
 * <ul>
 *   <li>DESTROYED 是终态，不可回退到其他状态</li>
 *   <li>SEALED 由 sealedUntilGameTime 比较派生</li>
 *   <li>ACTIVE 是正常运行状态</li>
 * </ul>
 */
public enum BastionState implements StringRepresentable {

    /**
     * 正常运行状态：扩张、刷怪、进化均活跃。
     */
    ACTIVE("active"),

    /**
     * 封印状态：扩张和刷怪禁用，进化减速/暂停。
     * 派生条件：gameTime < sealedUntilGameTime 且 state != DESTROYED
     */
    SEALED("sealed"),

    /**
     * 终态：核心被破坏，节点衰减中，最终从 SavedData 移除。
     * 一旦进入，不可回退到 ACTIVE 或 SEALED。
     */
    DESTROYED("destroyed");

    public static final Codec<BastionState> CODEC = StringRepresentable.fromEnum(BastionState::values);

    private final String name;

    BastionState(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    /**
     * 根据存储状态和游戏时间确定有效状态。
     * 使用此方法而非直接读取存储的 state 字段。
     *
     * @param storedState          持久化的状态
     * @param sealedUntilGameTime  封印截止的游戏时间
     * @param currentGameTime      当前游戏时间
     * @return 有效状态
     */
    public static BastionState getEffectiveState(
            BastionState storedState,
            long sealedUntilGameTime,
            long currentGameTime) {
        // DESTROYED 是终态，优先级最高
        if (storedState == DESTROYED) {
            return DESTROYED;
        }
        // 检查是否封印中（基于时间）
        if (currentGameTime < sealedUntilGameTime) {
            return SEALED;
        }
        // 默认为 ACTIVE
        return ACTIVE;
    }
}
