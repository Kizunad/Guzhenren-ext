# GuzhenrenExt（src/main/java/com/Kizunad/guzhenrenext）

## 概述
`guzhenrenext` 是主 Mod（modId: `guzhenrenext`），负责空窍系统、效果/事件、网络包、命令等。

## 入口与初始化
- 入口类：`com/Kizunad/guzhenrenext/GuzhenrenExt.java`
- 常见初始化职责（在构造/事件注册中出现）：
  - 注册菜单：`kongqiao/menu/KongqiaoMenus.register(modEventBus)`
  - 注册 Attachment：`kongqiao/attachment/KongqiaoAttachments.register(modEventBus)`
  - 注册实体：`kongqiao/flyingsword/FlyingSwordEntities.register(modEventBus)`
  - 注册网络：`network/GuzhenrenExtNetworking.register(modEventBus)`
  - 注册效果：`registry/ModMobEffects.register(modEventBus)`
- 命令：通过 `RegisterCommandsEvent` 注册（例如 debug 命令）。

## 端侧隔离
- 客户端入口/事件应放在 `client/` 下（例如 `GuzhenrenExtClient`、按键/Overlay/Screen），避免 server 侧类加载崩溃。

## 去哪找
- 空窍系统：`kongqiao/`（规模最大，另有子目录 AGENTS）
- 与 guzhenren 主模组桥接：`guzhenrenBridge/`（其中 `generated/**` 已被 checkstyle 排除）
- 网络：`network/`（注册入口在 `GuzhenrenExtNetworking`）
- 事件订阅：`event/`（大量 `@EventBusSubscriber`，注意 GAME vs MOD 总线、`Dist.CLIENT`）

## 约定
- 注释中文且详细；禁止 `@SuppressWarnings`。
- 改动前后建议跑：`./gradlew checkstyleMain`；涉及空窍/AI 行为建议补充/更新 GameTest。
