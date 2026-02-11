package com.Kizunad.guzhenrenext.kongqiao.flyingsword.forge;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundForgeDaoSyncPayload(
    Map<String, Integer> daoMarks,
    int totalScore,
    String lastMessage
) implements CustomPacketPayload {

    private static final String PACKET_PATH = "forge_dao_sync";

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
        GuzhenrenExt.MODID,
        PACKET_PATH
    );
    public static final Type<ClientboundForgeDaoSyncPayload> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundForgeDaoSyncPayload> STREAM_CODEC =
        StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.daoMarks.size());
                for (Map.Entry<String, Integer> entry : payload.daoMarks.entrySet()) {
                    buf.writeUtf(entry.getKey());
                    buf.writeVarInt(entry.getValue());
                }
                buf.writeVarInt(payload.totalScore);
                buf.writeUtf(payload.lastMessage);
            },
            buf -> {
                int mapSize = buf.readVarInt();
                if (mapSize < 0) {
                    throw new IllegalArgumentException("daoMarks 的大小不能为负数: " + mapSize);
                }
                Map<String, Integer> daoMarks = new HashMap<>(mapSize);
                for (int i = 0; i < mapSize; i++) {
                    String key = buf.readUtf();
                    int value = buf.readVarInt();
                    daoMarks.put(key, value);
                }
                int totalScore = buf.readVarInt();
                String lastMessage = buf.readUtf();
                return new ClientboundForgeDaoSyncPayload(
                    daoMarks,
                    totalScore,
                    lastMessage
                );
            }
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 客户端接收处理：将道痕数据更新到当前打开的锻造菜单缓存中。
     */
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof FlyingSwordForgeScreen forgeScreen) {
                forgeScreen.getMenu().applyDaoSync(
                    this.daoMarks,
                    this.totalScore,
                    this.lastMessage
                );
            }
        });
    }
}
