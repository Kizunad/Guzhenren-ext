package com.Kizunad.guzhenrenext.bastion.guardian;

import com.Kizunad.guzhenrenext.bastion.BastionDao;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.entity.BastionGuardianData;
import com.Kizunad.guzhenrenext.bastion.service.BastionTalentEffectService;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 基地守卫属性服务。
 * <p>
 * 守卫必须“远强于原版”，并且强度随转数显著提升。
 * 该服务在生成时（或读档恢复时）应用属性，避免依赖原版默认数值。</p>
 */
public final class BastionGuardianStatsService {

    private BastionGuardianStatsService() {
    }

    /**
     * 属性配置常量（避免 MagicNumber）。
     */
    private static final class Stats {
        // ===== 通用基线（1 转） =====
        static final double BASE_HEALTH = 80.0;
        static final double HEALTH_PER_TIER = 60.0;

        // ===== 攻击伤害（遵循 GZR_INFO：按转数对数级提升） =====
        // 1转: 1-10
        // 2转: 10-100
        // 3转: 100-1000
        // 4转: 1000-10000
        // 后续高转继续按 10 倍级数外推（用于基地 5-9 转的压迫感）。
        static final double BASE_ATTACK_TIER_1 = 6.0;
        static final double ATTACK_MULT_PER_TIER = 10.0;

        static final double BASE_ARMOR = 8.0;
        static final double ARMOR_PER_TIER = 3.0;

        static final double BASE_TOUGHNESS = 2.0;
        static final double TOUGHNESS_PER_TIER = 1.0;

        static final double BASE_FOLLOW_RANGE = 28.0;
        static final double FOLLOW_RANGE_PER_TIER = 4.0;

        static final double BASE_SPEED = 0.28;
        static final double SPEED_PER_TIER = 0.01;
        static final double MAX_SPEED = 0.36;

        static final double BASE_KNOCKBACK_RESIST = 0.2;
        static final double KNOCKBACK_RESIST_PER_TIER = 0.05;
        static final double MAX_KNOCKBACK_RESIST = 0.9;

        static final int MIN_TIER = 1;

        // ===== 道途倍率 =====
        static final double ZHI_DAO_RANGE_MULT = 1.25;
        static final double HUN_DAO_SPEED_MULT = 1.15;
        static final double MU_DAO_HEALTH_MULT = 1.35;
        static final double LI_DAO_ATTACK_MULT = 1.35;
        static final double LI_DAO_ARMOR_MULT = 1.15;

        // ===== 精英倍率（Warden 外观） =====
        static final double ELITE_HEALTH_MULT = 1.8;
        static final double ELITE_ATTACK_MULT = 1.4;
        static final double ELITE_ARMOR_MULT = 1.2;

        private Stats() {
        }
    }

    /**
     * 将守卫属性应用到实体上。
     * <p>
     * 注意：此方法会直接设置 baseValue（不使用堆叠 modifier），避免读档/重复调用造成叠加。</p>
     */
    public static void applyGuardianStats(Mob guardian, @Nullable BastionDao dao, int tier, boolean elite) {
        if (guardian == null) {
            return;
        }
        if (dao == null) {
            dao = BastionDao.ZHI_DAO;
        }

        int safeTier = Math.max(Stats.MIN_TIER, tier);

        double health = Stats.BASE_HEALTH + (safeTier - 1) * Stats.HEALTH_PER_TIER;
        double attack = computeAttackDamageByTier(safeTier);
        double armor = Stats.BASE_ARMOR + (safeTier - 1) * Stats.ARMOR_PER_TIER;
        double toughness = Stats.BASE_TOUGHNESS + (safeTier - 1) * Stats.TOUGHNESS_PER_TIER;
        double range = Stats.BASE_FOLLOW_RANGE + (safeTier - 1) * Stats.FOLLOW_RANGE_PER_TIER;
        double speed = Math.min(Stats.MAX_SPEED, Stats.BASE_SPEED + (safeTier - 1) * Stats.SPEED_PER_TIER);
        double knock = Math.min(
            Stats.MAX_KNOCKBACK_RESIST,
            Stats.BASE_KNOCKBACK_RESIST + (safeTier - 1) * Stats.KNOCKBACK_RESIST_PER_TIER
        );

        // 道途特化
        switch (dao) {
            case ZHI_DAO -> range *= Stats.ZHI_DAO_RANGE_MULT;
            case HUN_DAO -> speed *= Stats.HUN_DAO_SPEED_MULT;
            case MU_DAO -> health *= Stats.MU_DAO_HEALTH_MULT;
            case LI_DAO -> {
                attack *= Stats.LI_DAO_ATTACK_MULT;
                armor *= Stats.LI_DAO_ARMOR_MULT;
            }
        }

        // 天赋：守卫伤害加成（需找到守卫所属基地）
        attack *= resolveGuardianDamageMultiplier(guardian);

        // 精英倍率（Warden 外观）
        if (elite) {
            health *= Stats.ELITE_HEALTH_MULT;
            attack *= Stats.ELITE_ATTACK_MULT;
            armor *= Stats.ELITE_ARMOR_MULT;
        }

        setBaseValueIfPresent(guardian, Attributes.MAX_HEALTH, health);
        setBaseValueIfPresent(guardian, Attributes.ATTACK_DAMAGE, attack);
        setBaseValueIfPresent(guardian, Attributes.ARMOR, armor);
        setBaseValueIfPresent(guardian, Attributes.ARMOR_TOUGHNESS, toughness);
        setBaseValueIfPresent(guardian, Attributes.FOLLOW_RANGE, range);
        setBaseValueIfPresent(guardian, Attributes.MOVEMENT_SPEED, speed);
        setBaseValueIfPresent(guardian, Attributes.KNOCKBACK_RESISTANCE, knock);

        // 生成/恢复后，确保当前生命值不超过上限
        if (guardian.getHealth() > guardian.getMaxHealth()) {
            guardian.setHealth(guardian.getMaxHealth());
        }
    }

    /**
     * 按转数计算守卫基础攻击（不含道途/精英倍率）。
     */
    private static double computeAttackDamageByTier(int tier) {
        int safeTier = Math.max(Stats.MIN_TIER, tier);
        // tier=1 -> 6
        // tier=2 -> 60
        // tier=3 -> 600
        // tier=4 -> 6000
        return Stats.BASE_ATTACK_TIER_1 * Math.pow(Stats.ATTACK_MULT_PER_TIER, safeTier - 1);
    }

    private static double resolveGuardianDamageMultiplier(Mob guardian) {
        if (!(guardian.level() instanceof ServerLevel level)) {
            return 1.0d;
        }
        java.util.UUID bastionId = BastionGuardianData.getBastionId(guardian);
        if (bastionId == null) {
            return 1.0d;
        }
        BastionSavedData savedData = BastionSavedData.get(level);
        var bastion = savedData.getBastion(bastionId);
        if (bastion == null) {
            return 1.0d;
        }
        return BastionTalentEffectService.getGuardianDamageMultiplier(bastion);
    }

    private static void setBaseValueIfPresent(
            Mob mob,
            net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
            double value) {
        AttributeInstance instance = mob.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        instance.setBaseValue(value);
    }
}
