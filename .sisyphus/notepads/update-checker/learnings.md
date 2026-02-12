
## 2026-02-13 Task1: neoforge.mods.toml 注入 updateJSONURL
- 在多 `[[mods]]` TOML 中，定位主模组应以 `modId="${mod_id}"`（构建期展开为 `guzhenrenext`）为准，避免误改其他模块。
- `updateJSONURL` 必须放在目标 `[[mods]]` 块内部，不能放在全局键区。
- 断言脚本建议先过滤注释行后再做块级校验，避免注释中的示例块影响结果。

## 2026-02-13 Task2: 创建 update.json 模板
- 在项目根目录新增 `update.json`，采用 NeoForge update checker 基础结构：顶层 `homepage` + `promos`。
- 版本键采用 `1.21.1-latest` 与 `1.21.1-recommended`，初始值统一为 `1.0.0`，便于后续仅改版本值。
- 使用 Python `json.load` + `assert` 做结构验收，可快速保证语法与关键键存在。
