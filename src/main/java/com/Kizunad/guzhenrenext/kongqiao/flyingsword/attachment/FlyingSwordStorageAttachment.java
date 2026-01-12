package com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.growth.SwordGrowthData;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.growth.SwordGrowthTuning;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.quality.SwordQuality;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * 飞剑存储附件。
 * <p>
 * 用途：持久化玩家“已召回”的飞剑数据，支持死亡后继承（由 AttachmentType.copyOnDeath 控制）。
 * </p>
 * <p>
 * 说明：
 * <ul>
 *     <li>本附件不依赖 ChestCavity，也不在此阶段绑定“召唤来源/器官联动”。</li>
 *     <li>仅保存恢复飞剑所需的最小信息（等级/经验/耐久/外观等）。</li>
 * </ul>
 * </p>
 */
public class FlyingSwordStorageAttachment
    implements INBTSerializable<CompoundTag> {

    private static final String TAG_RECALLED_SWORDS = "RecalledSwords";

    private final List<RecalledSword> recalledSwords = new ArrayList<>();

    /**
     * 默认最大存储数量（后续可由“剑窍蛊/流派/道痕”等扩展调整）。
     */
    public static final int DEFAULT_MAX_RECALLED = 10;

    public boolean recallSword(final RecalledSword sword) {
        return recallSword(sword, DEFAULT_MAX_RECALLED);
    }

    public boolean recallSword(
        final RecalledSword sword,
        final int maxCapacity
    ) {
        if (sword == null) {
            return false;
        }
        final int limit = maxCapacity <= 0 ? DEFAULT_MAX_RECALLED : maxCapacity;
        if (recalledSwords.size() >= limit) {
            return false;
        }
        recalledSwords.add(sword);
        return true;
    }

    public List<RecalledSword> getRecalledSwords() {
        return new ArrayList<>(recalledSwords);
    }

    public int getCount() {
        return recalledSwords.size();
    }

    /**
     * 通过索引获取存储项引用。
     * <p>
     * 注意：返回的是内部对象引用，调用方可读可写，但必须遵守：
     * <ul>
     *     <li>不要长期持有引用（可能被移除）。</li>
     *     <li>不要跨线程访问。</li>
     * </ul>
     * </p>
     */
    @Nullable
    public RecalledSword getAt(final int index) {
        if (index < 0 || index >= recalledSwords.size()) {
            return null;
        }
        return recalledSwords.get(index);
    }

    public void remove(final int index) {
        if (index < 0 || index >= recalledSwords.size()) {
            return;
        }
        recalledSwords.remove(index);
    }

    public void clear() {
        recalledSwords.clear();
    }

    @Override
    public CompoundTag serializeNBT(final HolderLookup.Provider provider) {
        final CompoundTag tag = new CompoundTag();
        final ListTag list = new ListTag();
        for (RecalledSword sword : recalledSwords) {
            if (sword == null) {
                continue;
            }
            list.add(sword.serializeNBT());
        }
        tag.put(TAG_RECALLED_SWORDS, list);
        return tag;
    }

    @Override
    public void deserializeNBT(
        final HolderLookup.Provider provider,
        final CompoundTag tag
    ) {
        recalledSwords.clear();
        if (tag == null) {
            return;
        }
        if (!tag.contains(TAG_RECALLED_SWORDS, Tag.TAG_LIST)) {
            return;
        }
        final ListTag list = tag.getList(TAG_RECALLED_SWORDS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            recalledSwords.add(RecalledSword.fromNBT(list.getCompound(i)));
        }
    }

    /**
     * “已召回”的飞剑快照。
     * <p>
     * 这一层仅负责数据形态，不负责把数据应用回实体（那属于后续飞剑实体/Spawner 阶段）。
     * </p>
     */
    public static class RecalledSword {

        private static final String TAG_ATTRIBUTES = "Attributes";
        private static final String TAG_GROWTH_DATA = "GrowthData";
        private static final String TAG_QUALITY = "Quality";
        private static final String TAG_LEVEL = "Level";
        private static final String TAG_EXPERIENCE = "Experience";
        private static final String TAG_TOTAL_EXPERIENCE = "TotalExperience";
        private static final String TAG_DURABILITY = "Durability";
        private static final String TAG_DISPLAY_ITEM = "DisplayItem";
        private static final String TAG_MODEL_KEY = "ModelKey";
        private static final String TAG_SOUND_PROFILE = "SoundProfile";
        private static final String TAG_SWORD_TYPE = "SwordType";
        private static final String TAG_DISPLAY_ITEM_UUID = "DisplayItemUUID";
        private static final String TAG_ITEM_WITHDRAWN = "ItemWithdrawn";

        /**
         * 飞剑属性快照（暂存 NBT，避免阶段2硬依赖 FlyingSwordAttributes 结构）。
         */
        public CompoundTag attributes = new CompoundTag();

        /**
         * 飞剑品质。
         */
        public SwordQuality quality = SwordQuality.COMMON;

        public int level = 1;
        public int experience = 0;

        /**
         * 累计经验（用于统计）。
         */
        public long totalExperience = 0;

        public float durability = SwordGrowthTuning.DEFAULT_RECALLED_DURABILITY;

        /**
         * 完整 ItemStack NBT（用于渲染/附魔发光/组件）。
         */
        public CompoundTag displayItem = new CompoundTag();

        public String modelKey = "";
        public String soundProfile = "";
        public String swordType = "";

        /**
         * 稳定物品 UUID（用于追踪“同一把剑”的外观/绑定），可能为空。
         */
        public String displayItemUUID = "";

        /**
         * 是否已从存储中取出“本体物品”。为 true 时应禁止恢复/召唤。
         */
        public boolean itemWithdrawn = false;

        /**
         * 从成长数据创建召回记录。
         *
         * @param growthData 成长数据
         * @param currentDurability 当前耐久
         * @return 召回记录
         */
        public static RecalledSword fromGrowthData(
            SwordGrowthData growthData,
            float currentDurability
        ) {
            RecalledSword sword = new RecalledSword();
            if (growthData != null) {
                sword.quality = growthData.getQuality();
                sword.level = growthData.getLevel();
                sword.experience = growthData.getExperience();
                sword.totalExperience = growthData.getTotalExperience();
            }
            sword.durability = currentDurability;
            return sword;
        }

        /**
         * 转换为成长数据。
         *
         * @return 成长数据
         */
        public SwordGrowthData toGrowthData() {
            SwordGrowthData data = SwordGrowthData.create(quality, level);
            data.setExperienceRaw(experience);
            return data;
        }

        public CompoundTag serializeNBT() {
            final CompoundTag tag = new CompoundTag();

            if (attributes != null && !attributes.isEmpty()) {
                tag.put(TAG_ATTRIBUTES, attributes.copy());
            }

            // 品质/等级/经验（新系统）
            tag.putString(TAG_QUALITY, quality.name());
            tag.putInt(TAG_LEVEL, Math.max(1, level));
            tag.putInt(TAG_EXPERIENCE, Math.max(0, experience));
            tag.putLong(TAG_TOTAL_EXPERIENCE, Math.max(0, totalExperience));
            tag.putFloat(TAG_DURABILITY, Math.max(0.0f, durability));

            if (displayItem != null && !displayItem.isEmpty()) {
                tag.put(TAG_DISPLAY_ITEM, displayItem.copy());
            }
            if (modelKey != null && !modelKey.isBlank()) {
                tag.putString(TAG_MODEL_KEY, modelKey);
            }
            if (soundProfile != null && !soundProfile.isBlank()) {
                tag.putString(TAG_SOUND_PROFILE, soundProfile);
            }
            if (swordType != null && !swordType.isBlank()) {
                tag.putString(TAG_SWORD_TYPE, swordType);
            }
            if (displayItemUUID != null && !displayItemUUID.isBlank()) {
                tag.putString(TAG_DISPLAY_ITEM_UUID, displayItemUUID);
            }
            tag.putBoolean(TAG_ITEM_WITHDRAWN, itemWithdrawn);
            return tag;
        }

        public static RecalledSword fromNBT(final CompoundTag tag) {
            final RecalledSword sword = new RecalledSword();
            if (tag == null) {
                return sword;
            }

            if (tag.contains(TAG_ATTRIBUTES, Tag.TAG_COMPOUND)) {
                sword.attributes = tag.getCompound(TAG_ATTRIBUTES).copy();
            }

            // 品质（新系统）
            if (tag.contains(TAG_QUALITY, Tag.TAG_STRING)) {
                sword.quality = SwordQuality.fromName(
                    tag.getString(TAG_QUALITY)
                );
            }

            sword.level = Math.max(1, tag.getInt(TAG_LEVEL));
            sword.experience = Math.max(0, tag.getInt(TAG_EXPERIENCE));
            sword.totalExperience = Math.max(
                0,
                tag.getLong(TAG_TOTAL_EXPERIENCE)
            );
            sword.durability = Math.max(0.0f, tag.getFloat(TAG_DURABILITY));

            if (tag.contains(TAG_DISPLAY_ITEM, Tag.TAG_COMPOUND)) {
                sword.displayItem = tag.getCompound(TAG_DISPLAY_ITEM).copy();
            }
            if (tag.contains(TAG_MODEL_KEY, Tag.TAG_STRING)) {
                sword.modelKey = tag.getString(TAG_MODEL_KEY);
            }
            if (tag.contains(TAG_SOUND_PROFILE, Tag.TAG_STRING)) {
                sword.soundProfile = tag.getString(TAG_SOUND_PROFILE);
            }
            if (tag.contains(TAG_SWORD_TYPE, Tag.TAG_STRING)) {
                sword.swordType = tag.getString(TAG_SWORD_TYPE);
            }
            if (tag.contains(TAG_DISPLAY_ITEM_UUID, Tag.TAG_STRING)) {
                sword.displayItemUUID = tag.getString(TAG_DISPLAY_ITEM_UUID);
            }
            sword.itemWithdrawn = tag.getBoolean(TAG_ITEM_WITHDRAWN);
            return sword;
        }
    }
}
