package com.Kizunad.guzhenrenext.kongqiao.flyingsword.client;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordController;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordConstants;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordEntity;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordStateAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ai.SwordAIMode;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.quality.SwordQuality;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.resonance.FlyingSwordResonanceType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/**
 * 飞剑 HUD 客户端状态管理器。
 * <p>
 * 职责：
 * <ul>
 *     <li>每 tick 扫描附近的飞剑实体</li>
 *     <li>缓存飞剑数据供 HUD 渲染使用</li>
 *     <li>追踪"选中"的飞剑（用于高亮显示）</li>
 * </ul>
 * </p>
 * <p>
 * 注意：此类只在客户端运行，所有数据来自 SynchedEntityData 同步。
 * </p>
 */
public final class FlyingSwordHudState {

    /** Alpha 通道掩码（不透明）。 */
    private static final int ALPHA_OPAQUE = 0xFF000000;

    /** 默认白色（ARGB）。 */
    private static final int COLOR_WHITE_ARGB = 0xFFFFFFFF;

    /** 最大显示飞剑数量。 */
    private static final int MAX_DISPLAY_COUNT = 8;

    /** 刷新间隔（ticks）。 */
    private static final int REFRESH_INTERVAL = 5;

    private static final float OVERLOAD_WARNING_THRESHOLD_PERCENT = 80.0F;

    private static final float OVERLOAD_DANGER_THRESHOLD_PERCENT = 100.0F;

    /** 缓存的飞剑数据列表。 */
    private static final List<SwordDisplayData> CACHED_SWORDS =
        new ArrayList<>();

    /** 当前选中的飞剑 UUID（可能为 null）。 */
    @Nullable
    private static UUID selectedSwordId = null;

    /** 距上次刷新的 tick 数。 */
    private static int ticksSinceRefresh = 0;

    /** HUD 是否启用。 */
    private static boolean hudEnabled = true;

    private FlyingSwordHudState() {}

    /**
     * 每 tick 调用，刷新飞剑数据缓存。
     */
    public static void tick() {
        ticksSinceRefresh++;

        if (ticksSinceRefresh < REFRESH_INTERVAL) {
            return;
        }

        ticksSinceRefresh = 0;
        refreshCache();
    }

    /**
     * 刷新飞剑数据缓存。
     */
    private static void refreshCache() {
        CACHED_SWORDS.clear();

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }

        UUID playerUuid = player.getUUID();

        // 扫描附近实体
        AABB searchBox = player
            .getBoundingBox()
            .inflate(FlyingSwordConstants.SEARCH_RANGE);

        List<FlyingSwordEntity> nearbySwords = new ArrayList<>();
        for (Entity entity : mc.level.getEntities(player, searchBox)) {
            if (!(entity instanceof FlyingSwordEntity sword)) {
                continue;
            }
            // 检查是否属于当前玩家
            UUID ownerUuid = sword.getOwnerUUID();
            if (ownerUuid != null && ownerUuid.equals(playerUuid)) {
                nearbySwords.add(sword);
            }
        }

        // 按距离排序
        nearbySwords.sort(
            Comparator.comparingDouble(sword -> sword.distanceToSqr(player))
        );

