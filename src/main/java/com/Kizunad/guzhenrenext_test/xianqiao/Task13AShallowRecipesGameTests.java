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

    private record ValidationResult(List<String> errors, double connectivityRatio) {
    }
}
