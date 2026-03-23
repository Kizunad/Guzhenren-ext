package com.Kizunad.guzhenrenext.plan2;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Plan2AssetWhitelistValidator {

    static final String MODELS_ROOT = "src/main/resources/assets/guzhenrenext/models";
    static final String JAVA_ROOT = "src/main/java/com/Kizunad/guzhenrenext";

    private static final Pattern NAMESPACED_ID_PATTERN =
        Pattern.compile("([a-z0-9_.-]+:[a-z0-9_./-]+)");

    private Plan2AssetWhitelistValidator() {
    }

    static List<String> validateCurrentProject() throws IOException {
        return validate(Paths.get(MODELS_ROOT), collectJavaSources(Paths.get(JAVA_ROOT)));
    }

    static List<String> validate(Path modelsRoot, Map<Path, String> javaSources) throws IOException {
        List<String> errors = new ArrayList<>();
        Set<String> availableModels = collectAvailableModelIds(modelsRoot);
        validateModelJsonWhitelist(modelsRoot, availableModels, errors);
        validateSoundNamespaceWhitelist(javaSources, errors);
        return errors;
    }

    static Map<Path, String> collectJavaSources(Path javaRoot) throws IOException {
        Map<Path, String> sources = new HashMap<>();
        if (!Files.exists(javaRoot)) {
            return sources;
        }
        try (var stream = Files.walk(javaRoot)) {
            List<Path> files = stream
                .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".java"))
                .toList();
            for (Path file : files) {
                sources.put(file, Files.readString(file, StandardCharsets.UTF_8));
            }
        }
        return sources;
    }

    private static Set<String> collectAvailableModelIds(Path modelsRoot) throws IOException {
        Set<String> modelIds = new HashSet<>();
        if (!Files.exists(modelsRoot)) {
            return modelIds;
        }
        try (var stream = Files.walk(modelsRoot)) {
            List<Path> modelFiles = stream
                .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".json"))
                .toList();
            for (Path modelFile : modelFiles) {
                Path relative = modelsRoot.relativize(modelFile);
                String relativePath = relative.toString().replace('\\', '/');
                if (!relativePath.endsWith(".json")) {
                    continue;
                }
                String idPath = relativePath.substring(0, relativePath.length() - ".json".length());
                modelIds.add("guzhenrenext:" + idPath);
            }
        }
        return modelIds;
    }

    private static void validateModelJsonWhitelist(
        Path modelsRoot,
        Set<String> availableModels,
        List<String> errors
    ) throws IOException {
        if (!Files.exists(modelsRoot)) {
            return;
        }
        try (var stream = Files.walk(modelsRoot)) {
            List<Path> modelFiles = stream
                .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".json"))
                .toList();
            for (Path modelFile : modelFiles) {
                validateSingleModel(modelFile, availableModels, errors);
            }
        }
    }

    private static void validateSingleModel(Path modelFile, Set<String> availableModels, List<String> errors)
        throws IOException {
        JsonObject object = JsonParser.parseString(Files.readString(modelFile, StandardCharsets.UTF_8)).getAsJsonObject();
        String parent = getOptionalString(object, "parent");
        if (parent != null) {
            validateParent(modelFile, parent, availableModels, errors);
        }

        JsonObject textures = object.has("textures") && object.get("textures").isJsonObject()
            ? object.getAsJsonObject("textures")
            : null;
        if (textures == null) {
            return;
        }
        Set<String> textureKeys = textures.keySet();
        for (String key : textureKeys) {
            JsonElement valueElement = textures.get(key);
            if (!valueElement.isJsonPrimitive()) {
                errors.add(modelFile + " | textures." + key + " -> 非字符串纹理引用");
                continue;
            }
            String value = valueElement.getAsString();
            if (value.startsWith("#")) {
                String refKey = value.substring(1);
                if (!textureKeys.contains(refKey)) {
                    errors.add(modelFile + " | textures." + key + " -> 引用了不存在的纹理键 #" + refKey);
                }
                continue;
            }
            if (!value.startsWith("minecraft:")) {
                errors.add(modelFile + " | textures." + key + " -> 非白名单纹理命名空间: " + value);
            }
        }
    }

    private static void validateParent(Path modelFile, String parent, Set<String> availableModels, List<String> errors) {
        String normalizedParent = normalizeModelId(parent);
        if (normalizedParent.startsWith("minecraft:")) {
            return;
        }
        if (normalizedParent.startsWith("guzhenrenext:")) {
            if (!availableModels.contains(normalizedParent)) {
                errors.add(modelFile + " | parent -> 非法复用模型引用(不存在): " + parent);
            }
            return;
        }
        errors.add(modelFile + " | parent -> 非白名单模型命名空间: " + parent);
    }

    private static String normalizeModelId(String parent) {
        if (parent.contains(":")) {
            return parent;
        }
        return "minecraft:" + parent;
    }

    private static void validateSoundNamespaceWhitelist(Map<Path, String> javaSources, List<String> errors) {
        for (Map.Entry<Path, String> entry : javaSources.entrySet()) {
            Path javaFile = entry.getKey();
            String[] lines = entry.getValue().split("\\R", -1);
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                String lower = line.toLowerCase();
                boolean soundContext = lower.contains("sound");
                if (!soundContext) {
                    continue;
                }
                Matcher matcher = NAMESPACED_ID_PATTERN.matcher(line);
                while (matcher.find()) {
                    String id = matcher.group(1);
                    String namespace = id.substring(0, id.indexOf(':'));
                    if (!"minecraft".equals(namespace)) {
                        errors.add(
                            javaFile
                                + ":"
                                + (i + 1)
                                + " | sound namespace -> 非白名单命名空间: "
                                + id
                        );
                    }
                }
            }
        }
    }

    private static String getOptionalString(JsonObject source, String key) {
        if (!source.has(key) || !source.get(key).isJsonPrimitive()) {
            return null;
        }
        return source.get(key).getAsString();
    }
}
