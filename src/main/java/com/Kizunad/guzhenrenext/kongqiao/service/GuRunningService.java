package com.Kizunad.guzhenrenext.kongqiao.service;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoData;
import com.Kizunad.guzhenrenext.kongqiao.inventory.KongqiaoInventory;
import com.Kizunad.guzhenrenext.kongqiao.service.KongqiaoPressureProjectionService.PassiveRuntimeCandidate;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouDataManager;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouUnlockChecker;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouUsageId;
import com.Kizunad.guzhenrenext.kongqiao.service.KongqiaoPressureProjectionService.PassiveRuntimeSnapshot;
import com.Kizunad.guzhenrenext.kongqiao.service.KongqiaoPressureProjectionService.SlotPassiveRuntimeCandidate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 蛊虫被动逻辑运行时服务。
 * <p>
 * 负责在服务端 Tick 时遍历玩家空窍内的物品，并触发相应的被动逻辑。
 * </p>
 */
@EventBusSubscriber(
    modid = GuzhenrenExt.MODID,
    bus = EventBusSubscriber.Bus.GAME
)
public final class GuRunningService {

    static final int RECOVERY_DECAY_INTERVAL_TICKS = 20;
    static final double RECOVERY_DECAY_PER_INTERVAL = 1.0D;
    private static final int TICKS_PER_SECOND = 20;
    private static final double ACTIVE_USAGE_WHEEL_PRELOAD_PRESSURE = 1.0D;
    private static final Map<UUID, ItemStack[]> LAST_KONGQIAO_SNAPSHOT =
        new HashMap<>();

    private GuRunningService() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        KongqiaoData data = KongqiaoAttachments.getData(player);
        if (data == null) {
            return;
        }

