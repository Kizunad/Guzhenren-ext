package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.hundao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.common.AbstractDomainDefenseEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.service.HunDaoDomainService;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.server.level.ServerPlayer;

public class HunDaoDangHunMountainPhantomEffect extends AbstractDomainDefenseEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_hun_dao_dang_hun_mountain_phantom";

    private static final float DOMAIN_ALPHA = 0.8F;
    private static final float DOMAIN_ROTATION_SPEED = 0.05F;

    private static final double DEFAULT_SOUL_DAMAGE = 30.0;
    private static final int DEFAULT_WEAKNESS_AMPLIFIER = 5;

    public HunDaoDangHunMountainPhantomEffect() {
        super(SHAZHAO_ID, "minecraft:textures/block/soul_sand.png", DOMAIN_ALPHA, DOMAIN_ROTATION_SPEED);
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
            .putInt(HunDaoDomainService.KEY_DOMAIN_UNTIL_TICK, player.tickCount + duration);
        player.getPersistentData().putDouble(HunDaoDomainService.KEY_DOMAIN_RADIUS, radius);
        player.getPersistentData()
            .putDouble(HunDaoDomainService.KEY_DOMAIN_COST_PER_SECOND, costPerSecond);
    }

    @Override
    public boolean onActivate(final ServerPlayer player, final ShazhaoData data) {
        if (!super.onActivate(player, data)) {
            return false;
        }

        final double soulDamage = ShazhaoMetadataHelper.getDouble(
            data,
            getMetaKey("soul_damage"),
            DEFAULT_SOUL_DAMAGE
        );
        player.getPersistentData()
            .putDouble(HunDaoDomainService.KEY_DOMAIN_SOUL_DAMAGE, soulDamage);

        final int weakness = ShazhaoMetadataHelper.getInt(
            data,
            getMetaKey("weakness_amplifier"),
            DEFAULT_WEAKNESS_AMPLIFIER
        );
        player.getPersistentData().putInt(HunDaoDomainService.KEY_DOMAIN_WEAKNESS_AMP, weakness);
        return true;
    }
}
