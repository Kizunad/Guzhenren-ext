package com.Kizunad.guzhenrenext.bastion.skill.impl.special;

import com.Kizunad.guzhenrenext.bastion.skill.BastionSkillContext;
import com.Kizunad.guzhenrenext.bastion.skill.IBastionSkillEffect;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * 智道高转特效：演算光环。
 * <p>
 * 配置来源：bastion_type 的 high_tier.special_effects。
 * 该效果每秒在基地领域内生效，用于表达“智道压制/演算干扰”。
 * </p>
 * <p>
 * 设计：
 * <ul>
 *   <li>对领域内玩家施加短时负面效果（眩晕/挖掘减速/虚弱等）以体现压制。</li>
 *   <li>强度随 bonusMultiplier 缩放（7~9 转阈值提供）。</li>
 * </ul>
 * </p>
 */
public class HighTierZhiDaoCalculationAuraEffect implements IBastionSkillEffect {

    public static final String EFFECT_ID =
        "guzhenrenext:high_tier_zhi_dao_calculation_aura";

    /** 每秒刷新一次，给 2 秒持续时间避免边界抖动。 */
    private static final int EFFECT_DURATION_TICKS = 40;

    private static final int BASE_SLOWNESS_AMPLIFIER = 0;
    private static final int BASE_WEAKNESS_AMPLIFIER = 0;
    private static final int BASE_MINING_FATIGUE_AMPLIFIER = 0;

    private static final int DEFAULT_RADIUS = 32;
    private static final int MIN_RADIUS = 1;

    @Override
    public String getShazhaoId() {
        return EFFECT_ID;
    }

    @Override
    public void onBastionSecond(final BastionSkillContext context) {
        if (context == null || context.level() == null || context.bastion() == null) {
            return;
        }

        final Map<String, String> metadata = context.metadata();
        final int radius = clampInt(
            getInt(metadata, "radius", DEFAULT_RADIUS),
            MIN_RADIUS,
            DEFAULT_RADIUS * 4
        );

        final BlockPos core = context.bastion().corePos();

        // bonusMultiplier 越高，放大压制等级（但保持温和，避免无限叠加）。
        final int ampBonus = (int) Math.floor(Math.max(0.0, context.bonusMultiplier() - 1.0));
        final int slownessAmp = BASE_SLOWNESS_AMPLIFIER + ampBonus;
        final int weaknessAmp = BASE_WEAKNESS_AMPLIFIER + ampBonus;
        final int fatigueAmp = BASE_MINING_FATIGUE_AMPLIFIER + ampBonus;

        for (ServerPlayer player : context.targets()) {
            if (player == null) {
                continue;
            }
            if (!player.level().dimension().equals(context.level().dimension())) {
                continue;
            }
            if (player.blockPosition().distSqr(core) > (long) radius * radius) {
                continue;
            }

            // 标准的“短时刷新”做法：每秒写入 2 秒持续。
            player.addEffect(
                new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    EFFECT_DURATION_TICKS,
                    slownessAmp,
                    false,
                    false,
                    true
                )
            );
            player.addEffect(
                new MobEffectInstance(
                    MobEffects.WEAKNESS,
                    EFFECT_DURATION_TICKS,
                    weaknessAmp,
                    false,
                    false,
                    true
                )
            );
            player.addEffect(
                new MobEffectInstance(
                    MobEffects.DIG_SLOWDOWN,
                    EFFECT_DURATION_TICKS,
                    fatigueAmp,
                    false,
                    false,
                    true
                )
            );
        }
    }

    private static int getInt(
        final Map<String, String> metadata,
        final String key,
        final int defaultValue
    ) {
        if (metadata == null) {
            return defaultValue;
        }
        final String value = metadata.get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private static int clampInt(final int value, final int min, final int max) {
        return Math.min(max, Math.max(min, value));
    }
}
