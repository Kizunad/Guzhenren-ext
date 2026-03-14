package com.Kizunad.guzhenrenext.plan2;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Plan2ContentMatrixAnchorTargetTest {

    @Test
    void shouldSatisfyAnchorAndTargetRules() throws IOException {
        JsonArray array = Plan2ContentMatrixTestSupport.readMatrixArray();
        Set<String> targetSystems = Set.of(
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

        for (JsonElement element : array) {
            JsonObject entry = element.getAsJsonObject();
            String id = entry.get("id").getAsString();

            JsonArray mainAnchors = entry.getAsJsonObject("mainSource").getAsJsonArray("anchors");
            assertFalse(mainAnchors.isEmpty());
            boolean hasCodeOrPlan = false;
            boolean hasPlanAnchor = false;
            String expectedPlan = "plan:.sisyphus/plans/xianqiao-plan2-four-category-120-expansion.md#" + id;
            for (JsonElement anchorElement : mainAnchors) {
                String anchor = anchorElement.getAsString();
                if (anchor.startsWith("code:") || anchor.startsWith("plan:")) {
                    hasCodeOrPlan = true;
                }
                if (anchor.equals(expectedPlan)) {
                    hasPlanAnchor = true;
                }
            }
            assertTrue(hasCodeOrPlan);
            assertTrue(hasPlanAnchor);
            if ("deep".equals(entry.get("depth").getAsString())) {
                assertTrue(mainAnchors.size() >= 2);
            }

            JsonArray targets = entry.getAsJsonObject("primaryUse").getAsJsonArray("targets");
            assertFalse(targets.isEmpty());
            boolean hasItemTarget = false;
            for (JsonElement targetElement : targets) {
                String target = targetElement.getAsString();
                if (target.startsWith("item:")) {
                    hasItemTarget = true;
                }
                if (target.startsWith("system:")) {
                    String system = target.substring("system:".length());
                    assertTrue(targetSystems.contains(system));
                }
            }
            if ("endgame_core".equals(entry.getAsJsonObject("primaryUse").get("type").getAsString())) {
                assertTrue(targets.size() >= 2);
                assertTrue(hasItemTarget);
            }
        }
    }
}
