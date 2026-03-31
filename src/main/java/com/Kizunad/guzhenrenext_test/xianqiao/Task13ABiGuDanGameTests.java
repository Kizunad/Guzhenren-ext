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
public class Task13ABiGuDanGameTests {

    private static final String TASK13_BI_GU_DAN_BATCH = "task13_bi_gu_dan";
    private static final int TEST_TIMEOUT_TICKS = 120;
    private static final int REDUCED_FOOD_LEVEL = 6;
    private static final UUID HAPPY_PLAYER_UUID = UUID.fromString("04ec2d6c-8202-4f9c-ae4a-f4459f0ab42b");
    private static final String HAPPY_PLAYER_NAME = "task13a_bi_gu_dan_happy_player";
    private static final UUID GUARD_PLAYER_UUID = UUID.fromString("cdd467f0-93f4-4699-b4df-8f66d14a8517");
    private static final String GUARD_PLAYER_NAME = "task13a_bi_gu_dan_guard_player";

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_BI_GU_DAN_BATCH
    )
    public void testTask13ABiGuDanHappyPathShouldRestoreFoodAndConsumeOneItem(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, HAPPY_PLAYER_UUID, HAPPY_PLAYER_NAME);
        resetFoodBaseline(player, REDUCED_FOOD_LEVEL);

        ItemStack biGuDan = new ItemStack(FarmingItems.BI_GU_DAN.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, biGuDan);
        int foodBeforeUse = player.getFoodData().getFoodLevel();
        float saturationBeforeUse = player.getFoodData().getSaturationLevel();
        int countBeforeUse = biGuDan.getCount();
        biGuDan.getItem().use(level, player, InteractionHand.MAIN_HAND);

        helper.assertTrue(
            player.getFoodData().getFoodLevel() > foodBeforeUse,
            "happy path: 使用辟谷丹后应立即恢复饱食值"
        );
        helper.assertTrue(
            player.getFoodData().getSaturationLevel() > saturationBeforeUse,
            "happy path: 使用辟谷丹后应立即恢复饱和度"
        );
        helper.assertTrue(
            biGuDan.getCount() == countBeforeUse - 1,
            "happy path: 使用辟谷丹后应恰好消耗 1 个"
        );
        helper.succeed();
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_BI_GU_DAN_BATCH
    )
    public void testTask13ABiGuDanGuardPathNonPillShouldNotRestoreFoodOrSaturation(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, GUARD_PLAYER_UUID, GUARD_PLAYER_NAME);
        resetFoodBaseline(player, REDUCED_FOOD_LEVEL);

        Item nonPillShallowItem = FarmingItems.QING_YA_GRASS_ITEM.get();
        ItemStack nonPillStack = new ItemStack(nonPillShallowItem);
        player.setItemInHand(InteractionHand.MAIN_HAND, nonPillStack);
        int foodBeforeUse = player.getFoodData().getFoodLevel();
        float saturationBeforeUse = player.getFoodData().getSaturationLevel();
        nonPillShallowItem.use(level, player, InteractionHand.MAIN_HAND);

        helper.assertTrue(
            player.getFoodData().getFoodLevel() <= foodBeforeUse,
            "guard path: 使用非丹药浅层物品不应恢复饱食值"
        );
        helper.assertTrue(
            player.getFoodData().getSaturationLevel() <= saturationBeforeUse,
            "guard path: 使用非丹药浅层物品不应恢复饱和度"
        );
        helper.succeed();
    }

    private static ServerPlayer createTestPlayer(ServerLevel level, UUID playerUuid, String playerName) {
        return FakePlayerFactory.get(level, new GameProfile(playerUuid, playerName));
    }

    private static void resetFoodBaseline(ServerPlayer player, int foodLevel) {
        player.getFoodData().setFoodLevel(foodLevel);
        player.getFoodData().setSaturation(0.0F);
        player.getFoodData().setExhaustion(0.0F);
    }
}
