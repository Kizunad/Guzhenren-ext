package com.Kizunad.guzhenrenext_test.xianqiao;

import com.Kizunad.guzhenrenext.xianqiao.farming.FarmingItems;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("guzhenrenext")
@PrefixGameTestTemplate(false)
public class Task13AXunMaiDanGameTests {

    private static final String TASK13_XUN_MAI_DAN_BATCH = "task13_xun_mai_dan";
    private static final int TEST_TIMEOUT_TICKS = 120;
    private static final int XUN_MAI_DAN_INITIAL_COUNT = 2;
    private static final int XUN_MAI_DAN_EXPECTED_COUNT_AFTER_USE = 1;
    private static final UUID HAPPY_PATH_PLAYER_UUID = UUID.fromString("95d41220-f388-47b3-b09c-c53efbaf6e36");
    private static final String HAPPY_PATH_PLAYER_NAME = "task13a_xun_mai_dan_happy_player";
    private static final UUID GUARD_PATH_PLAYER_UUID = UUID.fromString("ba848f8f-a542-47f8-82a0-1f603177022e");
    private static final String GUARD_PATH_PLAYER_NAME = "task13a_xun_mai_dan_guard_player";
    private static final int BLOCK_UPDATE_FLAGS = 3;

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_XUN_MAI_DAN_BATCH
    )
    public void testTask13AXunMaiDanHappyPathShouldApplyGlowingAndConsumeOne(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, HAPPY_PATH_PLAYER_UUID, HAPPY_PATH_PLAYER_NAME);
        player.removeEffect(MobEffects.GLOWING);
        placeDetectableOreNearPlayer(level, player);

        ItemStack xunMaiDan = new ItemStack(FarmingItems.XUN_MAI_DAN.get(), XUN_MAI_DAN_INITIAL_COUNT);
        player.setItemInHand(InteractionHand.MAIN_HAND, xunMaiDan);
        xunMaiDan.getItem().use(level, player, InteractionHand.MAIN_HAND);

        helper.assertTrue(
            player.hasEffect(MobEffects.GLOWING),
            "happy path: 寻脉丹在附近有目标矿石时应触发发光标记"
        );
        helper.assertTrue(
            xunMaiDan.getCount() == XUN_MAI_DAN_EXPECTED_COUNT_AFTER_USE,
            "happy path: 寻脉丹应仅消耗1个"
        );
        helper.succeed();
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_XUN_MAI_DAN_BATCH
    )
    public void testTask13AXunMaiDanGuardPathNonPillShouldNotApplyGlowingOrConsume(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, GUARD_PATH_PLAYER_UUID, GUARD_PATH_PLAYER_NAME);
        player.removeEffect(MobEffects.GLOWING);

        Item nonPillItem = FarmingItems.QING_YA_GRASS_ITEM.get();
        ItemStack nonPillStack = new ItemStack(nonPillItem);
        int nonPillCountBeforeUse = nonPillStack.getCount();
        player.setItemInHand(InteractionHand.MAIN_HAND, nonPillStack);
        nonPillItem.use(level, player, InteractionHand.MAIN_HAND);

        helper.assertFalse(
            player.hasEffect(MobEffects.GLOWING),
            "guard path: 非丹药物品不应触发寻脉发光标记"
        );
        helper.assertTrue(
            nonPillStack.getCount() == nonPillCountBeforeUse,
            "guard path: 非丹药物品不应被错误消耗"
        );
        helper.succeed();
    }

    private static ServerPlayer createTestPlayer(ServerLevel level, UUID uuid, String name) {
        return FakePlayerFactory.get(level, new GameProfile(uuid, name));
    }

    private static void placeDetectableOreNearPlayer(ServerLevel level, ServerPlayer player) {
        BlockPos orePosition = player.blockPosition().offset(1, 0, 0);
        level.setBlock(orePosition, Blocks.IRON_ORE.defaultBlockState(), BLOCK_UPDATE_FLAGS);
    }
}
