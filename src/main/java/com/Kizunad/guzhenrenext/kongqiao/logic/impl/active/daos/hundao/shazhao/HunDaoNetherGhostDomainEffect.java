package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.hundao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.common.AbstractDomainDefenseEffect;
import com.Kizunad.guzhenrenext.kongqiao.service.HunDaoDomainService;
import net.minecraft.server.level.ServerPlayer;

public class HunDaoNetherGhostDomainEffect extends AbstractDomainDefenseEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_hun_dao_nether_ghost_domain";

    private static final float DOMAIN_ALPHA = 0.7F;
    private static final float DOMAIN_ROTATION_SPEED = 0.1F;

    public HunDaoNetherGhostDomainEffect() {
        super(SHAZHAO_ID, "minecraft:textures/block/obsidian.png", DOMAIN_ALPHA, DOMAIN_ROTATION_SPEED);
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
            .putInt(HunDaoDomainService.KEY_DOMAIN_UNTIL_TICK, player.tickCount + duration);
        player.getPersistentData().putDouble(HunDaoDomainService.KEY_DOMAIN_RADIUS, radius);
        player.getPersistentData()
            .putDouble(HunDaoDomainService.KEY_DOMAIN_ENEMY_DAMAGE, enemyDamage);
        player.getPersistentData()
            .putDouble(HunDaoDomainService.KEY_DOMAIN_COST_PER_SECOND, costPerSecond);
    }
}
