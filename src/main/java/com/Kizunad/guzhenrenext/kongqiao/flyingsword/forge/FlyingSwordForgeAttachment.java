package com.Kizunad.guzhenrenext.kongqiao.flyingsword.forge;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * 飞剑培养附件（玩家随身）。
 * <p>
 * 用于持久化"飞剑培养工程"的进度，支持死亡后继承（由 AttachmentType.copyOnDeath 控制）。
 * </p>
 * <p>
 * 约束：
 * <ul>
 *     <li>一次只能培养 1 把飞剑（active=true 时不能开启新工程）。</li>
 *     <li>核心剑放入时计入 fedSwordCount=1。</li>
 *     <li>材料剑必须与核心剑 itemId 相同。</li>
 *     <li>蛊虫材料按 z1~z5 和道 tag 写入 daoMarks。</li>
 * </ul>
 * </p>
 */
public class FlyingSwordForgeAttachment implements INBTSerializable<CompoundTag> {

    private static final String TAG_ACTIVE = "Active";
    private static final String TAG_BASE_SWORD_ITEM = "BaseSwordItem";
    private static final String TAG_BASE_SWORD_ITEM_ID = "BaseSwordItemId";
    private static final String TAG_REQUIRED_SWORD_COUNT = "RequiredSwordCount";
    private static final String TAG_FED_SWORD_COUNT = "FedSwordCount";
    private static final String TAG_DAO_MARKS = "DaoMarks";
    private static final String TAG_LAST_MESSAGE = "LastMessage";

    /**
     * 默认需要投喂的剑数量（包含核心剑）。
     */
    public static final int DEFAULT_REQUIRED_SWORD_COUNT = 64;

    /**
     * 是否有正在进行的培养工程。
     */
    private boolean active = false;

    /**
     * 核心剑的完整 ItemStack NBT（用于成品渲染/附魔发光/命名）。
     */
    private CompoundTag baseSwordItem = new CompoundTag();

    /**
     * 核心剑的物品注册 ID（用于严格判定投喂剑材质）。
     */
    private String baseSwordItemId = "";

    /**
     * 目标剑数量（包含核心剑）。
     */
    private int requiredSwordCount = DEFAULT_REQUIRED_SWORD_COUNT;

    /**
     * 已累计剑数量（核心剑放入时直接 +1）。
     */
    private int fedSwordCount = 0;

    /**
     * 各道点数：key = DaoType.key（如 "yandao"、"jiandao"），value = 累计点数。
     */
    private final Map<String, Integer> daoMarks = new HashMap<>();

    /**
     * 最近一次操作提示（用于 UI 显示）。
     */
    private String lastMessage = "";

    // ===== Getter/Setter =====

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public CompoundTag getBaseSwordItem() {
        return baseSwordItem;
    }

    public void setBaseSwordItem(CompoundTag baseSwordItem) {
        this.baseSwordItem = baseSwordItem != null ? baseSwordItem : new CompoundTag();
    }

    public String getBaseSwordItemId() {
        return baseSwordItemId;
    }

    public void setBaseSwordItemId(String baseSwordItemId) {
        this.baseSwordItemId = baseSwordItemId != null ? baseSwordItemId : "";
    }

    public int getRequiredSwordCount() {
        return requiredSwordCount;
    }

    public void setRequiredSwordCount(int requiredSwordCount) {
        this.requiredSwordCount = Math.max(1, requiredSwordCount);
    }

    public int getFedSwordCount() {
        return fedSwordCount;
    }

    public void setFedSwordCount(int fedSwordCount) {
        this.fedSwordCount = Math.max(0, fedSwordCount);
    }

    /**
     * 增加已投喂剑数量（自动 clamp 到 requiredSwordCount）。
     *
     * @param count 增加的数量
     * @return 实际增加的数量
     */
    public int addFedSwordCount(int count) {
        if (count <= 0) {
            return 0;
        }
        int before = fedSwordCount;
        fedSwordCount = Math.min(fedSwordCount + count, requiredSwordCount);
        return fedSwordCount - before;
    }

    public Map<String, Integer> getDaoMarks() {
        return daoMarks;
    }

    /**
     * 增加指定道的点数。
     *
     * @param daoKey 道类型 key（如 "yandao"）
     * @param points 增加的点数
     */
    public void addDaoMark(String daoKey, int points) {
        if (daoKey == null || daoKey.isBlank() || points <= 0) {
            return;
        }
        daoMarks.merge(daoKey, points, Integer::sum);
    }

