# tinyUI 原版风 UI 库规划（NeoForge 1.21，原生样式）

## 目标与约束
- 范围：基础控件 + 布局（按钮/输入/列表/弹窗，Flex/栅格布局）。
- 场景：Minecraft 内嵌 UI（NeoForge 1.21），原版风格，纯 Java；允许可选 PNG 资源（背景/九宫格）但无外部库。
- 输入：支持鼠标 + 键盘快捷键；无手柄/触摸。
- 状态：事件回调 + 数据监听（观察者/信号），便于 UI 随数据变动刷新。
- 主题：固定主题，保持原版观感；资源可替换但无运行时换肤。
- 测试：以单元测试为主，验证布局计算、事件分发、数据监听与渲染输出参数。

## 设计原则
- KISS/YAGNI：只做当前需要的基础控件与布局；不实现过度动画或皮肤系统。
- SOLID：渲染、事件、布局、状态解耦；控件依赖抽象接口；容器负责组合不负责业务。
- DRY：统一事件与布局管线，避免各控件自实现一套。

## 架构草案
- 渲染管线：`UIRenderContext`（矩阵/颜色/纹理绑定）；`Drawable` 接口，控件实现 `draw(context, mouseX, mouseY, partialTicks)`.
- 布局系统：
  - Flex：主轴/交叉轴对齐，按子项尺寸与权重分配；支持 wrap=false/true（可后置）。
  - Grid：固定列/行，支持跨列/行简化版（可延后），先做均分网格。
  - 绝对定位辅助：提供锚点+偏移用于 HUD/Overlay 兼容。
- 控件基类：`UIElement`（位置/尺寸/可见/启用/children）；`InteractiveElement`（hover/focus/press）；统一鼠标/键盘事件处理。
- 输入与快捷键：
  - 鼠标：点击/滚轮/拖拽事件分发；命中测试由元素树决定。
  - 键盘：焦点控件接受按键；全局快捷键表（命中后向监听者广播）。
  - 可选重复按键处理（后置）。
- 状态/数据监听：
  - 轻量 `ObservableValue<T>` 与 `Subscription`；控件可绑定数据源，在变更时触发 `invalidate` 重绘/重布局。
  - 简单信号/事件总线用于通知（如弹窗关闭、列表刷新）。
- 资源与皮肤：
  - 固定主题配置：色板、边距、圆角、阴影/描边参数。
  - 九宫格背景与基础 PNG 资源可选加载；未提供资源时走纯色/渐变回退。
- 典型控件：
  - Button（含禁用态/hover/press），ToggleButton
  - Label/Text（支持对齐）
  - TextInput（基础文本输入 + 光标/选中，快捷键：Ctrl+A/C/V，后置剪贴板支持）
  - ScrollList/ScrollContainer（可嵌子项，滚轮/拖动）
  - Tooltip 系统（延时显示，跟随鼠标）
  - Modal/Overlay 容器（阻塞点击，ESC 关闭）
- 复用场景示例：
  - 圆形空窍存储 Container UI：用 Grid/Flex 布局，按钮/slot 背景+Tooltip。
  - 自定义快捷键配置页：列表项含描述+按键捕获，数据监听更新显示。

## 包结构与接口设计（初稿）
- `com.Kizunad.tinyUI.core`
  - `UIElement`（基础属性/树）、`InteractiveElement`（输入状态）、`UIRenderContext`（渲染上下文）。
  - `UIRoot`（根容器，接入 MC 事件/渲染）。
- `com.Kizunad.tinyUI.layout`
  - `FlexLayout`, `FlexParams`；`GridLayout`, `GridParams`；`Anchor`（绝对+锚点辅助）。
- `com.Kizunad.tinyUI.input`
  - `InputRouter`（鼠标/键盘分发），`FocusManager`，`HotkeyManager`。
- `com.Kizunad.tinyUI.state`
  - `ObservableValue<T>`, `Subscription`，`Bindings`（属性绑定助手）。
- `com.Kizunad.tinyUI.theme`
  - `Theme`（色板、圆角、padding、字号），`NinePatch`（九宫格背景），`Textures`（PNG 资源描述）。
- `com.Kizunad.tinyUI.controls`
  - `Button`, `ToggleButton`, `Label`, `TextInput`, `ScrollContainer`, `Tooltip`, `ModalOverlay`。
