package com.Kizunad.guzhenrenext.xianqiao.opening;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * 开局地形推断服务。
 * <p>
 * 该服务属于“纯推断层”，输入为 Task2 冻结后的 {@link ResolvedOpeningProfile}，
 * 输出为稳定的 biome-key 倾向排序，不接触世界、不读取注册表运行态、不做随机漂移。
 * </p>
 * <p>
 * 设计要点：
 * </p>
 * <ul>
 *     <li>核心结果采用 {@link BiomeKey}（生产侧可直接消费的 biome-key 对象模型），而非仅字符串列表。</li>
 *     <li>推断主干采用 metadata 语义映射（family/climate/moisture），避免“道痕 -> biome”大魔法表硬绑定。</li>
 *     <li>仅保留窄范围硬编码特例：本命蛊 id 的少量已知映射 + 兼容旧策略的默认回退链。</li>
 *     <li>同分并列按 biome id 字典序固定打破，确保 deterministic。</li>
 * </ul>
 */
public final class BiomeInferenceService {

    private static final String BENMING_PREFIX = "benminggu:";
    private static final String EMPTY_DAO_KEY = "";
    private static final String KEY_SUFFIX_ALT_VERSION = "2";

    private static final double EPSILON = 1.0E-9D;
    private static final double DAO_MARK_SCORE_SCALE = 100.0D;
    private static final double BENMING_PRIORITY_BOOST = 60.0D;
    private static final double BENMING_MATCHED_DAO_MARK_BOOST = 20.0D;
    private static final double FAMILY_PRIMARY_WEIGHT = 2.0D;
    private static final double FAMILY_SECONDARY_WEIGHT = 1.0D;
    private static final double FAMILY_TERTIARY_WEIGHT = 1.0D;
    private static final double CLIMATE_MATCH_WEIGHT = 0.6D;
    private static final double MOISTURE_MATCH_WEIGHT = 0.4D;
    private static final double DEFAULT_FALLBACK_SCORE_START = 80.0D;
    private static final double DEFAULT_FALLBACK_SCORE_STEP = 1.0D;

    private static final long BENMING_ID_BASE = 1L;
    private static final long BENMING_SPECIAL_CASE_ID_ONE = 1L;
    private static final long BENMING_SPECIAL_CASE_ID_TWO = 2L;
    private static final long BENMING_SPECIAL_CASE_ID_THREE = 3L;
    private static final long BENMING_SPECIAL_CASE_ID_FOUR = 4L;

    /**
     * 本命蛊 id 的窄特例：仅覆盖已确认语义，避免把整表硬编码在推断层。
     */
    private static final Map<Long, String> BENMING_SPECIAL_CASE_DAO = Map.of(
        BENMING_SPECIAL_CASE_ID_ONE,
        "tudao",
        BENMING_SPECIAL_CASE_ID_TWO,
        "shuidao",
        BENMING_SPECIAL_CASE_ID_THREE,
        "mudao",
        BENMING_SPECIAL_CASE_ID_FOUR,
        "yandao"
    );

    /**
     * 回退链与 legacy 2x2 策略保持一致，保证无命中时行为可解释且兼容。
     */
    private static final List<BiomeKey> DEFAULT_FALLBACK_CHAIN = List.of(
        BiomeKey.minecraft("desert"),
        BiomeKey.minecraft("savanna"),
        BiomeKey.minecraft("plains"),
        BiomeKey.minecraft("badlands"),
        BiomeKey.minecraft("forest"),
        BiomeKey.minecraft("taiga"),
        BiomeKey.minecraft("meadow"),
        BiomeKey.minecraft("swamp")
    );

