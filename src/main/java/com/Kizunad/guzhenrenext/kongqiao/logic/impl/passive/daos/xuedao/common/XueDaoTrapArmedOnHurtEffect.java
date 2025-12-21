package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ItemStackCustomDataHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.PhysicalDamageSourceHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * 隐血盅被动：当“已布置陷阱”状态存在时，受击触发一次血爆。
 * <p>
 * 说明：原版为放置型陷阱，这里用“主动布置 -> 下次受击触发”的方式近似还原，
 * 既保留战术感，也避免引入复杂的方块/实体布置逻辑。
 * </p>
 */
public class XueDaoTrapArmedOnHurtEffect implements IGuEffect {

    public static final String KEY_TRAP_ARMED_UNTIL_TICK =
        "GuzhenrenExtXueDao_TrapArmedUntilTick";

    private static final String META_EXPLOSION_RADIUS = "explosion_radius";
    private static final String META_EXPLOSION_DAMAGE = "explosion_damage";
    private static final String META_SLOW_DURATION_TICKS = "slow_duration_ticks";
    private static final String META_SLOW_AMPLIFIER = "slow_amplifier";

    private static final double DEFAULT_RADIUS = 5.0;
    private static final double DEFAULT_DAMAGE = 18.0;

    private static final Holder<MobEffect> DEFAULT_SLOW = MobEffects.MOVEMENT_SLOWDOWN;

    private final String usageId;

    public XueDaoTrapArmedOnHurtEffect(final String usageId) {
        this.usageId = usageId;
    }

    @Override
    public String getUsageId() {
        return usageId;
    }

    @Override
    public float onHurt(
        final LivingEntity victim,
        final DamageSource source,
        final float damage,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (victim == null || stack == null || usageInfo == null) {
            return damage;
        }
        if (victim.level().isClientSide()) {
            return damage;
        }

        final CompoundTag tag = ItemStackCustomDataHelper.copyCustomDataTag(stack);
        final int until = tag.getInt(KEY_TRAP_ARMED_UNTIL_TICK);
        if (until <= 0) {
            return damage;
        }
        if (victim.tickCount > until) {
            tag.remove(KEY_TRAP_ARMED_UNTIL_TICK);
            ItemStackCustomDataHelper.setCustomDataTag(stack, tag);
            return damage;
        }

        triggerExplosion(victim, stack, usageInfo);
        tag.remove(KEY_TRAP_ARMED_UNTIL_TICK);
        ItemStackCustomDataHelper.setCustomDataTag(stack, tag);

        return damage;
    }

    @Override
    public void onUnequip(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user == null || stack == null) {
            return;
        }
        if (user.level().isClientSide()) {
            return;
        }
        ItemStackCustomDataHelper.removeKey(stack, KEY_TRAP_ARMED_UNTIL_TICK);
    }

    private static void triggerExplosion(
        final LivingEntity victim,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        final double radius = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_EXPLOSION_RADIUS,
                DEFAULT_RADIUS
            )
        );
        final double baseDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_EXPLOSION_DAMAGE,
                DEFAULT_DAMAGE
            )
        );
        final int baseSlowDuration = Math.max(
            0,
            UsageMetadataHelper.getInt(usageInfo, META_SLOW_DURATION_TICKS, 0)
        );
        final int slowAmplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(usageInfo, META_SLOW_AMPLIFIER, 0)
        );

        final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(victim, DaoHenHelper.DaoType.XUE_DAO)
        );
        final int slowDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
            baseSlowDuration,
            selfMultiplier
        );

        final AABB area = victim.getBoundingBox().inflate(radius);
        final List<LivingEntity> targets = victim.level().getEntitiesOfClass(
            LivingEntity.class,
            area,
            e -> e.isAlive() && e != victim && !e.isAlliedTo(victim)
        );

        final DamageSource dmgSource =
            PhysicalDamageSourceHelper.buildPhysicalDamageSource(victim);

        for (LivingEntity t : targets) {
            if (DEFAULT_SLOW != null && slowDuration > 0) {
                t.addEffect(
                    new MobEffectInstance(DEFAULT_SLOW, slowDuration, slowAmplifier, true, true)
                );
            }
            if (baseDamage > 0.0) {
                final double multiplier = DaoHenCalculator.calculateMultiplier(
                    victim,
                    t,
                    DaoHenHelper.DaoType.XUE_DAO
                );
                final double finalDamage = baseDamage * Math.max(0.0, multiplier);
                if (finalDamage > 0.0) {
                    t.hurt(dmgSource, (float) finalDamage);
                }
            }
        }
    }
}
