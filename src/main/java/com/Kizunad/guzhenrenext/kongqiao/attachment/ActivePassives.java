package com.Kizunad.guzhenrenext.kongqiao.attachment;

import com.Kizunad.guzhenrenext.kongqiao.KongqiaoLifecycleStateContract;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * 存储实体当前激活的用途 ID 列表。
 * <p>
 * 该类是
 * {@link KongqiaoLifecycleStateContract.StateCategory#RUNTIME_ACTIVE_STATE}
 * 的唯一运行时快照，用于快速查询，避免每次事件都扫描背包。
 * </p>
 * <p>
 * 它只记录“此刻已经由运行时逻辑判定为激活”的结果：
 * <ul>
 *   <li>不补写解锁状态；</li>
 *   <li>不补写玩家偏好；</li>
 *   <li>不承担玩法激活真相；</li>
 *   <li>不承担压力/失稳/封槽等稳定态真相。</li>
 * </ul>
 * </p>
 */
public class ActivePassives implements INBTSerializable<CompoundTag> {

    private final Set<String> activeUsageIds = new HashSet<>();

    public ActivePassives() {}

    public void add(String usageId) {
        activeUsageIds.add(usageId);
    }

    public void remove(String usageId) {
        activeUsageIds.remove(usageId);
    }

    public boolean isActive(String usageId) {
        return activeUsageIds.contains(usageId);
    }

    public void clear() {
        activeUsageIds.clear();
    }

    @Override
    public CompoundTag serializeNBT(
        net.minecraft.core.HolderLookup.Provider provider
    ) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (String id : activeUsageIds) {
            list.add(StringTag.valueOf(id));
        }
        tag.put("ActiveIds", list);
        return tag;
    }

    @Override
    public void deserializeNBT(
        net.minecraft.core.HolderLookup.Provider provider,
        CompoundTag nbt
    ) {
        activeUsageIds.clear();
        if (nbt.contains("ActiveIds")) {
            ListTag list = nbt.getList("ActiveIds", Tag.TAG_STRING);
            for (Tag t : list) {
                activeUsageIds.add(t.getAsString());
            }
        }
    }
}
