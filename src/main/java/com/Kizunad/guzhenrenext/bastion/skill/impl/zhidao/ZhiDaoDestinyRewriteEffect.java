package com.Kizunad.guzhenrenext.bastion.skill.impl.zhidao;

import com.Kizunad.guzhenrenext.bastion.skill.BastionSkillContext;
import com.Kizunad.guzhenrenext.bastion.skill.IBastionSkillEffect;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;

/**
 * 智道九转主动：命运改写。
 * <p>
 * 由于“免疫一次致命伤害”需要更深的战斗事件拦截链路（伤害事件/状态缓存），
 * 第一版实现采用更可控的近似：
 * <ul>
 *   <li>对基地守卫施加短时间的高强度保护（抗性/再生/吸收）。</li>
 *   <li>强度随 bonusMultiplier 增长（9 转通常为 3.0）。</li>
 * </ul>
 * </p>
 */
public class ZhiDaoDestinyRewriteEffect implements IBastionSkillEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_zhi_dao_destiny_rewrite";

    private static final int DEFAULT_RADIUS = 48;
    private static final int MIN_RADIUS = 1;

    private static final int BUFF_DURATION_TICKS = 200;

    private static final int BASE_RESISTANCE_AMPLIFIER = 2;
    private static final int BASE_REGEN_AMPLIFIER = 1;
    private static final int BASE_ABSORPTION_AMPLIFIER = 1;

    @Override
    public String getShazhaoId() {
        return SHAZHAO_ID;
    }

    @Override
    public boolean onBastionActivate(final BastionSkillContext context) {
        if (context == null || context.level() == null || context.bastion() == null) {
            return false;
        }

        final Map<String, String> metadata = context.metadata();
        final int radius = clampInt(
            getInt(metadata, "radius", DEFAULT_RADIUS),
            MIN_RADIUS,
            DEFAULT_RADIUS * 4
        );
        final BlockPos core = context.bastion().corePos();

        final int ampBonus = (int) Math.floor(Math.max(0.0, context.bonusMultiplier() - 1.0));

        final int resistanceAmp = BASE_RESISTANCE_AMPLIFIER + ampBonus;
        final int regenAmp = BASE_REGEN_AMPLIFIER + ampBonus;
        final int absorptionAmp = BASE_ABSORPTION_AMPLIFIER + ampBonus;

        boolean applied = false;
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
                    MobEffects.DAMAGE_RESISTANCE,
                    BUFF_DURATION_TICKS,
                    resistanceAmp,
                    false,
                    true,
                    true
                )
            );
            guardian.addEffect(
                new MobEffectInstance(
                    MobEffects.REGENERATION,
                    BUFF_DURATION_TICKS,
                    regenAmp,
                    false,
                    true,
                    true
                )
            );
            guardian.addEffect(
                new MobEffectInstance(
                    MobEffects.ABSORPTION,
                    BUFF_DURATION_TICKS,
                    absorptionAmp,
                    false,
                    true,
                    true
                )
            );
            applied = true;
        }

        return applied;
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