        boolean isSecond = (player.tickCount % TICKS_PER_SECOND == 0);
        final KongqiaoInventory inventory = data.getKongqiaoInventory();
        final int unlockedSlots = inventory == null
            ? 0
            : inventory.getSettings().getUnlockedSlots();
        final List<SlotPassiveRuntimeCandidate> slotPassiveRuntimeCandidates =
            inventory == null
                ? List.of()
                : KongqiaoPressureProjectionService.collectSlotPassiveRuntimeCandidates(
                    player,
                    inventory,
                    unlockedSlots
                );
        applyRecoveryTick(
            data.getStabilityState(),
            player.level().getGameTime(),
            KongqiaoCapacityBridge.resolveFromEntity(player),
            slotPassiveRuntimeCandidates,
            unlockedSlots
        );
        if (inventory != null) {
            handleContainerEquipChanges(player, inventory);
            tickKongqiaoEffects(player, inventory, isSecond);
        }
        ShazhaoRunningService.tickUnlockedEffects(player, isSecond);
    }

    /**
     * 遍历空窍，执行所有物品的被动逻辑。
     */
    public static void tickKongqiaoEffects(
        LivingEntity user,
        KongqiaoInventory inventory,
        boolean isSecond
    ) {
        if (inventory == null) {
            return;
        }
        int unlockedSlots = inventory.getSettings().getUnlockedSlots();
        tickContainerEffects(user, inventory, unlockedSlots, isSecond);
    }

    /**
     * 遍历任意容器，执行其中物品的被动逻辑。
     * <p>
     * 用途：将空窍被动运行时逻辑复用到自定义 NPC 上（NPC 的蛊虫通常存放在
     * {@code NpcInventory} 中，而非玩家的 {@link KongqiaoInventory}）。
     * </p>
     *
     * @param user 使用者（用于资源扣除、道痕倍率等）
     * @param container 要遍历的容器
     * @param slotCount 需要遍历的槽位数量（一般为已解锁槽位数或容器主槽数量）
     * @param isSecond 是否整秒 Tick
     */
    public static void tickContainerEffects(
        LivingEntity user,
        Container container,
        int slotCount,
        boolean isSecond
    ) {
        if (user == null || container == null || slotCount <= 0) {
            return;
        }

        int maxSlots = Math.min(slotCount, container.getContainerSize());
        final PassiveRuntimeSnapshot passiveRuntimeSnapshot =
            KongqiaoPressureProjectionService.resolvePassiveRuntimeSnapshot(
                user,
                container,
                maxSlots
            );
        syncPassiveRuntimeState(user, passiveRuntimeSnapshot);
        final Set<Integer> sealedSlots = resolveRuntimeSealedSlots(user);
        final ActivePassives activePassives = KongqiaoAttachments.getActivePassives(user);
        for (int slotIndex : collectRunnablePassiveRuntimeSlots(maxSlots, sealedSlots)) {
            ItemStack stack = container.getItem(slotIndex);
            if (stack.isEmpty()) {
                continue;
            }

            NianTouData niantouData = NianTouDataManager.getData(stack);
            if (niantouData == null || niantouData.usages() == null) {
                continue;
            }

            for (NianTouData.Usage usage : niantouData.usages()) {
                if (
                    !NianTouUnlockChecker.isUsageUnlocked(
                        user,
                        stack,
                        usage.usageID()
                    )
                ) {
                    continue;
                }
                final String usageId = usage.usageID();
                IGuEffect effect = GuEffectRegistry.get(usageId);
                runPassiveUsageIfAllowed(
                    activePassives,
                    user,
                    stack,
                    usage,
                    effect,
                    passiveRuntimeSnapshot,
                    isSecond
                );
            }
        }
    }

    static List<Integer> collectRunnablePassiveRuntimeSlots(
        final int slotCount,
        final Set<Integer> sealedSlots
    ) {
        final int normalizedSlotCount = Math.max(0, slotCount);
        final Set<Integer> normalizedSealedSlots = sealedSlots == null
            ? Set.of()
            : sealedSlots;
        final List<Integer> runnableSlots = new ArrayList<>(normalizedSlotCount);
        for (int slot = 0; slot < normalizedSlotCount; slot++) {
            if (normalizedSealedSlots.contains(slot)) {
                continue;
            }
            runnableSlots.add(slot);
        }
        return runnableSlots;
    }

    static Set<Integer> resolveRuntimeSealedSlots(final LivingEntity user) {
        if (user == null) {
            return Set.of();
        }
        final KongqiaoData data = KongqiaoAttachments.getData(user);
        if (data == null || data.getStabilityState() == null) {
            return Set.of();
        }
        return data.getStabilityState().getSealedSlots();
    }

    static boolean runPassiveUsageIfAllowed(
        final ActivePassives actives,
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usage,
        final IGuEffect effect,
        final PassiveRuntimeSnapshot passiveRuntimeSnapshot,
        final boolean isSecond
    ) {
        if (usage == null) {
            return false;
        }
        final String usageId = usage.usageID();
        if (
            NianTouUsageId.isPassive(usageId)
                && passiveRuntimeSnapshot != null
                && passiveRuntimeSnapshot.isForcedDisabled(usageId)
        ) {
            forceDisablePassiveUsage(actives, user, stack, usage, effect);
            return false;
        }
        if (effect == null) {
            return false;
        }

        try {
            effect.onTick(user, stack, usage);
            if (isSecond) {
                effect.onSecond(user, stack, usage);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    static void syncPassiveRuntimeState(
        final ActivePassives actives,
        final KongqiaoData.StabilityState stabilityState,
        final PassiveRuntimeSnapshot snapshot
    ) {
        if (snapshot == null) {
            return;
        }
        if (stabilityState != null) {
            stabilityState.setOverloadTier(snapshot.overloadTier());
            stabilityState.setForcedDisabledUsageIds(snapshot.forcedDisabledUsageIds());
        }
    }

    static void applyRecoveryTick(
        final KongqiaoData.StabilityState stabilityState,
        final long currentGameTime,
        final KongqiaoCapacityProfile capacityProfile,
        final List<SlotPassiveRuntimeCandidate> slotPassiveRuntimeCandidates,
        final int unlockedSlotCount
    ) {
        if (stabilityState == null) {
            return;
        }
        final long normalizedGameTime = Math.max(0L, currentGameTime);
        final long lastDecayGameTime = stabilityState.getLastDecayGameTime();
        if (lastDecayGameTime <= 0L) {
            stabilityState.setLastDecayGameTime(normalizedGameTime);
        } else if (normalizedGameTime > lastDecayGameTime) {
            final long elapsedTicks = normalizedGameTime - lastDecayGameTime;
            final long decayIntervals = elapsedTicks / RECOVERY_DECAY_INTERVAL_TICKS;
            if (decayIntervals > 0L) {
                final double decayAmount = decayIntervals * RECOVERY_DECAY_PER_INTERVAL;
                stabilityState.setFatigueDebt(
                    stabilityState.getFatigueDebt() - decayAmount
                );
                stabilityState.setBurstPressure(
                    stabilityState.getBurstPressure() - decayAmount
                );
                stabilityState.setLastDecayGameTime(
                    lastDecayGameTime + decayIntervals * RECOVERY_DECAY_INTERVAL_TICKS
                );
            }
        }

        final RecoverySnapshot recoverySnapshotBeforeSeal = evaluateRecoverySnapshot(
            stabilityState,
            capacityProfile,
            slotPassiveRuntimeCandidates
        );
        final Set<Integer> resolvedSealedSlots = resolveSealedSlotsAfterRecovery(
            stabilityState.getSealedSlots(),
            unlockedSlotCount,
            recoverySnapshotBeforeSeal.overloadTier(),
            stabilityState.getFatigueDebt(),
            stabilityState.getBurstPressure()
        );
        if (!resolvedSealedSlots.equals(stabilityState.getSealedSlots())) {
            stabilityState.setSealedSlots(resolvedSealedSlots);
        }

        final RecoverySnapshot recoverySnapshotAfterSeal = evaluateRecoverySnapshot(
            stabilityState,
            capacityProfile,
            slotPassiveRuntimeCandidates
        );
        stabilityState.setOverloadTier(recoverySnapshotAfterSeal.overloadTier());
    }

    static RecoverySnapshot evaluateRecoverySnapshot(
        final KongqiaoData.StabilityState stabilityState,
        final KongqiaoCapacityProfile capacityProfile,
        final List<SlotPassiveRuntimeCandidate> slotPassiveRuntimeCandidates
    ) {
        if (stabilityState == null) {
            return RecoverySnapshot.empty();
        }
        final List<PassiveRuntimeCandidate> passiveRuntimeCandidates =
            KongqiaoPressureProjectionService.collapseSlotRuntimeCandidatesByUsage(
                slotPassiveRuntimeCandidates,
                stabilityState.getSealedSlots()
            );
        final PassiveRuntimeSnapshot snapshot =
            KongqiaoPressureProjectionService.evaluatePassiveRuntimeSnapshot(
                passiveRuntimeCandidates,
                capacityProfile,
                stabilityState.getBurstPressure(),
                stabilityState.getFatigueDebt()
            );
        return new RecoverySnapshot(
            snapshot.passivePressure(),
            snapshot.pressureCap(),
            snapshot.effectivePressure(),
            snapshot.overloadTier()
        );
    }

    static Set<Integer> resolveSealedSlotsAfterRecovery(
        final Set<Integer> currentSealedSlots,
        final int unlockedSlotCount,
        final int overloadTier,
        final double fatigueDebt,
        final double burstPressure
    ) {
        final LinkedHashSet<Integer> sealedSlots = new LinkedHashSet<>(
            currentSealedSlots == null ? Set.of() : currentSealedSlots
        );
        if (KongqiaoPressureProjectionService.isCollapseEdgeOrWorse(overloadTier)) {
            final int sealCandidate = highestUnlockedUnsealedSlot(
                unlockedSlotCount,
                sealedSlots
            );
            if (sealCandidate >= 0) {
                sealedSlots.add(sealCandidate);
            }
            return sealedSlots;
        }
        if (
            !sealedSlots.isEmpty()
                && KongqiaoPressureProjectionService.isTenseOrBetter(overloadTier)
                && fatigueDebt <= 0.0D
                && burstPressure <= 0.0D
        ) {
            sealedSlots.clear();
        }
        return sealedSlots;
    }

    static int highestUnlockedUnsealedSlot(
        final int unlockedSlotCount,
        final Set<Integer> sealedSlots
    ) {
        final int normalizedUnlockedSlotCount = Math.max(0, unlockedSlotCount);
        final Set<Integer> normalizedSealedSlots = sealedSlots == null
            ? Set.of()
            : sealedSlots;
        for (int slot = normalizedUnlockedSlotCount - 1; slot >= 0; slot--) {
            if (!normalizedSealedSlots.contains(slot)) {
                return slot;
            }
        }
        return -1;
    }

    record RecoverySnapshot(
        double passivePressure,
        double pressureCap,
        double effectivePressure,
        int overloadTier
    ) {

        static RecoverySnapshot empty() {
            return new RecoverySnapshot(0.0D, 0.0D, 0.0D, 0);
        }
    }

    /**
     * 触发物品的主动逻辑。
     *
     * @param user     使用者
     * @param stack    物品
     * @param usageId  要触发的用途ID
     * @return 是否成功触发
     */
    public static boolean activateEffect(
        LivingEntity user,
        ItemStack stack,
        String usageId
    ) {
        return activateEffectWithResult(user, stack, usageId).success();
    }

    /**
     * 触发物品主动逻辑，并返回失败原因（用于 UI/轮盘提示）。
     *
     * @param user     使用者
     * @param stack    物品
     * @param usageId  要触发的用途ID
     * @return 触发结果（成功/失败原因）
     */
    public static ActivationResult activateEffectWithResult(
        LivingEntity user,
        ItemStack stack,
        String usageId
    ) {
        if (stack == null || stack.isEmpty() || usageId == null || usageId.isBlank()) {
            return new ActivationResult(false, ActivationFailureReason.INVALID_INPUT);
        }

        NianTouData data = NianTouDataManager.getData(stack);
        if (data == null || data.usages() == null) {
            return new ActivationResult(false, ActivationFailureReason.NO_NIANTOU_DATA);
        }

        for (NianTouData.Usage usage : data.usages()) {
            if (!usageId.equals(usage.usageID())) {
                continue;
            }

            if (!NianTouUnlockChecker.isUsageUnlocked(user, stack, usageId)) {
                return new ActivationResult(false, ActivationFailureReason.NOT_UNLOCKED);
            }

            IGuEffect effect = GuEffectRegistry.get(usageId);
            if (effect == null) {
                return new ActivationResult(false, ActivationFailureReason.NOT_IMPLEMENTED);
            }

            final KongqiaoData kongqiaoData = KongqiaoAttachments.getData(user);
            final double currentEffectivePressure;
            final double pressureCap;
            if (user != null && kongqiaoData != null) {
                final KongqiaoPressureProjection projection =
                    KongqiaoPressureProjectionService.assemblePressureProjection(
                        kongqiaoData,
                        user
                    );
                currentEffectivePressure = projection.effectivePressure();
                pressureCap = projection.pressureCap();
            } else {
                currentEffectivePressure = 0.0D;
                pressureCap = Double.POSITIVE_INFINITY;
            }
            return activateResolvedUsage(
                user,
                stack,
                usage,
                effect,
                currentEffectivePressure,
                pressureCap
            );
        }

        return new ActivationResult(false, ActivationFailureReason.USAGE_NOT_ON_ITEM);
    }

    static ActivationResult activateResolvedUsage(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usage,
        final IGuEffect effect,
        final double currentEffectivePressure,
        final double pressureCap
    ) {
        if (effect == null) {
            return new ActivationResult(false, ActivationFailureReason.NOT_IMPLEMENTED);
        }
        if (
            KongqiaoPressureProjectionService.wouldProjectedPressureReachOverload(
                currentEffectivePressure,
                pressureCap,
                ACTIVE_USAGE_WHEEL_PRELOAD_PRESSURE
            )
        ) {
            return new ActivationResult(false, ActivationFailureReason.PRESSURE_LIMIT);
        }
        final boolean success = effect.onActivate(user, stack, usage);
        if (success) {
            return new ActivationResult(true, null);
        }
        return new ActivationResult(false, ActivationFailureReason.CONDITION_NOT_MET);
    }

    static ActivationResult activateResolvedUsageForTests(
        final NianTouData.Usage usage,
        final IGuEffect effect,
        final double currentEffectivePressure,
        final double pressureCap
    ) {
        return activateResolvedUsage(
            null,
            null,
            usage,
            effect,
            currentEffectivePressure,
            pressureCap
        );
    }

    public enum ActivationFailureReason {
        INVALID_INPUT,
        NO_NIANTOU_DATA,
        USAGE_NOT_ON_ITEM,
        NOT_UNLOCKED,
        NOT_IMPLEMENTED,
        PRESSURE_LIMIT,
        CONDITION_NOT_MET,
    }

    public record ActivationResult(
        boolean success,
        ActivationFailureReason failureReason
    ) {}

    /**
     * 处理“容器内物品变更”导致的装备/卸下事件。
     * <p>
     * 空窍逻辑中部分被动效果依赖 {@link IGuEffect#onEquip} / {@link IGuEffect#onUnequip}
     * 来安装/移除属性修饰符，因此需要对容器内容做快照比对。<br>
     * 该方法同时用于：玩家空窍（{@link KongqiaoInventory}）与 NPC 背包（如 {@code NpcInventory}）。
     * </p>
     *
     * @param user 使用者
     * @param container 被视为“空窍”的容器
     */
    public static void handleContainerEquipChanges(
        LivingEntity user,
        Container container
    ) {
        if (user == null || container == null) {
            return;
        }

        int size = container.getContainerSize();
        if (size <= 0) {
            return;
        }

        final Set<Integer> sealedSlots = resolveRuntimeSealedSlots(user);
        ItemStack[] previous = LAST_KONGQIAO_SNAPSHOT.computeIfAbsent(
            user.getUUID(),
            id -> new ItemStack[size]
        );
        if (previous.length != size) {
            if (previous.length > size) {
                for (int i = size; i < previous.length; i++) {
                    ItemStack last = previous[i] == null
                        ? ItemStack.EMPTY
                        : previous[i];
                    if (!last.isEmpty()) {
                        triggerUnequip(user, last);
                    }
                }
            }

            ItemStack[] resized = new ItemStack[size];
            int copyCount = Math.min(previous.length, size);
            for (int i = 0; i < copyCount; i++) {
                resized[i] = previous[i];
            }
            previous = resized;
            LAST_KONGQIAO_SNAPSHOT.put(user.getUUID(), previous);
        }

        for (int i = 0; i < size; i++) {
            ItemStack current = effectiveEquippedStack(container, i, sealedSlots);
            ItemStack last = previous[i] == null
                ? ItemStack.EMPTY
                : previous[i];

            final EquipTransition transition = determineEquipTransition(last, current);
            if (transition == EquipTransition.NO_CHANGE) {
                previous[i] = current.isEmpty()
                    ? ItemStack.EMPTY
                    : current.copy();
                continue;
            }

            if (transition.shouldUnequip()) {
                triggerUnequip(user, last);
            }
            if (transition.shouldEquip()) {
                triggerEquip(user, current);
            }
            previous[i] = current.isEmpty() ? ItemStack.EMPTY : current.copy();
        }
    }

    static ItemStack effectiveEquippedStack(
        final Container container,
        final int slotIndex,
        final Set<Integer> sealedSlots
    ) {
        if (container == null || slotIndex < 0 || slotIndex >= container.getContainerSize()) {
            return ItemStack.EMPTY;
        }
        if (sealedSlots != null && sealedSlots.contains(slotIndex)) {
            return ItemStack.EMPTY;
        }
        return container.getItem(slotIndex);
    }

    static EquipTransition determineEquipTransition(
        final ItemStack last,
        final ItemStack current
    ) {
        return determineEquipTransitionByVisibility(
            !last.isEmpty(),
            !current.isEmpty(),
            isSameItem(last, current)
        );
    }

    static EquipTransition determineEquipTransitionByVisibility(
        final boolean hadVisibleStack,
        final boolean hasVisibleStack,
        final boolean sameVisibleItem
    ) {
        if (sameVisibleItem) {
            return EquipTransition.NO_CHANGE;
        }
        if (!hadVisibleStack) {
            return EquipTransition.EQUIP_ONLY;
        }
        if (!hasVisibleStack) {
            return EquipTransition.UNEQUIP_ONLY;
        }
        return EquipTransition.REPLACE;
    }

    enum EquipTransition {
        NO_CHANGE(false, false),
        EQUIP_ONLY(false, true),
        UNEQUIP_ONLY(true, false),
        REPLACE(true, true);

        private final boolean shouldUnequip;
        private final boolean shouldEquip;

        EquipTransition(
            final boolean shouldUnequip,
            final boolean shouldEquip
        ) {
            this.shouldUnequip = shouldUnequip;
            this.shouldEquip = shouldEquip;
        }

        boolean shouldUnequip() {
            return shouldUnequip;
        }

        boolean shouldEquip() {
            return shouldEquip;
        }
    }

    private static boolean isSameItem(ItemStack left, ItemStack right) {
        if (left.isEmpty() && right.isEmpty()) {
            return true;
        }
        if (left.isEmpty() != right.isEmpty()) {
            return false;
        }
        return left.getItem() == right.getItem();
    }

    private static void triggerEquip(LivingEntity user, ItemStack stack) {
        NianTouData data = NianTouDataManager.getData(stack);
        if (data == null || data.usages() == null) {
            return;
        }
        for (NianTouData.Usage usage : data.usages()) {
            if (
                !NianTouUnlockChecker.isUsageUnlocked(
                    user,
                    stack,
                    usage.usageID()
                )
            ) {
                continue;
            }
            IGuEffect effect = GuEffectRegistry.get(usage.usageID());
            if (effect != null) {
                try {
                    effect.onEquip(user, stack, usage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void triggerUnequip(LivingEntity user, ItemStack stack) {
        NianTouData data = NianTouDataManager.getData(stack);
        if (data == null || data.usages() == null) {
            return;
        }
        final ActivePassives activePassives = KongqiaoAttachments.getActivePassives(user);
        for (NianTouData.Usage usage : data.usages()) {
            IGuEffect effect = GuEffectRegistry.get(usage.usageID());
            if (effect != null) {
                try {
                    effect.onUnequip(user, stack, usage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            cleanupUnequippedRuntimeActiveState(activePassives, usage);
        }
    }

    static void cleanupUnequippedRuntimeActiveState(
        final ActivePassives activePassives,
        final NianTouData.Usage usage
    ) {
        if (activePassives == null || usage == null) {
            return;
        }
        final String usageId = usage.usageID();
        if (usageId == null || usageId.isBlank()) {
            return;
        }
        activePassives.remove(usageId);
    }

    private static void syncPassiveRuntimeState(
        final LivingEntity user,
        final PassiveRuntimeSnapshot snapshot
    ) {
        final KongqiaoData data = KongqiaoAttachments.getData(user);
        final KongqiaoData.StabilityState stabilityState = data == null
            ? null
            : data.getStabilityState();
        syncPassiveRuntimeState(
            KongqiaoAttachments.getActivePassives(user),
            stabilityState,
            snapshot
        );
    }

    private static void forceDisablePassiveUsage(
        final ActivePassives actives,
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usage,
        final IGuEffect effect
    ) {
        final String usageId = usage.usageID();
        final boolean wasActive = actives != null && actives.isActive(usageId);
        if (wasActive) {
            try {
                effect.onUnequip(user, stack, usage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (actives != null) {
            actives.remove(usageId);
        }
    }
}
