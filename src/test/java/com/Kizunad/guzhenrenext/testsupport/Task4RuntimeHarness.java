package com.Kizunad.guzhenrenext.testsupport;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

public final class Task4RuntimeHarness {

    private static final int MAX_RUNTIME_JAR_SEARCH_DEPTH = 6;
    private static final Path MAIN_CLASSES = Path.of("build/classes/java/main");
    private static final Path MAIN_RESOURCES = Path.of("build/resources/main");
    private static final Path ARTIFACT_MANIFEST =
        Path.of("build/tmp/createMinecraftArtifacts/nfrt_artifact_manifest.properties");
    private static final String NBT_TAG_CLASS_RESOURCE = "net/minecraft/nbt/Tag.class";
    private static Path cachedMinecraftJarPath;

    private Task4RuntimeHarness() {}

    public static RuntimeApi create() throws Exception {
        return RuntimeApi.create();
    }

    public static RuntimeApi createFromContextClassLoader() throws Exception {
        return RuntimeApi.createFromContextClassLoader();
    }

    public static final class RuntimeApi {

        private static final String HOLDER_LOOKUP_PROVIDER_CLASS_NAME =
            "net.minecraft.core.HolderLookup$Provider";
        private static final String COMPOUND_TAG_CLASS_NAME =
            "net.minecraft.nbt.CompoundTag";
        private static final String KONGQIAO_DATA_CLASS_NAME =
            "com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoData";
        private static final String KONGQIAO_PROJECTION_CLASS_NAME =
            "com.Kizunad.guzhenrenext.kongqiao.service.KongqiaoPressureProjection";
        private static final String KONGQIAO_PROJECTION_SERVICE_CLASS_NAME =
            "com.Kizunad.guzhenrenext.kongqiao.service.KongqiaoPressureProjectionService";
        private static final String GU_RUNNING_SERVICE_CLASS_NAME =
            "com.Kizunad.guzhenrenext.kongqiao.service.GuRunningService";
        private static final String I_GU_EFFECT_CLASS_NAME =
            "com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect";
        private static final String SHAZHAO_ACTIVE_SERVICE_CLASS_NAME =
            "com.Kizunad.guzhenrenext.kongqiao.service.ShazhaoActiveService";
        private static final String I_SHAZHAO_ACTIVE_EFFECT_CLASS_NAME =
            "com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoActiveEffect";
        private static final String ACTIVE_PASSIVES_CLASS_NAME =
            "com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives";
        private static final String TWEAK_CONFIG_CLASS_NAME =
            "com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig";
        private static final String TWEAK_CONFIG_UPDATE_PAYLOAD_CLASS_NAME =
            "com.Kizunad.guzhenrenext.kongqiao.network.ServerboundTweakConfigUpdatePayload";
        private static final String KONGQIAO_CAPACITY_PROFILE_CLASS_NAME =
            "com.Kizunad.guzhenrenext.kongqiao.service.KongqiaoCapacityProfile";
        private static final String KONGQIAO_APTITUDE_TIER_CLASS_NAME =
            "com.Kizunad.guzhenrenext.kongqiao.service.KongqiaoAptitudeTier";
        private static final String PASSIVE_RUNTIME_CANDIDATE_CLASS_NAME =
            KONGQIAO_PROJECTION_SERVICE_CLASS_NAME + "$PassiveRuntimeCandidate";
        private static final String PASSIVE_RUNTIME_SNAPSHOT_CLASS_NAME =
            KONGQIAO_PROJECTION_SERVICE_CLASS_NAME + "$PassiveRuntimeSnapshot";
        private static final String KONGQIAO_SYNC_PAYLOAD_CLASS_NAME =
            "com.Kizunad.guzhenrenext.network.ClientboundKongqiaoSyncPayload";
        private static final String KONGQIAO_SYNC_CLIENT_HANDLER_CLASS_NAME =
            "com.Kizunad.guzhenrenext.kongqiao.client.KongqiaoSyncClientHandler";
        private static final String KONGQIAO_CLIENT_CACHE_CLASS_NAME =
            "com.Kizunad.guzhenrenext.kongqiao.client.KongqiaoClientProjectionCache";
        private static final String NIAN_TOU_DATA_CLASS_NAME =
            "com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData";
        private static final String NIAN_TOU_USAGE_CLASS_NAME =
            NIAN_TOU_DATA_CLASS_NAME + "$Usage";
        private static final String SHAZHAO_DATA_CLASS_NAME =
            "com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData";
        private static final String PACKET_SYNC_KONGQIAO_DATA_CLASS_NAME =
            "com.Kizunad.guzhenrenext.kongqiao.network.PacketSyncKongqiaoData";
        private static final String RESOURCE_LOCATION_CLASS_NAME =
            "net.minecraft.resources.ResourceLocation";
        private static final String NIAN_TOU_DATA_MANAGER_CLASS_NAME =
            "com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouDataManager";
        private static final String SHAZHAO_DATA_MANAGER_CLASS_NAME =
            "com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoDataManager";
        private static final String NIAN_TOU_UNLOCKS_CLASS_NAME =
            "com.Kizunad.guzhenrenext.kongqiao.attachment.NianTouUnlocks";
        private static final String KONGQIAO_ATTACHMENTS_CLASS_NAME =
            "com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments";
        private static final String KONGQIAO_ATTACHMENT_EVENTS_CLASS_NAME =
            "com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachmentEvents";
        private static final String FAKE_PLAYER_FACTORY_CLASS_NAME =
            "net.neoforged.neoforge.common.util.FakePlayerFactory";
        private static final String GAME_PROFILE_CLASS_NAME =
            "com.mojang.authlib.GameProfile";
        private static final String SERVER_LEVEL_CLASS_NAME =
            "net.minecraft.server.level.ServerLevel";
        private static final String ENTITY_CLASS_NAME =
            "net.minecraft.world.entity.Entity";
        private static final String SERVER_PLAYER_CLASS_NAME =
            "net.minecraft.server.level.ServerPlayer";
        private static final String SHARED_CONSTANTS_CLASS_NAME =
            "net.minecraft.SharedConstants";
        private static final String BOOTSTRAP_CLASS_NAME =
            "net.minecraft.server.Bootstrap";

