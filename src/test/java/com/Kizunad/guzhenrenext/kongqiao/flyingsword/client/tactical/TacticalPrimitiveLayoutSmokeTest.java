package com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.tactical;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TacticalPrimitiveLayoutSmokeTest {

    private static final int BAR_WIDTH = 120;
    private static final int BAR_HEIGHT = 24;
    private static final int BADGE_WIDTH = 72;
    private static final int CARD_WIDTH = 180;
    private static final int CARD_HEIGHT = 96;
    private static final int FONT_WIDTH = 6;
    private static final int FONT_LINE_HEIGHT = 9;
    private static final int SEARCH_DEPTH = 8;

    private static final String TACTICAL_THEME_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.tactical.TacticalTheme";
    private static final String TACTICAL_BAR_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.tactical.TacticalBar";
    private static final String TACTICAL_BADGE_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.tactical.TacticalBadge";
    private static final String TACTICAL_ROUTE_CARD_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.tactical.TacticalRouteCard";
    private static final String TACTICAL_BADGE_SPEC_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.tactical.TacticalBadgeSpec";
    private static final String TACTICAL_TONE_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.tactical.TacticalTone";
    private static final String UI_RENDER_CONTEXT_CLASS_NAME =
        "com.Kizunad.tinyUI.core.UIRenderContext";
    private static final String COMPONENT_CLASS_NAME =
        "net.minecraft.network.chat.Component";
    private static final String PLAYER_CLASS_RESOURCE =
        "net/minecraft/world/entity/player/Player.class";
    private static final Path MAIN_CLASSES = Path.of("build/classes/java/main");
    private static final Path MAIN_RESOURCES = Path.of("build/resources/main");
    private static final Path ARTIFACT_MANIFEST =
        Path.of("build/tmp/createMinecraftArtifacts/nfrt_artifact_manifest.properties");
    private static Path cachedMinecraftJarPath;

    @Test
    void tacticalBarClampsRatioAndUsesDangerFillToken() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object bar = api.newBar();
        final RecordingContext context = new RecordingContext(api);

        api.setFrame(bar, 0, 0, BAR_WIDTH, BAR_HEIGHT);
        api.setComponent(bar, "setLabel", "过载");
        api.setComponent(bar, "setValueText", "100%");
        api.setTone(bar, "DANGER");
        api.invoke(bar, "setFillRatio", 1.5F);
        api.render(bar, context);

        assertEquals(1.0F, api.floatResult(bar, "getFillRatio"));
        final RectCall fillRect = context.findFirstRect(api.themeColor("barFillColor", "DANGER"));
        assertNotNull(fillRect);
        assertEquals(BAR_WIDTH - api.themeInt("panelPadding") * 2, fillRect.width());
        assertEquals(api.themeInt("barHeight"), fillRect.height());
    }

    @Test
    void tacticalBadgeUsesBenmingTokenColorsAndSuggestedWidth() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object badge = api.newBadge();
        final RecordingContext context = new RecordingContext(api);

        api.setFrame(badge, 0, 0, BADGE_WIDTH, api.themeInt("badgeHeight"));
        api.setComponent(badge, "setLabel", "本命");
        api.setTone(badge, "BENMING");
        api.render(badge, context);

        assertTrue(api.intResult(badge, "suggestWidth") >= api.themeInt("badgeMinWidth"));
        assertNotNull(context.findFirstRect(api.themeColor("badgeBackgroundColor", "BENMING")));
        assertNotNull(
            context.findRectByWidthAndColor(
                api.themeInt("badgeMarkerWidth"),
                api.themeColor("accentColor", "BENMING")
            )
        );
        final TextCall textCall = context.findFirstText(api.themeColor("badgeTextColor", "BENMING"));
        assertNotNull(textCall);
        assertEquals("本命", textCall.text());
    }

    @Test
    void tacticalRouteCardAcceptsBadgeListAndRendersLabels() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object routeCard = api.newRouteCard();
        final RecordingContext context = new RecordingContext(api);

        api.setFrame(routeCard, 0, 0, CARD_WIDTH, CARD_HEIGHT);
        api.setComponent(routeCard, "setTitle", "总览");
        api.setComponent(routeCard, "setSummary", "查看本命状态\n队列压力与风险入口");
        api.setComponent(routeCard, "setActionText", "回车进入");
        api.invoke(
            routeCard,
            "setBadges",
            List.of(
                api.newBadgeSpec("主路由", "INFO"),
                api.newBadgeSpec("警讯", "WARNING")
            )
        );
        api.invoke(routeCard, "setActive", true);
        api.invoke(routeCard, "onLayoutUpdated");
        api.render(routeCard, context);

        assertTrue(context.rectCalls().size() >= 4);
        assertTrue(context.hasText("总览"));
        assertTrue(context.hasText("主路由"));
        assertTrue(context.hasText("警讯"));
    }

    private static final class RuntimeApi {

        private final URLClassLoader classLoader;
        private final Class<?> themeClass;
        private final Class<?> barClass;
        private final Class<?> badgeClass;
        private final Class<?> routeCardClass;
        private final Class<?> badgeSpecClass;
        private final Class<?> toneClass;
        private final Class<?> renderContextClass;
        private final Class<?> componentClass;
        private final Object theme;

        private RuntimeApi(
            final URLClassLoader classLoader,
            final Class<?> themeClass,
            final Class<?> barClass,
            final Class<?> badgeClass,
            final Class<?> routeCardClass,
            final Class<?> badgeSpecClass,
            final Class<?> toneClass,
            final Class<?> renderContextClass,
            final Class<?> componentClass,
            final Object theme
        ) {
            this.classLoader = classLoader;
            this.themeClass = themeClass;
            this.barClass = barClass;
            this.badgeClass = badgeClass;
            this.routeCardClass = routeCardClass;
            this.badgeSpecClass = badgeSpecClass;
            this.toneClass = toneClass;
            this.renderContextClass = renderContextClass;
            this.componentClass = componentClass;
            this.theme = theme;
        }

        static RuntimeApi create() throws Exception {
            final URLClassLoader classLoader = buildRuntimeClassLoader();
            final Class<?> themeClass = classLoader.loadClass(TACTICAL_THEME_CLASS_NAME);
            final Class<?> barClass = classLoader.loadClass(TACTICAL_BAR_CLASS_NAME);
            final Class<?> badgeClass = classLoader.loadClass(TACTICAL_BADGE_CLASS_NAME);
            final Class<?> routeCardClass = classLoader.loadClass(TACTICAL_ROUTE_CARD_CLASS_NAME);
            final Class<?> badgeSpecClass = classLoader.loadClass(TACTICAL_BADGE_SPEC_CLASS_NAME);
            final Class<?> toneClass = classLoader.loadClass(TACTICAL_TONE_CLASS_NAME);
            final Class<?> renderContextClass = classLoader.loadClass(UI_RENDER_CONTEXT_CLASS_NAME);
            final Class<?> componentClass = classLoader.loadClass(COMPONENT_CLASS_NAME);
            final Object theme = themeClass.getMethod("coldConsole").invoke(null);
            return new RuntimeApi(
                classLoader,
                themeClass,
                barClass,
                badgeClass,
                routeCardClass,
                badgeSpecClass,
                toneClass,
                renderContextClass,
                componentClass,
                theme
            );
        }

        Object newBar() throws Exception {
            return barClass.getConstructor(themeClass).newInstance(theme);
        }

        Object newBadge() throws Exception {
            return badgeClass.getConstructor(themeClass).newInstance(theme);
        }

        Object newRouteCard() throws Exception {
            return routeCardClass.getConstructor(themeClass).newInstance(theme);
        }

        Object newBadgeSpec(final String text, final String toneName) throws Exception {
            return badgeSpecClass.getMethod("of", String.class, toneClass)
                .invoke(null, text, tone(toneName));
        }

        void setFrame(
            final Object target,
            final int x,
            final int y,
            final int width,
            final int height
        ) throws Exception {
            target.getClass().getMethod(
                "setFrame",
                int.class,
                int.class,
                int.class,
                int.class
            ).invoke(target, x, y, width, height);
        }

        void setComponent(final Object target, final String methodName, final String text)
            throws Exception {
            target.getClass().getMethod(methodName, componentClass)
                .invoke(target, component(text));
        }

        void setTone(final Object target, final String toneName) throws Exception {
            target.getClass().getMethod("setTone", toneClass)
                .invoke(target, tone(toneName));
        }

        void render(final Object target, final RecordingContext context) throws Exception {
            final Object proxy = Proxy.newProxyInstance(
                classLoader,
                new Class<?>[] { renderContextClass },
                context
            );
            target.getClass().getMethod(
                "render",
                renderContextClass,
                double.class,
                double.class,
                float.class
            ).invoke(target, proxy, 0.0D, 0.0D, 0.0F);
        }

        Object invoke(final Object target, final String methodName, final Object... args)
            throws Exception {
            final Method method = findMethod(target.getClass(), methodName, args.length);
            return method.invoke(target, args);
        }

        int themeInt(final String methodName) throws Exception {
            return (int) themeClass.getMethod(methodName).invoke(theme);
        }

        int themeColor(final String methodName, final String toneName) throws Exception {
            return (int) themeClass.getMethod(methodName, toneClass)
                .invoke(theme, tone(toneName));
        }

        int intResult(final Object target, final String methodName) throws Exception {
            return (int) target.getClass().getMethod(methodName).invoke(target);
        }

        float floatResult(final Object target, final String methodName) throws Exception {
            return (float) target.getClass().getMethod(methodName).invoke(target);
        }

        String componentText(final Object component) throws Exception {
            return (String) componentClass.getMethod("getString").invoke(component);
        }

        private Object component(final String text) throws Exception {
            return componentClass.getMethod("literal", String.class).invoke(null, text);
        }

        private Object tone(final String toneName) {
            for (final Object constant : toneClass.getEnumConstants()) {
                if (((Enum<?>) constant).name().equals(toneName)) {
                    return constant;
                }
            }
            throw new IllegalArgumentException("unknown tone: " + toneName);
        }

        private static Method findMethod(
            final Class<?> owner,
            final String methodName,
            final int argCount
        ) {
            for (final Method method : owner.getMethods()) {
                if (method.getName().equals(methodName)
                    && method.getParameterCount() == argCount) {
                    return method;
                }
            }
            throw new IllegalArgumentException(methodName + " not found on " + owner.getName());
        }
    }

    private static final class RecordingContext implements InvocationHandler {

        private final RuntimeApi api;
        private final List<RectCall> rectCalls = new ArrayList<>();
        private final List<TextCall> textCalls = new ArrayList<>();

        private RecordingContext(final RuntimeApi api) {
            this.api = api;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args)
            throws Throwable {
            final String methodName = method.getName();
            if (method.getDeclaringClass() == Object.class) {
                if ("toString".equals(methodName)) {
                    return "RecordingContext";
                }
                if ("hashCode".equals(methodName)) {
                    return System.identityHashCode(this);
                }
                if ("equals".equals(methodName)) {
                    return proxy == args[0];
                }
            }
            if ("pushState".equals(methodName) || "popState".equals(methodName)
                || "drawNinePatch".equals(methodName)) {
                return null;
            }
            if ("drawRect".equals(methodName)) {
                rectCalls.add(
                    new RectCall(
                        (int) args[0],
                        (int) args[1],
                        (int) args[2],
                        (int) args[3],
                        (int) args[4]
                    )
                );
                return null;
            }
            if ("drawText".equals(methodName) || "drawTextScaled".equals(methodName)) {
                final Object textArg = args[0];
                final String text = textArg instanceof String
                    ? (String) textArg
                    : api.componentText(textArg);
                textCalls.add(new TextCall(text, (int) args[1], (int) args[2], (int) args[3]));
                return null;
            }
            if ("measureTextWidth".equals(methodName)) {
                return api.componentText(args[0]).length() * FONT_WIDTH;
            }
            if ("getFontLineHeight".equals(methodName)) {
                return FONT_LINE_HEIGHT;
            }
            if (method.getReturnType() == boolean.class) {
                return false;
            }
            if (method.getReturnType() == int.class) {
                return 0;
            }
            return null;
        }

        RectCall findFirstRect(final int color) {
            return rectCalls.stream().filter(call -> call.argbColor() == color).findFirst().orElse(null);
        }

        RectCall findRectByWidthAndColor(final int width, final int color) {
            return rectCalls.stream()
                .filter(call -> call.width() == width && call.argbColor() == color)
                .findFirst()
                .orElse(null);
        }

        TextCall findFirstText(final int color) {
            return textCalls.stream().filter(call -> call.argbColor() == color).findFirst().orElse(null);
        }

        boolean hasText(final String text) {
            return textCalls.stream().anyMatch(call -> call.text().contains(text));
        }

        List<RectCall> rectCalls() {
            return rectCalls;
        }
    }

    private record RectCall(int x, int y, int width, int height, int argbColor) {
    }

    private record TextCall(String text, int x, int y, int argbColor) {
    }

    private static URLClassLoader buildRuntimeClassLoader() throws IOException {
        final List<URL> urls = new ArrayList<>();
        final Path mainClassesPath = MAIN_CLASSES.toAbsolutePath();
        if (!mainClassesPath.toFile().exists()) {
            throw new IOException("缺少主类输出目录: " + mainClassesPath);
        }
        urls.add(mainClassesPath.toUri().toURL());

        final Path mainResourcesPath = MAIN_RESOURCES.toAbsolutePath();
        if (mainResourcesPath.toFile().exists()) {
            urls.add(mainResourcesPath.toUri().toURL());
        }

        final Properties props = new Properties();
        final Path manifestPath = ARTIFACT_MANIFEST.toAbsolutePath();
        if (!manifestPath.toFile().exists()) {
            throw new IOException("缺少依赖清单: " + manifestPath);
        }
        try (InputStream input = Files.newInputStream(manifestPath)) {
            props.load(input);
        }

        for (final String key : props.stringPropertyNames()) {
            final String jarPath = props.getProperty(key);
            if (jarPath == null || jarPath.isBlank()) {
                continue;
            }
            final Path absoluteJarPath = Path.of(jarPath).toAbsolutePath();
            if (absoluteJarPath.toFile().exists()) {
                urls.add(absoluteJarPath.toUri().toURL());
            }
        }

        urls.add(resolveMinecraftRuntimeJar().toUri().toURL());
        return new URLClassLoader(
            urls.toArray(new URL[0]),
            ClassLoader.getPlatformClassLoader()
        );
    }

    private static synchronized Path resolveMinecraftRuntimeJar() throws IOException {
        if (cachedMinecraftJarPath != null && cachedMinecraftJarPath.toFile().exists()) {
            return cachedMinecraftJarPath;
        }

        final List<Path> searchRoots = new ArrayList<>();
        final String userHome = System.getProperty("user.home");
        searchRoots.add(
            Path.of(userHome, ".gradle", "caches", "neoformruntime", "intermediate_results")
        );
        searchRoots.add(
            Path.of(userHome, ".gradle", "caches", "fabric-loom", "minecraftMaven")
        );

        for (final Path root : searchRoots) {
            final Path matched = findJarContainingResource(root, PLAYER_CLASS_RESOURCE);
            if (matched != null) {
                cachedMinecraftJarPath = matched;
                return matched;
            }
        }

        throw new IOException("未找到包含 net.minecraft.world.entity.player.Player 的运行时 Jar");
    }

    private static Path findJarContainingResource(final Path root, final String resource)
        throws IOException {
        if (root == null || !root.toFile().exists()) {
            return null;
        }
        try (var stream = Files.walk(root, SEARCH_DEPTH)) {
            final List<Path> candidates = stream
                .filter(path -> path.toString().endsWith(".jar"))
                .toList();
            for (final Path candidate : candidates) {
                if (jarContainsResource(candidate, resource)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private static boolean jarContainsResource(final Path jarPath, final String resource) {
        try (var jar = new java.util.jar.JarFile(jarPath.toFile())) {
            return jar.getEntry(resource) != null;
        } catch (IOException ignored) {
            return false;
        }
    }
}
