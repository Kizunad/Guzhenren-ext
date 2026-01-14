# Kongqiao（src/main/java/com/Kizunad/guzhenrenext/kongqiao）

## 概述
`kongqiao` 是空窍子系统：Attachment 持久化数据 + 多类效果（蛊/杀招/道痕等）+ 客户端 UI/同步网络。

## 扩展入口（新增功能优先看这些）
- Attachment：`attachment/KongqiaoAttachments.java` / `attachment/KongqiaoAttachmentEvents.java`
- 菜单/Screen：`menu/KongqiaoMenus.java` + `client/ui/*`
- 飞剑：`flyingsword/FlyingSwordEntities.java`（实体注册）+ `flyingsword/client/*`（渲染/Overlay）
- 蛊效果注册：`logic/GuEffectRegistry.java`
- 杀招注册：`logic/ShazhaoEffectRegistry.java`

## 结构与约定（代码层）
- `logic/impl/active/daos/**`：主动道法效果
- `logic/impl/passive/daos/**`：被动道法效果
  - 常见子层级：`common/`、`tierOne..tierFive/`、`shazhao/`（具体以目录为准）
- `service/`：运行时服务（tick/同步/规则校验等）
- `domain/`、`shazhao/`、`niantou/`：对应数据结构、加载/校验、网络同步

## 事件与网络
- 大量使用 `@EventBusSubscriber`：
  - `bus = MOD`：注册/属性等（例如 entity attributes）
  - `bus = GAME`：运行时事件（tick、交互、同步触发等）
  - client-only 必须显式 `Dist.CLIENT`

## 对应资源（数据侧）
- `src/main/resources/data/guzhenrenext/niantou/`：念头数据（量大）
- `src/main/resources/data/guzhenrenext/shazhao/`：杀招数据
- `src/main/resources/data/guzhenrenext/structure/`：GameTest 结构 NBT

## 约定
- 注释中文且详细；禁止 `@SuppressWarnings`。
- 涉及数据结构/加载器变更时，优先用 `runData` 生成/校验，再跑 `runGameTestServer` 覆盖关键路径。
