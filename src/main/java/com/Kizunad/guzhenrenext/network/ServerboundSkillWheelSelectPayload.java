package com.Kizunad.guzhenrenext.network;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoData;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.inventory.KongqiaoInventory;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouDataManager;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouUsageId;
import com.Kizunad.guzhenrenext.kongqiao.service.GuRunningService;
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

            try {
                ResourceLocation.parse(selectedUsageId);
            } catch (Exception e) {
                return;
            }
            if (!NianTouUsageId.isActive(selectedUsageId)) {
                return;
            }
            if (GuEffectRegistry.get(selectedUsageId) == null) {
                return;
            }

            final NianTouDataManager.UsageLookup lookup =
                NianTouDataManager.findUsageLookup(selectedUsageId);
            final String title = lookup != null && lookup.usage() != null
                ? lookup.usage().usageTitle()
                : selectedUsageId;

            TweakConfig tweakConfig = KongqiaoAttachments.getTweakConfig(serverPlayer);
            if (tweakConfig == null || !tweakConfig.isInWheel(selectedUsageId)) {
                serverPlayer.sendSystemMessage(
                    Component.literal("技能轮盘：该技能未加入轮盘，无法触发 [" + title + "]")
                );
                return;
            }

            KongqiaoData data = KongqiaoAttachments.getData(serverPlayer);
            if (data == null) {
                serverPlayer.sendSystemMessage(
                    Component.literal("技能轮盘：未找到空窍数据，无法触发 [" + title + "]")
                );
                return;
            }

            KongqiaoInventory inventory = data.getKongqiaoInventory();
            if (inventory == null) {
                serverPlayer.sendSystemMessage(
                    Component.literal("技能轮盘：未找到空窍背包，无法触发 [" + title + "]")
                );
                return;
            }

            boolean activated = false;
            int unlockedSlots = inventory.getSettings().getUnlockedSlots();
            for (int i = 0; i < unlockedSlots; i++) {
                ItemStack stack = inventory.getItem(i);
                if (stack.isEmpty()) {
                    continue;
                }
                if (GuRunningService.activateEffect(serverPlayer, stack, selectedUsageId)) {
                    activated = true;
                    break;
                }
            }

            if (activated) {
                serverPlayer.sendSystemMessage(
                    Component.literal("技能轮盘：成功触发 [" + title + "] (" + selectedUsageId + ")")
                );
                return;
            }

            serverPlayer.sendSystemMessage(
                Component.literal(
                    "技能轮盘：触发失败（未解锁/未在空窍/未实现 onActivate）[" + title + "]"
                )
            );
        });
    }
}
