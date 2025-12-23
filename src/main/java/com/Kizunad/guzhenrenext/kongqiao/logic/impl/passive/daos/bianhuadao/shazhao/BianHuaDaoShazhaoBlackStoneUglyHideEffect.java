package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bianhuadao.shazhao;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 变化道被动杀招【黑石丑皮躯】：黑石负重，丑皮遮形，稳固防护与气血根基（持续维持）。
 */
public class BianHuaDaoShazhaoBlackStoneUglyHideEffect
    extends AbstractBianHuaDaoSustainedAttributeShazhaoEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_bian_hua_black_stone_ugly_hide";

    private static final String META_ARMOR_BONUS = "armor_bonus";
    private static final String META_ARMOR_TOUGHNESS_BONUS =
        "armor_toughness_bonus";
    private static final String META_MAX_HEALTH_BONUS = "max_health_bonus";

    private static final double DEFAULT_ARMOR_BONUS = 18.0;
    private static final double DEFAULT_ARMOR_TOUGHNESS_BONUS = 8.0;
    private static final double DEFAULT_MAX_HEALTH_BONUS = 2.0;

    private static final ResourceLocation ARMOR_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_bian_hua_black_stone_ugly_hide_armor"
        );
    private static final ResourceLocation TOUGHNESS_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_bian_hua_black_stone_ugly_hide_toughness"
        );
    private static final ResourceLocation MAX_HEALTH_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_bian_hua_black_stone_ugly_hide_max_health"
        );

    public BianHuaDaoShazhaoBlackStoneUglyHideEffect() {
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
                    Attributes.MAX_HEALTH,
                    AttributeModifier.Operation.ADD_VALUE,
                    META_MAX_HEALTH_BONUS,
                    DEFAULT_MAX_HEALTH_BONUS,
                    MAX_HEALTH_MODIFIER_ID
                )
            )
        );
    }

    @Override
    public String getShazhaoId() {
        return SHAZHAO_ID;
    }
}

