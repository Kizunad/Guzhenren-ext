package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierThree;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Random;

@EventBusSubscriber(modid = GuzhenrenExt.MODID)
public class GuiQiDamageHandler {

    // 硬编码默认值 (实际应从 NianTouData 获取，但事件中获取 Metadata 比较麻烦，这里先用常量或 NBT 传递)
    // 理想情况下，GuiQiGuEffect 应该把配置参数写进 ActivePassives 或 ItemStack NBT
    // 暂时使用默认值
    private static final double BASE_EVASION_CHANCE = 0.20; // 20%
    private static final double CHANCE_PER_1000_DAO_HEN = 0.05; // 每 1000 魂道道痕 +5%
    private static final double MAX_EVASION_CHANCE = 0.75; // 上限 75%
    private static final double DAO_HEN_DIVISOR = 1000.0;
    private static final float BASE_MAX_MITIGATION_FLAT = 300.0f; // 基础减伤上限
    private static final float MAX_MITIGATION_PERCENT = 0.5f;
    private static final double SOUL_COST_PERCENT = 0.10;

    private static final int EFFECT_PARTICLE_COUNT = 10;
    private static final double EFFECT_PARTICLE_OFFSET = 0.5;
    private static final double EFFECT_PARTICLE_SPEED = 0.1;
    private static final float EFFECT_SOUL_ESCAPE_PITCH = 0.5f;
    
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        float originalDamage = event.getOriginalDamage();
        if (originalDamage <= 0) {
            return;
        }

        // 1. 检查激活状态
        ActivePassives actives = KongqiaoAttachments.getActivePassives(player);
        if (actives == null || !actives.isActive(GuiQiGuEffect.USAGE_ID)) {
            return; 
        }

        // 2. 查找物品 (为了扣耐久)
        // 同样，我们需要扫描背包找到那个干活的蛊虫
        String targetItemId = "guzhenren:gushiguchong3_2"; 
        ItemStack activeStack = null;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (stack.getItem().getDescriptionId().equals("item." + targetItemId.replace(":", ".")) 
                || itemKey.toString().equals(targetItemId)) {
                activeStack = stack;
                break;
            }
        }

        if (activeStack == null || activeStack.getDamageValue() >= activeStack.getMaxDamage()) {
            return; // 没找到或已损坏
        }

        // 3. 计算闪避概率：基础 20%，每 1000 魂道道痕 +5%，上限 75%
        double daoHen = DaoHenHelper.getDaoHen(player, DaoHenHelper.DaoType.HUN_DAO);
        double chance = Math.min(
            BASE_EVASION_CHANCE + (daoHen / DAO_HEN_DIVISOR) * CHANCE_PER_1000_DAO_HEN,
            MAX_EVASION_CHANCE
        );
        if (RANDOM.nextDouble() > chance) {
            return; // 未触发
        }

        // 4. 计算减免：减伤上限随魂道道痕增幅
        double mitigationMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            player,
            DaoHenHelper.DaoType.HUN_DAO
        );
        float maxMitigationFlat = (float) (BASE_MAX_MITIGATION_FLAT * mitigationMultiplier);
        float mitigationFlat = Math.min(originalDamage, maxMitigationFlat);
        float mitigationPercent = originalDamage * MAX_MITIGATION_PERCENT;
        
        // 取两者较小值 (限制强度)
        float actualMitigation = Math.min(mitigationFlat, mitigationPercent);
        
        if (actualMitigation <= 0) {
            return;
        }

        // 5. 执行消耗
        // 5a. 扣耐久
        activeStack.setDamageValue(activeStack.getDamageValue() + 1);
        if (activeStack.getDamageValue() >= activeStack.getMaxDamage()) {
            // 损坏了，移除激活状态
            actives.remove(GuiQiGuEffect.USAGE_ID); 
            player.level().playSound(
                null,
                player.blockPosition(),
                net.minecraft.sounds.SoundEvents.ITEM_BREAK,
                net.minecraft.sounds.SoundSource.PLAYERS,
                1.0f,
                1.0f
            );
        }

        // 5b. 扣魂魄
        double soulCost = actualMitigation * SOUL_COST_PERCENT;
        HunPoHelper.modify(player, -soulCost);

        // 6. 应用减伤
        event.setNewDamage(originalDamage - actualMitigation);

        // 7. 特效：鬼气消散
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.SQUID_INK,
                player.getX(),
                player.getY() + 1,
                player.getZ(),
                EFFECT_PARTICLE_COUNT,
                EFFECT_PARTICLE_OFFSET,
                EFFECT_PARTICLE_OFFSET,
                EFFECT_PARTICLE_OFFSET,
                EFFECT_PARTICLE_SPEED
            );
            serverLevel.playSound(
                null,
                player.blockPosition(),
                net.minecraft.sounds.SoundEvents.SOUL_ESCAPE.value(),
                net.minecraft.sounds.SoundSource.PLAYERS,
                1.0f,
                EFFECT_SOUL_ESCAPE_PITCH
            );
        }
    }
}
