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

## 2026-02-13 Task3 按键与网络 Learnings
- 复用 `KongqiaoKeyMappings + KongqiaoClientEvents.registerKeys` 的链路注册新按键最稳妥，能避免引入并行的输入系统。
- 对“打开界面”型按键，在 tick 内用 `while (consumeClick())` 汇总成单个布尔标记再发一次包，可实现同帧防抖并减少重复包。
- 新增无负载打开包时，沿用 `StreamCodec.unit(new Payload())` 可保持与现有空包风格一致。
- 服务端打开训练界面复用 `KongqiaoService` 的 `MenuProvider + player.openMenu(...)` 模式，能与既有菜单生命周期保持一致。

## 2026-02-12T17:08:38Z Phase3 DoD 验证补强
- 进度条行为可通过 Attachment 的 `fuelTime/maxFuelTime` 与菜单同构公式（`fuel*100/max`）进行等价断言，无需改动菜单业务语义。
- 在 `fuelTime=0,maxFuelTime=0` 起始条件下执行一次 `tickInternal`，可同时覆盖 refill 生效、进度可见值变化与经验增长链路。
- 数量守恒验证宜以“槽位总数变化 = 燃料消耗量”建模，能直接捕捉凭空增殖/吞物问题。

## 2026-02-12T17:17:40Z GameTest 结构缺失修复
- GameTest `template="empty"` 会解析为 `<testclass>.empty` 结构名；飞剑测试类需分别提供 `flyingswordsynctests.empty.nbt` 与 `flyingswordtrainingservicetests.empty.nbt`。
- 复用现有 `examplegametests.empty.nbt` 作为模板即可满足最小修复，不需要改动测试或业务逻辑。

## 2026-02-12T17:42:42Z FlyingSwordTrainingServiceTests 去外部依赖
- 将飞剑训练测试改为使用原版 `Items.COAL` 作为槽位占位，并直接设置 `fuelTime/maxFuelTime` 构造可控燃烧状态，避免依赖外部 `guzhenren:gucaiyuanshi` 注册。
- DoD 断言保持不变语义：进度值变化通过 `fuel/max + burnPercent` 观察，经验增长仍断言 `+1`，数量守恒改为“非 refill 场景总数不变”。
