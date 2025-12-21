package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 宇道通用被动：攻击触发（概率）汲取（回血/回念头/回真元），可附带额外法术伤害。
 * <p>
 * 适合表现“吸魔/借力/斗空”等风格：以战养战。
 * </p>
 */
public class YuDaoAttackProcLeechEffect implements IGuEffect {

    private static final String META_PROC_CHANCE = "proc_chance";
    private static final String META_HEAL_AMOUNT = "heal_amount";
    private static final String META_NIANTOU_GAIN = "niantou_gain";
    private static final String META_ZHENYUAN_GAIN = "zhenyuan_gain";
    private static final String META_EXTRA_MAGIC_DAMAGE = "extra_magic_damage";

    private static final double DEFAULT_PROC_CHANCE = 0.12;
    private static final double DEFAULT_AMOUNT = 0.0;
    private static final double MAX_HEAL_PER_PROC = 60.0;

    private final String usageId;
    private final DaoHenHelper.DaoType daoType;

    public YuDaoAttackProcLeechEffect(
        final String usageId,
        final DaoHenHelper.DaoType daoType
    ) {
        this.usageId = usageId;
        this.daoType = daoType;
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
            return damage;
        }

        final double chance = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_PROC_CHANCE,
                DEFAULT_PROC_CHANCE
            ),
            0.0,
            1.0
        );
        if (attacker.getRandom().nextDouble() > chance) {
            return damage;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(null, attacker, usageInfo)) {
            return damage;
        }

        final double multiplier = daoType == null
            ? 1.0
            : DaoHenCalculator.calculateMultiplier(attacker, target, daoType);

        final double heal = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HEAL_AMOUNT, DEFAULT_AMOUNT)
        ) * Math.max(0.0, multiplier);
        if (heal > 0.0) {
            attacker.heal((float) Math.min(heal, MAX_HEAL_PER_PROC));
        }

        final double niantouGain = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_NIANTOU_GAIN, DEFAULT_AMOUNT)
        ) * Math.max(0.0, multiplier);
        if (niantouGain > 0.0) {
            NianTouHelper.modify(attacker, niantouGain);
        }

        final double zhenyuanGain = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_ZHENYUAN_GAIN, DEFAULT_AMOUNT)
        ) * Math.max(0.0, multiplier);
        if (zhenyuanGain > 0.0) {
            ZhenYuanHelper.modify(attacker, zhenyuanGain);
        }

        final double extraMagicDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_EXTRA_MAGIC_DAMAGE, DEFAULT_AMOUNT)
        );
        final double extraDamage = extraMagicDamage * Math.max(0.0, multiplier);
        if (extraDamage > 0.0) {
            if (attacker instanceof ServerPlayer player) {
                target.hurt(attacker.damageSources().playerAttack(player), (float) extraDamage);
            } else {
                target.hurt(attacker.damageSources().mobAttack(attacker), (float) extraDamage);
            }
        }

        return damage;
    }
}
