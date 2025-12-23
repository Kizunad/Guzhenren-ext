package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bianhuadao.shazhao;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 变化道被动杀招【游龙化影】：以游龙蝶影之形增益身法与攻势（持续维持）。
 */
public class BianHuaDaoShazhaoSoaringDragonPhantomEffect
    extends AbstractBianHuaDaoSustainedAttributeShazhaoEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_bian_hua_soaring_dragon_phantom";

    private static final String META_ARMOR_BONUS = "armor_bonus";
    private static final String META_ARMOR_TOUGHNESS_BONUS =
        "armor_toughness_bonus";
    private static final String META_MOVE_SPEED_BONUS = "move_speed_bonus";
    private static final String META_ATTACK_SPEED_BONUS = "attack_speed_bonus";

    private static final double DEFAULT_ARMOR_BONUS = 90.0;
    private static final double DEFAULT_ARMOR_TOUGHNESS_BONUS = 35.0;
    private static final double DEFAULT_MOVE_SPEED_BONUS = 0.15;
    private static final double DEFAULT_ATTACK_SPEED_BONUS = 0.25;

    private static final ResourceLocation ARMOR_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_bian_hua_soaring_dragon_phantom_armor"
        );
    private static final ResourceLocation TOUGHNESS_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_bian_hua_soaring_dragon_phantom_toughness"
        );
    private static final ResourceLocation MOVE_SPEED_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_bian_hua_soaring_dragon_phantom_move_speed"
        );
    private static final ResourceLocation ATTACK_SPEED_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_bian_hua_soaring_dragon_phantom_attack_speed"
        );

    public BianHuaDaoShazhaoSoaringDragonPhantomEffect() {
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
                    Attributes.ATTACK_SPEED,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                    META_ATTACK_SPEED_BONUS,
                    DEFAULT_ATTACK_SPEED_BONUS,
                    ATTACK_SPEED_MODIFIER_ID
                )
            )
        );
    }

    @Override
    public String getShazhaoId() {
        return SHAZHAO_ID;
    }
}

