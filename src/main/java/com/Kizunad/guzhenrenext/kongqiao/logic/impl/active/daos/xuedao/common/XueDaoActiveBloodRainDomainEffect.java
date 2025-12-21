package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ItemStackCustomDataHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.PhysicalDamageSourceHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * 血雨蛊主动：展开血雨领域（持续维持扣费，每秒执行一次）。
 * <p>
 * 主要效果：为自身回复真元/生命，并对领域内敌人造成少量普通伤害与压制。
 * </p>
 */
public class XueDaoActiveBloodRainDomainEffect implements IGuEffect {

    public static final String KEY_DOMAIN_UNTIL_TICK =
        "GuzhenrenExtXueDao_BloodRainUntilTick";
    public static final String KEY_DOMAIN_RADIUS =
        "GuzhenrenExtXueDao_BloodRainRadius";

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_RADIUS = "radius";
    private static final String META_DURATION_TICKS = "duration_ticks";

    private static final String META_DAMAGE_PER_SECOND = "damage_per_second";
    private static final String META_HEAL_PER_SECOND = "heal_per_second";
    private static final String META_ZHENYUAN_GAIN_PER_SECOND =
        "zhenyuan_gain_per_second";
    private static final String META_NIANTOU_GAIN_PER_SECOND =
        "niantou_gain_per_second";

    private static final String META_DEBUFF_DURATION_TICKS = "debuff_duration_ticks";
    private static final String META_DEBUFF_AMPLIFIER = "debuff_amplifier";

    private static final String META_ZHENYUAN_BASE_COST_PER_SECOND =
        GuEffectCostHelper.META_ZHENYUAN_BASE_COST_PER_SECOND;
    private static final String META_NIANTOU_COST_PER_SECOND =
        GuEffectCostHelper.META_NIANTOU_COST_PER_SECOND;
    private static final String META_JINGLI_COST_PER_SECOND =
        GuEffectCostHelper.META_JINGLI_COST_PER_SECOND;
    private static final String META_HUNPO_COST_PER_SECOND =
        GuEffectCostHelper.META_HUNPO_COST_PER_SECOND;

    private static final int TICKS_PER_SECOND = 20;

    private static final int DEFAULT_COOLDOWN_TICKS = 20 * 45;
    private static final double DEFAULT_RADIUS = 10.0;
    private static final int DEFAULT_DURATION_TICKS = 20 * 18;

    private static final Holder<MobEffect> DEFAULT_DEBUFF = MobEffects.WEAKNESS;

    private final String usageId;
    private final String nbtCooldownKey;

