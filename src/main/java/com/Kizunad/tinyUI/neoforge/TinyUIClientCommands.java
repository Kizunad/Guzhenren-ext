package com.Kizunad.tinyUI.neoforge;

import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

/**
 * 客户端命令注册：/tinyui demo <id> 打开 tinyUI 示例。
 */
public final class TinyUIClientCommands {

    private TinyUIClientCommands() {
    }

    public static void onRegisterClientCommands(final RegisterClientCommandsEvent event) {
        TinyUIDemoCommand.register(event.getDispatcher());
    }
}
