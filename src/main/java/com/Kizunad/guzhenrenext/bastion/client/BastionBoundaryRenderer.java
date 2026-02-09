package com.Kizunad.guzhenrenext.bastion.client;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.bastion.BastionState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
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

    /** 渲染距离缓冲区（确保玩家接近边缘时能看到边界）。 */
    private static final int RENDER_DISTANCE_BUFFER = 64;

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

    /** chunk 坐标位移量。 */
    private static final int CHUNK_SHIFT = 4;

    /** chunk 大小。 */
    private static final int CHUNK_SIZE = 16;

    /** chunk 掩码。 */
    private static final int CHUNK_MASK = 15;

    /** 半个 chunk 大小。 */
    private static final int HALF_CHUNK = 8;

    /** 渲染缓冲距离平方。 */
    private static final int RENDER_BUFFER_SQR = 256;

    /** Alpha通道在数组中的索引。 */
    private static final int ALPHA_INDEX = 3;

    /** 半个方块偏移量。 */
    private static final double HALF_BLOCK_OFFSET = 0.5;

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
     * <p>
     * 使用 auraRadius（光环影响半径）近似计算覆盖的区块，并渲染区块边界网格。
     * 这与 ApertureTerritory 的区块制领地逻辑保持一致。
     * </p>
     */
    private static void renderBastionBoundary(
            final PoseStack poseStack,
            final MultiBufferSource buffer,
            final Vec3 cameraPos,
            final BastionClientCache.CachedBastion bastion,
            final long gameTime) {
        // 距离检查：使用 auraRadius 确保大光环基地也能正确渲染
        double dx = bastion.corePos().getX() - cameraPos.x;
        double dz = bastion.corePos().getZ() - cameraPos.z;
        double distSq = dx * dx + dz * dz;
        int effectiveRadius = bastion.auraRadius() + RENDER_DISTANCE_BUFFER;
        if (distSq > (long) effectiveRadius * effectiveRadius) {
            return;
        }

        poseStack.pushPose();

        // 计算渲染中心点（以核心方块中心为原点，带高度偏移）
        double renderX = bastion.corePos().getX() + BLOCK_CENTER_OFFSET - cameraPos.x;
        double renderY = bastion.corePos().getY() + HEIGHT_OFFSET - cameraPos.y;
        double renderZ = bastion.corePos().getZ() + BLOCK_CENTER_OFFSET - cameraPos.z;
        poseStack.translate(renderX, renderY, renderZ);

        // 获取渲染参数
        float red;
        float green;
        float blue;
        float alpha;

        // 使用 gameTime 动态派生有效状态
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

        // 渲染区块领地网格
        renderTerritoryGrid(
                poseStack,
                buffer,
                bastion.corePos(),
                bastion.auraRadius(),
                new float[]{red, green, blue, alpha}
        );

        poseStack.popPose();
    }

    /**
     * 渲染区块领地网格。
     * <p>
     * 遍历光环范围内的所有区块，绘制其边界。
     * </p>
     */
    private static void renderTerritoryGrid(
            final PoseStack poseStack,
            final MultiBufferSource buffer,
            final BlockPos corePos,
            final int radius,
            final float[] rgba) {
        final VertexConsumer consumer = buffer.getBuffer(RenderType.lines());
        final Matrix4f matrix = poseStack.last().pose();

        float red = rgba[0];
        float green = rgba[1];
        float blue = rgba[2];
        float alpha = rgba[ALPHA_INDEX];

        // 核心所在的区块坐标
        int coreChunkX = corePos.getX() >> CHUNK_SHIFT;
        int coreChunkZ = corePos.getZ() >> CHUNK_SHIFT;

        // 计算受影响的区块范围（简单近似：覆盖半径的区块）
        int chunkRadius = (radius + CHUNK_MASK) >> CHUNK_SHIFT;
        int minCx = coreChunkX - chunkRadius;
        int maxCx = coreChunkX + chunkRadius;
        int minCz = coreChunkZ - chunkRadius;
        int maxCz = coreChunkZ + chunkRadius;

        // 核心中心相对于渲染原点(0,0,0)的偏移是0
        // 但我们需要计算区块角点相对于渲染原点的坐标
        // 渲染原点 = corePos.getX() + 0.5, corePos.getZ() + 0.5
        double coreCenterX = corePos.getX() + HALF_BLOCK_OFFSET;
        double coreCenterZ = corePos.getZ() + HALF_BLOCK_OFFSET;

        long radiusSq = (long) radius * radius;

        for (int cx = minCx; cx <= maxCx; cx++) {
            for (int cz = minCz; cz <= maxCz; cz++) {
                // 计算区块中心，判断是否在半径内
                int chunkCenterX = (cx << CHUNK_SHIFT) + HALF_CHUNK;
                int chunkCenterZ = (cz << CHUNK_SHIFT) + HALF_CHUNK;
                double distSq = (chunkCenterX - coreCenterX) * (chunkCenterX - coreCenterX)
                        + (chunkCenterZ - coreCenterZ) * (chunkCenterZ - coreCenterZ);

                // 如果区块中心在半径内（或接近边缘），则视为领地的一部分
                // 这里稍微放宽判断，或者严格按照 ApertureTerritory 的逻辑（通常是只要包含核心半径内的块）
                // 为了视觉美观，使用 center dist <= radius + 8 (半个区块缓冲)
                if (distSq <= radiusSq + RENDER_BUFFER_SQR) { // +16^2 buffer roughly
                    // 计算该区块相对于渲染原点的四个角坐标
                    float x1 = (float) ((cx << CHUNK_SHIFT) - coreCenterX);
                    float z1 = (float) ((cz << CHUNK_SHIFT) - coreCenterZ);
                    float x2 = x1 + CHUNK_SIZE;
                    float z2 = z1 + CHUNK_SIZE;

                    // 绘制矩形（四条边）
                    // 优化：相邻区块的边会重叠，渲染两次问题不大，或者可以只画两条边？
                    // 为了完整性，画四条边简单直接。

                    // Line 1: x1,z1 -> x2,z1
                    consumer.addVertex(matrix, x1, 0, z1)
                            .setColor(red, green, blue, alpha)
                            .setNormal(poseStack.last(), 0, 1, 0);
                    consumer.addVertex(matrix, x2, 0, z1)
                            .setColor(red, green, blue, alpha)
                            .setNormal(poseStack.last(), 0, 1, 0);

                    // Line 2: x2,z1 -> x2,z2
                    consumer.addVertex(matrix, x2, 0, z1)
                            .setColor(red, green, blue, alpha)
                            .setNormal(poseStack.last(), 0, 1, 0);
                    consumer.addVertex(matrix, x2, 0, z2)
                            .setColor(red, green, blue, alpha)
                            .setNormal(poseStack.last(), 0, 1, 0);

                    // Line 3: x2,z2 -> x1,z2
                    consumer.addVertex(matrix, x2, 0, z2)
                            .setColor(red, green, blue, alpha)
                            .setNormal(poseStack.last(), 0, 1, 0);
                    consumer.addVertex(matrix, x1, 0, z2)
                            .setColor(red, green, blue, alpha)
                            .setNormal(poseStack.last(), 0, 1, 0);

                    // Line 4: x1,z2 -> x1,z1
                    consumer.addVertex(matrix, x1, 0, z2)
                            .setColor(red, green, blue, alpha)
                            .setNormal(poseStack.last(), 0, 1, 0);
                    consumer.addVertex(matrix, x1, 0, z1)
                            .setColor(red, green, blue, alpha)
                            .setNormal(poseStack.last(), 0, 1, 0);
                }
            }
        }
    }
}
