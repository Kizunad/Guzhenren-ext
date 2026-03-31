# 势力整合与验证说明

## 1. 文档目的与范围

本文只记录已经落盘的势力整合链路，范围覆盖 Task 15, Task 16, Task 17 的现有实现与使用方式。重点是把“升仙整合”“势力信息界面”“势力关系图”三条路径讲清楚，并且把当前验证状态如实写出，方便后续验收者直接对照源码与测试证据。

本文不讨论新的功能设计，也不补充尚未实现的交互。所有描述都对应仓库内的真实类名、方法名和网络包名。

## 2. Task 15：升仙整合说明

Task 15 的核心实现位于 `src/main/java/com/Kizunad/guzhenrenext/faction/integration/FactionAscensionModifier.java`。这个类是一个纯工具类，负责把势力状态折算成升仙相关的外部修正值。它对外提供的主入口是：

- `resolveReadinessModifier(ServerLevel level, UUID playerId)`
- `resolveTribulationModifier(ServerLevel level, UUID ownerId)`
- `evaluateSnapshot(FactionInfluenceSnapshot snapshot)`

其中 `resolveTribulationModifier(...)` 会返回 `TribulationManager.ExternalTribulationModifier`，这是和既有灾劫系统对接的公开 API。内部逻辑会先定位玩家所属势力，再基于势力类型、势力资源、势力战力，以及敌对势力数量和敌对强度，算出两个方向的修正值：灾劫强度倍率和入侵生成倍率。

源码里可以直接看到这些规则：

- 宗门会应用 `SECT_PROTECTION_INTENSITY_MULTIPLIER`
- 家族会应用 `CLAN_PROTECTION_INTENSITY_MULTIPLIER`
- 敌对关系阈值是 `RELATION_HOSTILE_THRESHOLD = -50`
- `FactionInfluenceSnapshot.unaffiliated()` 会返回无势力状态

这意味着 Task 15 已经不是概念层，而是一个可直接从势力数据读取并输出灾劫修正的桥接层。

### API / 使用示例

```java
FactionAscensionModifier modifier = new FactionAscensionModifier();
TribulationManager.ExternalTribulationModifier tribulationModifier =
    modifier.resolveTribulationModifier(level, ownerId);
```

如果只想在已有快照上做纯计算，也可以直接用：

```java
FactionAscensionModifier.ModifierBundle bundle =
    FactionAscensionModifier.evaluateSnapshot(snapshot);
FactionAscensionModifier.ReadinessModifier readiness = bundle.readinessModifier();
FactionAscensionModifier.TribulationModifier tribulation = bundle.tribulationModifier();
```

## 3. Task 16：FactionInfoScreen 打开链路与同步链路

Task 16 的客户端界面入口是 `src/main/java/com/Kizunad/guzhenrenext/faction/client/FactionInfoScreen.java`。它是一个 `TinyUIScreen`，负责展示势力名称、类型、成员数、战力、资源和玩家与该势力的关系值。

打开链路是现成的按键触发链路，而不是直接在屏幕里硬编码打开：

1. `src/main/java/com/Kizunad/guzhenrenext/client/GuKeyBindings.java` 定义 `GuKeyBindings.OPEN_FACTION_INFO`
2. `src/main/java/com/Kizunad/guzhenrenext/client/GuClientEvents.java` 在客户端 tick 中监听 `GuKeyBindings.OPEN_FACTION_INFO.consumeClick()`
3. 按下后通过 `PacketDistributor.sendToServer(new ServerboundFactionInfoRequestPayload())` 发送请求
4. `src/main/java/com/Kizunad/guzhenrenext/network/ServerboundFactionInfoRequestPayload.java` 在服务端构造 `ClientboundFactionInfoSyncPayload`
5. `src/main/java/com/Kizunad/guzhenrenext/network/ClientboundFactionInfoSyncPayload.java` 通过 `FactionInfoClientHandler.applySync(...)` 写入客户端状态
6. `FactionInfoScreen.init()` 读取 `FactionInfoClientState.currentSnapshot()` 并渲染

`FactionInfoScreen` 的界面行为也很明确，ESC 或再次按下绑定键都能关闭界面。它还提供一个“关系图”按钮，进入 Task 17 的界面。

### API / 使用示例

```java
while (GuKeyBindings.OPEN_FACTION_INFO.consumeClick()) {
    if (minecraft.screen == null) {
        FactionInfoClientState.markSyncPending();
        PacketDistributor.sendToServer(new ServerboundFactionInfoRequestPayload());
    }
}
```

