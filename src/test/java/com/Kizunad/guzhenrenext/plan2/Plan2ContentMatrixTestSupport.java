package com.Kizunad.guzhenrenext.plan2;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Plan2ContentMatrixTestSupport {

    static final String PLAN_FILE =
        ".sisyphus/plans/xianqiao-plan2-four-category-120-expansion.md";

    static final String MATRIX_FILE =
        "src/main/resources/data/guzhenrenext/plan2/content_matrix.json";

    private static final Pattern PLAN_ENTRY_PATTERN = Pattern.compile(
        "^-\\s+((?:C|P|D|M)-(?:S|D)\\d{2})\\s+[^（]+（`[^`]+`）—\\s*.*$"
    );

    private static final Pattern STRUCTURED_PATTERN =
        Pattern.compile("^\\s*-\\s*结构化落地要求：.*$");

    private static final Map<String, Integer> CATEGORY_ORDER = Map.of(
        "creature", 0,
        "plant", 1,
        "pill", 2,
        "material", 3
    );

    private static final Map<String, Integer> DEPTH_ORDER = Map.of(
        "shallow", 0,
        "deep", 1
    );

    private static final Map<String, Integer> ANCHOR_ORDER = Map.of(
        "code", 0,
        "data", 1,
        "plan", 2,
        "item", 3,
        "system", 4
    );

    private static final Map<String, Integer> TARGET_ORDER = Map.of(
        "item", 0,
        "system", 1
    );

    private Plan2ContentMatrixTestSupport() {
    }

    static String readMatrixText() throws IOException {
        return Files.readString(Paths.get(MATRIX_FILE), StandardCharsets.UTF_8);
    }

    static JsonArray readMatrixArray() throws IOException {
        return JsonParser.parseString(readMatrixText()).getAsJsonArray();
    }

    static Set<String> extractPlanIds() throws IOException {
        Set<String> ids = new HashSet<>();
        for (String line : Files.readAllLines(Path.of(PLAN_FILE), StandardCharsets.UTF_8)) {
            Matcher matcher = PLAN_ENTRY_PATTERN.matcher(line);
            if (matcher.matches()) {
                ids.add(matcher.group(1));
            }
        }
        return ids;
    }

    static Map<String, Boolean> extractPlanStructuredFlags() throws IOException {
        Map<String, Boolean> flags = new HashMap<>();
        List<String> lines = Files.readAllLines(Path.of(PLAN_FILE), StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            Matcher matcher = PLAN_ENTRY_PATTERN.matcher(lines.get(i));
            if (!matcher.matches()) {
                continue;
            }
            String id = matcher.group(1);
            boolean hasStructured = false;
            int max = Math.min(lines.size(), i + 8);
            for (int j = i + 1; j < max; j++) {
                if (STRUCTURED_PATTERN.matcher(lines.get(j)).matches()) {
                    hasStructured = true;
                    break;
                }
            }
            flags.put(id, hasStructured);
        }
        return flags;
    }

    static void assertNoDuplicateIds(JsonArray array) {
        Set<String> ids = new HashSet<>();
        List<String> duplicates = new ArrayList<>();
        for (JsonElement element : array) {
            String id = element.getAsJsonObject().get("id").getAsString();
            if (!ids.add(id)) {
                duplicates.add(id);
            }
        }
        if (!duplicates.isEmpty()) {
            throw new IllegalStateException("duplicate ids: " + duplicates);
        }
    }

    static String canonicalize(JsonArray source) throws IOException {
        List<JsonObject> entries = new ArrayList<>();
        for (JsonElement element : source) {
            entries.add(element.getAsJsonObject());
        }
        entries.sort(
            Comparator
                .comparing((JsonObject object) -> CATEGORY_ORDER.get(object.get("category").getAsString()))
                .thenComparing(object -> DEPTH_ORDER.get(object.get("depth").getAsString()))
                .thenComparing(object -> naturalIdOrder(object.get("id").getAsString()))
        );

        StringWriter output = new StringWriter();
        JsonWriter writer = new JsonWriter(output);
        writer.setIndent("  ");
        writer.beginArray();
        for (JsonObject entry : entries) {
            writeEntry(writer, entry);
        }
        writer.endArray();
        writer.close();
        return output + "\n";
    }

    private static int naturalIdOrder(String id) {
        char depthFlag = id.charAt(2);
        int depthRank = depthFlag == 'S' ? 0 : 1;
        int number = Integer.parseInt(id.substring(3));
        return depthRank * 100 + number;
    }

    private static void writeEntry(JsonWriter writer, JsonObject entry) throws IOException {
        writer.beginObject();
        writeStringField(writer, entry, "id");
        writeStringField(writer, entry, "category");
        writeStringField(writer, entry, "depth");
        writeStringField(writer, entry, "assetReuse");
        writeStringField(writer, entry, "mechanic");
        writeStringField(writer, entry, "cost");
        writeStringArrayField(writer, entry.getAsJsonArray("linkPoints"), "linkPoints");
        writeSource(writer, entry.getAsJsonObject("mainSource"), "mainSource");
        writeSource(writer, entry.getAsJsonObject("backupSource"), "backupSource");
        writePreconditions(writer, entry.getAsJsonArray("preconditions"));
        writePrimaryUse(writer, entry.getAsJsonObject("primaryUse"));
        writeRisk(writer, entry.getAsJsonObject("risk"));
        writer.endObject();
    }

    private static void writeStringField(JsonWriter writer, JsonObject source, String field)
        throws IOException {
        writer.name(field).value(source.get(field).getAsString());
    }

    private static void writeStringArrayField(JsonWriter writer, JsonArray array, String field)
        throws IOException {
        writer.name(field);
        writer.beginArray();
        for (JsonElement element : array) {
            writer.value(element.getAsString());
        }
        writer.endArray();
    }

    private static void writeSource(JsonWriter writer, JsonObject source, String field)
        throws IOException {
        writer.name(field);
        writer.beginObject();
        writer.name("type").value(source.get("type").getAsString());
        writer.name("summary").value(source.get("summary").getAsString());
        writer.name("anchors");
        writer.beginArray();
        List<String> anchors = toStringList(source.getAsJsonArray("anchors"));
        sortByPrefixPriority(anchors, ANCHOR_ORDER);
        for (String anchor : anchors) {
            writer.value(anchor);
        }
        writer.endArray();
        writer.endObject();
    }

    private static void writePreconditions(JsonWriter writer, JsonArray array) throws IOException {
        writer.name("preconditions");
        writer.beginArray();
        for (JsonElement element : array) {
            JsonObject object = element.getAsJsonObject();
            writer.beginObject();
            writer.name("type").value(object.get("type").getAsString());
            writer.name("summary").value(object.get("summary").getAsString());
            writer.endObject();
        }
        writer.endArray();
    }

    private static void writePrimaryUse(JsonWriter writer, JsonObject primaryUse) throws IOException {
        writer.name("primaryUse");
        writer.beginObject();
        writer.name("type").value(primaryUse.get("type").getAsString());
        writer.name("summary").value(primaryUse.get("summary").getAsString());
        writer.name("targets");
        writer.beginArray();
        List<String> targets = toStringList(primaryUse.getAsJsonArray("targets"));
        sortByPrefixPriority(targets, TARGET_ORDER);
        for (String target : targets) {
            writer.value(target);
        }
        writer.endArray();
        writer.endObject();
    }

    private static void writeRisk(JsonWriter writer, JsonObject risk) throws IOException {
        writer.name("risk");
        writer.beginObject();
        writer.name("type").value(risk.get("type").getAsString());
        writer.name("summary").value(risk.get("summary").getAsString());
        writer.name("severity").value(risk.get("severity").getAsString());
        writer.endObject();
    }

    private static List<String> toStringList(JsonArray array) {
        List<String> values = new ArrayList<>();
        for (JsonElement element : array) {
            values.add(element.getAsString());
        }
        return values;
    }

    private static void sortByPrefixPriority(List<String> values, Map<String, Integer> priority) {
        Collections.sort(values, (left, right) -> {
            String leftPrefix = prefix(left);
            String rightPrefix = prefix(right);
            int leftRank = priority.getOrDefault(leftPrefix, Integer.MAX_VALUE);
            int rightRank = priority.getOrDefault(rightPrefix, Integer.MAX_VALUE);
            if (leftRank != rightRank) {
                return Integer.compare(leftRank, rightRank);
            }
            return left.compareTo(right);
        });
    }

    private static String prefix(String value) {
        int idx = value.indexOf(':');
        return idx < 0 ? value : value.substring(0, idx);
    }
}
