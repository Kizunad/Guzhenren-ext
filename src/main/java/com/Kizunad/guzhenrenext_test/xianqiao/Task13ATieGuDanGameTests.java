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
public class Task13ATieGuDanGameTests {

    private static final String TASK13_TIE_GU_DAN_BATCH = "task13_tie_gu_dan";
    private static final int TEST_TIMEOUT_TICKS = 120;
    private static final UUID HAPPY_PLAYER_UUID = UUID.fromString("8f5dad03-fcb6-4c5f-a048-d7bf41a98dd0");
    private static final String HAPPY_PLAYER_NAME = "task13a_tie_gu_dan_happy_player";
    private static final UUID GUARD_PLAYER_UUID = UUID.fromString("fc8acbe2-f946-4608-8b3e-e2f5ad8ec444");
    private static final String GUARD_PLAYER_NAME = "task13a_tie_gu_dan_guard_player";

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_TIE_GU_DAN_BATCH
    )
    public void testTask13ATieGuDanHappyPathShouldApplyDamageResistance(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, HAPPY_PLAYER_UUID, HAPPY_PLAYER_NAME);
        player.removeEffect(MobEffects.DAMAGE_RESISTANCE);

        ItemStack tieGuDan = new ItemStack(FarmingItems.TIE_GU_DAN.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, tieGuDan);
        tieGuDan.getItem().use(level, player, InteractionHand.MAIN_HAND);

        helper.assertTrue(
            player.hasEffect(MobEffects.DAMAGE_RESISTANCE),
            "happy path: 使用铁骨丹后应获得抗性提升效果"
        );
        helper.succeed();
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_TIE_GU_DAN_BATCH
    )
    public void testTask13ATieGuDanGuardPathNonPillShouldNotApplyDamageResistance(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, GUARD_PLAYER_UUID, GUARD_PLAYER_NAME);
        player.removeEffect(MobEffects.DAMAGE_RESISTANCE);

        Item nonPillShallowItem = FarmingItems.QING_YA_GRASS_ITEM.get();
        ItemStack nonPillStack = new ItemStack(nonPillShallowItem);
        player.setItemInHand(InteractionHand.MAIN_HAND, nonPillStack);
        nonPillShallowItem.use(level, player, InteractionHand.MAIN_HAND);

        helper.assertFalse(
            player.hasEffect(MobEffects.DAMAGE_RESISTANCE),
            "guard path: 使用非丹药浅层物品不应获得抗性提升效果"
        );
        helper.succeed();
    }

    private static ServerPlayer createTestPlayer(ServerLevel level, UUID playerUuid, String playerName) {
        return FakePlayerFactory.get(level, new GameProfile(playerUuid, playerName));
    }
}
