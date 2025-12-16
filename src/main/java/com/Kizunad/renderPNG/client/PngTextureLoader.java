package com.Kizunad.renderPNG.client;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletionException;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.slf4j.Logger;

/**
 * 基于 {@link NativeImage} + {@link DynamicTexture} 的 PNG 动态纹理加载器。
 * <p>
 * 主要解决两件事：
 * <ul>
 *     <li>把外部/动态 PNG（文件或网络字节）上传到 GPU，注册为可渲染的 {@link ResourceLocation}。</li>
 *     <li>提供缓存与释放接口，避免重复加载导致卡顿/显存泄露。</li>
 * </ul>
 * </p>
 * <p>
 * 线程约束：纹理注册必须在客户端线程执行；本类提供 Async 方法以便在 IO 线程读数据、在客户端线程注册纹理。
 * </p>
 */
@OnlyIn(Dist.CLIENT)
public final class PngTextureLoader {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final Map<String, ResourceLocation> CACHE = new ConcurrentHashMap<>();

    private static final String DEFAULT_NAMESPACE = GuzhenrenExt.MODID;
    private static final String PATH_PREFIX = "dynamic/";
    private static final int HASH_RADIX = 16;
    private static final int DEFAULT_HTTP_TIMEOUT_SECONDS = 10;

    private PngTextureLoader() {}

    /**
     * 从本地文件异步加载 PNG，并返回用于渲染的 {@link ResourceLocation}。
     * <p>
     * IO 与 PNG 解码在后台线程执行，纹理注册在客户端线程执行。
     * </p>
     */
    public static CompletableFuture<ResourceLocation> loadFromFileAsync(
        final String id,
        final Path absolutePath
    ) {
        return loadFromFileAsync(DEFAULT_NAMESPACE, id, absolutePath);
    }

    /**
     * 从本地文件异步加载 PNG，并返回用于渲染的 {@link ResourceLocation}。
     * <p>
     * namespace 用于生成 {@link ResourceLocation}，通常传入你的模组 ID。
     * </p>
     */
    public static CompletableFuture<ResourceLocation> loadFromFileAsync(
        final String namespace,
        final String id,
        final Path absolutePath
    ) {
        Objects.requireNonNull(namespace, "namespace");
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(absolutePath, "absolutePath");

        final String cacheKey = cacheKey(namespace, id);
        final ResourceLocation cached = CACHE.get(cacheKey);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        return CompletableFuture.supplyAsync(() -> readAllBytes(absolutePath))
            .thenCompose(bytes -> registerOnClientThread(namespace, id, bytes))
            .exceptionally(ex -> {
                LOGGER.warn("加载 PNG 失败: id={} path={}", id, absolutePath, unwrapCompletion(ex));
                return null;
            });
    }

    /**
     * 从 URL 异步加载 PNG，并返回用于渲染的 {@link ResourceLocation}。
     * <p>
     * 网络拉取在后台线程执行，纹理注册在客户端线程执行。
     * </p>
     */
    public static CompletableFuture<ResourceLocation> loadFromUrlAsync(
        final String id,
        final URI uri
    ) {
        return loadFromUrlAsync(DEFAULT_NAMESPACE, id, uri);
    }

    /**
     * 从 URL 异步加载 PNG，并返回用于渲染的 {@link ResourceLocation}。
     * <p>
     * namespace 用于生成 {@link ResourceLocation}，通常传入你的模组 ID。
     * </p>
     */
    public static CompletableFuture<ResourceLocation> loadFromUrlAsync(
        final String namespace,
        final String id,
        final URI uri
    ) {
        Objects.requireNonNull(namespace, "namespace");
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(uri, "uri");

        final String cacheKey = cacheKey(namespace, id);
        final ResourceLocation cached = CACHE.get(cacheKey);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        final HttpRequest request = HttpRequest.newBuilder(uri)
            .timeout(Duration.ofSeconds(DEFAULT_HTTP_TIMEOUT_SECONDS))
            .GET()
            .build();

        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
            .thenApply(HttpResponse::body)
            .thenCompose(bytes -> registerOnClientThread(namespace, id, bytes))
            .exceptionally(ex -> {
                LOGGER.warn("加载 PNG 失败: id={} url={}", id, uri, unwrapCompletion(ex));
                return null;
            });
    }

