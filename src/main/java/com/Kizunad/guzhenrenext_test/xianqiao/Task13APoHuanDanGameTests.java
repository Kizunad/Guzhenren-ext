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
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("guzhenrenext")
@PrefixGameTestTemplate(false)
public class Task13APoHuanDanGameTests {

    private static final String TASK13_PO_HUAN_DAN_BATCH = "task13_po_huan_dan";
    private static final int TEST_TIMEOUT_TICKS = 120;
    private static final int EFFECT_DURATION_TICKS = 100;
    private static final int PO_HUAN_DAN_INITIAL_COUNT = 3;
    private static final int PO_HUAN_DAN_EXPECTED_COUNT_AFTER_USE = 2;
    private static final int GUARD_APPLE_INITIAL_COUNT = 1;
    private static final UUID HAPPY_PLAYER_UUID = UUID.fromString("3f0f0f7b-5c68-4a91-a0a9-865e1f18811a");
    private static final String HAPPY_PLAYER_NAME = "task13a_po_huan_dan_happy_player";
    private static final UUID GUARD_PLAYER_UUID = UUID.fromString("7dc5f0a1-867a-43b4-9c4f-9154d2678ece");
    private static final String GUARD_PLAYER_NAME = "task13a_po_huan_dan_guard_player";

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_PO_HUAN_DAN_BATCH
    )
    public void testTask13APoHuanDanHappyPathShouldClearBlindnessAndConfusion(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, HAPPY_PLAYER_UUID, HAPPY_PLAYER_NAME);
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, EFFECT_DURATION_TICKS));
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, EFFECT_DURATION_TICKS));

        ItemStack poHuanDan = new ItemStack(FarmingItems.PO_HUAN_DAN.get(), PO_HUAN_DAN_INITIAL_COUNT);
        player.setItemInHand(InteractionHand.MAIN_HAND, poHuanDan);
        poHuanDan.getItem().use(level, player, InteractionHand.MAIN_HAND);

        helper.assertFalse(
            player.hasEffect(MobEffects.BLINDNESS),
            "happy path: 使用破幻丹后应移除失明效果"
        );
        helper.assertFalse(
            player.hasEffect(MobEffects.CONFUSION),
            "happy path: 使用破幻丹后应移除幻觉效果"
        );
        helper.assertTrue(
            poHuanDan.getCount() == PO_HUAN_DAN_EXPECTED_COUNT_AFTER_USE,
            "happy path: 使用破幻丹后应消耗 1 个"
        );
        helper.succeed();
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_PO_HUAN_DAN_BATCH
    )
    public void testTask13APoHuanDanGuardPathNonPillShouldNotClearEffectsOrConsume(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, GUARD_PLAYER_UUID, GUARD_PLAYER_NAME);
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, EFFECT_DURATION_TICKS));
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, EFFECT_DURATION_TICKS));

        ItemStack apple = new ItemStack(Items.APPLE, GUARD_APPLE_INITIAL_COUNT);
        player.setItemInHand(InteractionHand.MAIN_HAND, apple);
        apple.getItem().use(level, player, InteractionHand.MAIN_HAND);

        helper.assertTrue(
            player.hasEffect(MobEffects.BLINDNESS),
            "guard path: 使用非丹药浅层物品不应移除失明效果"
        );
        helper.assertTrue(
            player.hasEffect(MobEffects.CONFUSION),
            "guard path: 使用非丹药浅层物品不应移除幻觉效果"
        );
        helper.assertTrue(
            apple.getCount() == GUARD_APPLE_INITIAL_COUNT,
            "guard path: 非丹药物品不应被错误消耗"
        );
        helper.succeed();
    }

    private static ServerPlayer createTestPlayer(ServerLevel level, UUID playerUuid, String playerName) {
        return FakePlayerFactory.get(level, new GameProfile(playerUuid, playerName));
    }
}
