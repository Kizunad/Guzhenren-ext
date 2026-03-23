package com.Kizunad.guzhenrenext.plan2;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

final class Plan2RecipeGraphValidator {

    private static final String CATEGORY_CREATURE = "creature";
    private static final String CATEGORY_PLANT = "plant";
    private static final String CATEGORY_PILL = "pill";
    private static final String CATEGORY_MATERIAL = "material";

    private static final Set<String> REQUIRED_CATEGORIES = Set.of(
        CATEGORY_CREATURE,
        CATEGORY_PLANT,
        CATEGORY_PILL,
        CATEGORY_MATERIAL
    );

    private static final String SHALLOW_DEPTH = "shallow";
    private static final String MAIN_SOURCE = "mainSource";
    private static final String BACKUP_SOURCE = "backupSource";
    private static final String PRIMARY_USE = "primaryUse";
    private static final String ANCHORS = "anchors";
    private static final String TARGETS = "targets";
    private static final String ID = "id";
    private static final String CATEGORY = "category";
    private static final String DEPTH = "depth";
    private static final String TYPE = "type";
    private static final String ENDGAME_CORE_TYPE = "endgame_core";
    private static final String XQ_LAYERING = "xq_layering";
    private static final String PREREQUISITE_RECIPE_IDS = "prerequisite_recipe_ids";

    private Plan2RecipeGraphValidator() {
    }

    static List<String> validateCurrentGraphs() throws IOException {
        return validate(
            Plan2RecipeGraphTestSupport.readCurrentMatrixArray(),
            Plan2RecipeGraphTestSupport.readCurrentTask13ARecipeGraph()
        );
    }

    static List<String> validate(JsonArray matrixArray, Map<String, JsonObject> recipeGraphById) {
        List<String> errors = new ArrayList<>();
        PlanMatrixGraph planGraph = buildPlanMatrixGraph(matrixArray, errors);
        validatePlanOrphans(planGraph, errors);
        validateEndgameBacktrace(planGraph, errors);
        validateCategoryParticipation(planGraph, errors);
        validateTask13ARecipeGraph(recipeGraphById, errors);
        return errors;
    }

    private static PlanMatrixGraph buildPlanMatrixGraph(JsonArray matrixArray, List<String> errors) {
        Map<String, EntryMeta> entryById = new LinkedHashMap<>();
        for (JsonElement element : matrixArray) {
            JsonObject entry = element.getAsJsonObject();
            String entryId = Plan2RecipeGraphTestSupport.readStringOrNull(entry, ID);
            if (entryId == null) {
                continue;
            }
            String category = Plan2RecipeGraphTestSupport.readStringOrNull(entry, CATEGORY);
            String depth = Plan2RecipeGraphTestSupport.readStringOrNull(entry, DEPTH);
            entryById.put(entryId, new EntryMeta(entryId, category, depth));
        }

        Map<String, Set<String>> outgoing = new LinkedHashMap<>();
        Map<String, Set<String>> incoming = new LinkedHashMap<>();
        Map<String, Set<String>> undirected = new LinkedHashMap<>();
        Set<String> nodesWithItemReferenceToken = new LinkedHashSet<>();
        Set<String> nodesReferencedByOthers = new LinkedHashSet<>();
        Set<String> endgameSeeds = new LinkedHashSet<>();

        for (String entryId : entryById.keySet()) {
            outgoing.put(entryId, new LinkedHashSet<>());
            incoming.put(entryId, new LinkedHashSet<>());
            undirected.put(entryId, new LinkedHashSet<>());
        }

        GraphBuildContext graphBuildContext = new GraphBuildContext(
            entryById,
            outgoing,
            incoming,
            undirected,
            nodesWithItemReferenceToken,
            nodesReferencedByOthers,
            errors
        );

        for (JsonElement element : matrixArray) {
            JsonObject entry = element.getAsJsonObject();
            String sourceId = Plan2RecipeGraphTestSupport.readStringOrNull(entry, ID);
            if (sourceId == null || !entryById.containsKey(sourceId)) {
                continue;
            }
            JsonObject primaryUse = entry.getAsJsonObject(PRIMARY_USE);
            if (ENDGAME_CORE_TYPE.equals(Plan2RecipeGraphTestSupport.readStringOrNull(primaryUse, TYPE))) {
                endgameSeeds.add(sourceId);
            }
            collectDependencyEdges(
                sourceId,
                entry.getAsJsonObject(MAIN_SOURCE),
                ANCHORS,
                graphBuildContext,
                false
            );
            collectDependencyEdges(
                sourceId,
                entry.getAsJsonObject(BACKUP_SOURCE),
                ANCHORS,
                graphBuildContext,
                false
            );
            collectDependencyEdges(
                sourceId,
                primaryUse,
                TARGETS,
                graphBuildContext,
                true
            );
        }

        Set<String> participating = new LinkedHashSet<>(nodesWithItemReferenceToken);
        participating.addAll(nodesReferencedByOthers);

        return new PlanMatrixGraph(entryById, outgoing, incoming, undirected, participating, endgameSeeds);
    }

