package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.leidao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.common.AbstractDomainDefenseEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.service.LeiDaoDomainService;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.server.level.ServerPlayer;

public class LeiDaoThunderPoolForbiddenGroundEffect extends AbstractDomainDefenseEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_lei_dao_thunder_pool_forbidden_ground";

    private static final float DOMAIN_ALPHA = 0.7F;
    private static final float DOMAIN_ROTATION_SPEED = 4.0F;

    private static final int DEFAULT_STUN_DURATION = 20;
    private static final int DEFAULT_LIGHTNING_INTERVAL = 20;

    public LeiDaoThunderPoolForbiddenGroundEffect() {
        super(
            SHAZHAO_ID,
            "minecraft:textures/block/purple_stained_glass.png",
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
            .putInt(LeiDaoDomainService.KEY_DOMAIN_UNTIL_TICK, player.tickCount + duration);
        player.getPersistentData().putDouble(LeiDaoDomainService.KEY_DOMAIN_RADIUS, radius);
        player.getPersistentData()
            .putDouble(LeiDaoDomainService.KEY_DOMAIN_COST_PER_SECOND, costPerSecond);
    }

    @Override
    public boolean onActivate(final ServerPlayer player, final ShazhaoData data) {
        if (!super.onActivate(player, data)) {
            return false;
        }

        final int stunDuration = ShazhaoMetadataHelper.getInt(
            data,
            getMetaKey("stun_duration"),
            DEFAULT_STUN_DURATION
        );
        player.getPersistentData()
            .putInt(LeiDaoDomainService.KEY_DOMAIN_STUN_DURATION, stunDuration);

        final int interval = ShazhaoMetadataHelper.getInt(
            data,
            getMetaKey("lightning_interval"),
            DEFAULT_LIGHTNING_INTERVAL
        );
        player.getPersistentData()
            .putInt(LeiDaoDomainService.KEY_DOMAIN_LIGHTNING_INTERVAL, interval);
        return true;
    }
}
