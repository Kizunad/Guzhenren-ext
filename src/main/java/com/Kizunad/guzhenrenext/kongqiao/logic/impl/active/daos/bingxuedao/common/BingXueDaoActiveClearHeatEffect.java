package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bingxuedao.common;

import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 冰雪道主动：清热解毒（驱散负面效果 + 小幅治疗）。
 */
public class BingXueDaoActiveClearHeatEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_HEAL_AMOUNT = "heal_amount";
    private static final String META_MAX_REMOVED = "max_removed";
    private static final String META_CLEAR_FIRE = "clear_fire";
    private static final String META_CLEAR_FREEZE = "clear_freeze";

    private static final int DEFAULT_COOLDOWN_TICKS = 200;
    private static final double DEFAULT_HEAL_AMOUNT = 0.0;
    private static final int DEFAULT_MAX_REMOVED = 2;
    private static final boolean DEFAULT_CLEAR_FIRE = true;
    private static final boolean DEFAULT_CLEAR_FREEZE = true;

    private final String usageId;
    private final String nbtCooldownKey;

    public BingXueDaoActiveClearHeatEffect(
        final String usageId,
        final String nbtCooldownKey
    ) {
        this.usageId = usageId;
        this.nbtCooldownKey = nbtCooldownKey;
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
            nbtCooldownKey
        );
        if (remain > 0) {
            player.displayClientMessage(
                Component.literal("冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        if (
            UsageMetadataHelper.getBoolean(
                usageInfo,
                META_CLEAR_FIRE,
                DEFAULT_CLEAR_FIRE
            )
        ) {
            user.clearFire();
        }
        if (
            UsageMetadataHelper.getBoolean(
                usageInfo,
                META_CLEAR_FREEZE,
                DEFAULT_CLEAR_FREEZE
            )
        ) {
            user.setTicksFrozen(0);
        }

        final int maxRemoved = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_MAX_REMOVED,
                DEFAULT_MAX_REMOVED
            )
        );
        if (maxRemoved > 0) {
            int removed = 0;
            for (Holder<MobEffect> effect : collectHarmfulEffects(user)) {
                if (removed >= maxRemoved) {
                    break;
                }
                user.removeEffect(effect);
                removed++;
            }
        }

        final double healAmount = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HEAL_AMOUNT, DEFAULT_HEAL_AMOUNT)
        );
        if (healAmount > 0.0) {
            user.heal((float) healAmount);
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
                nbtCooldownKey,
                user.tickCount + cooldownTicks
            );
        }

        return true;
    }

    private static List<Holder<MobEffect>> collectHarmfulEffects(
        final LivingEntity user
    ) {
        final List<Holder<MobEffect>> result = new ArrayList<>();
        for (MobEffectInstance inst : user.getActiveEffects()) {
            if (inst == null) {
                continue;
            }
            final Holder<MobEffect> effect = inst.getEffect();
            if (
                effect != null
                    && effect.value().getCategory() == MobEffectCategory.HARMFUL
            ) {
                result.add(effect);
            }
        }
        return result;
    }
}
