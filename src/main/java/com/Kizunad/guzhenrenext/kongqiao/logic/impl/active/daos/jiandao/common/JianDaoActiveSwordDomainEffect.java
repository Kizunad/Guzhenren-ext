package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common.JianDaoBoostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.LineOfSightTargetHelper;
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
import net.minecraft.world.phys.Vec3;

public class JianDaoActiveSwordDomainEffect implements IGuEffect {

    private static final String KEY_DOMAIN_UNTIL_TICK =
        "GuzhenrenExtJianDaoSwordDomainUntilTick";
    private static final String KEY_DOMAIN_CENTER_X =
        "GuzhenrenExtJianDaoSwordDomainCenterX";
    private static final String KEY_DOMAIN_CENTER_Y =
        "GuzhenrenExtJianDaoSwordDomainCenterY";
    private static final String KEY_DOMAIN_CENTER_Z =
        "GuzhenrenExtJianDaoSwordDomainCenterZ";

    private static final String META_RANGE = "range";
    private static final String META_DURATION_TICKS = "duration_ticks";
    private static final String META_RADIUS = "radius";
    private static final String META_DAMAGE_PER_SECOND = "damage_per_second";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double DEFAULT_RANGE = 16.0;
    private static final int DEFAULT_DURATION_TICKS = 20 * 10;
    private static final double DEFAULT_RADIUS = 6.0;
    private static final int DEFAULT_COOLDOWN_TICKS = 0;

    private static final String META_ZHENYUAN_BASE_COST_PER_SECOND =
        GuEffectCostHelper.META_ZHENYUAN_BASE_COST_PER_SECOND;
    private static final String META_NIANTOU_COST_PER_SECOND =
        GuEffectCostHelper.META_NIANTOU_COST_PER_SECOND;
    private static final String META_JINGLI_COST_PER_SECOND =
        GuEffectCostHelper.META_JINGLI_COST_PER_SECOND;
    private static final String META_HUNPO_COST_PER_SECOND =
        GuEffectCostHelper.META_HUNPO_COST_PER_SECOND;

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

    public JianDaoActiveSwordDomainEffect(
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

        final double range = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RANGE, DEFAULT_RANGE)
        );
        final LivingEntity target = LineOfSightTargetHelper.findTarget(player, range);
        final Vec3 center = target == null ? user.position() : target.position();

        final int duration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_DURATION_TICKS,
                DEFAULT_DURATION_TICKS
            )
        );
        if (duration <= 0) {
            return false;
        }

        user.getPersistentData().putInt(
            KEY_DOMAIN_UNTIL_TICK,
            user.tickCount + duration
        );
        user.getPersistentData().putDouble(KEY_DOMAIN_CENTER_X, center.x);
        user.getPersistentData().putDouble(KEY_DOMAIN_CENTER_Y, center.y);
        user.getPersistentData().putDouble(KEY_DOMAIN_CENTER_Z, center.z);

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

    @Override
    public void onSecond(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user == null) {
            return;
        }
        if (user.level().isClientSide()) {
            return;
        }
        if (!(user instanceof ServerPlayer player)) {
            return;
        }

        final int untilTick = user.getPersistentData().getInt(KEY_DOMAIN_UNTIL_TICK);
        if (untilTick <= 0) {
            return;
        }
        if (user.tickCount > untilTick) {
            clearDomain(user);
            return;
        }

        final double zhenyuanBaseCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ZHENYUAN_BASE_COST_PER_SECOND,
                0.0
            )
        );
        final double niantouCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_NIANTOU_COST_PER_SECOND,
                0.0
            )
        );
        final double jingliCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_JINGLI_COST_PER_SECOND,
                0.0
            )
        );
        final double hunpoCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_HUNPO_COST_PER_SECOND,
                0.0
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
            clearDomain(user);
            return;
        }

        final double centerX = user.getPersistentData().getDouble(KEY_DOMAIN_CENTER_X);
        final double centerY = user.getPersistentData().getDouble(KEY_DOMAIN_CENTER_Y);
        final double centerZ = user.getPersistentData().getDouble(KEY_DOMAIN_CENTER_Z);
        final Vec3 center = new Vec3(centerX, centerY, centerZ);

        final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(user, DaoHenHelper.DaoType.JIAN_DAO)
                * JianDaoBoostHelper.getJianXinMultiplier(user)
        );

        final double radius = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RADIUS, DEFAULT_RADIUS)
        ) * Math.max(0.0, selfMultiplier);
        final double damagePerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_DAMAGE_PER_SECOND, 0.0)
        );

        final AABB box = new AABB(
            center.x - radius,
            center.y - radius,
            center.z - radius,
            center.x + radius,
            center.y + radius,
            center.z + radius
        );
        final List<LivingEntity> targets = user.level().getEntitiesOfClass(
            LivingEntity.class,
            box,
            e -> e.isAlive() && e != user && !e.isAlliedTo(user)
        );

        for (LivingEntity target : targets) {
            final double multiplier = DaoHenCalculator.calculateMultiplier(
                user,
                target,
                DaoHenHelper.DaoType.JIAN_DAO
            ) * JianDaoBoostHelper.getJianXinMultiplier(user);
            if (damagePerSecond > 0.0) {
                target.hurt(
                    user.damageSources().playerAttack(player),
                    (float) (damagePerSecond * Math.max(0.0, multiplier))
                );
            }
            applyDebuffs(target, usageInfo, multiplier);
        }
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

    @Override
    public void onUnequip(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user == null) {
            return;
        }
        if (user.level().isClientSide()) {
            return;
        }
        clearDomain(user);
    }

    private static void clearDomain(final LivingEntity user) {
        user.getPersistentData().remove(KEY_DOMAIN_UNTIL_TICK);
        user.getPersistentData().remove(KEY_DOMAIN_CENTER_X);
        user.getPersistentData().remove(KEY_DOMAIN_CENTER_Y);
        user.getPersistentData().remove(KEY_DOMAIN_CENTER_Z);
    }
}
