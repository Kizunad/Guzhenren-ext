package com.Kizunad.guzhenrenext.kongqiao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.attachment.NianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouDataManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * 杀招解锁与推演概率计算工具。
 */
public final class ShazhaoUnlockService {

    private static final double MIN_CHANCE = 0.1D;
    private static final double MAX_CHANCE = 0.9D;

    private ShazhaoUnlockService() {}

    public record UnlockCandidate(ShazhaoData data, double chance) {}

    /**
     * 根据当前念头解锁情况，筛选出可尝试推演的杀招。
     * <p>
     * 规则：
     * - 所需蛊虫必须全部至少解锁过一条用途；
     * - 解锁进度为 0 时，该杀招不可推演；
     * - 概率线性映射到 10% ~ 90%。
     * </p>
     */
    public static List<UnlockCandidate> listUnlockCandidates(
        final NianTouUnlocks unlocks
    ) {
        final List<UnlockCandidate> candidates = new ArrayList<>();
        if (unlocks == null) {
            return candidates;
        }
        for (ShazhaoData data : ShazhaoDataManager.getAll()) {
            if (data == null || data.shazhaoID() == null) {
                continue;
            }
            ResourceLocation shazhaoId;
            try {
                shazhaoId = ResourceLocation.parse(data.shazhaoID());
            } catch (Exception e) {
                continue;
            }
            if (unlocks.isShazhaoUnlocked(shazhaoId)) {
                continue;
            }

            double chance = calculateUnlockChance(unlocks, data);
            if (chance <= 0D) {
                continue;
            }
            candidates.add(new UnlockCandidate(data, chance));
        }
        return candidates;
    }

    public static boolean tryUnlockRandom(
        final RandomSource random,
        final NianTouUnlocks unlocks
    ) {
        if (random == null || unlocks == null) {
            return false;
        }
        List<UnlockCandidate> candidates = listUnlockCandidates(unlocks);
        if (candidates.isEmpty()) {
            return false;
        }
        UnlockCandidate candidate =
            candidates.get(random.nextInt(candidates.size()));
        if (random.nextDouble() <= candidate.chance()) {
            unlocks.unlockShazhao(
                ResourceLocation.parse(candidate.data().shazhaoID())
            );
            return true;
        }
        return false;
    }

    /**
     * 计算单个杀招的推演成功率。
     * <p>
     * 线性区间为 10%~90%，若所有所需蛊虫的用途解锁总数为 0，则返回 0。
     * </p>
     */
    public static double calculateUnlockChance(
        final NianTouUnlocks unlocks,
        final ShazhaoData data
    ) {
        if (unlocks == null || data == null) {
            return 0D;
        }
        List<String> requiredItems = data.requiredItems();
        if (requiredItems == null || requiredItems.isEmpty()) {
            return 0D;
        }

        int totalUsages = 0;
        int unlockedUsages = 0;
        for (String itemId : requiredItems) {
            if (itemId == null || itemId.isBlank()) {
                return 0D;
            }
            ResourceLocation id;
            try {
                id = ResourceLocation.parse(itemId);
            } catch (Exception e) {
                return 0D;
            }
            if (!unlocks.isUnlocked(id)) {
                return 0D;
            }
            Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(Items.AIR);
            if (item == Items.AIR) {
                return 0D;
            }
            NianTouData itemData = NianTouDataManager.getData(item);
            if (itemData == null || itemData.usages() == null) {
                return 0D;
            }
            for (NianTouData.Usage usage : itemData.usages()) {
                if (usage == null || usage.usageID() == null) {
                    continue;
                }
                totalUsages++;
                if (unlocks.isUsageUnlocked(id, usage.usageID())) {
                    unlockedUsages++;
                }
            }
        }

        if (totalUsages <= 0 || unlockedUsages <= 0) {
            return 0D;
        }
        double ratio = (double) unlockedUsages / (double) totalUsages;
        double chance = MIN_CHANCE + ratio * (MAX_CHANCE - MIN_CHANCE);
        return Math.min(MAX_CHANCE, chance);
    }
}
