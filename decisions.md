# Decisions Log

## Task 6: Cluster NPC Slot Interaction
- **Menu Architecture**: Implemented `ClusterNpcMenu` with `ContainerData` sync (4 ints) for `pendingOutput` (long split into 2 ints) and `efficiencyBase`.
- **Inventory**: Added `SimpleContainer(1)` to `ClusterNpcEntity` specifically for `StorageGuItem`.
- **Interaction Logic**:
  - NPC ticks server-side.
  - If `pendingOutput > 0` and Slot 0 has `StorageGuItem`, it attempts to push output into the item using `StorageGuHandler`.
  - Pushed amount is consumed from `pendingOutput`.
- **Screen UI**:
  - Used standard `generic_54` background.
  - Displays "待提取: [amount]".
  - Displays warning "请放入储物蛊以接收产出" when `pendingOutput > 0`.
- **Magic Numbers**: Extracted all GUI coordinates and data synchronization constants to static final fields to satisfy Checkstyle.
