package com.Kizunad.customNPCs.registry;

import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import java.util.ArrayList;
import java.util.List;

/**
 * NPC 每秒的回调注册表。
 * 统一处理低频逻辑，避免在 Tick 注册表中频繁做取模判断。
 */
public class NpcSecondRegistry {

    @FunctionalInterface
    public interface SecondHandler {
        void handle(CustomNpcEntity npc);
    }

    private static final int SECOND_INTERVAL_TICKS = 20;
    private static final float PASSIVE_HEAL_RATIO = 0.001f;

    private static final List<SecondHandler> HANDLERS = new ArrayList<>();

    static {
        // 内置示例：每秒自然回复 0.1% 血量
        register(npc -> {
            if (npc.level().isClientSide || npc.getHealth() <= 0) {
                return;
            }
            if (npc.getHealth() < npc.getMaxHealth()) {
                float healAmount = npc.getMaxHealth() * PASSIVE_HEAL_RATIO; // 0.1%
                if (healAmount > 0) {
                    npc.heal(healAmount);
                }
            }
        });
    }

    /**
     * 注册一个秒级处理器。
     * @param handler 处理器逻辑
     */
    public static void register(SecondHandler handler) {
        HANDLERS.add(handler);
    }

    /**
     * 执行所有注册的处理器（内部调用）。
     * 仅在服务端且满足 20 tick 间隔时触发。
     */
    public static void onSecond(CustomNpcEntity npc) {
        if (
            npc.level().isClientSide ||
            npc.tickCount % SECOND_INTERVAL_TICKS != 0
        ) {
            return;
        }
        for (SecondHandler handler : HANDLERS) {
            handler.handle(npc);
        }
    }
}
