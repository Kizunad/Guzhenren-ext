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
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("guzhenrenext")
@PrefixGameTestTemplate(false)
public class Task13AQuShouSanGameTests {

    private static final String TASK13_QU_SHOU_SAN_BATCH = "task13_qu_shou_san";
    private static final int TEST_TIMEOUT_TICKS = 120;
    private static final int QU_SHOU_SAN_INITIAL_COUNT = 3;
    private static final int QU_SHOU_SAN_EXPECTED_COUNT_AFTER_USE = 2;
    private static final int GUARD_APPLE_INITIAL_COUNT = 1;
    private static final double MOVEMENT_COMPARE_EPSILON = 0.0001D;
    private static final UUID TEST_PLAYER_UUID = UUID.fromString("13f4022b-c95b-46a7-96a0-fc44b2f21985");
    private static final String TEST_PLAYER_NAME = "task13a_qu_shou_san_test_player";

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_QU_SHOU_SAN_BATCH
    )
    public void testTask13AQuShouSanHappyPathShouldPushNearbyMonsterAndConsumeOne(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level);
        Zombie zombie = spawnTestZombie(level, player);

        Vec3 playerPosition = player.position();
        double distanceBeforeUse = zombie.position().distanceTo(playerPosition);

        ItemStack quShouSan = new ItemStack(FarmingItems.QU_SHOU_SAN.get(), QU_SHOU_SAN_INITIAL_COUNT);
        player.setItemInHand(InteractionHand.MAIN_HAND, quShouSan);
        quShouSan.getItem().use(level, player, InteractionHand.MAIN_HAND);

        double distanceAfterUse = zombie.position().distanceTo(playerPosition);
        helper.assertTrue(
            distanceAfterUse > distanceBeforeUse + MOVEMENT_COMPARE_EPSILON,
            "happy path: 驱兽散应让附近敌对目标远离玩家"
        );
        helper.assertTrue(
            quShouSan.getCount() == QU_SHOU_SAN_EXPECTED_COUNT_AFTER_USE,
            "happy path: 驱兽散应仅消耗1个"
        );
        helper.succeed();
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_QU_SHOU_SAN_BATCH
    )
    public void testTask13AQuShouSanGuardPathAppleShouldNotPushMonsterOrConsume(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level);
        Zombie zombie = spawnTestZombie(level, player);

        double xBeforeUse = zombie.getX();
        double zBeforeUse = zombie.getZ();
        ItemStack apple = new ItemStack(Items.APPLE, GUARD_APPLE_INITIAL_COUNT);
        player.setItemInHand(InteractionHand.MAIN_HAND, apple);
        apple.getItem().use(level, player, InteractionHand.MAIN_HAND);

        double xAfterUse = zombie.getX();
        double zAfterUse = zombie.getZ();
        helper.assertTrue(
            Math.abs(xAfterUse - xBeforeUse) < MOVEMENT_COMPARE_EPSILON
                && Math.abs(zAfterUse - zBeforeUse) < MOVEMENT_COMPARE_EPSILON,
            "guard path: 苹果不应触发驱兽散位移逻辑"
        );
        helper.assertTrue(
            apple.getCount() == GUARD_APPLE_INITIAL_COUNT,
            "guard path: 非丹药物品不应被错误消耗"
        );
        helper.succeed();
    }

    private static ServerPlayer createTestPlayer(ServerLevel level) {
        return FakePlayerFactory.get(level, new GameProfile(TEST_PLAYER_UUID, TEST_PLAYER_NAME));
    }

    private static Zombie spawnTestZombie(ServerLevel level, ServerPlayer player) {
        Zombie zombie = EntityType.ZOMBIE.create(level);
        if (zombie == null) {
            throw new IllegalStateException("failed to create zombie for test");
        }
        zombie.setNoAi(true);
        zombie.setPos(player.getX() + 1.0D, player.getY(), player.getZ());
        zombie.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(zombie);
        return zombie;
    }
}
