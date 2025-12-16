package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierThree;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = GuzhenrenExt.MODID)
public class GuiYanDamageHandler {

    private static final float DAMAGE_THRESHOLD = 100.0f;
    private static final int STASIS_DURATION_SECONDS = 60;
    private static final int DURABILITY_LOSS = 1;
    private static final float SHIELD_BREAK_PITCH = 0.8f;

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (event.getOriginalDamage() < DAMAGE_THRESHOLD) {
            return;
        }

        // 1. 高效检查：玩家是否激活了鬼炎蛊？
        ActivePassives actives = KongqiaoAttachments.getActivePassives(player);
        if (actives == null || !actives.isActive(GuiYanGuEffect.USAGE_ID)) {
            return; // 未激活，直接返回，无需扫描背包
        }

        // 2. 只有确认激活后，才扫描背包寻找物品实例以修改状态
        // (虽然还是要扫描，但大大减少了无用扫描的次数)
        String targetItemId = "guzhenren:gushiguchong3_1";

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(
                stack.getItem()
            );

            if (
                stack
                    .getItem()
                    .getDescriptionId()
                    .equals("item." + targetItemId.replace(":", ".")) ||
                stack.getItem().toString().equals(targetItemId) ||
                itemKey.toString().equals(targetItemId)
            ) {
                // 再次确认没有处于停滞期 (双重保险)
                if (getStasisTimer(stack) > 0) {
                    continue;
                }

                // 触发破防逻辑
                triggerShieldBreak(player, stack);

                // 重要：立即更新 Attachment 状态，防止同一次攻击中多次触发（如果有多段伤害）
                actives.remove(GuiYanGuEffect.USAGE_ID);
                break;
            }
        }
    }

    private static void triggerShieldBreak(Player player, ItemStack stack) {
        // 1. 扣耐久
        int newDamage = stack.getDamageValue() + DURABILITY_LOSS;
        if (newDamage < stack.getMaxDamage()) {
            stack.setDamageValue(newDamage);
        } else {
            stack.shrink(1); // 彻底损坏
        }

        // 2. 设置停滞计时器
        setStasisTimer(stack, STASIS_DURATION_SECONDS);

        // 3. 播放音效 (破裂声)
        player
            .level()
            .playSound(
                null,
                player.blockPosition(),
                net.minecraft.sounds.SoundEvents.SHIELD_BREAK,
                net.minecraft.sounds.SoundSource.PLAYERS,
                1.0f,
                SHIELD_BREAK_PITCH
            );
    }

    private static int getStasisTimer(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return 0;
        }
        CompoundTag tag = data.copyTag();
        return tag.getInt(GuiYanGuEffect.NBT_STASIS_TIMER);
    }

    private static void setStasisTimer(ItemStack stack, int value) {
        CompoundTag tag = stack
            .getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
            .copyTag();
        tag.putInt(GuiYanGuEffect.NBT_STASIS_TIMER, value);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}
