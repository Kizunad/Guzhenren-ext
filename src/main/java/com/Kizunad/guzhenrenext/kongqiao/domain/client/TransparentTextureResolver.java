package com.Kizunad.guzhenrenext.kongqiao.domain.client;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * 将带“预览棋盘格”的 PNG 在客户端运行时转为透明背景的动态纹理。
 * <p>
 * 注意：这只是兜底修正，最佳实践仍是提供真正透明的源 PNG。
 * </p>
 * <p>
 * 参考实现：{@code ChestCavityForge} 的 TransparentTextureResolver。
 * </p>
 */
public final class TransparentTextureResolver {

    private static final int BYTE_MASK = 0xFF;
    private static final int ALPHA_SHIFT = 24;
    private static final int RED_SHIFT = 16;
    private static final int GREEN_SHIFT = 8;

    private static final int GRID_LIGHT_GRAY = 0xB3;
    private static final int GRID_DARK_GRAY = 0x8F;
    private static final int GRID_THRESHOLD = 14;

    private static final int TRANSPARENT = 0x00000000;

    private TransparentTextureResolver() {}

    private static final Map<ResourceLocation, ResourceLocation> CACHE = new HashMap<>();

    /**
     * 返回已处理（去棋盘格）的纹理路径；失败时回退原路径。
     */
    public static ResourceLocation getOrProcess(ResourceLocation original) {
        if (original == null) {
            return null;
        }
        ResourceLocation cached = CACHE.get(original);
        if (cached != null) {
            return cached;
        }

        Minecraft minecraft = Minecraft.getInstance();
        try {
            var resOpt = minecraft.getResourceManager().getResource(original);
            if (resOpt.isEmpty()) {
                return original;
            }
            try (InputStream in = resOpt.get().open()) {
                NativeImage src = NativeImage.read(in);
                NativeImage out = new NativeImage(src.getWidth(), src.getHeight(), false);

                // 近似识别两种常见的棋盘格灰度色（亮/暗），允许一定偏差
                final int gridLight = GRID_LIGHT_GRAY;
                final int gridDark = GRID_DARK_GRAY;
                final int threshold = GRID_THRESHOLD;

                for (int y = 0; y < src.getHeight(); y++) {
                    for (int x = 0; x < src.getWidth(); x++) {
                        int argb = src.getPixelRGBA(x, y);
                        int a = (argb >> ALPHA_SHIFT) & BYTE_MASK;
                        int r = (argb >> RED_SHIFT) & BYTE_MASK;
                        int g = (argb >> GREEN_SHIFT) & BYTE_MASK;
                        int b = argb & BYTE_MASK;

                        if (a == 0) {
                            out.setPixelRGBA(x, y, TRANSPARENT);
                            continue;
                        }

                        boolean isGrid =
                            approx(r, gridLight, threshold)
                                && approx(g, gridLight, threshold)
                                && approx(b, gridLight, threshold)
                                || approx(r, gridDark, threshold)
                                    && approx(g, gridDark, threshold)
                                    && approx(b, gridDark, threshold);

                        if (isGrid) {
                            out.setPixelRGBA(x, y, TRANSPARENT);
                        } else {
                            out.setPixelRGBA(x, y, argb);
                        }
                    }
                }

                DynamicTexture dyn = new DynamicTexture(out);
                ResourceLocation dynId = ResourceLocation.fromNamespaceAndPath(
                    original.getNamespace(),
                    "dynamic/domain_clean/" + original.getPath().replace('/', '_')
                );
                minecraft.getTextureManager().register(dynId, dyn);
                CACHE.put(original, dynId);
                src.close();
                return dynId;
            }
        } catch (IOException | RuntimeException ex) {
            return original;
        }
    }

    private static boolean approx(int value, int target, int threshold) {
        return Math.abs(value - target) <= threshold;
    }
}
