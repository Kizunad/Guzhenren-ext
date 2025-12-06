# NPC 骑乘系统规划 (Ride Entity System)

## 1. 目标
让 `CustomNpcEntity` 能够识别、寻找并骑乘可用的载具（如马、矿车、船），并能在骑乘状态下自主控制载具移动，实现“骑士”或“驾驶员”行为。

## 2. 核心组件设计

### 2.1 状态键 (WorldStateKeys)
扩展 `WorldStateKeys` 以支持骑乘相关的状态判断：
*   `IS_RIDING` (Boolean): 当前是否处于骑乘状态。
*   `HAS_MOUNT_NEARBY` (Boolean): 附近是否有可用的空闲坐骑。
*   `MOUNT_UUID` (UUID): 目标坐骑的 UUID。
*   `MOUNT_TYPE` (String): 坐骑类型（如 "horse", "boat"），用于区分控制策略。

### 2.2 感知层 (Sensors)
需要让 NPC “看到”坐骑。
*   **方案**: 扩展现有的 `VisionSensor` 或新增 `MountSensor`。
*   **逻辑**:
    *   扫描范围内的 `AbstractHorse` (马/驴/骡), `Boat`, `AbstractMinecart`。
    *   **过滤条件**:
        *   `isAlive()`
        *   `!isVehicle()` (没有被骑)
        *   `isTame()` (如果是马，需驯服；或者允许 NPC 骑野马并驯服？初期先只骑驯服的或无需驯服的)
    *   **记忆**: 将最近的可用坐骑 UUID 存入 `MOUNT_UUID`，并设置 `HAS_MOUNT_NEARBY`。

### 2.3 动作层 (Actions)

#### `MountAction` (上马动作)
*   **类型**: `AbstractStandardAction`
*   **参数**: `targetUuid` (坐骑)
*   **逻辑**:
    1.  **导航**: 移动到目标坐骑附近 (距离 < 2.0)。
    2.  **交互**: 调用 `npc.startRiding(mount)`.
    3.  **校验**: 检查 `npc.getVehicle()` 是否为目标，确认上马成功。
*   **状态更新**: 成功后写入 `IS_RIDING = true`。

#### `DismountAction` (下马动作)
*   **逻辑**: 调用 `npc.stopRiding()`。
*   **触发**: 到达目的地、战斗需要下马、或坐骑死亡时。

### 2.4 决策层 (Goals)

#### `FindMountGoal` (寻找坐骑)
*   **类型**: `PlanBasedGoal`
*   **触发条件**:
    *   当前未骑乘 (`!IS_RIDING`).
    *   附近有坐骑 (`HAS_MOUNT_NEARBY`).
    *   **动机**:
        *   长途旅行需求 (例如当前 `MoveTo` 目标距离 > 30 格).
        *   或者拥有 "Rider" 特质/职业.
*   **规划**: `[MoveTo(mount), MountAction(mount)]`.

#### `RideEntityGoal` (可选)
*   如果 `FindMountGoal` 只是为了“上马”，那么“骑马移动”是否需要单独的 Goal？
*   **优化**: 现有的 `MoveToAction` 应该适配骑乘状态。如果 NPC 正在骑乘，`MoveToAction` 应该驱动坐骑而不是 NPC 双脚。

### 2.5 驾驭与导航 (Navigation & Control) - **核心难点**

Minecraft 原版导航 (`PathNavigation`) 是为生物自身设计的。当 NPC 骑乘时，控制权逻辑如下：

#### A. 控制权获取
NPC 必须是载具的 `ControllingPassenger`。
*   对于马：通常第一位乘客是控制者。
*   对于船：同上。

#### B. 导航适配 (`OnEntityNavigation`)
需要确保 NPC 在骑乘时，其导航系统能驱动载具。
1.  **检测**: 在 `CustomNpcEntity.tick()` 或 `travel()` 中检测 `isPassenger()`。
2.  **驱动**:
    *   **转向**: 将 NPC 的 `YRot` (Yaw) 传递给载具。`vehicle.setYRot(npc.getYRot())`。
    *   **移动**:
        *   如果使用原版导航：需要确保 `navigation` 组件意识到它现在是在控制载具的包围盒和速度。
        *   或者，**手动驱动**：
            *   计算路径点方向。
            *   调用 `vehicle.setDeltaMovement(...)` 或修改载具的输入属性（如 `xxa`, `zza` 对于 LivingEntity 载具）。
            *   对于 `AbstractHorse`，可能需要模拟玩家输入 `setIsJumping`, `setInput(forward, strafe, jump, sneak)`.

#### C. 修改 `MoveToAction`
`MoveToAction` 需要识别骑乘状态：
*   如果 `isRiding()`:
    *   不要调用 `mob.getNavigation().moveTo(...)` (除非导航系统已魔改为支持骑乘)。
    *   或者，调用特定的 `driveTo(target)` 逻辑。
    *   **推荐方案**: 让 `CustomNpcEntity` 重写 `travel()` 方法，当骑乘时，将自身的导航意图（由 AI 计算出的 `moveForward`/`moveStrafe`）应用到载具上。这样 `MoveToAction` 只需要像往常一样设置导航路径即可。

## 3. 实施路线图

1.  **Phase 1: 基础骑乘能力**
    *   实现 `MountAction` 和 `DismountAction`。
    *   通过调试命令 `/mind action mount <uuid>` 测试 NPC 是否能上马。

2.  **Phase 2: 驾驭逻辑 (The Driver)**
    *   修改 `CustomNpcEntity`。
    *   重写 `tick()` 或 `serverAiStep()`。
    *   实现：当 NPC 骑乘且有导航路径时，计算并设置载具的运动/朝向。
    *   **验证**: 让 NPC 骑在马上，使用 `/mind action move_to <coord>`，观察马是否移动到目标点。

3.  **Phase 3: 自主决策**
    *   在 `VisionSensor` 中添加坐骑识别。
    *   实现 `FindMountGoal`。
    *   测试：在远处放置马匹，观察其是否会优先寻找马匹。

4.  **Phase 4: 细节打磨**
    *   战斗中的骑乘（骑射 正常攻击等）。
    *   下马逻辑（到达目的地后自动下马？）。
    *   载具的保护（不攻击自己的座驾）。

## 4. 示例代码片段 (Entity Control)

```java
// 在 CustomNpcEntity 中
@Override
public void serverAiStep() {
    super.serverAiStep();
    if (this.isPassenger() && this.getVehicle() instanceof LivingEntity mount) {
        // 简单的驾驭逻辑：让坐骑朝向 NPC 的视线方向，并按 NPC 的导航意图前进
        if (this.getNavigation().isInProgress()) {
            // 获取导航的下一个路径点
            // 计算转向和速度
            // mount.setYRot(...)
            // mount.setSpeed(...)
        }
    }
}
```
