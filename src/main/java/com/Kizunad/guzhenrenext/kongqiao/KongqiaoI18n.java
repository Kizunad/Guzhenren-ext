package com.Kizunad.guzhenrenext.kongqiao;

import net.minecraft.network.chat.Component;

/**
 * 空窍及相关 UI 文本的 i18n key 集中管理，避免硬编码字符串。
 */
public final class KongqiaoI18n {

    public static final String COMMON_PLAYER_INVENTORY =
        "screen.guzhenrenext.common.player_inventory";
    public static final String COMMON_FEED_BUTTON =
        "screen.guzhenrenext.common.button.feed";
    public static final String KONGQIAO_TITLE =
        "screen.guzhenrenext.kongqiao.title";
    public static final String KONGQIAO_HINT =
        "screen.guzhenrenext.kongqiao.hint";
    public static final String KONGQIAO_BUTTON_EXPAND =
        "screen.guzhenrenext.kongqiao.button.expand";
    public static final String KONGQIAO_BUTTON_ATTACK =
        "screen.guzhenrenext.kongqiao.button.attack";
    public static final String ATTACK_TITLE =
        "screen.guzhenrenext.attack.title";
    public static final String ATTACK_BUTTON_SWAP =
        "screen.guzhenrenext.attack.button.swap";
    public static final String ATTACK_BUTTON_RETURN =
        "screen.guzhenrenext.attack.button.return";
    public static final String FEED_TITLE =
        "screen.guzhenrenext.feed.title";
    public static final String FEED_BUTTON_AUTO =
        "screen.guzhenrenext.feed.button.auto";

    private KongqiaoI18n() {}

    /**
     * 构建一个翻译组件，便于统一传入 TinyUI 控件。
     *
     * @param key 语言键
     * @param args 可选占位符
     * @return 对应的翻译组件
     */
    public static Component text(String key, Object... args) {
        return Component.translatable(key, args);
    }
}
