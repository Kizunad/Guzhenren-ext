package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.common;

import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 变化道：主动效果额外资源消耗装饰器。
 * <p>
 * 用于给“复用通用模板的主动效果”补充精力/魂魄消耗逻辑，避免为每个效果重复造轮子。
 * </p>
 * <p>
 * 约束：只对 onActivate 生效（成功后扣除），不干预被动/战斗回调，避免误扣与难以判定触发的问题。
 * </p>
 */
public final class BianHuaDaoActiveExtraCostDecorator implements IGuEffect {

    private final IGuEffect delegate;

    public BianHuaDaoActiveExtraCostDecorator(final IGuEffect delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getUsageId() {
        return delegate.getUsageId();
    }

    @Override
    public boolean onActivate(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user != null && user.level() != null && user.level().isClientSide()) {
            return false;
        }
        final ServerPlayer player =
            user instanceof ServerPlayer serverPlayer ? serverPlayer : null;
        if (!GuEffectCostHelper.hasEnoughJingliHunpo(player, user, usageInfo)) {
            return false;
        }
        final boolean ok = delegate.onActivate(user, stack, usageInfo);
        if (ok) {
            GuEffectCostHelper.consumeJingliHunpoIfPresent(user, usageInfo);
        }
        return ok;
    }

    @Override
    public void onTick(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        delegate.onTick(user, stack, usageInfo);
    }

    @Override
    public void onSecond(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        delegate.onSecond(user, stack, usageInfo);
    }

    @Override
    public void onEquip(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        delegate.onEquip(user, stack, usageInfo);
    }

    @Override
    public void onUnequip(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        delegate.onUnequip(user, stack, usageInfo);
    }

    @Override
    public float onAttack(
        final LivingEntity attacker,
        final LivingEntity target,
        final float damage,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        return delegate.onAttack(attacker, target, damage, stack, usageInfo);
    }

    @Override
    public float onHurt(
        final LivingEntity victim,
        final DamageSource source,
        final float damage,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        return delegate.onHurt(victim, source, damage, stack, usageInfo);
    }
}