- `com.Kizunad.tinyUI.demo`
  - 示例界面与手动验证入口（圆形空窍 UI，快捷键配置页）。

## 里程碑与任务拆分（原子任务组合）
1) 基础框架
   - 建立 `ui` 包结构，定义 `UIElement`/`InteractiveElement`/`UIRenderContext` 接口与基础实现。
   - 事件分发：鼠标/键盘路由与命中测试；焦点管理。
   - 可测试性：为布局/事件提供纯逻辑单元测试（无渲染依赖）。
2) 布局模块
   - Flex 布局：主轴/交叉轴对齐、gap、padding、flex grow/shrink，单行版。
   - Grid 布局：行列均分，子项位置计算；留接口支持跨行列（后置）。
   - 绝对定位/锚点工具：便于 HUD/Overlay 固定位置。
3) 主题与资源
   - 固定主题配置类（颜色、字体大小、边距、圆角）。
   - 九宫格背景渲染器，支持可选 PNG；无资源时回退纯色/描边。
4) 控件实现
   - Button/ToggleButton（禁用、hover、press 态；点击回调）。
   - Label/Text（对齐、颜色）。
   - TextInput（文本编辑、光标/选区、基本快捷键）。
   - ScrollContainer/List（滚动条、滚轮、内容裁剪）。
   - Tooltip 管理器（延时与位置计算）。
   - Modal/Overlay 容器（阻塞点击，ESC/按钮关闭）。
5) 状态/数据监听
   - `ObservableValue<T>`/`Subscription`，支持手动 `invalidate`。
   - 绑定助手：将数据源变更映射到控件属性（文本、可见性、启用状态）。
6) 快捷键系统
   - 全局快捷键注册/优先级；焦点控件事件优先，再到全局。
   - 显示层与容器联动（例如配置页捕获按键）。
7) 集成与示例
   - 示例界面：圆形空窍存储 UI、快捷键配置页 demo。
   - 文档与使用指南（初始化、资源放置、事件钩子）。
8) 测试计划
   - 布局：给定子项尺寸期望输出坐标/尺寸。
   - 事件：命中测试、焦点切换、滚轮与键盘分发。
   - 状态：Observable 更新触发布局/绘制标记。
   - 渲染参数：验证背景九宫格切片与坐标（逻辑层），无需真实 GPU。

## 风险与取舍
- 纯 Java 无外部依赖，渲染需使用现有 MC API，功能与性能有限；必要时以资源优化与批次绘制弥补。
- TextInput/滚动/光标等细节易出边界问题，需要用测试覆盖。
- 跨行列 Grid、剪贴板、复杂动画留待后续迭代。

## 初步设计任务（优先级）
- P0 基础框架与输入
  - 创建包骨架与核心接口：`core`、`input`、`layout`、`state`、`theme`、`controls`。
  - 实现 `UIElement` 树、`InteractiveElement`，事件命中测试与 `InputRouter`、`FocusManager`。
  - `HotkeyManager`：注册、冲突策略（先焦点后全局），基本按键模型。
- P0 布局
  - `FlexLayout`（单行/列，grow/shrink、gap、padding、对齐）。
  - `GridLayout`（均分行列，基础放置）。
  - 单元测试：Flex/Grid 布局尺寸与坐标计算。
- P0 状态
  - `ObservableValue`/`Subscription`，`Bindings` 辅助（数据改动触发标记）。
  - 单元测试：订阅/取消订阅/多监听触发顺序。
- P1 主题与资源
  - `Theme` 固定配置，`NinePatch` 解析与逻辑渲染参数（不依赖实际 GPU）。
  - 回退纯色绘制路径。
- P1 控件首批
  - `Button`/`ToggleButton`（三态渲染，点击/禁用）。
  - `Label`。
  - `ScrollContainer`（滚轮/拖动，裁剪计算）。
  - 单元测试：点击命中、滚动边界、禁用状态不触发。
- P1 Tooltip/Modal
  - Tooltip 管理与延时策略；Modal/Overlay 捕获点击、ESC 关闭。
- P2 TextInput & 快捷键配置支持
  - 文本编辑、光标移动、选中、基础快捷键（Ctrl+A/C/V 钩子留空实现或后置）。
  - 热键捕获控件（用于快捷键配置页）。
- P2 Demo
  - 圆形空窍 UI 示例（Grid + Button + Tooltip）。
  - 快捷键配置页示例（列表 + 热键捕获）。
