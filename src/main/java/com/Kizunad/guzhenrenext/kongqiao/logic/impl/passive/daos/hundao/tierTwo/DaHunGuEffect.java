package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierTwo;

import com.Kizunad.guzhenrenext.guzhenrenBridge.CultivationHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
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
    private static final double DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND = 120.0;
    private static final double DEFAULT_SOUL_REGEN = 5.0;
    private static final double DEFAULT_RES_GAIN = 0.5;
    private static final double DEFAULT_CULTIVATION_GAIN = 5.0;

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
        if (user.level().isClientSide()) {
            return;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(user);
        if (config != null && !config.isPassiveEnabled(USAGE_ID)) {
            return;
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.HUN_DAO
        );

        final double niantouCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_NIANTOU_COST_PER_SECOND,
                0.0
            )
        );
        final double jingliCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_JINGLI_COST_PER_SECOND,
                0.0
            )
        );
        final double hunpoCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_HUNPO_COST_PER_SECOND,
                0.0
            )
        );
        final double zhenyuanBaseCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_ZHENYUAN_BASE_COST_PER_SECOND,
                DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND
            )
        );
        if (
            !GuEffectCostHelper.tryConsumeSustain(
                user,
                niantouCostPerSecond,
                jingliCostPerSecond,
                hunpoCostPerSecond,
                zhenyuanBaseCostPerSecond
            )
        ) {
            return;
        }

        final double soulRegenBase = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, "soul_regen", DEFAULT_SOUL_REGEN)
        );
        final double resGainBase = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, "resistance_gain", DEFAULT_RES_GAIN)
        );
        final double cultivationBase = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                "cultivation_gain",
                DEFAULT_CULTIVATION_GAIN
            )
        );

        // 2. 获取基础数据
        double currentSoul = HunPoHelper.getAmount(user);
        double maxSoul = HunPoHelper.getMaxAmount(user);
        double currentRes = HunPoHelper.getResistance(user);
        double maxRes = HunPoHelper.getMaxResistance(user);

        // 3. 执行逻辑（道痕仅增幅产出效果）
        CultivationHelper.modifyProgress(user, cultivationBase * selfMultiplier);

        if (currentSoul < maxSoul) {
            HunPoHelper.modify(user, soulRegenBase * selfMultiplier);
            spawnParticles(user, ParticleTypes.HAPPY_VILLAGER, SOUL_PARTICAL_COUNT);
            return;
        }
        if (currentRes < maxRes) {
            HunPoHelper.modifyResistance(user, resGainBase * selfMultiplier);
            spawnParticles(user, ParticleTypes.SOUL, SOUL_RESISTANCE_PARTICAL_COUNT);
        }
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
