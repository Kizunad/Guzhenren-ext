package com.Kizunad.guzhenrenext.kongqiao.flyingsword;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ai.SwordAIMode;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordSelectionAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordStorageAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.effects.FlyingSwordEffects;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/**
 * 飞剑控制接口（最小版）。
 * <p>
 * Phase 2 目标：
 * <ul>
 *     <li>支持查找/选择/切换模式/召回/从存储恢复。</li>
 *     <li>后续再加入护幕、领域、复杂事件钩子等。</li>
 * </ul>
 * </p>
 */
public final class FlyingSwordController {

    private FlyingSwordController() {}

    public static List<FlyingSwordEntity> getPlayerSwords(
        ServerLevel level,
        Player owner
    ) {
        if (level == null || owner == null) {
            return List.of();
        }

        List<FlyingSwordEntity> swords = new ArrayList<>();
        AABB searchBox = owner
            .getBoundingBox()
            .inflate(FlyingSwordConstants.SEARCH_RANGE);

        for (Entity entity : level.getEntities(null, searchBox)) {
            if (!(entity instanceof FlyingSwordEntity sword)) {
                continue;
            }
            if (sword.isOwnedBy(owner)) {
                swords.add(sword);
            }
        }

        return swords;
    }

    /**
     * 循环切换 AI 模式（不包含 RECALL）。
     */
    public static SwordAIMode cycleAIMode(FlyingSwordEntity sword) {
        if (sword == null) {
            return SwordAIMode.ORBIT;
        }
        SwordAIMode current = sword.getAIModeEnum();
        SwordAIMode next = current.cycleNext();
        sword.setAIMode(next);

        // 播放模式切换特效
        FlyingSwordEffects.playModeSwitchEffect(sword);

        return next;
    }

    public static void recall(FlyingSwordEntity sword) {
        if (sword == null || sword.isRemoved()) {
            return;
        }
        // Phase 2：一律允许召回；后续再区分"不可召回的主动技能飞剑"。
        sword.setAIMode(SwordAIMode.RECALL);

        // 播放召回特效
        LivingEntity owner = sword.getOwner();
        if (owner instanceof Player player) {
            FlyingSwordEffects.playRecallEffect(sword, player);
        }
    }

    public static int recallAll(ServerLevel level, Player owner) {
        List<FlyingSwordEntity> swords = getPlayerSwords(level, owner);
        for (FlyingSwordEntity sword : swords) {
            recall(sword);
        }
        return swords.size();
    }

    /**
     * 在 RECALL 模式接近主人时调用：写入存储并销毁实体。
     */
    public static void finishRecall(FlyingSwordEntity sword, Player owner) {
        if (sword == null || owner == null || sword.isRemoved()) {
            return;
        }

        FlyingSwordStorageAttachment storage =
            KongqiaoAttachments.getFlyingSwordStorage(owner);
        if (storage == null) {
            sword.discard();
            return;
        }

        // 如果这把飞剑曾经被选中，召回后应清理选中状态。
        if (owner instanceof ServerPlayer player) {
            FlyingSwordSelectionAttachment selection =
                KongqiaoAttachments.getFlyingSwordSelection(player);
            if (selection != null) {
                Optional<UUID> selected = selection.getSelectedSword();
                if (
                    selected.isPresent() &&
                    selected.get().equals(sword.getUUID())
                ) {
                    selection.clear();
                }
            }
        }

        FlyingSwordStorageAttachment.RecalledSword recalled =
            new FlyingSwordStorageAttachment.RecalledSword();
        // Phase 2：暂存最小字段；属性等后续对齐 calculator。
        recalled.level = 1;
        recalled.experience = 0;
        recalled.durability = 1.0f;

        try {
            recalled.displayItem = (net.minecraft.nbt.CompoundTag) sword
                .getDisplayItemStack()
                .save(sword.registryAccess());
        } catch (Exception ignored) {}
        try {
            recalled.attributes = sword.writeAttributesToTag();
        } catch (Exception ignored) {}

        boolean ok = storage.recallSword(recalled);
        if (owner instanceof ServerPlayer player) {
            player.sendSystemMessage(
                Component.literal(
                    ok ? "[飞剑] 召回成功" : "[飞剑] 召回失败：存储已满"
                )
            );
        }
        sword.discard();
    }

    /**
     * 获取最近的一把飞剑。
     */
    @Nullable
    public static FlyingSwordEntity getNearestSword(
        ServerLevel level,
        Player owner
    ) {
        List<FlyingSwordEntity> swords = getPlayerSwords(level, owner);
        if (swords.isEmpty()) {
            return null;
        }
        FlyingSwordEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (FlyingSwordEntity sword : swords) {
            double d = sword.distanceToSqr(owner);
            if (d < best) {
                best = d;
                nearest = sword;
            }
        }
        return nearest;
    }

