package com.Kizunad.guzhenrenext.kongqiao.client;

import com.Kizunad.guzhenrenext.kongqiao.service.KongqiaoPressureProjection;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class KongqiaoClientProjectionCache {

    private static volatile KongqiaoPressureProjection currentProjection =
        KongqiaoPressureProjection.empty();

    private KongqiaoClientProjectionCache() {}

    public static void apply(final KongqiaoPressureProjection projection) {
        currentProjection = projection == null
            ? KongqiaoPressureProjection.empty()
            : projection;
    }

    public static KongqiaoPressureProjection getCurrentProjection() {
        return currentProjection;
    }

    public static void clear() {
        currentProjection = KongqiaoPressureProjection.empty();
    }
}
