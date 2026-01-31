package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.common.AbstractDomainDefenseEffect;
import com.Kizunad.guzhenrenext.kongqiao.service.ZhiDaoDomainService;
import net.minecraft.server.level.ServerPlayer;

public class ZhiDaoStarryChessBoardEffect extends AbstractDomainDefenseEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_zhi_dao_starry_chess_board";

    private static final float DOMAIN_ALPHA = 0.3F;
    private static final float DOMAIN_ROTATION_SPEED = 0.0F;

    public ZhiDaoStarryChessBoardEffect() {
        super(
            SHAZHAO_ID,
            "minecraft:textures/block/chiseled_quartz_block.png",
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
        player.getPersistentData().putInt(
            ZhiDaoDomainService.KEY_DOMAIN_UNTIL_TICK,
            player.tickCount + duration
        );
        player.getPersistentData().putDouble(ZhiDaoDomainService.KEY_DOMAIN_RADIUS, radius);
        player.getPersistentData().putDouble(
            ZhiDaoDomainService.KEY_DOMAIN_COST_PER_SECOND,
            costPerSecond
        );
    }
}
