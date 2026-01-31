package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.common.AbstractDomainDefenseEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.service.ZhiDaoDomainService;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.server.level.ServerPlayer;

public class ZhiDaoHeavenlySecretCalculationEffect extends AbstractDomainDefenseEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_zhi_dao_heavenly_secret_calculation";

    private static final float DOMAIN_ALPHA = 0.4F;
    private static final float DOMAIN_ROTATION_SPEED = 0.5F;

    private static final double DEFAULT_DODGE_CHANCE = 0.8;
    private static final double DEFAULT_COUNTER_DAMAGE = 20.0;

    public ZhiDaoHeavenlySecretCalculationEffect() {
        super(
            SHAZHAO_ID,
            "minecraft:textures/block/enchanting_table_top.png",
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
            .putInt(ZhiDaoDomainService.KEY_DOMAIN_UNTIL_TICK, player.tickCount + duration);
        player.getPersistentData().putDouble(ZhiDaoDomainService.KEY_DOMAIN_RADIUS, radius);
        player.getPersistentData()
            .putDouble(ZhiDaoDomainService.KEY_DOMAIN_COST_PER_SECOND, costPerSecond);
    }

    @Override
    public boolean onActivate(final ServerPlayer player, final ShazhaoData data) {
        if (!super.onActivate(player, data)) {
            return false;
        }

        final double dodge = ShazhaoMetadataHelper.getDouble(
            data,
            getMetaKey("dodge_chance"),
            DEFAULT_DODGE_CHANCE
        );
        player.getPersistentData()
            .putDouble(ZhiDaoDomainService.KEY_DOMAIN_DODGE_CHANCE, dodge);

        final double counter = ShazhaoMetadataHelper.getDouble(
            data,
            getMetaKey("counter_damage"),
            DEFAULT_COUNTER_DAMAGE
        );
        player.getPersistentData()
            .putDouble(ZhiDaoDomainService.KEY_DOMAIN_COUNTER_DAMAGE, counter);
        return true;
    }
}
