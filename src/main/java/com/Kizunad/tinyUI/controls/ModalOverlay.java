package com.Kizunad.tinyUI.controls;

import com.Kizunad.tinyUI.core.InteractiveElement;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRenderContext;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.Objects;

/**
 * 模态遮罩层控件 - 用于显示模态对话框或弹出窗口。
 * 继承自 {@link InteractiveElement}，提供全屏遮罩和内容容器功能。
 * <p>
 * 功能：
 * <ul>
 *   <li>全屏半透明遮罩背景</li>
 *   <li>阻止背景交互（点击穿透）</li>
 *   <li>可设置内容元素</li>
 *   <li>支持按键关闭（如 ESC 键）</li>
 *   <li>可设置关闭回调</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>{@code
 * ModalOverlay modal = new ModalOverlay(theme);
 * modal.setContent(dialogElement);
 * modal.setCloseKeyCode(GLFW.GLFW_KEY_ESCAPE);
 * modal.setOnClose(() -> System.out.println("Modal closed"));
 * }</pre>
 *
 * @see InteractiveElement
 * @see Theme
 */
public final class ModalOverlay extends InteractiveElement {

    /** 默认关闭键（GLFW_KEY_ESCAPE），-1 可用于禁用。 */
    private static final int DEFAULT_CLOSE_KEY = 256;
    /** 默认遮罩层透明度（ARGB 格式，0x88 = 约 53% 不透明度） */
    private static final int DEFAULT_ALPHA = 0x88000000;

    /** 主题配置 */
    private final Theme theme;
    /** 模态窗口的内容元素 */
    private UIElement content;
    /** 关闭回调函数 */
    private Runnable onClose;
    /** 关闭按键码（默认 ESC，-1 表示不设置快捷键） */
    private int closeKeyCode = DEFAULT_CLOSE_KEY;

    /**
     * 创建模态遮罩层。
     *
     * @param theme 主题配置（不能为 null）
     * @throws NullPointerException 如果 theme 为 null
     */
    public ModalOverlay(final Theme theme) {
        this.theme = Objects.requireNonNull(theme, "theme");
    }

    /**
     * 设置模态窗口的内容元素。
     * 如果之前有内容，会先移除旧内容。
     *
     * @param content 新的内容元素（可以为 null 表示清空）
     */
    public void setContent(final UIElement content) {
        if (this.content != null) {
            removeChild(this.content);
        }
        this.content = content;
        if (content != null) {
            addChild(content);
        }
    }

    /**
     * 设置关闭回调函数。
     * 当模态窗口关闭时（通过快捷键或程序调用）会调用此回调。
     *
     * @param onClose 关闭回调函数（可以为 null）
     */
    public void setOnClose(final Runnable onClose) {
        this.onClose = onClose;
    }

    /**
     * 设置关闭快捷键。
     * 按下指定的键时会关闭模态窗口（例如 ESC 键）。
     *
     * @param closeKeyCode 键码（-1 表示不设置快捷键）
     */
    public void setCloseKeyCode(final int closeKeyCode) {
        this.closeKeyCode = closeKeyCode;
    }

    @Override
    public boolean onMouseClick(final double mouseX, final double mouseY, final int button) {
        if (content != null && content.isPointInside(mouseX, mouseY)) {
            return forwardMouseClick(content, mouseX, mouseY, button);
        }
        return true; // 阻塞背景点击
    }

    @Override
    public boolean onMouseRelease(final double mouseX, final double mouseY, final int button) {
        if (content != null && content.isPointInside(mouseX, mouseY)) {
            return forwardMouseRelease(content, mouseX, mouseY, button);
        }
        return true;
    }

    @Override
    public boolean onKeyPressed(final int keyCode, final int scanCode, final int modifiers) {
        if (closeKeyCode >= 0 && keyCode == closeKeyCode) {
            close();
            return true;
        }
        return content instanceof InteractiveElement
                && ((InteractiveElement) content).onKeyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void onRender(final UIRenderContext context, final double mouseX, final double mouseY,
                            final float partialTicks) {
        context.drawRect(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight(), DEFAULT_ALPHA);
        if (content != null) {
            content.render(context, mouseX, mouseY, partialTicks);
        }
    }

    /**
     * 将鼠标点击事件转发给内容元素。
     * 仅当内容元素是交互元素时才会转发。
     *
     * @return true 如果事件被处理
     */
    private boolean forwardMouseClick(final UIElement element, final double mouseX,
                                      final double mouseY, final int button) {
        if (element instanceof InteractiveElement) {
            return ((InteractiveElement) element).onMouseClick(mouseX, mouseY, button);
        }
        return false;
    }

    /**
     * 将鼠标释放事件转发给内容元素。
     * 仅当内容元素是交互元素时才会转发。
     *
     * @return true 如果事件被处理
     */
    private boolean forwardMouseRelease(final UIElement element, final double mouseX,
                                        final double mouseY, final int button) {
        if (element instanceof InteractiveElement) {
            return ((InteractiveElement) element).onMouseRelease(mouseX, mouseY, button);
        }
        return false;
    }

    /**
     * 关闭模态窗口。
     * 设置为不可见并触发关闭回调。
     */
    private void close() {
        setVisible(false);
        if (onClose != null) {
            onClose.run();
        }
    }
}
