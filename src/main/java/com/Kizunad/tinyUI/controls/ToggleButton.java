package com.Kizunad.tinyUI.controls;

import com.Kizunad.tinyUI.theme.Theme;
import java.util.function.Consumer;

/**
 * 切换按钮控件 - 支持开/关两种状态的按钮。
 * 继承自 {@link Button}，并增加了切换状态功能。
 * <p>
 * 功能：
 * <ul>
 *   <li>支持开/关两种状态</li>
 *   <li>点击时自动切换状态</li>
 *   <li>可设置状态改变回调</li>
 *   <li>开启状态使用不同的背景颜色</li>
 * </ul>
 *
 * @see Button
 */
public final class ToggleButton extends Button {

    /** 当前状态（true = 开启, false = 关闭） */
    private boolean toggled;
    /** 状态改变回调函数 */
    private Consumer<Boolean> onToggle;

    /**
     * 创建切换按钮。
     *
     * @param text 按钮文本（如果为 null 则使用空字符串）
     * @param theme 主题配置（不能为 null）
     */
    public ToggleButton(final String text, final Theme theme) {
        super(text, theme);
    }

    /**
     * 设置状态改变回调函数。
     * 当按钮状态改变时（点击或程序设置），会调用此回调。
     *
     * @param onToggle 回调函数，参数为新的状态（可以为 null）
     */
    public void setOnToggle(final Consumer<Boolean> onToggle) {
        this.onToggle = onToggle;
    }

    /**
     * 获取当前的切换状态。
     *
     * @return true 表示开启，false 表示关闭
     */
    public boolean isToggled() {
        return toggled;
    }

    /**
     * 设置切换状态。
     * 如果新状态与当前状态不同，则更新并触发回调。
     *
     * @param toggled 新的状态（true = 开启, false = 关闭）
     */
    public void setToggled(final boolean toggled) {
        if (this.toggled == toggled) {
            return;
        }
        this.toggled = toggled;
        notifyToggle();
    }

    @Override
    public void setOnClick(final Runnable onClick) {
        // Toggle button uses onToggle; ignore standalone click handler.
    }

    @Override
    public boolean onMouseRelease(final double mouseX, final double mouseY, final int button) {
        final boolean handled = super.onMouseRelease(mouseX, mouseY, button);
        if (handled && isPointInside(mouseX, mouseY) && isEnabledAndVisible()) {
            toggled = !toggled;
            notifyToggle();
        }
        return handled;
    }

    @Override
    protected int chooseBackground(final boolean hovered) {
        if (!isEnabled()) {
            return super.chooseBackground(hovered);
        }
        if (toggled) {
            return hovered ? super.chooseBackground(true) : super.chooseBackground(false);
        }
        return super.chooseBackground(hovered);
    }

    /**
     * 通知监听者状态已改变。
     * 如果设置了回调函数，则调用它。
     */
    private void notifyToggle() {
        final Consumer<Boolean> callback = this.onToggle;
        if (callback != null) {
            callback.accept(toggled);
        }
    }
}
