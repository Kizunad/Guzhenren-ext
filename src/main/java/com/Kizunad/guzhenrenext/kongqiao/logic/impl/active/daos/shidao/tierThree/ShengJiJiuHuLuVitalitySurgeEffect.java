package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shidao.tierThree;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 三转生机酒葫芦：主动【生机灌顶】。
 * <p>
 * 设计目标：一个明确的“紧急治疗”按钮，用真元换血量与短暂回复。</p>
 */
public class ShengJiJiuHuLuVitalitySurgeEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:shengjijiuhulu_active_vitality_surge";

    private static final String NBT_COOLDOWN_UNTIL_TICK =
        "ShengJiJiuHuLuVitalitySurgeCooldownUntilTick";

    private static final String META_HEAL_AMOUNT = "heal_amount";
    private static final String META_REGEN_SECONDS = "regen_seconds";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double DEFAULT_HEAL_AMOUNT = 10.0;
    private static final int DEFAULT_REGEN_SECONDS = 10;
    private static final int DEFAULT_COOLDOWN_TICKS = 520;
    private static final int TICKS_PER_SECOND = 20;

    @Override
    public String getUsageId() {
        return USAGE_ID;
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
            NBT_COOLDOWN_UNTIL_TICK
        );
        if (remain > 0) {
            player.displayClientMessage(
                Component.literal("生机灌顶冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final double multiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.SHI_DAO
        );
        final double heal = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_HEAL_AMOUNT,
                DEFAULT_HEAL_AMOUNT
            )
        ) * multiplier;
        if (heal > 0.0) {
            user.heal((float) heal);
        }

        final int baseRegenSeconds = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_REGEN_SECONDS,
                DEFAULT_REGEN_SECONDS
            )
        );
        final int regenSeconds = (int) Math.round(baseRegenSeconds * multiplier);
        if (regenSeconds > 0) {
            user.addEffect(
                new MobEffectInstance(
                    MobEffects.REGENERATION,
                    regenSeconds * TICKS_PER_SECOND,
                    0,
                    true,
                    true
                )
            );
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
                NBT_COOLDOWN_UNTIL_TICK,
                user.tickCount + cooldownTicks
            );
        }
        return true;
    }
}
