package com.Kizunad.customNPCs.ai.status;

import com.Kizunad.customNPCs.ai.status.config.NpcStatusConfig;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

/**
 * NPC 状态组件：管理饥饿/饱和/耗竭，以及基于饥饿的回血/掉血。
 */
@SuppressWarnings("checkstyle:MagicNumber")
public class NpcStatus {

    private final NpcStatusConfig config = NpcStatusConfig.getInstance();

    private int hunger = config.getMaxHunger();
    private float saturation = config.getMaxSaturation() / 2.0f;
    private float exhaustion = 0.0f;

    private int regenTimer = 0;
    private int starvationTimer = 0;

    public void tick(ServerLevel level, LivingEntity entity) {
        applyExhaustionDecay();
        handleRegen(level, entity);
        handleStarvation(level, entity);
    }

    public void eat(ItemStack stack, LivingEntity entity) {
        FoodProperties props = stack.getFoodProperties(entity);
        if (props == null) {
            return;
        }
        int nutrition = props.nutrition();
        float saturationGain = props.saturation() * nutrition * 2.0f;

        hunger = Math.min(config.getMaxHunger(), hunger + nutrition);
        saturation = Math.min(config.getMaxSaturation(), saturation + saturationGain);
        exhaustion = Math.max(0.0f, exhaustion - 0.5f);

        if (!entity.level().isClientSide()) {
            for (var pair : props.effects()) {
                MobEffectInstance effect = pair.effect();
                float chance = pair.probability();
                if (effect != null && entity.getRandom().nextFloat() < chance) {
                    entity.addEffect(new MobEffectInstance(effect));
                }
            }
        }
    }

    public void addExhaustion(float amount) {
        exhaustion = Math.min(exhaustion + amount, 40.0f);
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
