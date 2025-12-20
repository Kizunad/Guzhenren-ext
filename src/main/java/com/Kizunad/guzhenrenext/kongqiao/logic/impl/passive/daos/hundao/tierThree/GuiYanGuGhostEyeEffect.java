package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierThree;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * 三转鬼眼蛊：被动【窥魂索敌】。
 * <p>
 * 原著描述为“依据魂魄来侦察敌人位置，可以令隐身的敌人无所遁形”。这里用 MC 的“发光”实现：
 * - 每秒扫描一定半径内的敌对生物并给予短暂 {@link MobEffects#GLOWING}。
 * - 发光会透视轮廓，因此即使隐身也会暴露。
 * - 扫描半径受魂道道痕的自乘倍率增幅；为了体现“看得更远更费力”，魂魄消耗也随之提升。
 * </p>
 */
public class GuiYanGuGhostEyeEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:gguiyangu_passive_soul_scout";

    private static final double DEFAULT_RADIUS = 16.0;
    private static final int DEFAULT_GLOW_DURATION_TICKS = 50;
    private static final double DEFAULT_HUNPO_COST_PER_SECOND = 0.5;
    private static final double DEFAULT_HUNPO_COST_PER_TARGET = 0.05;
    private static final double DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND = 180.0;

    private static final int DEFAULT_PARTICLE_COUNT_PER_TARGET = 2;
    private static final double PARTICLE_SPREAD = 0.2;
    private static final double PARTICLE_SPEED = 0.01;
    private static final double PARTICLE_Y_OFFSET = 0.75;

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public void onSecond(
        LivingEntity user,
        ItemStack stack,
        NianTouData.Usage usageInfo
    ) {
        if (user.level().isClientSide()) {
            return;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(user);
        if (config != null && !config.isPassiveEnabled(USAGE_ID)) {
            return;
        }

        if (!(user.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        double baseRadius = UsageMetadataHelper.getDouble(
            usageInfo,
            "scan_radius",
            DEFAULT_RADIUS
        );
        double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.HUN_DAO
        );
        double radius = baseRadius * selfMultiplier;

        AABB area = user.getBoundingBox().inflate(radius);
        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(
            LivingEntity.class,
            area,
            e -> e.isAlive() && e != user && !isAlly(user, e)
        );

        double baseHunpoCostPerSecond = UsageMetadataHelper.getDouble(
            usageInfo,
            GuEffectCostHelper.META_HUNPO_COST_PER_SECOND,
            DEFAULT_HUNPO_COST_PER_SECOND
        );
        double hunpoCostPerTarget = UsageMetadataHelper.getDouble(
            usageInfo,
            "hunpo_cost_per_target",
            DEFAULT_HUNPO_COST_PER_TARGET
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
        final double niantouCostPerTarget = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, "niantou_cost_per_target", 0.0)
        );
        final double jingliCostPerTarget = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, "jingli_cost_per_target", 0.0)
        );
        final double zhenyuanBaseCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_ZHENYUAN_BASE_COST_PER_SECOND,
                DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND
            )
        );
        final double hunpoCostPerSecond = Math.max(
            0.0,
            (baseHunpoCostPerSecond + hunpoCostPerTarget * targets.size()) * selfMultiplier
        );

        // “看得越远越费力”：既增幅侦察半径，也同步提高维持消耗。
        if (
            !GuEffectCostHelper.tryConsumeSustain(
                user,
                niantouCostPerSecond + niantouCostPerTarget * targets.size(),
                jingliCostPerSecond + jingliCostPerTarget * targets.size(),
                hunpoCostPerSecond,
                zhenyuanBaseCostPerSecond
            )
        ) {
            return;
        }

        int glowDuration = UsageMetadataHelper.getInt(
            usageInfo,
            "glow_duration_ticks",
            DEFAULT_GLOW_DURATION_TICKS
        );
        int particleCount = UsageMetadataHelper.getInt(
            usageInfo,
            "particle_count_per_target",
            DEFAULT_PARTICLE_COUNT_PER_TARGET
        );

        for (LivingEntity target : targets) {
            if (glowDuration > 0) {
                target.addEffect(
                    new MobEffectInstance(
                        MobEffects.GLOWING,
                        glowDuration,
                        0,
                        true,
                        false,
                        false
                    )
                );
            }

            if (particleCount > 0) {
                serverLevel.sendParticles(
                    ParticleTypes.SOUL,
                    target.getX(),
                    target.getY() + target.getBbHeight() * PARTICLE_Y_OFFSET,
                    target.getZ(),
                    particleCount,
                    PARTICLE_SPREAD,
                    PARTICLE_SPREAD,
                    PARTICLE_SPREAD,
                    PARTICLE_SPEED
                );
            }
        }
    }

    private static boolean isAlly(LivingEntity owner, LivingEntity target) {
        if (target.isAlliedTo(owner)) {
            return true;
        }
        if (target instanceof TamableAnimal pet && pet.getOwner() == owner) {
            return true;
        }
        return false;
    }
}
