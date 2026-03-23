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

    @Test
    void shouldReportCreatureCrosslinkMismatchWhenCreatureItemTargetMissing() throws IOException {
        JsonArray array = cloneCurrentArray();
        JsonObject creature = findById(array, "C-S05");
        JsonArray targets = creature.getAsJsonObject("primaryUse").getAsJsonArray("targets");
        targets.set(0, JsonParser.parseString("\"item:D-S99\""));

        List<String> errors = validateWithCurrentText(array);
        assertContains(errors, "creature crosslink mismatch");
        assertTrue(
            errors.stream().anyMatch(message -> message.contains("item:D-S99")),
            "断边失败应明确指出损坏的 creature item 引用，实际: " + errors
        );
    }

    @Test
    void shouldReportPlantCrosslinkMismatchWhenPlantItemTargetMissing() throws IOException {
        JsonArray array = cloneCurrentArray();
        JsonObject plant = findById(array, "P-S01");
        JsonArray targets = plant.getAsJsonObject("primaryUse").getAsJsonArray("targets");
        targets.set(0, JsonParser.parseString("\"item:D-S99\""));

        List<String> errors = validateWithCurrentText(array);
        assertContains(errors, "plant crosslink mismatch");
        assertTrue(
            errors.stream().anyMatch(message -> message.contains("item:D-S99")),
            "断边失败应明确指出损坏的 plant item 引用，实际: " + errors
        );
    }

    @Test
    void shouldReportPillCrosslinkMismatchWhenPillItemTargetMissing() throws IOException {
        JsonArray array = cloneCurrentArray();
        JsonObject pill = findById(array, "D-S02");
        JsonArray targets = pill.getAsJsonObject("primaryUse").getAsJsonArray("targets");
        targets.set(0, JsonParser.parseString("\"item:D-S99\""));

        List<String> errors = validateWithCurrentText(array);
        assertContains(errors, "pill crosslink mismatch");
        assertTrue(
            errors.stream().anyMatch(message -> message.contains("item:D-S99")),
            "断边失败应明确指出损坏的 pill item 引用，实际: " + errors
        );
    }

    @Test
    void shouldReportMaterialCrosslinkMismatchWhenMaterialItemTargetMissing() throws IOException {
        JsonArray array = cloneCurrentArray();
        JsonObject material = findById(array, "M-S01");
        JsonArray targets = material.getAsJsonObject("primaryUse").getAsJsonArray("targets");
        targets.set(0, JsonParser.parseString("\"item:M-S99\""));

        List<String> errors = validateWithCurrentText(array);
        assertContains(errors, "material crosslink mismatch");
        assertTrue(
            errors.stream().anyMatch(message -> message.contains("item:M-S99")),
            "断边失败应明确指出损坏的 material item 引用，实际: " + errors
        );
    }

    @Test
    void shouldReportMaterialCrosslinkMismatchWhenShallowEntryOutputsDeepMaterial() throws IOException {
        JsonArray array = cloneCurrentArray();
        JsonObject shallowCreature = findById(array, "C-S01");
        JsonArray targets = shallowCreature.getAsJsonObject("primaryUse").getAsJsonArray("targets");
        targets.set(0, JsonParser.parseString("\"item:M-D10\""));

        List<String> errors = validateWithCurrentText(array);
        assertContains(errors, "material crosslink mismatch");
        assertTrue(
            errors.stream().anyMatch(message -> message.contains("shallow 条目禁止产出 deep material")),
            "越层产出失败应明确指出 shallow->deep material 违规，实际: " + errors
        );
        assertTrue(
            errors.stream().anyMatch(message -> message.contains("item:M-D10")),
            "越层产出失败应明确指出损坏边 ID，实际: " + errors
        );
    }

    @Test
    void shouldReportGlobalBudgetMismatchWhenEndgameCoreDowngradedToShallowMedium() throws IOException {
        JsonArray array = cloneCurrentArray();
        JsonObject endgameCoreEntry = findById(array, "D-D04");
        endgameCoreEntry.addProperty("depth", "shallow");
        endgameCoreEntry.getAsJsonObject("risk").addProperty("severity", "medium");

        List<String> errors = validateWithCurrentText(array);
        assertContains(errors, "global budget mismatch");
        assertTrue(
            errors.stream().anyMatch(message -> message.contains("D-D04") && message.contains("endgame_core")),
            "endgame_core 预算失败应包含条目 ID 与规则语义，实际: " + errors
        );
        assertTrue(
            errors.stream().anyMatch(message -> message.contains("actualSeverity=medium")),
            "endgame_core 预算失败应明确指出过低风险等级，实际: " + errors
        );
    }

    @Test
    void shouldReportGlobalBudgetMismatchWhenDeepHeavyLinkUsesMediumSeverity() throws IOException {
        JsonArray array = cloneCurrentArray();
        JsonObject deepHeavyEntry = findById(array, "D-D03");
        deepHeavyEntry.getAsJsonObject("risk").addProperty("severity", "medium");

        List<String> errors = validateWithCurrentText(array);
        assertContains(errors, "global budget mismatch");
        assertTrue(
            errors.stream().anyMatch(message -> message.contains("D-D03") && message.contains("heavyLinks")),
            "deep 重影响预算失败应包含条目 ID 与 heavyLinks，实际: " + errors
        );
        assertTrue(
            errors.stream().anyMatch(message -> message.contains("actualSeverity=medium")),
            "deep 重影响预算失败应明确指出过低风险等级，实际: " + errors
        );
    }

    @Test
    void shouldReportPlantCrosslinkMismatchWhenPillMaterialCollapseToSinglePlantGate() throws IOException {
        JsonArray array = cloneCurrentArray();
        rewritePlantItemReferences(array, "pill", "P-S04");
        rewritePlantItemReferences(array, "material", "P-S04");

        List<String> errors = validateWithCurrentText(array);
        assertContains(errors, "plant crosslink mismatch");
        assertTrue(
            errors.stream().anyMatch(message -> message.contains("pill 消费链路植物候选不足")),
            "瓶颈失败应明确指出 pill 单点硬门槛，实际: " + errors
        );
        assertTrue(
            errors.stream().anyMatch(message -> message.contains("material 消费链路植物候选不足")),
            "瓶颈失败应明确指出 material 单点硬门槛，实际: " + errors
        );
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

    private static void rewritePlantItemReferences(JsonArray array, String category, String plantId) {
        String rewrittenReference = "item:" + plantId;
        for (JsonElement element : array) {
            JsonObject object = element.getAsJsonObject();
            if (!category.equals(object.get("category").getAsString())) {
                continue;
            }
            rewritePlantItemReferences(
                object.getAsJsonObject("mainSource").getAsJsonArray("anchors"),
                rewrittenReference
            );
            rewritePlantItemReferences(
                object.getAsJsonObject("backupSource").getAsJsonArray("anchors"),
                rewrittenReference
            );
            rewritePlantItemReferences(
                object.getAsJsonObject("primaryUse").getAsJsonArray("targets"),
                rewrittenReference
            );
        }
    }

    private static void rewritePlantItemReferences(JsonArray values, String rewrittenReference) {
        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i).getAsString();
            if (value.startsWith("item:P-")) {
                values.set(i, JsonParser.parseString("\"" + rewrittenReference + "\""));
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
