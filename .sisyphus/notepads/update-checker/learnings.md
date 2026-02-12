
## 2026-02-13 Task1: neoforge.mods.toml 注入 updateJSONURL
- 在多 `[[mods]]` TOML 中，定位主模组应以 `modId="${mod_id}"`（构建期展开为 `guzhenrenext`）为准，避免误改其他模块。
- `updateJSONURL` 必须放在目标 `[[mods]]` 块内部，不能放在全局键区。
- 断言脚本建议先过滤注释行后再做块级校验，避免注释中的示例块影响结果。
