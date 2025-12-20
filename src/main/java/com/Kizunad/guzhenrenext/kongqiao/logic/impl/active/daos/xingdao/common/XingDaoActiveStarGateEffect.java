package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xingdao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.SafeTeleportHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * 星道主动：星门。
 * <p>
 * 长距挪移（沿视线方向），可选要求夜晚与露天；并可附带传送后的自增益。
 * </p>
 */
public class XingDaoActiveStarGateEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_DISTANCE = "distance";
    private static final String META_NIANTOU_COST = "niantou_cost";
    private static final String META_ZHENYUAN_BASE_COST = "zhenyuan_base_cost";
    private static final String META_REQUIRES_NIGHT = "requires_night";
    private static final String META_REQUIRES_OPEN_SKY = "requires_open_sky";

    private static final int DEFAULT_COOLDOWN_TICKS = 900;
    private static final double DEFAULT_DISTANCE = 24.0;
    private static final double MIN_DISTANCE_SQR = 0.0001;

    public record EffectSpec(
        Holder<MobEffect> effect,
        String durationKey,
        int defaultDurationTicks,
        String amplifierKey,
        int defaultAmplifier
    ) {}

    private final String usageId;
    private final String nbtCooldownKey;
    private final List<EffectSpec> afterEffects;

    public XingDaoActiveStarGateEffect(
        final String usageId,
        final String nbtCooldownKey,
        final List<EffectSpec> afterEffects
    ) {
        this.usageId = Objects.requireNonNull(usageId, "usageId");
        this.nbtCooldownKey = Objects.requireNonNull(nbtCooldownKey, "nbtCooldownKey");
        this.afterEffects = Objects.requireNonNull(afterEffects, "afterEffects");
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

        if (UsageMetadataHelper.getBoolean(usageInfo, META_REQUIRES_NIGHT, false)) {
            if (player.level().isDay()) {
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("白日无星，难以催发。"),
                    true
                );
                return false;
            }
        }

        if (UsageMetadataHelper.getBoolean(usageInfo, META_REQUIRES_OPEN_SKY, false)) {
            if (!(player.level() instanceof ServerLevel level)) {
                return false;
            }
            final BlockPos pos = player.blockPosition();
            if (!level.canSeeSky(pos)) {
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("需露天见星，方可开门。"),
                    true
                );
                return false;
            }
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

        final double distance = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(usageInfo, META_DISTANCE, DEFAULT_DISTANCE)
        );
        final Vec3 base = player.position()
            .add(player.getLookAngle().normalize().scale(distance));
        final Vec3 safe = SafeTeleportHelper.findSafeTeleportPos(player, base);
        if (safe.distanceToSqr(player.position()) <= MIN_DISTANCE_SQR) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("星门未能定位到安全落点。"),
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

        SafeTeleportHelper.teleportSafely(player, safe);
        applyAfterEffects(player, usageInfo);

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

    private void applyAfterEffects(
        final LivingEntity user,
        final NianTouData.Usage usageInfo
    ) {
        if (afterEffects.isEmpty()) {
            return;
        }
        for (EffectSpec spec : afterEffects) {
            if (spec == null || spec.effect() == null) {
                continue;
            }
            final int duration = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    spec.durationKey(),
                    spec.defaultDurationTicks()
                )
            );
            final int amplifier = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    spec.amplifierKey(),
                    spec.defaultAmplifier()
                )
            );
            if (duration <= 0) {
                continue;
            }
            user.addEffect(
                new MobEffectInstance(
                    spec.effect(),
                    duration,
                    amplifier,
                    true,
                    true
                )
            );
        }
    }
}

