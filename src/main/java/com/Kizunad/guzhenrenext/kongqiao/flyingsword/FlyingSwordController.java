package com.Kizunad.guzhenrenext.kongqiao.flyingsword;

import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ai.SwordAIMode;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordSelectionAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordStateAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordStorageAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.effects.FlyingSwordEffects;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.BenmingSwordBondService;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.BenmingSwordResourceTransaction;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.CultivationSnapshot;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.resonance.FlyingSwordResonanceType;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ZhuanCostHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/**
 * 飞剑控制接口（最小版）。
 * <p>
 * Phase 2 目标：
 * <ul>
 *     <li>支持查找/选择/切换模式/召回/从存储恢复。</li>
 *     <li>后续再加入护幕、领域、复杂事件钩子等。</li>
 * </ul>
 * </p>
 */
public final class FlyingSwordController {

    private static final double RITUAL_BIND_ZHENYUAN_BASE_COST = 10.0D;
    private static final double RITUAL_BIND_NIANTOU_BASE_COST = 6.0D;
    private static final double RITUAL_BIND_HUNPO_BASE_COST = 4.0D;
    private static final double BURST_ATTEMPT_ZHENYUAN_BASE_COST = 12.0D;
    private static final double BURST_ATTEMPT_NIANTOU_BASE_COST = 8.0D;
    private static final double BURST_ATTEMPT_HUNPO_BASE_COST = 4.0D;
    private static final double BURST_ATTEMPT_OVERLOAD_LIMIT = 100.0D;
    private static final double BURST_ATTEMPT_DEFENSE_STABLE_OVERLOAD_LIMIT = 40.0D;
    private static final double BURST_ATTEMPT_OFFENSE_PRESSURE_OVERLOAD_FLOOR = 80.0D;
    private static final long MINIMAL_BURST_ATTEMPT_COOLDOWN_TICKS = 40L;
    private static final long MINIMAL_BURST_ACTIVE_DURATION_TICKS = 20L;
    private static final long MINIMAL_BURST_AFTERSHOCK_DURATION_TICKS = 20L;

    private FlyingSwordController() {}

    public static List<FlyingSwordEntity> getPlayerSwords(
        ServerLevel level,
        Player owner
    ) {
        if (level == null || owner == null) {
            return List.of();
        }

        List<FlyingSwordEntity> swords = new ArrayList<>();
        AABB searchBox = owner
            .getBoundingBox()
            .inflate(FlyingSwordConstants.SEARCH_RANGE);

        for (Entity entity : level.getEntities(null, searchBox)) {
            if (!(entity instanceof FlyingSwordEntity sword)) {
                continue;
            }
            if (sword.isOwnedBy(owner)) {
                swords.add(sword);
            }
        }

        return swords;
    }

    /**
     * 循环切换 AI 模式（不包含 RECALL）。
     */
    public static SwordAIMode cycleAIMode(FlyingSwordEntity sword) {
        if (sword == null) {
            return SwordAIMode.ORBIT;
        }
        SwordAIMode current = sword.getAIModeEnum();
        SwordAIMode next = current.cycleNext();
        sword.setAIMode(next);

        // 播放模式切换特效
        FlyingSwordEffects.playModeSwitchEffect(sword);

        return next;
    }

    public static void recall(FlyingSwordEntity sword) {
        if (sword == null || sword.isRemoved()) {
            return;
        }
        // Phase 2：一律允许召回；后续再区分"不可召回的主动技能飞剑"。
        sword.setAIMode(SwordAIMode.RECALL);

        // 播放召回特效
        LivingEntity owner = sword.getOwner();
        if (owner instanceof Player player) {
            FlyingSwordEffects.playRecallEffect(sword, player);
        }
    }

    public static int recallAll(ServerLevel level, Player owner) {
        List<FlyingSwordEntity> swords = getPlayerSwords(level, owner);
        for (FlyingSwordEntity sword : swords) {
            recall(sword);
        }
        return swords.size();
    }

    /**
     * 在 RECALL 模式接近主人时调用：写入存储并销毁实体。
     */
    public static void finishRecall(FlyingSwordEntity sword, Player owner) {
        if (sword == null || owner == null || sword.isRemoved()) {
            return;
        }

        FlyingSwordStorageAttachment storage =
            KongqiaoAttachments.getFlyingSwordStorage(owner);
        if (storage == null) {
            sword.discard();
            return;
        }

        // 如果这把飞剑曾经被选中，召回后应清理选中状态。
        if (owner instanceof ServerPlayer player) {
            FlyingSwordSelectionAttachment selection =
                KongqiaoAttachments.getFlyingSwordSelection(player);
            if (selection != null) {
                Optional<UUID> selected = selection.getSelectedSword();
                if (
                    selected.isPresent() &&
                    selected.get().equals(sword.getUUID())
                ) {
                    selection.clear();
                }
            }
        }

        FlyingSwordStorageAttachment.RecalledSword recalled =
            new FlyingSwordStorageAttachment.RecalledSword();
        recalled.quality = sword.getQuality();
        recalled.level = sword.getSwordLevel();
        recalled.experience = sword.getSwordExperience();
        recalled.durability = (float) sword.getSwordAttributes().durability;
        recalled.totalExperience = sword.getSwordAttributes().getGrowthData().getTotalExperience();

        try {
            recalled.displayItem = (net.minecraft.nbt.CompoundTag) sword
                .getDisplayItemStack()
                .save(sword.registryAccess());
        } catch (Exception ignored) {}
        try {
            recalled.attributes = sword.writeAttributesToTag();
        } catch (Exception ignored) {}

        boolean ok = storage.recallSword(recalled);
        if (owner instanceof ServerPlayer player) {
            player.sendSystemMessage(
                Component.literal(
                    ok ? "[飞剑] 召回成功" : "[飞剑] 召回失败：存储已满"
                )
            );
        }
        sword.discard();
    }

    /**
     * 获取最近的一把飞剑。
     */
    @Nullable
    public static FlyingSwordEntity getNearestSword(
        ServerLevel level,
        Player owner
    ) {
        List<FlyingSwordEntity> swords = getPlayerSwords(level, owner);
        if (swords.isEmpty()) {
            return null;
        }
        FlyingSwordEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (FlyingSwordEntity sword : swords) {
            double d = sword.distanceToSqr(owner);
            if (d < best) {
                best = d;
                nearest = sword;
            }
        }
        return nearest;
    }

    /**
     * 选择最近的一把飞剑。
     * <p>
     * 选择结果会写入 {@link com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordSelectionAttachment}。
     * </p>
     */
    public static boolean selectNearest(ServerLevel level, ServerPlayer owner) {
        if (level == null || owner == null) {
            return false;
        }
        FlyingSwordSelectionAttachment selection =
            KongqiaoAttachments.getFlyingSwordSelection(owner);
        if (selection == null) {
            return false;
        }

        FlyingSwordEntity nearest = getNearestSword(level, owner);
        if (nearest == null) {
            selection.clear();
            owner.sendSystemMessage(Component.literal("[飞剑] 附近没有飞剑"));
            return false;
        }

        selection.setSelectedSword(nearest.getUUID());
        owner.sendSystemMessage(Component.literal("[飞剑] 已选中最近飞剑"));
        return true;
    }

