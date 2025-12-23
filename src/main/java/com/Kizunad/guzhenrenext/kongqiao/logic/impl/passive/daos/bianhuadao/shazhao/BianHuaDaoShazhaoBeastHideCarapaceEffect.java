package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bianhuadao.shazhao;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 变化道被动杀招【兽皮甲胄】：以皮鳞化铠，提升基础防御与气血上限（持续维持）。
 */
public class BianHuaDaoShazhaoBeastHideCarapaceEffect
    extends AbstractBianHuaDaoSustainedAttributeShazhaoEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_bian_hua_beast_hide_carapace";

    private static final String META_ARMOR_BONUS = "armor_bonus";
    private static final String META_ARMOR_TOUGHNESS_BONUS =
        "armor_toughness_bonus";
    private static final String META_MAX_HEALTH_BONUS = "max_health_bonus";

    private static final double DEFAULT_ARMOR_BONUS = 30.0;
    private static final double DEFAULT_ARMOR_TOUGHNESS_BONUS = 12.0;
    private static final double DEFAULT_MAX_HEALTH_BONUS = 4.0;

    private static final ResourceLocation ARMOR_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_bian_hua_beast_hide_carapace_armor"
        );
    private static final ResourceLocation TOUGHNESS_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_bian_hua_beast_hide_carapace_toughness"
        );
    private static final ResourceLocation MAX_HEALTH_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_bian_hua_beast_hide_carapace_max_health"
        );

    public BianHuaDaoShazhaoBeastHideCarapaceEffect() {
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