    /**
     * Vanilla biome metadata map（v1）。
     * <p>
     * 该表不是“道痕直连 biome”魔法表，而是“biome 语义标签 + 气候标签”数据层。
     * 后续接入 registry/tag 时可替换/扩展该 catalog，不影响推断主流程。
     * </p>
     */
    private static final List<BiomeMetadata> BIOME_METADATA = List.of(
        biome("desert", ClimateBand.HOT, MoistureBand.DRY, BiomeFamily.ARID),
        biome("savanna", ClimateBand.HOT, MoistureBand.DRY, BiomeFamily.ARID, BiomeFamily.TEMPERATE),
        biome("plains", ClimateBand.TEMPERATE, MoistureBand.NORMAL, BiomeFamily.TEMPERATE),
        biome("badlands", ClimateBand.HOT, MoistureBand.DRY, BiomeFamily.ARID, BiomeFamily.MOUNTAIN),
        biome("forest", ClimateBand.TEMPERATE, MoistureBand.NORMAL, BiomeFamily.FOREST, BiomeFamily.TEMPERATE),
        biome("taiga", ClimateBand.COLD, MoistureBand.NORMAL, BiomeFamily.FOREST, BiomeFamily.COLD),
        biome("meadow", ClimateBand.TEMPERATE, MoistureBand.NORMAL, BiomeFamily.TEMPERATE, BiomeFamily.MOUNTAIN),
        biome("swamp", ClimateBand.TEMPERATE, MoistureBand.WET, BiomeFamily.SWAMP, BiomeFamily.WET),
        biome("mangrove_swamp", ClimateBand.HOT, MoistureBand.WET, BiomeFamily.SWAMP, BiomeFamily.WET),
        biome("river", ClimateBand.TEMPERATE, MoistureBand.WET, BiomeFamily.WET),
        biome("snowy_plains", ClimateBand.COLD, MoistureBand.NORMAL, BiomeFamily.COLD, BiomeFamily.TEMPERATE),
        biome("jagged_peaks", ClimateBand.COLD, MoistureBand.DRY, BiomeFamily.MOUNTAIN, BiomeFamily.COLD),
        biome("cherry_grove", ClimateBand.TEMPERATE, MoistureBand.NORMAL, BiomeFamily.MYSTIC, BiomeFamily.FOREST),
        biome(
            "dark_forest",
            ClimateBand.TEMPERATE,
            MoistureBand.WET,
            BiomeFamily.FOREST,
            BiomeFamily.SWAMP,
            BiomeFamily.MYSTIC
        ),
        biome("mushroom_fields", ClimateBand.TEMPERATE, MoistureBand.WET, BiomeFamily.MYSTIC, BiomeFamily.WET)
    );

    private static final List<String> BENMING_DAO_RING = buildBenmingDaoRing();

    /**
     * 推断主入口。
     *
     * @param profile 冻结画像
     * @return 稳定排序结果
     */
    public BiomePreferenceResult infer(ResolvedOpeningProfile profile) {
        if (profile == null) {
            throw new IllegalArgumentException("profile 不能为空");
        }

        AscensionConditionSnapshot snapshot = profile.conditionSnapshot();
        String benmingPriorityDaoKey = resolveBenmingPriorityDaoKey(snapshot);
        Map<String, Double> daoMarks = normalizeDaoMarks(snapshot.daoMarks());

        if (daoMarks.isEmpty() && benmingPriorityDaoKey.isBlank()) {
            return buildDefaultFallbackResult(EMPTY_DAO_KEY);
        }

        Map<BiomeKey, Double> scoreByBiome = new HashMap<>();
        Map<BiomeKey, Boolean> benmingBoostedByBiome = new HashMap<>();
        initializeScoreContainers(scoreByBiome, benmingBoostedByBiome);

        applyDaoMarkScores(daoMarks, scoreByBiome);
        applyBenmingBoost(benmingPriorityDaoKey, daoMarks, scoreByBiome, benmingBoostedByBiome);

        List<RankedBiome> rankedBiomes = rankByScore(scoreByBiome, benmingBoostedByBiome);
        if (rankedBiomes.isEmpty() || rankedBiomes.getFirst().score() <= EPSILON) {
            return buildDefaultFallbackResult(benmingPriorityDaoKey);
        }
        return buildResult(rankedBiomes, false, benmingPriorityDaoKey);
    }

    private static void initializeScoreContainers(
        Map<BiomeKey, Double> scoreByBiome,
        Map<BiomeKey, Boolean> benmingBoostedByBiome
    ) {
        for (BiomeMetadata metadata : BIOME_METADATA) {
            scoreByBiome.put(metadata.biomeKey(), 0.0D);
            benmingBoostedByBiome.put(metadata.biomeKey(), false);
        }
    }

