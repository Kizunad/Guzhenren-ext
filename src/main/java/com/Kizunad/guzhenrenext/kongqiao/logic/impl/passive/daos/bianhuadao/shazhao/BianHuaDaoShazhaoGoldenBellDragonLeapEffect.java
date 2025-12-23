package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bianhuadao.shazhao;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 变化道被动杀招【金钟龙跃】：金钟护体，龙形轻跃，兼顾防护与身法（持续维持）。
 */
public class BianHuaDaoShazhaoGoldenBellDragonLeapEffect
    extends AbstractBianHuaDaoSustainedAttributeShazhaoEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_bian_hua_golden_bell_dragon_leap";

    private static final String META_ARMOR_BONUS = "armor_bonus";
    private static final String META_ARMOR_TOUGHNESS_BONUS =
        "armor_toughness_bonus";
    private static final String META_MOVE_SPEED_BONUS = "move_speed_bonus";
    private static final String META_JUMP_STRENGTH_BONUS = "jump_strength_bonus";

    private static final double DEFAULT_ARMOR_BONUS = 150.0;
    private static final double DEFAULT_ARMOR_TOUGHNESS_BONUS = 70.0;
    private static final double DEFAULT_MOVE_SPEED_BONUS = 0.08;
    private static final double DEFAULT_JUMP_STRENGTH_BONUS = 0.18;

    private static final ResourceLocation ARMOR_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_bian_hua_golden_bell_dragon_leap_armor"
        );
    private static final ResourceLocation TOUGHNESS_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_bian_hua_golden_bell_dragon_leap_toughness"
        );
    private static final ResourceLocation MOVE_SPEED_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_bian_hua_golden_bell_dragon_leap_move_speed"
        );
    private static final ResourceLocation JUMP_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_bian_hua_golden_bell_dragon_leap_jump_strength"
        );

    public BianHuaDaoShazhaoGoldenBellDragonLeapEffect() {
        super(
            List.of(
                new AttributeSpec(
                    Attributes.ARMOR,
                    AttributeModifier.Operation.ADD_VALUE,
                    META_ARMOR_BONUS,
                    DEFAULT_ARMOR_BONUS,
                    ARMOR_MODIFIER_ID
                ),
                new AttributeSpec(
                    Attributes.ARMOR_TOUGHNESS,
                    AttributeModifier.Operation.ADD_VALUE,
                    META_ARMOR_TOUGHNESS_BONUS,
                    DEFAULT_ARMOR_TOUGHNESS_BONUS,
                    TOUGHNESS_MODIFIER_ID
                ),
                new AttributeSpec(
                    Attributes.MOVEMENT_SPEED,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                    META_MOVE_SPEED_BONUS,
                    DEFAULT_MOVE_SPEED_BONUS,
                    MOVE_SPEED_MODIFIER_ID
                ),
                new AttributeSpec(
                    Attributes.JUMP_STRENGTH,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                    META_JUMP_STRENGTH_BONUS,
                    DEFAULT_JUMP_STRENGTH_BONUS,
                    JUMP_MODIFIER_ID
                )
            )
        );
    }

    @Override
    public String getShazhaoId() {
        return SHAZHAO_ID;
    }
}

