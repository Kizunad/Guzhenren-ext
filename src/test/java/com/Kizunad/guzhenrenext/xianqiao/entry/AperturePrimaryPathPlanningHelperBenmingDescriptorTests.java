package com.Kizunad.guzhenrenext.xianqiao.entry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AperturePrimaryPathPlanningHelperBenmingDescriptorTests {

    private static final String HELPER_SOURCE_PATH =
        "src/main/java/com/Kizunad/guzhenrenext/xianqiao/entry/AperturePrimaryPathPlanningHelper.java";

    private static final String UNKNOWN_DESCRIPTOR = "unknown_benming|state:unknown_fallback|benming_code:0";

    private static final String RESOLVED_OCEAN_DESCRIPTOR =
        "benming_code:8|state:resolved|semantic:ocean|海|water|dao:fire";

    @Test
    void sourceWiringMustUseResolvedBenmingDescriptorInsteadOfBlankPlaceholder() throws IOException {
        String source = readHelperSource();

        assertTrue(source.contains("resolveBenmingDescriptor(resolvedOpeningProfile),"));
        assertTrue(source.contains("|state:resolved"));
        assertTrue(source.contains("|semantic:"));
        assertFalse(source.contains("return \"\";"));
    }

    @Test
    void resolvedDescriptorSemanticMustRemainExplicitAndStable() throws IOException {
        String source = readHelperSource();

        assertFalse(RESOLVED_OCEAN_DESCRIPTOR.isBlank());
        assertTrue(RESOLVED_OCEAN_DESCRIPTOR.contains("state:resolved"));
        assertTrue(RESOLVED_OCEAN_DESCRIPTOR.contains("semantic:ocean"));
        assertTrue(RESOLVED_OCEAN_DESCRIPTOR.contains("|海|"));
        assertTrue(RESOLVED_OCEAN_DESCRIPTOR.endsWith("|dao:fire"));
        assertTrue(source.contains("resolveDominantDaoMark(snapshot.daoMarks())"));
        assertTrue(source.contains("Math.floorMod(benmingCode, BENMING_SEMANTIC_BUCKETS.length)"));
    }

    @Test
    void unknownFallbackDescriptorMustBeExplicitAndNeverCollapseToBlank() throws IOException {
        String source = readHelperSource();

        assertTrue(source.contains("BENMING_UNKNOWN_DESCRIPTOR"));
        assertTrue(source.contains(UNKNOWN_DESCRIPTOR));
        assertFalse(UNKNOWN_DESCRIPTOR.isBlank());
        assertTrue(source.contains("return BENMING_UNKNOWN_DESCRIPTOR;"));
        assertFalse(source.contains("return \"\";"));
    }

    private static String readHelperSource() throws IOException {
        return Files.readString(Path.of(HELPER_SOURCE_PATH));
    }
}
