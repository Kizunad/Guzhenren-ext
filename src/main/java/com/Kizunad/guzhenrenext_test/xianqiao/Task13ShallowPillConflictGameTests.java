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

/*
 * Task13 的首个真实冲突切片。
 * 这里刻意不展开 20x20 的完整矩阵，只锁定三类最小且可审计的冲突面：
 * 1. 同效果连续服用：验证两个都给 DAMAGE_BOOST 的丹药顺序使用后，力量效果仍然存在。
 * 2. 非冲突正面效果共存：验证夜视与抗火可以同时保留，避免后一次服用粗暴覆盖前一次效果。
 * 3. 定向清除与正面效果共存：验证破幻丹在已有正面增益时仍只清除目标负面效果，不误伤其他增益。
 * 这三类断言已经覆盖“同类叠加/异类共存/清除交互”三个最小代表面，便于后续继续扩展。
 */
@GameTestHolder("guzhenrenext")
@PrefixGameTestTemplate(false)
public class Task13ShallowPillConflictGameTests {

    private static final String PLAN2_PILL_SHALLOW_CONFLICT_BATCH = "plan2.pill.shallow.conflict";
    private static final int TEST_TIMEOUT_TICKS = 120;
    private static final int POSITIVE_EFFECT_DURATION_TICKS = 200;
    private static final int NEGATIVE_EFFECT_DURATION_TICKS = 100;
    private static final UUID SAME_EFFECT_PLAYER_UUID = UUID.fromString("ce1643ee-f070-4243-a2dd-e16535fd900b");
    private static final String SAME_EFFECT_PLAYER_NAME = "plan2_pill_shallow_conflict_same_effect_player";
    private static final UUID COEXIST_PLAYER_UUID = UUID.fromString("20c5a6f9-5884-46be-a2d5-06e8452f15b0");
    private static final String COEXIST_PLAYER_NAME = "plan2_pill_shallow_conflict_coexist_player";
    private static final UUID CLEARING_PLAYER_UUID = UUID.fromString("2d5fb93c-373d-421a-951a-cc1bf55fc3dc");
    private static final String CLEARING_PLAYER_NAME = "plan2_pill_shallow_conflict_clearing_player";

