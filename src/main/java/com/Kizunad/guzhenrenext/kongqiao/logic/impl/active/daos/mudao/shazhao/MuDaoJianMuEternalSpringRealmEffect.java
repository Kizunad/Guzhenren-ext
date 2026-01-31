package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.mudao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.common.AbstractDomainDefenseEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.service.MuDaoDomainService;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.server.level.ServerPlayer;

public class MuDaoJianMuEternalSpringRealmEffect extends AbstractDomainDefenseEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_mu_dao_jian_mu_eternal_spring_realm";

    private static final float DOMAIN_ALPHA = 0.6F;
    private static final float DOMAIN_ROTATION_SPEED = 0.1F;

    private static final double DEFAULT_TRANSFER_RATIO = 0.5;
    private static final int DEFAULT_REGEN_AMPLIFIER = 4;

    public MuDaoJianMuEternalSpringRealmEffect() {
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
        player.getPersistentData()
            .putDouble(MuDaoDomainService.KEY_DOMAIN_COST_PER_SECOND, costPerSecond);
    }

    @Override
    public boolean onActivate(final ServerPlayer player, final ShazhaoData data) {
        if (!super.onActivate(player, data)) {
            return false;
        }

        final double transferRatio = ShazhaoMetadataHelper.getDouble(
            data,
            getMetaKey("damage_transfer_ratio"),
            DEFAULT_TRANSFER_RATIO
        );
        player.getPersistentData()
            .putDouble(MuDaoDomainService.KEY_DOMAIN_DAMAGE_TRANSFER_RATIO, transferRatio);

        final int regen = ShazhaoMetadataHelper.getInt(
            data,
            getMetaKey("regen_amplifier"),
            DEFAULT_REGEN_AMPLIFIER
        );
        player.getPersistentData().putInt(MuDaoDomainService.KEY_DOMAIN_REGEN_AMP, regen);
        return true;
    }
}