        private final Class<?> kongqiaoDataClass;
        private final Class<?> compoundTagClass;
        private final Class<?> projectionClass;
        private final Class<?> activePassivesClass;
        private final Class<?> tweakConfigClass;
        private final Class<?> capacityProfileClass;
        private final Class<?> aptitudeTierClass;
        private final Class<?> passiveRuntimeSnapshotClass;
        private final Class<?> iGuEffectClass;
        private final Class<?> iShazhaoActiveEffectClass;
        private final Class<?> nianTouDataClass;
        private final Class<?> nianTouUsageClass;
        private final Class<?> shazhaoDataClass;
        private final Class<?> nianTouUnlocksClass;
        private final Class<?> unlockProcessClass;
        private final Class<?> packetSyncKongqiaoDataClass;
        private final Class<?> nianTouDataManagerClass;
        private final Class<?> shazhaoDataManagerClass;
        private final Class<?> resourceLocationClass;
        private final Method createBootstrapSafeForTestsMethod;
        private final Method getStabilityStateMethod;
        private final Method isGameplayActivatedMethod;
        private final Method setGameplayActivatedMethod;
        private final Method clearDirtyMethod;
        private final Method isDirtyMethod;
        private final Method serializeDataMethod;
        private final Method deserializeDataMethod;
        private final Method setBurstPressureMethod;
        private final Method getBurstPressureMethod;
        private final Method setFatigueDebtMethod;
        private final Method getFatigueDebtMethod;
        private final Method setOverloadTierMethod;
        private final Method getOverloadTierMethod;
        private final Method setForcedDisabledUsageIdsMethod;
        private final Method getForcedDisabledUsageIdsMethod;
        private final Method setSealedSlotsMethod;
        private final Method getSealedSlotsMethod;
        private final Method setLastDecayGameTimeMethod;
        private final Method getLastDecayGameTimeMethod;
        private final Constructor<?> projectionConstructor;
        private final Constructor<?> activePassivesConstructor;
        private final Constructor<?> tweakConfigConstructor;
        private final Constructor<?> capacityProfileConstructor;
        private final Constructor<?> passiveRuntimeCandidateConstructor;
        private final Method projectionToTagMethod;
        private final Method projectionFromTagMethod;
        private final Method assembleProjectionMethod;
        private final Method activePassivesAddMethod;
        private final Method activePassivesIsActiveMethod;
        private final Method tweakConfigIsPassiveEnabledMethod;
        private final Method computePassivePressureCoreMethod;
        private final Method evaluatePassiveRuntimeSnapshotMethod;
        private final Method syncPassiveRuntimeStateMethod;
        private final Method runPassiveUsageIfAllowedMethod;
        private final Method activateResolvedUsageForTestsMethod;
        private final Method activateResolvedEffectForTestsMethod;
        private final Constructor<?> payloadConstructor;
        private final Method payloadDataMethod;
        private final Method payloadProjectionMethod;
        private final Method applyAuthoritativeStateMethod;
        private final Method clearProjectionCacheMethod;
        private final Method getCurrentProjectionMethod;
        private final Method packetSyncHandleMethod;
        private final Method nianTouManagerClearMethod;
        private final Method shazhaoManagerClearMethod;
        private final Method nianTouManagerGetAllMethod;
        private final Method shazhaoManagerGetAllMethod;
        private final Method nianTouManagerFindUsageLookupMethod;
        private final Method usageLookupUsageMethod;
        private final Method nianTouUsageTitleMethod;
        private final Method nianTouUsageFormattedInfoMethod;
        private final Method shazhaoManagerGetMethod;
        private final Method shazhaoTitleMethod;
        private final Method shazhaoFormattedInfoMethod;
        private final Constructor<?> compoundTagConstructor;
        private final Method putDoubleMethod;
        private final Method putIntMethod;
        private final Method getDoubleMethod;
        private final Method getIntMethod;
        private final Method getAllKeysMethod;
        private final Constructor<?> nianTouUsageConstructor;
        private final Constructor<?> nianTouDataConstructor;
        private final Constructor<?> shazhaoDataConstructor;
        private final Constructor<?> nianTouUnlocksConstructor;
        private final Constructor<?> packetSyncKongqiaoDataConstructor;
        private final Method tweakConfigGetWheelSkillsMethod;
        private final Method tryAddWheelSkillWithOverloadGateMethod;
        private final Method tweakConfigSetPassiveEnabledMethod;
        private final Method tweakConfigSetWheelSkillsMethod;
        private final Method activePassivesRemoveMethod;
        private final Method activePassivesClearMethod;
        private final Method tweakConfigSerializeMethod;
        private final Method tweakConfigDeserializeMethod;
        private final Method activePassivesSerializeMethod;
        private final Method activePassivesDeserializeMethod;
        private final Method unlocksSerializeMethod;
        private final Method unlocksDeserializeMethod;
        private final Method unlocksStartProcessMethod;
        private final Method unlocksGetCurrentProcessMethod;
        private final Method unlocksUnlockMethod;
        private final Method unlocksIsUsageUnlockedMethod;
        private final Method unlocksUnlockShazhaoMethod;
        private final Method unlocksIsShazhaoUnlockedMethod;
        private final Method unlocksSetShazhaoMessageMethod;
        private final Method unlocksGetShazhaoMessageMethod;
        private LazySharedRuntimeBridge lazySharedRuntimeBridge;

