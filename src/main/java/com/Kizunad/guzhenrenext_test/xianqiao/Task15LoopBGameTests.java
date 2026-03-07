package com.Kizunad.guzhenrenext_test.xianqiao;

import com.Kizunad.guzhenrenext.xianqiao.block.XianqiaoBlocks;
import com.Kizunad.guzhenrenext.xianqiao.item.StorageGuData;
import com.Kizunad.guzhenrenext.xianqiao.item.XianqiaoItems;
import com.Kizunad.guzhenrenext.xianqiao.resource.ResourceControllerBlockEntity;
import com.Kizunad.guzhenrenext.xianqiao.spirit.ClusterNpcEntity;
import com.Kizunad.guzhenrenext.xianqiao.spirit.XianqiaoEntities;
import java.lang.reflect.Field;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;

/**
 * Task15 里程碑闭环 B：灵兽培育 + 结构建造联动验收。
 * <p>
 * 本类遵循最小可验收实现原则，仅验证两个关键信号：
 * 1) happy：结构激活后可观察到 ResourceController 反馈，且 ClusterNpc 可向储物蛊推送产出；
 * 2) failure：结构无效且缺失生灵时，反馈与产出均保持为空，链路断开。
 * </p>
 */
@GameTestHolder("guzhenrenext")
public class Task15LoopBGameTests {

