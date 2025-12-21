package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common.JianDaoBoostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class JianDaoActiveAoEBurstEffect implements IGuEffect {

    private static final String META_RADIUS = "radius";
    private static final String META_DAMAGE = "damage";
    private static final String META_KNOCKBACK_STRENGTH = "knockback_strength";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double DEFAULT_RADIUS = 4.0;
    private static final double DEFAULT_KNOCKBACK = 0.0;
    private static final int DEFAULT_COOLDOWN_TICKS = 0;
    private static final double KNOCKBACK_Y = 0.1;

    public static final class DebuffSpec {
        private final Holder<MobEffect> effect;
        private final String durationKey;
        private final int defaultDurationTicks;
        private final String amplifierKey;
        private final int defaultAmplifier;

        public DebuffSpec(
            final Holder<MobEffect> effect,
            final String durationKey,
            final int defaultDurationTicks,
            final String amplifierKey,
            final int defaultAmplifier
        ) {
            this.effect = effect;
            this.durationKey = durationKey;
            this.defaultDurationTicks = defaultDurationTicks;
            this.amplifierKey = amplifierKey;
            this.defaultAmplifier = defaultAmplifier;
        }
    }

    private final String usageId;
    private final String nbtCooldownKey;
    private final List<DebuffSpec> debuffs;

    public JianDaoActiveAoEBurstEffect(
        final String usageId,
        final String nbtCooldownKey,
        final List<DebuffSpec> debuffs
    ) {
        this.usageId = usageId;
        this.nbtCooldownKey = nbtCooldownKey;
        this.debuffs = debuffs == null ? List.of() : List.copyOf(debuffs);
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
        if (user == null) {
            return false;
        }
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
                net.minecraft.network.chat.Component.literal(
                    "冷却中，剩余 " + remain + "t"
                ),
                true
            );
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.JIAN_DAO
        ) * JianDaoBoostHelper.getJianXinMultiplier(user);
        final double suiren = JianDaoBoostHelper.consumeSuiRenMultiplierIfActive(
            user
        );
        final double scale = DaoHenEffectScalingHelper.clampMultiplier(
            selfMultiplier * Math.max(1.0, suiren)
        );

        final double radius = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RADIUS, DEFAULT_RADIUS)
        ) * Math.max(0.0, scale);
        final double damage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_DAMAGE, 0.0)
        );
        final double knockbackStrength = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_KNOCKBACK_STRENGTH,
                DEFAULT_KNOCKBACK
            )
        );

        final AABB box = user.getBoundingBox().inflate(radius);
        final List<LivingEntity> targets = user.level().getEntitiesOfClass(
            LivingEntity.class,
            box,
            e -> e.isAlive() && e != user && !e.isAlliedTo(user)
        );

        for (LivingEntity target : targets) {
            final double dmgMultiplier = DaoHenCalculator.calculateMultiplier(
                user,
                target,
                DaoHenHelper.DaoType.JIAN_DAO
            ) * JianDaoBoostHelper.getJianXinMultiplier(user) * Math.max(1.0, suiren);
            if (damage > 0.0) {
                target.hurt(
                    user.damageSources().playerAttack(player),
                    (float) (damage * Math.max(0.0, dmgMultiplier))
                );
            }
            applyDebuffs(target, usageInfo, dmgMultiplier);
            if (knockbackStrength > 0.0) {
                applyKnockback(user, target, knockbackStrength);
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

    private void applyDebuffs(
        final LivingEntity target,
        final NianTouData.Usage usageInfo,
        final double multiplier
    ) {
        if (debuffs.isEmpty()) {
            return;
        }
        for (DebuffSpec spec : debuffs) {
            if (spec == null || spec.effect == null) {
                continue;
            }
            final int duration = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    spec.durationKey,
                    spec.defaultDurationTicks
                )
            );
            if (duration <= 0) {
                continue;
            }
            final int scaledDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
                duration,
                multiplier
            );
            if (scaledDuration <= 0) {
                continue;
            }
            final int amplifier = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    spec.amplifierKey,
                    spec.defaultAmplifier
                )
            );
            target.addEffect(
                new MobEffectInstance(spec.effect, scaledDuration, amplifier, true, true)
            );
        }
    }

    private static void applyKnockback(
        final LivingEntity user,
        final LivingEntity target,
        final double strength
    ) {
        final double dx = target.getX() - user.getX();
        final double dz = target.getZ() - user.getZ();
        final double len = Math.sqrt((dx * dx) + (dz * dz));
        if (len <= 0.0) {
            return;
        }
        final double nx = dx / len;
        final double nz = dz / len;
        target.push(nx * strength, KNOCKBACK_Y, nz * strength);
    }
}
