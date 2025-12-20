package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shidao.tierOne;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * 一转酒囊花蛊：主动【酒雾迷魂】。
 * <p>
 * 设计目标：提供一个偏辅助的范围控场技能，给食道一个“防守型出手”。
 * </p>
 */
public class JiuNangHuaGuWineMistEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:jiu_nang_hua_gu_active_wine_mist";

    private static final String NBT_COOLDOWN_UNTIL_TICK =
        "JiuNangHuaGuWineMistCooldownUntilTick";

    private static final String META_RADIUS = "radius";
    private static final String META_EFFECT_SECONDS = "effect_seconds";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double DEFAULT_RADIUS = 6.0;
    private static final int DEFAULT_EFFECT_SECONDS = 8;
    private static final int DEFAULT_COOLDOWN_TICKS = 240;
    private static final double MAX_RADIUS = 32.0;

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
                Component.literal("酒雾迷魂冷却中，剩余 " + remain + "t"),
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
        final double radius = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(usageInfo, META_RADIUS, DEFAULT_RADIUS),
            0.0,
            MAX_RADIUS
        );
        final int baseEffectSeconds = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_EFFECT_SECONDS,
                DEFAULT_EFFECT_SECONDS
            )
        );
        final int effectSeconds = (int) Math.round(baseEffectSeconds * multiplier);

        if (radius > 0.0 && effectSeconds > 0) {
            final int duration = effectSeconds * 20;
            final List<LivingEntity> targets = findTargets(user, radius);
            for (LivingEntity target : targets) {
                target.addEffect(
                    new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        duration,
                        0,
                        true,
                        true
                    )
                );
                target.addEffect(
                    new MobEffectInstance(
                        MobEffects.WEAKNESS,
                        duration,
                        0,
                        true,
                        true
                    )
                );
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
                NBT_COOLDOWN_UNTIL_TICK,
                user.tickCount + cooldownTicks
            );
        }
        return true;
    }

    private static List<LivingEntity> findTargets(
        final LivingEntity user,
        final double radius
    ) {
        final AABB area = user.getBoundingBox().inflate(radius);
        return user.level().getEntitiesOfClass(
            LivingEntity.class,
            area,
            e -> e.isAlive() && e != user && e instanceof Monster
        );
    }
}
