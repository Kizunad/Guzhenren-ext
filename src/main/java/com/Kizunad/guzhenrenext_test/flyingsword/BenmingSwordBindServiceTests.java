package com.Kizunad.guzhenrenext_test.flyingsword;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordController;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordEntity;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordSpawner;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordCooldownAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordSelectionAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordStateAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.BenmingSwordBondService;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.BenmingSwordResourceTransaction;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.CultivationSnapshot;
import com.mojang.authlib.GameProfile;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder("guzhenrenext")
public class BenmingSwordBindServiceTests {

    private static final int TEST_TIMEOUT_TICKS = 80;
    private static final int PLAYER_RELATIVE_X = 4;
    private static final int PLAYER_RELATIVE_Y = 2;
    private static final int PLAYER_RELATIVE_Z = 4;

    private static final String HAPPY_STABLE_ID = "task7-bind-happy-sword-001";
    private static final String DUPLICATE_FIRST_STABLE_ID = "task7-bind-duplicate-first-001";
    private static final String DUPLICATE_SECOND_STABLE_ID = "task7-bind-duplicate-second-001";
    private static final String FORCED_UNBIND_STABLE_ID = "task12-forced-unbind-001";
    private static final String COOLDOWN_FIRST_STABLE_ID = "task12-cooldown-first-001";
    private static final String COOLDOWN_SECOND_STABLE_ID = "task12-cooldown-second-001";
    private static final double TEST_RESOURCE_INITIAL = 200.0D;
    private static final double DOUBLE_EPSILON = 1.0E-6D;
    private static final double EXPECTED_LIGHT_BACKLASH_ZHENYUAN_LOSS = 12.0D;
    private static final double EXPECTED_LIGHT_BACKLASH_NIANTOU_LOSS = 8.0D;
    private static final double EXPECTED_LIGHT_BACKLASH_HUNPO_LOSS = 5.0D;
    private static final int EXPECTED_LIGHT_BACKLASH_OPERATION_COUNT = 3;

    @GameTest(
        template = "empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = "benming_bind_happy"
    )
    public void testBindHappyPath(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createDeterministicPlayer(level, "bind_happy_path");
        relocatePlayer(helper, player);

        FlyingSwordEntity sword = FlyingSwordSpawner.spawnBasic(level, player);
        helper.assertTrue(sword != null, "前置失败：happy 场景飞剑生成失败");

        sword.getSwordAttributes().setStableSwordId(HAPPY_STABLE_ID);
        sword.getSwordAttributes().getBond().resetToUnbound();
        sword.syncAttributesToEntityData();

        BenmingSwordBondService.Result bindResult =
            FlyingSwordController.bindSelectedOrNearestSwordAsBenming(level, player);
        helper.assertTrue(bindResult.success(), "happy：绑定应成功");
        helper.assertTrue(
            bindResult.branch() == BenmingSwordBondService.ResultBranch.BIND,
            "happy：绑定结果分支应为 BIND"
        );
        helper.assertTrue(
            HAPPY_STABLE_ID.equals(bindResult.stableSwordId()),
            "happy：绑定返回的 stableSwordId 应与目标飞剑一致"
        );
        helper.assertTrue(
            player.getUUID().toString().equals(sword.getSwordAttributes().getBond().getOwnerUuid()),
            "happy：绑定后 sword.bond.ownerUuid 应写为玩家 UUID"
        );

        FlyingSwordStateAttachment state = KongqiaoAttachments.getFlyingSwordState(player);
        helper.assertTrue(state != null, "happy：玩家状态附件不应为空");
        helper.assertTrue(
            HAPPY_STABLE_ID.equals(state.getBondedSwordId()),
            "happy：绑定后缓存中的 bondedSwordId 应为目标 stableSwordId"
        );
        helper.assertTrue(
            !state.isBondCacheDirty(),
            "happy：绑定成功后缓存应为 clean 状态"
        );

        BenmingSwordBondService.Result queryResult =
            FlyingSwordController.queryBenmingSword(level, player);
        helper.assertTrue(queryResult.success(), "happy：查询本命应成功");
        helper.assertTrue(
            queryResult.branch() == BenmingSwordBondService.ResultBranch.QUERY,
            "happy：查询结果分支应为 QUERY"
        );
        helper.assertTrue(
            HAPPY_STABLE_ID.equals(queryResult.stableSwordId()),
            "happy：查询返回的 stableSwordId 应稳定等于已绑定飞剑"
        );

        helper.succeed();
    }

