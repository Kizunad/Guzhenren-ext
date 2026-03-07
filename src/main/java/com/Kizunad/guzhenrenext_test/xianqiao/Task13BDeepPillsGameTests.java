package com.Kizunad.guzhenrenext_test.xianqiao;

import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoMarkDiffusionService;
import com.Kizunad.guzhenrenext.xianqiao.alchemy.effect.DeepPillEffectState;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.farming.FarmingItems;
import com.Kizunad.guzhenrenext.xianqiao.item.XianqiaoItems;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder("guzhenrenext")
public class Task13BDeepPillsGameTests {

    private static final String TASK13B_BATCH = "task13b_deep_pills";
    private static final int TEST_TIMEOUT_TICKS = 220;
    private static final UUID TEST_PLAYER_UUID = UUID.fromString("8d8f7b2d-b3b7-4e4f-8ae5-8f58da53cf57");
    private static final String TEST_PLAYER_NAME = "task13b_test_player";

    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS, batch = TASK13B_BATCH)
    public void testTask13BDeepPillsShouldTriggerAllMechanisms(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level);
        prepareApertureContext(level, player);

        ItemStack lifeDeath = new ItemStack(FarmingItems.SHENG_SI_ZAO_HUA_DAN.get());
        lifeDeath.getItem().finishUsingItem(lifeDeath, level, player);
        helper.assertTrue(
            DeepPillEffectState.dumpDebugState(player).getBoolean(DeepPillEffectState.KEY_NEAR_DEATH_TOKEN),
            "21 生死造化丹应写入保命标记"
        );

        ItemStack forceBreakthrough = new ItemStack(FarmingItems.QIANG_ZHI_PO_JING_DAN.get());
        forceBreakthrough.getItem().finishUsingItem(forceBreakthrough, level, player);
        helper.assertTrue(
            DeepPillEffectState.dumpDebugState(player).getBoolean(DeepPillEffectState.KEY_FORCE_BREAKTHROUGH_USED),
            "22 强制破境丹应写入永久使用标记"
        );

        ItemStack marrowReforge = new ItemStack(FarmingItems.XI_SUI_FA_GU_DAN.get());
        marrowReforge.getItem().finishUsingItem(marrowReforge, level, player);
        helper.assertTrue(
            DeepPillEffectState.dumpDebugState(player).getInt(DeepPillEffectState.KEY_DAO_RESET_COUNT) > 0,
            "23 洗髓伐骨丹应累计道痕重洗计数"
        );

        ItemStack reversal = new ItemStack(FarmingItems.NI_SHI_DAN.get());
        reversal.getItem().finishUsingItem(reversal, level, player);
        helper.assertTrue(
            DeepPillEffectState.dumpDebugState(player).getLong(DeepPillEffectState.KEY_REVERSAL_END) > 0,
            "24 逆时丹应写入时间流速窗口"
        );

        ItemStack tribulation = new ItemStack(FarmingItems.YIN_ZAI_DAN.get());
        tribulation.getItem().finishUsingItem(tribulation, level, player);
        helper.assertTrue(
            DeepPillEffectState.dumpDebugState(player).getLong(DeepPillEffectState.KEY_TRIBULATION_DAMP_END) > 0,
            "25 引灾丹应写入灾劫衰减窗口"
        );

        ItemStack disperse = new ItemStack(FarmingItems.SAN_GONG_DAN.get());
        disperse.getItem().finishUsingItem(disperse, level, player);
        helper.assertTrue(
            DeepPillEffectState.dumpDebugState(player).getInt(DeepPillEffectState.KEY_POWER_DISPERSE_COUNT) > 0,
            "26 散功丹应累计散功计数"
        );

        ItemStack beast = new ItemStack(FarmingItems.NU_SHOU_YIN.get());
        beast.getItem().finishUsingItem(beast, level, player);
        helper.assertTrue(
            DeepPillEffectState.dumpDebugState(player).getLong(DeepPillEffectState.KEY_BEAST_DOMINATION_END) > 0,
            "27 奴兽印应写入驯服窗口"
        );

        ItemStack heaven = new ItemStack(FarmingItems.DUO_TIAN_DAN.get());
        heaven.getItem().finishUsingItem(heaven, level, player);
        helper.assertTrue(
            DeepPillEffectState.dumpDebugState(player).getLong(DeepPillEffectState.KEY_WORLD_SUPPRESSION_END) > 0,
            "28 夺天丹应写入压制窗口"
        );

        ItemStack enlightenment = new ItemStack(FarmingItems.WU_DAO_CHA.get());
        enlightenment.getItem().finishUsingItem(enlightenment, level, player);
        helper.assertTrue(
            DeepPillEffectState.dumpDebugState(player).getLong(DeepPillEffectState.KEY_ENLIGHTENMENT_END) > 0,
            "29 悟道茶应写入悟道窗口"
        );

        ItemStack reshape = new ItemStack(FarmingItems.SU_TI_NI.get());
        reshape.getItem().finishUsingItem(reshape, level, player);
        helper.assertTrue(
            DeepPillEffectState.dumpDebugState(player).getBoolean(DeepPillEffectState.KEY_BODY_RESHAPE_USED),
            "30 塑体泥应写入永久塑体标记"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS, batch = TASK13B_BATCH)
    public void testTask13BDeepPillGuardShouldNotAffectShallowItem(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level);
        prepareApertureContext(level, player);

        long beforeSuppression = DeepPillEffectState.dumpDebugState(player)
            .getLong(DeepPillEffectState.KEY_WORLD_SUPPRESSION_END);
        ItemStack shallow = new ItemStack(XianqiaoItems.HEAVENLY_FRAGMENT.get());
        shallow.getItem().finishUsingItem(shallow, level, player);
        long afterSuppression = DeepPillEffectState.dumpDebugState(player)
            .getLong(DeepPillEffectState.KEY_WORLD_SUPPRESSION_END);
        helper.assertTrue(
            beforeSuppression == afterSuppression,
            "guard path: 非深度丹药不应写入深度丹药状态"
        );
        helper.succeed();
    }

    private static ServerPlayer createTestPlayer(ServerLevel level) {
        return FakePlayerFactory.get(level, new GameProfile(TEST_PLAYER_UUID, TEST_PLAYER_NAME));
    }

    private static void prepareApertureContext(ServerLevel level, ServerPlayer player) {
        ApertureWorldData.get(level).allocateAperture(player.getUUID());
        ServerLevel apertureLevel = level.getServer().getLevel(DaoMarkDiffusionService.APERTURE_DIMENSION);
        if (apertureLevel != null) {
            ApertureWorldData.get(apertureLevel).allocateAperture(player.getUUID());
        }
    }
}
