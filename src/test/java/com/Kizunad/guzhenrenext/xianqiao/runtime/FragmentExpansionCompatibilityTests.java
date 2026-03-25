package com.Kizunad.guzhenrenext.xianqiao.runtime;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FragmentExpansionCompatibilityTests {

    private static final Path FRAGMENT_EXPANSION_POLICY_SOURCE = Path.of(
        "src/main/java/com/Kizunad/guzhenrenext/xianqiao/runtime/FragmentExpansionPolicy.java"
    );

    private static final Path FRAGMENT_PLACEMENT_SOURCE = Path.of(
        "src/main/java/com/Kizunad/guzhenrenext/xianqiao/service/FragmentPlacementService.java"
    );

    private static final Path TRIBULATION_MANAGER_SOURCE = Path.of(
        "src/main/java/com/Kizunad/guzhenrenext/xianqiao/tribulation/TribulationManager.java"
    );

    private static final Path HEAVENLY_FRAGMENT_ITEM_SOURCE = Path.of(
        "src/main/java/com/Kizunad/guzhenrenext/xianqiao/item/HeavenlyFragmentItem.java"
    );

    @Test
    void shouldKeepV1AsSymmetricRectangularExpansionInPolicySource() throws IOException {
        String policySource = readUtf8(FRAGMENT_EXPANSION_POLICY_SOURCE);

        assertTrue(
            policySource.contains("V1_SYMMETRIC_RECTANGULAR_CHUNK_DELTA = 1"),
            "v1 扩张常量必须固定为 1，避免兼容层悄悄改变每次矩形外推尺度"
        );
        assertTrue(
            policySource.contains("minChunkX - normalizedChunkDelta"),
            "矩形边界西侧必须按统一增量向外扩张，不能退化为单向朝向偏移"
        );
        assertTrue(
            policySource.contains("maxChunkX + normalizedChunkDelta"),
            "矩形边界东侧必须按统一增量向外扩张，不能只扩一边"
        );
        assertTrue(
            policySource.contains("minChunkZ - normalizedChunkDelta"),
            "矩形边界北侧必须按统一增量向外扩张，保持 v1 对称兼容"
        );
        assertTrue(
            policySource.contains("maxChunkZ + normalizedChunkDelta"),
            "矩形边界南侧必须按统一增量向外扩张，保持四向一致"
        );
        assertTrue(
            policySource.contains("expandSymmetricBounds("),
            "共享策略必须保留独立的对称矩形边界推导接缝，供放置与奖励共同复用"
        );
    }

    @Test
    void shouldKeepDirectionLimitedToPreviewPlacementTargetSeam() throws IOException {
        String policySource = readUtf8(FRAGMENT_EXPANSION_POLICY_SOURCE);

        assertTrue(
            policySource.contains("resolvePlacementTarget(")
                && policySource.contains("expandSymmetricBounds(")
                && policySource.contains("V1_SYMMETRIC_RECTANGULAR_CHUNK_DELTA"),
            "放置点推导必须先建立对称扩张后的矩形边界，再根据朝向选择外缘参考点"
        );
        assertTrue(
            policySource.contains("if (stepX > 0)")
                && policySource.contains("if (stepX < 0)")
                && policySource.contains("if (stepZ > 0)"),
            "朝向只应参与外缘投放点选择，不能直接决定扩边公式"
        );
        assertTrue(
            policySource.contains("SectionPos.sectionToBlockCoord(expandedBounds.maxChunkX())")
                && policySource.contains("SectionPos.sectionToBlockCoord(expandedBounds.minChunkX())")
                && policySource.contains("SectionPos.sectionToBlockCoord(expandedBounds.maxChunkZ())")
                && policySource.contains("SectionPos.sectionToBlockCoord(expandedBounds.minChunkZ())"),
            "四个方向都必须从同一扩张后矩形边界读取外缘投放坐标"
        );
    }

    @Test
    void fragmentPlacementAndTribulationRewardShouldUseSameSharedPolicy() throws IOException {
        String fragmentPlacementSource = readUtf8(FRAGMENT_PLACEMENT_SOURCE);
        String tribulationManagerSource = readUtf8(TRIBULATION_MANAGER_SOURCE);

        assertTrue(
            fragmentPlacementSource.contains("return FragmentExpansionPolicy.resolvePlacementTarget(info, direction);"),
            "碎片放置服务必须把预览/目标推导委托给共享策略，避免保留旧的本地几何公式"
        );
        assertTrue(
            fragmentPlacementSource.contains("FragmentExpansionPolicy.applySymmetricExpansion(worldData, player.getUUID());"),
            "碎片实际扩边必须走共享策略，不能继续直接写边界增量"
        );
        assertTrue(
            tribulationManagerSource.contains("FragmentExpansionPolicy.applySymmetricExpansion(worldData, owner);"),
            "天劫成功奖励必须复用与碎片相同的共享策略"
        );
        assertFalse(
            fragmentPlacementSource.contains("placementDistance"),
            "旧的局部距离推导变量不应继续留在碎片服务中，避免出现第二套方向扩张心智模型"
        );
        assertFalse(
            tribulationManagerSource.contains("worldData.expandBoundaryByChunkDelta(owner"),
            "天劫奖励路径不应保留旧的直接扩边写法，必须统一走共享策略"
        );
    }

    @Test
    void previewAndTextSemanticsShouldMatchSymmetricExpansionPolicy() throws IOException {
        String fragmentPlacementSource = readUtf8(FRAGMENT_PLACEMENT_SOURCE);
        String heavenlyFragmentItemSource = readUtf8(HEAVENLY_FRAGMENT_ITEM_SOURCE);

        assertTrue(
            heavenlyFragmentItemSource.contains("外缘投放点 ("),
            "Shift 预览必须说明它展示的是朝向外缘投放点，而不是单向扩边终点"
        );
        assertTrue(
            heavenlyFragmentItemSource.contains("实际边界仍向四周各 +1 区块"),
            "Shift 预览必须把真实结果明确为四向对称扩张"
        );
        assertTrue(
            heavenlyFragmentItemSource.contains("边界四向对称扩张"),
            "物品说明必须直述 v1 仍是对称矩形扩张语义"
        );
        assertFalse(
            heavenlyFragmentItemSource.contains("右键: 沿朝向放置"),
            "旧提示会把玩家误导成单向扩边，因此必须移除"
        );
        assertTrue(
            fragmentPlacementSource.contains("已在朝向外缘投放；仙窍边界向四周各扩张 1 个区块"),
            "成功提示必须同时区分投放点朝向语义与真实边界四向扩张语义"
        );
    }

    private static String readUtf8(Path sourcePath) throws IOException {
        return Files.readString(sourcePath, StandardCharsets.UTF_8);
    }
}