    private static Map<String, Double> normalizeDaoMarks(Map<String, Double> rawDaoMarks) {
        if (rawDaoMarks == null || rawDaoMarks.isEmpty()) {
            return Map.of();
        }
        TreeMap<String, Double> normalized = new TreeMap<>();
        for (Map.Entry<String, Double> entry : rawDaoMarks.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            String canonicalKey = canonicalizeDaoKey(entry.getKey());
            if (canonicalKey.isBlank()) {
                continue;
            }
            double value = sanitizeNonNegativeFinite(entry.getValue());
            if (value <= EPSILON) {
                continue;
            }
            normalized.put(canonicalKey, value);
        }
        return Map.copyOf(normalized);
    }

    private static String canonicalizeDaoKey(String daoKey) {
        if (daoKey == null) {
            return EMPTY_DAO_KEY;
        }
        String normalized = daoKey.trim();
        if (normalized.isBlank()) {
            return EMPTY_DAO_KEY;
        }
        return normalized;
    }

    private static void applyDaoMarkScores(Map<String, Double> daoMarks, Map<BiomeKey, Double> scoreByBiome) {
        double total = sumValues(daoMarks);
        if (total <= EPSILON) {
            return;
        }
        TreeMap<String, Double> sortedDaoMarks = new TreeMap<>(daoMarks);
        for (Map.Entry<String, Double> entry : sortedDaoMarks.entrySet()) {
            DaoAffinity affinity = resolveDaoAffinity(entry.getKey());
            double normalizedWeight = entry.getValue() / total;
            double scoreBudget = normalizedWeight * DAO_MARK_SCORE_SCALE;
            applyAffinityScore(scoreByBiome, affinity, scoreBudget);
        }
    }

    private static void applyBenmingBoost(
        String benmingPriorityDaoKey,
        Map<String, Double> daoMarks,
        Map<BiomeKey, Double> scoreByBiome,
        Map<BiomeKey, Boolean> benmingBoostedByBiome
    ) {
        if (benmingPriorityDaoKey == null || benmingPriorityDaoKey.isBlank()) {
            return;
        }
        DaoAffinity benmingAffinity = resolveDaoAffinity(benmingPriorityDaoKey);
        double boostBudget = BENMING_PRIORITY_BOOST;
        if (daoMarks.containsKey(benmingPriorityDaoKey)) {
            boostBudget += BENMING_MATCHED_DAO_MARK_BOOST;
        }
        applyAffinityScore(scoreByBiome, benmingAffinity, boostBudget);

        for (BiomeMetadata metadata : BIOME_METADATA) {
            if (matchesAffinity(metadata, benmingAffinity)) {
                benmingBoostedByBiome.put(metadata.biomeKey(), true);
            }
        }
    }

    private static void applyAffinityScore(
        Map<BiomeKey, Double> scoreByBiome,
        DaoAffinity affinity,
        double scoreBudget
    ) {
        if (scoreBudget <= EPSILON) {
            return;
        }
        for (BiomeMetadata metadata : BIOME_METADATA) {
            double matchWeight = 0.0D;
            if (metadata.families().contains(affinity.primaryFamily())) {
                matchWeight += FAMILY_PRIMARY_WEIGHT;
            }
            if (affinity.secondaryFamily() != null && metadata.families().contains(affinity.secondaryFamily())) {
                matchWeight += FAMILY_SECONDARY_WEIGHT;
            }
            if (affinity.tertiaryFamily() != null && metadata.families().contains(affinity.tertiaryFamily())) {
                matchWeight += FAMILY_TERTIARY_WEIGHT;
            }
            if (affinity.preferredClimate() == metadata.climateBand()) {
                matchWeight += CLIMATE_MATCH_WEIGHT;
            }
            if (affinity.preferredMoisture() == metadata.moistureBand()) {
                matchWeight += MOISTURE_MATCH_WEIGHT;
            }
            if (matchWeight <= EPSILON) {
                continue;
            }
            double current = scoreByBiome.getOrDefault(metadata.biomeKey(), 0.0D);
            scoreByBiome.put(metadata.biomeKey(), current + (scoreBudget * matchWeight));
        }
    }

    private static boolean matchesAffinity(BiomeMetadata metadata, DaoAffinity affinity) {
        if (metadata.families().contains(affinity.primaryFamily())) {
            return true;
        }
        if (affinity.secondaryFamily() != null && metadata.families().contains(affinity.secondaryFamily())) {
            return true;
        }
        if (affinity.tertiaryFamily() != null && metadata.families().contains(affinity.tertiaryFamily())) {
            return true;
        }
        if (affinity.preferredClimate() == metadata.climateBand()) {
            return true;
        }
        return affinity.preferredMoisture() == metadata.moistureBand();
    }

