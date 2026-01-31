package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.tudao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.common.AbstractDomainDefenseEffect;
import com.Kizunad.guzhenrenext.kongqiao.service.TuDaoDomainService;
import net.minecraft.server.level.ServerPlayer;

public class TuDaoHeavyEarthFortressEffect extends AbstractDomainDefenseEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_tu_dao_heavy_earth_fortress";

    private static final float DOMAIN_ALPHA = 0.6F;
    private static final float DOMAIN_ROTATION_SPEED = 0.1F;

    public TuDaoHeavyEarthFortressEffect() {
        super(
            SHAZHAO_ID,
            "minecraft:textures/block/yellow_stained_glass.png",
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
            .putInt(TuDaoDomainService.KEY_DOMAIN_UNTIL_TICK, player.tickCount + duration);
        player.getPersistentData().putDouble(TuDaoDomainService.KEY_DOMAIN_RADIUS, radius);
        player.getPersistentData().putDouble(TuDaoDomainService.KEY_DOMAIN_REDUCTION, reduction);
        player.getPersistentData().putDouble(TuDaoDomainService.KEY_DOMAIN_ENEMY_DAMAGE, enemyDamage);
        player.getPersistentData()
            .putDouble(TuDaoDomainService.KEY_DOMAIN_COST_PER_SECOND, costPerSecond);
    }
}
