package com.Kizunad.tinyUI.controls;

import com.Kizunad.tinyUI.core.InteractiveElement;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ModalOverlayTest {

    private static final int SIZE = 40;
    private static final int ESC_KEY = 256;
    private static final int CONTENT_MARGIN = 5;
    private static final int CONTENT_INSET = 10;
    private static final int MOUSE_OFFSET = 5;

    @Test
    void escClosesAndFiresCallback() {
        final ModalOverlay overlay = new ModalOverlay(Theme.vanilla());
        overlay.setFrame(0, 0, SIZE, SIZE);
        final AtomicBoolean closed = new AtomicBoolean(false);
        overlay.setOnClose(() -> closed.set(true));

        assertTrue(overlay.onKeyPressed(ESC_KEY, 0, 0));
        assertFalse(overlay.isVisible(), "overlay should be hidden after close");
        assertTrue(closed.get(), "callback should be called");
    }

    @Test
    void clickOutsideBlocksInteraction() {
        final ModalOverlay overlay = new ModalOverlay(Theme.vanilla());
        overlay.setFrame(0, 0, SIZE, SIZE);
        final TestInteractive content = new TestInteractive();
        content.setFrame(CONTENT_MARGIN, CONTENT_MARGIN, SIZE - CONTENT_INSET, SIZE - CONTENT_INSET);
        overlay.setContent(content);

        assertTrue(overlay.onMouseClick(SIZE + MOUSE_OFFSET, SIZE + MOUSE_OFFSET, 0),
                "click outside should be consumed");
        assertFalse(content.clicked, "content should not receive outside click");
    }

    private static final class TestInteractive extends InteractiveElement {
        private boolean clicked;

        @Override
        public boolean onMouseClick(final double mouseX, final double mouseY, final int button) {
            clicked = true;
            return true;
        }
    }
}
