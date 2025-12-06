package com.Kizunad.customNPCs.network;

import com.Kizunad.customNPCs.CustomNPCsMod;
import com.Kizunad.customNPCs.ai.interaction.MaterialWorkService;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * C2S：请求将当前界面输入转化为材料点。
 */
public record RequestMaterialConversionPayload(int npcEntityId)
    implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<
        RequestMaterialConversionPayload
    > TYPE = new CustomPacketPayload.Type<>(
        ResourceLocation.fromNamespaceAndPath(
            CustomNPCsMod.MODID,
            "request_material_conversion"
        )
    );

    public static final StreamCodec<
        RegistryFriendlyByteBuf,
        RequestMaterialConversionPayload
    > STREAM_CODEC = StreamCodec.of(
        RequestMaterialConversionPayload::write,
        RequestMaterialConversionPayload::read
    );

    private static void write(
        RegistryFriendlyByteBuf buf,
        RequestMaterialConversionPayload payload
    ) {
        buf.writeVarInt(payload.npcEntityId);
    }

    private static RequestMaterialConversionPayload read(
        RegistryFriendlyByteBuf buf
    ) {
        return new RequestMaterialConversionPayload(buf.readVarInt());
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
        RequestMaterialConversionPayload payload,
        IPayloadContext context
    ) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            var level = serverPlayer.serverLevel();
            var entity = level.getEntity(payload.npcEntityId());
            if (entity instanceof CustomNpcEntity npc) {
                MaterialWorkService.handleConversionFromMenu(
                    npc,
                    serverPlayer
                );
            }
        });
    }
}
