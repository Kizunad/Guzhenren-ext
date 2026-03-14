package com.Kizunad.guzhenrenext_test.xianqiao;

import com.Kizunad.guzhenrenext.xianqiao.farming.FarmingBlocks;
import com.Kizunad.guzhenrenext.xianqiao.farming.FarmingItems;
import com.Kizunad.guzhenrenext.xianqiao.farming.ManEatingSporeBlossomBlock;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("guzhenrenext")
@PrefixGameTestTemplate(false)
public class Task19PD01ManEatingFlowerGameTests {

    private static final String TASK19_PD01_BATCH = "task19_p_d01_man_eating_flower";
    private static final int TEST_TIMEOUT_TICKS = 140;
    private static final int ASSERT_DELAY_TICKS = 2;
    private static final float HEALTH_COMPARE_EPSILON = 0.0001F;
    private static final int PROOF_DROP_MIN_COUNT = 1;
    private static final double ZOMBIE_SPAWN_OFFSET = 0.5D;
    private static final float ZOMBIE_INITIAL_HEALTH = 12.0F;
    private static final BlockPos HAPPY_FLOWER_POS = new BlockPos(2, 2, 2);
    private static final BlockPos FAILURE_FLOWER_POS = new BlockPos(5, 2, 2);
    private static final double SCAN_RADIUS = 1.2D;
    private static final double SCAN_HEIGHT_MARGIN = 1.2D;

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK19_PD01_BATCH
    )
    public void testTask19PD01HappyPathShouldDamageNearbyEntityAndDropProof(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos flowerPos = helper.absolutePos(HAPPY_FLOWER_POS);
        ManEatingSporeBlossomBlock flowerBlock = FarmingBlocks.MAN_EATING_SPORE_BLOSSOM.get();

        level.setBlockAndUpdate(flowerPos.above(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(flowerPos.below(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(flowerPos, flowerBlock.defaultBlockState());

        Zombie target = createZombie(level, flowerPos);
        float healthBeforeTrigger = target.getHealth();
        boolean triggered = flowerBlock.triggerPredationForTest(level, flowerPos);

        helper.runAfterDelay(ASSERT_DELAY_TICKS, () -> {
            AABB scanArea = createScanArea(flowerPos);
            List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, scanArea);
            helper.assertTrue(triggered, "happy path: 合法吊挂开阔环境下食人花应触发捕食");
            helper.assertTrue(
                target.getHealth() < healthBeforeTrigger - HEALTH_COMPARE_EPSILON,
                "happy path: 服务端应对附近实体造成可观测伤害"
            );
            helper.assertTrue(
                countItems(drops, FarmingItems.XUE_PO_LI.get()) >= PROOF_DROP_MIN_COUNT,
                "happy path: 触发后应产出证明物 XUE_PO_LI"
            );
            helper.succeed();
        });
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK19_PD01_BATCH
    )
    public void testTask19PD01FailurePathShouldRejectInvalidEnvironmentAndDropNothing(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos flowerPos = helper.absolutePos(FAILURE_FLOWER_POS);
        ManEatingSporeBlossomBlock flowerBlock = FarmingBlocks.MAN_EATING_SPORE_BLOSSOM.get();

        level.setBlockAndUpdate(flowerPos.above(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(flowerPos.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(flowerPos, flowerBlock.defaultBlockState());

        BlockState flowerState = level.getBlockState(flowerPos);
        Zombie target = createZombie(level, flowerPos);
        float healthBeforeTrigger = target.getHealth();
        boolean triggered = flowerBlock.triggerPredationForTest(level, flowerPos);

        helper.runAfterDelay(ASSERT_DELAY_TICKS, () -> {
            AABB scanArea = createScanArea(flowerPos);
            List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, scanArea);
            helper.assertFalse(
                flowerState.canSurvive(level, flowerPos),
                "failure path: 非法环境下食人花不应满足稳定存活门槛"
            );
            helper.assertFalse(triggered, "failure path: 非法环境下食人花不应触发捕食逻辑");
            helper.assertTrue(
                target.getHealth() >= healthBeforeTrigger - HEALTH_COMPARE_EPSILON,
                "failure path: 非法环境下不应对附近实体造成伤害"
            );
            helper.assertTrue(
                !level.getBlockState(flowerPos).is(flowerBlock),
                "failure path: 非法环境触发后方块不应保持稳定存在"
            );
            helper.assertTrue(
                countItems(drops, FarmingItems.XUE_PO_LI.get()) == 0,
                "failure path: 非法环境下不应产出 XUE_PO_LI"
            );
            helper.succeed();
        });
    }

    private static Zombie createZombie(ServerLevel level, BlockPos centerPos) {
        Zombie zombie = EntityType.ZOMBIE.create(level);
        if (zombie == null) {
            throw new IllegalStateException("failed to create zombie for task19_p_d01 test");
        }
        zombie.setNoAi(true);
        zombie.setPos(centerPos.getX() + ZOMBIE_SPAWN_OFFSET, centerPos.getY(), centerPos.getZ() + ZOMBIE_SPAWN_OFFSET);
        zombie.setHealth(ZOMBIE_INITIAL_HEALTH);
        level.addFreshEntity(zombie);
        return zombie;
    }

    private static int countItems(List<ItemEntity> drops, Item targetItem) {
        return drops.stream()
            .filter(entity -> entity.getItem().is(targetItem))
            .mapToInt(entity -> entity.getItem().getCount())
            .sum();
    }

    private static AABB createScanArea(BlockPos centerPos) {
        return new AABB(
            centerPos.getX() - SCAN_RADIUS,
            centerPos.getY() - SCAN_HEIGHT_MARGIN,
            centerPos.getZ() - SCAN_RADIUS,
            centerPos.getX() + 1 + SCAN_RADIUS,
            centerPos.getY() + 1 + SCAN_HEIGHT_MARGIN,
            centerPos.getZ() + 1 + SCAN_RADIUS
        );
    }
}
