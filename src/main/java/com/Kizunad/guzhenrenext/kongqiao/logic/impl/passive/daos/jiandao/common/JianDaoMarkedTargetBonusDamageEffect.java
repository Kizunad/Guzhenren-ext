package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common;

import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class JianDaoMarkedTargetBonusDamageEffect
    extends JianDaoAttackProcBonusDamageEffect {

    private static final String META_REQUIRE_MARK = "require_mark";

    public JianDaoMarkedTargetBonusDamageEffect(
        final String usageId,
        final String procCooldownKey,
        final List<EffectSpec> debuffs
    ) {
        super(usageId, procCooldownKey, debuffs);
    }

    @Override
    public float onAttack(
        final LivingEntity attacker,
        final LivingEntity target,
        final float damage,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (attacker == null || target == null) {
            return damage;
        }
        if (!shouldApplyToTarget(attacker, target, usageInfo)) {
            return damage;
        }
        return super.onAttack(attacker, target, damage, stack, usageInfo);
    }

    private static boolean shouldApplyToTarget(
        final LivingEntity attacker,
        final LivingEntity target,
        final NianTouData.Usage usageInfo
    ) {
        final boolean requireMark = NianTouDataMetadataBool.get(
            usageInfo,
            META_REQUIRE_MARK,
            true
        );
        if (!requireMark) {
            return true;
        }
        final UUID marked = JianDaoMarkedTargetState.getMarkedTargetUuid(attacker);
        return marked != null && marked.equals(target.getUUID());
    }
}

