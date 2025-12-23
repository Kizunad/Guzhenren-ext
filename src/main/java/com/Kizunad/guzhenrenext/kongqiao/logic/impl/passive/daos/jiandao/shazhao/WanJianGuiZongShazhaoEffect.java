package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
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
 * 剑道主动杀招：万剑归宗。
 * <p>
 * 以自身为中心剑气爆发：大范围普通伤害 + 击退；命中后提供少量续航与护体。
 * </p>
 */
public class WanJianGuiZongShazhaoEffect implements IShazhaoActiveEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_wan_jian_gui_zong";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.JIAN_DAO;

    private static final String META_RADIUS = "radius";
    private static final String META_DAMAGE = "damage";
    private static final String META_KNOCKBACK_STRENGTH = "knockback_strength";
    private static final String META_SELF_ABSORPTION_PER_HIT =
        "self_absorption_per_hit";
    private static final String META_SELF_ABSORPTION_MAX = "self_absorption_max";

    private static final String META_SELF_ZHENYUAN_PER_HIT = "self_zhenyuan_per_hit";
    private static final String META_SELF_NIANTOU_PER_HIT = "self_niantou_per_hit";
    private static final String META_SELF_JINGLI_PER_HIT = "self_jingli_per_hit";
    private static final String META_SELF_HUNPO_PER_HIT = "self_hunpo_per_hit";

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double DEFAULT_RADIUS = 8.0;
    private static final double MIN_RADIUS = 1.0;
    private static final double DEFAULT_DAMAGE = 10000.0;
    private static final double DEFAULT_KNOCKBACK_STRENGTH = 0.55;

    private static final double DEFAULT_ABSORPTION_PER_HIT = 1.6;
    private static final double DEFAULT_ABSORPTION_MAX = 18.0;

    private static final double DEFAULT_SELF_ZHENYUAN_PER_HIT = 18.0;
    private static final double DEFAULT_SELF_NIANTOU_PER_HIT = 4.0;
    private static final double DEFAULT_SELF_JINGLI_PER_HIT = 4.0;
    private static final double DEFAULT_SELF_HUNPO_PER_HIT = 2.5;

    private static final double MIN_VALUE = 0.0;
    private static final double MAX_DAMAGE_PER_TARGET = 60000.0;
    private static final double MAX_KNOCKBACK_STRENGTH = 1.6;
    private static final double MIN_HORIZONTAL_VECTOR_SQR = 0.0001;
    private static final double VERTICAL_PUSH = 0.06;

    private static final double MAX_TOTAL_ZHENYUAN_RESTORE = 220.0;
    private static final double MAX_TOTAL_NIANTOU_RESTORE = 60.0;
    private static final double MAX_TOTAL_JINGLI_RESTORE = 80.0;
    private static final double MAX_TOTAL_HUNPO_RESTORE = 50.0;

    private static final double MAX_ABSORPTION_MAX = 60.0;

    private static final int DEFAULT_COOLDOWN_TICKS = 3600;

    private static final String COOLDOWN_KEY = DaoCooldownKeys.active(SHAZHAO_ID);

    private record CombatSpec(double damage, double knockbackStrength) {}

    private record SustainSpec(
        double absorptionPerHit,
        double absorptionMax,
        double zhenyuanPerHit,
        double niantouPerHit,
        double jingliPerHit,
        double hunpoPerHit
    ) {}

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

        final int remain = GuEffectCooldownHelper.getRemainingTicks(player, COOLDOWN_KEY);
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

        final CombatSpec combatSpec = readCombatSpec(data);
        final SustainSpec sustainSpec = readSustainSpec(data);

        final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(player, DAO_TYPE)
        );

        final double baseRadius = Math.max(
            MIN_RADIUS,
            ShazhaoMetadataHelper.getDouble(data, META_RADIUS, DEFAULT_RADIUS)
        );
        final double radius = baseRadius * selfMultiplier;

        final AABB area = player.getBoundingBox().inflate(radius);
        final List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class,
            area,
            e -> e.isAlive() && e != player && !e.isAlliedTo(player)
        );

        int hits = 0;
        for (LivingEntity target : targets) {
            final double multiplier = DaoHenCalculator.calculateMultiplier(player, target, DAO_TYPE);

            final double damage = ShazhaoMetadataHelper.clamp(
                combatSpec.damage() * Math.max(MIN_VALUE, multiplier),
                MIN_VALUE,
                MAX_DAMAGE_PER_TARGET
            );
            if (damage > MIN_VALUE) {
                target.hurt(
                    PhysicalDamageSourceHelper.buildPhysicalDamageSource(player),
                    (float) damage
                );
                hits += 1;
            }

            final double scaledKnockback =
                combatSpec.knockbackStrength()
                    * DaoHenEffectScalingHelper.clampMultiplier(multiplier);
            applyKnockback(player.position(), target, scaledKnockback);
        }

        if (hits > 0) {
            applySustainAndShield(player, hits, selfMultiplier, sustainSpec);
        }

        final int cooldownTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, META_COOLDOWN_TICKS, DEFAULT_COOLDOWN_TICKS)
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

    private static CombatSpec readCombatSpec(final ShazhaoData data) {
        final double damage = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(data, META_DAMAGE, DEFAULT_DAMAGE)
        );
        final double knockback = ShazhaoMetadataHelper.clamp(
            Math.max(
                MIN_VALUE,
                ShazhaoMetadataHelper.getDouble(
                    data,
                    META_KNOCKBACK_STRENGTH,
                    DEFAULT_KNOCKBACK_STRENGTH
                )
            ),
            MIN_VALUE,
            MAX_KNOCKBACK_STRENGTH
        );
        return new CombatSpec(damage, knockback);
    }

    private static SustainSpec readSustainSpec(final ShazhaoData data) {
        final double absorptionPerHit = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_SELF_ABSORPTION_PER_HIT,
                DEFAULT_ABSORPTION_PER_HIT
            )
        );
        final double absorptionMax = ShazhaoMetadataHelper.clamp(
            Math.max(
                MIN_VALUE,
                ShazhaoMetadataHelper.getDouble(
                    data,
                    META_SELF_ABSORPTION_MAX,
                    DEFAULT_ABSORPTION_MAX
                )
            ),
            MIN_VALUE,
            MAX_ABSORPTION_MAX
        );
        final double zhenyuanPerHit = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_SELF_ZHENYUAN_PER_HIT,
                DEFAULT_SELF_ZHENYUAN_PER_HIT
            )
        );
        final double niantouPerHit = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_SELF_NIANTOU_PER_HIT,
                DEFAULT_SELF_NIANTOU_PER_HIT
            )
        );
        final double jingliPerHit = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_SELF_JINGLI_PER_HIT,
                DEFAULT_SELF_JINGLI_PER_HIT
            )
        );
        final double hunpoPerHit = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_SELF_HUNPO_PER_HIT,
                DEFAULT_SELF_HUNPO_PER_HIT
            )
        );
        return new SustainSpec(
            absorptionPerHit,
            absorptionMax,
            zhenyuanPerHit,
            niantouPerHit,
            jingliPerHit,
            hunpoPerHit
        );
    }

    private static void applySustainAndShield(
        final ServerPlayer player,
        final int hits,
        final double selfMultiplier,
        final SustainSpec spec
    ) {
        if (spec == null) {
            return;
        }
        final double scaledAbsorptionPerHit = DaoHenEffectScalingHelper.scaleValue(
            spec.absorptionPerHit(),
            selfMultiplier
        );
        final double addAbsorption = Math.max(MIN_VALUE, scaledAbsorptionPerHit) * Math.max(0, hits);
        if (addAbsorption > MIN_VALUE && spec.absorptionMax() > MIN_VALUE) {
            final float next = (float) ShazhaoMetadataHelper.clamp(
                player.getAbsorptionAmount() + addAbsorption,
                MIN_VALUE,
                spec.absorptionMax()
            );
            player.setAbsorptionAmount(next);
        }

        final double zhenyuanRestore = ShazhaoMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(spec.zhenyuanPerHit(), selfMultiplier) * Math.max(0, hits),
            MIN_VALUE,
            MAX_TOTAL_ZHENYUAN_RESTORE
        );
        final double nianTouRestore = ShazhaoMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(spec.niantouPerHit(), selfMultiplier) * Math.max(0, hits),
            MIN_VALUE,
            MAX_TOTAL_NIANTOU_RESTORE
        );
        final double jingLiRestore = ShazhaoMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(spec.jingliPerHit(), selfMultiplier) * Math.max(0, hits),
            MIN_VALUE,
            MAX_TOTAL_JINGLI_RESTORE
        );
        final double hunPoRestore = ShazhaoMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(spec.hunpoPerHit(), selfMultiplier) * Math.max(0, hits),
            MIN_VALUE,
            MAX_TOTAL_HUNPO_RESTORE
        );

        if (zhenyuanRestore > MIN_VALUE) {
            ZhenYuanHelper.modify(player, zhenyuanRestore);
        }
        if (nianTouRestore > MIN_VALUE) {
            NianTouHelper.modify(player, nianTouRestore);
        }
        if (jingLiRestore > MIN_VALUE) {
            JingLiHelper.modify(player, jingLiRestore);
        }
        if (hunPoRestore > MIN_VALUE) {
            HunPoHelper.modify(player, hunPoRestore);
        }
    }

    private static void applyKnockback(
        final Vec3 center,
        final LivingEntity target,
        final double strength
    ) {
        if (center == null || target == null || strength <= MIN_VALUE) {
            return;
        }
        final Vec3 delta = target.position().subtract(center);
        final Vec3 horizontal = new Vec3(delta.x, 0.0, delta.z);
        if (horizontal.lengthSqr() <= MIN_HORIZONTAL_VECTOR_SQR) {
            return;
        }
        final Vec3 dir = horizontal.normalize();
        target.push(dir.x * strength, VERTICAL_PUSH, dir.z * strength);
        target.hurtMarked = true;
    }
}
