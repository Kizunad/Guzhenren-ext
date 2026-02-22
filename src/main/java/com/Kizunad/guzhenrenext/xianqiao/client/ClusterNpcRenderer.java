package com.Kizunad.guzhenrenext.xianqiao.client;

import com.Kizunad.guzhenrenext.xianqiao.spirit.ClusterNpcEntity;
import javax.annotation.Nullable;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ClusterNpcRenderer
    extends HumanoidMobRenderer<ClusterNpcEntity, HumanoidModel<ClusterNpcEntity>> {

    private static final float SHADOW_RADIUS = 0.35F;

    private static final ResourceLocation TEXTURE =
        ResourceLocation.withDefaultNamespace("textures/entity/steve.png");

    public ClusterNpcRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), SHADOW_RADIUS);
    }

    @Override
    public ResourceLocation getTextureLocation(ClusterNpcEntity entity) {
        return TEXTURE;
    }

    @Override
    @Nullable
    protected RenderType getRenderType(
        ClusterNpcEntity entity,
        boolean bodyVisible,
        boolean translucent,
        boolean glowing
    ) {
        return RenderType.entityTranslucent(getTextureLocation(entity));
    }
}
