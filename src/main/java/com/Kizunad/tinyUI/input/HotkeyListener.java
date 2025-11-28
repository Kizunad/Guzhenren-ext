package com.Kizunad.tinyUI.input;

public interface HotkeyListener {

    /**
     * @return true if the hotkey was handled and should not propagate further.
     */
    boolean onHotkey(KeyStroke stroke);
}
