package com.Kizunad.guzhenrenext.kongqiao.flyingsword;

/**
 * 飞剑系统常量（Phase 2）。
 */
public final class FlyingSwordConstants {

    public static final float ENTITY_WIDTH = 0.6f;
    public static final float ENTITY_HEIGHT = 0.25f;
    public static final int CLIENT_TRACK_RANGE = 96;

    public static final double ENTITY_MAX_HEALTH = 20.0D;
    public static final double ENTITY_FOLLOW_RANGE = 64.0D;

    public static final double SEARCH_RANGE = 128.0;
    public static final double OWNER_SCAN_RANGE = 256.0;

    public static final double OWNER_HEIGHT_ORBIT = 0.8;
    public static final double OWNER_HEIGHT_HOVER_MULTIPLIER = 1.0;

    public static final double ORBIT_DEGREES_PER_REV = 360.0;
    public static final double DEG_TO_RAD = Math.PI / 180.0;
    public static final double RAD_TO_DEG = 180.0 / Math.PI;
    public static final float LOOK_ROTATE_DEG_OFFSET = 90.0f;

    public static final double ORBIT_RADIUS = 1.6;
    public static final double ORBIT_VERTICAL_OFFSET = 0.15;
    public static final double ORBIT_APPROACH_FACTOR = 0.35;

    public static final double HOVER_HEIGHT_OFFSET = 0.3;
    public static final double HOVER_APPROACH_FACTOR = 0.25;

    public static final double RECALL_FINISH_DISTANCE = 0.35;
    public static final double RECALL_SPEED = 0.35;

    // ===== AI（Phase 2：GUARD/HUNT 最小版） =====

    /**
     * 目标扫描间隔（tick）。
     * <p>
     * 说明：飞剑 AI 采用“轻量扫描”而非完整 Goal/Target 系统。
     * </p>
     */
    public static final int TARGET_SCAN_INTERVAL_TICKS = 5;

    /** GUARD：以主人为中心的防御半径（格）。 */
    public static final double GUARD_RANGE = 16.0;

    /** HUNT：主动搜索半径（格）。 */
    public static final double HUNT_RANGE = 24.0;

    /** HUNT：脱离主人过远则放弃（格）。 */
    public static final double HUNT_LEASH_RANGE = 32.0;

    /** 攻击判定距离（格）。 */
    public static final double ATTACK_RANGE = 1.4;

    /** 攻击冷却（tick）。 */
    public static final int ATTACK_COOLDOWN_TICKS = 10;

    /** 基础攻击伤害（Phase 2 暂定）。 */
    public static final float ATTACK_DAMAGE = 4.0F;

    /** GUARD：追击速度倍率（相对 speedMax）。 */
    public static final double GUARD_CHASE_SPEED_SCALE = 0.85;

    /** HUNT：追击速度倍率（相对 speedMax）。 */
    public static final double HUNT_CHASE_SPEED_SCALE = 1.0;

    public static final double RENDER_BOB_FREQUENCY = 0.15;
    public static final double RENDER_BOB_AMPLITUDE = 0.05;
    public static final double RENDER_BASE_HEIGHT = 0.15;
    public static final float RENDER_ROTATE_X_DEG = 90.0f;

    public static final double LOOK_EPSILON = 1.0e-6;

    public static final double MIN_DISTANCE = 1.0e-6;

    public static final double ORBIT_SPEED_WHEN_DESCENDING_SCALE = 0.75;
    public static final double HOVER_SPEED_SCALE = 0.8;

    private FlyingSwordConstants() {}
}
