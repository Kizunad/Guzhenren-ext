# NPC 交互界面规格说明书 (UI Specifications)

本以此文档为 UI 开发的精确参照。坐标系统基于 `1920 x 1080` 分辨率下的 `GUI Scale: Auto (通常为 2 或 3)`，此处使用逻辑像素 (Logical Pixels) 描述相对位置，以 **居中对齐 (Center-Aligned)** 为基准。

## 1. 全局布局 (Global Layout)

*   **对齐方式**: `Center` (屏幕中心点 (0,0))
*   **容器层级**:
    *   `Layer 0`: 游戏画面 (变暗/模糊处理)
    *   `Layer 1`: **Dashboard Panel** (默认显示)
    *   `Layer 2`: **Dialogue Panel** (互斥显示，位于 Layer 1 之上或替代之)

---

## 2. 仪表盘 (Dashboard Panel)

*   **总尺寸**: `680w x 400h`
*   **位置**: 屏幕正中 `(x: -340, y: -200)` (相对于中心点)
*   **背景**: 深色半透明圆角矩形 (`#202020`, Alpha 85%)

### 2.1 区域 A: 实体预览 (Entity Preview)
*   **位置**: 左侧
*   **坐标**: `Relative(20, 40)` (相对于 Panel 左上角)
*   **尺寸**: `200w x 280h`
*   **组件**:
    *   `EntityWidget`: `x:0, y:0, w:200, h:280` (渲染 NPC 模型，支持鼠标拖拽旋转)
    *   `NameLabel`: `x:0, y:-30` (位于模型上方，居中，字号 Large)

### 2.2 区域 B: 状态列表 (Status List) - [可扩展区域]
*   **位置**: 中部
*   **坐标**: `Relative(240, 40)`
*   **尺寸**: `240w x 320h`
*   **布局**: `VerticalScrollList` (垂直滚动列表)
*   **Item 规格**:
    *   **高度**: `30px` per item
    *   **间距**: `5px`
    *   **内容格式**: `[Icon] Label: Value` (例如: `[♥] HP: 20/20`)
*   **扩展接口数据**:
    *   输入: `List<StatusEntry>`
    *   `StatusEntry`: `{ iconPath, labelKey, valueString, color }`

### 2.3 区域 C: 功能菜单 (Action Menu)
*   **位置**: 右侧
*   **坐标**: `Relative(520, 40)`
*   **尺寸**: `120w x 320h`
*   **布局**: `VerticalLayout` (垂直排列，右对齐)
*   **按钮规格**:
    *   **尺寸**: `120w x 40h`
    *   **间距**: `15px`
*   **默认按钮**:
    1.  **Trade** (交易)
    2.  **Gift** (赠送/背包)
    3.  **Chat** (对话 - 切换到 Dialogue Panel)
    4.  **Owner** (管理 - 仅 Owner 可见)

---

## 3. 对话面板 (Dialogue Panel)

*   **总尺寸**: `680w x 360h`
*   **位置**: 屏幕底部居中 `(x: -340, y: 100)` (偏下沉浸式)
*   **背景**: 深色渐变/装饰框 (`#101010`, Alpha 95%)

### 3.1 区域 D: 肖像 (Portrait)
*   **位置**: 左侧
*   **坐标**: `Relative(20, 20)`
*   **尺寸**: `150w x 300h`
*   **组件**:
    *   `Image/EntityWidget`: 显示 NPC 半身像或特写。
    *   `NamePlate`: `x:0, y:260` (名字牌)

### 3.2 区域 E: 对话内容 (Dialogue Content)
*   **位置**: 右侧上方
*   **坐标**: `Relative(190, 20)`
*   **尺寸**: `460w x 120h`
*   **组件**:
    *   `Label`: `WrapText` (自动换行), `FontSize: Medium`
    *   **打字机效果**: 文本逐字显示。

### 3.3 区域 F: 选项列表 (Chat Options) - [可扩展区域]
*   **位置**: 右侧下方
*   **坐标**: `Relative(190, 150)`
*   **尺寸**: `460w x 190h`
*   **布局**: `VerticalScrollList`
*   **Item 规格**:
    *   **高度**: `35px`
    *   **样式**: 悬停高亮 (`Hover: #404040`)
*   **数据结构 (`DialogueOption`)**:
    *   `text`: 显示文本 (支持颜色代码)
    *   `actionId`: 触发动作 (例如 "hire", "leave", "trade")
    *   `payload`: 附加数据

---

## 4. 数据结构参考 (JSON Prototype)

### Status Entry (S2C Packet Payload)
```json
[
  { "key": "hp", "label": "HP", "value": "20/20", "icon": "textures/gui/heart.png" },
  { "key": "hunger", "label": "Hunger", "value": "80%", "icon": "textures/gui/food.png" },
  { "key": "job", "label": "Job", "value": "Farmer", "icon": "textures/gui/hoe.png" }
]
```

### Chat Option (Structure)
```json
{
  "text": "I want to hire you. (100 Gold)",
  "action": "npc_hire",
  "enabled": true
}
```
