# TinyUI（src/main/java/com/Kizunad/tinyUI）

## 概述
`tinyui` 是独立的 UI 辅助 Mod/库：既可随主工程加载，也可通过 Gradle 任务打包为独立 Jar。

## 去哪找
- Mod 入口：`com/Kizunad/tinyUI/TinyUIMod.java`
- NeoForge 适配层：`com/Kizunad/tinyUI/neoforge/*`（Screen/渲染桥接/命令）
- Demo 注册：`com/Kizunad/tinyUI/demo/DemoRegistry.java`

## 模块边界
- `core/`：UI 树/渲染上下文/交互基类
- `layout/`：布局（Flex/Grid）
- `controls/`：可交互控件（Button/TextInput/Scroll/Tooltip 等）
- `theme/`：NinePatch/主题
- `input/`：输入路由/焦点/热键
- `state/`：ObservableValue/绑定

## 构建与测试
- 独立打包：`./gradlew tinyUiJar`
- 仅跑 tinyUI 单测：`./gradlew tinyUiTest`（测试位于 `src/test/java/com/Kizunad/tinyUI/**`）

## 运行/验证
- 客户端命令：`/tinyui demo <id>`（注册点：`com/Kizunad/tinyUI/neoforge/TinyUIClientCommands.java`）

## 约定
- client-only 代码必须留在 `neoforge/` 或 demo 层，不要泄漏到通用逻辑。
- 注释中文且详细；禁止 `@SuppressWarnings`。
