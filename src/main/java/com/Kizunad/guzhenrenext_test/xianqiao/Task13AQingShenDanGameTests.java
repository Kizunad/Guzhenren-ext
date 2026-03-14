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
public class Task13AQingShenDanGameTests {

    private static final String TASK13_QING_SHEN_DAN_BATCH = "task13_qing_shen_dan";
    private static final int TEST_TIMEOUT_TICKS = 120;
    private static final UUID HAPPY_PLAYER_UUID = UUID.fromString("f0f04058-77e0-43ad-9b8f-52a8bf24d7f2");
    private static final String HAPPY_PLAYER_NAME = "task13a_qing_shen_dan_happy_player";
    private static final UUID GUARD_PLAYER_UUID = UUID.fromString("f8d9f8d7-d1f4-41e7-b196-768c57f56fbf");
    private static final String GUARD_PLAYER_NAME = "task13a_qing_shen_dan_guard_player";

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_QING_SHEN_DAN_BATCH
    )
    public void testTask13AQingShenDanHappyPathShouldApplySlowFalling(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, HAPPY_PLAYER_UUID, HAPPY_PLAYER_NAME);
        player.removeEffect(MobEffects.SLOW_FALLING);

        ItemStack qingShenDan = new ItemStack(FarmingItems.QING_SHEN_DAN.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, qingShenDan);
        qingShenDan.getItem().use(level, player, InteractionHand.MAIN_HAND);

        helper.assertTrue(
            player.hasEffect(MobEffects.SLOW_FALLING),
            "happy path: 使用轻身丹后应获得缓降效果"
        );
        helper.succeed();
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_QING_SHEN_DAN_BATCH
    )
    public void testTask13AQingShenDanGuardPathNonPillShouldNotApplySlowFalling(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, GUARD_PLAYER_UUID, GUARD_PLAYER_NAME);
        player.removeEffect(MobEffects.SLOW_FALLING);

        Item nonPillShallowItem = FarmingItems.QING_YA_GRASS_ITEM.get();
        ItemStack nonPillStack = new ItemStack(nonPillShallowItem);
        player.setItemInHand(InteractionHand.MAIN_HAND, nonPillStack);
        nonPillShallowItem.use(level, player, InteractionHand.MAIN_HAND);

        helper.assertFalse(
            player.hasEffect(MobEffects.SLOW_FALLING),
            "guard path: 使用非丹药浅层物品不应获得缓降效果"
        );
        helper.succeed();
    }

    private static ServerPlayer createTestPlayer(ServerLevel level, UUID playerUuid, String playerName) {
        return FakePlayerFactory.get(level, new GameProfile(playerUuid, playerName));
    }
}
