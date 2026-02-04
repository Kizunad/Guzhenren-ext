package com.Kizunad.guzhenrenext.bastion.event;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.entity.BastionGuardianData;
import com.Kizunad.guzhenrenext.bastion.entity.BossRewardData;
import com.Kizunad.guzhenrenext.bastion.service.BastionCaptureService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;

/**
 * Boss 掉落与经验倍率事件。
 * <p>
 * Round 36：根据威胁等级提升 Boss 奖励。
 * 仅在实体被标记为基地 Boss 时生效。
 * </p>
 */
@EventBusSubscriber(modid = com.Kizunad.guzhenrenext.GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class BastionBossLootEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger(BastionBossLootEvents.class);

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

    /**
     * Boss 死亡事件：标记基地为可接管状态。
     * <p>
     * 当属于基地的 Boss 死亡时，调用 CaptureService 尝试将基地标记为可接管。
     * </p>
     */
    @SubscribeEvent
    public static void onBossDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!BossRewardData.isBoss(entity)) {
            return;
        }
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        if (!(entity instanceof Mob mob)) {
            return;
        }

        // 获取 Boss 归属的基地
        UUID bastionId = BastionGuardianData.getBastionId(mob);
        if (bastionId == null) {
            return;
        }

        BastionSavedData savedData = BastionSavedData.get(level);
        BastionData bastion = savedData.getBastion(bastionId);
        if (bastion == null) {
            return;
        }

        // 标记基地为可接管状态
        boolean marked = BastionCaptureService.tryMarkCapturableViaBossDefeat(level, bastion);
        if (marked) {
            LOGGER.info("Boss 死亡，基地 {} 已标记为可接管", bastionId);
        }
    }
}
