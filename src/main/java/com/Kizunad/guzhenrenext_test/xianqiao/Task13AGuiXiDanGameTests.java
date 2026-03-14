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
public class Task13AGuiXiDanGameTests {

    private static final String TASK13_GUI_XI_DAN_BATCH = "task13_gui_xi_dan";
    private static final int TEST_TIMEOUT_TICKS = 120;
    private static final UUID HAPPY_PLAYER_UUID = UUID.fromString("67a8ca7e-f35f-4d56-80af-2855ca90fd5f");
    private static final String HAPPY_PLAYER_NAME = "task13a_gui_xi_dan_happy_player";
    private static final UUID GUARD_PLAYER_UUID = UUID.fromString("98616c32-fded-4ef7-be5d-cf8b04c31fd1");
    private static final String GUARD_PLAYER_NAME = "task13a_gui_xi_dan_guard_player";

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_GUI_XI_DAN_BATCH
    )
    public void testTask13AGuiXiDanHappyPathShouldApplyWaterBreathing(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, HAPPY_PLAYER_UUID, HAPPY_PLAYER_NAME);
        player.removeEffect(MobEffects.WATER_BREATHING);

        ItemStack guiXiDan = new ItemStack(FarmingItems.GUI_XI_DAN.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, guiXiDan);
        guiXiDan.getItem().use(level, player, InteractionHand.MAIN_HAND);

        helper.assertTrue(
            player.hasEffect(MobEffects.WATER_BREATHING),
            "happy path: 使用龟息丹后应获得水下呼吸效果"
        );
        helper.succeed();
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_GUI_XI_DAN_BATCH
    )
    public void testTask13AGuiXiDanGuardPathNonPillShouldNotApplyWaterBreathing(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, GUARD_PLAYER_UUID, GUARD_PLAYER_NAME);
        player.removeEffect(MobEffects.WATER_BREATHING);

        Item nonPillShallowItem = FarmingItems.QING_YA_GRASS_ITEM.get();
        ItemStack nonPillStack = new ItemStack(nonPillShallowItem);
        player.setItemInHand(InteractionHand.MAIN_HAND, nonPillStack);
        nonPillShallowItem.use(level, player, InteractionHand.MAIN_HAND);

        helper.assertFalse(
            player.hasEffect(MobEffects.WATER_BREATHING),
            "guard path: 使用非丹药浅层物品不应获得水下呼吸效果"
        );
        helper.succeed();
    }

    private static ServerPlayer createTestPlayer(ServerLevel level, UUID playerUuid, String playerName) {
        return FakePlayerFactory.get(level, new GameProfile(playerUuid, playerName));
    }
}
