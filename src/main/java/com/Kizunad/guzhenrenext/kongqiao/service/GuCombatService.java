package com.Kizunad.guzhenrenext.kongqiao.service;

import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoData;
import com.Kizunad.guzhenrenext.kongqiao.inventory.KongqiaoInventory;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouDataManager;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouUnlockChecker;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * 蛊虫战斗逻辑服务。
 * <p>
 * 负责监听实体受伤或攻击事件，并触发空窍中物品的相应逻辑。
 * </p>
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class GuCombatService {

    private GuCombatService() {}

    private record SlotView(Container container, int slotCount) {}

    @SubscribeEvent
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        LivingEntity victim = event.getEntity();
        LivingEntity attacker = null;
        if (event.getSource().getEntity() instanceof LivingEntity sourceEntity) {
            attacker = sourceEntity;
        }

        float amount = event.getAmount();

        // 1. 处理攻击者触发（玩家空窍 / NPC 背包都可视为“空窍”）
        if (attacker != null) {
            SlotView attackerSlots = resolveSlotView(attacker);
            if (attackerSlots != null) {
                amount = handleAttackEffects(
                    attacker,
                    victim,
                    amount,
                    attackerSlots.container(),
                    attackerSlots.slotCount()
                );
            }
        }

        // 2. 处理受害者触发
        // 注意：以攻击者调整后的伤害为基础进行减免/触发
        SlotView victimSlots = resolveSlotView(victim);
        if (victimSlots != null) {
            amount = handleHurtEffects(
                victim,
                event.getSource(),
                amount,
                victimSlots.container(),
                victimSlots.slotCount()
            );
        }

        event.setAmount(amount);
    }

    private static SlotView resolveSlotView(LivingEntity entity) {
        if (entity == null) {
            return null;
        }

        if (entity instanceof CustomNpcEntity npc) {
            var mind = npc.getData(NpcMindAttachment.NPC_MIND);
            if (mind == null || mind.getInventory() == null) {
                return null;
            }
            return new SlotView(mind.getInventory(), mind.getInventory().getMainSize());
        }

        KongqiaoData data = KongqiaoAttachments.getData(entity);
        if (data == null) {
            return null;
        }
        KongqiaoInventory inventory = data.getKongqiaoInventory();
        if (inventory == null) {
            return null;
        }
        return new SlotView(inventory, inventory.getSettings().getUnlockedSlots());
    }

    /**
     * 执行攻击特效。
     */
    private static float handleAttackEffects(
        LivingEntity attacker,
        LivingEntity target,
        float damage,
        Container inventory,
        int slotCount
    ) {
        float currentDamage = damage;
        int maxSlots = Math.min(slotCount, inventory.getContainerSize());

        for (int i = 0; i < maxSlots; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            NianTouData data = NianTouDataManager.getData(stack);
            if (data == null || data.usages() == null) {
                continue;
            }

            for (NianTouData.Usage usage : data.usages()) {
                if (
                    !NianTouUnlockChecker.isUsageUnlocked(
                        attacker,
                        stack,
                        usage.usageID()
                    )
                ) {
                    continue;
                }
                IGuEffect effect = GuEffectRegistry.get(usage.usageID());
                if (effect != null) {
                    try {
                        // TODO: 可以在此处添加真元消耗判定
                        currentDamage = effect.onAttack(
                            attacker,
                            target,
                            currentDamage,
                            stack,
                            usage
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return currentDamage;
    }

    /**
     * 执行受伤特效。
     */
    private static float handleHurtEffects(
        LivingEntity victim,
        net.minecraft.world.damagesource.DamageSource source,
        float damage,
        Container inventory,
        int slotCount
    ) {
        float currentDamage = damage;
        int maxSlots = Math.min(slotCount, inventory.getContainerSize());

        for (int i = 0; i < maxSlots; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            NianTouData data = NianTouDataManager.getData(stack);
            if (data == null || data.usages() == null) {
                continue;
            }

            for (NianTouData.Usage usage : data.usages()) {
                if (
                    !NianTouUnlockChecker.isUsageUnlocked(
                        victim,
                        stack,
                        usage.usageID()
                    )
                ) {
                    continue;
                }
                IGuEffect effect = GuEffectRegistry.get(usage.usageID());
                if (effect != null) {
                    try {
                        // TODO: 可以在此处添加真元消耗判定
                        currentDamage = effect.onHurt(
                            victim,
                            source,
                            currentDamage,
                            stack,
                            usage
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return currentDamage;
    }
}