服务端请求包的处理方式也是固定的：

```java
ServerboundFactionInfoRequestPayload.handle(payload, context);
```

如果服务端找不到玩家所属势力，它会回退到“按创建时间和名称排序后的第一个势力”，再继续同步；这点在 `resolveDisplayFaction(...)` 里已经写死，属于当前实现的一部分。

## 4. Task 17：FactionRelationGraph 打开链路、同步链路、交互行为

Task 17 的主类是 `src/main/java/com/Kizunad/guzhenrenext/faction/client/FactionRelationGraph.java`。这个界面同样是 `TinyUIScreen`，但它展示的是全服势力关系图，而不是单个势力详情。

打开链路从 `FactionInfoScreen` 里的“关系图”按钮开始：

1. `FactionInfoScreen` 中的 `relationGraphButton.setOnClick(...)` 直接创建 `new FactionRelationGraph()`
2. `FactionRelationGraph.init()` 会在第一次初始化时标记客户端状态为 pending
3. 同时它会发出 `new ServerboundFactionRelationGraphRequestPayload()`
4. 服务端在 `ServerboundFactionRelationGraphRequestPayload.handle(...)` 中构造 `ClientboundFactionRelationGraphSyncPayload`
5. 客户端在 `ClientboundFactionRelationGraphSyncPayload.handle(...)` 中写入 `FactionRelationGraphClientState`
6. `FactionRelationGraph.renderScaledContent(...)` 每帧从 `FactionRelationGraphClientState.currentSnapshot()` 取最新数据并渲染

交互行为也已经实现并可从源码直接读到：

- 左键拖拽平移
- 滚轮缩放
- ESC 关闭界面
- 悬停节点或连线显示详情
- 同步未到位时显示等待文案

同步数据结构也很清晰，节点使用 `NodeSnapshot`，连线使用 `EdgeSnapshot`。服务端构造快照时按势力创建时间和名称排序，再从 `FactionWorldData` 读取关系矩阵，生成节点和连线。

### API / 使用示例

```java
FactionRelationGraph graph = new FactionRelationGraph();
minecraft.setScreen(graph);
```

请求和同步链路示意：

```java
PacketDistributor.sendToServer(new ServerboundFactionRelationGraphRequestPayload());
// 服务端返回 ClientboundFactionRelationGraphSyncPayload
```

如果只看客户端状态入口，代码也已经固定：

```java
FactionRelationGraphClientState.markSyncPending();
FactionRelationGraphSnapshot snapshot = FactionRelationGraphClientState.currentSnapshot();
```

## 5. 当前验证状态与已知阻塞

当前对势力 UI 路径的定向验证是绿色的，证据来自下面这三项已经通过的命令：

- `./gradlew --no-daemon factionUiTest`
- `./gradlew --no-daemon compileJava`
- `./gradlew --no-daemon checkstyleMain --rerun-tasks`

此外，`./gradlew --no-daemon test` 已经是绿色，`./gradlew --no-daemon runGameTestServer` 也已经完成全量顺序执行并保持绿色，这说明当前仓库级自动化证据已经补齐。

不过，Task 16 和 Task 17 的手动 UI QA 仍然被共享客户端运行时阻塞，当前环境里 `runClient` 还会在可用的人工界面验证前失败，日志里可见 `glfwInit failed`，同时本机也没有可用的 `Xvfb` 或 `xvfb-run` 来补出真实的客户端走查证据，所以不能把“人工点开界面并实机走完”写成已完成。

仓库级别的验证基线也必须如实保留。这里虽然已经有 `test` 和全量顺序 `runGameTestServer` 的绿色结果，但这仍只代表自动化验证通过，还不能把 Task 16、Task 17 或 Task 20 写成已经完成人工验收。

和 Task 18, Task 19 相关、但这里只能简短提到的已落盘事实有两点：

- `GoapMindCore.TICKS_PER_SECOND = 20L`
- `build.gradle` 的 `sourceSets` 显式把 `com/Kizunad/customNPCs/**` 从 `main` 和 `test` 编译路径里排除了

这些信息和势力 UI 不直接相连，但它们说明了仓库里相关集成点的边界已经被明确切开。

## 6. 结语

本文档只记录现有实现，不新增设计，不假装补齐未完成的人工验证。下一步如果要接受 Task 20，应该直接拿源码、契约测试和当前验证状态逐项核对，而不是重新推测链路。
