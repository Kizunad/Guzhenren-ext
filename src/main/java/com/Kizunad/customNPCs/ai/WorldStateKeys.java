package com.Kizunad.customNPCs.ai;

/**
 * 世界状态键常量 - 集中管理所有状态键名称
 * <p>
 * 用于 GOAP 规划器的前置条件和效果定义，避免状态名漂移（命名不一致）。
 * <p>
 * 命名规范：
 * - 使用小写下划线命名：target_in_range, has_food
 * - 布尔值前缀：is_, has_, can_
 * - 位置相关：at_block_<x>_<y>_<z>
 */
public final class WorldStateKeys {

    private WorldStateKeys() {
        // 工具类，禁止实例化
    }

    // ==================== 目标相关 ====================
    /**
     * 目标是否可见（视线检测）
     */
    public static final String TARGET_VISIBLE = "target_visible";

    /**
     * 目标是否在攻击范围内
     */
    public static final String TARGET_IN_RANGE = "target_in_range";

    /**
     * 目标是否受到伤害
     */
    public static final String TARGET_DAMAGED = "target_damaged";

    /**
     * 目标距离（double，单位：格）
     */
    public static final String DISTANCE_TO_TARGET = "distance_to_target";

    /**
     * 是否有可用远程武器
     */
    public static final String HAS_RANGED_WEAPON = "has_ranged_weapon";

    /**
     * 是否可以格挡（持盾或可格挡物品）
     */
    public static final String CAN_BLOCK = "can_block";

    /**
     * 攻击冷却是否激活
     */
    public static final String ATTACK_COOLDOWN_ACTIVE = "attack_cooldown_active";

    // ==================== 物品相关 ====================
    /**
     * 是否拥有指定物品（格式：has_item_<item_type>）
     */
    public static final String HAS_ITEM_PREFIX = "has_item_";

    /**
     * 物品是否可用
     */
    public static final String ITEM_USABLE = "item_usable";

    /**
     * 物品是否已使用
     */
    public static final String ITEM_USED = "item_used";

    /**
     * 饥饿值是否已恢复
     */
    public static final String HUNGER_RESTORED = "hunger_restored";

    /**
     * 饥饿百分比（0~1）
     */
    public static final String HUNGER_PERCENT = "hunger_percent";

    /**
     * 是否饥饿（低于高饱食阈值）
     */
    public static final String IS_HUNGRY = "is_hungry";

    /**
     * 饥饿是否达到危急阈值
     */
    public static final String HUNGER_CRITICAL = "hunger_critical";

    // ==================== 方块相关 ====================
    /**
     * 是否在指定方块附近（格式：at_block_<x>_<y>_<z>）
     */
    public static final String AT_BLOCK_PREFIX = "at_block_";

    /**
     * 方块是否存在
     */
    public static final String BLOCK_EXISTS = "block_exists";

    /**
     * 方块是否已交互
     */
    public static final String BLOCK_INTERACTED = "block_interacted";

    /**
     * 门是否打开
     */
    public static final String DOOR_OPEN = "door_open";

    // ==================== 装备相关 ====================
    /**
     * 背包中是否存在更优的盔甲
     */
    public static final String ARMOR_BETTER_AVAILABLE = "armor_better_available";

    /**
     * 当前盔甲是否已最佳化
     */
    public static final String ARMOR_OPTIMIZED = "armor_optimized";

    /**
     * 当前盔甲综合评分
     */
    public static final String ARMOR_SCORE = "armor_score";

    // ==================== 状态相关 ====================
    /**
     * 生命值百分比（格式：health_percent）
     */
    public static final String HEALTH_PERCENT = "health_percent";

    /**
     * 是否处于危险中
     */
    public static final String IN_DANGER = "in_danger";

    /**
     * 是否附近存在环境危险
     */
    public static final String HAZARD_NEARBY = "hazard_nearby";

    /**
     * 是否有敌对单位
     */
    public static final String HOSTILE_NEARBY = "hostile_nearby";

    /**
     * 是否检测到威胁
     */
    public static final String THREAT_DETECTED = "threat_detected";

    /**
     * 当前威胁的 UUID
     */
    public static final String CURRENT_THREAT_ID = "current_threat_id";

    /**
     * 是否有友方单位
     */
    public static final String ALLY_NEARBY = "ally_nearby";

    /**
     * 是否有食物
     */
    public static final String HAS_FOOD = "has_food";

    // ==================== 关系 / 指令 ====================
    /**
     * 主人 UUID
     */
    public static final String OWNER_UUID = "owner_uuid";

    /**
     * 当前指令类型（字符串，枚举名称）
     */
    public static final String CURRENT_COMMAND = "current_command";

    /**
     * 当前关系类型（字符串，如 HIRED / OPPRESSED）
     */
    public static final String RELATIONSHIP_TYPE = "relationship_type";

    /**
     * 是否已接近主人（跟随指令使用）
     */
    public static final String NEAR_OWNER = "near_owner";

    // ==================== 关系流程状态 ====================
    /**
     * 雇佣所需价值（double）
     */
    public static final String HIRE_REQUIRED_VALUE = "hire_required_value";

    /**
     * 雇佣候选玩家 UUID
     */
    public static final String HIRE_CANDIDATE = "hire_candidate";

    /**
     * 是否正在进行雇佣流程
     */
    public static final String HIRE_PENDING = "hire_pending";

    // ==================== 工具方法 ====================
    /**
     * 生成"拥有物品"的状态键
     * @param itemType 物品类型
     * @return 状态键（has_item_<item_type>）
     */
    public static String hasItem(String itemType) {
        return HAS_ITEM_PREFIX + itemType.toLowerCase();
    }

    /**
     * 生成"在方块附近"的状态键
     * @param x 方块 X 坐标
     * @param y 方块 Y 坐标
     * @param z 方块 Z 坐标
     * @return 状态键（at_block_<x>_<y>_<z>）
     */
    public static String atBlock(int x, int y, int z) {
        return AT_BLOCK_PREFIX + x + "_" + y + "_" + z;
    }
}
