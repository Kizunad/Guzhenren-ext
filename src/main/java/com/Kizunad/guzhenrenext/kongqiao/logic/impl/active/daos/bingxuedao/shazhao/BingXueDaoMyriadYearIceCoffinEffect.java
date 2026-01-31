package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bingxuedao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.common.AbstractDomainDefenseEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.service.BingXueDaoDomainService;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.server.level.ServerPlayer;

public class BingXueDaoMyriadYearIceCoffinEffect extends AbstractDomainDefenseEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_bing_xue_dao_myriad_year_ice_coffin";

    private static final float DOMAIN_ALPHA = 0.9F;
    private static final float DOMAIN_ROTATION_SPEED = 0.0F;

    private static final double DEFAULT_SELF_HEAL = 10.0;
    private static final double DEFAULT_ENEMY_DAMAGE = 20.0;

    public BingXueDaoMyriadYearIceCoffinEffect() {
        super(SHAZHAO_ID, "minecraft:textures/block/blue_ice.png", DOMAIN_ALPHA, DOMAIN_ROTATION_SPEED);
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
            .putInt(BingXueDaoDomainService.KEY_DOMAIN_UNTIL_TICK, player.tickCount + duration);
        player.getPersistentData().putDouble(BingXueDaoDomainService.KEY_DOMAIN_RADIUS, radius);
        player.getPersistentData()
            .putDouble(
                BingXueDaoDomainService.KEY_DOMAIN_ZHENYUAN_BASE_COST_PER_SECOND,
                costPerSecond
            );
        player.getPersistentData().putDouble(BingXueDaoDomainService.KEY_DOMAIN_ENEMY_DAMAGE, enemyDamage);
    }

    @Override
    public boolean onActivate(final ServerPlayer player, final ShazhaoData data) {
        if (!super.onActivate(player, data)) {
            return false;
        }

        final double selfHeal = ShazhaoMetadataHelper.getDouble(
            data,
            getMetaKey("self_heal"),
            DEFAULT_SELF_HEAL
        );
        player.getPersistentData()
            .putDouble(BingXueDaoDomainService.KEY_DOMAIN_SELF_HEAL, selfHeal);
        return true;
    }
}
