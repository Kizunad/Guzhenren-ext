package com.Kizunad.guzhenrenext.bastion.skill.impl.special;

import com.Kizunad.guzhenrenext.bastion.skill.BastionSkillContext;
import com.Kizunad.guzhenrenext.bastion.skill.IBastionSkillEffect;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;

/**
 * 智道高转特效：命运丝线。
 * <p>
 * 配置来源：bastion_type 的 high_tier.special_effects。
 * 该效果每秒触发一次，主要用于强化基地守卫的持续作战能力。
 * </p>
 */
public class HighTierZhiDaoFateThreadsEffect implements IBastionSkillEffect {

    public static final String EFFECT_ID =
        "guzhenrenext:high_tier_zhi_dao_fate_threads";

    /** 每秒刷新一次，给 2 秒持续时间避免边界抖动。 */
    private static final int EFFECT_DURATION_TICKS = 40;

    private static final int DEFAULT_RADIUS = 48;
    private static final int MIN_RADIUS = 1;

    private static final int BASE_REGEN_AMPLIFIER = 0;
    private static final int BASE_RESISTANCE_AMPLIFIER = 0;

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

        final int ampBonus = (int) Math.floor(Math.max(0.0, context.bonusMultiplier() - 1.0));
        final int regenAmp = BASE_REGEN_AMPLIFIER + ampBonus;
        final int resistanceAmp = BASE_RESISTANCE_AMPLIFIER + ampBonus;

        for (Mob guardian : context.guardians()) {
            if (guardian == null) {
                continue;
            }
            if (guardian.isRemoved() || !guardian.isAlive()) {
                continue;
            }
            if (guardian.blockPosition().distSqr(core) > (long) radius * radius) {
                continue;
            }

            guardian.addEffect(
                new MobEffectInstance(
                    MobEffects.REGENERATION,
                    EFFECT_DURATION_TICKS,
                    regenAmp,
                    false,
                    false,
                    true
                )
            );
            guardian.addEffect(
                new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE,
                    EFFECT_DURATION_TICKS,
                    resistanceAmp,
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
