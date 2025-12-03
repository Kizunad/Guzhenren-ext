package com.Kizunad.customNPCs.ai.config;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.common.NeoForgeMod;

/**
 * Custom NPC 的属性基线模板。
 * <p>
 * 将基础属性集中管理，便于后续配置化或统一调优。
 */
@SuppressWarnings("checkstyle:MagicNumber")
public final class NpcAttributeDefaults {

    private NpcAttributeDefaults() {}

    public static final double MAX_HEALTH = 800.0D;
    public static final double ATTACK_DAMAGE = 20.0D;
    public static final double ATTACK_KNOCKBACK = 0.4D;
    public static final double ATTACK_SPEED = 8.0D;
    public static final double MOVEMENT_SPEED = 0.40D;
    public static final double FLYING_SPEED = 0.4D;
    public static final double SWIM_SPEED = 1.0D;
    public static final double ARMOR = 80.0D;
    public static final double ARMOR_TOUGHNESS = 0.0D;
    public static final double KNOCKBACK_RESISTANCE = 0.0D;
    public static final double FOLLOW_RANGE = 24.0D;
    public static final double GRAVITY = 0.08D;
    public static final double STEP_HEIGHT = 3.0D;
    public static final double MOVEMENT_EFFICIENCY = 1.0D;
    public static final double WATER_MOVEMENT_EFFICIENCY = 0.0D;
    public static final double MINING_EFFICIENCY = 0.0D;
    public static final double BLOCK_BREAK_SPEED = 1.0D;
    public static final double SUBMERGED_MINING_SPEED = 0.2D;
    public static final double JUMP_STRENGTH = 0.42D;
    public static final double SAFE_FALL_DISTANCE = 3.0D;
    public static final double FALL_DAMAGE_MULTIPLIER = 1.0D;
    public static final double OXYGEN_BONUS = 0.0D;
    public static final double LUCK = 0.0D;
    public static final double MAX_ABSORPTION = 0.0D;
    public static final double SCALE = 1.0D;
    public static final double SNEAKING_SPEED = 0.3D;
    public static final double SWEEPING_DAMAGE_RATIO = 0.0D;
    public static final double BURNING_TIME = 1.0D;
    public static final double EXPLOSION_KB_RESISTANCE = 0.0D;
    public static final double BLOCK_INTERACTION_RANGE = 4.5D;
    public static final double ENTITY_INTERACTION_RANGE = 3.0D;
    public static final double SPAWN_REINFORCEMENTS = 0.0D;
    public static final double NAMETAG_DISTANCE = 64.0D;

    private static final ResourceLocation NAMETAG_ATTR_ID =
        ResourceLocation.parse("neoforge:nametag_distance");

    /**
     * 应用默认属性到给定构建器。
     */
    public static AttributeSupplier.Builder apply(
        AttributeSupplier.Builder builder
    ) {
        return builder
            .add(Attributes.MAX_HEALTH, MAX_HEALTH)
            .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
            .add(Attributes.ATTACK_KNOCKBACK, ATTACK_KNOCKBACK)
            .add(Attributes.ATTACK_SPEED, ATTACK_SPEED)
            .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
            .add(Attributes.FLYING_SPEED, FLYING_SPEED)
            .add(Attributes.ARMOR, ARMOR)
            .add(Attributes.ARMOR_TOUGHNESS, ARMOR_TOUGHNESS)
            .add(Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_RESISTANCE)
            .add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE)
            .add(Attributes.GRAVITY, GRAVITY)
            .add(Attributes.STEP_HEIGHT, STEP_HEIGHT)
            .add(Attributes.MOVEMENT_EFFICIENCY, MOVEMENT_EFFICIENCY)
            .add(
                Attributes.WATER_MOVEMENT_EFFICIENCY,
                WATER_MOVEMENT_EFFICIENCY
            )
            .add(Attributes.MINING_EFFICIENCY, MINING_EFFICIENCY)
            .add(Attributes.BLOCK_BREAK_SPEED, BLOCK_BREAK_SPEED)
            .add(Attributes.SUBMERGED_MINING_SPEED, SUBMERGED_MINING_SPEED)
            .add(Attributes.JUMP_STRENGTH, JUMP_STRENGTH)
            .add(Attributes.SAFE_FALL_DISTANCE, SAFE_FALL_DISTANCE)
            .add(Attributes.FALL_DAMAGE_MULTIPLIER, FALL_DAMAGE_MULTIPLIER)
            .add(Attributes.OXYGEN_BONUS, OXYGEN_BONUS)
            .add(Attributes.LUCK, LUCK)
            .add(Attributes.MAX_ABSORPTION, MAX_ABSORPTION)
            .add(Attributes.SCALE, SCALE)
            .add(Attributes.SNEAKING_SPEED, SNEAKING_SPEED)
            .add(Attributes.SWEEPING_DAMAGE_RATIO, SWEEPING_DAMAGE_RATIO)
            .add(Attributes.BURNING_TIME, BURNING_TIME)
            .add(
                Attributes.EXPLOSION_KNOCKBACK_RESISTANCE,
                EXPLOSION_KB_RESISTANCE
            )
            .add(Attributes.BLOCK_INTERACTION_RANGE, BLOCK_INTERACTION_RANGE)
            .add(Attributes.ENTITY_INTERACTION_RANGE, ENTITY_INTERACTION_RANGE)
            .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, SPAWN_REINFORCEMENTS)
            // NeoForge 扩展属性
            .add(NeoForgeMod.SWIM_SPEED, SWIM_SPEED);
    }

    /**
     * 附加可选属性（如 nametag 距离），若注册表中存在则添加。
     */
    public static void applyOptionalAttributes(
        AttributeSupplier.Builder builder
    ) {
        resolveAttribute(NAMETAG_ATTR_ID).ifPresent(holder ->
            builder.add(holder, NAMETAG_DISTANCE)
        );
    }

    private static Optional<
        Holder.Reference<net.minecraft.world.entity.ai.attributes.Attribute>
    > resolveAttribute(ResourceLocation id) {
        return BuiltInRegistries.ATTRIBUTE.getHolder(id);
    }
}