    /**
     * 读取已缓存的纹理句柄。
     */
    @Nullable
    public static ResourceLocation getCached(final String id) {
        return getCached(DEFAULT_NAMESPACE, id);
    }

    /**
     * 读取已缓存的纹理句柄。
     */
    @Nullable
    public static ResourceLocation getCached(final String namespace, final String id) {
        Objects.requireNonNull(namespace, "namespace");
        Objects.requireNonNull(id, "id");
        return CACHE.get(cacheKey(namespace, id));
    }

    /**
     * 释放指定 id 对应的动态纹理（从 {@link TextureManager} 中移除并关闭），并清理缓存。
     */
    public static void release(final String id) {
        release(DEFAULT_NAMESPACE, id);
    }

    /**
     * 释放指定 id 对应的动态纹理（从 {@link TextureManager} 中移除并关闭），并清理缓存。
     */
    public static void release(final String namespace, final String id) {
        Objects.requireNonNull(namespace, "namespace");
        Objects.requireNonNull(id, "id");

        final String key = cacheKey(namespace, id);
        final ResourceLocation location = CACHE.remove(key);
        if (location == null) {
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> minecraft.getTextureManager().release(location));
    }

    /**
     * 释放所有动态纹理并清空缓存。
     */
    public static void clear() {
        clear(DEFAULT_NAMESPACE);
    }

    /**
     * 释放指定 namespace 下所有动态纹理并清空缓存。
     */
    public static void clear(final String namespace) {
        Objects.requireNonNull(namespace, "namespace");

        final Minecraft minecraft = Minecraft.getInstance();
        for (Map.Entry<String, ResourceLocation> entry : CACHE.entrySet()) {
            if (!entry.getKey().startsWith(namespace + "|")) {
                continue;
            }
            final ResourceLocation location = entry.getValue();
            minecraft.execute(() -> minecraft.getTextureManager().release(location));
        }
        CACHE.keySet().removeIf(key -> key.startsWith(namespace + "|"));
    }

    private static CompletableFuture<ResourceLocation> registerOnClientThread(
        final String namespace,
        final String id,
        final byte[] bytes
    ) {
        final CompletableFuture<ResourceLocation> future = new CompletableFuture<>();
        Minecraft.getInstance().execute(() -> {
            try {
                future.complete(registerTexture(namespace, id, bytes));
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    private static ResourceLocation registerTexture(
        final String namespace,
        final String id,
        final byte[] bytes
    ) throws IOException {
        final String cacheKey = cacheKey(namespace, id);
        final ResourceLocation cached = CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        try (InputStream stream = new ByteArrayInputStream(bytes)) {
            final NativeImage image = NativeImage.read(stream);
            final DynamicTexture texture = new DynamicTexture(image);
            final ResourceLocation location = ResourceLocation.fromNamespaceAndPath(
                namespace,
                PATH_PREFIX + safeHash(id)
            );
            Minecraft.getInstance().getTextureManager().register(location, texture);
            CACHE.put(cacheKey, location);
            return location;
        }
    }

    private static byte[] readAllBytes(final Path absolutePath) {
        if (!Files.isRegularFile(absolutePath)) {
            throw new CompletionException(
                new IOException("文件不存在或不是普通文件: " + absolutePath)
            );
        }
        try {
            return Files.readAllBytes(absolutePath);
        } catch (IOException e) {
            throw new CompletionException(e);
        }
    }

    private static String cacheKey(final String namespace, final String id) {
        return namespace + "|" + id;
    }

    private static String safeHash(final String id) {
        return Integer.toUnsignedString(id.hashCode(), HASH_RADIX);
    }

    private static Throwable unwrapCompletion(final Throwable throwable) {
        if (throwable instanceof CompletionException ce && ce.getCause() != null) {
            return ce.getCause();
        }
        return throwable;
    }
}
