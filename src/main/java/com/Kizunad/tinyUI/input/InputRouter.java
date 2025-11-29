package com.Kizunad.tinyUI.input;

import com.Kizunad.tinyUI.core.InteractiveElement;
import com.Kizunad.tinyUI.core.UIElement;
import java.util.Objects;
import java.util.Optional;

/**
 * 输入路由器 - 将鼠标/键盘输入路由到目标元素，并协调焦点与热键。
 * <p>
 * 功能：
 * <ul>
 *   <li>将鼠标事件发送到点击位置最顶层的交互元素</li>
 *   <li>将键盘事件发送到当前获得焦点的元素</li>
 *   <li>处理热键组合</li>
 *   <li>自动管理焦点转移</li>
 * </ul>
 * <p>
 * 使用方法：
 * <pre>{@code
 * InputRouter router = new InputRouter(rootElement, focusManager, hotkeyManager);
 * router.mouseClick(x, y, button);  // 在点击事件中调用
 * router.keyPressed(keyCode, scanCode, modifiers);  // 在按键事件中调用
 * }</pre>
 *
 * @see InteractiveElement
 * @see FocusManager
 * @see HotkeyManager
 */
public final class InputRouter {

    /** UI 树的根节点 */
    private UIElement root;
    /** 焦点管理器 */
    private final FocusManager focusManager;
    /** 热键管理器 */
    private final HotkeyManager hotkeyManager;

    /**
     * 创建输入路由器。
     *
     * @param root UI 树的根节点（不能为 null）
     * @param focusManager 焦点管理器（不能为 null）
     * @param hotkeyManager 热键管理器（不能为 null）
     * @throws NullPointerException 如果任何参数为 null
     */
    public InputRouter(final UIElement root, final FocusManager focusManager,
                       final HotkeyManager hotkeyManager) {
        this.root = Objects.requireNonNull(root, "root");
        this.focusManager = Objects.requireNonNull(focusManager, "focusManager");
        this.hotkeyManager = Objects.requireNonNull(hotkeyManager, "hotkeyManager");
    }

    /**
     * 设置新的根节点。
     *
     * @param root 新的 UI 树根节点（不能为 null）
     * @throws NullPointerException 如果 root 为 null
     */
    public void setRoot(final UIElement root) {
        this.root = Objects.requireNonNull(root, "root");
    }

    /**
     * 路由鼠标点击事件。
     * 找到点击位置最顶层的交互元素，并设置其为焦点，然后调用元素的点击处理。
     *
     * @param mouseX 鼠标 X 坐标
     * @param mouseY 鼠标 Y 坐标
     * @param button 鼠标按钮（0 = 左键, 1 = 右键, 2 = 中键）
     * @return true 如果事件被处理，false 否则
     */
    public boolean mouseClick(final double mouseX, final double mouseY, final int button) {
        final InteractiveElement target = findTopInteractive(root, mouseX, mouseY);
        if (target == null) {
            return false;
        }
        focusManager.requestFocus(target);
        return target.onMouseClick(mouseX, mouseY, button);
    }

    /**
     * 路由鼠标释放事件。
     * 找到释放位置最顶层的交互元素，并调用其释放处理。
     *
     * @param mouseX 鼠标 X 坐标
     * @param mouseY 鼠标 Y 坐标
     * @param button 鼠标按钮
     * @return true 如果事件被处理，false 否则
     */
    public boolean mouseRelease(final double mouseX, final double mouseY, final int button) {
        final InteractiveElement target = findTopInteractive(root, mouseX, mouseY);
        if (target == null) {
            return false;
        }
        return target.onMouseRelease(mouseX, mouseY, button);
    }

    /**
     * 路由鼠标滚轮事件。
     * 找到鼠标位置最顶层的交互元素，并调用其滚轮处理。
     *
     * @param mouseX 鼠标 X 坐标
     * @param mouseY 鼠标 Y 坐标
     * @param delta 滚轮增量（正值表示向上滚，负值表示向下滚）
     * @return true 如果事件被处理，false 否则
     */
    public boolean mouseScroll(final double mouseX, final double mouseY, final double delta) {
        final InteractiveElement target = findTopInteractive(root, mouseX, mouseY);
        if (target == null) {
            return false;
        }
        return target.onMouseScroll(mouseX, mouseY, delta);
    }

    /**
     * 路由鼠标拖拽事件。
     * 发送给当前焦点元素（通常是刚被点击的元素）。
     *
     * @param mouseX 鼠标 X 坐标
     * @param mouseY 鼠标 Y 坐标
     * @param button 鼠标按钮
     * @param dragX X 轴拖拽增量
     * @param dragY Y 轴拖拽增量
     * @return true 如果事件被处理，false 否则
     */
    public boolean mouseDrag(final double mouseX, final double mouseY, final int button,
                             final double dragX, final double dragY) {
        final Optional<InteractiveElement> focused = focusManager.getFocused();
        return focused.isPresent() && focused.get().onMouseDrag(mouseX, mouseY, button, dragX, dragY);
    }

    /**
     * 路由按键事件。
     * 先尝试发送给当前焦点元素，如果未处理再尝试触发热键。
     *
     * @param keyCode 按键码
     * @param scanCode 扫描码
     * @param modifiers 修饰键（Shift/Ctrl/Alt 组合）
     * @return true 如果事件被处理或热键被触发，false 否则
     */
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        final Optional<InteractiveElement> focused = focusManager.getFocused();
        if (focused.isPresent() && focused.get().onKeyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return hotkeyManager.dispatch(KeyStroke.of(keyCode, modifiers));
    }

    /**
     * 路由字符输入事件。
     * 只发送给当前焦点元素（用于文本输入）。
     *
     * @param codePoint Unicode 码点
     * @param modifiers 修饰键
     * @return true 如果事件被处理，false 否则
     */
    public boolean charTyped(final char codePoint, final int modifiers) {
        final Optional<InteractiveElement> focused = focusManager.getFocused();
        return focused.isPresent() && focused.get().onCharTyped(codePoint, modifiers);
    }

    /**
     * 在 UI 树中查找指定位置最顶层的交互元素。
     * 使用深度优先搜索，从后向前遍历子节点（最后添加的元素在最顶层）。
     *
     * @param current 当前节点
     * @param mouseX 鼠标 X 坐标
     * @param mouseY 鼠标 Y 坐标
     * @return 找到的交互元素，或 null 如果没有找到
     */
    private InteractiveElement findTopInteractive(final UIElement current, final double mouseX,
                                                  final double mouseY) {
        if (current == null || !current.isEnabledAndVisible()) {
            return null;
        }
        if (!current.isPointInside(mouseX, mouseY)) {
            return null;
        }
        // 从后向前遍历子节点（最后添加的在最顶层）
        final int childCount = current.getChildren().size();
        for (int i = childCount - 1; i >= 0; i--) {
            final UIElement child = current.getChildren().get(i);
            final InteractiveElement hit = findTopInteractive(child, mouseX, mouseY);
            if (hit != null) {
                return hit;
            }
        }
        if (current instanceof InteractiveElement) {
            return (InteractiveElement) current;
        }
        return null;
    }
}
