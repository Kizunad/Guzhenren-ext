package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierFive;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * 五转：百鬼夜行蛊 - 百鬼夜行（被动）
 * <p>
 * 原著风味：夜色为幕，百鬼随行；并非召唤真实鬼物，而是以魂道手段牵引“鬼影”，扰乱敌魂。
 * </p>
 * <p>
 * 机制：
 * <ul>
 *   <li>夜晚或低光照环境才会运转；</li>
 *   <li>每秒消耗魂魄维持“夜行”状态；</li>
 *   <li>对附近怪物施加短促的恐惧扰乱（黑暗/虚弱/缓慢）；若其正锁定玩家，则额外造成魂魄伤害；</li>
 *   <li>魂道道痕越深，伤害越高，同时消耗也会随自我倍率增大。</li>
 * </ul>
 * </p>
 */
public class BaiGuiYeXingGuHundredGhostsMarchEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:baiguiyexinggu_passive_hundred_ghosts_march";

    private static final double DEFAULT_MAINTAIN_SOUL_COST_PER_SECOND = 1.20;
    private static final double DEFAULT_SOUL_COST_PER_TARGET = 0.30;

    private static final double DEFAULT_RADIUS = 6.0;
    private static final double DEFAULT_BASE_SOUL_DAMAGE_PER_SECOND = 56.0;

    private static final int DEFAULT_DEBUFF_DURATION_TICKS = 25;
    private static final int DEFAULT_SLOW_AMPLIFIER = 1;
    private static final int DEFAULT_WEAKNESS_AMPLIFIER = 0;
    private static final int DEFAULT_DARKNESS_DURATION_TICKS = 20;

    private static final int DEFAULT_LOW_LIGHT_THRESHOLD = 4;

    private static final int AURA_PARTICLE_COUNT = 18;
    private static final double AURA_PARTICLE_OFFSET = 0.35;
    private static final double AURA_PARTICLE_SPEED = 0.01;

    private static final int TARGET_PARTICLE_COUNT = 8;
    private static final double TARGET_PARTICLE_OFFSET = 0.25;
    private static final double TARGET_PARTICLE_SPEED = 0.02;

    private static final double DAO_HEN_DIVISOR = 1000.0;

    private static final double PARTICLE_HEIGHT_RATIO = 0.6;

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public void onSecond(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user.level().isClientSide()) {
            return;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(user);
        if (config != null && !config.isPassiveEnabled(USAGE_ID)) {
            return;
        }

        if (!(user instanceof Player player)) {
            return;
        }

        if (!shouldMarch(player, usageInfo)) {
            return;
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            player,
            DaoHenHelper.DaoType.HUN_DAO
        );
        final double maintainCost = calculateMaintainCost(
            usageInfo,
            selfMultiplier
        );
        if (HunPoHelper.getAmount(player) < maintainCost) {
            return;
        }
        if (maintainCost > 0.0) {
            HunPoHelper.modify(player, -maintainCost);
        }

        final MarchSettings settings = MarchSettings.from(
            usageInfo,
            selfMultiplier
        );
        if (settings.radius <= 0.0) {
            return;
        }

        final List<Monster> targets = findTargets(player, settings.radius);
        final int affected = applyMarchToTargets(player, targets, settings);

        if (affected > 0 || targets.isEmpty()) {
            spawnAuraParticles(player);
        }
    }

    @Override
    public void onUnequip(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {}

    private static boolean shouldMarch(
        final LivingEntity user,
        final NianTouData.Usage usageInfo
    ) {
        final Level level = user.level();
        final boolean isNight =
            level.dimensionType().hasSkyLight() && level.isNight();
        final int threshold = getMetaInt(
            usageInfo,
            "low_light_threshold",
            DEFAULT_LOW_LIGHT_THRESHOLD
        );
        final BlockPos pos = user.blockPosition();
        final int brightness = level.getMaxLocalRawBrightness(pos);
        final boolean lowLight = brightness <= threshold;
        return isNight || lowLight;
    }

    private static List<Monster> findTargets(
        final Player player,
        final double radius
    ) {
        final AABB area = player.getBoundingBox().inflate(radius);
        return player
            .level()
            .getEntitiesOfClass(
                Monster.class,
                area,
                entity -> entity != null && entity.isAlive()
            );
    }

    private static double calculateMaintainCost(
        final NianTouData.Usage usageInfo,
        final double selfMultiplier
    ) {
        final double maintainBase = getMetaDouble(
            usageInfo,
            "maintain_soul_cost_per_second",
            DEFAULT_MAINTAIN_SOUL_COST_PER_SECOND
        );
        return Math.max(0.0, maintainBase * selfMultiplier);
    }

    private static int applyMarchToTargets(
        final Player player,
        final List<Monster> targets,
        final MarchSettings settings
    ) {
        if (targets.isEmpty()) {
            return 0;
        }

        final double daoHen = DaoHenHelper.getDaoHen(
            player,
            DaoHenHelper.DaoType.HUN_DAO
        );
        final double extraScale = 1.0 + (daoHen / DAO_HEN_DIVISOR);
        final DamageSource hunpoDamageSource = createHunpoDamageSource(player);

        int affected = 0;
        for (Monster monster : targets) {
            if (monster == null || monster.isDeadOrDying()) {
                continue;
            }

            if (
                settings.perTargetCost > 0.0 &&
                HunPoHelper.getAmount(player) < settings.perTargetCost
            ) {
                break;
            }
            if (settings.perTargetCost > 0.0) {
                HunPoHelper.modify(player, -settings.perTargetCost);
            }

            applyDebuffs(monster, settings);
            trySoulBite(
                player,
                monster,
                settings,
                extraScale,
                hunpoDamageSource
            );

            affected++;
        }
        return affected;
    }

    private static void applyDebuffs(
        final Monster monster,
        final MarchSettings settings
    ) {
        monster.addEffect(
            new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                settings.debuffDurationTicks,
                settings.slowAmplifier,
                false,
                false,
                true
            )
        );
        monster.addEffect(
            new MobEffectInstance(
                MobEffects.WEAKNESS,
                settings.debuffDurationTicks,
                settings.weaknessAmplifier,
                false,
                false,
                true
            )
        );
        if (settings.darknessDurationTicks > 0) {
            monster.addEffect(
                new MobEffectInstance(
                    MobEffects.DARKNESS,
                    settings.darknessDurationTicks,
                    0,
                    false,
                    false,
                    true
                )
            );
        }
    }

    private static void trySoulBite(
        final Player player,
        final Monster monster,
        final MarchSettings settings,
        final double extraScale,
        final DamageSource hunpoDamageSource
    ) {
        if (!(monster instanceof Mob mob) || mob.getTarget() != player) {
            return;
        }

        final double multiplier = DaoHenCalculator.calculateMultiplier(
            player,
            monster,
            DaoHenHelper.DaoType.HUN_DAO
        );
        final float soulDamage = (float) (settings.baseSoulDamagePerSecond *
            multiplier *
            extraScale);
        if (soulDamage <= 0.0F) {
            return;
        }

        monster.hurt(hunpoDamageSource, soulDamage);
        spawnTargetParticles(monster);
    }

    private static void spawnAuraParticles(final Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.sendParticles(
            ParticleTypes.SOUL,
            player.getX(),
            player.getY() + player.getBbHeight() * PARTICLE_HEIGHT_RATIO,
            player.getZ(),
            AURA_PARTICLE_COUNT,
            AURA_PARTICLE_OFFSET,
            AURA_PARTICLE_OFFSET,
            AURA_PARTICLE_OFFSET,
            AURA_PARTICLE_SPEED
        );
    }

    private static void spawnTargetParticles(final LivingEntity target) {
        if (!(target.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.sendParticles(
            ParticleTypes.SOUL_FIRE_FLAME,
            target.getX(),
            target.getY() + target.getBbHeight() * PARTICLE_HEIGHT_RATIO,
            target.getZ(),
            TARGET_PARTICLE_COUNT,
            TARGET_PARTICLE_OFFSET,
            TARGET_PARTICLE_OFFSET,
            TARGET_PARTICLE_OFFSET,
            TARGET_PARTICLE_SPEED
        );
    }

    private static DamageSource createHunpoDamageSource(final Player attacker) {
        return new DamageSource(
            attacker
                .level()
                .registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(
                    ResourceKey.create(
                        Registries.DAMAGE_TYPE,
                        ResourceLocation.parse("guzhenren:hunpoxiaosuan")
                    )
                ),
            attacker
        );
    }

    private static double getMetaDouble(
        final NianTouData.Usage usageInfo,
        final String key,
        final double defaultValue
    ) {
        final Map<String, String> meta = usageInfo.metadata();
        if (meta == null) {
            return defaultValue;
        }
        final String value = meta.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static int getMetaInt(
        final NianTouData.Usage usageInfo,
        final String key,
        final int defaultValue
    ) {
        final Map<String, String> meta = usageInfo.metadata();
        if (meta == null) {
            return defaultValue;
        }
        final String value = meta.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static final class MarchSettings {

        private final double radius;
        private final double perTargetCost;
        private final double baseSoulDamagePerSecond;
        private final int debuffDurationTicks;
        private final int slowAmplifier;
        private final int weaknessAmplifier;
        private final int darknessDurationTicks;

        private MarchSettings(
            final double radius,
            final double perTargetCost,
            final double baseSoulDamagePerSecond,
            final int debuffDurationTicks,
            final int slowAmplifier,
            final int weaknessAmplifier,
            final int darknessDurationTicks
        ) {
            this.radius = radius;
            this.perTargetCost = perTargetCost;
            this.baseSoulDamagePerSecond = baseSoulDamagePerSecond;
            this.debuffDurationTicks = debuffDurationTicks;
            this.slowAmplifier = slowAmplifier;
            this.weaknessAmplifier = weaknessAmplifier;
            this.darknessDurationTicks = darknessDurationTicks;
        }

        private static MarchSettings from(
            final NianTouData.Usage usageInfo,
            final double selfMultiplier
        ) {
            final double radius = Math.max(
                0.0,
                getMetaDouble(usageInfo, "radius", DEFAULT_RADIUS)
            );

            final double perTargetBaseCost = getMetaDouble(
                usageInfo,
                "soul_cost_per_target",
                DEFAULT_SOUL_COST_PER_TARGET
            );
            final double perTargetCost = Math.max(
                0.0,
                perTargetBaseCost * selfMultiplier
            );

            final double baseSoulDamagePerSecond = Math.max(
                0.0,
                getMetaDouble(
                    usageInfo,
                    "base_soul_damage_per_second",
                    DEFAULT_BASE_SOUL_DAMAGE_PER_SECOND
                )
            );

            final int debuffDurationTicks = Math.max(
                1,
                getMetaInt(
                    usageInfo,
                    "debuff_duration_ticks",
                    DEFAULT_DEBUFF_DURATION_TICKS
                )
            );
            final int slowAmplifier = Math.max(
                0,
                getMetaInt(usageInfo, "slow_amplifier", DEFAULT_SLOW_AMPLIFIER)
            );
            final int weaknessAmplifier = Math.max(
                0,
                getMetaInt(
                    usageInfo,
                    "weakness_amplifier",
                    DEFAULT_WEAKNESS_AMPLIFIER
                )
            );
            final int darknessDurationTicks = Math.max(
                0,
                getMetaInt(
                    usageInfo,
                    "darkness_duration_ticks",
                    DEFAULT_DARKNESS_DURATION_TICKS
                )
            );

            return new MarchSettings(
                radius,
                perTargetCost,
                baseSoulDamagePerSecond,
                debuffDurationTicks,
                slowAmplifier,
                weaknessAmplifier,
                darknessDurationTicks
            );
        }
    }
}
