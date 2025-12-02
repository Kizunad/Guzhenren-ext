package com.Kizunad.customNPCs.entity;

import com.Kizunad.customNPCs.ai.config.NpcAttributeDefaults;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 专属自定义 NPC 实体，运行自研 AI（NpcMind + Sensors + Actions）。
 * 仅保留必要的导航能力，移除原版 AI 干扰。
 */
public class CustomNpcEntity extends PathfinderMob {

    public static final String MIND_TAG = "customnpcs:mind_allowed";
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
        this.setCanPickUpLoot(true);

        /*
         * NOTE: FlyingMoveControl
         * 默认 MoveControl 是地面逻辑，假设有重力、地面摩擦，会尝试贴地，无法
         * 正常悬停/升降。飞行实体需要绕过重力/地面摩擦并支持垂直/三维调整，因此要用 FlyingMoveControl 来正确
         * 应用速度和角度。
         */
        if (navigationMode == NavigationMode.FLYING) {
            this.moveControl = new FlyingMoveControl(
                this,
                FLYING_MAX_TURN,
                false
            );
        }
        // 覆盖 super 中创建的默认导航，按模式替换
        this.navigation = createNavigation(level);
    }

    @Override
    protected void registerGoals() {
        // 自定义 AI 全由 NpcMind 驱动，清空原版 Goals 避免干扰。
    }

    @Override
    protected void pickUpItem(
        net.minecraft.world.entity.item.ItemEntity itemEntity
    ) {
        var mindHolder = this.getData(
            com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment.NPC_MIND
        );
        if (mindHolder != null) {
            var mind = mindHolder;
            var inventory = mind.getInventory();
            ItemStack remaining = inventory.addItem(itemEntity.getItem());
            if (remaining.isEmpty()) {
                this.take(itemEntity, itemEntity.getItem().getCount());
                itemEntity.discard();
                return;
            } else {
                itemEntity.setItem(remaining);
            }
        }
        super.pickUpItem(itemEntity);
    }

    @Override
    protected void dropCustomDeathLoot(
        net.minecraft.server.level.ServerLevel level,
        net.minecraft.world.damagesource.DamageSource source,
        boolean causedByPlayer
    ) {
        super.dropCustomDeathLoot(level, source, causedByPlayer);
        if (
            this.hasData(
                com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment.NPC_MIND
            )
        ) {
            var mind = this.getData(
                com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment.NPC_MIND
            );
            var inventory = mind.getInventory();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack stack = inventory.removeItemNoUpdate(i);
                if (!stack.isEmpty()) {
                    this.spawnAtLocation(stack);
                }
            }
        }

        // 确保装备槽（主手/副手/护甲）也掉落，避免仅背包物品被清空
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HAND || slot.isArmor()) {
                ItemStack equipped = this.getItemBySlot(slot);
                if (!equipped.isEmpty()) {
                    this.spawnAtLocation(equipped.copy());
                    this.setItemSlot(slot, ItemStack.EMPTY);
                }
            }
        }
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
        AttributeSupplier.Builder builder = NpcAttributeDefaults.apply(
            AttributeSupplier.builder()
        );
        NpcAttributeDefaults.applyOptionalAttributes(builder);
        return builder;
    }

    /**
     * 便于后续自定义分类或生成规则时复用。
     */
    public static MobCategory getCategory() {
        return MobCategory.CREATURE;
    }
}
