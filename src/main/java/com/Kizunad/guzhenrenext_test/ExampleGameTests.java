package com.Kizunad.guzhenrenext_test;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder("guzhenrenext")
public class ExampleGameTests {

    @GameTest(template = "empty")
    public void testAlwaysPass(GameTestHelper helper) {
        // 基座：确保 GameTest 环境可正常运行。
        helper.succeed();
    }
}
