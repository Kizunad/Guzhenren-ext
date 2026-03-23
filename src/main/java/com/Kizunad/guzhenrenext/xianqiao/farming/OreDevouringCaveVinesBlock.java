package com.Kizunad.guzhenrenext.xianqiao.farming;

import com.Kizunad.guzhenrenext.xianqiao.item.XianqiaoItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.CaveVinesBlock;
import net.minecraft.world.level.block.state.BlockState;

public class OreDevouringCaveVinesBlock extends CaveVinesBlock {

    private static final int BLOCK_UPDATE_FLAGS = 3;
    private static final int MAX_VERTICAL_SCAN_DISTANCE = 1;
    private static final int DROP_SINGLE_COUNT = 1;
    private static final int REFINED_DROP_SINGLE_COUNT = 1;
    private static final int FEEDBACK_PARTICLE_COUNT = 8;
    private static final double BLOCK_CENTER_OFFSET = 0.5D;
    private static final double FEEDBACK_PARTICLE_OFFSET = 0.2D;
    private static final double FEEDBACK_PARTICLE_SPEED = 0.01D;
    private static final float FEEDBACK_SOUND_VOLUME = 0.8F;
    private static final float FEEDBACK_SOUND_PITCH = 0.7F;
    private static final String SUCCESS_NAME_TAG = "噬金藤·吞矿成精";
    private static final String REFINED_SUCCESS_NAME_TAG = "噬金藤·万象副产";

    public OreDevouringCaveVinesBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level.isClientSide()) {
            return;
        }
        // given: 新放置噬金藤。
        // when: 服务端执行一次邻位矿块检测。
        // then: 仅在命中上下矿块时触发单次吞噬。
        tryConsumeAdjacentOreOnce((ServerLevel) level, pos, state);
    }

    private static void tryConsumeAdjacentOreOnce(ServerLevel level, BlockPos vinePos, BlockState vineState) {
        // given: berries=true 代表该藤株已完成吞噬。
        // when: 再次触发 onPlace/neighborChanged。
        // then: 直接返回，保证“只吞噬一次”。
        if (vineState.hasProperty(CaveVines.BERRIES) && vineState.getValue(CaveVines.BERRIES)) {
            return;
        }
        BlockPos orePos = resolveOreTarget(vinePos, level);
        if (orePos == null) {
            return;
        }
        BlockState primaryOreState = level.getBlockState(orePos);
        BlockPos secondaryOrePos = resolveSecondaryOreTarget(vinePos, level, orePos);
        level.setBlock(orePos, Blocks.STONE.defaultBlockState(), BLOCK_UPDATE_FLAGS);
        if (vineState.hasProperty(CaveVines.BERRIES)) {
            level.setBlock(vinePos, vineState.setValue(CaveVines.BERRIES, true), BLOCK_UPDATE_FLAGS);
        }
        ItemEntity essenceDrop = new ItemEntity(
            level,
            orePos.getX() + BLOCK_CENTER_OFFSET,
            orePos.getY() + BLOCK_CENTER_OFFSET,
            orePos.getZ() + BLOCK_CENTER_OFFSET,
            FarmingItems.JIN_SUI_XIE.toStack(DROP_SINGLE_COUNT)
        );
        essenceDrop.setCustomName(Component.literal(SUCCESS_NAME_TAG));
        essenceDrop.setCustomNameVisible(true);
        level.addFreshEntity(essenceDrop);
        tryDropRefinedByproduct(level, vinePos, primaryOreState, secondaryOrePos);
        level.sendParticles(
            ParticleTypes.SMOKE,
            vinePos.getX() + BLOCK_CENTER_OFFSET,
            vinePos.getY() + BLOCK_CENTER_OFFSET,
            vinePos.getZ() + BLOCK_CENTER_OFFSET,
            FEEDBACK_PARTICLE_COUNT,
            FEEDBACK_PARTICLE_OFFSET,
            FEEDBACK_PARTICLE_OFFSET,
            FEEDBACK_PARTICLE_OFFSET,
            FEEDBACK_PARTICLE_SPEED
        );
        level.playSound(
            null,
            vinePos,
            SoundEvents.ENDERMITE_HURT,
            SoundSource.BLOCKS,
            FEEDBACK_SOUND_VOLUME,
            FEEDBACK_SOUND_PITCH
        );
    }

    private static void tryDropRefinedByproduct(
        ServerLevel level,
        BlockPos vinePos,
        BlockState primaryOreState,
        BlockPos secondaryOrePos
    ) {
        if (!isGoldBearingOre(primaryOreState) || secondaryOrePos == null) {
            return;
        }
        BlockState secondaryOreState = level.getBlockState(secondaryOrePos);
        level.setBlock(secondaryOrePos, Blocks.COBBLESTONE.defaultBlockState(), BLOCK_UPDATE_FLAGS);
        if (!isRefineCatalystOre(secondaryOreState)) {
            return;
        }
        ItemEntity refinedDrop = new ItemEntity(
            level,
            vinePos.getX() + BLOCK_CENTER_OFFSET,
            vinePos.getY() + BLOCK_CENTER_OFFSET,
            vinePos.getZ() + BLOCK_CENTER_OFFSET,
            XianqiaoItems.WAN_XIANG_JIN_SHA.toStack(REFINED_DROP_SINGLE_COUNT)
        );
        refinedDrop.setCustomName(Component.literal(REFINED_SUCCESS_NAME_TAG));
        refinedDrop.setCustomNameVisible(true);
        level.addFreshEntity(refinedDrop);
    }

    private static BlockPos resolveOreTarget(BlockPos vinePos, BlockGetter level) {
        BlockPos upPos = vinePos.above(MAX_VERTICAL_SCAN_DISTANCE);
        BlockPos downPos = vinePos.below(MAX_VERTICAL_SCAN_DISTANCE);
        if (isOreBlock(level.getBlockState(upPos))) {
            return upPos;
        }
        if (isOreBlock(level.getBlockState(downPos))) {
            return downPos;
        }
        return null;
    }

    private static BlockPos resolveSecondaryOreTarget(BlockPos vinePos, BlockGetter level, BlockPos primaryOrePos) {
        BlockPos upPos = vinePos.above(MAX_VERTICAL_SCAN_DISTANCE);
        BlockPos downPos = vinePos.below(MAX_VERTICAL_SCAN_DISTANCE);
        if (!upPos.equals(primaryOrePos) && isOreBlock(level.getBlockState(upPos))) {
            return upPos;
        }
        if (!downPos.equals(primaryOrePos) && isOreBlock(level.getBlockState(downPos))) {
            return downPos;
        }
        return null;
    }

    private static boolean isOreBlock(BlockState state) {
        return state.is(Blocks.COAL_ORE)
            || state.is(Blocks.DEEPSLATE_COAL_ORE)
            || state.is(Blocks.IRON_ORE)
            || state.is(Blocks.DEEPSLATE_IRON_ORE)
            || state.is(Blocks.COPPER_ORE)
            || state.is(Blocks.DEEPSLATE_COPPER_ORE)
            || state.is(Blocks.GOLD_ORE)
            || state.is(Blocks.DEEPSLATE_GOLD_ORE)
            || state.is(Blocks.LAPIS_ORE)
            || state.is(Blocks.DEEPSLATE_LAPIS_ORE)
            || state.is(Blocks.REDSTONE_ORE)
            || state.is(Blocks.DEEPSLATE_REDSTONE_ORE)
            || state.is(Blocks.DIAMOND_ORE)
            || state.is(Blocks.DEEPSLATE_DIAMOND_ORE)
            || state.is(Blocks.EMERALD_ORE)
            || state.is(Blocks.DEEPSLATE_EMERALD_ORE)
            || state.is(Blocks.NETHER_GOLD_ORE)
            || state.is(Blocks.NETHER_QUARTZ_ORE)
            || state.is(Blocks.ANCIENT_DEBRIS);
    }

    private static boolean isGoldBearingOre(BlockState state) {
        return state.is(Blocks.GOLD_ORE)
            || state.is(Blocks.DEEPSLATE_GOLD_ORE)
            || state.is(Blocks.NETHER_GOLD_ORE);
    }

    private static boolean isRefineCatalystOre(BlockState state) {
        return state.is(Blocks.IRON_ORE)
            || state.is(Blocks.DEEPSLATE_IRON_ORE)
            || state.is(Blocks.COPPER_ORE)
            || state.is(Blocks.DEEPSLATE_COPPER_ORE)
            || state.is(Blocks.COAL_ORE)
            || state.is(Blocks.DEEPSLATE_COAL_ORE);
    }
}