        private RuntimeApi(
            final Class<?> providerClass,
            final Class<?> kongqiaoDataClass,
            final Class<?> stabilityStateClass,
            final Class<?> compoundTagClass,
            final Class<?> projectionClass,
            final Class<?> projectionServiceClass,
            final Class<?> payloadClass,
            final Class<?> clientHandlerClass,
            final Class<?> clientCacheClass
        ) throws Exception {
            this.kongqiaoDataClass = kongqiaoDataClass;
            this.compoundTagClass = compoundTagClass;
            this.projectionClass = projectionClass;
            this.activePassivesClass = Class.forName(ACTIVE_PASSIVES_CLASS_NAME, true, providerClass.getClassLoader());
            this.tweakConfigClass = Class.forName(TWEAK_CONFIG_CLASS_NAME, true, providerClass.getClassLoader());
            this.capacityProfileClass = Class.forName(KONGQIAO_CAPACITY_PROFILE_CLASS_NAME, true, providerClass.getClassLoader());
            this.aptitudeTierClass = Class.forName(KONGQIAO_APTITUDE_TIER_CLASS_NAME, true, providerClass.getClassLoader());
            this.iGuEffectClass = Class.forName(I_GU_EFFECT_CLASS_NAME, true, providerClass.getClassLoader());
            this.iShazhaoActiveEffectClass = Class.forName(
                I_SHAZHAO_ACTIVE_EFFECT_CLASS_NAME,
                true,
                providerClass.getClassLoader()
            );
            final Class<?> passiveRuntimeCandidateClass = Class.forName(
                PASSIVE_RUNTIME_CANDIDATE_CLASS_NAME,
                true,
                providerClass.getClassLoader()
            );
            this.passiveRuntimeSnapshotClass = Class.forName(
                PASSIVE_RUNTIME_SNAPSHOT_CLASS_NAME,
                true,
                providerClass.getClassLoader()
            );
            this.nianTouDataClass = Class.forName(NIAN_TOU_DATA_CLASS_NAME, true, providerClass.getClassLoader());
            this.nianTouUsageClass = Class.forName(NIAN_TOU_USAGE_CLASS_NAME, true, providerClass.getClassLoader());
            this.shazhaoDataClass = Class.forName(SHAZHAO_DATA_CLASS_NAME, true, providerClass.getClassLoader());
            this.nianTouUnlocksClass = Class.forName(NIAN_TOU_UNLOCKS_CLASS_NAME, true, providerClass.getClassLoader());
            this.unlockProcessClass = Class.forName(
                NIAN_TOU_UNLOCKS_CLASS_NAME + "$UnlockProcess",
                true,
                providerClass.getClassLoader()
            );
            this.packetSyncKongqiaoDataClass = Class.forName(PACKET_SYNC_KONGQIAO_DATA_CLASS_NAME, true, providerClass.getClassLoader());
            this.resourceLocationClass = Class.forName(RESOURCE_LOCATION_CLASS_NAME, true, providerClass.getClassLoader());
            this.nianTouDataManagerClass = Class.forName(NIAN_TOU_DATA_MANAGER_CLASS_NAME, true, providerClass.getClassLoader());
            this.shazhaoDataManagerClass = Class.forName(SHAZHAO_DATA_MANAGER_CLASS_NAME, true, providerClass.getClassLoader());
            this.createBootstrapSafeForTestsMethod = kongqiaoDataClass.getDeclaredMethod(
                "createBootstrapSafeForTests"
            );
            this.createBootstrapSafeForTestsMethod.setAccessible(true);
            this.getStabilityStateMethod = kongqiaoDataClass.getMethod("getStabilityState");
            this.isGameplayActivatedMethod = kongqiaoDataClass.getMethod(
                "isGameplayActivated"
            );
            this.setGameplayActivatedMethod = kongqiaoDataClass.getMethod(
                "setGameplayActivated",
                boolean.class
            );
            this.clearDirtyMethod = kongqiaoDataClass.getMethod("clearDirty");
            this.isDirtyMethod = kongqiaoDataClass.getMethod("isDirty");
            this.serializeDataMethod = kongqiaoDataClass.getMethod(
                "serializeNBT",
                providerClass
            );
            this.deserializeDataMethod = kongqiaoDataClass.getMethod(
                "deserializeNBT",
                providerClass,
                compoundTagClass
            );
            this.setBurstPressureMethod = stabilityStateClass.getMethod(
                "setBurstPressure",
                double.class
            );
            this.getBurstPressureMethod = stabilityStateClass.getMethod("getBurstPressure");
            this.setFatigueDebtMethod = stabilityStateClass.getMethod(
                "setFatigueDebt",
                double.class
            );
            this.getFatigueDebtMethod = stabilityStateClass.getMethod("getFatigueDebt");
            this.setOverloadTierMethod = stabilityStateClass.getMethod(
                "setOverloadTier",
                int.class
            );
            this.getOverloadTierMethod = stabilityStateClass.getMethod("getOverloadTier");
            this.setForcedDisabledUsageIdsMethod = stabilityStateClass.getMethod(
                "setForcedDisabledUsageIds",
                Set.class
            );
            this.getForcedDisabledUsageIdsMethod = stabilityStateClass.getMethod(
                "getForcedDisabledUsageIds"
            );
            this.setSealedSlotsMethod = stabilityStateClass.getMethod(
                "setSealedSlots",
                Set.class
            );
            this.getSealedSlotsMethod = stabilityStateClass.getMethod("getSealedSlots");
            this.setLastDecayGameTimeMethod = stabilityStateClass.getMethod(
                "setLastDecayGameTime",
                long.class
            );
            this.getLastDecayGameTimeMethod = stabilityStateClass.getMethod(
                "getLastDecayGameTime"
            );
            // 更新为 18 参数构造函数（Task 3 扩展了 6 个容量字段）
            this.projectionConstructor = projectionClass.getDeclaredConstructor(
                double.class,  // totalPressure
                double.class,  // effectivePressure
                double.class,  // pressureCap
                double.class,  // residentPressure
                double.class,  // passivePressure
                double.class,  // wheelReservePressure
                double.class,  // burstPressure
                double.class,  // fatigueDebt
                int.class,     // overloadTier
                String.class,  // blockedReason
                int.class,     // sealedSlotCount
                int.class,     // forcedDisabledCount
                String.class,  // aptitudeTier (Task 3)
                int.class,     // apertureRank (Task 3)
                int.class,     // apertureStage (Task 3)
                int.class,     // baseRows (Task 3)
                int.class,     // bonusRows (Task 3)
                int.class      // totalRows (Task 3)
            );
            this.projectionToTagMethod = projectionClass.getMethod("toTag");
            this.projectionFromTagMethod = projectionClass.getMethod(
                "fromTag",
                compoundTagClass
            );
            this.activePassivesConstructor = activePassivesClass.getConstructor();
            this.tweakConfigConstructor = tweakConfigClass.getConstructor();
            this.capacityProfileConstructor = capacityProfileClass.getDeclaredConstructor(
                aptitudeTierClass,
                int.class,
                int.class,
                int.class,
                int.class,
                int.class,
                double.class,
                Class.forName(
                    "com.Kizunad.guzhenrenext.xianqiao.opening.AscensionConditionSnapshot",
                    true,
                    providerClass.getClassLoader()
                )
            );
            this.passiveRuntimeCandidateConstructor = passiveRuntimeCandidateClass.getDeclaredConstructor(
                String.class,
                int.class
            );
            this.passiveRuntimeCandidateConstructor.setAccessible(true);
            this.activePassivesAddMethod = activePassivesClass.getMethod(
                "add",
                String.class
            );
            this.activePassivesRemoveMethod = activePassivesClass.getMethod(
                "remove",
                String.class
            );
            this.activePassivesClearMethod = activePassivesClass.getMethod("clear");
            this.activePassivesIsActiveMethod = activePassivesClass.getMethod(
                "isActive",
                String.class
            );
            this.tweakConfigIsPassiveEnabledMethod = tweakConfigClass.getMethod(
                "isPassiveEnabled",
                String.class
            );
            this.tweakConfigSetPassiveEnabledMethod = tweakConfigClass.getMethod(
                "setPassiveEnabled",
                String.class,
                boolean.class
            );
            this.tweakConfigSetWheelSkillsMethod = tweakConfigClass.getMethod(
                "setWheelSkills",
                List.class
            );
            this.tweakConfigGetWheelSkillsMethod = tweakConfigClass.getMethod(
                "getWheelSkills"
            );
            this.tweakConfigSerializeMethod = tweakConfigClass.getMethod(
                "serializeNBT",
                providerClass
            );
            this.tweakConfigDeserializeMethod = tweakConfigClass.getMethod(
                "deserializeNBT",
                providerClass,
                compoundTagClass
            );
            this.activePassivesSerializeMethod = activePassivesClass.getMethod(
                "serializeNBT",
                providerClass
            );
            this.activePassivesDeserializeMethod = activePassivesClass.getMethod(
                "deserializeNBT",
                providerClass,
                compoundTagClass
            );
            this.computePassivePressureCoreMethod = projectionServiceClass.getDeclaredMethod(
                "computePassivePressureCore",
                nianTouUsageClass
            );
            this.computePassivePressureCoreMethod.setAccessible(true);
            this.evaluatePassiveRuntimeSnapshotMethod = projectionServiceClass.getDeclaredMethod(
                "evaluatePassiveRuntimeSnapshot",
                java.util.Collection.class,
                capacityProfileClass,
                double.class,
                double.class
            );
            this.evaluatePassiveRuntimeSnapshotMethod.setAccessible(true);
            final Class<?> guRunningServiceClass = Class.forName(
                GU_RUNNING_SERVICE_CLASS_NAME,
                true,
                providerClass.getClassLoader()
            );
            this.syncPassiveRuntimeStateMethod = guRunningServiceClass.getDeclaredMethod(
                "syncPassiveRuntimeState",
                activePassivesClass,
                stabilityStateClass,
                passiveRuntimeSnapshotClass
            );
            this.syncPassiveRuntimeStateMethod.setAccessible(true);
            Method foundRunPassiveUsageIfAllowedMethod = null;
            for (Method method : guRunningServiceClass.getDeclaredMethods()) {
                if (
                    method.getName().equals("runPassiveUsageIfAllowed")
                        && method.getParameterCount() == 7
                ) {
                    foundRunPassiveUsageIfAllowedMethod = method;
                    break;
                }
            }
            if (foundRunPassiveUsageIfAllowedMethod == null) {
                throw new NoSuchMethodException(
                    "无法定位 GuRunningService.runPassiveUsageIfAllowed 方法"
                );
            }
            this.runPassiveUsageIfAllowedMethod = foundRunPassiveUsageIfAllowedMethod;
            this.runPassiveUsageIfAllowedMethod.setAccessible(true);
            this.activateResolvedUsageForTestsMethod = guRunningServiceClass.getDeclaredMethod(
                "activateResolvedUsageForTests",
                nianTouUsageClass,
                iGuEffectClass,
                double.class,
                double.class
            );
            this.activateResolvedUsageForTestsMethod.setAccessible(true);
            final Class<?> shazhaoActiveServiceClass = Class.forName(
                SHAZHAO_ACTIVE_SERVICE_CLASS_NAME,
                true,
                providerClass.getClassLoader()
            );
            this.activateResolvedEffectForTestsMethod = shazhaoActiveServiceClass.getDeclaredMethod(
                "activateResolvedEffectForTests",
                shazhaoDataClass,
                iShazhaoActiveEffectClass,
                stabilityStateClass,
                double.class,
                double.class
            );
            this.activateResolvedEffectForTestsMethod.setAccessible(true);
            final Class<?> tweakConfigUpdatePayloadClass = Class.forName(
                TWEAK_CONFIG_UPDATE_PAYLOAD_CLASS_NAME,
                true,
                providerClass.getClassLoader()
            );
            this.tryAddWheelSkillWithOverloadGateMethod =
                tweakConfigUpdatePayloadClass.getDeclaredMethod(
                    "tryAddWheelSkillWithOverloadGate",
                    tweakConfigClass,
                    String.class,
                    int.class,
                    int.class
                );
            this.tryAddWheelSkillWithOverloadGateMethod.setAccessible(true);
            // 保留单参数版本用于向后兼容（已 deprecated）
            this.assembleProjectionMethod = projectionServiceClass.getMethod(
                "assembleProjection",
                kongqiaoDataClass
            );
            this.payloadConstructor = payloadClass.getDeclaredConstructor(
                compoundTagClass,
                compoundTagClass
            );
            this.payloadDataMethod = payloadClass.getMethod("data");
            this.payloadProjectionMethod = payloadClass.getMethod("projection");
            this.applyAuthoritativeStateMethod = clientHandlerClass.getDeclaredMethod(
                "applyAuthoritativeState",
                kongqiaoDataClass,
                providerClass,
                compoundTagClass,
                projectionClass
            );
            this.applyAuthoritativeStateMethod.setAccessible(true);
            this.clearProjectionCacheMethod = clientCacheClass.getMethod("clear");
            this.getCurrentProjectionMethod = clientCacheClass.getMethod(
                "getCurrentProjection"
            );
            Method foundHandleMethod = null;
            for (Method method : packetSyncKongqiaoDataClass.getDeclaredMethods()) {
                if (method.getName().equals("handle") && method.getParameterCount() == 2) {
                    foundHandleMethod = method;
                    break;
                }
            }
            if (foundHandleMethod == null) {
                throw new NoSuchMethodException("无法定位 PacketSyncKongqiaoData.handle 方法");
            }
            this.packetSyncHandleMethod = foundHandleMethod;
            this.nianTouManagerClearMethod = nianTouDataManagerClass.getMethod("clear");
            this.shazhaoManagerClearMethod = shazhaoDataManagerClass.getMethod("clear");
            this.nianTouManagerGetAllMethod = nianTouDataManagerClass.getMethod("getAll");
            this.shazhaoManagerGetAllMethod = shazhaoDataManagerClass.getMethod("getAll");
            this.nianTouManagerFindUsageLookupMethod = nianTouDataManagerClass.getMethod(
                "findUsageLookup",
                String.class
            );
            // 修复：UsageLookup 实际定义在 NianTouDataManager 中，而非 NianTouData
            final Class<?> usageLookupClass = Class.forName(
                NIAN_TOU_DATA_MANAGER_CLASS_NAME + "$UsageLookup",
                true,
                providerClass.getClassLoader()
            );
            this.usageLookupUsageMethod = usageLookupClass.getMethod("usage");
            this.nianTouUsageTitleMethod = nianTouUsageClass.getMethod("usageTitle");
            this.nianTouUsageFormattedInfoMethod = nianTouUsageClass.getMethod("getFormattedInfo");
            this.shazhaoManagerGetMethod = shazhaoDataManagerClass.getMethod("get", resourceLocationClass);
            this.shazhaoTitleMethod = shazhaoDataClass.getMethod("title");
            this.shazhaoFormattedInfoMethod = shazhaoDataClass.getMethod("getFormattedInfo");
            this.compoundTagConstructor = compoundTagClass.getConstructor();
            this.putDoubleMethod = compoundTagClass.getMethod(
                "putDouble",
                String.class,
                double.class
            );
            this.putIntMethod = compoundTagClass.getMethod(
                "putInt",
                String.class,
                int.class
            );
            this.getDoubleMethod = compoundTagClass.getMethod("getDouble", String.class);
            this.getIntMethod = compoundTagClass.getMethod("getInt", String.class);
            this.getAllKeysMethod = compoundTagClass.getMethod("getAllKeys");
            this.nianTouUsageConstructor = nianTouUsageClass.getDeclaredConstructor(
                String.class,
                String.class,
                String.class,
                String.class,
                int.class,
                int.class,
                Map.class
            );
            this.nianTouDataConstructor = nianTouDataClass.getDeclaredConstructor(String.class, List.class);
            this.shazhaoDataConstructor = shazhaoDataClass.getDeclaredConstructor(
                String.class,
                String.class,
                String.class,
                String.class,
                int.class,
                List.class,
                Map.class
            );
            this.nianTouUnlocksConstructor = nianTouUnlocksClass.getDeclaredConstructor();
            this.unlocksStartProcessMethod = nianTouUnlocksClass.getMethod(
                "startProcess",
                resourceLocationClass,
                String.class,
                int.class,
                int.class
            );
            this.unlocksGetCurrentProcessMethod = nianTouUnlocksClass.getMethod(
                "getCurrentProcess"
            );
            this.unlocksUnlockMethod = nianTouUnlocksClass.getMethod(
                "unlock",
                resourceLocationClass,
                String.class
            );
            this.unlocksIsUsageUnlockedMethod = nianTouUnlocksClass.getMethod(
                "isUsageUnlocked",
                resourceLocationClass,
                String.class
            );
            this.unlocksUnlockShazhaoMethod = nianTouUnlocksClass.getMethod(
                "unlockShazhao",
                resourceLocationClass
            );
            this.unlocksIsShazhaoUnlockedMethod = nianTouUnlocksClass.getMethod(
                "isShazhaoUnlocked",
                resourceLocationClass
            );
            this.unlocksSetShazhaoMessageMethod = nianTouUnlocksClass.getMethod(
                "setShazhaoMessage",
                String.class
            );
            this.unlocksGetShazhaoMessageMethod = nianTouUnlocksClass.getMethod(
                "getShazhaoMessage"
            );
            this.unlocksSerializeMethod = nianTouUnlocksClass.getMethod(
                "serializeNBT",
                providerClass
            );
            this.unlocksDeserializeMethod = nianTouUnlocksClass.getMethod(
                "deserializeNBT",
                providerClass,
                compoundTagClass
            );
            this.packetSyncKongqiaoDataConstructor = packetSyncKongqiaoDataClass.getDeclaredConstructor(
                List.class,
                List.class
            );
        }

