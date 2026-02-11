package com.Kizunad.guzhenrenext.xianqiao.data;

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

    private static final String KEY_OWNER = "owner";

    private static final String KEY_INFO = "info";

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

    private int nextIndex = DEFAULT_NEXT_INDEX;

    private final Map<UUID, ApertureInfo> apertures = new HashMap<>();

    private final Set<UUID> initializedApertures = new HashSet<>();

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
            false
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
        boolean isFrozen
    ) {

        private static final String KEY_CENTER_X = "centerX";

        private static final String KEY_CENTER_Y = "centerY";

        private static final String KEY_CENTER_Z = "centerZ";

        private static final String KEY_CURRENT_RADIUS = "currentRadius";

        private static final String KEY_TIME_SPEED = "timeSpeed";

        private static final String KEY_NEXT_TRIBULATION_TICK = "nextTribulationTick";

        private static final String KEY_IS_FROZEN = "isFrozen";

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putInt(KEY_CENTER_X, center.getX());
            tag.putInt(KEY_CENTER_Y, center.getY());
            tag.putInt(KEY_CENTER_Z, center.getZ());
            tag.putInt(KEY_CURRENT_RADIUS, currentRadius);
            tag.putFloat(KEY_TIME_SPEED, timeSpeed);
            tag.putLong(KEY_NEXT_TRIBULATION_TICK, nextTribulationTick);
            tag.putBoolean(KEY_IS_FROZEN, isFrozen);
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
            return new ApertureInfo(centerPos, radius, speed, tribulationTick, frozen);
        }
    }
}
