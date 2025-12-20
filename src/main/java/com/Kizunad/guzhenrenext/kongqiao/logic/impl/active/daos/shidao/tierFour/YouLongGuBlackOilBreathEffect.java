package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shidao.tierFour;

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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * 四转油龙蛊：主动【黑油喷吐】。
 * <p>
 * 设计目标：主动施加油火并造成一次伤害，作为食道的输出/控场补充。</p>
 */
public class YouLongGuBlackOilBreathEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:youlonggu_active_black_oil_breath";

    private static final String NBT_COOLDOWN_UNTIL_TICK =
        "YouLongGuBlackOilBreathCooldownUntilTick";

    private static final String TAG_OIL_UNTIL_TICK = "YouLongGuOilUntilTick";

    private static final String META_RADIUS = "radius";
    private static final String META_DAMAGE = "damage";
    private static final String META_BURN_SECONDS = "burn_seconds";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double DEFAULT_RADIUS = 6.0;
    private static final double DEFAULT_DAMAGE = 3.0;
    private static final int DEFAULT_BURN_SECONDS = 5;
    private static final int DEFAULT_COOLDOWN_TICKS = 520;

    private static final int DEFAULT_OIL_DURATION_TICKS = 200;
    private static final double MAX_RADIUS = 64.0;
    private static final double MAX_MAGIC_DAMAGE = 2000.0;

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
                Component.literal("黑油喷吐冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final double radius = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(usageInfo, META_RADIUS, DEFAULT_RADIUS),
            0.0,
            MAX_RADIUS
        );
        final double baseDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_DAMAGE, DEFAULT_DAMAGE)
        );
        final int burnSeconds = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_BURN_SECONDS,
                DEFAULT_BURN_SECONDS
            )
        );

        if (radius > 0.0) {
            final List<LivingEntity> targets = findTargets(user, radius);
            for (LivingEntity target : targets) {
                target.getPersistentData()
                    .putInt(
                        TAG_OIL_UNTIL_TICK,
                        target.tickCount + DEFAULT_OIL_DURATION_TICKS
                    );
                if (burnSeconds > 0) {
                    target.igniteForSeconds(burnSeconds);
                }
                if (baseDamage > 0.0) {
                    final double multiplier = DaoHenCalculator.calculateMultiplier(
                        user,
                        target,
                        DaoHenHelper.DaoType.SHI_DAO
                    );
                    final double damage = UsageMetadataHelper.clamp(
                        baseDamage * multiplier,
                        0.0,
                        MAX_MAGIC_DAMAGE
                    );
                    if (damage > 0.0) {
                        target.hurt(
                            user.damageSources().magic(),
                            (float) damage
                        );
                    }
                }
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
