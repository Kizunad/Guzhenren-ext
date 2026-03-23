package com.Kizunad.guzhenrenext.plan2;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Plan2LangTestSupport {

    static final String ZH_CN_FILE =
        "src/main/resources/assets/guzhenrenext/lang/zh_cn.json";

    private static final String FARMING_BLOCKS_FILE =
        "src/main/java/com/Kizunad/guzhenrenext/xianqiao/farming/FarmingBlocks.java";
    private static final String FARMING_ITEMS_FILE =
        "src/main/java/com/Kizunad/guzhenrenext/xianqiao/farming/FarmingItems.java";
    private static final String XIANQIAO_ITEMS_FILE =
        "src/main/java/com/Kizunad/guzhenrenext/xianqiao/item/XianqiaoItems.java";
    private static final String XIANQIAO_ENTITIES_FILE =
        "src/main/java/com/Kizunad/guzhenrenext/xianqiao/spirit/XianqiaoEntities.java";

    private static final String MODID_PREFIX_BLOCK = "block.guzhenrenext.";
    private static final String MODID_PREFIX_ITEM = "item.guzhenrenext.";
    private static final String MODID_PREFIX_ENTITY = "entity.guzhenrenext.";

    private static final String PILL_RANGE_START = "cui_sheng_dan";
    private static final String PILL_RANGE_END = "su_ti_ni";

    private static final Set<String> FARMING_PLAIN_ITEM_IDS = Set.of(
        "lei_ying_sha",
        "xue_po_li",
        "jin_sui_xie"
    );

    private static final Set<String> REQUIRED_FARMING_BLOCK_IDS = Set.of(
        "alchemy_furnace",
        "qing_ya_grass",
        "ning_xue_gen",
        "ju_yuan_flower",
        "xi_sui_vine",
        "tie_pi_bamboo",
        "huo_ling_zhi_mushroom",
        "bing_xin_grass",
        "huan_du_mushroom",
        "ying_tai_lichen",
        "ci_vine",
        "jian_ye_grass",
        "chen_shui_lily_pad",
        "di_long_berry_bush",
        "feng_xin_zi",
        "lei_gu_sapling",
        "shi_yin_grass",
        "chun_yang_flower",
        "yan_shou_cocoa",
        "wang_you_grass",
        "she_yan_melon_stem",
        "lightning_attracting_fern",
        "man_eating_spore_blossom",
        "spirit_tree_spruce_sapling",
        "cave_vines"
    );

    private static final Set<String> REQUIRED_XIANQIAO_MATERIAL_IDS = Set.of(
        "ling_tie_xie",
        "chi_tong_sha",
        "xuan_mei_jing",
        "chi_xiao_fen",
        "yue_yin_pian",
        "han_shuang_yan",
        "yan_sui_zha",
        "feng_wen_yu_pian",
        "di_mai_sha",
        "yun_mu_pian",
        "qing_mu_xin",
        "you_ying_mo",
        "xing_hui_chen",
        "yu_sui_tuan",
        "ling_liu_kuai",
        "xuan_bing_jing",
        "gui_yuan_li",
        "zhen_qiao_xuan_tie_he",
        "ni_mai_xing_yun_he",
        "tian_lei_ci_mu",
        "kong_shi_hei_jing",
        "jiu_zhuan_sui_jing",
        "di_mai_long_jing",
        "wan_xiang_jin_sha",
        "shi_sha_liu_li",
        "you_hun_ning_po_shi",
        "dao_yuan_mu_kuang"
    );

    private static final Set<String> REQUIRED_DEEP_ENTITY_IDS = Set.of(
        "treasure_mink",
        "mutated_spirit_fox",
        "aperture_guardian",
        "sacrificial_sheep",
        "dao_devouring_mite",
        "stone_vein_sentinel",
        "mimic_slime",
        "void_walker",
        "calamity_beast",
        "symbiotic_spirit_bee"
    );

    private static final Pattern BLOCK_REGISTER_PATTERN = Pattern.compile(
        "BLOCKS\\.register\\(\\s*\\\"([a-z0-9_]+)\\\""
    );
    private static final Pattern SHALLOW_CROP_PATTERN = Pattern.compile(
        "registerShallowCrop\\(\\\"([a-z0-9_]+)\\\"\\)"
    );
    private static final Pattern ITEM_REGISTER_PATTERN = Pattern.compile(
        "ITEMS\\.register\\(\\s*\\\"([a-z0-9_]+)\\\""
    );
    private static final Pattern REGISTER_ITEM_PATTERN = Pattern.compile(
        "ITEMS\\.registerItem\\(\\s*\\\"([a-z0-9_]+)\\\""
    );
    private static final Pattern ENTITY_ID_PATTERN = Pattern.compile(
        "public static final String [A-Z0-9_]+_ID = \\\"([a-z0-9_]+)\\\""
    );

    private Plan2LangTestSupport() {
    }

    static Map<String, String> readCurrentZhCnEntries() throws IOException {
        String text = Files.readString(Path.of(ZH_CN_FILE), StandardCharsets.UTF_8);
        JsonObject root = JsonParser.parseString(text).getAsJsonObject();
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            JsonElement value = entry.getValue();
            if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                result.put(entry.getKey(), value.getAsString());
            }
        }
        return result;
    }

    static Map<String, String> deepCopyLangEntries(Map<String, String> source) {
        return new LinkedHashMap<>(source);
    }

    static Set<String> collectCurrentRequiredNameKeys() throws IOException {
        Set<String> requiredKeys = new LinkedHashSet<>();
        requiredKeys.addAll(collectRequiredBlockNameKeys());
        requiredKeys.addAll(collectRequiredFarmingItemNameKeys());
        requiredKeys.addAll(collectRequiredXianqiaoMaterialNameKeys());
        requiredKeys.addAll(collectRequiredDeepEntityNameKeys());
        return requiredKeys;
    }

    private static Set<String> collectRequiredBlockNameKeys() throws IOException {
        String source = Files.readString(Path.of(FARMING_BLOCKS_FILE), StandardCharsets.UTF_8);
        Set<String> blockIds = new LinkedHashSet<>();
        blockIds.addAll(extractIds(source, BLOCK_REGISTER_PATTERN));
        blockIds.addAll(extractIds(source, SHALLOW_CROP_PATTERN));
        assertRequiredIdsPresent("FarmingBlocks", REQUIRED_FARMING_BLOCK_IDS, blockIds);

        Set<String> scopedIds = new LinkedHashSet<>();
        for (String id : blockIds) {
            if (REQUIRED_FARMING_BLOCK_IDS.contains(id)) {
                scopedIds.add(id);
            }
        }
        return withPrefix(scopedIds, MODID_PREFIX_BLOCK);
    }

    private static Set<String> collectRequiredFarmingItemNameKeys() throws IOException {
        String source = Files.readString(Path.of(FARMING_ITEMS_FILE), StandardCharsets.UTF_8);
        List<String> registeredItemsInOrder = extractIdsInOrder(source, ITEM_REGISTER_PATTERN);
        int start = registeredItemsInOrder.indexOf(PILL_RANGE_START);
        int end = registeredItemsInOrder.indexOf(PILL_RANGE_END);
        if (start < 0 || end < 0 || start > end) {
            throw new IllegalStateException("无法在 FarmingItems 中定位丹药范围: " + PILL_RANGE_START + " -> " + PILL_RANGE_END);
        }

        assertRequiredIdsPresent(
            "FarmingItems plain items",
            FARMING_PLAIN_ITEM_IDS,
            new LinkedHashSet<>(registeredItemsInOrder)
        );

        Set<String> ids = new LinkedHashSet<>(FARMING_PLAIN_ITEM_IDS);
        ids.addAll(registeredItemsInOrder.subList(start, end + 1));
        return withPrefix(ids, MODID_PREFIX_ITEM);
    }

    private static Set<String> collectRequiredXianqiaoMaterialNameKeys() throws IOException {
        String source = Files.readString(Path.of(XIANQIAO_ITEMS_FILE), StandardCharsets.UTF_8);
        Set<String> allRegisterItemIds = extractIds(source, REGISTER_ITEM_PATTERN);
        Set<String> ids = new LinkedHashSet<>();
        for (String id : allRegisterItemIds) {
            if (REQUIRED_XIANQIAO_MATERIAL_IDS.contains(id)) {
                ids.add(id);
            }
        }
        assertRequiredIdsPresent("XianqiaoItems materials", REQUIRED_XIANQIAO_MATERIAL_IDS, allRegisterItemIds);
        return withPrefix(ids, MODID_PREFIX_ITEM);
    }

    private static Set<String> collectRequiredDeepEntityNameKeys() throws IOException {
        String source = Files.readString(Path.of(XIANQIAO_ENTITIES_FILE), StandardCharsets.UTF_8);
        Set<String> allEntityIds = extractIds(source, ENTITY_ID_PATTERN);
        Set<String> ids = new LinkedHashSet<>();
        for (String id : allEntityIds) {
            if (REQUIRED_DEEP_ENTITY_IDS.contains(id)) {
                ids.add(id);
            }
        }
        assertRequiredIdsPresent("XianqiaoEntities deep entities", REQUIRED_DEEP_ENTITY_IDS, allEntityIds);
        return withPrefix(ids, MODID_PREFIX_ENTITY);
    }

    private static void assertRequiredIdsPresent(String sourceName, Set<String> requiredIds, Set<String> allFoundIds) {
        List<String> missing = new ArrayList<>();
        for (String requiredId : requiredIds) {
            if (!allFoundIds.contains(requiredId)) {
                missing.add(requiredId);
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException("锚点文件缺少预期注册项(" + sourceName + "): " + missing);
        }
    }

    private static Set<String> extractIds(String source, Pattern pattern) {
        Set<String> ids = new LinkedHashSet<>();
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            ids.add(matcher.group(1));
        }
        return ids;
    }

    private static List<String> extractIdsInOrder(String source, Pattern pattern) {
        List<String> ids = new ArrayList<>();
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            ids.add(matcher.group(1));
        }
        return ids;
    }

    private static Set<String> withPrefix(Set<String> ids, String prefix) {
        Set<String> keys = new LinkedHashSet<>();
        for (String id : ids) {
            keys.add(prefix + id);
        }
        return keys;
    }
}
