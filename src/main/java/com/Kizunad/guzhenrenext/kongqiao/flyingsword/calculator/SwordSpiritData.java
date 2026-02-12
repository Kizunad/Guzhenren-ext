package com.Kizunad.guzhenrenext.kongqiao.flyingsword.calculator;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;

/**
 * 剑灵数据。
 * <p>
 * 存储飞剑的灵性相关数据：
 * <ul>
 *     <li>好感度（affinity）：剑灵对主人的好感程度</li>
 *     <li>心情（mood）：剑灵当前心情状态</li>
 * </ul>
 * </p>
 */
public class SwordSpiritData {

    // ==================== NBT 键名 ====================

    private static final String TAG_AFFINITY = "Affinity";
    private static final String TAG_MOOD = "Mood";

    // ==================== 好感度常量 ====================

    /** 好感度上限。 */
    public static final int MAX_AFFINITY = 1000;

    /** 好感度下限。 */
    public static final int MIN_AFFINITY = -100;

    // ==================== 心情常量 ====================

    /** 中立心情。 */
    public static final int MOOD_NEUTRAL = 0;

    /** 开心心情。 */
    public static final int MOOD_HAPPY = 1;

    /** 生气心情。 */
    public static final int MOOD_ANGRY = -1;

    // ==================== 数据字段 ====================

    /** 好感度（范围 MIN_AFFINITY ~ MAX_AFFINITY）。 */
    private int affinity;

    /** 心情（0=中立, 1=开心, -1=生气）。 */
    private int mood;

    /**
     * 创建默认剑灵数据。
     * <p>
     * 默认值：好感度为 0，心情为中立。
     * </p>
     */
    public SwordSpiritData() {
        this.affinity = MOOD_NEUTRAL;
        this.mood = MOOD_NEUTRAL;
    }

    /**
     * 获取当前好感度。
     *
     * @return 当前好感度
     */
    public int getAffinity() {
        return affinity;
    }

    /**
     * 设置好感度。
     * <p>
     * 输入值会自动限制在 [{@link #MIN_AFFINITY}, {@link #MAX_AFFINITY}] 区间内。
     * </p>
     *
     * @param affinity 目标好感度
     */
    public void setAffinity(int affinity) {
        this.affinity = clampAffinity(affinity);
    }

    /**
     * 增减好感度。
     * <p>
     * 在当前好感度基础上叠加增量，并自动限制到合法范围。
     * </p>
     *
     * @param delta 好感度变化量（可正可负）
     */
    public void addAffinity(int delta) {
        setAffinity(this.affinity + delta);
    }

    /**
     * 获取当前心情。
     *
     * @return 当前心情常量值
     */
    public int getMood() {
        return mood;
    }

    /**
     * 设置心情。
     * <p>
     * 仅支持中立、开心、生气三种状态。若传入非法值，则回退为中立。
     * </p>
     *
     * @param mood 心情值
     */
    public void setMood(int mood) {
        if (mood == MOOD_NEUTRAL || mood == MOOD_HAPPY || mood == MOOD_ANGRY) {
            this.mood = mood;
            return;
        }
        this.mood = MOOD_NEUTRAL;
    }

    /**
     * 序列化为 NBT。
     *
     * @return NBT 标签
     */
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(TAG_AFFINITY, affinity);
        tag.putInt(TAG_MOOD, mood);
        return tag;
    }

    /**
     * 从 NBT 反序列化。
     *
     * @param tag NBT 标签
     * @return 剑灵数据实例
     */
    public static SwordSpiritData fromNBT(@Nullable CompoundTag tag) {
        SwordSpiritData data = new SwordSpiritData();
        if (tag == null) {
            return data;
        }

        if (tag.contains(TAG_AFFINITY)) {
            data.setAffinity(tag.getInt(TAG_AFFINITY));
        }
        if (tag.contains(TAG_MOOD)) {
            data.setMood(tag.getInt(TAG_MOOD));
        }

        return data;
    }

    /**
     * 创建深拷贝。
     *
     * @return 新实例
     */
    public SwordSpiritData copy() {
        return fromNBT(toNBT());
    }

    /**
     * 将好感度限制在合法区间。
     *
     * @param value 待限制值
     * @return 限制后的值
     */
    private static int clampAffinity(int value) {
        return Math.max(MIN_AFFINITY, Math.min(MAX_AFFINITY, value));
    }
}
