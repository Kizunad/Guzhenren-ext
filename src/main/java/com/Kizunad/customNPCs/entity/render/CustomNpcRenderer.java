package com.Kizunad.customNPCs.entity.render;

import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
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
        // 显示盔甲层（与玩家相同模型），否则装备不会被渲染
        this.addLayer(new HumanoidArmorLayer<>(
            this,
            new HumanoidModel<>(
                context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)
            ),
            new HumanoidModel<>(
                context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)
            ),
            context.getModelManager()
        ));
    }

    @Override
    public ResourceLocation getTextureLocation(CustomNpcEntity entity) {
        return entity.getSkinTexture();
    }
}
