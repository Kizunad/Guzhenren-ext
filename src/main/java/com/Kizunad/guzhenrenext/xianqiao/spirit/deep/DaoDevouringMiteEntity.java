package com.Kizunad.guzhenrenext.xianqiao.spirit.deep;

import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoMarkApi;
import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.level.Level;

public class DaoDevouringMiteEntity extends Endermite {

    private static final int CHECK_INTERVAL_TICKS = 20;
    private static final int SPLIT_AURA_COST = 80;
    private static final int STARVATION_DAMAGE = 2;
    private static final double SPLIT_OFFSET_X = 0.4D;
    private static final double SPLIT_OFFSET_Z = 0.4D;

    private int auraCheckTicker;

    public DaoDevouringMiteEntity(EntityType<? extends Endermite> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide()) {
            return;
        }
        auraCheckTicker++;
        if (auraCheckTicker < CHECK_INTERVAL_TICKS) {
            return;
        }
        auraCheckTicker = 0;
        processAuraCycle();
    }

    public void forceAuraCheckForTest() {
        auraCheckTicker = CHECK_INTERVAL_TICKS;
    }

    public boolean runAuraCycleForTest() {
        if (level().isClientSide()) {
            return false;
        }
        return processAuraCycle();
    }

    private boolean processAuraCycle() {
        if (DaoMarkApi.consumeAura(level(), blockPosition(), DaoType.DARK, SPLIT_AURA_COST)) {
            splitOnce();
            return true;
        }
        hurt(damageSources().starve(), STARVATION_DAMAGE);
        return false;
    }

    private void splitOnce() {
        net.minecraft.world.entity.Entity child = getType().create(level());
        if (!(child instanceof DaoDevouringMiteEntity miteChild)) {
            return;
        }
        miteChild.moveTo(getX() + SPLIT_OFFSET_X, getY(), getZ() + SPLIT_OFFSET_Z, getYRot(), getXRot());
        level().addFreshEntity(miteChild);
    }
}
