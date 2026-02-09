package com.Kizunad.guzhenrenext.bastion.client.renderer;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.bastion.entity.LandSpiritEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * 地灵实体渲染器。
 * <p>
 * 使用原版狐狸模型，但带有半透明/灵体效果。
 * </p>
 */
public class LandSpiritRenderer extends MobRenderer<LandSpiritEntity, FoxModel<LandSpiritEntity>> {

    private static final float SHADOW_RADIUS = 0.5f;
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        GuzhenrenExt.MODID,
        "textures/entity/land_spirit/fox.png"
    );

    public LandSpiritRenderer(EntityRendererProvider.Context context) {
        super(context, new FoxModel<>(context.bakeLayer(ModelLayers.FOX)), SHADOW_RADIUS);
    }

    @Override
    public ResourceLocation getTextureLocation(LandSpiritEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(
        LandSpiritEntity entity,
        float entityYaw,
        float partialTicks,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight
    ) {
        poseStack.pushPose();
        // 稍微放大一点，或者是浮动效果（entity.tick 已经处理了位置，这里处理缩放/颜色等）
        // 灵体通常有点透明，这里可以通过 RenderType 控制，或者 shader。
        // 原版 MobRenderer 默认不支持半透明，除非重写 getRenderType。
        // 但这里为了简单，先按普通实体渲染，后续可以添加 GlStateManager 设置。
        
        // 稍微调整一下缩放，让它看起来更轻盈
        poseStack.scale(1.0f, 1.0f, 1.0f);
        
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}
