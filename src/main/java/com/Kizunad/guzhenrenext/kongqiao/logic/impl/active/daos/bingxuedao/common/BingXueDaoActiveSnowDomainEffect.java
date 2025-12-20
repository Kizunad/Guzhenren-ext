package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bingxuedao.common;

import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.kongqiao.service.BingXueDaoDomainService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 冰雪道主动：雪域（持续性领域，效果由 {@link BingXueDaoDomainService} 每秒驱动）。
 */
public class BingXueDaoActiveSnowDomainEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_RADIUS = "radius";
    private static final String META_DURATION_TICKS = "duration_ticks";
    private static final String META_NIANTOU_COST_PER_SECOND =
        "niantou_cost_per_second";
    private static final String META_JINGLI_COST_PER_SECOND =
        "jingli_cost_per_second";
    private static final String META_HUNPO_COST_PER_SECOND = "hunpo_cost_per_second";
    private static final String META_ZHENYUAN_BASE_COST_PER_SECOND =
        "zhenyuan_base_cost_per_second";
    private static final String META_MAGIC_DAMAGE_PER_SECOND =
        "magic_damage_per_second";
    private static final String META_FREEZE_TICKS_PER_SECOND =
        "freeze_ticks_per_second";
    private static final String META_SLOW_DURATION_TICKS = "slow_duration_ticks";
    private static final String META_SLOW_AMPLIFIER = "slow_amplifier";

    private static final int DEFAULT_COOLDOWN_TICKS = 20 * 35;
    private static final double DEFAULT_RADIUS = 10.0;
    private static final int DEFAULT_DURATION_TICKS = 20 * 20;

    private final String usageId;
    private final String nbtCooldownKey;

    public BingXueDaoActiveSnowDomainEffect(
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

        final double radius = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RADIUS, DEFAULT_RADIUS)
        );
        final int durationTicks = Math.max(
            1,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_DURATION_TICKS,
                DEFAULT_DURATION_TICKS
            )
        );

        final double perSecondNianTou = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_NIANTOU_COST_PER_SECOND, 0.0)
        );
        final double perSecondJingli = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_JINGLI_COST_PER_SECOND, 0.0)
        );
        final double perSecondHunpo = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HUNPO_COST_PER_SECOND, 0.0)
        );

        final double zhenyuanBasePerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ZHENYUAN_BASE_COST_PER_SECOND,
                0.0
            )
        );
        if (
            !GuEffectCostHelper.hasEnoughForSustain(
                player,
                user,
                perSecondNianTou,
                perSecondJingli,
                perSecondHunpo,
                zhenyuanBasePerSecond
            )
        ) {
            return false;
        }

        final int untilTick = user.tickCount + durationTicks;
        final SustainCosts costs = new SustainCosts(
            perSecondNianTou,
            perSecondJingli,
            perSecondHunpo,
            zhenyuanBasePerSecond
        );
        writeDomainData(
            player,
            usageInfo,
            untilTick,
            radius,
            costs
        );

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

    private static void writeDomainData(
        final ServerPlayer player,
        final NianTouData.Usage usageInfo,
        final int untilTick,
        final double radius,
        final SustainCosts costs
    ) {
        player.getPersistentData().putInt(
            BingXueDaoDomainService.KEY_DOMAIN_UNTIL_TICK,
            untilTick
        );
        player.getPersistentData().putDouble(
            BingXueDaoDomainService.KEY_DOMAIN_RADIUS,
            radius
        );
        player.getPersistentData().putDouble(
            BingXueDaoDomainService.KEY_DOMAIN_NIANTOU_COST_PER_SECOND,
            costs.niantou()
        );
        player.getPersistentData().putDouble(
            BingXueDaoDomainService.KEY_DOMAIN_JINGLI_COST_PER_SECOND,
            costs.jingli()
        );
        player.getPersistentData().putDouble(
            BingXueDaoDomainService.KEY_DOMAIN_HUNPO_COST_PER_SECOND,
            costs.hunpo()
        );
        player.getPersistentData().putDouble(
            BingXueDaoDomainService.KEY_DOMAIN_ZHENYUAN_BASE_COST_PER_SECOND,
            costs.zhenyuanBase()
        );
        player.getPersistentData().putDouble(
            BingXueDaoDomainService.KEY_DOMAIN_MAGIC_DAMAGE_PER_SECOND,
            Math.max(
                0.0,
                UsageMetadataHelper.getDouble(
                    usageInfo,
                    META_MAGIC_DAMAGE_PER_SECOND,
                    0.0
                )
            )
        );
        player.getPersistentData().putInt(
            BingXueDaoDomainService.KEY_DOMAIN_FREEZE_TICKS_PER_SECOND,
            Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    META_FREEZE_TICKS_PER_SECOND,
                    0
                )
            )
        );
        player.getPersistentData().putInt(
            BingXueDaoDomainService.KEY_DOMAIN_SLOW_DURATION_TICKS,
            Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    META_SLOW_DURATION_TICKS,
                    0
                )
            )
        );
        player.getPersistentData().putInt(
            BingXueDaoDomainService.KEY_DOMAIN_SLOW_AMPLIFIER,
            Math.max(
                0,
                UsageMetadataHelper.getInt(usageInfo, META_SLOW_AMPLIFIER, 0)
            )
        );
    }

    private record SustainCosts(
        double niantou,
        double jingli,
        double hunpo,
        double zhenyuanBase
    ) {}
}
