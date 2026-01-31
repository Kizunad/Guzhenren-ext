package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.fengdao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.common.AbstractDomainDefenseEffect;
import com.Kizunad.guzhenrenext.kongqiao.service.FengDaoDomainService;
import net.minecraft.server.level.ServerPlayer;

public class FengDaoInvisibleWindCloakEffect extends AbstractDomainDefenseEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_feng_dao_invisible_wind_cloak";

    private static final float DOMAIN_ALPHA = 0.3F;
    private static final float DOMAIN_ROTATION_SPEED = 2.0F;

    private static final double DEFAULT_KNOCKBACK = 0.8;

    public FengDaoInvisibleWindCloakEffect() {
        super(
            SHAZHAO_ID,
            "minecraft:textures/block/white_stained_glass.png",
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
            .putInt(FengDaoDomainService.KEY_DOMAIN_UNTIL_TICK, player.tickCount + duration);
        player.getPersistentData().putDouble(FengDaoDomainService.KEY_DOMAIN_RADIUS, radius);
        player.getPersistentData()
            .putDouble(FengDaoDomainService.KEY_DOMAIN_COST_PER_SECOND, costPerSecond);

        final double strength = enemyDamage > 0.0 ? enemyDamage : DEFAULT_KNOCKBACK;
        player.getPersistentData().putDouble(FengDaoDomainService.KEY_DOMAIN_KNOCKBACK, strength);
    }
}
