package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common.JianDaoBoostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class JianDaoActiveSwordLightPierceEffect implements IGuEffect {

    private static final String KEY_PENDING_LIST =
        "GuzhenrenExtJianDaoLieJianPendingList";

    private static final String TAG_TARGET_UUID = "target_uuid";
    private static final String TAG_TRIGGER_TICK = "trigger_tick";
    private static final String TAG_DAMAGE = "damage";
    private static final String TAG_RADIUS = "radius";

    private static final String META_RANGE = "range";
    private static final String META_BEAM_RADIUS = "beam_radius";
    private static final String META_DAMAGE = "damage";
    private static final String META_EXPLOSION_DELAY_TICKS = "explosion_delay_ticks";
    private static final String META_EXPLOSION_DAMAGE = "explosion_damage";
    private static final String META_EXPLOSION_RADIUS = "explosion_radius";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double DEFAULT_RANGE = 32.0;
    private static final double DEFAULT_BEAM_RADIUS = 1.2;
    private static final int DEFAULT_EXPLOSION_DELAY_TICKS = 20 * 2;
    private static final double DEFAULT_EXPLOSION_RADIUS = 4.0;
    private static final int DEFAULT_COOLDOWN_TICKS = 0;

    private final String usageId;
    private final String nbtCooldownKey;

    public JianDaoActiveSwordLightPierceEffect(
        final String usageId,
        final String nbtCooldownKey
    ) {
        this.usageId = usageId;
        this.nbtCooldownKey = nbtCooldownKey;
    }

    @Override
    public String getUsageId() {
        return usageId;
    }

    @Override
    public boolean onActivate(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user == null) {
            return false;
        }
        if (user.level().isClientSide()) {
            return false;
        }
        if (!(user instanceof ServerPlayer player)) {
            return false;
        }

        final int remain = GuEffectCooldownHelper.getRemainingTicks(
            user,
            nbtCooldownKey
        );
        if (remain > 0) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                    "冷却中，剩余 " + remain + "t"
                ),
                true
            );
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(user, DaoHenHelper.DaoType.JIAN_DAO)
                * JianDaoBoostHelper.getJianXinMultiplier(user)
        );
        final double suiren = JianDaoBoostHelper.consumeSuiRenMultiplierIfActive(
            user
        );
        final double selfScale = DaoHenEffectScalingHelper.clampMultiplier(
            selfMultiplier * Math.max(1.0, suiren)
        );

        final double range = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RANGE, DEFAULT_RANGE)
        ) * Math.max(0.0, selfScale);
        final double beamRadius = Math.max(
            0.1,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_BEAM_RADIUS,
                DEFAULT_BEAM_RADIUS
            )
        );

        final Vec3 start = player.getEyePosition();
        final Vec3 end = start.add(player.getLookAngle().normalize().scale(range));

        final AABB box = new AABB(start, end).inflate(beamRadius);
        final List<LivingEntity> candidates = player.level().getEntitiesOfClass(
            LivingEntity.class,
            box,
            e -> e.isAlive() && e != user && !e.isAlliedTo(user)
        );

        final double baseDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_DAMAGE, 0.0)
        );
        final double baseExplosionDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_EXPLOSION_DAMAGE, 0.0)
        );
        final double explosionRadius = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_EXPLOSION_RADIUS,
                DEFAULT_EXPLOSION_RADIUS
            )
        );
        final int delayTicks = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_EXPLOSION_DELAY_TICKS,
                DEFAULT_EXPLOSION_DELAY_TICKS
            )
        );

        final int triggerTick = user.tickCount + delayTicks;
        final List<CompoundTag> pending = new ArrayList<>();

        for (LivingEntity target : candidates) {
            if (distanceToSegmentSqr(start, end, target.position()) > (beamRadius * beamRadius)) {
                continue;
            }

            final double dmgMultiplier = DaoHenCalculator.calculateMultiplier(
                user,
                target,
                DaoHenHelper.DaoType.JIAN_DAO
            ) * JianDaoBoostHelper.getJianXinMultiplier(user) * Math.max(1.0, suiren);

            if (baseDamage > 0.0) {
                target.hurt(
                    user.damageSources().playerAttack(player),
                    (float) (baseDamage * Math.max(0.0, dmgMultiplier))
                );
            }

            if (baseExplosionDamage > 0.0 && explosionRadius > 0.0 && delayTicks > 0) {
                final CompoundTag tag = new CompoundTag();
                tag.putUUID(TAG_TARGET_UUID, target.getUUID());
                tag.putInt(TAG_TRIGGER_TICK, triggerTick);
                tag.putDouble(TAG_DAMAGE, baseExplosionDamage * Math.max(0.0, dmgMultiplier));
                tag.putDouble(TAG_RADIUS, explosionRadius);
                pending.add(tag);
            }
        }

        if (!pending.isEmpty()) {
            final ListTag list = new ListTag();
            for (CompoundTag tag : pending) {
                list.add(tag);
            }
            user.getPersistentData().put(KEY_PENDING_LIST, list);
        }

        final int cooldownTicks = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_COOLDOWN_TICKS,
                DEFAULT_COOLDOWN_TICKS
            )
        );
        if (cooldownTicks > 0) {
            GuEffectCooldownHelper.setCooldownUntilTick(
                user,
                nbtCooldownKey,
                user.tickCount + cooldownTicks
            );
        }

        return true;
    }

    @Override
    public void onTick(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user == null) {
            return;
        }
        if (user.level().isClientSide()) {
            return;
        }
        if (!(user instanceof ServerPlayer player)) {
            return;
        }
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        if (!user.getPersistentData().contains(KEY_PENDING_LIST, Tag.TAG_LIST)) {
            return;
        }

        final ListTag list = user.getPersistentData().getList(
            KEY_PENDING_LIST,
            Tag.TAG_COMPOUND
        );
        if (list.isEmpty()) {
            user.getPersistentData().remove(KEY_PENDING_LIST);
            return;
        }

        final int now = user.tickCount;
        final ListTag remain = new ListTag();

        for (int i = 0; i < list.size(); i++) {
            final CompoundTag tag = list.getCompound(i);
            final int triggerTick = tag.getInt(TAG_TRIGGER_TICK);
            if (triggerTick > now) {
                remain.add(tag);
                continue;
            }
            if (!tag.hasUUID(TAG_TARGET_UUID)) {
                continue;
            }
            final UUID uuid = tag.getUUID(TAG_TARGET_UUID);
            final Entity entity = level.getEntity(uuid);
            if (!(entity instanceof LivingEntity center) || !center.isAlive()) {
                continue;
            }

            final double damage = Math.max(0.0, tag.getDouble(TAG_DAMAGE));
            final double radius = Math.max(0.0, tag.getDouble(TAG_RADIUS));
            if (damage <= 0.0 || radius <= 0.0) {
                continue;
            }

            final AABB box = center.getBoundingBox().inflate(radius);
            final List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                box,
                e -> e.isAlive() && e != user && !e.isAlliedTo(user)
            );
            for (LivingEntity target : targets) {
                target.hurt(
                    user.damageSources().playerAttack(player),
                    (float) damage
                );
            }
        }

        if (remain.isEmpty()) {
            user.getPersistentData().remove(KEY_PENDING_LIST);
            return;
        }
        user.getPersistentData().put(KEY_PENDING_LIST, remain);
    }

    @Override
    public void onUnequip(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user == null) {
            return;
        }
        if (user.level().isClientSide()) {
            return;
        }
        user.getPersistentData().remove(KEY_PENDING_LIST);
    }

    private static double distanceToSegmentSqr(
        final Vec3 a,
        final Vec3 b,
        final Vec3 p
    ) {
        final Vec3 ab = b.subtract(a);
        final double abLen2 = ab.lengthSqr();
        if (abLen2 <= 0.0) {
            return p.distanceToSqr(a);
        }
        final double t = UsageMetadataHelper.clamp(
            p.subtract(a).dot(ab) / abLen2,
            0.0,
            1.0
        );
        final Vec3 proj = a.add(ab.scale(t));
        return p.distanceToSqr(proj);
    }
}
