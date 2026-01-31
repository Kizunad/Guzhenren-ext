package com.Kizunad.guzhenrenext.bastion;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * 基地道途类型枚举（MVP：4 种，预留扩展到 8 种）。
 * <p>
 * 每种道途对应蛊真人体系中的一种主要资源类型。
 * </p>
 */
public enum BastionDao implements StringRepresentable {

    /**
     * 智道 - 消耗/产出念头（niantou）。
     */
    ZHI_DAO("zhi_dao", "nian_tou", BastionDaoColors.ZHI_DAO_COLOR),

    /**
     * 魂道 - 消耗/产出魂魄（hunpo）。
     */
    HUN_DAO("hun_dao", "hun_po", BastionDaoColors.HUN_DAO_COLOR),

    /**
     * 木道 - 消耗/产出真元（zhenyuan）。
     */
    MU_DAO("mu_dao", "zhen_yuan", BastionDaoColors.MU_DAO_COLOR),

    /**
     * 力道 - 消耗/产出精力（jingli）。
     */
    LI_DAO("li_dao", "jing_li", BastionDaoColors.LI_DAO_COLOR);

    // 预留扩展位（最多 8 种）：
    // BING_XUE_DAO("bing_xue_dao", "zhen_yuan", 0x00BCD4),
    // LEI_DAO("lei_dao", "jing_li", 0xFFEB3B),
    // JIN_DAO("jin_dao", "zhen_yuan", 0xFFD700),
    // YAN_DAO("yan_dao", "zhen_yuan", 0xFF5722)

    public static final Codec<BastionDao> CODEC = StringRepresentable.fromEnum(BastionDao::values);

    private final String name;
    private final String primaryResource;
    private final int color;

    BastionDao(String name, String primaryResource, int color) {
        this.name = name;
        this.primaryResource = primaryResource;
        this.color = color;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    /**
     * 返回此道途的主要资源类型 id。
     *
     * @return 资源类型 id（如 "nian_tou"、"hun_po"）
     */
    public String getPrimaryResource() {
        return primaryResource;
    }

    /**
     * 返回此道途的显示颜色（RGB 格式，无 alpha）。
     *
     * @return 颜色整数
     */
    public int getColor() {
        return color;
    }

    /**
     * 返回带完整 alpha 的显示颜色。
     *
     * @return 带 0xFF alpha 的颜色整数
     */
    public int getColorWithAlpha() {
        return BastionDaoColors.ALPHA_MASK | color;
    }

    /**
     * 智道光环：挖掘疲劳 + 缓慢。
     * <p>
     * 设计：
     * <ul>
     *   <li>效果每秒刷新一次，持续 2 秒，避免边界抖动。</li>
     *   <li>等级随转数提升，并受距离衰减影响（边缘更弱）。</li>
     * </ul>
     * </p>
     */
    private static void applyZhiDaoAura(ServerPlayer player, int tier, double falloff) {
        // falloff 在边缘可能接近 minFalloff（默认 5%），这里不做过多数学变换。
        int tierBonus = Math.max(0, tier - 1);
        int amplifier = Math.min(Constants.MAX_AMPLIFIER, tierBonus / 2);

        // 距离越近越强：当 falloff 足够低时，降低一档
        if (falloff < Constants.STRONG_EFFECT_THRESHOLD) {
            amplifier = Math.max(0, amplifier - 1);
        }

        int duration = Constants.EFFECT_DURATION_TICKS;
        player.addEffect(new MobEffectInstance(
            MobEffects.DIG_SLOWDOWN,
            duration,
            amplifier,
            false,
            false,
            true
        ));
        player.addEffect(new MobEffectInstance(
            MobEffects.MOVEMENT_SLOWDOWN,
            duration,
            amplifier,
            false,
            false,
            true
        ));
    }

    public void onAuraTick(ServerPlayer player, int tier, double falloff) {
        if (player == null) {
            return;
        }
        switch (this) {
            case ZHI_DAO -> applyZhiDaoAura(player, tier, falloff);
            default -> {
                // no-op
            }
        }
    }

    /**
     * 道途颜色常量，避免 MagicNumber checkstyle 错误。
     */
    private static final class BastionDaoColors {
        /** 智道颜色：蓝色 */
        static final int ZHI_DAO_COLOR = 0x3498DB;
        /** 魂道颜色：紫色 */
        static final int HUN_DAO_COLOR = 0x9B59B6;
        /** 木道颜色：绿色 */
        static final int MU_DAO_COLOR = 0x27AE60;
        /** 力道颜色：红色 */
        static final int LI_DAO_COLOR = 0xE74C3C;
        /** Alpha 掩码：完全不透明 */
        static final int ALPHA_MASK = 0xFF000000;

        private BastionDaoColors() {
            // 工具类
        }
    }

    /**
     * 道途特化光环常量。
     */
    private static final class Constants {
        /** 每秒刷新一次，但给 2 秒持续避免抖动。 */
        static final int EFFECT_DURATION_TICKS = 40;
        /** 最大效果等级。 */
        static final int MAX_AMPLIFIER = 3;
        /**
         * 强效果阈值：低于该衰减值认为在边缘，减弱一档。
         */
        static final double STRONG_EFFECT_THRESHOLD = 0.2;

        private Constants() {
        }
    }
}
