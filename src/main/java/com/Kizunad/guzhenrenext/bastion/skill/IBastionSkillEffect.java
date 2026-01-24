package com.Kizunad.guzhenrenext.bastion.skill;

import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoEffect;

/**
 * 基地高转技能/特效接口。
 * <p>
 * 设计目标：复用 ShazhaoEffectRegistry 的 ID -> 实现 映射能力，但提供 Bastion 执行所需的
 * 额外上下文（基地坐标、目标玩家集合、守卫集合、bonusMultiplier 等）。
 * </p>
 * <p>
 * 注意：该接口不影响玩家侧杀招系统的正常运行；玩家系统只会调用 {@link IShazhaoEffect#onSecond}
 * 与 {@link IShazhaoEffect#onInactive}。
 * </p>
 */
public interface IBastionSkillEffect extends IShazhaoEffect {

    /**
     * 被动效果：每秒调用一次。
     * <p>
     * 由 BastionAuraService 在“玩家处于基地领域内”时驱动。
     * </p>
     */
    default void onBastionSecond(final BastionSkillContext context) {
    }

    /**
     * 主动效果：按冷却触发。
     * <p>
     * 由 BastionTicker 在 FULL tick 中驱动。
     * </p>
     */
    default boolean onBastionActivate(final BastionSkillContext context) {
        return false;
    }
}
