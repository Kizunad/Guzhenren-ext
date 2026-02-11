package com.Kizunad.guzhenrenext.xianqiao.daomark;

import net.minecraft.nbt.CompoundTag;

/**
 * 单个 Section（16×16×16 方块区域）内的道痕浓度数据。
 * <p>
 * 每种 {@link DaoType} 的浓度以 short 存储，值域 [0, {@value #MAX_AURA}]。
 * </p>
 */
public class DaoSectionData {

    /** 灵气浓度上限。 */
    public static final int MAX_AURA = 30000;

    /** NBT 序列化 key。 */
    private static final String TAG_MARKS = "marks";

    /** 各道类型浓度数组，索引对应 {@link DaoType#getIndex()}。 */
    private final short[] marks;

    /** 脏标记，用于判断是否需要持久化。 */
    private boolean dirty;

    /**
     * 构造一个全零浓度的 Section 数据。
     */
    public DaoSectionData() {
        this.marks = new short[DaoType.count()];
    }

    /**
     * 获取指定道类型的灵气浓度。
     *
     * @param type 道类型
     * @return 灵气浓度（0 ~ {@value #MAX_AURA}）
     */
    public int getAura(DaoType type) {
        return marks[type.getIndex()];
    }

    /**
     * 直接设置指定道类型的灵气浓度，超出范围会 clamp。
     *
     * @param type   道类型
     * @param amount 目标浓度
     */
    public void setAura(DaoType type, int amount) {
        marks[type.getIndex()] = (short) Math.max(0, Math.min(MAX_AURA, amount));
        dirty = true;
    }

    /**
     * 增加指定道类型的灵气浓度，结果 clamp 到 [0, {@value #MAX_AURA}]。
     *
     * @param type   道类型
     * @param amount 增加量（应 >= 0）
     */
    public void addAura(DaoType type, int amount) {
        int idx = type.getIndex();
        marks[idx] = (short) Math.min(MAX_AURA, marks[idx] + amount);
        dirty = true;
    }

    /**
     * 消耗指定道类型的灵气。
     * <p>
     * 当前浓度 >= amount 时扣除并返回 true，否则不扣除并返回 false。
     * </p>
     *
     * @param type   道类型
     * @param amount 消耗量
     * @return 是否消耗成功
     */
    public boolean consumeAura(DaoType type, int amount) {
        int idx = type.getIndex();
        if (marks[idx] >= amount) {
            marks[idx] = (short) (marks[idx] - amount);
            dirty = true;
            return true;
        }
        return false;
    }

    /**
     * 获取脏标记。
     *
     * @return 是否有数据变更
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * 清除脏标记。
     */
    public void clearDirty() {
        dirty = false;
    }

    /**
     * 将数据序列化到 NBT。
     *
     * @param tag 目标 CompoundTag
     */
    public void save(CompoundTag tag) {
        int[] intArray = new int[marks.length];
        for (int i = 0; i < marks.length; i++) {
            intArray[i] = marks[i];
        }
        tag.putIntArray(TAG_MARKS, intArray);
    }

    /**
     * 从 NBT 反序列化创建 Section 数据。
     *
     * @param tag 包含道痕数据的 CompoundTag
     * @return 反序列化后的 DaoSectionData
     */
    public static DaoSectionData load(CompoundTag tag) {
        DaoSectionData data = new DaoSectionData();
        if (tag.contains(TAG_MARKS)) {
            int[] intArray = tag.getIntArray(TAG_MARKS);
            int len = Math.min(intArray.length, data.marks.length);
            for (int i = 0; i < len; i++) {
                data.marks[i] = (short) Math.max(0, Math.min(MAX_AURA, intArray[i]));
            }
        }
        return data;
    }
}
