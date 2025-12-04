# How To Test: Guzhenren-ext GameTest Guide

This guide provides a practical walkthrough for writing and running tests in the `Guzhenren-ext` project using the NeoForge GameTest framework. We use a **Headless (NO_GUI)** environment for CI-friendly, rapid testing of NPC AI.

## 1. Quick Start: Running Tests

You do not need to launch the full Minecraft client.

### Run All Tests
```bash
./gradlew runGameTestServer
```

### Run Specific Batches
You can filter tests by their `batch` parameter (defined in `TestBatches.java`).
*(Note: This requires passing arguments to the gradle task, or modifying the run configuration. For local dev, running all is usually fast enough ~30s).*

**Log Location**: `run/logs/latest.log` (Check this if tests fail silently or hang).

---

## 2. The Testing Toolkit

We have built a robust set of utilities to make testing NPC AI easier. **Do not rely on raw vanilla `GameTestHelper` methods alone.**

### 📍 Key Classes
*   **`NpcTestHelper`**: The core utility for assertions, waiting, and AI management.
*   **`TestEntityFactory`**: Creates NPCs with `NpcMind` correctly attached and configured.
*   **`TestBatches`**: Constants for test grouping (e.g., `GOAP`, `REAL_API`).

---

## 3. Writing a Test: The Standard Pattern

A robust AI test usually follows this 4-step pattern:

1.  **Setup**: Spawn entities and build the environment.
2.  **Configure AI**: Inject Goals or Sensors into the `NpcMind`.
3.  **Drive AI**: Register the tick hook.
4.  **Await Result**: Use `waitForCondition` to handle asynchronous AI behavior.

### Example: Zombie Picks Up Item

```java
@GameTestHolder("guzhenrenext")
public class ExampleTests {

    @GameTest(template = "empty", timeoutTicks = 200, batch = TestBatches.GOAP)
    public void testZombiePicksUpItem(GameTestHelper helper) {
        // 1. Setup
        BlockPos spawnPos = new BlockPos(2, 2, 2); // Relative coords
        Zombie npc = TestEntityFactory.createTestZombie(helper, spawnPos);
        
        ItemEntity item = helper.spawnItem(Items.DIAMOND, new BlockPos(4, 2, 2));

        // 2. Configure AI (Get Mind -> Register Goal)
        INpcMind mind = NpcTestHelper.getMind(helper, npc);
        mind.getGoalSelector().registerGoal(new PickUpItemGoal(item, 1.0f));

        // 3. Drive AI (CRITICAL STEP)
        // Without this, the NPC's brain will not update!
        NpcTestHelper.tickMind(helper, npc);

        // 4. Await Result
        // We wait up to 100 ticks for the condition to become true
        NpcTestHelper.waitForCondition(helper, () -> {
            boolean itemGone = !item.isAlive();
            boolean npcHasItem = npc.isHolding(Items.DIAMOND);
            return itemGone && npcHasItem;
        }, 100, "Zombie failed to pick up diamond");
    }
}
```

---

## 4. ⚠️ Critical Concept: The Coordinate Trap

This is the #1 source of bugs in GameTests.

*   **GameTest Helper** uses **Relative Coordinates** (e.g., `(2, 2, 2)` is inside the test structure).
*   **Entity AI / Navigation** uses **Absolute Coordinates** (World coordinates).

**The Rule:**
If you pass a target location to an NPC Goal or Action, you **MUST** convert it.

```java
// ❌ WRONG: NPC will try to walk to (5, 2, 5) which might be at bedrock or void
BlockPos target = new BlockPos(5, 2, 5);
new MoveToGoal(target); 

// ✅ CORRECT: Convert relative structure pos to absolute world pos
BlockPos targetRel = new BlockPos(5, 2, 5);
BlockPos targetAbs = helper.absolutePos(targetRel); 
new MoveToGoal(targetAbs);
```

---

## 5. Best Practices

### 1. Always use `TestEntityFactory`
It automatically:
*   Attaches the `NpcMind` capability.
*   Applies a unique `test:hash` tag to isolate the entity from other running tests.
*   Initializes default sensors.

### 2. Prefer `waitForCondition` over `succeedWhen`
`succeedWhen` is brittle for complex AI chains. `NpcTestHelper.waitForCondition` allows you to check state every tick and fail with a custom message if the timeout is reached.

### 3. Test Isolation
Sensors like `VisionSensor` are patched in tests (`TestVisionSensor`) to only see entities with the same `test:` tag. This prevents Test A's Zombie from getting scared of Test B's Iron Golem running nearby.

### 4. Real API vs. Mock
We prefer **Real API** tests.
*   Don't mock `move()`. Let the NPC pathfind using `MoveToAction`.
*   Don't mock `breakBlock()`. Use `level.destroyBlock()`.
*   Ensure you increase `timeoutTicks` for these tests (pathfinding takes time!).

---

## 6. Debugging Tips

*   **Use `System.out.println`**: The logs capture stdout. Print NPC positions and states during the test.
*   **Mind Inspector**: In a real game, use the `MindInspectorItem` (compass) to click NPCs and see their current Goal/Memory.
*   **Watch the Logs**: If a test times out, the log usually shows the last tick's state if you added print statements.

---

## 7. Directory Structure

*   **Tests**: `src/main/java/com/Kizunad/customNPCs_test/`
*   **Structure Files**: `src/main/resources/data/guzhenrenext/structure/`
    *   If you get "Structure not found", check this folder.
    *   Use `empty.nbt` (or `empty3x3x3.nbt`) for code-driven tests to avoid making NBT files.
