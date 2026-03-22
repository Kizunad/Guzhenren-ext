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
import java.util.TreeMap;
import java.util.TreeSet;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Task12（20 植物覆盖续切片）：浅度植物到 task13a 配方图的静态接入校验。
 *
 * <p>本类只做 JSON 静态图检查，不执行合成行为，也不尝试覆盖完整图论联通逻辑：
 * 1) happy-path：验证 20 个浅度植物（P-S01~P-S20），
 *    在 task13a 配方 ingredients 中至少出现一次；
 * 2) closeout-path：由于 task13a JSON 不存在 probability/weight 字段，
 *    本切片仅以 ingredients[].item 做保守预算窗校验，要求 20 个浅度植物在当前图中出现次数均为 [1,1]；
 * 3) failure-path：在内存中注入“缺失植物映射”坏样本，确保校验器会报告缺失/孤儿植物 ID。
 */
@GameTestHolder("guzhenrenext")
@PrefixGameTestTemplate(false)
public class Task12ShallowPlantRecipeGraphGameTests {

    private static final String TASK12_BATCH = "task12_plant_shallow_recipe_graph";
    private static final int TEST_TIMEOUT_TICKS = 200;
    private static final int EXPECTED_SHALLOW_PLANT_COUNT = 20;
    private static final String TASK13A_RECIPE_PATH_PREFIX = "recipes/task13a";
    private static final int SHALLOW_PLANT_OCCURRENCE_MIN = 1;
    private static final int SHALLOW_PLANT_OCCURRENCE_MAX = 1;

    /**
     * 本切片要求覆盖的 20 个浅度植物物品 ID。
     *
     * <p>来源：content_matrix.json 中 P-S01~P-S20 条目；本续切片在已覆盖 18 项基础上补齐 P-S05/P-S10，
     * 以满足 Task12 顶层“浅度植物全量入网”的静态最小合同。
     */
    private static final Set<String> EXPECTED_SHALLOW_PLANT_ITEM_IDS = Set.of(
        "guzhenrenext:qing_ya_grass",
        "guzhenrenext:ning_xue_gen",
        "guzhenrenext:ju_yuan_flower",
        "guzhenrenext:xi_sui_vine",
        "guzhenrenext:tie_pi_bamboo",
        "guzhenrenext:huo_ling_zhi_mushroom",
        "guzhenrenext:bing_xin_grass",
        "guzhenrenext:huan_du_mushroom",
        "guzhenrenext:ying_tai_lichen",
        "guzhenrenext:ci_vine",
        "guzhenrenext:jian_ye_grass",
        "guzhenrenext:chen_shui_lily_pad",
        "guzhenrenext:di_long_berry_bush",
        "guzhenrenext:feng_xin_zi",
        "guzhenrenext:lei_gu_sapling",
        "guzhenrenext:shi_yin_grass",
        "guzhenrenext:chun_yang_flower",
        "guzhenrenext:yan_shou_cocoa",
        "guzhenrenext:wang_you_grass",
        "guzhenrenext:she_yan_melon_stem"
    );

