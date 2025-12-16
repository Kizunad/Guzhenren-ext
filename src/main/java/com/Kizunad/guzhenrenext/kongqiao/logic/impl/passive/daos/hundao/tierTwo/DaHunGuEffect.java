package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierTwo;

import com.Kizunad.guzhenrenext.guzhenrenBridge.CultivationHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 二转大魂蛊逻辑：壮魂。
 * <p>
 * 1. 魂魄未满时：消耗真元，快速回复魂魄。
 * 2. 魂魄已满时：消耗真元，缓慢提升魂魄抗性。
 * 3. 始终生效：消耗真元，缓慢提升蛊师修为。
 * </p>
 */
public class DaHunGuEffect implements IGuEffect {

    public static final String USAGE_ID = "guzhenren:dahuongu_passive_strengthen";

    // 默认数值
    private static final double DEFAULT_BASE_COST = 100.0; // 基础消耗，经模组公式计算后降低
    private static final double DEFAULT_SOUL_REGEN = 5.0;
    private static final double DEFAULT_RES_GAIN = 0.5;
    private static final double DEFAULT_CULTIVATION_GAIN = 5.0;

    private static final double DAO_HEN_DIVISOR = 1000.0;

    private static final int SOUL_PARTICAL_COUNT = 3;
    private static final int SOUL_RESISTANCE_PARTICAL_COUNT = 2;
    private static final double PARTICLE_VERTICAL_OFFSET = 0.8;
    private static final double PARTICLE_SPREAD_X = 0.3;
    private static final double PARTICLE_SPREAD_Y = 0.1;
    private static final double PARTICLE_SPREAD_Z = 0.3;
    private static final double PARTICLE_SPEED = 0.05;

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public void onSecond(LivingEntity user, ItemStack stack, NianTouData.Usage usageInfo) {
        final TweakConfig config = KongqiaoAttachments.getTweakConfig(user);
        if (config != null && !config.isPassiveEnabled(USAGE_ID)) {
            return;
        }
        // 1. 读取 Metadata 配置
        double baseCost = getMetaDouble(usageInfo, "zhenyuan_base_cost", DEFAULT_BASE_COST);
        double soulRegenBase = getMetaDouble(usageInfo, "soul_regen", DEFAULT_SOUL_REGEN);
        double resGainBase = getMetaDouble(usageInfo, "resistance_gain", DEFAULT_RES_GAIN);
        double cultivationBase = getMetaDouble(usageInfo, "cultivation_gain", DEFAULT_CULTIVATION_GAIN);

        // 2. 获取基础数据
        double currentSoul = HunPoHelper.getAmount(user);
        double maxSoul = HunPoHelper.getMaxAmount(user);
        double currentRes = HunPoHelper.getResistance(user);
        double maxRes = HunPoHelper.getMaxResistance(user);

        // 3. 计算消耗 (使用模组标准转数/阶段算法)
        double realCost = ZhenYuanHelper.calculateGuCost(user, baseCost);

        // 4. 计算道痕增幅 (仅增幅产出效果)
        double daoHen = DaoHenHelper.getDaoHen(user, DaoHenHelper.DaoType.HUN_DAO);
        double multiplier = 1.0 + (daoHen / DAO_HEN_DIVISOR);

        // 5. 执行逻辑 (如果有足够真元)
        if (ZhenYuanHelper.hasEnough(user, realCost)) {
            ZhenYuanHelper.modify(user, -realCost);

            // A. 修为提升 (始终生效)
            double cultGain = cultivationBase * multiplier;
            CultivationHelper.modifyProgress(user, cultGain);

            // B. 魂魄/抗性提升
            if (currentSoul < maxSoul) {
                // 修复魂魄
                double healAmount = soulRegenBase * multiplier;
                HunPoHelper.modify(user, healAmount);
                spawnParticles(user, ParticleTypes.HAPPY_VILLAGER, SOUL_PARTICAL_COUNT);
            } else if (currentRes < maxRes) {
                // 壮大抗性
                double gainAmount = resGainBase * multiplier;
                HunPoHelper.modifyResistance(user, gainAmount);
                spawnParticles(user, ParticleTypes.SOUL, SOUL_RESISTANCE_PARTICAL_COUNT);
            }
        }
    }

    private double getMetaDouble(NianTouData.Usage usage, String key, double defaultValue) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Double.parseDouble(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    private void spawnParticles(LivingEntity entity, net.minecraft.core.particles.SimpleParticleType type, int count) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                type,
                entity.getX(),
                entity.getY() + entity.getEyeHeight() * PARTICLE_VERTICAL_OFFSET,
                entity.getZ(),
                count,
                PARTICLE_SPREAD_X,
                PARTICLE_SPREAD_Y,
                PARTICLE_SPREAD_Z,
                PARTICLE_SPEED
            );
        }
    }
}
