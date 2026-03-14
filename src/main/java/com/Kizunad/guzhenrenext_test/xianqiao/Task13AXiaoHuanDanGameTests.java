package com.Kizunad.guzhenrenext_test.xianqiao;

import com.Kizunad.guzhenrenext.xianqiao.farming.FarmingItems;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
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
    private static final float REDUCED_HEALTH = 8.0F;
    private static final float HEALTH_COMPARE_EPSILON = 0.0001F;
    private static final UUID HAPPY_PLAYER_UUID = UUID.fromString("8e346499-6072-494f-b62e-c444ff7968b4");
    private static final String HAPPY_PLAYER_NAME = "task13a_xiao_huan_dan_happy_player";
    private static final UUID GUARD_PLAYER_UUID = UUID.fromString("395ae351-04a1-4d19-a8ff-fcf7131d6f00");
    private static final String GUARD_PLAYER_NAME = "task13a_xiao_huan_dan_guard_player";

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_XIAO_HUAN_DAN_BATCH
    )
    public void testTask13AXiaoHuanDanHappyPathShouldHealImmediately(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, HAPPY_PLAYER_UUID, HAPPY_PLAYER_NAME);
        player.setHealth(REDUCED_HEALTH);

        ItemStack xiaoHuanDan = new ItemStack(FarmingItems.XIAO_HUAN_DAN.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, xiaoHuanDan);
        float healthBeforeUse = player.getHealth();
        xiaoHuanDan.getItem().use(level, player, InteractionHand.MAIN_HAND);

        helper.assertTrue(
            player.getHealth() > healthBeforeUse,
            "happy path: 使用小还丹后应立即回复生命"
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
        ServerPlayer player = createTestPlayer(level, GUARD_PLAYER_UUID, GUARD_PLAYER_NAME);
        player.setHealth(REDUCED_HEALTH);

        Item nonPillShallowItem = FarmingItems.QING_YA_GRASS_ITEM.get();
        ItemStack nonPillStack = new ItemStack(nonPillShallowItem);
        player.setItemInHand(InteractionHand.MAIN_HAND, nonPillStack);
        float healthBeforeUse = player.getHealth();
        nonPillShallowItem.use(level, player, InteractionHand.MAIN_HAND);

        helper.assertTrue(
            player.getHealth() <= healthBeforeUse + HEALTH_COMPARE_EPSILON,
            "guard path: 使用非丹药浅层物品不应回复生命"
        );
        helper.succeed();
    }

    private static ServerPlayer createTestPlayer(ServerLevel level, UUID playerUuid, String playerName) {
        return FakePlayerFactory.get(level, new GameProfile(playerUuid, playerName));
    }
}
