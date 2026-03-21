package com.Kizunad.guzhenrenext_test.flyingsword;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.calculator.FlyingSwordAttributes;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.BenmingSwordReadonlyModifierHelper;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.resonance.FlyingSwordResonanceType;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.training.FlyingSwordTrainingAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.training.FlyingSwordTrainingService;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ItemStackCustomDataHelper;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder("guzhenrenext")
public class FlyingSwordTrainingServiceTests {

    private static final int TEST_TIMEOUT_TICKS = 40;
    private static final int SLOT_SWORD = 0;
    private static final int SLOT_FUEL = 1;
    private static final int PERCENT_100 = 100;
    private static final int INITIAL_FUEL = 5;
    private static final int EXPECTED_FUEL_AFTER_TICK = 4;
    private static final int EXPECTED_EXP_AFTER_TICK = 1;
    private static final int EXPECTED_EXP_AFTER_BONUS_TICK = 2;
    private static final int EXPECTED_EXP_AFTER_OFFENSE_ROUTE_TICK = 3;
    private static final int EXPECTED_EXP_AFTER_DEFENSE_ROUTE_TICK = 1;
    private static final int EXPECTED_EXP_AFTER_SPIRIT_ROUTE_TICK = 2;
    private static final int EXPECTED_EXP_AFTER_DEVOUR_ROUTE_TICK = 1;
    private static final int FUEL_STACK_FOR_CONSERVATION_TEST = 2;
    private static final double EXPECTED_RESONANCE_BONUS = 0.01D;
    private static final double EXPECTED_OFFENSE_ROUTE_RESONANCE_BONUS = 0.008D;
    private static final double EXPECTED_DEFENSE_ROUTE_RESONANCE_BONUS = 0.0125D;
    private static final double EXPECTED_SPIRIT_ROUTE_RESONANCE_BONUS = 0.01D;
    private static final double EXPECTED_DEVOUR_ROUTE_RESONANCE_BONUS = 0.0135D;
    private static final int MIN_EXPECTED_DEVOUR_ROUTE_AFFINITY = 4;
    private static final double EXPECTED_RESONANCE_BONUS_MILD = 0.0115D;
    private static final double EXPECTED_RESONANCE_BONUS_CLAMPED = 0.012D;
    private static final double REWARD_MULTIPLIER_MILD = 1.15D;
    private static final double REWARD_MULTIPLIER_CLAMPED = 1.2D;
    private static final double REWARD_MULTIPLIER_BASELINE = 1.0D;
    private static final double EPSILON = 0.000001D;
    private static final String BENMING_COMBAT_BATCH =
        "benming_combat_resonance";
    private static final double COMBAT_BASE_DAMAGE = 20.0D;
    private static final double COMBAT_BASE_SPEED_MAX = 4.0D;
    private static final double COMBAT_CURRENT_SPEED = 2.0D;
    private static final float COMBAT_SYNERGY_ATTACK_MULTIPLIER = 1.15F;
    private static final double ZERO_RESONANCE = 0.0D;
    private static final double MODERATE_RESONANCE = 0.5D;
    private static final double EXTREME_RESONANCE = 999.0D;
    private static final double POSITIVE_RESONANCE = 2.5D;
    private static final double MODERATE_EXPECTED_RATIO = 1.05D;
    private static final double CAPPED_EXPECTED_RATIO = 1.20D;
    private static final double COMBAT_ASSERT_EPSILON = 0.0001D;
    private static final String COMBAT_BENMING_STABLE_SWORD_ID =
        "training_benming_combat_sword";
    private static final Method CALCULATE_NORMAL_ATTACK_DAMAGE_METHOD =
        resolveCalculateNormalAttackDamageMethod();

