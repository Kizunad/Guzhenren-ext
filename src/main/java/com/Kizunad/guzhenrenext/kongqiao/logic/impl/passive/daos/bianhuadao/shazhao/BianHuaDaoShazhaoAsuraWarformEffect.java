package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bianhuadao.shazhao;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 变化道被动杀招【修罗战体】：以凶躯化形，兼顾攻防与抗击退（持续维持）。
 */
public class BianHuaDaoShazhaoAsuraWarformEffect
    extends AbstractBianHuaDaoSustainedAttributeShazhaoEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_bian_hua_asura_warform";

    private static final String META_ARMOR_BONUS = "armor_bonus";
    private static final String META_ARMOR_TOUGHNESS_BONUS =
        "armor_toughness_bonus";
    private static final String META_KNOCKBACK_RESISTANCE_BONUS =
        "knockback_resistance_bonus";
    private static final String META_ATTACK_DAMAGE_BONUS =
        "attack_damage_bonus";

    private static final double DEFAULT_ARMOR_BONUS = 120.0;
    private static final double DEFAULT_ARMOR_TOUGHNESS_BONUS = 60.0;
    private static final double DEFAULT_KNOCKBACK_RESISTANCE_BONUS = 0.25;
    private static final double DEFAULT_ATTACK_DAMAGE_BONUS = 0.15;

    private static final ResourceLocation ARMOR_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_bian_hua_asura_warform_armor"
        );
    private static final ResourceLocation TOUGHNESS_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_bian_hua_asura_warform_toughness"
        );
    private static final ResourceLocation KNOCKBACK_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_bian_hua_asura_warform_knockback_resistance"
        );
    private static final ResourceLocation ATTACK_DAMAGE_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_bian_hua_asura_warform_attack_damage"
        );

    public BianHuaDaoShazhaoAsuraWarformEffect() {
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
                    Attributes.KNOCKBACK_RESISTANCE,
                    AttributeModifier.Operation.ADD_VALUE,
                    META_KNOCKBACK_RESISTANCE_BONUS,
                    DEFAULT_KNOCKBACK_RESISTANCE_BONUS,
                    KNOCKBACK_MODIFIER_ID
                ),
                new AttributeSpec(
                    Attributes.ATTACK_DAMAGE,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                    META_ATTACK_DAMAGE_BONUS,
                    DEFAULT_ATTACK_DAMAGE_BONUS,
                    ATTACK_DAMAGE_MODIFIER_ID
                )
            )
        );
    }

    @Override
    public String getShazhaoId() {
        return SHAZHAO_ID;
    }
}

