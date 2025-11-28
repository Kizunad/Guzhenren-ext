# tinyUI 使用说明（NeoForge 客户端）

## 模块概览
- modId：`tinyui`（已在 `META-INF/neoforge.mods.toml` 声明）。
- 功能：提供原生风格 UI 核心 + 示例命令 `/tinyui demo <id>` 打开演示界面。
- 依赖：NeoForge 1.21.1（随主工程）。

## 构建与产物
- 仅 tinyUI jar：`./gradlew tinyUiJar`，产物 `build/libs/guzhenrenext-1.0.0-tinyui.jar`。
- 仅 tinyUI 测试：`./gradlew tinyUiTest`（或 `-x compileJava` 避开其他模块编译）。

## 客户端命令（演示）
- 在客户端聊天输入 `/tinyui demo <id>`：
  - `0`：Flex 布局 + 按钮示例。
  - `1`：快捷键配置示例（HotkeyCapture + TextInput）。
  - `2`：网格/槽位 + 滚动示例。
- 命令注册于 `RegisterClientCommandsEvent`，需客户端环境；若无补全，请检查 tinyui 模块是否加载。

## 在屏幕中嵌入 tinyUI
1. 创建 `UIRoot` 并构建控件树（可复用 `DemoRegistry`）。
2. 使用 `new TinyUIScreen(Component.literal("title"), root)` 作为 Screen：
   - 渲染桥接：`GuiRenderContext` 包装 `GuiGraphics`。
   - 输入桥接：鼠标/滚轮/键盘/字符输入均转发到 tinyUI。
3. 打开界面：`Minecraft.getInstance().setScreen(new TinyUIScreen(...))`。

## 资源与样式
- 当前极简纯色，无外部纹理。`GuiRenderContext.drawNinePatch` 支持回退填充；若需原版风纹理，可在 `assets/<modid>/textures/gui/tinyui/` 放置 PNG 并扩展此方法。

## 代码位置速览
- 入口：`com.Kizunad.tinyUI.TinyUIMod`
- 命令注册：`com.Kizunad.tinyUI.neoforge.TinyUIClientCommands`
- 命令实现：`com.Kizunad.tinyUI.neoforge.TinyUIDemoCommand`
- Screen 桥接：`com.Kizunad.tinyUI.neoforge.TinyUIScreen`
- Demo 生成：`com.Kizunad.tinyUI.demo.DemoRegistry`
