package com.Kizunad.guzhenrenext.bastion.event;

import com.Kizunad.guzhenrenext.bastion.entity.BossRewardData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;

/**
 * Boss 掉落与经验倍率事件。
 * <p>
 * Round 36：根据威胁等级提升 Boss 奖励。
 * 仅在实体被标记为基地 Boss 时生效。
 * </p>
 */
@EventBusSubscriber(modid = com.Kizunad.guzhenrenext.GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class BastionBossLootEvents {

    private BastionBossLootEvents() {
    }

    /**
     * 放大掉落物数量（简单倍乘）。
     */
    @SubscribeEvent
    public static void onBossDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (!BossRewardData.isBoss(entity)) {
            return;
        }
        double multiplier = BossRewardData.getRewardMultiplier(entity);
        if (multiplier <= 1.0d) {
            return;
        }
        if (event.getDrops() == null || event.getDrops().isEmpty()) {
            return;
        }
        for (var drop : event.getDrops()) {
            ItemStack stack = drop.getItem();
            if (stack.isEmpty()) {
                continue;
            }
            int original = stack.getCount();
            int boosted = Math.max(original, (int) Math.round(original * multiplier));
            stack.setCount(boosted);
        }
    }

    /**
     * 放大经验掉落。
     */
    @SubscribeEvent
    public static void onBossXp(LivingExperienceDropEvent event) {
        LivingEntity entity = event.getEntity();
        if (!BossRewardData.isBoss(entity)) {
            return;
        }
        double multiplier = BossRewardData.getRewardMultiplier(entity);
        if (multiplier <= 1.0d) {
            return;
        }
        int original = event.getDroppedExperience();
        int boosted = Math.max(original, (int) Math.round(original * multiplier));
        event.setDroppedExperience(boosted);
    }
}
