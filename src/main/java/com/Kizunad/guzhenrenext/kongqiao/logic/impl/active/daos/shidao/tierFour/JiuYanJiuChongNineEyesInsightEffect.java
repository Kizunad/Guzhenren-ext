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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * 四转九眼酒虫：主动【九眼洞察】。
 * <p>
 * 设计目标：提供“侦测 + 输出窗口”：
 * 激活后标记周围敌人（发光），并在持续时间内提升攻击伤害。</p>
 */
public class JiuYanJiuChongNineEyesInsightEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:jiuyanjiuchong_active_nine_eyes_insight";

    private static final String NBT_COOLDOWN_UNTIL_TICK =
        "JiuYanJiuChongNineEyesInsightCooldownUntilTick";
    private static final String TAG_ACTIVE_UNTIL_TICK =
        "JiuYanJiuChongNineEyesInsightActiveUntilTick";

    private static final String META_RADIUS = "radius";
    private static final String META_DURATION_TICKS = "duration_ticks";
    private static final String META_BONUS_DAMAGE = "bonus_damage";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double DEFAULT_RADIUS = 10.0;
    private static final int DEFAULT_DURATION_TICKS = 240;
    private static final double DEFAULT_BONUS_DAMAGE = 3.0;
    private static final int DEFAULT_COOLDOWN_TICKS = 600;
    private static final double MAX_RADIUS = 64.0;

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
                Component.literal("九眼洞察冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.SHI_DAO
        );
        final double radius = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(usageInfo, META_RADIUS, DEFAULT_RADIUS),
            0.0,
            MAX_RADIUS
        );
        final int baseDurationTicks = Math.max(
            1,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_DURATION_TICKS,
                DEFAULT_DURATION_TICKS
            )
        );
        final int durationTicks =
            (int) Math.round(baseDurationTicks * selfMultiplier);
        user.getPersistentData()
            .putInt(TAG_ACTIVE_UNTIL_TICK, user.tickCount + durationTicks);

        if (radius > 0.0) {
            final List<LivingEntity> targets = findTargets(user, radius);
            for (LivingEntity target : targets) {
                target.addEffect(
                    new MobEffectInstance(
                        MobEffects.GLOWING,
                        durationTicks,
                        0,
                        true,
                        false,
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

    @Override
    public float onAttack(
        final LivingEntity attacker,
        final LivingEntity target,
        final float damage,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (attacker.level().isClientSide()) {
            return damage;
        }

        final int until = attacker.getPersistentData()
            .getInt(TAG_ACTIVE_UNTIL_TICK);
        if (until <= attacker.tickCount) {
            return damage;
        }

        final double bonus = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_BONUS_DAMAGE,
                DEFAULT_BONUS_DAMAGE
            )
        );
        final double multiplier = DaoHenCalculator.calculateMultiplier(
            attacker,
            target,
            DaoHenHelper.DaoType.SHI_DAO
        );
        return (float) (damage + (bonus * multiplier));
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
