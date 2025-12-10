# GuzhenrenItemDispatcher

该组件位于 `src/main/java/com/Kizunad/guzhenrenext/guzhenrenBridge/generated/itemUse/GuzhenrenItemDispatcher.java`，用于让自定义 NPC 在使用蛊真人模组物品时自动触发对应的 Procedure，彻底摆脱“蛊真人更新一次、手动补一次”的重复劳动。

## 核心能力

- **自动映射**：启动时遍历 `BuiltInRegistries.ITEM` 中 `namespace=guzhenren` 的所有物品，根据物品类名（例如 `WeiLianHuaXiongLiGuItem`）自动拼接固定后缀（`DangYouJiKongQiShiShiTiDeWeiZhi`, `YouJiKongQiShiShiTiDeWeiZhi` 等）寻找 `net.guzhenren.procedures` 包下的 `execute` 方法。
- **通用调用**：按 Procedure 的真实参数签名（`LevelAccessor` / `double` 坐标 / `Entity` / `ItemStack`）自动构造调用实参，再用反射调用，无需关心方法是否需要物品栈或坐标。
- **一次性缓存**：所有映射写入 `IdentityHashMap<Item, ProcedureInvoker>` 并对外只读，`dispatch` 在战斗/交互时只需 O(1) 查表即可完成调用。
- **日志可观测**：成功映射数量、缺失 Procedure、参数签名不受支持等都会写入日志，方便调试或追踪蛊真人上游命名变动。

## 调用方式

```java
boolean consumed = GuzhenrenItemDispatcher.dispatch(npcEntity, heldStack);
if (consumed) {
    // Procedure 已执行，按需继续后续逻辑
}
```

返回值表示是否存在匹配 Procedure 并成功调用，便于调用方回退到默认逻辑（例如直接执行 `Item#use` 或放弃攻击）。

## 扩展/维护

1. **新增 Procedure 后缀**：若蛊真人未来出现新的命名模式，只需在 `PROCEDURE_SUFFIXES` 中添加对应字符串即可立即生效，无需改动其它代码。
2. **签名支持**：默认支持 `LevelAccessor`、`double`、`Entity`、`ItemStack` 四种参数；如果出现其它参数类型，请视情况扩展 `supportsSignature` 与 `buildArguments`。
3. **调试建议**：使用 `-Dorg.slf4j.simpleLogger.log.com.Kizunad.guzhenrenext=debug` 可观察缓存构建细节，确保所有关键蛊虫都完成映射。

通过该机制，只要蛊真人保持类名与 Procedure 命名一致，即可“一劳永逸”地在 NPC 攻击时调用对应逻辑。无需再反编译/复制蛊真人代码，也无需在模组更新后手工维护庞大的 `if-else` 链。***
