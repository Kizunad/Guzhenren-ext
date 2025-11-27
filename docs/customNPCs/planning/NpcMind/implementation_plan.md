# Action Standardization Implementation Plan

## Goal Description
Standardize the Action layer by defining generic interfaces (`Interaction`, `Attack`, `UseItem`) and eliminating duplicate action lifecycle management logic in specific Goals (`HealGoal`, `DefendGoal`) to adhere to DRY principles.

## User Review Required
> [!IMPORTANT]
> This refactoring will modify `HealGoal` and `DefendGoal`. Any custom logic tightly coupled to the old implementation might need adjustment, though the behavior should remain the same.

## Proposed Changes

### Action Interfaces
Define standard interfaces for common actions to decouple Goals from concrete Action implementations.

#### [NEW] [IAttackAction.java](file:///home/kiz/Code/java/Guzhenren-ext/src/main/java/com/Kizunad/customNPCs/ai/actions/interfaces/IAttackAction.java)
- Extends `IAction`.
- Defines methods specific to attacking (e.g., `getTarget()`).

#### [NEW] [IUseItemAction.java](file:///home/kiz/Code/java/Guzhenren-ext/src/main/java/com/Kizunad/customNPCs/ai/actions/interfaces/IUseItemAction.java)
- Extends `IAction`.
- Defines methods specific to item usage.

#### [NEW] [IInteractionAction.java](file:///home/kiz/Code/java/Guzhenren-ext/src/main/java/com/Kizunad/customNPCs/ai/actions/interfaces/IInteractionAction.java)
- Extends `IAction`.
- Defines methods specific to block/entity interaction.

### Action Implementations
Update existing actions to implement the new interfaces.

#### [MODIFY] [AttackAction.java](file:///home/kiz/Code/java/Guzhenren-ext/src/main/java/com/Kizunad/customNPCs/ai/actions/common/AttackAction.java)
- Implement `IAttackAction`.

#### [MODIFY] [UseItemAction.java](file:///home/kiz/Code/java/Guzhenren-ext/src/main/java/com/Kizunad/customNPCs/ai/actions/common/UseItemAction.java)
- Implement `IUseItemAction`.

#### [MODIFY] [InteractBlockAction.java](file:///home/kiz/Code/java/Guzhenren-ext/src/main/java/com/Kizunad/customNPCs/ai/actions/common/InteractBlockAction.java)
- Implement `IInteractionAction`.

### Goal Logic Standardization
Eliminate duplicate code for creating, starting, ticking, and checking actions in Goals.

#### [NEW] [GoalActionHelper.java](file:///home/kiz/Code/java/Guzhenren-ext/src/main/java/com/Kizunad/customNPCs/ai/decision/goals/GoalActionHelper.java)
- A utility class to manage a single action's lifecycle within a Goal.
- Methods: `startAction`, `tickAction`, `stopAction`, `isRunning`, `isSuccess`.
- Handles the interaction with `ActionExecutor` if necessary, or manages the tick manually if the Goal is responsible (currently Goals seem to tick actions manually in some cases, or we should standardize on `ActionExecutor`).
- **Decision**: We will standardize on using `ActionExecutor` to run the action, and the Goal monitors it.

#### [MODIFY] [HealGoal.java](file:///home/kiz/Code/java/Guzhenren-ext/src/main/java/com/Kizunad/customNPCs/ai/decision/goals/HealGoal.java)
- Use `GoalActionHelper` (or similar logic) to manage `UseItemAction`.
- Remove duplicate state management code.

#### [MODIFY] [DefendGoal.java](file:///home/kiz/Code/java/Guzhenren-ext/src/main/java/com/Kizunad/customNPCs/ai/decision/goals/DefendGoal.java)
- Use `GoalActionHelper` to manage `AttackAction`.
- Remove duplicate state management code.

## Verification Plan

### Automated Tests
- Run existing `NpcMindGameTests` to ensure `HealGoal` still works.
- Run `ComplexScenarios` (e.g., `testTheGatherer`) to ensure general stability.
- Create a new test `ActionStandardizationTest` if needed, but existing tests cover `HealGoal` and `DefendGoal` behavior implicitly.
    - `testSurvivalGoal` covers `HealGoal`.
    - We should verify `DefendGoal` if there is a test for it. If not, we might rely on manual verification or add a simple test.

### Manual Verification
- Spawn an NPC with `HealGoal` (low health). Verify it eats food.
- Spawn an NPC with `DefendGoal` (hit it). Verify it attacks back.
