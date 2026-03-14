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
public class Task13ACuiTiDanGameTests {

    private static final String TASK13_CUI_TI_DAN_BATCH = "task13_cui_ti_dan";
    private static final int TEST_TIMEOUT_TICKS = 120;
    private static final UUID HAPPY_PLAYER_UUID = UUID.fromString("29c8dfe8-c091-484f-be15-1456eac9b974");
    private static final String HAPPY_PLAYER_NAME = "task13a_cui_ti_dan_happy_player";
    private static final UUID GUARD_PLAYER_UUID = UUID.fromString("9a4e742f-8a3d-4c1e-ae8e-1179db8dd11d");
    private static final String GUARD_PLAYER_NAME = "task13a_cui_ti_dan_guard_player";

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_CUI_TI_DAN_BATCH
    )
    public void testTask13ACuiTiDanHappyPathShouldApplyDamageBoost(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, HAPPY_PLAYER_UUID, HAPPY_PLAYER_NAME);
        player.removeEffect(MobEffects.DAMAGE_BOOST);

        ItemStack cuiTiDan = new ItemStack(FarmingItems.CUI_TI_DAN.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, cuiTiDan);
        cuiTiDan.getItem().use(level, player, InteractionHand.MAIN_HAND);

        helper.assertTrue(
            player.hasEffect(MobEffects.DAMAGE_BOOST),
            "happy path: 使用淬体丹后应获得力量效果"
        );
        helper.succeed();
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK13_CUI_TI_DAN_BATCH
    )
    public void testTask13ACuiTiDanGuardPathNonPillShouldNotApplyDamageBoost(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, GUARD_PLAYER_UUID, GUARD_PLAYER_NAME);
        player.removeEffect(MobEffects.DAMAGE_BOOST);

        Item nonPillShallowItem = FarmingItems.QING_YA_GRASS_ITEM.get();
        ItemStack nonPillStack = new ItemStack(nonPillShallowItem);
        player.setItemInHand(InteractionHand.MAIN_HAND, nonPillStack);
        nonPillShallowItem.use(level, player, InteractionHand.MAIN_HAND);

        helper.assertFalse(
            player.hasEffect(MobEffects.DAMAGE_BOOST),
            "guard path: 使用非丹药浅层物品不应获得力量效果"
        );
        helper.succeed();
    }

    private static ServerPlayer createTestPlayer(ServerLevel level, UUID playerUuid, String playerName) {
        return FakePlayerFactory.get(level, new GameProfile(playerUuid, playerName));
    }
}