        public static RuntimeApi create() throws Exception {
            final URLClassLoader loader = buildRuntimeClassLoader();
            final Class<?> providerClass = Class.forName(
                HOLDER_LOOKUP_PROVIDER_CLASS_NAME,
                true,
                loader
            );
            final Class<?> kongqiaoDataClass = Class.forName(
                KONGQIAO_DATA_CLASS_NAME,
                true,
                loader
            );
            final Class<?> stabilityStateClass = Class.forName(
                KONGQIAO_DATA_CLASS_NAME + "$StabilityState",
                true,
                loader
            );
            final Class<?> compoundTagClass = Class.forName(
                COMPOUND_TAG_CLASS_NAME,
                true,
                loader
            );
            final Class<?> projectionClass = Class.forName(
                KONGQIAO_PROJECTION_CLASS_NAME,
                true,
                loader
            );
            final Class<?> projectionServiceClass = Class.forName(
                KONGQIAO_PROJECTION_SERVICE_CLASS_NAME,
                true,
                loader
            );
            final Class<?> payloadClass = Class.forName(
                KONGQIAO_SYNC_PAYLOAD_CLASS_NAME,
                true,
                loader
            );
            final Class<?> clientHandlerClass = Class.forName(
                KONGQIAO_SYNC_CLIENT_HANDLER_CLASS_NAME,
                true,
                loader
            );
            final Class<?> clientCacheClass = Class.forName(
                KONGQIAO_CLIENT_CACHE_CLASS_NAME,
                true,
                loader
            );
            return new RuntimeApi(
                providerClass,
                kongqiaoDataClass,
                stabilityStateClass,
                compoundTagClass,
                projectionClass,
                projectionServiceClass,
                payloadClass,
                clientHandlerClass,
                clientCacheClass
            );
        }

        public static RuntimeApi createFromContextClassLoader() throws Exception {
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            final Class<?> providerClass = Class.forName(
                HOLDER_LOOKUP_PROVIDER_CLASS_NAME,
                true,
                loader
            );
            final Class<?> kongqiaoDataClass = Class.forName(
                KONGQIAO_DATA_CLASS_NAME,
                true,
                loader
            );
            final Class<?> stabilityStateClass = Class.forName(
                KONGQIAO_DATA_CLASS_NAME + "$StabilityState",
                true,
                loader
            );
            final Class<?> compoundTagClass = Class.forName(
                COMPOUND_TAG_CLASS_NAME,
                true,
                loader
            );
            final Class<?> projectionClass = Class.forName(
                KONGQIAO_PROJECTION_CLASS_NAME,
                true,
                loader
            );
            final Class<?> projectionServiceClass = Class.forName(
                KONGQIAO_PROJECTION_SERVICE_CLASS_NAME,
                true,
                loader
            );
            final Class<?> payloadClass = Class.forName(
                KONGQIAO_SYNC_PAYLOAD_CLASS_NAME,
                true,
                loader
            );
            final Class<?> clientHandlerClass = Class.forName(
                KONGQIAO_SYNC_CLIENT_HANDLER_CLASS_NAME,
                true,
                loader
            );
            final Class<?> clientCacheClass = Class.forName(
                KONGQIAO_CLIENT_CACHE_CLASS_NAME,
                true,
                loader
            );
            return new RuntimeApi(
                providerClass,
                kongqiaoDataClass,
                stabilityStateClass,
                compoundTagClass,
                projectionClass,
                projectionServiceClass,
                payloadClass,
                clientHandlerClass,
                clientCacheClass
            );
        }

        public Object newKongqiaoData() throws Exception {
            return createBootstrapSafeForTestsMethod.invoke(null);
        }

        public RuntimeGraph newRuntimeGraph() throws Exception {
            return new RuntimeGraph(
                newKongqiaoData(),
                nianTouUnlocksConstructor.newInstance(),
                newTweakConfig(),
                newActivePassives()
            );
        }

        public SharedRuntimeFixture newSharedRuntimeFixtureOnLiveLevel(final Object liveServerLevel)
            throws Exception {
            if (liveServerLevel == null) {
                throw new IllegalArgumentException("liveServerLevel 不能为空");
            }
            final LazySharedRuntimeBridge bridge = lazySharedRuntimeBridge();
            return bridge.createFixture(liveServerLevel);
        }

