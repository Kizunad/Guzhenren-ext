package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoActiveEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.common.DaoCooldownKeys;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.LineOfSightTargetHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.PhysicalDamageSourceHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * 血道主动杀招【血锥追袭】：锁定单体的普通伤害打击（附带短暂迟滞），并小幅夺取魂魄续航。
 */
public class XueDaoShazhaoBloodAwlPursuitEffect implements IShazhaoActiveEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_xue_dao_blood_awl_pursuit";

    private static final DaoHenHelper.DaoType DAO_TYPE =
        DaoHenHelper.DaoType.XUE_DAO;

    private static final String META_RANGE = "range";
    private static final String META_PHYSICAL_DAMAGE = "physical_damage";
    private static final String META_SLOW_DURATION_TICKS = "slow_duration_ticks";
    private static final String META_SLOW_AMPLIFIER = "slow_amplifier";
    private static final String META_SELF_HUNPO_GAIN = "self_hunpo_gain";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double DEFAULT_RANGE = 10.0;
    private static final int DEFAULT_SLOW_DURATION_TICKS = 80;
    private static final int DEFAULT_SLOW_AMPLIFIER = 0;
    private static final int DEFAULT_COOLDOWN_TICKS = 240;

    private static final double MIN_VALUE = 0.0;
    private static final double MAX_DAMAGE = 20000.0;
    private static final double MAX_HUNPO_GAIN = 100.0;

    private static final String COOLDOWN_KEY = DaoCooldownKeys.active(SHAZHAO_ID);

    @Override
    public String getShazhaoId() {
        return SHAZHAO_ID;
    }

    @Override
    public boolean onActivate(final ServerPlayer player, final ShazhaoData data) {
        if (player == null || data == null) {
            return false;
        }
        if (player.level().isClientSide()) {
            return false;
        }

        final int remain = GuEffectCooldownHelper.getRemainingTicks(
            player,
            COOLDOWN_KEY
        );
        if (remain > 0) {
            player.displayClientMessage(
                Component.literal("冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        final double range = Math.max(
            1.0,
            ShazhaoMetadataHelper.getDouble(data, META_RANGE, DEFAULT_RANGE)
        );
        final LivingEntity target = LineOfSightTargetHelper.findTarget(
            player,
            range
        );
        if (target == null) {
            player.displayClientMessage(Component.literal("未找到可锁定目标。"), true);
            return false;
        }

        if (!ShazhaoCostHelper.tryConsumeOnce(player, player, data)) {
            return false;
        }

        final double multiplier = DaoHenCalculator.calculateMultiplier(
            player,
            target,
            DAO_TYPE
        );
        final double baseDamage = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(data, META_PHYSICAL_DAMAGE, MIN_VALUE)
        );
        final double damage = ShazhaoMetadataHelper.clamp(
            baseDamage * Math.max(MIN_VALUE, multiplier),
            MIN_VALUE,
            MAX_DAMAGE
        );
        if (damage > MIN_VALUE) {
            target.hurt(
                PhysicalDamageSourceHelper.buildPhysicalDamageSource(player),
                (float) damage
            );
        }

        final int baseSlowTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_SLOW_DURATION_TICKS,
                DEFAULT_SLOW_DURATION_TICKS
            )
        );
        final int amplifier = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_SLOW_AMPLIFIER,
                DEFAULT_SLOW_AMPLIFIER
            )
        );
        final int slowTicks = DaoHenEffectScalingHelper.scaleDurationTicks(
            baseSlowTicks,
            multiplier
        );
        if (slowTicks > 0) {
            target.addEffect(
                new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    slowTicks,
                    amplifier,
                    true,
                    true
                )
            );
        }

        final double selfHunpoBase = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(data, META_SELF_HUNPO_GAIN, MIN_VALUE)
        );
        if (selfHunpoBase > MIN_VALUE) {
            final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
                DaoHenCalculator.calculateSelfMultiplier(player, DAO_TYPE)
            );
            final double selfHunpo = ShazhaoMetadataHelper.clamp(
                DaoHenEffectScalingHelper.scaleValue(selfHunpoBase, selfMultiplier),
                MIN_VALUE,
                MAX_HUNPO_GAIN
            );
            if (selfHunpo > MIN_VALUE) {
                HunPoHelper.modify(player, selfHunpo);
            }
        }

        final int cooldownTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_COOLDOWN_TICKS,
                DEFAULT_COOLDOWN_TICKS
            )
        );
        if (cooldownTicks > 0) {
            GuEffectCooldownHelper.setCooldownUntilTick(
                player,
                COOLDOWN_KEY,
                player.tickCount + cooldownTicks
            );
        }

        return true;
    }
}

