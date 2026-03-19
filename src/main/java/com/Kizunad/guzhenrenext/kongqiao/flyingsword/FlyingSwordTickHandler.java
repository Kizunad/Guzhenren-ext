package com.Kizunad.guzhenrenext.kongqiao.flyingsword;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordCooldownAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordPreferencesAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordRuntimeAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordSelectionAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordStateAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordStorageAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.calculator.FlyingSwordAttributes;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.BenmingSwordBondService;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.BenmingSwordResourceTransaction;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.CultivationSnapshot;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.training.FlyingSwordTrainingService;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 飞剑 Tick 驱动（Phase 2 最小版）。
 * <p>
 * 目标：
 * <ul>
 *     <li>确保飞剑附件每 tick 可维护（冷却递减等）。</li>
 *     <li>为后续 AI/运动/网络同步提供集中入口。</li>
 * </ul>
 * </p>
 */
@EventBusSubscriber(
    modid = com.Kizunad.guzhenrenext.GuzhenrenExt.MODID,
    bus = EventBusSubscriber.Bus.GAME
)
public final class FlyingSwordTickHandler {

    private static final String ATTR_STABLE_SWORD_ID = "stableSwordId";
    private static final String ATTR_BOND = "bond";
    private static final String ATTR_BOND_OWNER_UUID = "ownerUuid";
    private static final String ATTR_BOND_RESONANCE = "resonance";
    private static final BiFunction<
        ServerPlayer,
        String,
        BenmingSwordBondService.BacklashContext
    > DEFAULT_WITHDRAWN_BACKLASH_CONTEXT_PROVIDER =
        new BiFunction<
            ServerPlayer,
            String,
            BenmingSwordBondService.BacklashContext
        >() {
            @Override
            public BenmingSwordBondService.BacklashContext apply(
                final ServerPlayer player,
                final String ownerUuid
            ) {
                return buildDefaultWithdrawnBacklashContext(player, ownerUuid);
            }
        };
    private static volatile BiFunction<
        ServerPlayer,
        String,
        BenmingSwordBondService.BacklashContext
    > withdrawnBacklashContextProvider = DEFAULT_WITHDRAWN_BACKLASH_CONTEXT_PROVIDER;

    private FlyingSwordTickHandler() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // 冷却递减
        FlyingSwordCooldownAttachment cooldowns = KongqiaoAttachments.getFlyingSwordCooldowns(player);
        if (cooldowns != null) {
            cooldowns.tickAll();
        }

        // runtime/state 占位：为后续 AI/网络同步预留，当前确保附件存在即可。
        FlyingSwordRuntimeAttachment runtime = KongqiaoAttachments.getFlyingSwordRuntime(player);
        FlyingSwordStateAttachment state = KongqiaoAttachments.getFlyingSwordState(player);

        // 偏好/选择存活性检查（目前仅确保附件存在）。
        FlyingSwordPreferencesAttachment preferences = KongqiaoAttachments.getFlyingSwordPreferences(player);
        FlyingSwordSelectionAttachment selection = KongqiaoAttachments.getFlyingSwordSelection(player);
        FlyingSwordStorageAttachment storage = KongqiaoAttachments.getFlyingSwordStorage(player);
        if (
            preferences == null
                || selection == null
                || storage == null
                || runtime == null
                || cooldowns == null
                || state == null
        ) {
            return;
        }

        reconcileBenmingCacheForTick(player);
        reconcileOverloadLoopForTick(state, player.serverLevel().getGameTime());
        if (!state.isInitialized()) {
            state.setInitialized(true);
        }

