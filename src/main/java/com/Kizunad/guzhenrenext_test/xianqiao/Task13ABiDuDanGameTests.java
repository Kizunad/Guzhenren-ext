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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("guzhenrenext")
@PrefixGameTestTemplate(false)
public class Task13ABiDuDanGameTests {

    private static final String TASK13_BI_DU_DAN_BATCH = "task13_bi_du_dan";
    private static final int TEST_TIMEOUT_TICKS = 120;
    private static final UUID HAPPY_PLAYER_UUID = UUID.fromString("2d9d3294-3f72-4e17-bbdf-b5fd1fc8d54c");
    private static final String HAPPY_PLAYER_NAME = "task13a_bi_du_dan_happy_player";
    private static final UUID GUARD_PLAYER_UUID = UUID.fromString("3596699e-f809-4f9f-ab34-3edecf3ca737");
    private static final String GUARD_PLAYER_NAME = "task13a_bi_du_dan_guard_player";

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_BI_DU_DAN_BATCH
    )
    public void testTask13ABiDuDanHappyPathShouldRemovePoison(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, HAPPY_PLAYER_UUID, HAPPY_PLAYER_NAME);
        player.addEffect(new MobEffectInstance(MobEffects.POISON, TEST_TIMEOUT_TICKS));

        ItemStack biDuDan = new ItemStack(FarmingItems.BI_DU_DAN.get());
        int useCountBefore = biDuDan.getCount();
        player.setItemInHand(InteractionHand.MAIN_HAND, biDuDan);
        biDuDan.getItem().use(level, player, InteractionHand.MAIN_HAND);

        helper.assertFalse(
            player.hasEffect(MobEffects.POISON),
            "happy path: 使用避毒丹后应移除中毒效果"
        );
        helper.assertTrue(
            biDuDan.getCount() == useCountBefore - 1,
            "happy path: 使用避毒丹后应消耗 1 个"
        );
        helper.succeed();
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_BI_DU_DAN_BATCH
    )
    public void testTask13ABiDuDanGuardPathNonPillShouldNotRemovePoison(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, GUARD_PLAYER_UUID, GUARD_PLAYER_NAME);
        player.addEffect(new MobEffectInstance(MobEffects.POISON, TEST_TIMEOUT_TICKS));

        Item nonPillShallowItem = FarmingItems.QING_YA_GRASS_ITEM.get();
        ItemStack nonPillStack = new ItemStack(nonPillShallowItem);
        player.setItemInHand(InteractionHand.MAIN_HAND, nonPillStack);
        nonPillShallowItem.use(level, player, InteractionHand.MAIN_HAND);

        helper.assertTrue(
            player.hasEffect(MobEffects.POISON),
            "guard path: 使用非丹药浅层物品不应移除中毒效果"
        );
        helper.succeed();
    }

    private static ServerPlayer createTestPlayer(ServerLevel level, UUID playerUuid, String playerName) {
        return FakePlayerFactory.get(level, new GameProfile(playerUuid, playerName));
    }
}
