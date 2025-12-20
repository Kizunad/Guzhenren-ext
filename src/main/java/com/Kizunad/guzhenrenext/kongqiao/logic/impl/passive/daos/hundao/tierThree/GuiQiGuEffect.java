package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierThree;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
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
    private static final double DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND = 220.0;
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
        if (user.level().isClientSide()) {
            return;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(user);
        if (config != null && !config.isPassiveEnabled(USAGE_ID)) {
            KongqiaoAttachments.getActivePassives(user).remove(USAGE_ID);
            return;
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.HUN_DAO
        );
        final double niantouCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_NIANTOU_COST_PER_SECOND,
                0.0
            )
        );
        final double jingliCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_JINGLI_COST_PER_SECOND,
                0.0
            )
        );
        final double hunpoCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_HUNPO_COST_PER_SECOND,
                0.0
            ) * selfMultiplier
        );
        final double zhenyuanBaseCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_ZHENYUAN_BASE_COST_PER_SECOND,
                DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND
            )
        );
        if (
            !GuEffectCostHelper.tryConsumeSustain(
                user,
                niantouCostPerSecond,
                jingliCostPerSecond,
                hunpoCostPerSecond,
                zhenyuanBaseCostPerSecond
            )
        ) {
            KongqiaoAttachments.getActivePassives(user).remove(USAGE_ID);
            return;
        }

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

}
