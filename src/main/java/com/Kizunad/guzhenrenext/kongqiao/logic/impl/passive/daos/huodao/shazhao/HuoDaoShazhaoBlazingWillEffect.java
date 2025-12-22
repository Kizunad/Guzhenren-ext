package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.huodao.shazhao;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 火/炎道被动杀招【炎心战意】：持续战意（攻击伤害 + 移速，维持消耗，多资源）。
 */
public class HuoDaoShazhaoBlazingWillEffect
    extends AbstractSustainedAttributeShazhaoEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_huo_dao_blazing_will";

    private static final String META_ATTACK_DAMAGE_BONUS =
        "attack_damage_bonus";
    private static final String META_MOVEMENT_SPEED_BONUS =
        "movement_speed_bonus";

    private static final double DEFAULT_ATTACK_DAMAGE_BONUS = 12.0;
    private static final double DEFAULT_MOVEMENT_SPEED_BONUS = 0.05;

    private static final ResourceLocation ATTACK_DAMAGE_MODIFIER_ID = ResourceLocation.parse(
        "guzhenrenext:shazhao_passive_huo_dao_blazing_will_attack_damage"
    );
    private static final ResourceLocation MOVEMENT_SPEED_MODIFIER_ID = ResourceLocation.parse(
        "guzhenrenext:shazhao_passive_huo_dao_blazing_will_movement_speed"
    );

    public HuoDaoShazhaoBlazingWillEffect() {
        super(
            List.of(
                new AttributeSpec(
                    Attributes.ATTACK_DAMAGE,
                    AttributeModifier.Operation.ADD_VALUE,
                    META_ATTACK_DAMAGE_BONUS,
                    DEFAULT_ATTACK_DAMAGE_BONUS,
                    ATTACK_DAMAGE_MODIFIER_ID
                ),
                new AttributeSpec(
                    Attributes.MOVEMENT_SPEED,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                    META_MOVEMENT_SPEED_BONUS,
                    DEFAULT_MOVEMENT_SPEED_BONUS,
                    MOVEMENT_SPEED_MODIFIER_ID
                )
            )
        );
    }

    @Override
    public String getShazhaoId() {
        return SHAZHAO_ID;
    }
}

