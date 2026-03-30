package com.Kizunad.guzhenrenext.kongqiao.service;

import com.Kizunad.guzhenrenext.xianqiao.opening.AscensionConditionSnapshot;
import com.Kizunad.guzhenrenext.xianqiao.opening.ResolvedOpeningProfile;
import com.Kizunad.guzhenrenext.xianqiao.opening.AscensionThreeQiEvaluator;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 空窍容量桥接层测试。
 * <p>
 * 验证桥接层能够正确地将冻结画像转换为容量画像，
 * 包括资质档位映射、基础行数、修为追加行数和总行数计算。
 * </p>
 */
final class KongqiaoCapacityBridgeTests {

    private static final double DELTA = 1.0E-6D;

    /**
     * 测试：相同的冻结快照应解析为相同的资质档位和行数。
     */
    @Test
    void sameSnapshotProducesSameAptitudeTierAndRows() {
        Map<String, Double> raw = baseHealthyRaw();
        KongqiaoCapacityProfile profile1 = KongqiaoCapacityBridge.resolveFromRawVariables(raw);
        KongqiaoCapacityProfile profile2 = KongqiaoCapacityBridge.resolveFromRawVariables(raw);

        assertNotNull(profile1);
        assertNotNull(profile2);
        assertEquals(profile1.aptitudeTier(), profile2.aptitudeTier());
        assertEquals(profile1.baseRows(), profile2.baseRows());
        assertEquals(profile1.bonusRows(), profile2.bonusRows());
        assertEquals(profile1.totalRows(), profile2.totalRows());
    }

    /**
     * 测试：不同资质分数应映射到不同的资质档位和基础行数。
     */
    @Test
    void differentAptitudeScoresChangeBaseRowsIndependentlyOfBonusRows() {
        // 低资质：资源较低
        Map<String, Double> lowAptitude = new HashMap<>(baseHealthyRaw());
        lowAptitude.put("zuida_zhenyuan", 30.0D);
        lowAptitude.put("shouyuan", 30.0D);
        lowAptitude.put("jingli", 30.0D);
        lowAptitude.put("zuida_jingli", 30.0D);
        lowAptitude.put("hunpo", 30.0D);
        lowAptitude.put("zuida_hunpo", 30.0D);
        lowAptitude.put("tizhi", 30.0D);

        KongqiaoCapacityProfile lowProfile = KongqiaoCapacityBridge.resolveFromRawVariables(lowAptitude);

        // 高资质：资源较高
        Map<String, Double> highAptitude = new HashMap<>(baseHealthyRaw());
        highAptitude.put("zuida_zhenyuan", 120.0D);
        highAptitude.put("shouyuan", 120.0D);
        highAptitude.put("jingli", 100.0D);
        highAptitude.put("zuida_jingli", 100.0D);
        highAptitude.put("hunpo", 100.0D);
        highAptitude.put("zuida_hunpo", 100.0D);
        highAptitude.put("tizhi", 100.0D);

        KongqiaoCapacityProfile highProfile = KongqiaoCapacityBridge.resolveFromRawVariables(highAptitude);

        // 验证：基础行数应不同
        assertTrue(
            highProfile.baseRows() > lowProfile.baseRows(),
            "高资质的基础行数应大于低资质的基础行数"
        );

        // 验证： bonusRows 独立于 aptitudeTier 变化
        // 两者 maxZhenyuan 相同，bonusRows 应该相同
        assertEquals(highProfile.bonusRows(), lowProfile.bonusRows(), DELTA);
    }

    /**
     * 测试：不同的最大真元应改变追加行数，而不影响资质解析。
     */
    @Test
    void differentMaxZhenyuanChangesBonusRowsWithoutRewritingAptitudeResolution() {
        // 固定资质，变化真元
        Map<String, Double> rawBase = baseHealthyRaw();

        Map<String, Double> lowZhenyuan = new HashMap<>(rawBase);
        lowZhenyuan.put("zuida_zhenyuan", 250.0D); // 25% 进度

        Map<String, Double> highZhenyuan = new HashMap<>(rawBase);
        highZhenyuan.put("zuida_zhenyuan", 1000.0D); // 100% 进度

        KongqiaoCapacityProfile lowProfile = KongqiaoCapacityBridge.resolveFromRawVariables(lowZhenyuan);
        KongqiaoCapacityProfile highProfile = KongqiaoCapacityBridge.resolveFromRawVariables(highZhenyuan);

        // 验证：bonusRows 应不同
        assertTrue(
            highProfile.bonusRows() > lowProfile.bonusRows(),
            "高真元的追加行数应大于低真元的追加行数"
        );

        // 验证：aptitudeTier 应相同（因为其他资源不变）
        assertEquals(lowProfile.aptitudeTier(), highProfile.aptitudeTier());
        assertEquals(lowProfile.baseRows(), highProfile.baseRows());
    }

