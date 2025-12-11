package com.Kizunad.guzhenrenext.util;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 服务端计时器工具类。
 * <p>
 * 基于 ServerTickEvent 实现，提供简单的定时任务管理。
 * 支持启动、停止、暂停、恢复、获取进度以及回调。
 * 所有时间单位均为 Tick (1/20 秒)。
 * </p>
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public class TimerUtil {

    private static final Map<Long, TimerTask> TASKS = new ConcurrentHashMap<>();
    private static final AtomicLong ID_GENERATOR = new AtomicLong(0);

    private TimerUtil() {}

    /**
     * 启动一个计时器。
     *
     * @param durationTicks 持续时间 (Tick)
     * @param callback      计时结束时的回调
     * @return 计时器 ID (可用于后续操作)
     */
    public static long start(int durationTicks, Runnable callback) {
        long id = ID_GENERATOR.incrementAndGet();
        TASKS.put(id, new TimerTask(durationTicks, callback));
        return id;
    }

    /**
     * 停止并移除一个计时器。
     *
     * @param id 计时器 ID
     */
    public static void stop(long id) {
        TASKS.remove(id);
    }

    /**
     * 获取计时器当前剩余时间 (Tick)。
     *
     * @param id 计时器 ID
     * @return 剩余 Tick 数，若不存在则返回 -1
     */
    public static int getRemaining(long id) {
        TimerTask task = TASKS.get(id);
        return task != null ? task.remainingTicks : -1;
    }
    
    /**
     * 获取计时器总持续时间 (Tick)。
     * 
     * @param id 计时器 ID
     * @return 总 Tick 数，若不存在则返回 -1
     */
    public static int getTotal(long id) {
        TimerTask task = TASKS.get(id);
        return task != null ? task.totalTicks : -1;
    }

    /**
     * 重新设置计时器的时间。
     *
     * @param id            计时器 ID
     * @param durationTicks 新的持续时间
     */
    public static void set(long id, int durationTicks) {
        TimerTask task = TASKS.get(id);
        if (task != null) {
            task.remainingTicks = durationTicks;
            task.totalTicks = durationTicks;
        }
    }

    /**
     * 判断计时器是否存在且正在运行。
     */
    public static boolean isRunning(long id) {
        return TASKS.containsKey(id);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (TASKS.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<Long, TimerTask>> iterator = TASKS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, TimerTask> entry = iterator.next();
            TimerTask task = entry.getValue();

            if (task.remainingTicks > 0) {
                task.remainingTicks--;
            }

            if (task.remainingTicks <= 0) {
                try {
                    if (task.callback != null) {
                        task.callback.run();
                    }
                } catch (Exception e) {
                    // 防止回调报错导致整个 Loop 崩溃
                    e.printStackTrace();
                } finally {
                    iterator.remove();
                }
            }
        }
    }

    private static class TimerTask {
        int totalTicks;
        int remainingTicks;
        Runnable callback;

        TimerTask(int duration, Runnable callback) {
            this.totalTicks = duration;
            this.remainingTicks = duration;
            this.callback = callback;
        }
    }
}
