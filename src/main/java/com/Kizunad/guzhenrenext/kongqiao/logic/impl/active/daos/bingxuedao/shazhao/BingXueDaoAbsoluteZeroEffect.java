package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bingxuedao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.common.AbstractDomainDefenseEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.service.BingXueDaoDomainService;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.server.level.ServerPlayer;

public class BingXueDaoAbsoluteZeroEffect extends AbstractDomainDefenseEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_bing_xue_dao_absolute_zero";

    private static final float DOMAIN_ALPHA = 0.5F;
    private static final float DOMAIN_ROTATION_SPEED = 0.05F;

    private static final double DEFAULT_MAGIC_DAMAGE = 8.0;

    public BingXueDaoAbsoluteZeroEffect() {
        super(SHAZHAO_ID, "minecraft:textures/block/ice.png", DOMAIN_ALPHA, DOMAIN_ROTATION_SPEED);
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
    }

    @Override
    public boolean onActivate(final ServerPlayer player, final ShazhaoData data) {
        if (!super.onActivate(player, data)) {
            return false;
        }

        final double magicDamage = ShazhaoMetadataHelper.getDouble(
            data,
            getMetaKey("magic_damage_per_second"),
            DEFAULT_MAGIC_DAMAGE
        );
        player.getPersistentData().putDouble(
            BingXueDaoDomainService.KEY_DOMAIN_MAGIC_DAMAGE_PER_SECOND,
            magicDamage
        );
        return true;
    }
}
