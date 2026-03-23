package com.Kizunad.guzhenrenext_test.xianqiao;

import com.Kizunad.guzhenrenext.xianqiao.farming.FarmingItems;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("guzhenrenext")
@PrefixGameTestTemplate(false)
public class Task13AYeShiDanGameTests {

    private static final String TASK13_YE_SHI_DAN_BATCH = "task13_ye_shi_dan";
    private static final int TEST_TIMEOUT_TICKS = 120;
    private static final int YE_SHI_DAN_INITIAL_COUNT = 3;
    private static final int YE_SHI_DAN_EXPECTED_COUNT_AFTER_USE = 2;
    private static final UUID HAPPY_PLAYER_UUID = UUID.fromString("5cbaf117-2864-49b6-a9ef-c6f72e7ef778");
    private static final String HAPPY_PLAYER_NAME = "task13a_ye_shi_dan_happy_player";
    private static final UUID GUARD_PLAYER_UUID = UUID.fromString("b30a3192-8df7-4dd7-9d7e-2af362087f80");
    private static final String GUARD_PLAYER_NAME = "task13a_ye_shi_dan_guard_player";

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_YE_SHI_DAN_BATCH
    )
    public void testTask13AYeShiDanShouldApplyNightVisionAndConsumeOne(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, HAPPY_PLAYER_UUID, HAPPY_PLAYER_NAME);
        ItemStack yeShiDan = new ItemStack(FarmingItems.YE_SHI_DAN.get(), YE_SHI_DAN_INITIAL_COUNT);
        player.setItemInHand(InteractionHand.MAIN_HAND, yeShiDan);
        yeShiDan.getItem().use(level, player, InteractionHand.MAIN_HAND);

        MobEffectInstance nightVision = player.getEffect(MobEffects.NIGHT_VISION);
        helper.assertTrue(nightVision != null, "happy path: 夜视丹应给玩家添加夜视效果");
        helper.assertTrue(
            nightVision != null && nightVision.getDuration() > 0,
            "happy path: 夜视效果持续时间应大于0"
        );
        helper.assertTrue(
            yeShiDan.getCount() == YE_SHI_DAN_EXPECTED_COUNT_AFTER_USE,
            "happy path: 夜视丹应仅消耗1个"
        );
        helper.succeed();
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_YE_SHI_DAN_BATCH
    )
    public void testTask13AYeShiDanGuardNonPillShouldNotApplyNightVision(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, GUARD_PLAYER_UUID, GUARD_PLAYER_NAME);
        ItemStack qingYaGrass = new ItemStack(FarmingItems.QING_YA_GRASS_ITEM.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, qingYaGrass);
        qingYaGrass.getItem().use(level, player, InteractionHand.MAIN_HAND);

        MobEffectInstance nightVision = player.getEffect(MobEffects.NIGHT_VISION);
        helper.assertTrue(nightVision == null, "guard path: 非丹药浅层物品不应给玩家添加夜视效果");
        helper.assertTrue(qingYaGrass.getCount() == 1, "guard path: 非丹药浅层物品不应被错误消耗");
        helper.succeed();
    }

    private static ServerPlayer createTestPlayer(ServerLevel level, UUID playerUuid, String playerName) {
        return FakePlayerFactory.get(level, new GameProfile(playerUuid, playerName));
    }
}
