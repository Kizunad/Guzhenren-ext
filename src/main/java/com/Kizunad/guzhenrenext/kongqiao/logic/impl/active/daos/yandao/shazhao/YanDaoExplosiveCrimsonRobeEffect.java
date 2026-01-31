package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yandao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.common.AbstractDomainDefenseEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.service.YanDaoDomainService;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.server.level.ServerPlayer;

public class YanDaoExplosiveCrimsonRobeEffect extends AbstractDomainDefenseEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_yan_dao_explosive_crimson_robe";

    private static final float DOMAIN_ALPHA = 0.5F;
    private static final float DOMAIN_ROTATION_SPEED = 0.5F;

    private static final double DEFAULT_EXPLOSION_RADIUS = 3.0;

    public YanDaoExplosiveCrimsonRobeEffect() {
        super(
            SHAZHAO_ID,
            "minecraft:textures/block/red_stained_glass.png",
            DOMAIN_ALPHA,
            DOMAIN_ROTATION_SPEED
        );
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
        return true;
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
}
