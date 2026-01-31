package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.common.AbstractDomainDefenseEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.service.XueDaoDomainService;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.server.level.ServerPlayer;

public class XueDaoBloodRiverChariotEffect extends AbstractDomainDefenseEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_xue_dao_blood_river_chariot";

    private static final float DOMAIN_ALPHA = 0.8F;
    private static final float DOMAIN_ROTATION_SPEED = 0.5F;

    private static final double DEFAULT_ABSORPTION_PER_KILL = 10.0;
    private static final int DEFAULT_WITHER_AMPLIFIER = 4;

    public XueDaoBloodRiverChariotEffect() {
        super(
            SHAZHAO_ID,
            "minecraft:textures/block/red_nether_bricks.png",
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
            .putInt(XueDaoDomainService.KEY_DOMAIN_UNTIL_TICK, player.tickCount + duration);
        player.getPersistentData().putDouble(XueDaoDomainService.KEY_DOMAIN_RADIUS, radius);
        player.getPersistentData().putDouble(XueDaoDomainService.KEY_DOMAIN_ENEMY_DAMAGE, enemyDamage);
        player.getPersistentData()
            .putDouble(XueDaoDomainService.KEY_DOMAIN_COST_PER_SECOND, costPerSecond);
    }

    @Override
    public boolean onActivate(final ServerPlayer player, final ShazhaoData data) {
        if (!super.onActivate(player, data)) {
            return false;
        }

        final double absorption = ShazhaoMetadataHelper.getDouble(
            data,
            getMetaKey("absorption_per_kill"),
            DEFAULT_ABSORPTION_PER_KILL
        );
        player.getPersistentData()
            .putDouble(XueDaoDomainService.KEY_DOMAIN_ABSORPTION_PER_KILL, absorption);

        final int wither = ShazhaoMetadataHelper.getInt(
            data,
            getMetaKey("wither_amplifier"),
            DEFAULT_WITHER_AMPLIFIER
        );
        player.getPersistentData().putInt(XueDaoDomainService.KEY_DOMAIN_WITHER_AMP, wither);
        return true;
    }
}
