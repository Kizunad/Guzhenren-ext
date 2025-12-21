package com.Kizunad.guzhenrenext.kongqiao.logic.util;

import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/**
 * 蛊虫技能资源消耗工具。
 * <p>
 * 统一读取 metadata 中的标准字段，并执行“足够性检查 + 扣除”。当前支持：<br>
 * - 念头：niantou_cost / niantou_cost_per_second<br>
 * - 精力：jingli_cost / jingli_cost_per_second<br>
 * - 魂魄：hunpo_cost / hunpo_cost_per_second<br>
 * - 真元：zhenyuan_base_cost / zhenyuan_base_cost_per_second（需走真元算法换算）<br>
 * </p>
 */
public final class GuEffectCostHelper {

    public static final String META_NIANTOU_COST = "niantou_cost";
    public static final String META_JINGLI_COST = "jingli_cost";
    public static final String META_HUNPO_COST = "hunpo_cost";
    public static final String META_ZHENYUAN_BASE_COST = "zhenyuan_base_cost";

    public static final String META_NIANTOU_COST_PER_SECOND =
        "niantou_cost_per_second";
    public static final String META_JINGLI_COST_PER_SECOND =
        "jingli_cost_per_second";
    public static final String META_HUNPO_COST_PER_SECOND =
        "hunpo_cost_per_second";
    public static final String META_ZHENYUAN_BASE_COST_PER_SECOND =
        "zhenyuan_base_cost_per_second";

    private GuEffectCostHelper() {}

