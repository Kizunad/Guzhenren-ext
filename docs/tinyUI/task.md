# tinyUI 任务清单

## P0 基础框架与输入
- [x] 建立包骨架与核心接口（core/input/layout/state/theme/controls/demo），提交空类/接口和基础文档注释。
- [x] 实现 UIElement/InteractiveElement 树结构，支持可见/启用、子节点管理、渲染入口。
- [x] 实现 InputRouter + FocusManager：鼠标/键盘命中测试、焦点切换、事件向子节点分发的顺序规则。
- [x] 实现 HotkeyManager：注册/注销、焦点优先再全局的冲突策略，支持组合键表示。
- [x] 单元测试：事件分发、焦点切换、热键匹配的纯逻辑测试。

## P0 布局
- [x] FlexLayout：单行/列，grow/shrink、gap、padding、对齐；计算输出子项矩形。
- [x] GridLayout：均分行列，基础放置；暂不支持跨行列。
- [x] Anchor/绝对定位工具：根据锚点+偏移计算矩形。
- [x] 单元测试：Flex/Grid/Anchor 布局输出坐标与尺寸。

## P0 状态
- [x] ObservableValue/Subscription，支持多监听、取消订阅。
- [x] Bindings 辅助：数据变化触发控件属性更新并标记重绘/重布局。
- [x] 单元测试：订阅触发顺序、取消订阅、绑定回调。

## P1 主题与资源
- [x] Theme 固定配置（色板、圆角、padding、字号）。
- [x] NinePatch 逻辑渲染参数解析；无资源时走纯色回退。
- [x] 单元测试：九宫格切片参数与尺寸计算。

## P1 控件首批
- [x] Button/ToggleButton：三态渲染，禁用态阻断事件。
- [x] Label：文本显示、对齐。
- [x] ScrollContainer：内容裁剪、滚轮/拖动、滚动边界。
- [x] 单元测试：按钮点击命中、禁用态、滚动边界。

## P1 Tooltip/Modal
- [x] Tooltip 管理：延时显示、位置计算、跟随鼠标。
- [x] Modal/Overlay：阻塞点击、ESC 关闭、背景遮罩。
- [x] 单元测试：Tooltip 延时策略、Modal 拦截事件。

## P2 TextInput & 快捷键配置
- [x] TextInput：文本编辑、光标移动、选中，基础快捷键（Ctrl+A 占位，C/V 保留）。
- [x] 热键捕获控件：用于快捷键配置页，显示当前绑定状态。
- [x] 单元测试：光标/选区移动、按键输入、捕获逻辑。

## P2 Demo
- [x] 圆形空窍 UI 示例：使用 Grid/Flex、按钮/slot 背景、Tooltip（demo 2 占位）。
- [x] 快捷键配置页示例：列表项 + 热键捕获 + 状态绑定展示（demo 1）。
- [x] 简要使用文档：初始化、资源放置路径、事件钩子接入。
