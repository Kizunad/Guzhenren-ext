package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xingdao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * 星道主动：救星。
 * <p>
 * 在指定半径内寻找“最需要治疗”的盟友（缺失生命最多），并按“基础治疗 + 缺失生命倍率”进行治疗。
 * 可选净化负面状态。
 * </p>
 */
public class XingDaoActiveRescueStarEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_RADIUS = "radius";
    private static final String META_NIANTOU_COST = "niantou_cost";
    private static final String META_ZHENYUAN_BASE_COST = "zhenyuan_base_cost";
    private static final String META_HEAL_BASE = "heal_base";
    private static final String META_HEAL_MISSING_MULTIPLIER =
        "heal_missing_multiplier";
    private static final String META_CLEANSE_NEGATIVE = "cleanse_negative";

    private static final int DEFAULT_COOLDOWN_TICKS = 600;
    private static final double DEFAULT_RADIUS = 10.0;
    private static final double MIN_MISSING_HEALTH = 0.0001;

    private final String usageId;
    private final String nbtCooldownKey;

    public XingDaoActiveRescueStarEffect(
        final String usageId,
        final String nbtCooldownKey
    ) {
        this.usageId = Objects.requireNonNull(usageId, "usageId");
        this.nbtCooldownKey = Objects.requireNonNull(nbtCooldownKey, "nbtCooldownKey");
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
                net.minecraft.network.chat.Component.literal(
                    "冷却中，剩余 " + remain + "t"
                ),
                true
            );
            return false;
        }

        final double radius = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RADIUS, DEFAULT_RADIUS)
        );
        final LivingEntity target = findMostInjuredAlly(user, radius);
        if (target == null) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("附近没有可治疗目标。"),
                true
            );
            return false;
        }

        final double missing = Math.max(0.0, target.getMaxHealth() - target.getHealth());
        if (missing <= MIN_MISSING_HEALTH) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("无需治疗。"),
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

        final double healBase = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HEAL_BASE, 0.0)
        );
        final double healMissingMultiplier = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_HEAL_MISSING_MULTIPLIER,
                0.0
            )
        );
        final double healAmount = healBase + missing * healMissingMultiplier;
        if (healAmount > 0.0) {
            target.heal((float) healAmount);
        }

        final boolean cleanseNegative = UsageMetadataHelper.getBoolean(
            usageInfo,
            META_CLEANSE_NEGATIVE,
            false
        );
        if (cleanseNegative) {
            cleanseNegativeEffects(target);
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

    private static LivingEntity findMostInjuredAlly(
        final LivingEntity user,
        final double radius
    ) {
        final AABB box = user.getBoundingBox().inflate(radius);
        LivingEntity best = null;
        double bestMissing = 0.0;

        for (LivingEntity ally : user.level().getEntitiesOfClass(
            LivingEntity.class,
            box,
            e -> e.isAlive() && (e == user || user.isAlliedTo(e))
        )) {
            final double missing = Math.max(0.0, ally.getMaxHealth() - ally.getHealth());
            if (missing > bestMissing) {
                bestMissing = missing;
                best = ally;
            }
        }

        return best;
    }

    private static void cleanseNegativeEffects(final LivingEntity ally) {
        final List<MobEffectInstance> toRemove = new ArrayList<>();
        for (MobEffectInstance inst : ally.getActiveEffects()) {
            if (inst == null || inst.getEffect() == null) {
                continue;
            }
            final MobEffect effect = inst.getEffect().value();
            if (effect != null && !effect.isBeneficial()) {
                toRemove.add(inst);
            }
        }
        for (MobEffectInstance inst : toRemove) {
            ally.removeEffect(inst.getEffect());
        }
    }
}

