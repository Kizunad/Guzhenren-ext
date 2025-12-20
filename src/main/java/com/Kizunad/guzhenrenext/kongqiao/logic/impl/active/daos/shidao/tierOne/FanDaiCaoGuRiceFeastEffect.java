package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shidao.tierOne;

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
 * 一转饭袋草蛊：主动【饭袋急食】。
 * <p>
 * 设计目标：紧急回补饱食并提供短暂防护，但附带迟缓作为代价，避免无脑“跑图加速器”。</p>
 */
public class FanDaiCaoGuRiceFeastEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:fan_dai_cao_gu_active_rice_feast";

    private static final String NBT_COOLDOWN_UNTIL_TICK =
        "FanDaiCaoGuRiceFeastCooldownUntilTick";

    private static final String META_HUNGER_RESTORE = "hunger_restore";
    private static final String META_SATURATION_RESTORE = "saturation_restore";
    private static final String META_SLOWNESS_SECONDS = "slowness_seconds";
    private static final String META_ABSORPTION = "absorption";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final int DEFAULT_HUNGER_RESTORE = 10;
    private static final double DEFAULT_SATURATION_RESTORE = 6.0;
    private static final int DEFAULT_SLOWNESS_SECONDS = 6;
    private static final double DEFAULT_ABSORPTION = 4.0;
    private static final int DEFAULT_COOLDOWN_TICKS = 260;
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
                Component.literal("饭袋急食冷却中，剩余 " + remain + "t"),
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

        final int baseHunger = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_HUNGER_RESTORE,
                DEFAULT_HUNGER_RESTORE
            )
        );
        final int hunger = (int) Math.round(baseHunger * multiplier);
        final double saturation = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_SATURATION_RESTORE,
                DEFAULT_SATURATION_RESTORE
            )
        ) * multiplier;
        if (hunger > 0 || saturation > 0.0) {
            player.getFoodData().eat(hunger, (float) saturation);
        }

        final double absorption = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ABSORPTION,
                DEFAULT_ABSORPTION
            )
        ) * multiplier;
        if (absorption > 0.0) {
            player.setAbsorptionAmount(
                (float) (player.getAbsorptionAmount() + absorption)
            );
        }

        final int slownessSeconds = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_SLOWNESS_SECONDS,
                DEFAULT_SLOWNESS_SECONDS
            )
        );
        if (slownessSeconds > 0) {
            player.addEffect(
                new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    slownessSeconds * TICKS_PER_SECOND,
                    0,
                    true,
                    false,
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
