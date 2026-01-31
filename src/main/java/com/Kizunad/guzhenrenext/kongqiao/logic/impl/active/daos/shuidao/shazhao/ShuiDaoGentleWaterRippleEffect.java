package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shuidao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.common.AbstractDomainDefenseEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.service.ShuiDaoDomainService;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.server.level.ServerPlayer;

public class ShuiDaoGentleWaterRippleEffect extends AbstractDomainDefenseEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_shui_dao_gentle_water_ripple";

    private static final float DOMAIN_ALPHA = 0.4F;
    private static final float DOMAIN_ROTATION_SPEED = 0.5F;

    private static final int DEFAULT_REGEN_AMPLIFIER = 1;
    private static final int DEFAULT_WEAKNESS_AMPLIFIER = 1;

    public ShuiDaoGentleWaterRippleEffect() {
        super(
            SHAZHAO_ID,
            "minecraft:textures/block/water_still.png",
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
            .putInt(ShuiDaoDomainService.KEY_DOMAIN_UNTIL_TICK, player.tickCount + duration);
        player.getPersistentData().putDouble(ShuiDaoDomainService.KEY_DOMAIN_RADIUS, radius);
        player.getPersistentData()
            .putDouble(ShuiDaoDomainService.KEY_DOMAIN_COST_PER_SECOND, costPerSecond);
    }

    @Override
    public boolean onActivate(final ServerPlayer player, final ShazhaoData data) {
        if (!super.onActivate(player, data)) {
            return false;
        }

        final int regen = ShazhaoMetadataHelper.getInt(
            data,
            getMetaKey("regen_amplifier"),
            DEFAULT_REGEN_AMPLIFIER
        );
        final int weakness = ShazhaoMetadataHelper.getInt(
            data,
            getMetaKey("weakness_amplifier"),
            DEFAULT_WEAKNESS_AMPLIFIER
        );
        player.getPersistentData().putInt(ShuiDaoDomainService.KEY_DOMAIN_REGEN_AMP, regen);
        player.getPersistentData()
            .putInt(ShuiDaoDomainService.KEY_DOMAIN_WEAKNESS_AMP, weakness);
        return true;
    }
}
