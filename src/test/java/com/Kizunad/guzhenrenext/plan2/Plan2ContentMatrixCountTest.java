package com.Kizunad.guzhenrenext.plan2;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Plan2ContentMatrixCountTest {

    private static final int EXPECTED_TOTAL_COUNT = 120;
    private static final int EXPECTED_CATEGORY_COUNT = 30;
    private static final int EXPECTED_SHALLOW_COUNT = 20;
    private static final int EXPECTED_DEEP_COUNT = 10;

    @Test
    void shouldMatchTotalAndCategoryDepthQuota() throws IOException {
        JsonArray array = Plan2ContentMatrixTestSupport.readMatrixArray();
        assertQuotaMatchesExpected(array);
    }

    @Test
    void shouldRejectPlantShallowQuotaDriftWhenShallowEntryFlipsToDeep() throws IOException {
        JsonArray array = Plan2ContentMatrixTestSupport.readMatrixArray();
        JsonArray mutated = array.deepCopy();
        JsonObject brokenEntry = findById(mutated, "P-S01");
        brokenEntry.addProperty("depth", "deep");

        AssertionError error = assertThrows(
            AssertionError.class,
            () -> assertQuotaMatchesExpected(mutated)
        );
        assertTrue(
            error.getMessage() != null && error.getMessage().contains("plant shallow quota"),
            "配额漂移失败必须明确指出损坏桶 plant shallow，实际: " + error.getMessage()
        );
    }

    private static void assertQuotaMatchesExpected(JsonArray array) {
        assertEquals(EXPECTED_TOTAL_COUNT, array.size(), "matrix total quota");

        Map<String, Integer> categoryCount = new HashMap<>();
        Map<String, Integer> categoryShallow = new HashMap<>();
        Map<String, Integer> categoryDeep = new HashMap<>();

        for (JsonElement element : array) {
            String category = element.getAsJsonObject().get("category").getAsString();
            String depth = element.getAsJsonObject().get("depth").getAsString();
            categoryCount.merge(category, 1, Integer::sum);
            if ("shallow".equals(depth)) {
                categoryShallow.merge(category, 1, Integer::sum);
            } else if ("deep".equals(depth)) {
                categoryDeep.merge(category, 1, Integer::sum);
            }
        }

        assertEquals(EXPECTED_CATEGORY_COUNT, categoryCount.get("creature"), "creature total quota");
        assertEquals(EXPECTED_CATEGORY_COUNT, categoryCount.get("plant"), "plant total quota");
        assertEquals(EXPECTED_CATEGORY_COUNT, categoryCount.get("pill"), "pill total quota");
        assertEquals(EXPECTED_CATEGORY_COUNT, categoryCount.get("material"), "material total quota");

        assertEquals(EXPECTED_SHALLOW_COUNT, categoryShallow.get("creature"), "creature shallow quota");
        assertEquals(EXPECTED_SHALLOW_COUNT, categoryShallow.get("plant"), "plant shallow quota");
        assertEquals(EXPECTED_SHALLOW_COUNT, categoryShallow.get("pill"), "pill shallow quota");
        assertEquals(EXPECTED_SHALLOW_COUNT, categoryShallow.get("material"), "material shallow quota");

        assertEquals(EXPECTED_DEEP_COUNT, categoryDeep.get("creature"), "creature deep quota");
        assertEquals(EXPECTED_DEEP_COUNT, categoryDeep.get("plant"), "plant deep quota");
        assertEquals(EXPECTED_DEEP_COUNT, categoryDeep.get("pill"), "pill deep quota");
        assertEquals(EXPECTED_DEEP_COUNT, categoryDeep.get("material"), "material deep quota");
    }

    private static JsonObject findById(JsonArray array, String id) {
        for (JsonElement element : array) {
            JsonObject entry = element.getAsJsonObject();
            if (id.equals(entry.get("id").getAsString())) {
                return entry;
            }
        }
        throw new IllegalStateException("找不到测试条目: " + id);
    }
}
