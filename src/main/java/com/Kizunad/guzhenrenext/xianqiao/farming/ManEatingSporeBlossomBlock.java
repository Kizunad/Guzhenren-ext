package com.Kizunad.guzhenrenext.xianqiao.farming;

import com.Kizunad.guzhenrenext.xianqiao.item.XianqiaoItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class ManEatingSporeBlossomBlock extends BushBlock {

    public static final MapCodec<ManEatingSporeBlossomBlock> CODEC =
        simpleCodec(ManEatingSporeBlossomBlock::new);

    private static final int INITIAL_TRIGGER_DELAY_TICKS = 2;
    private static final int DAMAGE_INTERVAL_TICKS = 20;
    private static final float DAMAGE_AMOUNT = 2.0F;
    private static final int PROOF_DROP_COUNT = 1;
    private static final double BLOCK_CENTER_OFFSET = 0.5D;
    private static final double HURT_BOX_PADDING = 0.1D;
    private static final double HURT_BOX_BOTTOM_OFFSET = 1.2D;
    private static final double HURT_BOX_TOP_OFFSET = 0.2D;

    public ManEatingSporeBlossomBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return hasValidEnvironment(level, pos);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level.isClientSide()) {
            return;
        }
        // given: 食人花刚被放置到世界中。
        // when: 在服务端安排首次捕食调度。
        // then: 使用固定延迟启动，避免随机 tick 造成行为抖动。
        level.scheduleTick(pos, this, INITIAL_TRIGGER_DELAY_TICKS);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // given: P-D01 要求“合法吊挂环境”作为触发门槛。
        // when: 每次服务端 tick 回调先校验当前环境。
        // then: 非法环境直接销毁，不触发伤害与掉落。
        if (!hasValidEnvironment(level, pos)) {
            level.destroyBlock(pos, false);
            return;
        }
        if (tryDamageNearbyTarget(level, pos)) {
            spawnProofDrop(level, pos);
        }
        // given: 捕食行为需节流，防止一 tick 多次重复伤害。
        // when: 本轮处理结束后重新安排下一轮。
        // then: 固定 20 tick 间隔执行，形成确定性的最小持续伤害节奏。
        level.scheduleTick(pos, this, DAMAGE_INTERVAL_TICKS);
    }

    public boolean triggerPredationForTest(ServerLevel level, BlockPos blossomPos) {
        if (!hasValidEnvironment(level, blossomPos)) {
            level.destroyBlock(blossomPos, false);
            return false;
        }
        if (!tryDamageNearbyTarget(level, blossomPos)) {
            return false;
        }
        spawnProofDrop(level, blossomPos);
        level.scheduleTick(blossomPos, this, DAMAGE_INTERVAL_TICKS);
        return true;
    }

    private static boolean hasValidEnvironment(LevelReader level, BlockPos pos) {
        BlockPos abovePos = pos.above();
        return level.getBlockState(abovePos).isFaceSturdy(level, abovePos, Direction.DOWN)
            && level.getBlockState(pos.below()).isAir();
    }

    private static boolean tryDamageNearbyTarget(ServerLevel level, BlockPos centerPos) {
        AABB hurtArea = createHurtArea(centerPos);
        for (LivingEntity livingEntity : level.getEntitiesOfClass(LivingEntity.class, hurtArea)) {
            if (!canBeTarget(livingEntity)) {
                continue;
            }
            if (livingEntity.hurt(level.damageSources().magic(), DAMAGE_AMOUNT)) {
                return true;
            }
        }
        return false;
    }

    private static boolean canBeTarget(LivingEntity livingEntity) {
        if (!livingEntity.isAlive() || livingEntity.isInvulnerable()) {
            return false;
        }
        if (livingEntity instanceof Player player) {
            return !player.isCreative() && !player.isSpectator();
        }
        return true;
    }

    private static AABB createHurtArea(BlockPos centerPos) {
        double minX = centerPos.getX() + HURT_BOX_PADDING;
        double maxX = centerPos.getX() + 1.0D - HURT_BOX_PADDING;
        double minY = centerPos.getY() - HURT_BOX_BOTTOM_OFFSET;
        double maxY = centerPos.getY() + HURT_BOX_TOP_OFFSET;
        double minZ = centerPos.getZ() + HURT_BOX_PADDING;
        double maxZ = centerPos.getZ() + 1.0D - HURT_BOX_PADDING;
        return new AABB(
            minX,
            minY,
            minZ,
            maxX,
            maxY,
            maxZ
        );
    }

    private static void spawnProofDrop(ServerLevel level, BlockPos blossomPos) {
        ItemEntity proofDrop = new ItemEntity(
            level,
            blossomPos.getX() + BLOCK_CENTER_OFFSET,
            blossomPos.getY() + BLOCK_CENTER_OFFSET,
            blossomPos.getZ() + BLOCK_CENTER_OFFSET,
            XianqiaoItems.XUE_PO_LI.toStack(PROOF_DROP_COUNT)
        );
        level.addFreshEntity(proofDrop);
    }
}