    private static void collectDependencyEdges(
        String sourceId,
        JsonObject section,
        String field,
        GraphBuildContext context,
        boolean reverseDirection
    ) {
        for (String raw : Plan2RecipeGraphTestSupport.readStringArray(section, field)) {
            String targetId = Plan2RecipeGraphTestSupport.extractItemReferenceId(raw);
            if (targetId == null) {
                continue;
            }
            context.nodesWithItemReferenceToken().add(sourceId);
            if (!context.entryById().containsKey(targetId)) {
                context.errors().add(
                    "plan2 item graph broken_reference: source="
                        + sourceId
                        + ", brokenId="
                        + targetId
                        + ", raw="
                        + raw
                );
                continue;
            }
            String edgeSource = reverseDirection ? targetId : sourceId;
            String edgeTarget = reverseDirection ? sourceId : targetId;
            context.outgoing().get(edgeSource).add(edgeTarget);
            context.incoming().get(edgeTarget).add(edgeSource);
            context.undirected().get(sourceId).add(targetId);
            context.undirected().get(targetId).add(sourceId);
            context.nodesReferencedByOthers().add(targetId);
        }
    }

    private static void validatePlanOrphans(PlanMatrixGraph graph, List<String> errors) {
        for (String nodeId : graph.participatingNodeIds()) {
            Set<String> out = graph.outgoing().getOrDefault(nodeId, Set.of());
            Set<String> in = graph.incoming().getOrDefault(nodeId, Set.of());
            if (out.isEmpty() && in.isEmpty()) {
                errors.add("plan2 item graph orphan_entry: " + nodeId);
            }
        }
    }

    private static void validateEndgameBacktrace(PlanMatrixGraph graph, List<String> errors) {
        Set<String> shallowIds = graph.entryById().values().stream()
            .filter(entry -> SHALLOW_DEPTH.equals(entry.depth()))
            .map(EntryMeta::id)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        for (String seedId : graph.endgameSeedIds()) {
            if (!canReachAnyShallow(seedId, shallowIds, graph.outgoing())
                && containsAnyShallow(seedId, shallowIds, graph.undirected())) {
                errors.add(
                    "plan2 endgame backtrace unreachable: seed="
                        + seedId
                        + ", expectedReachDepth="
                        + SHALLOW_DEPTH
                );
            }
        }
    }

    private static boolean canReachAnyShallow(
        String startId,
        Set<String> shallowIds,
        Map<String, Set<String>> outgoing
    ) {
        Set<String> visited = new LinkedHashSet<>();
        Deque<String> stack = new ArrayDeque<>();
        visited.add(startId);
        stack.push(startId);
        while (!stack.isEmpty()) {
            String current = stack.pop();
            if (shallowIds.contains(current) && !startId.equals(current)) {
                return true;
            }
            for (String next : outgoing.getOrDefault(current, Set.of())) {
                if (visited.add(next)) {
                    stack.push(next);
                }
            }
        }
        return false;
    }

    private static boolean containsAnyShallow(
        String startId,
        Set<String> shallowIds,
        Map<String, Set<String>> undirected
    ) {
        Set<String> visited = new LinkedHashSet<>();
        Deque<String> stack = new ArrayDeque<>();
        visited.add(startId);
        stack.push(startId);
        while (!stack.isEmpty()) {
            String current = stack.pop();
            if (shallowIds.contains(current)) {
                return true;
            }
            for (String next : undirected.getOrDefault(current, Set.of())) {
                if (visited.add(next)) {
                    stack.push(next);
                }
            }
        }
        return false;
    }

    private static void validateCategoryParticipation(PlanMatrixGraph graph, List<String> errors) {
        Set<String> closure = new LinkedHashSet<>();
        for (String seedId : graph.endgameSeedIds()) {
            collectReachable(seedId, graph.outgoing(), closure);
        }
        Set<String> closureCategories = closure.stream()
            .map(graph.entryById()::get)
            .filter(meta -> meta != null)
            .map(EntryMeta::category)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> missingClosureCategories = new LinkedHashSet<>(REQUIRED_CATEGORIES);
        missingClosureCategories.removeAll(closureCategories);
        if (!missingClosureCategories.isEmpty()) {
            errors.add("plan2 category participation disconnected closure: " + missingClosureCategories);
        }
    }

