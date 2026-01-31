package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.fengdao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.common.AbstractDomainDefenseEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.service.FengDaoDomainService;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.server.level.ServerPlayer;

public class FengDaoNineHeavensAstralWindEffect extends AbstractDomainDefenseEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_feng_dao_nine_heavens_astral_wind";

    private static final float DOMAIN_ALPHA = 0.6F;
    private static final float DOMAIN_ROTATION_SPEED = 5.0F;

    private static final double DEFAULT_KNOCKBACK = 3.0;

    public FengDaoNineHeavensAstralWindEffect() {
        super(
            SHAZHAO_ID,
            "minecraft:textures/block/blue_stained_glass.png",
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
    }

    @Override
    public boolean onActivate(final ServerPlayer player, final ShazhaoData data) {
        if (!super.onActivate(player, data)) {
            return false;
        }

        final double knockback = ShazhaoMetadataHelper.getDouble(
            data,
            getMetaKey("knockback_strength"),
            DEFAULT_KNOCKBACK
        );
        player.getPersistentData().putDouble(FengDaoDomainService.KEY_DOMAIN_KNOCKBACK, knockback);
        return true;
    }
}