    /**
     * 获取“选中”的飞剑；如果选中无效则回退到最近飞剑。
     * <p>
     * 注意：选择系统目前只存 UUID，因此需要从 {@link ServerLevel} 查回实体。
     * </p>
     */
    @Nullable
    public static FlyingSwordEntity getSelectedOrNearestSword(
        ServerLevel level,
        ServerPlayer owner
    ) {
        if (level == null || owner == null) {
            return null;
        }

        FlyingSwordSelectionAttachment selection =
            KongqiaoAttachments.getFlyingSwordSelection(owner);
        return resolveSelectedOrNearestCandidate(
            selection == null ? Optional.empty() : selection.getSelectedSword(),
            selectedUuid -> {
                final Entity entity = level.getEntity(selectedUuid);
                if (
                    entity instanceof FlyingSwordEntity sword
                        && sword.isOwnedBy(owner)
                ) {
                    return sword;
                }
                return null;
            },
            () -> {
                if (selection != null) {
                    selection.clear();
                }
            },
            () -> getNearestSword(level, owner),
            nearestSword -> {
                if (selection != null) {
                    selection.setSelectedSword(nearestSword.getUUID());
                }
            }
        );
    }

    @Nullable
    static FlyingSwordEntity getStrictSelectedSword(
        final ServerLevel level,
        final ServerPlayer owner
    ) {
        if (level == null || owner == null) {
            return null;
        }

        final FlyingSwordSelectionAttachment selection =
            KongqiaoAttachments.getFlyingSwordSelection(owner);
        return resolveStrictSelectedCandidate(
            selection == null ? Optional.empty() : selection.getSelectedSword(),
            selectedUuid -> {
                final Entity entity = level.getEntity(selectedUuid);
                if (
                    entity instanceof FlyingSwordEntity sword
                        && sword.isOwnedBy(owner)
                ) {
                    return sword;
                }
                return null;
            },
            () -> {
                if (selection != null) {
                    selection.clear();
                }
            }
        );
    }

    public static BenmingSwordBondService.Result queryBenmingSword(
        ServerLevel level,
        ServerPlayer owner
    ) {
        if (level == null || owner == null) {
            return BenmingSwordBondService.Result.failure(
                BenmingSwordBondService.ResultBranch.QUERY,
                BenmingSwordBondService.FailureReason.INVALID_REQUEST,
                ""
            );
        }

        final List<BenmingSwordBondService.SwordBondPort> swords = toBondPorts(
            getPlayerSwords(level, owner)
        );
        final BenmingSwordBondService.PlayerBondCachePort cache =
            toBondCachePort(KongqiaoAttachments.getFlyingSwordState(owner));
        return BenmingSwordBondService.queryBoundSword(
            owner.getUUID().toString(),
            swords,
            cache,
            level.getGameTime()
        );
    }

    public static BenmingSwordBondService.Result bindSelectedOrNearestSwordAsBenming(
        ServerLevel level,
        ServerPlayer owner
    ) {
        if (level == null || owner == null) {
            return BenmingSwordBondService.Result.failure(
                BenmingSwordBondService.ResultBranch.RITUAL_PRECHECK,
                BenmingSwordBondService.FailureReason.INVALID_REQUEST,
                ""
            );
        }

        final FlyingSwordEntity sword = getStrictSelectedSword(level, owner);
        if (sword == null) {
            return BenmingSwordBondService.Result.failure(
                BenmingSwordBondService.ResultBranch.RITUAL_PRECHECK,
                BenmingSwordBondService.FailureReason.NO_SELECTED_SWORD,
                ""
            );
        }

        final List<BenmingSwordBondService.SwordBondPort> swords = toBondPorts(
            getPlayerSwords(level, owner)
        );
        final long resolvedTick = level.getGameTime();
        final FlyingSwordStateAttachment state =
            KongqiaoAttachments.getFlyingSwordState(owner);
        final BenmingSwordBondService.PlayerBondCachePort cache =
            toBondCachePort(state);
        return bindSwordAsBenmingWithRitual(
            owner.getUUID().toString(),
            new EntitySwordBondPort(sword),
            swords,
            cache,
            createDefaultRitualBindContext(owner, state, resolvedTick),
            resolvedTick
        );
    }

    static <T> T resolveSelectedOrNearestCandidate(
        Optional<UUID> selectedId,
        Function<UUID, T> selectedResolver,
        Runnable clearSelection,
        Supplier<T> nearestSupplier,
        Consumer<T> rememberNearest
    ) {
        final Optional<UUID> normalizedSelectedId =
            selectedId == null ? Optional.empty() : selectedId;
        if (normalizedSelectedId.isPresent()) {
            final T selected = selectedResolver == null
                ? null
                : selectedResolver.apply(normalizedSelectedId.get());
            if (selected != null) {
                return selected;
            }
            if (clearSelection != null) {
                clearSelection.run();
            }
        }

        final T nearest = nearestSupplier == null ? null : nearestSupplier.get();
        if (nearest != null && rememberNearest != null) {
            rememberNearest.accept(nearest);
        }
        return nearest;
    }

    @Nullable
    static <T> T resolveStrictSelectedCandidate(
        final Optional<UUID> selectedId,
        final Function<UUID, T> selectedResolver,
        final Runnable clearSelection
    ) {
        final Optional<UUID> normalizedSelectedId =
            selectedId == null ? Optional.empty() : selectedId;
        if (normalizedSelectedId.isEmpty()) {
            return null;
        }

        final T selected = selectedResolver == null
            ? null
            : selectedResolver.apply(normalizedSelectedId.get());
        if (selected != null) {
            return selected;
        }
        if (clearSelection != null) {
            clearSelection.run();
        }
        return null;
    }

    static BenmingSwordBondService.Result bindSwordAsBenmingWithRitual(
        String ownerUuid,
        BenmingSwordBondService.SwordBondPort targetSword,
        List<? extends BenmingSwordBondService.SwordBondPort> swords,
        BenmingSwordBondService.PlayerBondCachePort cache,
        BenmingSwordBondService.RitualBindTransactionContext context,
        long resolvedTick
    ) {
        if (context == null || context.requestContext() == null) {
            return BenmingSwordBondService.Result.failure(
                BenmingSwordBondService.ResultBranch.RITUAL_BIND,
                BenmingSwordBondService.FailureReason.INVALID_REQUEST,
                ""
            );
        }

        final BenmingSwordBondService.Result precheckResult =
            BenmingSwordBondService.precheckRitualBind(
                ownerUuid,
                targetSword,
                swords,
                cache,
                context.requestContext(),
                resolvedTick
            );
        if (!precheckResult.success()) {
            return precheckResult;
        }
        return BenmingSwordBondService.ritualBindWithTransaction(
            ownerUuid,
            targetSword,
            swords,
            cache,
            context,
            resolvedTick
        );
    }

