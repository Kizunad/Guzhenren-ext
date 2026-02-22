package com.Kizunad.guzhenrenext_test.xianqiao;

import com.Kizunad.guzhenrenext.xianqiao.item.StorageGuData;
import com.Kizunad.guzhenrenext.xianqiao.item.XianqiaoItems;
import com.Kizunad.guzhenrenext.xianqiao.spirit.ClusterNpcEntity;
import com.Kizunad.guzhenrenext.xianqiao.spirit.XianqiaoEntities;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.slf4j.Logger;

/**
 * 集群种田最小闭环测试。
 * <p>
 * 目标：验证“集群 NPC 产出 -> 推送到储物蛊”这条最小链路在 headless GameTest 环境可稳定运行。
 * 测试步骤严格对应任务要求：
 * 1) 生成一个 ClusterNpcEntity；
 * 2) 向其 0 号背包槽位放入一个 Storage Gu；
 * 3) 等待约 5 秒（100 tick）；
 * 4) 断言储物蛊内 heavenly_fragment 数量大于 0。
 * </p>
 */
@GameTestHolder("guzhenrenext")
public class ClusterFarmingTest {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int TEST_TIMEOUT_TICKS = 160;
    private static final int WAIT_TICKS = 100;
    private static final int ENTITY_RELATIVE_X = 2;
    private static final int ENTITY_RELATIVE_Y = 2;
    private static final int ENTITY_RELATIVE_Z = 2;
    private static final int STORAGE_SLOT_INDEX = 0;

    /**
     * 验证集群种田最小闭环：在等待 100 tick 后，储物蛊应收到至少 1 个九天碎片。
     */
    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS)
    public void testClusterNpcShouldPushOutputIntoStorageGuAfterFiveSeconds(GameTestHelper helper) {
        LOGGER.info("[ClusterFarmingTest] testClusterNpcShouldPushOutputIntoStorageGuAfterFiveSeconds started");
        ClusterNpcEntity clusterNpc = createClusterNpcInTestSpace(helper);
        // 显式切换为工作态，避免默认 idle 导致测试语义与生产链路意图不一致。
        clusterNpc.setWorkType("farming");
        ItemStack storageGuStack = new ItemStack(XianqiaoItems.STORAGE_GU.get());
        clusterNpc.getInventory().setItem(STORAGE_SLOT_INDEX, storageGuStack);

        // 延迟到 100 tick 后再断言，确保至少经历多轮生产与推送步进。
        helper.runAfterDelay(WAIT_TICKS, () -> {
            long fragmentCount = StorageGuData
                .fromItemStack(storageGuStack)
                .getCount(XianqiaoItems.HEAVENLY_FRAGMENT.getId());

            helper.assertTrue(
                fragmentCount > 0L,
                "等待 100 tick 后，储物蛊内 heavenly_fragment 数量应大于 0"
            );
            LOGGER.info(
                "[ClusterFarmingTest] testClusterNpcShouldPushOutputIntoStorageGuAfterFiveSeconds passed"
                    + ", fragmentCount={}",
                fragmentCount
            );
            helper.succeed();
        });
    }

    /**
     * 在 GameTest 空间内创建并放置集群 NPC 实体。
     *
     * @param helper 测试辅助对象
     * @return 已加入世界的集群 NPC
     */
    private static ClusterNpcEntity createClusterNpcInTestSpace(GameTestHelper helper) {
        ClusterNpcEntity clusterNpc = XianqiaoEntities.CLUSTER_NPC.get().create(helper.getLevel());
        helper.assertTrue(clusterNpc != null, "集群 NPC 创建失败：注册类型未就绪或世界无效");

        BlockPos absolutePos = helper.absolutePos(
            new BlockPos(ENTITY_RELATIVE_X, ENTITY_RELATIVE_Y, ENTITY_RELATIVE_Z)
        );
        clusterNpc.setPos(absolutePos.getX(), absolutePos.getY(), absolutePos.getZ());
        helper.getLevel().addFreshEntity(clusterNpc);
        return clusterNpc;
    }
}
