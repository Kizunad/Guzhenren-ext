package com.Kizunad.guzhenrenext.kongqiao.flyingsword.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FlyingSwordHubScreenStateTests {

    private static final Path HUB_SCREEN_SOURCE = Path.of(
        "src/main/java/com/Kizunad/guzhenrenext/kongqiao/flyingsword/client/FlyingSwordHubScreen.java"
    );

    @Test
    void reboundToggleCloseUsesRealHubKeybindingInsteadOfHardcodedH() throws Exception {
        final String source = readHubScreenSource();
        final String closeKeyMethod = extractBlock(source, "static boolean isCloseKey(final int keyCode)");
        final String toggleKeyMethod = extractBlock(source, "private static int resolveHubToggleKeyCode()");

        assertTrue(closeKeyMethod.contains("resolveHubToggleKeyCode()"));
        assertTrue(closeKeyMethod.contains("GLFW.GLFW_KEY_ESCAPE"));
        assertTrue(closeKeyMethod.contains("GLFW.GLFW_KEY_TAB"));
        assertFalse(closeKeyMethod.contains("GLFW.GLFW_KEY_H"));

        assertTrue(toggleKeyMethod.contains("GuKeyBindings.FLYING_SWORD_TOGGLE_HUB.getKey()"));
        assertTrue(toggleKeyMethod.contains("InputConstants.Type.KEYSYM"));
    }

    @Test
    void topLevelTabsResolveFreshLocalizedTextPerCall() throws Exception {
        final String source = readHubScreenSource();
        final String buildUiMethod = extractBlock(source, "private void buildUi(final UIRoot root)");
        final String testingAccessor = extractBlock(source, "static List<String> topLevelTabTitlesForTesting()");
        final String topLevelTabsMethod = extractBlock(source, "private static List<String> topLevelTabs()");

        assertFalse(source.contains("private static final List<String> TOP_LEVEL_TABS"));
        assertTrue(testingAccessor.contains("return topLevelTabs();"));
        assertTrue(buildUiMethod.contains("final List<String> topLevelTabs = topLevelTabs();"));
        assertTrue(topLevelTabsMethod.contains("localizedText(KongqiaoI18n.FLYING_SWORD_HUB_TAB_OVERVIEW)"));
        assertTrue(topLevelTabsMethod.contains("localizedText(KongqiaoI18n.FLYING_SWORD_HUB_TAB_CULTIVATION)"));
        assertTrue(topLevelTabsMethod.contains("localizedText(KongqiaoI18n.FLYING_SWORD_HUB_TAB_HELP)"));
    }

    private static String readHubScreenSource() throws IOException {
        return Files.readString(HUB_SCREEN_SOURCE, StandardCharsets.UTF_8);
    }

    private static String extractBlock(final String source, final String anchor) {
        final int anchorIndex = source.indexOf(anchor);
        assertTrue(anchorIndex >= 0, "缺少锚点: " + anchor);
        final int blockStart = source.indexOf('{', anchorIndex);
        assertTrue(blockStart >= 0, "缺少方法体起始: " + anchor);
        int depth = 1;
        for (int index = blockStart + 1; index < source.length(); index++) {
            final char current = source.charAt(index);
            if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;
                if (depth == 0) {
                    return source.substring(anchorIndex, index + 1);
                }
            }
        }
        throw new IllegalStateException("未找到完整代码块: " + anchor);
    }
}
