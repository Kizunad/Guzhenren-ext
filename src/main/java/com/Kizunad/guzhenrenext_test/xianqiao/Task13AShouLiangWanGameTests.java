package com.Kizunad.guzhenrenext_test.xianqiao;

import com.Kizunad.guzhenrenext.xianqiao.farming.FarmingItems;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("guzhenrenext")
@PrefixGameTestTemplate(false)
public class Task13AShouLiangWanGameTests {

    private static final String TASK13_SHOU_LIANG_WAN_BATCH = "task13_shou_liang_wan";
    private static final int TEST_TIMEOUT_TICKS = 120;
    private static final int SHOU_LIANG_WAN_INITIAL_COUNT = 2;
    private static final int SHOU_LIANG_WAN_EXPECTED_COUNT_AFTER_USE = 1;
    private static final int GUARD_STICK_INITIAL_COUNT = 1;
    private static final UUID TEST_PLAYER_UUID = UUID.fromString("6f5a3ce6-a3f2-4442-890c-0ed7878f45df");
    private static final String TEST_PLAYER_NAME = "task13a_shou_liang_wan_test_player";

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_SHOU_LIANG_WAN_BATCH
    )
    public void testTask13AShouLiangWanHappyPathShouldSetNearbyAnimalInLoveAndConsumeOne(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level);
        Cow cow = spawnTestCow(level, player);
        helper.assertTrue(cow.canFallInLove(), "happy path: 初始奶牛应可进入求偶状态");

        ItemStack shouLiangWan = new ItemStack(FarmingItems.SHOU_LIANG_WAN.get(), SHOU_LIANG_WAN_INITIAL_COUNT);
        player.setItemInHand(InteractionHand.MAIN_HAND, shouLiangWan);
        shouLiangWan.getItem().use(level, player, InteractionHand.MAIN_HAND);

        helper.assertTrue(!cow.canFallInLove(), "happy path: 兽粮丸应让附近可繁殖动物进入求偶状态");
        helper.assertTrue(
            shouLiangWan.getCount() == SHOU_LIANG_WAN_EXPECTED_COUNT_AFTER_USE,
            "happy path: 兽粮丸应仅消耗1个"
        );
        helper.succeed();
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_SHOU_LIANG_WAN_BATCH
    )
    public void testTask13AShouLiangWanGuardPathNonPillShouldNotSetAnimalInLoveOrConsume(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level);
        Cow cow = spawnTestCow(level, player);
        helper.assertTrue(cow.canFallInLove(), "guard path: 初始奶牛应可进入求偶状态");

        ItemStack stick = new ItemStack(Items.STICK, GUARD_STICK_INITIAL_COUNT);
        player.setItemInHand(InteractionHand.MAIN_HAND, stick);
        stick.getItem().use(level, player, InteractionHand.MAIN_HAND);

        helper.assertTrue(cow.canFallInLove(), "guard path: 非丹药物品不应触发兽粮丸求偶逻辑");
        helper.assertTrue(stick.getCount() == GUARD_STICK_INITIAL_COUNT, "guard path: 非丹药物品不应被误消耗");
        helper.succeed();
    }

    private static ServerPlayer createTestPlayer(ServerLevel level) {
        return FakePlayerFactory.get(level, new GameProfile(TEST_PLAYER_UUID, TEST_PLAYER_NAME));
    }

    private static Cow spawnTestCow(ServerLevel level, ServerPlayer player) {
        Cow cow = EntityType.COW.create(level);
        if (cow == null) {
            throw new IllegalStateException("failed to create cow for test");
        }
        cow.setNoAi(true);
        cow.setPos(player.getX() + 1.0D, player.getY(), player.getZ());
        cow.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(cow);
        return cow;
    }
}
