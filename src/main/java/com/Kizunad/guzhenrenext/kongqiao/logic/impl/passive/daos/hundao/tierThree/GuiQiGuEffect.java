package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierThree;

import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * 三转鬼气蛊：鬼气森森。
 * <p>
 * 逻辑：
 * 1. 消耗真元维持激活状态，注册进 ActivePassives。
 * 2. 每 60 秒自动修复 1 点耐久。
 * </p>
 */
public class GuiQiGuEffect implements IGuEffect {

    public static final String USAGE_ID = "guzhenren:guiqigu_passive_evasion";

    // NBT Keys
    private static final String NBT_REPAIR_TIMER = "GuiQiRepairTimer";

    // Config
    private static final double DEFAULT_BASE_COST = 38400.00; // 3转1阶约 50.0/s
    private static final int REPAIR_INTERVAL_SECONDS = 60;

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public void onSecond(
        LivingEntity user,
        ItemStack stack,
        NianTouData.Usage usageInfo
    ) {
        // 1. 消耗真元
        double baseCost = getMetaDouble(
            usageInfo,
            "zhenyuan_base_cost",
            DEFAULT_BASE_COST
        );
        double realCost = ZhenYuanHelper.calculateGuCost(user, baseCost);

        if (!ZhenYuanHelper.hasEnough(user, realCost)) {
            // 真元不足，从激活列表移除
            KongqiaoAttachments.getActivePassives(user).remove(USAGE_ID);
            return;
        }
        ZhenYuanHelper.modify(user, -realCost);

        // 2. 标记为激活 (供 DamageHandler 使用)
        KongqiaoAttachments.getActivePassives(user).add(USAGE_ID);

        // 3. 自动修复耐久
        if (stack.isDamaged()) {
            int timer = getRepairTimer(stack);
            timer++;
            if (timer >= REPAIR_INTERVAL_SECONDS) {
                stack.setDamageValue(stack.getDamageValue() - 1);
                timer = 0;
                // 播放轻微的修复音效? (可选)
            }
            setRepairTimer(stack, timer);
        }
    }

    private int getRepairTimer(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return 0;
        }
        CompoundTag tag = data.copyTag();
        return tag.getInt(NBT_REPAIR_TIMER);
    }

    private void setRepairTimer(ItemStack stack, int value) {
        CompoundTag tag = stack
            .getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
            .copyTag();
        tag.putInt(NBT_REPAIR_TIMER, value);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private double getMetaDouble(
        NianTouData.Usage usage,
        String key,
        double defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Double.parseDouble(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }
}
