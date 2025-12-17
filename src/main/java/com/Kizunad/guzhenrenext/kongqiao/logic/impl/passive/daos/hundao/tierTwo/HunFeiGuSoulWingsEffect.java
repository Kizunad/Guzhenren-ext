package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierTwo;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 二转魂飞蛊：被动【魂翼余势】。
 * <p>
 * 原版（本体模组）提供飞行能力后，本扩展补充“游击型”空战手感：
 * <ul>
 *   <li>空中/飞行状态下获得少量移速与飞行速度加成，便于拉扯与位移。</li>
 *   <li>空中遭受投射物伤害时进行一定减免，表现为魂翼散影的卸力。</li>
 *   <li>维持消耗魂魄：魂魄不足时本被动暂停生效。</li>
 * </ul>
 * 魂道道痕用于增幅身法与卸力强度（自我增幅），同时也提高维持消耗，体现“越强越耗”。</p>
 */
public class HunFeiGuSoulWingsEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:hunfeigu_passive_soul_wings";

    private static final String NBT_ACTIVE = "HunFeiGuSoulWingsActive";

    private static final ResourceLocation MOVE_SPEED_MODIFIER_ID =
        ResourceLocation.parse("guzhenrenext:hunfeigu_air_move_speed");
    private static final ResourceLocation FLY_SPEED_MODIFIER_ID =
        ResourceLocation.parse("guzhenrenext:hunfeigu_air_flying_speed");

    private static final double DEFAULT_SOUL_COST_PER_SECOND = 0.25;
    private static final double DEFAULT_AIR_MOVE_SPEED_BONUS = 0.10;
    private static final double DEFAULT_AIR_FLY_SPEED_BONUS = 0.12;
    private static final double DEFAULT_PROJECTILE_DAMAGE_MULTIPLIER = 0.90;

    private static final double MIN_PROJECTILE_DAMAGE_MULTIPLIER = 0.55;
    private static final double MAX_AIR_SPEED_BONUS = 0.60;

    private static final int SOUL_PARTICLE_COUNT = 6;
    private static final double SOUL_PARTICLE_SPREAD = 0.25;
    private static final double SOUL_PARTICLE_SPEED = 0.02;
    private static final double SOUL_PARTICLE_Y_FACTOR = 0.6;

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public void onTick(final LivingEntity user, final ItemStack stack, final NianTouData.Usage usageInfo) {
        if (user.level().isClientSide()) {
            return;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(user);
        if (config != null && !config.isPassiveEnabled(USAGE_ID)) {
            deactivate(user);
            return;
        }

        if (!(user instanceof Player player)) {
            return;
        }

        if (!isActive(player) || !isAirborne(player)) {
            removeSpeedModifiers(player);
            return;
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            player,
            DaoHenHelper.DaoType.HUN_DAO
        );

        final double moveSpeedBonus = clamp(
            getMetaDouble(usageInfo, "air_move_speed_bonus", DEFAULT_AIR_MOVE_SPEED_BONUS) * selfMultiplier,
            0.0,
            MAX_AIR_SPEED_BONUS
        );
        final double flySpeedBonus = clamp(
            getMetaDouble(usageInfo, "air_flying_speed_bonus", DEFAULT_AIR_FLY_SPEED_BONUS) * selfMultiplier,
            0.0,
            MAX_AIR_SPEED_BONUS
        );

        applyModifier(player, Attributes.MOVEMENT_SPEED, MOVE_SPEED_MODIFIER_ID, moveSpeedBonus);
        applyModifier(player, Attributes.FLYING_SPEED, FLY_SPEED_MODIFIER_ID, flySpeedBonus);
    }

    @Override
    public void onSecond(final LivingEntity user, final ItemStack stack, final NianTouData.Usage usageInfo) {
        if (user.level().isClientSide()) {
            return;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(user);
        if (config != null && !config.isPassiveEnabled(USAGE_ID)) {
            deactivate(user);
            return;
        }

        if (!(user instanceof Player player)) {
            return;
        }

        if (!isAirborne(player)) {
            setActive(player, false);
            removeSpeedModifiers(player);
            return;
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            player,
            DaoHenHelper.DaoType.HUN_DAO
        );
        final double cost = Math.max(
            0.0,
            getMetaDouble(usageInfo, "soul_cost_per_second", DEFAULT_SOUL_COST_PER_SECOND) * selfMultiplier
        );

        if (HunPoHelper.getAmount(player) < cost) {
            setActive(player, false);
            removeSpeedModifiers(player);
            return;
        }

        HunPoHelper.modify(player, -cost);
        setActive(player, true);
    }

    @Override
    public float onHurt(
        final LivingEntity victim,
        final DamageSource source,
        final float damage,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        final TweakConfig config = KongqiaoAttachments.getTweakConfig(victim);
        if (config != null && !config.isPassiveEnabled(USAGE_ID)) {
            return damage;
        }

        if (!(victim instanceof Player player)) {
            return damage;
        }
        if (!isActive(player) || !isAirborne(player)) {
            return damage;
        }
        if (!source.is(DamageTypeTags.IS_PROJECTILE)) {
            return damage;
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            player,
            DaoHenHelper.DaoType.HUN_DAO
        );
        final double baseMultiplier = clamp(
            getMetaDouble(usageInfo, "projectile_damage_multiplier", DEFAULT_PROJECTILE_DAMAGE_MULTIPLIER),
            MIN_PROJECTILE_DAMAGE_MULTIPLIER,
            1.0
        );
        final double finalMultiplier = clamp(
            baseMultiplier / Math.max(1.0, selfMultiplier),
            MIN_PROJECTILE_DAMAGE_MULTIPLIER,
            1.0
        );

        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.SOUL,
                player.getX(),
                player.getY() + player.getBbHeight() * SOUL_PARTICLE_Y_FACTOR,
                player.getZ(),
                SOUL_PARTICLE_COUNT,
                SOUL_PARTICLE_SPREAD,
                SOUL_PARTICLE_SPREAD,
                SOUL_PARTICLE_SPREAD,
                SOUL_PARTICLE_SPEED
            );
        }

        return (float) (damage * finalMultiplier);
    }

    @Override
    public void onUnequip(final LivingEntity user, final ItemStack stack, final NianTouData.Usage usageInfo) {
        deactivate(user);
    }

    private static boolean isAirborne(final Player player) {
        return !player.onGround()
            && !player.isInWaterOrBubble()
            && !player.onClimbable()
            && !player.isPassenger();
    }

    private static boolean isActive(final Player player) {
        return player.getPersistentData().getBoolean(NBT_ACTIVE);
    }

    private static void setActive(final Player player, final boolean active) {
        player.getPersistentData().putBoolean(NBT_ACTIVE, active);
    }

    private static void deactivate(final LivingEntity user) {
        if (user instanceof Player player) {
            setActive(player, false);
            removeSpeedModifiers(player);
        }
    }

    private static void applyModifier(
        final LivingEntity user,
        final Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
        final ResourceLocation id,
        final double amount
    ) {
        final AttributeInstance attr = user.getAttribute(attribute);
        if (attr == null) {
            return;
        }
        final AttributeModifier existing = attr.getModifier(id);
        if (existing == null || Double.compare(existing.amount(), amount) != 0) {
            if (existing != null) {
                attr.removeModifier(id);
            }
            if (amount > 0.0) {
                attr.addTransientModifier(
                    new AttributeModifier(
                        id,
                        amount,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    )
                );
            }
        }
    }

    private static void removeSpeedModifiers(final LivingEntity user) {
        removeModifier(user, Attributes.MOVEMENT_SPEED, MOVE_SPEED_MODIFIER_ID);
        removeModifier(user, Attributes.FLYING_SPEED, FLY_SPEED_MODIFIER_ID);
    }

    private static void removeModifier(
        final LivingEntity user,
        final Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
        final ResourceLocation id
    ) {
        final AttributeInstance attr = user.getAttribute(attribute);
        if (attr == null) {
            return;
        }
        if (attr.getModifier(id) != null) {
            attr.removeModifier(id);
        }
    }

    private static double clamp(final double value, final double min, final double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private static double getMetaDouble(
        final NianTouData.Usage usage,
        final String key,
        final double defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Double.parseDouble(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }
}
