package com.Kizunad.guzhenrenext.plan2;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Plan2ContentMatrixSchemaTest {

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

    @Test
    void shouldSatisfySchemaAndEnumConstraints() throws IOException {
        JsonArray array = Plan2ContentMatrixTestSupport.readMatrixArray();
        Set<String> categories = Set.of("creature", "plant", "pill", "material");
        Set<String> depths = Set.of("shallow", "deep");
        Set<String> severities = Set.of("low", "medium", "high", "critical");
        Set<String> sourceTypes = Set.of(
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
        Set<String> preconditionTypes = Set.of(
            "environment",
            "unlock",
            "structure",
            "material",
            "combat_power",
            "system_state"
        );
        Set<String> primaryUseTypes = Set.of(
            "recipe_input",
            "combat_consumable",
            "growth_accelerator",
            "progression_gate",
            "infrastructure",
            "endgame_core"
        );
        Set<String> riskTypes = Set.of(
            "resource_drain",
            "status_penalty",
            "environment_damage",
            "disaster_escalation",
            "irreversible",
            "production_stall"
        );

        for (JsonElement element : array) {
            JsonObject entry = element.getAsJsonObject();
            assertTrue(entry.keySet().containsAll(ENTRY_FIELDS));
            assertTrue(categories.contains(entry.get("category").getAsString()));
            assertTrue(depths.contains(entry.get("depth").getAsString()));

            assertFalse(entry.get("assetReuse").getAsString().isBlank());
            assertFalse(entry.get("mechanic").getAsString().isBlank());
            assertFalse(entry.get("cost").getAsString().isBlank());

            JsonArray linkPoints = entry.getAsJsonArray("linkPoints");
            assertFalse(linkPoints.isEmpty());

            JsonObject mainSource = entry.getAsJsonObject("mainSource");
            JsonObject backupSource = entry.getAsJsonObject("backupSource");
            JsonObject primaryUse = entry.getAsJsonObject("primaryUse");
            JsonObject risk = entry.getAsJsonObject("risk");

            assertTrue(sourceTypes.contains(mainSource.get("type").getAsString()));
            assertTrue(sourceTypes.contains(backupSource.get("type").getAsString()));
            assertTrue(primaryUseTypes.contains(primaryUse.get("type").getAsString()));
            assertTrue(riskTypes.contains(risk.get("type").getAsString()));
            assertTrue(severities.contains(risk.get("severity").getAsString()));

            assertFalse(mainSource.get("summary").getAsString().isBlank());
            assertFalse(backupSource.get("summary").getAsString().isBlank());
            assertFalse(primaryUse.get("summary").getAsString().isBlank());
            assertFalse(risk.get("summary").getAsString().isBlank());

            assertFalse(mainSource.getAsJsonArray("anchors").isEmpty());
            assertFalse(primaryUse.getAsJsonArray("targets").isEmpty());

            JsonArray preconditions = entry.getAsJsonArray("preconditions");
            assertFalse(preconditions.isEmpty());
            for (JsonElement preconditionElement : preconditions) {
                JsonObject precondition = preconditionElement.getAsJsonObject();
                assertTrue(preconditionTypes.contains(precondition.get("type").getAsString()));
                assertFalse(precondition.get("summary").getAsString().isBlank());
            }
        }
    }
}
