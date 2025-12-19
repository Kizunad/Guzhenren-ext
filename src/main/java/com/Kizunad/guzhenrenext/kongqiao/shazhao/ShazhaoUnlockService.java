package com.Kizunad.guzhenrenext.kongqiao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.attachment.NianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouDataManager;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;

/**
 * 杀招解锁与推演概率计算工具。
 */
public final class ShazhaoUnlockService {

    private static final Logger LOGGER = LogUtils.getLogger();
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
            LOGGER.info("杀招推演候选筛选失败：unlocks 为空");
            return candidates;
        }
        Collection<ShazhaoData> allData = ShazhaoDataManager.getAll();
        LOGGER.info("开始筛选杀招候选 | total={}", allData.size());
        if (allData.isEmpty()) {
            LOGGER.info("杀招推演候选为空：当前未加载任何杀招数据");
            return candidates;
        }
        for (ShazhaoData data : allData) {
            if (data == null || data.shazhaoID() == null) {
                LOGGER.info("杀招推演候选跳过：数据为空或 shazhaoID 为空");
                continue;
            }
            String shazhaoIdText = data.shazhaoID();
            ResourceLocation shazhaoId;
            try {
                shazhaoId = ResourceLocation.parse(shazhaoIdText);
            } catch (Exception e) {
                LOGGER.info(
                    "杀招推演候选跳过：shazhaoID 非法 | shazhaoID={}",
                    shazhaoIdText
                );
                continue;
            }
            if (unlocks.isShazhaoUnlocked(shazhaoId)) {
                LOGGER.info(
                    "杀招推演候选跳过：已解锁 | shazhaoID={}",
                    shazhaoId
                );
                continue;
            }

            double chance = calculateUnlockChance(unlocks, data);
            if (chance <= 0D) {
                LOGGER.info(
                    "杀招推演候选跳过：推演概率为 0 | shazhaoID={}",
                    shazhaoId
                );
                continue;
            }
            candidates.add(new UnlockCandidate(data, chance));
        }
        LOGGER.info("杀招候选筛选完成 | candidates={}", candidates.size());
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
        if (unlocks == null) {
            LOGGER.info("杀招推演判定失败：unlocks 为空");
            return 0D;
        }
        if (data == null) {
            LOGGER.info("杀招推演判定失败：data 为空");
            return 0D;
        }
        String shazhaoId = data.shazhaoID();
        if (shazhaoId == null || shazhaoId.isBlank()) {
            LOGGER.info("杀招推演判定失败：shazhaoID 为空");
            return 0D;
        }
        List<String> requiredItems = data.requiredItems();
        if (requiredItems == null || requiredItems.isEmpty()) {
            LOGGER.info("杀招推演判定失败：required_items 为空 | shazhaoID={}", shazhaoId);
            return 0D;
        }

        int totalUsages = 0;
        int unlockedUsages = 0;
        for (String itemId : requiredItems) {
            if (itemId == null || itemId.isBlank()) {
                LOGGER.info(
                    "杀招推演判定失败：required_items 含空值 | shazhaoID={}",
                    shazhaoId
                );
                return 0D;
            }
            ResourceLocation id;
            try {
                id = ResourceLocation.parse(itemId);
            } catch (Exception e) {
                LOGGER.info(
                    "杀招推演判定失败：required_items 非法 | shazhaoID={} itemId={}",
                    shazhaoId,
                    itemId
                );
                return 0D;
            }
            if (!unlocks.isUnlocked(id)) {
                LOGGER.info(
                    "杀招推演判定失败：所需蛊虫未解锁 | shazhaoID={} itemId={}",
                    shazhaoId,
                    itemId
                );
                return 0D;
            }
            Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(Items.AIR);
            if (item == Items.AIR) {
                LOGGER.info(
                    "杀招推演判定失败：所需蛊虫不存在 | shazhaoID={} itemId={}",
                    shazhaoId,
                    itemId
                );
                return 0D;
            }
            NianTouData itemData = NianTouDataManager.getData(item);
            if (itemData == null || itemData.usages() == null) {
                LOGGER.info(
                    "杀招推演判定失败：所需蛊虫缺少念头数据 | shazhaoID={} itemId={}",
                    shazhaoId,
                    itemId
                );
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
            LOGGER.info(
                "杀招推演判定失败：解锁用途为 0 | shazhaoID={} totalUsages={} unlockedUsages={}",
                shazhaoId,
                totalUsages,
                unlockedUsages
            );
            return 0D;
        }
        double ratio = (double) unlockedUsages / (double) totalUsages;
        double chance = MIN_CHANCE + ratio * (MAX_CHANCE - MIN_CHANCE);
        return Math.min(MAX_CHANCE, chance);
    }
}