    /**
     * 获取指定道的点数。
     */
    public int getDaoMark(String daoKey) {
        if (daoKey == null || daoKey.isBlank()) {
            return 0;
        }
        return daoMarks.getOrDefault(daoKey, 0);
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage != null ? lastMessage : "";
    }

    /**
     * 判断是否可以收取成品。
     */
    public boolean canClaim() {
        return active && fedSwordCount >= requiredSwordCount;
    }

    private static final int PERCENT_100 = 100;

    /**
     * 获取进度百分比（0~100）。
     */
    public int getProgressPercent() {
        if (requiredSwordCount <= 0) {
            return 0;
        }
        return Math.min(PERCENT_100, (fedSwordCount * PERCENT_100) / requiredSwordCount);
    }

    /**
     * 清空培养工程（收取成品或取消时调用）。
     */
    public void clear() {
        active = false;
        baseSwordItem = new CompoundTag();
        baseSwordItemId = "";
        requiredSwordCount = DEFAULT_REQUIRED_SWORD_COUNT;
        fedSwordCount = 0;
        daoMarks.clear();
        lastMessage = "";
    }

    // ===== 序列化 =====

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(TAG_ACTIVE, active);
        if (baseSwordItem != null && !baseSwordItem.isEmpty()) {
            tag.put(TAG_BASE_SWORD_ITEM, baseSwordItem.copy());
        }
        if (baseSwordItemId != null && !baseSwordItemId.isBlank()) {
            tag.putString(TAG_BASE_SWORD_ITEM_ID, baseSwordItemId);
        }
        tag.putInt(TAG_REQUIRED_SWORD_COUNT, requiredSwordCount);
        tag.putInt(TAG_FED_SWORD_COUNT, fedSwordCount);

        if (!daoMarks.isEmpty()) {
            CompoundTag daoTag = new CompoundTag();
            for (Map.Entry<String, Integer> entry : daoMarks.entrySet()) {
                if (entry.getKey() != null && !entry.getKey().isBlank()) {
                    daoTag.putInt(entry.getKey(), entry.getValue());
                }
            }
            tag.put(TAG_DAO_MARKS, daoTag);
        }

        if (lastMessage != null && !lastMessage.isBlank()) {
            tag.putString(TAG_LAST_MESSAGE, lastMessage);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        clear();
        if (tag == null) {
            return;
        }
        active = tag.getBoolean(TAG_ACTIVE);
        if (tag.contains(TAG_BASE_SWORD_ITEM, Tag.TAG_COMPOUND)) {
            baseSwordItem = tag.getCompound(TAG_BASE_SWORD_ITEM).copy();
        }
        if (tag.contains(TAG_BASE_SWORD_ITEM_ID, Tag.TAG_STRING)) {
            baseSwordItemId = tag.getString(TAG_BASE_SWORD_ITEM_ID);
        }
        if (tag.contains(TAG_REQUIRED_SWORD_COUNT, Tag.TAG_INT)) {
            requiredSwordCount = Math.max(1, tag.getInt(TAG_REQUIRED_SWORD_COUNT));
        }
        if (tag.contains(TAG_FED_SWORD_COUNT, Tag.TAG_INT)) {
            fedSwordCount = Math.max(0, tag.getInt(TAG_FED_SWORD_COUNT));
        }
        if (tag.contains(TAG_DAO_MARKS, Tag.TAG_COMPOUND)) {
            CompoundTag daoTag = tag.getCompound(TAG_DAO_MARKS);
            for (String key : daoTag.getAllKeys()) {
                if (daoTag.contains(key, Tag.TAG_INT)) {
                    daoMarks.put(key, daoTag.getInt(key));
                }
            }
        }
        if (tag.contains(TAG_LAST_MESSAGE, Tag.TAG_STRING)) {
            lastMessage = tag.getString(TAG_LAST_MESSAGE);
        }
    }

    /**
     * 创建一个空附件实例（用于 AttachmentType 默认值工厂）。
     */
    public static FlyingSwordForgeAttachment create() {
        return new FlyingSwordForgeAttachment();
    }

    /**
     * 复制附件（用于 copyOnDeath）。
     */
    @Nullable
    public static FlyingSwordForgeAttachment copy(
        FlyingSwordForgeAttachment source,
        HolderLookup.Provider provider
    ) {
        if (source == null) {
            return null;
        }
        FlyingSwordForgeAttachment copy = new FlyingSwordForgeAttachment();
        copy.deserializeNBT(provider, source.serializeNBT(provider));
        return copy;
    }
}