    /**
     * failure-path 固定注入样本：从真实已入网集合中临时删掉两个植物输入，
     * 用于证明校验器能够稳定报出缺失/孤儿。
     */
    private static final Set<String> INJECTED_MISSING_SAMPLE = Set.of(
        "guzhenrenext:qing_ya_grass",
        "guzhenrenext:wang_you_grass"
    );

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK12_BATCH
    )
    public void testTask12ShallowPlantRecipeGraphShouldCoverAllExpectedPlantInputs(GameTestHelper helper) {
        ResourceManager resourceManager = helper.getLevel().getServer().getResourceManager();
        RecipeLoadResult loadResult = loadTask13ARecipes(resourceManager);
        helper.assertTrue(
            loadResult.loadErrors().isEmpty(),
            "Task12 读取 task13a 配方 JSON 失败: " + String.join(" | ", loadResult.loadErrors())
        );

        helper.assertTrue(
            EXPECTED_SHALLOW_PLANT_ITEM_IDS.size() == EXPECTED_SHALLOW_PLANT_COUNT,
            "Task12 固定期望集合必须为 20 项，实际=" + EXPECTED_SHALLOW_PLANT_ITEM_IDS.size()
        );

        Set<String> actualIngredientItems = collectIngredientItemIds(loadResult.recipeJsonById());
        Map<String, Integer> ingredientItemOccurrences = collectIngredientItemOccurrences(loadResult.recipeJsonById());
        PlantInputValidationResult validationResult = validateExpectedPlantInputMapping(
            EXPECTED_SHALLOW_PLANT_ITEM_IDS,
            actualIngredientItems
        );
        PlantInputBudgetValidationResult budgetValidationResult = validateExpectedPlantInputBudgetWindow(
            EXPECTED_SHALLOW_PLANT_ITEM_IDS,
            ingredientItemOccurrences,
            SHALLOW_PLANT_OCCURRENCE_MIN,
            SHALLOW_PLANT_OCCURRENCE_MAX
        );

        helper.assertTrue(
            validationResult.missingExpectedPlantItemIds().isEmpty(),
            "Task12 缺失浅度植物配方输入映射: " + validationResult.missingExpectedPlantItemIds()
        );
        helper.assertTrue(
            validationResult.orphanExpectedPlantItemIds().isEmpty(),
            "Task12 存在要求映射但完全未入网的孤儿植物: " + validationResult.orphanExpectedPlantItemIds()
        );
        helper.assertTrue(
            budgetValidationResult.outOfWindowPlantItemIds().isEmpty(),
            "Task12 JSON-only 保守预算窗越界: outOfWindow="
                + budgetValidationResult.outOfWindowPlantItemIds()
                + ", expectedWindow=["
                + SHALLOW_PLANT_OCCURRENCE_MIN
                + ", "
                + SHALLOW_PLANT_OCCURRENCE_MAX
                + "], actual="
                + budgetValidationResult.actualOccurrenceByPlantItemId()
        );
        helper.succeed();
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK12_BATCH
    )
    public void testTask12ValidatorShouldReportMissingAndOrphanWhenPlantInputsAreBroken(GameTestHelper helper) {
        ResourceManager resourceManager = helper.getLevel().getServer().getResourceManager();
        RecipeLoadResult loadResult = loadTask13ARecipes(resourceManager);
        helper.assertTrue(
            loadResult.loadErrors().isEmpty(),
            "Task12 读取 task13a 配方 JSON 失败: " + String.join(" | ", loadResult.loadErrors())
        );

        Set<String> actualIngredientItems = collectIngredientItemIds(loadResult.recipeJsonById());
        Map<String, Integer> ingredientItemOccurrences = collectIngredientItemOccurrences(loadResult.recipeJsonById());
        helper.assertTrue(
            actualIngredientItems.containsAll(INJECTED_MISSING_SAMPLE),
            "Task12 failure-path 夹具前提不成立：真实配方未包含注入样本=" + INJECTED_MISSING_SAMPLE
        );

        Set<String> brokenIngredientItems = new HashSet<>(actualIngredientItems);
        for (String brokenPlantId : INJECTED_MISSING_SAMPLE) {
            brokenIngredientItems.remove(brokenPlantId);
        }
        Map<String, Integer> brokenIngredientItemOccurrences = new HashMap<>(ingredientItemOccurrences);
        for (String brokenPlantId : INJECTED_MISSING_SAMPLE) {
            brokenIngredientItemOccurrences.put(brokenPlantId, 0);
        }

        PlantInputValidationResult validationResult = validateExpectedPlantInputMapping(
            EXPECTED_SHALLOW_PLANT_ITEM_IDS,
            brokenIngredientItems
        );
        PlantInputBudgetValidationResult budgetValidationResult = validateExpectedPlantInputBudgetWindow(
            EXPECTED_SHALLOW_PLANT_ITEM_IDS,
            brokenIngredientItemOccurrences,
            SHALLOW_PLANT_OCCURRENCE_MIN,
            SHALLOW_PLANT_OCCURRENCE_MAX
        );

        helper.assertTrue(
            !validationResult.missingExpectedPlantItemIds().isEmpty(),
            "Task12 注入坏样本后应命中缺失植物映射"
        );
        helper.assertTrue(
            validationResult.missingExpectedPlantItemIds().containsAll(INJECTED_MISSING_SAMPLE),
            "Task12 缺失集合应包含注入样本，actual=" + validationResult.missingExpectedPlantItemIds()
        );
        helper.assertTrue(
            validationResult.orphanExpectedPlantItemIds().containsAll(INJECTED_MISSING_SAMPLE),
            "Task12 孤儿集合应包含注入样本，actual=" + validationResult.orphanExpectedPlantItemIds()
        );
        helper.assertTrue(
            !validationResult.errors().isEmpty(),
            "Task12 注入坏样本后应产出可诊断错误"
        );
        helper.assertTrue(
            !budgetValidationResult.outOfWindowPlantItemIds().isEmpty(),
            "Task12 注入坏样本后应命中 JSON-only 保守预算窗"
        );
        helper.assertTrue(
            budgetValidationResult.outOfWindowPlantItemIds().containsAll(INJECTED_MISSING_SAMPLE),
            "Task12 预算窗越界集合应包含注入样本，actual=" + budgetValidationResult.outOfWindowPlantItemIds()
        );
        helper.assertTrue(
            !budgetValidationResult.errors().isEmpty(),
            "Task12 注入坏样本后预算窗应产出可诊断错误"
        );
        helper.succeed();
    }

    /**
     * 使用 ResourceManager 一次性读取 task13a 配方 JSON。
     *
     * <p>此实现对齐 Task13A/Task14 的“listResources + JsonParser.parseReader”静态加载风格。
     */
    private static RecipeLoadResult loadTask13ARecipes(ResourceManager resourceManager) {
        Map<String, JsonObject> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        Map<ResourceLocation, Resource> resources =
            resourceManager.listResources(TASK13A_RECIPE_PATH_PREFIX, path -> path.getPath().endsWith(".json"));
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

    /**
     * 收集 task13a 所有配方的 ingredients[].item 字段。
     *
     * <p>本切片故意保持“字段可见层”静态口径：仅以 item 级出现性判断是否入网，
     * 不推导数量平衡、不推导前驱/后继一一映射。
     */
    private static Set<String> collectIngredientItemIds(Map<String, JsonObject> recipeJsonById) {
        Set<String> result = new HashSet<>();
        for (JsonObject recipeJson : recipeJsonById.values()) {
            if (!recipeJson.has("ingredients") || !recipeJson.get("ingredients").isJsonArray()) {
                continue;
            }
            JsonArray ingredients = recipeJson.getAsJsonArray("ingredients");
            for (JsonElement ingredientElement : ingredients) {
                if (!ingredientElement.isJsonObject()) {
                    continue;
                }
                JsonObject ingredientObject = ingredientElement.getAsJsonObject();
                if (!ingredientObject.has("item") || !ingredientObject.get("item").isJsonPrimitive()) {
                    continue;
                }
                result.add(ingredientObject.get("item").getAsString());
            }
        }
        return result;
    }

    /**
     * 统计 task13a 所有配方中 ingredients[].item 的出现次数。
     *
     * <p>该统计严格限定在 JSON 可见字段层，不引入 runtime 概率、权重、价值模型。
     */
    private static Map<String, Integer> collectIngredientItemOccurrences(Map<String, JsonObject> recipeJsonById) {
        Map<String, Integer> result = new HashMap<>();
        for (JsonObject recipeJson : recipeJsonById.values()) {
            if (!recipeJson.has("ingredients") || !recipeJson.get("ingredients").isJsonArray()) {
                continue;
            }
            JsonArray ingredients = recipeJson.getAsJsonArray("ingredients");
            for (JsonElement ingredientElement : ingredients) {
                if (!ingredientElement.isJsonObject()) {
                    continue;
                }
                JsonObject ingredientObject = ingredientElement.getAsJsonObject();
                if (!ingredientObject.has("item") || !ingredientObject.get("item").isJsonPrimitive()) {
                    continue;
                }
                String ingredientItemId = ingredientObject.get("item").getAsString();
                int nextCount = result.getOrDefault(ingredientItemId, 0) + 1;
                result.put(ingredientItemId, nextCount);
            }
        }
        return result;
    }

    /**
     * 仅校验“浅度植物期望输入是否入网”的窄口径 validator。
     *
     * <p>这里“孤儿”定义为：被 content_matrix 要求进入浅度丹药网络、但在 task13a ingredients 完全未出现的植物。
     * 在该首切片中，孤儿集合与缺失集合语义一致，保持实现最小可审计。
     */
    private static PlantInputValidationResult validateExpectedPlantInputMapping(
        Set<String> expectedPlantItemIds,
        Set<String> actualIngredientItemIds
    ) {
        Set<String> missing = new TreeSet<>(expectedPlantItemIds);
        missing.removeAll(actualIngredientItemIds);

        Set<String> orphan = new TreeSet<>(missing);
        List<String> errors = new ArrayList<>();
        if (!missing.isEmpty()) {
            errors.add("missing_expected_plant_inputs:" + missing);
        }
        if (!orphan.isEmpty()) {
            errors.add("orphan_expected_plants:" + orphan);
        }
        return new PlantInputValidationResult(missing, orphan, errors);
    }

    /**
     * Task12 closeout 的最小预算守卫：20 个浅度植物在当前 task13a 图切片中都应出现且仅出现一次。
     *
     * <p>说明：当前 task13a JSON 不存在 probability/weight 字段，因此无法做“概率字段”真校验。
     * 这里采用可审计、可复核的 JSON-only 保守窗口：只看 ingredients[].item 的静态出现次数，
     * 对每个期望浅度植物施加 [1,1] 次窗口约束。
     */
    private static PlantInputBudgetValidationResult validateExpectedPlantInputBudgetWindow(
        Set<String> expectedPlantItemIds,
        Map<String, Integer> ingredientItemOccurrences,
        int minOccurrenceInclusive,
        int maxOccurrenceInclusive
    ) {
        Set<String> outOfWindow = new TreeSet<>();
        Map<String, Integer> actualOccurrenceByPlantItemId = new TreeMap<>();
        List<String> errors = new ArrayList<>();
        for (String expectedPlantItemId : expectedPlantItemIds) {
            int occurrence = ingredientItemOccurrences.getOrDefault(expectedPlantItemId, 0);
            actualOccurrenceByPlantItemId.put(expectedPlantItemId, occurrence);
            if (occurrence < minOccurrenceInclusive || occurrence > maxOccurrenceInclusive) {
                outOfWindow.add(expectedPlantItemId);
                errors.add(
                    "plant_input_budget_window_violation:"
                        + expectedPlantItemId
                        + ":count="
                        + occurrence
                        + ":expected=["
                        + minOccurrenceInclusive
                        + ","
                        + maxOccurrenceInclusive
                        + "]"
                );
            }
        }
        return new PlantInputBudgetValidationResult(outOfWindow, actualOccurrenceByPlantItemId, errors);
    }

    private record RecipeLoadResult(Map<String, JsonObject> recipeJsonById, List<String> loadErrors) {
    }

    private record PlantInputValidationResult(
        Set<String> missingExpectedPlantItemIds,
        Set<String> orphanExpectedPlantItemIds,
        List<String> errors
    ) {
    }

    private record PlantInputBudgetValidationResult(
        Set<String> outOfWindowPlantItemIds,
        Map<String, Integer> actualOccurrenceByPlantItemId,
        List<String> errors
    ) {
    }
}
