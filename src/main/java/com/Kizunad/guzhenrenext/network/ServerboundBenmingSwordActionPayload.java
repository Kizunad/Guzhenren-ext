package com.Kizunad.guzhenrenext.network;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.KongqiaoI18n;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordController;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordController.BenmingControllerActionResult;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordPreferencesAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.BenmingSwordBondService;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.resonance.FlyingSwordResonanceType;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundBenmingSwordActionPayload(BenmingAction action)
    implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
        GuzhenrenExt.MODID,
        "benming_sword_action"
    );

    public static final Type<ServerboundBenmingSwordActionPayload> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundBenmingSwordActionPayload> STREAM_CODEC =
        StreamCodec.of(
            (buf, payload) -> buf.writeEnum(payload.action),
            buf ->
                new ServerboundBenmingSwordActionPayload(
                    buf.readEnum(BenmingAction.class)
                )
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            if (!(serverPlayer.level() instanceof ServerLevel level)) {
                return;
            }

            final FlyingSwordPreferencesAttachment preferences =
                KongqiaoAttachments.getFlyingSwordPreferences(serverPlayer);
            if (preferences != null && !preferences.isEnabled()) {
                serverPlayer.sendSystemMessage(
                    Component.literal(
                        BenmingActionRoutingHelper.localizedText(
                            KongqiaoI18n.BENMING_FEEDBACK_SYSTEM_DISABLED
                        )
                    )
                );
                return;
            }

            final BenmingActionFeedback feedback = BenmingActionRoutingHelper.execute(
                action,
                new LiveBenmingActionExecutor(level, serverPlayer)
            );
            if (BenmingFeedbackDeliveryThrottle.dedupeKey(feedback) != null
                && !BenmingFeedbackDeliveryThrottle.shouldSend(
                    serverPlayer,
                    level.getGameTime(),
                    feedback
                )) {
                return;
            }
            serverPlayer.sendSystemMessage(
                Component.literal(feedback.message())
            );
            final String firstGuideTopic = BenmingFirstGuideDelivery.consumeTopic(
                serverPlayer,
                feedback
            );
            if (firstGuideTopic == null) {
                return;
            }
            serverPlayer.sendSystemMessage(
                Component.literal(
                    BenmingActionRoutingHelper.localizedText(firstGuideTopic)
                )
            );
        });
    }

    private record LiveBenmingActionExecutor(ServerLevel level, ServerPlayer player)
        implements BenmingActionExecutor {

        @Override
        public BenmingSwordBondService.Result query() {
            return FlyingSwordController.queryBenmingSword(level, player);
        }

        @Override
        public BenmingSwordBondService.Result ritualBind() {
            return FlyingSwordController.bindSelectedOrNearestSwordAsBenming(level, player);
        }

        @Override
        public BenmingSwordBondService.Result activeUnbind() {
            return FlyingSwordController.activeUnbindSelectedOrNearestBenmingSword(
                level,
                player
            );
        }

        @Override
        public BenmingSwordBondService.Result forcedUnbind() {
            return FlyingSwordController.forcedUnbindSelectedOrNearestBenmingSword(
                level,
                player
            );
        }

        @Override
        public BenmingControllerActionResult switchResonance() {
            return FlyingSwordController.switchResonanceForSelectedOrNearestBenmingSword(
                level,
                player,
                BenmingActionRoutingHelper.resolveNextResonanceType(
                    KongqiaoAttachments.getFlyingSwordState(player)
                )
            );
        }

        @Override
        public BenmingControllerActionResult attemptBurst() {
            return FlyingSwordController.attemptBurstForSelectedOrNearestBenmingSword(
                level,
                player
            );
        }
    }
}

final class BenmingFirstGuideDelivery {

    private static final String PLAYER_DATA_KEY =
        GuzhenrenExt.MODID + ".benming_first_guide_seen_topics";

    private BenmingFirstGuideDelivery() {}

    static String consumeTopic(
        final ServerPlayer player,
        final BenmingActionFeedback feedback
    ) {
        if (player == null) {
            return null;
        }
        final CompoundTag playerData = player.getPersistentData();
        final CompoundTag seenTopics = playerData.getCompound(PLAYER_DATA_KEY);
        final String topicKey = consumeTopic(feedback, seenTopics);
        if (topicKey == null) {
            return null;
        }
        playerData.put(PLAYER_DATA_KEY, seenTopics);
        return topicKey;
    }

