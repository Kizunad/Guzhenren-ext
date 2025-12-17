package com.Kizunad.guzhenrenext.client.effect;

import com.Kizunad.renderPNG.client.BackPngEffectManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 客户端背后 PNG 特效的定时清理器。
 * <p>
 * 由于 {@link BackPngEffectManager} 本身只负责“设置/清除”，这里额外维护一个 TTL 映射，
 * 在客户端 Tick 中递减并到期自动清除，避免技能特效残留。
 * </p>
 */
@OnlyIn(Dist.CLIENT)
public final class BackPngTimedEffects {

    private static final Map<Integer, Integer> TTL = new ConcurrentHashMap<>();

    private BackPngTimedEffects() {}

    public static void start(final int entityId, final int durationTicks) {
        TTL.put(entityId, Math.max(1, durationTicks));
    }

    public static void tick() {
        if (TTL.isEmpty()) {
            return;
        }
        for (var it = TTL.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Integer, Integer> entry = it.next();
            int next = entry.getValue() - 1;
            if (next <= 0) {
                BackPngEffectManager.clearForEntity(entry.getKey());
                it.remove();
            } else {
                entry.setValue(next);
            }
        }
    }
}
