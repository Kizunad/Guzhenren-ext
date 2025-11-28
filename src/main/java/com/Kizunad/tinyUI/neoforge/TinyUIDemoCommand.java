package com.Kizunad.tinyUI.neoforge;

import com.Kizunad.tinyUI.demo.DemoRegistry;
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
