package com.Kizunad.customNPCs.registry;

import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import java.util.ArrayList;
import java.util.List;

/**
 * NPC 每 tick 的回调注册表。
 * 高频逻辑放在此处，低频（每秒）逻辑请使用 NpcSecondRegistry，避免手动取模。
 */
public class NpcTickRegistry {

    @FunctionalInterface
    public interface TickHandler {
        void handle(CustomNpcEntity npc);
    }

    private static final List<TickHandler> HANDLERS = new ArrayList<>();

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