    private static DaoAffinity resolveDaoAffinity(String daoKey) {
        String key = canonicalizeDaoKey(daoKey);
        if (key.contains("shui")) {
            return DaoAffinity.of(
                BiomeFamily.WET,
                BiomeFamily.SWAMP,
                BiomeFamily.TEMPERATE,
                ClimateBand.TEMPERATE,
                MoistureBand.WET
            );
        }
        if (key.contains("bing") || key.contains("xue")) {
            return DaoAffinity.of(
                BiomeFamily.COLD,
                BiomeFamily.MOUNTAIN,
                BiomeFamily.WET,
                ClimateBand.COLD,
                MoistureBand.NORMAL
            );
        }
        if (key.contains("mu") || key.contains("hua")) {
            return DaoAffinity.of(
                BiomeFamily.FOREST,
                BiomeFamily.TEMPERATE,
                BiomeFamily.WET,
                ClimateBand.TEMPERATE,
                MoistureBand.NORMAL
            );
        }
        if (key.contains("tu")) {
            return DaoAffinity.of(
                BiomeFamily.ARID,
                BiomeFamily.TEMPERATE,
                BiomeFamily.MOUNTAIN,
                ClimateBand.HOT,
                MoistureBand.DRY
            );
        }
        if (key.contains("du") || key.contains("an") || key.contains("ying")) {
            return DaoAffinity.of(
                BiomeFamily.SWAMP,
                BiomeFamily.WET,
                BiomeFamily.FOREST,
                ClimateBand.TEMPERATE,
                MoistureBand.WET
            );
        }
        if (key.contains("feng") || key.contains("lei") || key.contains("jin")
            || key.contains("jian") || key.contains("dao") || key.contains("zhou")
            || key.contains("tian") || key.contains("yun") || key.contains("xing")
            || key.contains("yue")) {
            return DaoAffinity.of(
                BiomeFamily.MOUNTAIN,
                BiomeFamily.TEMPERATE,
                BiomeFamily.ARID,
                ClimateBand.COLD,
                MoistureBand.DRY
            );
        }
        if (key.contains("hun") || key.contains("meng") || key.contains("xu") || key.contains("zhi")) {
            return DaoAffinity.of(
                BiomeFamily.MYSTIC,
                BiomeFamily.SWAMP,
                BiomeFamily.FOREST,
                ClimateBand.TEMPERATE,
                MoistureBand.WET
            );
        }
        return DaoAffinity.of(
            BiomeFamily.TEMPERATE,
            BiomeFamily.FOREST,
            BiomeFamily.MOUNTAIN,
            ClimateBand.TEMPERATE,
            MoistureBand.NORMAL
        );
    }

    private static String resolveBenmingPriorityDaoKey(AscensionConditionSnapshot snapshot) {
        if (snapshot == null) {
            return EMPTY_DAO_KEY;
        }
        if (snapshot.benmingGuFallbackState() != AscensionConditionSnapshot.BenmingGuFallbackState.RESOLVED) {
            return EMPTY_DAO_KEY;
        }
        String token = snapshot.benmingGuToken();
        if (token == null || !token.startsWith(BENMING_PREFIX)) {
            return EMPTY_DAO_KEY;
        }
        String idLiteral = token.substring(BENMING_PREFIX.length());
        long id;
        try {
            id = Long.parseLong(idLiteral);
        } catch (NumberFormatException ignored) {
            return EMPTY_DAO_KEY;
        }
        if (id <= 0L) {
            return EMPTY_DAO_KEY;
        }
        String specialCase = BENMING_SPECIAL_CASE_DAO.get(id);
        if (specialCase != null && !specialCase.isBlank()) {
            return specialCase;
        }
        if (BENMING_DAO_RING.isEmpty()) {
            return EMPTY_DAO_KEY;
        }
        long modulo = Math.floorMod(id - BENMING_ID_BASE, BENMING_DAO_RING.size());
        int index = (int) modulo;
        return BENMING_DAO_RING.get(index);
    }

