package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.mudao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.common.AbstractDomainDefenseEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.service.MuDaoDomainService;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.server.level.ServerPlayer;

public class MuDaoForestManifestationEffect extends AbstractDomainDefenseEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_mu_dao_forest_manifestation";

    private static final float DOMAIN_ALPHA = 0.5F;
    private static final float DOMAIN_ROTATION_SPEED = 0.2F;

    private static final double DEFAULT_LIFESTEAL_RATIO = 0.5;

    public MuDaoForestManifestationEffect() {
        super(
            SHAZHAO_ID,
            "minecraft:textures/block/lime_stained_glass.png",
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
            .putInt(MuDaoDomainService.KEY_DOMAIN_UNTIL_TICK, player.tickCount + duration);
        player.getPersistentData().putDouble(MuDaoDomainService.KEY_DOMAIN_RADIUS, radius);
        player.getPersistentData().putDouble(MuDaoDomainService.KEY_DOMAIN_ENEMY_DAMAGE, enemyDamage);
        player.getPersistentData()
            .putDouble(MuDaoDomainService.KEY_DOMAIN_COST_PER_SECOND, costPerSecond);
    }

    @Override
    public boolean onActivate(final ServerPlayer player, final ShazhaoData data) {
        if (!super.onActivate(player, data)) {
            return false;
        }

        final double lifesteal = ShazhaoMetadataHelper.getDouble(
            data,
            getMetaKey("lifesteal_ratio"),
            DEFAULT_LIFESTEAL_RATIO
        );
        player.getPersistentData().putDouble(MuDaoDomainService.KEY_DOMAIN_LIFESTEAL, lifesteal);
        return true;
    }
}