    public static BenmingSwordBondService.Result activeUnbindSelectedOrNearestBenmingSword(
        ServerLevel level,
        ServerPlayer owner
    ) {
        if (level == null || owner == null) {
            return BenmingSwordBondService.Result.failure(
                BenmingSwordBondService.ResultBranch.ACTIVE_UNBIND,
                BenmingSwordBondService.FailureReason.INVALID_REQUEST,
                ""
            );
        }

        final FlyingSwordEntity sword = getSelectedOrNearestSword(level, owner);
        if (sword == null) {
            return BenmingSwordBondService.Result.failure(
                BenmingSwordBondService.ResultBranch.ACTIVE_UNBIND,
                BenmingSwordBondService.FailureReason.INVALID_REQUEST,
                ""
            );
        }

        final String ownerUuid = owner.getUUID().toString();
        final String bondOwnerUuid = sword.getSwordAttributes().getBond().getOwnerUuid();
        final String stableSwordId = sword.getSwordAttributes().getStableSwordId();
        if (!ownerUuid.equals(bondOwnerUuid)) {
            return BenmingSwordBondService.Result.failure(
                BenmingSwordBondService.ResultBranch.ACTIVE_UNBIND,
                BenmingSwordBondService.FailureReason.TARGET_NOT_BOUND_TO_PLAYER,
                stableSwordId
            );
        }

        final BenmingSwordResourceTransaction.Result consumeResult =
            BenmingSwordResourceTransaction.tryConsume(
                owner,
                BenmingSwordBondService.defaultActiveUnbindRequest()
            );
        final BenmingSwordBondService.PlayerBondCachePort cache =
            toBondCachePort(KongqiaoAttachments.getFlyingSwordState(owner));
        return BenmingSwordBondService.activeUnbind(
            ownerUuid,
            new EntitySwordBondPort(sword),
            cache,
            consumeResult,
            level.getGameTime()
        );
    }

    public static BenmingSwordBondService.Result forcedUnbindSelectedOrNearestBenmingSword(
        ServerLevel level,
        ServerPlayer owner
    ) {
        if (level == null || owner == null) {
            return BenmingSwordBondService.Result.failure(
                BenmingSwordBondService.ResultBranch.FORCED_UNBIND,
                BenmingSwordBondService.FailureReason.INVALID_REQUEST,
                ""
            );
        }

        final FlyingSwordEntity sword = getSelectedOrNearestSword(level, owner);
        if (sword == null) {
            return BenmingSwordBondService.Result.failure(
                BenmingSwordBondService.ResultBranch.FORCED_UNBIND,
                BenmingSwordBondService.FailureReason.INVALID_REQUEST,
                ""
            );
        }

        final BenmingSwordBondService.PlayerBondCachePort cache =
            toBondCachePort(KongqiaoAttachments.getFlyingSwordState(owner));
        return BenmingSwordBondService.forcedUnbind(
            owner.getUUID().toString(),
            new EntitySwordBondPort(sword),
            cache,
            level.getGameTime()
        );
    }

    public static BenmingControllerActionResult switchResonanceForSelectedOrNearestBenmingSword(
        ServerLevel level,
        ServerPlayer owner,
        @Nullable FlyingSwordResonanceType targetType
    ) {
        if (level == null || owner == null) {
            return BenmingControllerActionResult.failure(
                BenmingControllerAction.RESONANCE_SWITCH,
                BenmingControllerFailureReason.INVALID_REQUEST,
                "",
                "",
                0L
            );
        }

        final FlyingSwordStateAttachment state =
            KongqiaoAttachments.getFlyingSwordState(owner);
        if (state == null) {
            return BenmingControllerActionResult.failure(
                BenmingControllerAction.RESONANCE_SWITCH,
                BenmingControllerFailureReason.STATE_ATTACHMENT_MISSING,
                "",
                "",
                0L
            );
        }

        final FlyingSwordEntity sword = getSelectedOrNearestSword(level, owner);
        if (sword == null) {
            return createMissingStrongSelectedTargetFailure(
                BenmingControllerAction.RESONANCE_SWITCH,
                state
            );
        }

        final String selectedSwordId = sword.getSwordAttributes().getStableSwordId();
        final BenmingSwordBondService.Result queryResult =
            queryBenmingSword(level, owner);
        if (!queryResult.success()) {
            return mapBenmingControllerFailure(
                BenmingControllerAction.RESONANCE_SWITCH,
                state,
                selectedSwordId,
                queryResult
            );
        }
        return switchBenmingSwordResonance(
            state,
            selectedSwordId,
            queryResult.stableSwordId(),
            targetType
        );
    }

    public static BenmingControllerActionResult attemptBurstForSelectedOrNearestBenmingSword(
        ServerLevel level,
        ServerPlayer owner
    ) {
        if (level == null || owner == null) {
            return BenmingControllerActionResult.failure(
                BenmingControllerAction.BURST_ATTEMPT,
                BenmingControllerFailureReason.INVALID_REQUEST,
                "",
                "",
                0L
            );
        }

        final FlyingSwordStateAttachment state =
            KongqiaoAttachments.getFlyingSwordState(owner);
        if (state == null) {
            return BenmingControllerActionResult.failure(
                BenmingControllerAction.BURST_ATTEMPT,
                BenmingControllerFailureReason.STATE_ATTACHMENT_MISSING,
                "",
                "",
                0L
            );
        }

        final FlyingSwordEntity sword = getSelectedOrNearestSword(level, owner);
        if (sword == null) {
            return createMissingStrongSelectedTargetFailure(
                BenmingControllerAction.BURST_ATTEMPT,
                state
            );
        }

        final String selectedSwordId = sword.getSwordAttributes().getStableSwordId();
        final BenmingSwordBondService.Result queryResult =
            queryBenmingSword(level, owner);
        if (!queryResult.success()) {
            return mapBenmingControllerFailure(
                BenmingControllerAction.BURST_ATTEMPT,
                state,
                selectedSwordId,
                queryResult
            );
        }

        final long resolvedTick = level.getGameTime();
        final BenmingControllerActionResult availabilityResult =
            validateBurstAttemptAvailability(
                owner,
                state,
                selectedSwordId,
                queryResult.stableSwordId(),
                resolvedTick
            );
        if (!availabilityResult.success()) {
            return availabilityResult;
        }
        return attemptBenmingSwordBurst(
            state,
            selectedSwordId,
            queryResult.stableSwordId(),
            resolvedTick
        );
    }

