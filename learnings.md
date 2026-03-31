# Learnings

## Task 6: Cluster NPC Slot Interaction
- **ContainerData Long Sync**: `ContainerData` only supports `int`. To sync a `long` (pendingOutput), we must split it into two `int`s (low/high) and reconstruct it on the client.
- **Checkstyle Rigor**: The project enforces strict Checkstyle rules, including Magic Number checks even for UI coordinates (e.g., `12`, `94`, `37`). All integers in UI code must be extracted to constants.
- **StorageGu Integration**: Reusing `StorageGuItem.getStorageHandler(stack)` allows seamless insertion without knowing internal NBT structure, maintaining encapsulation.
 - Deep creature cost batch: keep `TASK17_COST_ASSERT_DELAY_TICKS` below `TEST_TIMEOUT_TICKS`; scheduling the only success callback on the timeout boundary caused the batch to hang under this environment, while the main batch stayed green with its snapshot isolation.