    static String consumeTopic(
        final BenmingActionFeedback feedback,
        final Set<String> seenTopics
    ) {
        final String topicKey = resolveTopic(feedback);
        if (!markFirstSeen(seenTopics, topicKey)) {
            return null;
        }
        return topicKey;
    }

    private static String consumeTopic(
        final BenmingActionFeedback feedback,
        final CompoundTag seenTopics
    ) {
        final String topicKey = resolveTopic(feedback);
        if (!markFirstSeen(seenTopics, topicKey)) {
            return null;
        }
        return topicKey;
    }

    static String resolveTopic(final BenmingActionFeedback feedback) {
        if (feedback == null) {
            return null;
        }
        if (feedback.bondResult() != null) {
            return resolveBondTopic(feedback.action(), feedback.bondResult());
        }
        return resolveControllerTopic(feedback.action(), feedback.controllerResult());
    }

    private static String resolveBondTopic(
        final BenmingAction action,
        final BenmingSwordBondService.Result result
    ) {
        if (action != BenmingAction.RITUAL_BIND || result == null) {
            return null;
        }
        if (result.success()) {
            return KongqiaoI18n.BENMING_GUIDE_AFTER_BOND;
        }
        return KongqiaoI18n.BENMING_GUIDE_BOND_FAIL_NEXT_STEP;
    }

    private static String resolveControllerTopic(
        final BenmingAction action,
        final BenmingControllerActionResult result
    ) {
        if (result == null) {
            return null;
        }
        if (!result.success()) {
            return switch (action) {
                case BURST_ATTEMPT ->
                    result.failureReason() ==
                        FlyingSwordController.BenmingControllerFailureReason.BURST_OVERLOAD_BLOCKED
                        ? KongqiaoI18n.BENMING_GUIDE_OVERLOAD_FIRST_WARNING
                        : null;
                case QUERY,
                    RITUAL_BIND,
                    ACTIVE_UNBIND,
                    FORCED_UNBIND,
                    SWITCH_RESONANCE -> null;
            };
        }
        return switch (action) {
            case SWITCH_RESONANCE ->
                KongqiaoI18n.BENMING_GUIDE_RESONANCE_FIRST_CHOICE;
            case QUERY,
                RITUAL_BIND,
                ACTIVE_UNBIND,
                FORCED_UNBIND,
                BURST_ATTEMPT -> null;
        };
    }

    private static boolean markFirstSeen(
        final CompoundTag seenTopics,
        final String topicKey
    ) {
        final String normalizedTopic = normalizeTopicKey(topicKey);
        if (seenTopics == null || normalizedTopic.isBlank()) {
            return false;
        }
        if (seenTopics.getBoolean(normalizedTopic)) {
            return false;
        }
        seenTopics.putBoolean(normalizedTopic, true);
        return true;
    }

    private static boolean markFirstSeen(
        final Set<String> seenTopics,
        final String topicKey
    ) {
        final String normalizedTopic = normalizeTopicKey(topicKey);
        if (seenTopics == null || normalizedTopic.isBlank()) {
            return false;
        }
        return seenTopics.add(normalizedTopic);
    }

    private static String normalizeTopicKey(final String topicKey) {
        if (topicKey == null) {
            return "";
        }
        return topicKey.trim();
    }
}

final class BenmingFeedbackDeliveryThrottle {

    private static final String PLAYER_DATA_KEY =
        GuzhenrenExt.MODID + ".benming_feedback_delivery_cooldowns";

    private BenmingFeedbackDeliveryThrottle() {}

    static boolean shouldSend(
        final ServerPlayer player,
        final long currentTick,
        final BenmingActionFeedback feedback
    ) {
        final String dedupeKey = dedupeKey(feedback);
        if (dedupeKey == null) {
            return true;
        }
        final CompoundTag playerData = player.getPersistentData();
        final CompoundTag cooldowns = playerData.getCompound(PLAYER_DATA_KEY);
        if (!BenmingFeedbackDeliveryThrottleLogic.shouldSend(
            dedupeKey,
            currentTick,
            cooldowns.getLong(dedupeKey)
        )) {
            return false;
        }
        cooldowns.putLong(
            dedupeKey,
            currentTick + BenmingFeedbackDeliveryThrottleLogic.cooldownWindowTicks()
        );
        playerData.put(PLAYER_DATA_KEY, cooldowns);
        return true;
    }

