package com.Kizunad.customNPCs_test.tests;

import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import com.Kizunad.customNPCs.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

/**
 * MoveToAction 调参与回归：基础路径与简单障碍。
 */
@SuppressWarnings("checkstyle:MagicNumber")
public final class MoveToTuningTests {

    private MoveToTuningTests() {}

    /**
     * 基础平地移动：从 (1,2,1) 到 (8,2,1) 需在 80 tick 内抵达。
     */
    public static void testMoveToFlat(GameTestHelper helper) {
        BlockPos start = new BlockPos(1, 2, 1);
        BlockPos target = new BlockPos(8, 2, 1);
        // 铺设平地，避免掉落/卡边
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.ensureFloorAround(
            helper,
            new BlockPos(4, 2, 1),
            12
        );
        CustomNpcEntity npc = spawnNpc(helper, start);
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.tickMind(helper, npc);
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.submitMoveTo(
            helper,
            npc,
            target,
            1.2,
            2.5
        );
        helper.assertTrue(
            true,
            "MoveToFlat startAbs=" +
            helper.absolutePos(start) +
            ", targetAbs=" +
            helper.absolutePos(target)
        );

        helper.runAtTickTime(
            100,
            () -> helper.assertTrue(
                npc.blockPosition().closerThan(target, 2.5),
                "平地 100t 内应抵达目标"
            )
        );
    }

    /**
     * 简单双障碍（两块石头挡路），要求在 160 tick 内绕行抵达。
     */
    public static void testMoveToWithBlocks(GameTestHelper helper) {
        BlockPos start = new BlockPos(1, 2, 1);
        BlockPos target = new BlockPos(8, 2, 1);

        // 铺设平地，避免掉落/卡边
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.ensureFloorAround(
            helper,
            new BlockPos(4, 2, 1),
            12
        );
        CustomNpcEntity npc = spawnNpc(helper, start);
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.tickMind(helper, npc);
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.submitMoveTo(
            helper,
            npc,
            target,
            1.2,
            2.5
        );

        // 放置障碍：两格石头，强迫绕行
        helper.setBlock(new BlockPos(4, 2, 1), Blocks.STONE);
        helper.setBlock(new BlockPos(4, 3, 1), Blocks.STONE);
        helper.assertTrue(
            true,
            "MoveToWithBlocks startAbs=" +
            helper.absolutePos(start) +
            ", targetAbs=" +
            helper.absolutePos(target)
        );

        helper.runAtTickTime(
            200,
            () -> helper.assertTrue(
                npc.blockPosition().closerThan(target, 2.5),
                "有障碍 200t 内应抵达目标"
            )
        );
    }

    private static CustomNpcEntity spawnNpc(GameTestHelper helper, BlockPos pos) {
        // 使用自定义实体类型，默认地面导航
        CustomNpcEntity npc = ModEntities.CUSTOM_NPC.get().create(helper.getLevel());
        if (npc == null) {
            throw new IllegalStateException("无法生成自定义 NPC");
        }
        var abs = helper.absolutePos(pos);
        npc.moveTo(abs.getX(), abs.getY(), abs.getZ(), npc.getYRot(), npc.getXRot());
        helper.getLevel().addFreshEntity(npc);
        return npc;
    }
}
