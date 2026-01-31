package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.lidao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.common.AbstractDomainDefenseEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.service.LiDaoDomainService;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.server.level.ServerPlayer;

public class LiDaoOverlordForceFieldEffect extends AbstractDomainDefenseEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_li_dao_overlord_force_field";

    private static final float DOMAIN_ALPHA = 0.5F;
    private static final float DOMAIN_ROTATION_SPEED = 0.1F;

    private static final double DEFAULT_REPEL_STRENGTH = 1.5;

    public LiDaoOverlordForceFieldEffect() {
        super(
            SHAZHAO_ID,
            "minecraft:textures/block/nether_bricks.png",
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
            .putInt(LiDaoDomainService.KEY_DOMAIN_UNTIL_TICK, player.tickCount + duration);
        player.getPersistentData().putDouble(LiDaoDomainService.KEY_DOMAIN_RADIUS, radius);
        player.getPersistentData()
            .putDouble(LiDaoDomainService.KEY_DOMAIN_COST_PER_SECOND, costPerSecond);
    }

    @Override
    public boolean onActivate(final ServerPlayer player, final ShazhaoData data) {
        if (!super.onActivate(player, data)) {
            return false;
        }

        final double strength = ShazhaoMetadataHelper.getDouble(
            data,
            getMetaKey("repel_strength"),
            DEFAULT_REPEL_STRENGTH
        );
        player.getPersistentData().putDouble(LiDaoDomainService.KEY_DOMAIN_REPEL_STRENGTH, strength);
        return true;
    }
}
