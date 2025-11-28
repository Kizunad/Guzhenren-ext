package com.Kizunad.tinyUI.controls;

import com.Kizunad.tinyUI.input.KeyStroke;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TextInputTest {

    private static final int WIDTH = 100;
    private static final int HEIGHT = 20;

    @Test
    void typingAddsCharactersAndBackspaceRemoves() {
        final TextInput input = new TextInput(Theme.vanilla());
        input.setFrame(0, 0, WIDTH, HEIGHT);
        final AtomicReference<String> last = new AtomicReference<>("");
        input.setOnChange(last::set);

        assertTrue(input.onCharTyped('a', 0));
        assertTrue(input.onCharTyped('b', 0));
        assertEquals("ab", input.getText());
        assertEquals("ab", last.get());

        assertTrue(input.onKeyPressed(TextInput.KEY_BACKSPACE, 0, 0));
        assertEquals("a", input.getText());
    }

    @Test
    void ctrlASelectAllThenTypeReplaces() {
        final TextInput input = new TextInput(Theme.vanilla());
        input.setFrame(0, 0, WIDTH, HEIGHT);
        input.setText("abc");

        final int ctrl = KeyStroke.CONTROL;
        assertTrue(input.onKeyPressed('A', 0, ctrl));
        assertTrue(input.onCharTyped('z', 0));
        assertEquals("z", input.getText());
    }

    @Test
    void mouseClickMovesCursor() {
        final TextInput input = new TextInput(Theme.vanilla());
        input.setFrame(0, 0, WIDTH, HEIGHT);
        input.setText("abcd");

        assertTrue(input.onMouseClick(20, 5, 0));
        // With char width 6 and padding 3 -> (20-3)/6 = 2
        assertTrue(input.onCharTyped('x', 0));
        assertEquals("abxcd", input.getText());
    }
}
