package com.Kizunad.customNPCs.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Holder;

/**
 * 专属自定义 NPC 实体，运行自研 AI（NpcMind + Sensors + Actions）。
 * 仅保留必要的导航能力，移除原版 AI 干扰。
 */
public class CustomNpcEntity extends PathfinderMob {

    public static final String MIND_TAG = "customnpcs:mind_allowed";
    private static final double BASE_HEALTH = 20.0D;
    private static final double BASE_ATTACK = 4.0D;
    private static final double BASE_ATTACK_KNOCKBACK = 0.4D;
    private static final double BASE_ATTACK_SPEED = 4.0D;
    private static final double BASE_MOVE_SPEED = 0.32D;
    private static final double BASE_FLYING_SPEED = 0.4D;
    private static final double BASE_SWIM_SPEED = 1.0D;
    private static final double BASE_ARMOR = 2.0D;
    private static final double BASE_ARMOR_TOUGHNESS = 0.0D;
    private static final double BASE_KNOCKBACK_RESISTANCE = 0.0D;
    private static final double BASE_FOLLOW_RANGE = 24.0D;
    private static final double BASE_GRAVITY = 0.08D;
    private static final double BASE_STEP_HEIGHT = 0.6D;
    private static final double BASE_MOVEMENT_EFFICIENCY = 1.0D;
    private static final double BASE_WATER_MOVEMENT_EFFICIENCY = 0.0D;
    private static final double BASE_MINING_EFFICIENCY = 0.0D;
    private static final double BASE_BLOCK_BREAK_SPEED = 1.0D;
    private static final double BASE_SUBMERGED_MINING_SPEED = 0.2D;
    private static final double BASE_JUMP_STRENGTH = 0.42D;
    private static final double BASE_SAFE_FALL_DISTANCE = 3.0D;
    private static final double BASE_FALL_DAMAGE_MULTIPLIER = 1.0D;
    private static final double BASE_OXYGEN_BONUS = 0.0D;
    private static final double BASE_LUCK = 0.0D;
    private static final double BASE_MAX_ABSORPTION = 0.0D;
    private static final double BASE_SCALE = 1.0D;
    private static final double BASE_SNEAKING_SPEED = 0.3D;
    private static final double BASE_SWEEPING_DAMAGE_RATIO = 0.0D;
    private static final double BASE_BURNING_TIME = 1.0D;
    private static final double BASE_EXPLOSION_KB_RESISTANCE = 0.0D;
    private static final double BASE_BLOCK_INTERACTION_RANGE = 4.5D;
    private static final double BASE_ENTITY_INTERACTION_RANGE = 3.0D;
    private static final double BASE_SPAWN_REINFORCEMENTS = 0.0D;
    private static final double BASE_NAMETAG_DISTANCE = 64.0D;
    private static final ResourceLocation NAMETAG_ATTR_ID = ResourceLocation.parse("neoforge:nametag_distance");
    private static final ResourceLocation SWIM_SPEED_ATTR_ID = ResourceLocation.parse("neoforge:swim_speed");
    private static final int FLYING_MAX_TURN = 10;

    public enum NavigationMode {
        GROUND,
        FLYING,
        WATER,
        AMPHIBIOUS,
        WALL,
    }

    private NavigationMode navigationMode = NavigationMode.GROUND;

    public CustomNpcEntity(
        EntityType<? extends CustomNpcEntity> type,
        Level level
    ) {
        this(type, level, NavigationMode.GROUND);
    }

    public CustomNpcEntity(
        EntityType<? extends CustomNpcEntity> type,
        Level level,
        NavigationMode mode
    ) {
        super(type, level);
        this.navigationMode = mode;
        this.setPersistenceRequired();
        this.getTags().add(MIND_TAG); // 触发 NpcMindAttachment 自动挂载

        /*
         * NOTE: FlyingMoveControl
         * 默认 MoveControl 是地面逻辑，假设有重力、地面摩擦，会尝试贴地，无法
         * 正常悬停/升降。飞行实体需要绕过重力/地面摩擦并支持垂直/三维调整，因此要用 FlyingMoveControl 来正确
         * 应用速度和角度。
         */
        if (navigationMode == NavigationMode.FLYING) {
            this.moveControl = new FlyingMoveControl(this, FLYING_MAX_TURN, false);
        }
        // 覆盖 super 中创建的默认导航，按模式替换
        this.navigation = createNavigation(level);
    }

