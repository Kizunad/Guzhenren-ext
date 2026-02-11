package com.Kizunad.guzhenrenext.xianqiao.client;

import com.Kizunad.guzhenrenext.xianqiao.spirit.LandSpiritEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import javax.annotation.Nullable;

/**
 * 地灵实体渲染器（客户端）。
 * <p>
 * MVP 阶段复用人形占位模型，并通过半透明渲染层表现灵体效果。
 * </p>
 */
public class LandSpiritRenderer
    extends HumanoidMobRenderer<LandSpiritEntity, HumanoidModel<LandSpiritEntity>> {

    /** 影子半径。 */
    private static final float SHADOW_RADIUS = 0.35F;

    /** 占位纹理。 */
    private static final ResourceLocation TEXTURE =
        ResourceLocation.withDefaultNamespace("textures/entity/steve.png");

    public LandSpiritRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), SHADOW_RADIUS);
    }

    @Override
    public ResourceLocation getTextureLocation(LandSpiritEntity entity) {
        return TEXTURE;
    }

    @Override
    @Nullable
    protected RenderType getRenderType(
        LandSpiritEntity entity,
        boolean bodyVisible,
        boolean translucent,
        boolean glowing
    ) {
        return RenderType.entityTranslucent(getTextureLocation(entity));
    }
}
