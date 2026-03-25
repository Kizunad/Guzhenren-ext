package com.Kizunad.guzhenrenext.xianqiao.runtime;

import java.util.Objects;

/**
 * 仙窍边界运行时分区模型。
 * <p>
 * 该模型以“越界距离（方块）”作为唯一判定输入，输出当前坐标所属分区：
 * 1) playable zone：outsideDistance=0（仍在 chunk 真值边界内）；
 * 2) safezone：outsideDistance∈[1,8]；
 * 3) warning band：outsideDistance∈[9,16]；
 * 4) lethal chaos band：outsideDistance>=17。
 * </p>
 */
public record ChaosZoneModel(int outsideDistanceBlocks, long outsideDistanceSquared, ZoneType zoneType) {

    private static final int ZERO = 0;

    private static final int SAFEZONE_MAX_OUTSIDE_DISTANCE_BLOCKS = 8;

    private static final int WARNING_MAX_OUTSIDE_DISTANCE_BLOCKS = 16;

    private static final int LETHAL_ZONE_START_OUTSIDE_DISTANCE_BLOCKS = 17;

    private static final long LONG_ONE = 1L;

    public ChaosZoneModel {
        if (outsideDistanceBlocks < ZERO) {
            throw new IllegalArgumentException("outsideDistanceBlocks 不能小于 0");
        }
        if (outsideDistanceSquared < 0L) {
            throw new IllegalArgumentException("outsideDistanceSquared 不能小于 0");
        }
        Objects.requireNonNull(zoneType, "zoneType");
    }

    /**
     * 由越界距离平方构建运行时分区模型。
     * <p>
     * 采用 ceil(sqrt(d²)) 还原“最小整数量级的越界方块数”，
     * 以确保 0/8/16/17 阈值语义可被稳定测试。
     * </p>
     *
     * @param outsideDistanceSquared 越界距离平方
     * @return 运行时分区模型
     */
    public static ChaosZoneModel fromOutsideDistanceSquared(long outsideDistanceSquared) {
        if (outsideDistanceSquared < 0L) {
            throw new IllegalArgumentException("outsideDistanceSquared 不能小于 0");
        }
        int outsideDistanceBlocks = toOutsideDistanceBlocks(outsideDistanceSquared);
        return new ChaosZoneModel(
            outsideDistanceBlocks,
            outsideDistanceSquared,
            resolveZoneType(outsideDistanceBlocks)
        );
    }

    /**
     * 判定当前是否位于 playable zone（即仍在边界内）。
     */
    public boolean isPlayableZone() {
        return zoneType == ZoneType.PLAYABLE_ZONE;
    }

    /**
     * 判定当前是否位于 safezone（1~8 格越界）。
     */
    public boolean isSafeZone() {
        return zoneType == ZoneType.SAFEZONE;
    }

    /**
     * 判定当前是否位于 warning band（9~16 格越界）。
     */
    public boolean isWarningBand() {
        return zoneType == ZoneType.WARNING_BAND;
    }

    /**
     * 判定当前是否位于 lethal chaos band（>=17 格越界）。
     */
    public boolean isLethalChaosBand() {
        return zoneType == ZoneType.LETHAL_CHAOS_BAND;
    }

    /**
     * 判定是否仍处于“16 格预留上限”内。
     * <p>
     * 语义：只对已越界位置生效，因此 outsideDistance=0 不计入预留带。
     * </p>
     */
    public boolean isWithinReservedChaosBand() {
        return outsideDistanceBlocks > ZERO
            && outsideDistanceBlocks <= WARNING_MAX_OUTSIDE_DISTANCE_BLOCKS;
    }

    public static int safeZoneMaxOutsideDistanceBlocks() {
        return SAFEZONE_MAX_OUTSIDE_DISTANCE_BLOCKS;
    }

    public static int warningZoneMaxOutsideDistanceBlocks() {
        return WARNING_MAX_OUTSIDE_DISTANCE_BLOCKS;
    }

    public static int lethalZoneStartOutsideDistanceBlocks() {
        return LETHAL_ZONE_START_OUTSIDE_DISTANCE_BLOCKS;
    }

    /**
     * 依据越界方块数解析分区类型。
     */
    public static ZoneType resolveZoneType(int outsideDistanceBlocks) {
        if (outsideDistanceBlocks <= ZERO) {
            return ZoneType.PLAYABLE_ZONE;
        }
        if (outsideDistanceBlocks <= SAFEZONE_MAX_OUTSIDE_DISTANCE_BLOCKS) {
            return ZoneType.SAFEZONE;
        }
        if (outsideDistanceBlocks <= WARNING_MAX_OUTSIDE_DISTANCE_BLOCKS) {
            return ZoneType.WARNING_BAND;
        }
        return ZoneType.LETHAL_CHAOS_BAND;
    }

    private static int toOutsideDistanceBlocks(long outsideDistanceSquared) {
        if (outsideDistanceSquared <= 0L) {
            return ZERO;
        }
        long floor = (long) Math.sqrt(outsideDistanceSquared);
        long squared = floor * floor;
        long ceil = squared == outsideDistanceSquared ? floor : floor + LONG_ONE;
        if (ceil > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) ceil;
    }

    /**
     * 运行时分区类型。
     */
    public enum ZoneType {
        /** 在仙窍 chunk 真值边界内（outsideDistance=0）。 */
        PLAYABLE_ZONE,

        /** 越界安全带（outsideDistance 1~8）。 */
        SAFEZONE,

        /** 越界告警带（outsideDistance 9~16）。 */
        WARNING_BAND,

        /** 致命混沌带（outsideDistance>=17）。 */
        LETHAL_CHAOS_BAND
    }
}
