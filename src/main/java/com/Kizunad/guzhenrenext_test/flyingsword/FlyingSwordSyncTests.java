package com.Kizunad.guzhenrenext_test.flyingsword;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordEntities;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordEntity;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.calculator.FlyingSwordAttributes;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.quality.SwordQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.gametest.GameTestHolder;

/**
 * 飞剑同步测试。
 * <p>
 * 目标：验证飞剑的品质/等级/经验在关键入口调用后，
 * 能通过 SynchedEntityData 正确回写与读取，避免客户端展示与服务端状态不一致。
 * </p>
 */
@GameTestHolder("guzhenrenext")
public class FlyingSwordSyncTests {

    private static final int TEST_TIMEOUT_TICKS = 40;
    private static final int ENTITY_RELATIVE_X = 2;
    private static final int ENTITY_RELATIVE_Y = 2;
    private static final int ENTITY_RELATIVE_Z = 2;
    private static final int KING_QUALITY_LEVEL = 10;
    private static final int EXP_TO_ADD = 100;

    /**
     * 测试：从 NBT 读取 Attributes 后，品质与等级应同步到实体读取接口。
     */
    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS)
    public void testSwordAttributesSyncOnLoad(GameTestHelper helper) {
        // 1) 在测试空间创建飞剑实体并加入世界。
        FlyingSwordEntity sword = createSwordInTestSpace(helper);

        // 2) 构造目标属性（王品 + 指定等级），并放入外层 Attributes NBT。
        FlyingSwordAttributes attributes = new FlyingSwordAttributes(
            SwordQuality.KING,
            KING_QUALITY_LEVEL
        );
        CompoundTag outerTag = new CompoundTag();
        outerTag.put("Attributes", attributes.toNBT());

        // 3) 执行读取路径，触发 readAdditionalSaveData -> syncAttributesToEntityData。
        sword.readAdditionalSaveData(outerTag);

        // 4) 断言品质与等级已经与预期一致。
        helper.assertTrue(
            sword.getQuality() == SwordQuality.KING,
            "读取 Attributes 后，飞剑品质应为 KING"
        );
        helper.assertTrue(
            sword.getSwordLevel() == KING_QUALITY_LEVEL,
            "读取 Attributes 后，飞剑等级应与 NBT 一致"
        );

        helper.succeed();
    }

    /**
     * 测试：调用 addExperience 后，经验应通过同步链路更新。
     */
    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS)
    public void testSwordExpAddSyncs(GameTestHelper helper) {
        // 1) 在测试空间创建飞剑实体。
        FlyingSwordEntity sword = createSwordInTestSpace(helper);

        // 2) 调用经验添加入口（内部会执行 syncAttributesToEntityData）。
        sword.addExperience(EXP_TO_ADD);

        // 3) 验证经验值已更新为正数。
        helper.assertTrue(
            sword.getSwordExperience() > 0,
            "调用 addExperience 后，飞剑经验应大于 0"
        );

        helper.succeed();
    }

    /**
     * 在 GameTest 空间内创建并放置飞剑实体。
     *
     * @param helper 测试辅助对象
     * @return 已加入世界的飞剑实体
     */
    private static FlyingSwordEntity createSwordInTestSpace(GameTestHelper helper) {
        FlyingSwordEntity sword = FlyingSwordEntities.FLYING_SWORD
            .get()
            .create(helper.getLevel());
        helper.assertTrue(sword != null, "飞剑实体创建失败：注册类型未就绪或世界无效");

        BlockPos absolutePos = helper.absolutePos(
            new BlockPos(ENTITY_RELATIVE_X, ENTITY_RELATIVE_Y, ENTITY_RELATIVE_Z)
        );
        sword.setPos(absolutePos.getX(), absolutePos.getY(), absolutePos.getZ());
        helper.getLevel().addFreshEntity(sword);
        return sword;
    }
}
