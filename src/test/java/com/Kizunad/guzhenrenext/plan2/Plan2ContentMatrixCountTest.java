package com.Kizunad.guzhenrenext.plan2;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class Plan2ContentMatrixCountTest {

    @Test
    void shouldMatchTotalAndCategoryDepthQuota() throws IOException {
        JsonArray array = Plan2ContentMatrixTestSupport.readMatrixArray();
        assertEquals(120, array.size());

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

        assertEquals(30, categoryCount.get("creature"));
        assertEquals(30, categoryCount.get("plant"));
        assertEquals(30, categoryCount.get("pill"));
        assertEquals(30, categoryCount.get("material"));

        assertEquals(20, categoryShallow.get("creature"));
        assertEquals(20, categoryShallow.get("plant"));
        assertEquals(20, categoryShallow.get("pill"));
        assertEquals(20, categoryShallow.get("material"));

        assertEquals(10, categoryDeep.get("creature"));
        assertEquals(10, categoryDeep.get("plant"));
        assertEquals(10, categoryDeep.get("pill"));
        assertEquals(10, categoryDeep.get("material"));
    }
}
