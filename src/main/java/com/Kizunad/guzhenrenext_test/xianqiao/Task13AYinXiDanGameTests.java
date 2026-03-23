package com.Kizunad.guzhenrenext_test.xianqiao;

import com.Kizunad.guzhenrenext.xianqiao.farming.FarmingItems;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("guzhenrenext")
@PrefixGameTestTemplate(false)
public class Task13AYinXiDanGameTests {

    private static final String TASK13_YIN_XI_DAN_BATCH = "task13_yin_xi_dan";
    private static final int TEST_TIMEOUT_TICKS = 120;
    private static final UUID HAPPY_PLAYER_UUID = UUID.fromString("a9fd6177-0e83-49d5-ae0e-9004d5e619d9");
    private static final String HAPPY_PLAYER_NAME = "task13a_yin_xi_dan_happy_player";
    private static final UUID GUARD_PLAYER_UUID = UUID.fromString("4f644122-6c06-4d77-b18c-c9339781798a");
    private static final String GUARD_PLAYER_NAME = "task13a_yin_xi_dan_guard_player";

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_YIN_XI_DAN_BATCH
    )
    public void testTask13AYinXiDanHappyPathShouldApplyInvisibility(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, HAPPY_PLAYER_UUID, HAPPY_PLAYER_NAME);
        player.removeEffect(MobEffects.INVISIBILITY);

        ItemStack yinXiDan = new ItemStack(FarmingItems.YIN_XI_DAN.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, yinXiDan);
        yinXiDan.getItem().use(level, player, InteractionHand.MAIN_HAND);

        helper.assertTrue(
            player.hasEffect(MobEffects.INVISIBILITY),
            "happy path: 使用隐息丹后应获得隐身效果"
        );
        helper.succeed();
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_YIN_XI_DAN_BATCH
    )
    public void testTask13AYinXiDanGuardPathNonPillShouldNotApplyInvisibility(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, GUARD_PLAYER_UUID, GUARD_PLAYER_NAME);
        player.removeEffect(MobEffects.INVISIBILITY);

        Item nonPillShallowItem = FarmingItems.QING_YA_GRASS_ITEM.get();
        ItemStack nonPillStack = new ItemStack(nonPillShallowItem);
        player.setItemInHand(InteractionHand.MAIN_HAND, nonPillStack);
        nonPillShallowItem.use(level, player, InteractionHand.MAIN_HAND);

        helper.assertFalse(
            player.hasEffect(MobEffects.INVISIBILITY),
            "guard path: 使用非丹药浅层物品不应获得隐身效果"
        );
        helper.succeed();
    }

    private static ServerPlayer createTestPlayer(ServerLevel level, UUID playerUuid, String playerName) {
        return FakePlayerFactory.get(level, new GameProfile(playerUuid, playerName));
    }
}
