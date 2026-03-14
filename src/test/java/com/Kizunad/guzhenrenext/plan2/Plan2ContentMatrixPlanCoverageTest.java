package com.Kizunad.guzhenrenext.plan2;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Plan2ContentMatrixPlanCoverageTest {

    @Test
    void shouldCoverAllPlanIdsAndStructuredRows() throws IOException {
        Set<String> planIds = Plan2ContentMatrixTestSupport.extractPlanIds();
        Map<String, Boolean> structuredFlags = Plan2ContentMatrixTestSupport.extractPlanStructuredFlags();

        JsonArray array = Plan2ContentMatrixTestSupport.readMatrixArray();
        Set<String> jsonIds = new HashSet<>();
        for (JsonElement element : array) {
            jsonIds.add(element.getAsJsonObject().get("id").getAsString());
        }

        assertEquals(120, planIds.size());
        assertEquals(120, jsonIds.size());
        assertEquals(planIds, jsonIds);

        for (String id : planIds) {
            assertTrue(structuredFlags.containsKey(id));
            assertTrue(Boolean.TRUE.equals(structuredFlags.get(id)));
        }
    }
}
