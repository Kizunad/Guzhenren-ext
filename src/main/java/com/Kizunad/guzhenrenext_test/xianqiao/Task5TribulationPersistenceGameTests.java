package com.Kizunad.guzhenrenext_test.xianqiao;

import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionAttemptStage;
import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoMarkDiffusionService;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInitializationState;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.AscensionAttemptState;
import com.Kizunad.guzhenrenext.xianqiao.tribulation.TribulationState;
import com.Kizunad.guzhenrenext.xianqiao.tribulation.TribulationTickHandler;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("guzhenrenext")
@PrefixGameTestTemplate(false)
public class Task5TribulationPersistenceGameTests {

    private static final String TASK5_BATCH = "task5_tribulation_persistence";

    private static final int TEST_TIMEOUT_TICKS = 360;

    private static final int APERTURE_RADIUS_BLOCKS = 64;

    private static final int START_TRIGGER_DELAY_TICKS = 5;

    private static final int CACHE_RESET_DELAY_TICKS = 10;

    private static final int CLEANUP_CHECK_DELAY_TICKS = 15;

    private static final UUID OWNER_UUID_A = UUID.fromString("00000000-0000-0000-0000-000000050001");

    private static final UUID OWNER_UUID_B = UUID.fromString("00000000-0000-0000-0000-000000050002");

    private static final String OWNER_NAME_A = "task5_owner_a";

