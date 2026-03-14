package com.Kizunad.guzhenrenext_test.xianqiao;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

/**
 * Task14：浅度丹药配方静态校验。
 *
 * <p>本测试类只做“配方清单/引用关系”的静态校验，不验证实际合成行为：
 * 1) 校验 20 个浅度丹药 pill 配方均存在；
 * 2) 校验 pill 的 manifest_index 完整覆盖 1~20；
 * 3) 校验 pill 的 prerequisite_recipe_ids 全部指向存在的 task13a/net_* 配方。
 */
@GameTestHolder("guzhenrenext")
public class Task14ShallowPillRecipeGameTests {

    private static final String TASK14_BATCH = "task14_pill_shallow_recipe";
    private static final int TEST_TIMEOUT_TICKS = 200;
    private static final String RECIPE_PATH_PREFIX = "recipes/task13a";
    private static final String TASK13A_RECIPE_NAMESPACE_PREFIX = "guzhenrenext:task13a/";
    private static final String PILL_RECIPE_ID_PREFIX = TASK13A_RECIPE_NAMESPACE_PREFIX + "pill_";
    private static final String NET_RECIPE_ID_PREFIX = TASK13A_RECIPE_NAMESPACE_PREFIX + "net_";
    private static final int EXPECTED_PILL_COUNT = 20;
    private static final int MANIFEST_MIN = 1;
    private static final int MANIFEST_MAX = 20;

