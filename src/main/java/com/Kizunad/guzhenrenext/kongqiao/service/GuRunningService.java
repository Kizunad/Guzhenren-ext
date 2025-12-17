package com.Kizunad.guzhenrenext.kongqiao.service;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoData;
import com.Kizunad.guzhenrenext.kongqiao.inventory.KongqiaoInventory;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouDataManager;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouUnlockChecker;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
@EventBusSubscriber(
    modid = GuzhenrenExt.MODID,
    bus = EventBusSubscriber.Bus.GAME
)
public final class GuRunningService {

    private static final int TICKS_PER_SECOND = 20;
    private static final Map<UUID, ItemStack[]> LAST_KONGQIAO_SNAPSHOT =
        new HashMap<>();

    private GuRunningService() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        KongqiaoData data = KongqiaoAttachments.getData(player);
        if (data == null) {
            return;
        }

        boolean isSecond = (player.tickCount % TICKS_PER_SECOND == 0);
        handleEquipChanges(player, data.getKongqiaoInventory());
        tickKongqiaoEffects(player, data.getKongqiaoInventory(), isSecond);
    }

    /**
     * 遍历空窍，执行所有物品的被动逻辑。
     */
    public static void tickKongqiaoEffects(
        LivingEntity user,
        KongqiaoInventory inventory,
        boolean isSecond
    ) {
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
                if (
                    !NianTouUnlockChecker.isUsageUnlocked(
                        user,
                        stack,
                        usage.usageID()
                    )
                ) {
                    continue;
                }
                IGuEffect effect = GuEffectRegistry.get(usage.usageID());
                if (effect != null) {
                    try {
                        // TODO: 可以在这里添加真元消耗判定 (costDuration, costTotalNiantou)

                        // 每 Tick 逻辑
                        effect.onTick(user, stack, usage);

                        // 每秒逻辑
                        if (isSecond) {
                            effect.onSecond(user, stack, usage);
                        }
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
    public static boolean activateEffect(
        LivingEntity user,
        ItemStack stack,
        String usageId
    ) {
        return activateEffectWithResult(user, stack, usageId).success();
    }

    /**
     * 触发物品主动逻辑，并返回失败原因（用于 UI/轮盘提示）。
     *
     * @param user     使用者
     * @param stack    物品
     * @param usageId  要触发的用途ID
     * @return 触发结果（成功/失败原因）
     */
    public static ActivationResult activateEffectWithResult(
        LivingEntity user,
        ItemStack stack,
        String usageId
    ) {
        if (stack == null || stack.isEmpty() || usageId == null || usageId.isBlank()) {
            return new ActivationResult(false, ActivationFailureReason.INVALID_INPUT);
        }

        NianTouData data = NianTouDataManager.getData(stack);
        if (data == null || data.usages() == null) {
            return new ActivationResult(false, ActivationFailureReason.NO_NIANTOU_DATA);
        }

        for (NianTouData.Usage usage : data.usages()) {
            if (!usageId.equals(usage.usageID())) {
                continue;
            }

            if (!NianTouUnlockChecker.isUsageUnlocked(user, stack, usageId)) {
                return new ActivationResult(false, ActivationFailureReason.NOT_UNLOCKED);
            }

            IGuEffect effect = GuEffectRegistry.get(usageId);
            if (effect == null) {
                return new ActivationResult(false, ActivationFailureReason.NOT_IMPLEMENTED);
            }

            // TODO: 检查并扣除一次性消耗 (costTotalNiantou等)
            final boolean success = effect.onActivate(user, stack, usage);
            if (success) {
                return new ActivationResult(true, null);
            }
            return new ActivationResult(false, ActivationFailureReason.CONDITION_NOT_MET);
        }

        return new ActivationResult(false, ActivationFailureReason.USAGE_NOT_ON_ITEM);
    }

    public enum ActivationFailureReason {
        INVALID_INPUT,
        NO_NIANTOU_DATA,
        USAGE_NOT_ON_ITEM,
        NOT_UNLOCKED,
        NOT_IMPLEMENTED,
        CONDITION_NOT_MET,
    }

    public record ActivationResult(
        boolean success,
        ActivationFailureReason failureReason
    ) {}

    private static void handleEquipChanges(
        ServerPlayer player,
        KongqiaoInventory inventory
    ) {
        int size = inventory.getContainerSize();
        ItemStack[] previous = LAST_KONGQIAO_SNAPSHOT.computeIfAbsent(
            player.getUUID(),
            id -> new ItemStack[size]
        );
        if (previous.length != size) {
            previous = new ItemStack[size];
            LAST_KONGQIAO_SNAPSHOT.put(player.getUUID(), previous);
        }

        for (int i = 0; i < size; i++) {
            ItemStack current = inventory.getItem(i);
            ItemStack last = previous[i] == null
                ? ItemStack.EMPTY
                : previous[i];

            if (isSameItem(last, current)) {
                previous[i] = current.isEmpty()
                    ? ItemStack.EMPTY
                    : current.copy();
                continue;
            }

            if (!last.isEmpty()) {
                triggerUnequip(player, last);
            }
            if (!current.isEmpty()) {
                triggerEquip(player, current);
            }
            previous[i] = current.isEmpty() ? ItemStack.EMPTY : current.copy();
        }
    }

    private static boolean isSameItem(ItemStack left, ItemStack right) {
        if (left.isEmpty() && right.isEmpty()) {
            return true;
        }
        if (left.isEmpty() != right.isEmpty()) {
            return false;
        }
        return left.getItem() == right.getItem();
    }

    private static void triggerEquip(LivingEntity user, ItemStack stack) {
        NianTouData data = NianTouDataManager.getData(stack);
        if (data == null || data.usages() == null) {
            return;
        }
        for (NianTouData.Usage usage : data.usages()) {
            if (
                !NianTouUnlockChecker.isUsageUnlocked(
                    user,
                    stack,
                    usage.usageID()
                )
            ) {
                continue;
            }
            IGuEffect effect = GuEffectRegistry.get(usage.usageID());
            if (effect != null) {
                try {
                    effect.onEquip(user, stack, usage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void triggerUnequip(LivingEntity user, ItemStack stack) {
        NianTouData data = NianTouDataManager.getData(stack);
        if (data == null || data.usages() == null) {
            return;
        }
        for (NianTouData.Usage usage : data.usages()) {
            IGuEffect effect = GuEffectRegistry.get(usage.usageID());
            if (effect != null) {
                try {
                    effect.onUnequip(user, stack, usage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