    /**
     * 选择最近的一把飞剑。
     * <p>
     * 选择结果会写入 {@link com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordSelectionAttachment}。
     * </p>
     */
    public static boolean selectNearest(ServerLevel level, ServerPlayer owner) {
        if (level == null || owner == null) {
            return false;
        }
        FlyingSwordSelectionAttachment selection =
            KongqiaoAttachments.getFlyingSwordSelection(owner);
        if (selection == null) {
            return false;
        }

        FlyingSwordEntity nearest = getNearestSword(level, owner);
        if (nearest == null) {
            selection.clear();
            owner.sendSystemMessage(Component.literal("[飞剑] 附近没有飞剑"));
            return false;
        }

        selection.setSelectedSword(nearest.getUUID());
        owner.sendSystemMessage(Component.literal("[飞剑] 已选中最近飞剑"));
        return true;
    }

    /**
     * 获取“选中”的飞剑；如果选中无效则回退到最近飞剑。
     * <p>
     * 注意：选择系统目前只存 UUID，因此需要从 {@link ServerLevel} 查回实体。
     * </p>
     */
    @Nullable
    public static FlyingSwordEntity getSelectedOrNearestSword(
        ServerLevel level,
        ServerPlayer owner
    ) {
        if (level == null || owner == null) {
            return null;
        }

        FlyingSwordSelectionAttachment selection =
            KongqiaoAttachments.getFlyingSwordSelection(owner);
        if (selection != null) {
            Optional<UUID> selected = selection.getSelectedSword();
            if (selected.isPresent()) {
                Entity entity = level.getEntity(selected.get());
                if (
                    entity instanceof FlyingSwordEntity sword &&
                    sword.isOwnedBy(owner)
                ) {
                    return sword;
                }
                selection.clear();
            }
        }

        FlyingSwordEntity nearest = getNearestSword(level, owner);
        if (nearest != null && selection != null) {
            selection.setSelectedSword(nearest.getUUID());
        }
        return nearest;
    }

    /**
     * 从存储中恢复一把飞剑（优先恢复最前面的可用项）。
     */
    public static int restoreOne(ServerLevel level, ServerPlayer owner) {
        if (level == null || owner == null) {
            return 0;
        }

        final FlyingSwordStorageAttachment storage =
            KongqiaoAttachments.getFlyingSwordStorage(owner);
        if (storage == null || storage.getCount() <= 0) {
            owner.sendSystemMessage(Component.literal("[飞剑] 存储中没有飞剑"));
            return 0;
        }

        for (int i = 0; i < storage.getCount(); i++) {
            final FlyingSwordStorageAttachment.RecalledSword rec =
                storage.getAt(i);
            if (rec == null) {
                storage.remove(i);
                i--;
                continue;
            }
            if (rec.itemWithdrawn) {
                storage.remove(i);
                i--;
                continue;
            }
            final FlyingSwordEntity sword =
                FlyingSwordSpawner.restoreFromStorage(level, owner, rec);
            if (sword != null) {
                storage.remove(i);
                FlyingSwordSelectionAttachment selection =
                    KongqiaoAttachments.getFlyingSwordSelection(owner);
                if (selection != null) {
                    selection.setSelectedSword(sword.getUUID());
                }
                owner.sendSystemMessage(
                    Component.literal("[飞剑] 已恢复一把飞剑")
                );
                return 1;
            }
        }

        owner.sendSystemMessage(Component.literal("[飞剑] 没有可恢复的飞剑"));
        return 0;
    }

    /**
     * 从存储中恢复所有可用飞剑。
     */
    public static int restoreAll(ServerLevel level, ServerPlayer owner) {
        if (level == null || owner == null) {
            return 0;
        }

        final FlyingSwordStorageAttachment storage =
            KongqiaoAttachments.getFlyingSwordStorage(owner);
        if (storage == null || storage.getCount() <= 0) {
            owner.sendSystemMessage(Component.literal("[飞剑] 存储中没有飞剑"));
            return 0;
        }

        int restored = 0;
        // 注意：循环中会 remove，所以这里用 i-- 的方式保持索引一致。
        for (int i = 0; i < storage.getCount(); i++) {
            final FlyingSwordStorageAttachment.RecalledSword rec =
                storage.getAt(i);
            if (rec == null) {
                storage.remove(i);
                i--;
                continue;
            }
            if (rec.itemWithdrawn) {
                storage.remove(i);
                i--;
                continue;
            }

            final FlyingSwordEntity sword =
                FlyingSwordSpawner.restoreFromStorage(level, owner, rec);
            if (sword != null) {
                restored++;
                storage.remove(i);
                i--;
            }
        }

        owner.sendSystemMessage(
            Component.literal("[飞剑] 已恢复飞剑: " + restored)
        );
        return restored;
    }
}
