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
public class Task13ABaoShiDanGameTests {

    private static final String TASK13_BAO_SHI_DAN_BATCH = "task13_bao_shi_dan";
    private static final int TEST_TIMEOUT_TICKS = 120;
    private static final int REDUCED_FOOD_LEVEL = 10;
    private static final UUID HAPPY_PLAYER_UUID = UUID.fromString("0dfd3909-4f3d-4ff0-8f07-eac15f859e89");
    private static final String HAPPY_PLAYER_NAME = "task13a_bao_shi_dan_happy_player";
    private static final UUID GUARD_PLAYER_UUID = UUID.fromString("4630585f-c385-4118-a4c4-225f9057b513");
    private static final String GUARD_PLAYER_NAME = "task13a_bao_shi_dan_guard_player";

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_BAO_SHI_DAN_BATCH
    )
    public void testTask13ABaoShiDanHappyPathShouldRestoreFoodAndConsumeItem(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, HAPPY_PLAYER_UUID, HAPPY_PLAYER_NAME);
        player.getFoodData().setFoodLevel(REDUCED_FOOD_LEVEL);

        ItemStack baoShiDan = new ItemStack(FarmingItems.BAO_SHI_DAN.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, baoShiDan);
        int foodBeforeUse = player.getFoodData().getFoodLevel();
        int countBeforeUse = baoShiDan.getCount();
        baoShiDan.getItem().use(level, player, InteractionHand.MAIN_HAND);

        helper.assertTrue(
            player.getFoodData().getFoodLevel() > foodBeforeUse,
            "happy path: 使用饱食丹后应恢复饱食值"
        );
        helper.assertTrue(
            baoShiDan.getCount() < countBeforeUse,
            "happy path: 使用饱食丹后应消耗丹药"
        );
        helper.succeed();
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_BAO_SHI_DAN_BATCH
    )
    public void testTask13ABaoShiDanGuardPathNonPillShouldNotRestoreFood(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, GUARD_PLAYER_UUID, GUARD_PLAYER_NAME);
        player.getFoodData().setFoodLevel(REDUCED_FOOD_LEVEL);

        Item nonPillShallowItem = FarmingItems.QING_YA_GRASS_ITEM.get();
        ItemStack nonPillStack = new ItemStack(nonPillShallowItem);
        player.setItemInHand(InteractionHand.MAIN_HAND, nonPillStack);
        int foodBeforeUse = player.getFoodData().getFoodLevel();
        nonPillShallowItem.use(level, player, InteractionHand.MAIN_HAND);

        helper.assertTrue(
            player.getFoodData().getFoodLevel() <= foodBeforeUse,
            "guard path: 使用非丹药浅层物品不应恢复饱食值"
        );
        helper.succeed();
    }

    private static ServerPlayer createTestPlayer(ServerLevel level, UUID playerUuid, String playerName) {
        return FakePlayerFactory.get(level, new GameProfile(playerUuid, playerName));
    }
}
