package com.Kizunad.guzhenrenext.xianqiao.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * 仙窍维度全局数据。
 * <p>
 * 本数据对象负责：
 * 1) 记录玩家 UUID 与其仙窍信息映射；
 * 2) 维护 nextIndex 用于分配新的仙窍坐标；
 * 3) 持久化到世界存档。
 * </p>
 */
public class ApertureWorldData extends SavedData {

    private static final String DATA_NAME = "guzhenrenext_aperture_world";

    private static final String KEY_NEXT_INDEX = "nextIndex";

    private static final String KEY_APERTURES = "apertures";

    private static final String KEY_INITIALIZED_APERTURES = "initializedApertures";

    private static final String KEY_LAST_LOGOUT_TIMES = "lastLogoutTimes";

    private static final String KEY_OWNER = "owner";

    private static final String KEY_INFO = "info";

    private static final String KEY_LAST_LOGOUT_TIME = "lastLogoutTime";

    private static final int TAG_COMPOUND = Tag.TAG_COMPOUND;

    private static final int TAG_LIST = Tag.TAG_LIST;

    private static final int DEFAULT_NEXT_INDEX = 1;

    private static final int APERTURE_SPACING = 100000;

    private static final int DEFAULT_CENTER_Y = 64;

    private static final int DEFAULT_INITIAL_RADIUS = 48;

    private static final float DEFAULT_TIME_SPEED = 1.0F;

    private static final int DAY_TICKS = 24000;

    private static final int TRIBULATION_CYCLE_DAYS = 20;

    private static final long DEFAULT_TRIBULATION_CYCLE = (long) DAY_TICKS * TRIBULATION_CYCLE_DAYS;

    private static final float DEFAULT_FAVORABILITY = 0.0F;

    private static final float MIN_FAVORABILITY = 0.0F;

    private static final float MAX_FAVORABILITY = 100.0F;

    private static final int DEFAULT_TIER = 1;

    private static final long UNRECORDED_LOGOUT_TIME = -1L;

    private int nextIndex = DEFAULT_NEXT_INDEX;

    private final Map<UUID, ApertureInfo> apertures = new HashMap<>();

    private final Set<UUID> initializedApertures = new HashSet<>();

    private final Map<UUID, Long> lastLogoutTimes = new HashMap<>();

    /**
     * 分配一个新的仙窍信息；若已有记录，则直接返回既有信息。
     *
     * @param owner 玩家 UUID
     * @return 仙窍信息
     */
    public ApertureInfo allocateAperture(UUID owner) {
        ApertureInfo existing = apertures.get(owner);
        if (existing != null) {
            return existing;
        }

        int centerX = nextIndex * APERTURE_SPACING;
        nextIndex++;

        ApertureInfo created = new ApertureInfo(
            new BlockPos(centerX, DEFAULT_CENTER_Y, 0),
            DEFAULT_INITIAL_RADIUS,
            DEFAULT_TIME_SPEED,
            DEFAULT_TRIBULATION_CYCLE,
            false,
            DEFAULT_FAVORABILITY,
            DEFAULT_TIER
        );
        apertures.put(owner, created);
        setDirty();
        return created;
    }

    /**
     * 获取指定玩家仙窍信息。
     *
     * @param owner 玩家 UUID
     * @return 仙窍信息，不存在则返回 null
     */
    @Nullable
    public ApertureInfo getAperture(UUID owner) {
        return apertures.get(owner);
    }

    /**
     * 获取或分配仙窍信息。
     *
     * @param owner 玩家 UUID
     * @return 已存在或新分配的仙窍信息
     */
    public ApertureInfo getOrAllocate(UUID owner) {
        ApertureInfo existing = apertures.get(owner);
        if (existing != null) {
            return existing;
        }
        return allocateAperture(owner);
    }

    /**
     * 更新指定玩家仙窍半径。
     * <p>
     * 由于 {@link ApertureInfo} 为不可变 record，更新时需要构造新实例并替换映射。
     * </p>
     *
     * @param owner 玩家 UUID
     * @param newRadius 新半径
     */
    public void updateRadius(UUID owner, int newRadius) {
        ApertureInfo existing = apertures.get(owner);
        if (existing == null) {
            return;
        }
        ApertureInfo updated = new ApertureInfo(
            existing.center(),
            newRadius,
            existing.timeSpeed(),
            existing.nextTribulationTick(),
            existing.isFrozen(),
            existing.favorability(),
            existing.tier()
        );
        apertures.put(owner, updated);
        setDirty();
    }