    public XueDaoActiveBloodRainDomainEffect(
        final String usageId,
        final String nbtCooldownKey
    ) {
        this.usageId = usageId;
        this.nbtCooldownKey = nbtCooldownKey;
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
        if (user == null || stack == null || usageInfo == null) {
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
                Component.literal("冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        final double perSecondNianTou = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_NIANTOU_COST_PER_SECOND, 0.0)
        );
        final double perSecondJingli = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_JINGLI_COST_PER_SECOND, 0.0)
        );
        final double perSecondHunpo = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HUNPO_COST_PER_SECOND, 0.0)
        );
        final double perSecondZhenyuanBase = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ZHENYUAN_BASE_COST_PER_SECOND,
                0.0
            )
        );
        if (
            !GuEffectCostHelper.hasEnoughForSustain(
                player,
                user,
                perSecondNianTou,
                perSecondJingli,
                perSecondHunpo,
                perSecondZhenyuanBase
            )
        ) {
            return false;
        }

        final double baseRadius = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RADIUS, DEFAULT_RADIUS)
        );
        final int durationTicks = Math.max(
            1,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_DURATION_TICKS,
                DEFAULT_DURATION_TICKS
            )
        );

        final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(user, DaoHenHelper.DaoType.XUE_DAO)
        );
        final CompoundTag tag = ItemStackCustomDataHelper.copyCustomDataTag(stack);
        tag.putInt(KEY_DOMAIN_UNTIL_TICK, user.tickCount + durationTicks);
        tag.putDouble(KEY_DOMAIN_RADIUS, baseRadius * Math.max(0.0, selfMultiplier));
        ItemStackCustomDataHelper.setCustomDataTag(stack, tag);

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
    public void onTick(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user == null || stack == null || usageInfo == null) {
            return;
        }
        if (user.level().isClientSide()) {
            return;
        }
        final CompoundTag tag = ItemStackCustomDataHelper.copyCustomDataTag(stack);
        final int until = tag.getInt(KEY_DOMAIN_UNTIL_TICK);
        if (until <= 0) {
            return;
        }
        if (user.tickCount > until) {
            clear(stack);
            return;
        }
        if (user.tickCount % TICKS_PER_SECOND != 0) {
            return;
        }

        final double perSecondNianTou = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_NIANTOU_COST_PER_SECOND, 0.0)
        );
        final double perSecondJingli = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_JINGLI_COST_PER_SECOND, 0.0)
        );
        final double perSecondHunpo = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HUNPO_COST_PER_SECOND, 0.0)
        );
        final double perSecondZhenyuanBase = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ZHENYUAN_BASE_COST_PER_SECOND,
                0.0
            )
        );

        if (
            !GuEffectCostHelper.tryConsumeSustain(
                user,
                perSecondNianTou,
                perSecondJingli,
                perSecondHunpo,
                perSecondZhenyuanBase
            )
        ) {
            if (user instanceof ServerPlayer player) {
                player.displayClientMessage(Component.literal("血雨维持失败：资源不足。"), true);
            }
            clear(stack);
            return;
        }

        final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(user, DaoHenHelper.DaoType.XUE_DAO)
        );
        applySelfRecovery(user, usageInfo, selfMultiplier);
        applyDomainDamage(user, stack, usageInfo, selfMultiplier);
    }

    private static void applySelfRecovery(
        final LivingEntity user,
        final NianTouData.Usage usageInfo,
        final double selfMultiplier
    ) {
        final double baseHeal = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HEAL_PER_SECOND, 0.0)
        );
        final double heal = DaoHenEffectScalingHelper.scaleValue(baseHeal, selfMultiplier);
        if (heal > 0.0) {
            user.heal((float) heal);
        }

        final double baseZhenyuan = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_ZHENYUAN_GAIN_PER_SECOND, 0.0)
        );
        if (baseZhenyuan > 0.0) {
            ZhenYuanHelper.modify(
                user,
                DaoHenEffectScalingHelper.scaleValue(baseZhenyuan, selfMultiplier)
            );
        }

        final double baseNianTou = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_NIANTOU_GAIN_PER_SECOND, 0.0)
        );
        if (baseNianTou > 0.0) {
            NianTouHelper.modify(
                user,
                DaoHenEffectScalingHelper.scaleValue(baseNianTou, selfMultiplier)
            );
        }
    }

    private static void applyDomainDamage(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo,
        final double selfMultiplier
    ) {
        final CompoundTag tag = ItemStackCustomDataHelper.copyCustomDataTag(stack);
        final double radius = Math.max(1.0, tag.getDouble(KEY_DOMAIN_RADIUS));
        final double baseDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_DAMAGE_PER_SECOND, 0.0)
        );
        final int baseDebuffDuration = Math.max(
            0,
            UsageMetadataHelper.getInt(usageInfo, META_DEBUFF_DURATION_TICKS, 0)
        );
        final int debuffDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
            baseDebuffDuration,
            selfMultiplier
        );
        final int debuffAmplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(usageInfo, META_DEBUFF_AMPLIFIER, 0)
        );

        final AABB area = user.getBoundingBox().inflate(radius);
        final List<LivingEntity> targets = user.level().getEntitiesOfClass(
            LivingEntity.class,
            area,
            e -> e.isAlive() && e != user && !e.isAlliedTo(user)
        );
        for (LivingEntity t : targets) {
            if (DEFAULT_DEBUFF != null && debuffDuration > 0) {
                t.addEffect(
                    new MobEffectInstance(
                        DEFAULT_DEBUFF,
                        debuffDuration,
                        debuffAmplifier,
                        true,
                        true
                    )
                );
            }
            if (baseDamage > 0.0) {
                final double multiplier = DaoHenCalculator.calculateMultiplier(
                    user,
                    t,
                    DaoHenHelper.DaoType.XUE_DAO
                );
                final double finalDamage = baseDamage * Math.max(0.0, multiplier);
                if (finalDamage > 0.0) {
                    t.hurt(
                        PhysicalDamageSourceHelper.buildPhysicalDamageSource(user),
                        (float) finalDamage
                    );
                }
            }
        }
    }

    private static void clear(final ItemStack stack) {
        ItemStackCustomDataHelper.removeKey(stack, KEY_DOMAIN_UNTIL_TICK);
        ItemStackCustomDataHelper.removeKey(stack, KEY_DOMAIN_RADIUS);
    }
}
