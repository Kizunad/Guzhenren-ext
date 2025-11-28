package com.Kizunad.tinyUI.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 热键管理器 - 管理全局热键映射，支持注册/注销与分发。
 * <p>
 * 功能：
 * <ul>
 *   <li>注册热键组合和对应的监听器</li>
 *   <li>支持同一热键组合多个监听器</li>
 *   <li>按注册顺序分发热键事件</li>
 *   <li>支持监听器动态注册和注销</li>
 * </ul>
 *
 * @see KeyStroke
 * @see HotkeyListener
 */
public final class HotkeyManager {

    /** 热键组合到监听器列表的映射 */
    private final Map<KeyStroke, List<HotkeyListener>> listeners = new HashMap<>();

    /**
     * 注册热键监听器。
     * 将监听器添加到指定热键组合的监听器列表中。
     *
     * @param stroke 热键组合（不能为 null）
     * @param listener 监听器（不能为 null）
     * @throws NullPointerException 如果任何参数为 null
     */
    public void register(final KeyStroke stroke, final HotkeyListener listener) {
        Objects.requireNonNull(stroke, "stroke");
        Objects.requireNonNull(listener, "listener");
        listeners.computeIfAbsent(stroke, unused -> new ArrayList<>()).add(listener);
    }

    /**
     * 注销热键监听器。
     * 从指定热键组合的监听器列表中移除监听器。
     * 如果移除后列表为空，则从映射中移除该热键组合。
     *
     * @param stroke 热键组合
     * @param listener 要移除的监听器
     */
    public void unregister(final KeyStroke stroke, final HotkeyListener listener) {
        final List<HotkeyListener> list = listeners.get(stroke);
        if (list == null) {
            return;
        }
        list.remove(listener);
        if (list.isEmpty()) {
            listeners.remove(stroke);
        }
    }

    /**
     * 分发热键事件。
     * 按注册顺序调用所有监听器，直到有监听器处理该事件。
     *
     * @param stroke 热键组合
     * @return true 如果有监听器处理了该热键，false 否则
     */
    public boolean dispatch(final KeyStroke stroke) {
        final List<HotkeyListener> list = listeners.get(stroke);
        if (list == null || list.isEmpty()) {
            return false;
        }
        // 创建快照以避免并发修改问题
        final List<HotkeyListener> snapshot = new ArrayList<>(list);
        for (final HotkeyListener listener : snapshot) {
            if (listener.onHotkey(stroke)) {
                return true;
            }
        }
        return false;
    }
}