    /**
     * 更新指定玩家仙窍好感度。
     * <p>
     * 由于 {@link ApertureInfo} 为不可变 record，更新时需要构造新实例并替换映射。
     * 新值会被限制在 [{@value #MIN_FAVORABILITY}, {@value #MAX_FAVORABILITY}] 区间内，
     * 以避免脏数据破坏后续逻辑。
     * </p>
     *
     * @param owner 玩家 UUID
     * @param newFavorability 新好感度
     */
    public void updateFavorability(UUID owner, float newFavorability) {
        ApertureInfo existing = apertures.get(owner);
        if (existing == null) {
            return;
        }
        float clampedFavorability = clampFavorability(newFavorability);
        ApertureInfo updated = new ApertureInfo(
            existing.center(),
            existing.currentRadius(),
            existing.timeSpeed(),
            existing.nextTribulationTick(),
            existing.isFrozen(),
            clampedFavorability,
            existing.tier()
        );
        apertures.put(owner, updated);
        setDirty();
    }

    /**
     * 更新指定玩家仙窍层级。
     * <p>
     * 由于 {@link ApertureInfo} 为不可变 record，更新时需要构造新实例并替换映射。
     * 层级最低为 {@value #DEFAULT_TIER}，可防止外部输入非法值导致数据越界。
     * </p>
     *
     * @param owner 玩家 UUID
     * @param newTier 新层级
     */
    public void updateTier(UUID owner, int newTier) {
        ApertureInfo existing = apertures.get(owner);
        if (existing == null) {
            return;
        }
        int normalizedTier = normalizeTier(newTier);
        ApertureInfo updated = new ApertureInfo(
            existing.center(),
            existing.currentRadius(),
            existing.timeSpeed(),
            existing.nextTribulationTick(),
            existing.isFrozen(),
            existing.favorability(),
            normalizedTier
        );
        apertures.put(owner, updated);
        setDirty();
    }

    /**
     * 判断指定玩家的仙窍是否已经执行过首次初始化。
     *
     * @param owner 玩家 UUID
     * @return 已初始化返回 true，否则 false
     */
    public boolean isApertureInitialized(UUID owner) {
        return initializedApertures.contains(owner);
    }

    /**
     * 将指定玩家仙窍标记为已初始化。
     *
     * @param owner 玩家 UUID
     */
    public void markApertureInitialized(UUID owner) {
        if (initializedApertures.add(owner)) {
            setDirty();
        }
    }

    /**
     * 设置玩家最后离线时间（以维度 gameTime 计）。
     *
     * @param owner 玩家 UUID
     * @param gameTime 离线时游戏刻
     */
    public void setLastLogoutTime(UUID owner, long gameTime) {
        Long previous = lastLogoutTimes.put(owner, gameTime);
        if (previous == null || previous.longValue() != gameTime) {
            setDirty();
        }
    }

    /**
     * 读取玩家最后离线时间。
     *
     * @param owner 玩家 UUID
     * @return 最后离线刻；若无记录返回 {@value #UNRECORDED_LOGOUT_TIME}
     */
    public long getLastLogoutTime(UUID owner) {
        return lastLogoutTimes.getOrDefault(owner, UNRECORDED_LOGOUT_TIME);
    }

    /**
     * 更新指定玩家仙窍的下次灾劫触发刻（绝对游戏时间）。
     *
     * @param owner 玩家 UUID
     * @param nextTick 下次灾劫触发刻
     */
    public void updateTribulationTick(UUID owner, long nextTick) {
        if (nextTick < 0L) {
            return;
        }
        ApertureInfo existing = apertures.get(owner);
        if (existing == null) {
            return;
        }
        ApertureInfo updated = new ApertureInfo(
            existing.center(),
            existing.currentRadius(),
            existing.timeSpeed(),
            nextTick,
            existing.isFrozen(),
            existing.favorability(),
            existing.tier()
        );
        apertures.put(owner, updated);
        setDirty();
    }