    @GameTest(
        template = "empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = "benming_bind_duplicate_guard"
    )
    public void testDuplicateBindGuard(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createDeterministicPlayer(level, "bind_duplicate_guard");
        relocatePlayer(helper, player);

        FlyingSwordEntity firstSword = FlyingSwordSpawner.spawnBasic(level, player);
        FlyingSwordEntity secondSword = FlyingSwordSpawner.spawnBasic(level, player);
        helper.assertTrue(firstSword != null, "guard：第一把飞剑生成失败");
        helper.assertTrue(secondSword != null, "guard：第二把飞剑生成失败");

        firstSword.getSwordAttributes().setStableSwordId(DUPLICATE_FIRST_STABLE_ID);
        firstSword.getSwordAttributes().getBond().resetToUnbound();
        firstSword.syncAttributesToEntityData();
        secondSword.getSwordAttributes().setStableSwordId(DUPLICATE_SECOND_STABLE_ID);
        secondSword.getSwordAttributes().getBond().resetToUnbound();
        secondSword.syncAttributesToEntityData();

        FlyingSwordSelectionAttachment selection =
            KongqiaoAttachments.getFlyingSwordSelection(player);
        helper.assertTrue(selection != null, "guard：选择附件不应为空");

        selection.setSelectedSword(firstSword.getUUID());
        BenmingSwordBondService.Result firstBindResult =
            FlyingSwordController.bindSelectedOrNearestSwordAsBenming(level, player);
        helper.assertTrue(firstBindResult.success(), "guard：首次绑定应成功");

        selection.setSelectedSword(secondSword.getUUID());
        BenmingSwordBondService.Result duplicateBindResult =
            FlyingSwordController.bindSelectedOrNearestSwordAsBenming(level, player);
        helper.assertTrue(!duplicateBindResult.success(), "guard：重复绑定应失败");
        helper.assertTrue(
            duplicateBindResult.failureReason()
                == BenmingSwordBondService.FailureReason.PLAYER_ALREADY_HAS_BONDED_SWORD,
            "guard：重复绑定失败原因应为 PLAYER_ALREADY_HAS_BONDED_SWORD"
        );
        helper.assertTrue(
            DUPLICATE_FIRST_STABLE_ID.equals(duplicateBindResult.stableSwordId()),
            "guard：失败返回应指向已存在的本命 stableSwordId"
        );

        helper.assertTrue(
            player.getUUID().toString().equals(firstSword.getSwordAttributes().getBond().getOwnerUuid()),
            "guard：原本命飞剑绑定关系必须保持不变"
        );
        helper.assertTrue(
            secondSword.getSwordAttributes().getBond().getOwnerUuid().isBlank(),
            "guard：第二把飞剑在重复绑定失败后仍应未绑定"
        );

        BenmingSwordBondService.Result queryResult =
            FlyingSwordController.queryBenmingSword(level, player);
        helper.assertTrue(queryResult.success(), "guard：查询本命应成功");
        helper.assertTrue(
            DUPLICATE_FIRST_STABLE_ID.equals(queryResult.stableSwordId()),
            "guard：查询结果应保持原本命 stableSwordId"
        );

        helper.succeed();
    }

