package com.Kizunad.customNPCs.events;

import com.Kizunad.customNPCs.CustomNPCsMod;
import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

/**
 * 监听实体使用物品事件, 将进食行为同步到 NpcStatus。
 */
@EventBusSubscriber(modid = CustomNPCsMod.MODID)
public final class NpcConsumptionEvents {

    private NpcConsumptionEvents() {}

    @SubscribeEvent
    public static void onFinishUse(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        if (!entity.hasData(NpcMindAttachment.NPC_MIND)) {
            return;
        }
        ItemStack consumed = event.getItem();
        if (consumed.isEmpty() || consumed.getFoodProperties(entity) == null) {
            return;
        }
        var mind = entity.getData(NpcMindAttachment.NPC_MIND);
        if (mind == null) {
            return;
        }
        mind.getStatus().eat(consumed.copy(), entity);
    }
}