    static boolean shouldSendWithCooldowns(
        final BenmingActionFeedback feedback,
        final long currentTick,
        final Map<String, Long> cooldowns
    ) {
        return BenmingFeedbackDeliveryThrottleLogic.shouldSendWithCooldowns(
            dedupeKey(feedback),
            currentTick,
            cooldowns
        );
    }

    static String dedupeKey(final BenmingActionFeedback feedback) {
        if (feedback == null || feedback.action() != BenmingAction.BURST_ATTEMPT) {
            return null;
        }
        final BenmingControllerActionResult controllerResult = feedback.controllerResult();
        if (controllerResult == null || controllerResult.success()) {
            return null;
        }
        if (controllerResult.failureReason() == null
            || controllerResult.failureReason()
                == FlyingSwordController.BenmingControllerFailureReason.NONE) {
            return null;
        }
        return "controller."
            + feedback.action().name()
            + "."
            + controllerResult.failureReason().name();
    }

    static long cooldownWindowTicks() {
        return BenmingFeedbackDeliveryThrottleLogic.cooldownWindowTicks();
    }
}

final class BenmingFeedbackDeliveryThrottleLogic {

    private static final long DELIVERY_COOLDOWN_TICKS = 20L;

    private BenmingFeedbackDeliveryThrottleLogic() {}

    static boolean shouldSendWithCooldowns(
        final String dedupeKey,
        final long currentTick,
        final Map<String, Long> cooldowns
    ) {
        if (dedupeKey == null || cooldowns == null) {
            return true;
        }
        if (!shouldSend(dedupeKey, currentTick, cooldowns.getOrDefault(dedupeKey, 0L))) {
            return false;
        }
        cooldowns.put(dedupeKey, currentTick + DELIVERY_COOLDOWN_TICKS);
        return true;
    }

    static long cooldownWindowTicks() {
        return DELIVERY_COOLDOWN_TICKS;
    }

    static boolean shouldSend(
        final String dedupeKey,
        final long currentTick,
        final long nextAllowedTick
    ) {
        if (dedupeKey == null) {
            return true;
        }
        return currentTick >= nextAllowedTick;
    }
}

enum BenmingAction {
    QUERY,
    RITUAL_BIND,
    ACTIVE_UNBIND,
    FORCED_UNBIND,
    SWITCH_RESONANCE,
    BURST_ATTEMPT,
}

interface BenmingActionExecutor {
    BenmingSwordBondService.Result query();

    BenmingSwordBondService.Result ritualBind();

    BenmingSwordBondService.Result activeUnbind();

    BenmingSwordBondService.Result forcedUnbind();

    BenmingControllerActionResult switchResonance();

    BenmingControllerActionResult attemptBurst();
}

final class BenmingActionRoutingHelper {

    private static final String UNKNOWN_LABEL_TRANSLATION_KEY = "key.keyboard.unknown";

    interface ResonanceStateView {
        String resonanceType();
    }

    private BenmingActionRoutingHelper() {}

    static String localizedText(final String key, final Object... args) {
        final String missingValue = UNKNOWN_LABEL_TRANSLATION_KEY.equals(key)
            ? "Unknown"
            : "Unavailable";
        return KongqiaoI18n.localizedTextWithBundledFallback(key, missingValue, args);
    }

    static BenmingActionFeedback execute(
        final BenmingAction action,
        final BenmingActionExecutor executor
    ) {
        if (action == null) {
            return BenmingActionFeedback.bond(invalidRequest(null));
        }
        if (executor == null) {
            return switch (action) {
                case QUERY,
                    RITUAL_BIND,
                    ACTIVE_UNBIND,
                    FORCED_UNBIND -> BenmingActionFeedback.bond(invalidRequest(action));
                case SWITCH_RESONANCE,
                    BURST_ATTEMPT -> BenmingActionFeedback.controller(
                        invalidControllerRequest(action)
                    );
            };
        }
        return switch (action) {
            case QUERY -> BenmingActionFeedback.bond(executor.query());
            case RITUAL_BIND -> BenmingActionFeedback.bond(executor.ritualBind());
            case ACTIVE_UNBIND -> BenmingActionFeedback.bond(executor.activeUnbind());
            case FORCED_UNBIND -> BenmingActionFeedback.bond(executor.forcedUnbind());
            case SWITCH_RESONANCE -> BenmingActionFeedback.controller(
                executor.switchResonance()
            );
            case BURST_ATTEMPT -> BenmingActionFeedback.controller(
                executor.attemptBurst()
            );
        };
    }

