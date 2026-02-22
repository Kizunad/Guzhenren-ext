package com.Kizunad.guzhenrenext.xianqiao.alchemy.effect;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.xianqiao.alchemy.item.PillQuality;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public final class PillEffectState {

    public static final String KEY_CUI_SHENG_ACCEL_END_GAME_TIME =
        "GuzhenrenExtCuiShengAccelEndGameTime";
    public static final String KEY_CUI_SHENG_ACCEL_MULTIPLIER =
        "GuzhenrenExtCuiShengAccelMultiplier";

    /** 润泽丹效果结束时间（游戏刻）。 */
    public static final String KEY_RUN_ZE_ACCEL_END_GAME_TIME =
        "GuzhenrenExtRunZeAccelEndGameTime";

    /** 润泽丹加速倍率。 */
    public static final String KEY_RUN_ZE_ACCEL_MULTIPLIER =
        "GuzhenrenExtRunZeAccelMultiplier";

    /** 护体丹效果结束时间（游戏刻）。 */
    public static final String KEY_HU_TI_DEFENSE_END_GAME_TIME =
        "GuzhenrenExtHuTiDefenseEndGameTime";

    /** 护体丹效果防御加成值。 */
    public static final String KEY_HU_TI_DEFENSE_AMOUNT =
        "GuzhenrenExtHuTiDefenseAmount";

    /** 护体丹防御加成使用的固定属性修饰符 ID。 */
    public static final ResourceLocation HU_TI_DEFENSE_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(
        GuzhenrenExt.MODID,
        "pill_hu_ti_dan_defense_bonus"
    );

    private static final double DEFAULT_MULTIPLIER = 1.0D;
    private static final double MIN_MULTIPLIER = 1.0D;

    /** 护体丹最小防御加成。 */
    private static final double MIN_DEFENSE_AMOUNT = 0.0D;

    /** 黄品护体丹防御加成。 */
    private static final double HUANG_DEFENSE_AMOUNT = 2.0D;

    /** 地品护体丹防御加成。 */
    private static final double DI_DEFENSE_AMOUNT = 3.0D;

    /** 玄品护体丹防御加成。 */
    private static final double XUAN_DEFENSE_AMOUNT = 4.0D;

    /** 天品护体丹防御加成。 */
    private static final double TIAN_DEFENSE_AMOUNT = 6.0D;

    /** 黄品润泽丹仙元加速倍率。 */
    private static final double RUN_ZE_MULTIPLIER_HUANG = 1.5D;

    /** 地品润泽丹仙元加速倍率。 */
    private static final double RUN_ZE_MULTIPLIER_DI = 2.0D;

    /** 玄品润泽丹仙元加速倍率。 */
    private static final double RUN_ZE_MULTIPLIER_XUAN = 2.5D;

    /** 天品润泽丹仙元加速倍率。 */
    private static final double RUN_ZE_MULTIPLIER_TIAN = 3.0D;

    private PillEffectState() {
    }

    public static void writeGrowthAccelerationState(
        Player player,
        long currentGameTime,
        long durationTicks,
        double multiplier
    ) {
        if (player == null) {
            return;
        }
        long safeDuration = Math.max(0L, durationTicks);
        long endGameTime = currentGameTime + safeDuration;
        double safeMultiplier = Math.max(MIN_MULTIPLIER, multiplier);
        player.getPersistentData().putLong(KEY_CUI_SHENG_ACCEL_END_GAME_TIME, endGameTime);
        player.getPersistentData().putDouble(KEY_CUI_SHENG_ACCEL_MULTIPLIER, safeMultiplier);
    }

    public static long readGrowthAccelerationEndGameTime(Player player) {
        if (player == null) {
            return 0L;
        }
        return player.getPersistentData().getLong(KEY_CUI_SHENG_ACCEL_END_GAME_TIME);
    }

    public static double readGrowthAccelerationMultiplier(Player player) {
        if (player == null) {
            return DEFAULT_MULTIPLIER;
        }
        double raw = player.getPersistentData().getDouble(KEY_CUI_SHENG_ACCEL_MULTIPLIER);
        return raw > 0.0D ? raw : DEFAULT_MULTIPLIER;
    }

    public static boolean isGrowthAccelerationActive(Player player, long currentGameTime) {
        if (player == null) {
            return false;
        }
        long endGameTime = readGrowthAccelerationEndGameTime(player);
        return endGameTime > 0L && currentGameTime <= endGameTime;
    }

    public static void clearGrowthAccelerationState(Player player) {
        if (player == null) {
            return;
        }
        player.getPersistentData().remove(KEY_CUI_SHENG_ACCEL_END_GAME_TIME);
        player.getPersistentData().remove(KEY_CUI_SHENG_ACCEL_MULTIPLIER);
    }

    /**
     * 写入润泽丹仙元加速状态。
     *
     * @param player 玩家
     * @param currentGameTime 当前游戏刻
     * @param durationTicks 持续时间（刻）
     * @param multiplier 加速倍率
     */
    public static void writeRunZeAccelerationState(
        Player player,
        long currentGameTime,
        long durationTicks,
        double multiplier
    ) {
        if (player == null) {
            return;
        }
        long safeDuration = Math.max(0L, durationTicks);
        long endGameTime = currentGameTime + safeDuration;
        double safeMultiplier = Math.max(MIN_MULTIPLIER, multiplier);
        player.getPersistentData().putLong(KEY_RUN_ZE_ACCEL_END_GAME_TIME, endGameTime);
        player.getPersistentData().putDouble(KEY_RUN_ZE_ACCEL_MULTIPLIER, safeMultiplier);
    }

    /**
     * 读取润泽丹仙元加速结束时间。
     *
     * @param player 玩家
     * @return 结束游戏刻
     */
    public static long readRunZeAccelerationEndGameTime(Player player) {
        if (player == null) {
            return 0L;
        }
        return player.getPersistentData().getLong(KEY_RUN_ZE_ACCEL_END_GAME_TIME);
    }

    /**
     * 读取润泽丹仙元加速倍率。
     *
     * @param player 玩家
     * @return 仙元加速倍率
     */
    public static double readRunZeAccelerationMultiplier(Player player) {
        if (player == null) {
            return DEFAULT_MULTIPLIER;
        }
        double rawMultiplier = player.getPersistentData().getDouble(KEY_RUN_ZE_ACCEL_MULTIPLIER);
        return rawMultiplier > 0.0D ? rawMultiplier : DEFAULT_MULTIPLIER;
    }

    /**
     * 判断润泽丹仙元加速是否激活。
     *
     * @param player 玩家
     * @param currentGameTime 当前游戏刻
     * @return true=激活，false=未激活
     */
    public static boolean isRunZeAccelerationActive(Player player, long currentGameTime) {
        if (player == null) {
            return false;
        }
        long endGameTime = readRunZeAccelerationEndGameTime(player);
        return endGameTime > 0L && currentGameTime <= endGameTime;
    }

    /**
     * 清理润泽丹仙元加速状态。
     *
     * @param player 玩家
     */
    public static void clearRunZeAccelerationState(Player player) {
        if (player == null) {
            return;
        }
        player.getPersistentData().remove(KEY_RUN_ZE_ACCEL_END_GAME_TIME);
        player.getPersistentData().remove(KEY_RUN_ZE_ACCEL_MULTIPLIER);
    }

    /**
     * 根据丹药品质解析润泽丹仙元加速倍率。
     * <p>
     * 映射规则固定：HUANG=1.5、DI=2.0、XUAN=2.5、TIAN=3.0。
     * </p>
     *
     * @param quality 丹药品质
     * @return 仙元加速倍率
     */
    public static double resolveRunZeAccelerationMultiplier(PillQuality quality) {
        PillQuality safeQuality = quality == null ? PillQuality.HUANG : quality;
        return switch (safeQuality) {
            case HUANG -> RUN_ZE_MULTIPLIER_HUANG;
            case DI -> RUN_ZE_MULTIPLIER_DI;
            case XUAN -> RUN_ZE_MULTIPLIER_XUAN;
            case TIAN -> RUN_ZE_MULTIPLIER_TIAN;
        };
    }

    /**
     * 写入护体丹防御增强状态。
     *
     * @param player 玩家
     * @param currentGameTime 当前游戏刻
     * @param durationTicks 持续时间（刻）
     * @param quality 丹药品质
     */
    public static void writeBodyDefenseState(
        Player player,
        long currentGameTime,
        long durationTicks,
        PillQuality quality
    ) {
        if (player == null) {
            return;
        }
        long safeDuration = Math.max(0L, durationTicks);
        long endGameTime = currentGameTime + safeDuration;
        double defenseAmount = resolveBodyDefenseAmount(quality);
        player.getPersistentData().putLong(KEY_HU_TI_DEFENSE_END_GAME_TIME, endGameTime);
        player.getPersistentData().putDouble(KEY_HU_TI_DEFENSE_AMOUNT, defenseAmount);
    }

    /**
     * 读取护体丹结束时间。
     *
     * @param player 玩家
     * @return 结束游戏刻
     */
    public static long readBodyDefenseEndGameTime(Player player) {
        if (player == null) {
            return 0L;
        }
        return player.getPersistentData().getLong(KEY_HU_TI_DEFENSE_END_GAME_TIME);
    }

    /**
     * 读取护体丹防御加成值。
     *
     * @param player 玩家
     * @return 防御加成值
     */
    public static double readBodyDefenseAmount(Player player) {
        if (player == null) {
            return MIN_DEFENSE_AMOUNT;
        }
        double rawAmount = player.getPersistentData().getDouble(KEY_HU_TI_DEFENSE_AMOUNT);
        return Math.max(MIN_DEFENSE_AMOUNT, rawAmount);
    }

    /**
     * 判断护体丹防御增强是否处于激活状态。
     *
     * @param player 玩家
     * @param currentGameTime 当前游戏刻
     * @return true=激活，false=未激活
     */
    public static boolean isBodyDefenseActive(Player player, long currentGameTime) {
        if (player == null) {
            return false;
        }
        long endGameTime = readBodyDefenseEndGameTime(player);
        return endGameTime > 0L && currentGameTime <= endGameTime;
    }

    /**
     * 清理护体丹防御增强状态。
     *
     * @param player 玩家
     */
    public static void clearBodyDefenseState(Player player) {
        if (player == null) {
            return;
        }
        player.getPersistentData().remove(KEY_HU_TI_DEFENSE_END_GAME_TIME);
        player.getPersistentData().remove(KEY_HU_TI_DEFENSE_AMOUNT);
    }

    /**
     * 根据丹药品质解析护体丹防御加成。
     * <p>
     * 映射规则：HUANG=+2、DI=+3、XUAN=+4、TIAN=+6。
     * </p>
     *
     * @param quality 丹药品质
     * @return 防御加成值
     */
    public static double resolveBodyDefenseAmount(PillQuality quality) {
        PillQuality safeQuality = quality == null ? PillQuality.HUANG : quality;
        return switch (safeQuality) {
            case HUANG -> HUANG_DEFENSE_AMOUNT;
            case DI -> DI_DEFENSE_AMOUNT;
            case XUAN -> XUAN_DEFENSE_AMOUNT;
            case TIAN -> TIAN_DEFENSE_AMOUNT;
        };
    }

    /**
     * 刷新护体丹防御属性修饰符。
     * <p>
     * 实现严格遵循固定 modifierId + 先移除旧值再添加 transient modifier 的约定。
     * </p>
     *
     * @param player 玩家
     * @param amount 目标防御加成
     */
    public static void applyBodyDefenseModifier(Player player, double amount) {
        if (player == null) {
            return;
        }
        AttributeInstance armorAttribute = player.getAttribute(Attributes.ARMOR);
        if (armorAttribute == null) {
            return;
        }
        double clampedAmount = Math.max(MIN_DEFENSE_AMOUNT, amount);
        AttributeModifier existingModifier = armorAttribute.getModifier(HU_TI_DEFENSE_MODIFIER_ID);
        if (existingModifier != null && Double.compare(existingModifier.amount(), clampedAmount) == 0) {
            return;
        }
        if (existingModifier != null) {
            armorAttribute.removeModifier(HU_TI_DEFENSE_MODIFIER_ID);
        }
        if (clampedAmount > MIN_DEFENSE_AMOUNT) {
            armorAttribute.addTransientModifier(
                new AttributeModifier(
                    HU_TI_DEFENSE_MODIFIER_ID,
                    clampedAmount,
                    AttributeModifier.Operation.ADD_VALUE
                )
            );
        }
    }

    /**
     * 移除护体丹防御属性修饰符。
     *
     * @param player 玩家
     */
    public static void removeBodyDefenseModifier(Player player) {
        if (player == null) {
            return;
        }
        AttributeInstance armorAttribute = player.getAttribute(Attributes.ARMOR);
        if (armorAttribute == null) {
            return;
        }
        if (armorAttribute.getModifier(HU_TI_DEFENSE_MODIFIER_ID) != null) {
            armorAttribute.removeModifier(HU_TI_DEFENSE_MODIFIER_ID);
        }
    }
}