    /**
     * 按离线时长扣减灾劫剩余时间（向下不低于 0）。
     * <p>
     * 该方法用于兼容离线聚合结算模型：
     * 当存档中的 {@code nextTribulationTick} 仍被作为“剩余刻”使用时，
     * 可通过此方法统一扣减。
     * </p>
     *
     * @param owner 玩家 UUID
     * @param elapsedTicks 流逝 tick
     */
    public void reduceTribulationTick(UUID owner, long elapsedTicks) {
        if (elapsedTicks <= 0L) {
            return;
        }
        ApertureInfo existing = apertures.get(owner);
        if (existing == null) {
            return;
        }
        long updatedTribulationTick = Math.max(0L, existing.nextTribulationTick() - elapsedTicks);
        ApertureInfo updated = new ApertureInfo(
            existing.center(),
            existing.currentRadius(),
            existing.timeSpeed(),
            updatedTribulationTick,
            existing.isFrozen(),
            existing.favorability(),
            existing.tier()
        );
        apertures.put(owner, updated);
        setDirty();
    }

    /**
     * 归一化层级值，确保层级至少为 1。
     *
     * @param tier 原始层级
     * @return 归一化后的合法层级
     */
    private static int normalizeTier(int tier) {
        return Math.max(DEFAULT_TIER, tier);
    }

    /**
     * 限制好感度区间，确保数值位于 [0, 100]。
     *
     * @param favorability 原始好感度
     * @return 限幅后的好感度
     */
    private static float clampFavorability(float favorability) {
        return Math.max(MIN_FAVORABILITY, Math.min(MAX_FAVORABILITY, favorability));
    }

    /**
     * 获取全部仙窍数据快照。
     *
     * @return 不可变快照（key 为 owner UUID，value 为仙窍信息）
     */
    public Map<UUID, ApertureInfo> getAllApertures() {
        return Collections.unmodifiableMap(new HashMap<>(apertures));
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt(KEY_NEXT_INDEX, nextIndex);

        ListTag list = new ListTag();
        for (Map.Entry<UUID, ApertureInfo> entry : apertures.entrySet()) {
            CompoundTag apertureTag = new CompoundTag();
            apertureTag.putUUID(KEY_OWNER, entry.getKey());
            apertureTag.put(KEY_INFO, entry.getValue().save());
            list.add(apertureTag);
        }
        tag.put(KEY_APERTURES, list);

        ListTag initializedList = new ListTag();
        for (UUID owner : initializedApertures) {
            CompoundTag initializedTag = new CompoundTag();
            initializedTag.putUUID(KEY_OWNER, owner);
            initializedList.add(initializedTag);
        }
        tag.put(KEY_INITIALIZED_APERTURES, initializedList);

        ListTag logoutList = new ListTag();
        for (Map.Entry<UUID, Long> entry : lastLogoutTimes.entrySet()) {
            CompoundTag logoutTag = new CompoundTag();
            logoutTag.putUUID(KEY_OWNER, entry.getKey());
            logoutTag.putLong(KEY_LAST_LOGOUT_TIME, entry.getValue());
            logoutList.add(logoutTag);
        }
        tag.put(KEY_LAST_LOGOUT_TIMES, logoutList);
        return tag;
    }

    private static ApertureWorldData load(CompoundTag tag) {
        ApertureWorldData data = new ApertureWorldData();
        data.nextIndex = Math.max(DEFAULT_NEXT_INDEX, tag.getInt(KEY_NEXT_INDEX));

        if (tag.contains(KEY_APERTURES, TAG_LIST)) {
            ListTag list = tag.getList(KEY_APERTURES, TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag apertureTag = list.getCompound(i);
                if (!apertureTag.hasUUID(KEY_OWNER) || !apertureTag.contains(KEY_INFO, TAG_COMPOUND)) {
                    continue;
                }
                UUID owner = apertureTag.getUUID(KEY_OWNER);
                CompoundTag infoTag = apertureTag.getCompound(KEY_INFO);
                data.apertures.put(owner, ApertureInfo.load(infoTag));
            }
        }

        if (tag.contains(KEY_INITIALIZED_APERTURES, TAG_LIST)) {
            ListTag initializedList = tag.getList(KEY_INITIALIZED_APERTURES, TAG_COMPOUND);
            for (int i = 0; i < initializedList.size(); i++) {
                CompoundTag initializedTag = initializedList.getCompound(i);
                if (!initializedTag.hasUUID(KEY_OWNER)) {
                    continue;
                }
                data.initializedApertures.add(initializedTag.getUUID(KEY_OWNER));
            }
        }

        if (tag.contains(KEY_LAST_LOGOUT_TIMES, TAG_LIST)) {
            ListTag logoutList = tag.getList(KEY_LAST_LOGOUT_TIMES, TAG_COMPOUND);
            for (int i = 0; i < logoutList.size(); i++) {
                CompoundTag logoutTag = logoutList.getCompound(i);
                if (!logoutTag.hasUUID(KEY_OWNER) || !logoutTag.contains(KEY_LAST_LOGOUT_TIME)) {
                    continue;
                }
                UUID owner = logoutTag.getUUID(KEY_OWNER);
                long logoutTick = logoutTag.getLong(KEY_LAST_LOGOUT_TIME);
                data.lastLogoutTimes.put(owner, logoutTick);
            }
        }
        return data;
    }

