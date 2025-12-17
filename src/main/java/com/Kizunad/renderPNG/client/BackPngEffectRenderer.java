package com.Kizunad.renderPNG.client;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * 在世界渲染阶段绘制“实体背后 PNG”。
 * <p>
 * 使用 {@link RenderLevelStageEvent} 的 {@link RenderLevelStageEvent.Stage#AFTER_ENTITIES} 阶段，
 * 便于技能特效与实体深度关系正确（PNG 在实体背后时会被遮挡）。
 * </p>
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
@OnlyIn(Dist.CLIENT)
public final class BackPngEffectRenderer {

    private static final float QUAD_HALF_SIZE = 0.5F;
    private static final float QUAD_Z = 0.0F;
    private static final double ANCHOR_HEIGHT_FACTOR = 0.5D;
    private static final int BYTE_MASK = 0xFF;
    private static final int ALPHA_SHIFT = 24;
    private static final int RED_SHIFT = 16;
    private static final int GREEN_SHIFT = 8;

    private BackPngEffectRenderer() {}

    @SubscribeEvent
    public static void onRenderLevelStage(final RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        final Map<Integer, BackPngEffect> effects = BackPngEffectManager.effectsView();
        if (effects.isEmpty()) {
            return;
        }

        final Camera camera = event.getCamera();
        final Vec3 cameraPos = camera.getPosition();
        final PoseStack poseStack = event.getPoseStack();
        final float partialTick = event.getPartialTick().getGameTimeDeltaTicks();

        for (Map.Entry<Integer, BackPngEffect> entry : effects.entrySet()) {
            final Entity entity = minecraft.level.getEntity(entry.getKey());
            if (entity == null) {
                continue;
            }
            renderBehindEntity(
                entry.getValue(),
                entity,
                partialTick,
                camera,
                cameraPos,
                poseStack,
                minecraft.renderBuffers().bufferSource()
            );
        }
    }

    private static void renderBehindEntity(
        final BackPngEffect effect,
        final Entity entity,
        final float partialTick,
        final Camera camera,
        final Vec3 cameraPos,
        final PoseStack poseStack,
        final MultiBufferSource.BufferSource bufferSource
    ) {
        final Vec3 renderPos = resolveRenderPosition(effect, entity, partialTick);
        final double relX = renderPos.x - cameraPos.x;
        final double relY = renderPos.y - cameraPos.y;
        final double relZ = renderPos.z - cameraPos.z;

        poseStack.pushPose();
        poseStack.translate(relX, relY, relZ);
        poseStack.mulPose(camera.rotation());
        poseStack.scale(-effect.width(), -effect.height(), 1.0F);

        final Pose pose = poseStack.last();
        final RenderType renderType = RenderType.entityTranslucent(effect.texture());
        final VertexConsumer consumer = bufferSource.getBuffer(renderType);

        final int argb = effect.argbColor();
        final int alpha = (argb >> ALPHA_SHIFT) & BYTE_MASK;
        final int red = (argb >> RED_SHIFT) & BYTE_MASK;
        final int green = (argb >> GREEN_SHIFT) & BYTE_MASK;
        final int blue = argb & BYTE_MASK;
        final int packedLight = effect.fullBright() ? LightTexture.FULL_BRIGHT : resolvePackedLight(entity);

        quad(
            consumer,
            pose,
            red,
            green,
            blue,
            alpha,
            packedLight
        );

        poseStack.popPose();
        bufferSource.endBatch(renderType);
    }

    private static Vec3 resolveRenderPosition(
        final BackPngEffect effect,
        final Entity entity,
        final float partialTick
    ) {
        final double x = Mth.lerp(partialTick, entity.xo, entity.getX());
        final double y = Mth.lerp(partialTick, entity.yo, entity.getY());
        final double z = Mth.lerp(partialTick, entity.zo, entity.getZ());

        final float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        final Vec3 backward = Vec3.directionFromRotation(0.0F, yaw).scale(-effect.backOffset());
        final double anchorY = y + entity.getBbHeight() * ANCHOR_HEIGHT_FACTOR + effect.upOffset();
        return new Vec3(x + backward.x, anchorY, z + backward.z);
    }

    private static int resolvePackedLight(final Entity entity) {
        if (Minecraft.getInstance().level == null) {
            return LightTexture.FULL_BRIGHT;
        }
        return LevelRenderer.getLightColor(Minecraft.getInstance().level, entity.blockPosition());
    }

    private static void quad(
        final VertexConsumer consumer,
        final Pose pose,
        final int red,
        final int green,
        final int blue,
        final int alpha,
        final int packedLight
    ) {
        consumer.addVertex(pose, -QUAD_HALF_SIZE, -QUAD_HALF_SIZE, QUAD_Z)
            .setColor(red, green, blue, alpha)
            .setUv(1.0F, 0.0F)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(pose, 0.0F, 0.0F, 1.0F);

        consumer.addVertex(pose, QUAD_HALF_SIZE, -QUAD_HALF_SIZE, QUAD_Z)
            .setColor(red, green, blue, alpha)
            .setUv(0.0F, 0.0F)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(pose, 0.0F, 0.0F, 1.0F);

        consumer.addVertex(pose, QUAD_HALF_SIZE, QUAD_HALF_SIZE, QUAD_Z)
            .setColor(red, green, blue, alpha)
            .setUv(0.0F, 1.0F)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(pose, 0.0F, 0.0F, 1.0F);

        consumer.addVertex(pose, -QUAD_HALF_SIZE, QUAD_HALF_SIZE, QUAD_Z)
            .setColor(red, green, blue, alpha)
            .setUv(1.0F, 1.0F)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }
}