    /**
     * 任务要求中的 20 个浅度丹药配方 ID 全量清单。
     *
     * <p>这里采用“固定期望集合”而不是仅判断数量，原因是数量相等无法识别“错名替换/漏一个补一个”的风险。
     */
    private static final Set<String> EXPECTED_PILL_RECIPE_IDS = new HashSet<>(List.of(
        "guzhenrenext:task13a/pill_01_xiao_huan_dan",
        "guzhenrenext:task13a/pill_02_ju_qi_san",
        "guzhenrenext:task13a/pill_03_cui_ti_dan",
        "guzhenrenext:task13a/pill_04_ji_feng_dan",
        "guzhenrenext:task13a/pill_05_tie_gu_dan",
        "guzhenrenext:task13a/pill_06_bi_du_dan",
        "guzhenrenext:task13a/pill_07_po_huan_dan",
        "guzhenrenext:task13a/pill_08_gui_xi_dan",
        "guzhenrenext:task13a/pill_09_bi_huo_dan",
        "guzhenrenext:task13a/pill_10_ye_shi_dan",
        "guzhenrenext:task13a/pill_11_bao_shi_dan",
        "guzhenrenext:task13a/pill_12_qing_shen_dan",
        "guzhenrenext:task13a/pill_13_yin_xi_dan",
        "guzhenrenext:task13a/pill_14_kuang_bao_dan",
        "guzhenrenext:task13a/pill_15_ning_shen_dan",
        "guzhenrenext:task13a/pill_16_shou_liang_wan",
        "guzhenrenext:task13a/pill_17_ling_zhi_ye",
        "guzhenrenext:task13a/pill_18_bi_gu_dan",
        "guzhenrenext:task13a/pill_19_qu_shou_san",
        "guzhenrenext:task13a/pill_20_xun_mai_dan"
    ));

    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS, batch = TASK14_BATCH)
    public void testTask14ShallowPillRecipesShouldCoverAllManifestIndices(GameTestHelper helper) {
        ResourceManager resourceManager = helper.getLevel().getServer().getResourceManager();
        RecipeLoadResult loadResult = loadTask13ARecipes(resourceManager);
        helper.assertTrue(
            loadResult.loadErrors().isEmpty(),
            "Task14 配方 JSON 读取失败: " + String.join(" | ", loadResult.loadErrors())
        );

        Map<String, JsonObject> recipes = loadResult.recipeJsonById();
        Set<String> actualPillIds = collectPillRecipeIds(recipes);
        Set<String> missingPillIds = new HashSet<>(EXPECTED_PILL_RECIPE_IDS);
        missingPillIds.removeAll(actualPillIds);

        helper.assertTrue(
            actualPillIds.size() == EXPECTED_PILL_COUNT,
            "Task14 pill 配方数量不为 20，实际=" + actualPillIds.size() + "，实际列表=" + actualPillIds
        );
        helper.assertTrue(
            missingPillIds.isEmpty(),
            "Task14 存在缺失 pill 配方: " + missingPillIds
        );

        List<String> manifestErrors = new ArrayList<>();
        Set<Integer> manifestIndices = new HashSet<>();
        for (String pillId : EXPECTED_PILL_RECIPE_IDS) {
            JsonObject recipeJson = recipes.get(pillId);
            if (recipeJson == null) {
                continue;
            }
            Integer manifestIndex = readManifestIndex(recipeJson);
            if (manifestIndex == null) {
                manifestErrors.add("缺失或非法 manifest_index: " + pillId);
                continue;
            }
            if (manifestIndex < MANIFEST_MIN || manifestIndex > MANIFEST_MAX) {
                manifestErrors.add("manifest_index 越界(" + manifestIndex + "): " + pillId);
                continue;
            }
            if (!manifestIndices.add(manifestIndex)) {
                manifestErrors.add("manifest_index 重复(" + manifestIndex + "): " + pillId);
            }
        }

        Set<Integer> missingManifestIndices = new HashSet<>();
        for (int index = MANIFEST_MIN; index <= MANIFEST_MAX; index++) {
            if (!manifestIndices.contains(index)) {
                missingManifestIndices.add(index);
            }
        }

        helper.assertTrue(
            manifestErrors.isEmpty(),
            "Task14 manifest_index 结构错误: " + String.join(" | ", manifestErrors)
        );
        helper.assertTrue(
            missingManifestIndices.isEmpty(),
            "Task14 缺失 manifest_index 覆盖: " + missingManifestIndices
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS, batch = TASK14_BATCH)
    public void testTask14ShallowPillRecipesShouldReferenceExistingPrerequisites(GameTestHelper helper) {
        ResourceManager resourceManager = helper.getLevel().getServer().getResourceManager();
        RecipeLoadResult loadResult = loadTask13ARecipes(resourceManager);
        helper.assertTrue(
            loadResult.loadErrors().isEmpty(),
            "Task14 配方 JSON 读取失败: " + String.join(" | ", loadResult.loadErrors())
        );

        Map<String, JsonObject> recipes = loadResult.recipeJsonById();
        Set<String> allRecipeIds = recipes.keySet();
        Set<String> allNetRecipeIds = collectNetRecipeIds(recipes);

        List<String> prerequisiteErrors = new ArrayList<>();
        for (String pillId : EXPECTED_PILL_RECIPE_IDS) {
            JsonObject recipeJson = recipes.get(pillId);
            if (recipeJson == null) {
                prerequisiteErrors.add("缺失 pill 配方，无法校验 prerequisite: " + pillId);
                continue;
            }
            List<String> prerequisiteIds = readPrerequisiteIds(recipeJson);
            if (prerequisiteIds.isEmpty()) {
                prerequisiteErrors.add("缺失 prerequisite_recipe_ids: " + pillId);
                continue;
            }
            for (String prerequisiteId : prerequisiteIds) {
                if (!prerequisiteId.startsWith(NET_RECIPE_ID_PREFIX)) {
                    prerequisiteErrors.add(
                        "prerequisite 非 task13a/net_*: pill=" + pillId + ", prerequisite=" + prerequisiteId
                    );
                    continue;
                }
                if (!allRecipeIds.contains(prerequisiteId) || !allNetRecipeIds.contains(prerequisiteId)) {
                    prerequisiteErrors.add(
                        "prerequisite 指向不存在 net 配方: pill=" + pillId + ", prerequisite=" + prerequisiteId
                    );
                }
            }
        }

        helper.assertTrue(
            prerequisiteErrors.isEmpty(),
            "Task14 prerequisite 校验失败: " + String.join(" | ", prerequisiteErrors)
        );
        helper.succeed();
    }

    /**
     * 从 ResourceManager 统一加载 task13a 下全部 JSON 配方。
     *
     * <p>任务要求明确只能通过 listResources("recipes/task13a", ...) 获取资源，本方法是唯一入口。
     */
    private static RecipeLoadResult loadTask13ARecipes(ResourceManager resourceManager) {
        Map<String, JsonObject> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        Map<ResourceLocation, Resource> resources =
            resourceManager.listResources(RECIPE_PATH_PREFIX, path -> path.getPath().endsWith(".json"));
        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation path = entry.getKey();
            String filePath = path.getPath();
            String relative = filePath.substring("recipes/".length(), filePath.length() - ".json".length());
            String recipeId = "guzhenrenext:" + relative;
            try (InputStream stream = entry.getValue().open();
                InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                JsonElement element = JsonParser.parseReader(reader);
                if (!element.isJsonObject()) {
                    errors.add("JSON 根节点不是对象: " + recipeId);
                    continue;
                }
                result.put(recipeId, element.getAsJsonObject());
            } catch (Exception exception) {
                errors.add("读取异常: " + recipeId + ", cause=" + exception.getMessage());
            }
        }
        return new RecipeLoadResult(result, errors);
    }

    private static Set<String> collectPillRecipeIds(Map<String, JsonObject> recipes) {
        Set<String> result = new HashSet<>();
        for (String recipeId : recipes.keySet()) {
            if (recipeId.startsWith(PILL_RECIPE_ID_PREFIX)) {
                result.add(recipeId);
            }
        }
        return result;
    }

    private static Set<String> collectNetRecipeIds(Map<String, JsonObject> recipes) {
        Set<String> result = new HashSet<>();
        for (String recipeId : recipes.keySet()) {
            if (recipeId.startsWith(NET_RECIPE_ID_PREFIX)) {
                result.add(recipeId);
            }
        }
        return result;
    }

    private static Integer readManifestIndex(JsonObject recipeJson) {
        JsonObject layering = recipeJson.has("xq_layering") ? recipeJson.getAsJsonObject("xq_layering") : null;
        if (layering == null || !layering.has("manifest_index")) {
            return null;
        }
        try {
            return layering.get("manifest_index").getAsInt();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static List<String> readPrerequisiteIds(JsonObject recipeJson) {
        JsonObject layering = recipeJson.has("xq_layering") ? recipeJson.getAsJsonObject("xq_layering") : null;
        if (layering == null || !layering.has("prerequisite_recipe_ids")) {
            return List.of();
        }
        JsonArray prerequisites = layering.getAsJsonArray("prerequisite_recipe_ids");
        List<String> result = new ArrayList<>();
        for (JsonElement prerequisite : prerequisites) {
            result.add(prerequisite.getAsString());
        }
        return result;
    }

    private record RecipeLoadResult(Map<String, JsonObject> recipeJsonById, List<String> loadErrors) {
    }
}