    static BenmingControllerActionResult createMissingStrongSelectedTargetFailure(
        final BenmingControllerAction action,
        @Nullable final FlyingSwordStateAttachment state
    ) {
        return BenmingControllerActionResult.failure(
            action,
            BenmingControllerFailureReason.NO_TARGET_SWORD,
            "",
            state == null ? "" : state.getResonanceType(),
            state == null ? 0L : state.getBurstCooldownUntilTick()
        );
    }

    static BenmingControllerActionResult switchBenmingSwordResonance(
        @Nullable final FlyingSwordStateAttachment state,
        final String resolvedSwordId,
        final String bondedSwordId,
        @Nullable final FlyingSwordResonanceType targetType
    ) {
        if (state == null) {
            return BenmingControllerActionResult.failure(
                BenmingControllerAction.RESONANCE_SWITCH,
                BenmingControllerFailureReason.STATE_ATTACHMENT_MISSING,
                "",
                "",
                0L
            );
        }
        if (targetType == null) {
            return BenmingControllerActionResult.failure(
                BenmingControllerAction.RESONANCE_SWITCH,
                BenmingControllerFailureReason.RESONANCE_TYPE_INVALID,
                resolvePrimaryStableSwordId(resolvedSwordId, bondedSwordId),
                state.getResonanceType(),
                state.getBurstCooldownUntilTick()
            );
        }
        if (normalizeSwordId(bondedSwordId).isBlank()) {
            return BenmingControllerActionResult.failure(
                BenmingControllerAction.RESONANCE_SWITCH,
                BenmingControllerFailureReason.NO_BONDED_SWORD,
                resolvePrimaryStableSwordId(resolvedSwordId, bondedSwordId),
                state.getResonanceType(),
                state.getBurstCooldownUntilTick()
            );
        }
        if (!normalizeSwordId(bondedSwordId).equals(normalizeSwordId(resolvedSwordId))) {
            return BenmingControllerActionResult.failure(
                BenmingControllerAction.RESONANCE_SWITCH,
                BenmingControllerFailureReason.TARGET_NOT_CURRENT_BENMING,
                resolvePrimaryStableSwordId(resolvedSwordId, bondedSwordId),
                state.getResonanceType(),
                state.getBurstCooldownUntilTick()
            );
        }

        state.setResonanceType(targetType.getCode());
        return BenmingControllerActionResult.success(
            BenmingControllerAction.RESONANCE_SWITCH,
            normalizeSwordId(resolvedSwordId),
            state.getResonanceType(),
            state.getBurstCooldownUntilTick()
        );
    }

    static BenmingControllerActionResult attemptBenmingSwordBurst(
        @Nullable final FlyingSwordStateAttachment state,
        final String resolvedSwordId,
        final String bondedSwordId,
        final long resolvedTick
    ) {
        if (state == null) {
            return BenmingControllerActionResult.failure(
                BenmingControllerAction.BURST_ATTEMPT,
                BenmingControllerFailureReason.STATE_ATTACHMENT_MISSING,
                "",
                "",
                0L
            );
        }
        if (normalizeSwordId(bondedSwordId).isBlank()) {
            return BenmingControllerActionResult.failure(
                BenmingControllerAction.BURST_ATTEMPT,
                BenmingControllerFailureReason.NO_BONDED_SWORD,
                resolvePrimaryStableSwordId(resolvedSwordId, bondedSwordId),
                state.getResonanceType(),
                state.getBurstCooldownUntilTick()
            );
        }
        if (!normalizeSwordId(bondedSwordId).equals(normalizeSwordId(resolvedSwordId))) {
            return BenmingControllerActionResult.failure(
                BenmingControllerAction.BURST_ATTEMPT,
                BenmingControllerFailureReason.TARGET_NOT_CURRENT_BENMING,
                resolvePrimaryStableSwordId(resolvedSwordId, bondedSwordId),
                state.getResonanceType(),
                state.getBurstCooldownUntilTick()
            );
        }

        final long normalizedTick = Math.max(0L, resolvedTick);
        if (normalizedTick < state.getBurstCooldownUntilTick()) {
            return BenmingControllerActionResult.failure(
                BenmingControllerAction.BURST_ATTEMPT,
                BenmingControllerFailureReason.BURST_COOLDOWN_ACTIVE,
                normalizeSwordId(resolvedSwordId),
                state.getResonanceType(),
                state.getBurstCooldownUntilTick()
            );
        }
        if (!isBurstWindowReady(state, resolvedSwordId, bondedSwordId, normalizedTick)) {
            return BenmingControllerActionResult.failure(
                BenmingControllerAction.BURST_ATTEMPT,
                BenmingControllerFailureReason.BURST_OVERLOAD_BLOCKED,
                normalizeSwordId(resolvedSwordId),
                state.getResonanceType(),
                state.getBurstCooldownUntilTick()
            );
        }

        final long nextBurstCooldown = resolveBurstAttemptCooldownUntilTick(
            normalizedTick
        );
        final long burstActiveUntilTick = resolveBurstActiveUntilTick(normalizedTick);
        final long burstAftershockUntilTick = resolveBurstAftershockUntilTick(
            burstActiveUntilTick
        );
        state.setBurstCooldownUntilTick(nextBurstCooldown);
        state.setBurstActiveUntilTick(burstActiveUntilTick);
        state.setBurstAftershockUntilTick(burstAftershockUntilTick);
        return BenmingControllerActionResult.success(
            BenmingControllerAction.BURST_ATTEMPT,
            normalizeSwordId(resolvedSwordId),
            state.getResonanceType(),
            nextBurstCooldown
        );
    }

    static long resolveBurstActiveUntilTick(final long resolvedTick) {
        return Math.max(0L, resolvedTick) + MINIMAL_BURST_ACTIVE_DURATION_TICKS;
    }

    static long resolveBurstAftershockUntilTick(final long burstActiveUntilTick) {
        return Math.max(0L, burstActiveUntilTick)
            + MINIMAL_BURST_AFTERSHOCK_DURATION_TICKS;
    }

    static long resolveBurstAttemptCooldownUntilTick(final long resolvedTick) {
        return Math.max(0L, resolvedTick) + MINIMAL_BURST_ATTEMPT_COOLDOWN_TICKS;
    }

    public static boolean isBurstWindowReady(
        @Nullable final String resolvedSwordId,
        @Nullable final String bondedSwordId,
        final double overload,
        final long burstCooldownUntilTick,
        final long resolvedTick
    ) {
        final String normalizedResolvedSwordId = normalizeSwordId(resolvedSwordId);
        final String normalizedBondedSwordId = normalizeSwordId(bondedSwordId);
        final long normalizedTick = Math.max(0L, resolvedTick);
        return !normalizedResolvedSwordId.isBlank()
            && !normalizedBondedSwordId.isBlank()
            && normalizedResolvedSwordId.equals(normalizedBondedSwordId)
            && normalizedTick >= Math.max(0L, burstCooldownUntilTick)
            && !isBurstOverloadBlocked(overload);
    }

