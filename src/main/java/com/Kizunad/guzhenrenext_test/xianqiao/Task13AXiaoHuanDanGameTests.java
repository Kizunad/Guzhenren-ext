package com.Kizunad.guzhenrenext_test.xianqiao;

import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("guzhenrenext")
@PrefixGameTestTemplate(false)
public class Task13AXiaoHuanDanGameTests {

    private static final String TASK13_XIAO_HUAN_DAN_BATCH = "task13_xiao_huan_dan";
    private static final int TEST_TIMEOUT_TICKS = 120;
    private static final String HAPPY_PLAYER_NAME = "task13a_xiao_huan_dan_happy_player";
    private static final String GUARD_PLAYER_NAME = "task13a_xiao_huan_dan_guard_player";

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_XIAO_HUAN_DAN_BATCH
    )
    public void testTask13AXiaoHuanDanHappyPathShouldHealImmediately(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, HAPPY_PLAYER_NAME);
        Item xiaoHuanDanItem = com.Kizunad.guzhenrenext.xianqiao.farming.FarmingItems.XIAO_HUAN_DAN.get();

        ItemStack xiaoHuanDan = new ItemStack(xiaoHuanDanItem);
        player.setItemInHand(InteractionHand.MAIN_HAND, xiaoHuanDan);
        xiaoHuanDan.getItem().use(level, player, InteractionHand.MAIN_HAND);

        helper.assertTrue(
            xiaoHuanDan.getCount() == 0,
            "happy path: 使用小还丹后应仅消耗1个"
        );
        helper.assertTrue(
            player.getHealth() == player.getMaxHealth(),
            "happy path: 使用小还丹后玩家生命值应保持满血"
        );
        helper.succeed();
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_XIAO_HUAN_DAN_BATCH
    )
    public void testTask13AXiaoHuanDanGuardPathNonPillShouldNotHeal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, GUARD_PLAYER_NAME);

        Item nonPillShallowItem = com.Kizunad.guzhenrenext.xianqiao.farming.FarmingItems.QING_YA_GRASS_ITEM.get();
        ItemStack nonPillStack = new ItemStack(nonPillShallowItem);
        player.setItemInHand(InteractionHand.MAIN_HAND, nonPillStack);
        nonPillShallowItem.use(level, player, InteractionHand.MAIN_HAND);

        helper.assertTrue(
            player.getHealth() == player.getMaxHealth(),
            "guard path: 使用非丹药浅层物品不应回复生命"
        );
        helper.assertTrue(
            nonPillStack.getCount() == 1,
            "guard path: 使用非丹药浅层物品不应被错误消耗"
        );
        helper.succeed();
    }

    private static ServerPlayer createTestPlayer(ServerLevel level, String playerName) {
        return FakePlayerFactory.get(level, new GameProfile(java.util.UUID.randomUUID(), playerName));
    }
}
