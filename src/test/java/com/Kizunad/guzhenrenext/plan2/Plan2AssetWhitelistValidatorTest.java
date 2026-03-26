package com.Kizunad.guzhenrenext.plan2;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class Plan2AssetWhitelistValidatorTest {

    @Test
    void shouldPassCurrentProjectAssetWhitelist() throws IOException {
        List<String> errors = Plan2AssetWhitelistValidator.validateCurrentProject();
        assertTrue(errors.isEmpty(), "当前资源应通过白名单校验，实际错误: " + errors);
    }

    @Test
    void shouldReportIllegalTextureNamespaceWithFileAndField() throws IOException {
        Path tempModelsRoot = Files.createTempDirectory("task3-models-");
        try {
            writeModel(
                tempModelsRoot.resolve("item/valid_base.json"),
                "minecraft:item/generated",
                mapOf("layer0", "minecraft:item/apple")
            );
            writeModel(
                tempModelsRoot.resolve("item/bad_texture.json"),
                "minecraft:item/generated",
                mapOf("layer0", "guzhenrenext:item/not_allowed")
            );

            List<String> errors = Plan2AssetWhitelistValidator.validate(tempModelsRoot, Map.of());
            assertContains(errors, "bad_texture.json | textures.layer0");
            assertContains(errors, "非白名单纹理命名空间");
        } finally {
            deleteRecursively(tempModelsRoot);
        }
    }

    @Test
    void shouldReportIllegalParentModelReferenceWithFileAndField() throws IOException {
        Path tempModelsRoot = Files.createTempDirectory("task3-parent-");
        try {
            writeModel(
                tempModelsRoot.resolve("item/valid_base.json"),
                "minecraft:item/generated",
                mapOf("layer0", "minecraft:item/apple")
            );
            writeModel(
                tempModelsRoot.resolve("item/bad_parent.json"),
                "guzhenrenext:item/missing_model",
                mapOf("layer0", "minecraft:item/apple")
            );

            List<String> errors = Plan2AssetWhitelistValidator.validate(tempModelsRoot, Map.of());
            assertContains(errors, "bad_parent.json | parent");
            assertContains(errors, "非法复用模型引用");
        } finally {
            deleteRecursively(tempModelsRoot);
        }
    }

    @Test
    void shouldReportIllegalSoundNamespaceWithFileAndLine() throws IOException {
        Path fakeJava = Path.of("src/test/java/com/Kizunad/guzhenrenext/plan2/fixture/BadSoundFixture.java");
        String javaText = """
            package com.Kizunad.guzhenrenext.plan2.fixture;

            final class BadSoundFixture {
                void bad() {
                    String customSound = \"modded:sound/boom\";
                    String vanillaSound = \"minecraft:entity.fox.screech\";
                }
            }
            """;

        Map<Path, String> sources = new HashMap<>();
        sources.put(fakeJava, javaText);

        List<String> errors = Plan2AssetWhitelistValidator.validate(
            Files.createTempDirectory("task3-empty-models-"),
            sources
        );
        assertContains(errors, "BadSoundFixture.java:5 | sound namespace");
        assertContains(errors, "modded:sound/boom");
    }

    private static Map<String, String> mapOf(String key, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    private static void writeModel(Path file, String parent, Map<String, String> textures) throws IOException {
        Files.createDirectories(file.getParent());
        JsonObject model = new JsonObject();
        model.addProperty("parent", parent);
        JsonObject texturesObject = new JsonObject();
        for (Map.Entry<String, String> entry : textures.entrySet()) {
            texturesObject.addProperty(entry.getKey(), entry.getValue());
        }
        model.add("textures", texturesObject);
        Files.writeString(file, prettyJson(model), StandardCharsets.UTF_8);
    }

    private static String prettyJson(JsonObject object) throws IOException {
        StringWriter output = new StringWriter();
        JsonWriter writer = new JsonWriter(output);
        writer.setIndent("  ");
        writer.beginObject();
        writer.name("parent").value(object.get("parent").getAsString());
        writer.name("textures");
        writer.beginObject();
        JsonObject textures = object.getAsJsonObject("textures");
        for (String key : textures.keySet()) {
            writer.name(key).value(textures.get(key).getAsString());
        }
        writer.endObject();
        writer.endObject();
        writer.close();
        return output + "\n";
    }

    private static void deleteRecursively(Path root) throws IOException {
        try (var stream = Files.walk(root)) {
            List<Path> paths = stream.sorted((left, right) -> right.compareTo(left)).toList();
            for (Path path : paths) {
                Files.deleteIfExists(path);
            }
        }
    }

    private static void assertContains(List<String> errors, String expected) {
        assertTrue(
            errors.stream().anyMatch(message -> message.contains(expected)),
            "期望错误包含: " + expected + "，实际: " + errors
        );
    }
}