    private static List<String> buildBenmingDaoRing() {
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (DaoHenHelper.DaoType daoType : DaoHenHelper.DaoType.values()) {
            if (daoType == DaoHenHelper.DaoType.GENERIC) {
                continue;
            }
            String key = canonicalizeDaoKey(daoType.getKey());
            if (key.isBlank()) {
                continue;
            }
            if (key.endsWith(KEY_SUFFIX_ALT_VERSION) && key.length() > KEY_SUFFIX_ALT_VERSION.length()) {
                key = key.substring(0, key.length() - KEY_SUFFIX_ALT_VERSION.length());
            }
            unique.add(key);
        }
        return List.copyOf(unique);
    }

    private static List<RankedBiome> rankByScore(
        Map<BiomeKey, Double> scoreByBiome,
        Map<BiomeKey, Boolean> benmingBoostedByBiome
    ) {
        List<RankedBiome> ranked = new ArrayList<>();
        for (BiomeMetadata metadata : BIOME_METADATA) {
            BiomeKey biomeKey = metadata.biomeKey();
            ranked.add(
                new RankedBiome(
                    biomeKey,
                    sanitizeNonNegativeFinite(scoreByBiome.getOrDefault(biomeKey, 0.0D)),
                    benmingBoostedByBiome.getOrDefault(biomeKey, false)
                )
            );
        }
        ranked.sort(
            (left, right) -> {
                int byScore = Double.compare(right.score(), left.score());
                if (byScore != 0) {
                    return byScore;
                }
                return left.biomeId().compareTo(right.biomeId());
            }
        );
        return List.copyOf(ranked);
    }

    private static BiomePreferenceResult buildDefaultFallbackResult(String benmingPriorityDaoKey) {
        List<BiomeKey> ordered = new ArrayList<>(DEFAULT_FALLBACK_CHAIN);
        for (BiomeMetadata metadata : BIOME_METADATA) {
            if (!ordered.contains(metadata.biomeKey())) {
                ordered.add(metadata.biomeKey());
            }
        }

        List<RankedBiome> ranked = new ArrayList<>(ordered.size());
        double score = DEFAULT_FALLBACK_SCORE_START;
        for (BiomeKey biomeKey : ordered) {
            ranked.add(new RankedBiome(biomeKey, score, false));
            score -= DEFAULT_FALLBACK_SCORE_STEP;
        }
        return buildResult(List.copyOf(ranked), true, benmingPriorityDaoKey);
    }

    private static BiomePreferenceResult buildResult(
        List<RankedBiome> rankedBiomes,
        boolean usedDefaultFallback,
        String benmingPriorityDaoKey
    ) {
        if (rankedBiomes == null || rankedBiomes.isEmpty()) {
            throw new IllegalArgumentException("rankedBiomes 不能为空");
        }
        BiomeKey primaryBiome = rankedBiomes.getFirst().biomeKey();
        List<BiomeKey> secondaryBiomes = new ArrayList<>();
        for (int index = 1; index < rankedBiomes.size(); index++) {
            secondaryBiomes.add(rankedBiomes.get(index).biomeKey());
        }
        return new BiomePreferenceResult(
            primaryBiome,
            List.copyOf(secondaryBiomes),
            rankedBiomes,
            usedDefaultFallback,
            benmingPriorityDaoKey == null ? EMPTY_DAO_KEY : benmingPriorityDaoKey
        );
    }

    private static double sumValues(Map<String, Double> values) {
        double total = 0.0D;
        for (Double value : values.values()) {
            total += sanitizeNonNegativeFinite(value);
        }
        return total;
    }

    private static double sanitizeNonNegativeFinite(Double value) {
        if (value == null) {
            return 0.0D;
        }
        return sanitizeNonNegativeFinite(value.doubleValue());
    }

    private static double sanitizeNonNegativeFinite(double value) {
        if (!Double.isFinite(value) || value < 0.0D) {
            return 0.0D;
        }
        return value;
    }

    private static BiomeMetadata biome(
        String path,
        ClimateBand climateBand,
        MoistureBand moistureBand,
        BiomeFamily first,
        BiomeFamily... others
    ) {
        EnumSet<BiomeFamily> families = EnumSet.of(first, others);
        return new BiomeMetadata(BiomeKey.minecraft(path), climateBand, moistureBand, families);
    }

    /**
     * 生产侧 biome-key 对象。
     * <p>
     * 该对象与 MC 运行态类解耦，便于在纯 JVM 单测中稳定运行；
     * 未来接入搜索层时可按 id 再映射到具体 registry key。
     * </p>
     */
    public record BiomeKey(String namespace, String path) {

        public BiomeKey {
            namespace = sanitize(namespace, "namespace");
            path = sanitize(path, "path");
        }

