package com.Kizunad.guzhenrenext.xianqiao.daomark;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * 挂载在 LevelChunk 上的道痕附件数据。
 * <p>
 * 按 sectionY 索引存储每个 Section 的 {@link DaoSectionData}，
 * 实现 {@link INBTSerializable} 以支持自动持久化。
 * </p>
 */
public class DaoChunkAttachment implements INBTSerializable<CompoundTag> {

    /** NBT 中 sections 的 key 前缀。 */
    private static final String TAG_SECTION_PREFIX = "s_";

    /** 按 sectionY 索引存储各 Section 的道痕数据。 */
    private final Map<Integer, DaoSectionData> sections = new HashMap<>();

    /**
     * 获取指定 sectionY 的道痕数据；如果不存在则创建并存入。
     *
     * @param sectionY section 的 Y 轴索引
     * @return 对应的道痕数据（非 null）
     */
    public DaoSectionData getOrCreate(int sectionY) {
        return sections.computeIfAbsent(sectionY, k -> new DaoSectionData());
    }

    /**
     * 获取指定 sectionY 的道痕数据；如果不存在则返回 null。
     *
     * @param sectionY section 的 Y 轴索引
     * @return 对应的道痕数据，或 null
     */
    public DaoSectionData get(int sectionY) {
        return sections.get(sectionY);
    }

    /**
     * 获取所有已有数据的 sections 映射（只读视图用于遍历）。
     *
     * @return sectionY → DaoSectionData 映射
     */
    public Map<Integer, DaoSectionData> getSections() {
        return sections;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag root = new CompoundTag();
        for (Map.Entry<Integer, DaoSectionData> entry : sections.entrySet()) {
            CompoundTag sectionTag = new CompoundTag();
            entry.getValue().save(sectionTag);
            root.put(TAG_SECTION_PREFIX + entry.getKey(), sectionTag);
        }
        return root;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        sections.clear();
        for (String key : tag.getAllKeys()) {
            if (key.startsWith(TAG_SECTION_PREFIX)) {
                String indexStr = key.substring(TAG_SECTION_PREFIX.length());
                try {
                    int sectionY = Integer.parseInt(indexStr);
                    DaoSectionData data = DaoSectionData.load(tag.getCompound(key));
                    sections.put(sectionY, data);
                } catch (NumberFormatException ignored) {
                    // 跳过格式异常的 key
                }
            }
        }
    }
}
