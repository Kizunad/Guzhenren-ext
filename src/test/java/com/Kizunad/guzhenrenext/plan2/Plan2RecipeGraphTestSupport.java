package com.Kizunad.guzhenrenext.plan2;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Plan2RecipeGraphTestSupport {

    // given: Task32 首切片边界只读取静态 recipes/task13a（复数目录）。
    // given: runtime 的 recipe/task13a（单数目录）镜像不在本校验读取范围。
    static final String PLAN2_MATRIX_FILE =
        "src/main/resources/data/guzhenrenext/plan2/content_matrix.json";
    static final String TASK13A_STATIC_RECIPE_DIR =
        "src/main/resources/data/guzhenrenext/recipes/task13a";

    static final String TASK13A_RECIPE_NAMESPACE_PREFIX = "guzhenrenext:task13a/";

    private static final String JSON_FILE_SUFFIX = ".json";
    private static final Pattern ITEM_REFERENCE_PATTERN = Pattern.compile(
        "^item:((?:C|P|D|M)-(?:S|D)\\d{2})$"
    );

    private Plan2RecipeGraphTestSupport() {
    }

    static JsonArray readCurrentMatrixArray() throws IOException {
        String text = Files.readString(Path.of(PLAN2_MATRIX_FILE), StandardCharsets.UTF_8);
        return JsonParser.parseString(text).getAsJsonArray();
    }

    static Map<String, JsonObject> readCurrentTask13ARecipeGraph() throws IOException {
        Map<String, JsonObject> result = new LinkedHashMap<>();
        Path recipeDir = Path.of(TASK13A_STATIC_RECIPE_DIR);
        try (var stream = Files.list(recipeDir)) {
            List<Path> files = stream
                .filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(JSON_FILE_SUFFIX))
                .sorted()
                .toList();
            for (Path file : files) {
                String fileName = file.getFileName().toString();
                String recipePathId = fileName.substring(0, fileName.length() - JSON_FILE_SUFFIX.length());
                String recipeId = TASK13A_RECIPE_NAMESPACE_PREFIX + recipePathId;
                String jsonText = Files.readString(file, StandardCharsets.UTF_8);
                result.put(recipeId, JsonParser.parseString(jsonText).getAsJsonObject());
            }
        }
        return result;
    }

    static JsonArray deepCopyMatrix(JsonArray source) {
        return JsonParser.parseString(source.toString()).getAsJsonArray();
    }

    static Map<String, JsonObject> deepCopyRecipeGraph(Map<String, JsonObject> source) {
        Map<String, JsonObject> copy = new LinkedHashMap<>();
        for (Map.Entry<String, JsonObject> entry : source.entrySet()) {
            copy.put(entry.getKey(), entry.getValue().deepCopy());
        }
        return copy;
    }

    static JsonObject findMatrixEntryById(JsonArray matrixArray, String entryId) {
        for (JsonElement element : matrixArray) {
            JsonObject entry = element.getAsJsonObject();
            if (entryId.equals(readStringOrNull(entry, "id"))) {
                return entry;
            }
        }
        throw new IllegalStateException("找不到矩阵条目: " + entryId);
    }

    static String extractItemReferenceId(String rawValue) {
        Matcher matcher = ITEM_REFERENCE_PATTERN.matcher(rawValue);
        if (!matcher.matches()) {
            return null;
        }
        return matcher.group(1);
    }

    static List<String> readStringArray(JsonObject parent, String fieldName) {
        List<String> values = new ArrayList<>();
        if (parent == null || !parent.has(fieldName) || !parent.get(fieldName).isJsonArray()) {
            return values;
        }
        JsonArray array = parent.getAsJsonArray(fieldName);
        for (JsonElement element : array) {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                values.add(element.getAsString());
            }
        }
        return values;
    }

    static String readStringOrNull(JsonObject object, String fieldName) {
        if (object == null || !object.has(fieldName) || !object.get(fieldName).isJsonPrimitive()) {
            return null;
        }
        if (!object.get(fieldName).getAsJsonPrimitive().isString()) {
            return null;
        }
        return object.get(fieldName).getAsString();
    }
}