    /**
     * 测试：全零资源状态应映射为最低档位（残缺）。
     */
    @Test
    void allZeroResourcesMapToLowestTier() {
        Map<String, Double> allZero = new HashMap<>();
        allZero.put("zuida_zhenyuan", 0.0D);
        allZero.put("shouyuan", 0.0D);
        allZero.put("jingli", 0.0D);
        allZero.put("zuida_jingli", 0.0D);
        allZero.put("hunpo", 0.0D);
        allZero.put("zuida_hunpo", 0.0D);
        allZero.put("tizhi", 0.0D);

        KongqiaoCapacityProfile profile = KongqiaoCapacityBridge.resolveFromRawVariables(allZero);

        assertEquals(KongqiaoAptitudeTier.CANCI, profile.aptitudeTier());
        assertEquals(1, profile.baseRows());
    }

    /**
     * 测试：转数归一化。
     */
    @Test
    void apertureRankNormalization() {
        Map<String, Double> raw = baseHealthyRaw();
        raw.put("zhuanshu", 5.7D); // 5.7 转

        KongqiaoCapacityProfile profile = KongqiaoCapacityBridge.resolveFromRawVariables(raw);

        assertEquals(5, profile.apertureRank()); // floor(5.7) = 5
    }

    /**
     * 测试：阶段归一化。
     */
    @Test
    void apertureStageNormalization() {
        Map<String, Double> raw = baseHealthyRaw();
        raw.put("jieduan", 3.9D); // 3.9 阶段

        KongqiaoCapacityProfile profile = KongqiaoCapacityBridge.resolveFromRawVariables(raw);

        assertEquals(3, profile.apertureStage()); // floor(3.9) = 3
    }

    /**
     * 测试：总行数不超过最大行数限制。
     */
    @Test
    void totalRowsClampedByMaxRows() {
        Map<String, Double> raw = baseHealthyRaw();
        raw.put("zuida_zhenyuan", 10000.0D); // 超高真元

        KongqiaoCapacityProfile profile = KongqiaoCapacityBridge.resolveFromRawVariables(raw);

        // 绝品资质(5) + 满修为追加(4) = 9，不超过 MAX_ROWS = 9
        assertTrue(profile.totalRows() <= 9);
    }

    /**
     * 测试：空输入返回默认画像。
     */
    @Test
    void nullInputReturnsDefaultProfile() {
        KongqiaoCapacityProfile profile = KongqiaoCapacityBridge.resolveFromRawVariables(null);
        assertNotNull(profile);
        assertEquals(KongqiaoAptitudeTier.CANCI, profile.aptitudeTier());
    }

    /**
     * 测试：空 Map 返回默认画像。
     */
    @Test
    void emptyMapReturnsDefaultProfile() {
        KongqiaoCapacityProfile profile = KongqiaoCapacityBridge.resolveFromRawVariables(Map.of());
        assertNotNull(profile);
        assertEquals(KongqiaoAptitudeTier.CANCI, profile.aptitudeTier());
    }

    /**
     * 创建基础健康资源映射（用于测试）。
     */
    private static Map<String, Double> baseHealthyRaw() {
        Map<String, Double> raw = new HashMap<>();
        raw.put("benminggu", 12.0D);
        raw.put("zhuanshu", 5.0D);
        raw.put("jieduan", 5.0D);
        raw.put("kongqiao", 7.0D);
        raw.put("qiyun", 22.0D);
        raw.put("qiyun_shangxian", 60.0D);
        raw.put("renqi", 80.0D);
        raw.put("zuida_zhenyuan", 120.0D);
        raw.put("shouyuan", 80.0D);
        raw.put("jingli", 70.0D);
        raw.put("zuida_jingli", 100.0D);
        raw.put("hunpo", 75.0D);
        raw.put("zuida_hunpo", 100.0D);
        raw.put("tizhi", 90.0D);
        raw.put("humanQiTarget", 100.0D);
        raw.put("earthQiTarget", 100.0D);
        return raw;
    }
}
