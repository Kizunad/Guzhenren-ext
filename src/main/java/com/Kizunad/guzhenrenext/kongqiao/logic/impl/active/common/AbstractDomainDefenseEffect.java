package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.common;

import com.Kizunad.guzhenrenext.kongqiao.domain.network.ClientboundDomainSyncPayload;
import com.Kizunad.guzhenrenext.kongqiao.domain.network.DomainNetworkHandler;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoActiveEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.common.DaoCooldownKeys;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.server.level.ServerPlayer;

public abstract class AbstractDomainDefenseEffect implements IShazhaoActiveEffect {

    private static final int DEFAULT_COOLDOWN_TICKS = 600;
    private static final int DEFAULT_DURATION_TICKS = 200;

    private static final double DEFAULT_RADIUS = 5.0;
    private static final double DEFAULT_DAMAGE_REDUCTION = 100.0;
    private static final double DEFAULT_ENEMY_DAMAGE = 30.0;
    private static final double DEFAULT_ZHENYUAN_COST_PER_SECOND = 5.0;

    private static final int DEFAULT_LEVEL = 1;

    private static final double DEFAULT_HEIGHT_OFFSET = 0.1;

    private final String shazhaoId;
    private final String cooldownKey;
    private final String domainTexture;
    private final float domainAlpha;
    private final float domainRotationSpeed;

    protected AbstractDomainDefenseEffect(
        final String shazhaoId,
        final String domainTexture,
        final float domainAlpha,
        final float domainRotationSpeed
    ) {
        this.shazhaoId = shazhaoId;
        this.cooldownKey = DaoCooldownKeys.active(shazhaoId);
        this.domainTexture = domainTexture;
        this.domainAlpha = domainAlpha;
        this.domainRotationSpeed = domainRotationSpeed;
    }

    @Override
    public String getShazhaoId() {
        return shazhaoId;
    }

    @Override
    public boolean onActivate(final ServerPlayer player, final ShazhaoData data) {
        if (!ShazhaoCostHelper.tryConsumeOnce(player, player, data)) {
            return false;
        }

        final int cooldownTicks = ShazhaoMetadataHelper.getInt(
            data,
            getMetaKey("cooldown"),
            DEFAULT_COOLDOWN_TICKS
        );
        if (cooldownTicks > 0) {
            GuEffectCooldownHelper.setCooldownUntilTick(
                player,
                cooldownKey,
                player.tickCount + cooldownTicks
            );
        }

        final double radius = ShazhaoMetadataHelper.getDouble(
            data,
            getMetaKey("radius"),
            DEFAULT_RADIUS
        );
        final int duration = ShazhaoMetadataHelper.getInt(
            data,
            getMetaKey("duration"),
            DEFAULT_DURATION_TICKS
        );
        final double reduction = ShazhaoMetadataHelper.getDouble(
            data,
            getMetaKey("damage_reduction"),
            DEFAULT_DAMAGE_REDUCTION
        );
        final double enemyDamage = ShazhaoMetadataHelper.getDouble(
            data,
            getMetaKey("enemy_damage"),
            DEFAULT_ENEMY_DAMAGE
        );
        final double costPerSecond = ShazhaoMetadataHelper.getDouble(
            data,
            getMetaKey("zhenyuan_base_cost_per_second"),
            DEFAULT_ZHENYUAN_COST_PER_SECOND
        );

        writeDomainData(player, radius, duration, reduction, enemyDamage, costPerSecond);

        sendClientRenderPacket(player, radius);

        return true;
    }

    private void sendClientRenderPacket(final ServerPlayer player, final double radius) {
        ClientboundDomainSyncPayload payload = new ClientboundDomainSyncPayload(
            player.getUUID(),
            player.getUUID(),
            player.getX(),
            player.getY(),
            player.getZ(),
            radius,
            DEFAULT_LEVEL,
            domainTexture,
            DEFAULT_HEIGHT_OFFSET,
            domainAlpha,
            domainRotationSpeed
        );
        DomainNetworkHandler.sendDomainSync(payload, player.position(), player.serverLevel());
    }

    protected abstract void writeDomainData(
        ServerPlayer player,
        double radius,
        int duration,
        double reduction,
        double enemyDamage,
        double costPerSecond
    );

    protected String getMetaKey(final String suffix) {
        return shazhaoId + "_" + suffix;
    }
}
