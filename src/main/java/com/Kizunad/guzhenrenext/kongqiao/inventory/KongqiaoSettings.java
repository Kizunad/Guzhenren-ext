package com.Kizunad.guzhenrenext.kongqiao.inventory;

import com.Kizunad.guzhenrenext.kongqiao.KongqiaoConstants;

/**
 * 空窍容量/拓展配置，独立抽象便于后续接入任务或 UI。
 */
public class KongqiaoSettings {

    private final int columns;
    private int unlockedRows;

    public KongqiaoSettings() {
        this(KongqiaoConstants.DEFAULT_VISIBLE_ROWS, KongqiaoConstants.COLUMNS);
    }

    public KongqiaoSettings(int unlockedRows, int columns) {
        this.columns = columns;
        this.unlockedRows = Math.min(
                Math.max(unlockedRows, 1),
                KongqiaoConstants.MAX_ROWS
            );
    }

    /**
     * 当前已解锁的槽位数量。
     */
    public int getUnlockedSlots() {
        return unlockedRows * columns;
    }

    /**
     * 判定指定槽位是否已解锁。
     */
    public boolean isSlotUnlocked(int slot) {
        return slot >= 0 && slot < getUnlockedSlots();
    }

    public int getUnlockedRows() {
        return unlockedRows;
    }

    public int getColumns() {
        return columns;
    }

    /**
     * 递增解锁行数，返回是否发生变化。
     */
    public boolean unlockNextRow() {
        if (unlockedRows >= KongqiaoConstants.MAX_ROWS) {
            return false;
        }
        unlockedRows += 1;
        return true;
    }

    /**
     * 强制设置行数，供外部在同步/加载后写回。
     */
    public void setUnlockedRows(int rows) {
        this.unlockedRows = Math.min(
                Math.max(rows, 1),
                KongqiaoConstants.MAX_ROWS
            );
    }
}
