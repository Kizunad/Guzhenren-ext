package com.Kizunad.guzhenrenext.bastion.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * 地灵实体。
 * <p>
 * 继承自 Fox 以复用 FoxModel。
 * 这种实体类似于幽灵/灵体，漂浮在空中，作为基地的某种守护或管理单位。
 * </p>
 */
public class LandSpiritEntity extends Fox {

    private static final double MAX_HEALTH = 10.0D;
    private static final double MOVEMENT_SPEED = 0.2D;
    private static final double FLYING_SPEED = 0.4D;
    private static final double VERTICAL_DRAG = 0.9D;

    public LandSpiritEntity(EntityType<? extends Fox> type, Level level) {
        super(type, level);
        this.noPhysics = true; // 灵体不参与物理碰撞（穿墙/漂浮）
        this.setNoGravity(true); // 无重力
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Fox.createAttributes()
            .add(Attributes.MAX_HEALTH, MAX_HEALTH)
            .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
            .add(Attributes.FLYING_SPEED, FLYING_SPEED);
    }

    @Override
    protected void registerGoals() {
        // 清空 Fox 默认的 AI，地灵不需要睡觉、吃浆果等行为
        // 我们这里只添加基本的
        this.goalSelector.addGoal(0, new net.minecraft.world.entity.ai.goal.FloatGoal(this));
        // 可以添加 LookAtPlayerGoal 等
    }

    @Override
    public void tick() {
        super.tick();
        // 简单的上下浮动效果
        if (!this.level().isClientSide) {
            // 确保不会因为无重力而一直上升，且保持悬停
            this.setDeltaMovement(this.getDeltaMovement().multiply(VERTICAL_DRAG, VERTICAL_DRAG, VERTICAL_DRAG));
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        // 如果有自定义数据同步，在这里定义
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        // 保存额外数据
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        // 读取额外数据
    }

    @Nullable
    @Override
    public Fox getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null; // 地灵不繁殖
    }

    @Override
    public boolean isFood(net.minecraft.world.item.ItemStack stack) {
        return false; // 不能喂食
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        // 灵体没有脚步声
    }
}