    static boolean isBurstWindowReady(
        @Nullable final FlyingSwordStateAttachment state,
        final String resolvedSwordId,
        final String bondedSwordId,
        final long resolvedTick
    ) {
        if (state == null) {
            return false;
        }
        return isBurstWindowReady(
            resolvedSwordId,
            bondedSwordId,
            BurstWindowContext.fromState(
                state.getResonanceType(),
                state.getOverload(),
                state.getBurstCooldownUntilTick(),
                state.getOverloadBacklashUntilTick(),
                state.getOverloadRecoveryUntilTick(),
                resolvedTick
            )
        );
    }

    public static boolean isBurstWindowReady(
        @Nullable final String resolvedSwordId,
        @Nullable final String bondedSwordId,
        final BurstWindowContext burstWindowContext
    ) {
        return isBurstWindowReady(
            resolvedSwordId,
            bondedSwordId,
            burstWindowContext.overload(),
            burstWindowContext.burstCooldownUntilTick(),
            burstWindowContext.resolvedTick()
        ) && isBurstRouteWindowReady(
            burstWindowContext
        );
    }

    public record BurstWindowContext(
        @Nullable String resonanceRaw,
        double overload,
        long burstCooldownUntilTick,
        long overloadBacklashUntilTick,
        long overloadRecoveryUntilTick,
        long resolvedTick
    ) {

        public static BurstWindowContext fromState(
            @Nullable final String resonanceRaw,
            final double overload,
            final long burstCooldownUntilTick,
            final long overloadBacklashUntilTick,
            final long overloadRecoveryUntilTick,
            final long resolvedTick
        ) {
            return new BurstWindowContext(
                resonanceRaw,
                overload,
                burstCooldownUntilTick,
                overloadBacklashUntilTick,
                overloadRecoveryUntilTick,
                resolvedTick
            );
        }

        public static BurstWindowContext fromRouteWindowTicks(
            @Nullable final String resonanceRaw,
            final double overload,
            final long burstCooldownUntilTick,
            final long... routeWindowTicks
        ) {
            return new BurstWindowContext(
                resonanceRaw,
                overload,
                burstCooldownUntilTick,
                resolveRouteWindowTick(routeWindowTicks, 0),
                resolveRouteWindowTick(routeWindowTicks, 1),
                resolveRouteWindowTick(routeWindowTicks, 2)
            );
        }
    }

    public static boolean isBurstWindowReady(
        @Nullable final String resolvedSwordId,
        @Nullable final String bondedSwordId,
        @Nullable final String resonanceRaw,
        final double overload,
        final long burstCooldownUntilTick,
        final long... routeWindowTicks
    ) {
        return isBurstWindowReady(
            resolvedSwordId,
            bondedSwordId,
            BurstWindowContext.fromRouteWindowTicks(
                resonanceRaw,
                overload,
                burstCooldownUntilTick,
                routeWindowTicks
            )
        );
    }

    private static boolean isBurstRouteWindowReady(final BurstWindowContext burstWindowContext) {
        final double normalizedOverload = Math.max(0.0D, burstWindowContext.overload());
        final long normalizedTick = Math.max(0L, burstWindowContext.resolvedTick());
        final long normalizedBacklashUntilTick = Math.max(
            0L,
            burstWindowContext.overloadBacklashUntilTick()
        );
        final long normalizedRecoveryUntilTick = Math.max(
            0L,
            burstWindowContext.overloadRecoveryUntilTick()
        );
        return FlyingSwordResonanceType.resolve(burstWindowContext.resonanceRaw())
            .map(type -> switch (type) {
                case OFFENSE ->
                    normalizedOverload >=
                    BURST_ATTEMPT_OFFENSE_PRESSURE_OVERLOAD_FLOOR;
                case DEFENSE ->
                    normalizedOverload < BURST_ATTEMPT_DEFENSE_STABLE_OVERLOAD_LIMIT;
                case SPIRIT ->
                    normalizedOverload >=
                        BURST_ATTEMPT_DEFENSE_STABLE_OVERLOAD_LIMIT
                        && normalizedOverload <
                        BURST_ATTEMPT_OFFENSE_PRESSURE_OVERLOAD_FLOOR;
                case DEVOUR ->
                    normalizedTick >= normalizedBacklashUntilTick
                        && normalizedTick < normalizedRecoveryUntilTick;
            })
            .orElse(true);
    }

    private static long resolveRouteWindowTick(final long[] routeWindowTicks, final int index) {
        if (routeWindowTicks == null || index < 0 || index >= routeWindowTicks.length) {
            return 0L;
        }
        return routeWindowTicks[index];
    }

    private static boolean isBurstOverloadBlocked(final double overload) {
        return Math.max(0.0D, overload) >= BURST_ATTEMPT_OVERLOAD_LIMIT;
    }

    static BenmingControllerActionResult validateBurstAttemptAvailability(
        @Nullable final ServerPlayer owner,
        @Nullable final FlyingSwordStateAttachment state,
        final String resolvedSwordId,
        final String bondedSwordId,
        final long resolvedTick
    ) {
        if (state == null) {
            return BenmingControllerActionResult.failure(
                BenmingControllerAction.BURST_ATTEMPT,
                BenmingControllerFailureReason.STATE_ATTACHMENT_MISSING,
                "",
                "",
                0L
            );
        }
        if (normalizeSwordId(bondedSwordId).isBlank()) {
            return BenmingControllerActionResult.failure(
                BenmingControllerAction.BURST_ATTEMPT,
                BenmingControllerFailureReason.NO_BONDED_SWORD,
                resolvePrimaryStableSwordId(resolvedSwordId, bondedSwordId),
                state.getResonanceType(),
                state.getBurstCooldownUntilTick()
            );
        }
        if (!normalizeSwordId(bondedSwordId).equals(normalizeSwordId(resolvedSwordId))) {
            return BenmingControllerActionResult.failure(
                BenmingControllerAction.BURST_ATTEMPT,
                BenmingControllerFailureReason.TARGET_NOT_CURRENT_BENMING,
                resolvePrimaryStableSwordId(resolvedSwordId, bondedSwordId),
                state.getResonanceType(),
                state.getBurstCooldownUntilTick()
            );
        }

        final long normalizedTick = Math.max(0L, resolvedTick);
        if (normalizedTick < state.getBurstCooldownUntilTick()) {
            return BenmingControllerActionResult.failure(
                BenmingControllerAction.BURST_ATTEMPT,
                BenmingControllerFailureReason.BURST_COOLDOWN_ACTIVE,
                normalizeSwordId(resolvedSwordId),
                state.getResonanceType(),
                state.getBurstCooldownUntilTick()
            );
        }
        if (owner == null) {
            return BenmingControllerActionResult.failure(
                BenmingControllerAction.BURST_ATTEMPT,
                BenmingControllerFailureReason.INVALID_REQUEST,
                normalizeSwordId(resolvedSwordId),
                state.getResonanceType(),
                state.getBurstCooldownUntilTick()
            );
        }
        if (!isBurstWindowReady(state, resolvedSwordId, bondedSwordId, normalizedTick)) {
            return BenmingControllerActionResult.failure(
                BenmingControllerAction.BURST_ATTEMPT,
                BenmingControllerFailureReason.BURST_OVERLOAD_BLOCKED,
                normalizeSwordId(resolvedSwordId),
                state.getResonanceType(),
                state.getBurstCooldownUntilTick()
            );
        }

        final BenmingSwordResourceTransaction.Result burstConsumeResult =
            tryConsumeBurstAttemptResources(owner, state, normalizedTick);
        return mapBurstAttemptAvailabilityResult(
            burstConsumeResult,
            normalizeSwordId(resolvedSwordId),
            state
        );
    }

