package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.common.AbstractDomainDefenseEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.service.JianDaoDomainService;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.server.level.ServerPlayer;

public class JianDaoTenThousandSwordsEffect extends AbstractDomainDefenseEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_jian_dao_ten_thousand_swords";

    private static final float DOMAIN_ALPHA = 0.3F;
    private static final float DOMAIN_ROTATION_SPEED = 2.5F;

    private static final double DEFAULT_DISARM_CHANCE = 0.1;

    public JianDaoTenThousandSwordsEffect() {
        super(SHAZHAO_ID, "minecraft:textures/block/iron_block.png", DOMAIN_ALPHA, DOMAIN_ROTATION_SPEED);
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
            .putInt(JianDaoDomainService.KEY_DOMAIN_UNTIL_TICK, player.tickCount + duration);
        player.getPersistentData().putDouble(JianDaoDomainService.KEY_DOMAIN_RADIUS, radius);
        player.getPersistentData().putDouble(JianDaoDomainService.KEY_DOMAIN_ENEMY_DAMAGE, enemyDamage);
        player.getPersistentData()
            .putDouble(JianDaoDomainService.KEY_DOMAIN_COST_PER_SECOND, costPerSecond);
    }

    @Override
    public boolean onActivate(final ServerPlayer player, final ShazhaoData data) {
        if (!super.onActivate(player, data)) {
            return false;
        }

        final double chance = ShazhaoMetadataHelper.getDouble(
            data,
            getMetaKey("disarm_chance"),
            DEFAULT_DISARM_CHANCE
        );
        player.getPersistentData().putDouble(JianDaoDomainService.KEY_DOMAIN_DISARM_CHANCE, chance);
        return true;
    }
}
