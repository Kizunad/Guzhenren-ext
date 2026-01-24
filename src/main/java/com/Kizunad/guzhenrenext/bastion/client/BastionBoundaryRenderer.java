package com.Kizunad.guzhenrenext.bastion.client;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.bastion.BastionState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

/**
 * 基地边界渲染器 - 在世界中渲染基地领域边界。
 * <p>
 * 渲染效果：
 * <ul>
 *   <li>ACTIVE 状态：半透明彩色边界线（颜色基于道途）</li>
 *   <li>SEALED 状态：灰色边界线 + 闪烁效果</li>
 *   <li>DESTROYED 状态：淡化边界线</li>
 * </ul>
 * </p>
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = GuzhenrenExt.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class BastionBoundaryRenderer {

    private BastionBoundaryRenderer() {
        // 工具类
    }

    // ===== 渲染配置常量 =====

    /** 边界线高度偏移（避免 Z-Fighting）。 */
    private static final float HEIGHT_OFFSET = 0.1f;

    /** 边界线段数（圆形近似）。 */
    private static final int SEGMENTS = 64;

    /** 边界线透明度（ACTIVE）。 */
    private static final float ALPHA_ACTIVE = 0.6f;

    /** 边界线透明度（SEALED）。 */
    private static final float ALPHA_SEALED = 0.4f;

    /** 边界线透明度（DESTROYED）。 */
    private static final float ALPHA_DESTROYED = 0.2f;

    /** 封印闪烁速度（弧度/tick）。 */
    private static final float SEALED_BLINK_SPEED = 0.15f;

    /** 封印闪烁振幅。 */
    private static final float SEALED_BLINK_AMPLITUDE = 0.2f;

    /** 渲染距离阈值的平方。 */
    private static final double RENDER_DISTANCE_SQUARED = 256.0 * 256.0;

    /** 颜色分量提取常量。 */
    private static final int COLOR_SHIFT_RED = 16;
    private static final int COLOR_SHIFT_GREEN = 8;
    private static final int COLOR_MASK = 0xFF;
    private static final float COLOR_NORMALIZE = 255.0f;

    /** 封印灰色。 */
    private static final float SEALED_GRAY = 0.5f;

    /** 销毁状态灰色。 */
    private static final float DESTROYED_GRAY = 0.3f;

    /** 方块中心偏移量。 */
    private static final double BLOCK_CENTER_OFFSET = 0.5;

    /** 缓存的 level 身份（用于世界切换检测）。 */
    private static Object lastClientLevelIdentity;

    // ===== 事件处理 =====

    /**
     * 世界渲染事件处理。
     */
    @SubscribeEvent
    public static void onRenderLevelStage(final RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            if (BastionClientCache.size() > 0) {
                BastionClientCache.clear();
            }
            lastClientLevelIdentity = null;
            return;
        }

        // 世界切换时清空缓存
        if (lastClientLevelIdentity != minecraft.level) {
            BastionClientCache.clear();
            lastClientLevelIdentity = minecraft.level;
        }

        if (BastionClientCache.size() == 0) {
            return;
        }

        final PoseStack poseStack = event.getPoseStack();
        final Camera camera = event.getCamera();
        final Vec3 cameraPos = camera.getPosition();
        final MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
        final long gameTime = minecraft.level.getGameTime();

        for (BastionClientCache.CachedBastion bastion : BastionClientCache.getAll()) {
            renderBastionBoundary(poseStack, buffer, cameraPos, bastion, gameTime);
        }

        buffer.endBatch();
    }

    // ===== 边界渲染 =====

    /**
     * 渲染单个基地的边界。
     */
    private static void renderBastionBoundary(
            final PoseStack poseStack,
            final MultiBufferSource buffer,
            final Vec3 cameraPos,
            final BastionClientCache.CachedBastion bastion,
            final long gameTime) {
        // 距离检查
        double dx = bastion.corePos().getX() - cameraPos.x;
        double dz = bastion.corePos().getZ() - cameraPos.z;
        if (dx * dx + dz * dz > RENDER_DISTANCE_SQUARED) {
            return;
        }

        poseStack.pushPose();

        // 计算渲染位置（相对于相机）
        double renderX = bastion.corePos().getX() + BLOCK_CENTER_OFFSET - cameraPos.x;
        double renderY = bastion.corePos().getY() + HEIGHT_OFFSET - cameraPos.y;
        double renderZ = bastion.corePos().getZ() + BLOCK_CENTER_OFFSET - cameraPos.z;
        poseStack.translate(renderX, renderY, renderZ);

        // 获取渲染参数
        float red;
        float green;
        float blue;
        float alpha;

        // 使用 gameTime 动态派生有效状态（处理封印到期自动变回 ACTIVE）
        BastionState effectiveState = bastion.getEffectiveState(gameTime);

        switch (effectiveState) {
            case SEALED -> {
                red = SEALED_GRAY;
                green = SEALED_GRAY;
                blue = SEALED_GRAY;
                // 闪烁效果
                float blink = (float) Math.sin(gameTime * SEALED_BLINK_SPEED);
                alpha = ALPHA_SEALED + blink * SEALED_BLINK_AMPLITUDE;
            }
            case DESTROYED -> {
                red = DESTROYED_GRAY;
                green = DESTROYED_GRAY;
                blue = DESTROYED_GRAY;
                alpha = ALPHA_DESTROYED;
            }
            default -> {
                // ACTIVE - 使用道途颜色
                int color = bastion.color();
                red = ((color >> COLOR_SHIFT_RED) & COLOR_MASK) / COLOR_NORMALIZE;
                green = ((color >> COLOR_SHIFT_GREEN) & COLOR_MASK) / COLOR_NORMALIZE;
                blue = (color & COLOR_MASK) / COLOR_NORMALIZE;
                alpha = ALPHA_ACTIVE;
            }
        }

        // 渲染圆形边界
        renderCircle(poseStack, buffer, bastion.radius(), red, green, blue, alpha);

        poseStack.popPose();
    }

    /**
     * 渲染圆形边界线。
     */
    private static void renderCircle(
            final PoseStack poseStack,
            final MultiBufferSource buffer,
            final float radius,
            final float red,
            final float green,
            final float blue,
            final float alpha) {
        final VertexConsumer consumer = buffer.getBuffer(RenderType.lines());
        final Matrix4f matrix = poseStack.last().pose();

        final float angleStep = (float) (2.0 * Math.PI / SEGMENTS);

        for (int i = 0; i < SEGMENTS; i++) {
            float angle1 = i * angleStep;
            float angle2 = (i + 1) * angleStep;

            float x1 = (float) (Math.cos(angle1) * radius);
            float z1 = (float) (Math.sin(angle1) * radius);
            float x2 = (float) (Math.cos(angle2) * radius);
            float z2 = (float) (Math.sin(angle2) * radius);

            // 计算法线方向（用于线条渲染）
            float nx = x2 - x1;
            float nz = z2 - z1;
            float len = (float) Math.sqrt(nx * nx + nz * nz);
            if (len > 0) {
                nx /= len;
                nz /= len;
            }

            consumer.addVertex(matrix, x1, 0, z1)
                .setColor(red, green, blue, alpha)
                .setNormal(poseStack.last(), nx, 0, nz);
            consumer.addVertex(matrix, x2, 0, z2)
                .setColor(red, green, blue, alpha)
                .setNormal(poseStack.last(), nx, 0, nz);
        }
    }
}
