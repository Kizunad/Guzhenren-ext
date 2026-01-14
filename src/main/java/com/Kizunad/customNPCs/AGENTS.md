# CustomNPCs（src/main/java/com/Kizunad/customNPCs）

## 概述
`customnpcs` 是独立 Mod（modId: `customnpcs`），实现自定义 NPC 实体与 AI 系统，并提供菜单/网络/任务系统。

## 入口与初始化
- 入口类：`com/Kizunad/customNPCs/CustomNPCsMod.java`
- 在构造函数中统一注册：Attachment / Entities / Menus / Networking。
- **端侧隔离（关键）**：
  - client-only 注册通过反射调用 `com.Kizunad.customNPCs.client.CustomNPCsClientBootstrap.registerClient(IEventBus)`。
  - 主类/通用逻辑禁止直接引用 `client/*` 下类。
- **测试钩子（关键）**：
  - 通过反射尝试调用 `com.Kizunad.customNPCs_test.TestRegistry.register(IEventBus)`；测试包不存在则跳过。

## 去哪找
- NPC 实体：`entity/CustomNpcEntity.java`（高复杂度热点）
- AI：`ai/`（sensors / decision(goals) / actions / planner / llm 等）
- 网络：`network/`（Payload 定义与注册入口通常在 `network/ModNetworking.java`）
- UI：`menu/` + `client/ui/`（Screen 侧需严格 client-only）
- 任务系统：`tasks/`（Objective/Reward/同步服务）
- Debug：`commands/MindDebugCommand.java`（高复杂度热点）

## 测试（GameTest 优先）
- GameTest 主位置：`src/main/java/com/Kizunad/customNPCs_test/**`
- 指南：`docs/HowToTest.md`

## 约定
- 注释中文且详细；禁止 `@SuppressWarnings`。
- 遵守 checkstyle（行长 120、禁止通配符导入、MagicNumber 等）；测试目录对 `MagicNumber` 有单独豁免（见 `config/checkstyle/checkstyle.xml`）。