    /**
     * 尝试扣除一次性资源消耗（用于主动技能）。
     *
     * @return true 表示扣除成功；false 表示任意资源不足
     */
    public static boolean tryConsumeOnce(
        final ServerPlayer player,
        final LivingEntity user,
        final NianTouData.Usage usageInfo
    ) {
        if (user == null || usageInfo == null) {
            return false;
        }

        final double niantouBaseCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_NIANTOU_COST, 0.0)
        );
        final double niantouCost = ZhuanCostHelper.scaleCost(user, niantouBaseCost);
        if (niantouCost > 0.0 && NianTouHelper.getAmount(user) < niantouCost) {
            sendMessage(player, "念头不足。");
            return false;
        }

        final double jingliBaseCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_JINGLI_COST, 0.0)
        );
        final double jingliCost = ZhuanCostHelper.scaleCost(user, jingliBaseCost);
        if (jingliCost > 0.0 && JingLiHelper.getAmount(user) < jingliCost) {
            sendMessage(player, "精力不足。");
            return false;
        }

        final double hunpoBaseCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HUNPO_COST, 0.0)
        );
        final double hunpoCost = ZhuanCostHelper.scaleCost(user, hunpoBaseCost);
        if (hunpoCost > 0.0 && HunPoHelper.getAmount(user) < hunpoCost) {
            sendMessage(player, "魂魄不足。");
            return false;
        }

        final double zhenyuanBaseCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_ZHENYUAN_BASE_COST, 0.0)
        );
        final double zhenyuanCost = ZhenYuanHelper.calculateGuCost(
            user,
            zhenyuanBaseCost
        );
        if (zhenyuanCost > 0.0 && !ZhenYuanHelper.hasEnough(user, zhenyuanCost)) {
            sendMessage(player, "真元不足。");
            return false;
        }

        if (niantouCost > 0.0) {
            NianTouHelper.modify(user, -niantouCost);
        }
        if (jingliCost > 0.0) {
            JingLiHelper.modify(user, -jingliCost);
        }
        if (hunpoCost > 0.0) {
            HunPoHelper.modify(user, -hunpoCost);
        }
        if (zhenyuanCost > 0.0) {
            ZhenYuanHelper.modify(user, -zhenyuanCost);
        }

        return true;
    }

    /**
     * 检查一次性资源是否足够（不扣除，用于“先判定成功再扣费”的场景）。
     *
     * @return true 表示资源足够；false 表示任意资源不足
     */
    public static boolean hasEnoughOnce(
        final ServerPlayer player,
        final LivingEntity user,
        final NianTouData.Usage usageInfo
    ) {
        if (user == null || usageInfo == null) {
            return false;
        }

        final double niantouBaseCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_NIANTOU_COST, 0.0)
        );
        final double niantouCost = ZhuanCostHelper.scaleCost(user, niantouBaseCost);
        if (niantouCost > 0.0 && NianTouHelper.getAmount(user) < niantouCost) {
            sendMessage(player, "念头不足。");
            return false;
        }

        final double jingliBaseCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_JINGLI_COST, 0.0)
        );
        final double jingliCost = ZhuanCostHelper.scaleCost(user, jingliBaseCost);
        if (jingliCost > 0.0 && JingLiHelper.getAmount(user) < jingliCost) {
            sendMessage(player, "精力不足。");
            return false;
        }

        final double hunpoBaseCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HUNPO_COST, 0.0)
        );
        final double hunpoCost = ZhuanCostHelper.scaleCost(user, hunpoBaseCost);
        if (hunpoCost > 0.0 && HunPoHelper.getAmount(user) < hunpoCost) {
            sendMessage(player, "魂魄不足。");
            return false;
        }

        final double zhenyuanBaseCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_ZHENYUAN_BASE_COST, 0.0)
        );
        final double zhenyuanCost = ZhenYuanHelper.calculateGuCost(
            user,
            zhenyuanBaseCost
        );
        if (zhenyuanCost > 0.0 && !ZhenYuanHelper.hasEnough(user, zhenyuanCost)) {
            sendMessage(player, "真元不足。");
            return false;
        }

        return true;
    }

    /**
     * 检查额外消耗（仅精力/魂魄，不含念头/真元）。
     * <p>
     * 用于“通用模板已处理念头/真元，但某些流派额外要求精力/魂魄”的场景。</p>
     *
     * @return true 表示资源足够；false 表示任意资源不足
     */
    public static boolean hasEnoughJingliHunpo(
        final ServerPlayer player,
        final LivingEntity user,
        final NianTouData.Usage usageInfo
    ) {
        if (user == null || usageInfo == null) {
            return false;
        }

        final double jingliBaseCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_JINGLI_COST, 0.0)
        );
        final double jingliCost = ZhuanCostHelper.scaleCost(user, jingliBaseCost);
        if (jingliCost > 0.0 && JingLiHelper.getAmount(user) < jingliCost) {
            sendMessage(player, "精力不足。");
            return false;
        }

        final double hunpoBaseCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HUNPO_COST, 0.0)
        );
        final double hunpoCost = ZhuanCostHelper.scaleCost(user, hunpoBaseCost);
        if (hunpoCost > 0.0 && HunPoHelper.getAmount(user) < hunpoCost) {
            sendMessage(player, "魂魄不足。");
            return false;
        }

        return true;
    }

    /**
     * 扣除额外消耗（仅精力/魂魄，不含念头/真元）。
     */
    public static void consumeJingliHunpoIfPresent(
        final LivingEntity user,
        final NianTouData.Usage usageInfo
    ) {
        if (user == null || usageInfo == null) {
            return;
        }

        final double jingliBaseCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_JINGLI_COST, 0.0)
        );
        final double jingliCost = ZhuanCostHelper.scaleCost(user, jingliBaseCost);
        if (jingliCost > 0.0) {
            JingLiHelper.modify(user, -jingliCost);
        }

        final double hunpoBaseCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HUNPO_COST, 0.0)
        );
        final double hunpoCost = ZhuanCostHelper.scaleCost(user, hunpoBaseCost);
        if (hunpoCost > 0.0) {
            HunPoHelper.modify(user, -hunpoCost);
        }
    }

    /**
     * 检查是否能启动持续效果（不扣除，用于领域/持续技能的“启动门槛”）。
     */
    public static boolean hasEnoughForSustain(
        final ServerPlayer player,
        final LivingEntity user,
        final double niantouCostPerSecond,
        final double jingliCostPerSecond,
        final double hunpoCostPerSecond,
        final double zhenyuanBaseCostPerSecond
    ) {
        if (user == null) {
            return false;
        }

        final double scaledNianTouCostPerSecond = ZhuanCostHelper.scaleCost(
            user,
            Math.max(0.0, niantouCostPerSecond)
        );
        final double scaledJingLiCostPerSecond = ZhuanCostHelper.scaleCost(
            user,
            Math.max(0.0, jingliCostPerSecond)
        );
        final double scaledHunPoCostPerSecond = ZhuanCostHelper.scaleCost(
            user,
            Math.max(0.0, hunpoCostPerSecond)
        );

        if (
            scaledNianTouCostPerSecond > 0.0
                && NianTouHelper.getAmount(user) < scaledNianTouCostPerSecond
        ) {
            sendMessage(player, "念头不足。");
            return false;
        }
        if (
            scaledJingLiCostPerSecond > 0.0
                && JingLiHelper.getAmount(user) < scaledJingLiCostPerSecond
        ) {
            sendMessage(player, "精力不足。");
            return false;
        }
        if (
            scaledHunPoCostPerSecond > 0.0
                && HunPoHelper.getAmount(user) < scaledHunPoCostPerSecond
        ) {
            sendMessage(player, "魂魄不足。");
            return false;
        }

        final double zhenyuanCostPerSecond = ZhenYuanHelper.calculateGuCost(
            user,
            zhenyuanBaseCostPerSecond
        );
        if (
            zhenyuanCostPerSecond > 0.0
                && !ZhenYuanHelper.hasEnough(user, zhenyuanCostPerSecond)
        ) {
            sendMessage(player, "真元不足。");
            return false;
        }

        return true;
    }

    /**
     * 执行持续效果的资源维持扣除。
     *
     * @return true 表示扣除成功；false 表示任意资源不足（不会部分扣除）
     */
    public static boolean tryConsumeSustain(
        final LivingEntity user,
        final double niantouCostPerSecond,
        final double jingliCostPerSecond,
        final double hunpoCostPerSecond,
        final double zhenyuanBaseCostPerSecond
    ) {
        if (user == null) {
            return false;
        }

        final double scaledNianTouCostPerSecond = ZhuanCostHelper.scaleCost(
            user,
            Math.max(0.0, niantouCostPerSecond)
        );
        final double scaledJingLiCostPerSecond = ZhuanCostHelper.scaleCost(
            user,
            Math.max(0.0, jingliCostPerSecond)
        );
        final double scaledHunPoCostPerSecond = ZhuanCostHelper.scaleCost(
            user,
            Math.max(0.0, hunpoCostPerSecond)
        );

        if (
            scaledNianTouCostPerSecond > 0.0
                && NianTouHelper.getAmount(user) < scaledNianTouCostPerSecond
        ) {
            return false;
        }
        if (
            scaledJingLiCostPerSecond > 0.0
                && JingLiHelper.getAmount(user) < scaledJingLiCostPerSecond
        ) {
            return false;
        }
        if (
            scaledHunPoCostPerSecond > 0.0
                && HunPoHelper.getAmount(user) < scaledHunPoCostPerSecond
        ) {
            return false;
        }

        final double zhenyuanCostPerSecond = ZhenYuanHelper.calculateGuCost(
            user,
            zhenyuanBaseCostPerSecond
        );
        if (
            zhenyuanCostPerSecond > 0.0
                && !ZhenYuanHelper.hasEnough(user, zhenyuanCostPerSecond)
        ) {
            return false;
        }

        if (scaledNianTouCostPerSecond > 0.0) {
            NianTouHelper.modify(user, -scaledNianTouCostPerSecond);
        }
        if (scaledJingLiCostPerSecond > 0.0) {
            JingLiHelper.modify(user, -scaledJingLiCostPerSecond);
        }
        if (scaledHunPoCostPerSecond > 0.0) {
            HunPoHelper.modify(user, -scaledHunPoCostPerSecond);
        }
        if (zhenyuanCostPerSecond > 0.0) {
            ZhenYuanHelper.modify(user, -zhenyuanCostPerSecond);
        }

        return true;
    }

    private static void sendMessage(
        final ServerPlayer player,
        final String message
    ) {
        if (player == null || message == null || message.isBlank()) {
            return;
        }
        player.displayClientMessage(Component.literal(message), true);
    }
}
