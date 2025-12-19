package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.shazhao;

import net.minecraft.resources.ResourceLocation;

/**
 * 魂盾杀招【魂盾铁壁】：小幅度护甲加成的防御型被动。
 */
public class HunDunShazhaoIronWallEffect extends AbstractArmorShazhaoEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_hun_dun_iron_wall";

    private static final double ARMOR_BONUS = 10.0;
    private static final double SOUL_COST_PER_SECOND = 0.1;
    private static final ResourceLocation ARMOR_MODIFIER_ID =
        ResourceLocation.parse(
            "guzhenrenext:shazhao_passive_hun_dun_iron_wall_armor_modifier"
        );

    public HunDunShazhaoIronWallEffect() {
        super(ARMOR_MODIFIER_ID, ARMOR_BONUS, SOUL_COST_PER_SECOND);
    }

    @Override
    public String getShazhaoId() {
        return SHAZHAO_ID;
    }
}