    /*
     * 代表面 1：
     * 淬体丹与狂暴丹都映射到 DAMAGE_BOOST。
     * 本断言不强行规定持续时间必须如何叠加，只要求顺序服用后效果仍存在，且两次服用都正常完成。
     */
    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PILL_SHALLOW_CONFLICT_BATCH
    )
    public void testPlan2PillShallowConflictSameEffectSequentialUseShouldKeepDamageBoost(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, SAME_EFFECT_PLAYER_UUID, SAME_EFFECT_PLAYER_NAME);
        player.removeEffect(MobEffects.DAMAGE_BOOST);

        ItemStack cuiTiDan = new ItemStack(FarmingItems.CUI_TI_DAN.get());
        useHeldItem(level, player, cuiTiDan);
        helper.assertTrue(
            player.hasEffect(MobEffects.DAMAGE_BOOST),
            "同效果冲突面：先服用淬体丹后，玩家应先获得力量效果"
        );

        ItemStack kuangBaoDan = new ItemStack(FarmingItems.KUANG_BAO_DAN.get());
        useHeldItem(level, player, kuangBaoDan);

        helper.assertTrue(
            player.hasEffect(MobEffects.DAMAGE_BOOST),
            "同效果冲突面：连续服用淬体丹与狂暴丹后，力量效果仍应存在"
        );
        helper.assertTrue(
            cuiTiDan.isEmpty(),
            "同效果冲突面：第一枚淬体丹应被正常消耗，说明顺序使用未破坏物品状态"
        );
        helper.assertTrue(
            kuangBaoDan.isEmpty(),
            "同效果冲突面：第二枚狂暴丹应被正常消耗，说明后一次使用流程未异常中断"
        );
        helper.succeed();
    }

    /*
     * 代表面 2：
     * 夜视丹与辟火丹属于不同正面效果，最小合同是“后者加入时不清掉前者”。
     * 这里只断言两个目标效果同时存在，避免把当前实现过度约束成某种特定叠加算法。
     */
    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PILL_SHALLOW_CONFLICT_BATCH
    )
    public void testPlan2PillShallowConflictProtectiveBuffsShouldCoexist(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, COEXIST_PLAYER_UUID, COEXIST_PLAYER_NAME);
        player.removeEffect(MobEffects.NIGHT_VISION);
        player.removeEffect(MobEffects.FIRE_RESISTANCE);

        ItemStack yeShiDan = new ItemStack(FarmingItems.YE_SHI_DAN.get());
        useHeldItem(level, player, yeShiDan);
        ItemStack biHuoDan = new ItemStack(FarmingItems.BI_HUO_DAN.get());
        useHeldItem(level, player, biHuoDan);

        helper.assertTrue(
            player.hasEffect(MobEffects.NIGHT_VISION),
            "共存冲突面：先服用夜视丹后，后续再服辟火丹也不应丢失夜视效果"
        );
        helper.assertTrue(
            player.hasEffect(MobEffects.FIRE_RESISTANCE),
            "共存冲突面：顺序服用夜视丹与辟火丹后，玩家应获得抗火效果"
        );
        helper.assertTrue(
            yeShiDan.isEmpty() && biHuoDan.isEmpty(),
            "共存冲突面：两枚丹药都应被正常消耗，说明顺序使用链路完整"
        );
        helper.succeed();
    }

    /*
     * 代表面 3：
     * 破幻丹的真实语义是定向移除 BLINDNESS 与 CONFUSION。
     * 该用例先建立一个正面丹药效果，再叠加目标负面状态，最后验证破幻丹只清除目标 debuff，
     * 不会把先前已经存在的夜视效果一并清掉。
     */
    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PILL_SHALLOW_CONFLICT_BATCH
    )
    public void testPlan2PillShallowConflictClearingPillShouldPreservePositiveBuff(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, CLEARING_PLAYER_UUID, CLEARING_PLAYER_NAME);
        player.removeEffect(MobEffects.NIGHT_VISION);
        player.removeEffect(MobEffects.BLINDNESS);
        player.removeEffect(MobEffects.CONFUSION);

        ItemStack yeShiDan = new ItemStack(FarmingItems.YE_SHI_DAN.get());
        useHeldItem(level, player, yeShiDan);
        helper.assertTrue(
            player.hasEffect(MobEffects.NIGHT_VISION),
            "清除冲突面：在服用破幻丹前，夜视丹应先为玩家建立一个正面增益基线"
        );

        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, NEGATIVE_EFFECT_DURATION_TICKS));
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, NEGATIVE_EFFECT_DURATION_TICKS));
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, POSITIVE_EFFECT_DURATION_TICKS));

        ItemStack poHuanDan = new ItemStack(FarmingItems.PO_HUAN_DAN.get());
        useHeldItem(level, player, poHuanDan);

        helper.assertFalse(
            player.hasEffect(MobEffects.BLINDNESS),
            "清除冲突面：已有正面丹药效果时，破幻丹仍应移除失明效果"
        );
        helper.assertFalse(
            player.hasEffect(MobEffects.CONFUSION),
            "清除冲突面：已有正面丹药效果时，破幻丹仍应移除幻觉效果"
        );
        helper.assertTrue(
            player.hasEffect(MobEffects.NIGHT_VISION),
            "清除冲突面：破幻丹不应误清除无关的夜视正面效果"
        );
        helper.assertTrue(
            poHuanDan.isEmpty(),
            "清除冲突面：破幻丹应被正常消耗，说明定向清除路径完整执行"
        );
        helper.succeed();
    }

    private static void useHeldItem(ServerLevel level, ServerPlayer player, ItemStack stack) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        stack.getItem().use(level, player, InteractionHand.MAIN_HAND);
    }

    private static ServerPlayer createTestPlayer(ServerLevel level, UUID playerUuid, String playerName) {
        return FakePlayerFactory.get(level, new GameProfile(playerUuid, playerName));
    }
}
