package com.Kizunad.tinyUI.input;

import com.Kizunad.tinyUI.core.InteractiveElement;
import java.util.Objects;
import java.util.Optional;

/**
 * 焦点管理器 - 管理焦点切换，保证同一时间仅一个元素持有焦点。
 * <p>
 * 功能：
 * <ul>
 *   <li>管理当前获得焦点的元素</li>
 *   <li>支持焦点请求和清除</li>
 *   <li>自动检查元素是否可获得焦点</li>
 *   <li>在焦点切换时通知元素</li>
 * </ul>
 *
 * @see InteractiveElement
 */
public final class FocusManager {

    /** 当前获得焦点的元素（可以为 null） */
    private InteractiveElement focused;

    /**
     * 请求为指定元素设置焦点。
     * 如果元素不可获得焦点，请求将失败。
     * 如果元素已经有焦点，直接返回 true。
     * 否则，清除旧焦点并设置新焦点。
     *
     * @param element 要获得焦点的元素（不能为 null）
     * @return true 如果焦点设置成功，false 否则
     * @throws NullPointerException 如果 element 为 null
     */
    public boolean requestFocus(final InteractiveElement element) {
        Objects.requireNonNull(element, "element");
        if (!element.isFocusable()) {
            return false;
        }
        if (element == focused) {
            return true;
        }
        clearFocus();
        focused = element;
        focused.setFocused(true);
        return true;
    }

    /**
     * 清除当前焦点。
     * 如果没有元素持有焦点，此方法无效。
     */
    public void clearFocus() {
        if (focused == null) {
            return;
        }
        final InteractiveElement previous = focused;
        focused = null;
        previous.setFocused(false);
    }

    /**
     * 获取当前持有焦点的元素。
     *
     * @return Optional 包装的焦点元素，如果没有焦点则为空
     */
    public Optional<InteractiveElement> getFocused() {
        return Optional.ofNullable(focused);
    }

    /**
     * 检查指定元素是否当前持有焦点。
     *
     * @param element 要检查的元素
     * @return true 如果该元素持有焦点，false 否则
     */
    public boolean isFocused(final InteractiveElement element) {
        return focused == element;
    }
}
