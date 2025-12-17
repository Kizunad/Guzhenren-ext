package com.Kizunad.guzhenrenext.kongqiao.network;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.NianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouDataManager;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouUsageId;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 客户端更新 TweakConfig 的请求包。
 * <p>
 * 该包只负责“玩家偏好”，服务端需要做最小校验并回传 {@link PacketSyncTweakConfig} 覆盖客户端状态。
 * </p>
 */
public record ServerboundTweakConfigUpdatePayload(
    Action action,
    String usageId,
    boolean enabled
) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
        GuzhenrenExt.MODID,
        "tweak_config_update"
    );
    public static final Type<ServerboundTweakConfigUpdatePayload> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundTweakConfigUpdatePayload> STREAM_CODEC =
        StreamCodec.of(
            (buf, payload) -> {
                buf.writeEnum(payload.action);
                buf.writeUtf(payload.usageId == null ? "" : payload.usageId);
                buf.writeBoolean(payload.enabled);
            },
            buf ->
                new ServerboundTweakConfigUpdatePayload(
                    buf.readEnum(Action.class),
                    buf.readUtf(),
                    buf.readBoolean()
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

            final TweakConfig config = KongqiaoAttachments.getTweakConfig(serverPlayer);
            if (config == null) {
                return;
            }

            if (action == Action.REQUEST_SYNC) {
                syncToClient(serverPlayer, config);
                return;
            }

            final String idString = usageId == null ? "" : usageId;
            if (idString.isBlank()) {
                return;
            }

            try {
                ResourceLocation.parse(idString);
            } catch (Exception e) {
                return;
            }

            final NianTouDataManager.UsageLookup lookup =
                NianTouDataManager.findUsageLookup(idString);
            if (lookup == null || lookup.data() == null || lookup.usage() == null) {
                serverPlayer.displayClientMessage(
                    Component.literal("未知用途ID，无法更新配置。"),
                    true
                );
                return;
            }

            switch (action) {
                case SET_PASSIVE_ENABLED -> {
                    if (!NianTouUsageId.isPassive(idString)) {
                        return;
                    }
                    config.setPassiveEnabled(idString, enabled);
                    if (!enabled) {
                        var actives = KongqiaoAttachments.getActivePassives(serverPlayer);
                        if (actives != null) {
                            actives.remove(idString);
                        }
                    }
                }
                case ADD_WHEEL_SKILL -> {
                    if (!NianTouUsageId.isActive(idString)) {
                        return;
                    }
                    final NianTouUnlocks unlocks = KongqiaoAttachments.getUnlocks(
                        serverPlayer
                    );
                    if (unlocks != null) {
                        final String itemId = lookup.data().itemID();
                        try {
                            final ResourceLocation item =
                                ResourceLocation.parse(itemId);
                            if (!unlocks.isUsageUnlocked(item, idString)) {
                                serverPlayer.displayClientMessage(
                                    Component.literal(
                                        "该技能尚未解锁，无法加入轮盘："
                                            + lookup.usage().usageTitle()
                                    ),
                                    true
                                );
                                return;
                            }
                        } catch (Exception e) {
                            // 忽略：数据异常时不阻断玩家配置，但可能在触发时表现为无效
                        }
                    }
                    final boolean added = config.addWheelSkill(
                        idString,
                        TweakConfig.DEFAULT_MAX_WHEEL_SKILLS
                    );
                    if (!added) {
                        serverPlayer.displayClientMessage(
                            Component.literal("轮盘已满或技能已存在。"),
                            true
                        );
                        return;
                    }
                    serverPlayer.displayClientMessage(
                        Component.literal(
                            "已加入轮盘："
                                + lookup.usage().usageTitle()
                                + " ("
                                + idString
                                + ")"
                        ),
                        true
                    );
                }
                case REMOVE_WHEEL_SKILL -> {
                    if (!NianTouUsageId.isActive(idString)) {
                        return;
                    }
                    final boolean removed = config.removeWheelSkill(idString);
                    if (!removed) {
                        serverPlayer.displayClientMessage(
                            Component.literal("轮盘中不存在该技能。"),
                            true
                        );
                        return;
                    }
                    serverPlayer.displayClientMessage(
                        Component.literal(
                            "已移出轮盘："
                                + lookup.usage().usageTitle()
                                + " ("
                                + idString
                                + ")"
                        ),
                        true
                    );
                }
                default -> {
                }
            }

            syncToClient(serverPlayer, config);
        });
    }

    private static void syncToClient(final ServerPlayer player, final TweakConfig config) {
        PacketDistributor.sendToPlayer(player, new PacketSyncTweakConfig(config));
    }

    public enum Action {
        REQUEST_SYNC,
        SET_PASSIVE_ENABLED,
        ADD_WHEEL_SKILL,
        REMOVE_WHEEL_SKILL,
    }
}