    static BenmingControllerActionResult mapBurstAttemptAvailabilityResult(
        @Nullable final BenmingSwordResourceTransaction.Result burstConsumeResult,
        final String resolvedSwordId,
        @Nullable final FlyingSwordStateAttachment state
    ) {
        if (burstConsumeResult == null) {
            return BenmingControllerActionResult.failure(
                BenmingControllerAction.BURST_ATTEMPT,
                BenmingControllerFailureReason.INVALID_REQUEST,
                normalizeSwordId(resolvedSwordId),
                state == null ? "" : state.getResonanceType(),
                state == null ? 0L : state.getBurstCooldownUntilTick()
            );
        }
        if (burstConsumeResult.success()) {
            return BenmingControllerActionResult.success(
                BenmingControllerAction.BURST_ATTEMPT,
                normalizeSwordId(resolvedSwordId),
                state == null ? "" : state.getResonanceType(),
                state == null ? 0L : state.getBurstCooldownUntilTick()
            );
        }
        return BenmingControllerActionResult.failure(
            BenmingControllerAction.BURST_ATTEMPT,
            mapBurstAttemptFailureReason(burstConsumeResult.failureReason()),
            normalizeSwordId(resolvedSwordId),
            state == null ? "" : state.getResonanceType(),
            state == null ? 0L : state.getBurstCooldownUntilTick()
        );
    }

    private static BenmingControllerFailureReason mapBurstAttemptFailureReason(
        @Nullable final BenmingSwordResourceTransaction.FailureReason failureReason
    ) {
        if (failureReason == null) {
            return BenmingControllerFailureReason.INVALID_REQUEST;
        }
        return switch (failureReason) {
            case INSUFFICIENT_ZHENYUAN,
                INSUFFICIENT_NIANTOU,
                INSUFFICIENT_HUNPO ->
                BenmingControllerFailureReason.BURST_RESOURCES_INSUFFICIENT;
            case BURST_COOLDOWN_ACTIVE ->
                BenmingControllerFailureReason.BURST_COOLDOWN_ACTIVE;
            case OVERLOAD_LIMIT_EXCEEDED ->
                BenmingControllerFailureReason.BURST_OVERLOAD_BLOCKED;
            case NONE,
                INVALID_ENTITY,
                INVALID_REQUEST,
                ATOMIC_MUTATION_UNSUPPORTED,
                RITUAL_LOCK_ACTIVE,
                ILLEGAL_SWORD_STATE,
                RESOURCE_WRITE_FAILED,
                PHASE_STATE_WRITE_FAILED ->
                BenmingControllerFailureReason.BOND_STATE_INVALID;
        };
    }

    private static BenmingSwordResourceTransaction.Result tryConsumeBurstAttemptResources(
        final ServerPlayer owner,
        final FlyingSwordStateAttachment state,
        final long normalizedTick
    ) {
        return BenmingSwordResourceTransaction.tryConsume(
            owner,
            new BenmingSwordResourceTransaction.Request(
                BURST_ATTEMPT_ZHENYUAN_BASE_COST,
                BURST_ATTEMPT_NIANTOU_BASE_COST,
                BURST_ATTEMPT_HUNPO_BASE_COST,
                BenmingSwordResourceTransaction.PhaseGuard.liveSword(
                    true,
                    true,
                    false,
                    state.getOverload(),
                    true,
                    BURST_ATTEMPT_OVERLOAD_LIMIT,
                    normalizedTick,
                    state.getBurstCooldownUntilTick(),
                    state.getRitualLockUntilTick()
                ),
                BenmingSwordResourceTransaction.PhaseMutation.none()
            ),
            new BurstValidationMutationPort()
        );
    }

    /**
     * 从存储中恢复一把飞剑（优先恢复最前面的可用项）。
     */
    public static int restoreOne(ServerLevel level, ServerPlayer owner) {
        if (level == null || owner == null) {
            return 0;
        }

        final FlyingSwordStorageAttachment storage =
            KongqiaoAttachments.getFlyingSwordStorage(owner);
        if (storage == null || storage.getCount() <= 0) {
            owner.sendSystemMessage(Component.literal("[飞剑] 存储中没有飞剑"));
            return 0;
        }

        for (int i = 0; i < storage.getCount(); i++) {
            final FlyingSwordStorageAttachment.RecalledSword rec =
                storage.getAt(i);
            if (rec == null) {
                storage.remove(i);
                i--;
                continue;
            }
            if (rec.itemWithdrawn) {
                storage.remove(i);
                i--;
                continue;
            }
            final FlyingSwordEntity sword =
                FlyingSwordSpawner.restoreFromStorage(level, owner, rec);
            if (sword != null) {
                storage.remove(i);
                FlyingSwordStateAttachment state =
                    KongqiaoAttachments.getFlyingSwordState(owner);
                if (state != null) {
                    state.markBondCacheDirty();
                }
                FlyingSwordSelectionAttachment selection =
                    KongqiaoAttachments.getFlyingSwordSelection(owner);
                if (selection != null) {
                    selection.setSelectedSword(sword.getUUID());
                }
                owner.sendSystemMessage(
                    Component.literal("[飞剑] 已恢复一把飞剑")
                );
                return 1;
            }
        }

        owner.sendSystemMessage(Component.literal("[飞剑] 没有可恢复的飞剑"));
        return 0;
    }

