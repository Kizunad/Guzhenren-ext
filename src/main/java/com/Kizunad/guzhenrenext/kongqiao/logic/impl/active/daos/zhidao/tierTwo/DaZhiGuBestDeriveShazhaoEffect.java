package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.tierTwo;

import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.NianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.tierTwo.DaZhiGuWisdomStrategyEffect;
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
 * 二转大智蛊：主动【智推】（择优推演）。
 * <p>
 * 设计目标：在轮盘中直接触发一次“杀招推演”，但不是随机选择候选，而是择取成功率最高者推演。
 * 若被动【智算】处于激活状态，将获得推演成功率加成与失败念头消耗折扣（均由 metadata 控制）。
 * </p>
 */
public class DaZhiGuBestDeriveShazhaoEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:dazhigu_active_shazhao_derive_best";

    private static final String NBT_COOLDOWN_UNTIL_TICK =
        "DaZhiGuBestDeriveShazhaoCooldownUntilTick";

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
            setMessageAndSync(
                player,
                unlocks,
                "智推冷却中，剩余 " + (remainTicks / TICKS_PER_SECOND) + " 秒"
            );
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
            selectBestCandidate(player, candidates);
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
            ? ("推演失败，消耗 " + finalCost + " 念头（大智折扣）")
            : ("推演失败，消耗 " + finalCost + " 念头");
        setMessageAndSync(player, unlocks, message);
        applyCooldown(player, usageInfo, currentTick);
        return true;
    }

    private static ShazhaoUnlockService.UnlockCandidate selectBestCandidate(
        final ServerPlayer player,
        final List<ShazhaoUnlockService.UnlockCandidate> candidates
    ) {
        ShazhaoUnlockService.UnlockCandidate best = candidates.get(0);
        for (int i = 1; i < candidates.size(); i++) {
            final ShazhaoUnlockService.UnlockCandidate current = candidates.get(i);
            if (current == null) {
                continue;
            }
            if (current.chance() > best.chance()) {
                best = current;
                continue;
            }
            if (Double.compare(current.chance(), best.chance()) == 0
                && player.getRandom().nextBoolean()) {
                best = current;
            }
        }
        return best;
    }

    private static DeriveBonus resolveBonus(final ServerPlayer player) {
        final ActivePassives actives = KongqiaoAttachments.getActivePassives(player);
        if (actives == null
            || !actives.isActive(DaZhiGuWisdomStrategyEffect.PASSIVE_USAGE_ID)) {
            return DeriveBonus.none();
        }

        final double chanceBonus = clampDouble(
            DaZhiGuWisdomStrategyEffect.getChanceBonusFromConfig(),
            0.0,
            0.8
        );
        final double failCostMultiplier = clampDouble(
            DaZhiGuWisdomStrategyEffect.getFailCostMultiplierFromConfig(),
            0.0,
            1.0
        );
        return new DeriveBonus(true, chanceBonus, failCostMultiplier);
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

    private record DeriveBonus(
        boolean isActive,
        double chanceBonus,
        double failCostMultiplier
    ) {
        private static DeriveBonus none() {
            return new DeriveBonus(false, 0.0, 1.0);
        }
    }
}

