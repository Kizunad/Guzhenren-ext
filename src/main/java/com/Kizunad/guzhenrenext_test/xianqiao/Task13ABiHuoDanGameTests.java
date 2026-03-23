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
public class Task13ABiHuoDanGameTests {

    private static final String TASK13_BI_HUO_DAN_BATCH = "task13_bi_huo_dan";
    private static final int TEST_TIMEOUT_TICKS = 120;
    private static final UUID HAPPY_PLAYER_UUID = UUID.fromString("5f13eb8d-49d5-4c37-8a8c-1e587fe4f987");
    private static final String HAPPY_PLAYER_NAME = "task13a_bi_huo_dan_happy_player";
    private static final UUID GUARD_PLAYER_UUID = UUID.fromString("b2c88633-12c4-46f9-bff9-d94abf2b5f6d");
    private static final String GUARD_PLAYER_NAME = "task13a_bi_huo_dan_guard_player";

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_BI_HUO_DAN_BATCH
    )
    public void testTask13ABiHuoDanHappyPathShouldApplyFireResistance(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, HAPPY_PLAYER_UUID, HAPPY_PLAYER_NAME);
        player.removeEffect(MobEffects.FIRE_RESISTANCE);

        ItemStack biHuoDan = new ItemStack(FarmingItems.BI_HUO_DAN.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, biHuoDan);
        biHuoDan.getItem().use(level, player, InteractionHand.MAIN_HAND);

        helper.assertTrue(
            player.hasEffect(MobEffects.FIRE_RESISTANCE),
            "happy path: 使用辟火丹后应获得抗火效果"
        );
        helper.succeed();
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_BI_HUO_DAN_BATCH
    )
    public void testTask13ABiHuoDanGuardPathNonPillShouldNotApplyFireResistance(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, GUARD_PLAYER_UUID, GUARD_PLAYER_NAME);
        player.removeEffect(MobEffects.FIRE_RESISTANCE);

        Item nonPillShallowItem = FarmingItems.QING_YA_GRASS_ITEM.get();
        ItemStack nonPillStack = new ItemStack(nonPillShallowItem);
        player.setItemInHand(InteractionHand.MAIN_HAND, nonPillStack);
        nonPillShallowItem.use(level, player, InteractionHand.MAIN_HAND);

        helper.assertFalse(
            player.hasEffect(MobEffects.FIRE_RESISTANCE),
            "guard path: 使用非丹药浅层物品不应获得抗火效果"
        );
        helper.succeed();
    }

    private static ServerPlayer createTestPlayer(ServerLevel level, UUID playerUuid, String playerName) {
        return FakePlayerFactory.get(level, new GameProfile(playerUuid, playerName));
    }
}
