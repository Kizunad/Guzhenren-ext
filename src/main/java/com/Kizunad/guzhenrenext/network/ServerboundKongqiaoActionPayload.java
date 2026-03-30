package com.Kizunad.guzhenrenext.network;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.service.KongqiaoService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 客户端请求空窍相关操作的包。
 */
public record ServerboundKongqiaoActionPayload(Action action)
    implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
        GuzhenrenExt.MODID,
        "kongqiao_action"
    );
    public static final Type<ServerboundKongqiaoActionPayload> TYPE = new Type<>(
        ID
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundKongqiaoActionPayload> STREAM_CODEC =
        StreamCodec.of(
            (buf, payload) -> buf.writeEnum(payload.action),
            buf ->
                new ServerboundKongqiaoActionPayload(
                    buf.readEnum(Action.class)
                )
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            switch (action) {
                case OPEN_KONGQIAO -> {
                    final var owner = KongqiaoService.requireGameplayActivatedData(
                        serverPlayer
                    );
                    if (owner != null) {
                        KongqiaoService.openKongqiaoMenu(serverPlayer, owner);
                    }
                }
                case OPEN_ATTACK -> {
                    final var owner = KongqiaoService.requireGameplayActivatedData(
                        serverPlayer
                    );
                    if (owner != null) {
                        KongqiaoService.openAttackInventoryMenu(serverPlayer, owner);
                    }
                }
                case OPEN_FEED -> {
                    final var owner = KongqiaoService.requireGameplayActivatedData(
                        serverPlayer
                    );
                    if (owner != null) {
                        KongqiaoService.openGuchongFeedMenu(serverPlayer, owner);
                    }
                }
                case EXPAND -> {
                    final var owner = KongqiaoService.requireGameplayActivatedData(
                        serverPlayer
                    );
                    if (owner != null) {
                        KongqiaoService.expand(owner);
                    }
                }
                case SWAP_ATTACK -> {
                    final var owner = KongqiaoService.requireGameplayActivatedData(
                        serverPlayer
                    );
                    if (owner != null) {
                        KongqiaoService.swapAttackInventory(serverPlayer, owner);
                    }
                }
                case OPEN_FORGE -> KongqiaoService.openFlyingSwordForgeMenu(
                        serverPlayer
                    );
                default -> {
                }
            }
        });
    }

    public enum Action {
        OPEN_KONGQIAO,
        OPEN_ATTACK,
        OPEN_FEED,
        EXPAND,
        SWAP_ATTACK,
        OPEN_FORGE,
    }
}
