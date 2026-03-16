package com.Kizunad.guzhenrenext_test.xianqiao;

import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoMarkApi;
import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoType;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import com.Kizunad.guzhenrenext.xianqiao.tribulation.TribulationManager;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder("guzhenrenext")
public class Task16LoopCGameTests {

    private static final String TASK16_LOOP_C_BATCH = "task16_loop_c";
    private static final int TEST_TIMEOUT_TICKS = 220;
    private static final int LOW_RISK_DAMAGE = 20;
    private static final int HIGH_RISK_DAMAGE = 90;
    private static final int EXPECTED_BOUNDARY_REWARD_DELTA = 1;
    private static final int EXPECTED_SUCCESS_AURA_REWARD = 160;
    private static final int EXPECTED_FAILURE_AURA_PENALTY = 120;
    private static final int PRELOAD_AURA_FOR_PENALTY = 240;
    private static final int HAPPY_CENTER_X = 2;
    private static final int FAILURE_CENTER_X = 8;
    private static final int CENTER_Y = 2;
    private static final int CENTER_Z = 2;

    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS, batch = TASK16_LOOP_C_BATCH)
    public void testTask16LoopCHappyPathShouldGrantEconomyCompensationOnLowRisk(GameTestHelper helper) {
        UUID owner = deterministicOwner("task16-happy-owner");
        BlockPos center = helper.absolutePos(new BlockPos(HAPPY_CENTER_X, CENTER_Y, CENTER_Z));
        ApertureWorldData worldData = ApertureWorldData.get(helper.getLevel());

        worldData.allocateAperture(owner);
        worldData.updateCenter(owner, center);

        ApertureInfo beforeInfo = requireApertureInfo(helper, worldData, owner, "happy path: 仙窍信息缺失");
        int auraBefore = DaoMarkApi.getAura(helper.getLevel(), center, DaoType.TIME);

        TribulationManager manager = createSettlementStageManager(owner, LOW_RISK_DAMAGE);
        manager.tick(helper.getLevel(), beforeInfo);

        ApertureInfo afterInfo = requireApertureInfo(helper, worldData, owner, "happy path: 结算后仙窍信息缺失");
        int auraAfter = DaoMarkApi.getAura(helper.getLevel(), center, DaoType.TIME);

        // Given 低风险结算输入，When 执行灾劫结算，Then 应命中经济补偿分支。
        helper.assertTrue(
            afterInfo.minChunkX() == beforeInfo.minChunkX() - EXPECTED_BOUNDARY_REWARD_DELTA
                && afterInfo.maxChunkX() == beforeInfo.maxChunkX() + EXPECTED_BOUNDARY_REWARD_DELTA
                && afterInfo.minChunkZ() == beforeInfo.minChunkZ() - EXPECTED_BOUNDARY_REWARD_DELTA
                && afterInfo.maxChunkZ() == beforeInfo.maxChunkZ() + EXPECTED_BOUNDARY_REWARD_DELTA,
            "happy path: 低风险结算应触发经济补偿（仙窍边界每方向扩展 1 chunk）"
        );
        helper.assertTrue(
            auraAfter - auraBefore == EXPECTED_SUCCESS_AURA_REWARD,
            "happy path: 低风险结算应奖励固定时道灵气"
        );
        helper.assertTrue(manager.isFinished(), "happy path: 结算后管理器必须标记为完成");
        helper.assertTrue(
            afterInfo.nextTribulationTick() > helper.getLevel().getGameTime(),
            "happy path: 必须刷新下一次灾劫触发时间"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS, batch = TASK16_LOOP_C_BATCH)
    public void testTask16LoopCFailurePathShouldApplyEconomyPenaltyOnHighRisk(GameTestHelper helper) {
        UUID owner = deterministicOwner("task16-failure-owner");
        BlockPos center = helper.absolutePos(new BlockPos(FAILURE_CENTER_X, CENTER_Y, CENTER_Z));
        ApertureWorldData worldData = ApertureWorldData.get(helper.getLevel());

        worldData.allocateAperture(owner);
        worldData.updateCenter(owner, center);
        DaoMarkApi.addAura(helper.getLevel(), center, DaoType.TIME, PRELOAD_AURA_FOR_PENALTY);

        ApertureInfo beforeInfo = requireApertureInfo(helper, worldData, owner, "failure path: 仙窍信息缺失");
        int auraBefore = DaoMarkApi.getAura(helper.getLevel(), center, DaoType.TIME);

        TribulationManager manager = createSettlementStageManager(owner, HIGH_RISK_DAMAGE);
        manager.tick(helper.getLevel(), beforeInfo);

        ApertureInfo afterInfo = requireApertureInfo(helper, worldData, owner, "failure path: 结算后仙窍信息缺失");
        int auraAfter = DaoMarkApi.getAura(helper.getLevel(), center, DaoType.TIME);

        // Given 高风险结算输入，When 执行灾劫结算，Then 应命中经济惩罚分支。
        helper.assertTrue(
            afterInfo.minChunkX() == beforeInfo.minChunkX()
                && afterInfo.maxChunkX() == beforeInfo.maxChunkX()
                && afterInfo.minChunkZ() == beforeInfo.minChunkZ()
                && afterInfo.maxChunkZ() == beforeInfo.maxChunkZ(),
            "failure path: 高风险结算不应触发边界扩展（经济补偿被取消）"
        );
        helper.assertTrue(
            auraBefore - auraAfter == EXPECTED_FAILURE_AURA_PENALTY,
            "failure path: 高风险结算应扣减固定时道灵气（经济惩罚）"
        );
        helper.assertTrue(manager.isFinished(), "failure path: 结算后管理器必须标记为完成");
        helper.assertTrue(
            afterInfo.nextTribulationTick() > helper.getLevel().getGameTime(),
            "failure path: 必须刷新下一次灾劫触发时间"
        );
        helper.succeed();
    }

    private static TribulationManager createSettlementStageManager(UUID owner, int damageAccumulated) {
        TribulationManager manager = new TribulationManager(owner);
        manager.startTribulation();
        manager.advanceState();
        manager.advanceState();
        manager.advanceState();
        setPrivateFloatField(manager, "damageAccumulated", damageAccumulated);
        return manager;
    }

    private static UUID deterministicOwner(String token) {
        return UUID.nameUUIDFromBytes(token.getBytes(StandardCharsets.UTF_8));
    }

    private static ApertureInfo requireApertureInfo(
        GameTestHelper helper,
        ApertureWorldData worldData,
        UUID owner,
        String errorMessage
    ) {
        ApertureInfo info = worldData.getAperture(owner);
        helper.assertTrue(info != null, errorMessage);
        return info;
    }

    private static void setPrivateFloatField(Object target, String fieldName, float value) {
        Field field = readPrivateField(target, fieldName);
        try {
            field.setFloat(target, value);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("写入私有浮点字段失败: " + fieldName, exception);
        }
    }

    private static Field readPrivateField(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException exception) {
            throw new IllegalStateException("读取私有字段失败: " + fieldName, exception);
        }
    }
}
