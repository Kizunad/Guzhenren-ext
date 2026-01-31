package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yandao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.common.AbstractDomainDefenseEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.service.YanDaoDomainService;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class YanDaoRedLotusKarmicRobeEffect extends AbstractDomainDefenseEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_yan_dao_red_lotus_karmic_robe";

    private static final float DOMAIN_ALPHA = 0.8F;
    private static final float DOMAIN_ROTATION_SPEED = 1.0F;

    private static final double DEFAULT_EXPLOSION_RADIUS = 6.0;
    private static final int DEFAULT_RESISTANCE_AMPLIFIER = 2;
    private static final int DEFAULT_DURATION_TICKS = 300;

    public YanDaoRedLotusKarmicRobeEffect() {
        super(
            SHAZHAO_ID,
            "minecraft:textures/block/fire_1.png",
            DOMAIN_ALPHA,
            DOMAIN_ROTATION_SPEED
        );
    }

    @Override
    protected void writeDomainData(
        final ServerPlayer player,
        final double radius,
        final int duration,
        final double reduction,
        final double enemyDamage,
        final double costPerSecond
    ) {
        player.getPersistentData()
            .putInt(YanDaoDomainService.KEY_DOMAIN_UNTIL_TICK, player.tickCount + duration);
        player.getPersistentData().putDouble(YanDaoDomainService.KEY_DOMAIN_RADIUS, radius);
        player.getPersistentData()
            .putDouble(YanDaoDomainService.KEY_DOMAIN_ENEMY_DAMAGE, enemyDamage);
        player.getPersistentData()
            .putDouble(YanDaoDomainService.KEY_DOMAIN_COST_PER_SECOND, costPerSecond);
    }

    @Override
    public boolean onActivate(final ServerPlayer player, final ShazhaoData data) {
        if (!super.onActivate(player, data)) {
            return false;
        }

        final double explosionRadius = ShazhaoMetadataHelper.getDouble(
            data,
            getMetaKey("explosion_radius"),
            DEFAULT_EXPLOSION_RADIUS
        );
        player.getPersistentData()
            .putDouble(YanDaoDomainService.KEY_DOMAIN_EXPLOSION_RADIUS, explosionRadius);

        final int resistance = ShazhaoMetadataHelper.getInt(
            data,
            getMetaKey("resistance_amplifier"),
            DEFAULT_RESISTANCE_AMPLIFIER
        );
        player.addEffect(
            new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE,
                ShazhaoMetadataHelper.getInt(data, getMetaKey("duration"), DEFAULT_DURATION_TICKS),
                resistance,
                false,
                false,
                true
            )
        );
        player.addEffect(
            new MobEffectInstance(
                MobEffects.FIRE_RESISTANCE,
                ShazhaoMetadataHelper.getInt(data, getMetaKey("duration"), DEFAULT_DURATION_TICKS),
                0,
                false,
                false,
                true
            )
        );
        return true;
    }
}