        private LazySharedRuntimeBridge lazySharedRuntimeBridge() throws Exception {
            if (lazySharedRuntimeBridge != null) {
                return lazySharedRuntimeBridge;
            }
            final ClassLoader loader = kongqiaoDataClass.getClassLoader();
            lazySharedRuntimeBridge = new LazySharedRuntimeBridge(loader);
            return lazySharedRuntimeBridge;
        }

        public RuntimeGraph cloneRuntimeGraph(final RuntimeGraph source) throws Exception {
            final RuntimeGraph cloned = newRuntimeGraph();
            deserializeData(cloned.data(), serializeData(source.data()));
            unlocksDeserializeMethod.invoke(
                cloned.unlocks(),
                new Object[] {
                    null,
                    unlocksSerializeMethod.invoke(source.unlocks(), new Object[] {null}),
                }
            );
            tweakConfigDeserializeMethod.invoke(
                cloned.tweakConfig(),
                new Object[] {
                    null,
                    tweakConfigSerializeMethod.invoke(source.tweakConfig(), new Object[] {null}),
                }
            );
            activePassivesDeserializeMethod.invoke(
                cloned.activePassives(),
                new Object[] {
                    null,
                    activePassivesSerializeMethod.invoke(
                        source.activePassives(),
                        new Object[] {null}
                    ),
                }
            );
            return cloned;
        }

        public void clearDirty(final Object data) throws Exception {
            clearDirtyMethod.invoke(data);
        }

        public boolean isDirty(final Object data) throws Exception {
            return (boolean) isDirtyMethod.invoke(data);
        }

        public Object getStabilityState(final Object data) throws Exception {
            return getStabilityStateMethod.invoke(data);
        }

        public boolean isGameplayActivated(final Object data) throws Exception {
            return (boolean) isGameplayActivatedMethod.invoke(data);
        }

        public void setGameplayActivated(final Object data, final boolean activated)
            throws Exception {
            setGameplayActivatedMethod.invoke(data, activated);
        }

        public void setBurstPressure(final Object stabilityState, final double value)
            throws Exception {
            setBurstPressureMethod.invoke(stabilityState, value);
        }

        public double getBurstPressure(final Object stabilityState) throws Exception {
            return (double) getBurstPressureMethod.invoke(stabilityState);
        }

        public void setFatigueDebt(final Object stabilityState, final double value)
            throws Exception {
            setFatigueDebtMethod.invoke(stabilityState, value);
        }

        public double getFatigueDebt(final Object stabilityState) throws Exception {
            return (double) getFatigueDebtMethod.invoke(stabilityState);
        }

        public void setOverloadTier(final Object stabilityState, final int value)
            throws Exception {
            setOverloadTierMethod.invoke(stabilityState, value);
        }

        public int getOverloadTier(final Object stabilityState) throws Exception {
            return (int) getOverloadTierMethod.invoke(stabilityState);
        }

        public void setForcedDisabledUsageIds(
            final Object stabilityState,
            final Set<String> usageIds
        ) throws Exception {
            setForcedDisabledUsageIdsMethod.invoke(stabilityState, usageIds);
        }

        public Set<String> getForcedDisabledUsageIds(final Object stabilityState)
            throws Exception {
            return (Set<String>) getForcedDisabledUsageIdsMethod.invoke(stabilityState);
        }

        public void setSealedSlots(final Object stabilityState, final Set<Integer> sealedSlots)
            throws Exception {
            setSealedSlotsMethod.invoke(stabilityState, sealedSlots);
        }

        public Set<Integer> getSealedSlots(final Object stabilityState) throws Exception {
            return (Set<Integer>) getSealedSlotsMethod.invoke(stabilityState);
        }

        public void setLastDecayGameTime(final Object stabilityState, final long value)
            throws Exception {
            setLastDecayGameTimeMethod.invoke(stabilityState, value);
        }

        public long getLastDecayGameTime(final Object stabilityState) throws Exception {
            return (long) getLastDecayGameTimeMethod.invoke(stabilityState);
        }

        public Object serializeData(final Object data) throws Exception {
            return serializeDataMethod.invoke(data, new Object[] {null});
        }

        public void deserializeData(final Object data, final Object tag) throws Exception {
            deserializeDataMethod.invoke(data, new Object[] {null, tag});
        }

        public Object newCompoundTag() throws Exception {
            return compoundTagConstructor.newInstance();
        }

        public void putDouble(final Object tag, final String key, final double value)
            throws Exception {
            putDoubleMethod.invoke(tag, key, value);
        }

        public void putInt(final Object tag, final String key, final int value)
            throws Exception {
            putIntMethod.invoke(tag, key, value);
        }

        public double getDouble(final Object tag, final String key) throws Exception {
            return (double) getDoubleMethod.invoke(tag, key);
        }

        public int getInt(final Object tag, final String key) throws Exception {
            return (int) getIntMethod.invoke(tag, key);
        }

        public Set<String> getAllKeys(final Object tag) throws Exception {
            return (Set<String>) getAllKeysMethod.invoke(tag);
        }

        // 更新为 18 参数版本（Task 3 扩展了 6 个容量字段）
        public Object newProjection(
            final double totalPressure,
            final double effectivePressure,
            final double pressureCap,
            final double residentPressure,
            final double passivePressure,
            final double wheelReservePressure,
            final double burstPressure,
            final double fatigueDebt,
            final int overloadTier,
            final String blockedReason,
            final int sealedSlotCount,
            final int forcedDisabledCount,
            // Task 3 新增的 6 个容量字段
            final String aptitudeTier,
            final int apertureRank,
            final int apertureStage,
            final int baseRows,
            final int bonusRows,
            final int totalRows
        ) throws Exception {
            return projectionConstructor.newInstance(
                totalPressure,
                effectivePressure,
                pressureCap,
                residentPressure,
                passivePressure,
                wheelReservePressure,
                burstPressure,
                fatigueDebt,
                overloadTier,
                blockedReason,
                sealedSlotCount,
                forcedDisabledCount,
                aptitudeTier,
                apertureRank,
                apertureStage,
                baseRows,
                bonusRows,
                totalRows
            );
        }

        public Object newActivePassives() throws Exception {
            return activePassivesConstructor.newInstance();
        }

        public void addActivePassive(final Object actives, final String usageId)
            throws Exception {
            activePassivesAddMethod.invoke(actives, usageId);
        }

        public boolean isActivePassive(final Object actives, final String usageId)
            throws Exception {
            return (boolean) activePassivesIsActiveMethod.invoke(actives, usageId);
        }

        public void removeActivePassive(final Object actives, final String usageId)
            throws Exception {
            activePassivesRemoveMethod.invoke(actives, usageId);
        }

        public void clearActivePassives(final Object actives) throws Exception {
            activePassivesClearMethod.invoke(actives);
        }

        public Object newTweakConfig() throws Exception {
            return tweakConfigConstructor.newInstance();
        }

        public boolean isPassiveEnabled(final Object config, final String usageId)
            throws Exception {
            return (boolean) tweakConfigIsPassiveEnabledMethod.invoke(config, usageId);
        }

        public void setPassiveEnabled(
            final Object config,
            final String usageId,
            final boolean enabled
        ) throws Exception {
            tweakConfigSetPassiveEnabledMethod.invoke(config, usageId, enabled);
        }

        public void setWheelSkills(final Object config, final List<String> wheelSkills)
            throws Exception {
            tweakConfigSetWheelSkillsMethod.invoke(config, wheelSkills);
        }

        public Object newUnlocks() throws Exception {
            return nianTouUnlocksConstructor.newInstance();
        }

        public void unlockUsage(
            final Object unlocks,
            final String itemNamespace,
            final String itemPath,
            final String usageId
        ) throws Exception {
            unlocksUnlockMethod.invoke(
                unlocks,
                newResourceLocation(itemNamespace, itemPath),
                usageId
            );
        }

        public boolean isUsageUnlocked(
            final Object unlocks,
            final String itemNamespace,
            final String itemPath,
            final String usageId
        ) throws Exception {
            return (boolean) unlocksIsUsageUnlockedMethod.invoke(
                unlocks,
                newResourceLocation(itemNamespace, itemPath),
                usageId
            );
        }

