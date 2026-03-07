package com.Kizunad.guzhenrenext.xianqiao.spirit.deep;

import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoMarkDiffusionService;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class CalamityBeastEntity extends Ravager {

    private static final int RESOURCE_DROP_COUNT = 16;

    private UUID boundOwner;

    public CalamityBeastEntity(EntityType<? extends Ravager> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void dropCustomDeathLoot(
        ServerLevel level,
        net.minecraft.world.damagesource.DamageSource source,
        boolean causedByPlayer
    ) {
        super.dropCustomDeathLoot(level, source, causedByPlayer);
        for (int i = 0; i < RESOURCE_DROP_COUNT; i++) {
            spawnAtLocation(Items.DIAMOND);
        }
        if (boundOwner != null) {
            ServerLevel apertureLevel = level.getServer().getLevel(DaoMarkDiffusionService.APERTURE_DIMENSION);
            if (apertureLevel != null) {
                ApertureWorldData data = ApertureWorldData.get(apertureLevel);
                long forceEndTick = apertureLevel.getGameTime() + 1;
                data.updateTribulationTick(boundOwner, forceEndTick);
            }
        }
    }

    public void bindTribulationOwner(UUID owner) {
        boundOwner = owner;
    }
}
