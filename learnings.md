# Learnings

## Task 6: Cluster NPC Slot Interaction
- **ContainerData Long Sync**: `ContainerData` only supports `int`. To sync a `long` (pendingOutput), we must split it into two `int`s (low/high) and reconstruct it on the client.
- **Checkstyle Rigor**: The project enforces strict Checkstyle rules, including Magic Number checks even for UI coordinates (e.g., `12`, `94`, `37`). All integers in UI code must be extracted to constants.
- **StorageGu Integration**: Reusing `StorageGuItem.getStorageHandler(stack)` allows seamless insertion without knowing internal NBT structure, maintaining encapsulation.