    /**
     * 创建 SavedData 工厂。
     *
     * @return SavedData Factory
     */
    public static SavedData.Factory<ApertureWorldData> factory() {
        return new SavedData.Factory<>(ApertureWorldData::new, (tag, provider) -> load(tag), null);
    }

    /**
     * 从指定维度读取或创建仙窍世界数据。
     *
     * @param level 服务器维度实例
     * @return 仙窍世界数据
     */
    public static ApertureWorldData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    /**
     * 单个仙窍记录。
     */
    public record ApertureInfo(
        BlockPos center,
        int currentRadius,
        float timeSpeed,
        long nextTribulationTick,
        boolean isFrozen,
        float favorability,
        int tier
    ) {

        private static final String KEY_CENTER_X = "centerX";

        private static final String KEY_CENTER_Y = "centerY";

        private static final String KEY_CENTER_Z = "centerZ";

        private static final String KEY_CURRENT_RADIUS = "currentRadius";

        private static final String KEY_TIME_SPEED = "timeSpeed";

        private static final String KEY_NEXT_TRIBULATION_TICK = "nextTribulationTick";

        private static final String KEY_IS_FROZEN = "isFrozen";

        private static final String KEY_FAVORABILITY = "favorability";

        private static final String KEY_TIER = "tier";

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putInt(KEY_CENTER_X, center.getX());
            tag.putInt(KEY_CENTER_Y, center.getY());
            tag.putInt(KEY_CENTER_Z, center.getZ());
            tag.putInt(KEY_CURRENT_RADIUS, currentRadius);
            tag.putFloat(KEY_TIME_SPEED, timeSpeed);
            tag.putLong(KEY_NEXT_TRIBULATION_TICK, nextTribulationTick);
            tag.putBoolean(KEY_IS_FROZEN, isFrozen);
            tag.putFloat(KEY_FAVORABILITY, favorability);
            tag.putInt(KEY_TIER, tier);
            return tag;
        }

        public static ApertureInfo load(CompoundTag tag) {
            BlockPos centerPos = new BlockPos(
                tag.getInt(KEY_CENTER_X),
                tag.getInt(KEY_CENTER_Y),
                tag.getInt(KEY_CENTER_Z)
            );
            int radius = tag.getInt(KEY_CURRENT_RADIUS);
            float speed = tag.getFloat(KEY_TIME_SPEED);
            long tribulationTick = tag.getLong(KEY_NEXT_TRIBULATION_TICK);
            boolean frozen = tag.getBoolean(KEY_IS_FROZEN);

            // 兼容旧存档：缺少新字段时回退到默认值。
            float storedFavorability = tag.contains(KEY_FAVORABILITY)
                ? tag.getFloat(KEY_FAVORABILITY)
                : DEFAULT_FAVORABILITY;
            float normalizedFavorability = clampFavorability(storedFavorability);

            int storedTier = tag.contains(KEY_TIER) ? tag.getInt(KEY_TIER) : DEFAULT_TIER;
            int normalizedTier = normalizeTier(storedTier);

            return new ApertureInfo(
                centerPos,
                radius,
                speed,
                tribulationTick,
                frozen,
                normalizedFavorability,
                normalizedTier
            );
        }
    }
}
