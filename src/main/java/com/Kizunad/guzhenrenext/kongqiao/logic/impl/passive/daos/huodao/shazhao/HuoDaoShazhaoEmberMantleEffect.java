package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.huodao.shazhao;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 火/炎道被动杀招【余烬披风】：持续护体（护甲/韧性加成，维持消耗，多资源）。
 */
public class HuoDaoShazhaoEmberMantleEffect
    extends AbstractSustainedAttributeShazhaoEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_huo_dao_ember_mantle";

    private static final String META_ARMOR_BONUS = "armor_bonus";
    private static final String META_ARMOR_TOUGHNESS_BONUS =
        "armor_toughness_bonus";

    private static final double DEFAULT_ARMOR_BONUS = 80.0;
    private static final double DEFAULT_ARMOR_TOUGHNESS_BONUS = 30.0;

    private static final ResourceLocation ARMOR_MODIFIER_ID = ResourceLocation.parse(
        "guzhenrenext:shazhao_passive_huo_dao_ember_mantle_armor"
    );
    private static final ResourceLocation TOUGHNESS_MODIFIER_ID = ResourceLocation.parse(
        "guzhenrenext:shazhao_passive_huo_dao_ember_mantle_toughness"
    );

    public HuoDaoShazhaoEmberMantleEffect() {
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
                )
            )
        );
    }

    @Override
    public String getShazhaoId() {
        return SHAZHAO_ID;
    }
}

