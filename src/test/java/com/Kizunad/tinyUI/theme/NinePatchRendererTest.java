package com.Kizunad.tinyUI.theme;

import com.Kizunad.tinyUI.core.UIRenderContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NinePatchRendererTest {

    private static final int WIDTH = 30;
    private static final int HEIGHT = 40;

    @Test
    void usesNinePatchWhenTextureProvided() {
        final RecordingContext context = new RecordingContext();
        final NinePatchRenderer renderer = new NinePatchRenderer(Theme.vanilla());
        final NinePatch patch = new NinePatch("path/to/tex.png", 2, 2, 2, 2);

        renderer.render(context, patch, 0, 0, WIDTH, HEIGHT);

        assertTrue(context.ninePatchCalled);
        assertFalse(context.rectCalled);
    }

    @Test
    void fallsBackToRectWhenPatchMissing() {
        final RecordingContext context = new RecordingContext();
        final NinePatchRenderer renderer = new NinePatchRenderer(Theme.vanilla());

        renderer.render(context, null, 0, 0, WIDTH, HEIGHT);

        assertFalse(context.ninePatchCalled);
        assertTrue(context.rectCalled);
    }

    private static final class RecordingContext implements UIRenderContext {

        private boolean rectCalled;
        private boolean ninePatchCalled;

        @Override
        public void pushState() {
        }

        @Override
        public void popState() {
        }

        @Override
        public void drawRect(final int x, final int y, final int width, final int height,
                             final int argbColor) {
            rectCalled = true;
        }

        @Override
        public void drawText(final String text, final int x, final int y, final int argbColor) {
        }

        @Override
        public void drawNinePatch(final NinePatch patch, final int x, final int y,
                                  final int width, final int height) {
            ninePatchCalled = true;
        }
    }
}