    static FlyingSwordResonanceType resolveNextResonanceType(
        final com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordStateAttachment state
    ) {
        return resolveNextResonanceType(
            () -> state == null ? "" : state.getResonanceType()
        );
    }

    static FlyingSwordResonanceType resolveNextResonanceType(
        final ResonanceStateView state
    ) {
        final FlyingSwordResonanceType[] types = FlyingSwordResonanceType.values();
        if (types.length == 0) {
            return null;
        }
        final String rawCurrentType = state == null ? "" : state.resonanceType();
        final FlyingSwordResonanceType currentType = FlyingSwordResonanceType.resolve(
            rawCurrentType
        ).orElse(types[0]);
        final int nextIndex = (currentType.ordinal() + 1) % types.length;
        return types[nextIndex];
    }

    static String feedback(final BenmingActionFeedback feedback) {
        if (feedback == null) {
            return feedback(BenmingActionFeedback.bond(invalidRequest(null)));
        }
        if (feedback.bondResult() != null) {
            return feedbackBond(feedback.action(), feedback.bondResult());
        }
        return feedbackController(feedback.action(), feedback.controllerResult());
    }

    static String feedbackBond(
        final BenmingAction action,
        final BenmingSwordBondService.Result result
    ) {
        final BenmingAction safeAction = action == null ? BenmingAction.QUERY : action;
        final BenmingSwordBondService.Result safeResult =
            result == null ? invalidRequest(safeAction) : result;
        if (safeResult.success()) {
            return successFeedback(safeAction, safeResult);
        }
        return failureFeedback(safeAction, safeResult);
    }

    static String feedbackController(
        final BenmingAction action,
        final BenmingControllerActionResult result
    ) {
        final BenmingAction safeAction = action == null
            ? BenmingAction.SWITCH_RESONANCE
            : action;
        final BenmingControllerActionResult safeResult =
            result == null ? invalidControllerRequest(safeAction) : result;
        if (safeResult.success()) {
            return switch (safeAction) {
                case SWITCH_RESONANCE ->
                    localizedText(
                        KongqiaoI18n.BENMING_FEEDBACK_RESONANCE_SWITCH_SUCCESS,
                        displayResonanceType(safeResult.resonanceType())
                    );
                case BURST_ATTEMPT ->
                    localizedText(KongqiaoI18n.BENMING_FEEDBACK_BURST_ATTEMPT_SUCCESS);
                default -> invalidRequestMessage(safeAction);
            };
        }
        return switch (safeResult.failureReason()) {
            case NONE, INVALID_REQUEST -> invalidRequestMessage(safeAction);
            case STATE_ATTACHMENT_MISSING ->
                localizedText(KongqiaoI18n.BENMING_FEEDBACK_FAIL_STATE_ATTACHMENT_MISSING);
            case NO_TARGET_SWORD ->
                localizedText(KongqiaoI18n.BENMING_FEEDBACK_FAIL_NO_TARGET_SWORD);
            case NO_BONDED_SWORD ->
                localizedText(KongqiaoI18n.BENMING_FEEDBACK_FAIL_NOT_BONDED);
            case TARGET_NOT_CURRENT_BENMING ->
                localizedText(KongqiaoI18n.BENMING_FEEDBACK_FAIL_TARGET_NOT_CURRENT_BENMING);
            case BOND_STATE_INVALID ->
                localizedText(KongqiaoI18n.BENMING_FEEDBACK_FAIL_BOND_STATE_INVALID);
            case RESONANCE_TYPE_INVALID ->
                localizedText(KongqiaoI18n.BENMING_FEEDBACK_FAIL_RESONANCE_TYPE_INVALID);
            case BURST_COOLDOWN_ACTIVE ->
                localizedText(KongqiaoI18n.BENMING_FEEDBACK_FAIL_BURST_COOLDOWN);
            case BURST_RESOURCES_INSUFFICIENT ->
                localizedText(KongqiaoI18n.BENMING_FEEDBACK_FAIL_BURST_RESOURCE);
            case BURST_OVERLOAD_BLOCKED ->
                localizedText(KongqiaoI18n.BENMING_FEEDBACK_FAIL_BURST_OVERLOAD);
        };
    }

