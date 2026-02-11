package com.Kizunad.guzhenrenext.xianqiao.client;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

/**
 * 仙窍维度天空效果。
 * <p>
 * MVP 仅实现深紫色伪时间天空氛围：
 * 1) 固定深紫色雾与天空基色；
 * 2) 关闭地面雾化判定，避免远处突兀白雾；
 * 3) 通过独立维度 effects id 与主世界视觉隔离。
 * </p>
 */
public final class ApertureSkyRenderer extends DimensionSpecialEffects {

    /** 云层高度。 */
    private static final float CLOUD_LEVEL = 192.0F;

    /** 深紫天空红色通道。 */
    private static final double SKY_RED = 0.22D;

    /** 深紫天空绿色通道。 */
    private static final double SKY_GREEN = 0.05D;

    /** 深紫天空蓝色通道。 */
    private static final double SKY_BLUE = 0.32D;

    /** 单例，避免重复分配对象。 */
    public static final ApertureSkyRenderer INSTANCE = new ApertureSkyRenderer();

    private ApertureSkyRenderer() {
        super(CLOUD_LEVEL, true, SkyType.NORMAL, false, false);
    }

    /**
     * 返回亮度相关雾色。
     * <p>
     * 为保证伪时间天空稳定，直接返回固定深紫色，忽略生物群系雾色。
     * </p>
     *
     * @param biomeFogColor 生物群系雾色（未使用）
     * @param brightness 亮度参数（未使用）
     * @return 深紫色
     */
    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 biomeFogColor, float brightness) {
        return new Vec3(SKY_RED, SKY_GREEN, SKY_BLUE);
    }

    /**
     * 仙窍维度不启用地面雾化。
     *
     * @param x 方块 X
     * @param z 方块 Z
     * @return 恒为 false
     */
    @Override
    public boolean isFoggyAt(int x, int z) {
        return false;
    }
}
