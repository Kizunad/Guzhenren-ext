package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.tierOne;

import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 一转随意蛊：主动【随意转机】。
 * <p>
 * 消耗念头（与少量真元）发动一次“转机”，随机获得三种结果之一：\n
 * - 迅捷：短暂移速提升\n
 * - 坚韧：短暂抗性提升\n
 * - 回春：瞬间治疗少量生命\n
 * </p>
 */
public class SuiYiGuRandomShiftEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_NIANTOU_COST = "niantou_cost";
    private static final String META_ZHENYUAN_BASE_COST = "zhenyuan_base_cost";
    private static final String META_BUFF_DURATION_TICKS = "buff_duration_ticks";
    private static final String META_BUFF_AMPLIFIER = "buff_amplifier";
    private static final String META_HEAL_AMOUNT = "heal_amount";

    private static final int DEFAULT_COOLDOWN_TICKS = 240;
    private static final int DEFAULT_BUFF_DURATION_TICKS = 120;
    private static final int DEFAULT_BUFF_AMPLIFIER = 0;
    private static final double DEFAULT_HEAL_AMOUNT = 4.0;

    private final String usageId;
    private final String cooldownKey;

    public SuiYiGuRandomShiftEffect(final String usageId) {
        this.usageId = usageId;
        this.cooldownKey =
            "GuzhenrenExtCooldown_" + usageId + "_random_shift";
    }

    @Override
    public String getUsageId() {
        return usageId;
    }

    @Override
    public boolean onActivate(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user.level().isClientSide()) {
            return false;
        }
        if (!(user instanceof ServerPlayer player)) {
            return false;
        }

        final int remain = GuEffectCooldownHelper.getRemainingTicks(
            user,
            cooldownKey
        );
        if (remain > 0) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                    "冷却中，剩余 " + remain + "t"
                ),
                true
            );
            return false;
        }

        final double niantouCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_NIANTOU_COST, 0.0)
        );
        if (niantouCost > 0.0 && NianTouHelper.getAmount(user) < niantouCost) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("念头不足。"),
                true
            );
            return false;
        }

        final double zhenyuanBaseCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_ZHENYUAN_BASE_COST, 0.0)
        );
        final double zhenyuanCost = ZhenYuanHelper.calculateGuCost(
            user,
            zhenyuanBaseCost
        );
        if (zhenyuanCost > 0.0 && !ZhenYuanHelper.hasEnough(user, zhenyuanCost)) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("真元不足。"),
                true
            );
            return false;
        }

        if (niantouCost > 0.0) {
            NianTouHelper.modify(user, -niantouCost);
        }
        if (zhenyuanCost > 0.0) {
            ZhenYuanHelper.modify(user, -zhenyuanCost);
        }

        final int duration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_BUFF_DURATION_TICKS,
                DEFAULT_BUFF_DURATION_TICKS
            )
        );
        final int amplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_BUFF_AMPLIFIER,
                DEFAULT_BUFF_AMPLIFIER
            )
        );

        final int roll = user.getRandom().nextInt(3);
        if (roll == 0) {
            user.addEffect(
                new MobEffectInstance(
                    MobEffects.MOVEMENT_SPEED,
                    duration,
                    amplifier,
                    true,
                    true
                )
            );
        } else if (roll == 1) {
            user.addEffect(
                new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE,
                    duration,
                    amplifier,
                    true,
                    true
                )
            );
        } else {
            final double heal = Math.max(
                0.0,
                UsageMetadataHelper.getDouble(
                    usageInfo,
                    META_HEAL_AMOUNT,
                    DEFAULT_HEAL_AMOUNT
                )
            );
            if (heal > 0.0) {
                user.heal((float) heal);
            }
        }

        final int cooldownTicks = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_COOLDOWN_TICKS,
                DEFAULT_COOLDOWN_TICKS
            )
        );
        if (cooldownTicks > 0) {
            GuEffectCooldownHelper.setCooldownUntilTick(
                user,
                cooldownKey,
                user.tickCount + cooldownTicks
            );
        }

        return true;
    }
}

