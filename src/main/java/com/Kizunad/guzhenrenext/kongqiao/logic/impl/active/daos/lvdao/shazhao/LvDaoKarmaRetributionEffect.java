package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.lvdao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.common.AbstractDomainDefenseEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.service.LvDaoDomainService;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.server.level.ServerPlayer;

public class LvDaoKarmaRetributionEffect extends AbstractDomainDefenseEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_lv_dao_karma_retribution";

    private static final float DOMAIN_ALPHA = 0.5F;
    private static final float DOMAIN_ROTATION_SPEED = 0.0F;

    private static final double DEFAULT_REFLECT_RATIO = 1.0;

    public LvDaoKarmaRetributionEffect() {
        super(SHAZHAO_ID, "minecraft:textures/block/bedrock.png", DOMAIN_ALPHA, DOMAIN_ROTATION_SPEED);
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
            .putInt(LvDaoDomainService.KEY_DOMAIN_UNTIL_TICK, player.tickCount + duration);
        player.getPersistentData().putDouble(LvDaoDomainService.KEY_DOMAIN_RADIUS, radius);
        player.getPersistentData()
            .putDouble(LvDaoDomainService.KEY_DOMAIN_COST_PER_SECOND, costPerSecond);
    }

    @Override
    public boolean onActivate(final ServerPlayer player, final ShazhaoData data) {
        if (!super.onActivate(player, data)) {
            return false;
        }

        final double reflect = ShazhaoMetadataHelper.getDouble(
            data,
            getMetaKey("reflect_ratio"),
            DEFAULT_REFLECT_RATIO
        );
        player.getPersistentData()
            .putDouble(LvDaoDomainService.KEY_DOMAIN_REFLECT_RATIO, reflect);
        return true;
    }
}
