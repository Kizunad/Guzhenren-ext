package com.Kizunad.guzhenrenext.worldgen;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Bastion 遗迹定位器（仅调试用途）。
 * <p>
 * 由于 Feature 生成是概率事件，玩家在超平坦/大地图中不易肉眼寻找。
 * 此类在服务端记录最近 N 次 bastion_ruin 的生成位置，供 debug 指令查询/传送。
 * </p>
 */
public final class BastionRuinLocator {

    private BastionRuinLocator() {
    }

    /**
     * 最近记录上限。
     */
    private static final int MAX_RECORDS = 64;

    /**
     * 环形队列：最新记录在队尾。
     */
    private static final Deque<RuinRecord> RECORDS = new ArrayDeque<>();

    /** 待激活队列（记录核心坐标）。 */
    private static final Deque<RuinRecord> PENDING_ACTIVATION = new ArrayDeque<>();

    /** 总尝试次数（place 被调用次数）。 */
    private static long attemptCount;

    /** 总成功次数（核心成功放置次数）。 */
    private static long successCount;

    /** 最近一次尝试。 */
    private static RuinRecord lastAttempt;

    /** 最近一次成功。 */
    private static RuinRecord lastSuccess;

    public static void recordAttempt(ResourceKey<Level> dimension, BlockPos pos) {
        if (dimension == null || pos == null) {
            return;
        }
        attemptCount++;
        lastAttempt = new RuinRecord(dimension, pos);
    }

    public static void record(ResourceKey<Level> dimension, BlockPos pos) {
        if (dimension == null || pos == null) {
            return;
        }
        successCount++;
        lastSuccess = new RuinRecord(dimension, pos);
        if (RECORDS.size() >= MAX_RECORDS) {
            RECORDS.removeFirst();
        }
        RECORDS.addLast(new RuinRecord(dimension, pos));

        // 同时加入待激活队列
        enqueueActivation(dimension, pos);
    }

    public static void enqueueActivation(ResourceKey<Level> dimension, BlockPos pos) {
        if (dimension == null || pos == null) {
            return;
        }
        if (PENDING_ACTIVATION.size() >= MAX_RECORDS) {
            PENDING_ACTIVATION.removeFirst();
        }
        PENDING_ACTIVATION.addLast(new RuinRecord(dimension, pos));
    }

    public static Optional<RuinRecord> pollActivation() {
        return Optional.ofNullable(PENDING_ACTIVATION.pollFirst());
    }

    public static int pendingCount() {
        return PENDING_ACTIVATION.size();
    }

    public static void clear() {
        RECORDS.clear();
        PENDING_ACTIVATION.clear();
        attemptCount = 0;
        successCount = 0;
        lastAttempt = null;
        lastSuccess = null;
    }

    public static long attemptCount() {
        return attemptCount;
    }

    public static long successCount() {
        return successCount;
    }

    public static Optional<RuinRecord> lastAttempt() {
        return Optional.ofNullable(lastAttempt);
    }

    public static int count() {
        return RECORDS.size();
    }

    public static Optional<RuinRecord> last() {
        return Optional.ofNullable(RECORDS.peekLast());
    }

    public static Optional<RuinRecord> lastSuccess() {
        return Optional.ofNullable(lastSuccess);
    }

    public static java.util.List<RuinRecord> snapshot() {
        return java.util.List.copyOf(RECORDS);
    }

    public record RuinRecord(ResourceKey<Level> dimension, BlockPos pos) {
    }
}
