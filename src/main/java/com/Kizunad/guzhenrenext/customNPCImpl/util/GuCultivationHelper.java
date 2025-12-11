package com.Kizunad.guzhenrenext.customNPCImpl.util;

import net.guzhenren.network.GuzhenrenModVariables;
import net.minecraft.world.entity.LivingEntity;

/**
 * 蛊真人修行数据工具类。
 * <p>
 * 负责安全访问、初始化和推进 {@link GuzhenrenModVariables.PlayerVariables}
 * 中与修行相关的字段，避免业务逻辑直接操作裸数据导致空指针或未初始化。
 * </p>
 */
public final class GuCultivationHelper {

    private static final int MAX_RANK = 5;
    private static final double BASE_REQUIREMENT = 200.0D;
    private static final double STAGE_STEP_REQUIREMENT = 50.0D;
    private static final double RANK_STEP_REQUIREMENT = 80.0D;
    private static final double EPSILON = 1.0E-4D;

    private GuCultivationHelper() {}

    /**
     * 获取蛊真人变量。
     *
     * @param entity 实体
     * @return 玩家变量，可能为 null
     */
    public static GuzhenrenModVariables.PlayerVariables getVariables(
        LivingEntity entity
    ) {
        if (entity == null) {
            return null;
        }
        try {
            return entity.getData(GuzhenrenModVariables.PLAYER_VARIABLES);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 当前转数是否已达上限。
     */
    public static boolean isAtMaxRank(
        GuzhenrenModVariables.PlayerVariables vars
    ) {
        if (vars == null) {
            return true;
        }
        return vars.zhuanshu >= MAX_RANK - EPSILON;
    }

    /**
     * 确保修行需求值已初始化并返回。
     * <p>
     * 若原值缺失或为 0，则按当前阶段与转数推导一个近似需求，
     * 并写回变量以便后续复用。
     * </p>
     */
    public static double ensureRequirement(
        GuzhenrenModVariables.PlayerVariables vars
    ) {
        if (vars == null) {
            return 0.0D;
        }
        double requirement = vars.gushi_xiulian_jindu;
        if (requirement <= EPSILON) {
            int stage = clampStage(vars.jieduan);
            int rank = Math.max(
                1,
                (int) Math.floor(Math.max(1.0D, vars.zhuanshu))
            );
            requirement =
                BASE_REQUIREMENT +
                (stage - 1) * STAGE_STEP_REQUIREMENT +
                (rank - 1) * RANK_STEP_REQUIREMENT;
            vars.gushi_xiulian_jindu = requirement;
            vars.markSyncDirty();
        }
        return requirement;
    }

    /**
     * 判断是否仍需修行。
     */
    public static boolean needsCultivation(
        GuzhenrenModVariables.PlayerVariables vars
    ) {
        if (vars == null) {
            return false;
        }
        double requirement = ensureRequirement(vars);
        return requirement > EPSILON &&
            vars.gushi_xiulian_dangqian + EPSILON < requirement;
    }

    /**
     * 将阶段值限制在合法范围。
     */
    public static int clampStage(double raw) {
        int stage = (int) Math.floor(Math.max(1.0D, raw));
        return Math.min(stage, MAX_RANK);
    }

    /**
     * 增加修行进度。
     */
    public static void addProgress(
        GuzhenrenModVariables.PlayerVariables vars,
        double amount
    ) {
        if (vars == null || amount <= 0.0D) {
            return;
        }
        double current = Math.max(0.0D, vars.gushi_xiulian_dangqian);
        double next = current + amount;
        if (Double.compare(current, next) != 0) {
            vars.gushi_xiulian_dangqian = next;
            vars.markSyncDirty();
        }
    }

    /**
     * 尝试突破：当进度满足需求时提升阶段/转数并刷新需求。
     */
    public static void tryBreakthrough(
        GuzhenrenModVariables.PlayerVariables vars
    ) {
        if (vars == null || isAtMaxRank(vars)) {
            return;
        }
        double requirement = ensureRequirement(vars);
        if (requirement <= EPSILON) {
            return;
        }
        if (vars.gushi_xiulian_dangqian + EPSILON < requirement) {
            return;
        }

        vars.gushi_xiulian_dangqian =
            Math.max(0.0D, vars.gushi_xiulian_dangqian - requirement);

        int currentStage = clampStage(vars.jieduan);
        int nextStage = clampStage(currentStage + 1.0D);
        vars.jieduan = nextStage;

        double nextRank = Math.max(nextStage, Math.floor(vars.zhuanshu + 1.0D));
        vars.zhuanshu = Math.min(nextRank, MAX_RANK);

        double nextRequirement = requirement + STAGE_STEP_REQUIREMENT;
        vars.gushi_xiulian_jindu = nextRequirement;
        vars.markSyncDirty();
    }
}
