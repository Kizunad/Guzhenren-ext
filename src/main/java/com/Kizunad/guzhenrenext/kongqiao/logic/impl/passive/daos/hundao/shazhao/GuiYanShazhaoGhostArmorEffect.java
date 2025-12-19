package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.shazhao;

import net.minecraft.resources.ResourceLocation;

/**
 * 鬼炎杀招【幽炎护甲】：轻量护甲加成的防御型被动。
 */
public class GuiYanShazhaoGhostArmorEffect extends AbstractArmorShazhaoEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_gui_yan_ghost_armor";

    private static final double ARMOR_BONUS = 6.0;
    private static final double SOUL_COST_PER_SECOND = 0.1;
    private static final ResourceLocation ARMOR_MODIFIER_ID =
        ResourceLocation.parse(
            "guzhenrenext:shazhao_passive_gui_yan_ghost_armor_modifier"
        );

    public GuiYanShazhaoGhostArmorEffect() {
        super(ARMOR_MODIFIER_ID, ARMOR_BONUS, SOUL_COST_PER_SECOND);
    }

    @Override
    public String getShazhaoId() {
        return SHAZHAO_ID;
    }
}
