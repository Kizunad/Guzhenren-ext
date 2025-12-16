package com.Kizunad.renderPNG.client;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 世界渲染的 PNG 特效管理器。
 * <p>
 * 当前实现以实体 ID 为键，允许在渲染阶段按需绘制“玩家身后 PNG”。
 * 由调用方负责在技能开始/结束时 set/clear，避免长期占用渲染资源。
 * </p>
 */
@OnlyIn(Dist.CLIENT)
public final class BackPngEffectManager {

    private static final Map<Integer, BackPngEffect> EFFECTS = new ConcurrentHashMap<>();

    private BackPngEffectManager() {}

    /**
     * 为指定实体设置背后 PNG 特效。
     * <p>
     * 仅在客户端有意义；如果在非客户端环境调用会被忽略。
     * </p>
     */
    public static void setForEntity(final int entityId, final BackPngEffect effect) {
        Objects.requireNonNull(effect, "effect");
        if (Minecraft.getInstance().level == null) {
            return;
        }
        EFFECTS.put(entityId, effect);
    }

    /**
     * 清除指定实体的背后 PNG 特效。
     */
    public static void clearForEntity(final int entityId) {
        EFFECTS.remove(entityId);
    }

    static Map<Integer, BackPngEffect> effectsView() {
        return EFFECTS;
    }
}

