package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shidao.tierThree;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 三转养生蛊：主动【延寿清茶】。
 * <p>
 * 设计目标：提供一个“救急 + 净化”的主动能力，补全食道的辅助侧玩法。
 * </p>
 */
public class YangShengGuLongevityBrewEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:yangshenggu_active_longevity_brew";

    private static final String NBT_COOLDOWN_UNTIL_TICK =
        "YangShengGuLongevityBrewCooldownUntilTick";

    private static final String META_HEAL_AMOUNT = "heal_amount";
    private static final String META_CLEANSE_COUNT = "cleanse_count";
    private static final String META_BUFF_SECONDS = "buff_seconds";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double DEFAULT_HEAL_AMOUNT = 6.0;
    private static final int DEFAULT_CLEANSE_COUNT = 2;
    private static final int DEFAULT_BUFF_SECONDS = 10;
    private static final int DEFAULT_COOLDOWN_TICKS = 420;
    private static final int TICKS_PER_SECOND = 20;

    private static final List<Holder<MobEffect>> NEGATIVE_EFFECTS =
        List.of(
            MobEffects.MOVEMENT_SLOWDOWN,
            MobEffects.WEAKNESS,
            MobEffects.POISON,
            MobEffects.WITHER,
            MobEffects.BLINDNESS,
            MobEffects.DARKNESS
        );

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
                Component.literal("延寿清茶冷却中，剩余 " + remain + "t"),
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

        final int cleanseCount = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_CLEANSE_COUNT,
                DEFAULT_CLEANSE_COUNT
            )
        );
        if (cleanseCount > 0) {
            cleanseNegatives(user, cleanseCount);
        }

        final int baseBuffSeconds = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_BUFF_SECONDS,
                DEFAULT_BUFF_SECONDS
            )
        );
        final int buffSeconds = (int) Math.round(baseBuffSeconds * multiplier);
        if (buffSeconds > 0) {
            user.addEffect(
                new MobEffectInstance(
                    MobEffects.REGENERATION,
                    buffSeconds * TICKS_PER_SECOND,
                    1,
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

    private static void cleanseNegatives(
        final LivingEntity user,
        final int limit
    ) {
        int removed = 0;
        for (Holder<MobEffect> effect : NEGATIVE_EFFECTS) {
            if (effect == null) {
                continue;
            }
            if (user.hasEffect(effect)) {
                user.removeEffect(effect);
                removed++;
                if (removed >= limit) {
                    break;
                }
            }
        }
    }
}
