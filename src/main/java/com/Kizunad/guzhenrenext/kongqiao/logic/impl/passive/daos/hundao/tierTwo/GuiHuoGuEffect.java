package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierTwo;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 二转鬼火蛊逻辑：离魂阴火。
 * <p>
 * 防御性反伤：
 * 1. 敌人攻击时，反弹魂道伤害，优先削减魂魄抗性。
 * 2. 持续消耗魂魄维持。
 * 3. 反弹时额外消耗魂魄。
 * </p>
 */
public class GuiHuoGuEffect implements IGuEffect {

    public static final String USAGE_ID = "guzhenren:guihuogu_passive_thorns";
    private static final double BASE_SOUL_DMG = 10.0;

    private static final int CRIT_PARTICLE_COUNT = 15;
    private static final double CRIT_PARTICLE_SPREAD = 0.5;
    private static final double CRIT_PARTICLE_SPEED = 0.1;
    private static final int SOUL_FIRE_COUNT = 8;
    private static final double SOUL_FIRE_OFFSET_XZ = 0.2;
    private static final double SOUL_FIRE_OFFSET_Y = 0.4;
    private static final double SOUL_FIRE_SPEED = 0.02;
    private static final int PAIN_EFFECT_BASE_DURATION = 10;
    private static final int MAX_PAIN_EFFECT_DURATION = 20 * 5;
    private static final String NBT_LAST_THORNS_TICK =
        "GuzhenrenExtGuiHuoGuLastThornsTick";

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    /**
     * 每秒维持消耗：按 metadata 的 *_cost_per_second 配置扣除。
     */
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
            ) * selfMultiplier
        );
        final double zhenyuanBaseCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_ZHENYUAN_BASE_COST_PER_SECOND,
                0.0
            )
        );

        GuEffectCostHelper.tryConsumeSustain(
            user,
            niantouCostPerSecond,
            jingliCostPerSecond,
            hunpoCostPerSecond,
            zhenyuanBaseCostPerSecond
        );
    }

    /**
     * 受伤反制逻辑
     */
    @Override
    public float onHurt(
        LivingEntity victim,
        DamageSource source,
        float damage,
        ItemStack stack,
        NianTouData.Usage usageInfo
    ) {
        final TweakConfig config = KongqiaoAttachments.getTweakConfig(victim);
        if (config != null && !config.isPassiveEnabled(USAGE_ID)) {
            return damage;
        }
        // 1. 检查攻击者是否有效
        if (!(source.getEntity() instanceof LivingEntity attacker) || attacker == victim) {
            return damage;
        }

        final long nowTick = victim.level().getGameTime();
        final int cooldownTicks = Math.max(
            0,
            UsageMetadataHelper.getInt(usageInfo, "cooldown_ticks", 0)
        );
        if (cooldownTicks > 0 && !canTrigger(victim, nowTick, cooldownTicks)) {
            return damage;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(null, victim, usageInfo)) {
            return damage;
        }

        // 3. 计算反击伤害
        // 基础伤害 * 道痕交互倍率
        double baseDmg = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, "soul_damage", BASE_SOUL_DMG)
        );

        double multiplier = DaoHenCalculator.calculateMultiplier(victim, attacker, DaoHenHelper.DaoType.HUN_DAO);
        double finalDmg = baseDmg * multiplier;

        // 4. 执行伤害逻辑：混合机制
        // 优先扣除魂魄；如果无魂魄或魂魄耗尽，则造成生命值伤害
        double targetSoul = HunPoHelper.getAmount(attacker);

        if (targetSoul > 0) {
            // 敌人有魂魄：攻击灵魂
            HunPoHelper.modify(attacker, -finalDmg);
            // 检查是否致死（触发模组原本的魂魄消散逻辑）
            HunPoHelper.checkAndKill(attacker);
        } else {
            // 敌人无魂魄（如普通怪物或魂魄已归零）：转化为普通伤害扣除 HP（避免法术伤害穿甲过强）
            attacker.hurt(victim.damageSources().mobAttack(victim), (float) finalDmg);
        }

        // 5. 视觉反馈：幽蓝鬼火
        if (attacker.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                attacker.getX(),
                attacker.getY() + (attacker.getEyeHeight() / 2),
                attacker.getZ(),
                SOUL_FIRE_COUNT,
                SOUL_FIRE_OFFSET_XZ,
                SOUL_FIRE_OFFSET_Y,
                SOUL_FIRE_OFFSET_XZ,
                SOUL_FIRE_SPEED
            );
        }
        
        // 6. 剧痛特效 (减速)
        final int painDuration = (int) UsageMetadataHelper.clamp(
            PAIN_EFFECT_BASE_DURATION + finalDmg,
            PAIN_EFFECT_BASE_DURATION,
            MAX_PAIN_EFFECT_DURATION
        );
        attacker.addEffect(
            new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                painDuration, // 伤害越高，减速越久（有上限）
                1
            )
        );

        return damage;
    }

    private static boolean canTrigger(
        final LivingEntity victim,
        final long nowTick,
        final int cooldownTicks
    ) {
        final CompoundTag tag = victim.getPersistentData();
        final long last = tag.getLong(NBT_LAST_THORNS_TICK);
        if (nowTick - last < cooldownTicks) {
            return false;
        }
        tag.putLong(NBT_LAST_THORNS_TICK, nowTick);
        return true;
    }
}