    @GameTest(
        template = "empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = "benming_forced_unbind_backlash"
    )
    public void testForcedUnbindAppliesSingleLightBacklash(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createDeterministicPlayer(
            level,
            "forced_unbind_single_backlash"
        );
        relocatePlayer(helper, player);
        FlyingSwordEntity sword = FlyingSwordSpawner.spawnBasic(level, player);
        helper.assertTrue(sword != null, "forced：前置失败，飞剑生成失败");

        sword.getSwordAttributes().setStableSwordId(FORCED_UNBIND_STABLE_ID);
        sword.getSwordAttributes().getBond().setOwnerUuid(player.getUUID().toString());
        sword.syncAttributesToEntityData();

        FlyingSwordStateAttachment state = KongqiaoAttachments.getFlyingSwordState(player);
        helper.assertTrue(state != null, "forced：状态附件不应为空");
        state.updateBondCache(FORCED_UNBIND_STABLE_ID, level.getGameTime());

        FlyingSwordCooldownAttachment cooldownAttachment =
            KongqiaoAttachments.getFlyingSwordCooldowns(player);
        helper.assertTrue(cooldownAttachment != null, "forced：冷却附件不应为空");
        String cooldownKey = BenmingSwordBondService.defaultLightBacklashCooldownKey(
            player.getUUID().toString()
        );

        MutationRecorder mutationRecorder = new MutationRecorder();

        BenmingSwordBondService.Result forcedResult = BenmingSwordBondService.forcedUnbind(
            player.getUUID().toString(),
            toSwordBondPort(sword),
            toPlayerCachePort(state),
            level.getGameTime(),
            new BenmingSwordBondService.BacklashContext(
                CultivationSnapshot.of(
                    TEST_RESOURCE_INITIAL,
                    TEST_RESOURCE_INITIAL,
                    TEST_RESOURCE_INITIAL,
                    0.0D,
                    0
                ),
                mutationRecorder,
                BenmingSwordBondService.toBacklashCooldownPort(cooldownAttachment),
                cooldownKey,
                BenmingSwordBondService.defaultLightBacklashCooldownTicks()
            )
        );

        helper.assertTrue(forcedResult.success(), "forced：强制解绑应成功");
        helper.assertTrue(
            forcedResult.branch() == BenmingSwordBondService.ResultBranch.FORCED_UNBIND,
            "forced：结果分支应为 FORCED_UNBIND"
        );
        helper.assertTrue(
            forcedResult.backlashEffect().type()
                == BenmingSwordBondService.BacklashType.FORCED_UNBIND_LIGHT,
            "forced：应产生轻度强制解绑反噬"
        );

        assertClose(
            helper,
            EXPECTED_LIGHT_BACKLASH_ZHENYUAN_LOSS,
            mutationRecorder.zhenyuanLoss(),
            "forced：真元应仅扣除一次轻度反噬值"
        );
        assertClose(
            helper,
            EXPECTED_LIGHT_BACKLASH_NIANTOU_LOSS,
            mutationRecorder.niantouLoss(),
            "forced：念头应仅扣除一次轻度反噬值"
        );
        assertClose(
            helper,
            EXPECTED_LIGHT_BACKLASH_HUNPO_LOSS,
            mutationRecorder.hunpoLoss(),
            "forced：魂魄应仅扣除一次轻度反噬值"
        );
        helper.assertTrue(
            cooldownAttachment.get(cooldownKey)
                == BenmingSwordBondService.defaultLightBacklashCooldownTicks(),
            "forced：反噬冷却应被写入默认时长"
        );

        helper.assertTrue(
            sword.getSwordAttributes().getBond().getOwnerUuid().isBlank(),
            "forced：解绑后飞剑 ownerUuid 应被清空"
        );
        helper.assertTrue(
            state.getBondedSwordId().isBlank() && state.isBondCacheDirty(),
            "forced：解绑后玩家缓存应被清空并标记 dirty"
        );
        helper.assertTrue(
            mutationRecorder.operationCount() == EXPECTED_LIGHT_BACKLASH_OPERATION_COUNT,
            "forced：轻度反噬应只发生一次三资源扣减"
        );

        helper.succeed();
    }