    @Override
    protected void registerGoals() {
        // 自定义 AI 全由 NpcMind 驱动，清空原版 Goals 避免干扰。
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        NavigationMode mode = navigationMode;
        if (mode == null) {
            mode = NavigationMode.GROUND;
            this.navigationMode = mode;
        }
        return switch (mode) {
            case FLYING -> new FlyingPathNavigation(this, level);
            case WATER -> new WaterBoundPathNavigation(this, level);
            case AMPHIBIOUS -> new AmphibiousPathNavigation(this, level);
            case WALL -> new WallClimberNavigation(this, level);
            case GROUND -> new GroundPathNavigation(this, level);
        };
    }

    /**
     * 定义实体默认属性（生命/攻击/移速/护甲），供属性注册事件使用。
     */
    public static AttributeSupplier.Builder createAttributes() {
        AttributeSupplier.Builder builder = AttributeSupplier.builder()
            .add(Attributes.MAX_HEALTH, BASE_HEALTH)
            .add(Attributes.ATTACK_DAMAGE, BASE_ATTACK)
            .add(Attributes.ATTACK_KNOCKBACK, BASE_ATTACK_KNOCKBACK)
            .add(Attributes.ATTACK_SPEED, BASE_ATTACK_SPEED)
            .add(Attributes.MOVEMENT_SPEED, BASE_MOVE_SPEED)
            .add(Attributes.FLYING_SPEED, BASE_FLYING_SPEED)
            .add(Attributes.ARMOR, BASE_ARMOR)
            .add(Attributes.ARMOR_TOUGHNESS, BASE_ARMOR_TOUGHNESS)
            .add(Attributes.KNOCKBACK_RESISTANCE, BASE_KNOCKBACK_RESISTANCE)
            .add(Attributes.FOLLOW_RANGE, BASE_FOLLOW_RANGE)
            .add(Attributes.GRAVITY, BASE_GRAVITY)
            .add(Attributes.STEP_HEIGHT, BASE_STEP_HEIGHT)
            .add(Attributes.MOVEMENT_EFFICIENCY, BASE_MOVEMENT_EFFICIENCY)
            .add(Attributes.WATER_MOVEMENT_EFFICIENCY, BASE_WATER_MOVEMENT_EFFICIENCY)
            .add(Attributes.MINING_EFFICIENCY, BASE_MINING_EFFICIENCY)
            .add(Attributes.BLOCK_BREAK_SPEED, BASE_BLOCK_BREAK_SPEED)
            .add(Attributes.SUBMERGED_MINING_SPEED, BASE_SUBMERGED_MINING_SPEED)
            .add(Attributes.JUMP_STRENGTH, BASE_JUMP_STRENGTH)
            .add(Attributes.SAFE_FALL_DISTANCE, BASE_SAFE_FALL_DISTANCE)
            .add(Attributes.FALL_DAMAGE_MULTIPLIER, BASE_FALL_DAMAGE_MULTIPLIER)
            .add(Attributes.OXYGEN_BONUS, BASE_OXYGEN_BONUS)
            .add(Attributes.LUCK, BASE_LUCK)
            .add(Attributes.MAX_ABSORPTION, BASE_MAX_ABSORPTION)
            .add(Attributes.SCALE, BASE_SCALE)
            .add(Attributes.SNEAKING_SPEED, BASE_SNEAKING_SPEED)
            .add(Attributes.SWEEPING_DAMAGE_RATIO, BASE_SWEEPING_DAMAGE_RATIO)
            .add(Attributes.BURNING_TIME, BASE_BURNING_TIME)
            .add(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE, BASE_EXPLOSION_KB_RESISTANCE)
            .add(Attributes.BLOCK_INTERACTION_RANGE, BASE_BLOCK_INTERACTION_RANGE)
            .add(Attributes.ENTITY_INTERACTION_RANGE, BASE_ENTITY_INTERACTION_RANGE)
            .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, BASE_SPAWN_REINFORCEMENTS);

        resolveAttribute(NAMETAG_ATTR_ID).ifPresent(holder ->
            builder.add(holder, BASE_NAMETAG_DISTANCE)
        );
        resolveAttribute(SWIM_SPEED_ATTR_ID).ifPresent(holder ->
            builder.add(holder, BASE_SWIM_SPEED)
        );
        return builder;
    }

    private static java.util.Optional<
        Holder.Reference<net.minecraft.world.entity.ai.attributes.Attribute>
    > resolveAttribute(ResourceLocation id) {
        return BuiltInRegistries.ATTRIBUTE.getHolder(id);
    }

    /**
     * 便于后续自定义分类或生成规则时复用。
     */
    public static MobCategory getCategory() {
        return MobCategory.CREATURE;
    }
}
