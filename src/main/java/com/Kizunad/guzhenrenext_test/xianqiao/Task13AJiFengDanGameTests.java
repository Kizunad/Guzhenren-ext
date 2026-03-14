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
public class Task13AJiFengDanGameTests {

    private static final String TASK13_JI_FENG_DAN_BATCH = "task13_ji_feng_dan";
    private static final int TEST_TIMEOUT_TICKS = 120;
    private static final UUID HAPPY_PLAYER_UUID = UUID.fromString("d0e4e3a5-cf3f-4f95-9889-c244c503f293");
    private static final String HAPPY_PLAYER_NAME = "task13a_ji_feng_dan_happy_player";
    private static final UUID GUARD_PLAYER_UUID = UUID.fromString("7e3ac430-c95f-46a3-bf46-fe6c2f4f496d");
    private static final String GUARD_PLAYER_NAME = "task13a_ji_feng_dan_guard_player";

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_JI_FENG_DAN_BATCH
    )
    public void testTask13AJiFengDanShouldApplyMovementSpeed(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, HAPPY_PLAYER_UUID, HAPPY_PLAYER_NAME);
        player.removeEffect(MobEffects.MOVEMENT_SPEED);

        ItemStack jiFengDan = new ItemStack(FarmingItems.JI_FENG_DAN.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, jiFengDan);
        jiFengDan.getItem().use(level, player, InteractionHand.MAIN_HAND);

        helper.assertTrue(
            player.hasEffect(MobEffects.MOVEMENT_SPEED),
            "happy path: 使用疾风丹后应获得速度效果"
        );
        helper.succeed();
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_JI_FENG_DAN_BATCH
    )
    public void testTask13AJiFengDanGuardShouldNotApplySpeedForQingYaGrass(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, GUARD_PLAYER_UUID, GUARD_PLAYER_NAME);
        player.removeEffect(MobEffects.MOVEMENT_SPEED);

        Item nonPillShallowItem = FarmingItems.QING_YA_GRASS_ITEM.get();
        ItemStack nonPillStack = new ItemStack(nonPillShallowItem);
        player.setItemInHand(InteractionHand.MAIN_HAND, nonPillStack);
        nonPillShallowItem.use(level, player, InteractionHand.MAIN_HAND);

        helper.assertFalse(
            player.hasEffect(MobEffects.MOVEMENT_SPEED),
            "guard path: 使用非丹药浅层物品不应获得速度效果"
        );
        helper.succeed();
    }

    private static ServerPlayer createTestPlayer(ServerLevel level, UUID playerUuid, String playerName) {
        return FakePlayerFactory.get(level, new GameProfile(playerUuid, playerName));
    }
}
