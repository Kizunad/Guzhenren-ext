package com.Kizunad.guzhenrenext.plan2;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

final class Plan2RuleSetTest {

    private static final Pattern ID_PATTERN = Pattern.compile("^[CPDM]-(S|D)\\d{2}$");

    @Test
    void namingRuleHappyPathAllIdsAreValidAndConsistent() throws IOException {
        final JsonArray matrix = Plan2ContentMatrixTestSupport.readMatrixArray();
        validateNamingRules(matrix);
    }

    @Test
    void namingRuleFailurePathReportsIllegalId() throws IOException {
        final JsonArray matrix = Plan2ContentMatrixTestSupport.readMatrixArray();
        final JsonArray mutated = matrix.deepCopy();
        final JsonObject first = mutated.get(0).getAsJsonObject();
        final String illegalId = "X-S01";
        first.addProperty("id", illegalId);

        final IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> validateNamingRules(mutated)
        );
        assertContainsIllegalId(exception, illegalId);
    }

    @Test
    void namingRuleFailurePathReportsIdCategoryMismatch() throws IOException {
        final JsonArray matrix = Plan2ContentMatrixTestSupport.readMatrixArray();
        final JsonArray mutated = matrix.deepCopy();
        final String brokenId = "P-S01";
        final JsonObject entry = findById(mutated, brokenId);
        entry.addProperty("category", "creature");

        final IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> validateNamingRules(mutated)
        );
        assertMessageContains(exception, "id/category mismatch");
        assertMessageContains(exception, brokenId);
    }

    @Test
    void namingRuleFailurePathReportsIdDepthMismatch() throws IOException {
        final JsonArray matrix = Plan2ContentMatrixTestSupport.readMatrixArray();
        final JsonArray mutated = matrix.deepCopy();
        final String brokenId = "P-S01";
        final JsonObject entry = findById(mutated, brokenId);
        entry.addProperty("depth", "deep");

        final IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> validateNamingRules(mutated)
        );
        assertMessageContains(exception, "id/depth mismatch");
        assertMessageContains(exception, brokenId);
    }

    @Test
    void namingRuleFailurePathReportsDuplicateId() throws IOException {
        final JsonArray matrix = Plan2ContentMatrixTestSupport.readMatrixArray();
        final JsonArray mutated = matrix.deepCopy();
        final String duplicatedId = "P-S01";
        final JsonObject duplicatedEntry = findById(mutated, "P-S02");
        duplicatedEntry.addProperty("id", duplicatedId);

        final IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> validateNamingRules(mutated)
        );
        assertMessageContains(exception, "duplicate id");
        assertMessageContains(exception, duplicatedId);
    }

    private static void validateNamingRules(JsonArray matrix) {
        final Set<String> seen = new HashSet<>();
        for (JsonElement element : matrix) {
            final JsonObject entry = element.getAsJsonObject();
            final String id = idOf(entry);
            if (!ID_PATTERN.matcher(id).matches()) {
                throw new IllegalStateException("invalid id format: " + id);
            }
            if (!seen.add(id)) {
                throw new IllegalStateException("duplicate id: " + id);
            }
            final String category = entry.get("category").getAsString();
            final String depth = entry.get("depth").getAsString();
            validateIdPrefixAndDepth(id, category, depth);
        }
    }

    private static void validateIdPrefixAndDepth(String id, String category, String depth) {
        final char categoryPrefix = id.charAt(0);
        final char depthPrefix = id.charAt(2);
        final char expectedCategoryPrefix;
        switch (category) {
            case "creature":
                expectedCategoryPrefix = 'C';
                break;
            case "plant":
                expectedCategoryPrefix = 'P';
                break;
            case "pill":
                expectedCategoryPrefix = 'D';
                break;
            case "material":
                expectedCategoryPrefix = 'M';
                break;
            default:
                throw new IllegalStateException("invalid category for id " + id + ": " + category);
        }
        if (categoryPrefix != expectedCategoryPrefix) {
            throw new IllegalStateException("id/category mismatch for id " + id + ": " + category);
        }

        if ("shallow".equals(depth) && depthPrefix != 'S') {
            throw new IllegalStateException("id/depth mismatch for id " + id + ": " + depth);
        }
        if ("deep".equals(depth) && depthPrefix != 'D') {
            throw new IllegalStateException("id/depth mismatch for id " + id + ": " + depth);
        }
        if (!"shallow".equals(depth) && !"deep".equals(depth)) {
            throw new IllegalStateException("invalid depth for id " + id + ": " + depth);
        }
    }

    private static String idOf(JsonObject entry) {
        return entry.get("id").getAsString();
    }

    private static JsonObject findById(JsonArray matrix, String id) {
        for (JsonElement element : matrix) {
            final JsonObject entry = element.getAsJsonObject();
            if (id.equals(idOf(entry))) {
                return entry;
            }
        }
        throw new IllegalStateException("找不到测试条目: " + id);
    }

    private static void assertContainsIllegalId(IllegalStateException exception, String illegalId) {
        final String message = exception.getMessage();
        if (message == null || !message.contains(illegalId)) {
            throw new IllegalStateException("error message must contain illegal id: " + illegalId);
        }
    }

    private static void assertMessageContains(IllegalStateException exception, String expectedFragment) {
        final String message = exception.getMessage();
        if (message == null || !message.contains(expectedFragment)) {
            throw new IllegalStateException("error message must contain fragment: " + expectedFragment);
        }
    }
}
