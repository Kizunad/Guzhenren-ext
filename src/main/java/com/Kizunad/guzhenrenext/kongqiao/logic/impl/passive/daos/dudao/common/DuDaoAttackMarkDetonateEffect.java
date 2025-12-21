package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.dudao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.PhysicalDamageSourceHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 毒道被动：攻击触发（概率）- 种下“毒印”，在延迟后于下一次命中引爆（普通伤害）并附带减益。
 * <p>
 * 该实现不依赖全局 tick 扫描：毒印在目标身上以 NBT 记录“可引爆时间点”，当后续攻击再次命中时引爆。
 * 设计目标：体现毒道“延迟爆发/持续压迫”，同时避免法术伤害穿甲过强。
 * </p>
 *
 * <p>metadata:</p>
 * <ul>
 *   <li>proc_chance</li>
 *   <li>cooldown_ticks</li>
 *   <li>mark_delay_ticks（毒印延迟）</li>
 *   <li>mark_expire_ticks（毒印过期）</li>
 *   <li>detonate_physical_damage（引爆基础普通伤害）</li>
 *   <li>effect_duration_ticks / effect_amplifier（引爆时附带减益）</li>
 *   <li>（消耗）niantou_cost / jingli_cost / hunpo_cost / zhenyuan_base_cost（仅在“种印”时扣除）</li>
 * </ul>
 */
public class DuDaoAttackMarkDetonateEffect implements IGuEffect {

    private static final String META_PROC_CHANCE = "proc_chance";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_MARK_DELAY_TICKS = "mark_delay_ticks";
    private static final String META_MARK_EXPIRE_TICKS = "mark_expire_ticks";

    private static final String META_DETONATE_PHYSICAL_DAMAGE = "detonate_physical_damage";
    private static final String META_EFFECT_DURATION_TICKS = "effect_duration_ticks";
    private static final String META_EFFECT_AMPLIFIER = "effect_amplifier";

    private static final double DEFAULT_PROC_CHANCE = 0.12;
    private static final int DEFAULT_COOLDOWN_TICKS = 80;
    private static final int DEFAULT_MARK_DELAY_TICKS = 80;
    private static final int DEFAULT_MARK_EXPIRE_TICKS = 240;

    private static final int MIN_TICKS = 1;
    private static final double MAX_DETONATE_DAMAGE = 20000.0;

    private final String usageId;
    private final String nbtCooldownKey;
    private final String nbtMarkUntilKey;
    private final String nbtMarkExpireKey;
    private final List<Holder<MobEffect>> debuffs;

    public DuDaoAttackMarkDetonateEffect(
        final String usageId,
        final String nbtCooldownKey,
        final String nbtMarkUntilKey,
        final String nbtMarkExpireKey,
        final List<Holder<MobEffect>> debuffs
    ) {
        this.usageId = usageId;
        this.nbtCooldownKey = nbtCooldownKey;
        this.nbtMarkUntilKey = nbtMarkUntilKey;
        this.nbtMarkExpireKey = nbtMarkExpireKey;
        this.debuffs = debuffs == null ? List.of() : List.copyOf(debuffs);
    }

    @Override
    public String getUsageId() {
        return usageId;
    }

    @Override
    public float onAttack(
        final LivingEntity attacker,
        final LivingEntity target,
        final float damage,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (attacker.level().isClientSide()) {
            return damage;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(attacker);
        if (config != null && !config.isPassiveEnabled(usageId)) {
            clearMark(target);
            return damage;
        }

        if (tryDetonate(attacker, target, usageInfo)) {
            return damage;
        }

        final int remain = GuEffectCooldownHelper.getRemainingTicks(attacker, nbtCooldownKey);
        if (remain > 0) {
            return damage;
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            attacker,
            DaoHenHelper.DaoType.DU_DAO
        );

        final double baseChance = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_PROC_CHANCE,
                DEFAULT_PROC_CHANCE
            ),
            0.0,
            1.0
        );
        final double chance = DaoHenEffectScalingHelper.scaleChance(baseChance, selfMultiplier);
        if (attacker.getRandom().nextDouble() > chance) {
            return damage;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(null, attacker, usageInfo)) {
            return damage;
        }

        final int delay = Math.max(
            MIN_TICKS,
            UsageMetadataHelper.getInt(usageInfo, META_MARK_DELAY_TICKS, DEFAULT_MARK_DELAY_TICKS)
        );
        final int expire = Math.max(
            delay,
            UsageMetadataHelper.getInt(usageInfo, META_MARK_EXPIRE_TICKS, DEFAULT_MARK_EXPIRE_TICKS)
        );

        final int now = attacker.tickCount;
        target.getPersistentData().putInt(nbtMarkUntilKey, now + delay);
        target.getPersistentData().putInt(nbtMarkExpireKey, now + expire);

        final int cooldownTicks = Math.max(
            0,
            UsageMetadataHelper.getInt(usageInfo, META_COOLDOWN_TICKS, DEFAULT_COOLDOWN_TICKS)
        );
        if (cooldownTicks > 0) {
            GuEffectCooldownHelper.setCooldownUntilTick(
                attacker,
                nbtCooldownKey,
                attacker.tickCount + cooldownTicks
            );
        }

        return damage;
    }

    private boolean tryDetonate(
        final LivingEntity attacker,
        final LivingEntity target,
        final NianTouData.Usage usageInfo
    ) {
        final int until = target.getPersistentData().getInt(nbtMarkUntilKey);
        final int expire = target.getPersistentData().getInt(nbtMarkExpireKey);
        if (until <= 0 || expire <= 0) {
            return false;
        }

        final int now = attacker.tickCount;
        if (now > expire) {
            clearMark(target);
            return false;
        }
        if (now < until) {
            return false;
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            attacker,
            DaoHenHelper.DaoType.DU_DAO
        );

        final double baseDamage = UsageMetadataHelper.clamp(
            Math.max(
                0.0,
                UsageMetadataHelper.getDouble(usageInfo, META_DETONATE_PHYSICAL_DAMAGE, 0.0)
            ),
            0.0,
            MAX_DETONATE_DAMAGE
        );
        if (baseDamage > 0.0) {
            final double multiplier = DaoHenCalculator.calculateMultiplier(
                attacker,
                target,
                DaoHenHelper.DaoType.DU_DAO
            );
            final DamageSource source = PhysicalDamageSourceHelper.buildPhysicalDamageSource(attacker);
            target.hurt(source, (float) (baseDamage * multiplier));
        }

        applyDebuffs(target, usageInfo, selfMultiplier);

        clearMark(target);
        return true;
    }

    private void applyDebuffs(
        final LivingEntity target,
        final NianTouData.Usage usageInfo,
        final double selfMultiplier
    ) {
        if (debuffs.isEmpty()) {
            return;
        }

        final int duration = Math.max(
            0,
            UsageMetadataHelper.getInt(usageInfo, META_EFFECT_DURATION_TICKS, 0)
        );
        final int scaledDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
            duration,
            selfMultiplier
        );
        if (scaledDuration <= 0) {
            return;
        }

        final int amplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(usageInfo, META_EFFECT_AMPLIFIER, 0)
        );

        for (Holder<MobEffect> effect : debuffs) {
            if (effect == null) {
                continue;
            }
            target.addEffect(new MobEffectInstance(effect, scaledDuration, amplifier, true, true));
        }
    }

    private void clearMark(final LivingEntity target) {
        if (target == null) {
            return;
        }
        target.getPersistentData().remove(nbtMarkUntilKey);
        target.getPersistentData().remove(nbtMarkExpireKey);
    }
}