    @GameTest(
        template = "empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = "benming_forced_unbind_cooldown_guard"
    )
    public void testBacklashCooldownSuppressesRepeatedStormBehavior(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createDeterministicPlayer(
            level,
            "forced_unbind_cooldown_guard"
        );
        relocatePlayer(helper, player);
        FlyingSwordEntity firstSword = FlyingSwordSpawner.spawnBasic(level, player);
        FlyingSwordEntity secondSword = FlyingSwordSpawner.spawnBasic(level, player);
        helper.assertTrue(firstSword != null, "cooldown：第一把飞剑生成失败");
        helper.assertTrue(secondSword != null, "cooldown：第二把飞剑生成失败");

        firstSword.getSwordAttributes().setStableSwordId(COOLDOWN_FIRST_STABLE_ID);
        firstSword.getSwordAttributes().getBond().setOwnerUuid(player.getUUID().toString());
        firstSword.syncAttributesToEntityData();
        secondSword.getSwordAttributes().setStableSwordId(COOLDOWN_SECOND_STABLE_ID);
        secondSword.getSwordAttributes().getBond().setOwnerUuid(player.getUUID().toString());
        secondSword.syncAttributesToEntityData();

        FlyingSwordStateAttachment state = KongqiaoAttachments.getFlyingSwordState(player);
        helper.assertTrue(state != null, "cooldown：状态附件不应为空");
        state.updateBondCache(COOLDOWN_FIRST_STABLE_ID, level.getGameTime());

        FlyingSwordCooldownAttachment cooldownAttachment =
            KongqiaoAttachments.getFlyingSwordCooldowns(player);
        helper.assertTrue(cooldownAttachment != null, "cooldown：冷却附件不应为空");
        String cooldownKey = BenmingSwordBondService.defaultLightBacklashCooldownKey(
            player.getUUID().toString()
        );

        MutationRecorder mutationRecorder = new MutationRecorder();

        BenmingSwordBondService.Result firstResult = BenmingSwordBondService.forcedUnbind(
            player.getUUID().toString(),
            toSwordBondPort(firstSword),
            toPlayerCachePort(state),
            level.getGameTime(),
            new BenmingSwordBondService.BacklashContext(
                CultivationSnapshot.of(
                    TEST_RESOURCE_INITIAL,
                    TEST_RESOURCE_INITIAL,
                    TEST_RESOURCE_INITIAL,
                    0.0D,
                    0
                ),
                mutationRecorder,
                BenmingSwordBondService.toBacklashCooldownPort(cooldownAttachment),
                cooldownKey,
                BenmingSwordBondService.defaultLightBacklashCooldownTicks()
            )
        );

        double afterFirstZhenyuanLoss = mutationRecorder.zhenyuanLoss();
        double afterFirstNiantouLoss = mutationRecorder.niantouLoss();
        double afterFirstHunpoLoss = mutationRecorder.hunpoLoss();
        int afterFirstOperationCount = mutationRecorder.operationCount();

        BenmingSwordBondService.Result secondResult = BenmingSwordBondService.illegalDetach(
            player.getUUID().toString(),
            toSwordBondPort(secondSword),
            toPlayerCachePort(state),
            level.getGameTime(),
            new BenmingSwordBondService.BacklashContext(
                CultivationSnapshot.of(
                    TEST_RESOURCE_INITIAL,
                    TEST_RESOURCE_INITIAL,
                    TEST_RESOURCE_INITIAL,
                    0.0D,
                    0
                ),
                mutationRecorder,
                BenmingSwordBondService.toBacklashCooldownPort(cooldownAttachment),
                cooldownKey,
                BenmingSwordBondService.defaultLightBacklashCooldownTicks()
            )
        );

        helper.assertTrue(firstResult.success(), "cooldown：首次触发应成功");
        helper.assertTrue(secondResult.success(), "cooldown：二次触发应成功完成解绑");
        helper.assertTrue(
            firstResult.branch() == BenmingSwordBondService.ResultBranch.FORCED_UNBIND,
            "cooldown：首次触发分支应为 FORCED_UNBIND"
        );
        helper.assertTrue(
            secondResult.branch() == BenmingSwordBondService.ResultBranch.ILLEGAL_DETACH,
            "cooldown：二次触发分支应为 ILLEGAL_DETACH"
        );
        helper.assertTrue(
            firstResult.backlashEffect().type()
                == BenmingSwordBondService.BacklashType.FORCED_UNBIND_LIGHT,
            "cooldown：首次触发应命中轻度反噬"
        );
        helper.assertTrue(
            secondResult.backlashEffect().type() == BenmingSwordBondService.BacklashType.NONE,
            "cooldown：冷却期间二次触发不应再次施加反噬"
        );

        assertClose(
            helper,
            EXPECTED_LIGHT_BACKLASH_ZHENYUAN_LOSS,
            afterFirstZhenyuanLoss,
            "cooldown：首次触发应仅扣除一次轻度反噬真元"
        );
        assertClose(
            helper,
            EXPECTED_LIGHT_BACKLASH_NIANTOU_LOSS,
            afterFirstNiantouLoss,
            "cooldown：首次触发应仅扣除一次轻度反噬念头"
        );
        assertClose(
            helper,
            EXPECTED_LIGHT_BACKLASH_HUNPO_LOSS,
            afterFirstHunpoLoss,
            "cooldown：首次触发应仅扣除一次轻度反噬魂魄"
        );

        assertClose(
            helper,
            afterFirstZhenyuanLoss,
            mutationRecorder.zhenyuanLoss(),
            "cooldown：冷却期间不应再次扣除真元"
        );
        assertClose(
            helper,
            afterFirstNiantouLoss,
            mutationRecorder.niantouLoss(),
            "cooldown：冷却期间不应再次扣除念头"
        );
        assertClose(
            helper,
            afterFirstHunpoLoss,
            mutationRecorder.hunpoLoss(),
            "cooldown：冷却期间不应再次扣除魂魄"
        );
        helper.assertTrue(
            mutationRecorder.operationCount() == afterFirstOperationCount,
            "cooldown：冷却期间不应新增资源扣减操作"
        );

        helper.assertTrue(
            cooldownAttachment.get(cooldownKey)
                == BenmingSwordBondService.defaultLightBacklashCooldownTicks(),
            "cooldown：二次触发后冷却剩余应保持首次写入值"
        );

        helper.succeed();
    }

