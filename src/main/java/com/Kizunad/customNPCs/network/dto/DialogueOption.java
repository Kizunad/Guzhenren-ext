package com.Kizunad.customNPCs.network.dto;

import java.util.Objects;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * 对话选项 DTO, 用于在客户端展示并回传选项标识。
 * text: 展示文本。
 * actionId: 选项对应的动作/处理器标识, 服务端解析后执行。
 * payload: 附加数据字符串 (例如参数), 点击后原样回传给服务端。
 */
public record DialogueOption(Component text, ResourceLocation actionId, String payload) {

    public static final StreamCodec<
        RegistryFriendlyByteBuf,
        DialogueOption
    > STREAM_CODEC = StreamCodec.composite(
        ComponentSerialization.STREAM_CODEC,
        DialogueOption::text,
        ResourceLocation.STREAM_CODEC,
        DialogueOption::actionId,
        ByteBufCodecs.STRING_UTF8,
        DialogueOption::payload,
        DialogueOption::new
    );

    public DialogueOption {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(actionId, "actionId");
        Objects.requireNonNull(payload, "payload");
    }
}
