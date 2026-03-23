package com.Kizunad.guzhenrenext_test.xianqiao;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder("guzhenrenext")
public class Task13AShallowRecipesGameTests {

    private static final String TASK13A_BATCH = "task13a_shallow_recipes";
    private static final int TEST_TIMEOUT_TICKS = 200;
    private static final String TASK13A_RECIPE_PATH_PREFIX = "recipes/task13a";
    private static final int EXPECTED_MIN_RECIPE_COUNT = 100;
    private static final int EXPECTED_PILL_RECIPE_COUNT = 20;
    private static final double CONNECTIVITY_RATIO_THRESHOLD = 0.80D;
    private static final String LING_TIE_XIE_ID = "guzhenrenext:ling_tie_xie";
    private static final double LING_TIE_XIE_BUDGET_RATIO_MIN = 1.0D;
    private static final double LING_TIE_XIE_BUDGET_RATIO_MAX = 2.0D;
    private static final List<String> LING_TIE_XIE_FRONTIER_SUBSET = List.of(
        "guzhenrenext:task13a/net_015",
        "guzhenrenext:task13a/net_016",
        "guzhenrenext:task13a/net_017",
        "guzhenrenext:task13a/net_077"
    );

    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS, batch = TASK13A_BATCH)
    public void testTask13ARecipeNetworkShouldBeConnected(GameTestHelper helper) {
        ResourceManager resourceManager = helper.getLevel().getServer().getResourceManager();
        Map<String, JsonObject> recipeJsonById = loadTask13ARecipes(resourceManager);
        helper.assertTrue(
            recipeJsonById.size() >= EXPECTED_MIN_RECIPE_COUNT,
            "Task13A 配方数量不足，期望至少=" + EXPECTED_MIN_RECIPE_COUNT + "，实际=" + recipeJsonById.size()
        );
        long pillRecipeCount = recipeJsonById.keySet().stream().filter(id -> id.contains("pill_")).count();
        helper.assertTrue(
            pillRecipeCount == EXPECTED_PILL_RECIPE_COUNT,
            "Task13A 浅度丹药配方数量必须为20，实际=" + pillRecipeCount
        );

        ValidationResult validationResult = validateConnectivity(recipeJsonById);
        helper.assertTrue(
            validationResult.errors().isEmpty(),
            "Task13A 配方连通校验失败: " + String.join(" | ", validationResult.errors())
        );
        helper.assertTrue(
            validationResult.connectivityRatio() >= CONNECTIVITY_RATIO_THRESHOLD,
            "Task13A 配方连通率低于阈值，expected>="
                + CONNECTIVITY_RATIO_THRESHOLD
                + ", actual="
                + validationResult.connectivityRatio()
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS, batch = TASK13A_BATCH)
    public void testTask13ARecipeNetworkValidatorShouldRejectBrokenDefinitions(GameTestHelper helper) {
        Map<String, JsonObject> broken = new HashMap<>();
        broken.put(
            "guzhenrenext:task13a/net_a",
            createLayeredRecipe("advanced", List.of("guzhenrenext:task13a/net_missing"))
        );
        broken.put(
            "guzhenrenext:task13a/net_b",
            createLayeredRecipe("core", List.of("guzhenrenext:task13a/net_c"))
        );
        broken.put(
            "guzhenrenext:task13a/net_c",
            createLayeredRecipe("core", List.of("guzhenrenext:task13a/net_b"))
        );

        ValidationResult result = validateConnectivity(broken);
        helper.assertTrue(!result.errors().isEmpty(), "坏图应触发连通性校验失败");
        helper.assertTrue(
            containsError(result.errors(), "missing_dependency") && containsError(result.errors(), "dependency_cycle"),
            "坏图应同时命中 missing_dependency 与 dependency_cycle，实际=" + result.errors()
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS, batch = TASK13A_BATCH)
    public void testTask13ARealRecipeNetworkShouldNotContainNonDecreasingItemFlowError(GameTestHelper helper) {
        ResourceManager resourceManager = helper.getLevel().getServer().getResourceManager();
        Map<String, JsonObject> recipeJsonById = loadTask13ARecipes(resourceManager);

        ValidationResult validationResult = validateConnectivity(recipeJsonById);
        helper.assertTrue(
            !containsError(validationResult.errors(), "non_decreasing_item_flow"),
            "真实 Task13A 配方网络不应命中 non_decreasing_item_flow，实际错误="
                + String.join(" | ", validationResult.errors())
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS, batch = TASK13A_BATCH)
    public void testTask13ARecipeNetworkValidatorShouldRejectNonDecreasingItemFlow(GameTestHelper helper) {
        Map<String, JsonObject> broken = new HashMap<>();
        broken.put("guzhenrenext:task13a/copy_arbitrage", createCopyArbitrageRecipe());

        ValidationResult result = validateConnectivity(broken);
        helper.assertTrue(!result.errors().isEmpty(), "复制套利坏配方应触发校验失败");
        helper.assertTrue(
            containsError(result.errors(), "non_decreasing_item_flow"),
            "复制套利坏配方应命中 non_decreasing_item_flow，实际=" + result.errors()
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS, batch = TASK13A_BATCH)
    public void testTask13ALingTieXieBudgetRatioShouldStayInJsonOnlyWindow(GameTestHelper helper) {
        ResourceManager resourceManager = helper.getLevel().getServer().getResourceManager();
        Map<String, JsonObject> recipeJsonById = loadTask13ARecipes(resourceManager);

        int consume = 0;
        int produce = 0;
        for (String recipeId : LING_TIE_XIE_FRONTIER_SUBSET) {
            JsonObject recipeJson = recipeJsonById.get(recipeId);
            helper.assertTrue(recipeJson != null, "缺少前沿子集配方: " + recipeId);

            consume += countIngredientItemOccurrences(recipeJson, LING_TIE_XIE_ID);
            produce += readResultCountIfMatches(recipeJson, LING_TIE_XIE_ID);
        }

        helper.assertTrue(
            consume > 0,
            "JSON-only 保守预算比近似要求 consume>0，当前前沿子集 consume=" + consume
        );
        helper.assertTrue(
            produce > 0,
            "JSON-only 保守预算比近似要求 produce>0，当前前沿子集 produce=" + produce
        );

        // 说明：这里故意只使用 ingredients[].item 与 result.id/result.count，
        // 只做“字段可见层”的保守预算比近似，不代表完整价值模型或全网络经济闭包。
        double ratio = (double) consume / produce;
        helper.assertTrue(
            ratio >= LING_TIE_XIE_BUDGET_RATIO_MIN && ratio <= LING_TIE_XIE_BUDGET_RATIO_MAX,
            "JSON-only 保守预算比窗口越界: subset="
                + LING_TIE_XIE_FRONTIER_SUBSET
                + ", consume="
                + consume
                + ", produce="
                + produce
                + ", ratio="
                + ratio
                + ", expectedWindow=["
                + LING_TIE_XIE_BUDGET_RATIO_MIN
                + ", "
                + LING_TIE_XIE_BUDGET_RATIO_MAX
                + "]"
        );
        helper.succeed();
    }

    private static boolean containsError(Collection<String> errors, String token) {
        for (String error : errors) {
            if (error.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private static Map<String, JsonObject> loadTask13ARecipes(ResourceManager resourceManager) {
        Map<String, JsonObject> result = new HashMap<>();
        Map<ResourceLocation, Resource> all =
            resourceManager.listResources(TASK13A_RECIPE_PATH_PREFIX, path -> path.getPath().endsWith(".json"));
        for (Map.Entry<ResourceLocation, Resource> entry : all.entrySet()) {
            ResourceLocation path = entry.getKey();
            String filePath = path.getPath();
            String relative = filePath.substring("recipes/".length(), filePath.length() - ".json".length());
            String recipeId = "guzhenrenext:" + relative;
            try (InputStream stream = entry.getValue().open();
                InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                JsonElement element = JsonParser.parseReader(reader);
                if (element.isJsonObject()) {
                    result.put(recipeId, element.getAsJsonObject());
                }
            } catch (Exception ignored) {
            }
        }
        return result;
    }

    private static ValidationResult validateConnectivity(Map<String, JsonObject> recipeJsonById) {
        List<String> errors = new ArrayList<>();
        Map<String, List<String>> edges = new HashMap<>();
        Map<String, Integer> indegree = new HashMap<>();
        int connectedNodes = 0;

        for (Map.Entry<String, JsonObject> entry : recipeJsonById.entrySet()) {
            String recipeId = entry.getKey();
            JsonObject root = entry.getValue();
            List<String> prerequisites = readPrerequisiteIds(root);
            String tier = readTier(root);

            validateCopyArbitrage(recipeId, root, errors);

            edges.put(recipeId, prerequisites);
            indegree.putIfAbsent(recipeId, 0);
            for (String prerequisite : prerequisites) {
                if (!recipeJsonById.containsKey(prerequisite)) {
                    errors.add("missing_dependency:" + recipeId + "->" + prerequisite);
                    continue;
                }
                indegree.put(recipeId, indegree.get(recipeId) + 1);
            }
            if (!"basic".equals(tier) && prerequisites.isEmpty()) {
                errors.add("non_basic_without_upstream:" + recipeId);
            }
        }

        Map<String, Set<String>> reverseEdges = new HashMap<>();
        for (String id : recipeJsonById.keySet()) {
            reverseEdges.put(id, new HashSet<>());
        }
        for (Map.Entry<String, List<String>> edgeEntry : edges.entrySet()) {
            String id = edgeEntry.getKey();
            for (String prerequisite : edgeEntry.getValue()) {
                if (reverseEdges.containsKey(prerequisite)) {
                    reverseEdges.get(prerequisite).add(id);
                }
            }
        }

        for (String id : recipeJsonById.keySet()) {
            boolean hasUpstream = !edges.getOrDefault(id, List.of()).isEmpty();
            boolean hasDownstream = !reverseEdges.getOrDefault(id, Set.of()).isEmpty();
            if (hasUpstream && hasDownstream) {
                connectedNodes++;
            }
        }

        double connectivityRatio = recipeJsonById.isEmpty() ? 0.0D : (double) connectedNodes / recipeJsonById.size();

        int visitedCount = runTopoAndReturnVisitedCount(edges, indegree);
        if (visitedCount != recipeJsonById.size()) {
            errors.add("dependency_cycle:visited=" + visitedCount + ",total=" + recipeJsonById.size());
        }
        return new ValidationResult(errors, connectivityRatio);
    }

    private static int runTopoAndReturnVisitedCount(Map<String, List<String>> edges, Map<String, Integer> indegree) {
        Map<String, Integer> mutableIndegree = new HashMap<>(indegree);
        ArrayDeque<String> queue = new ArrayDeque<>();
        for (Map.Entry<String, Integer> entry : mutableIndegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        int visited = 0;
        Map<String, Set<String>> reverse = new HashMap<>();
        for (String id : edges.keySet()) {
            reverse.put(id, new HashSet<>());
        }
        for (Map.Entry<String, List<String>> entry : edges.entrySet()) {
            for (String prerequisite : entry.getValue()) {
                if (reverse.containsKey(prerequisite)) {
                    reverse.get(prerequisite).add(entry.getKey());
                }
            }
        }

        while (!queue.isEmpty()) {
            String id = queue.removeFirst();
            visited++;
            for (String dependent : reverse.getOrDefault(id, Set.of())) {
                int next = mutableIndegree.get(dependent) - 1;
                mutableIndegree.put(dependent, next);
                if (next == 0) {
                    queue.add(dependent);
                }
            }
        }
        return visited;
    }

    private static void validateCopyArbitrage(String recipeId, JsonObject recipeJson, List<String> errors) {
        if (!recipeJson.has("ingredients") || !recipeJson.get("ingredients").isJsonArray()) {
            errors.add("unsupported_ingredient_form:" + recipeId + ":missing_ingredients_array");
            return;
        }
        JsonArray ingredients = recipeJson.getAsJsonArray("ingredients");
        int totalIngredientItemCount = 0;
        for (int i = 0; i < ingredients.size(); i++) {
            JsonElement ingredientElement = ingredients.get(i);
            if (!ingredientElement.isJsonObject()) {
                errors.add("unsupported_ingredient_form:" + recipeId + ":index=" + i + ":not_object");
                return;
            }
            JsonObject ingredientObject = ingredientElement.getAsJsonObject();
            if (!ingredientObject.has("item") || !ingredientObject.get("item").isJsonPrimitive()) {
                errors.add("unsupported_ingredient_form:" + recipeId + ":index=" + i + ":missing_item");
                return;
            }
            totalIngredientItemCount++;
        }

        if (!recipeJson.has("result") || !recipeJson.get("result").isJsonObject()) {
            errors.add("unsupported_result_form:" + recipeId + ":missing_result_object");
            return;
        }
        JsonObject result = recipeJson.getAsJsonObject("result");
        if (!result.has("id") || !result.get("id").isJsonPrimitive()) {
            errors.add("unsupported_result_form:" + recipeId + ":missing_result_id");
            return;
        }
        if (!result.has("count") || !result.get("count").isJsonPrimitive()) {
            errors.add("unsupported_result_form:" + recipeId + ":missing_result_count");
            return;
        }

        int resultCount;
        try {
            resultCount = result.get("count").getAsInt();
        } catch (Exception ex) {
            errors.add("unsupported_result_form:" + recipeId + ":invalid_result_count");
            return;
        }

        if (resultCount <= 0) {
            errors.add("unsupported_result_form:" + recipeId + ":non_positive_result_count=" + resultCount);
            return;
        }

        if (totalIngredientItemCount <= resultCount) {
            errors.add(
                "non_decreasing_item_flow:"
                    + recipeId
                    + ":ingredient_count="
                    + totalIngredientItemCount
                    + ":result_count="
                    + resultCount
            );
        }
    }

    private static List<String> readPrerequisiteIds(JsonObject recipeJson) {
        JsonObject layering = recipeJson.has("xq_layering") ? recipeJson.getAsJsonObject("xq_layering") : null;
        if (layering == null || !layering.has("prerequisite_recipe_ids")) {
            return List.of();
        }
        JsonArray array = layering.getAsJsonArray("prerequisite_recipe_ids");
        List<String> result = new ArrayList<>();
        for (JsonElement element : array) {
            result.add(element.getAsString());
        }
        return result;
    }

    private static int countIngredientItemOccurrences(JsonObject recipeJson, String itemId) {
        if (!recipeJson.has("ingredients") || !recipeJson.get("ingredients").isJsonArray()) {
            return 0;
        }
        JsonArray ingredients = recipeJson.getAsJsonArray("ingredients");
        int count = 0;
        for (JsonElement ingredientElement : ingredients) {
            if (!ingredientElement.isJsonObject()) {
                continue;
            }
            JsonObject ingredientObject = ingredientElement.getAsJsonObject();
            if (!ingredientObject.has("item") || !ingredientObject.get("item").isJsonPrimitive()) {
                continue;
            }
            if (itemId.equals(ingredientObject.get("item").getAsString())) {
                count++;
            }
        }
        return count;
    }

    private static int readResultCountIfMatches(JsonObject recipeJson, String itemId) {
        if (!recipeJson.has("result") || !recipeJson.get("result").isJsonObject()) {
            return 0;
        }
        JsonObject result = recipeJson.getAsJsonObject("result");
        if (!result.has("id") || !result.get("id").isJsonPrimitive()) {
            return 0;
        }
        if (!itemId.equals(result.get("id").getAsString())) {
            return 0;
        }
        if (!result.has("count") || !result.get("count").isJsonPrimitive()) {
            return 0;
        }
        try {
            return result.get("count").getAsInt();
        } catch (Exception ex) {
            return 0;
        }
    }

    private static String readTier(JsonObject recipeJson) {
        JsonObject layering = recipeJson.has("xq_layering") ? recipeJson.getAsJsonObject("xq_layering") : null;
        if (layering == null || !layering.has("tier")) {
            return "basic";
        }
        return layering.get("tier").getAsString();
    }

    private static JsonObject createLayeredRecipe(String tier, List<String> prerequisites) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shapeless");
        JsonArray ingredients = new JsonArray();
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", "minecraft:wheat");
        ingredients.add(ingredient);
        root.add("ingredients", ingredients);

        JsonObject result = new JsonObject();
        result.addProperty("id", "minecraft:bread");
        result.addProperty("count", 1);
        root.add("result", result);

        JsonObject layering = new JsonObject();
        layering.addProperty("tier", tier);
        JsonArray ids = new JsonArray();
        for (String prerequisite : prerequisites) {
            ids.add(prerequisite);
        }
        layering.add("prerequisite_recipe_ids", ids);
        root.add("xq_layering", layering);
        return root;
    }

    private static JsonObject createCopyArbitrageRecipe() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shapeless");
        JsonArray ingredients = new JsonArray();
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", "minecraft:wheat");
        ingredients.add(ingredient);
        root.add("ingredients", ingredients);

        JsonObject result = new JsonObject();
        result.addProperty("id", "minecraft:wheat");
        result.addProperty("count", 1);
        root.add("result", result);

        JsonObject layering = new JsonObject();
        layering.addProperty("tier", "basic");
        layering.add("prerequisite_recipe_ids", new JsonArray());
        root.add("xq_layering", layering);
        return root;
    }

    private record ValidationResult(List<String> errors, double connectivityRatio) {
    }
}
