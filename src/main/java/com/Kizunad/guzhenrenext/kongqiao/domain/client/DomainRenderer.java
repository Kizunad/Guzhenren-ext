package com.Kizunad.guzhenrenext.kongqiao.domain.client;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

/**
 * 通用领域 PNG 渲染器（Phase 3）。
 * <p>
 * 目标：在世界中渲染“领域平面 PNG”，并支持网络同步与移除。
 * </p>
 * <p>
 * 说明：该实现参考 {@code ChestCavityForge} 的 DomainRenderer。
 * </p>
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = GuzhenrenExt.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class DomainRenderer {

    /** 默认高度偏移（格）。
     * <p>
     * 领域平面贴图通常需要略微抬高，避免与地表 Z-Fighting。
     * </p>
     */
    public static final double DEFAULT_HEIGHT_OFFSET = 0.02;

    private static final float DEFAULT_ALPHA = 0.75F;

    private static final int FULL_BRIGHT = 0xF000F0;
    private static final int OVERLAY_NONE = 0;

    private static final double ROTATION_DEGREES_FULL = 360.0;

    private static final Map<UUID, ClientDomainData> CLIENT_DOMAINS = new ConcurrentHashMap<>();

    private static Object lastClientLevelIdentity;

    private DomainRenderer() {}

    /**
     * 注册或更新客户端领域数据。
     */
    /**
     * 注册或更新客户端领域数据。
     */
    public static void registerDomain(com.Kizunad.guzhenrenext.kongqiao.domain.DomainData domain) {
        if (domain == null || domain.domainId() == null) {
            return;
        }
        CLIENT_DOMAINS.put(
            domain.domainId(),
            new ClientDomainData(
                domain.domainId(),
                domain.ownerUuid(),
                domain.centerX(),
                domain.centerY(),
                domain.centerZ(),
                domain.radius(),
                domain.level(),
                normalizeTexture(domain.texture()),
                domain.heightOffset(),
                domain.alpha() <= 0.0F ? DEFAULT_ALPHA : domain.alpha(),
                domain.rotationSpeed()
            )
        );
    }

    private static ResourceLocation normalizeTexture(ResourceLocation texturePath) {
        if (texturePath != null) {
            return texturePath;
        }
        return ResourceLocation.fromNamespaceAndPath(
            GuzhenrenExt.MODID,
            "textures/misc/empty.png"
        );
    }

    /**
     * 移除客户端领域。
     */
    public static void removeDomain(UUID domainId) {
        if (domainId == null) {
            return;
        }
        CLIENT_DOMAINS.remove(domainId);
    }

    /**
     * 清空所有客户端领域（切换世界时调用）。
     */
    public static void clearAll() {
        CLIENT_DOMAINS.clear();
    }

    @SubscribeEvent
    public static void onRenderLevelStage(final RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            if (!CLIENT_DOMAINS.isEmpty()) {
                clearAll();
            }
            lastClientLevelIdentity = null;
            return;
        }

        if (lastClientLevelIdentity != minecraft.level) {
            clearAll();
            lastClientLevelIdentity = minecraft.level;
        }

        if (CLIENT_DOMAINS.isEmpty()) {
            return;
        }

        final PoseStack poseStack = event.getPoseStack();
        final Camera camera = event.getCamera();
        final Vec3 cameraPos = camera.getPosition();
        final MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();

        for (ClientDomainData domain : CLIENT_DOMAINS.values()) {
            renderDomain(poseStack, buffer, cameraPos, domain);
        }

        buffer.endBatch();
    }

    private static void renderDomain(
        final PoseStack poseStack,
        final MultiBufferSource buffer,
        final Vec3 cameraPos,
        final ClientDomainData domain
    ) {
        if (domain == null || domain.radius <= 0.0) {
            return;
        }

        poseStack.pushPose();

        final double renderX = domain.centerX - cameraPos.x;
        final double renderY = domain.centerY + domain.heightOffset - cameraPos.y;
        final double renderZ = domain.centerZ - cameraPos.z;
        poseStack.translate(renderX, renderY, renderZ);

        final long gameTime = Minecraft.getInstance().level == null
            ? 0
            : Minecraft.getInstance().level.getGameTime();
        final float rotation = (float) ((gameTime * domain.rotationSpeed) % ROTATION_DEGREES_FULL);
        if (domain.rotationSpeed != 0.0F) {
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        }

        final float scale = (float) domain.radius;
        poseStack.scale(scale, 1.0F, scale);

        final ResourceLocation resolved = TransparentTextureResolver.getOrProcess(domain.texturePath);
        final RenderType renderType = RenderType.entityTranslucent(resolved);
        final VertexConsumer consumer = buffer.getBuffer(renderType);
        final Matrix4f matrix = poseStack.last().pose();

        // 水平四边形（顶点顺序：左下、右下、右上、左上）
        addVertex(consumer, new QuadVertex(matrix, -1.0F, 0.0F, -1.0F, 0.0F, 1.0F, domain.alpha));
        addVertex(consumer, new QuadVertex(matrix, 1.0F, 0.0F, -1.0F, 1.0F, 1.0F, domain.alpha));
        addVertex(consumer, new QuadVertex(matrix, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, domain.alpha));
        addVertex(consumer, new QuadVertex(matrix, -1.0F, 0.0F, 1.0F, 0.0F, 0.0F, domain.alpha));

        poseStack.popPose();
    }

    private static void addVertex(final VertexConsumer consumer, final QuadVertex vertex) {
        consumer
            .addVertex(vertex.matrix, vertex.x, vertex.y, vertex.z)
            .setColor(1.0F, 1.0F, 1.0F, vertex.alpha)
            .setUv(vertex.u, vertex.v)
            .setOverlay(OVERLAY_NONE)
            .setLight(FULL_BRIGHT)
            .setNormal(0.0F, 1.0F, 0.0F);
    }

    private record QuadVertex(
        Matrix4f matrix,
        float x,
        float y,
        float z,
        float u,
        float v,
        float alpha
    ) {}

    public record ClientDomainData(
        UUID domainId,
        UUID ownerUuid,
        double centerX,
        double centerY,
        double centerZ,
        double radius,
        int level,
        ResourceLocation texturePath,
        double heightOffset,
        float alpha,
        float rotationSpeed
    ) {}
}
