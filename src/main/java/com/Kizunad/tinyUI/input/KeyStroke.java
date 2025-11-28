package com.Kizunad.tinyUI.input;

import java.util.Objects;

/**
 * 按键与修饰键组合。
 */
public final class KeyStroke {

    public static final int SHIFT = 1;
    public static final int CONTROL = 1 << 1;
    public static final int ALT = 1 << 2;
    private static final int META_SHIFT = 3;
    public static final int META = 1 << META_SHIFT;

    private final int keyCode;
    private final int modifiers;

    private KeyStroke(final int keyCode, final int modifiers) {
        this.keyCode = keyCode;
        this.modifiers = modifiers;
    }

    public static KeyStroke of(final int keyCode, final int modifiers) {
        return new KeyStroke(keyCode, modifiers);
    }

    public static KeyStroke of(final int keyCode, final boolean shift, final boolean control,
                               final boolean alt, final boolean meta) {
        int mods = 0;
        if (shift) {
            mods |= SHIFT;
        }
        if (control) {
            mods |= CONTROL;
        }
        if (alt) {
            mods |= ALT;
        }
        if (meta) {
            mods |= META;
        }
        return new KeyStroke(keyCode, mods);
    }

    public int getKeyCode() {
        return keyCode;
    }

    public int getModifiers() {
        return modifiers;
    }

    public boolean matches(final int keyCode, final int modifiers) {
        return this.keyCode == keyCode && this.modifiers == modifiers;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof KeyStroke)) {
            return false;
        }
        final KeyStroke keyStroke = (KeyStroke) o;
        return keyCode == keyStroke.keyCode && modifiers == keyStroke.modifiers;
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyCode, modifiers);
    }

    @Override
    public String toString() {
        return "KeyStroke{" + "keyCode=" + keyCode + ", modifiers=" + modifiers + '}';
    }
}
