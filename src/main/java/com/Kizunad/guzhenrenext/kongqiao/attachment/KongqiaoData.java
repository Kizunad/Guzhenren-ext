package com.Kizunad.guzhenrenext.kongqiao.attachment;

import com.Kizunad.guzhenrenext.kongqiao.KongqiaoOwner;
import com.Kizunad.guzhenrenext.kongqiao.inventory.AttackInventory;
import com.Kizunad.guzhenrenext.kongqiao.inventory.GuchongFeedInventory;
import com.Kizunad.guzhenrenext.kongqiao.inventory.KongqiaoInventory;
import com.Kizunad.guzhenrenext.kongqiao.service.KongqiaoService;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class KongqiaoData
    implements KongqiaoOwner, INBTSerializable<CompoundTag> {

    private static final String TAG_GAMEPLAY_ACTIVATED = "gameplayActivated";

    private final KongqiaoInventory kongqiaoInventory;
    private final AttackInventory attackInventory;
    private final GuchongFeedInventory feedInventory;
    // given：附件里只保存会跨重登延续的稳定态原始真相
    // when：客户端需要展示总压力、上限与分解条目
    // then：这些投影值必须走同步层下发，不能由客户端从附件本地反推
    private final StabilityState stabilityState;
    // given：玩家进入世界时会自动补齐空窍附件，保证底层承载始终存在
    // when：玩法层需要明确区分“只有技术附件”与“已经真正开窍可用”
    // then：这里必须额外保存显式玩法激活态，不能再把附件存在误判成玩法已开放
    private boolean gameplayActivated;
    private UUID ownerId = Util.NIL_UUID;
    private boolean clientSide;
    private boolean dirty;

    public KongqiaoData() {
        this(
            KongqiaoService.createInventory(),
            new AttackInventory(),
            new GuchongFeedInventory()
        );
    }

    KongqiaoData(
        final KongqiaoInventory kongqiaoInventory,
        final AttackInventory attackInventory,
        final GuchongFeedInventory feedInventory
    ) {
        this.kongqiaoInventory = kongqiaoInventory;
        this.attackInventory = attackInventory;
        this.feedInventory = feedInventory;
        this.stabilityState = new StabilityState(() -> dirty = true);
        if (this.kongqiaoInventory != null) {
            this.kongqiaoInventory.setChangeListener(() -> dirty = true);
        }
        if (this.attackInventory != null) {
            this.attackInventory.setChangeListener(() -> dirty = true);
        }
        if (this.feedInventory != null) {
            this.feedInventory.setChangeListener(() -> dirty = true);
        }
    }

    // given：普通生产路径仍然必须走真实空窍容器与标签校验
    // when：纯 JVM 测试只想验证稳定态持久化与同步契约
    // then：这里提供不触发注册表/标签初始化的最小构造缝，不改变生产行为
    static KongqiaoData createBootstrapSafeForTests() {
        return new KongqiaoData(null, null, null);
    }

    public void bind(Entity entity) {
        if (entity != null) {
            this.ownerId = entity.getUUID();
            this.clientSide = entity.level().isClientSide();
        } else {
            this.ownerId = Util.NIL_UUID;
            this.clientSide = false;
        }
    }

    @Override
    public KongqiaoInventory getKongqiaoInventory() {
        return kongqiaoInventory;
    }

    @Override
    public AttackInventory getAttackInventory() {
        return attackInventory;
    }

    @Override
    public GuchongFeedInventory getFeedInventory() {
        return feedInventory;
    }

    public StabilityState getStabilityState() {
        return stabilityState;
    }

    public boolean isGameplayActivated() {
        return gameplayActivated;
    }

    public void setGameplayActivated(final boolean gameplayActivated) {
        if (this.gameplayActivated != gameplayActivated) {
            this.gameplayActivated = gameplayActivated;
            dirty = true;
        }
    }

    @Override
    public void markKongqiaoDirty() {
        dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void clearDirty() {
        dirty = false;
    }

    @Override
    public UUID getKongqiaoId() {
        return ownerId;
    }

    @Override
    public boolean isClientSide() {
        return clientSide;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        if (kongqiaoInventory != null) {
            tag.put("kongqiao", kongqiaoInventory.serializeNBT(provider));
        }
        if (attackInventory != null) {
            tag.put("attack", attackInventory.serializeNBT(provider));
        }
        if (feedInventory != null) {
            tag.put("feed", feedInventory.serializeNBT(provider));
        }
        tag.putBoolean(TAG_GAMEPLAY_ACTIVATED, gameplayActivated);
        tag.put("stability", stabilityState.serializeNBT(provider));
        return tag;
    }

    @Override
    public void deserializeNBT(
        HolderLookup.Provider provider,
        CompoundTag tag
    ) {
        if (kongqiaoInventory != null && tag.contains("kongqiao")) {
            kongqiaoInventory.deserializeNBT(
                provider,
                tag.getCompound("kongqiao")
            );
        }
        if (attackInventory != null && tag.contains("attack")) {
            attackInventory.deserializeNBT(provider, tag.getCompound("attack"));
        }
        if (feedInventory != null && tag.contains("feed")) {
            feedInventory.deserializeNBT(provider, tag.getCompound("feed"));
        }
        gameplayActivated = tag.contains(TAG_GAMEPLAY_ACTIVATED)
            && tag.getBoolean(TAG_GAMEPLAY_ACTIVATED);
        if (tag.contains("stability", Tag.TAG_COMPOUND)) {
            stabilityState.deserializeNBT(provider, tag.getCompound("stability"));
        } else {
            stabilityState.deserializeNBT(provider, null);
        }
    }

    public static final class StabilityState
        implements INBTSerializable<CompoundTag> {

        private static final String TAG_BURST_PRESSURE = "burstPressure";
        private static final String TAG_FATIGUE_DEBT = "fatigueDebt";
        private static final String TAG_OVERLOAD_TIER = "overloadTier";
        private static final String TAG_FORCED_DISABLED_USAGE_IDS =
            "forcedDisabledUsageIds";
        private static final String TAG_SEALED_SLOTS = "sealedSlots";
        private static final String TAG_LAST_DECAY_GAME_TIME = "lastDecayGameTime";

        private final Runnable dirtyCallback;
        private double burstPressure;
        private double fatigueDebt;
        private int overloadTier;
        private LinkedHashSet<String> forcedDisabledUsageIds = new LinkedHashSet<>();
        private LinkedHashSet<Integer> sealedSlots = new LinkedHashSet<>();
        private long lastDecayGameTime;

        private StabilityState(final Runnable dirtyCallback) {
            this.dirtyCallback = dirtyCallback;
        }

        public double getBurstPressure() {
            return burstPressure;
        }

        public void setBurstPressure(final double burstPressure) {
            final double normalized = normalizeNonNegativeDouble(burstPressure);
            if (Double.compare(this.burstPressure, normalized) != 0) {
                this.burstPressure = normalized;
                markDirty();
            }
        }

        public double getFatigueDebt() {
            return fatigueDebt;
        }

        public void setFatigueDebt(final double fatigueDebt) {
            final double normalized = normalizeNonNegativeDouble(fatigueDebt);
            if (Double.compare(this.fatigueDebt, normalized) != 0) {
                this.fatigueDebt = normalized;
                markDirty();
            }
        }

        public int getOverloadTier() {
            return overloadTier;
        }

        public void setOverloadTier(final int overloadTier) {
            final int normalized = normalizeNonNegativeInt(overloadTier);
            if (this.overloadTier != normalized) {
                this.overloadTier = normalized;
                markDirty();
            }
        }

        public Set<String> getForcedDisabledUsageIds() {
            return Collections.unmodifiableSet(forcedDisabledUsageIds);
        }

        public void setForcedDisabledUsageIds(final Set<String> usageIds) {
            final LinkedHashSet<String> normalized = new LinkedHashSet<>();
            if (usageIds != null) {
                for (String usageId : usageIds) {
                    if (usageId == null || usageId.isBlank()) {
                        continue;
                    }
                    normalized.add(usageId);
                }
            }
            if (!forcedDisabledUsageIds.equals(normalized)) {
                forcedDisabledUsageIds = normalized;
                markDirty();
            }
        }

        public Set<Integer> getSealedSlots() {
            return Collections.unmodifiableSet(sealedSlots);
        }

        public void setSealedSlots(final Set<Integer> sealedSlots) {
            final LinkedHashSet<Integer> normalized = new LinkedHashSet<>();
            if (sealedSlots != null) {
                for (Integer sealedSlot : sealedSlots) {
                    if (sealedSlot == null || sealedSlot < 0) {
                        continue;
                    }
                    normalized.add(sealedSlot);
                }
            }
            if (!this.sealedSlots.equals(normalized)) {
                this.sealedSlots = normalized;
                markDirty();
            }
        }

        public long getLastDecayGameTime() {
            return lastDecayGameTime;
        }

        public void setLastDecayGameTime(final long lastDecayGameTime) {
            final long normalized = normalizeNonNegativeLong(lastDecayGameTime);
            if (this.lastDecayGameTime != normalized) {
                this.lastDecayGameTime = normalized;
                markDirty();
            }
        }

        @Override
        public CompoundTag serializeNBT(final HolderLookup.Provider provider) {
            final CompoundTag tag = new CompoundTag();
            final ListTag forcedDisabledList = new ListTag();
            for (String usageId : forcedDisabledUsageIds) {
                forcedDisabledList.add(StringTag.valueOf(usageId));
            }
            tag.putDouble(TAG_BURST_PRESSURE, burstPressure);
            tag.putDouble(TAG_FATIGUE_DEBT, fatigueDebt);
            tag.putInt(TAG_OVERLOAD_TIER, overloadTier);
            tag.put(TAG_FORCED_DISABLED_USAGE_IDS, forcedDisabledList);
            tag.putIntArray(
                TAG_SEALED_SLOTS,
                sealedSlots.stream().mapToInt(Integer::intValue).toArray()
            );
            tag.putLong(TAG_LAST_DECAY_GAME_TIME, lastDecayGameTime);
            return tag;
        }

        @Override
        public void deserializeNBT(
            final HolderLookup.Provider provider,
            final CompoundTag tag
        ) {
            if (tag == null) {
                burstPressure = 0.0D;
                fatigueDebt = 0.0D;
                overloadTier = 0;
                forcedDisabledUsageIds = new LinkedHashSet<>();
                sealedSlots = new LinkedHashSet<>();
                lastDecayGameTime = 0L;
                return;
            }
            burstPressure = tag.contains(TAG_BURST_PRESSURE)
                ? normalizeNonNegativeDouble(tag.getDouble(TAG_BURST_PRESSURE))
                : 0.0D;
            fatigueDebt = tag.contains(TAG_FATIGUE_DEBT)
                ? normalizeNonNegativeDouble(tag.getDouble(TAG_FATIGUE_DEBT))
                : 0.0D;
            overloadTier = tag.contains(TAG_OVERLOAD_TIER)
                ? normalizeNonNegativeInt(tag.getInt(TAG_OVERLOAD_TIER))
                : 0;
            forcedDisabledUsageIds = new LinkedHashSet<>();
            if (tag.contains(TAG_FORCED_DISABLED_USAGE_IDS, Tag.TAG_LIST)) {
                final ListTag forcedDisabledList = tag.getList(
                    TAG_FORCED_DISABLED_USAGE_IDS,
                    Tag.TAG_STRING
                );
                for (Tag entry : forcedDisabledList) {
                    if (entry instanceof StringTag stringTag) {
                        final String usageId = stringTag.getAsString();
                        if (!usageId.isBlank()) {
                            forcedDisabledUsageIds.add(usageId);
                        }
                    }
                }
            }
            sealedSlots = new LinkedHashSet<>();
            for (int sealedSlot : tag.getIntArray(TAG_SEALED_SLOTS)) {
                if (sealedSlot >= 0) {
                    sealedSlots.add(sealedSlot);
                }
            }
            lastDecayGameTime = tag.contains(TAG_LAST_DECAY_GAME_TIME)
                ? normalizeNonNegativeLong(tag.getLong(TAG_LAST_DECAY_GAME_TIME))
                : 0L;
        }

        private void markDirty() {
            if (dirtyCallback != null) {
                dirtyCallback.run();
            }
        }

        private static double normalizeNonNegativeDouble(final double value) {
            return value < 0.0D ? 0.0D : value;
        }

        private static int normalizeNonNegativeInt(final int value) {
            return value < 0 ? 0 : value;
        }

        private static long normalizeNonNegativeLong(final long value) {
            return value < 0L ? 0L : value;
        }
    }
}
