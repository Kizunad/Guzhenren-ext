package com.Kizunad.tinyUI.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 管理全局热键映射，支持注册/注销与分发。
 */
public final class HotkeyManager {

    private final Map<KeyStroke, List<HotkeyListener>> listeners = new HashMap<>();

    public void register(final KeyStroke stroke, final HotkeyListener listener) {
        Objects.requireNonNull(stroke, "stroke");
        Objects.requireNonNull(listener, "listener");
        listeners.computeIfAbsent(stroke, unused -> new ArrayList<>()).add(listener);
    }

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
     * @return true if a listener handled the hotkey.
     */
    public boolean dispatch(final KeyStroke stroke) {
        final List<HotkeyListener> list = listeners.get(stroke);
        if (list == null || list.isEmpty()) {
            return false;
        }
        final List<HotkeyListener> snapshot = new ArrayList<>(list);
        for (final HotkeyListener listener : snapshot) {
            if (listener.onHotkey(stroke)) {
                return true;
            }
        }
        return false;
    }
}
