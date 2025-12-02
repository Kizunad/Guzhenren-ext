package com.Kizunad.customNPCs.config;

import net.minecraft.world.level.GameRules;

/**
 * 自定义 gamerule 注册处。
 */
public final class ModGameRules {

    private ModGameRules() {}

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_CUSTOM_NPC_LOAD_CHUNK =
        GameRules.register(
            "allowCustomNPCLoadChunk",
            GameRules.Category.MOBS,
            GameRules.BooleanValue.create(true)
        );

    public static void register() {
        // 触发静态字段初始化。当前注册 API 为即时注册，无额外逻辑。
    }
}
