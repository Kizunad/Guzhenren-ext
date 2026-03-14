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
public class Task13AKuangBaoDanGameTests {

    private static final String TASK13_KUANG_BAO_DAN_BATCH = "task13_kuang_bao_dan";
    private static final int TEST_TIMEOUT_TICKS = 120;
    private static final UUID HAPPY_PLAYER_UUID = UUID.fromString("2962c6f9-0858-46e4-922e-b40299c647f2");
    private static final String HAPPY_PLAYER_NAME = "task13a_kuang_bao_dan_happy_player";
    private static final UUID GUARD_PLAYER_UUID = UUID.fromString("0f4fdb90-2a47-4654-8ff3-c48232531d0d");
    private static final String GUARD_PLAYER_NAME = "task13a_kuang_bao_dan_guard_player";

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_KUANG_BAO_DAN_BATCH
    )
    public void testTask13AKuangBaoDanHappyPathShouldApplyDamageBoost(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, HAPPY_PLAYER_UUID, HAPPY_PLAYER_NAME);
        player.removeEffect(MobEffects.DAMAGE_BOOST);

        ItemStack kuangBaoDan = new ItemStack(FarmingItems.KUANG_BAO_DAN.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, kuangBaoDan);
        kuangBaoDan.getItem().use(level, player, InteractionHand.MAIN_HAND);

        helper.assertTrue(
            player.hasEffect(MobEffects.DAMAGE_BOOST),
            "happy path: 使用狂暴丹后应获得力量效果"
        );
        helper.succeed();
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_KUANG_BAO_DAN_BATCH
    )
    public void testTask13AKuangBaoDanGuardPathNonPillShouldNotApplyDamageBoost(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, GUARD_PLAYER_UUID, GUARD_PLAYER_NAME);
        player.removeEffect(MobEffects.DAMAGE_BOOST);

        Item nonPillShallowItem = FarmingItems.QING_YA_GRASS_ITEM.get();
        ItemStack nonPillStack = new ItemStack(nonPillShallowItem);
        player.setItemInHand(InteractionHand.MAIN_HAND, nonPillStack);
        nonPillShallowItem.use(level, player, InteractionHand.MAIN_HAND);

        helper.assertFalse(
            player.hasEffect(MobEffects.DAMAGE_BOOST),
            "guard path: 使用非丹药浅层物品不应获得力量效果"
        );
        helper.succeed();
    }

    private static ServerPlayer createTestPlayer(ServerLevel level, UUID playerUuid, String playerName) {
        return FakePlayerFactory.get(level, new GameProfile(playerUuid, playerName));
    }
}
