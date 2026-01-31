package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jindao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.common.AbstractDomainDefenseEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.service.JinDaoDomainService;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.server.level.ServerPlayer;

public class JinDaoGengJinSwordArrayEffect extends AbstractDomainDefenseEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_jin_dao_geng_jin_sword_array";

    private static final float DOMAIN_ALPHA = 0.4F;
    private static final float DOMAIN_ROTATION_SPEED = 1.5F;

    private static final double DEFAULT_THORNS_DAMAGE = 15.0;

    public JinDaoGengJinSwordArrayEffect() {
        super(
            SHAZHAO_ID,
            "minecraft:textures/block/gold_block.png",
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
            .putInt(JinDaoDomainService.KEY_DOMAIN_UNTIL_TICK, player.tickCount + duration);
        player.getPersistentData().putDouble(JinDaoDomainService.KEY_DOMAIN_RADIUS, radius);
        player.getPersistentData().putDouble(JinDaoDomainService.KEY_DOMAIN_ENEMY_DAMAGE, enemyDamage);
        player.getPersistentData()
            .putDouble(JinDaoDomainService.KEY_DOMAIN_COST_PER_SECOND, costPerSecond);
    }

    @Override
    public boolean onActivate(final ServerPlayer player, final ShazhaoData data) {
        if (!super.onActivate(player, data)) {
            return false;
        }

        final double thorns = ShazhaoMetadataHelper.getDouble(
            data,
            getMetaKey("thorns_damage"),
            DEFAULT_THORNS_DAMAGE
        );
        player.getPersistentData().putDouble(JinDaoDomainService.KEY_DOMAIN_THORNS_DAMAGE, thorns);
        return true;
    }
}