    private static BenmingSwordBondService.SwordBondPort toSwordBondPort(
        FlyingSwordEntity sword
    ) {
        return new BenmingSwordBondService.SwordBondPort() {
            @Override
            public String getStableSwordId() {
                return sword.getSwordAttributes().getStableSwordId();
            }

            @Override
            public String getBondOwnerUuid() {
                return sword.getSwordAttributes().getBond().getOwnerUuid();
            }

            @Override
            public double getBondResonance() {
                return sword.getSwordAttributes().getBond().getResonance();
            }

            @Override
            public void setBondOwnerUuid(String ownerUuid) {
                sword.getSwordAttributes().getBond().setOwnerUuid(ownerUuid);
                sword.syncAttributesToEntityData();
            }

            @Override
            public void setBondResonance(double resonance) {
                sword.getSwordAttributes().getBond().setResonance(resonance);
                sword.syncAttributesToEntityData();
            }
        };
    }

    private static BenmingSwordBondService.PlayerBondCachePort toPlayerCachePort(
        FlyingSwordStateAttachment state
    ) {
        return new BenmingSwordBondService.PlayerBondCachePort() {
            @Override
            public String getBondedSwordId() {
                return state.getBondedSwordId();
            }

            @Override
            public boolean isBondCacheDirty() {
                return state.isBondCacheDirty();
            }

            @Override
            public void updateBondCache(String stableSwordId, long resolvedTick) {
                state.updateBondCache(stableSwordId, resolvedTick);
            }

            @Override
            public void markBondCacheDirty() {
                state.markBondCacheDirty();
            }

            @Override
            public void clearBondCache() {
                state.clearBondCache();
            }
        };
    }

    private static final class MutationRecorder
        implements BenmingSwordResourceTransaction.ResourceMutationPort {

        private double zhenyuanLoss;
        private double niantouLoss;
        private double hunpoLoss;
        private int operationCount;

        @Override
        public void spendZhenyuan(double amount) {
            zhenyuanLoss += amount;
            operationCount++;
        }

        @Override
        public void spendNiantou(double amount) {
            niantouLoss += amount;
            operationCount++;
        }

        @Override
        public void spendHunpo(double amount) {
            hunpoLoss += amount;
            operationCount++;
        }

        private double zhenyuanLoss() {
            return zhenyuanLoss;
        }

        private double niantouLoss() {
            return niantouLoss;
        }

        private double hunpoLoss() {
            return hunpoLoss;
        }

        private int operationCount() {
            return operationCount;
        }
    }

    private static void assertClose(
        GameTestHelper helper,
        double expected,
        double actual,
        String message
    ) {
        helper.assertTrue(
            Math.abs(expected - actual) <= DOUBLE_EPSILON,
            message + "，expected=" + expected + "，actual=" + actual
        );
    }

    private static ServerPlayer createDeterministicPlayer(
        ServerLevel level,
        String suffix
    ) {
        UUID uuid = UUID.nameUUIDFromBytes(
            ("benming_bind_service_" + suffix).getBytes(StandardCharsets.UTF_8)
        );
        return FakePlayerFactory.get(
            level,
            new GameProfile(uuid, "benming_bind_service_" + suffix)
        );
    }

    private static void relocatePlayer(GameTestHelper helper, ServerPlayer player) {
        BlockPos playerPos = helper.absolutePos(
            new BlockPos(PLAYER_RELATIVE_X, PLAYER_RELATIVE_Y, PLAYER_RELATIVE_Z)
        );
        player.setPos(playerPos.getX(), playerPos.getY(), playerPos.getZ());
    }
}