        public static BiomeKey minecraft(String path) {
            return new BiomeKey("minecraft", path);
        }

        public String id() {
            return namespace + ":" + path;
        }

        private static String sanitize(String value, String fieldName) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(fieldName + " 不能为空");
            }
            return value.trim();
        }
    }

    /**
     * 推断结果。
     * <p>
     * 主/副/全量排序都保留 BiomeKey，供 Task4/6 直接消费；
     * 同时提供字符串投影方法，专用于 test classpath 受限时断言。
     * </p>
     */
    public record BiomePreferenceResult(
        BiomeKey primaryBiome,
        List<BiomeKey> secondaryBiomes,
        List<RankedBiome> rankedBiomes,
        boolean usedDefaultFallback,
        String benmingPriorityDaoKey
    ) {

        public BiomePreferenceResult {
            primaryBiome = Objects.requireNonNull(primaryBiome, "primaryBiome");
            secondaryBiomes = List.copyOf(Objects.requireNonNull(secondaryBiomes, "secondaryBiomes"));
            rankedBiomes = List.copyOf(Objects.requireNonNull(rankedBiomes, "rankedBiomes"));
            if (rankedBiomes.isEmpty()) {
                throw new IllegalArgumentException("rankedBiomes 不能为空");
            }
            if (benmingPriorityDaoKey == null) {
                benmingPriorityDaoKey = EMPTY_DAO_KEY;
            }
        }

        public String primaryBiomeId() {
            return primaryBiome.id();
        }

        public List<String> secondaryBiomeIds() {
            List<String> ids = new ArrayList<>(secondaryBiomes.size());
            for (BiomeKey biomeKey : secondaryBiomes) {
                ids.add(biomeKey.id());
            }
            return List.copyOf(ids);
        }

        public List<String> rankedBiomeIds() {
            List<String> ids = new ArrayList<>(rankedBiomes.size());
            for (RankedBiome rankedBiome : rankedBiomes) {
                ids.add(rankedBiome.biomeId());
            }
            return List.copyOf(ids);
        }
    }

    /**
     * 单个 biome 的排序项。
     */
    public record RankedBiome(BiomeKey biomeKey, double score, boolean benmingBoosted) {

        public RankedBiome {
            biomeKey = Objects.requireNonNull(biomeKey, "biomeKey");
            score = sanitizeNonNegativeFinite(score);
        }

        public String biomeId() {
            return biomeKey.id();
        }
    }

    /** biome 语义族群标签。 */
    private enum BiomeFamily {
        ARID,
        TEMPERATE,
        FOREST,
        WET,
        SWAMP,
        COLD,
        MOUNTAIN,
        MYSTIC
    }

    /** biome 气候带标签。 */
    private enum ClimateBand {
        HOT,
        TEMPERATE,
        COLD
    }

    /** biome 湿度带标签。 */
    private enum MoistureBand {
        DRY,
        NORMAL,
        WET
    }

    /**
     * biome metadata 节点。
     */
    private record BiomeMetadata(
        BiomeKey biomeKey,
        ClimateBand climateBand,
        MoistureBand moistureBand,
        EnumSet<BiomeFamily> families
    ) {

        private BiomeMetadata {
            Objects.requireNonNull(biomeKey, "biomeKey");
            Objects.requireNonNull(climateBand, "climateBand");
            Objects.requireNonNull(moistureBand, "moistureBand");
            Objects.requireNonNull(families, "families");
        }
    }

    /**
     * 道痕语义亲和标签。
     */
    private record DaoAffinity(
        BiomeFamily primaryFamily,
        BiomeFamily secondaryFamily,
        BiomeFamily tertiaryFamily,
        ClimateBand preferredClimate,
        MoistureBand preferredMoisture
    ) {

        private DaoAffinity {
            Objects.requireNonNull(primaryFamily, "primaryFamily");
            Objects.requireNonNull(preferredClimate, "preferredClimate");
            Objects.requireNonNull(preferredMoisture, "preferredMoisture");
        }

        private static DaoAffinity of(
            BiomeFamily primary,
            BiomeFamily secondary,
            BiomeFamily tertiary,
            ClimateBand preferredClimate,
            MoistureBand preferredMoisture
        ) {
            return new DaoAffinity(primary, secondary, tertiary, preferredClimate, preferredMoisture);
        }
    }
}
