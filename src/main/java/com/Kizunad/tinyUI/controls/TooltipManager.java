package com.Kizunad.tinyUI.controls;

import java.util.Objects;
import java.util.Optional;

/**
 * 工具提示管理器 - 管理 Tooltip 显示的延时与位置，逻辑独立于渲染。
 * <p>
 * 功能：
 * <ul>
 *   <li>延迟显示工具提示（鼠标悬停一定时间后）</li>
 *   <li>跟踪鼠标位置并设置提示位置</li>
 *   <li>自动重置延时器当文本改变</li>
 *   <li>鼠标移开时自动隐藏</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>{@code
 * TooltipManager manager = new TooltipManager(0.5); // 0.5秒延迟
 * manager.update("Tooltip text", isHovering, mouseX, mouseY, deltaTime);
 * manager.getActiveState().ifPresent(state -> {
 *     // 渲染 tooltip
 * });
 * }</pre>
 *
 * @see Tooltip
 */
public final class TooltipManager {

    /** 提示位置相对鼠标的 X 偏移量（像素） */
    private static final int OFFSET_X = 10;
    /** 提示位置相对鼠标的 Y 偏移量（像素） */
    private static final int OFFSET_Y = 12;

    /** 延迟显示时间（秒） */
    private final double delaySeconds;
    /** 当前计时器（秒） */
    private double timer;
    /** 是否已激活（超过延迟时间） */
    private boolean active;
    /** 待显示的文本 */
    private String pendingText = "";
    /** 激活的提示状态 */
    private TooltipState activeState;

    /**
     * 创建工具提示管理器。
     *
     * @param delaySeconds 延迟显示时间（秒，负数会被钳制为 0）
     */
    public TooltipManager(final double delaySeconds) {
        this.delaySeconds = Math.max(0.0d, delaySeconds);
    }

    /**
     * 更新工具提示状态。
     * 根据鼠标悬停状态和文本内容更新计时器和激活状态。
     *
     * @param text 提示文本（null 或空字符串会重置状态）
     * @param hovering 鼠标是否悬停在目标上
     * @param mouseX 鼠标 X 坐标
     * @param mouseY 鼠标 Y 坐标
     * @param deltaSeconds 从上次更新经过的时间（秒）
     */
    public void update(final String text, final boolean hovering, final int mouseX, final int mouseY,
                       final double deltaSeconds) {
        final double delta = Math.max(0.0d, deltaSeconds);
        if (!hovering || text == null || text.isEmpty()) {
            reset();
            return;
        }
        final String next = text;
        if (!Objects.equals(next, pendingText)) {
            pendingText = next;
            timer = 0.0d;
            active = false;
        }
        timer += delta;
        if (timer >= delaySeconds) {
            active = true;
            activeState = new TooltipState(next, mouseX + OFFSET_X, mouseY + OFFSET_Y);
        }
    }

    /**
     * 获取当前激活的提示状态。
     *
     * @return Optional 包装的提示状态，如果未激活则为空
     */
    public Optional<TooltipState> getActiveState() {
        if (active && activeState != null) {
            return Optional.of(activeState);
        }
        return Optional.empty();
    }

    /**
     * 重置管理器状态。
     * 清除计时器、激活状态和待显示文本。
     */
    public void reset() {
        timer = 0.0d;
        active = false;
        pendingText = "";
        activeState = null;
    }

    /**
     * 工具提示状态 - 存储提示的文本和位置信息。
     * 这是一个不可变的数据类。
     */
    public static final class TooltipState {

        /** 提示文本 */
        private final String text;
        /** 显示位置 X 坐标 */
        private final int x;
        /** 显示位置 Y 坐标 */
        private final int y;

        /**
         * 创建提示状态。
         * 此构造器仅在包内可见。
         *
         * @param text 提示文本
         * @param x X 坐标
         * @param y Y 坐标
         */
        TooltipState(final String text, final int x, final int y) {
            this.text = text;
            this.x = x;
            this.y = y;
        }

        /**
         * 获取提示文本。
         *
         * @return 文本内容
         */
        public String getText() {
            return text;
        }

        /**
         * 获取显示位置的 X 坐标。
         *
         * @return X 坐标
         */
        public int getX() {
            return x;
        }

        /**
         * 获取显示位置的 Y 坐标。
         *
         * @return Y 坐标
         */
        public int getY() {
            return y;
        }
    }
}
