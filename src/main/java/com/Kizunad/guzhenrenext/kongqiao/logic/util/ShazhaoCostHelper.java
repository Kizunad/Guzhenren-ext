package com.Kizunad.guzhenrenext.kongqiao.logic.util;

import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/**
 * 杀招资源消耗工具。
 * <p>
 * 约束：
 * <ul>
 *   <li>真元消耗必须走 {@link ZhenYuanHelper#calculateGuCost(LivingEntity, double)}。</li>
 *   <li>念头/精力/魂魄等其他资源按 {@link ZhuanCostHelper} 折算（与真元同一分母），保证转数分层。</li>
 * </ul>
 * </p>
 */
public final class ShazhaoCostHelper {

    private ShazhaoCostHelper() {}

    /**
     * 尝试扣除一次性消耗（主动杀招）。
     *
     * @return true 表示扣除成功；false 表示任意资源不足
     */
    public static boolean tryConsumeOnce(
        final ServerPlayer player,
        final LivingEntity user,
        final ShazhaoData data
    ) {
        if (user == null || data == null) {
            return false;
        }

        final double niantouBaseCost = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(
                data,
                GuEffectCostHelper.META_NIANTOU_COST,
                0.0
            )
        );
        final double niantouCost = ZhuanCostHelper.scaleCost(user, niantouBaseCost);
        if (niantouCost > 0.0 && NianTouHelper.getAmount(user) < niantouCost) {
            sendMessage(player, "念头不足。");
            return false;
        }

        final double jingliBaseCost = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(
                data,
                GuEffectCostHelper.META_JINGLI_COST,
                0.0
            )
        );
        final double jingliCost = ZhuanCostHelper.scaleCost(user, jingliBaseCost);
        if (jingliCost > 0.0 && JingLiHelper.getAmount(user) < jingliCost) {
            sendMessage(player, "精力不足。");
            return false;
        }

        final double hunpoBaseCost = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(
                data,
                GuEffectCostHelper.META_HUNPO_COST,
                0.0
            )
        );
        final double hunpoCost = ZhuanCostHelper.scaleCost(user, hunpoBaseCost);
        if (hunpoCost > 0.0 && HunPoHelper.getAmount(user) < hunpoCost) {
            sendMessage(player, "魂魄不足。");
            return false;
        }

        final double zhenyuanBaseCost = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(
                data,
                GuEffectCostHelper.META_ZHENYUAN_BASE_COST,
                0.0
            )
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
     * 尝试扣除每秒维持消耗（被动杀招）。
     * <p>
     * 被动杀招每秒触发，失败时通常直接失效并撤销属性修饰，避免刷屏提示，因此这里不发送提示消息。
     * </p>
     */
    public static boolean tryConsumeSustain(final LivingEntity user, final ShazhaoData data) {
        if (user == null || data == null) {
            return false;
        }
        final double niantouCostPerSecond = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(
                data,
                GuEffectCostHelper.META_NIANTOU_COST_PER_SECOND,
                0.0
            )
        );
        final double jingliCostPerSecond = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(
                data,
                GuEffectCostHelper.META_JINGLI_COST_PER_SECOND,
                0.0
            )
        );
        final double hunpoCostPerSecond = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(
                data,
                GuEffectCostHelper.META_HUNPO_COST_PER_SECOND,
                0.0
            )
        );
        final double zhenyuanBaseCostPerSecond = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(
                data,
                GuEffectCostHelper.META_ZHENYUAN_BASE_COST_PER_SECOND,
                0.0
            )
        );
        return GuEffectCostHelper.tryConsumeSustain(
            user,
            niantouCostPerSecond,
            jingliCostPerSecond,
            hunpoCostPerSecond,
            zhenyuanBaseCostPerSecond
        );
    }

    private static void sendMessage(final ServerPlayer player, final String message) {
        if (player == null || message == null || message.isBlank()) {
            return;
        }
        player.displayClientMessage(Component.literal(message), true);
    }
}

