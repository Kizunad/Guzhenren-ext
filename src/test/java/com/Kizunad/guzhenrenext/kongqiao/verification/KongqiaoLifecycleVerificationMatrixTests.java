package com.Kizunad.guzhenrenext.kongqiao.verification;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class KongqiaoLifecycleVerificationMatrixTests {

    @Test
    void kongqiaoActivationAndMenuAvailabilityAreExplicitlyGated() throws Exception {
        final String serviceSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/kongqiao/service/KongqiaoService.java")
        );
        final String menuSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/kongqiao/menu/KongqiaoMenu.java")
        );

        assertTrue(serviceSource.contains("public static KongqiaoData requireGameplayActivatedData("));
        assertTrue(serviceSource.contains("GAMEPLAY_NOT_ACTIVATED_MESSAGE"));
        assertTrue(serviceSource.contains("!data.isGameplayActivated()"));
        assertTrue(serviceSource.contains("player.sendSystemMessage(GAMEPLAY_NOT_ACTIVATED_MESSAGE);"));
        assertTrue(serviceSource.contains("openKongqiaoMenu("));
        assertTrue(menuSource.contains("getUnlockedSlots()"));
        assertTrue(menuSource.contains("public boolean isActive()"));
        assertTrue(menuSource.contains("return unlocked;"));
    }

    @Test
    void relogAndRestartPreserveStabilityStateRoundTrip() throws Exception {
        final String dataSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/kongqiao/attachment/KongqiaoData.java")
        );

        assertTrue(dataSource.contains("tag.put(\"stability\", stabilityState.serializeNBT(provider));"));
        assertTrue(dataSource.contains("if (tag.contains(\"stability\", Tag.TAG_COMPOUND))"));
        assertTrue(dataSource.contains("stabilityState.deserializeNBT(provider, null);"));
        assertTrue(dataSource.contains("TAG_BURST_PRESSURE"));
        assertTrue(dataSource.contains("TAG_FATIGUE_DEBT"));
        assertTrue(dataSource.contains("TAG_OVERLOAD_TIER"));
        assertTrue(dataSource.contains("TAG_FORCED_DISABLED_USAGE_IDS"));
        assertTrue(dataSource.contains("TAG_SEALED_SLOTS"));
        assertTrue(dataSource.contains("TAG_LAST_DECAY_GAME_TIME"));
    }

    @Test
    void dedicatedServerSyncPopulatesNianTouAndShazhaoDescriptionsAfterLogin() throws Exception {
        final String loginSyncSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/kongqiao/event/NianTouSyncEvents.java")
        );
        final String packetSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/kongqiao/network/PacketSyncKongqiaoData.java")
        );
        final String nianTouManagerSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/kongqiao/niantou/NianTouDataManager.java")
        );
        final String shazhaoManagerSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/kongqiao/shazhao/ShazhaoDataManager.java")
        );

        assertTrue(loginSyncSource.contains("new PacketSyncKongqiaoData("));
        assertTrue(loginSyncSource.contains("List.copyOf(NianTouDataManager.getAll())"));
        assertTrue(loginSyncSource.contains("List.copyOf(ShazhaoDataManager.getAll())"));
        assertTrue(packetSource.contains("NianTouDataManager.clear();"));
        assertTrue(packetSource.contains("NianTouDataManager.register(data);"));
        assertTrue(packetSource.contains("ShazhaoDataManager.clear();"));
        assertTrue(packetSource.contains("ShazhaoDataManager.register(data);"));
        assertTrue(nianTouManagerSource.contains("public static Collection<NianTouData> getAll()"));
        assertTrue(shazhaoManagerSource.contains("public static Collection<ShazhaoData> getAll()"));
    }

    @Test
    void capacitySyncDelegatesToTask3BridgeTruthAndReclaimsOverflowOnShrink() throws Exception {
        final String capacityServiceSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/kongqiao/service/KongqiaoCapacityService.java")
        );

        assertTrue(capacityServiceSource.contains("KongqiaoCapacityProfile profile = KongqiaoCapacityBridge.resolveFromEntity(entity);"));
        assertTrue(capacityServiceSource.contains("int targetRows = profile.totalRows();"));
        assertTrue(capacityServiceSource.contains("settings.setUnlockedRows(targetRows);"));
        assertTrue(capacityServiceSource.contains("if (targetSlots < currentSlots) {"));
        assertTrue(capacityServiceSource.contains("reclaimOverflowItems(entity, inventory, targetSlots);"));
        assertTrue(capacityServiceSource.contains("for (int slot = unlockedSlots; slot < inventory.getContainerSize(); slot++) {"));
        assertTrue(capacityServiceSource.contains("ItemStack toReturn = stack.copy();"));
        assertTrue(capacityServiceSource.contains("inventory.setItem(slot, ItemStack.EMPTY);"));
        assertTrue(capacityServiceSource.contains("returnItem(entity, toReturn);"));
        assertTrue(capacityServiceSource.contains("boolean added = player.getInventory().add(stack);"));
        assertTrue(capacityServiceSource.contains("player.drop(stack, false);"));
        assertTrue(capacityServiceSource.contains("entity.spawnAtLocation(stack, DEFAULT_DROP_OFFSET);"));
    }

    @Test
    void identificationProgressionAndUnlockPersistenceStayOnTheSameTruthSource() throws Exception {
        final String unlockSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/kongqiao/attachment/NianTouUnlocks.java")
        );
        final String tickHandlerSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/kongqiao/handler/NianTouTickHandler.java")
        );
        final String menuSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/kongqiao/menu/NianTouMenu.java")
        );
        final String packetSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/kongqiao/network/PacketSyncNianTouUnlocks.java")
        );
        final String shazhaoServiceSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/kongqiao/shazhao/ShazhaoUnlockService.java")
        );

        assertTrue(unlockSource.contains("private String shazhaoMessage = \"\";"));
        assertTrue(unlockSource.contains("private UnlockProcess currentProcess = null;"));
        assertTrue(unlockSource.contains("public void unlock(ResourceLocation item, String usageId)"));
        assertTrue(unlockSource.contains("public boolean isUsageUnlocked(ResourceLocation item, String usageId)"));
        assertTrue(unlockSource.contains("public void setShazhaoMessage(String message)"));
        assertTrue(unlockSource.contains("tag.put(\"shazhao_unlocked\", shazhaoList);"));
        assertTrue(unlockSource.contains("tag.put(\"process\", processTag);"));
        assertTrue(unlockSource.contains("this.currentProcess = new UnlockProcess("));
        assertTrue(unlockSource.contains("this.currentProcess = null;"));

        assertTrue(tickHandlerSource.contains("unlocks.unlock(process.itemId, process.usageId);"));
        assertTrue(tickHandlerSource.contains("ShazhaoUnlockService.tryUnlockRandom(player.getRandom(), unlocks);"));
        assertTrue(tickHandlerSource.contains("PacketDistributor.sendToPlayer(player, new PacketSyncNianTouUnlocks(unlocks));"));

        assertTrue(menuSource.contains("final NianTouUnlocks unlocks = KongqiaoAttachments.getUnlocks(serverPlayer);"));
        assertTrue(menuSource.contains("if (!unlocks.isUsageUnlocked(itemId, usage.usageID()))"));
        assertTrue(menuSource.contains("unlocks.startProcess(itemId, selected.usageID(), duration, cost);"));
        assertTrue(menuSource.contains("new PacketSyncNianTouUnlocks(unlocks)"));

        assertTrue(packetSource.contains("unlocks.startProcess(id, usageId, total, cost);"));
        assertTrue(packetSource.contains("unlocks.getCurrentProcess().remainingTicks = remaining;"));
        assertTrue(packetSource.contains("unlocks.unlockShazhao(buf.readResourceLocation());"));
        assertTrue(packetSource.contains("unlocks.setShazhaoMessage(buf.readUtf());"));
        assertTrue(packetSource.contains("context.player().setData(KongqiaoAttachments.NIANTOU_UNLOCKS.get(), payload.data);"));

        assertTrue(shazhaoServiceSource.contains("public static List<UnlockCandidate> listUnlockCandidates("));
        assertTrue(shazhaoServiceSource.contains("if (unlocks.isShazhaoUnlocked(shazhaoId))"));
        assertTrue(shazhaoServiceSource.contains("if (!unlocks.isUnlocked(id))"));
        assertTrue(shazhaoServiceSource.contains("unlocks.unlockShazhao("));
    }
}
