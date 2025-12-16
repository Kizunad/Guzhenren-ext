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

        // 1. 处理攻击者触发 (如果攻击者是玩家或拥有空窍的实体)
        if (attacker != null) {
            KongqiaoData attackerData = KongqiaoAttachments.getData(attacker);
            // 只有当攻击者拥有空窍数据时才触发
            if (attackerData != null) {
                float newDamage = handleAttackEffects(
                    attacker,
                    victim,
                    event.getAmount(),
                    attackerData.getKongqiaoInventory()
                );
                event.setAmount(newDamage);
            }
        }

        // 2. 处理受害者触发 (如果受害者拥有空窍)
        // 注意：使用的是更新后的 event.getAmount()；若攻击者增加了伤害，
        // 受害者以新伤害为基础进行减免
        KongqiaoData victimData = KongqiaoAttachments.getData(victim);
        if (victimData != null) {
            float finalDamage = handleHurtEffects(
                victim,
                event.getSource(),
                event.getAmount(),
                victimData.getKongqiaoInventory()
            );
            event.setAmount(finalDamage);
        }
    }

    /**
     * 执行攻击特效。
     */
    private static float handleAttackEffects(
        LivingEntity attacker,
        LivingEntity target,
        float damage,
        KongqiaoInventory inventory
    ) {
        float currentDamage = damage;
        int unlockedSlots = inventory.getSettings().getUnlockedSlots();

        for (int i = 0; i < unlockedSlots; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            NianTouData data = NianTouDataManager.getData(stack);
            if (data == null || data.usages() == null) {
                continue;
            }

            for (NianTouData.Usage usage : data.usages()) {
                if (!NianTouUnlockChecker.isUsageUnlocked(attacker, stack, usage.usageID())) {
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
        KongqiaoInventory inventory
    ) {
        float currentDamage = damage;
        int unlockedSlots = inventory.getSettings().getUnlockedSlots();

        for (int i = 0; i < unlockedSlots; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            NianTouData data = NianTouDataManager.getData(stack);
            if (data == null || data.usages() == null) {
                continue;
            }

            for (NianTouData.Usage usage : data.usages()) {
                if (!NianTouUnlockChecker.isUsageUnlocked(victim, stack, usage.usageID())) {
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
