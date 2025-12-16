package com.Kizunad.guzhenrenext.kongqiao.attachment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * 玩家空窍 UI 调整面板配置。
 * <p>
 * 该配置用于“玩家偏好”，不等同于被动效果的实时激活状态：
 * <ul>
 *   <li>被动开关：决定某个被动用途是否允许生效（关闭时应强制失活）。</li>
 *   <li>轮盘技能：决定哪些主动技能会出现在轮盘菜单中（仅保存列表与顺序）。</li>
 * </ul>
 * </p>
 */
public class TweakConfig implements INBTSerializable<CompoundTag> {

    /**
     * 默认轮盘容量（与 UI 设计一致；后续可按需求调整）。
     */
    public static final int DEFAULT_MAX_WHEEL_SKILLS = 8;

    private static final String TAG_DISABLED_PASSIVES = "DisabledPassives";
    private static final String TAG_WHEEL_SKILLS = "WheelSkills";

    private final Set<String> disabledPassives = new HashSet<>();
    private final List<String> wheelSkills = new ArrayList<>();

    public TweakConfig() {}

    /**
     * 判定某个被动用途是否允许生效。
     *
     * @param usageId 用途 ID
     * @return true 表示允许生效
     */
    public boolean isPassiveEnabled(final String usageId) {
        if (usageId == null || usageId.isBlank()) {
            return false;
        }
        return !disabledPassives.contains(usageId);
    }

    /**
     * 设置某个被动用途是否允许生效。
     *
     * @param usageId 用途 ID
     * @param enabled true 表示开启；false 表示关闭
     */
    public void setPassiveEnabled(final String usageId, final boolean enabled) {
        if (usageId == null || usageId.isBlank()) {
            return;
        }
        if (enabled) {
            disabledPassives.remove(usageId);
        } else {
            disabledPassives.add(usageId);
        }
    }

    /**
     * 获取被动禁用列表（副本）。
     *
     * @return 禁用的被动 usageId 集合
     */
    public Set<String> getDisabledPassives() {
        return new HashSet<>(disabledPassives);
    }

    /**
     * 判定技能是否已加入轮盘。
     *
     * @param usageId 用途 ID
     * @return true 表示已加入
     */
    public boolean isInWheel(final String usageId) {
        if (usageId == null || usageId.isBlank()) {
            return false;
        }
        return wheelSkills.contains(usageId);
    }

    /**
     * 添加技能到轮盘（保持顺序且去重）。
     *
     * @param usageId 用途 ID
     * @param maxSize 最大容量
     * @return true 表示成功加入；false 表示已存在或容量已满
     */
    public boolean addWheelSkill(final String usageId, final int maxSize) {
        if (usageId == null || usageId.isBlank()) {
            return false;
        }
        if (wheelSkills.contains(usageId)) {
            return false;
        }
        final int limit = Math.max(0, maxSize);
        if (limit > 0 && wheelSkills.size() >= limit) {
            return false;
        }
        wheelSkills.add(usageId);
        return true;
    }

    /**
     * 移出轮盘技能。
     *
     * @param usageId 用途 ID
     * @return true 表示成功移除
     */
    public boolean removeWheelSkill(final String usageId) {
        if (usageId == null || usageId.isBlank()) {
            return false;
        }
        return wheelSkills.remove(usageId);
    }

    /**
     * 获取轮盘技能列表（副本）。
     *
     * @return 轮盘技能 usageId 列表
     */
    public List<String> getWheelSkills() {
        return new ArrayList<>(wheelSkills);
    }

    public void setWheelSkills(final List<String> skills) {
        wheelSkills.clear();
        if (skills == null || skills.isEmpty()) {
            return;
        }
        for (String id : skills) {
            if (id == null || id.isBlank() || wheelSkills.contains(id)) {
                continue;
            }
            wheelSkills.add(id);
        }
    }

    public void setDisabledPassives(final Set<String> ids) {
        disabledPassives.clear();
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (String id : ids) {
            if (id == null || id.isBlank()) {
                continue;
            }
            disabledPassives.add(id);
        }
    }

    @Override
    public CompoundTag serializeNBT(net.minecraft.core.HolderLookup.Provider provider) {
        final CompoundTag tag = new CompoundTag();

        final ListTag disabled = new ListTag();
        for (String id : disabledPassives) {
            disabled.add(StringTag.valueOf(id));
        }
        tag.put(TAG_DISABLED_PASSIVES, disabled);

        final ListTag wheel = new ListTag();
        for (String id : wheelSkills) {
            wheel.add(StringTag.valueOf(id));
        }
        tag.put(TAG_WHEEL_SKILLS, wheel);
        return tag;
    }

    @Override
    public void deserializeNBT(
        net.minecraft.core.HolderLookup.Provider provider,
        final CompoundTag nbt
    ) {
        disabledPassives.clear();
        wheelSkills.clear();
        if (nbt == null) {
            return;
        }

        if (nbt.contains(TAG_DISABLED_PASSIVES)) {
            final ListTag list = nbt.getList(
                TAG_DISABLED_PASSIVES,
                Tag.TAG_STRING
            );
            for (Tag t : list) {
                final String id = t.getAsString();
                if (id != null && !id.isBlank()) {
                    disabledPassives.add(id);
                }
            }
        }

        if (nbt.contains(TAG_WHEEL_SKILLS)) {
            final ListTag list = nbt.getList(TAG_WHEEL_SKILLS, Tag.TAG_STRING);
            for (Tag t : list) {
                final String id = t.getAsString();
                if (id == null || id.isBlank() || wheelSkills.contains(id)) {
                    continue;
                }
                wheelSkills.add(id);
            }
        }
    }
}

