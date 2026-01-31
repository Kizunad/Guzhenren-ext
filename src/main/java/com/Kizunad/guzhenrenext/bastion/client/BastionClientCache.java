package com.Kizunad.guzhenrenext.bastion.client;

import com.Kizunad.guzhenrenext.bastion.BastionDao;
import com.Kizunad.guzhenrenext.bastion.BastionState;
import com.Kizunad.guzhenrenext.bastion.network.ClientboundBastionSyncPayload;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;

/**
 * 客户端基地数据缓存 - 存储从服务端同步的基地渲染数据。
 * <p>
 * 此类仅在客户端使用，用于：
 * <ul>
 *   <li>存储附近基地的位置和属性</li>
 *   <li>为边界渲染提供数据源</li>
 *   <li>支持基地状态的视觉反馈</li>
 * </ul>
 * </p>
 */
public final class BastionClientCache {

    private BastionClientCache() {
        // 工具类
    }

    // ===== 缓存数据结构 =====

    /**
     * 客户端基地数据记录。
     *
     * @param id                  基地唯一标识符
     * @param corePos             核心方块坐标
     * @param dao                 道途类型
     * @param tier                转数
     * @param radius              节点扩张半径
     * @param auraRadius          光环影响半径（用于边界渲染）
     * @param persistedState      持久化状态（ACTIVE/DESTROYED）
     * @param sealedUntilGameTime 封印解除时间（0 表示未封印）
     * @param color               渲染颜色（基于道途）
     */
    public record CachedBastion(
            UUID id,
            BlockPos corePos,
            BastionDao dao,
            int tier,
            int radius,
            int auraRadius,
            BastionState persistedState,
            long sealedUntilGameTime,
            int color
    ) {
        /**
         * 从网络数据包创建缓存条目。
         *
         * @param payload 同步数据包
         * @return 缓存条目
         */
        public static CachedBastion fromPayload(ClientboundBastionSyncPayload payload) {
            BastionDao dao = payload.getDao();
            return new CachedBastion(
                payload.bastionId(),
                payload.getCorePos(),
                dao,
                payload.tier(),
                payload.radius(),
                payload.auraRadius(),
                payload.getState(),
                payload.sealedUntilGameTime(),
                dao.getColorWithAlpha()
            );
        }

        /**
         * 获取当前有效状态（考虑封印时间）。
         *
         * @param currentGameTime 当前游戏时间
         * @return 有效状态
         */
        public BastionState getEffectiveState(long currentGameTime) {
            return BastionState.getEffectiveState(persistedState, sealedUntilGameTime, currentGameTime);
        }
    }

    /** 基地缓存映射（ID -> 数据）。 */
    private static final Map<UUID, CachedBastion> CACHE = new ConcurrentHashMap<>();

    // ===== 公开 API =====

    /**
     * 注册或更新基地数据。
     * <p>
     * 由网络包处理器调用。
     * </p>
     *
     * @param payload 同步数据包
     */
    public static void register(ClientboundBastionSyncPayload payload) {
        CachedBastion cached = CachedBastion.fromPayload(payload);
        CACHE.put(cached.id(), cached);
    }

    /**
     * 从缓存移除基地。
     *
     * @param bastionId 基地 ID
     */
    public static void unregister(UUID bastionId) {
        CACHE.remove(bastionId);
    }

    /**
     * 获取所有缓存的基地。
     *
     * @return 基地集合（不可修改）
     */
    public static Collection<CachedBastion> getAll() {
        return CACHE.values();
    }

    /**
     * 根据 ID 获取缓存的基地。
     *
     * @param bastionId 基地 ID
     * @return 基地数据，如果不存在则返回 null
     */
    public static CachedBastion get(UUID bastionId) {
        return CACHE.get(bastionId);
    }

    /**
     * 清空所有缓存（用于世界切换或登出）。
     */
    public static void clear() {
        CACHE.clear();
    }

    /**
     * 获取缓存大小。
     *
     * @return 缓存中的基地数量
     */
    public static int size() {
        return CACHE.size();
    }
}
