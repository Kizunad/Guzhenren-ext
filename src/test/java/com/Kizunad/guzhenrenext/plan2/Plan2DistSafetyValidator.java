package com.Kizunad.guzhenrenext.plan2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Plan2DistSafetyValidator {

    static final String JAVA_ROOT = "src/main/java/com/Kizunad/guzhenrenext";
    static final String GUZHENREN_EXT_FILE = JAVA_ROOT + "/GuzhenrenExt.java";
    static final String PLAN2_ROOT = JAVA_ROOT + "/plan2";
    static final String XIANQIAO_ROOT = JAVA_ROOT + "/xianqiao";

    private static final String CLIENT_SEGMENT = "/client/";
    private static final String ALLOWED_GUZHENREN_EXT_CLIENT_BOOTSTRAP =
        "\"com.Kizunad.guzhenrenext.client.GuzhenrenExtClient\"";
    private static final String ALLOWED_GUZHENREN_EXT_PLACEHOLDER =
        "\"__TASK31_ALLOWED_GUZHENREN_EXT_CLIENT_BOOTSTRAP__\"";

    private static final Pattern CLIENT_IMPORT_PATTERN = Pattern.compile(
        "\\bimport\\s+("
            + "net\\.minecraft\\.client\\.[\\w.*]+"
            + "|net\\.neoforged\\.neoforge\\.client\\.[\\w.*]+"
            + "|com\\.Kizunad\\.guzhenrenext(?:\\.[A-Za-z0-9_]+)*\\.client\\.[\\w.*]+"
            + ")\\s*;"
    );

    private static final Pattern CLIENT_REFERENCE_PATTERN = Pattern.compile(
        "("
            + "net\\.minecraft\\.client\\.[A-Za-z0-9_$.]+"
            + "|net\\.neoforged\\.neoforge\\.client\\.[A-Za-z0-9_$.]+"
            + "|com\\.Kizunad\\.guzhenrenext(?:\\.[A-Za-z0-9_]+)*\\.client\\.[A-Za-z0-9_$.]+"
            + ")"
    );

    private Plan2DistSafetyValidator() {
    }

    static List<String> validateCurrentProject() throws IOException {
        return validate(collectCurrentBoundarySources());
    }

    static List<String> validate(Map<Path, String> javaSources) {
        List<String> errors = new ArrayList<>();
        List<Map.Entry<Path, String>> entries = javaSources.entrySet().stream()
            .sorted(Comparator.comparing(entry -> normalizePath(entry.getKey())))
            .toList();
        for (Map.Entry<Path, String> entry : entries) {
            String normalizedPath = normalizePath(entry.getKey());
            if (!isAuditedBoundary(normalizedPath) || isExplicitClientPath(normalizedPath)) {
                continue;
            }
            scanSource(normalizedPath, entry.getValue(), errors);
        }
        return errors;
    }

    private static Map<Path, String> collectCurrentBoundarySources() throws IOException {
        Map<Path, String> sources = new LinkedHashMap<>();
        collectSingleFileIfExists(Path.of(GUZHENREN_EXT_FILE), sources);
        collectJavaTreeIfExists(Paths.get(PLAN2_ROOT), sources);
        collectJavaTreeIfExists(Paths.get(XIANQIAO_ROOT), sources);
        return sources;
    }

    private static void collectSingleFileIfExists(Path file, Map<Path, String> sources) throws IOException {
        if (!Files.isRegularFile(file)) {
            return;
        }
        sources.put(file, Files.readString(file, StandardCharsets.UTF_8));
    }

    private static void collectJavaTreeIfExists(Path root, Map<Path, String> sources) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (var stream = Files.walk(root)) {
            List<Path> files = stream
                .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".java"))
                .sorted()
                .toList();
            for (Path file : files) {
                sources.put(file, Files.readString(file, StandardCharsets.UTF_8));
            }
        }
    }

    private static void scanSource(String normalizedPath, String sourceText, List<String> errors) {
        String preparedSource = stripComments(applyAcceptedBootstrapException(normalizedPath, sourceText));
        String[] lines = preparedSource.split("\\R", -1);
        for (int index = 0; index < lines.length; index++) {
            String line = lines[index];
            Matcher importMatcher = CLIENT_IMPORT_PATTERN.matcher(line);
            if (importMatcher.find()) {
                errors.add(
                    normalizedPath
                        + ":"
                        + (index + 1)
                        + " | dist safety -> 公共/服务端可达路径直接 import 客户端类: "
                        + importMatcher.group(1)
                );
                continue;
            }

            Matcher referenceMatcher = CLIENT_REFERENCE_PATTERN.matcher(line);
            if (referenceMatcher.find()) {
                errors.add(
                    normalizedPath
                        + ":"
                        + (index + 1)
                        + " | dist safety -> 公共/服务端可达路径直接引用客户端类/包: "
                        + referenceMatcher.group(1)
                );
            }
        }
    }

    private static String applyAcceptedBootstrapException(String normalizedPath, String sourceText) {
        if (!matchesFile(normalizedPath, GUZHENREN_EXT_FILE)) {
            return sourceText;
        }
        if (!containsAcceptedGuzhenrenExtBootstrap(sourceText)) {
            return sourceText;
        }
        return sourceText.replace(
            ALLOWED_GUZHENREN_EXT_CLIENT_BOOTSTRAP,
            ALLOWED_GUZHENREN_EXT_PLACEHOLDER
        );
    }

    private static boolean containsAcceptedGuzhenrenExtBootstrap(String sourceText) {
        return sourceText.contains("if (FMLEnvironment.dist == Dist.CLIENT)")
            && sourceText.contains("runClientBootstrap(modContainer);")
            && sourceText.contains("private static void runClientBootstrap(ModContainer modContainer)")
            && sourceText.contains("Class.forName(")
            && sourceText.contains(ALLOWED_GUZHENREN_EXT_CLIENT_BOOTSTRAP)
            && sourceText.contains("registerConfigScreen")
            && sourceText.contains("ClassNotFoundException");
    }

    private static String stripComments(String sourceText) {
        StringBuilder sanitized = new StringBuilder(sourceText.length());
        boolean inLineComment = false;
        boolean inBlockComment = false;
        boolean inString = false;
        boolean inChar = false;
        boolean escaped = false;
        for (int index = 0; index < sourceText.length(); index++) {
            char current = sourceText.charAt(index);
            char next = index + 1 < sourceText.length() ? sourceText.charAt(index + 1) : '\0';

            if (inLineComment) {
                if (current == '\n' || current == '\r') {
                    inLineComment = false;
                    sanitized.append(current);
                } else {
                    sanitized.append(' ');
                }
                continue;
            }

            if (inBlockComment) {
                if (current == '*' && next == '/') {
                    sanitized.append("  ");
                    inBlockComment = false;
                    index++;
                } else if (current == '\n' || current == '\r') {
                    sanitized.append(current);
                } else {
                    sanitized.append(' ');
                }
                continue;
            }

            if (inString) {
                sanitized.append(current);
                if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if (current == '"') {
                    inString = false;
                }
                continue;
            }

            if (inChar) {
                sanitized.append(current);
                if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if (current == '\'') {
                    inChar = false;
                }
                continue;
            }

            if (current == '/' && next == '/') {
                sanitized.append("  ");
                inLineComment = true;
                index++;
                continue;
            }

            if (current == '/' && next == '*') {
                sanitized.append("  ");
                inBlockComment = true;
                index++;
                continue;
            }

            sanitized.append(current);
            if (current == '"') {
                inString = true;
            } else if (current == '\'') {
                inChar = true;
            }
        }
        return sanitized.toString();
    }

    private static boolean isAuditedBoundary(String normalizedPath) {
        return matchesFile(normalizedPath, GUZHENREN_EXT_FILE)
            || isUnderRoot(normalizedPath, PLAN2_ROOT)
            || isUnderRoot(normalizedPath, XIANQIAO_ROOT);
    }

    private static boolean isExplicitClientPath(String normalizedPath) {
        return normalizedPath.contains(CLIENT_SEGMENT);
    }

    private static boolean matchesFile(String normalizedPath, String expectedPath) {
        return normalizedPath.equals(expectedPath) || normalizedPath.endsWith("/" + expectedPath);
    }

    private static boolean isUnderRoot(String normalizedPath, String root) {
        return normalizedPath.startsWith(root + "/") || normalizedPath.contains("/" + root + "/");
    }

    private static String normalizePath(Path path) {
        return path.toString().replace('\\', '/');
    }
}
