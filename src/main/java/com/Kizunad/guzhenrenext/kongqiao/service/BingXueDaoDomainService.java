package com.Kizunad.guzhenrenext.kongqiao.service;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bingxuedao.common.BingXueDaoActiveSummonSerpentEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 冰雪道领域/召唤运行时服务。
 * <p>
 * 负责：<br>
 * 1) 雪域领域：每秒执行一次范围效果与资源维持；<br>
 * 2) 白相仙蛇：到期自动清理召唤物（基于玩家 persistentData 记录）。<br>
 * </p>
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class BingXueDaoDomainService {

    public static final String KEY_DOMAIN_UNTIL_TICK =
        "GuzhenrenExtBingXue_SnowDomainUntilTick";
    public static final String KEY_DOMAIN_RADIUS =
        "GuzhenrenExtBingXue_SnowDomainRadius";
    public static final String KEY_DOMAIN_NIANTOU_COST_PER_SECOND =
        "GuzhenrenExtBingXue_SnowDomainNianTouCostPerSecond";
    public static final String KEY_DOMAIN_JINGLI_COST_PER_SECOND =
        "GuzhenrenExtBingXue_SnowDomainJingLiCostPerSecond";
    public static final String KEY_DOMAIN_HUNPO_COST_PER_SECOND =
        "GuzhenrenExtBingXue_SnowDomainHunPoCostPerSecond";
    public static final String KEY_DOMAIN_ZHENYUAN_BASE_COST_PER_SECOND =
        "GuzhenrenExtBingXue_SnowDomainZhenYuanBaseCostPerSecond";
    public static final String KEY_DOMAIN_MAGIC_DAMAGE_PER_SECOND =
        "GuzhenrenExtBingXue_SnowDomainMagicDamagePerSecond";
    public static final String KEY_DOMAIN_FREEZE_TICKS_PER_SECOND =
        "GuzhenrenExtBingXue_SnowDomainFreezeTicksPerSecond";
    public static final String KEY_DOMAIN_SLOW_DURATION_TICKS =
        "GuzhenrenExtBingXue_SnowDomainSlowDurationTicks";
    public static final String KEY_DOMAIN_SLOW_AMPLIFIER =
        "GuzhenrenExtBingXue_SnowDomainSlowAmplifier";

    private static final int TICKS_PER_SECOND = 20;

    private BingXueDaoDomainService() {}

    @SubscribeEvent
    public static void onPlayerTick(final PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        cleanupSummons(player);
        if (player.tickCount % TICKS_PER_SECOND == 0) {
            tickSnowDomain(player);
        }
    }

    private static void cleanupSummons(final ServerPlayer player) {
        if (player == null) {
            return;
        }
        if (!player.getPersistentData().hasUUID(BingXueDaoActiveSummonSerpentEffect.KEY_SUMMON_UUID)) {
            return;
        }

        final int untilTick = player.getPersistentData().getInt(
            BingXueDaoActiveSummonSerpentEffect.KEY_SUMMON_UNTIL_TICK
        );
        if (untilTick > 0 && player.tickCount <= untilTick) {
            return;
        }

        final ServerLevel level = player.serverLevel();
        final Entity entity = level.getEntity(
            player.getPersistentData().getUUID(
                BingXueDaoActiveSummonSerpentEffect.KEY_SUMMON_UUID
            )
        );
        if (entity != null && entity.isAlive()) {
            entity.discard();
        }
        player.getPersistentData().remove(
            BingXueDaoActiveSummonSerpentEffect.KEY_SUMMON_UUID
        );
        player.getPersistentData().remove(
            BingXueDaoActiveSummonSerpentEffect.KEY_SUMMON_UNTIL_TICK
        );
    }

    private static void tickSnowDomain(final ServerPlayer player) {
        final int untilTick = player.getPersistentData().getInt(KEY_DOMAIN_UNTIL_TICK);
        if (untilTick <= 0) {
            return;
        }
        if (player.tickCount > untilTick) {
            clearSnowDomain(player);
            return;
        }

        final double radius = Math.max(
            1.0,
            player.getPersistentData().getDouble(KEY_DOMAIN_RADIUS)
        );
        final double niantouCostPerSecond = Math.max(
            0.0,
            player.getPersistentData().getDouble(KEY_DOMAIN_NIANTOU_COST_PER_SECOND)
        );
        final double jingliCostPerSecond = Math.max(
            0.0,
            player.getPersistentData().getDouble(KEY_DOMAIN_JINGLI_COST_PER_SECOND)
        );
        final double hunpoCostPerSecond = Math.max(
            0.0,
            player.getPersistentData().getDouble(KEY_DOMAIN_HUNPO_COST_PER_SECOND)
        );
        final double zhenyuanBaseCostPerSecond = Math.max(
            0.0,
            player.getPersistentData().getDouble(KEY_DOMAIN_ZHENYUAN_BASE_COST_PER_SECOND)
        );

        if (
            niantouCostPerSecond > 0.0
                && NianTouHelper.getAmount(player) < niantouCostPerSecond
        ) {
            player.displayClientMessage(Component.literal("雪域维持失败：念头不足。"), true);
            clearSnowDomain(player);
            return;
        }
        if (
            jingliCostPerSecond > 0.0
                && JingLiHelper.getAmount(player) < jingliCostPerSecond
        ) {
            player.displayClientMessage(Component.literal("雪域维持失败：精力不足。"), true);
            clearSnowDomain(player);
            return;
        }
        if (
            hunpoCostPerSecond > 0.0
                && HunPoHelper.getAmount(player) < hunpoCostPerSecond
        ) {
            player.displayClientMessage(Component.literal("雪域维持失败：魂魄不足。"), true);
            clearSnowDomain(player);
            return;
        }
        final double zhenyuanCostPerSecond = ZhenYuanHelper.calculateGuCost(
            player,
            zhenyuanBaseCostPerSecond
        );
        if (
            zhenyuanCostPerSecond > 0.0
                && !ZhenYuanHelper.hasEnough(player, zhenyuanCostPerSecond)
        ) {
            player.displayClientMessage(Component.literal("雪域维持失败：真元不足。"), true);
            clearSnowDomain(player);
            return;
        }

        if (
            !GuEffectCostHelper.tryConsumeSustain(
                player,
                niantouCostPerSecond,
                jingliCostPerSecond,
                hunpoCostPerSecond,
                zhenyuanBaseCostPerSecond
            )
        ) {
            player.displayClientMessage(Component.literal("雪域维持失败：资源不足。"), true);
            clearSnowDomain(player);
            return;
        }

        final double baseDamage = Math.max(
            0.0,
            player.getPersistentData().getDouble(KEY_DOMAIN_MAGIC_DAMAGE_PER_SECOND)
        );
        final int freezeTicks = Math.max(
            0,
            player.getPersistentData().getInt(KEY_DOMAIN_FREEZE_TICKS_PER_SECOND)
        );
        final int slowDuration = Math.max(
            0,
            player.getPersistentData().getInt(KEY_DOMAIN_SLOW_DURATION_TICKS)
        );
        final int slowAmplifier = Math.max(
            0,
            player.getPersistentData().getInt(KEY_DOMAIN_SLOW_AMPLIFIER)
        );

        final AABB area = player.getBoundingBox().inflate(radius);
        final List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class,
            area,
            e -> e.isAlive() && e != player && !e.isAlliedTo(player)
        );

        final Holder<MobEffect> slow = MobEffects.MOVEMENT_SLOWDOWN;
        for (LivingEntity target : targets) {
            if (slow != null && slowDuration > 0) {
                target.addEffect(
                    new MobEffectInstance(slow, slowDuration, slowAmplifier, true, true)
                );
            }
            if (freezeTicks > 0) {
                target.setTicksFrozen(target.getTicksFrozen() + freezeTicks);
            }
            if (baseDamage > 0.0) {
                final double multiplier = DaoHenCalculator.calculateMultiplier(
                    player,
                    target,
                    DaoHenHelper.DaoType.BING_XUE_DAO
                );
                target.hurt(
                    player.damageSources().mobAttack(player),
                    (float) (baseDamage * multiplier)
                );
            }
        }
    }

    private static void clearSnowDomain(final ServerPlayer player) {
        player.getPersistentData().remove(KEY_DOMAIN_UNTIL_TICK);
        player.getPersistentData().remove(KEY_DOMAIN_RADIUS);
        player.getPersistentData().remove(KEY_DOMAIN_NIANTOU_COST_PER_SECOND);
        player.getPersistentData().remove(KEY_DOMAIN_JINGLI_COST_PER_SECOND);
        player.getPersistentData().remove(KEY_DOMAIN_HUNPO_COST_PER_SECOND);
        player.getPersistentData().remove(KEY_DOMAIN_ZHENYUAN_BASE_COST_PER_SECOND);
        player.getPersistentData().remove(KEY_DOMAIN_MAGIC_DAMAGE_PER_SECOND);
        player.getPersistentData().remove(KEY_DOMAIN_FREEZE_TICKS_PER_SECOND);
        player.getPersistentData().remove(KEY_DOMAIN_SLOW_DURATION_TICKS);
        player.getPersistentData().remove(KEY_DOMAIN_SLOW_AMPLIFIER);
    }
}