        public void startUnlockProcess(
            final Object unlocks,
            final String itemNamespace,
            final String itemPath,
            final String usageId,
            final int totalTicks,
            final int totalCost
        ) throws Exception {
            unlocksStartProcessMethod.invoke(
                unlocks,
                newResourceLocation(itemNamespace, itemPath),
                usageId,
                totalTicks,
                totalCost
            );
        }

        public int currentProcessRemainingTicks(final Object unlocks) throws Exception {
            final Object process = unlocksGetCurrentProcessMethod.invoke(unlocks);
            if (process == null) {
                return 0;
            }
            return unlockProcessClass.getField("remainingTicks").getInt(process);
        }

        public void unlockShazhao(
            final Object unlocks,
            final String shazhaoNamespace,
            final String shazhaoPath
        ) throws Exception {
            unlocksUnlockShazhaoMethod.invoke(
                unlocks,
                newResourceLocation(shazhaoNamespace, shazhaoPath)
            );
        }

        public boolean isShazhaoUnlocked(
            final Object unlocks,
            final String shazhaoNamespace,
            final String shazhaoPath
        ) throws Exception {
            return (boolean) unlocksIsShazhaoUnlockedMethod.invoke(
                unlocks,
                newResourceLocation(shazhaoNamespace, shazhaoPath)
            );
        }

        public void setShazhaoMessage(final Object unlocks, final String message)
            throws Exception {
            unlocksSetShazhaoMessageMethod.invoke(unlocks, message);
        }

        public String getShazhaoMessage(final Object unlocks) throws Exception {
            return (String) unlocksGetShazhaoMessageMethod.invoke(unlocks);
        }

        public List<String> wheelSkills(final Object config) throws Exception {
            return (List<String>) tweakConfigGetWheelSkillsMethod.invoke(config);
        }

        public Object newCapacityProfile(
            final String aptitudeTierName,
            final int apertureRank,
            final int apertureStage,
            final int baseRows,
            final int bonusRows,
            final int totalRows,
            final double maxZhenyuan
        ) throws Exception {
            final Object aptitudeTier = resolveEnumConstant(
                aptitudeTierClass,
                aptitudeTierName
            );
            return capacityProfileConstructor.newInstance(
                aptitudeTier,
                apertureRank,
                apertureStage,
                baseRows,
                bonusRows,
                totalRows,
                maxZhenyuan,
                null
            );
        }

        public int computePassivePressureCore(final Object usage) throws Exception {
            return (int) computePassivePressureCoreMethod.invoke(null, usage);
        }

        public Object newPassiveRuntimeCandidate(
            final String usageId,
            final int passivePressure
        ) throws Exception {
            return passiveRuntimeCandidateConstructor.newInstance(
                usageId,
                passivePressure
            );
        }

        public Object evaluatePassiveRuntimeSnapshot(
            final java.util.Collection<?> candidates,
            final Object capacityProfile,
            final double burstPressure,
            final double fatigueDebt
        ) throws Exception {
            return evaluatePassiveRuntimeSnapshotMethod.invoke(
                null,
                candidates,
                capacityProfile,
                burstPressure,
                fatigueDebt
            );
        }

        public double passiveRuntimeSnapshotDouble(
            final Object snapshot,
            final String accessorName
        ) throws Exception {
            final Method accessor = passiveRuntimeSnapshotClass.getDeclaredMethod(
                accessorName
            );
            accessor.setAccessible(true);
            return (double) accessor.invoke(snapshot);
        }

        public int passiveRuntimeSnapshotInt(
            final Object snapshot,
            final String accessorName
        ) throws Exception {
            final Method accessor = passiveRuntimeSnapshotClass.getDeclaredMethod(
                accessorName
            );
            accessor.setAccessible(true);
            return (int) accessor.invoke(snapshot);
        }

        public String passiveRuntimeSnapshotString(
            final Object snapshot,
            final String accessorName
        ) throws Exception {
            final Method accessor = passiveRuntimeSnapshotClass.getDeclaredMethod(
                accessorName
            );
            accessor.setAccessible(true);
            return (String) accessor.invoke(snapshot);
        }

        public Set<?> passiveRuntimeSnapshotSet(
            final Object snapshot,
            final String accessorName
        ) throws Exception {
            final Method accessor = passiveRuntimeSnapshotClass.getDeclaredMethod(
                accessorName
            );
            accessor.setAccessible(true);
            return (Set<?>) accessor.invoke(snapshot);
        }

        public void syncPassiveRuntimeState(
            final Object actives,
            final Object stabilityState,
            final Object snapshot
        ) throws Exception {
            syncPassiveRuntimeStateMethod.invoke(
                null,
                actives,
                stabilityState,
                snapshot
            );
        }

        public PassiveEffectSpy newPassiveEffectSpy(final String usageId) {
            final InvocationHandler handler = new PassiveEffectSpy(usageId);
            final Object proxy = Proxy.newProxyInstance(
                iGuEffectClass.getClassLoader(),
                new Class<?>[] {iGuEffectClass},
                handler
            );
            return ((PassiveEffectSpy) handler).bind(proxy);
        }

        public ShazhaoActiveEffectSpy newShazhaoActiveEffectSpy(
            final String shazhaoId,
            final boolean activateResult
        ) {
            final InvocationHandler handler = new ShazhaoActiveEffectSpy(
                shazhaoId,
                activateResult
            );
            final Object proxy = Proxy.newProxyInstance(
                iShazhaoActiveEffectClass.getClassLoader(),
                new Class<?>[] {iShazhaoActiveEffectClass},
                handler
            );
            return ((ShazhaoActiveEffectSpy) handler).bind(proxy);
        }

        public boolean runPassiveUsageIfAllowed(
            final Object actives,
            final Object usage,
            final Object effect,
            final Object snapshot,
            final boolean isSecond
        ) throws Exception {
            return (boolean) runPassiveUsageIfAllowedMethod.invoke(
                null,
                actives,
                null,
                null,
                usage,
                effect,
                snapshot,
                isSecond
            );
        }

        public Object activateResolvedUsageForTests(
            final Object usage,
            final Object effect,
            final double currentEffectivePressure,
            final double pressureCap
        ) throws Exception {
            return activateResolvedUsageForTestsMethod.invoke(
                null,
                usage,
                effect,
                currentEffectivePressure,
                pressureCap
            );
        }

        public Object activateResolvedEffectForTests(
            final Object shazhaoData,
            final Object effect,
            final Object stabilityState,
            final double currentEffectivePressure,
            final double pressureCap
        ) throws Exception {
            return activateResolvedEffectForTestsMethod.invoke(
                null,
                shazhaoData,
                effect,
                stabilityState,
                currentEffectivePressure,
                pressureCap
            );
        }

        public boolean tryAddWheelSkillWithOverloadGate(
            final Object config,
            final String usageId,
            final int overloadTier,
            final int maxSize
        ) throws Exception {
            return (boolean) tryAddWheelSkillWithOverloadGateMethod.invoke(
                null,
                config,
                usageId,
                overloadTier,
                maxSize
            );
        }

        public boolean activationResultSuccess(final Object result) throws Exception {
            final Method accessor = result.getClass().getDeclaredMethod("success");
            accessor.setAccessible(true);
            return (boolean) accessor.invoke(result);
        }

        public String activationResultFailureReasonName(final Object result)
            throws Exception {
            final Method accessor = result.getClass().getDeclaredMethod(
                "failureReason"
            );
            accessor.setAccessible(true);
            final Object reason = accessor.invoke(result);
            return reason == null ? null : ((Enum<?>) reason).name();
        }

        public Object projectionToTag(final Object projection) throws Exception {
            return projectionToTagMethod.invoke(projection);
        }

        public Object projectionFromTag(final Object tag) throws Exception {
            return projectionFromTagMethod.invoke(null, tag);
        }

        public double projectionDouble(final Object projection, final String accessorName)
            throws Exception {
            return (double) projectionClass.getMethod(accessorName).invoke(projection);
        }

        public int projectionInt(final Object projection, final String accessorName)
            throws Exception {
            return (int) projectionClass.getMethod(accessorName).invoke(projection);
        }

        public String projectionString(final Object projection, final String accessorName)
            throws Exception {
            return (String) projectionClass.getMethod(accessorName).invoke(projection);
        }

        public boolean objectsEqual(final Object left, final Object right) {
            return left == null ? right == null : left.equals(right);
        }

