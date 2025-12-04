package com.Kizunad.customNPCs.entity.render;

import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * 自定义 NPC 渲染器：复用僵尸模型+占位纹理。
 */
public class CustomNpcRenderer
    extends HumanoidMobRenderer<CustomNpcEntity, PlayerModel<CustomNpcEntity>> {

    private static final float SHADOW_RADIUS = 0.5f;

    public CustomNpcRenderer(EntityRendererProvider.Context context) {
        super(
            context,
            new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false),
            SHADOW_RADIUS
        );
    }

    @Override
    public ResourceLocation getTextureLocation(CustomNpcEntity entity) {
        return entity.getSkinTexture();
    }
}
