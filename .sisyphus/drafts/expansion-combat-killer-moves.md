# Draft: Expansion Pack 2 - Combat & Killer Moves

> **Status**: Concept Draft
> **Dependency**: `xianqiao-tier-and-tribulation` (Base), `expansion-ecology-time` (Dao Marks)
> **Goal**: Transform combat from "Stat Stick" to "Strategic Programming".

## 1. Core Concept (核心概念)
- **Killer Moves (杀招)**: Not just a skill, but a **programmable combination** of Gu worms.
- **Deduction (推演)**: Players must "compile" their moves. Success is not guaranteed; it depends on Dao Marks and Wisdom Path attainment.
- **Risk (风险)**: Powerful moves carry the risk of **Backlash (反噬)**.

---

## 2. The Deduction Board (推演盘 GUI)

### Interface
- **Grid**: 3x3 layout (expandable to 5x5 with upgrades).
- **Core Slot (Center)**: Accepts 1 Immortal Gu (Determines Main Element/Form).
- **Support Slots (Surrounding)**: Accepts 1-8 Mortal/Immortal Gu (Modifiers).
- **Result Preview**: Shows calculated stats (Damage, Range, Cost, Cool-down, Success Rate).

### Logic flow
1.  **Input**: Player places `Fireball Gu` (Core) + `Wind Blade Gu` (Support) + `Amplify Gu` (Support).
2.  **Calculation**:
    - `Base Damage` = Core Gu Damage.
    - `Multiplier` = 1.0 + (Support Gu Count * 0.2) + (Dao Mark Bonus).
    - `Cost` = Sum of all Gu activation costs * 0.8 (Synergy discount).
    - `Success Rate` = `Base (80%)` + `Wisdom Path Bonus` - `Complexity Penalty`.
3.  **Output**: `Killer Move Token` (Item).
    - Right-click to activate the move.
    - Infinite durability, but consumes Immortal Essence on use.

---

## 3. Activation & Backlash (激活与反噬)

When a player uses a `Killer Move Token`:

### Success Check (RNG)
- Roll `d100` vs `Success Rate`.
- **Pass**: Move executes perfectly.
- **Fail**: **Backlash** occurs.

### Backlash Tiers
1.  **Minor (10% chance)**:
    - Move fails. Cooldown triggered. Essence consumed.
    - *Message*: "Activation failed! Essence wasted."
2.  **Major (5% chance)**:
    - Player takes `30% Max HP` true damage.
    - Core Gu is disabled for 1 minute (cannot be used).
    - *Message*: "Backlash! Your meridian is damaged."
3.  **Catastrophic (1% chance - only for complex moves)**:
    - Player takes `80% Max HP` damage.
    - Random Support Gu breaks (durability -100 or destroyed).
    - *Message*: "CATASTROPHIC FAILURE! Gu worm destroyed!"

---

## 4. Dao Mark Conflict (流派互斥)

Implemented via **Success Rate Penalties**, not hard blocks.

### The Conflict Formula
`ConflictPenalty = (OffPathDaoMarks / MainPathDaoMarks) * 20%`

*   **Example**:
    - **Main Path**: Fire (Core Gu is Fire). Player has 5000 Fire Marks.
    - **Off Path**: Water (Support Gu is Water). Player has 1000 Water Marks.
    - **Result**: `(1000 / 5000) * 20%` = **4% Penalty**. Manageable.
    - **Scenario B**: Player has 5000 Water Marks (Water Path Cultivator) trying to use Fire Move.
    - **Result**: `(5000 / 100) * 20%` = **1000% Penalty**. Impossible to activate.

---

## 5. Integration with Ecology (与生态联动)

- **Tier 2 Resource Points (Formations)**:
    - Building a **"Refinement Hall"** unlocks the 5x5 Deduction Grid (allowing Ultimate Moves).
    - Building a **"Wisdom Star Platform"** adds flat +10% Success Rate to all moves.
- **Resource Consumption**:
    - Deduction requires **"Star Thoughts"** (consumable resource from Wisdom Path resource points).

## 6. Technical Roadmap
1.  Implement `KillerMoveData` structure (NBT).
2.  Create `DeductionMenu` and `DeductionScreen` (GUI).
3.  Implement `KillerMoveLogic` (Calculation & Execution).
4.  Add `Backlash` damage source and event handling.
