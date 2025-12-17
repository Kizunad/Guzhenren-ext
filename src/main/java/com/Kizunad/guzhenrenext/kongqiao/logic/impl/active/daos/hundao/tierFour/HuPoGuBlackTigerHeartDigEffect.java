package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.hundao.tierFour;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 虎魄蛊：技能·【黑虎掏心】(Black Tiger Heart Dig)。
 * <p>
 * 玩法定位：高爆发单体重击 + 长冷却。若击杀目标，获得“掏心”奖励（饱食与少量治疗 + 额外战利品）。
 * </p>
 */
public class HuPoGuBlackTigerHeartDigEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:hupogu_active_blacktigerheartdig";

    private static final String TAG_COOLDOWN_UNTIL_TICK =
        "guzhenrenext_hupogu_heart_dig_cd_until";

    private static final ResourceLocation SANZHUAN_LOOT_TABLE =
        ResourceLocation.parse("guzhenren:loot_table/sanzhuan");

    private static final double ZHENYUAN_SIZHUAN_YIJIE_DENOMINATOR = 16384.0;

    private static final double DEFAULT_ZHENYUAN_COST_SIZHUAN_YIJIE = 80.0;
    private static final double DEFAULT_SOUL_DAMAGE = 150.0;
    private static final float DEFAULT_PHYSICAL_DAMAGE = 150.0f;
    private static final float DEFAULT_CRIT_MULTIPLIER = 1.5f;
    private static final int DEFAULT_COOLDOWN_TICKS = 300;
    private static final double DEFAULT_RANGE = 4.5;

    private static final float TICKS_PER_SECOND = 20.0f;
    private static final double TARGET_DOT_THRESHOLD = 0.85;

    private static final double PARTICLE_HEIGHT_FACTOR = 0.5;
    private static final int CRIT_PARTICLE_COUNT = 18;
    private static final double CRIT_PARTICLE_SPREAD = 0.25;
    private static final double CRIT_PARTICLE_SPEED = 0.1;
    private static final int DAMAGE_INDICATOR_PARTICLE_COUNT = 10;
    private static final double DAMAGE_INDICATOR_SPREAD = 0.2;

    private static final int HEART_RESTORE_FOOD = 12;
    private static final float HEART_RESTORE_HEALTH_RATIO = 0.1f;

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public boolean onActivate(
        LivingEntity user,
        ItemStack stack,
        NianTouData.Usage usageInfo
    ) {
        if (user.level().isClientSide()) {
            return false;
        }
        if (!(user.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        int currentTick = user.tickCount;
        int cdUntil = getCooldownUntilTick(user);
        if (cdUntil > currentTick) {
            if (user instanceof ServerPlayer serverPlayer) {
                int remain = cdUntil - currentTick;
                serverPlayer.displayClientMessage(
                    Component.literal(
                        "黑虎掏心冷却中，剩余 " +
                            (remain / TICKS_PER_SECOND) +
                            " 秒"
                    ),
                    true
                );
            }
            return false;
        }

        LivingEntity target = findFrontTarget(user, usageInfo);
        if (target == null) {
            if (user instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(
                    Component.literal("前方无可掏心之敌"),
                    true
                );
            }
            return false;
        }

        double zhenyuanCost = getMetaDouble(
            usageInfo,
            "activate_zhenyuan_cost_sizhuan_yijie",
            DEFAULT_ZHENYUAN_COST_SIZHUAN_YIJIE
        );
        double baseCost = zhenyuanCost * ZHENYUAN_SIZHUAN_YIJIE_DENOMINATOR;
        double realCost = ZhenYuanHelper.calculateGuCost(user, baseCost);
        if (realCost > 0 && !ZhenYuanHelper.hasEnough(user, realCost)) {
            if (user instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(
                    Component.literal(
                        "真元不足：需要 " + zhenyuanCost + " 四转一阶真元"
                    ),
                    true
                );
            }
            return false;
        }

        int cooldownTicks = getMetaInt(
            usageInfo,
            "cooldown_ticks",
            DEFAULT_COOLDOWN_TICKS
        );
        if (cooldownTicks > 0) {
            setCooldownUntilTick(user, currentTick + cooldownTicks);
        }

        if (realCost > 0) {
            ZhenYuanHelper.modify(user, -realCost);
        }

        double soulDamage = getMetaDouble(
            usageInfo,
            "soul_damage",
            DEFAULT_SOUL_DAMAGE
        );
        float physicalDamage = getMetaFloat(
            usageInfo,
            "physical_damage",
            DEFAULT_PHYSICAL_DAMAGE
        );
        float critMultiplier = getMetaFloat(
            usageInfo,
            "crit_multiplier",
            DEFAULT_CRIT_MULTIPLIER
        );

        double lidaoMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.LI_DAO
        );
        double hunDaoMultiplier = DaoHenCalculator.calculateMultiplier(
            user,
            target,
            DaoHenHelper.DaoType.HUN_DAO
        );

        double finalSoulDamage =
            soulDamage * lidaoMultiplier * hunDaoMultiplier;
        float finalPhysicalDamage = (float) (physicalDamage *
            lidaoMultiplier *
            critMultiplier);

        boolean wasAlive = target.isAlive();
        applySoulDamage(target, finalSoulDamage);
        DamageSource source = user.damageSources().mobAttack(user);
        target.hurt(source, finalPhysicalDamage);

        serverLevel.sendParticles(
            ParticleTypes.CRIT,
            target.getX(),
            target.getY() + target.getBbHeight() * PARTICLE_HEIGHT_FACTOR,
            target.getZ(),
            CRIT_PARTICLE_COUNT,
            CRIT_PARTICLE_SPREAD,
            CRIT_PARTICLE_SPREAD,
            CRIT_PARTICLE_SPREAD,
            CRIT_PARTICLE_SPEED
        );
        serverLevel.sendParticles(
            ParticleTypes.DAMAGE_INDICATOR,
            target.getX(),
            target.getY() + target.getBbHeight() * PARTICLE_HEIGHT_FACTOR,
            target.getZ(),
            DAMAGE_INDICATOR_PARTICLE_COUNT,
            DAMAGE_INDICATOR_SPREAD,
            DAMAGE_INDICATOR_SPREAD,
            DAMAGE_INDICATOR_SPREAD,
            0.0
        );

        if (wasAlive && !target.isAlive()) {
            rewardHeartDig(user, serverLevel, target);
        }

        return true;
    }

    private static LivingEntity findFrontTarget(
        LivingEntity user,
        NianTouData.Usage usageInfo
    ) {
        double range = getMetaDouble(usageInfo, "range", DEFAULT_RANGE);
        Vec3 forward = user.getLookAngle().normalize();
        Vec3 origin = user.getEyePosition();

        AABB box = user
            .getBoundingBox()
            .expandTowards(forward.scale(range))
            .inflate(1.0, 1.0, 1.0);

        List<LivingEntity> candidates = user
            .level()
            .getEntitiesOfClass(LivingEntity.class, box);

        return candidates
            .stream()
            .filter(e -> e != user && e.isAlive())
            .filter(
                e -> origin.distanceToSqr(e.getEyePosition()) <= range * range
            )
            .filter(user::hasLineOfSight)
            .filter(e -> {
                Vec3 to = e.getEyePosition().subtract(origin).normalize();
                return to.dot(forward) > TARGET_DOT_THRESHOLD;
            })
            .min(
                Comparator.comparingDouble(e ->
                    origin.distanceToSqr(e.getEyePosition())
                )
            )
            .orElse(null);
    }

    private static void rewardHeartDig(
        LivingEntity user,
        ServerLevel serverLevel,
        LivingEntity deadTarget
    ) {
        if (user instanceof Player player) {
            player.getFoodData().eat(HEART_RESTORE_FOOD, 1.0f);
            player.heal(player.getMaxHealth() * HEART_RESTORE_HEALTH_RATIO);
        }

        ResourceKey<LootTable> lootKey = ResourceKey.create(
            Registries.LOOT_TABLE,
            SANZHUAN_LOOT_TABLE
        );
        LootTable table = serverLevel
            .getServer()
            .reloadableRegistries()
            .getLootTable(lootKey);
        LootParams params = new LootParams.Builder(serverLevel)
            .withParameter(LootContextParams.ORIGIN, deadTarget.position())
            .create(LootContextParamSets.CHEST);

        for (ItemStack loot : table.getRandomItems(params)) {
            Containers.dropItemStack(
                serverLevel,
                deadTarget.getX(),
                deadTarget.getY(),
                deadTarget.getZ(),
                loot
            );
        }
    }

    private static void applySoulDamage(LivingEntity target, double amount) {
        if (amount <= 0) {
            return;
        }
        HunPoHelper.modify(target, -amount);
        HunPoHelper.checkAndKill(target);
    }

    private static int getCooldownUntilTick(LivingEntity user) {
        CompoundTag tag = user.getPersistentData();
        return tag.getInt(TAG_COOLDOWN_UNTIL_TICK);
    }

    private static void setCooldownUntilTick(LivingEntity user, int tick) {
        CompoundTag tag = user.getPersistentData();
        if (tick <= 0) {
            tag.remove(TAG_COOLDOWN_UNTIL_TICK);
            return;
        }
        tag.putInt(TAG_COOLDOWN_UNTIL_TICK, tick);
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

    private static float getMetaFloat(
        NianTouData.Usage usage,
        String key,
        float defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Float.parseFloat(
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
