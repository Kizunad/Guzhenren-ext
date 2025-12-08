package com.Kizunad.customNPCs.tasks.objective;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * “击杀指定实体”目标定义，接受任务时会生成目标实体。
 */
public record KillEntityObjectiveDefinition(
    EntityType<? extends LivingEntity> entityType,
    int requiredKills,
    double spawnRadius,
    @Nullable Component customName,
    @Nullable Double overrideMaxHealth,
    @Nullable Double overrideMoveSpeed,
    @Nullable Double overrideAttackDamage,
    Map<EquipmentSlot, ItemStack> armorPieces,
    @Nullable net.minecraft.core.BlockPos fixedSpawnPos,
    boolean snapToSurface
) implements TaskObjectiveDefinition {

    private static final double MIN_SPAWN_RADIUS = 4.0D;

    public KillEntityObjectiveDefinition {
        requiredKills = Math.max(1, requiredKills);
        spawnRadius = Math.max(MIN_SPAWN_RADIUS, spawnRadius);
        if (armorPieces == null || armorPieces.isEmpty()) {
            armorPieces = Collections.emptyMap();
        } else {
            armorPieces = Collections.unmodifiableMap(new EnumMap<>(armorPieces));
        }
    }

    @Override
    public TaskObjectiveType getType() {
        return TaskObjectiveType.KILL_ENTITY;
    }

    public ResourceLocation typeId() {
        return EntityType.getKey(entityType);
    }

    /**
     * 将预设属性应用到实体上（只设置提供的条目）。
     */
    public void applyAttributes(LivingEntity entity) {
        if (overrideMaxHealth != null &&
            entity.getAttributes().hasAttribute(Attributes.MAX_HEALTH)
        ) {
            var attr = entity.getAttribute(Attributes.MAX_HEALTH);
            if (attr != null) {
                attr.setBaseValue(overrideMaxHealth);
                entity.setHealth((float) overrideMaxHealth.doubleValue());
            }
        }
        if (overrideMoveSpeed != null &&
            entity.getAttributes().hasAttribute(Attributes.MOVEMENT_SPEED)
        ) {
            var attr = entity.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attr != null) {
                attr.setBaseValue(overrideMoveSpeed);
            }
        }
        if (overrideAttackDamage != null &&
            entity.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE)
        ) {
            var attr = entity.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attr != null) {
                attr.setBaseValue(overrideAttackDamage);
            }
        }

        // 覆盖盔甲
        if (!armorPieces.isEmpty()) {
            for (Map.Entry<EquipmentSlot, ItemStack> entry : armorPieces.entrySet()) {
                entity.setItemSlot(entry.getKey(), entry.getValue().copy());
            }
        }
    }
}
