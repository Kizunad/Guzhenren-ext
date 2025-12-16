package com.Kizunad.renderPNG.client;

import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * “玩家身后 PNG”渲染参数。
 * <p>
 * 尺寸单位为世界坐标（方块单位），建议 0.5 ~ 2.0 之间；颜色使用 ARGB（包含透明度）。
 * </p>
 */
@OnlyIn(Dist.CLIENT)
public record BackPngEffect(
    ResourceLocation texture,
    float width,
    float height,
    float backOffset,
    float upOffset,
    int argbColor,
    boolean fullBright
) {

    public BackPngEffect {
        Objects.requireNonNull(texture, "texture");
    }
}

