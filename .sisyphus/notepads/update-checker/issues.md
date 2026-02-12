
## 2026-02-13 Task1 验证问题记录
- 初版 Python 断言误把注释掉的 `customnpcs` 示例块当成有效 `[[mods]]` 块，导致“customnpcs block must not contain updateJSONURL”假阳性失败。
- 处理方式：脚本先剔除注释行（`^\s*#`）后再进行块解析与断言，验证通过。
