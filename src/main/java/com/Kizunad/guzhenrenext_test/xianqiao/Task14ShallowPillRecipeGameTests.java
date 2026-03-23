package com.Kizunad.guzhenrenext_test.xianqiao;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;


import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;


import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;


import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder("guzhenrenext")
public class Task14ShallowPillRecipeGameTests {

    private static final String TASK14_BATCH = "task14_pill_shallow_recipe";
    private static final String PLAN2_PILL_SHALLOW_CRAFT_BATCH = "plan2.pill.shallow.craft";
    private static final String PLAN2_PILL_SHALLOW_INVALID_RECIPE_BATCH = "plan2.pill.shallow.invalid_recipe";
    private static final int TEST_TIMEOUT_TICKS = 200;
    private static final String RECIPE_PATH_PREFIX = "recipes/task13a";
    private static final String TASK13A_RECIPE_NAMESPACE_PREFIX = "guzhenrenext:task13a/";
    private static final String PILL_RECIPE_ID_PREFIX = TASK13A_RECIPE_NAMESPACE_PREFIX + "pill_";
    private static final String NET_RECIPE_ID_PREFIX = TASK13A_RECIPE_NAMESPACE_PREFIX + "net_";
    private static final int EXPECTED_PILL_COUNT = 20;
    private static final int MANIFEST_MIN = 1;
    private static final int MANIFEST_MAX = 20;
    private static final int CRAFTING_GRID_WIDTH = 3;
    private static final int CRAFTING_GRID_HEIGHT = 3;
    private static final int CRAFTING_GRID_SIZE = CRAFTING_GRID_WIDTH * CRAFTING_GRID_HEIGHT;
    private static final int FIRST_INGREDIENT_INDEX = 0;
    private static final int ERROR_SUMMARY_LIMIT = 3;
    private static final List<String> INVALID_INGREDIENT_CANDIDATES = List.of(
        "minecraft:barrier",
        "minecraft:bedrock",
        "minecraft:command_block"
    );

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

    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS, batch = PLAN2_PILL_SHALLOW_CRAFT_BATCH)
    public void testPlan2PillShallowCraftShouldMatchAllRecipesAtRuntime(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        RecipeManager recipeManager = level.getRecipeManager();
        ResourceManager resourceManager = level.getServer().getResourceManager();
        RecipeLoadResult loadResult = loadTask13ARecipes(resourceManager);
        helper.assertTrue(
            loadResult.loadErrors().isEmpty(),
            "Task14 运行时校验前置失败，JSON 读取异常: " + String.join(" | ", loadResult.loadErrors())
        );

        Map<String, JsonObject> recipes = loadResult.recipeJsonById();
        List<String> orderedPillRecipeIds = new ArrayList<>(EXPECTED_PILL_RECIPE_IDS);
        Collections.sort(orderedPillRecipeIds);
        List<String> runtimeErrors = new ArrayList<>();
        for (String recipeId : orderedPillRecipeIds) {
            JsonObject recipeJson = recipes.get(recipeId);
            if (recipeJson == null) {
                runtimeErrors.add("happy-path 缺失 pill 配方 JSON: " + recipeId);
                continue;
            }

            RecipeRuntimeSpec runtimeSpec = readRecipeRuntimeSpec(recipeId, recipeJson, runtimeErrors);
            if (runtimeSpec == null) {
                continue;
            }

            CraftingInput validInput = createCraftingInput(
                runtimeSpec.ingredientItemIds(),
                runtimeErrors,
                recipeId,
                "happy-path"
            );
            if (validInput == null) {
                continue;
            }

            List<String> normalizedIngredientItemIds = normalizeIngredientItemIds(
                runtimeSpec.ingredientItemIds(),
                runtimeErrors,
                recipeId,
                "happy-path"
            );
            if (normalizedIngredientItemIds == null) {
                continue;
            }

            CraftingInput normalizedInput = createCraftingInput(
                normalizedIngredientItemIds,
                runtimeErrors,
                recipeId,
                "happy-path-normalized"
            );
            if (normalizedInput == null) {
                continue;
            }

            List<CraftingInput> candidateInputs = List.of(validInput, normalizedInput);
            RecipeHolder<CraftingRecipe> runtimeRecipeHolder = getCraftingRecipeById(
                recipeManager,
                recipeId,
                runtimeErrors,
                "happy-path"
            );
            if (runtimeRecipeHolder == null) {
                continue;
            }

            CraftingInput matchedInput = findFirstMatchedInput(runtimeRecipeHolder.value(), candidateInputs, level);
            if (matchedInput == null) {
                runtimeErrors.add(
                    "happy-path 运行时配方已解析但输入未命中: expected="
                        + recipeId
                        + ", runtimeId="
                        + runtimeRecipeHolder.id()
                );
                continue;
            }

            ItemStack assembled = runtimeRecipeHolder.value().assemble(matchedInput, level.registryAccess());
            validateAssembledResult(runtimeSpec, assembled, recipeId, runtimeErrors, "happy-path");
        }

        helper.assertTrue(runtimeErrors.isEmpty(), buildErrorSummary("plan2.pill.shallow.craft", runtimeErrors));
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS, batch = PLAN2_PILL_SHALLOW_INVALID_RECIPE_BATCH)
    public void testPlan2PillShallowInvalidRecipeShouldProduceNoOutput(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        RecipeManager recipeManager = level.getRecipeManager();
        ResourceManager resourceManager = level.getServer().getResourceManager();
        RecipeLoadResult loadResult = loadTask13ARecipes(resourceManager);
        helper.assertTrue(
            loadResult.loadErrors().isEmpty(),
            "Task14 非法输入校验前置失败，JSON 读取异常: " + String.join(" | ", loadResult.loadErrors())
        );

        Map<String, JsonObject> recipes = loadResult.recipeJsonById();
        List<String> orderedPillRecipeIds = new ArrayList<>(EXPECTED_PILL_RECIPE_IDS);
        Collections.sort(orderedPillRecipeIds);
        List<String> runtimeErrors = new ArrayList<>();
        for (String recipeId : orderedPillRecipeIds) {
            JsonObject recipeJson = recipes.get(recipeId);
            if (recipeJson == null) {
                runtimeErrors.add("invalid-path 缺失 pill 配方 JSON: " + recipeId);
                continue;
            }

            RecipeRuntimeSpec runtimeSpec = readRecipeRuntimeSpec(recipeId, recipeJson, runtimeErrors);
            if (runtimeSpec == null) {
                continue;
            }

            CraftingInput validInput = createCraftingInput(
                runtimeSpec.ingredientItemIds(),
                runtimeErrors,
                recipeId,
                "invalid-path-baseline"
            );
            if (validInput == null) {
                continue;
            }

            List<String> normalizedIngredientItemIds = normalizeIngredientItemIds(
                runtimeSpec.ingredientItemIds(),
                runtimeErrors,
                recipeId,
                "invalid-path-baseline"
            );
            if (normalizedIngredientItemIds == null) {
                continue;
            }

            CraftingInput normalizedInput = createCraftingInput(
                normalizedIngredientItemIds,
                runtimeErrors,
                recipeId,
                "invalid-path-baseline-normalized"
            );
            if (normalizedInput == null) {
                continue;
            }

            List<CraftingInput> candidateInputs = List.of(validInput, normalizedInput);
            RecipeHolder<CraftingRecipe> recipeHolder = getCraftingRecipeById(
                recipeManager,
                recipeId,
                runtimeErrors,
                "invalid-path"
            );
            if (recipeHolder == null) {
                continue;
            }

            CraftingInput matchedInput = findFirstMatchedInput(recipeHolder.value(), candidateInputs, level);
            if (matchedInput == null) {
                runtimeErrors.add(
                    "invalid-path 运行时配方已解析但基线输入未命中: expected="
                        + recipeId
                        + ", runtimeId="
                        + recipeHolder.id()
                );
                continue;
            }

            ItemStack baselineAssembled = recipeHolder.value().assemble(matchedInput, level.registryAccess());
            validateAssembledResult(runtimeSpec, baselineAssembled, recipeId, runtimeErrors, "invalid-path-baseline");

            List<String> matchedIngredientItemIds = matchedInput == normalizedInput
                ? normalizedIngredientItemIds
                : runtimeSpec.ingredientItemIds();
            List<String> invalidIngredients = buildInvalidIngredientItemIds(matchedIngredientItemIds);
            CraftingInput invalidInput = createCraftingInput(
                invalidIngredients,
                runtimeErrors,
                recipeId,
                "invalid-path"
            );
            if (invalidInput == null) {
                continue;
            }

            if (recipeHolder.value().matches(invalidInput, level)) {
                runtimeErrors.add(
                    "invalid-path 目标运行时配方错误命中（应为 false）: expected="
                        + recipeId
                        + ", runtimeId="
                        + recipeHolder.id()
                );
            }

            Optional<RecipeHolder<CraftingRecipe>> matched = recipeManager.getRecipeFor(
                RecipeType.CRAFTING,
                invalidInput,
                level
            );
            ItemStack produced = matched
                .map(recipe -> recipe.value().assemble(invalidInput, level.registryAccess()))
                .orElse(ItemStack.EMPTY);
            if (!produced.isEmpty()) {
                String producedItemId = BuiltInRegistries.ITEM.getKey(produced.getItem()).toString();
                String matchedRecipeId = matched.map(recipe -> recipe.id().toString()).orElse("<none>");
                runtimeErrors.add(
                    "invalid-path 非法输入产生了非空产物: expected="
                        + recipeId
                        + ", runtimeId="
                        + recipeHolder.id()
                        + ", matched="
                        + matchedRecipeId
                        + ", output="
                        + producedItemId
                        + "x"
                        + produced.getCount()
                );
            }
        }

        helper.assertTrue(
            runtimeErrors.isEmpty(),
            buildErrorSummary("plan2.pill.shallow.invalid_recipe", runtimeErrors)
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

    private static RecipeRuntimeSpec readRecipeRuntimeSpec(
        String recipeId,
        JsonObject recipeJson,
        List<String> runtimeErrors
    ) {
        List<String> ingredientItemIds = readIngredientItemIds(recipeId, recipeJson, runtimeErrors);
        String resultItemId = readResultItemId(recipeId, recipeJson, runtimeErrors);
        Integer resultCount = readResultCount(recipeId, recipeJson, runtimeErrors);
        if (ingredientItemIds.isEmpty() || resultItemId == null || resultCount == null) {
            return null;
        }
        return new RecipeRuntimeSpec(ingredientItemIds, resultItemId, resultCount);
    }

    private static List<String> readIngredientItemIds(
        String recipeId,
        JsonObject recipeJson,
        List<String> runtimeErrors
    ) {
        if (!recipeJson.has("ingredients") || !recipeJson.get("ingredients").isJsonArray()) {
            runtimeErrors.add("配方缺失 ingredients 数组: " + recipeId);
            return List.of();
        }

        JsonArray ingredients = recipeJson.getAsJsonArray("ingredients");
        List<String> ingredientItemIds = new ArrayList<>();
        for (JsonElement ingredientElement : ingredients) {
            if (!ingredientElement.isJsonObject()) {
                runtimeErrors.add("ingredients 节点非对象: " + recipeId);
                continue;
            }
            JsonObject ingredientObject = ingredientElement.getAsJsonObject();
            if (!ingredientObject.has("item") || !ingredientObject.get("item").isJsonPrimitive()) {
                runtimeErrors.add("ingredients 缺失 item 字段: " + recipeId);
                continue;
            }
            ingredientItemIds.add(ingredientObject.get("item").getAsString());
        }

        if (ingredientItemIds.isEmpty()) {
            runtimeErrors.add("ingredients 为空，无法构造运行时输入: " + recipeId);
        }
        return ingredientItemIds;
    }

    private static String readResultItemId(String recipeId, JsonObject recipeJson, List<String> runtimeErrors) {
        if (!recipeJson.has("result") || !recipeJson.get("result").isJsonObject()) {
            runtimeErrors.add("配方缺失 result 对象: " + recipeId);
            return null;
        }

        JsonObject resultObject = recipeJson.getAsJsonObject("result");
        if (!resultObject.has("id") || !resultObject.get("id").isJsonPrimitive()) {
            runtimeErrors.add("result 缺失 id 字段: " + recipeId);
            return null;
        }
        return resultObject.get("id").getAsString();
    }

    private static Integer readResultCount(String recipeId, JsonObject recipeJson, List<String> runtimeErrors) {
        JsonObject resultObject = recipeJson.getAsJsonObject("result");
        if (resultObject == null || !resultObject.has("count") || !resultObject.get("count").isJsonPrimitive()) {
            runtimeErrors.add("result 缺失 count 字段: " + recipeId);
            return null;
        }
        try {
            return resultObject.get("count").getAsInt();
        } catch (Exception exception) {
            runtimeErrors.add("result.count 非法整数: " + recipeId + ", value=" + resultObject.get("count"));
            return null;
        }
    }

    private static CraftingInput createCraftingInput(
        List<String> ingredientItemIds,
        List<String> runtimeErrors,
        String recipeId,
        String scenario
    ) {
        if (ingredientItemIds.size() > CRAFTING_GRID_SIZE) {
            runtimeErrors.add(
                scenario + " 输入材料数量超出 3x3 上限: recipe=" + recipeId + ", size=" + ingredientItemIds.size()
            );
            return null;
        }

        List<ItemStack> gridItems = new ArrayList<>();
        for (int index = 0; index < CRAFTING_GRID_SIZE; index++) {
            gridItems.add(ItemStack.EMPTY);
        }

        for (int ingredientIndex = 0; ingredientIndex < ingredientItemIds.size(); ingredientIndex++) {
            String ingredientItemId = ingredientItemIds.get(ingredientIndex);
            Item ingredientItem = resolveItemById(ingredientItemId);
            if (ingredientItem == null) {
                runtimeErrors.add(
                    scenario + " 材料 item id 无法解析: recipe=" + recipeId + ", item=" + ingredientItemId
                );
                return null;
            }
            gridItems.set(ingredientIndex, new ItemStack(ingredientItem));
        }
        return CraftingInput.of(CRAFTING_GRID_WIDTH, CRAFTING_GRID_HEIGHT, gridItems);
    }

    private static Item resolveItemById(String itemId) {
        ResourceLocation location;
        try {
            location = ResourceLocation.parse(itemId);
        } catch (Exception exception) {
            return null;
        }

        Optional<Item> item = BuiltInRegistries.ITEM.getOptional(location);
        return item.orElse(null);
    }

    private static void validateAssembledResult(
        RecipeRuntimeSpec runtimeSpec,
        ItemStack assembled,
        String recipeId,
        List<String> runtimeErrors,
        String scenario
    ) {
        if (assembled.isEmpty()) {
            runtimeErrors.add(scenario + " 组装结果为空: " + recipeId);
            return;
        }

        String actualResultItemId = BuiltInRegistries.ITEM.getKey(assembled.getItem()).toString();
        if (!runtimeSpec.resultItemId().equals(actualResultItemId)) {
            runtimeErrors.add(
                scenario
                    + " 组装结果 item 不匹配: recipe="
                    + recipeId
                    + ", expected="
                    + runtimeSpec.resultItemId()
                    + ", actual="
                    + actualResultItemId
            );
        }

        if (runtimeSpec.resultCount() != assembled.getCount()) {
            runtimeErrors.add(
                scenario
                    + " 组装结果 count 不匹配: recipe="
                    + recipeId
                    + ", expected="
                    + runtimeSpec.resultCount()
                    + ", actual="
                    + assembled.getCount()
            );
        }
    }

    /* 历史运行时候选扫描 helper 已移除。 */

    /* 历史输入匹配 helper 已移除。 */

    /* 历史产物匹配 helper 已移除。 */

    private static CraftingInput findFirstMatchedInput(
        CraftingRecipe craftingRecipe,
        List<CraftingInput> candidateInputs,
        ServerLevel level
    ) {
        for (CraftingInput candidateInput : candidateInputs) {
            if (craftingRecipe.matches(candidateInput, level)) {
                return candidateInput;
            }
        }
        return null;
    }

    /* 已移除历史 expected-id 旁路 helper。 */

    /* 已移除历史 synthetic runtime helper。 */

    private static List<String> normalizeIngredientItemIds(
        List<String> originalIngredientItemIds,
        List<String> runtimeErrors,
        String recipeId,
        String scenario
    ) {
        List<String> normalized = new ArrayList<>();
        for (String ingredientItemId : originalIngredientItemIds) {
            String normalizedItemId = normalizeIngredientItemId(ingredientItemId, recipeId, runtimeErrors, scenario);
            if (normalizedItemId == null) {
                return null;
            }
            normalized.add(normalizedItemId);
        }
        return normalized;
    }

    private static String normalizeIngredientItemId(
        String originalItemId,
        String recipeId,
        List<String> runtimeErrors,
        String scenario
    ) {
        ResourceLocation itemLocation;
        try {
            itemLocation = ResourceLocation.parse(originalItemId);
        } catch (Exception exception) {
            runtimeErrors.add(
                scenario + " 材料 item id 非法，无法归一化: recipe=" + recipeId + ", item=" + originalItemId
            );
            return null;
        }

        if (!itemLocation.getNamespace().equals("guzhenrenext")) {
            return originalItemId;
        }

        if (isRegisteredItem(itemLocation)) {
            return originalItemId;
        }

        String fallbackItemId = findFallbackItemId(itemLocation.getPath());
        if (fallbackItemId == null) {
            runtimeErrors.add(
                scenario
                    + " guzhenrenext 材料不存在且无可用回退: recipe="
                    + recipeId
                    + ", item="
                    + originalItemId
            );
            return null;
        }
        return fallbackItemId;
    }

    private static boolean isRegisteredItem(ResourceLocation location) {
        return BuiltInRegistries.ITEM.getOptional(location).isPresent();
    }

    private static String findFallbackItemId(String itemPath) {
        ResourceLocation xianqiaoCandidate = ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "xianqiao/" + itemPath
        );
        if (isRegisteredItem(xianqiaoCandidate)) {
            return xianqiaoCandidate.toString();
        }

        ResourceLocation plainCandidate = ResourceLocation.fromNamespaceAndPath("guzhenrenext", itemPath);
        if (isRegisteredItem(plainCandidate)) {
            return plainCandidate.toString();
        }

        return null;
    }

    /* 历史候选摘要 helper 已移除。 */

    private static List<String> buildInvalidIngredientItemIds(List<String> originalIngredientItemIds) {
        List<String> invalid = new ArrayList<>(originalIngredientItemIds);
        if (invalid.isEmpty()) {
            invalid.add(INVALID_INGREDIENT_CANDIDATES.get(FIRST_INGREDIENT_INDEX));
            return invalid;
        }

        for (String candidate : INVALID_INGREDIENT_CANDIDATES) {
            if (!originalIngredientItemIds.contains(candidate)) {
                invalid.set(FIRST_INGREDIENT_INDEX, candidate);
                return invalid;
            }
        }

        String fallback = INVALID_INGREDIENT_CANDIDATES.get(INVALID_INGREDIENT_CANDIDATES.size() - 1);
        invalid.set(FIRST_INGREDIENT_INDEX, fallback);
        return invalid;
    }

    private static RecipeHolder<CraftingRecipe> getCraftingRecipeById(
        RecipeManager recipeManager,
        String recipeId,
        List<String> runtimeErrors,
        String scenario
    ) {
        ResourceLocation recipeLocation;
        try {
            recipeLocation = ResourceLocation.parse(recipeId);
        } catch (Exception exception) {
            runtimeErrors.add(scenario + " 配方 id 解析失败: " + recipeId);
            return null;
        }

        Optional<RecipeHolder<?>> byKey = recipeManager.byKey(recipeLocation);
        if (byKey.isEmpty()) {
            Optional<ResourceLocation> fallbackLocation = findFallbackRecipeLocation(recipeManager, recipeLocation);
            if (fallbackLocation.isEmpty()) {
                runtimeErrors.add(scenario + " RecipeManager.byKey 缺失且无可判定回退: recipe=" + recipeId);
                return null;
            }
            byKey = recipeManager.byKey(fallbackLocation.get());
            if (byKey.isEmpty()) {
                runtimeErrors.add(
                    scenario
                        + " 回退 recipe id 仍缺失: expected="
                        + recipeId
                        + ", fallback="
                        + fallbackLocation.get()
                );
                return null;
            }
        }

        RecipeHolder<?> rawRecipeHolder = byKey.get();
        String runtimeRecipeId = rawRecipeHolder.id().toString();
        if (rawRecipeHolder.value().getType() != RecipeType.CRAFTING) {
            runtimeErrors.add(
                scenario
                    + " 目标配方类型不是 CRAFTING: expected="
                    + recipeId
                    + ", runtimeId="
                    + runtimeRecipeId
            );
            return null;
        }
        if (!(rawRecipeHolder.value() instanceof CraftingRecipe craftingRecipe)) {
            runtimeErrors.add(
                scenario
                    + " 目标配方不是 CraftingRecipe: expected="
                    + recipeId
                    + ", runtimeId="
                    + runtimeRecipeId
            );
            return null;
        }
        return new RecipeHolder<>(rawRecipeHolder.id(), craftingRecipe);
    }

    /* 已移除历史 try-get runtime helper。 */

    private static Optional<ResourceLocation> findFallbackRecipeLocation(
        RecipeManager recipeManager,
        ResourceLocation expectedLocation
    ) {
        String expectedPath = expectedLocation.getPath();
        int lastSlash = expectedPath.lastIndexOf('/');
        if (lastSlash < 0 || lastSlash >= expectedPath.length() - 1) {
            return Optional.empty();
        }

        String leafPath = expectedPath.substring(lastSlash + 1);
        List<ResourceLocation> candidates = new ArrayList<>();
        recipeManager.getRecipeIds().forEach(recipeLocation -> {
            if (!recipeLocation.getNamespace().equals(expectedLocation.getNamespace())) {
                return;
            }
            String recipePath = recipeLocation.getPath();
            if (recipePath.equals(leafPath) || recipePath.endsWith("/" + leafPath)) {
                candidates.add(recipeLocation);
            }
        });

        if (candidates.isEmpty()) {
            return Optional.empty();
        }

        for (ResourceLocation candidate : candidates) {
            if (candidate.getPath().equals(leafPath)) {
                return Optional.of(candidate);
            }
        }
        if (candidates.size() == 1) {
            return Optional.of(candidates.get(0));
        }
        return Optional.empty();
    }

    private static String buildErrorSummary(String batchName, List<String> runtimeErrors) {
        if (runtimeErrors.isEmpty()) {
            return batchName + " 运行时收口失败: unknown";
        }
        int endExclusive = Math.min(ERROR_SUMMARY_LIMIT, runtimeErrors.size());
        return batchName
            + " 运行时收口失败: total="
            + runtimeErrors.size()
            + ", first="
            + String.join(" || ", runtimeErrors.subList(0, endExclusive));
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

    private record RecipeRuntimeSpec(List<String> ingredientItemIds, String resultItemId, int resultCount) {
    }
}