    private static final String OWNER_NAME_B = "task5_owner_b";

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK5_BATCH
    )
    public void testTask5CanonicalCommittedAttemptShouldStartTribulationByPersistedTruth(GameTestHelper helper) {
        ServerLevel level = resolveAuthoritativeLevel(helper.getLevel());
        ServerPlayer owner = createTestPlayer(level, OWNER_UUID_A, OWNER_NAME_A);
        prepareApertureContext(level, owner);
        UUID ownerUuid = owner.getUUID();

        setConfirmedTribulationTruth(level, ownerUuid);
        helper.succeedWhen(() -> {
            ApertureWorldData worldData = ApertureWorldData.get(resolveAuthoritativeLevel(helper.getLevel()));
            helper.assertTrue(
                worldData.hasActiveTribulationRuntimeState(ownerUuid),
                "CONFIRMED 尝试未通过持久化真源启动灾劫运行态"
            );
            helper.assertTrue(
                worldData.isTribulationActive(ownerUuid),
                "灾劫激活判定未锚定到持久化运行态"
            );
            helper.assertTrue(
                worldData.getTribulationActiveOwners().contains(ownerUuid),
                "活跃灾劫 owner 集未反映持久化运行态"
            );
            helper.assertTrue(
                worldData.getAscensionAttemptState(ownerUuid).stage()
                    == AscensionAttemptStage.WORLD_TRIBULATION_IN_PLACE,
                "灾劫启动后尝试阶段未推进到 WORLD_TRIBULATION_IN_PLACE"
            );
            helper.succeed();
        });
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK5_BATCH
    )
    public void testTask5PersistedRuntimeStateShouldResumeAfterManagerCacheReset(GameTestHelper helper) {
        ServerLevel level = resolveAuthoritativeLevel(helper.getLevel());
        ServerPlayer owner = createTestPlayer(level, OWNER_UUID_B, OWNER_NAME_B);
        prepareApertureContext(level, owner);
        UUID ownerUuid = owner.getUUID();

        setConfirmedTribulationTruth(level, ownerUuid);
        helper.runAfterDelay(START_TRIGGER_DELAY_TICKS, () -> {
            ApertureWorldData worldData = ApertureWorldData.get(resolveAuthoritativeLevel(helper.getLevel()));
            helper.assertTrue(
                worldData.hasActiveTribulationRuntimeState(ownerUuid),
                "灾劫首次启动后未写入持久化运行态"
            );
            clearActiveManagersCache();
            worldData.markAttemptStage(ownerUuid, AscensionAttemptStage.WORLD_TRIBULATION_IN_PLACE);
            worldData.updateTribulationTick(ownerUuid, Long.MAX_VALUE);
        });

        helper.runAfterDelay(CACHE_RESET_DELAY_TICKS, () -> {
            ApertureWorldData worldData = ApertureWorldData.get(resolveAuthoritativeLevel(helper.getLevel()));
            helper.assertTrue(
                worldData.hasActiveTribulationRuntimeState(ownerUuid),
                "缓存中断后未从持久化运行态恢复灾劫"
            );
            clearActiveManagersCache();
            injectTribulationRuntimeState(
                worldData,
                ownerUuid,
                new ApertureWorldData.TribulationRuntimeState(
                    TribulationState.SETTLEMENT,
                    0,
                    0.0F,
                    0,
                    0,
                    0,
                    false,
                    false
                )
            );
            TribulationTickHandler.onServerTickPost(
                new ServerTickEvent.Post(() -> true, resolveAuthoritativeLevel(helper.getLevel()).getServer())
            );
        });

        helper.runAfterDelay(CLEANUP_CHECK_DELAY_TICKS, () -> {
            ApertureWorldData worldData = ApertureWorldData.get(resolveAuthoritativeLevel(helper.getLevel()));
            helper.assertTrue(
                !worldData.hasActiveTribulationRuntimeState(ownerUuid),
                "退出灾劫交接阶段后未清理持久化运行态"
            );
            helper.assertTrue(
                worldData.getAscensionAttemptState(ownerUuid).stage() == AscensionAttemptStage.APERTURE_FORMING,
                "恢复后结算/退出路径未回收至 APERTURE_FORMING"
            );
            helper.succeed();
        });
    }

    private static ApertureWorldData prepareApertureContext(ServerLevel level, ServerPlayer owner) {
        ServerLevel overworldLevel = level.getServer().overworld();
        ApertureWorldData overworldData = ApertureWorldData.get(overworldLevel);
        overworldData.allocateAperture(owner.getUUID());

        ServerLevel apertureLevel = level.getServer().getLevel(DaoMarkDiffusionService.APERTURE_DIMENSION);
        if (apertureLevel == null) {
            apertureLevel = overworldLevel;
        }
        ApertureWorldData worldData = ApertureWorldData.get(apertureLevel);

        ApertureInfo info = worldData.getOrAllocate(owner.getUUID());
        worldData.updateBoundaryByRadius(owner.getUUID(), APERTURE_RADIUS_BLOCKS);
        worldData.setInitializationState(
            owner.getUUID(),
            new ApertureInitializationState(
                ApertureWorldData.InitPhase.COMPLETED,
                null,
                null,
                null,
                AscensionAttemptState.defaultForPhase(ApertureWorldData.InitPhase.COMPLETED),
                null
            )
        );
        worldData.updateTribulationTick(owner.getUUID(), info.nextTribulationTick());
        return worldData;
    }

    private static void setConfirmedTribulationTruth(ServerLevel level, UUID ownerUuid) {
        ApertureWorldData overworldData = ApertureWorldData.get(level.getServer().overworld());
        overworldData.markAttemptStage(ownerUuid, AscensionAttemptStage.CONFIRMED);
        overworldData.updateTribulationTick(ownerUuid, 0L);

        ServerLevel apertureLevel = level.getServer().getLevel(DaoMarkDiffusionService.APERTURE_DIMENSION);
        if (apertureLevel != null && apertureLevel != level.getServer().overworld()) {
            ApertureWorldData apertureData = ApertureWorldData.get(apertureLevel);
            apertureData.markAttemptStage(ownerUuid, AscensionAttemptStage.CONFIRMED);
            apertureData.updateTribulationTick(ownerUuid, 0L);
        }
    }

    private static ServerLevel resolveAuthoritativeLevel(ServerLevel fallbackLevel) {
        ServerLevel apertureLevel = fallbackLevel.getServer().getLevel(DaoMarkDiffusionService.APERTURE_DIMENSION);
        return apertureLevel != null ? apertureLevel : fallbackLevel.getServer().overworld();
    }

    private static ServerPlayer createTestPlayer(ServerLevel level, UUID playerUuid, String playerName) {
        return FakePlayerFactory.get(level, new com.mojang.authlib.GameProfile(playerUuid, playerName));
    }

    private static void clearActiveManagersCache() {
        try {
            Field activeManagersField = TribulationTickHandler.class.getDeclaredField("ACTIVE_MANAGERS");
            activeManagersField.setAccessible(true);
            Object raw = activeManagersField.get(null);
            if (raw instanceof Map<?, ?> rawMap) {
                rawMap.clear();
            }
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("无法清空 ACTIVE_MANAGERS 运行态缓存", exception);
        }
    }

    private static void injectTribulationRuntimeState(
        ApertureWorldData worldData,
        UUID ownerUuid,
        ApertureWorldData.TribulationRuntimeState runtimeState
    ) {
        try {
            Field initializationStatesField = ApertureWorldData.class.getDeclaredField("initializationStates");
            initializationStatesField.setAccessible(true);
            Object raw = initializationStatesField.get(worldData);
            if (raw instanceof Map<?, ?> rawMap) {
                ApertureWorldData.ApertureInitializationState currentState =
                    worldData.getInitializationState(ownerUuid);
                Map typedMap = (Map) rawMap;
                typedMap.put(
                    ownerUuid,
                    new ApertureWorldData.ApertureInitializationState(
                        currentState.initPhase(),
                        currentState.openingSnapshot(),
                        currentState.layoutVersion(),
                        currentState.planSeed(),
                        currentState.attemptState(),
                        runtimeState
                    )
                );
            }
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("无法注入灾劫运行态缓存", exception);
        }
    }
}