    private static String successFeedback(
        final BenmingAction action,
        final BenmingSwordBondService.Result result
    ) {
        final String swordId = displaySwordId(result.stableSwordId());
        return switch (action) {
            case QUERY -> localizedText(KongqiaoI18n.BENMING_FEEDBACK_QUERY_SUCCESS, swordId);
            case RITUAL_BIND ->
                localizedText(KongqiaoI18n.BENMING_FEEDBACK_BOND_SUCCESS, swordId);
            case ACTIVE_UNBIND ->
                localizedText(
                    KongqiaoI18n.BENMING_FEEDBACK_ACTIVE_UNBIND_SUCCESS,
                    swordId
                );
            case FORCED_UNBIND ->
                localizedText(
                    KongqiaoI18n.BENMING_FEEDBACK_FORCED_UNBIND_SUCCESS,
                    swordId
                );
            case SWITCH_RESONANCE, BURST_ATTEMPT -> invalidRequestMessage(action);
        };
    }

    private static String failureFeedback(
        final BenmingAction action,
        final BenmingSwordBondService.Result result
    ) {
        final String swordId = displaySwordId(result.stableSwordId());
        return switch (result.failureReason()) {
            case NONE, INVALID_REQUEST -> invalidRequestMessage(action);
            case NO_SELECTED_SWORD ->
                localizedText(KongqiaoI18n.BENMING_FEEDBACK_FAIL_NO_SELECTED_SWORD);
            case NO_BONDED_SWORD ->
                localizedText(KongqiaoI18n.BENMING_FEEDBACK_FAIL_NOT_BONDED);
            case PLAYER_ALREADY_HAS_BONDED_SWORD ->
                localizedText(KongqiaoI18n.BENMING_FEEDBACK_FAIL_ALREADY_BONDED, swordId);
            case TARGET_BOUND_TO_OTHER_PLAYER ->
                localizedText(KongqiaoI18n.BENMING_FEEDBACK_FAIL_BOUND_TO_OTHER, swordId);
            case TARGET_NOT_BOUND_TO_PLAYER ->
                localizedText(
                    KongqiaoI18n.BENMING_FEEDBACK_FAIL_NOT_PLAYER_BENMING,
                    swordId
                );
            case MULTIPLE_BONDED_SWORDS ->
                localizedText(KongqiaoI18n.BENMING_FEEDBACK_FAIL_MULTIPLE_BONDED);
            case ACTIVE_UNBIND_COST_REJECTED ->
                localizedText(KongqiaoI18n.BENMING_FEEDBACK_FAIL_ACTIVE_UNBIND_COST);
            case RITUAL_RESOURCES_INSUFFICIENT ->
                localizedText(KongqiaoI18n.BENMING_FEEDBACK_FAIL_RITUAL_RESOURCE);
            case RITUAL_STATE_ILLEGAL ->
                localizedText(KongqiaoI18n.BENMING_FEEDBACK_FAIL_RITUAL_STATE);
            case RITUAL_COOLDOWN_ACTIVE ->
                localizedText(KongqiaoI18n.BENMING_FEEDBACK_FAIL_RITUAL_COOLDOWN);
            case RITUAL_DUPLICATE_REQUEST ->
                localizedText(KongqiaoI18n.BENMING_FEEDBACK_FAIL_RITUAL_DUPLICATE);
            case RITUAL_TARGET_MISMATCH ->
                localizedText(KongqiaoI18n.BENMING_FEEDBACK_FAIL_RITUAL_TARGET_MISMATCH);
        };
    }

    private static FlyingSwordController.BenmingControllerActionResult invalidControllerRequest(
        final BenmingAction action
    ) {
        final FlyingSwordController.BenmingControllerAction controllerAction =
            action == BenmingAction.BURST_ATTEMPT
                ? FlyingSwordController.BenmingControllerAction.BURST_ATTEMPT
                : FlyingSwordController.BenmingControllerAction.RESONANCE_SWITCH;
        return FlyingSwordController.BenmingControllerActionResult.failure(
            controllerAction,
            FlyingSwordController.BenmingControllerFailureReason.INVALID_REQUEST,
            "",
            "",
            0L
        );
    }