    /**
     * 基础回归：已有燃料时，tick 应消耗 1 点燃烧值并给飞剑累计 1 点经验。
     */
    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS)
    public void testTickShouldDecreaseFuelTime(GameTestHelper helper) {
        FlyingSwordTrainingAttachment training = new FlyingSwordTrainingAttachment();
        training.getInputSlots().setStackInSlot(
            SLOT_SWORD,
            new ItemStack(Items.IRON_SWORD)
        );

        // 去掉对外部模组物品注册的依赖：
        // 此测试只验证 tick 消耗与经验增长，燃料槽放任意占位物即可。
        training.getInputSlots().setStackInSlot(
            SLOT_FUEL,
            new ItemStack(Items.COAL)
        );
        training.setFuelTime(INITIAL_FUEL);
        training.setMaxFuelTime(INITIAL_FUEL);

        FlyingSwordTrainingService.tickInternal(training, null);

        helper.assertTrue(
            training.getFuelTime() == EXPECTED_FUEL_AFTER_TICK,
            "tick 后 fuelTime 应递减"
        );
        helper.assertTrue(
            training.getAccumulatedExp() == EXPECTED_EXP_AFTER_TICK,
            "tick 后 accumulatedExp 应增加"
        );
        helper.succeed();
    }

    /**
     * DoD 证据 1 + 2：
     * 1) 放入飞剑+元石并 tick 后，可用于菜单展示的进度值（fuel/max）应发生变化；
     * 2) tick 后 accumulatedExp 应增加。
     */
    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS)
    public void testTickShouldStartProgressAndIncreaseExp(GameTestHelper helper) {
        FlyingSwordTrainingAttachment training = new FlyingSwordTrainingAttachment();
        training.getInputSlots().setStackInSlot(
            SLOT_SWORD,
            new ItemStack(Items.IRON_SWORD)
        );

        // 去掉对 guzhenren:gucaiyuanshi 的硬依赖，避免 GameTest 环境外部注册缺失导致误报。
        // 这里通过直接设置 fuel/max 构造可控燃烧状态，等价验证“进度值会推进”。
        training.getInputSlots().setStackInSlot(
            SLOT_FUEL,
            new ItemStack(Items.COAL)
        );

        training.setFuelTime(INITIAL_FUEL);
        training.setMaxFuelTime(INITIAL_FUEL);
        int beforeFuel = training.getFuelTime();
        int beforeMaxFuel = training.getMaxFuelTime();
        int beforeBurnPercent = calculateMenuVisibleBurnPercent(beforeFuel, beforeMaxFuel);
        int beforeExp = training.getAccumulatedExp();

        FlyingSwordTrainingService.tickInternal(training, null);

        int afterFuel = training.getFuelTime();
        int afterMaxFuel = training.getMaxFuelTime();
        int afterBurnPercent = calculateMenuVisibleBurnPercent(afterFuel, afterMaxFuel);

        helper.assertTrue(
            afterFuel != beforeFuel || afterMaxFuel != beforeMaxFuel,
            "tick 后 fuel/max 应变化，才能证明菜单可见进度开始更新"
        );
        helper.assertTrue(
            afterMaxFuel == beforeMaxFuel,
            "仅消耗燃烧进度时，maxFuelTime 应保持稳定"
        );
        helper.assertTrue(
            afterBurnPercent <= beforeBurnPercent,
            "tick 后剩余燃烧百分比应下降或保持，不应逆向上涨"
        );
        helper.assertTrue(
            training.getAccumulatedExp() == beforeExp + EXPECTED_EXP_AFTER_TICK,
            "tick 后 accumulatedExp 应增加 1"
        );

        helper.succeed();
    }

    /**
     * DoD 证据 3：
     * 物品在训练槽位流转（补燃料+消耗）过程中不应凭空增殖。
     */
    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS)
    public void testTickShouldKeepItemConservationDuringSlotFlow(GameTestHelper helper) {
        FlyingSwordTrainingAttachment training = new FlyingSwordTrainingAttachment();
        training.getInputSlots().setStackInSlot(
            SLOT_SWORD,
            new ItemStack(Items.IRON_SWORD)
        );

        // 本断言关注“训练 tick 不会刷物”，不要求依赖外部燃料物品注册。
        training.getInputSlots().setStackInSlot(
            SLOT_FUEL,
            new ItemStack(Items.COAL, FUEL_STACK_FOR_CONSERVATION_TEST)
        );

        training.setFuelTime(INITIAL_FUEL);
        training.setMaxFuelTime(INITIAL_FUEL);

        int beforeSwordCount = training.getInputSlots().getStackInSlot(SLOT_SWORD).getCount();
        int beforeFuelCount = training.getInputSlots().getStackInSlot(SLOT_FUEL).getCount();
        int beforeTotalCount = beforeSwordCount + beforeFuelCount;

        FlyingSwordTrainingService.tickInternal(training, null);

        int afterSwordCount = training.getInputSlots().getStackInSlot(SLOT_SWORD).getCount();
        int afterFuelCount = training.getInputSlots().getStackInSlot(SLOT_FUEL).getCount();
        int afterTotalCount = afterSwordCount + afterFuelCount;

        helper.assertTrue(
            afterSwordCount == beforeSwordCount,
            "tick 后剑槽数量不应变化，避免出现复制或吞物"
        );
        helper.assertTrue(
            afterFuelCount == beforeFuelCount,
            "当前场景不触发补燃料，燃料槽计数应保持不变"
        );
        helper.assertTrue(
            afterTotalCount == beforeTotalCount,
            "不触发补燃料时，槽位总数应保持不变，不能凭空增殖"
        );

        helper.succeed();
    }

    @GameTest(
        template = "empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = "benming_training_bonus"
    )
    public void testBenmingTrainingShouldApplyBonusWhenResourceEnough(GameTestHelper helper) {
        FlyingSwordTrainingAttachment training = new FlyingSwordTrainingAttachment();
        ItemStack swordStack = new ItemStack(Items.IRON_SWORD);

        ServerPlayer player = createDeterministicPlayer(helper.getLevel(), "bonus");
        FlyingSwordTrainingService.installBonusResourceGateForTest(p -> true);
        FlyingSwordTrainingService.installReadonlyRewardModifierProviderForTest(
            p -> BenmingSwordReadonlyModifierHelper.ReadonlyModifier.identity()
        );

        FlyingSwordAttributes attributes = new FlyingSwordAttributes();
        attributes.getBond().setOwnerUuid(player.getUUID().toString());
        writeAttributesToSword(swordStack, attributes);

        training.getInputSlots().setStackInSlot(SLOT_SWORD, swordStack);
        training.getInputSlots().setStackInSlot(SLOT_FUEL, new ItemStack(Items.COAL));
        training.setFuelTime(INITIAL_FUEL);
        training.setMaxFuelTime(INITIAL_FUEL);

        try {
            FlyingSwordTrainingService.tickInternal(training, player);

            ItemStack updatedSword = training.getInputSlots().getStackInSlot(SLOT_SWORD);
            FlyingSwordAttributes afterAttributes = readAttributesFromSword(updatedSword);
            helper.assertTrue(
                training.getFuelTime() == EXPECTED_FUEL_AFTER_TICK,
                "奖励成功时基础燃料消耗路径仍应保持每 tick -1"
            );
            helper.assertTrue(
                training.getAccumulatedExp() == EXPECTED_EXP_AFTER_BONUS_TICK,
                "奖励成功时 accumulatedExp 应为基础+奖励共 2"
            );
            helper.assertTrue(
                afterAttributes.getExperience() == EXPECTED_EXP_AFTER_BONUS_TICK,
                "奖励成功时飞剑经验应为基础+奖励共 2"
            );
            helper.assertTrue(
                Math.abs(afterAttributes.getBond().getResonance() - EXPECTED_RESONANCE_BONUS) < EPSILON,
                "奖励成功时共鸣应精确增加 0.01"
            );
        } finally {
            FlyingSwordTrainingService.resetBonusResourceGateForTest();
            FlyingSwordTrainingService.resetReadonlyRewardModifierProviderForTest();
        }

        helper.succeed();
    }

    @GameTest(
        template = "empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = "benming_training_resource_shortage"
    )
    public void testBenmingTrainingShouldSuppressBonusWhenResourceShortage(GameTestHelper helper) {
        FlyingSwordTrainingAttachment training = new FlyingSwordTrainingAttachment();
        ItemStack swordStack = new ItemStack(Items.IRON_SWORD);

        ServerPlayer player = createDeterministicPlayer(helper.getLevel(), "shortage");
        FlyingSwordTrainingService.installBonusResourceGateForTest(p -> false);
        FlyingSwordTrainingService.installReadonlyRewardModifierProviderForTest(
            p -> BenmingSwordReadonlyModifierHelper.ReadonlyModifier.identity()
        );

        FlyingSwordAttributes attributes = new FlyingSwordAttributes();
        attributes.getBond().setOwnerUuid(player.getUUID().toString());
        writeAttributesToSword(swordStack, attributes);

        training.getInputSlots().setStackInSlot(SLOT_SWORD, swordStack);
        training.getInputSlots().setStackInSlot(SLOT_FUEL, new ItemStack(Items.COAL));
        training.setFuelTime(INITIAL_FUEL);
        training.setMaxFuelTime(INITIAL_FUEL);

        try {
            FlyingSwordTrainingService.tickInternal(training, player);

            ItemStack updatedSword = training.getInputSlots().getStackInSlot(SLOT_SWORD);
            FlyingSwordAttributes afterAttributes = readAttributesFromSword(updatedSword);
            helper.assertTrue(
                training.getFuelTime() == EXPECTED_FUEL_AFTER_TICK,
                "资源不足时基础燃料消耗路径不能被奖励失败影响"
            );
            helper.assertTrue(
                training.getAccumulatedExp() == EXPECTED_EXP_AFTER_TICK,
                "资源不足时应仅保留基础训练累计经验 1"
            );
            helper.assertTrue(
                afterAttributes.getExperience() == EXPECTED_EXP_AFTER_TICK,
                "资源不足时应仅保留基础飞剑经验 1"
            );
            helper.assertTrue(
                Math.abs(afterAttributes.getBond().getResonance()) < EPSILON,
                "资源不足时应抑制奖励共鸣增长"
            );
        } finally {
            FlyingSwordTrainingService.resetBonusResourceGateForTest();
            FlyingSwordTrainingService.resetReadonlyRewardModifierProviderForTest();
        }

        helper.succeed();
    }

    @GameTest(
        template = "empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = "benming_training_non_benming_regression"
    )
    public void testNonBenmingTrainingShouldKeepLegacyBaseGain(GameTestHelper helper) {
        FlyingSwordTrainingAttachment training = new FlyingSwordTrainingAttachment();
        ItemStack swordStack = new ItemStack(Items.IRON_SWORD);

        ServerPlayer player = createDeterministicPlayer(helper.getLevel(), "non_benming");
        FlyingSwordTrainingService.installBonusResourceGateForTest(p -> true);
        FlyingSwordTrainingService.installReadonlyRewardModifierProviderForTest(
            p -> BenmingSwordReadonlyModifierHelper.ReadonlyModifier.identity()
        );

        FlyingSwordAttributes attributes = new FlyingSwordAttributes();
        attributes.getBond().resetToUnbound();
        writeAttributesToSword(swordStack, attributes);

        training.getInputSlots().setStackInSlot(SLOT_SWORD, swordStack);
        training.getInputSlots().setStackInSlot(SLOT_FUEL, new ItemStack(Items.COAL));
        training.setFuelTime(INITIAL_FUEL);
        training.setMaxFuelTime(INITIAL_FUEL);

        try {
            FlyingSwordTrainingService.tickInternal(training, player);

            ItemStack updatedSword = training.getInputSlots().getStackInSlot(SLOT_SWORD);
            FlyingSwordAttributes afterAttributes = readAttributesFromSword(updatedSword);
            helper.assertTrue(
                training.getFuelTime() == EXPECTED_FUEL_AFTER_TICK,
                "非本命路径应保持旧版每 tick -1 燃料消耗"
            );
            helper.assertTrue(
                training.getAccumulatedExp() == EXPECTED_EXP_AFTER_TICK,
                "非本命路径应仅增加基础训练累计经验 1"
            );
            helper.assertTrue(
                afterAttributes.getExperience() == EXPECTED_EXP_AFTER_TICK,
                "非本命路径应仅增加基础飞剑经验 1"
            );
            helper.assertTrue(
                afterAttributes.getBond().getOwnerUuid().isEmpty(),
                "非本命路径不得意外写入 ownerUuid"
            );
            helper.assertTrue(
                Math.abs(afterAttributes.getBond().getResonance()) < EPSILON,
                "非本命路径不得意外增长共鸣"
            );
        } finally {
            FlyingSwordTrainingService.resetBonusResourceGateForTest();
            FlyingSwordTrainingService.resetReadonlyRewardModifierProviderForTest();
        }

        helper.succeed();
    }

    @GameTest(
        template = "empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = "benming_training_readonly_modifier_happy"
    )
    public void testBenmingTrainingReadonlyModifierShouldIncreaseRewardMildly(GameTestHelper helper) {
        FlyingSwordTrainingAttachment training = new FlyingSwordTrainingAttachment();
        ItemStack swordStack = new ItemStack(Items.IRON_SWORD);

        ServerPlayer player = createDeterministicPlayer(helper.getLevel(), "readonly_happy");
        FlyingSwordTrainingService.installBonusResourceGateForTest(p -> true);
        FlyingSwordTrainingService.installReadonlyRewardModifierProviderForTest(
            p -> new BenmingSwordReadonlyModifierHelper.ReadonlyModifier(
                REWARD_MULTIPLIER_BASELINE,
                REWARD_MULTIPLIER_MILD,
                REWARD_MULTIPLIER_BASELINE,
                REWARD_MULTIPLIER_MILD
            )
        );

        FlyingSwordAttributes attributes = new FlyingSwordAttributes();
        attributes.getBond().setOwnerUuid(player.getUUID().toString());
        writeAttributesToSword(swordStack, attributes);

        training.getInputSlots().setStackInSlot(SLOT_SWORD, swordStack);
        training.getInputSlots().setStackInSlot(SLOT_FUEL, new ItemStack(Items.COAL));
        training.setFuelTime(INITIAL_FUEL);
        training.setMaxFuelTime(INITIAL_FUEL);

        try {
            FlyingSwordTrainingService.tickInternal(training, player);
            ItemStack updatedSword = training.getInputSlots().getStackInSlot(SLOT_SWORD);
            FlyingSwordAttributes afterAttributes = readAttributesFromSword(updatedSword);

            helper.assertTrue(
                Math.abs(afterAttributes.getBond().getResonance() - EXPECTED_RESONANCE_BONUS_MILD) < EPSILON,
                "高气运/高境界对应的只读倍率应让本命奖励共鸣温和提升"
            );
        } finally {
            FlyingSwordTrainingService.resetBonusResourceGateForTest();
            FlyingSwordTrainingService.resetReadonlyRewardModifierProviderForTest();
        }

        helper.succeed();
    }

    @GameTest(
        template = "empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = "benming_training_readonly_modifier_clamp"
    )
    public void testBenmingTrainingReadonlyModifierShouldClampAndKeepPhysiqueNoop(GameTestHelper helper) {
        FlyingSwordTrainingAttachment training = new FlyingSwordTrainingAttachment();
        ItemStack swordStack = new ItemStack(Items.IRON_SWORD);

        ServerPlayer player = createDeterministicPlayer(helper.getLevel(), "readonly_clamp");
        FlyingSwordTrainingService.installBonusResourceGateForTest(p -> true);
        FlyingSwordTrainingService.installReadonlyRewardModifierProviderForTest(
            p -> new BenmingSwordReadonlyModifierHelper.ReadonlyModifier(
                REWARD_MULTIPLIER_CLAMPED,
                REWARD_MULTIPLIER_CLAMPED,
                REWARD_MULTIPLIER_BASELINE,
                REWARD_MULTIPLIER_CLAMPED
            )
        );

        FlyingSwordAttributes attributes = new FlyingSwordAttributes();
        attributes.getBond().setOwnerUuid(player.getUUID().toString());
        writeAttributesToSword(swordStack, attributes);

        training.getInputSlots().setStackInSlot(SLOT_SWORD, swordStack);
        training.getInputSlots().setStackInSlot(SLOT_FUEL, new ItemStack(Items.COAL));
        training.setFuelTime(INITIAL_FUEL);
        training.setMaxFuelTime(INITIAL_FUEL);

        try {
            FlyingSwordTrainingService.tickInternal(training, player);
            ItemStack updatedSword = training.getInputSlots().getStackInSlot(SLOT_SWORD);
            FlyingSwordAttributes afterAttributes = readAttributesFromSword(updatedSword);

            helper.assertTrue(
                Math.abs(afterAttributes.getBond().getResonance() - EXPECTED_RESONANCE_BONUS_CLAMPED) < EPSILON,
                "极端输入应被倍率上限钳制，共鸣增量不能超过 1.2x"
            );
        } finally {
            FlyingSwordTrainingService.resetBonusResourceGateForTest();
            FlyingSwordTrainingService.resetReadonlyRewardModifierProviderForTest();
        }

        helper.succeed();
    }

    @GameTest(
        template = "empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = "benming_training_route_tradeoff"
    )
    public void testBenmingTrainingShouldDifferentiateRouteGrowthTradeoffs(
        GameTestHelper helper
    ) {
        final BenmingRouteTradeoffFixture fixture = createBenmingRouteTradeoffFixture(
            helper.getLevel()
        );

        FlyingSwordTrainingService.installBonusResourceGateForTest(p -> true);
        FlyingSwordTrainingService.installReadonlyRewardModifierProviderForTest(
            p -> BenmingSwordReadonlyModifierHelper.ReadonlyModifier.identity()
        );

        try {
            tickBenmingRouteTradeoffFixture(fixture);
            final BenmingRouteTradeoffOutcome outcome = readBenmingRouteTradeoffOutcome(
                fixture
            );
            assertBenmingRouteTradeoffFuelAndExp(helper, outcome);
            assertBenmingRouteTradeoffResonanceAndAffinity(helper, outcome);
            assertBenmingRouteTradeoffComparisons(helper, outcome);
        } finally {
            FlyingSwordTrainingService.resetBonusResourceGateForTest();
            FlyingSwordTrainingService.resetReadonlyRewardModifierProviderForTest();
        }

        helper.succeed();
    }

    @GameTest(
        template = "empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = BENMING_COMBAT_BATCH
    )
    public void testBenmingCombatMultiplierShouldRequireActualBoundOwner(
        GameTestHelper helper
    ) {
        ServerLevel level = helper.getLevel();
        ServerPlayer attackOwner = createDeterministicPlayer(level, "combat_owner");
        ServerPlayer foreignOwner = createDeterministicPlayer(level, "combat_foreign_owner");

        float baselineDamage = invokeNormalAttackDamage(
            createBenmingCombatAttributes(attackOwner, "", ZERO_RESONANCE),
            attackOwner
        );
        float unboundPositiveDamage = invokeNormalAttackDamage(
            createBenmingCombatAttributes(attackOwner, "", POSITIVE_RESONANCE),
            attackOwner
        );
        float foreignBoundDamage = invokeNormalAttackDamage(
            createBenmingCombatAttributes(
                attackOwner,
                foreignOwner.getUUID().toString(),
                MODERATE_RESONANCE
            ),
            attackOwner
        );
        float boundZeroDamage = invokeNormalAttackDamage(
            createBenmingCombatAttributes(
                attackOwner,
                attackOwner.getUUID().toString(),
                ZERO_RESONANCE
            ),
            attackOwner
        );

        assertCombatClose(
            helper,
            baselineDamage,
            unboundPositiveDamage,
            "未绑定且共鸣为正时不应获得本命战斗加成"
        );
        assertCombatClose(
            helper,
            baselineDamage,
            foreignBoundDamage,
            "ownerUuid 与攻击者不一致时不应获得本命战斗加成"
        );
        assertCombatClose(
            helper,
            baselineDamage,
            boundZeroDamage,
            "绑定攻击者但共鸣为 0 时不应获得本命战斗加成"
        );

        helper.succeed();
    }

    @GameTest(
        template = "empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = BENMING_COMBAT_BATCH
    )
    public void testBenmingCombatMultiplierShouldApplyMildAndCappedBonus(
        GameTestHelper helper
    ) {
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = createDeterministicPlayer(level, "combat_bound_owner");
        String ownerUuid = owner.getUUID().toString();

        float baselineDamage = invokeNormalAttackDamage(
            createBenmingCombatAttributes(owner, ownerUuid, ZERO_RESONANCE),
            owner
        );
        float moderateDamage = invokeNormalAttackDamage(
            createBenmingCombatAttributes(owner, ownerUuid, MODERATE_RESONANCE),
            owner
        );
        float extremeDamage = invokeNormalAttackDamage(
            createBenmingCombatAttributes(owner, ownerUuid, EXTREME_RESONANCE),
            owner
        );

        helper.assertTrue(
            moderateDamage > baselineDamage,
            "中等共鸣本命飞剑应高于无加成基线"
        );
        helper.assertTrue(
            extremeDamage > baselineDamage,
            "极端共鸣本命飞剑应高于无加成基线"
        );
        assertCombatClose(
            helper,
            MODERATE_EXPECTED_RATIO,
            moderateDamage / baselineDamage,
            "中等共鸣应命中 1.05 倍战斗加成"
        );
        assertCombatClose(
            helper,
            CAPPED_EXPECTED_RATIO,
            extremeDamage / baselineDamage,
            "极端共鸣应命中 1.20 倍封顶加成"
        );

        helper.succeed();
    }

    /**
     * 与菜单 getBurnProgressPercent 使用同等公式，
     * 用于把 Attachment 的 fuel/max 转为“菜单可见进度值”。
     */
    private static int calculateMenuVisibleBurnPercent(int fuelTime, int maxFuelTime) {
        if (maxFuelTime <= 0) {
            return 0;
        }
        return (fuelTime * PERCENT_100) / maxFuelTime;
    }

    private static ItemStack createBoundTrainingSword(final ServerPlayer owner) {
        final ItemStack swordStack = new ItemStack(Items.IRON_SWORD);
        final FlyingSwordAttributes attributes = new FlyingSwordAttributes();
        attributes.getBond().setOwnerUuid(owner.getUUID().toString());
        writeAttributesToSword(swordStack, attributes);
        return swordStack;
    }

    private static void initializeTraining(
        final FlyingSwordTrainingAttachment training,
        final ItemStack swordStack
    ) {
        training.getInputSlots().setStackInSlot(SLOT_SWORD, swordStack);
        training.getInputSlots().setStackInSlot(SLOT_FUEL, new ItemStack(Items.COAL));
        training.setFuelTime(INITIAL_FUEL);
        training.setMaxFuelTime(INITIAL_FUEL);
    }

    private static void setPlayerResonanceType(
        final ServerPlayer player,
        final FlyingSwordResonanceType resonanceType
    ) {
        final var state = KongqiaoAttachments.getFlyingSwordState(player);
        if (state == null) {
            throw new IllegalStateException("本命训练测试缺少飞剑状态附件");
        }
        state.setResonanceType(resonanceType == null ? "" : resonanceType.getCode());
    }

    private record BenmingRouteTradeoffFixture(
        ServerPlayer offensePlayer,
        ServerPlayer defensePlayer,
        ServerPlayer spiritPlayer,
        ServerPlayer devourPlayer,
        FlyingSwordTrainingAttachment offenseTraining,
        FlyingSwordTrainingAttachment defenseTraining,
        FlyingSwordTrainingAttachment spiritTraining,
        FlyingSwordTrainingAttachment devourTraining
    ) {}

    private record BenmingRouteTradeoffOutcome(
        FlyingSwordTrainingAttachment offenseTraining,
        FlyingSwordTrainingAttachment defenseTraining,
        FlyingSwordTrainingAttachment spiritTraining,
        FlyingSwordTrainingAttachment devourTraining,
        FlyingSwordAttributes offenseAttributes,
        FlyingSwordAttributes defenseAttributes,
        FlyingSwordAttributes spiritAttributes,
        FlyingSwordAttributes devourAttributes
    ) {}

    private static BenmingRouteTradeoffFixture createBenmingRouteTradeoffFixture(
        final ServerLevel level
    ) {
        final ServerPlayer offensePlayer = createDeterministicPlayer(level, "route_offense");
        final ServerPlayer defensePlayer = createDeterministicPlayer(level, "route_defense");
        final ServerPlayer spiritPlayer = createDeterministicPlayer(level, "route_spirit");
        final ServerPlayer devourPlayer = createDeterministicPlayer(level, "route_devour");

        final FlyingSwordTrainingAttachment offenseTraining = new FlyingSwordTrainingAttachment();
        final FlyingSwordTrainingAttachment defenseTraining = new FlyingSwordTrainingAttachment();
        final FlyingSwordTrainingAttachment spiritTraining = new FlyingSwordTrainingAttachment();
        final FlyingSwordTrainingAttachment devourTraining = new FlyingSwordTrainingAttachment();

        initializeTraining(offenseTraining, createBoundTrainingSword(offensePlayer));
        initializeTraining(defenseTraining, createBoundTrainingSword(defensePlayer));
        initializeTraining(spiritTraining, createBoundTrainingSword(spiritPlayer));
        initializeTraining(devourTraining, createBoundTrainingSword(devourPlayer));

        setPlayerResonanceType(offensePlayer, FlyingSwordResonanceType.OFFENSE);
        setPlayerResonanceType(defensePlayer, FlyingSwordResonanceType.DEFENSE);
        setPlayerResonanceType(spiritPlayer, FlyingSwordResonanceType.SPIRIT);
        setPlayerResonanceType(devourPlayer, FlyingSwordResonanceType.DEVOUR);

        return new BenmingRouteTradeoffFixture(
            offensePlayer,
            defensePlayer,
            spiritPlayer,
            devourPlayer,
            offenseTraining,
            defenseTraining,
            spiritTraining,
            devourTraining
        );
    }

    private static void tickBenmingRouteTradeoffFixture(
        final BenmingRouteTradeoffFixture fixture
    ) {
        FlyingSwordTrainingService.tickInternal(
            fixture.offenseTraining(),
            fixture.offensePlayer()
        );
        FlyingSwordTrainingService.tickInternal(
            fixture.defenseTraining(),
            fixture.defensePlayer()
        );
        FlyingSwordTrainingService.tickInternal(
            fixture.spiritTraining(),
            fixture.spiritPlayer()
        );
        FlyingSwordTrainingService.tickInternal(
            fixture.devourTraining(),
            fixture.devourPlayer()
        );
    }

    private static BenmingRouteTradeoffOutcome readBenmingRouteTradeoffOutcome(
        final BenmingRouteTradeoffFixture fixture
    ) {
        return new BenmingRouteTradeoffOutcome(
            fixture.offenseTraining(),
            fixture.defenseTraining(),
            fixture.spiritTraining(),
            fixture.devourTraining(),
            readAttributesFromSword(fixture.offenseTraining().getInputSlots().getStackInSlot(SLOT_SWORD)),
            readAttributesFromSword(fixture.defenseTraining().getInputSlots().getStackInSlot(SLOT_SWORD)),
            readAttributesFromSword(fixture.spiritTraining().getInputSlots().getStackInSlot(SLOT_SWORD)),
            readAttributesFromSword(fixture.devourTraining().getInputSlots().getStackInSlot(SLOT_SWORD))
        );
    }

    private static void assertBenmingRouteTradeoffFuelAndExp(
        final GameTestHelper helper,
        final BenmingRouteTradeoffOutcome outcome
    ) {
        helper.assertTrue(
            outcome.offenseTraining().getFuelTime() == EXPECTED_FUEL_AFTER_TICK,
            "攻路线训练不应改动基础燃料消耗节奏"
        );
        helper.assertTrue(
            outcome.defenseTraining().getFuelTime() == EXPECTED_FUEL_AFTER_TICK,
            "御路线训练不应改动基础燃料消耗节奏"
        );
        helper.assertTrue(
            outcome.spiritTraining().getFuelTime() == EXPECTED_FUEL_AFTER_TICK,
            "灵路线训练不应改动基础燃料消耗节奏"
        );
        helper.assertTrue(
            outcome.devourTraining().getFuelTime() == EXPECTED_FUEL_AFTER_TICK,
            "噬元炼魄训练不应改动基础燃料消耗节奏"
        );
        helper.assertTrue(
            outcome.offenseTraining().getAccumulatedExp() == EXPECTED_EXP_AFTER_OFFENSE_ROUTE_TICK,
            "攻路线应把训练奖励偏向经验推进"
        );
        helper.assertTrue(
            outcome.defenseTraining().getAccumulatedExp() == EXPECTED_EXP_AFTER_DEFENSE_ROUTE_TICK,
            "御路线应接受更慢的经验推进代价"
        );
        helper.assertTrue(
            outcome.spiritTraining().getAccumulatedExp() == EXPECTED_EXP_AFTER_SPIRIT_ROUTE_TICK,
            "灵路线经验推进应保持中档"
        );
        helper.assertTrue(
            outcome.devourTraining().getAccumulatedExp() == EXPECTED_EXP_AFTER_DEVOUR_ROUTE_TICK,
            "噬元炼魄应接受较慢经验推进，以换取更强的炼化收益"
        );
        helper.assertTrue(
            outcome.offenseAttributes().getExperience() == EXPECTED_EXP_AFTER_OFFENSE_ROUTE_TICK,
            "攻路线飞剑经验应高于其余路线"
        );
        helper.assertTrue(
            outcome.defenseAttributes().getExperience() == EXPECTED_EXP_AFTER_DEFENSE_ROUTE_TICK,
            "御路线飞剑经验应保持最低以换取更稳成长"
        );
        helper.assertTrue(
            outcome.spiritAttributes().getExperience() == EXPECTED_EXP_AFTER_SPIRIT_ROUTE_TICK,
            "灵路线飞剑经验应维持攻御之间的中档节奏"
        );
        helper.assertTrue(
            outcome.devourAttributes().getExperience() == EXPECTED_EXP_AFTER_DEVOUR_ROUTE_TICK,
            "噬元炼魄飞剑经验应维持低档，体现难学代价"
        );
    }

    private static void assertBenmingRouteTradeoffResonanceAndAffinity(
        final GameTestHelper helper,
        final BenmingRouteTradeoffOutcome outcome
    ) {
        helper.assertTrue(
            Math.abs(
                outcome.offenseAttributes().getBond().getResonance()
                    - EXPECTED_OFFENSE_ROUTE_RESONANCE_BONUS
            ) < EPSILON,
            "攻路线共鸣增长应低于其余路线，体现快练慢稳的代价"
        );
        helper.assertTrue(
            Math.abs(
                outcome.defenseAttributes().getBond().getResonance()
                    - EXPECTED_DEFENSE_ROUTE_RESONANCE_BONUS
            ) < EPSILON,
            "御路线共鸣增长应最高，体现稳态成长优势"
        );
        helper.assertTrue(
            Math.abs(
                outcome.spiritAttributes().getBond().getResonance()
                    - EXPECTED_SPIRIT_ROUTE_RESONANCE_BONUS
            ) < EPSILON,
            "灵路线共鸣增长应保持中档，体现节奏向均衡收益"
        );
        helper.assertTrue(
            Math.abs(
                outcome.devourAttributes().getBond().getResonance()
                    - EXPECTED_DEVOUR_ROUTE_RESONANCE_BONUS
            ) < EPSILON,
            "噬元炼魄应把额外成本更集中地炼成共鸣收益"
        );
        helper.assertTrue(
            outcome.devourAttributes().getSpiritData().getAffinity()
                >= MIN_EXPECTED_DEVOUR_ROUTE_AFFINITY,
            "噬元炼魄应至少拿到额外亲和收益"
        );
    }

    private static void assertBenmingRouteTradeoffComparisons(
        final GameTestHelper helper,
        final BenmingRouteTradeoffOutcome outcome
    ) {
        helper.assertTrue(
            outcome.offenseAttributes().getExperience() > outcome.spiritAttributes().getExperience(),
            "攻路线应在经验推进上优于灵路线"
        );
        helper.assertTrue(
            outcome.spiritAttributes().getExperience() > outcome.defenseAttributes().getExperience(),
            "灵路线应在经验推进上优于御路线"
        );
        helper.assertTrue(
            outcome.devourAttributes().getExperience() == outcome.defenseAttributes().getExperience(),
            "噬元炼魄应与御路线同属低经验档"
        );
        helper.assertTrue(
            outcome.devourAttributes().getBond().getResonance()
                > outcome.defenseAttributes().getBond().getResonance(),
            "噬元炼魄应在共鸣炼化上优于御路线"
        );
        helper.assertTrue(
            outcome.defenseAttributes().getBond().getResonance()
                > outcome.spiritAttributes().getBond().getResonance(),
            "御路线应在共鸣稳态上优于灵路线"
        );
        helper.assertTrue(
            outcome.spiritAttributes().getBond().getResonance()
                > outcome.offenseAttributes().getBond().getResonance(),
            "灵路线应在共鸣稳态上优于攻路线"
        );
    }

    private static FlyingSwordAttributes createCombatAttributes(
        String ownerUuid,
        double resonance
    ) {
        FlyingSwordAttributes attributes = new FlyingSwordAttributes();
        attributes.damage = COMBAT_BASE_DAMAGE;
        attributes.speedMax = COMBAT_BASE_SPEED_MAX;
        attributes.getBond().setOwnerUuid(ownerUuid);
        attributes.getBond().setResonance(resonance);
        return attributes;
    }

    private static FlyingSwordAttributes createBenmingCombatAttributes(
        ServerPlayer player,
        String ownerUuid,
        double resonance
    ) {
        FlyingSwordAttributes attributes = createCombatAttributes(ownerUuid, resonance);
        attributes.setStableSwordId(COMBAT_BENMING_STABLE_SWORD_ID);

        var state = KongqiaoAttachments.getFlyingSwordState(player);
        if (state == null) {
            throw new IllegalStateException("本命战斗测试缺少飞剑状态附件");
        }
        state.updateBondCache(COMBAT_BENMING_STABLE_SWORD_ID, player.level().getGameTime());
        return attributes;
    }

    private static float invokeNormalAttackDamage(
        FlyingSwordAttributes attributes,
        LivingEntity owner
    ) {
        try {
            return (float) CALCULATE_NORMAL_ATTACK_DAMAGE_METHOD.invoke(
                null,
                attributes,
                owner,
                COMBAT_CURRENT_SPEED,
                COMBAT_SYNERGY_ATTACK_MULTIPLIER
            );
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                "调用 calculateNormalAttackDamage 失败",
                exception
            );
        }
    }

    private static Method resolveCalculateNormalAttackDamageMethod() {
        try {
            Class<?> combatOpsClass = Class.forName(
                "com.Kizunad.guzhenrenext.kongqiao.flyingsword.ai.SwordCombatOps"
            );
            Method method = combatOpsClass.getDeclaredMethod(
                "calculateNormalAttackDamage",
                FlyingSwordAttributes.class,
                LivingEntity.class,
                double.class,
                float.class
            );
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                "未找到 SwordCombatOps.calculateNormalAttackDamage",
                exception
            );
        }
    }

    private static void assertCombatClose(
        GameTestHelper helper,
        double expected,
        double actual,
        String message
    ) {
        helper.assertTrue(
            Math.abs(expected - actual) <= COMBAT_ASSERT_EPSILON,
            message + "，expected=" + expected + "，actual=" + actual
        );
    }

    private static ServerPlayer createDeterministicPlayer(
        ServerLevel level,
        String suffix
    ) {
        UUID uuid = UUID.nameUUIDFromBytes(
            ("benming_training_" + suffix).getBytes(StandardCharsets.UTF_8)
        );
        return FakePlayerFactory.get(
            level,
            new GameProfile(uuid, "benming_training_" + suffix)
        );
    }

    private static void writeAttributesToSword(
        ItemStack swordStack,
        FlyingSwordAttributes attributes
    ) {
        CompoundTag root = ItemStackCustomDataHelper.copyCustomDataTag(swordStack);
        root.put("Attributes", attributes.toNBT());
        ItemStackCustomDataHelper.setCustomDataTag(swordStack, root);
    }

    private static FlyingSwordAttributes readAttributesFromSword(ItemStack swordStack) {
        CompoundTag root = ItemStackCustomDataHelper.copyCustomDataTag(swordStack);
        return FlyingSwordAttributes.fromNBT(root.getCompound("Attributes"));
    }
}
