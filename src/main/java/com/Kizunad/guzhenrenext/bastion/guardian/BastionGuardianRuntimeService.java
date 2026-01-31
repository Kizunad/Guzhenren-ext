package com.Kizunad.guzhenrenext.bastion.guardian;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionDao;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.entity.BastionGuardianData;
import com.Kizunad.guzhenrenext.bastion.guardian.entity.BastionWardenGuardian;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;

/**
 * 基地守卫运行时服务（服务端）。
 * <p>
 * 负责：
 * <ul>
 *   <li>在实体首次 tick 时，从 BastionGuardianData / BastionSavedData 补全同步信息。</li>
 *   <li>应用守卫属性（高强度）。</li>
 *   <li>目标选择（玩家/敌对守卫同优先级）。</li>
 *   <li>技能 tick。</li>
 *   <li>脱战兜底（清理非法目标）。</li>
 * </ul>
 * </p>
 */
public final class BastionGuardianRuntimeService {

    private BastionGuardianRuntimeService() {
    }

    private static final class Config {
        static final int TARGETING_INTERVAL_TICKS = 10;
        static final int MIN_FOLLOW_RANGE = 16;
        static final int MAX_FOLLOW_RANGE = 64;
        static final int MIN_TIER = 1;

        private Config() {
        }
    }

    private static final class PersistentKeys {
        static final String ROOT = "BastionGuardianRuntime";
        static final String STATS_APPLIED = "StatsApplied";
        static final String STATS_TIER = "StatsTier";
        static final String STATS_DAO = "StatsDao";
        static final String STATS_ELITE = "StatsElite";

        private PersistentKeys() {
        }
    }

    public static void tick(ServerLevel level, Mob mob) {
        if (!BastionGuardianData.hasCompleteData(mob)) {
            return;
        }
        if (!mob.isAlive() || mob.isRemoved()) {
            return;
        }

        UUID bastionId = BastionGuardianData.getBastionId(mob);
        if (bastionId == null) {
            return;
        }
        BastionDao dao = resolveDao(level, bastionId);
        int tier = Math.max(Config.MIN_TIER, BastionGuardianData.getTier(mob));
        boolean elite = mob instanceof BastionWardenGuardian;

        ensureGuardianStatsApplied(mob, dao, tier, elite);

        // 目标选择：玩家/敌对守卫同优先级
        long gameTime = level.getGameTime();
        if ((gameTime % Config.TARGETING_INTERVAL_TICKS) == 0) {
            tickTargeting(level, mob);
        }

        BastionGuardianSkillService.tickGuardianSkills(mob, dao, tier, elite);
        BastionGuardianCombatRules.clearInvalidTarget(level, mob, mob.getTarget());

        // 守卫杀招：复用 niantou 的杀招 JSON 配置（无消耗）
        com.Kizunad.guzhenrenext.bastion.guardian.shazhao.BastionGuardianShazhaoService
            .tryCast(level, mob, dao, tier);

    }

    private static BastionDao resolveDao(ServerLevel level, UUID bastionId) {
        BastionSavedData savedData = BastionSavedData.get(level);
        BastionData bastion = savedData.getBastion(bastionId);
        if (bastion == null) {
            return BastionDao.ZHI_DAO;
        }
        return bastion.primaryDao();
    }

    private static void ensureGuardianStatsApplied(Mob mob, BastionDao dao, int tier, boolean elite) {
        var persistentData = mob.getPersistentData();
        var root = persistentData.getCompound(PersistentKeys.ROOT);

        boolean applied = root.getBoolean(PersistentKeys.STATS_APPLIED);
        int daoOrdinal = dao.ordinal();

        if (applied
            && root.getInt(PersistentKeys.STATS_TIER) == tier
            && root.getInt(PersistentKeys.STATS_DAO) == daoOrdinal
            && root.getBoolean(PersistentKeys.STATS_ELITE) == elite) {
            return;
        }

        BastionGuardianStatsService.applyGuardianStats(mob, dao, tier, elite);
        mob.setHealth(mob.getMaxHealth());

        root.putBoolean(PersistentKeys.STATS_APPLIED, true);
        root.putInt(PersistentKeys.STATS_TIER, tier);
        root.putInt(PersistentKeys.STATS_DAO, daoOrdinal);
        root.putBoolean(PersistentKeys.STATS_ELITE, elite);
        persistentData.put(PersistentKeys.ROOT, root);
    }

    private static void tickTargeting(ServerLevel level, Mob guardian) {
        LivingEntity current = guardian.getTarget();
        if (current != null
            && current.isAlive()
            && !current.isRemoved()
            && !(current instanceof ServerPlayer player && isIgnoredPlayer(player))) {
            // 当前目标合法，保留
            return;
        }

        int range = (int) Math.round(guardian.getAttributeValue(Attributes.FOLLOW_RANGE));
        range = Math.max(Config.MIN_FOLLOW_RANGE, Math.min(Config.MAX_FOLLOW_RANGE, range));

        AABB box = guardian.getBoundingBox().inflate(range);
        List<LivingEntity> candidates = level.getEntitiesOfClass(
            LivingEntity.class,
            box,
            e -> e != guardian
                && e.isAlive()
                && !e.isRemoved()
                && isTargetCandidate(level, guardian, e)
        );

        LivingEntity best = null;
        double bestDistSq = Double.MAX_VALUE;
        for (LivingEntity candidate : candidates) {
            double distSq = guardian.distanceToSqr(candidate);
            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                best = candidate;
            }
        }

        guardian.setTarget(best);
    }

    private static boolean isTargetCandidate(ServerLevel level, Mob guardian, LivingEntity candidate) {
        if (candidate instanceof ServerPlayer player) {
            return !isIgnoredPlayer(player);
        }
        if (!BastionGuardianCombatRules.isGuardian(candidate)) {
            return false;
        }
        return BastionGuardianCombatRules.canGuardianDamage(level, guardian, candidate);
    }

    private static boolean isIgnoredPlayer(ServerPlayer player) {
        return player.isSpectator() || player.isCreative();
    }
}
