# NpcInventory 使用说明

## addItem 行为
- 会直接修改传入的 `ItemStack`，将可放入的数量写入背包，剩余数量保留在原栈并作为返回值。
- 优先尝试与同类物品合并，然后按最大堆叠数拆分放入空槽；不会产生超量堆叠。
- 返回值为未能放入的余量；返回 `ItemStack.EMPTY` 表示已全部存入。

## 调用方约定（防止复制）
- **禁止** 在调用前对入参再额外拷贝；直接把手上/掉落物栈传入。
- 调用后若需要保留余量：使用 `remaining = inventory.addItem(stack);`，随后只操作 `remaining`。
- 调用后若无需余量：直接 `inventory.addItem(stack);`，不要再对原栈执行 `shrink` 或 `split`。
- 需要记录拾取数量时，先读原始 `count`，再调用 `addItem`，以免被入参缩减影响统计。

## 推荐模式
```java
int original = stack.getCount();
ItemStack leftover = inventory.addItem(stack);
if (leftover.isEmpty()) {
    // 全部放入，stack 已变为空
} else {
    // 只剩下未放入的数量
}
```
