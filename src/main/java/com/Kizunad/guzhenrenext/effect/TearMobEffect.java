package com.Kizunad.guzhenrenext.effect;

import com.Kizunad.guzhenrenext.util.ModConstants;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 撕裂效果 (Tear Effect)
 * <p>
 * 效果：敌人移动时会持续掉血（流血效果）。
 * 治疗减半效果需配合事件监听实现。
 * </p>
 */
public class TearMobEffect extends MobEffect {

    public TearMobEffect() {
        // HARMFUL: 有害效果
        // 0x990000: 深红色
        super(MobEffectCategory.HARMFUL, ModConstants.COLOR_TEAR_EFFECT);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // 每 tick 都检查移动
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // 检查实体是否显著移动
        // xOld, yOld, zOld 是实体上一 tick 的位置
        double dx = entity.getX() - entity.xOld;
        double dy = entity.getY() - entity.yOld;
        double dz = entity.getZ() - entity.zOld;
        
        // 计算移动距离平方
        double distSqr = dx * dx + dy * dy + dz * dz;
        
        // 阈值设为 0.0004 (即 0.02 方块)，避免站立不动的微小偏移导致扣血
        if (distSqr > ModConstants.TEAR_MOVE_THRESHOLD_SQ) {
            // 造成 1.0点 + 等级 * 0.5点 的伤害
            entity.hurt(entity.damageSources().generic(), 1.0F + amplifier * ModConstants.TEAR_DAMAGE_AMPLIFIER_FACTOR);
        }
        
        return true;
    }
}
