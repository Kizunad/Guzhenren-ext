package com.Kizunad.guzhenrenext.kongqiao.service;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoData;
import com.Kizunad.guzhenrenext.kongqiao.inventory.KongqiaoInventory;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouDataManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 蛊虫被动逻辑运行时服务。
 * <p>
 * 负责在服务端 Tick 时遍历玩家空窍内的物品，并触发相应的被动逻辑。
 * </p>
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class GuRunningService {

    private GuRunningService() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // 简单频率控制：每 20 tick (1秒) 执行一次被动逻辑，避免性能开销过大
        // 如果需要更实时的效果，可以去掉这个判断或针对特定效果优化
        if (player.tickCount % 20 != 0) {
            return;
        }

        KongqiaoData data = KongqiaoAttachments.getData(player);
        if (data == null) {
            return;
        }

        tickKongqiaoEffects(player, data.getKongqiaoInventory());
    }

    /**
     * 遍历空窍，执行所有物品的被动逻辑。
     */
    public static void tickKongqiaoEffects(LivingEntity user, KongqiaoInventory inventory) {
        int unlockedSlots = inventory.getSettings().getUnlockedSlots();
        for (int i = 0; i < unlockedSlots; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            NianTouData niantouData = NianTouDataManager.getData(stack);
            if (niantouData == null || niantouData.usages() == null) {
                continue;
            }

            for (NianTouData.Usage usage : niantouData.usages()) {
                IGuEffect effect = GuEffectRegistry.get(usage.usageID());
                if (effect != null) {
                    try {
                        // TODO: 可以在这里添加真元消耗判定 (costDuration, costTotalNiantou)
                        // 如果是持续消耗型被动，需要检查并扣除资源，资源不足则跳过
                        effect.onTick(user, stack, usage);
                    } catch (Exception e) {
                        // 防止单个蛊虫逻辑崩溃影响整个循环
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 触发物品的主动逻辑。
     *
     * @param user     使用者
     * @param stack    物品
     * @param usageId  要触发的用途ID
     * @return 是否成功触发
     */
    public static boolean activateEffect(LivingEntity user, ItemStack stack, String usageId) {
        NianTouData data = NianTouDataManager.getData(stack);
        if (data == null) return false;

        for (NianTouData.Usage usage : data.usages()) {
            if (usage.usageID().equals(usageId)) {
                IGuEffect effect = GuEffectRegistry.get(usageId);
                if (effect != null) {
                    // TODO: 检查并扣除一次性消耗 (costTotalNiantou等)
                    return effect.onActivate(user, stack, usage);
                }
            }
        }
        return false;
    }
}
