package com.Kizunad.guzhenrenext_test.xianqiao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import com.Kizunad.guzhenrenext.xianqiao.farming.FarmingItems;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("guzhenrenext")
@PrefixGameTestTemplate(false)
public class Task13AJuQiSanGameTests {

    private static final String TASK13_JU_QI_SAN_BATCH = "task13_ju_qi_san";
    private static final int TEST_TIMEOUT_TICKS = 200;
    private static final int JU_QI_SAN_INITIAL_COUNT = 3;
    private static final int JU_QI_SAN_EXPECTED_COUNT_AFTER_SUCCESSFUL_USE = 2;
    private static final int JU_QI_SAN_EXPECTED_COUNT_AFTER_SKIPPED_USE = 3;
    private static final double INITIAL_ZHEN_YUAN = 10.0D;
    private static final double MAX_ZHEN_YUAN = 100.0D;
    private static final double ZHEN_YUAN_COMPARE_EPSILON = 0.0001D;
    private static final String MAX_ZHEN_YUAN_USAGE_ID = "task13_ju_qi_san_test";
    private static final String HAPPY_PLAYER_NAME = "task13a_ju_qi_san_happy_player";
    private static final String GUARD_PLAYER_NAME = "task13a_ju_qi_san_guard_player";

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_JU_QI_SAN_BATCH
    )
    public void testTask13AJuQiSanShouldRestoreZhenYuanAndConsumeOne(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, HAPPY_PLAYER_NAME);
        boolean zhenYuanBridgeAvailable = hasRuntimeZhenYuanVariables(player);
        prepareZhenYuanForTest(player);
        double zhenYuanBeforeUse = safeGetZhenYuan(player);

        ItemStack juQiSan = new ItemStack(FarmingItems.JU_QI_SAN.get(), JU_QI_SAN_INITIAL_COUNT);
        player.setItemInHand(InteractionHand.MAIN_HAND, juQiSan);
        juQiSan.getItem().use(level, player, InteractionHand.MAIN_HAND);
        double zhenYuanAfterUse = safeGetZhenYuan(player);

        if (zhenYuanBridgeAvailable) {
            helper.assertTrue(
                zhenYuanAfterUse > zhenYuanBeforeUse,
                "happy path: 聚气散应使真元立即增加"
            );
            helper.assertTrue(
                juQiSan.getCount() == JU_QI_SAN_EXPECTED_COUNT_AFTER_SUCCESSFUL_USE,
                "happy path: 聚气散在恢复生效时应仅消耗1个"
            );
        } else {
            helper.assertTrue(
                Math.abs(zhenYuanAfterUse - zhenYuanBeforeUse) < ZHEN_YUAN_COMPARE_EPSILON,
                "guard path: 聚气散在桥接不可用时不应伪造真元恢复"
            );
            helper.assertTrue(
                juQiSan.getCount() == JU_QI_SAN_EXPECTED_COUNT_AFTER_SKIPPED_USE,
                "guard path: 聚气散在恢复无法应用时不应被消耗"
            );
        }
        helper.succeed();
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_JU_QI_SAN_BATCH
    )
    public void testTask13AJuQiSanGuardNonPillShouldNotRestoreZhenYuan(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, GUARD_PLAYER_NAME);
        boolean zhenYuanBridgeAvailable = hasRuntimeZhenYuanVariables(player);
        prepareZhenYuanForTest(player);
        double zhenYuanBeforeUse = safeGetZhenYuan(player);

        ItemStack qingYaGrass = new ItemStack(FarmingItems.QING_YA_GRASS_ITEM.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, qingYaGrass);
        qingYaGrass.getItem().use(level, player, InteractionHand.MAIN_HAND);
        double zhenYuanAfterUse = safeGetZhenYuan(player);

        if (zhenYuanBridgeAvailable) {
            helper.assertTrue(
                Math.abs(zhenYuanAfterUse - zhenYuanBeforeUse) < ZHEN_YUAN_COMPARE_EPSILON,
                "guard path: 非丹药物品不应触发聚气散真元恢复"
            );
        }
        helper.assertTrue(qingYaGrass.getCount() == 1, "guard path: 非丹药物品不应被错误消耗");
        helper.succeed();
    }

    private static ServerPlayer createTestPlayer(ServerLevel level, String playerName) {
        return FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), playerName));
    }

    private static void prepareZhenYuanForTest(ServerPlayer player) {
        if (!hasRuntimeZhenYuanVariables(player)) {
            return;
        }
        GuzhenrenVariableModifierService.removeModifier(
            player,
            GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN,
            MAX_ZHEN_YUAN_USAGE_ID
        );
        double currentAmount = safeGetZhenYuan(player);
        if (currentAmount > 0.0D) {
            safeModifyZhenYuan(player, -currentAmount);
        }
        GuzhenrenVariableModifierService.setAdditiveModifier(
            player,
            GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN,
            MAX_ZHEN_YUAN_USAGE_ID,
            MAX_ZHEN_YUAN
        );
        safeModifyZhenYuan(player, INITIAL_ZHEN_YUAN);
    }

    private static boolean hasRuntimeZhenYuanVariables(ServerPlayer player) {
        try {
            return ZhenYuanHelper.hasRuntimeVariables(player);
        } catch (NoClassDefFoundError error) {
            return false;
        }
    }

    private static double safeGetZhenYuan(ServerPlayer player) {
        try {
            return ZhenYuanHelper.getAmount(player);
        } catch (NoClassDefFoundError error) {
            return 0.0D;
        }
    }

    private static void safeModifyZhenYuan(ServerPlayer player, double delta) {
        try {
            ZhenYuanHelper.modify(player, delta);
        } catch (NoClassDefFoundError error) {
            return;
        }
    }
}
