package com.Kizunad.guzhenrenext.guzhenrenBridge;

import net.guzhenren.network.GuzhenrenModVariables;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * 蛊真人魂魄交互桥接类。
 * <p>
 * 负责魂魄的读取、修改与同步。
 * </p>
 */
public final class HunPoHelper {

    private HunPoHelper() {}

    private static final float DAMAGE = 1f;

    /**
     * 获取当前魂魄值。
     */
    public static double getAmount(LivingEntity entity) {
        if (entity == null) {
            return 0.0;
        }
        try {
            return getVariables(entity).hunpo;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 获取最大魂魄值。
     */
    public static double getMaxAmount(LivingEntity entity) {
        if (entity == null) {
            return 0.0;
        }
        try {
            return getVariables(entity).zuida_hunpo;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 修改魂魄值。
     *
     * @param entity 目标实体
     * @param amount 变化量（正数为增加，负数为减少）
     * @return 修改后的魂魄值
     */
    public static double modify(LivingEntity entity, double amount) {
        if (entity == null) {
            return 0.0;
        }
        try {
            var vars = getVariables(entity);
            double original = vars.hunpo;
            double max = vars.zuida_hunpo;

            double newValue = Math.max(0, Math.min(max, original + amount));

            if (Double.compare(original, newValue) != 0) {
                vars.hunpo = newValue;
                PlayerVariablesSyncHelper.markSyncDirty(vars);
            }
            return newValue;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 获取当前魂魄抗性。
     */
    public static double getResistance(LivingEntity entity) {
        if (entity == null) {
            return 0.0;
        }
        try {
            return getVariables(entity).hunpo_kangxing;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 获取最大魂魄抗性。
     */
    public static double getMaxResistance(LivingEntity entity) {
        if (entity == null) {
            return 0.0;
        }
        try {
            return getVariables(entity).hunpo_kangxing_shangxian;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 设置最大魂魄抗性上限。
     * <p>
     * 注意：原模组里该字段有时可能为 0（未启用/未初始化），因此使用方应自行决定如何解释 0 的含义。
     * 本方法仅负责安全写入并触发同步。
     * </p>
     *
     * @return 写入后的上限值
     */
    public static double setMaxResistance(LivingEntity entity, double value) {
        if (entity == null) {
            return 0.0;
        }
        try {
            var vars = getVariables(entity);
            double next = Math.max(0.0, value);
            if (Double.compare(vars.hunpo_kangxing_shangxian, next) != 0) {
                vars.hunpo_kangxing_shangxian = next;
                PlayerVariablesSyncHelper.markSyncDirty(vars);
            }
            return next;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 修改最大魂魄抗性上限。
     *
     * @return 修改后的上限值
     */
    public static double modifyMaxResistance(LivingEntity entity, double amount) {
        if (entity == null) {
            return 0.0;
        }
        try {
            var vars = getVariables(entity);
            return setMaxResistance(entity, vars.hunpo_kangxing_shangxian + amount);
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 修改魂魄抗性值。
     * @return 修改后的抗性值
     */
    public static double modifyResistance(LivingEntity entity, double amount) {
        if (entity == null) {
            return 0.0;
        }
        try {
            var vars = getVariables(entity);
            double original = vars.hunpo_kangxing;
            // 抗性上限似乎默认为0(未开启?)，或者根据逻辑动态变化。这里暂不强制限制上限，只保证下限>=0，除非有明确上限逻辑。
            // 查阅原代码习惯，通常会检查 hunpo_kangxing_shangxian
            double max = vars.hunpo_kangxing_shangxian > 0
                ? vars.hunpo_kangxing_shangxian
                : Double.MAX_VALUE;

            double newValue = Math.max(0, Math.min(max, original + amount));

            if (Double.compare(original, newValue) != 0) {
                vars.hunpo_kangxing = newValue;
                PlayerVariablesSyncHelper.markSyncDirty(vars);
            }
            return newValue;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 检查实体魂魄是否耗尽，若耗尽则执行处死逻辑（模仿原模组行为）。
     */
    public static void checkAndKill(LivingEntity entity) {
        if (entity == null) {
            return;
        }
        if (getAmount(entity) <= 0) {
            // 造成 1 亿点魂魄消散伤害
            entity.hurt(
                new DamageSource(
                    entity
                        .level()
                        .registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(
                            ResourceKey.create(
                                Registries.DAMAGE_TYPE,
                                ResourceLocation.parse(
                                    "guzhenren:hunpoxiaosuan"
                                )
                            )
                        )
                ),
                DAMAGE
            );
        }
    }

    private static GuzhenrenModVariables.PlayerVariables getVariables(
        LivingEntity entity
    ) {
        return entity.getData(GuzhenrenModVariables.PLAYER_VARIABLES);
    }
}
