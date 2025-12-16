# 念头数据（Item → 技能/用途）命名规范

本目录的 JSON 用于定义“物品（蛊虫）对应的用途（Usage）列表”，并由 `NianTouDataLoader` 加载进 `NianTouDataManager`。

## 文件结构

- 路径：`src/main/resources/data/guzhenrenext/niantou/*.json`
- 基本字段：
  - `itemID`: 物品 ID（`namespace:path`）
  - `usages`: 用途列表，每项包含 `usageID/usageTitle/usageDesc/usageInfo/cost_duration/cost_total_niantou/metadata`

## usageID 的“类型规则”（name_type）

当前项目使用 `usageID` 的路径字符串承载“用途类型”：

- `name_type = "active"`：**主动用途**（用于轮盘/按钮触发），要求 `usageID` 路径中包含 `_active_`
  - 示例：`guzhenren:langhungu_active_greedy_devour`
- `name_type = ""`：**被动用途**，要求 `usageID` 路径中包含 `_passive_`
  - 示例：`guzhenren:guiyangu_passive_shield`

代码侧工具函数：

- `NianTouUsageId.extractNameType(usageId)`：返回 `""` 或 `"active"`
- `NianTouUsageId.isActive(usageId)`：判定是否为主动用途
- `NianTouUsageId.isPassive(usageId)`：判定是否为被动用途

## 命名要求（强制）

1) `usageID` 必须全小写，使用 `snake_case`，并保持稳定（它是 `GuEffectRegistry` 的唯一键）。  
2) 主动用途必须包含 `_active_` 且不得包含 `_passive_`；被动用途必须包含 `_passive_` 且不得包含 `_active_`。  
3) 同一个 `itemID` 下的 `usageID` 必须唯一。  
4) `metadata` 的 key 必须与代码读取一致（建议统一 `snake_case`），value 统一使用字符串（即使是数字），便于占位符替换。

## 变更注意事项

- 修改 `usageID` 属于破坏性变更：需要同步修改对应 `IGuEffect#getUsageId()`（以及任何引用该 ID 的逻辑/数据）。

## 加载期强制校验

`NianTouDataLoader` 在资源加载时会调用 `NianTouDataValidator` 做强制校验；不符合规范的文件会输出错误日志并跳过加载：

- `itemID/usageID` 必须是合法的 `ResourceLocation` 且全小写
- `itemID/usageID` 的 path 必须为 `snake_case`（仅允许 `[a-z0-9_]`）
- 同一 `itemID` 内 `usageID` 必须唯一
- `metadata` 的 key 必须全小写 `snake_case`
