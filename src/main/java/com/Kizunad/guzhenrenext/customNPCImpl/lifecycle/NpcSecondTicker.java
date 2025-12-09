package com.Kizunad.guzhenrenext.customNPCImpl.lifecycle;

import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import com.Kizunad.customNPCs.registry.NpcTickRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * NPC 秒级逻辑注册表。
 * <p>
 * 桥接 {@link NpcTickRegistry}，每 20 tick (1秒) 分发一次事件，
 * 专门用于处理如资源恢复、状态检查等不需要每 tick 执行的低频逻辑。
 * </p>
 */
public final class NpcSecondTicker {

    private static final int TICKS_PER_SECOND = 20;
    private static final List<Consumer<CustomNpcEntity>> HANDLERS = new ArrayList<>();
    private static boolean registered = false;

    private NpcSecondTicker() {}

    /**
     * 初始化调度器，将其挂载到 {@link NpcTickRegistry}。
     * 应在模组加载阶段调用一次。
     */
    public static void register() {
        if (registered) {
            return;
        }
        NpcTickRegistry.register(NpcSecondTicker::onTick);
        registered = true;
    }

    /**
     * 注册一个秒级业务处理器。
     *
     * @param handler 接收 NPC 实体并执行逻辑的消费者
     */
    public static void addHandler(Consumer<CustomNpcEntity> handler) {
        HANDLERS.add(handler);
    }

    /**
     * 实际的 Tick 回调，执行频率过滤与分发。
     */
    private static void onTick(CustomNpcEntity npc) {
        // 仅服务端执行，且每秒触发一次
        if (npc.level().isClientSide() || npc.tickCount % TICKS_PER_SECOND != 0) {
            return;
        }

        for (Consumer<CustomNpcEntity> handler : HANDLERS) {
            try {
                handler.accept(npc);
            } catch (Exception ignored) {
                // 捕获异常，防止单个业务报错导致整个循环中断
            }
        }
    }
}
