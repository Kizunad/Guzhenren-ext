package com.Kizunad.guzhenrenext.xianqiao.entry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ApertureInitializationRuntimeExecutorWiringTests {

    @Test
    void plannedCellMaterializationMustInjectBoundedBiomeResolverIntoSampler() throws IOException {
        String source = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/xianqiao/entry/ApertureInitializationRuntimeExecutor.java")
        );

        assertTrue(
            source.contains("BIOME_SEARCH_SERVICE.createBoundedResolver("),
            "planned-cell 物化主链必须显式构建 bounded biome resolver，禁止继续直接走 legacy locate 路径"
        );
        assertTrue(
            source.contains("new OverworldTerrainSampler.SourceSearchRequest("),
            "runtime 物化路径必须构造 SourceSearchRequest 并把 bounded resolver 注入 terrain sampler"
        );
        assertTrue(
            source.contains("searchContext.searchOrigin(),")
                && source.contains("searchContext.random(),")
                && source.contains("boundedResolver"),
            "runtime 物化路径必须把 bounded resolver 注入 SourceSearchRequest，再交给 terrain sampler"
        );
        assertTrue(
            source.contains("OverworldTerrainSampler.sampleAndPlace("),
            "runtime 物化仍应经由 OverworldTerrainSampler source-resolver+materializer 接缝"
        );
        assertFalse(
            source.contains("null,\n                searchOrigin,\n                random"),
            "planned-cell 物化路径不应继续使用旧签名 sampleAndPlace(..., searchOrigin, random)"
        );
    }

    @Test
    void finalizeWorldDataMustWriteMappedChunkBoundaryUsingCoreAnchorSeamCenterBasis() throws IOException {
        String source = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/xianqiao/entry/ApertureInitializationRuntimeExecutor.java")
        );

        assertTrue(
            source.contains("WorldChunkBoundary worldChunkBoundary = resolveWorldChunkBoundary(plan, resolvedCenter);")
                && source.contains("worldData.updateChunkBoundary("),
            "finalize 阶段必须把 plan 边界写回 worldData 的 min/maxChunk 真源"
        );
        assertTrue(
            source.contains("InitialTerrainPlan.InitialChunkBoundary localBoundary = plan.initialChunkBoundary();"),
            "边界写回必须消费 planner 输出的 initialChunkBoundary，而不是继续沿用 allocate 默认大边界"
        );
        assertTrue(
            source.contains("InitialTerrainPlan.CoreAnchor coreAnchor = plan.coreAnchor();")
                && source.contains("coreAnchor.seamCenterChunkX()")
                && source.contains("coreAnchor.seamCenterChunkZ()"),
            "边界映射必须使用 coreAnchor seam-center 基准，避免偶数布局退化成 teleportAnchor 单格中心"
        );
        assertTrue(
            source.contains("Math.round((localChunk - seamCenterChunk) * CHUNK_SIZE_BLOCKS)")
                && source.contains("+ PLANNED_CELL_SIDE_LENGTH - 1"),
            "边界换算必须与 planned-cell 物化同尺度（16 格整块覆盖），确保偶数布局 0.5 chunk 偏移正确落在 8 格"
        );
        assertTrue(
            source.contains("SectionPos.blockToSectionCoord(minBlockX)")
                && source.contains("SectionPos.blockToSectionCoord(maxBlockX)")
                && source.contains("SectionPos.blockToSectionCoord(minBlockZ)")
                && source.contains("SectionPos.blockToSectionCoord(maxBlockZ)"),
            "世界方块覆盖范围必须最终回折到 chunk 闭区间，供 runtime/tribulation/fragment 统一消费"
        );
    }
}
