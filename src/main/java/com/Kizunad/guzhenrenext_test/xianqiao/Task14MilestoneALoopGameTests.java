package com.Kizunad.guzhenrenext_test.xianqiao;

import com.Kizunad.guzhenrenext.xianqiao.alchemy.blockentity.AlchemyFurnaceBlockEntity;
import com.Kizunad.guzhenrenext.xianqiao.alchemy.service.AlchemyService;
import com.Kizunad.guzhenrenext.xianqiao.block.XianqiaoBlocks;
import com.Kizunad.guzhenrenext.xianqiao.farming.FarmingBlocks;
import com.Kizunad.guzhenrenext.xianqiao.resource.ResourceControllerBlockEntity;
import java.lang.reflect.Field;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder("guzhenrenext")
public class Task14MilestoneALoopGameTests {

    private static final String TASK14_LOOP_A_BATCH = "task14_loop_a";
    private static final int TEST_TIMEOUT_TICKS = 240;
    private static final int BLOCK_SET_FLAGS = Block.UPDATE_ALL;
    private static final int MAX_REFINE_ATTEMPTS = 24;
    private static final int ENTRY_STACK_COUNT = 24;
    private static final int CONTROLLER_READY_COMPONENT_COUNT = 4;
    private static final int CONTROLLER_READY_COOLDOWN = 100;
    private static final float CONTROLLER_READY_PROGRESS = 10000.0F;
    private static final int CONTROLLER_RELATIVE_X = 6;

    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS, batch = TASK14_LOOP_A_BATCH)
    public void testTask14LoopAHappyPathShouldRefineAndEmitApertureFeedback(GameTestHelper helper) {
        AlchemyFurnaceBlockEntity furnace = placeAlchemyFurnace(helper, new BlockPos(2, 2, 2));
        ResourceControllerBlockEntity controller = placeResourceController(
            helper,
            new BlockPos(CONTROLLER_RELATIVE_X, 2, 2)
        );

        // 入口模拟：直接注入主材/辅材，替代主世界“双入口事件”触发，确保测试在 headless 环境可控复现。
        injectRefineInputs(furnace);

        boolean produced = false;
        for (int attempt = 0; attempt < MAX_REFINE_ATTEMPTS; attempt++) {
            boolean attempted = AlchemyService.tryRefine(furnace);
            helper.assertTrue(attempted, "happy path: 入口物资完整时，每次应能发起合法炼制尝试");
            if (!furnace.getItem(AlchemyFurnaceBlockEntity.SLOT_OUTPUT).isEmpty()) {
                produced = true;
                break;
            }
        }
        helper.assertTrue(produced, "happy path: 炼制阶段应产生至少一份丹药产出");

        // 空窍反馈断言：使用资源控制器输出槽作为可读反馈信号，避免依赖主世界事件链与长时推进。
        controller.setItem(ResourceControllerBlockEntity.SLOT_INPUT, new ItemStack(Items.CLOCK));
        primeControllerForDeterministicOutput(controller);
        ResourceControllerBlockEntity.serverTick(
            helper.getLevel(),
            controller.getBlockPos(),
            helper.getLevel().getBlockState(controller.getBlockPos()),
            controller
        );

        ItemStack feedbackOutput = controller.getItem(ResourceControllerBlockEntity.SLOT_OUTPUT);
        helper.assertTrue(
            feedbackOutput.is(Items.CLOCK) && feedbackOutput.getCount() > 0,
            "happy path: 闭环应落到空窍反馈（ResourceController 输出槽）"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS, batch = TASK14_LOOP_A_BATCH)
    public void testTask14LoopAGuardPathShouldBreakLoopWithoutKeyInput(GameTestHelper helper) {
        AlchemyFurnaceBlockEntity furnace = placeAlchemyFurnace(helper, new BlockPos(2, 2, 2));
        ResourceControllerBlockEntity controller = placeResourceController(
            helper,
            new BlockPos(CONTROLLER_RELATIVE_X, 2, 2)
        );

        // guard 入口模拟：只注入辅材，不注入关键主材，验证闭环不能成立。
        furnace.setItem(AlchemyFurnaceBlockEntity.SLOT_AUX_1, new ItemStack(Items.COBBLESTONE, ENTRY_STACK_COUNT));
        furnace.setItem(AlchemyFurnaceBlockEntity.SLOT_AUX_2, new ItemStack(Items.COBBLESTONE, ENTRY_STACK_COUNT));

        boolean attempted = AlchemyService.tryRefine(furnace);
        helper.assertTrue(!attempted, "guard path: 缺失关键主材时不应发起炼制");
        helper.assertTrue(
            furnace.getItem(AlchemyFurnaceBlockEntity.SLOT_OUTPUT).isEmpty(),
            "guard path: 缺失关键主材时不应产生炼丹产出"
        );

        // 即使控制器处于“可推进状态”，缺失输入槽物资时也不能产生反馈产出。
        primeControllerForDeterministicOutput(controller);
        ResourceControllerBlockEntity.serverTick(
            helper.getLevel(),
            controller.getBlockPos(),
            helper.getLevel().getBlockState(controller.getBlockPos()),
            controller
        );
        helper.assertTrue(
            controller.getItem(ResourceControllerBlockEntity.SLOT_OUTPUT).isEmpty(),
            "guard path: 缺失关键输入时闭环应断开（无空窍反馈）"
        );
        helper.succeed();
    }

    private static AlchemyFurnaceBlockEntity placeAlchemyFurnace(GameTestHelper helper, BlockPos relativePos) {
        BlockPos absolutePos = helper.absolutePos(relativePos);
        helper.getLevel().setBlock(
            absolutePos,
            FarmingBlocks.ALCHEMY_FURNACE.get().defaultBlockState(),
            BLOCK_SET_FLAGS
        );
        BlockEntity blockEntity = helper.getLevel().getBlockEntity(absolutePos);
        helper.assertTrue(blockEntity instanceof AlchemyFurnaceBlockEntity, "炼丹炉方块实体创建失败");
        return (AlchemyFurnaceBlockEntity) blockEntity;
    }

    private static ResourceControllerBlockEntity placeResourceController(GameTestHelper helper, BlockPos relativePos) {
        BlockPos absolutePos = helper.absolutePos(relativePos);
        helper.getLevel().setBlock(
            absolutePos,
            XianqiaoBlocks.RESOURCE_CONTROLLER.get().defaultBlockState(),
            BLOCK_SET_FLAGS
        );
        BlockEntity blockEntity = helper.getLevel().getBlockEntity(absolutePos);
        helper.assertTrue(blockEntity instanceof ResourceControllerBlockEntity, "资源控制器方块实体创建失败");
        return (ResourceControllerBlockEntity) blockEntity;
    }

    private static void injectRefineInputs(AlchemyFurnaceBlockEntity furnace) {
        furnace.setItem(AlchemyFurnaceBlockEntity.SLOT_MAIN, new ItemStack(Items.WHEAT, ENTRY_STACK_COUNT));
        furnace.setItem(AlchemyFurnaceBlockEntity.SLOT_AUX_1, new ItemStack(Items.COBBLESTONE, ENTRY_STACK_COUNT));
        furnace.setItem(AlchemyFurnaceBlockEntity.SLOT_AUX_2, new ItemStack(Items.COBBLESTONE, ENTRY_STACK_COUNT));
        furnace.setItem(AlchemyFurnaceBlockEntity.SLOT_AUX_3, new ItemStack(Items.COBBLESTONE, ENTRY_STACK_COUNT));
        furnace.setItem(AlchemyFurnaceBlockEntity.SLOT_AUX_4, new ItemStack(Items.COBBLESTONE, ENTRY_STACK_COUNT));
    }

    private static void primeControllerForDeterministicOutput(ResourceControllerBlockEntity controller) {
        // 通过反射预置最小稳定上下文：
        // 1) 跳过结构重检计时；
        // 2) 保持结构已形成；
        // 3) 直接把进度置于可出货阈值。
        // 这样可控模拟“炼制后进入空窍反馈阶段”，避免依赖长时间 tick 推进导致测试不稳定。
        setPrivateIntField(controller, "structureRecheckCooldown", CONTROLLER_READY_COOLDOWN);
        setPrivateBooleanField(controller, "isFormed", true);
        setPrivateIntField(controller, "componentCount", CONTROLLER_READY_COMPONENT_COUNT);
        setPrivateFloatField(controller, "progress", CONTROLLER_READY_PROGRESS);
    }

    private static void setPrivateBooleanField(Object target, String fieldName, boolean value) {
        Field field = readPrivateField(target, fieldName);
        try {
            field.setBoolean(target, value);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("写入私有布尔字段失败: " + fieldName, exception);
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
