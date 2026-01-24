package com.Kizunad.guzhenrenext.bastion.skill.impl.zhidao;

import com.Kizunad.guzhenrenext.bastion.skill.BastionSkillContext;
import com.Kizunad.guzhenrenext.bastion.skill.IBastionSkillEffect;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * 智道高转被动：全知领域。
 * <p>
 * 配置来源：bastion_type.high_tier.skills（category=passive）。
 * </p>
 * <p>
 * 需求语义（来自配置描述）：
 * <ul>
 *   <li>领域内敌人无法隐身：移除 Invisibility，并施加 Glowing 作为可见提示。</li>
 *   <li>“命中率提升”在原版缺少直接概念：这里以守卫获得短时力量/速度作为近似增益。</li>
 * </ul>
 * </p>
 */
public class ZhiDaoOmniscienceDomainEffect implements IBastionSkillEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_zhi_dao_omniscience_domain";

    /** 每秒刷新一次，给 2 秒持续时间避免边界抖动。 */
    private static final int EFFECT_DURATION_TICKS = 40;

    private static final int DEFAULT_RADIUS = 32;
    private static final int MIN_RADIUS = 1;

    private static final int BASE_STRENGTH_AMPLIFIER = 0;
    private static final int BASE_SPEED_AMPLIFIER = 0;

    @Override
    public String getShazhaoId() {
        return SHAZHAO_ID;
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

        // 1) 处理领域内玩家：移除隐身并施加发光
        for (LivingEntity target : context.targets()) {
            if (target == null) {
                continue;
            }
            if (target.blockPosition().distSqr(core) > (long) radius * radius) {
                continue;
            }

            // “无法隐身”
            target.removeEffect(MobEffects.INVISIBILITY);
            target.addEffect(
                new MobEffectInstance(
                    MobEffects.GLOWING,
                    EFFECT_DURATION_TICKS,
                    0,
                    false,
                    false,
                    true
                )
            );
        }

        // 2) 处理守卫：提供轻量增益（近似“命中/压制”）
        final int ampBonus = (int) Math.floor(Math.max(0.0, context.bonusMultiplier() - 1.0));
        final int strengthAmp = BASE_STRENGTH_AMPLIFIER + ampBonus;
        final int speedAmp = BASE_SPEED_AMPLIFIER + ampBonus;

        context.guardians().forEach(guardian -> {
            if (guardian == null) {
                return;
            }
            if (guardian.blockPosition().distSqr(core) > (long) radius * radius) {
                return;
            }
            guardian.addEffect(
                new MobEffectInstance(
                    MobEffects.DAMAGE_BOOST,
                    EFFECT_DURATION_TICKS,
                    strengthAmp,
                    false,
                    false,
                    true
                )
            );
            guardian.addEffect(
                new MobEffectInstance(
                    MobEffects.MOVEMENT_SPEED,
                    EFFECT_DURATION_TICKS,
                    speedAmp,
                    false,
                    false,
                    true
                )
            );
        });
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
