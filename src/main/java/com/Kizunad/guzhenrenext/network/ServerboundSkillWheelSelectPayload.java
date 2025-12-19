package com.Kizunad.guzhenrenext.network;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoData;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.inventory.KongqiaoInventory;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouDataManager;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouUsageId;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoDataManager;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoId;
import com.Kizunad.guzhenrenext.kongqiao.service.GuRunningService;
import com.Kizunad.guzhenrenext.kongqiao.service.GuRunningService.ActivationFailureReason;
import com.Kizunad.guzhenrenext.kongqiao.service.GuRunningService.ActivationResult;
import com.Kizunad.guzhenrenext.kongqiao.service.ShazhaoActiveService;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 客户端在技能轮盘确认选择后发送到服务端的包。
 * <p>
 * 当前版本发送被选中的 {@code usageId}：
 * 服务端做最小校验后执行对应
 * {@link com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect#onActivate}
 * 主动逻辑（必须是 {@code _active_} 用途）。
 * </p>
 */
public record ServerboundSkillWheelSelectPayload(String selectedUsageId)
    implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
        GuzhenrenExt.MODID,
        "skill_wheel_select"
    );
    public static final Type<ServerboundSkillWheelSelectPayload> TYPE = new Type<>(ID);

    private static final String DADAHUIGU_DERIVE_USAGE_ID =
        "guzhenren:dadahuigu_active_shazhao_derive";

    private static final String DAZHIGU_DERIVE_BEST_USAGE_ID =
        "guzhenren:dazhigu_active_shazhao_derive_best";

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSkillWheelSelectPayload> STREAM_CODEC =
        StreamCodec.of(
            (buf, payload) -> buf.writeUtf(payload.selectedUsageId == null ? "" : payload.selectedUsageId),
            buf -> new ServerboundSkillWheelSelectPayload(buf.readUtf())
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
            if (selectedUsageId == null || selectedUsageId.isBlank()) {
                return;
            }

            final ResourceLocation parsedId;
            try {
                parsedId = ResourceLocation.parse(selectedUsageId);
            } catch (Exception e) {
                return;
            }

            if (ShazhaoId.isActive(selectedUsageId)) {
                handleShazhaoActivation(serverPlayer, selectedUsageId, parsedId);
                return;
            }
            if (!NianTouUsageId.isActive(selectedUsageId)) {
                serverPlayer.sendSystemMessage(
                    Component.literal(
                        "技能轮盘：该用途不是主动技能，无法触发 (" +
                        selectedUsageId +
                        ")"
                    )
                );
                return;
            }

            handleNianTouActivation(serverPlayer, selectedUsageId);
        });
    }

    private static void handleShazhaoActivation(
        final ServerPlayer serverPlayer,
        final String shazhaoId,
        final ResourceLocation parsedId
    ) {
        final String title = resolveShazhaoTitle(shazhaoId, parsedId);
        final TweakConfig tweakConfig =
            KongqiaoAttachments.getTweakConfig(serverPlayer);
        if (tweakConfig == null || !tweakConfig.isInWheel(shazhaoId)) {
            serverPlayer.sendSystemMessage(
                Component.literal(
                    "技能轮盘：该杀招未加入轮盘，无法触发 [" + title + "]"
                )
            );
            return;
        }

        final ShazhaoActiveService.ActivationResult result =
            ShazhaoActiveService.activate(serverPlayer, shazhaoId);
        if (result.success()) {
            serverPlayer.sendSystemMessage(
                Component.literal(
                    "技能轮盘：成功触发 [" +
                    title +
                    "] (" +
                    shazhaoId +
                    ")"
                )
            );
            return;
        }
        switch (result.failureReason()) {
            case NOT_UNLOCKED -> serverPlayer.sendSystemMessage(
                Component.literal(
                    "技能轮盘：该杀招尚未解锁，无法触发 [" + title + "]"
                )
            );
            case NO_DATA -> serverPlayer.sendSystemMessage(
                Component.literal(
                    "技能轮盘：杀招配置不存在，无法触发 [" + title + "]"
                )
            );
            case NOT_IMPLEMENTED -> serverPlayer.sendSystemMessage(
                Component.literal(
                    "技能轮盘：该杀招尚未实现，无法触发 [" + title + "]"
                )
            );
            case CONDITION_NOT_MET -> serverPlayer.sendSystemMessage(
                Component.literal(
                    "技能轮盘：条件不满足，触发失败 [" + title + "]"
                )
            );
            case INVALID_INPUT, NONE -> serverPlayer.sendSystemMessage(
                Component.literal(
                    "技能轮盘：杀招ID非法，无法触发 (" + shazhaoId + ")"
                )
            );
        }
    }

    private static String resolveShazhaoTitle(
        final String shazhaoId,
        final ResourceLocation parsedId
    ) {
        final ShazhaoData data = ShazhaoDataManager.get(parsedId);
        if (data != null && data.title() != null) {
            return data.title();
        }
        return shazhaoId;
    }

    private static void handleNianTouActivation(
        final ServerPlayer serverPlayer,
        final String usageId
    ) {
        final NianTouDataManager.UsageLookup lookup =
            NianTouDataManager.findUsageLookup(usageId);
        final String title = lookup != null && lookup.usage() != null
            ? lookup.usage().usageTitle()
            : usageId;

        if (GuEffectRegistry.get(usageId) == null) {
            serverPlayer.sendSystemMessage(
                Component.literal("技能轮盘：该技能尚未实现，无法触发 [" + title + "]")
            );
            return;
        }

        final TweakConfig tweakConfig =
            KongqiaoAttachments.getTweakConfig(serverPlayer);
        if (tweakConfig == null || !tweakConfig.isInWheel(usageId)) {
            serverPlayer.sendSystemMessage(
                Component.literal("技能轮盘：该技能未加入轮盘，无法触发 [" + title + "]")
            );
            return;
        }

        final KongqiaoData data = KongqiaoAttachments.getData(serverPlayer);
        if (data == null) {
            serverPlayer.sendSystemMessage(
                Component.literal("技能轮盘：未找到空窍数据，无法触发 [" + title + "]")
            );
            return;
        }

        final KongqiaoInventory inventory = data.getKongqiaoInventory();
        if (inventory == null) {
            serverPlayer.sendSystemMessage(
                Component.literal("技能轮盘：未找到空窍背包，无法触发 [" + title + "]")
            );
            return;
        }

        final ActivationSummary summary = tryActivateFromInventory(
            serverPlayer,
            inventory,
            usageId
        );
        if (summary.activated()) {
            if (DADAHUIGU_DERIVE_USAGE_ID.equals(usageId)
                || DAZHIGU_DERIVE_BEST_USAGE_ID.equals(usageId)) {
                final var unlocks = KongqiaoAttachments.getUnlocks(serverPlayer);
                if (unlocks != null) {
                    final String message = unlocks.getShazhaoMessage();
                    if (message != null && !message.isBlank()) {
                        serverPlayer.sendSystemMessage(
                            Component.literal("技能轮盘：" + message)
                        );
                        return;
                    }
                }
            }
            serverPlayer.sendSystemMessage(
                Component.literal(
                    "技能轮盘：成功触发 [" + title + "] (" + usageId + ")"
                )
            );
            return;
        }

        if (!summary.matchedUsage()) {
            serverPlayer.sendSystemMessage(
                Component.literal("技能轮盘：该技能未放入空窍，无法触发 [" + title + "]")
            );
            return;
        }
        if (summary.matchedNotUnlocked()) {
            serverPlayer.sendSystemMessage(
                Component.literal("技能轮盘：该技能尚未解锁，无法触发 [" + title + "]")
            );
            return;
        }
        if (summary.matchedNotImplemented()) {
            serverPlayer.sendSystemMessage(
                Component.literal("技能轮盘：该技能尚未实现，无法触发 [" + title + "]")
            );
            return;
        }
        if (summary.matchedConditionNotMet()) {
            serverPlayer.sendSystemMessage(
                Component.literal(
                    "技能轮盘：条件不满足（资源不足/状态不符），触发失败 [" + title + "]"
                )
            );
            return;
        }

        serverPlayer.sendSystemMessage(
            Component.literal(
                "技能轮盘：触发失败 [" + title + "] (" + usageId + ")"
            )
        );
    }

    private static ActivationSummary tryActivateFromInventory(
        final ServerPlayer serverPlayer,
        final KongqiaoInventory inventory,
        final String usageId
    ) {
        boolean activated = false;
        boolean matchedUsage = false;
        boolean matchedNotUnlocked = false;
        boolean matchedNotImplemented = false;
        boolean matchedConditionNotMet = false;
        int unlockedSlots = inventory.getSettings().getUnlockedSlots();
        for (int i = 0; i < unlockedSlots; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            ActivationResult result = GuRunningService.activateEffectWithResult(
                serverPlayer,
                stack,
                usageId
            );
            if (result.success()) {
                activated = true;
                break;
            }
            if (
                result.failureReason() == ActivationFailureReason.USAGE_NOT_ON_ITEM ||
                result.failureReason() == ActivationFailureReason.NO_NIANTOU_DATA ||
                result.failureReason() == ActivationFailureReason.INVALID_INPUT
            ) {
                continue;
            }

            matchedUsage = true;
            if (result.failureReason() == ActivationFailureReason.NOT_UNLOCKED) {
                matchedNotUnlocked = true;
            } else if (
                result.failureReason() == ActivationFailureReason.NOT_IMPLEMENTED
            ) {
                matchedNotImplemented = true;
            } else if (
                result.failureReason() == ActivationFailureReason.CONDITION_NOT_MET
            ) {
                matchedConditionNotMet = true;
            }
        }

        return new ActivationSummary(
            activated,
            matchedUsage,
            matchedNotUnlocked,
            matchedNotImplemented,
            matchedConditionNotMet
        );
    }

    private record ActivationSummary(
        boolean activated,
        boolean matchedUsage,
        boolean matchedNotUnlocked,
        boolean matchedNotImplemented,
        boolean matchedConditionNotMet
    ) {}
}
