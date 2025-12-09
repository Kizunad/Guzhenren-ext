package com.Kizunad.guzhenrenext.customNPCImpl.util;

import javax.annotation.Nullable;
import net.guzhenren.network.GuzhenrenModVariables;
import net.minecraft.world.entity.LivingEntity;

/**
 * 蛊师修炼辅助工具。
 * <p>
 * 封装空窍修炼相关的数据读写、突破判定与需求计算逻辑，
 * 便于 NPC 与玩家复用同一套路（进度 -> 阶段 -> 转数）。
 * </p>
 */
public final class GuCultivationHelper {

    private static final double[] BASE_STAGE_REQUIREMENTS = {
        400.0D,
        600.0D,
        800.0D,
        1200.0D,
    };
    private static final int MAX_STAGE_PER_TURN =
        BASE_STAGE_REQUIREMENTS.length;
    private static final int MAX_TURN = 5;
    private static final double EPSILON = 1.0E-4D;

    private GuCultivationHelper() {}

    /**
     * 读取蛊师变量，确保异常不会中断调用者。
     */
    @Nullable
    public static GuzhenrenModVariables.PlayerVariables getVariables(
        LivingEntity entity
    ) {
        if (entity == null) {
            return null;
        }
        try {
            return entity.getData(GuzhenrenModVariables.PLAYER_VARIABLES);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 计算并同步当前阶段所需修炼进度。
     *
     * @return 最新需求值，若环境异常则返回 0
     */
    public static double ensureRequirement(
        GuzhenrenModVariables.PlayerVariables vars
    ) {
        if (vars == null) {
            return 0.0D;
        }
        int stageIndex = clampStage(vars.jieduan) - 1;
        double zhuanshu = Math.max(1.0D, vars.zhuanshu);
        double base = BASE_STAGE_REQUIREMENTS[
            Math.min(
                Math.max(stageIndex, 0),
                BASE_STAGE_REQUIREMENTS.length - 1
            )
        ];
        double required = base * Math.pow(zhuanshu, zhuanshu);
        if (Double.compare(vars.gushi_xiulian_jindu, required) != 0) {
            vars.gushi_xiulian_jindu = required;
            vars.markSyncDirty();
        }
        return required;
    }

    /**
     * 增加修炼进度，自动封顶并触发同步。
     *
     * @return 最新进度值
     */
    public static double addProgress(
        GuzhenrenModVariables.PlayerVariables vars,
        double delta
    ) {
        if (vars == null || delta <= 0.0D) {
            return vars == null ? 0.0D : vars.gushi_xiulian_dangqian;
        }
        double required = ensureRequirement(vars);
        double current = vars.gushi_xiulian_dangqian;
        double next = Math.min(required, current + delta);
        if (Double.compare(current, next) != 0) {
            vars.gushi_xiulian_dangqian = next;
            vars.markSyncDirty();
        }
        return next;
    }

    /**
     * 尝试执行阶段突破：满进度 -> 阶段+1，阶段4再满 -> 转数+1 并重置阶段。
     *
     * @return 若发生突破则返回 true
     */
    public static boolean tryBreakthrough(
        GuzhenrenModVariables.PlayerVariables vars
    ) {
        if (vars == null) {
            return false;
        }
        double required = ensureRequirement(vars);
        if (required <= 0.0D) {
            return false;
        }
        if (vars.gushi_xiulian_dangqian + EPSILON < required) {
            return false;
        }

        int stage = clampStage(vars.jieduan);
        if (stage < MAX_STAGE_PER_TURN) {
            vars.jieduan = stage + 1;
            vars.gushi_xiulian_dangqian = 0.0D;
            vars.markSyncDirty();
            ensureRequirement(vars);
            return true;
        }

        if (vars.zhuanshu >= MAX_TURN) {
            vars.gushi_xiulian_dangqian = required;
            vars.markSyncDirty();
            return false;
        }

        vars.zhuanshu = Math.floor(vars.zhuanshu) + 1.0D;
        vars.jieduan = 1.0D;
        vars.gushi_xiulian_dangqian = 0.0D;
        vars.markSyncDirty();
        ensureRequirement(vars);
        return true;
    }

    /**
     * 判断当前是否仍需继续修炼。
     */
    public static boolean needsCultivation(
        GuzhenrenModVariables.PlayerVariables vars
    ) {
        if (vars == null) {
            return false;
        }
        if (isAtMaxRank(vars)) {
            return false;
        }
        double required = ensureRequirement(vars);
        return required > 0 && vars.gushi_xiulian_dangqian + EPSILON < required;
    }

    /**
     * 判断是否已经达到最高转数。
     */
    public static boolean isAtMaxRank(
        GuzhenrenModVariables.PlayerVariables vars
    ) {
        return vars != null && vars.zhuanshu >= MAX_TURN;
    }

    /**
     * 将阶段值转换为 1~4 的整数，忽略小数阶段。
     */
    public static int clampStage(double rawStage) {
        if (rawStage < 1.0D) {
            return 1;
        }
        int stage = (int) Math.floor(rawStage);
        return Math.min(Math.max(stage, 1), MAX_STAGE_PER_TURN);
    }
}
