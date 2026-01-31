package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.common.AbstractDomainDefenseEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.service.BianHuaDaoDomainService;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.server.level.ServerPlayer;

public class BianHuaDaoHeavenlyDemonBodyEffect extends AbstractDomainDefenseEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_bian_hua_dao_heavenly_demon_body";

    private static final float DOMAIN_ALPHA = 0.5F;
    private static final float DOMAIN_ROTATION_SPEED = 0.5F;

    private static final double DEFAULT_MAX_HEALTH_BONUS = 100.0;
    private static final double DEFAULT_ARMOR_BONUS = 20.0;
    private static final double DEFAULT_HEAL_RATIO = 0.1;

    public BianHuaDaoHeavenlyDemonBodyEffect() {
        super(SHAZHAO_ID, "minecraft:textures/block/nether_wart_block.png", DOMAIN_ALPHA, DOMAIN_ROTATION_SPEED);
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
            .putInt(BianHuaDaoDomainService.KEY_DOMAIN_UNTIL_TICK, player.tickCount + duration);
        player.getPersistentData().putDouble(BianHuaDaoDomainService.KEY_DOMAIN_RADIUS, radius);
        player.getPersistentData()
            .putDouble(BianHuaDaoDomainService.KEY_DOMAIN_COST_PER_SECOND, costPerSecond);
    }

    @Override
    public boolean onActivate(final ServerPlayer player, final ShazhaoData data) {
        if (!super.onActivate(player, data)) {
            return false;
        }

        final double healthBonus = ShazhaoMetadataHelper.getDouble(
            data,
            getMetaKey("max_health_bonus"),
            DEFAULT_MAX_HEALTH_BONUS
        );
        player.getPersistentData()
            .putDouble(BianHuaDaoDomainService.KEY_DOMAIN_MAX_HEALTH_BONUS, healthBonus);

        final double armorBonus = ShazhaoMetadataHelper.getDouble(
            data,
            getMetaKey("armor_bonus"),
            DEFAULT_ARMOR_BONUS
        );
        player.getPersistentData()
            .putDouble(BianHuaDaoDomainService.KEY_DOMAIN_ARMOR_BONUS, armorBonus);

        final double healRatio = ShazhaoMetadataHelper.getDouble(
            data,
            getMetaKey("heal_ratio"),
            DEFAULT_HEAL_RATIO
        );
        player.getPersistentData()
            .putDouble(BianHuaDaoDomainService.KEY_DOMAIN_HEAL_RATIO, healRatio);
        return true;
    }
}
