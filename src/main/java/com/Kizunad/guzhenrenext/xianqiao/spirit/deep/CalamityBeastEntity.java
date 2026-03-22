package com.Kizunad.guzhenrenext.xianqiao.spirit.deep;

import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoMarkDiffusionService;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import com.Kizunad.guzhenrenext.xianqiao.item.XianqiaoItems;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class CalamityBeastEntity extends Ravager {

    private static final int RESOURCE_DROP_COUNT = 16;

    private static final long HIGH_PRESSURE_TRIBULATION_ADVANCE_TICKS = 1200L;

    private static final int DAO_YUAN_DROP_COUNT = 1;

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
        if (!causedByPlayer || boundOwner == null) {
            return;
        }
        if (!(source.getEntity() instanceof ServerPlayer killer)) {
            return;
        }
        if (!killer.getUUID().equals(boundOwner)) {
            return;
        }

        ServerLevel apertureLevel = level.getServer().getLevel(DaoMarkDiffusionService.APERTURE_DIMENSION);
        ServerLevel tribulationLevel = apertureLevel != null ? apertureLevel : level;
        ApertureWorldData data = ApertureWorldData.get(tribulationLevel);
        ApertureInfo info = data.getAperture(boundOwner);
        if (info == null) {
            return;
        }

        long highPressureTick = Math.max(
            tribulationLevel.getGameTime() + 1,
            info.nextTribulationTick() - HIGH_PRESSURE_TRIBULATION_ADVANCE_TICKS
        );
        if (highPressureTick >= info.nextTribulationTick()) {
            return;
        }

        data.updateTribulationTick(boundOwner, highPressureTick);
        spawnAtLocation(XianqiaoItems.DAO_YUAN_MU_KUANG.get(), DAO_YUAN_DROP_COUNT);
    }

    public void bindTribulationOwner(UUID owner) {
        boundOwner = owner;
    }
}
