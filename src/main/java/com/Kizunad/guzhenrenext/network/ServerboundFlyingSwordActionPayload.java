package com.Kizunad.guzhenrenext.network;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordController;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordEntity;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordCooldownAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordPreferencesAttachment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 客户端请求飞剑相关操作的包（Phase 2 最小版）。
 * <p>
 * 说明：当前包只负责“输入→服务端执行”通路；更复杂的同步/动画后续再扩展。
 * </p>
 */
public record ServerboundFlyingSwordActionPayload(Action action)
    implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
        GuzhenrenExt.MODID,
        "flying_sword_action"
    );

    public static final Type<ServerboundFlyingSwordActionPayload> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundFlyingSwordActionPayload> STREAM_CODEC =
        StreamCodec.of(
            (buf, payload) -> buf.writeEnum(payload.action),
            buf -> new ServerboundFlyingSwordActionPayload(buf.readEnum(Action.class))
        );

    private static final String GLOBAL_CD_RECALL = "guzhenrenext:flying_sword/global/recall";
    private static final String GLOBAL_CD_RESTORE = "guzhenrenext:flying_sword/global/restore";

    private static final int COOLDOWN_RECALL_TICKS = 8;
    private static final int COOLDOWN_RESTORE_TICKS = 8;

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
                serverPlayer.sendSystemMessage(Component.literal("[飞剑] 已关闭"));
                return;
            }

            final FlyingSwordCooldownAttachment cooldowns =
                KongqiaoAttachments.getFlyingSwordCooldowns(serverPlayer);
            if (cooldowns == null) {
                return;
            }

            switch (action) {
                case SELECT_NEAREST -> FlyingSwordController.selectNearest(level, serverPlayer);
                case CYCLE_MODE_NEAREST -> {
                    final FlyingSwordEntity sword =
                        FlyingSwordController.getSelectedOrNearestSword(level, serverPlayer);
                    if (sword == null) {
                        serverPlayer.sendSystemMessage(Component.literal("[飞剑] 附近没有飞剑"));
                        return;
                    }
                    final var next = FlyingSwordController.cycleAIMode(sword);
                    serverPlayer.sendSystemMessage(
                        Component.literal("[飞剑] 已切换模式为: " + next.name())
                    );
                }
                case RECALL_NEAREST -> {
                    if (cooldowns.get(GLOBAL_CD_RECALL) > 0) {
                        return;
                    }
                    final FlyingSwordEntity sword =
                        FlyingSwordController.getSelectedOrNearestSword(level, serverPlayer);
                    if (sword == null) {
                        serverPlayer.sendSystemMessage(Component.literal("[飞剑] 附近没有飞剑"));
                        return;
                    }
                    FlyingSwordController.recall(sword);
                    cooldowns.set(GLOBAL_CD_RECALL, COOLDOWN_RECALL_TICKS);
                }
                case RECALL_ALL -> {
                    if (cooldowns.get(GLOBAL_CD_RECALL) > 0) {
                        return;
                    }
                    int count = FlyingSwordController.recallAll(level, serverPlayer);
                    if (count > 0) {
                        cooldowns.set(GLOBAL_CD_RECALL, COOLDOWN_RECALL_TICKS);
                    }
                    serverPlayer.sendSystemMessage(
                        Component.literal("[飞剑] 已请求召回飞剑: " + count)
                    );
                }
                case RESTORE_ONE -> {
                    if (cooldowns.get(GLOBAL_CD_RESTORE) > 0) {
                        return;
                    }
                    int restored = FlyingSwordController.restoreOne(level, serverPlayer);
                    if (restored > 0) {
                        cooldowns.set(GLOBAL_CD_RESTORE, COOLDOWN_RESTORE_TICKS);
                    }
                }
                case RESTORE_ALL -> {
                    if (cooldowns.get(GLOBAL_CD_RESTORE) > 0) {
                        return;
                    }
                    int restored = FlyingSwordController.restoreAll(level, serverPlayer);
                    if (restored > 0) {
                        cooldowns.set(GLOBAL_CD_RESTORE, COOLDOWN_RESTORE_TICKS);
                    }
                }
            }
        });
    }

    public enum Action {
        SELECT_NEAREST,
        CYCLE_MODE_NEAREST,
        RECALL_NEAREST,
        RECALL_ALL,
        RESTORE_ONE,
        RESTORE_ALL,
    }
}