        FlyingSwordTrainingService.tick(player);
        // Phase 2：后续在此调用 AI/同步/战斗模块。
    }

    public static void markCombatActivityForTick(final ServerPlayer player) {
        if (player == null) {
            return;
        }
        final FlyingSwordStateAttachment state =
            KongqiaoAttachments.getFlyingSwordState(player);
        if (state == null) {
            return;
        }
        state.setLastCombatTick(player.serverLevel().getGameTime());
    }

    private static void reconcileOverloadLoopForTick(
        final FlyingSwordStateAttachment state,
        final long gameTick
    ) {
        if (state == null) {
            return;
        }

        final boolean combatActivityThisTick = state.getLastCombatTick() == gameTick;
        final OverloadResolution resolution = resolveOverloadForTick(
            new OverloadTickInput(
                state.getOverload(),
                state.getBurstCooldownUntilTick(),
                state.getOverloadBacklashUntilTick(),
                state.getOverloadRecoveryUntilTick(),
                state.getLastOverloadTick(),
                gameTick,
                combatActivityThisTick,
                OverloadBacklashEngine.defaultCombatOverloadGrowthPerTick()
            )
        );
        state.setOverload(resolution.overloadAfter());
        state.setBurstCooldownUntilTick(resolution.burstCooldownUntilTickAfter());
        state.setOverloadBacklashUntilTick(
            resolution.overloadBacklashUntilTickAfter()
        );
        state.setOverloadRecoveryUntilTick(
            resolution.overloadRecoveryUntilTickAfter()
        );
        state.setLastOverloadTick(resolution.lastOverloadTickAfter());
    }

    static OverloadResolution resolveOverloadForTick(
        final OverloadTickInput input
    ) {
        if (input == null) {
            return new OverloadResolution(0.0D, 0L, 0L, 0L, 0L, false);
        }
        final OverloadBacklashEngine.OverloadResolution resolution =
            OverloadBacklashEngine.resolveOverloadForTick(
                new OverloadBacklashEngine.OverloadTickInput(
                    input.overloadBefore(),
                    input.burstCooldownUntilTickBefore(),
                    input.overloadBacklashUntilTickBefore(),
                    input.overloadRecoveryUntilTickBefore(),
                    input.lastOverloadTickBefore(),
                    input.currentTick(),
                    input.combatActivityThisTick(),
                    input.combatOverloadGrowthPerTick()
                )
            );
        return new OverloadResolution(
            resolution.overloadAfter(),
            resolution.burstCooldownUntilTickAfter(),
            resolution.overloadBacklashUntilTickAfter(),
            resolution.overloadRecoveryUntilTickAfter(),
            resolution.lastOverloadTickAfter(),
            resolution.backlashTriggered()
        );
    }

    static boolean shouldTriggerOverloadBacklash(final double overload) {
        return OverloadBacklashEngine.shouldTriggerOverloadBacklash(overload);
    }

    public static void markCombatActivityForServerOwner(
        @Nullable final LivingEntity owner,
        @Nullable final FlyingSwordEntity attackingSword
    ) {
        if (!(owner instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (attackingSword == null) {
            return;
        }
        final FlyingSwordStateAttachment state =
            KongqiaoAttachments.getFlyingSwordState(serverPlayer);
        if (state == null) {
            return;
        }
        if (!shouldMarkCombatActivityForSword(state, attackingSword)) {
            return;
        }
        markCombatActivityForTick(serverPlayer);
    }

    static boolean shouldMarkCombatActivityForSword(
        @Nullable final FlyingSwordStateAttachment state,
        @Nullable final FlyingSwordEntity attackingSword
    ) {
        if (state == null || attackingSword == null) {
            return false;
        }
        return OverloadBacklashEngine.shouldMarkCombatActivityForSword(
            state.getBondedSwordId(),
            attackingSword.getSwordAttributes().getStableSwordId(),
            state.isBondCacheDirty()
        );
    }

    record OverloadResolution(
        double overloadAfter,
        long burstCooldownUntilTickAfter,
        long overloadBacklashUntilTickAfter,
        long overloadRecoveryUntilTickAfter,
        long lastOverloadTickAfter,
        boolean backlashTriggered
    ) {}

    // given tick 协调层聚合玩家状态 when 进入过载结算 then 统一封装为单对象传递。
    record OverloadTickInput(
        double overloadBefore,
        long burstCooldownUntilTickBefore,
        long overloadBacklashUntilTickBefore,
        long overloadRecoveryUntilTickBefore,
        long lastOverloadTickBefore,
        long currentTick,
        boolean combatActivityThisTick,
        double combatOverloadGrowthPerTick
    ) {}

    public static void reconcileBenmingCacheForTick(final ServerPlayer player) {
        if (player == null) {
            return;
        }
        final ServerLevel level = player.serverLevel();
        final FlyingSwordStateAttachment state =
            KongqiaoAttachments.getFlyingSwordState(player);
        final FlyingSwordStorageAttachment storage =
            KongqiaoAttachments.getFlyingSwordStorage(player);
        if (state == null || storage == null) {
            return;
        }

        final String ownerUuid = player.getUUID().toString();
        reconcileWithdrawnIllegalDetachForTick(
            level,
            player,
            state,
            storage,
            ownerUuid
        );
        final List<String> liveCandidates = scanLiveBoundSwordIds(
            level,
            player,
            ownerUuid
        );
        final String resolvedSwordId;
        if (liveCandidates.size() == 1) {
            resolvedSwordId = liveCandidates.get(0);
        } else if (liveCandidates.size() > 1) {
            state.clearBondCache();
            return;
        } else {
            final List<String> recalledCandidates = scanRecalledBoundSwordIds(
                storage,
                ownerUuid
            );
            if (recalledCandidates.size() == 1) {
                resolvedSwordId = recalledCandidates.get(0);
            } else {
                state.clearBondCache();
                return;
            }
        }

        if (
            state.isBondCacheDirty()
                || state.getLastResolvedTick() == -1L
                || !resolvedSwordId.equals(state.getBondedSwordId())
        ) {
            state.updateBondCache(resolvedSwordId, level.getGameTime());
        }
    }

    public static void installWithdrawnBacklashContextProviderForTest(
        final BiFunction<
            ServerPlayer,
            String,
            BenmingSwordBondService.BacklashContext
        > provider
    ) {
        if (provider == null) {
            withdrawnBacklashContextProvider =
                DEFAULT_WITHDRAWN_BACKLASH_CONTEXT_PROVIDER;
            return;
        }
        withdrawnBacklashContextProvider = provider;
    }

    public static void resetWithdrawnBacklashContextProviderForTest() {
        withdrawnBacklashContextProvider =
            DEFAULT_WITHDRAWN_BACKLASH_CONTEXT_PROVIDER;
    }

    private static void reconcileWithdrawnIllegalDetachForTick(
        final ServerLevel level,
        final ServerPlayer player,
        final FlyingSwordStateAttachment state,
        final FlyingSwordStorageAttachment storage,
        final String ownerUuid
    ) {
        final BenmingSwordBondService.PlayerBondCachePort cachePort =
            toPlayerBondCachePort(state);
        final BenmingSwordBondService.BacklashContext backlashContext =
            resolveWithdrawnBacklashContext(player, ownerUuid);

        for (int i = 0; i < storage.getCount(); i++) {
            final FlyingSwordStorageAttachment.RecalledSword recalled = storage.getAt(i);
            if (recalled == null || !recalled.itemWithdrawn) {
                continue;
            }
            if (!isWithdrawnRecalledSwordStillBoundToOwner(recalled, ownerUuid)) {
                continue;
            }
            BenmingSwordBondService.illegalDetach(
                ownerUuid,
                toRecalledSwordBondPort(recalled),
                cachePort,
                level.getGameTime(),
                backlashContext
            );
        }
    }

    private static BenmingSwordBondService.BacklashContext resolveWithdrawnBacklashContext(
        final ServerPlayer player,
        final String ownerUuid
    ) {
        final BenmingSwordBondService.BacklashContext context =
            withdrawnBacklashContextProvider.apply(player, ownerUuid);
        if (context == null) {
            return buildDefaultWithdrawnBacklashContext(player, ownerUuid);
        }
        return context;
    }

    private static BenmingSwordBondService.BacklashContext buildDefaultWithdrawnBacklashContext(
        final ServerPlayer player,
        final String ownerUuid
    ) {
        final BenmingSwordResourceTransaction.ResourceMutationPort mutationPort =
            toPlayerResourceMutationPort(player);
        return BenmingSwordBondService.defaultLightBacklashContext(
            ownerUuid,
            CultivationSnapshot.capture(player),
            mutationPort,
            KongqiaoAttachments.getFlyingSwordCooldowns(player)
        );
    }

    private static boolean isWithdrawnRecalledSwordStillBoundToOwner(
        final FlyingSwordStorageAttachment.RecalledSword recalled,
        final String ownerUuid
    ) {
        final CompoundTag attrsTag = recalled.attributes;
        if (attrsTag == null || attrsTag.isEmpty()) {
            return false;
        }
        if (!attrsTag.contains(ATTR_BOND, Tag.TAG_COMPOUND)) {
            return false;
        }
        final CompoundTag bondTag = attrsTag.getCompound(ATTR_BOND);
        if (!bondTag.contains(ATTR_BOND_OWNER_UUID, Tag.TAG_STRING)) {
            return false;
        }
        final String bondOwnerUuid = bondTag.getString(ATTR_BOND_OWNER_UUID);
        return ownerUuid.equals(bondOwnerUuid);
    }

    private static BenmingSwordBondService.SwordBondPort toRecalledSwordBondPort(
        final FlyingSwordStorageAttachment.RecalledSword recalled
    ) {
        return new BenmingSwordBondService.SwordBondPort() {
            @Override
            public String getStableSwordId() {
                final CompoundTag attrsTag = getOrCreateAttributesTag(recalled);
                return attrsTag.getString(ATTR_STABLE_SWORD_ID);
            }

            @Override
            public String getBondOwnerUuid() {
                final CompoundTag bondTag = getOrCreateBondTag(recalled);
                return bondTag.getString(ATTR_BOND_OWNER_UUID);
            }

            @Override
            public double getBondResonance() {
                final CompoundTag bondTag = getOrCreateBondTag(recalled);
                if (!bondTag.contains(ATTR_BOND_RESONANCE, Tag.TAG_DOUBLE)) {
                    return 0.0D;
                }
                return bondTag.getDouble(ATTR_BOND_RESONANCE);
            }

            @Override
            public void setBondOwnerUuid(final String ownerUuid) {
                final CompoundTag bondTag = getOrCreateBondTag(recalled);
                bondTag.putString(ATTR_BOND_OWNER_UUID, ownerUuid == null ? "" : ownerUuid);
            }

            @Override
            public void setBondResonance(final double resonance) {
                final CompoundTag bondTag = getOrCreateBondTag(recalled);
                bondTag.putDouble(ATTR_BOND_RESONANCE, resonance);
            }
        };
    }

    private static CompoundTag getOrCreateAttributesTag(
        final FlyingSwordStorageAttachment.RecalledSword recalled
    ) {
        if (recalled.attributes == null) {
            recalled.attributes = new CompoundTag();
        }
        return recalled.attributes;
    }

    private static CompoundTag getOrCreateBondTag(
        final FlyingSwordStorageAttachment.RecalledSword recalled
    ) {
        final CompoundTag attrsTag = getOrCreateAttributesTag(recalled);
        if (!attrsTag.contains(ATTR_BOND, Tag.TAG_COMPOUND)) {
            attrsTag.put(ATTR_BOND, new CompoundTag());
        }
        return attrsTag.getCompound(ATTR_BOND);
    }

    private static BenmingSwordBondService.PlayerBondCachePort toPlayerBondCachePort(
        final FlyingSwordStateAttachment state
    ) {
        return new BenmingSwordBondService.PlayerBondCachePort() {
            @Override
            public String getBondedSwordId() {
                return state.getBondedSwordId();
            }

            @Override
            public boolean isBondCacheDirty() {
                return state.isBondCacheDirty();
            }

            @Override
            public void updateBondCache(
                final String stableSwordId,
                final long resolvedTick
            ) {
                state.updateBondCache(stableSwordId, resolvedTick);
            }

            @Override
            public void markBondCacheDirty() {
                state.markBondCacheDirty();
            }

            @Override
            public void clearBondCache() {
                state.clearBondCache();
            }
        };
    }

    private static BenmingSwordResourceTransaction.ResourceMutationPort toPlayerResourceMutationPort(
        final ServerPlayer player
    ) {
        return new BenmingSwordResourceTransaction.ResourceMutationPort() {
            @Override
            public void spendZhenyuan(final double amount) {
                ZhenYuanHelper.modify(player, -amount);
            }

            @Override
            public void spendNiantou(final double amount) {
                NianTouHelper.modify(player, -amount);
            }

            @Override
            public void spendHunpo(final double amount) {
                HunPoHelper.modify(player, -amount);
            }
        };
    }

    private static List<String> scanLiveBoundSwordIds(
        final ServerLevel level,
        final ServerPlayer player,
        final String ownerUuid
    ) {
        final List<String> ids = new ArrayList<>();
        for (FlyingSwordEntity sword : FlyingSwordController.getPlayerSwords(level, player)) {
            if (sword == null) {
                continue;
            }
            if (sword.getSwordAttributes().isDurabilityDepleted()) {
                continue;
            }
            final String stableSwordId = sword.getSwordAttributes().getStableSwordId();
            if (stableSwordId == null || stableSwordId.isBlank()) {
                continue;
            }
            final String bondOwnerUuid = sword
                .getSwordAttributes()
                .getBond()
                .getOwnerUuid();
            if (ownerUuid.equals(bondOwnerUuid)) {
                ids.add(stableSwordId);
            }
        }
        return ids;
    }

    private static List<String> scanRecalledBoundSwordIds(
        final FlyingSwordStorageAttachment storage,
        final String ownerUuid
    ) {
        final List<String> ids = new ArrayList<>();
        for (int i = 0; i < storage.getCount(); i++) {
            final FlyingSwordStorageAttachment.RecalledSword recalled = storage.getAt(i);
            if (recalled == null || recalled.itemWithdrawn) {
                continue;
            }
            final CompoundTag attrsTag = recalled.attributes;
            if (attrsTag == null || attrsTag.isEmpty()) {
                continue;
            }
            if (!attrsTag.contains(ATTR_STABLE_SWORD_ID, Tag.TAG_STRING)) {
                continue;
            }
            if (!attrsTag.contains(ATTR_BOND, Tag.TAG_COMPOUND)) {
                continue;
            }
            final CompoundTag bondTag = attrsTag.getCompound(ATTR_BOND);
            if (!bondTag.contains(ATTR_BOND_OWNER_UUID, Tag.TAG_STRING)) {
                continue;
            }

            final String stableSwordId = attrsTag.getString(ATTR_STABLE_SWORD_ID);
            if (stableSwordId == null || stableSwordId.isBlank()) {
                continue;
            }

            final FlyingSwordAttributes attrs = FlyingSwordAttributes.fromNBT(attrsTag);
            if (attrs.isDurabilityDepleted()) {
                continue;
            }
            if (ownerUuid.equals(attrs.getBond().getOwnerUuid())) {
                ids.add(stableSwordId);
            }
        }
        return ids;
    }
}
