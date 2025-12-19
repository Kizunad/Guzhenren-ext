package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.tierTwo;

import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.NianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.tierTwo.DaDaHuiGuWisdomDeductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.network.PacketSyncNianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoUnlockService;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 二转大慧蛊：主动【慧推】。
 * <p>
 * 设计目标：允许在轮盘中直接触发一次“杀招推演”，并在 NianTou UI 的结果区域给出提示。
 * 若被动【慧悟】处于激活状态，将获得推演成功率加成与失败念头消耗折扣（均由 metadata 控制）。
 * </p>
 */
public class DaDaHuiGuShazhaoDeriveEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:dadahuigu_active_shazhao_derive";

    private static final String NBT_COOLDOWN_UNTIL_TICK =
        "DaDaHuiGuShazhaoDeriveCooldownUntilTick";

    private static final int TICKS_PER_SECOND = 20;
    private static final int DEFAULT_COOLDOWN_TICKS = 400;

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public boolean onActivate(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user.level().isClientSide()) {
            return false;
        }
        if (!(user instanceof ServerPlayer player)) {
            return false;
        }

        final NianTouUnlocks unlocks = KongqiaoAttachments.getUnlocks(player);
        if (unlocks == null) {
            return false;
        }

        final int currentTick = player.tickCount;
        final int cooldownUntil = player.getPersistentData()
            .getInt(NBT_COOLDOWN_UNTIL_TICK);
        if (cooldownUntil > currentTick) {
            final int remainTicks = cooldownUntil - currentTick;
            final String msg =
                "慧推冷却中，剩余 " + (remainTicks / TICKS_PER_SECOND) + " 秒";
            setMessageAndSync(player, unlocks, msg);
            return true;
        }

        final DeriveBonus bonus = resolveBonus(player);
        final List<ShazhaoUnlockService.UnlockCandidate> candidates =
            ShazhaoUnlockService.listUnlockCandidates(unlocks);
        if (candidates.isEmpty()) {
            setMessageAndSync(player, unlocks, "无可解锁杀招");
            applyCooldown(player, usageInfo, currentTick);
            return true;
        }

        final ShazhaoUnlockService.UnlockCandidate candidate =
            candidates.get(player.getRandom().nextInt(candidates.size()));
        final ShazhaoData data = candidate.data();
        if (data == null) {
            setMessageAndSync(player, unlocks, "推演失败：杀招数据为空");
            applyCooldown(player, usageInfo, currentTick);
            return true;
        }

        final double baseChance = candidate.chance();
        final double chance = clampDouble(
            baseChance + bonus.chanceBonus(),
            0.0,
            0.9
        );

        final double roll = player.getRandom().nextDouble();
        final boolean success = roll <= chance;
        if (success) {
            unlocks.unlockShazhao(ResourceLocation.parse(data.shazhaoID()));
            final String info = data.getFormattedInfo();
            String message = "推演成功：" + data.title();
            if (info != null && !info.isBlank()) {
                message = message + " | " + info;
            }
            setMessageAndSync(player, unlocks, message);
            applyCooldown(player, usageInfo, currentTick);
            return true;
        }

        final int baseCost = Math.max(0, data.costTotalNiantou());
        final int finalCost = applyFailCostDiscount(baseCost, bonus);
        final double currentNianTou = NianTouHelper.getAmount(player);
        if (finalCost > 0 && currentNianTou < finalCost) {
            setMessageAndSync(player, unlocks, "念头不足，无法推演杀招");
            applyCooldown(player, usageInfo, currentTick);
            return true;
        }

        if (finalCost > 0) {
            NianTouHelper.modify(player, -finalCost);
        }
        final String message = bonus.isActive()
            ? ("推演失败，消耗 " + finalCost + " 念头（大慧折扣）")
            : ("推演失败，消耗 " + finalCost + " 念头");
        setMessageAndSync(player, unlocks, message);
        applyCooldown(player, usageInfo, currentTick);
        return true;
    }

    private static void setMessageAndSync(
        final ServerPlayer player,
        final NianTouUnlocks unlocks,
        final String message
    ) {
        unlocks.setShazhaoMessage(message);
        PacketDistributor.sendToPlayer(player, new PacketSyncNianTouUnlocks(unlocks));
    }

    private static void applyCooldown(
        final ServerPlayer player,
        final NianTouData.Usage usageInfo,
        final int currentTick
    ) {
        final int cooldownTicks = Math.max(
            0,
            getMetaInt(usageInfo, "cooldown_ticks", DEFAULT_COOLDOWN_TICKS)
        );
        if (cooldownTicks <= 0) {
            return;
        }
        player.getPersistentData()
            .putInt(NBT_COOLDOWN_UNTIL_TICK, currentTick + cooldownTicks);
    }

    private static DeriveBonus resolveBonus(final ServerPlayer player) {
        final ActivePassives actives = KongqiaoAttachments.getActivePassives(player);
        if (actives == null
            || !actives.isActive(DaDaHuiGuWisdomDeductionEffect.PASSIVE_USAGE_ID)) {
            return DeriveBonus.none();
        }

        final double bonusChance = clampDouble(
            DaDaHuiGuWisdomDeductionEffect.getChanceBonusFromConfig(),
            0.0,
            0.8
        );
        final double failCostMultiplier = clampDouble(
            DaDaHuiGuWisdomDeductionEffect.getFailCostMultiplierFromConfig(),
            0.0,
            1.0
        );
        return new DeriveBonus(true, bonusChance, failCostMultiplier);
    }

    private static int applyFailCostDiscount(
        final int baseCost,
        final DeriveBonus bonus
    ) {
        if (!bonus.isActive() || baseCost <= 0) {
            return baseCost;
        }
        final double scaled = baseCost * bonus.failCostMultiplier();
        return Math.max(0, (int) Math.ceil(scaled));
    }

    private record DeriveBonus(
        boolean isActive,
        double chanceBonus,
        double failCostMultiplier
    ) {
        private static DeriveBonus none() {
            return new DeriveBonus(false, 0.0, 1.0);
        }
    }

    private static int getMetaInt(
        final NianTouData.Usage usage,
        final String key,
        final int defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Integer.parseInt(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    private static double clampDouble(
        final double value,
        final double min,
        final double max
    ) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}
