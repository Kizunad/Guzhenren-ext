# Guzhenren-ext: Agent 约定（根目录）

## 项目概览
- 类型：NeoForge Minecraft Mod（MC `1.21.1`，NeoForge `21.1.11`，JDK `21`）
- 形态：单仓库多 Mod（`guzhenrenext` / `customnpcs` / `tinyui`）
- 构建：Gradle（`net.neoforged.moddev` `2.0.119`），启用 `checkstyle`

## 目录结构（只写非显然部分）
- `src/main/java/com/Kizunad/`：核心代码（多 Mod 并存）
- `src/main/resources/`：`neoforge.mods.toml` + datapack/assets 资源
- `src/generated/resources/`：数据生成产物（由 `runData` 写入；作为 resources 目录被包含）
- `docs/`：设计/测试/计划文档（AI/测试框架说明在这里）
- `local_libs/`：本地依赖（如 `guzhenren`）
- `LibSourceCodes/`：参考源码（NeoForge / decompile / 其他项目；一般不应修改）

## 必须遵循（仓库约束）
- 使用 Serena MCP（Project: `Guzhenren-ext`）。
- 多 Agent 协作前先锁定项目：`python3 agent_workflow.py assign --agent <NAME> --project <SCOPE> --details "<你要做什么>"`；完成后必须 `unlock`。
- 代码规范：提交/交付前跑 `./gradlew checkstyleMain`；禁止用 `@SuppressWarnings` 绕过。
- 注释：中文，详细。

## 开发流程（仓库推荐）
1. 了解上下文（读代码/任务）
2. 需要时反馈理解
3. 计划任务
4. 具体编写（此处 assign project）
5. 规范测试/编译测试
6.（需要时）补齐/维护 GameTest 并验证
7.（用户要求时）提交 + unlock

## 去哪找（高频入口）
- `guzhenrenext`：`src/main/java/com/Kizunad/guzhenrenext/GuzhenrenExt.java`
- `customnpcs`：`src/main/java/com/Kizunad/customNPCs/CustomNPCsMod.java`
- `tinyui`：`src/main/java/com/Kizunad/tinyUI/TinyUIMod.java`
- GameTest 指南：`docs/HowToTest.md`

## 常用命令
- 构建：`./gradlew build`
- 代码规范：`./gradlew checkstyleMain`
- 运行：`./gradlew runClient` / `./gradlew runServer`
- GameTest：`./gradlew runGameTestServer`
- 数据生成：`./gradlew runData`
- tinyUI：`./gradlew tinyUiJar` / `./gradlew tinyUiTest`
- CustomNPCs：`./gradlew customNpcsJar`

## 本仓库反模式（明确禁止/强烈不建议）
- 通过 `@SuppressWarnings` 掩盖 Checkstyle/代码问题。
- 修改生成目录（如 `src/generated/resources`）里的文件来“修复”数据；应修源数据/生成逻辑后再 `runData`。
- 在通用逻辑（server 侧可达路径）中直接引用 client-only 类（Screen/Minecraft/渲染相关）。
