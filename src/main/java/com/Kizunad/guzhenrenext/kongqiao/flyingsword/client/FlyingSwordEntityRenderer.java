package com.Kizunad.guzhenrenext.kongqiao.flyingsword.client;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordConstants;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * 飞剑实体渲染器（最小版）。
 * <p>
 * 直接渲染一个 ItemStack，避免 Phase 2 引入 GeckoLib/复杂模型。
 * </p>
 */
public class FlyingSwordEntityRenderer extends EntityRenderer<FlyingSwordEntity> {

    public FlyingSwordEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
    }

    @Override
    public void render(
        FlyingSwordEntity entity,
        float entityYaw,
        float partialTick,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight
    ) {
        ItemStack stack = entity.getDisplayItemStack();
        if (stack == null || stack.isEmpty()) {
            return;
        }

        poseStack.pushPose();

        // 轻微上下浮动
        final float bob = (float) (
            Mth.sin(
                (entity.tickCount + partialTick)
                    * (float) FlyingSwordConstants.RENDER_BOB_FREQUENCY
            )
                * FlyingSwordConstants.RENDER_BOB_AMPLITUDE
        );
        poseStack.translate(0.0, FlyingSwordConstants.RENDER_BASE_HEIGHT + bob, 0.0);

        // 让剑按实体朝向旋转
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-entity.getYRot()));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(FlyingSwordConstants.RENDER_ROTATE_X_DEG));

        Minecraft.getInstance().getItemRenderer().renderStatic(
            stack,
            ItemDisplayContext.GROUND,
            packedLight,
            OverlayTexture.NO_OVERLAY,
            poseStack,
            buffer,
            entity.level(),
            entity.getId()
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FlyingSwordEntity entity) {
        // 物品渲染器不需要纹理，这里返回一个占位即可。
        return ResourceLocation.withDefaultNamespace("missingno");
    }
}