        final List<SwordDisplayData> preparedDisplayData = new ArrayList<>(
            nearbySwords.size()
        );
        for (FlyingSwordEntity sword : nearbySwords) {
            preparedDisplayData.add(createDisplayData(sword, player));
        }
        CACHED_SWORDS.addAll(buildVisibleDisplayWindow(preparedDisplayData));
    }

    /**
     * 从飞剑实体创建显示数据。
     */
    private static SwordDisplayData createDisplayData(
        FlyingSwordEntity sword,
        Player player
    ) {
        SwordDisplayData data = new SwordDisplayData();
        data.entityId = sword.getId();
        data.uuid = sword.getUUID();
        data.quality = sword.getQuality();
        data.level = sword.getSwordLevel();
        data.experience = sword.getSwordExperience();
        data.aiMode = sword.getAIModeEnum();
        data.distance = (float) Math.sqrt(sword.distanceToSqr(player));
        data.health = sword.getHealth();
        data.maxHealth = sword.getMaxHealth();
        data.isSelected = sword.getUUID().equals(selectedSwordId);
        final String stableSwordId = sword.getSwordAttributes() == null
            ? ""
            : normalizeSwordId(sword.getSwordAttributes().getStableSwordId());
        final FlyingSwordStateAttachment state = KongqiaoAttachments.getFlyingSwordState(player);
        final String bondedSwordId = state == null ? "" : state.getBondedSwordId();
        final String resonanceRaw = state == null ? "" : state.getResonanceType();
        final double overload = state == null ? 0.0D : state.getOverload();
        final long burstCooldownUntilTick = state == null ? 0L : state.getBurstCooldownUntilTick();
        final long overloadBacklashUntilTick = state == null
            ? 0L
            : state.getOverloadBacklashUntilTick();
        final long overloadRecoveryUntilTick = state == null
            ? 0L
            : state.getOverloadRecoveryUntilTick();
        final long burstActiveUntilTick = state == null ? 0L : state.getBurstActiveUntilTick();
        final long burstAftershockUntilTick = state == null
            ? 0L
            : state.getBurstAftershockUntilTick();
        projectBenmingPhase2Fields(
            data,
            new BenmingPhase2ProjectionInput(
                stableSwordId,
                bondedSwordId,
                resonanceRaw,
                overload,
                burstCooldownUntilTick,
                overloadBacklashUntilTick,
                overloadRecoveryUntilTick,
                burstActiveUntilTick,
                burstAftershockUntilTick,
                player.level().getGameTime()
            )
        );

        // 计算经验进度
        var attrs = sword.getSwordAttributes();
        if (attrs != null) {
            int expForNext = attrs.getExpForNextLevel();
            if (expForNext > 0) {
                data.expProgress = (float) data.experience / expForNext;
            } else {
                data.expProgress = 1.0f; // 满级
            }
        }

        return data;
    }

    /**
     * 投影本命二期 HUD 字段。
     * <p>
     * 该方法仅做客户端缓存字段转换，不承担业务判定权威。
     * 当同步面未提供明确权威状态时，使用保守默认值（例如余震期默认 false）。
     * </p>
     */
    static void projectBenmingPhase2Fields(
        SwordDisplayData data,
        BenmingPhase2ProjectionInput projectionInput
    ) {
        final long normalizedGameTick = Math.max(0L, projectionInput.gameTick());
        final String normalizedStableSwordId = normalizeSwordId(
            projectionInput.stableSwordId()
        );
        final String normalizedBondedSwordId = normalizeSwordId(
            projectionInput.bondedSwordId()
        );
        data.isBenmingSword =
            !normalizedBondedSwordId.isBlank() && normalizedBondedSwordId.equals(normalizedStableSwordId);

        data.benmingResonanceType = FlyingSwordResonanceType.resolve(
            projectionInput.resonanceRaw()
        ).orElse(null);
        data.overloadPercent = (float) Math.max(0.0D, projectionInput.overload());
        data.isOverloadDanger = data.overloadPercent >= OVERLOAD_DANGER_THRESHOLD_PERCENT;
        final long normalizedBacklashUntilTick = Math.max(
            0L,
            projectionInput.overloadBacklashUntilTick()
        );
        final long normalizedRecoveryUntilTick = Math.max(
            0L,
            projectionInput.overloadRecoveryUntilTick()
        );
        data.isOverloadBacklashActive =
            data.isBenmingSword && normalizedBacklashUntilTick > normalizedGameTick;
        data.isOverloadRecoveryActive =
            data.isBenmingSword
                && normalizedRecoveryUntilTick > normalizedBacklashUntilTick
                && normalizedGameTick >= normalizedBacklashUntilTick
                && normalizedGameTick < normalizedRecoveryUntilTick;
        data.isBurstReady = FlyingSwordController.isBurstWindowReady(
            normalizedStableSwordId,
            normalizedBondedSwordId,
            projectionInput.resonanceRaw(),
            projectionInput.overload(),
            projectionInput.burstCooldownUntilTick(),
            projectionInput.overloadBacklashUntilTick(),
            projectionInput.overloadRecoveryUntilTick(),
            projectionInput.gameTick()
        );
        final long normalizedActiveUntilTick = Math.max(
            0L,
            projectionInput.burstActiveUntilTick()
        );
        final long normalizedAftershockUntilTick = Math.max(
            0L,
            projectionInput.burstAftershockUntilTick()
        );
        data.isAftershockPeriod =
            data.isBenmingSword
                && normalizedGameTick >= normalizedActiveUntilTick
                && normalizedGameTick < normalizedAftershockUntilTick;

        data.shouldHighlightWarning = data.overloadPercent >= OVERLOAD_WARNING_THRESHOLD_PERCENT;
    }

    static List<SwordDisplayData> buildVisibleDisplayWindow(
        List<SwordDisplayData> distanceOrderedDisplayData
    ) {
        final int effectiveDisplayCount = Math.min(
            distanceOrderedDisplayData.size(),
            MAX_DISPLAY_COUNT
        );
        final List<SwordDisplayData> visibleDisplayData = new ArrayList<>(
            effectiveDisplayCount
        );
        if (effectiveDisplayCount <= 0) {
            return visibleDisplayData;
        }

        appendMatchingDisplayRows(
            visibleDisplayData,
            distanceOrderedDisplayData,
            true,
            effectiveDisplayCount
        );
        appendMatchingDisplayRows(
            visibleDisplayData,
            distanceOrderedDisplayData,
            false,
            effectiveDisplayCount
        );
        return visibleDisplayData;
    }

    private static void appendMatchingDisplayRows(
        List<SwordDisplayData> visibleDisplayData,
        List<SwordDisplayData> distanceOrderedDisplayData,
        boolean expectBenmingSword,
        int effectiveDisplayCount
    ) {
        for (SwordDisplayData displayData : distanceOrderedDisplayData) {
            if (visibleDisplayData.size() >= effectiveDisplayCount) {
                return;
            }
            if (displayData.isBenmingSword != expectBenmingSword) {
                continue;
            }
            visibleDisplayData.add(displayData);
        }
    }

    private record BenmingPhase2ProjectionInput(
        @Nullable String stableSwordId,
        @Nullable String bondedSwordId,
        @Nullable String resonanceRaw,
        double overload,
        long burstCooldownUntilTick,
        long overloadBacklashUntilTick,
        long overloadRecoveryUntilTick,
        long burstActiveUntilTick,
        long burstAftershockUntilTick,
        long gameTick
    ) {}

    private static String normalizeSwordId(@Nullable String swordId) {
        return swordId == null ? "" : swordId;
    }

    /**
     * 获取缓存的飞剑显示数据列表。
     * <p>
     * 返回的列表是只读副本。
     * </p>
     */
    public static List<SwordDisplayData> getCachedSwords() {
        return new ArrayList<>(CACHED_SWORDS);
    }

    /**
     * 获取飞剑数量。
     */
    public static int getSwordCount() {
        return CACHED_SWORDS.size();
    }

    /**
     * 检查是否有飞剑可显示。
     */
    public static boolean hasSwords() {
        return !CACHED_SWORDS.isEmpty();
    }

    /**
     * 设置选中的飞剑。
     */
    public static void setSelectedSword(@Nullable UUID swordId) {
        selectedSwordId = swordId;
        // 立即刷新以更新选中状态
        refreshCache();
    }

    /**
     * 获取选中的飞剑 UUID。
     */
    @Nullable
    public static UUID getSelectedSwordId() {
        return selectedSwordId;
    }

    /**
     * 清除选中状态。
     */
    public static void clearSelection() {
        selectedSwordId = null;
    }

    /**
     * 设置 HUD 是否启用。
     */
    public static void setHudEnabled(boolean enabled) {
        hudEnabled = enabled;
    }

    /**
     * 检查 HUD 是否启用。
     */
    public static boolean isHudEnabled() {
        return hudEnabled;
    }

    /**
     * 切换 HUD 显示状态。
     */
    public static void toggleHud() {
        hudEnabled = !hudEnabled;
    }

    /**
     * 清除所有缓存（用于断开连接等场景）。
     */
    public static void clearAll() {
        CACHED_SWORDS.clear();
        selectedSwordId = null;
        ticksSinceRefresh = 0;
    }

    /**
     * 飞剑显示数据。
     * <p>
     * 包含 HUD 渲染所需的所有信息。
     * </p>
     */
    public static class SwordDisplayData {

        /** 实体 ID（用于快速查找）。 */
        public int entityId;

        /** 实体 UUID。 */
        public UUID uuid;

        /** 品质。 */
        public SwordQuality quality = SwordQuality.COMMON;

        /** 等级。 */
        public int level = 1;

        /** 当前经验。 */
        public int experience = 0;

        /** 经验进度（0.0 ~ 1.0）。 */
        public float expProgress = 0.0f;

        /** AI 模式。 */
        public SwordAIMode aiMode = SwordAIMode.ORBIT;

        /** 距离玩家的距离（格）。 */
        public float distance = 0.0f;

        /** 当前生命值。 */
        public float health = 1.0f;

        /** 最大生命值。 */
        public float maxHealth = 1.0f;

        /** 是否被选中。 */
        public boolean isSelected = false;

        /** 是否为本命飞剑（可与 isSelected 同时为 true）。 */
        public boolean isBenmingSword = false;

        /** 本命二期分型（缺省态为 null）。 */
        @Nullable
        public FlyingSwordResonanceType benmingResonanceType = null;

        /** 本命过载百分比（非负，缺省为 0）。 */
        public float overloadPercent = 0.0F;

        /** 本命爆发是否可用（基于共享爆发窗口语义的客户端显示投影）。 */
        public boolean isBurstReady = true;

        /** 是否处于余震期（当前同步面未提供独立权威字段时默认 false）。 */
        public boolean isAftershockPeriod = false;

        public boolean isOverloadBacklashActive = false;

        public boolean isOverloadRecoveryActive = false;

        public boolean shouldHighlightWarning = false;

        public boolean isOverloadDanger = false;

        /**
         * 获取生命值百分比。
         */
        public float getHealthPercent() {
            if (maxHealth <= 0) {
                return 0.0f;
            }
            return Math.min(1.0f, health / maxHealth);
        }

        /**
         * 获取品质对应的颜色（ARGB）。
         */
        public int getQualityColor() {
            ChatFormatting color = quality.getColor();
            Integer colorInt = color.getColor();
            if (colorInt == null) {
                return COLOR_WHITE_ARGB;
            }
            // ChatFormatting 返回的是 RGB，需要添加 Alpha 通道
            return ALPHA_OPAQUE | colorInt;
        }

        /**
         * 获取显示名称（品质 + 等级）。
         */
        public String getDisplayName() {
            return quality.getDisplayName() + " Lv." + level;
        }

        /**
         * 获取 AI 模式显示名称。
         */
        public String getAIModeDisplayName() {
            return aiMode.getDisplayName();
        }
    }
}
