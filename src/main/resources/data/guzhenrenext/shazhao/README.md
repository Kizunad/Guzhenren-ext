# 杀招数据（推演解锁）命名规范

本目录的 JSON 用于定义“杀招”配置，并由 `ShazhaoDataLoader` 加载进 `ShazhaoDataManager`。

## 文件结构

- 路径：`src/main/resources/data/guzhenrenext/shazhao/*.json`
- 基本字段：
  - `shazhaoID`: 杀招唯一 ID（`namespace:path`）
  - `title`: 杀招名称
  - `desc`: 杀招描述
  - `info`: 杀招信息（用于 UI 展示）
  - `cost_total_niantou`: 推演失败时消耗的念头
  - `required_items`: 所需蛊虫列表（`itemID` 字符串数组）
  - `metadata`: 可选扩展参数（键值均为字符串）

## 命名要求（强制）

1) `shazhaoID` 必须全小写，使用 `snake_case`，且 path 必须以 `shazhao_passive_` 或 `shazhao_active_` 开头。  
2) `required_items` 内每个 itemID 必须全小写、`snake_case`，且对应物品必须存在。  
3) `required_items` 必须至少包含 1 个物品，且不得重复。  
4) `metadata` 的 key 必须全小写 `snake_case`，value 使用字符串（即使是数字）。  

## 加载期强制校验

`ShazhaoDataLoader` 在资源加载时会调用 `ShazhaoDataValidator` 做强制校验；不符合规范的文件会输出错误日志并跳过加载。
