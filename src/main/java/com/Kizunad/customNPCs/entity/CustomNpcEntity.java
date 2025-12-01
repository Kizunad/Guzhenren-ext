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

/**
 * 专属自定义 NPC 实体，运行自研 AI（NpcMind + Sensors + Actions）。
 * 仅保留必要的导航能力，移除原版 AI 干扰。
 */
public class CustomNpcEntity extends PathfinderMob {

    public static final String MIND_TAG = "customnpcs:mind_allowed";
    private static final double BASE_HEALTH = 20.0D;
    private static final double BASE_ATTACK = 4.0D;
    private static final double BASE_MOVE_SPEED = 0.32D;
    private static final double BASE_ARMOR = 2.0D;
    private static final int FLYING_MAX_TURN = 10;

    public enum NavigationMode {
        GROUND,
        FLYING,
        WATER,
        AMPHIBIOUS,
        WALL,
    }

    private final NavigationMode navigationMode;

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
    }

    @Override
    protected void registerGoals() {
        // 自定义 AI 全由 NpcMind 驱动，清空原版 Goals 避免干扰。
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return switch (navigationMode) {
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
        return AttributeSupplier.builder()
            .add(Attributes.MAX_HEALTH, BASE_HEALTH)
            .add(Attributes.ATTACK_DAMAGE, BASE_ATTACK)
            .add(Attributes.MOVEMENT_SPEED, BASE_MOVE_SPEED)
            .add(Attributes.ARMOR, BASE_ARMOR);
    }

    /**
     * 便于后续自定义分类或生成规则时复用。
     */
    public static MobCategory getCategory() {
        return MobCategory.CREATURE;
    }
}