    /**
     * 从存储中恢复所有可用飞剑。
     */
    public static int restoreAll(ServerLevel level, ServerPlayer owner) {
        if (level == null || owner == null) {
            return 0;
        }

        final FlyingSwordStorageAttachment storage =
            KongqiaoAttachments.getFlyingSwordStorage(owner);
        if (storage == null || storage.getCount() <= 0) {
            owner.sendSystemMessage(Component.literal("[飞剑] 存储中没有飞剑"));
            return 0;
        }

        int restored = 0;
        // 注意：循环中会 remove，所以这里用 i-- 的方式保持索引一致。
        for (int i = 0; i < storage.getCount(); i++) {
            final FlyingSwordStorageAttachment.RecalledSword rec =
                storage.getAt(i);
            if (rec == null) {
                storage.remove(i);
                i--;
                continue;
            }
            if (rec.itemWithdrawn) {
                storage.remove(i);
                i--;
                continue;
            }

            final FlyingSwordEntity sword =
                FlyingSwordSpawner.restoreFromStorage(level, owner, rec);
            if (sword != null) {
                restored++;
                storage.remove(i);
                FlyingSwordStateAttachment state =
                    KongqiaoAttachments.getFlyingSwordState(owner);
                if (state != null) {
                    state.markBondCacheDirty();
                }
                i--;
            }
        }

        owner.sendSystemMessage(
            Component.literal("[飞剑] 已恢复飞剑: " + restored)
        );
        return restored;
    }

    private static List<BenmingSwordBondService.SwordBondPort> toBondPorts(
        List<FlyingSwordEntity> swords
    ) {
        final List<BenmingSwordBondService.SwordBondPort> ports =
            new ArrayList<>();
        if (swords == null) {
            return ports;
        }
        for (FlyingSwordEntity sword : swords) {
            if (sword != null) {
                ports.add(new EntitySwordBondPort(sword));
            }
        }
        return ports;
    }

    private static BenmingSwordBondService.PlayerBondCachePort toBondCachePort(
        FlyingSwordStateAttachment state
    ) {
        return new AttachmentBondCachePort(state);
    }

    private static BenmingSwordBondService.RitualBindTransactionContext createDefaultRitualBindContext(
        ServerPlayer owner,
        @Nullable FlyingSwordStateAttachment state,
        long resolvedTick
    ) {
        final PlayerRitualBindTransactionPort mutationPort =
            new PlayerRitualBindTransactionPort(owner, state);
        return new BenmingSwordBondService.RitualBindTransactionContext(
            CultivationSnapshot.capture(owner),
            createDefaultRitualBindRequest(state, resolvedTick),
            resolveRitualBindZhenyuanCost(owner),
            baseCost -> normalizeNonNegativeCost(
                ZhuanCostHelper.scaleCost(owner, normalizeNonNegativeCost(baseCost))
            ),
            mutationPort,
            new BenmingSwordBondService.RitualRequestContext(
                new SingleUseRitualRequestState(),
                BenmingSwordBondService.defaultRitualDuplicateGuardTicks()
            )
        );
    }

    static BenmingSwordResourceTransaction.Request createDefaultRitualBindRequest(
        @Nullable final FlyingSwordStateAttachment state,
        final long resolvedTick
    ) {
        final double overloadBefore = state == null ? 0.0D : state.getOverload();
        final long burstCooldownUntilTick = state == null
            ? 0L
            : state.getBurstCooldownUntilTick();
        final long ritualLockUntilTick = state == null
            ? 0L
            : state.getRitualLockUntilTick();
        return new BenmingSwordResourceTransaction.Request(
            RITUAL_BIND_ZHENYUAN_BASE_COST,
            RITUAL_BIND_NIANTOU_BASE_COST,
            RITUAL_BIND_HUNPO_BASE_COST,
            BenmingSwordResourceTransaction.PhaseGuard.liveSword(
                true,
                false,
                true,
                overloadBefore,
                false,
                0.0D,
                resolvedTick,
                burstCooldownUntilTick,
                ritualLockUntilTick
            ),
            new BenmingSwordResourceTransaction.PhaseMutation(
                0.0D,
                false,
                burstCooldownUntilTick,
                true,
                resolveRitualBindLockUntilTick(resolvedTick)
            )
        );
    }

    static long resolveRitualBindLockUntilTick(final long resolvedTick) {
        return Math.max(0L, resolvedTick)
            + BenmingSwordBondService.defaultRitualDuplicateGuardTicks();
    }

    private static BenmingControllerActionResult mapBenmingControllerFailure(
        final BenmingControllerAction action,
        final FlyingSwordStateAttachment state,
        final String resolvedSwordId,
        final BenmingSwordBondService.Result queryResult
    ) {
        return BenmingControllerActionResult.failure(
            action,
            mapBenmingControllerFailureReason(queryResult),
            resolvePrimaryStableSwordId(resolvedSwordId, queryResult.stableSwordId()),
            state == null ? "" : state.getResonanceType(),
            state == null ? 0L : state.getBurstCooldownUntilTick()
        );
    }

    private static BenmingControllerFailureReason mapBenmingControllerFailureReason(
        final BenmingSwordBondService.Result queryResult
    ) {
        if (queryResult == null) {
            return BenmingControllerFailureReason.INVALID_REQUEST;
        }
        return switch (queryResult.failureReason()) {
            case NO_BONDED_SWORD -> BenmingControllerFailureReason.NO_BONDED_SWORD;
            case NONE, INVALID_REQUEST -> BenmingControllerFailureReason.INVALID_REQUEST;
            default -> BenmingControllerFailureReason.BOND_STATE_INVALID;
        };
    }

    private static String resolvePrimaryStableSwordId(
        final String resolvedSwordId,
        final String bondedSwordId
    ) {
        final String normalizedResolved = normalizeSwordId(resolvedSwordId);
        if (!normalizedResolved.isBlank()) {
            return normalizedResolved;
        }
        return normalizeSwordId(bondedSwordId);
    }

    private static String normalizeSwordId(final String swordId) {
        return swordId == null ? "" : swordId;
    }

    private static double resolveRitualBindZhenyuanCost(final ServerPlayer owner) {
        return normalizeNonNegativeCost(
            ZhenYuanHelper.calculateGuCost(owner, RITUAL_BIND_ZHENYUAN_BASE_COST)
        );
    }

    private static double normalizeNonNegativeCost(final double cost) {
        if (Double.isNaN(cost) || Double.isInfinite(cost) || cost <= 0.0D) {
            return 0.0D;
        }
        return cost;
    }

    private static final class SingleUseRitualRequestState
        implements BenmingSwordBondService.RitualRequestStatePort {

        private String lockedSwordId = "";
        private long lockedUntilTick;
        private boolean executionPending;

        @Override
        public String getLockedSwordId() {
            return lockedSwordId;
        }

        @Override
        public long getLockedUntilTick() {
            return lockedUntilTick;
        }

        @Override
        public boolean isExecutionPending() {
            return executionPending;
        }

        @Override
        public void beginRitualRequest(final String stableSwordId, final long lockedUntilTick) {
            this.lockedSwordId = stableSwordId == null ? "" : stableSwordId;
            this.lockedUntilTick = Math.max(0L, lockedUntilTick);
            this.executionPending = true;
        }

        @Override
        public void markExecutionConsumed() {
            this.executionPending = false;
        }

        @Override
        public void clearRitualRequest() {
            this.lockedSwordId = "";
            this.lockedUntilTick = 0L;
            this.executionPending = false;
        }
    }

