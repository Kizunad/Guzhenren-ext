# P0 Bug Report: Item Duping in `EatFromInventoryAction`

**Date:** 2025-12-07
**Severity:** P0 (Critical)
**Status:** Analysis Complete / Fix Proposed
**Component:** AI / Action System

## 1. Issue Summary
NPCs are duplicating items when performing the `EatFromInventoryAction`. specifically, if an NPC is holding a valuable item (e.g., a Diamond Sword) and decides to eat food from its inventory, the sword is duplicated into the inventory upon completion of the eating action.

## 2. Root Cause Analysis
The vulnerability lies in the **lifecycle management** of the `EatFromInventoryAction` class, specifically a "Double Cleanup" error.

### The Mechanism of Failure
1.  **Setup**: NPC holds **Item A** (Sword). `EatFromInventoryAction` starts, swaps Item A into temp storage, and puts **Item B** (Food) in the main hand.
2.  **Execution (`tickInternal`)**:
    *   The delegate `UseItemAction` finishes eating.
    *   The code immediately calls `returnLeftovers()`.
    *   `returnLeftovers()` puts remaining Food B in inventory and **restores Item A (Sword) to the main hand**.
    *   The method returns `ActionStatus.SUCCESS`.
3.  **Termination (`ActionExecutor`)**:
    *   Receiving `SUCCESS`, the executor calls `stop()` on the action to clean up.
4.  **The Glitch (`onStop`)**:
    *   `onStop()` unconditionally calls `rollback()`.
    *   `rollback()` checks the *current* main hand item. Because of step 2, this is now **Item A (Sword)**.
    *   Logic assumes the hand holds temporary food. It takes **Item A**, attempts to add it to the inventory (Success: **Duped Sword** in inventory).
    *   It then restores `previousMainHand` (Item A) to the hand (Result: **Original Sword** remains in hand).

### Code Reference
*File: `src/main/java/com/Kizunad/customNPCs/ai/actions/common/EatFromInventoryAction.java`*

```java
@Override
protected ActionStatus tickInternal(INpcMind mind, Mob mob) {
    // ...
    if (status == ActionStatus.SUCCESS) {
        mind.getStatus().eat(consumedStack, mob);
        returnLeftovers(mind, mob); // <--- CRITICAL: First restoration happen here
    } else if (status == ActionStatus.FAILURE) {
        rollback(mind, mob);
    }
    return status;
}

@Override
protected void onStop(INpcMind mind, LivingEntity entity) {
    // ...
    rollback(mind, entity); // <--- CRITICAL: Second restoration happens here unconditionally
}
```

## 3. Related Risks & Findings

### A. `NpcInventory.addItem` Side Effects
The method `NpcInventory.addItem(ItemStack stack)` modifies the **input stack object** (shrinking its count) directly.
*   **Risk**: If a developer passes a stack to `addItem` and later uses that same stack reference expecting it to be the "original state" (e.g., to restore it to a hand), they will find it empty or changed.
*   **Recommendation**: Always pass `stack.copy()` if the original reference is needed later, or verify documentation.

### B. `HealGoal` Implementation
`HealGoal` manually orchestrates `UseItemAction` without using the `ActionExecutor` queue.
*   **Risk**: It implements its own `prepareItem` and `cleanup`. If the item in the main hand changes unexpectedly during the healing tick (e.g., via command or interaction), the manual cleanup logic might overwrite or lose the new item.

## 4. Reproduction Steps
1.  Give an NPC a Diamond Sword in the Main Hand.
2.  Give the NPC a stack of Bread in the Inventory.
3.  Damage the NPC to trigger the hunger/healing logic (or wait for hunger).
4.  Observe the NPC eating.
5.  Check NPC inventory after eating finishes.
    *   **Expected**: Bread count decreases, Sword remains in hand.
    *   **Actual**: Bread count decreases, Sword remains in hand, **AND** a new Diamond Sword appears in the inventory.

## 5. Solution Proposal

### Immediate Fix (EatFromInventoryAction)
Refactor the cleanup logic to ensure it runs exactly once.

1.  Remove `returnLeftovers()` call from `tickInternal`.
2.  Introduce a state flag `boolean successful = false`.
3.  In `tickInternal`, set `successful = true` on success.
4.  In `onStop`, branch logic:
    ```java
    protected void onStop(INpcMind mind, LivingEntity entity) {
        if (successful) {
            returnLeftovers(mind, entity);
        } else {
            rollback(mind, entity);
        }
    }
    ```

### Long-term Prevention
1.  **Lifecycle Enforcement**: Strictly forbid state-restoring logic inside `tick()`. `tick()` should only return status; `stop()` should handle all state restoration.
2.  **API Safety**: Add `@SideEffect` annotations or defensive copies to `NpcInventory.addItem`.
