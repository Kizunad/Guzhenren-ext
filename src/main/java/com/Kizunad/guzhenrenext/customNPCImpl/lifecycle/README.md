# NPC 生命周期与事件处理器 (NPC Lifecycle & Event Handlers)

`lifecycle` 文件夹用于集中管理与 `CustomNpcEntity` 实体生命周期相关的逻辑和事件处理器。这些逻辑会在 NPC 生成、每游戏 tick 或每秒等固定时间点被调用。

**主要用途:**

*   **初始化逻辑:** 当 NPC 实体首次生成或加载时，执行必要的初始化操作（例如：设置默认属性、加载外部模组数据）。
*   **周期性更新:** 定义在每个游戏 tick 或特定时间间隔（例如每秒）执行的逻辑，用于处理 NPC 的持续行为、状态更新或与外部系统的交互。

**包含的组件:**

*   `NpcSpawnInitializer`: 负责在 `CustomNpcEntity` 生成时，初始化与蛊真人模组相关的特定变量和属性。
*   `NpcSecondTicker`: 提供一个秒级事件分发机制。通过注册到 `NpcTickRegistry`，它会筛选出每 20 ticks (即每秒) 执行一次的逻辑，并将其分发给所有注册的处理器。这适用于不需要每帧更新但需要定期检查或执行的任务。

**如何添加新的逻辑:**

1.  **对于生成时初始化:** 创建一个类似于 `NpcSpawnInitializer` 的类，并将其处理器注册到 `com.Kizunad.customNPCs.registry.NpcSpawnRegistry`。
2.  **对于秒级周期性逻辑:** 创建一个类或方法，并使用 `NpcSecondTicker.addHandler()` 注册您的逻辑。
3.  **对于每 tick 逻辑 (如果需要):** 直接在 `NpcTickRegistry` 中注册处理器（但通常建议使用秒级或更低频率的处理器来优化性能）。

通过这种方式，我们可以保持 NPC 核心逻辑的清晰分离，并便于扩展与维护。
