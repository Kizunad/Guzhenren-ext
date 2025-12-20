package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.hundao.tierThree;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.Objects;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 羚魂蛊：主动【羚羊挂角】。
 * <p>
 * 右键开启短暂招架姿态（当前实现由“技能轮盘/主动触发”驱动，保持系统一致性）。
 * 在招架窗口内若遭受近战攻击：自动瞬移至攻击者身后，并进行一次无视护甲的戳刺，
 * 同时附带魂魄伤害（魂道道痕增幅）。
 * </p>
 */
public class LingHunGuAntelopeHangsHornsEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:linghungu_active_antelope_hangs_horns";

    private static final ResourceKey<DamageType> ANTELOPE_PIERCE_DAMAGE_TYPE =
        ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(
                GuzhenrenExt.MODID,
                "antelope_pierce"
            )
        );

    private static final int DEFAULT_PARRY_WINDOW_TICKS = 10; // 0.5s
    private static final double DEFAULT_TELEPORT_BACK_DISTANCE = 1.2;
    private static final float DEFAULT_PHYSICAL_DAMAGE = 100.0F;
    private static final double DEFAULT_SOUL_DAMAGE = 100.0;

    private static final String NBT_PARRY_UNTIL_GAME_TIME = "LingHunGuParryUntilGameTime";
    private static final float ACTIVATE_SOUND_VOLUME = 0.6F;
    private static final float ACTIVATE_SOUND_PITCH = 1.6F;
    private static final float COUNTER_SOUND_VOLUME = 0.9F;
    private static final float COUNTER_SOUND_PITCH = 1.2F;

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
        if (!(user instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(user);
        if (config != null && !config.isInWheel(USAGE_ID)) {
            serverPlayer.displayClientMessage(
                Component.literal("羚羊挂角未加入轮盘，无法触发"),
                true
            );
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(serverPlayer, user, usageInfo)) {
            return false;
        }

        final int windowTicks = getMetaInt(
            usageInfo,
            "parry_window_ticks",
            DEFAULT_PARRY_WINDOW_TICKS
        );
        final long untilGameTime = user.level().getGameTime() + windowTicks;
        serverPlayer.getPersistentData().putLong(
            NBT_PARRY_UNTIL_GAME_TIME,
            untilGameTime
        );

        if (user.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(
                null,
                serverPlayer.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS,
                ACTIVATE_SOUND_VOLUME,
                ACTIVATE_SOUND_PITCH
            );
        }
        return true;
    }

    @Override
    public float onHurt(
        LivingEntity victim,
        DamageSource source,
        float damage,
        ItemStack stack,
        NianTouData.Usage usageInfo
    ) {
        if (victim.level().isClientSide()) {
            return damage;
        }
        if (!(victim instanceof ServerPlayer serverPlayer)) {
            return damage;
        }

        if (!isParryWindowActive(serverPlayer)) {
            return damage;
        }

        final LivingEntity attacker = resolveMeleeAttacker(source);
        if (attacker == null) {
            return damage;
        }

        // 招架成功：清空窗口，避免同一窗口多次触发。
        serverPlayer.getPersistentData().putLong(NBT_PARRY_UNTIL_GAME_TIME, 0L);

        counterAttack(serverPlayer, attacker, usageInfo);
        return 0.0F;
    }

    @Override
    public void onUnequip(
        LivingEntity user,
        ItemStack stack,
        NianTouData.Usage usageInfo
    ) {
        if (user instanceof ServerPlayer serverPlayer) {
            serverPlayer.getPersistentData().putLong(NBT_PARRY_UNTIL_GAME_TIME, 0L);
        }
    }

    private static boolean isParryWindowActive(final ServerPlayer player) {
        final long until = player.getPersistentData().getLong(NBT_PARRY_UNTIL_GAME_TIME);
        return until > 0 && player.level().getGameTime() <= until;
    }

    private static LivingEntity resolveMeleeAttacker(final DamageSource source) {
        if (!(source.getEntity() instanceof LivingEntity attacker)) {
            return null;
        }
        // 只处理近战：直接伤害实体必须就是攻击者自己，且排除投射物等标签。
        if (source.getDirectEntity() != attacker) {
            return null;
        }
        if (source.is(DamageTypeTags.IS_PROJECTILE)) {
            return null;
        }
        return attacker;
    }

    private static void counterAttack(
        final ServerPlayer defender,
        final LivingEntity attacker,
        final NianTouData.Usage usageInfo
    ) {
        final double backDistance = getMetaDouble(
            usageInfo,
            "teleport_back_distance",
            DEFAULT_TELEPORT_BACK_DISTANCE
        );
        teleportBehind(defender, attacker, backDistance);

        // 1) 无视护甲的戳刺（数据驱动 DamageType：bypasses_armor=true）
        final float physicalDamage = getMetaFloat(
            usageInfo,
            "physical_damage",
            DEFAULT_PHYSICAL_DAMAGE
        );
        final DamageSource pierce = defender
            .damageSources()
            .source(ANTELOPE_PIERCE_DAMAGE_TYPE, defender);
        attacker.hurt(pierce, physicalDamage);

        // 2) 魂魄伤害：吃魂道道痕增幅
        final double baseSoulDamage = getMetaDouble(
            usageInfo,
            "soul_damage",
            DEFAULT_SOUL_DAMAGE
        );
        final double hunDaoMultiplier = DaoHenCalculator.calculateMultiplier(
            defender,
            attacker,
            DaoHenHelper.DaoType.HUN_DAO
        );
        applySoulDamage(defender, attacker, baseSoulDamage * hunDaoMultiplier);

        // 3) 视觉/听觉反馈（服务端广播给周围玩家）
        if (defender.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(
                null,
                defender.blockPosition(),
                SoundEvents.PLAYER_ATTACK_CRIT,
                SoundSource.PLAYERS,
                COUNTER_SOUND_VOLUME,
                COUNTER_SOUND_PITCH
            );
        }
    }

    private static void teleportBehind(
        final ServerPlayer defender,
        final LivingEntity attacker,
        final double backDistance
    ) {
        final Vec3 forward = Vec3.directionFromRotation(0.0F, attacker.getYRot());
        final Vec3 target = attacker.position().add(forward.scale(-backDistance));

        final Vec3 safe = findSafeTeleportPos(defender, target);
        defender.teleportTo(safe.x, safe.y, safe.z);

        // 强制朝向攻击者眼部，避免“戳刺”错位观感。
        defender.lookAt(EntityAnchorArgument.Anchor.EYES, attacker.getEyePosition());
    }

    private static Vec3 findSafeTeleportPos(final ServerPlayer defender, final Vec3 base) {
        final ServerLevel level = Objects.requireNonNull(defender.serverLevel());
        final double[] yOffsets = new double[] {0.0, 0.5, 1.0};

        for (double yOffset : yOffsets) {
            final Vec3 candidate = base.add(0.0, yOffset, 0.0);
            final AABB moved = defender.getBoundingBox().move(
                candidate.x - defender.getX(),
                candidate.y - defender.getY(),
                candidate.z - defender.getZ()
            );
            if (!level.noCollision(defender, moved)) {
                continue;
            }
            final BlockPos pos = BlockPos.containing(candidate);
            if (!level.getWorldBorder().isWithinBounds(pos)) {
                continue;
            }
            return candidate;
        }
        return defender.position();
    }

    private static void applySoulDamage(
        final LivingEntity attacker,
        final LivingEntity target,
        final double amount
    ) {
        if (amount <= 0) {
            return;
        }
        final double targetSoul = HunPoHelper.getAmount(target);
        if (targetSoul > 0) {
            HunPoHelper.modify(target, -amount);
            HunPoHelper.checkAndKill(target);
        } else {
            target.hurt(attacker.damageSources().mobAttack(attacker), (float) amount);
        }
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

    private static int getMetaInt(
        final NianTouData.Usage usage,
        final String key,
        final int defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Integer.parseInt(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    private static float getMetaFloat(
        final NianTouData.Usage usage,
        final String key,
        final float defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Float.parseFloat(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }
}