    private static void collectReachable(
        String startId,
        Map<String, Set<String>> outgoing,
        Set<String> collected
    ) {
        Set<String> visited = new LinkedHashSet<>();
        Deque<String> stack = new ArrayDeque<>();
        visited.add(startId);
        stack.push(startId);
        while (!stack.isEmpty()) {
            String current = stack.pop();
            collected.add(current);
            for (String next : outgoing.getOrDefault(current, Set.of())) {
                if (visited.add(next)) {
                    stack.push(next);
                }
            }
        }
    }

    private static void validateTask13ARecipeGraph(Map<String, JsonObject> recipeGraphById, List<String> errors) {
        for (Map.Entry<String, JsonObject> entry : recipeGraphById.entrySet()) {
            String recipeId = entry.getKey();
            for (String prerequisiteId : readPrerequisiteRecipeIds(entry.getValue())) {
                if (!recipeGraphById.containsKey(prerequisiteId)) {
                    errors.add(
                        "task13a prerequisite missing: recipe="
                            + recipeId
                            + ", brokenId="
                            + prerequisiteId
                    );
                }
            }
        }

        detectRecipeCycles(recipeGraphById, errors);
    }

    private static List<String> readPrerequisiteRecipeIds(JsonObject recipeJson) {
        List<String> result = new ArrayList<>();
        if (recipeJson == null || !recipeJson.has(XQ_LAYERING) || !recipeJson.get(XQ_LAYERING).isJsonObject()) {
            return result;
        }
        JsonObject layering = recipeJson.getAsJsonObject(XQ_LAYERING);
        if (!layering.has(PREREQUISITE_RECIPE_IDS) || !layering.get(PREREQUISITE_RECIPE_IDS).isJsonArray()) {
            return result;
        }
        JsonArray prerequisites = layering.getAsJsonArray(PREREQUISITE_RECIPE_IDS);
        for (JsonElement element : prerequisites) {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                result.add(element.getAsString());
            }
        }
        return result;
    }

    private static void detectRecipeCycles(Map<String, JsonObject> recipeGraphById, List<String> errors) {
        Map<String, Integer> state = new LinkedHashMap<>();
        Deque<String> path = new ArrayDeque<>();
        Set<String> cycleDedup = new LinkedHashSet<>();
        for (String recipeId : recipeGraphById.keySet()) {
            if (state.getOrDefault(recipeId, 0) == 0) {
                dfsRecipe(recipeId, recipeGraphById, state, path, cycleDedup, errors);
            }
        }
    }

    private static void dfsRecipe(
        String recipeId,
        Map<String, JsonObject> recipeGraphById,
        Map<String, Integer> state,
        Deque<String> path,
        Set<String> cycleDedup,
        List<String> errors
    ) {
        state.put(recipeId, 1);
        path.addLast(recipeId);
        for (String prerequisiteId : readPrerequisiteRecipeIds(recipeGraphById.get(recipeId))) {
            if (!recipeGraphById.containsKey(prerequisiteId)) {
                continue;
            }
            int nextState = state.getOrDefault(prerequisiteId, 0);
            if (nextState == 0) {
                dfsRecipe(prerequisiteId, recipeGraphById, state, path, cycleDedup, errors);
                continue;
            }
            if (nextState == 1) {
                String cycle = buildCycleString(path, prerequisiteId);
                if (cycleDedup.add(cycle)) {
                    errors.add("task13a prerequisite cycle: " + cycle);
                }
            }
        }
        path.removeLast();
        state.put(recipeId, 2);
    }

    private static String buildCycleString(Deque<String> path, String repeatStart) {
        List<String> sequence = new ArrayList<>();
        boolean collecting = false;
        for (String node : path) {
            if (!collecting && repeatStart.equals(node)) {
                collecting = true;
            }
            if (collecting) {
                sequence.add(node);
            }
        }
        sequence.add(repeatStart);
        return String.join(" -> ", sequence);
    }

    private record EntryMeta(String id, String category, String depth) {
    }

    private record PlanMatrixGraph(
        Map<String, EntryMeta> entryById,
        Map<String, Set<String>> outgoing,
        Map<String, Set<String>> incoming,
        Map<String, Set<String>> undirected,
        Set<String> participatingNodeIds,
        Set<String> endgameSeedIds
    ) {
    }

    private record GraphBuildContext(
        Map<String, EntryMeta> entryById,
        Map<String, Set<String>> outgoing,
        Map<String, Set<String>> incoming,
        Map<String, Set<String>> undirected,
        Set<String> nodesWithItemReferenceToken,
        Set<String> nodesReferencedByOthers,
        List<String> errors
    ) {
    }
}
