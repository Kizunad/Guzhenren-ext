package com.Kizunad.guzhenrenext.kongqiao.network;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.NianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouDataManager;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouUsageId;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoDataManager;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoId;
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

            final ResourceLocation id;
            try {
                id = ResourceLocation.parse(idString);
            } catch (Exception e) {
                return;
            }

            final NianTouDataManager.UsageLookup lookup =
                NianTouDataManager.findUsageLookup(idString);
            final ShazhaoData shazhaoData = ShazhaoDataManager.get(id);
            if (!isValidUsageLookup(lookup) && shazhaoData == null) {
                serverPlayer.displayClientMessage(
                    Component.literal("未知用途/杀招ID，无法更新配置。"),
                    true
                );
                return;
            }

            final boolean shouldSync = switch (action) {
                case SET_PASSIVE_ENABLED -> handleSetPassiveEnabled(
                    serverPlayer,
                    config,
                    idString,
                    enabled,
                    lookup,
                    shazhaoData
                );
                case ADD_WHEEL_SKILL -> handleAddWheelSkill(
                    serverPlayer,
                    config,
                    idString,
                    id,
                    lookup,
                    shazhaoData
                );
                case REMOVE_WHEEL_SKILL -> handleRemoveWheelSkill(
                    serverPlayer,
                    config,
                    idString,
                    lookup,
                    shazhaoData
                );
                default -> false;
            };

            if (shouldSync) {
                syncToClient(serverPlayer, config);
            }
        });
    }

    private static boolean isValidUsageLookup(
        final NianTouDataManager.UsageLookup lookup
    ) {
        return lookup != null && lookup.data() != null && lookup.usage() != null;
    }

    private static boolean handleSetPassiveEnabled(
        final ServerPlayer serverPlayer,
        final TweakConfig config,
        final String idString,
        final boolean enabled,
        final NianTouDataManager.UsageLookup lookup,
        final ShazhaoData shazhaoData
    ) {
        if (isValidUsageLookup(lookup)) {
            if (!NianTouUsageId.isPassive(idString)) {
                return false;
            }
            config.setPassiveEnabled(idString, enabled);
            if (!enabled) {
                var actives = KongqiaoAttachments.getActivePassives(serverPlayer);
                if (actives != null) {
                    actives.remove(idString);
                }
            }
            return true;
        }
        if (shazhaoData != null) {
            if (!ShazhaoId.isPassive(idString)) {
                return false;
            }
            config.setPassiveEnabled(idString, enabled);
            if (!enabled) {
                var actives = KongqiaoAttachments.getActivePassives(serverPlayer);
                if (actives != null) {
                    actives.remove(idString);
                }
            }
            return true;
        }
        return false;
    }

    private static boolean handleAddWheelSkill(
        final ServerPlayer serverPlayer,
        final TweakConfig config,
        final String idString,
        final ResourceLocation id,
        final NianTouDataManager.UsageLookup lookup,
        final ShazhaoData shazhaoData
    ) {
        if (isValidUsageLookup(lookup)) {
            if (!NianTouUsageId.isActive(idString)) {
                return false;
            }
            final NianTouUnlocks unlocks =
                KongqiaoAttachments.getUnlocks(serverPlayer);
            if (unlocks != null) {
                final String itemId = lookup.data().itemID();
                try {
                    final ResourceLocation item = ResourceLocation.parse(itemId);
                    if (!unlocks.isUsageUnlocked(item, idString)) {
                        serverPlayer.displayClientMessage(
                            Component.literal(
                                "该技能尚未解锁，无法加入轮盘：" +
                                lookup.usage().usageTitle()
                            ),
                            true
                        );
                        return false;
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
                return false;
            }
            serverPlayer.displayClientMessage(
                Component.literal(
                    "已加入轮盘：" +
                    lookup.usage().usageTitle() +
                    " (" +
                    idString +
                    ")"
                ),
                true
            );
            return true;
        }
        if (shazhaoData != null) {
            if (!ShazhaoId.isActive(idString)) {
                return false;
            }
            final NianTouUnlocks unlocks =
                KongqiaoAttachments.getUnlocks(serverPlayer);
            if (unlocks != null && !unlocks.isShazhaoUnlocked(id)) {
                serverPlayer.displayClientMessage(
                    Component.literal(
                        "该杀招尚未解锁，无法加入轮盘：" +
                        shazhaoData.title()
                    ),
                    true
                );
                return false;
            }
            final boolean added = config.addWheelSkill(
                idString,
                TweakConfig.DEFAULT_MAX_WHEEL_SKILLS
            );
            if (!added) {
                serverPlayer.displayClientMessage(
                    Component.literal("轮盘已满或杀招已存在。"),
                    true
                );
                return false;
            }
            serverPlayer.displayClientMessage(
                Component.literal(
                    "已加入轮盘：" +
                    shazhaoData.title() +
                    " (" +
                    idString +
                    ")"
                ),
                true
            );
            return true;
        }
        return false;
    }

    private static boolean handleRemoveWheelSkill(
        final ServerPlayer serverPlayer,
        final TweakConfig config,
        final String idString,
        final NianTouDataManager.UsageLookup lookup,
        final ShazhaoData shazhaoData
    ) {
        if (isValidUsageLookup(lookup)) {
            if (!NianTouUsageId.isActive(idString)) {
                return false;
            }
            final boolean removed = config.removeWheelSkill(idString);
            if (!removed) {
                serverPlayer.displayClientMessage(
                    Component.literal("轮盘中不存在该技能。"),
                    true
                );
                return false;
            }
            serverPlayer.displayClientMessage(
                Component.literal(
                    "已移出轮盘：" +
                    lookup.usage().usageTitle() +
                    " (" +
                    idString +
                    ")"
                ),
                true
            );
            return true;
        }
        if (shazhaoData != null) {
            if (!ShazhaoId.isActive(idString)) {
                return false;
            }
            final boolean removed = config.removeWheelSkill(idString);
            if (!removed) {
                serverPlayer.displayClientMessage(
                    Component.literal("轮盘中不存在该杀招。"),
                    true
                );
                return false;
            }
            serverPlayer.displayClientMessage(
                Component.literal(
                    "已移出轮盘：" +
                    shazhaoData.title() +
                    " (" +
                    idString +
                    ")"
                ),
                true
            );
            return true;
        }
        return false;
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
