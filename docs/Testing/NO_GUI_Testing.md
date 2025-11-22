# Neoforge 1.21.1 NO_GUI Testing Guide

This guide explains how to run GameTests in NO_GUI mode (Headless Server) for this project.

## 1. Prerequisites

The [build.gradle](file:///home/kiz/Code/java/Guzhenren-ext/build.gradle) has been configured to support `runGameTestServer` and correctly process [neoforge.mods.toml](file:///home/kiz/Code/java/Guzhenren-ext/src/main/resources/META-INF/neoforge.mods.toml).

## 2. Creating a GameTest

Create a Java class in your mod package (e.g., `com.example.guzhenrenext`) and annotate it with `@GameTestHolder`.

```java
package com.example.guzhenrenext;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder("guzhenrenext")
public class ExampleGameTests {

    @GameTest(template = "empty")
    public void testAlwaysPass(GameTestHelper helper) {
        helper.succeed();
    }
}
```

## 3. Structure Files

GameTests require structure files (`.nbt` or [.snbt](file:///home/kiz/Code/java/Guzhenren-ext/src/main/resources/data/guzhenrenext/gametest/structures/examplegametests.empty.snbt)).
Place your structure files in:
`src/main/resources/data/<modid>/structure/<test_name>.nbt`

For the example above:
- Mod ID: `guzhenrenext`
- Template: `empty`
- Path: [src/main/resources/data/guzhenrenext/structure/examplegametests.empty.nbt](file:///home/kiz/Code/java/Guzhenren-ext/src/main/resources/data/guzhenrenext/structure/examplegametests.empty.nbt)

> **Note:** The structure name is typically `<classname>.<templatename>`. Lowercase is recommended.

## 4. Running Tests

Run the following Gradle command:

```bash
./gradlew runGameTestServer
```

This will launch a headless Minecraft server, run the registered GameTests, and output the results in the console.

## 5. Troubleshooting

- **Missing test structure**: Ensure the `.nbt` file is in the correct path `data/<modid>/structure/`.
- **Mod loading error**: Check [build.gradle](file:///home/kiz/Code/java/Guzhenren-ext/build.gradle) `processResources` configuration (already fixed in this project).
