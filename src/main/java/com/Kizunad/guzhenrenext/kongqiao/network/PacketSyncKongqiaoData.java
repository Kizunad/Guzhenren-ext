package com.Kizunad.guzhenrenext.kongqiao.network;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouDataManager;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoDataManager;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 同步念头/杀招数据到客户端，用于显示描述与调整页面。
 */
public record PacketSyncKongqiaoData(
    List<NianTouData> nianTouData,
    List<ShazhaoData> shazhaoData
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketSyncKongqiaoData> TYPE =
        new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(
                GuzhenrenExt.MODID,
                "sync_kongqiao_data"
            )
        );

    public static final StreamCodec<ByteBuf, PacketSyncKongqiaoData> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public PacketSyncKongqiaoData decode(ByteBuf buffer) {
                FriendlyByteBuf buf = new FriendlyByteBuf(buffer);
                List<NianTouData> nianTou =
                    decodeList(buf, NianTouData.CODEC);
                List<ShazhaoData> shazhao =
                    decodeList(buf, ShazhaoData.CODEC);
                return new PacketSyncKongqiaoData(nianTou, shazhao);
            }

            @Override
            public void encode(ByteBuf buffer, PacketSyncKongqiaoData value) {
                FriendlyByteBuf buf = new FriendlyByteBuf(buffer);
                encodeList(buf, value.nianTouData(), NianTouData.CODEC);
                encodeList(buf, value.shazhaoData(), ShazhaoData.CODEC);
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
        PacketSyncKongqiaoData payload,
        IPayloadContext context
    ) {
        context.enqueueWork(() -> {
            NianTouDataManager.clear();
            for (NianTouData data : payload.nianTouData()) {
                if (data != null) {
                    NianTouDataManager.register(data);
                }
            }
            ShazhaoDataManager.clear();
            for (ShazhaoData data : payload.shazhaoData()) {
                if (data != null) {
                    ShazhaoDataManager.register(data);
                }
            }
        });
    }

    private static <T> void encodeList(
        FriendlyByteBuf buf,
        List<T> values,
        Codec<T> codec
    ) {
        if (values == null || values.isEmpty()) {
            buf.writeVarInt(0);
            return;
        }
        buf.writeVarInt(values.size());
        for (T value : values) {
            if (value == null) {
                buf.writeNbt(null);
                continue;
            }
            Tag tag = codec
                .encodeStart(NbtOps.INSTANCE, value)
                .getOrThrow(error ->
                    new IllegalStateException("数据编码失败: " + error)
                );
            buf.writeNbt(tag);
        }
    }

    private static <T> List<T> decodeList(
        FriendlyByteBuf buf,
        Codec<T> codec
    ) {
        int size = buf.readVarInt();
        List<T> values = new ArrayList<>(Math.max(0, size));
        for (int i = 0; i < size; i++) {
            CompoundTag tag = buf.readNbt();
            if (tag == null) {
                continue;
            }
            try {
                T value =
                    codec.parse(NbtOps.INSTANCE, tag)
                        .getOrThrow(error ->
                            new IllegalStateException(
                                "数据解析失败: " + error
                            )
                        );
                values.add(value);
            } catch (Exception e) {
                // 忽略解析失败的数据，避免客户端崩溃
            }
        }
        return values;
    }
}