    private static final String TASK15_LOOP_B_BATCH = "task15_loop_b";
    private static final int TEST_TIMEOUT_TICKS = 260;
    private static final int BLOCK_SET_FLAGS = Block.UPDATE_ALL;
    private static final int CONTROLLER_RELATIVE_X = 6;
    private static final int CONTROLLER_RELATIVE_Y = 2;
    private static final int CONTROLLER_RELATIVE_Z = 2;
    private static final int NPC_RELATIVE_X = 2;
    private static final int NPC_RELATIVE_Y = 2;
    private static final int NPC_RELATIVE_Z = 2;
    private static final int STORAGE_SLOT_INDEX = 0;
    private static final int SPIRIT_WAIT_TICKS = 100;
    private static final float CONTROLLER_READY_PROGRESS = 10000.0F;
    private static final int CONTROLLER_RECHECK_IMMEDIATE = 0;
    private static final BlockPos[] COMPONENT_OFFSETS = new BlockPos[] {
        new BlockPos(1, 0, 0),
        new BlockPos(-1, 0, 0),
        new BlockPos(0, 0, 1),
        new BlockPos(0, 0, -1)
    };

    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS, batch = TASK15_LOOP_B_BATCH)
    public void testTask15LoopBHappyPathShouldObserveSpiritOutputAndStructureFeedback(GameTestHelper helper) {
        ResourceControllerBlockEntity controller = placeResourceController(helper);
        BlockPos controllerPos = controller.getBlockPos();
        buildValidControllerStructure(helper, controllerPos);
        controller.setItem(ResourceControllerBlockEntity.SLOT_INPUT, new ItemStack(Items.CLOCK));

        // Given: 已摆放完整结构；When: 触发一次重检；Then: 控制器必须进入已激活状态。
        setPrivateIntField(controller, "structureRecheckCooldown", CONTROLLER_RECHECK_IMMEDIATE);
        tickControllerOnce(helper, controller);
        boolean formed = readPrivateBooleanField(controller, "isFormed");
        helper.assertTrue(formed, "happy path: 结构有效时 ResourceController 必须进入已激活状态");

        ClusterNpcEntity clusterNpc = createClusterNpcInTestSpace(helper);
        clusterNpc.setWorkType("farming");
        ItemStack storageGuStack = new ItemStack(XianqiaoItems.STORAGE_GU.get());
        clusterNpc.getInventory().setItem(STORAGE_SLOT_INDEX, storageGuStack);

        // Given: 生灵已进入 farming 且挂载储物蛊；When: 等待稳定步进窗口；Then: 产出与反馈都应可观测。
        helper.runAfterDelay(SPIRIT_WAIT_TICKS, () -> {
            long fragmentCount = StorageGuData
                .fromItemStack(storageGuStack)
                .getCount(XianqiaoItems.HEAVENLY_FRAGMENT.getId());
            helper.assertTrue(fragmentCount > 0L, "happy path: ClusterNpc 应向储物蛊推送至少 1 份生灵产出");

            setPrivateFloatField(controller, "progress", CONTROLLER_READY_PROGRESS);
            tickControllerOnce(helper, controller);
            ItemStack feedbackOutput = controller.getItem(ResourceControllerBlockEntity.SLOT_OUTPUT);
            helper.assertTrue(
                feedbackOutput.is(Items.CLOCK) && feedbackOutput.getCount() > 0,
                "happy path: 结构激活后应在 ResourceController 输出槽观察到反馈产物"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS, batch = TASK15_LOOP_B_BATCH)
    public void testTask15LoopBFailurePathShouldBreakWhenStructureInvalidAndSpiritMissing(GameTestHelper helper) {
        ResourceControllerBlockEntity controller = placeResourceController(helper);
        controller.setItem(ResourceControllerBlockEntity.SLOT_INPUT, new ItemStack(Items.CLOCK));

        // Given: 不摆放结构组件；When: 预置进度并触发一次 tick；Then: isFormed=false 且输出槽仍为空。
        setPrivateIntField(controller, "structureRecheckCooldown", CONTROLLER_RECHECK_IMMEDIATE);
        setPrivateFloatField(controller, "progress", CONTROLLER_READY_PROGRESS);
        tickControllerOnce(helper, controller);
        boolean formed = readPrivateBooleanField(controller, "isFormed");
        helper.assertTrue(!formed, "failure path: 结构缺失时 ResourceController 不应被激活");
        helper.assertTrue(
            controller.getItem(ResourceControllerBlockEntity.SLOT_OUTPUT).isEmpty(),
            "failure path: 结构失效时反馈链路应断开（无输出槽产物）"
        );

        // Given: 不生成生灵实体；When: 等待同等窗口；Then: 储物蛊内生灵产出保持 0。
        ItemStack storageGuStack = new ItemStack(XianqiaoItems.STORAGE_GU.get());
        helper.runAfterDelay(SPIRIT_WAIT_TICKS, () -> {
            long fragmentCount = StorageGuData
                .fromItemStack(storageGuStack)
                .getCount(XianqiaoItems.HEAVENLY_FRAGMENT.getId());
            helper.assertTrue(fragmentCount == 0L, "failure path: 缺失生灵时不应产生任何生灵产出");
            helper.succeed();
        });
    }

    private static ResourceControllerBlockEntity placeResourceController(GameTestHelper helper) {
        BlockPos absolutePos = helper.absolutePos(
            new BlockPos(CONTROLLER_RELATIVE_X, CONTROLLER_RELATIVE_Y, CONTROLLER_RELATIVE_Z)
        );
        helper.getLevel().setBlock(
            absolutePos,
            XianqiaoBlocks.RESOURCE_CONTROLLER.get().defaultBlockState(),
            BLOCK_SET_FLAGS
        );
        BlockEntity blockEntity = helper.getLevel().getBlockEntity(absolutePos);
        helper.assertTrue(blockEntity instanceof ResourceControllerBlockEntity, "Task15: 资源控制器方块实体创建失败");
        return (ResourceControllerBlockEntity) blockEntity;
    }

    /**
     * 构建最小有效结构：
     * 1) 控制器下方放置 RESOURCE_COMPONENT；
     * 2) 周围放置 4 个 TIME_FIELD_COMPONENT，满足最小组件数门槛。
     */
    private static void buildValidControllerStructure(GameTestHelper helper, BlockPos controllerPos) {
        helper.getLevel().setBlock(
            controllerPos.below(),
            XianqiaoBlocks.RESOURCE_COMPONENT.get().defaultBlockState(),
            BLOCK_SET_FLAGS
        );
        for (BlockPos offset : COMPONENT_OFFSETS) {
            helper.getLevel().setBlock(
                controllerPos.offset(offset),
                XianqiaoBlocks.TIME_FIELD_COMPONENT.get().defaultBlockState(),
                BLOCK_SET_FLAGS
            );
        }
    }

    private static ClusterNpcEntity createClusterNpcInTestSpace(GameTestHelper helper) {
        ClusterNpcEntity clusterNpc = XianqiaoEntities.CLUSTER_NPC.get().create(helper.getLevel());
        helper.assertTrue(clusterNpc != null, "Task15: 集群 NPC 创建失败");
        BlockPos absolutePos = helper.absolutePos(new BlockPos(NPC_RELATIVE_X, NPC_RELATIVE_Y, NPC_RELATIVE_Z));
        clusterNpc.setPos(absolutePos.getX(), absolutePos.getY(), absolutePos.getZ());
        helper.getLevel().addFreshEntity(clusterNpc);
        return clusterNpc;
    }

    private static void tickControllerOnce(GameTestHelper helper, ResourceControllerBlockEntity controller) {
        ResourceControllerBlockEntity.serverTick(
            helper.getLevel(),
            controller.getBlockPos(),
            helper.getLevel().getBlockState(controller.getBlockPos()),
            controller
        );
    }

    private static boolean readPrivateBooleanField(Object target, String fieldName) {
        Field field = readPrivateField(target, fieldName);
        try {
            return field.getBoolean(target);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("读取私有布尔字段失败: " + fieldName, exception);
        }
    }

    private static void setPrivateIntField(Object target, String fieldName, int value) {
        Field field = readPrivateField(target, fieldName);
        try {
            field.setInt(target, value);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("写入私有整数字段失败: " + fieldName, exception);
        }
    }

    private static void setPrivateFloatField(Object target, String fieldName, float value) {
        Field field = readPrivateField(target, fieldName);
        try {
            field.setFloat(target, value);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("写入私有浮点字段失败: " + fieldName, exception);
        }
    }

    private static Field readPrivateField(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException exception) {
            throw new IllegalStateException("读取私有字段失败: " + fieldName, exception);
        }
    }
}
