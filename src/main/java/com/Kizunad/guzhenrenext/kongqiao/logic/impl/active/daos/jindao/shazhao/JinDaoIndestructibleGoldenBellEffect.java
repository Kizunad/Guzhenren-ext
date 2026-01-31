package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jin_dao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.common.AbstractDomainDefenseEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.service.JinDaoDomainService;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class JinDaoIndestructibleGoldenBellEffect extends AbstractDomainDefenseEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_jin_dao_indestructible_golden_bell";

    private static final float DOMAIN_ALPHA = 0.9F;
    private static final float DOMAIN_ROTATION_SPEED = 0.0F;

    private static final double DEFAULT_THORNS_MULTIPLIER = 5.0;
    private static final int DEFAULT_RESISTANCE_AMPLIFIER = 3;
    private static final int DEFAULT_DURATION_TICKS = 300;

    public JinDaoIndestructibleGoldenBellEffect() {
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
        player.getPersistentData()
            .putDouble(JinDaoDomainService.KEY_DOMAIN_ENEMY_DAMAGE, enemyDamage);
        player.getPersistentData()
            .putDouble(JinDaoDomainService.KEY_DOMAIN_COST_PER_SECOND, costPerSecond);
    }

    @Override
    public boolean onActivate(final ServerPlayer player, final ShazhaoData data) {
        if (!super.onActivate(player, data)) {
            return false;
        }

        final double multiplier = ShazhaoMetadataHelper.getDouble(
            data,
            getMetaKey("thorns_multiplier"),
            DEFAULT_THORNS_MULTIPLIER
        );
        player.getPersistentData()
            .putDouble(JinDaoDomainService.KEY_DOMAIN_THORNS_MULTIPLIER, multiplier);

        final int resistance = ShazhaoMetadataHelper.getInt(
            data,
            getMetaKey("resistance_amplifier"),
            DEFAULT_RESISTANCE_AMPLIFIER
        );
        player.addEffect(
            new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE,
                ShazhaoMetadataHelper.getInt(data, getMetaKey("duration"), DEFAULT_DURATION_TICKS),
                resistance,
                false,
                false,
                true
            )
        );
        return true;
    }
}