    private static final class PlayerRitualBindTransactionPort
        implements BenmingSwordResourceTransaction.TransactionMutationPort {

        private final ServerPlayer owner;
        @Nullable
        private final FlyingSwordStateAttachment state;

        private PlayerRitualBindTransactionPort(
            final ServerPlayer owner,
            @Nullable final FlyingSwordStateAttachment state
        ) {
            this.owner = owner;
            this.state = state;
        }

        @Override
        public void spendZhenyuan(final double amount) {
            ZhenYuanHelper.modify(owner, -amount);
        }

        @Override
        public void spendNiantou(final double amount) {
            NianTouHelper.modify(owner, -amount);
        }

        @Override
        public void spendHunpo(final double amount) {
            HunPoHelper.modify(owner, -amount);
        }

        @Override
        public boolean supportsResourceRollback() {
            return true;
        }

        @Override
        public boolean supportsPhaseStateWrites() {
            return state != null;
        }

        @Override
        public void refundZhenyuan(final double amount) {
            ZhenYuanHelper.modify(owner, amount);
        }

        @Override
        public void refundNiantou(final double amount) {
            NianTouHelper.modify(owner, amount);
        }

        @Override
        public void refundHunpo(final double amount) {
            HunPoHelper.modify(owner, amount);
        }

        @Override
        public void setOverload(final double overload) {
            if (state != null) {
                state.setOverload(overload);
            }
        }

        @Override
        public void setBurstCooldownUntilTick(final long burstCooldownUntilTick) {
            if (state != null) {
                state.setBurstCooldownUntilTick(burstCooldownUntilTick);
            }
        }

        @Override
        public void setRitualLockUntilTick(final long ritualLockUntilTick) {
            if (state != null) {
                state.setRitualLockUntilTick(ritualLockUntilTick);
            }
        }
    }

    private static final class BurstValidationMutationPort
        implements BenmingSwordResourceTransaction.TransactionMutationPort {

        @Override
        public void spendZhenyuan(final double amount) {}

        @Override
        public void spendNiantou(final double amount) {}

        @Override
        public void spendHunpo(final double amount) {}

        @Override
        public boolean supportsResourceRollback() {
            return true;
        }

        @Override
        public boolean supportsPhaseStateWrites() {
            return false;
        }

        @Override
        public void refundZhenyuan(final double amount) {}

        @Override
        public void refundNiantou(final double amount) {}

        @Override
        public void refundHunpo(final double amount) {}

        @Override
        public void setOverload(final double overload) {}

        @Override
        public void setBurstCooldownUntilTick(final long burstCooldownUntilTick) {}

        @Override
        public void setRitualLockUntilTick(final long ritualLockUntilTick) {}
    }

    private static final class EntitySwordBondPort
        implements BenmingSwordBondService.SwordBondPort {

        private final FlyingSwordEntity sword;

        private EntitySwordBondPort(FlyingSwordEntity sword) {
            this.sword = sword;
        }

        @Override
        public String getStableSwordId() {
            if (sword == null) {
                return "";
            }
            return sword.getSwordAttributes().getStableSwordId();
        }

        @Override
        public String getBondOwnerUuid() {
            if (sword == null) {
                return "";
            }
            return sword.getSwordAttributes().getBond().getOwnerUuid();
        }

        @Override
        public double getBondResonance() {
            if (sword == null) {
                return 0.0D;
            }
            return sword.getSwordAttributes().getBond().getResonance();
        }

        @Override
        public void setBondOwnerUuid(String ownerUuid) {
            if (sword == null) {
                return;
            }
            sword.getSwordAttributes().getBond().setOwnerUuid(ownerUuid);
            sword.syncAttributesToEntityData();
        }

        @Override
        public void setBondResonance(double resonance) {
            if (sword == null) {
                return;
            }
            sword.getSwordAttributes().getBond().setResonance(resonance);
            sword.syncAttributesToEntityData();
        }
    }

    private static final class AttachmentBondCachePort
        implements BenmingSwordBondService.PlayerBondCachePort {

        private final FlyingSwordStateAttachment state;

        private AttachmentBondCachePort(FlyingSwordStateAttachment state) {
            this.state = state;
        }

        @Override
        public String getBondedSwordId() {
            if (state == null) {
                return "";
            }
            return state.getBondedSwordId();
        }

        @Override
        public boolean isBondCacheDirty() {
            return state == null || state.isBondCacheDirty();
        }

        @Override
        public void updateBondCache(String stableSwordId, long resolvedTick) {
            if (state == null) {
                return;
            }
            state.updateBondCache(stableSwordId, resolvedTick);
        }

        @Override
        public void markBondCacheDirty() {
            if (state == null) {
                return;
            }
            state.markBondCacheDirty();
        }

        @Override
        public void clearBondCache() {
            if (state == null) {
                return;
            }
            state.clearBondCache();
        }
    }

    public enum BenmingControllerAction {
        RESONANCE_SWITCH,
        BURST_ATTEMPT,
    }

    public enum BenmingControllerFailureReason {
        NONE,
        INVALID_REQUEST,
        STATE_ATTACHMENT_MISSING,
        NO_TARGET_SWORD,
        NO_BONDED_SWORD,
        TARGET_NOT_CURRENT_BENMING,
        BOND_STATE_INVALID,
        RESONANCE_TYPE_INVALID,
        BURST_COOLDOWN_ACTIVE,
        BURST_RESOURCES_INSUFFICIENT,
        BURST_OVERLOAD_BLOCKED,
    }

    public record BenmingControllerActionResult(
        boolean success,
        BenmingControllerAction action,
        BenmingControllerFailureReason failureReason,
        String stableSwordId,
        String resonanceType,
        long burstCooldownUntilTick
    ) {

        public static BenmingControllerActionResult success(
            final BenmingControllerAction action,
            final String stableSwordId,
            final String resonanceType,
            final long burstCooldownUntilTick
        ) {
            return new BenmingControllerActionResult(
                true,
                action,
                BenmingControllerFailureReason.NONE,
                normalizeSwordId(stableSwordId),
                normalizeSwordId(resonanceType),
                Math.max(0L, burstCooldownUntilTick)
            );
        }

        public static BenmingControllerActionResult failure(
            final BenmingControllerAction action,
            final BenmingControllerFailureReason failureReason,
            final String stableSwordId,
            final String resonanceType,
            final long burstCooldownUntilTick
        ) {
            return new BenmingControllerActionResult(
                false,
                action,
                failureReason == null
                    ? BenmingControllerFailureReason.INVALID_REQUEST
                    : failureReason,
                normalizeSwordId(stableSwordId),
                normalizeSwordId(resonanceType),
                Math.max(0L, burstCooldownUntilTick)
            );
        }
    }
}
