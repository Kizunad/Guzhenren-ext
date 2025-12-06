package com.Kizunad.customNPCs.ai.status;

import com.Kizunad.customNPCs.ai.status.config.NpcStatusConfig;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

/**
 * NPC 状态组件:管理饥饿/饱和/耗竭,以及基于饥饿的回血/掉血。
 * <p>
 * 该组件模拟了类似于玩家的饥饿系统,包括:
 * - 饥饿值管理
 * - 饱和度管理
 * - 耗竭值管理
 * - 高饥饿度时的自然回血
 * - 饥饿时的持续伤害
 * </p>
 */
public class NpcStatus {

    /** 初始饱和度为最大值的一半 */
    private static final float INITIAL_SATURATION_RATIO = 0.5f;
    
    /** 进食时饱和度增益倍数 */
    private static final float SATURATION_GAIN_MULTIPLIER = 2.0f;
    
    /** 进食时耗竭值减少量 */
    private static final float EXHAUSTION_REDUCTION_ON_EAT = 0.5f;
    
    /** 最大耗竭值 */
    private static final float MAX_EXHAUSTION = 40.0f;

    /** 配置实例 */
    private final NpcStatusConfig config = NpcStatusConfig.getInstance();

    /** 当前饥饿值 */
    private int hunger = config.getMaxHunger();
    
    /** 当前饱和度 */
    private float saturation = config.getMaxSaturation() * INITIAL_SATURATION_RATIO;
    
    /** 当前耗竭值 */
    private float exhaustion = 0.0f;

    /** 回血计时器 */
    private int regenTimer = 0;
    
    /** 饥饿伤害计时器 */
    private int starvationTimer = 0;

    /**
     * 每tick执行一次的状态更新。
     * <p>
     * 处理耗竭值衰减、回血和饥饿伤害。
     * </p>
     *
     * @param level 服务端世界
     * @param entity NPC实体
     */
    public void tick(ServerLevel level, LivingEntity entity) {
        applyExhaustionDecay();
        handleRegen(level, entity);
        handleStarvation(level, entity);
    }

    /**
     * 消耗食物并恢复饥饿值、饱和度。
     * <p>
     * 根据食物属性增加饥饿值和饱和度,并减少耗竭值。
     * 效果应用交由原版 {@link net.minecraft.world.entity.LivingEntity#eat} 逻辑处理。
     * </p>
     *
     * @param stack 食物物品堆
     * @param entity NPC实体
     */
    public void eat(ItemStack stack, LivingEntity entity) {
        FoodProperties props = stack.getFoodProperties(entity);
        if (props == null) {
            return;
        }
        int nutrition = props.nutrition();
        float saturationGain = props.saturation() * nutrition * SATURATION_GAIN_MULTIPLIER;

        hunger = Math.min(config.getMaxHunger(), hunger + nutrition);
        saturation = Math.min(config.getMaxSaturation(), saturation + saturationGain);
        exhaustion = Math.max(0.0f, exhaustion - EXHAUSTION_REDUCTION_ON_EAT);

    }

    /**
     * 增加耗竭值。
     *
     * @param amount 增加量
     */
    public void addExhaustion(float amount) {
        exhaustion = Math.min(exhaustion + amount, MAX_EXHAUSTION);
    }

    public int getHunger() {
        return hunger;
    }

    public float getHungerPercent() {
        return hunger / (float) config.getMaxHunger();
    }

    public boolean isHungry() {
        return hunger < config.getHighHungerThreshold();
    }

    public boolean isCritical() {
        return hunger <= config.getCriticalHungerThreshold();
    }

    public float getSaturation() {
        return saturation;
    }

    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("hunger", hunger);
        tag.putFloat("saturation", saturation);
        tag.putFloat("exhaustion", exhaustion);
        tag.putInt("regenTimer", regenTimer);
        tag.putInt("starvationTimer", starvationTimer);
        return tag;
    }

    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("hunger")) {
            hunger = tag.getInt("hunger");
        }
        if (tag.contains("saturation")) {
            saturation = tag.getFloat("saturation");
        }
        if (tag.contains("exhaustion")) {
            exhaustion = tag.getFloat("exhaustion");
        }
        if (tag.contains("regenTimer")) {
            regenTimer = tag.getInt("regenTimer");
        }
        if (tag.contains("starvationTimer")) {
            starvationTimer = tag.getInt("starvationTimer");
        }
    }

    public void setHungerForTest(int hunger) {
        this.hunger = Math.max(0, Math.min(config.getMaxHunger(), hunger));
    }

    private void applyExhaustionDecay() {
        while (exhaustion > config.getExhaustionThreshold()) {
            exhaustion -= config.getExhaustionThreshold();
            if (saturation > 0.0f) {
                saturation = Math.max(0.0f, saturation - 1.0f);
            } else {
                hunger = Math.max(0, hunger - 1);
            }
        }
    }

    private void handleRegen(ServerLevel level, LivingEntity entity) {
        if (hunger >= config.getHighHungerThreshold() && entity.getHealth() < entity.getMaxHealth()) {
            regenTimer++;
            if (regenTimer >= config.getHighHungerRegenInterval()) {
                entity.heal(config.getRegenAmount());
                addExhaustion(config.getRegenExhaustionCost());
                regenTimer = 0;
            }
        } else {
            regenTimer = 0;
        }
    }

    private void handleStarvation(ServerLevel level, LivingEntity entity) {
        if (hunger <= 0) {
            starvationTimer++;
            if (starvationTimer >= config.getStarvationInterval()) {
                entity.hurt(level.damageSources().starve(), config.getStarvationDamage());
                starvationTimer = 0;
            }
        } else {
            starvationTimer = 0;
        }
    }
}
