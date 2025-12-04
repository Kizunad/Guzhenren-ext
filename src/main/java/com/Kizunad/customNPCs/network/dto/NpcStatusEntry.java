package com.Kizunad.customNPCs.network.dto;

import java.util.Objects;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * 状态面板条目 DTO, 由服务端序列化后发送给客户端 UI。
 * <p>
 * icon: 资源路径, 用于渲染图标或纹理。
 * label/value: 文本组件, 支持本地化与样式。
 * </p>
 */
public record NpcStatusEntry(
    ResourceLocation icon,
    Component label,
    Component value,
    int color
) {

    public static final StreamCodec<
        RegistryFriendlyByteBuf,
        NpcStatusEntry
    > STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC,
        NpcStatusEntry::icon,
        ComponentSerialization.STREAM_CODEC,
        NpcStatusEntry::label,
        ComponentSerialization.STREAM_CODEC,
        NpcStatusEntry::value,
        ByteBufCodecs.INT,
        NpcStatusEntry::color,
        NpcStatusEntry::new
    );

    public NpcStatusEntry {
        Objects.requireNonNull(icon, "icon");
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(value, "value");
    }
}
