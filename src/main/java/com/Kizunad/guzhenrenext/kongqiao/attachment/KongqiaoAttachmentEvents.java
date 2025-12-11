package com.Kizunad.guzhenrenext.kongqiao.attachment;

import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.service.KongqiaoCapacityService;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * 负责在实体进入世界时初始化空窍附件，并处理玩家克隆同步。
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID)
public final class KongqiaoAttachmentEvents {

    private KongqiaoAttachmentEvents() {}

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player) && !(entity instanceof CustomNpcEntity)) {
            return;
        }
        ensureAttachment(entity);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player clone = event.getEntity();
        if (clone.level().isClientSide()) {
            return;
        }
        copyKongqiaoData(original, clone);
        copyNianTouUnlocks(original, clone);
    }

    private static void ensureAttachment(Entity entity) {
        if (!entity.hasData(KongqiaoAttachments.KONGQIAO.get())) {
            entity.setData(KongqiaoAttachments.KONGQIAO.get(), new KongqiaoData());
        }
        KongqiaoData data = KongqiaoAttachments.getData(entity);
        if (data != null) {
            data.bind(entity);
            if (
                !entity.level().isClientSide() &&
                entity instanceof LivingEntity living
            ) {
                KongqiaoCapacityService.syncCapacity(living, data);
                if (living instanceof Player) {
                    // 玩家初次进入世界时依旧强制一次同步，保证客户端创建附件。
                    data.markKongqiaoDirty();
                }
            }
        }
        if (entity instanceof Player && !entity.hasData(KongqiaoAttachments.NIANTOU_UNLOCKS.get())) {
            entity.setData(
                KongqiaoAttachments.NIANTOU_UNLOCKS.get(),
                new NianTouUnlocks()
            );
        }
    }

    private static void copyKongqiaoData(Player original, Player clone) {
        KongqiaoData originalData = KongqiaoAttachments.getData(original);
        if (originalData == null) {
            return;
        }
        KongqiaoData newData = new KongqiaoData();
        var provider = clone.level().registryAccess();
        newData.deserializeNBT(provider, originalData.serializeNBT(provider));
        clone.setData(KongqiaoAttachments.KONGQIAO.get(), newData);
        newData.bind(clone);
        newData.markKongqiaoDirty();
    }

    private static void copyNianTouUnlocks(Player original, Player clone) {
        NianTouUnlocks originalUnlocks = KongqiaoAttachments.getUnlocks(original);
        if (originalUnlocks == null) {
            return;
        }
        NianTouUnlocks newUnlocks = new NianTouUnlocks();
        newUnlocks.setUnlockedItems(originalUnlocks.getUnlockedItems());
        clone.setData(KongqiaoAttachments.NIANTOU_UNLOCKS.get(), newUnlocks);
    }
}
