package com.Kizunad.guzhenrenext.kongqiao.domain;

import java.util.UUID;
import net.minecraft.resources.ResourceLocation;

/**
 * 领域渲染数据（Phase 3）。
 * <p>
 * 该对象用于跨“服务端同步包”和“客户端渲染器”传递必要信息，避免大量参数导致的 Checkstyle 报错。
 * </p>
 */
public record DomainData(
    UUID domainId,
    UUID ownerUuid,
    double centerX,
    double centerY,
    double centerZ,
    double radius,
    int level,
    ResourceLocation texture,
    double heightOffset,
    float alpha,
    float rotationSpeed
) {

    public DomainData {
        if (radius < 0.0) {
            radius = 0.0;
        }
        if (alpha < 0.0F) {
            alpha = 0.0F;
        }
        if (alpha > 1.0F) {
            alpha = 1.0F;
        }
    }
}
