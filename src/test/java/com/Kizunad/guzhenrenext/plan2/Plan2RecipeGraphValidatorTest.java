package com.Kizunad.guzhenrenext.plan2;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class Plan2RecipeGraphValidatorTest {

    private static final String RECIPE_PILL_01 = "guzhenrenext:task13a/pill_01_xiao_huan_dan";
    private static final String RECIPE_NET_001 = "guzhenrenext:task13a/net_001";
    private static final String RECIPE_NET_002 = "guzhenrenext:task13a/net_002";
    private static final String BROKEN_PREREQUISITE_ID = "guzhenrenext:task13a/net_999_missing";
    private static final String BROKEN_PLAN_ITEM_ID = "C-S99";

    @Test
    void shouldPassCurrentPlan2MatrixAndTask13AStaticRecipeGraph() throws IOException {
        // given: Task32 首切片边界固定为“测试侧 + JSON-only”。
        // given: 这里只读取静态 recipes/task13a（复数目录）图，不触碰 runtime 的 recipe/task13a（单数目录）镜像。
        List<String> errors = Plan2RecipeGraphValidator.validateCurrentGraphs();
        assertTrue(
            errors.isEmpty(),
            "Task32 首切片 happy-path 应通过（matrix + recipes/task13a 静态图），实际错误: " + errors
        );
    }

    @Test
    void shouldReportOrphanAndBrokenPlanItemReferenceWithSpecificBrokenId() throws IOException {
        JsonArray matrix = cloneCurrentMatrix();
        JsonObject dD10 = Plan2RecipeGraphTestSupport.findMatrixEntryById(matrix, "D-D10");
        replaceItemReferenceEverywhere(matrix, "item:D-D10", "system:breakthrough");

        JsonArray brokenBackupAnchors = new JsonArray();
        brokenBackupAnchors.add("item:" + BROKEN_PLAN_ITEM_ID);
        brokenBackupAnchors.add("system:mutation");
        dD10.getAsJsonObject("backupSource").add("anchors", brokenBackupAnchors);

        JsonArray noItemTargets = new JsonArray();
        noItemTargets.add("system:breakthrough");
        noItemTargets.add("system:mutation");
        dD10.getAsJsonObject("primaryUse").add("targets", noItemTargets);

        List<String> errors = validate(matrix, cloneCurrentRecipes());

        assertContains(errors, "plan2 item graph broken_reference");
        assertContains(errors, BROKEN_PLAN_ITEM_ID);
        assertContains(errors, "plan2 item graph orphan_entry: D-D10");
    }

    @Test
    void shouldReportMissingPrerequisiteRecipeIdWithSpecificBrokenRecipeId() throws IOException {
        Map<String, JsonObject> recipes = cloneCurrentRecipes();
        JsonObject pillRecipe = requireRecipe(recipes, RECIPE_PILL_01);
        JsonArray prerequisites = new JsonArray();
        prerequisites.add(BROKEN_PREREQUISITE_ID);
        pillRecipe.getAsJsonObject("xq_layering").add("prerequisite_recipe_ids", prerequisites);

        List<String> errors = validate(cloneCurrentMatrix(), recipes);

        assertContains(errors, "task13a prerequisite missing");
        assertContains(errors, RECIPE_PILL_01);
        assertContains(errors, BROKEN_PREREQUISITE_ID);
    }

    @Test
    void shouldReportTask13ARecipeCycleWhenPrerequisiteLoopInjectedInMemory() throws IOException {
        Map<String, JsonObject> recipes = cloneCurrentRecipes();
        JsonObject net001 = requireRecipe(recipes, RECIPE_NET_001);
        JsonObject net002 = requireRecipe(recipes, RECIPE_NET_002);

        JsonArray net001Prerequisites = new JsonArray();
        net001Prerequisites.add(RECIPE_NET_002);
        net001.getAsJsonObject("xq_layering").add("prerequisite_recipe_ids", net001Prerequisites);

        JsonArray net002Prerequisites = new JsonArray();
        net002Prerequisites.add(RECIPE_NET_001);
        net002.getAsJsonObject("xq_layering").add("prerequisite_recipe_ids", net002Prerequisites);

        List<String> errors = validate(cloneCurrentMatrix(), recipes);

        assertContains(errors, "task13a prerequisite cycle");
        assertContains(errors, RECIPE_NET_001);
        assertContains(errors, RECIPE_NET_002);
    }

    @Test
    void shouldReportUnreachableEndgameBacktraceWhenFinalChainIsIsolated() throws IOException {
        JsonArray matrix = cloneCurrentMatrix();
        JsonObject dD04 = Plan2RecipeGraphTestSupport.findMatrixEntryById(matrix, "D-D04");
        JsonObject cS01 = Plan2RecipeGraphTestSupport.findMatrixEntryById(matrix, "C-S01");

        JsonArray dD04BackupAnchors = new JsonArray();
        dD04BackupAnchors.add("item:M-D09");
        dD04BackupAnchors.add("system:timeflow");
        dD04.getAsJsonObject("backupSource").add("anchors", dD04BackupAnchors);

        JsonArray dD04Targets = new JsonArray();
        dD04Targets.add("item:M-D09");
        dD04Targets.add("system:timeflow");
        dD04.getAsJsonObject("primaryUse").add("targets", dD04Targets);

        JsonArray cS01BackupAnchors = new JsonArray();
        cS01BackupAnchors.add("item:D-D04");
        cS01BackupAnchors.add("system:spirit");
        cS01.getAsJsonObject("backupSource").add("anchors", cS01BackupAnchors);

        List<String> errors = validate(matrix, cloneCurrentRecipes());

        assertContains(errors, "plan2 endgame backtrace unreachable");
        assertContains(errors, "seed=D-D04");
    }

    @Test
    void shouldReportCategoryParticipationWhenAnyFinalCategorySeedIsMissing() throws IOException {
        JsonArray matrix = cloneCurrentMatrix();

        for (int index = 0; index < matrix.size(); index++) {
            JsonObject entry = matrix.get(index).getAsJsonObject();
            if ("plant".equals(entry.get("category").getAsString())) {
                entry.addProperty("category", "material");
            }
        }

        List<String> errors = validate(matrix, cloneCurrentRecipes());

        assertContains(errors, "plan2 category participation disconnected closure");
        assertContains(errors, "plant");
    }

    private static JsonArray cloneCurrentMatrix() throws IOException {
        return Plan2RecipeGraphTestSupport.deepCopyMatrix(Plan2RecipeGraphTestSupport.readCurrentMatrixArray());
    }

    private static Map<String, JsonObject> cloneCurrentRecipes() throws IOException {
        return Plan2RecipeGraphTestSupport.deepCopyRecipeGraph(
            Plan2RecipeGraphTestSupport.readCurrentTask13ARecipeGraph()
        );
    }

    private static List<String> validate(JsonArray matrix, Map<String, JsonObject> recipes) {
        return Plan2RecipeGraphValidator.validate(matrix, recipes);
    }

    private static JsonObject requireRecipe(Map<String, JsonObject> recipes, String recipeId) {
        JsonObject recipe = recipes.get(recipeId);
        if (recipe == null) {
            throw new IllegalStateException("找不到配方: " + recipeId);
        }
        return recipe;
    }

    private static void replaceItemReferenceEverywhere(JsonArray matrix, String oldValue, String newValue) {
        for (int index = 0; index < matrix.size(); index++) {
            JsonObject entry = matrix.get(index).getAsJsonObject();
            rewriteStringArrayValue(
                entry.getAsJsonObject("mainSource").getAsJsonArray("anchors"),
                oldValue,
                newValue
            );
            rewriteStringArrayValue(
                entry.getAsJsonObject("backupSource").getAsJsonArray("anchors"),
                oldValue,
                newValue
            );
            rewriteStringArrayValue(
                entry.getAsJsonObject("primaryUse").getAsJsonArray("targets"),
                oldValue,
                newValue
            );
        }
    }

    private static void rewriteStringArrayValue(JsonArray array, String oldValue, String newValue) {
        for (int index = 0; index < array.size(); index++) {
            if (oldValue.equals(array.get(index).getAsString())) {
                array.set(index, JsonParser.parseString("\"" + newValue + "\""));
            }
        }
    }

    private static void assertContains(List<String> errors, String expected) {
        assertTrue(
            errors.stream().anyMatch(message -> message.contains(expected)),
            "期望错误包含: " + expected + "，实际: " + errors
        );
    }
}
