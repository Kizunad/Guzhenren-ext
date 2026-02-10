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
    public static final String KONGQIAO_BUTTON_FORGE =
        "screen.guzhenrenext.kongqiao.button.forge";
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
    public static final String NIANTOU_TITLE =
        "screen.guzhenrenext.niantou.title";
    public static final String NIANTOU_RESULT_LABEL =
        "screen.guzhenrenext.niantou.result_label";
    public static final String NIANTOU_HISTORY_LABEL =
        "screen.guzhenrenext.niantou.history_label";
    public static final String NIANTOU_OUTPUT_LABEL =
        "screen.guzhenrenext.niantou.output_label";
    public static final String NIANTOU_BUTTON_IDENTIFY =
        "screen.guzhenrenext.niantou.button.identify";
    public static final String NIANTOU_BUTTON_DERIVE_SHAZHAO =
        "screen.guzhenrenext.niantou.button.derive_shazhao";
    public static final String NIANTOU_TIME_LABEL =
        "screen.guzhenrenext.niantou.time_label";
    public static final String NIANTOU_COST_LABEL =
        "screen.guzhenrenext.niantou.cost_label";

    public static final String FORGE_TITLE =
        "screen.guzhenrenext.forge.title";
    public static final String FORGE_BUTTON_CLAIM =
        "screen.guzhenrenext.forge.button.claim";
    public static final String FORGE_BUTTON_CANCEL =
        "screen.guzhenrenext.forge.button.cancel";
    public static final String FORGE_BUTTON_HELP =
        "screen.guzhenrenext.forge.button.help";
    public static final String HELP_TITLE =
        "screen.guzhenrenext.forge.help.title";
    public static final String HELP_TAB_OVERVIEW =
        "screen.guzhenrenext.forge.help.tab.overview";
    public static final String HELP_TAB_QUALITY =
        "screen.guzhenrenext.forge.help.tab.quality";
    public static final String HELP_TAB_COMBAT =
        "screen.guzhenrenext.forge.help.tab.combat";
    public static final String HELP_TAB_GROWTH =
        "screen.guzhenrenext.forge.help.tab.growth";
    public static final String HELP_OVERVIEW_TITLE =
        "screen.guzhenrenext.forge.help.overview.title";
    public static final String HELP_OVERVIEW_STEP1 =
        "screen.guzhenrenext.forge.help.overview.step1";
    public static final String HELP_OVERVIEW_STEP2 =
        "screen.guzhenrenext.forge.help.overview.step2";
    public static final String HELP_OVERVIEW_STEP3 =
        "screen.guzhenrenext.forge.help.overview.step3";
    public static final String HELP_OVERVIEW_STEP4 =
        "screen.guzhenrenext.forge.help.overview.step4";
    public static final String HELP_OVERVIEW_STEP5 =
        "screen.guzhenrenext.forge.help.overview.step5";
    public static final String HELP_OVERVIEW_KEYS_TITLE =
        "screen.guzhenrenext.forge.help.overview.keys_title";
    public static final String HELP_OVERVIEW_KEY_SELECT =
        "screen.guzhenrenext.forge.help.overview.key_select";
    public static final String HELP_OVERVIEW_KEY_MODE =
        "screen.guzhenrenext.forge.help.overview.key_mode";
    public static final String HELP_OVERVIEW_KEY_RECALL =
        "screen.guzhenrenext.forge.help.overview.key_recall";
    public static final String HELP_OVERVIEW_KEY_RESTORE =
        "screen.guzhenrenext.forge.help.overview.key_restore";
    public static final String HELP_OVERVIEW_KEY_HUD =
        "screen.guzhenrenext.forge.help.overview.key_hud";
    public static final String HELP_OVERVIEW_NOTE =
        "screen.guzhenrenext.forge.help.overview.note";
    public static final String HELP_QUALITY_TITLE =
        "screen.guzhenrenext.forge.help.quality.title";
    public static final String HELP_QUALITY_DESC =
        "screen.guzhenrenext.forge.help.quality.desc";
    public static final String HELP_QUALITY_ENTRY =
        "screen.guzhenrenext.forge.help.quality.entry";
    public static final String HELP_QUALITY_DAOHEN_TITLE =
        "screen.guzhenrenext.forge.help.quality.daohen_title";
    public static final String HELP_QUALITY_DAOHEN_DESC =
        "screen.guzhenrenext.forge.help.quality.daohen_desc";
    public static final String HELP_QUALITY_DAOHEN_NOTE =
        "screen.guzhenrenext.forge.help.quality.daohen_note";
    public static final String HELP_COMBAT_TITLE =
        "screen.guzhenrenext.forge.help.combat.title";
    public static final String HELP_COMBAT_MODE_ORBIT =
        "screen.guzhenrenext.forge.help.combat.mode_orbit";
    public static final String HELP_COMBAT_MODE_HOVER =
        "screen.guzhenrenext.forge.help.combat.mode_hover";
    public static final String HELP_COMBAT_MODE_GUARD =
        "screen.guzhenrenext.forge.help.combat.mode_guard";
    public static final String HELP_COMBAT_MODE_HUNT =
        "screen.guzhenrenext.forge.help.combat.mode_hunt";
    public static final String HELP_COMBAT_IMPRINT_TITLE =
        "screen.guzhenrenext.forge.help.combat.imprint_title";
    public static final String HELP_COMBAT_IMPRINT_DESC =
        "screen.guzhenrenext.forge.help.combat.imprint_desc";
    public static final String HELP_GROWTH_TITLE =
        "screen.guzhenrenext.forge.help.growth.title";
    public static final String HELP_GROWTH_EXP_DESC =
        "screen.guzhenrenext.forge.help.growth.exp_desc";
    public static final String HELP_GROWTH_LEVEL_DESC =
        "screen.guzhenrenext.forge.help.growth.level_desc";
    public static final String HELP_GROWTH_BREAKTHROUGH_DESC =
        "screen.guzhenrenext.forge.help.growth.breakthrough_desc";
    public static final String HELP_BUTTON_CLOSE =
        "screen.guzhenrenext.forge.help.button.close";

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
