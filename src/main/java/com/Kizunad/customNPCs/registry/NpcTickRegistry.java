package com.Kizunad.customNPCs.registry;

import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import java.util.ArrayList;
import java.util.List;

/**
 * NPC 每 tick 的回调注册表。
 * 允许注入持续运行的逻辑，如被动回血、状态监控等。
 */
public class NpcTickRegistry {

    @FunctionalInterface
    public interface TickHandler {
        void handle(CustomNpcEntity npc);
    }

    private static final int PASSIVE_HEAL_INTERVAL_TICKS = 20;
    private static final float PASSIVE_HEAL_RATIO = 0.001f;

    private static final List<TickHandler> HANDLERS = new ArrayList<>();

    static {
        // 内置示例：每秒自然回复 0.1% 血量
        register(npc -> {
            if (npc.level().isClientSide) {
                return;
            }

            // 每秒执行一次 (20 ticks)
            if (npc.tickCount % PASSIVE_HEAL_INTERVAL_TICKS == 0) {
                if (
                    npc.getHealth() < npc.getMaxHealth() && npc.getHealth() > 0
                ) {
                    float healAmount = npc.getMaxHealth() * PASSIVE_HEAL_RATIO; // 0.1%
                    if (healAmount > 0) {
                        npc.heal(healAmount);
                    }
                }
            }
        });
    }

    /**
     * 注册一个 Tick 处理器。
     * @param handler 处理器逻辑
     */
    public static void register(TickHandler handler) {
        HANDLERS.add(handler);
    }

    /**
     * 执行所有注册的处理器（内部调用）。
     */
    public static void onTick(CustomNpcEntity npc) {
        for (TickHandler handler : HANDLERS) {
            handler.handle(npc);
        }
    }
}
