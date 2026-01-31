package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.common.AbstractDomainDefenseEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.service.JianDaoDomainService;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.server.level.ServerPlayer;

public class JianDaoImmortalExecutionerArrayEffect extends AbstractDomainDefenseEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_jian_dao_immortal_executioner_array";

    private static final float DOMAIN_ALPHA = 0.4F;
    private static final float DOMAIN_ROTATION_SPEED = 3.0F;

    private static final double DEFAULT_EXECUTE_THRESHOLD = 0.2;
    private static final double DEFAULT_MELEE_REDUCTION = 0.9;

    public JianDaoImmortalExecutionerArrayEffect() {
        super(
            SHAZHAO_ID,
            "minecraft:textures/block/iron_block.png",
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

        final double threshold = ShazhaoMetadataHelper.getDouble(
            data,
            getMetaKey("execute_threshold"),
            DEFAULT_EXECUTE_THRESHOLD
        );
        player.getPersistentData()
            .putDouble(JianDaoDomainService.KEY_DOMAIN_EXECUTE_THRESHOLD, threshold);

        final double meleeReduction = ShazhaoMetadataHelper.getDouble(
            data,
            getMetaKey("damage_reduction_melee"),
            DEFAULT_MELEE_REDUCTION
        );
        player.getPersistentData()
            .putDouble(JianDaoDomainService.KEY_DOMAIN_DAMAGE_REDUCTION_MELEE, meleeReduction);
        return true;
    }
}
