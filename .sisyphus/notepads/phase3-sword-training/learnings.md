## 2026-02-12T12:40:15.005Z Session Init
- 初始化记录文件。

## 2026-02-13 Task1 修复记录
- MC 1.21.1 下 ItemStack 训练数据应走 `DataComponents.CUSTOM_DATA`，不能使用旧版 `getOrCreateTag/setTag`。
- 飞剑 Attributes 可继续复用既有键名 `"Attributes"`，通过 `ItemStackCustomDataHelper` 统一读写可与现有风格保持一致。
- 练剑燃料测试应与设计一致，使用 `guzhenren:primeval_stones` 对应物品（当前映射 `guzhenren:gucaiyuanshi`）。

## 2026-02-13 Task1 提交策略
- 按“每个 task 提交一次”执行，Task1 提交仅包含训练后端、注册/tick 挂接、燃料 tag、计划勾选与 notepad 记录，避免混入 Task2/Task3。

## 2026-02-13 Task2 Menu & UI Learnings
- **ContainerData pattern**: Successfully used ContainerData to sync primitive data (fuel, exp) from server attachment to client screen. This is cleaner than custom packets for simple integers.
- **TinyUISlot integration**: Created `TinyUISlotItemHandler` to bridge `TinyUISlot` (dynamic positioning) and `SlotItemHandler` (item validation/constraints). This allows us to use NeoForge's ItemHandler capabilities while keeping TinyUI's flexible layout.
- **Slot validation**: Enforced slot validation (Sword vs Fuel) in `FlyingSwordTrainingMenu` by overriding `mayPlace` in anonymous classes of `TinyUISlotItemHandler`. This ensures server-side security.
- **Checkstyle Compliance**: Extracted UI layout constants (sizes, paddings, gaps) into static final fields in `FlyingSwordTrainingScreen` to satisfy strict MagicNumber checks.
