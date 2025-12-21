package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common.JianDaoBoostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.SafeTeleportHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class JianDaoActiveMultiBlinkEffect implements IGuEffect {

    private static final String KEY_CHAIN_UNTIL_TICK =
        "GuzhenrenExtJianDaoMultiBlinkUntilTick";
    private static final String KEY_CHAIN_REMAINING =
        "GuzhenrenExtJianDaoMultiBlinkRemaining";

    private static final String META_DISTANCE = "distance";
    private static final String META_CHAIN_WINDOW_TICKS = "chain_window_ticks";
    private static final String META_CHAIN_CHARGES = "chain_charges";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double DEFAULT_DISTANCE = 10.0;
    private static final int DEFAULT_CHAIN_WINDOW_TICKS = 20 * 6;
    private static final int DEFAULT_CHAIN_CHARGES = 3;
    private static final int DEFAULT_COOLDOWN_TICKS = 0;

    private final String usageId;
    private final String nbtCooldownKey;

    public JianDaoActiveMultiBlinkEffect(
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
        if (user == null) {
            return false;
        }
        if (user.level().isClientSide()) {
            return false;
        }
        if (!(user instanceof ServerPlayer player)) {
            return false;
        }

        final int now = user.tickCount;
        int chainUntil = user.getPersistentData().getInt(KEY_CHAIN_UNTIL_TICK);
        int remaining = user.getPersistentData().getInt(KEY_CHAIN_REMAINING);
        final boolean inChain = chainUntil > now && remaining > 0;

        if (!inChain) {
            final int remainCooldown = GuEffectCooldownHelper.getRemainingTicks(
                user,
                nbtCooldownKey
            );
            if (remainCooldown > 0) {
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal(
                        "冷却中，剩余 " + remainCooldown + "t"
                    ),
                    true
                );
                return false;
            }
            remaining = Math.max(
                1,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    META_CHAIN_CHARGES,
                    DEFAULT_CHAIN_CHARGES
                )
            );
            final int windowTicks = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    META_CHAIN_WINDOW_TICKS,
                    DEFAULT_CHAIN_WINDOW_TICKS
                )
            );
            chainUntil = now + windowTicks;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(user, DaoHenHelper.DaoType.JIAN_DAO)
                * JianDaoBoostHelper.getJianXinMultiplier(user)
        );
        final double suiren = JianDaoBoostHelper.consumeSuiRenMultiplierIfActive(
            user
        );
        final double scale = DaoHenEffectScalingHelper.clampMultiplier(
            selfMultiplier * Math.max(1.0, suiren)
        );

        final double distance = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(usageInfo, META_DISTANCE, DEFAULT_DISTANCE)
        ) * Math.max(0.0, scale);
        final Vec3 base = user.position().add(user.getLookAngle().normalize().scale(distance));

        if (!SafeTeleportHelper.teleportSafely(user, base)) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("传送失败：附近无安全落点。"),
                true
            );
            return false;
        }

        remaining = remaining - 1;
        if (remaining > 0 && chainUntil > now) {
            user.getPersistentData().putInt(KEY_CHAIN_UNTIL_TICK, chainUntil);
            user.getPersistentData().putInt(KEY_CHAIN_REMAINING, remaining);
            return true;
        }

        clearChain(user);

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
                now + cooldownTicks
            );
        }

        return true;
    }

    @Override
    public void onUnequip(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user == null) {
            return;
        }
        if (user.level().isClientSide()) {
            return;
        }
        clearChain(user);
    }

    private static void clearChain(final LivingEntity user) {
        user.getPersistentData().remove(KEY_CHAIN_UNTIL_TICK);
        user.getPersistentData().remove(KEY_CHAIN_REMAINING);
    }
}
