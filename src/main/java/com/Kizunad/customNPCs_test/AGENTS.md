# CustomNPCs GameTest（src/main/java/com/Kizunad/customNPCs_test）

## 概述
本目录包含 `customnpcs` AI 的 NeoForge GameTest 套件（注意：**不是** `src/test/java`），用于 headless/CI 友好的行为验证。

## 权威指南
- `docs/HowToTest.md`（必读）：如何运行/写测试、常见陷阱、工具类入口。

## 去哪找（工具类）
- `utils/NpcTestHelper`：等待条件、断言、驱动 mind tick、隔离 tag 等
- `utils/TestEntityFactory`：创建带 `NpcMind` 的测试实体
- `utils/TestBatches`：batch 常量

## 关键陷阱：坐标转换（Coordinate Trap）
- `GameTestHelper` 的坐标通常是“结构内相对坐标”。
- AI/导航使用世界绝对坐标。
- 传给 Goal/Action 的坐标，必须用 `helper.absolutePos(relPos)` 转换。

## 运行方式
- 全量：`./gradlew runGameTestServer`
- 日志：`run/logs/latest.log`（超时/卡住优先看这里）

## Checkstyle 说明
- `config/checkstyle/checkstyle.xml` 对 `customNPCs_test` 路径的 `MagicNumber` 做了 suppress（便于测试书写）。
- 其他规则仍适用：行长 120、禁止通配符导入等。

## 约定
- 注释中文且详细；禁止 `@SuppressWarnings`。
- 测试必须可并行/可隔离：优先复用 `NpcTestHelper` 的 tag/隔离能力。
