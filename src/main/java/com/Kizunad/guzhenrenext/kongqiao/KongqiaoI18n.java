package com.Kizunad.guzhenrenext.kongqiao;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.Kizunad.guzhenrenext.GuzhenrenExt;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Locale;
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
    public static final String FORGE_DAO_TOTAL =
        "screen.guzhenrenext.forge.dao.total";
    public static final String FORGE_DAO_NONE =
        "screen.guzhenrenext.forge.dao.none";
    public static final String FORGE_DAO_SEPARATOR =
        "screen.guzhenrenext.forge.dao.separator";
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
    public static final String HELP_TAB_BENMING =
        "screen.guzhenrenext.forge.help.tab.benming";
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
    public static final String HELP_OVERVIEW_KEY_BENMING_ACTION =
        "screen.guzhenrenext.forge.help.overview.key_benming_action";
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
    public static final String HELP_BENMING_TITLE =
        "screen.guzhenrenext.forge.help.benming.title";
    public static final String HELP_BENMING_OVERVIEW =
        "screen.guzhenrenext.forge.help.benming.overview";
    public static final String HELP_BENMING_GUIDE_TITLE =
        "screen.guzhenrenext.forge.help.benming.guide.title";
    public static final String HELP_BENMING_GUIDE_DESC =
        "screen.guzhenrenext.forge.help.benming.guide.desc";
    public static final String HELP_BENMING_BOND_TITLE =
        "screen.guzhenrenext.forge.help.benming.bond.title";
    public static final String HELP_BENMING_BOND_ENTRY =
        "screen.guzhenrenext.forge.help.benming.bond.entry";
    public static final String HELP_BENMING_BOND_AFTER_SUCCESS =
        "screen.guzhenrenext.forge.help.benming.bond.after_success";
    public static final String HELP_BENMING_FAIL_BOND =
        "screen.guzhenrenext.forge.help.benming.fail.bond";
    public static final String HELP_BENMING_RESONANCE_TITLE =
        "screen.guzhenrenext.forge.help.benming.resonance.title";
    public static final String HELP_BENMING_RESONANCE_GUIDE =
        "screen.guzhenrenext.forge.help.benming.resonance.guide";
    public static final String HELP_BENMING_RESONANCE_OFFENSE =
        "screen.guzhenrenext.forge.help.benming.resonance.offense";
    public static final String HELP_BENMING_RESONANCE_DEFENSE =
        "screen.guzhenrenext.forge.help.benming.resonance.defense";
    public static final String HELP_BENMING_RESONANCE_SPIRIT =
        "screen.guzhenrenext.forge.help.benming.resonance.spirit";
    public static final String HELP_BENMING_RISK_TITLE =
        "screen.guzhenrenext.forge.help.benming.risk.title";
    public static final String HELP_BENMING_OVERLOAD_WARNING =
        "screen.guzhenrenext.forge.help.benming.overload.warning";
    public static final String HELP_BENMING_BACKLASH_RECOVERY =
        "screen.guzhenrenext.forge.help.benming.backlash.recovery";
    public static final String HELP_BENMING_BURST_TITLE =
        "screen.guzhenrenext.forge.help.benming.burst.title";
    public static final String HELP_BENMING_BURST_WINDOW =
        "screen.guzhenrenext.forge.help.benming.burst.window";
    public static final String HELP_BENMING_BURST_EFFECT =
        "screen.guzhenrenext.forge.help.benming.burst.effect";
    public static final String HELP_BENMING_AFTERSHOCK_RULE =
        "screen.guzhenrenext.forge.help.benming.aftershock.rule";
    public static final String HELP_BENMING_FAIL_TITLE =
        "screen.guzhenrenext.forge.help.benming.fail.title";
    public static final String HELP_BENMING_FAIL_NO_SELECTED_SWORD =
        "screen.guzhenrenext.forge.help.benming.fail.no_selected_sword";
    public static final String HELP_BENMING_FAIL_NOT_BONDED =
        "screen.guzhenrenext.forge.help.benming.fail.not_bonded";
    public static final String HELP_BENMING_FAIL_BOND_COOLDOWN =
        "screen.guzhenrenext.forge.help.benming.fail.bond_cooldown";
    public static final String HELP_BENMING_FAIL_RESOURCE_INSUFFICIENT =
        "screen.guzhenrenext.forge.help.benming.fail.resource_insufficient";
    public static final String HELP_BENMING_FAIL_RESONANCE_LOCKED =
        "screen.guzhenrenext.forge.help.benming.fail.resonance_locked";
    public static final String HELP_BENMING_FAIL_OVERLOAD_TOO_HIGH =
        "screen.guzhenrenext.forge.help.benming.fail.overload_too_high";
    public static final String HELP_BENMING_FAIL_BURST_NOT_READY =
        "screen.guzhenrenext.forge.help.benming.fail.burst_not_ready";
    public static final String HELP_BENMING_FAIL_BURST_COOLDOWN =
        "screen.guzhenrenext.forge.help.benming.fail.burst_cooldown";
    public static final String HELP_BENMING_FAIL_AFTERSHOCK_ACTIVE =
        "screen.guzhenrenext.forge.help.benming.fail.aftershock_active";
    public static final String HELP_BENMING_FAIL_WITHDRAWN_OR_ILLEGAL_DETACH =
        "screen.guzhenrenext.forge.help.benming.fail.withdrawn_or_illegal_detach";
    public static final String HELP_BUTTON_CLOSE =
        "screen.guzhenrenext.forge.help.button.close";
    public static final String BENMING_GUIDE_BOND_START =
        "message.guzhenrenext.flyingsword.benming.guide.bond_start";
    public static final String BENMING_GUIDE_AFTER_BOND =
        "message.guzhenrenext.flyingsword.benming.guide.after_bond";
    public static final String BENMING_GUIDE_BOND_FAIL_NEXT_STEP =
        "message.guzhenrenext.flyingsword.benming.guide.bond_fail_next_step";
    public static final String BENMING_GUIDE_RESONANCE_FIRST_CHOICE =
        "message.guzhenrenext.flyingsword.benming.guide.resonance_first_choice";
    public static final String BENMING_GUIDE_OVERLOAD_FIRST_WARNING =
        "message.guzhenrenext.flyingsword.benming.guide.overload_first_warning";
    public static final String BENMING_GUIDE_BACKLASH_FIRST_TIME =
        "message.guzhenrenext.flyingsword.benming.guide.backlash_first_time";
    public static final String BENMING_GUIDE_BURST_READY_FIRST_TIME =
        "message.guzhenrenext.flyingsword.benming.guide.burst_ready_first_time";
    public static final String BENMING_GUIDE_AFTERSHOCK_FIRST_TIME =
        "message.guzhenrenext.flyingsword.benming.guide.aftershock_first_time";
    public static final String BENMING_FEEDBACK_SYSTEM_DISABLED =
        "message.guzhenrenext.flyingsword.benming.feedback.system_disabled";
    public static final String BENMING_FEEDBACK_QUERY_SUCCESS =
        "message.guzhenrenext.flyingsword.benming.feedback.query.success";
    public static final String BENMING_FEEDBACK_BOND_SUCCESS =
        "message.guzhenrenext.flyingsword.benming.feedback.bond.success";
    public static final String BENMING_FEEDBACK_ACTIVE_UNBIND_SUCCESS =
        "message.guzhenrenext.flyingsword.benming.feedback.active_unbind.success";
    public static final String BENMING_FEEDBACK_FORCED_UNBIND_SUCCESS =
        "message.guzhenrenext.flyingsword.benming.feedback.forced_unbind.success";
    public static final String BENMING_FEEDBACK_RESONANCE_SWITCH_SUCCESS =
        "message.guzhenrenext.flyingsword.benming.feedback.resonance_switch.success";
    public static final String BENMING_FEEDBACK_BURST_ATTEMPT_SUCCESS =
        "message.guzhenrenext.flyingsword.benming.feedback.burst_attempt.success";
    public static final String BENMING_FEEDBACK_INVALID_QUERY =
        "message.guzhenrenext.flyingsword.benming.feedback.invalid.query";
    public static final String BENMING_FEEDBACK_INVALID_RESONANCE_SWITCH =
        "message.guzhenrenext.flyingsword.benming.feedback.invalid.resonance_switch";
    public static final String BENMING_FEEDBACK_INVALID_BURST_ATTEMPT =
        "message.guzhenrenext.flyingsword.benming.feedback.invalid.burst_attempt";
    public static final String BENMING_FEEDBACK_INVALID_TARGET =
        "message.guzhenrenext.flyingsword.benming.feedback.invalid.target";
    public static final String BENMING_FEEDBACK_FAIL_NOT_BONDED =
        "message.guzhenrenext.flyingsword.benming.feedback.fail.not_bonded";
    public static final String BENMING_FEEDBACK_FAIL_ALREADY_BONDED =
        "message.guzhenrenext.flyingsword.benming.feedback.fail.already_bonded";
    public static final String BENMING_FEEDBACK_FAIL_BOUND_TO_OTHER =
        "message.guzhenrenext.flyingsword.benming.feedback.fail.bound_to_other";
    public static final String BENMING_FEEDBACK_FAIL_NOT_PLAYER_BENMING =
        "message.guzhenrenext.flyingsword.benming.feedback.fail.not_player_benming";
    public static final String BENMING_FEEDBACK_FAIL_MULTIPLE_BONDED =
        "message.guzhenrenext.flyingsword.benming.feedback.fail.multiple_bonded";
    public static final String BENMING_FEEDBACK_FAIL_ACTIVE_UNBIND_COST =
        "message.guzhenrenext.flyingsword.benming.feedback.fail.active_unbind_cost";
    public static final String BENMING_FEEDBACK_FAIL_RITUAL_RESOURCE =
        "message.guzhenrenext.flyingsword.benming.feedback.fail.ritual_resource";
    public static final String BENMING_FEEDBACK_FAIL_RITUAL_STATE =
        "message.guzhenrenext.flyingsword.benming.feedback.fail.ritual_state";
    public static final String BENMING_FEEDBACK_FAIL_RITUAL_COOLDOWN =
        "message.guzhenrenext.flyingsword.benming.feedback.fail.ritual_cooldown";
    public static final String BENMING_FEEDBACK_FAIL_RITUAL_DUPLICATE =
        "message.guzhenrenext.flyingsword.benming.feedback.fail.ritual_duplicate";
    public static final String BENMING_FEEDBACK_FAIL_RITUAL_TARGET_MISMATCH =
        "message.guzhenrenext.flyingsword.benming.feedback.fail.ritual_target_mismatch";
    public static final String BENMING_FEEDBACK_FAIL_STATE_ATTACHMENT_MISSING =
        "message.guzhenrenext.flyingsword.benming.feedback.fail.state_attachment_missing";
    public static final String BENMING_FEEDBACK_FAIL_NO_TARGET_SWORD =
        "message.guzhenrenext.flyingsword.benming.feedback.fail.no_target_sword";
    public static final String BENMING_FEEDBACK_FAIL_TARGET_NOT_CURRENT_BENMING =
        "message.guzhenrenext.flyingsword.benming.feedback.fail.target_not_current_benming";
    public static final String BENMING_FEEDBACK_FAIL_BOND_STATE_INVALID =
        "message.guzhenrenext.flyingsword.benming.feedback.fail.bond_state_invalid";
    public static final String BENMING_FEEDBACK_FAIL_RESONANCE_TYPE_INVALID =
        "message.guzhenrenext.flyingsword.benming.feedback.fail.resonance_type_invalid";
    public static final String BENMING_FEEDBACK_FAIL_BURST_COOLDOWN =
        "message.guzhenrenext.flyingsword.benming.feedback.fail.burst_cooldown";
    public static final String BENMING_FEEDBACK_FAIL_BURST_RESOURCE =
        "message.guzhenrenext.flyingsword.benming.feedback.fail.burst_resource";
    public static final String BENMING_FEEDBACK_FAIL_BURST_OVERLOAD =
        "message.guzhenrenext.flyingsword.benming.feedback.fail.burst_overload";
    public static final String BENMING_HUD_BADGE_MARK =
        "hud.guzhenrenext.flyingsword.benming.badge.mark";
    public static final String BENMING_HUD_BADGE_OVERLOAD_WARNING =
        "hud.guzhenrenext.flyingsword.benming.badge.overload_warning";
    public static final String BENMING_HUD_BADGE_BURST_READY =
        "hud.guzhenrenext.flyingsword.benming.badge.burst_ready";
    public static final String BENMING_HUD_BADGE_AFTERSHOCK =
        "hud.guzhenrenext.flyingsword.benming.badge.aftershock";
    public static final String BENMING_HUD_RESONANCE_OFFENSE_SHORT =
        "hud.guzhenrenext.flyingsword.benming.resonance.offense.short";
    public static final String BENMING_HUD_RESONANCE_DEFENSE_SHORT =
        "hud.guzhenrenext.flyingsword.benming.resonance.defense.short";
    public static final String BENMING_HUD_RESONANCE_SPIRIT_SHORT =
        "hud.guzhenrenext.flyingsword.benming.resonance.spirit.short";
    public static final String BENMING_HUD_OVERLOAD_TEXT =
        "hud.guzhenrenext.flyingsword.benming.overload.text";
    public static final String RESONANCE_OFFENSE_NAME =
        "screen.guzhenrenext.forge.help.resonance.offense.name";
    public static final String RESONANCE_OFFENSE_DESC =
        "screen.guzhenrenext.forge.help.resonance.offense.desc";
    public static final String RESONANCE_OFFENSE_COLOR_SEMANTIC =
        "screen.guzhenrenext.forge.help.resonance.offense.color_semantic";
    public static final String RESONANCE_DEFENSE_NAME =
        "screen.guzhenrenext.forge.help.resonance.defense.name";
    public static final String RESONANCE_DEFENSE_DESC =
        "screen.guzhenrenext.forge.help.resonance.defense.desc";
    public static final String RESONANCE_DEFENSE_COLOR_SEMANTIC =
        "screen.guzhenrenext.forge.help.resonance.defense.color_semantic";
    public static final String RESONANCE_SPIRIT_NAME =
        "screen.guzhenrenext.forge.help.resonance.spirit.name";
    public static final String RESONANCE_SPIRIT_DESC =
        "screen.guzhenrenext.forge.help.resonance.spirit.desc";
    public static final String RESONANCE_SPIRIT_COLOR_SEMANTIC =
        "screen.guzhenrenext.forge.help.resonance.spirit.color_semantic";

    public static final String DAO_HUN_DAO = "guzhenrenext.dao.hundao";
    public static final String DAO_GU_DAO = "guzhenrenext.dao.gudao";
    public static final String DAO_MU_DAO = "guzhenrenext.dao.mudao";
    public static final String DAO_HUO_DAO = "guzhenrenext.dao.yandao";
    public static final String DAO_JIAN_DAO = "guzhenrenext.dao.jiandao";
    public static final String DAO_DAO_DAO = "guzhenrenext.dao.daodao";
    public static final String DAO_XUE_DAO = "guzhenrenext.dao.xuedao";
    public static final String DAO_LIAN_DAO = "guzhenrenext.dao.liandao";
    public static final String DAO_LV_DAO = "guzhenrenext.dao.lvdao";
    public static final String DAO_TIAN_DAO = "guzhenrenext.dao.tiandao";
    public static final String DAO_QI_DAO = "guzhenrenext.dao.qidao";
    public static final String DAO_NU_DAO = "guzhenrenext.dao.nudao";
    public static final String DAO_TOU_DAO = "guzhenrenext.dao.toudao";
    public static final String DAO_XIN_DAO = "guzhenrenext.dao.xindao";
    public static final String DAO_YING_DAO = "guzhenrenext.dao.yingdao";
    public static final String DAO_XING_DAO = "guzhenrenext.dao.xingdao";
    public static final String DAO_YUE_DAO = "guzhenrenext.dao.yuedao";
    public static final String DAO_YUN_DAO = "guzhenrenext.dao.yundao";
    public static final String DAO_YUN_DAO_2 = "guzhenrenext.dao.yundao2";
    public static final String DAO_ZHI_DAO = "guzhenrenext.dao.zhidao";
    public static final String DAO_ZHEN_DAO = "guzhenrenext.dao.zhendao";
    public static final String DAO_ZHOU_DAO = "guzhenrenext.dao.zhoudao";
    public static final String DAO_JIN_DAO = "guzhenrenext.dao.jindao";
    public static final String DAO_JIN_DAO_2 = "guzhenrenext.dao.jindao2";
    public static final String DAO_YIN_DAO = "guzhenrenext.dao.yindao";
    public static final String DAO_REN_DAO = "guzhenrenext.dao.rendao";
    public static final String DAO_LI_DAO = "guzhenrenext.dao.lidao";
    public static final String DAO_LEI_DAO = "guzhenrenext.dao.leidao";
    public static final String DAO_DU_DAO = "guzhenrenext.dao.dudao";
    public static final String DAO_SHUI_DAO = "guzhenrenext.dao.shuidao";
    public static final String DAO_TU_DAO = "guzhenrenext.dao.tudao";
    public static final String DAO_YU_DAO = "guzhenrenext.dao.yudao";
    public static final String DAO_SHI_DAO = "guzhenrenext.dao.shidao";
    public static final String DAO_DAN_DAO = "guzhenrenext.dao.dandao";
    public static final String DAO_HUA_DAO = "guzhenrenext.dao.huadao";
    public static final String DAO_AN_DAO = "guzhenrenext.dao.andao";
    public static final String DAO_HUAN_DAO = "guzhenrenext.dao.huandao";
    public static final String DAO_MENG_DAO = "guzhenrenext.dao.mengdao";
    public static final String DAO_BING_DAO = "guzhenrenext.dao.bingdao";
    public static final String DAO_BING_XUE_DAO = "guzhenrenext.dao.bingxuedao";
    public static final String DAO_BIAN_HUA_DAO = "guzhenrenext.dao.bianhuadao";
    public static final String DAO_XU_DAO = "guzhenrenext.dao.xudao";
    public static final String DAO_FENG_DAO = "guzhenrenext.dao.fengdao";
    public static final String DAO_GUANG_DAO = "guzhenrenext.dao.guangdao";
    public static final String DAO_GENERIC = "guzhenrenext.dao.generic";

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

    /**
     * 统一的“翻译组件 + 内置语言文件回退”读取入口。
     * <p>
     * 设计语义：
     * <ul>
     *     <li>先尝试 {@link #text(String, Object...)}，保持与游戏运行时语言系统一致。</li>
     *     <li>仅当运行时翻译链路不可用（如纯 JVM 测试）时，回退读取 jar 内置 lang json。</li>
     *     <li>回退顺序固定：{@code zh_cn -> 当前 Locale -> 当前语言 -> en_us}。</li>
     *     <li>格式化保持 {@code Locale.ROOT}，且格式失败时返回原模板。</li>
     * </ul>
     * </p>
     *
     * @param key 语言键
     * @param missingValue 所有回退失败时返回的兜底文本
     * @param args 可选占位符
     * @return 本地化文本
     */
    public static String localizedTextWithBundledFallback(
        final String key,
        final String missingValue,
        final Object... args
    ) {
        final String namespace = resolveTranslationNamespace(key);
        return localizedTextWithBundledFallback(key, namespace, missingValue, args);
    }

    /**
     * 与 {@link #localizedTextWithBundledFallback(String, String, Object...)} 语义一致，
     * 但允许调用方显式指定资源命名空间。
     *
     * @param key 语言键
     * @param namespace 语言资源命名空间（例如 {@code guzhenrenext}/{@code minecraft}）
     * @param missingValue 所有回退失败时返回的兜底文本
     * @param args 可选占位符
     * @return 本地化文本
     */
    public static String localizedTextWithBundledFallback(
        final String key,
        final String namespace,
        final String missingValue,
        final Object... args
    ) {
        try {
            return text(key, args).getString();
        } catch (RuntimeException | LinkageError exception) {
            return localizedTextFromBundledLang(key, namespace, missingValue, args);
        }
    }

    private static String localizedTextFromBundledLang(
        final String key,
        final String namespace,
        final String missingValue,
        final Object... args
    ) {
        for (final String localeKey : bundledLocaleFallbackOrder()) {
            final String localized = readBundledLangValue(namespace, localeKey, key);
            if (localized != null && !localized.isBlank()) {
                if (args == null || args.length == 0) {
                    return localized;
                }
                try {
                    return String.format(Locale.ROOT, localized, args);
                } catch (IllegalFormatException exception) {
                    return localized;
                }
            }
        }
        return missingValue;
    }

    private static String resolveTranslationNamespace(final String key) {
        if (key == null || key.isBlank()) {
            return "minecraft";
        }
        if (key.contains("." + GuzhenrenExt.MODID + ".") || key.startsWith(GuzhenrenExt.MODID + ".")) {
            return GuzhenrenExt.MODID;
        }
        return "minecraft";
    }

    private static List<String> bundledLocaleFallbackOrder() {
        final List<String> localeKeys = new ArrayList<>();
        addLocaleCandidate(localeKeys, "zh_cn");
        addLocaleCandidate(localeKeys, Locale.getDefault().toString());
        addLocaleCandidate(localeKeys, Locale.getDefault().getLanguage());
        addLocaleCandidate(localeKeys, "en_us");
        return localeKeys;
    }

    private static void addLocaleCandidate(
        final List<String> localeKeys,
        final String rawLocale
    ) {
        if (rawLocale == null || rawLocale.isBlank()) {
            return;
        }
        String normalized = rawLocale.replace('-', '_').toLowerCase(Locale.ROOT);
        if ("zh".equals(normalized)) {
            normalized = "zh_cn";
        } else if ("en".equals(normalized)) {
            normalized = "en_us";
        }
        if (!localeKeys.contains(normalized)) {
            localeKeys.add(normalized);
        }
    }

    private static String readBundledLangValue(
        final String namespace,
        final String localeKey,
        final String key
    ) {
        final String resourcePath = "assets/" + namespace + "/lang/" + localeKey + ".json";
        try (
            InputStream stream = KongqiaoI18n.class
                .getClassLoader()
                .getResourceAsStream(resourcePath)
        ) {
            if (stream == null) {
                return null;
            }
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                final JsonObject translations = JsonParser.parseReader(reader).getAsJsonObject();
                final JsonElement localized = translations.get(key);
                return localized == null ? null : localized.getAsString();
            }
        } catch (IOException | RuntimeException exception) {
            return null;
        }
    }
}