        public Object assembleProjection(final Object data) throws Exception {
            return assembleProjectionMethod.invoke(null, data);
        }

        public Object newPayload(final Object rawData, final Object projectionTag)
            throws Exception {
            return payloadConstructor.newInstance(rawData, projectionTag);
        }

        public Object payloadData(final Object payload) throws Exception {
            return payloadDataMethod.invoke(payload);
        }

        public Object payloadProjection(final Object payload) throws Exception {
            return payloadProjectionMethod.invoke(payload);
        }

        public void clearProjectionCache() throws Exception {
            clearProjectionCacheMethod.invoke(null);
        }

        public void clearKongqiaoDefinitionCaches() throws Exception {
            nianTouManagerClearMethod.invoke(null);
            shazhaoManagerClearMethod.invoke(null);
        }

        public int nianTouDefinitionCount() throws Exception {
            return ((List<?>) nianTouManagerGetAllMethod.invoke(null)).size();
        }

        public int shazhaoDefinitionCount() throws Exception {
            return ((List<?>) shazhaoManagerGetAllMethod.invoke(null)).size();
        }

        public Object newNianTouUsage(
            final String usageId,
            final String usageTitle,
            final String usageDesc,
            final String usageInfo,
            final int costDuration,
            final int costTotalNiantou,
            final Map<String, String> metadata
        ) throws Exception {
            return nianTouUsageConstructor.newInstance(
                usageId,
                usageTitle,
                usageDesc,
                usageInfo,
                costDuration,
                costTotalNiantou,
                metadata
            );
        }

        public Object newNianTouData(final String itemId, final List<?> usages) throws Exception {
            return nianTouDataConstructor.newInstance(itemId, usages);
        }

        public Object newShazhaoData(
            final String shazhaoId,
            final String title,
            final String desc,
            final String info,
            final int costTotalNiantou,
            final List<String> requiredItems,
            final Map<String, String> metadata
        ) throws Exception {
            return shazhaoDataConstructor.newInstance(
                shazhaoId,
                title,
                desc,
                info,
                costTotalNiantou,
                requiredItems,
                metadata
            );
        }

        public Object newPacketSyncKongqiaoData(final List<?> nianTouData, final List<?> shazhaoData)
            throws Exception {
            return packetSyncKongqiaoDataConstructor.newInstance(nianTouData, shazhaoData);
        }

        public Object newResourceLocation(final String namespace, final String path)
            throws Exception {
            return resourceLocationClass
                .getMethod("fromNamespaceAndPath", String.class, String.class)
                .invoke(null, namespace, path);
        }

        public String findUsageTitle(final String usageId) throws Exception {
            final Object lookup = nianTouManagerFindUsageLookupMethod.invoke(null, usageId);
            if (lookup == null) {
                return null;
            }
            final Object usage = usageLookupUsageMethod.invoke(lookup);
            return usage == null ? null : (String) nianTouUsageTitleMethod.invoke(usage);
        }

        public String findUsageFormattedInfo(final String usageId) throws Exception {
            final Object lookup = nianTouManagerFindUsageLookupMethod.invoke(null, usageId);
            if (lookup == null) {
                return null;
            }
            final Object usage = usageLookupUsageMethod.invoke(lookup);
            return usage == null ? null : (String) nianTouUsageFormattedInfoMethod.invoke(usage);
        }

        public String getShazhaoTitle(final Object shazhaoId) throws Exception {
            final Object data = shazhaoManagerGetMethod.invoke(null, shazhaoId);
            return data == null ? null : (String) shazhaoTitleMethod.invoke(data);
        }

        public String getShazhaoFormattedInfo(final Object shazhaoId) throws Exception {
            final Object data = shazhaoManagerGetMethod.invoke(null, shazhaoId);
            return data == null ? null : (String) shazhaoFormattedInfoMethod.invoke(data);
        }

        public Object currentProjection() throws Exception {
            return getCurrentProjectionMethod.invoke(null);
        }

        public void applyAuthoritativeState(
            final Object data,
            final Object rawData,
            final Object projection
        ) throws Exception {
            applyAuthoritativeStateMethod.invoke(
                null,
                data,
                null,
                rawData,
                projection
            );
        }

        private static Object resolveEnumConstant(
            final Class<?> enumClass,
            final String constantName
        ) {
            for (Object constant : enumClass.getEnumConstants()) {
                if (((Enum<?>) constant).name().equals(constantName)) {
                    return constant;
                }
            }
            throw new IllegalArgumentException(
                "无法定位枚举常量: " + enumClass.getName() + "." + constantName
            );
        }

        public static final class PassiveEffectSpy implements InvocationHandler {

            private final String usageId;
            private Object proxy;
            private int onActivateCalls;
            private int onTickCalls;
            private int onSecondCalls;
            private int onUnequipCalls;

            private PassiveEffectSpy(final String usageId) {
                this.usageId = usageId;
            }

            private PassiveEffectSpy bind(final Object proxy) {
                this.proxy = proxy;
                return this;
            }

            public Object proxy() {
                return proxy;
            }

            public int onTickCalls() {
                return onTickCalls;
            }

            public int onActivateCalls() {
                return onActivateCalls;
            }

            public int onSecondCalls() {
                return onSecondCalls;
            }

            public int onUnequipCalls() {
                return onUnequipCalls;
            }

            @Override
            public Object invoke(
                final Object proxy,
                final Method method,
                final Object[] args
            ) {
                final String name = method.getName();
                if ("getUsageId".equals(name)) {
                    return usageId;
                }
                if ("onTick".equals(name)) {
                    onTickCalls++;
                    return null;
                }
                if ("onSecond".equals(name)) {
                    onSecondCalls++;
                    return null;
                }
                if ("onUnequip".equals(name)) {
                    onUnequipCalls++;
                    return null;
                }
                if ("onActivate".equals(name)) {
                    onActivateCalls++;
                    return false;
                }
                if ("equals".equals(name)) {
                    return proxy == args[0];
                }
                if ("hashCode".equals(name)) {
                    return System.identityHashCode(proxy);
                }
                if ("toString".equals(name)) {
                    return "PassiveEffectSpy(" + usageId + ")";
                }
                return null;
            }
        }

        public static final class ShazhaoActiveEffectSpy implements InvocationHandler {

            private final String shazhaoId;
            private final boolean activateResult;
            private Object proxy;
            private int onActivateCalls;

            private ShazhaoActiveEffectSpy(
                final String shazhaoId,
                final boolean activateResult
            ) {
                this.shazhaoId = shazhaoId;
                this.activateResult = activateResult;
            }

            private ShazhaoActiveEffectSpy bind(final Object proxy) {
                this.proxy = proxy;
                return this;
            }

            public Object proxy() {
                return proxy;
            }

            public int onActivateCalls() {
                return onActivateCalls;
            }

            @Override
            public Object invoke(
                final Object proxy,
                final Method method,
                final Object[] args
            ) {
                final String name = method.getName();
                if ("getShazhaoId".equals(name)) {
                    return shazhaoId;
                }
                if ("onActivate".equals(name)) {
                    onActivateCalls++;
                    return activateResult;
                }
                if ("equals".equals(name)) {
                    return proxy == args[0];
                }
                if ("hashCode".equals(name)) {
                    return System.identityHashCode(proxy);
                }
                if ("toString".equals(name)) {
                    return "ShazhaoActiveEffectSpy(" + shazhaoId + ")";
                }
                return null;
            }
        }

        public record RuntimeGraph(
            Object data,
            Object unlocks,
            Object tweakConfig,
            Object activePassives
        ) {}

        public record SharedRuntimeFixture(
            Object owner,
            Object data,
            Object unlocks,
            Object tweakConfig,
            Object activePassives,
            Object kongqiaoInventory,
            Object attackInventory,
            Object feedInventory
        ) {}

        private static final class LazySharedRuntimeBridge {

            private final Class<?> serverLevelClass;
            private final Class<?> entityClass;
            private final Class<?> serverPlayerClass;
            private final Constructor<?> gameProfileConstructor;
            private final Method fakePlayerFactoryGetMethod;
            private final Method ensureAttachmentMethod;
            private final Method getDataMethod;
            private final Method getUnlocksMethod;
            private final Method getTweakConfigMethod;
            private final Method getActivePassivesMethod;
            private final Method getKongqiaoInventoryMethod;
            private final Method getAttackInventoryMethod;
            private final Method getFeedInventoryMethod;

