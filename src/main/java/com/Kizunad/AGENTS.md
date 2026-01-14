# AGENTS.md - com.Kizunad（Java 根包）

## 概述
本目录承载所有业务 Java 代码。仓库是 **多 Mod** 结构：`guzhenrenext` / `customnpcs` / `tinyui` 三个 `@Mod` 主类共存。

## 去哪找
- `guzhenrenext` 入口：`com/Kizunad/guzhenrenext/GuzhenrenExt.java`
- `customnpcs` 入口：`com/Kizunad/customNPCs/CustomNPCsMod.java`
- `tinyui` 入口：`com/Kizunad/tinyUI/TinyUIMod.java`

## 约定
- **端侧隔离**：client-only 初始化必须下沉到 `*ClientBootstrap` / `*Client*` 类（避免 Dedicated Server 类加载崩溃）。
- **Checkstyle（核心规则）**：行长 `120`；禁止 `import *`；启用 `MagicNumber`（默认仅忽略 `-1,0,1,2`）。
- **仓库强约束**：禁止 `@SuppressWarnings`；注释中文且详细。

## 常用命令
- `./gradlew checkstyleMain`
- `./gradlew runGameTestServer`（GameTest 详见 `docs/HowToTest.md`）
