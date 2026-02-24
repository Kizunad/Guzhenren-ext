# Draft: Expansion Pack 1 - Ecology & River of Time

> **Status**: Detailed Design Draft
> **Dependency**: `xianqiao-tier-and-tribulation` (Completed)
> **Goal**: Transform the Immortal Aperture from a "Combat Arena" into a "High-Stakes Management Simulation".

## 1. Core Philosophy
- **Ecology (生态)**: Players must build and manage an ecosystem, not just a storage room. Dao Marks are the "soil", Resource Points are the "crops".
- **Time (光阴)**: Time is a double-edged sword. Accelerating time yields resources but hastens death (Tribulations).
- **Uniqueness (唯一)**: True Immortal Domains (Tier 3) are unique artifacts, not mass-produced blocks.

---

## 2. Resource Point Architecture (资源点体系)

### Data Structure
Stored in `ApertureWorldData` (NBT).
```java
class ResourcePointData {
    BlockPos corePos;
    ResourceTier tier; // MORTAL, IMMORTAL, SECLUDED_DOMAIN
    ResourceType type; // FIRE, WATER, SOUL, TIME...
    int level;
    // For Tier 2/3:
    boolean isStructureComplete;
}
```

### Tier 1: Mortal Nodes (凡级资源点)
> **Concept**: "Micro-springs", "Earth Fire Vents".
- **Form**: Single Block (`BlockEntity`).
- **Logic**: **Environmental Scan**.
  - Example: `Earth Fire Vent` scans 3x3 area for `Lava` or `Magma Blocks`.
  - Efficiency = `(LavaCount * 0.1) * TimeRate`.
- **Output**: Mortal materials (e.g., `Fire Essence`, `Spirit Stones`).
- **Placement**: Unlimited.

### Tier 2: Immortal Formations (仙级资源点)
> **Concept**: "Dragonfish Sea", "Refinement Hall", "Cloud Soil Garden".
- **Form**: **Multiblock Structure** (Formation).
  - Core: `ImmortalFormationCore`.
  - Frame: Specific blocks (e.g., `Formation Flag`, `Immortal Stone Pillar`).
- **Logic**: **Structure Integrity Check**.
  - If structure broken: Deactivates immediately.
  - If intact: Output = `Base * DaoMarkBonus * TimeRate`.
- **Buffs**: Provides localized buffs within chunk (e.g., `Regeneration`, `Speed`).
- **Placement**: Limited by Aperture Size / Dao Marks.

### Tier 3: Secluded Domains of Heaven and Earth (天地秘境)
> **Concept**: "Danghun Mountain" (荡魂山), "Luo Po Valley" (落魄谷).
- **Form**: **Unique Mega-Structure**.
  - **Singleton**: Only **ONE** exists per World Save (or Server). Obtained via Chaos Tribulation drops or GM events.
  - **Indestructible**: Bedrock-level hardness. Only damaged by `Chaos Tribulation`.
- **Implementation**:
  - **No Biome Hacks**: We do NOT modify Biome IDs.
  - **Atmosphere**: Use Client-side Event Listeners (`RenderLevelStageEvent`) to change sky color/fog when player is near the structure center.
  - **Logic**:
    - **Global Aperture Buff**: Passive bonus to *entire* dimension (e.g., Soul Path Dao Marks +100/day).
    - **Exclusive Drop**: Produces `Guts Gu` (胆识蛊) - the only way to obtain it.
    - **Hazard**: Players/Mobs near it take specific damage (e.g., `Soul Shaking` damage).

---

## 3. River of Time Mechanics (光阴长河机制)

We adopt the **Logic Multiplier (逻辑倍率)** approach for server stability.

### The Time Rate (`timeFlowRate`)
- **Storage**: `double` in `ApertureWorldData`. Default `1.0`.
- **Modification**:
  - Requires **Time Path Immortal Gu** (e.g., `Spring Autumn Cicada` fragment).
  - Cost: Consumes `Immortal Essence` per tick to maintain high rates.

### Effects
1.  **Resource Output**:
    - `RealOutput = BaseOutput * timeFlowRate`.
2.  **Tribulation Timer**:
    - `NextTribulationTick -= (1 * timeFlowRate)`.
    - *Risk*: At `10x` speed, a 10-year Tribulation cycle arrives in 1 year.
3.  **Crop Growth (Optional)**:
    - Hook into `RandomTick`. If `timeFlowRate > 1`, execute `block.randomTick()` multiple times (capped at 5 to prevent lag).

---

## 4. Dao Mark Coupling (道痕联动)

Dao Marks are no longer just combat stats. They are **Environmental Modifiers**.

| Dao Type | Effect on Ecology | Effect on Combat |
|:---:|:---:|:---:|
| **Fire (炎)** | Boosts `Fire` resource points output. | Fire damage +%. Burn duration +%. |
| **Water (水)** | Boosts `Water`/`Ice` resource points. | Extinguishes fire. Water breathing. |
| **Time (宙)** | Reduces cost of maintaining `TimeRate`. | Cooldown reduction. |
| **Soul (魂)** | Boosts `Danghun Mountain` output. | Soul path damage +%. |
| **Refinement (炼)** | Increases success rate of automatic refining. | - |

---

## 5. Technical Roadmap (Post-Wave 4)

### Phase 1: The Foundation
1.  Implement `ResourcePointManager` capability.
2.  Add `timeFlowRate` to `ApertureWorldData`.

### Phase 2: The Content
3.  Implement Tier 1 Blocks (Scan logic).
4.  Implement Tier 2 Multiblock Validator.
5.  Implement Tier 3 "Danghun Mountain" (Structure template + Event logic).

### Phase 3: The Integration
6.  Hook `TribulationManager` to respect `timeFlowRate`.
7.  Hook `DaoMarkManager` to influence Resource Output.

---

## 6. Open Questions (To be resolved during implementation)
- **Q1**: How to display the "Atmosphere" of Tier 3 domains?
  - *Plan*: Use `FogEvents` and `RenderSystem` to tint the screen pink/grey when inside the structure's bounding box.
- **Q2**: How to prevent players from spamming Tier 1 blocks?
  - *Plan*: Diminishing returns if too many are in the same chunk (interference).

