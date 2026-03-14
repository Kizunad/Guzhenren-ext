package com.Kizunad.guzhenrenext.xianqiao.farming;

import com.Kizunad.guzhenrenext.xianqiao.item.XianqiaoItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;

public class LightningAttractingFernBlock extends BushBlock {

    public static final MapCodec<LightningAttractingFernBlock> CODEC = simpleCodec(LightningAttractingFernBlock::new);

    private static final int BLOCK_UPDATE_FLAGS = 3;
    private static final int PROOF_DROP_COUNT = 1;
    private static final int FEEDBACK_PARTICLE_COUNT = 10;
    private static final double BLOCK_CENTER_OFFSET = 0.5D;
    private static final double FEEDBACK_PARTICLE_OFFSET = 0.2D;
    private static final double FEEDBACK_PARTICLE_SPEED = 0.02D;
    private static final float FEEDBACK_SOUND_VOLUME = 0.8F;
    private static final float FEEDBACK_SOUND_PITCH = 1.0F;
    private static final String SUCCESS_DROP_NAME_TAG = "引雷草·雷萤凝砂";

    public LightningAttractingFernBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    public boolean triggerLightningReactionForTest(ServerLevel level, BlockPos fernPos) {
        if (!isValidActivationEnvironment(level, fernPos)) {
            return false;
        }
        spawnLightning(level, fernPos);
        level.setBlock(fernPos, Blocks.DEAD_BUSH.defaultBlockState(), BLOCK_UPDATE_FLAGS);
        spawnProofDrop(level, fernPos);
        emitServerFeedback(level, fernPos);
        return true;
    }

    private boolean isValidActivationEnvironment(ServerLevel level, BlockPos fernPos) {
        // given: 触发只允许在目标方块仍是“引雷草”时进行，避免重复或越位触发。
        // when: 读取当前位置方块状态并校验方块身份。
        // then: 非引雷草状态应直接拒绝触发。
        BlockState state = level.getBlockState(fernPos);
        if (!state.is(this)) {
            return false;
        }
        // given: P-D02 的环境门槛要求“开放天空/无遮盖”。
        // when: 以引雷草上方一格作为天穹观测点。
        // then: 仅在能直视天空时允许进入雷击反应。
        return level.getBlockState(fernPos.above()).isAir() && level.canSeeSky(fernPos);
    }

    private static void spawnLightning(ServerLevel level, BlockPos fernPos) {
        LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(level);
        if (lightningBolt == null) {
            return;
        }
        lightningBolt.moveTo(
            fernPos.getX() + BLOCK_CENTER_OFFSET,
            fernPos.getY(),
            fernPos.getZ() + BLOCK_CENTER_OFFSET
        );
        lightningBolt.setVisualOnly(true);
        level.addFreshEntity(lightningBolt);
    }

    private static void spawnProofDrop(ServerLevel level, BlockPos fernPos) {
        ItemEntity proofDrop = new ItemEntity(
            level,
            fernPos.getX() + BLOCK_CENTER_OFFSET,
            fernPos.getY() + BLOCK_CENTER_OFFSET,
            fernPos.getZ() + BLOCK_CENTER_OFFSET,
            XianqiaoItems.LEI_YING_SHA.toStack(PROOF_DROP_COUNT)
        );
        proofDrop.setCustomName(Component.literal(SUCCESS_DROP_NAME_TAG));
        proofDrop.setCustomNameVisible(true);
        level.addFreshEntity(proofDrop);
    }

    private static void emitServerFeedback(ServerLevel level, BlockPos fernPos) {
        level.sendParticles(
            ParticleTypes.CRIT,
            fernPos.getX() + BLOCK_CENTER_OFFSET,
            fernPos.getY() + BLOCK_CENTER_OFFSET,
            fernPos.getZ() + BLOCK_CENTER_OFFSET,
            FEEDBACK_PARTICLE_COUNT,
            FEEDBACK_PARTICLE_OFFSET,
            FEEDBACK_PARTICLE_OFFSET,
            FEEDBACK_PARTICLE_OFFSET,
            FEEDBACK_PARTICLE_SPEED
        );
        level.playSound(
            null,
            fernPos,
            SoundEvents.LIGHTNING_BOLT_IMPACT,
            SoundSource.BLOCKS,
            FEEDBACK_SOUND_VOLUME,
            FEEDBACK_SOUND_PITCH
        );
    }
}