            private LazySharedRuntimeBridge(final ClassLoader loader) throws Exception {
                ensureBootstrapForLiveLevel(loader);

                this.serverLevelClass = Class.forName(
                    SERVER_LEVEL_CLASS_NAME,
                    false,
                    loader
                );
                this.entityClass = Class.forName(ENTITY_CLASS_NAME, false, loader);
                this.serverPlayerClass = Class.forName(
                    SERVER_PLAYER_CLASS_NAME,
                    false,
                    loader
                );

                final Class<?> fakePlayerFactoryClass = Class.forName(
                    FAKE_PLAYER_FACTORY_CLASS_NAME,
                    false,
                    loader
                );
                final Class<?> gameProfileClass = Class.forName(
                    GAME_PROFILE_CLASS_NAME,
                    false,
                    loader
                );
                final Class<?> attachmentsClass = Class.forName(
                    KONGQIAO_ATTACHMENTS_CLASS_NAME,
                    false,
                    loader
                );
                final Class<?> attachmentEventsClass = Class.forName(
                    KONGQIAO_ATTACHMENT_EVENTS_CLASS_NAME,
                    false,
                    loader
                );
                final Class<?> kongqiaoDataClass = Class.forName(
                    KONGQIAO_DATA_CLASS_NAME,
                    false,
                    loader
                );

                this.gameProfileConstructor = gameProfileClass.getConstructor(
                    UUID.class,
                    String.class
                );
                this.fakePlayerFactoryGetMethod = fakePlayerFactoryClass.getMethod(
                    "get",
                    serverLevelClass,
                    gameProfileClass
                );
                this.ensureAttachmentMethod = attachmentEventsClass.getDeclaredMethod(
                    "ensureAttachment",
                    entityClass
                );
                this.ensureAttachmentMethod.setAccessible(true);
                this.getDataMethod = attachmentsClass.getMethod("getData", entityClass);
                this.getUnlocksMethod = attachmentsClass.getMethod("getUnlocks", entityClass);
                this.getTweakConfigMethod = attachmentsClass.getMethod(
                    "getTweakConfig",
                    entityClass
                );
                this.getActivePassivesMethod = attachmentsClass.getMethod(
                    "getActivePassives",
                    entityClass
                );
                this.getKongqiaoInventoryMethod = kongqiaoDataClass.getMethod(
                    "getKongqiaoInventory"
                );
                this.getAttackInventoryMethod = kongqiaoDataClass.getMethod(
                    "getAttackInventory"
                );
                this.getFeedInventoryMethod = kongqiaoDataClass.getMethod("getFeedInventory");
            }

            private SharedRuntimeFixture createFixture(final Object liveServerLevel)
                throws Exception {
                if (!serverLevelClass.isInstance(liveServerLevel)) {
                    throw new IllegalArgumentException(
                        "liveServerLevel 类型不匹配，期望 "
                            + serverLevelClass.getName()
                            + "，实际 "
                            + liveServerLevel.getClass().getName()
                    );
                }

                final Object owner = newFakePlayer(liveServerLevel);
                ensureAttachmentMethod.invoke(null, owner);

                final Object data = getDataMethod.invoke(null, owner);
                final Object unlocks = getUnlocksMethod.invoke(null, owner);
                final Object tweakConfig = getTweakConfigMethod.invoke(null, owner);
                final Object activePassives = getActivePassivesMethod.invoke(null, owner);
                final Object kongqiaoInventory = data == null
                    ? null
                    : getKongqiaoInventoryMethod.invoke(data);
                final Object attackInventory = data == null
                    ? null
                    : getAttackInventoryMethod.invoke(data);
                final Object feedInventory = data == null
                    ? null
                    : getFeedInventoryMethod.invoke(data);

                return new SharedRuntimeFixture(
                    owner,
                    data,
                    unlocks,
                    tweakConfig,
                    activePassives,
                    kongqiaoInventory,
                    attackInventory,
                    feedInventory
                );
            }

            private Object newFakePlayer(final Object liveServerLevel) throws Exception {
                final UUID uuid = UUID.nameUUIDFromBytes(
                    "task4_shared_runtime_fixture".getBytes(StandardCharsets.UTF_8)
                );
                final Object profile = gameProfileConstructor.newInstance(
                    uuid,
                    "task4_shared_runtime"
                );
                final Object player = fakePlayerFactoryGetMethod.invoke(
                    null,
                    liveServerLevel,
                    profile
                );
                if (!serverPlayerClass.isInstance(player)) {
                    throw new IllegalStateException(
                        "FakePlayerFactory 未返回 ServerPlayer，实际类型: "
                            + (player == null ? "null" : player.getClass().getName())
                    );
                }
                return player;
            }

            private static void ensureBootstrapForLiveLevel(final ClassLoader loader)
                throws Exception {
                final Class<?> sharedConstantsClass = Class.forName(
                    SHARED_CONSTANTS_CLASS_NAME,
                    false,
                    loader
                );
                final Class<?> bootstrapClass = Class.forName(
                    BOOTSTRAP_CLASS_NAME,
                    false,
                    loader
                );
                sharedConstantsClass.getMethod("tryDetectVersion").invoke(null);
                bootstrapClass.getMethod("bootStrap").invoke(null);
            }
        }
    }

    private static URLClassLoader buildRuntimeClassLoader() throws IOException {
        final List<URL> urls = new ArrayList<>();
        final Path mainClassesPath = MAIN_CLASSES.toAbsolutePath();
        if (!mainClassesPath.toFile().exists()) {
            throw new IOException("缺少主类输出目录: " + mainClassesPath);
        }
        urls.add(mainClassesPath.toUri().toURL());

        final Path mainResourcesPath = MAIN_RESOURCES.toAbsolutePath();
        if (mainResourcesPath.toFile().exists()) {
            urls.add(mainResourcesPath.toUri().toURL());
        }

        final Properties props = new Properties();
        final Path manifestPath = ARTIFACT_MANIFEST.toAbsolutePath();
        if (!manifestPath.toFile().exists()) {
            throw new IOException("缺少依赖清单: " + manifestPath);
        }
        try (InputStream input = Files.newInputStream(manifestPath)) {
            props.load(input);
        }

        for (String key : props.stringPropertyNames()) {
            final String jarPath = props.getProperty(key);
            if (jarPath == null || jarPath.isBlank()) {
                continue;
            }
            final Path absoluteJarPath = Path.of(jarPath).toAbsolutePath();
            if (absoluteJarPath.toFile().exists()) {
                urls.add(absoluteJarPath.toUri().toURL());
            }
        }

        urls.add(resolveMinecraftRuntimeJar().toUri().toURL());
        return new URLClassLoader(
            urls.toArray(new URL[0]),
            ClassLoader.getPlatformClassLoader()
        );
    }

    private static synchronized Path resolveMinecraftRuntimeJar() throws IOException {
        if (cachedMinecraftJarPath != null && cachedMinecraftJarPath.toFile().exists()) {
            return cachedMinecraftJarPath;
        }

        final List<Path> searchRoots = new ArrayList<>();
        final String userHome = System.getProperty("user.home");
        searchRoots.add(
            Path.of(
                userHome,
                ".gradle",
                "caches",
                "neoformruntime",
                "intermediate_results"
            )
        );
        searchRoots.add(
            Path.of(
                userHome,
                ".gradle",
                "caches",
                "fabric-loom",
                "minecraftMaven"
            )
        );

        for (Path root : searchRoots) {
            final Path matched = findJarContainingResource(root, NBT_TAG_CLASS_RESOURCE);
            if (matched != null) {
                cachedMinecraftJarPath = matched;
                return matched;
            }
        }

        throw new IOException("未找到包含 net.minecraft.nbt.Tag 的运行时 Jar");
    }

    private static Path findJarContainingResource(final Path root, final String resource)
        throws IOException {
        if (root == null || !root.toFile().exists()) {
            return null;
        }
        try (var stream = Files.walk(root, MAX_RUNTIME_JAR_SEARCH_DEPTH)) {
            final List<Path> candidates = stream
                .filter(path -> path.toString().endsWith(".jar"))
                .toList();
            for (Path candidate : candidates) {
                if (jarContainsResource(candidate, resource)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private static boolean jarContainsResource(final Path jarPath, final String resource) {
        try (var jar = new java.util.jar.JarFile(jarPath.toFile())) {
            return jar.getEntry(resource) != null;
        } catch (IOException ignored) {
            return false;
        }
    }
}
