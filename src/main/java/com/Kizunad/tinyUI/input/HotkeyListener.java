package com.Kizunad.tinyUI.input;

/**
 * 热键监听器接口 - 用于响应热键组合事件。
 * <p>
 * 实现此接口以处理特定的热键组合。
 * 监听器通过 {@link HotkeyManager#register} 注册。
 *
 * @see HotkeyManager
 * @see KeyStroke
 */
public interface HotkeyListener {

    /**
     * @return true if the hotkey was handled and should not propagate further.
     */
    boolean onHotkey(KeyStroke stroke);
}
