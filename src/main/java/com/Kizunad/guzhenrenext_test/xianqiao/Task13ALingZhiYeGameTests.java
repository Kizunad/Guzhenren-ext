package com.Kizunad.guzhenrenext_test.xianqiao;

import com.Kizunad.guzhenrenext.xianqiao.alchemy.effect.PillEffectState;
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
public class Task13ALingZhiYeGameTests {

    private static final String TASK13_LING_ZHI_YE_BATCH = "task13_ling_zhi_ye";
    private static final int TEST_TIMEOUT_TICKS = 120;
    private static final UUID HAPPY_PLAYER_UUID = UUID.fromString("c2edc8b6-f89e-4062-a95e-6491fd29dc1e");
    private static final String HAPPY_PLAYER_NAME = "task13a_ling_zhi_ye_happy_player";
    private static final UUID GUARD_PLAYER_UUID = UUID.fromString("9c5785a5-2645-4d8f-b8f8-f0a883fda771");
    private static final String GUARD_PLAYER_NAME = "task13a_ling_zhi_ye_guard_player";

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_LING_ZHI_YE_BATCH
    )
    public void testTask13ALingZhiYeHappyPathShouldWriteGrowthAccelerationState(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, HAPPY_PLAYER_UUID, HAPPY_PLAYER_NAME);
        PillEffectState.clearGrowthAccelerationState(player);

        ItemStack lingZhiYe = new ItemStack(FarmingItems.LING_ZHI_YE.get());
        int useCountBefore = lingZhiYe.getCount();
        player.setItemInHand(InteractionHand.MAIN_HAND, lingZhiYe);
        lingZhiYe.getItem().use(level, player, InteractionHand.MAIN_HAND);

        helper.assertTrue(
            PillEffectState.isGrowthAccelerationActive(player, level.getGameTime()),
            "happy path: 使用灵植液后应激活灵植生长加速状态"
        );
        helper.assertTrue(
            lingZhiYe.getCount() == useCountBefore - 1,
            "happy path: 使用灵植液后应精确消耗 1 个"
        );
        helper.succeed();
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_LING_ZHI_YE_BATCH
    )
    public void testTask13ALingZhiYeGuardPathNonPillShouldNotWriteGrowthAccelerationState(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, GUARD_PLAYER_UUID, GUARD_PLAYER_NAME);
        PillEffectState.clearGrowthAccelerationState(player);

        Item nonPillShallowItem = FarmingItems.QING_YA_GRASS_ITEM.get();
        ItemStack nonPillStack = new ItemStack(nonPillShallowItem);
        player.setItemInHand(InteractionHand.MAIN_HAND, nonPillStack);
        nonPillShallowItem.use(level, player, InteractionHand.MAIN_HAND);

        helper.assertFalse(
            PillEffectState.isGrowthAccelerationActive(player, level.getGameTime()),
            "guard path: 使用非丹药浅层物品不应激活灵植生长加速状态"
        );
        helper.succeed();
    }

    private static ServerPlayer createTestPlayer(ServerLevel level, UUID playerUuid, String playerName) {
        return FakePlayerFactory.get(level, new GameProfile(playerUuid, playerName));
    }
}
