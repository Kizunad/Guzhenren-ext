package com.Kizunad.guzhenrenext.plan2;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Plan2ContentMatrixValidator {

    private static final Pattern PLAN_ENTRY_PATTERN = Pattern.compile(
        "^-\\s+((?:C|P|D|M)-(?:S|D)\\d{2})\\s+[^（]+（`[^`]+`）—\\s*.*$"
    );

    private static final Pattern STRUCTURED_PATTERN = Pattern.compile(
        "^\\s*-\\s*结构化落地要求：.*$"
    );

    private static final List<String> ENTRY_FIELDS = List.of(
        "id",
        "category",
        "depth",
        "assetReuse",
        "mechanic",
        "cost",
        "linkPoints",
        "mainSource",
        "backupSource",
        "preconditions",
        "primaryUse",
        "risk"
    );

    private static final List<String> SOURCE_FIELDS = List.of("type", "summary", "anchors");
    private static final List<String> PRECONDITION_FIELDS = List.of("type", "summary");
    private static final List<String> PRIMARY_USE_FIELDS = List.of("type", "summary", "targets");
    private static final List<String> RISK_FIELDS = List.of("type", "summary", "severity");

    private static final List<String> CATEGORY_ORDER = List.of("creature", "plant", "pill", "material");
    private static final List<String> DEPTH_ORDER = List.of("shallow", "deep");

    private static final Set<String> LINK_POINTS = Set.of(
        "alchemy",
        "farming",
        "spirit",
        "material_flow",
        "daomark",
        "tribulation",
        "timeflow",
        "soul",
        "zhenyuan",
        "nianTou",
        "qiyun",
        "breakthrough",
        "aperture_environment",
        "mutation",
        "sealing",
        "infrastructure"
    );

    private static final Set<String> SOURCE_TYPES = Set.of(
        "natural_spawn",
        "breed",
        "plant",
        "craft",
        "loot",
        "event",
        "mine",
        "refine",
        "recycle",
        "ritual",
        "synthesis"
    );

    private static final Set<String> PRECONDITION_TYPES = Set.of(
        "environment",
        "unlock",
        "structure",
        "material",
        "combat_power",
        "system_state"
    );

    private static final Set<String> PRIMARY_USE_TYPES = Set.of(
        "recipe_input",
        "combat_consumable",
        "growth_accelerator",
        "progression_gate",
        "infrastructure",
        "endgame_core"
    );

    private static final Set<String> RISK_TYPES = Set.of(
        "resource_drain",
        "status_penalty",
        "environment_damage",
        "disaster_escalation",
        "irreversible",
        "production_stall"
    );

    private static final Set<String> SEVERITIES = Set.of("low", "medium", "high", "critical");

    private static final Map<String, Set<String>> CATEGORY_MAIN_SOURCE_RULES = Map.of(
        "creature", Set.of("natural_spawn", "breed", "event", "ritual"),
        "plant", Set.of("plant", "event", "ritual", "synthesis"),
        "pill", Set.of("craft", "refine", "loot", "event", "ritual"),
        "material", Set.of("mine", "loot", "refine", "recycle", "event", "ritual", "synthesis")
    );

    private static final Set<String> HEAVY_LINK_POINTS = Set.of(
        "tribulation",
        "timeflow",
        "breakthrough",
        "daomark",
        "mutation",
        "sealing"
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

    private Plan2ContentMatrixValidator() {
    }

    static List<String> validateCurrentMatrix() throws IOException {
        String matrixText = Plan2ContentMatrixTestSupport.readMatrixText();
        JsonArray matrixArray = Plan2ContentMatrixTestSupport.readMatrixArray();
        return validate(matrixText, matrixArray);
    }

    static List<String> validate(String matrixText, JsonArray matrixArray) throws IOException {
        List<String> errors = new ArrayList<>();
        List<String> planLines = Files.readAllLines(Path.of(Plan2ContentMatrixTestSupport.PLAN_FILE), StandardCharsets.UTF_8);
        PlanSnapshot plan = parsePlanSnapshot(planLines, errors);

        validateCoverage(plan, matrixArray, errors);
        validateOrderingAndFormat(matrixText, matrixArray, errors);
        validateEachEntry(plan, matrixArray, errors);

        return errors;
    }

    private static PlanSnapshot parsePlanSnapshot(List<String> lines, List<String> errors) {
        Map<String, PlanExtract> extracts = new LinkedHashMap<>();
        Map<String, StructuredExpect> structured = new LinkedHashMap<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Matcher matcher = PLAN_ENTRY_PATTERN.matcher(line);
            if (!matcher.matches()) {
                continue;
            }
            String id = matcher.group(1);
            try {
                extracts.put(id, parseEntryLine(id, line));
            } catch (IllegalArgumentException ex) {
                errors.add("mechanic/cost extraction mismatch: " + id + " | " + ex.getMessage());
            }

            String structuredLine = findStructuredLine(lines, i + 1, 8);
            if (structuredLine != null) {
                structured.put(id, parseStructuredExpect(id, structuredLine, errors));
            }
        }

        return new PlanSnapshot(extracts, structured);
    }

    private static String findStructuredLine(List<String> lines, int start, int maxScan) {
        int end = Math.min(lines.size(), start + maxScan);
        for (int i = start; i < end; i++) {
            if (STRUCTURED_PATTERN.matcher(lines.get(i)).matches()) {
                return lines.get(i);
            }
        }
        return null;
    }

    private static PlanExtract parseEntryLine(String id, String line) {
        int dashIndex = line.indexOf("—");
        int sourceIndex = line.indexOf("；主来源：");
        int backupIndex = line.indexOf("；备用来源：");
        int preconditionIndex = line.indexOf("；前置条件：");
        int useIndex = line.indexOf("；主要用途：");
        int riskIndex = line.indexOf("；风险：");

        if (dashIndex < 0 || sourceIndex < 0 || backupIndex < 0 || preconditionIndex < 0 || useIndex < 0 || riskIndex < 0) {
            throw new IllegalArgumentException("正文子句缺失");
        }

        String mechanic;
        String cost;
        int costIndex = line.indexOf("；代价：");
        if (costIndex >= 0 && costIndex < sourceIndex) {
            mechanic = cleanText(line.substring(dashIndex + 1, costIndex));
            cost = cleanText(line.substring(costIndex + "；代价：".length(), sourceIndex));
        } else {
            mechanic = cleanText(line.substring(dashIndex + 1, sourceIndex));
            cost = null;
        }

        String mainSourceSummary = cleanText(line.substring(sourceIndex + "；主来源：".length(), backupIndex));
        String backupSourceSummary = cleanText(line.substring(backupIndex + "；备用来源：".length(), preconditionIndex));
        String preconditionsRaw = cleanText(line.substring(preconditionIndex + "；前置条件：".length(), useIndex));
        String primaryUseSummary = cleanText(line.substring(useIndex + "；主要用途：".length(), riskIndex));
        String riskSummary = cleanText(line.substring(riskIndex + "；风险：".length()));
        if (cost == null) {
            cost = riskSummary;
        }

        String category = inferCategoryById(id);
        List<String> preconditions = splitPreconditions(preconditionsRaw);

        return new PlanExtract(
            id,
            category,
            mechanic,
            cost,
            mainSourceSummary,
            backupSourceSummary,
            preconditions,
            primaryUseSummary,
            riskSummary
        );
    }

    private static StructuredExpect parseStructuredExpect(String id, String line, List<String> errors) {
        try {
            return new StructuredExpect(
                extractStringList(line, "linkPoints"),
                extractEnum(line, "mainSource.type"),
                extractEnum(line, "backupSource.type"),
                extractEnum(line, "primaryUse.type"),
                extractEnum(line, "risk.type"),
                extractEnum(line, "risk.severity")
            );
        } catch (IllegalArgumentException ex) {
            errors.add("plan/json coverage mismatch: " + id + " 结构化落地要求解析失败 | " + ex.getMessage());
            return StructuredExpect.empty();
        }
    }

    private static String extractEnum(String line, String key) {
        Pattern p = Pattern.compile(Pattern.quote(key) + "=([a-zA-Z_]+)");
        Matcher matcher = p.matcher(line);
        if (!matcher.find()) {
            throw new IllegalArgumentException("缺少 " + key);
        }
        return matcher.group(1);
    }

    private static List<String> extractStringList(String line, String key) {
        Pattern p = Pattern.compile(Pattern.quote(key) + "=\\[(.*?)\\]");
        Matcher matcher = p.matcher(line);
        if (!matcher.find()) {
            throw new IllegalArgumentException("缺少 " + key);
        }
        String body = matcher.group(1).trim();
        if (body.isEmpty()) {
            return Collections.emptyList();
        }
        String[] parts = body.split(",");
        List<String> result = new ArrayList<>();
        for (String part : parts) {
            String value = cleanText(part);
            if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                value = value.substring(1, value.length() - 1);
            }
            result.add(value);
        }
        return result;
    }

    private static void validateCoverage(PlanSnapshot plan, JsonArray matrixArray, List<String> errors) {
        Set<String> planIds = plan.extracts.keySet();
        Set<String> jsonIds = new HashSet<>();
        for (JsonElement element : matrixArray) {
            JsonObject entry = element.getAsJsonObject();
            jsonIds.add(getString(entry, "id"));
        }

        if (!planIds.equals(jsonIds)) {
            Set<String> missingInJson = new HashSet<>(planIds);
            missingInJson.removeAll(jsonIds);
            Set<String> extraInJson = new HashSet<>(jsonIds);
            extraInJson.removeAll(planIds);
            errors.add(
                "plan/json coverage mismatch: missingInJson=" + missingInJson + ", extraInJson=" + extraInJson
            );
        }

        for (String id : planIds) {
            if (!plan.structured.containsKey(id)) {
                errors.add("plan/json coverage mismatch: 缺少结构化落地要求 " + id);
            }
        }
    }

    private static void validateOrderingAndFormat(String matrixText, JsonArray matrixArray, List<String> errors) {
        if (matrixText.startsWith("\uFEFF")) {
            errors.add("json ordering mismatch: 文件包含 BOM");
        }
        if (matrixText.contains("\t")) {
            errors.add("json ordering mismatch: 缩进包含 tab");
        }
        if (!matrixText.endsWith("\n")) {
            errors.add("json ordering mismatch: 文件末尾缺少换行");
        }

        try {
            String canonical = Plan2ContentMatrixTestSupport.canonicalize(matrixArray);
            if (!Objects.equals(canonical, matrixText)) {
                errors.add("json ordering mismatch: 内容未满足稳定序列化契约");
            }
        } catch (IOException ex) {
            errors.add("json ordering mismatch: 无法执行 canonicalize | " + ex.getMessage());
        }

        validateTopLevelArrayOrder(matrixArray, errors);
    }

    private static void validateEachEntry(PlanSnapshot plan, JsonArray matrixArray, List<String> errors) {
        for (JsonElement element : matrixArray) {
            JsonObject entry = element.getAsJsonObject();
            String id = getString(entry, "id");
            String category = getString(entry, "category");
            String depth = getString(entry, "depth");

            PlanExtract extract = plan.extracts.get(id);
            StructuredExpect expected = plan.structured.get(id);

            validateTopLevelOrdering(entry, errors);
            validateSchemaAndEnums(id, entry, errors);
            validateDeepShallowContract(id, depth, entry, errors);
            validateCategoryEnumCombination(id, category, depth, entry, errors);
            validateAnchorTargetSeverityContract(id, depth, entry, errors);

            if (extract != null) {
                validateClauseExtraction(id, entry, extract, errors);
            }
            if (expected != null) {
                validateStructuredConsistency(id, entry, expected, errors);
            }
        }
    }

    private static void validateTopLevelOrdering(JsonObject entry, List<String> errors) {
        List<String> keys = new ArrayList<>(entry.keySet());
        if (!ENTRY_FIELDS.equals(keys)) {
            errors.add("json ordering mismatch: " + getString(entry, "id") + " 顶层字段顺序不合法 -> " + keys);
        }
    }

    private static void validateTopLevelArrayOrder(JsonArray matrixArray, List<String> errors) {
        int previousCategoryRank = -1;
        int previousDepthRank = -1;
        int previousIdOrder = -1;
        for (int i = 0; i < matrixArray.size(); i++) {
            JsonObject entry = matrixArray.get(i).getAsJsonObject();
            String id = getString(entry, "id");
            String category = getString(entry, "category");
            String depth = getString(entry, "depth");

            int categoryRank = CATEGORY_ORDER.indexOf(category);
            int depthRank = DEPTH_ORDER.indexOf(depth);
            int idOrder = naturalIdOrder(id);

            boolean orderBroken = categoryRank < previousCategoryRank
                || (categoryRank == previousCategoryRank && depthRank < previousDepthRank)
                || (categoryRank == previousCategoryRank
                    && depthRank == previousDepthRank
                    && idOrder < previousIdOrder);

            if (orderBroken) {
                errors.add("json ordering mismatch: 顶层数组顺序错误 index=" + i + " id=" + id);
                return;
            }

            previousCategoryRank = categoryRank;
            previousDepthRank = depthRank;
            previousIdOrder = idOrder;
        }
    }

    private static void validateSchemaAndEnums(String id, JsonObject entry, List<String> errors) {
        String category = getString(entry, "category");
        String depth = getString(entry, "depth");
        if (!CATEGORY_ORDER.contains(category)) {
            errors.add("invalid source enum combination: " + id + " 非法 category=" + category);
        }
        if (!DEPTH_ORDER.contains(depth)) {
            errors.add("invalid source enum combination: " + id + " 非法 depth=" + depth);
        }

        assertNonBlank(id, "assetReuse", getString(entry, "assetReuse"), errors);
        assertNonBlank(id, "mechanic", getString(entry, "mechanic"), errors);
        assertNonBlank(id, "cost", getString(entry, "cost"), errors);

        JsonArray linkPoints = getArray(entry, "linkPoints");
        if (linkPoints.isEmpty()) {
            errors.add("invalid severity/anchor-target contract: " + id + " linkPoints 不能为空");
        }
        for (JsonElement linkPointElement : linkPoints) {
            String linkPoint = linkPointElement.getAsString();
            if (!LINK_POINTS.contains(linkPoint)) {
                errors.add("invalid severity/anchor-target contract: " + id + " 非法 linkPoints=" + linkPoint);
            }
        }

        JsonObject mainSource = getObject(entry, "mainSource");
        JsonObject backupSource = getObject(entry, "backupSource");
        JsonObject primaryUse = getObject(entry, "primaryUse");
        JsonObject risk = getObject(entry, "risk");
        JsonArray preconditions = getArray(entry, "preconditions");

        validateObjectKeyOrder(id, "mainSource", mainSource, SOURCE_FIELDS, errors);
        validateObjectKeyOrder(id, "backupSource", backupSource, SOURCE_FIELDS, errors);
        validateObjectKeyOrder(id, "primaryUse", primaryUse, PRIMARY_USE_FIELDS, errors);
        validateObjectKeyOrder(id, "risk", risk, RISK_FIELDS, errors);

        String mainType = getString(mainSource, "type");
        String backupType = getString(backupSource, "type");
        String useType = getString(primaryUse, "type");
        String riskType = getString(risk, "type");
        String severity = getString(risk, "severity");

        if (!SOURCE_TYPES.contains(mainType) || !SOURCE_TYPES.contains(backupType)) {
            errors.add("invalid source enum combination: " + id + " source.type 非法");
        }
        if (!PRIMARY_USE_TYPES.contains(useType)) {
            errors.add("invalid source enum combination: " + id + " primaryUse.type 非法=" + useType);
        }
        if (!RISK_TYPES.contains(riskType)) {
            errors.add("invalid source enum combination: " + id + " risk.type 非法=" + riskType);
        }
        if (!SEVERITIES.contains(severity)) {
            errors.add("invalid severity/anchor-target contract: " + id + " 非法 severity=" + severity);
        }

        assertNonBlank(id, "mainSource.summary", getString(mainSource, "summary"), errors);
        assertNonBlank(id, "backupSource.summary", getString(backupSource, "summary"), errors);
        assertNonBlank(id, "primaryUse.summary", getString(primaryUse, "summary"), errors);
        assertNonBlank(id, "risk.summary", getString(risk, "summary"), errors);

        JsonArray mainAnchors = getArray(mainSource, "anchors");
        JsonArray backupAnchors = getArray(backupSource, "anchors");
        JsonArray targets = getArray(primaryUse, "targets");
        if (mainAnchors.isEmpty()) {
            errors.add("invalid severity/anchor-target contract: " + id + " mainSource.anchors 不能为空");
        }
        if (targets.isEmpty()) {
            errors.add("invalid severity/anchor-target contract: " + id + " primaryUse.targets 不能为空");
        }

        if (!isOrderedByPrefix(mainAnchors, ANCHOR_ORDER) || !isOrderedByPrefix(backupAnchors, ANCHOR_ORDER)) {
            errors.add("json ordering mismatch: " + id + " anchors 顺序不符合约束");
        }
        if (!isOrderedByPrefix(targets, TARGET_ORDER)) {
            errors.add("json ordering mismatch: " + id + " targets 顺序不符合约束");
        }

        if (preconditions.isEmpty()) {
            errors.add("missing summary/preconditions: " + id + " preconditions 不能为空");
        }
        for (int i = 0; i < preconditions.size(); i++) {
            JsonObject precondition = preconditions.get(i).getAsJsonObject();
            validateObjectKeyOrder(id, "preconditions[" + i + "]", precondition, PRECONDITION_FIELDS, errors);
            String type = getString(precondition, "type");
            String summary = getString(precondition, "summary");
            if (!PRECONDITION_TYPES.contains(type)) {
                errors.add("invalid source enum combination: " + id + " preconditions.type 非法=" + type);
            }
            if (summary.isBlank()) {
                errors.add("missing summary/preconditions: " + id + " preconditions[" + i + "].summary 为空");
            }
        }
    }

    private static void validateDeepShallowContract(String id, String depth, JsonObject entry, List<String> errors) {
        JsonArray linkPoints = getArray(entry, "linkPoints");
        String cost = getString(entry, "cost");

        if ("deep".equals(depth)) {
            Set<String> unique = toStringSet(linkPoints);
            if (unique.size() < 2 || cost.isBlank()) {
                errors.add("missing cost/risk: " + id + " 深度条目需>=2联动点且 cost 非空");
            }
        }

        if ("shallow".equals(depth)) {
            if (linkPoints.size() < 1 || linkPoints.size() > 2) {
                errors.add("missing cost/risk: " + id + " 浅度条目联动点数量必须为1-2");
            }

            Set<String> values = toStringSet(linkPoints);
            if (values.contains("tribulation") && values.contains("timeflow")) {
                errors.add("missing cost/risk: " + id + " 浅度条目禁止同时含 tribulation 与 timeflow");
            }

            int heavyCount = 0;
            for (String value : values) {
                if (HEAVY_LINK_POINTS.contains(value)) {
                    heavyCount++;
                }
            }
            if (heavyCount >= 2) {
                errors.add("missing cost/risk: " + id + " 浅度条目出现重型联动组合");
            }
        }
    }

    private static void validateCategoryEnumCombination(
        String id,
        String category,
        String depth,
        JsonObject entry,
        List<String> errors
    ) {
        String mainType = getString(getObject(entry, "mainSource"), "type");
        String useType = getString(getObject(entry, "primaryUse"), "type");
        JsonArray preconditions = getArray(entry, "preconditions");

        Set<String> allowed = CATEGORY_MAIN_SOURCE_RULES.getOrDefault(category, Collections.emptySet());
        if (!allowed.contains(mainType)) {
            errors.add(
                "invalid source enum combination: " + id + " category=" + category + " 不允许 mainSource.type=" + mainType
            );
        }

        if ("creature".equals(category) && "endgame_core".equals(useType) && !"deep".equals(depth)) {
            errors.add("invalid source enum combination: " + id + " creature 浅度条目不允许 endgame_core");
        }

        if ("material".equals(category) && "deep".equals(depth) && ("natural_spawn".equals(mainType) || "breed".equals(mainType))) {
            errors.add("invalid source enum combination: " + id + " material 深度条目不允许 natural_spawn/breed");
        }

        if ("plant".equals(category) && !containsPreconditionType(preconditions, "environment")) {
            errors.add("missing summary/preconditions: " + id + " plant 必须包含 environment 前置");
        }

        if ("pill".equals(category) && !containsPreconditionType(preconditions, "material")) {
            errors.add("missing summary/preconditions: " + id + " pill 必须包含 material 前置");
        }
    }

    private static void validateAnchorTargetSeverityContract(String id, String depth, JsonObject entry, List<String> errors) {
        JsonObject risk = getObject(entry, "risk");
        String riskType = getString(risk, "type");
        String severity = getString(risk, "severity");

        if ("shallow".equals(depth) && !("low".equals(severity) || "medium".equals(severity))) {
            errors.add("invalid severity/anchor-target contract: " + id + " shallow 仅允许 low|medium");
        }
        if ("deep".equals(depth) && !("high".equals(severity) || "critical".equals(severity))) {
            errors.add("invalid severity/anchor-target contract: " + id + " deep 仅允许 high|critical");
        }
    }

    private static void validateClauseExtraction(
        String id,
        JsonObject entry,
        PlanExtract extract,
        List<String> errors
    ) {
        String mechanic = getString(entry, "mechanic");
        String cost = getString(entry, "cost");
        String mainSummary = getString(getObject(entry, "mainSource"), "summary");
        String backupSummary = getString(getObject(entry, "backupSource"), "summary");
        String useSummary = getString(getObject(entry, "primaryUse"), "summary");
        String riskSummary = getString(getObject(entry, "risk"), "summary");

        if (!mechanic.equals(extract.mechanic)) {
            errors.add("mechanic/cost extraction mismatch: " + id + " mechanic 不匹配");
        }
        if (!cost.equals(extract.cost)) {
            errors.add("mechanic/cost extraction mismatch: " + id + " cost 不匹配");
        }
        if (!mainSummary.equals(extract.mainSourceSummary)) {
            errors.add("mechanic/cost extraction mismatch: " + id + " mainSource.summary 不匹配");
        }
        if (!backupSummary.equals(extract.backupSourceSummary)) {
            errors.add("mechanic/cost extraction mismatch: " + id + " backupSource.summary 不匹配");
        }
        if (!useSummary.equals(extract.primaryUseSummary)) {
            errors.add("mechanic/cost extraction mismatch: " + id + " primaryUse.summary 不匹配");
        }
        if (!riskSummary.equals(extract.riskSummary)) {
            errors.add("mechanic/cost extraction mismatch: " + id + " risk.summary 不匹配");
        }

        JsonArray preconditions = getArray(entry, "preconditions");
        List<String> actualPreconditions = new ArrayList<>();
        for (JsonElement preconditionElement : preconditions) {
            actualPreconditions.add(getString(preconditionElement.getAsJsonObject(), "summary"));
        }
        if (!actualPreconditions.equals(extract.preconditions)) {
            errors.add("mechanic/cost extraction mismatch: " + id + " preconditions.summary 顺序或内容不匹配");
        }

    }

    private static void validateStructuredConsistency(String id, JsonObject entry, StructuredExpect expected, List<String> errors) {
        JsonArray linkPoints = getArray(entry, "linkPoints");
        List<String> actualLinkPoints = toStringList(linkPoints);
        if (!actualLinkPoints.equals(expected.linkPoints)) {
            errors.add("linkPoints lexicon mismatch: " + id + " 与计划结构化落地要求不一致");
        }

        String mainType = getString(getObject(entry, "mainSource"), "type");
        String backupType = getString(getObject(entry, "backupSource"), "type");
        String useType = getString(getObject(entry, "primaryUse"), "type");
        String riskType = getString(getObject(entry, "risk"), "type");
        String severity = getString(getObject(entry, "risk"), "severity");

        if (!mainType.equals(expected.mainSourceType)
            || !backupType.equals(expected.backupSourceType)
            || !useType.equals(expected.primaryUseType)
            || !riskType.equals(expected.riskType)
            || !severity.equals(expected.riskSeverity)) {
            errors.add("enum lexicon mismatch: " + id + " 与计划结构化落地要求不一致");
        }
    }

    private static void validateObjectKeyOrder(
        String id,
        String path,
        JsonObject object,
        List<String> expected,
        List<String> errors
    ) {
        List<String> keys = new ArrayList<>(object.keySet());
        if (!expected.equals(keys)) {
            errors.add("json ordering mismatch: " + id + " " + path + " 字段顺序不合法 -> " + keys);
        }
    }

    private static boolean containsPreconditionType(JsonArray preconditions, String expectedType) {
        for (JsonElement preconditionElement : preconditions) {
            JsonObject precondition = preconditionElement.getAsJsonObject();
            if (expectedType.equals(getString(precondition, "type"))) {
                return true;
            }
        }
        return false;
    }

    private static void assertNonBlank(String id, String field, String value, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add("missing summary/preconditions: " + id + " " + field + " 为空");
        }
    }

    private static JsonObject getObject(JsonObject object, String key) {
        return object.getAsJsonObject(key);
    }

    private static JsonArray getArray(JsonObject object, String key) {
        return object.getAsJsonArray(key);
    }

    private static String getString(JsonObject object, String key) {
        return object.get(key).getAsString();
    }

    private static Set<String> toStringSet(JsonArray array) {
        Set<String> values = new LinkedHashSet<>();
        for (JsonElement element : array) {
            values.add(element.getAsString());
        }
        return values;
    }

    private static List<String> toStringList(JsonArray array) {
        List<String> values = new ArrayList<>();
        for (JsonElement element : array) {
            values.add(element.getAsString());
        }
        return values;
    }

    private static boolean isOrderedByPrefix(JsonArray array, Map<String, Integer> order) {
        List<String> values = toStringList(array);
        List<String> sorted = new ArrayList<>(values);
        Collections.sort(sorted, (left, right) -> {
            int leftRank = order.getOrDefault(prefix(left), Integer.MAX_VALUE);
            int rightRank = order.getOrDefault(prefix(right), Integer.MAX_VALUE);
            if (leftRank != rightRank) {
                return Integer.compare(leftRank, rightRank);
            }
            return left.compareTo(right);
        });
        return values.equals(sorted);
    }

    private static String prefix(String value) {
        int idx = value.indexOf(':');
        return idx >= 0 ? value.substring(0, idx) : value;
    }

    private static int naturalIdOrder(String id) {
        if (id == null || id.length() < 5) {
            return Integer.MAX_VALUE;
        }
        char depthFlag = id.charAt(2);
        int depthRank = depthFlag == 'S' ? 0 : 1;
        int number = Integer.parseInt(id.substring(3));
        return depthRank * 100 + number;
    }

    private static String inferCategoryById(String id) {
        if (id.startsWith("C-")) {
            return "creature";
        }
        if (id.startsWith("P-")) {
            return "plant";
        }
        if (id.startsWith("D-")) {
            return "pill";
        }
        return "material";
    }

    private static String cleanText(String value) {
        String cleaned = value.trim();
        if (cleaned.startsWith("`") && cleaned.endsWith("`") && cleaned.length() >= 2) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        cleaned = cleaned.replace("`", "").trim();
        if (cleaned.endsWith("。")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        return cleaned.trim();
    }

    private static List<String> splitPreconditions(String raw) {
        List<String> firstPass = new ArrayList<>();
        for (String part : raw.split("、")) {
            String trimmed = cleanText(part);
            if (!trimmed.isEmpty()) {
                firstPass.add(trimmed);
            }
        }

        List<String> secondPass = new ArrayList<>();
        Pattern splitPattern = Pattern.compile("(?:与|及|并且|并)");
        for (String part : firstPass) {
            String[] fragments = splitPattern.split(part);
            for (String fragment : fragments) {
                String trimmed = cleanText(fragment);
                if (trimmed.isEmpty()) {
                    continue;
                }
                if (trimmed.length() < 2 && !secondPass.isEmpty()) {
                    int last = secondPass.size() - 1;
                    secondPass.set(last, secondPass.get(last) + trimmed);
                    continue;
                }
                secondPass.add(trimmed);
            }
        }

        if (secondPass.size() <= 4) {
            return secondPass;
        }

        List<String> merged = new ArrayList<>(secondPass.subList(0, 4));
        for (int i = 4; i < secondPass.size(); i++) {
            merged.set(3, merged.get(3) + "、" + secondPass.get(i));
        }
        return merged;
    }

    private record PlanSnapshot(
        Map<String, PlanExtract> extracts,
        Map<String, StructuredExpect> structured
    ) {
    }

    private record PlanExtract(
        String id,
        String category,
        String mechanic,
        String cost,
        String mainSourceSummary,
        String backupSourceSummary,
        List<String> preconditions,
        String primaryUseSummary,
        String riskSummary
    ) {
    }

    private record StructuredExpect(
        List<String> linkPoints,
        String mainSourceType,
        String backupSourceType,
        String primaryUseType,
        String riskType,
        String riskSeverity
    ) {
        static StructuredExpect empty() {
            return new StructuredExpect(Collections.emptyList(), "", "", "", "", "");
        }
    }
}
