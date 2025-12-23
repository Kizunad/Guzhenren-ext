package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoActiveEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.common.DaoCooldownKeys;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.PhysicalDamageSourceHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 血道主动杀招【血刃狂潮】：以自身为中心的范围普通伤害，并按命中数量提供吸血与夺魄续航。
 */
public class XueDaoShazhaoScarletRendingWaveEffect implements IShazhaoActiveEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_xue_dao_scarlet_rending_wave";

    private static final DaoHenHelper.DaoType DAO_TYPE =
        DaoHenHelper.DaoType.XUE_DAO;

    private static final String META_RADIUS = "radius";
    private static final String META_DAMAGE = "damage";
    private static final String META_KNOCKBACK_STRENGTH = "knockback_strength";
    private static final String META_SELF_HEAL_PER_HIT = "self_heal_per_hit";
    private static final String META_SELF_HUNPO_PER_HIT = "self_hunpo_per_hit";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double DEFAULT_RADIUS = 6.0;
    private static final double MIN_RADIUS = 1.0;
    private static final double DEFAULT_DAMAGE = 6500.0;
    private static final double DEFAULT_KNOCKBACK_STRENGTH = 0.6;
    private static final int DEFAULT_COOLDOWN_TICKS = 1200;

    private static final double MIN_VALUE = 0.0;
    private static final double MAX_DAMAGE_PER_TARGET = 80000.0;
    private static final double MAX_KNOCKBACK = 1.5;
    private static final double MIN_HORIZONTAL_VECTOR_SQR = 0.0001;
    private static final double VERTICAL_PUSH = 0.10;

    private static final double MAX_TOTAL_HEAL = 120.0;
    private static final double MAX_TOTAL_HUNPO = 120.0;

    private static final String COOLDOWN_KEY = DaoCooldownKeys.active(SHAZHAO_ID);

    @Override
    public String getShazhaoId() {
        return SHAZHAO_ID;
    }

    @Override
    public boolean onActivate(final ServerPlayer player, final ShazhaoData data) {
        if (player == null || data == null) {
            return false;
        }
        if (player.level().isClientSide()) {
            return false;
        }

        final int remain = GuEffectCooldownHelper.getRemainingTicks(
            player,
            COOLDOWN_KEY
        );
        if (remain > 0) {
            player.displayClientMessage(
                Component.literal("冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        if (!ShazhaoCostHelper.tryConsumeOnce(player, player, data)) {
            return false;
        }

        final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(player, DAO_TYPE)
        );
        final double baseRadius = Math.max(
            MIN_RADIUS,
            ShazhaoMetadataHelper.getDouble(data, META_RADIUS, DEFAULT_RADIUS)
        );
        final double radius = Math.max(
            MIN_RADIUS,
            baseRadius * Math.max(MIN_VALUE, selfMultiplier)
        );

        final double baseDamage = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(data, META_DAMAGE, DEFAULT_DAMAGE)
        );
        final double baseKnockback = ShazhaoMetadataHelper.clamp(
            Math.max(
                MIN_VALUE,
                ShazhaoMetadataHelper.getDouble(
                    data,
                    META_KNOCKBACK_STRENGTH,
                    DEFAULT_KNOCKBACK_STRENGTH
                )
            ),
            MIN_VALUE,
            MAX_KNOCKBACK
        );

        final double healPerHit = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_SELF_HEAL_PER_HIT,
                MIN_VALUE
            )
        );
        final double hunpoPerHit = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_SELF_HUNPO_PER_HIT,
                MIN_VALUE
            )
        );

        final AABB area = player.getBoundingBox().inflate(radius);
        final List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class,
            area,
            e -> e.isAlive() && e != player && !e.isAlliedTo(player)
        );

        double totalHeal = 0.0;
        double totalHunpo = 0.0;

        for (LivingEntity target : targets) {
            final double multiplier = DaoHenCalculator.calculateMultiplier(
                player,
                target,
                DAO_TYPE
            );
            final double damage = ShazhaoMetadataHelper.clamp(
                baseDamage * Math.max(MIN_VALUE, multiplier),
                MIN_VALUE,
                MAX_DAMAGE_PER_TARGET
            );
            if (damage > MIN_VALUE) {
                target.hurt(
                    PhysicalDamageSourceHelper.buildPhysicalDamageSource(player),
                    (float) damage
                );
            }

            final double knockback =
                baseKnockback * DaoHenEffectScalingHelper.clampMultiplier(multiplier);
            if (knockback > MIN_VALUE) {
                applyKnockback(player, target, knockback);
            }

            totalHeal += healPerHit;
            totalHunpo += hunpoPerHit;
        }

        restore(player, totalHeal, totalHunpo, selfMultiplier);

        final int cooldownTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_COOLDOWN_TICKS,
                DEFAULT_COOLDOWN_TICKS
            )
        );
        if (cooldownTicks > 0) {
            GuEffectCooldownHelper.setCooldownUntilTick(
                player,
                COOLDOWN_KEY,
                player.tickCount + cooldownTicks
            );
        }

        return true;
    }

    private static void restore(
        final ServerPlayer player,
        final double heal,
        final double hunpo,
        final double multiplier
    ) {
        final double scaledHeal = DaoHenEffectScalingHelper.scaleValue(
            heal,
            multiplier
        );
        final double scaledHunpo = DaoHenEffectScalingHelper.scaleValue(
            hunpo,
            multiplier
        );

        final double cappedHeal = Math.min(
            MAX_TOTAL_HEAL,
            Math.max(MIN_VALUE, scaledHeal)
        );
        final double cappedHunpo = Math.min(
            MAX_TOTAL_HUNPO,
            Math.max(MIN_VALUE, scaledHunpo)
        );

        if (cappedHeal > MIN_VALUE) {
            player.heal((float) cappedHeal);
        }
        if (cappedHunpo > MIN_VALUE) {
            HunPoHelper.modify(player, cappedHunpo);
        }
    }

    private static void applyKnockback(
        final LivingEntity user,
        final LivingEntity target,
        final double strength
    ) {
        final Vec3 delta = target.position().subtract(user.position());
        final Vec3 horizontal = new Vec3(delta.x, 0.0, delta.z);
        if (horizontal.lengthSqr() <= MIN_HORIZONTAL_VECTOR_SQR) {
            return;
        }
        final Vec3 dir = horizontal.normalize();
        target.push(dir.x * strength, VERTICAL_PUSH, dir.z * strength);
        target.hurtMarked = true;
    }
}
