# Resources（src/main/resources）

## 概述
本目录包含 NeoForge 模组元数据与 datapack/assets 资源。仓库同时声明了三个 modId：`guzhenrenext` / `customnpcs` / `tinyui`。

## 模组元数据
- `META-INF/neoforge.mods.toml`
  - 含 3 个 `[[mods]]` 块。
  - 使用 `${...}` 占位符，由 Gradle `ProcessResources` 在构建时 `expand`。

## 数据生成（Datagen）
- `./gradlew runData` 会把产物写入 `src/generated/resources/`。
- 工程已将 `src/generated/resources` 加入 resources sourceSet；不要手工把生成文件拷回 `src/main/resources`。

## 主要数据目录（不要枚举到文件级）
- `data/guzhenrenext/niantou/`：念头数据（量大）
- `data/guzhenrenext/shazhao/`：杀招数据
- `data/guzhenrenext/structure/`：GameTest 结构 NBT
- `data/customnpcs/tasks/`：任务系统数据（含 compat 子目录）
- `assets/customnpcs/`：CustomNPCs 纹理等资源

## 约定
- 不在 `src/generated/resources` 里直接改动（改源数据/生成逻辑后再 `runData`）。
- 资源改动涉及测试结构时，配合 `./gradlew runGameTestServer` 验证。
