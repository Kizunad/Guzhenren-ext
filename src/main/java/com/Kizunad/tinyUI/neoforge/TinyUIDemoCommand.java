package com.Kizunad.tinyUI.neoforge;

import com.Kizunad.tinyUI.demo.ComplexLayoutMenu;
import com.Kizunad.tinyUI.demo.ComplexLayoutScreen;
import com.Kizunad.tinyUI.demo.DemoRegistry;
import com.Kizunad.tinyUI.demo.SimpleContainerScreen;
import com.Kizunad.tinyUI.theme.Theme;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * 客户端命令：/tinyui demo <id> 打开 tinyUI 示例界面。
 */
public final class TinyUIDemoCommand {

    private static final int DEMO_ID_COMPLEX_CONTAINER = 6;
    private static final int DEMO_ID_SIMPLE_CONTAINER = 7;
    private static final int DEFAULT_CUSTOM_SLOT_COUNT = 63;
    private static final int SIMPLE_CONTAINER_SLOT_COUNT = 27;

    private TinyUIDemoCommand() {
    }

    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("tinyui")
                        .then(Commands.literal("demo")
                                .then(Commands.argument("id", IntegerArgumentType.integer(0))
                                        .executes(ctx -> openDemo(ctx.getArgument("id", Integer.class)))))
        );
    }

    private static int openDemo(final int id) {
        final Minecraft mc = Minecraft.getInstance();
        
        // Special case: Demo 6 is the Complex Layout with real containers
        if (id == DEMO_ID_COMPLEX_CONTAINER) {
            if (mc.player == null) {
                return 0;
            }
            // Create menu with 63 custom slots (7 rows of 9)
            ComplexLayoutMenu menu = new ComplexLayoutMenu(
                0,
                mc.player.getInventory(),
                DEFAULT_CUSTOM_SLOT_COUNT
            );
            ComplexLayoutScreen screen = new ComplexLayoutScreen(
                menu, 
                mc.player.getInventory(), 
                Component.literal("Complex Layout"), 
                Theme.vanilla()
            );
            mc.setScreen(screen);
            return 1;
        }
        // Demo 7: 简化容器布局（3x9 + 玩家背包）
        if (id == DEMO_ID_SIMPLE_CONTAINER) {
            if (mc.player == null) {
                return 0;
            }
            ComplexLayoutMenu menu = new ComplexLayoutMenu(
                0,
                mc.player.getInventory(),
                SIMPLE_CONTAINER_SLOT_COUNT
            );
            SimpleContainerScreen screen = new SimpleContainerScreen(
                menu,
                mc.player.getInventory(),
                Component.literal("Simple Container Demo"),
                Theme.vanilla()
            );
            mc.setScreen(screen);
            return 1;
        }
        
        // Regular UIRoot demos
        final DemoRegistry demos = new DemoRegistry(Theme.vanilla());
        return demos.create(id)
                .map(root -> {
                    mc.setScreen(new TinyUIScreen(Component.literal("tinyUI demo " + id), root));
                    return 1;
                })
                .orElseGet(() -> {
                    if (mc.player != null) {
                        mc.player.displayClientMessage(
                                Component.literal("tinyUI demo id not found: " + id), false);
                    }
                    return 0;
                });
    }
}
