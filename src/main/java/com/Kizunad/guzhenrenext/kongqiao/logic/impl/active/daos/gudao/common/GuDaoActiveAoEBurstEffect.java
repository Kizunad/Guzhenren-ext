package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.gudao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.AABB;

/**
 * 骨道通用主动：范围爆发（以自身为中心），可造成伤害并施加 debuff。
 */
public class GuDaoActiveAoEBurstEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_RADIUS = "radius";
    private static final String META_PHYSICAL_DAMAGE = "physical_damage";
    private static final String META_MAGIC_DAMAGE = "magic_damage";

    private static final int DEFAULT_COOLDOWN_TICKS = 200;
    private static final double DEFAULT_RADIUS = 4.0;

    public record EffectSpec(
        Holder<MobEffect> effect,
        String durationKey,
        int defaultDurationTicks,
        String amplifierKey,
        int defaultAmplifier
    ) {}

    private final String usageId;
    private final String nbtCooldownKey;
    private final List<EffectSpec> effects;

    public GuDaoActiveAoEBurstEffect(
        final String usageId,
        final String nbtCooldownKey,
        final List<EffectSpec> effects
    ) {
        this.usageId = usageId;
        this.nbtCooldownKey = nbtCooldownKey;
        this.effects = effects;
    }

    @Override
    public String getUsageId() {
        return usageId;
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

        final int remain = GuEffectCooldownHelper.getRemainingTicks(
            user,
            nbtCooldownKey
        );
        if (remain > 0) {
            player.displayClientMessage(
                Component.literal("冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        final double radius = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RADIUS, DEFAULT_RADIUS)
        );
        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final double physicalDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_PHYSICAL_DAMAGE, 0.0)
        );
        final double magicDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_MAGIC_DAMAGE, 0.0)
        );
        final AABB area = user.getBoundingBox().inflate(radius);
        final List<LivingEntity> victims = user.level().getEntitiesOfClass(
            LivingEntity.class,
            area,
            e -> e.isAlive() && e != user && !e.isAlliedTo(user)
        );

        for (LivingEntity victim : victims) {
            final double multiplier = DaoHenCalculator.calculateMultiplier(
                user,
                victim,
                DaoHenHelper.DaoType.GU_DAO
            );

            if (physicalDamage > 0.0) {
                final DamageSource source = buildPhysicalDamageSource(user);
                victim.hurt(source, (float) (physicalDamage * multiplier));
            } else if (magicDamage > 0.0) {
                victim.hurt(
                    user.damageSources().magic(),
                    (float) (magicDamage * multiplier)
                );
            }

            if (effects != null) {
                for (EffectSpec spec : effects) {
                    if (spec == null || spec.effect() == null) {
                        continue;
                    }
                    final int duration = Math.max(
                        0,
                        UsageMetadataHelper.getInt(
                            usageInfo,
                            spec.durationKey(),
                            spec.defaultDurationTicks()
                        )
                    );
                    final int scaledDuration = DaoHenEffectScalingHelper
                        .scaleDurationTicks(duration, multiplier);
                    final int amplifier = Math.max(
                        0,
                        UsageMetadataHelper.getInt(
                            usageInfo,
                            spec.amplifierKey(),
                            spec.defaultAmplifier()
                        )
                    );
                    if (scaledDuration > 0) {
                        victim.addEffect(
                            new MobEffectInstance(
                                spec.effect(),
                                scaledDuration,
                                amplifier,
                                true,
                                true
                            )
                        );
                    }
                }
            }
        }

        final int cooldownTicks = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_COOLDOWN_TICKS,
                DEFAULT_COOLDOWN_TICKS
            )
        );
        if (cooldownTicks > 0) {
            GuEffectCooldownHelper.setCooldownUntilTick(
                user,
                nbtCooldownKey,
                user.tickCount + cooldownTicks
            );
        }

        return true;
    }

    private static DamageSource buildPhysicalDamageSource(final LivingEntity user) {
        if (user instanceof Player player) {
            return user.damageSources().playerAttack(player);
        }
        return user.damageSources().mobAttack(user);
    }
}
