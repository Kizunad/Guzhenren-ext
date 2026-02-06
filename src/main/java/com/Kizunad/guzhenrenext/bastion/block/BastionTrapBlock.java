package com.Kizunad.guzhenrenext.bastion.block;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeConfig;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;

/**
 * 陷阱节点方块。
 * <p>
 * Round 25：挂载在 Anchor 上的陷阱节点，对范围内敌对目标施加减速与虚弱。
 * </p>
 */
public class BastionTrapBlock extends Block {

    /** 减速等级（Amplifier）。 */
    private static final int SLOWNESS_AMPLIFIER = 1;
    /** 虚弱等级（Amplifier）。 */
    private static final int WEAKNESS_AMPLIFIER = 0;

    public BastionTrapBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        // 作为功能节点，不允许路径寻找穿过。
        return false;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // 必须依附在 Anchor 上。
        BlockState below = level.getBlockState(pos.below());
        return below.getBlock() instanceof BastionAnchorBlock;
    }

    @Override
    public BlockState updateShape(
            BlockState state,
            net.minecraft.core.Direction direction,
            BlockState neighborState,
            net.minecraft.world.level.LevelAccessor level,
            BlockPos currentPos,
            BlockPos neighborPos) {
        // Anchor 被拆则掉落。
        if (direction == net.minecraft.core.Direction.DOWN && !canSurvive(state, level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public void onRemove(BlockState state,
                         Level level,
                         BlockPos pos,
                         BlockState newState,
                         boolean isMoving) {
        if (!level.isClientSide
            && state.getBlock() != newState.getBlock()
            && level instanceof ServerLevel serverLevel) {
            // 清理归属缓存，避免悬空引用。
            BastionSavedData savedData = BastionSavedData.get(serverLevel);
            savedData.removeNodeOwnershipIfPresent(pos);
            savedData.clearTrapCooldown(pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public void entityInside(BlockState state, Level level, BlockPos pos, LivingEntity entity) {
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        BastionSavedData savedData = BastionSavedData.get(serverLevel);
        BastionData bastion = savedData.findOwnerBastionByIndex(pos);
        if (bastion == null) {
            return;
        }

        BastionTypeConfig typeConfig = BastionTypeManager.getOrDefault(bastion.bastionType());
        BastionTypeConfig.TrapConfig trapConfig = typeConfig.trap();
        if (trapConfig == null || !trapConfig.enabled()) {
            return;
        }

        long gameTime = serverLevel.getGameTime();
        long nextAllowed = savedData.getNextTrapTryTick(pos);
        if (gameTime < nextAllowed) {
            return;
        }

        int cooldown = Math.max(1, trapConfig.cooldownTicks());
        savedData.setNextTrapTryTick(pos, gameTime + cooldown);

        if (!isHostile(entity, bastion)) {
            return;
        }

        // 触发范围：使用配置的 triggerRadius。
        int radius = Math.max(1, trapConfig.triggerRadius());
        int duration = Math.max(1, trapConfig.effectDuration());

        AABB box = new AABB(pos).inflate(radius);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, e -> isHostile(e, bastion))) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, SLOWNESS_AMPLIFIER));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, WEAKNESS_AMPLIFIER));
        }
    }

    private boolean isHostile(LivingEntity entity, BastionData bastion) {
        if (entity == null || !entity.isAlive()) {
            return false;
        }
        if (entity instanceof Player player) {
            // 创造模式不应被陷阱影响。
            if (player.isCreative()) {
                return false;
            }
            // 基地友方（主人/接管者）不应被陷阱影响。
            if (bastion != null && bastion.isFriendlyTo(player.getUUID())) {
                return false;
            }
        }
        return true;
    }
}
