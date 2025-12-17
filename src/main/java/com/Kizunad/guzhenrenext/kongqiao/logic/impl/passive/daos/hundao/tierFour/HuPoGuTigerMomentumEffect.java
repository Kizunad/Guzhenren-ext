package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierFour;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

/**
 * 虎魄蛊：被动·【虎势】(Tiger Momentum)。
 * <p>
 * 玩法定位：鼓励持续进攻。每次命中叠层，上限 5 层；若 3 秒未攻击则清零。
 * <ul>
 *     <li>每层：+5% 基础攻击力，+2% 攻击速度</li>
 *     <li>每层：额外 +10 点魂魄伤害（随同本次命中触发）</li>
 *     <li>代价：每秒每层消耗 10 点魂魄</li>
 * </ul>
 * </p>
 */
public class HuPoGuTigerMomentumEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:hupogu_passive_tigermomentum";

    private static final String TAG_STACKS =
        "guzhenrenext_hupogu_momentum_stacks";
    private static final String TAG_LAST_ATTACK_TICK =
        "guzhenrenext_hupogu_momentum_last_attack_tick";

    private static final int DEFAULT_MAX_STACKS = 5;
    private static final double DEFAULT_ATTACK_MULTIPLIER_PER_STACK = 0.05;
    private static final double DEFAULT_ATTACK_SPEED_MULTIPLIER_PER_STACK =
        0.02;
    private static final double DEFAULT_SOUL_DAMAGE_PER_STACK = 10.0;
    private static final double DEFAULT_SOUL_COST_PER_STACK_PER_SECOND = 10.0;
    private static final int DEFAULT_TIMEOUT_TICKS = 60;

    private static final ResourceLocation ATTACK_DAMAGE_MODIFIER_ID =
        ResourceLocation.parse("guzhenrenext:hupogu_tiger_momentum_attack");
    private static final ResourceLocation ATTACK_SPEED_MODIFIER_ID =
        ResourceLocation.parse("guzhenrenext:hupogu_tiger_momentum_speed");

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public float onAttack(
        LivingEntity attacker,
        LivingEntity target,
        float damage,
        ItemStack stack,
        NianTouData.Usage usageInfo
    ) {
        if (attacker.level().isClientSide()) {
            return damage;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(attacker);
        if (config != null && !config.isPassiveEnabled(USAGE_ID)) {
            clearMomentum(attacker);
            return damage;
        }

        int maxStacks = getMetaInt(usageInfo, "max_stacks", DEFAULT_MAX_STACKS);
        int currentStacks = getStacks(attacker);
        int newStacks = Math.min(maxStacks, currentStacks + 1);

        setStacks(attacker, newStacks);
        setLastAttackTick(attacker, attacker.tickCount);
        updateAttributeModifiers(attacker, usageInfo, newStacks);

        double soulDamagePerStack = getMetaDouble(
            usageInfo,
            "soul_damage_per_stack",
            DEFAULT_SOUL_DAMAGE_PER_STACK
        );
        if (soulDamagePerStack > 0 && newStacks > 0) {
            double multiplier = DaoHenCalculator.calculateMultiplier(
                attacker,
                target,
                DaoHenHelper.DaoType.HUN_DAO
            );
            double finalSoulDamage =
                newStacks * soulDamagePerStack * multiplier;
            applySoulDamage(target, finalSoulDamage);
        }

        return damage;
    }

    @Override
    public void onSecond(
        LivingEntity user,
        ItemStack stack,
        NianTouData.Usage usageInfo
    ) {
        if (user.level().isClientSide()) {
            return;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(user);
        if (config != null && !config.isPassiveEnabled(USAGE_ID)) {
            clearMomentum(user);
            return;
        }

        int stacks = getStacks(user);
        if (stacks <= 0) {
            removeAttributeModifiers(user);
            return;
        }

        int timeoutTicks = getMetaInt(
            usageInfo,
            "timeout_ticks",
            DEFAULT_TIMEOUT_TICKS
        );
        int lastAttackTick = getLastAttackTick(user);
        if (
            timeoutTicks > 0 && (user.tickCount - lastAttackTick) > timeoutTicks
        ) {
            clearMomentum(user);
            return;
        }

        double soulCostPerStack = getMetaDouble(
            usageInfo,
            "soul_cost_per_stack_per_second",
            DEFAULT_SOUL_COST_PER_STACK_PER_SECOND
        );
        if (soulCostPerStack <= 0) {
            return;
        }

        double totalCost = stacks * soulCostPerStack;
        if (HunPoHelper.getAmount(user) < totalCost) {
            clearMomentum(user);
            return;
        }
        HunPoHelper.modify(user, -totalCost);
    }

    @Override
    public void onUnequip(
        LivingEntity user,
        ItemStack stack,
        NianTouData.Usage usageInfo
    ) {
        clearMomentum(user);
    }

    private static void applySoulDamage(LivingEntity target, double amount) {
        if (amount <= 0) {
            return;
        }
        double current = HunPoHelper.getAmount(target);
        if (current <= 0) {
            return;
        }
        HunPoHelper.modify(target, -amount);
        HunPoHelper.checkAndKill(target);
    }

    private static int getStacks(LivingEntity user) {
        return getTag(user).getInt(TAG_STACKS);
    }

    private static void setStacks(LivingEntity user, int stacks) {
        if (stacks <= 0) {
            getTag(user).remove(TAG_STACKS);
            return;
        }
        getTag(user).putInt(TAG_STACKS, stacks);
    }

    private static int getLastAttackTick(LivingEntity user) {
        return getTag(user).getInt(TAG_LAST_ATTACK_TICK);
    }

    private static void setLastAttackTick(LivingEntity user, int tick) {
        getTag(user).putInt(TAG_LAST_ATTACK_TICK, tick);
    }

    private static CompoundTag getTag(LivingEntity user) {
        return user.getPersistentData();
    }

    private void clearMomentum(LivingEntity user) {
        setStacks(user, 0);
        getTag(user).remove(TAG_LAST_ATTACK_TICK);
        removeAttributeModifiers(user);
    }

    private void updateAttributeModifiers(
        LivingEntity user,
        NianTouData.Usage usageInfo,
        int stacks
    ) {
        if (stacks <= 0) {
            removeAttributeModifiers(user);
            return;
        }

        double attackPerStack = getMetaDouble(
            usageInfo,
            "attack_multiplier_per_stack",
            DEFAULT_ATTACK_MULTIPLIER_PER_STACK
        );
        double speedPerStack = getMetaDouble(
            usageInfo,
            "attack_speed_multiplier_per_stack",
            DEFAULT_ATTACK_SPEED_MULTIPLIER_PER_STACK
        );

        applyOrUpdateModifier(
            user.getAttribute(Attributes.ATTACK_DAMAGE),
            ATTACK_DAMAGE_MODIFIER_ID,
            stacks * attackPerStack,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );
        applyOrUpdateModifier(
            user.getAttribute(Attributes.ATTACK_SPEED),
            ATTACK_SPEED_MODIFIER_ID,
            stacks * speedPerStack,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );
    }

    private static void applyOrUpdateModifier(
        AttributeInstance attribute,
        ResourceLocation id,
        double amount,
        AttributeModifier.Operation operation
    ) {
        if (attribute == null) {
            return;
        }
        AttributeModifier existing = attribute.getModifier(id);
        if (
            existing != null && Double.compare(existing.amount(), amount) == 0
        ) {
            return;
        }
        if (existing != null) {
            attribute.removeModifier(id);
        }
        if (Double.compare(amount, 0.0) == 0) {
            return;
        }
        attribute.addTransientModifier(
            new AttributeModifier(id, amount, operation)
        );
    }

    private static void removeAttributeModifiers(LivingEntity user) {
        AttributeInstance attack = user.getAttribute(Attributes.ATTACK_DAMAGE);
        if (
            attack != null &&
            attack.getModifier(ATTACK_DAMAGE_MODIFIER_ID) != null
        ) {
            attack.removeModifier(ATTACK_DAMAGE_MODIFIER_ID);
        }
        AttributeInstance speed = user.getAttribute(Attributes.ATTACK_SPEED);
        if (
            speed != null && speed.getModifier(ATTACK_SPEED_MODIFIER_ID) != null
        ) {
            speed.removeModifier(ATTACK_SPEED_MODIFIER_ID);
        }
    }

    private static int getMetaInt(
        NianTouData.Usage usage,
        String key,
        int defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Integer.parseInt(
                    Objects.requireNonNull(usage.metadata().get(key))
                );
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    private static double getMetaDouble(
        NianTouData.Usage usage,
        String key,
        double defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Double.parseDouble(
                    Objects.requireNonNull(usage.metadata().get(key))
                );
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }
}
