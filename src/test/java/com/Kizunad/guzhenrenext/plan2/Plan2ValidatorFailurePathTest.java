package com.Kizunad.guzhenrenext.plan2;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class Plan2ValidatorFailurePathTest {

    @Test
    void shouldReportMissingCostRiskForDeepEntry() throws IOException {
        JsonArray array = cloneCurrentArray();
        JsonObject deep = findById(array, "C-D01");
        deep.addProperty("cost", "");

        List<String> errors = validateWithCurrentText(array);
        assertContains(errors, "missing cost/risk");
    }

    @Test
    void shouldReportInvalidSourceEnumCombination() throws IOException {
        JsonArray array = cloneCurrentArray();
        JsonObject plant = findById(array, "P-S01");
        plant.getAsJsonObject("mainSource").addProperty("type", "mine");

        List<String> errors = validateWithCurrentText(array);
        assertContains(errors, "invalid source enum combination");
    }

    @Test
    void shouldReportMissingCostRiskForShallowHeavyLinkage() throws IOException {
        JsonArray array = cloneCurrentArray();
        JsonObject shallow = findById(array, "P-S01");
        JsonArray heavyLinkPoints = new JsonArray();
        heavyLinkPoints.add("daomark");
        heavyLinkPoints.add("mutation");
        shallow.add("linkPoints", heavyLinkPoints);

        List<String> errors = validateWithCurrentText(array);
        assertContains(errors, "missing cost/risk");
    }

    @Test
    void shouldReportPlanJsonCoverageMismatch() throws IOException {
        JsonArray array = cloneCurrentArray();
        removeById(array, "M-D10");

        List<String> errors = validateWithCurrentText(array);
        assertContains(errors, "plan/json coverage mismatch");
    }

    @Test
    void shouldReportMissingSummaryOrPreconditions() throws IOException {
        JsonArray array = cloneCurrentArray();
        JsonObject entry = findById(array, "D-S01");
        entry.getAsJsonObject("risk").addProperty("summary", "");

        List<String> errors = validateWithCurrentText(array);
        assertContains(errors, "missing summary/preconditions");
    }

    @Test
    void shouldReportMissingSummaryOrPreconditionsWhenPreconditionsEmpty() throws IOException {
        JsonArray array = cloneCurrentArray();
        JsonObject entry = findById(array, "P-S01");
        entry.add("preconditions", new JsonArray());

        List<String> errors = validateWithCurrentText(array);
        assertContains(errors, "missing summary/preconditions");
    }

    @Test
    void shouldReportMechanicCostExtractionMismatch() throws IOException {
        JsonArray array = cloneCurrentArray();
        JsonObject entry = findById(array, "M-S01");
        entry.addProperty("mechanic", "不匹配的机制描述");

        List<String> errors = validateWithCurrentText(array);
        assertContains(errors, "mechanic/cost extraction mismatch");
    }

    @Test
    void shouldReportLinkPointsLexiconMismatch() throws IOException {
        JsonArray array = cloneCurrentArray();
        JsonObject entry = findById(array, "P-D01");
        JsonArray linkPoints = new JsonArray();
        linkPoints.add("farming");
        entry.add("linkPoints", linkPoints);

        List<String> errors = validateWithCurrentText(array);
        assertContains(errors, "linkPoints lexicon mismatch");
    }

    @Test
    void shouldReportEnumLexiconMismatch() throws IOException {
        JsonArray array = cloneCurrentArray();
        JsonObject entry = findById(array, "D-D01");
        entry.getAsJsonObject("risk").addProperty("type", "resource_drain");

        List<String> errors = validateWithCurrentText(array);
        assertContains(errors, "enum lexicon mismatch");
    }

    @Test
    void shouldReportJsonOrderingMismatch() throws IOException {
        JsonArray array = cloneCurrentArray();
        JsonObject first = array.get(0).getAsJsonObject().deepCopy();
        JsonObject second = array.get(1).getAsJsonObject().deepCopy();
        array.set(0, second);
        array.set(1, first);

        List<String> errors = validateWithCurrentText(array);
        assertContains(errors, "json ordering mismatch");
    }

    @Test
    void shouldReportInvalidSeverityAnchorTargetContract() throws IOException {
        JsonArray array = cloneCurrentArray();
        JsonObject entry = findById(array, "D-S01");
        entry.getAsJsonObject("risk").addProperty("severity", "critical");

        List<String> errors = validateWithCurrentText(array);
        assertContains(errors, "invalid severity/anchor-target contract");
    }

    private static JsonArray cloneCurrentArray() throws IOException {
        return JsonParser.parseString(Plan2ContentMatrixTestSupport.readMatrixText()).getAsJsonArray();
    }

    private static List<String> validateWithCurrentText(JsonArray array) throws IOException {
        return Plan2ContentMatrixValidator.validate(
            Plan2ContentMatrixTestSupport.readMatrixText(),
            array
        );
    }

    private static JsonObject findById(JsonArray array, String id) {
        for (JsonElement element : array) {
            JsonObject object = element.getAsJsonObject();
            if (id.equals(object.get("id").getAsString())) {
                return object;
            }
        }
        throw new IllegalStateException("找不到测试条目: " + id);
    }

    private static void removeById(JsonArray array, String id) {
        for (int i = 0; i < array.size(); i++) {
            JsonObject object = array.get(i).getAsJsonObject();
            if (id.equals(object.get("id").getAsString())) {
                array.remove(i);
                return;
            }
        }
    }

    private static void assertContains(List<String> errors, String expectedCategory) {
        assertTrue(
            errors.stream().anyMatch(message -> message.contains(expectedCategory)),
            "期望错误类别: " + expectedCategory + "，实际: " + errors
        );
    }
}
