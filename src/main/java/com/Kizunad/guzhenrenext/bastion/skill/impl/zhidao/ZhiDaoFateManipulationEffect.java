package com.Kizunad.guzhenrenext.bastion.skill.impl.zhidao;

import com.Kizunad.guzhenrenext.bastion.skill.BastionSkillContext;
import com.Kizunad.guzhenrenext.bastion.skill.IBastionSkillEffect;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;

/**
 * 智道高转主动：因果操控。
 * <p>
 * 触发方式：BastionTicker 在 FULL tick（每秒）驱动 BastionHighTierSkillService，
 * 并由该 Service 处理技能冷却（每基地每技能）。
 * </p>
 * <p>
 * 设计：
 * <ul>
 *   <li>对领域内玩家施加短时负面状态（减速/虚弱），表达“局势逆转”。</li>
 *   <li>同时对守卫施加短时增益（速度/力量），鼓励守卫主动压制。</li>
 * </ul>
 * </p>
 */
public class ZhiDaoFateManipulationEffect implements IBastionSkillEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_zhi_dao_fate_manipulation";

    private static final int DEFAULT_RADIUS = 32;
    private static final int MIN_RADIUS = 1;

    private static final int PLAYER_DEBUFF_DURATION_TICKS = 200;
    private static final int GUARDIAN_BUFF_DURATION_TICKS = 200;

    private static final int BASE_DEBUFF_AMPLIFIER = 0;
    private static final int BASE_BUFF_AMPLIFIER = 0;

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
        final int playerDebuffAmp = BASE_DEBUFF_AMPLIFIER + ampBonus;
        final int guardianBuffAmp = BASE_BUFF_AMPLIFIER + ampBonus;

        boolean applied = false;

        // 1) Debuff 玩家
        for (ServerPlayer player : context.targets()) {
            if (player == null) {
                continue;
            }
            if (player.blockPosition().distSqr(core) > (long) radius * radius) {
                continue;
            }

            player.addEffect(
                new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    PLAYER_DEBUFF_DURATION_TICKS,
                    playerDebuffAmp,
                    false,
                    true,
                    true
                )
            );
            player.addEffect(
                new MobEffectInstance(
                    MobEffects.WEAKNESS,
                    PLAYER_DEBUFF_DURATION_TICKS,
                    playerDebuffAmp,
                    false,
                    true,
                    true
                )
            );
            applied = true;
        }

        // 2) Buff 守卫
        for (Mob guardian : context.guardians()) {
            if (guardian == null) {
                continue;
            }
            if (guardian.blockPosition().distSqr(core) > (long) radius * radius) {
                continue;
            }

            guardian.addEffect(
                new MobEffectInstance(
                    MobEffects.MOVEMENT_SPEED,
                    GUARDIAN_BUFF_DURATION_TICKS,
                    guardianBuffAmp,
                    false,
                    false,
                    true
                )
            );
            guardian.addEffect(
                new MobEffectInstance(
                    MobEffects.DAMAGE_BOOST,
                    GUARDIAN_BUFF_DURATION_TICKS,
                    guardianBuffAmp,
                    false,
                    false,
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
