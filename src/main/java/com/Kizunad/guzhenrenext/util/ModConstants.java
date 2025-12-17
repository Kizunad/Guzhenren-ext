package com.Kizunad.guzhenrenext.util;

public final class ModConstants {

    private ModConstants() {}

    // MobEffect colors
    public static final int COLOR_TEAR_EFFECT = 0x990000; // 深红色
    public static final int COLOR_MAD_DOG_EFFECT = 0xFF0000; // 红色

    // TearMobEffect
    public static final double TEAR_MOVE_THRESHOLD_SQ = 0.0004; // 移动距离平方阈值，避免微小抖动触发
    public static final float TEAR_DAMAGE_AMPLIFIER_FACTOR = 0.5F; // 撕裂效果等级对伤害的加成因子

    // EffectEventHandler
    public static final float TEAR_HEALING_REDUCTION_FACTOR = 0.5f; // 撕裂效果治疗减免因子
    public static final int MAD_DOG_BASE_DURATION_TICKS = 100; // 疯狗状态基础持续时间 (5秒)
    public static final int MAD_DOG_NUDOA_DURATION_PER_MARK = 2; // 疯狗状态奴道道痕对持续时间的加成 (每1道痕加2tick)
}