    private static BenmingSwordBondService.Result invalidRequest(
        final BenmingAction action
    ) {
        return BenmingSwordBondService.Result.failure(
            defaultBranch(action),
            BenmingSwordBondService.FailureReason.INVALID_REQUEST,
            ""
        );
    }

    private static BenmingSwordBondService.ResultBranch defaultBranch(
        final BenmingAction action
    ) {
        if (action == null) {
            return BenmingSwordBondService.ResultBranch.QUERY;
        }
        return switch (action) {
            case QUERY -> BenmingSwordBondService.ResultBranch.QUERY;
            case RITUAL_BIND -> BenmingSwordBondService.ResultBranch.RITUAL_PRECHECK;
            case ACTIVE_UNBIND -> BenmingSwordBondService.ResultBranch.ACTIVE_UNBIND;
            case FORCED_UNBIND -> BenmingSwordBondService.ResultBranch.FORCED_UNBIND;
            case SWITCH_RESONANCE, BURST_ATTEMPT ->
                BenmingSwordBondService.ResultBranch.QUERY;
        };
    }

    private static String invalidRequestMessage(final BenmingAction action) {
        if (action == BenmingAction.QUERY) {
            return localizedText(KongqiaoI18n.BENMING_FEEDBACK_INVALID_QUERY);
        }
        if (action == BenmingAction.SWITCH_RESONANCE) {
            return localizedText(KongqiaoI18n.BENMING_FEEDBACK_INVALID_RESONANCE_SWITCH);
        }
        if (action == BenmingAction.BURST_ATTEMPT) {
            return localizedText(KongqiaoI18n.BENMING_FEEDBACK_INVALID_BURST_ATTEMPT);
        }
        return localizedText(KongqiaoI18n.BENMING_FEEDBACK_INVALID_TARGET);
    }

    private static String displayResonanceType(final String rawType) {
        final FlyingSwordResonanceType resonanceType = FlyingSwordResonanceType.resolve(
            rawType
        ).orElse(FlyingSwordResonanceType.OFFENSE);
        return localizedText(resonanceType.getDisplayNameKey());
    }

    private static String displaySwordId(final String stableSwordId) {
        if (stableSwordId == null || stableSwordId.isBlank()) {
            return localizedText(UNKNOWN_LABEL_TRANSLATION_KEY);
        }
        return stableSwordId;
    }
}

record BenmingActionFeedback(
    BenmingAction action,
    BenmingSwordBondService.Result bondResult,
    BenmingControllerActionResult controllerResult,
    String message
) {

    static BenmingActionFeedback bond(final BenmingSwordBondService.Result result) {
        final BenmingAction action = resolveBondAction(result);
        final String message = BenmingActionRoutingHelper.feedbackBond(action, result);
        return new BenmingActionFeedback(action, result, null, message);
    }

    static BenmingActionFeedback controller(
        final BenmingControllerActionResult result
    ) {
        final BenmingAction action = resolveControllerAction(result);
        final String message = BenmingActionRoutingHelper.feedbackController(action, result);
        return new BenmingActionFeedback(action, null, result, message);
    }

    private static BenmingAction resolveBondAction(
        final BenmingSwordBondService.Result result
    ) {
        if (result == null || result.branch() == null) {
            return BenmingAction.QUERY;
        }
        return switch (result.branch()) {
            case QUERY -> BenmingAction.QUERY;
            case RITUAL_PRECHECK, RITUAL_BIND -> BenmingAction.RITUAL_BIND;
            case ACTIVE_UNBIND -> BenmingAction.ACTIVE_UNBIND;
            case FORCED_UNBIND, ILLEGAL_DETACH -> BenmingAction.FORCED_UNBIND;
            case BIND -> BenmingAction.RITUAL_BIND;
        };
    }

    private static BenmingAction resolveControllerAction(
        final BenmingControllerActionResult result
    ) {
        if (result == null || result.action() == null) {
            return BenmingAction.SWITCH_RESONANCE;
        }
        return switch (result.action()) {
            case RESONANCE_SWITCH -> BenmingAction.SWITCH_RESONANCE;
            case BURST_ATTEMPT -> BenmingAction.BURST_ATTEMPT;
        };
    }
}
