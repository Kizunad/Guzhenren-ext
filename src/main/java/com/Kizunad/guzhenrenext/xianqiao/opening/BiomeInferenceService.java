package com.Kizunad.guzhenrenext.xianqiao.opening;

import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class BiomeInferenceService {

    private static final double HOT_TEMPERATURE_THRESHOLD = 0.95D;

    private static final double COLD_TEMPERATURE_THRESHOLD = 0.15D;

    private static final double DAO_MARK_SCORE_BUDGET = 12.0D;

    private static final double BENMING_DAO_SCORE_MULTIPLIER = 1.6D;

    private static final double BENMING_DIRECT_BIOME_BONUS = 18.0D;

    private static final double BENMING_KEYWORD_TAG_BONUS = 5.0D;

    private static final double METADATA_OVERWORLD_SIGNAL = 0.2D;

    private static final double METADATA_NON_OVERWORLD_SIGNAL = 0.1D;

    private static final double POSITIVE_SCORE_THRESHOLD = 0.0D;

    private static final List<String> STABLE_FALLBACK_POOL = List.of(
        "minecraft:plains",
        "minecraft:forest",
        "minecraft:savanna",
        "minecraft:taiga",
        "minecraft:meadow",
        "minecraft:swamp",
        "minecraft:desert",
        "minecraft:badlands"
    );

    private static final Map<String, DaoType> BENMING_DAO_HINTS = createBenmingDaoHints();

    private static final Map<String, List<String>> BENMING_DIRECT_BIOMES = createBenmingDirectBiomes();

    private static final Map<String, List<String>> BENMING_RELATED_BIOMES = createBenmingRelatedBiomes();

    private static final Comparator<BiomePreference> PREFERENCE_ORDER = Comparator
        .comparing(BiomePreference::totalScore, Comparator.reverseOrder())
        .thenComparing(BiomePreference::benmingScore, Comparator.reverseOrder())
        .thenComparing(BiomePreference::daoMarkScore, Comparator.reverseOrder())
        .thenComparing(BiomePreference::metadataScore, Comparator.reverseOrder())
        .thenComparing(BiomePreference::biomeId);

    private static final Set<String> TAG_HOT = Set.of("c:is_hot", "c:is_hot/overworld");

    private static final Set<String> TAG_COLD = Set.of("c:is_cold", "c:is_cold/overworld");

    private static final Set<String> TAG_WET = Set.of("c:is_wet", "c:is_wet/overworld");

    private static final Set<String> TAG_DRY = Set.of("c:is_dry", "c:is_dry/overworld");

    private static final Set<String> TAG_AQUATIC = Set.of("c:is_aquatic", "c:is_aquatic_icy");

    private static final Set<String> TAG_OCEAN = Set.of(
        "minecraft:is_ocean",
        "minecraft:is_deep_ocean",
        "c:is_ocean",
        "c:is_deep_ocean",
        "c:is_shallow_ocean"
    );

    private static final Set<String> TAG_DEEP_OCEAN = Set.of("minecraft:is_deep_ocean", "c:is_deep_ocean");

    private static final Set<String> TAG_RIVER = Set.of("minecraft:is_river", "c:is_river");

    private static final Set<String> TAG_MOUNTAIN = Set.of(
        "minecraft:is_mountain",
        "c:is_mountain",
        "c:is_mountain/peak"
    );

    private static final Set<String> TAG_HILL = Set.of("minecraft:is_hill", "c:is_hill");

    private static final Set<String> TAG_PLAINS = Set.of("c:is_plains", "c:is_snowy_plains");

    private static final Set<String> TAG_FOREST = Set.of("minecraft:is_forest", "c:is_forest", "c:is_birch_forest");

    private static final Set<String> TAG_JUNGLE = Set.of("minecraft:is_jungle", "c:is_jungle");

    private static final Set<String> TAG_SWAMP = Set.of("c:is_swamp");

    private static final Set<String> TAG_DESERT = Set.of("c:is_desert");

    private static final Set<String> TAG_BADLANDS = Set.of("minecraft:is_badlands", "c:is_badlands");

    private static final Set<String> TAG_SNOWY = Set.of("c:is_snowy", "c:is_snowy_plains");

    private static final Set<String> TAG_ICY = Set.of("c:is_icy", "c:is_aquatic_icy");

    private static final Set<String> TAG_CAVE = Set.of("c:is_cave", "c:is_underground");

    private static final Set<String> TAG_WINDSWEPT = Set.of("c:is_windswept");

    private static final Set<String> TAG_LUSH = Set.of(
        "c:is_lush",
        "c:is_dense_vegetation",
        "c:is_dense_vegetation/overworld"
    );

    private static final Set<String> TAG_RARE = Set.of("c:is_rare");

    private static final Set<String> TAG_MAGICAL = Set.of("c:is_magical");

    private static final Set<String> TAG_MODIFIED = Set.of("c:is_modified");

    private static final Set<String> TAG_SPOOKY = Set.of("c:is_spooky");

    private static final Set<String> TAG_WASTELAND = Set.of("c:is_wasteland");

    private static final Set<String> TAG_DEAD = Set.of("c:is_dead");

    private static final Set<String> TAG_FLORAL = Set.of("c:is_floral", "c:is_flower_forest");

    private static final Set<String> TAG_OVERWORLD = Set.of("minecraft:is_overworld", "c:is_overworld");

    private static final Set<String> TAG_NETHER = Set.of("minecraft:is_nether", "c:is_nether", "c:is_nether_forest");

    private static final Set<String> TAG_END = Set.of("minecraft:is_end", "c:is_end", "c:is_outer_end_island");

    private static final Set<String> TAG_VOID = Set.of("c:is_void");

    private static final Map<DaoType, DaoSemanticRule> DAO_RULES = createDaoRules();

    public Map<String, VanillaBiomeDescriptor> buildStableBiomeMap(List<VanillaBiomeDescriptor> vanillaBiomes) {
        Objects.requireNonNull(vanillaBiomes, "vanillaBiomes");
        List<VanillaBiomeDescriptor> sorted = new ArrayList<>(vanillaBiomes);
        sorted.sort(Comparator.comparing(VanillaBiomeDescriptor::biomeId));
        Map<String, VanillaBiomeDescriptor> stableMap = new LinkedHashMap<>();
        for (VanillaBiomeDescriptor descriptor : sorted) {
            if (descriptor == null) {
                continue;
            }
            stableMap.putIfAbsent(descriptor.biomeId(), descriptor);
        }
        return Map.copyOf(stableMap);
    }

    public BiomeInferenceResult infer(BiomeInferenceInput input, List<VanillaBiomeDescriptor> vanillaBiomes) {
        Objects.requireNonNull(input, "input");
        Map<String, VanillaBiomeDescriptor> biomeMap = buildStableBiomeMap(vanillaBiomes);
        if (biomeMap.isEmpty()) {
            return new BiomeInferenceResult(
                List.of(),
                Optional.of("minecraft:plains"),
                BiomeFallbackPolicy.DEFAULT_PLAINS
            );
        }

        Set<DaoType> benmingDaoHints = collectBenmingDaoHints(input);
        double daoMarkTotal = sumPositiveDaoMarks(input.daoMarkWeights());
        List<BiomePreference> preferences = new ArrayList<>();

        for (VanillaBiomeDescriptor descriptor : biomeMap.values()) {
            Set<SemanticToken> tokens = collectSemanticTokens(descriptor);
            double benmingScore = scoreBenming(input, benmingDaoHints, descriptor, tokens);
            double daoMarkScore = scoreDaoMarks(input.daoMarkWeights(), daoMarkTotal, tokens);
            double metadataScore = scoreMetadataSignal(tokens, benmingDaoHints, daoMarkTotal);
            double totalScore = benmingScore + daoMarkScore + metadataScore;
            preferences.add(
                new BiomePreference(
                    descriptor.biomeId(),
                    totalScore,
                    benmingScore,
                    daoMarkScore,
                    metadataScore
                )
            );
        }

        preferences.sort(PREFERENCE_ORDER);
        return resolveFallback(input, preferences, biomeMap);
    }

    private BiomeInferenceResult resolveFallback(
        BiomeInferenceInput input,
        List<BiomePreference> preferences,
        Map<String, VanillaBiomeDescriptor> biomeMap
    ) {
        BiomePreference top = preferences.get(0);
        if (top.totalScore() > POSITIVE_SCORE_THRESHOLD) {
            return new BiomeInferenceResult(List.copyOf(preferences), Optional.empty(), BiomeFallbackPolicy.NONE);
        }

        Optional<String> benmingRelated = findBenmingRelatedFallback(input.benmingGuDescriptor(), biomeMap.keySet());
        if (benmingRelated.isPresent()) {
            return new BiomeInferenceResult(
                List.copyOf(preferences),
                benmingRelated,
                BiomeFallbackPolicy.BENMING_RELATED
            );
        }

        List<String> stablePool = new ArrayList<>();
        for (String candidate : STABLE_FALLBACK_POOL) {
            if (biomeMap.containsKey(candidate)) {
                stablePool.add(candidate);
            }
        }
        if (stablePool.isEmpty()) {
            return new BiomeInferenceResult(
                List.copyOf(preferences),
                Optional.of("minecraft:plains"),
                BiomeFallbackPolicy.DEFAULT_PLAINS
            );
        }

        int index = Math.floorMod(stableFallbackHash(input), stablePool.size());
        return new BiomeInferenceResult(
            List.copyOf(preferences),
            Optional.of(stablePool.get(index)),
            BiomeFallbackPolicy.STABLE_HASH_POOL
        );
    }

    private Optional<String> findBenmingRelatedFallback(String benmingGuDescriptor, Set<String> availableBiomeIds) {
        String descriptor = normalizeDescriptor(benmingGuDescriptor);
        if (descriptor.isBlank()) {
            return Optional.empty();
        }
        for (Map.Entry<String, List<String>> entry : BENMING_RELATED_BIOMES.entrySet()) {
            if (!descriptor.contains(entry.getKey())) {
                continue;
            }
            for (String candidate : entry.getValue()) {
                if (availableBiomeIds.contains(candidate)) {
                    return Optional.of(candidate);
                }
            }
        }
        return Optional.empty();
    }

    private int stableFallbackHash(BiomeInferenceInput input) {
        String descriptor = normalizeDescriptor(input.benmingGuDescriptor());
        List<String> daoEntries = new ArrayList<>();
        for (Map.Entry<DaoType, Double> entry : input.daoMarkWeights().entrySet()) {
            if (entry.getValue() != null && entry.getValue() > 0.0D) {
                daoEntries.add(entry.getKey().name() + "=" + String.format(Locale.ROOT, "%.6f", entry.getValue()));
            }
        }
        daoEntries.sort(Comparator.naturalOrder());
        return Objects.hash(descriptor, String.join("|", daoEntries), input.fallbackSeed());
    }

    private double scoreBenming(
        BiomeInferenceInput input,
        Set<DaoType> benmingDaoHints,
        VanillaBiomeDescriptor descriptor,
        Set<SemanticToken> tokens
    ) {
        double score = scoreBenmingByDaoHints(benmingDaoHints, tokens);
        score += scoreBenmingDirectBiome(input.benmingGuDescriptor(), descriptor.biomeId());
        score += scoreBenmingKeywordTagAffinity(input.benmingGuDescriptor(), tokens);
        return score;
    }

    private double scoreBenmingByDaoHints(Set<DaoType> benmingDaoHints, Set<SemanticToken> tokens) {
        double score = 0.0D;
        for (DaoType daoType : benmingDaoHints) {
            double affinity = Math.max(0.0D, scoreDaoAffinity(daoType, tokens));
            score += affinity * BENMING_DAO_SCORE_MULTIPLIER;
        }
        return score;
    }

    private double scoreBenmingDirectBiome(String benmingGuDescriptor, String biomeId) {
        String descriptor = normalizeDescriptor(benmingGuDescriptor);
        if (descriptor.isBlank()) {
            return 0.0D;
        }
        for (Map.Entry<String, List<String>> entry : BENMING_DIRECT_BIOMES.entrySet()) {
            if (!descriptor.contains(entry.getKey())) {
                continue;
            }
            if (entry.getValue().contains(biomeId)) {
                return BENMING_DIRECT_BIOME_BONUS;
            }
        }
        return 0.0D;
    }

    private double scoreBenmingKeywordTagAffinity(String benmingGuDescriptor, Set<SemanticToken> tokens) {
        String descriptor = normalizeDescriptor(benmingGuDescriptor);
        if (descriptor.isBlank()) {
            return 0.0D;
        }
        double score = 0.0D;
        if (containsAny(descriptor, "火", "炎", "fire", "huo") && tokens.contains(SemanticToken.HOT)) {
            score += BENMING_KEYWORD_TAG_BONUS;
        }
        if (containsAny(descriptor, "水", "海", "潮", "water", "ocean")
            && (tokens.contains(SemanticToken.AQUATIC) || tokens.contains(SemanticToken.OCEAN))) {
            score += BENMING_KEYWORD_TAG_BONUS;
        }
        if (containsAny(descriptor, "沙", "荒", "desert") && tokens.contains(SemanticToken.DESERT)) {
            score += BENMING_KEYWORD_TAG_BONUS;
        }
        if (containsAny(descriptor, "林", "wood", "forest") && tokens.contains(SemanticToken.FOREST)) {
            score += BENMING_KEYWORD_TAG_BONUS;
        }
        if (containsAny(descriptor, "雪", "冰", "snow", "ice")
            && (tokens.contains(SemanticToken.SNOWY) || tokens.contains(SemanticToken.ICY))) {
            score += BENMING_KEYWORD_TAG_BONUS;
        }
        if (containsAny(descriptor, "山", "mountain", "peak") && tokens.contains(SemanticToken.MOUNTAIN)) {
            score += BENMING_KEYWORD_TAG_BONUS;
        }
        if (containsAny(descriptor, "沼", "swamp") && tokens.contains(SemanticToken.SWAMP)) {
            score += BENMING_KEYWORD_TAG_BONUS;
        }
        return score;
    }

    private double scoreDaoMarks(Map<DaoType, Double> daoMarkWeights, double daoMarkTotal, Set<SemanticToken> tokens) {
        if (daoMarkTotal <= 0.0D) {
            return 0.0D;
        }
        double score = 0.0D;
        for (Map.Entry<DaoType, Double> entry : daoMarkWeights.entrySet()) {
            if (entry.getValue() == null || entry.getValue() <= 0.0D) {
                continue;
            }
            double normalizedWeight = entry.getValue() / daoMarkTotal;
            double affinity = Math.max(0.0D, scoreDaoAffinity(entry.getKey(), tokens));
            score += normalizedWeight * affinity * DAO_MARK_SCORE_BUDGET;
        }
        return score;
    }

    private double scoreMetadataSignal(Set<SemanticToken> tokens, Set<DaoType> benmingDaoHints, double daoMarkTotal) {
        if (daoMarkTotal <= 0.0D) {
            return 0.0D;
        }
        if (tokens.contains(SemanticToken.OVERWORLD)) {
            return METADATA_OVERWORLD_SIGNAL;
        }
        if (tokens.contains(SemanticToken.NETHER) || tokens.contains(SemanticToken.END)) {
            return METADATA_NON_OVERWORLD_SIGNAL;
        }
        return 0.0D;
    }

    private double scoreDaoAffinity(DaoType daoType, Set<SemanticToken> tokens) {
        DaoSemanticRule rule = DAO_RULES.get(daoType);
        if (rule == null) {
            return 0.0D;
        }
        double score = 0.0D;
        for (SemanticToken preferred : rule.preferred()) {
            if (tokens.contains(preferred)) {
                score += 1.0D;
            }
        }
        for (SemanticToken avoided : rule.avoided()) {
            if (tokens.contains(avoided)) {
                score -= 1.0D;
            }
        }
        return score;
    }

    private Set<DaoType> collectBenmingDaoHints(BiomeInferenceInput input) {
        Set<DaoType> hints = EnumSet.noneOf(DaoType.class);
        hints.addAll(input.benmingPriorityDaoTypes());

        String descriptor = normalizeDescriptor(input.benmingGuDescriptor());
        if (descriptor.isBlank()) {
            return hints;
        }
        for (Map.Entry<String, DaoType> entry : BENMING_DAO_HINTS.entrySet()) {
            if (descriptor.contains(entry.getKey())) {
                hints.add(entry.getValue());
            }
        }
        return hints;
    }

    private Set<SemanticToken> collectSemanticTokens(VanillaBiomeDescriptor descriptor) {
        Set<SemanticToken> tokens = EnumSet.noneOf(SemanticToken.class);
        Set<String> tags = descriptor.normalizedTags();

        if (containsAnyTag(tags, TAG_OCEAN)) {
            tokens.add(SemanticToken.OCEAN);
        }
        if (containsAnyTag(tags, TAG_DEEP_OCEAN)) {
            tokens.add(SemanticToken.DEEP_OCEAN);
        }
        if (containsAnyTag(tags, TAG_RIVER)) {
            tokens.add(SemanticToken.RIVER);
        }
        if (containsAnyTag(tags, TAG_AQUATIC)) {
            tokens.add(SemanticToken.AQUATIC);
        }
        if (containsAnyTag(tags, TAG_MOUNTAIN)) {
            tokens.add(SemanticToken.MOUNTAIN);
        }
        if (containsAnyTag(tags, TAG_HILL)) {
            tokens.add(SemanticToken.HILL);
        }
        if (containsAnyTag(tags, TAG_PLAINS)) {
            tokens.add(SemanticToken.PLAINS);
        }
        if (containsAnyTag(tags, TAG_FOREST)) {
            tokens.add(SemanticToken.FOREST);
        }
        if (containsAnyTag(tags, TAG_JUNGLE)) {
            tokens.add(SemanticToken.JUNGLE);
        }
        if (containsAnyTag(tags, TAG_SWAMP)) {
            tokens.add(SemanticToken.SWAMP);
        }
        if (containsAnyTag(tags, TAG_DESERT)) {
            tokens.add(SemanticToken.DESERT);
        }
        if (containsAnyTag(tags, TAG_BADLANDS)) {
            tokens.add(SemanticToken.BADLANDS);
        }
        if (containsAnyTag(tags, TAG_SNOWY)) {
            tokens.add(SemanticToken.SNOWY);
        }
        if (containsAnyTag(tags, TAG_ICY)) {
            tokens.add(SemanticToken.ICY);
        }
        if (containsAnyTag(tags, TAG_CAVE)) {
            tokens.add(SemanticToken.CAVE);
        }
        if (containsAnyTag(tags, TAG_WINDSWEPT)) {
            tokens.add(SemanticToken.WINDSWEPT);
        }
        if (containsAnyTag(tags, TAG_LUSH)) {
            tokens.add(SemanticToken.LUSH);
        }
        if (containsAnyTag(tags, TAG_RARE)) {
            tokens.add(SemanticToken.RARE);
        }
        if (containsAnyTag(tags, TAG_MAGICAL)) {
            tokens.add(SemanticToken.MAGICAL);
        }
        if (containsAnyTag(tags, TAG_MODIFIED)) {
            tokens.add(SemanticToken.MODIFIED);
        }
        if (containsAnyTag(tags, TAG_SPOOKY)) {
            tokens.add(SemanticToken.SPOOKY);
        }
        if (containsAnyTag(tags, TAG_WASTELAND)) {
            tokens.add(SemanticToken.WASTELAND);
        }
        if (containsAnyTag(tags, TAG_DEAD)) {
            tokens.add(SemanticToken.DEAD);
        }
        if (containsAnyTag(tags, TAG_FLORAL)) {
            tokens.add(SemanticToken.FLORAL);
        }
        if (containsAnyTag(tags, TAG_OVERWORLD)) {
            tokens.add(SemanticToken.OVERWORLD);
        }
        if (containsAnyTag(tags, TAG_NETHER)) {
            tokens.add(SemanticToken.NETHER);
        }
        if (containsAnyTag(tags, TAG_END)) {
            tokens.add(SemanticToken.END);
        }
        if (containsAnyTag(tags, TAG_VOID)) {
            tokens.add(SemanticToken.VOID);
        }

        if (containsAnyTag(tags, TAG_HOT) || descriptor.baseTemperature() >= HOT_TEMPERATURE_THRESHOLD) {
            tokens.add(SemanticToken.HOT);
        }
        if (containsAnyTag(tags, TAG_COLD) || descriptor.baseTemperature() <= COLD_TEMPERATURE_THRESHOLD) {
            tokens.add(SemanticToken.COLD);
        }
        if (containsAnyTag(tags, TAG_WET) || descriptor.hasPrecipitation()) {
            tokens.add(SemanticToken.WET);
        }
        if (containsAnyTag(tags, TAG_DRY) || !descriptor.hasPrecipitation()) {
            tokens.add(SemanticToken.DRY);
        }
        return Set.copyOf(tokens);
    }

    private static boolean containsAnyTag(Set<String> tags, Set<String> expected) {
        for (String tag : expected) {
            if (tags.contains(tag)) {
                return true;
            }
        }
        return false;
    }

    private static double sumPositiveDaoMarks(Map<DaoType, Double> daoMarkWeights) {
        double total = 0.0D;
        for (Double value : daoMarkWeights.values()) {
            if (value != null && value > 0.0D) {
                total += value;
            }
        }
        return total;
    }

    private static boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private static String normalizeDescriptor(String descriptor) {
        if (descriptor == null) {
            return "";
        }
        return descriptor.toLowerCase(Locale.ROOT).replace(" ", "");
    }

    private static Map<DaoType, DaoSemanticRule> createDaoRules() {
        Map<DaoType, DaoSemanticRule> rules = new EnumMap<>(DaoType.class);
        rules.put(
            DaoType.FIRE,
            rule(
                set(SemanticToken.HOT, SemanticToken.DRY, SemanticToken.DESERT, SemanticToken.NETHER),
                set(SemanticToken.COLD, SemanticToken.WET)
            )
        );
        rules.put(
            DaoType.WATER,
            rule(
                set(SemanticToken.WET, SemanticToken.AQUATIC, SemanticToken.OCEAN, SemanticToken.RIVER),
                set(SemanticToken.DRY)
            )
        );
        rules.put(DaoType.EARTH, rule(set(SemanticToken.MOUNTAIN, SemanticToken.HILL, SemanticToken.PLAINS,
            SemanticToken.BADLANDS), set(SemanticToken.OCEAN)));
        rules.put(DaoType.WOOD, rule(set(SemanticToken.FOREST, SemanticToken.JUNGLE, SemanticToken.LUSH,
            SemanticToken.FLORAL), set(SemanticToken.DRY, SemanticToken.WASTELAND)));
        rules.put(DaoType.METAL, rule(set(SemanticToken.MOUNTAIN, SemanticToken.HILL, SemanticToken.WINDSWEPT),
            set(SemanticToken.SWAMP)));
        rules.put(DaoType.LIGHTNING, rule(set(SemanticToken.WINDSWEPT, SemanticToken.MOUNTAIN, SemanticToken.WET),
            set(SemanticToken.CAVE)));
        rules.put(DaoType.WIND, rule(set(SemanticToken.WINDSWEPT, SemanticToken.MOUNTAIN, SemanticToken.PLAINS),
            set(SemanticToken.CAVE)));
        rules.put(DaoType.ICE, rule(set(SemanticToken.COLD, SemanticToken.SNOWY, SemanticToken.ICY),
            set(SemanticToken.HOT)));
        rules.put(DaoType.DEATH, rule(set(SemanticToken.SPOOKY, SemanticToken.CAVE, SemanticToken.DEAD,
            SemanticToken.WASTELAND), set(SemanticToken.FLORAL, SemanticToken.LUSH)));
        rules.put(DaoType.LIFE, rule(set(SemanticToken.LUSH, SemanticToken.FLORAL, SemanticToken.FOREST,
            SemanticToken.PLAINS), set(SemanticToken.DEAD, SemanticToken.WASTELAND)));
        rules.put(DaoType.TIME, rule(set(SemanticToken.RARE, SemanticToken.MODIFIED, SemanticToken.END),
            set(SemanticToken.PLAINS)));
        rules.put(DaoType.SPACE, rule(set(SemanticToken.END, SemanticToken.VOID), set(SemanticToken.OVERWORLD)));
        rules.put(DaoType.POISON, rule(set(SemanticToken.SWAMP, SemanticToken.JUNGLE, SemanticToken.WET,
            SemanticToken.CAVE), set(SemanticToken.COLD)));
        rules.put(DaoType.SOUL, rule(set(SemanticToken.CAVE, SemanticToken.SPOOKY, SemanticToken.NETHER,
            SemanticToken.END), set(SemanticToken.FLORAL)));
        rules.put(DaoType.SWORD, rule(set(SemanticToken.MOUNTAIN, SemanticToken.WINDSWEPT, SemanticToken.HILL),
            set(SemanticToken.SWAMP)));
        rules.put(DaoType.BLOOD, rule(set(SemanticToken.BADLANDS, SemanticToken.HOT, SemanticToken.NETHER,
            SemanticToken.SWAMP), set(SemanticToken.SNOWY)));
        rules.put(DaoType.STRENGTH, rule(set(SemanticToken.MOUNTAIN, SemanticToken.HILL, SemanticToken.WINDSWEPT,
            SemanticToken.BADLANDS), set(SemanticToken.OCEAN)));
        rules.put(DaoType.RULE, rule(set(SemanticToken.PLAINS, SemanticToken.OVERWORLD), set(SemanticToken.VOID)));
        rules.put(DaoType.WISDOM, rule(set(SemanticToken.FOREST, SemanticToken.RARE, SemanticToken.CAVE),
            set(SemanticToken.WASTELAND)));
        rules.put(DaoType.DARK, rule(set(SemanticToken.CAVE, SemanticToken.SPOOKY, SemanticToken.NETHER,
            SemanticToken.DEEP_OCEAN), set(SemanticToken.FLORAL)));
        rules.put(DaoType.LIGHT, rule(set(SemanticToken.FLORAL, SemanticToken.PLAINS, SemanticToken.DESERT),
            set(SemanticToken.CAVE, SemanticToken.SPOOKY)));
        rules.put(DaoType.CLOUD, rule(set(SemanticToken.MOUNTAIN, SemanticToken.WINDSWEPT, SemanticToken.COLD),
            set(SemanticToken.CAVE)));
        rules.put(DaoType.STAR, rule(set(SemanticToken.END, SemanticToken.RARE, SemanticToken.COLD),
            set(SemanticToken.SWAMP)));
        rules.put(DaoType.MOON, rule(set(SemanticToken.SNOWY, SemanticToken.COLD, SemanticToken.OCEAN),
            set(SemanticToken.HOT)));
        rules.put(DaoType.TRANSFORMATION, rule(set(SemanticToken.MODIFIED, SemanticToken.RARE,
            SemanticToken.JUNGLE, SemanticToken.SWAMP), set(SemanticToken.PLAINS)));
        rules.put(DaoType.DREAM, rule(set(SemanticToken.LUSH, SemanticToken.MAGICAL, SemanticToken.RARE),
            set(SemanticToken.WASTELAND)));
        rules.put(DaoType.EMOTION, rule(set(SemanticToken.FLORAL, SemanticToken.FOREST, SemanticToken.SWAMP,
            SemanticToken.RIVER), set(SemanticToken.DEAD)));
        rules.put(DaoType.LUCK, rule(set(SemanticToken.RARE, SemanticToken.PLAINS, SemanticToken.LUSH,
            SemanticToken.RIVER), set(SemanticToken.VOID)));
        rules.put(DaoType.FATE, rule(set(SemanticToken.END, SemanticToken.NETHER, SemanticToken.CAVE,
            SemanticToken.RARE), set(SemanticToken.OVERWORLD)));
        return Map.copyOf(rules);
    }

    private static DaoSemanticRule rule(Set<SemanticToken> preferred, Set<SemanticToken> avoided) {
        return new DaoSemanticRule(preferred, avoided);
    }

    @SafeVarargs
    private static <T> Set<T> set(T... values) {
        return Set.of(values);
    }

    private static Map<String, DaoType> createBenmingDaoHints() {
        Map<String, DaoType> hints = new LinkedHashMap<>();
        hints.put("火", DaoType.FIRE);
        hints.put("炎", DaoType.FIRE);
        hints.put("fire", DaoType.FIRE);
        hints.put("huo", DaoType.FIRE);

        hints.put("水", DaoType.WATER);
        hints.put("海", DaoType.WATER);
        hints.put("潮", DaoType.WATER);
        hints.put("water", DaoType.WATER);
        hints.put("aqua", DaoType.WATER);

        hints.put("土", DaoType.EARTH);
        hints.put("岩", DaoType.EARTH);
        hints.put("earth", DaoType.EARTH);

        hints.put("木", DaoType.WOOD);
        hints.put("林", DaoType.WOOD);
        hints.put("wood", DaoType.WOOD);

        hints.put("风", DaoType.WIND);
        hints.put("wind", DaoType.WIND);
        hints.put("雷", DaoType.LIGHTNING);
        hints.put("lightning", DaoType.LIGHTNING);

        hints.put("冰", DaoType.ICE);
        hints.put("雪", DaoType.ICE);
        hints.put("ice", DaoType.ICE);
        hints.put("snow", DaoType.ICE);

        hints.put("魂", DaoType.SOUL);
        hints.put("soul", DaoType.SOUL);
        hints.put("血", DaoType.BLOOD);
        hints.put("blood", DaoType.BLOOD);
        hints.put("剑", DaoType.SWORD);
        hints.put("sword", DaoType.SWORD);

        hints.put("毒", DaoType.POISON);
        hints.put("poison", DaoType.POISON);
        hints.put("梦", DaoType.DREAM);
        hints.put("dream", DaoType.DREAM);
        hints.put("运", DaoType.LUCK);
        hints.put("luck", DaoType.LUCK);

        hints.put("空", DaoType.SPACE);
        hints.put("虚", DaoType.SPACE);
        hints.put("space", DaoType.SPACE);
        hints.put("时", DaoType.TIME);
        hints.put("time", DaoType.TIME);
        return Map.copyOf(hints);
    }

    private static Map<String, List<String>> createBenmingDirectBiomes() {
        Map<String, List<String>> mapping = new LinkedHashMap<>();
        mapping.put("沙漠", List.of("minecraft:desert"));
        mapping.put("desert", List.of("minecraft:desert"));
        mapping.put("badlands", List.of("minecraft:badlands"));
        mapping.put("沼泽", List.of("minecraft:swamp", "minecraft:mangrove_swamp"));
        mapping.put("swamp", List.of("minecraft:swamp", "minecraft:mangrove_swamp"));
        mapping.put("山岳", List.of("minecraft:jagged_peaks", "minecraft:windswept_hills"));
        mapping.put("mountain", List.of("minecraft:jagged_peaks", "minecraft:windswept_hills"));
        mapping.put("雪原", List.of("minecraft:snowy_plains", "minecraft:ice_spikes"));
        mapping.put("snowfield", List.of("minecraft:snowy_plains", "minecraft:ice_spikes"));
        mapping.put("冰原", List.of("minecraft:snowy_plains", "minecraft:ice_spikes"));
        mapping.put("icefield", List.of("minecraft:snowy_plains", "minecraft:ice_spikes"));
        mapping.put("魂沼", List.of("minecraft:soul_sand_valley"));
        mapping.put("soulsand", List.of("minecraft:soul_sand_valley"));
        return Map.copyOf(mapping);
    }

    private static Map<String, List<String>> createBenmingRelatedBiomes() {
        Map<String, List<String>> mapping = new LinkedHashMap<>();
        mapping.put("沙", List.of("minecraft:desert", "minecraft:badlands"));
        mapping.put("desert", List.of("minecraft:desert", "minecraft:badlands"));
        mapping.put("海", List.of("minecraft:warm_ocean", "minecraft:ocean"));
        mapping.put("ocean", List.of("minecraft:warm_ocean", "minecraft:ocean"));
        mapping.put("林", List.of("minecraft:forest", "minecraft:old_growth_spruce_taiga"));
        mapping.put("forest", List.of("minecraft:forest", "minecraft:old_growth_spruce_taiga"));
        mapping.put("雪", List.of("minecraft:snowy_plains", "minecraft:ice_spikes"));
        mapping.put("冰", List.of("minecraft:snowy_plains", "minecraft:ice_spikes"));
        mapping.put("snow", List.of("minecraft:snowy_plains", "minecraft:ice_spikes"));
        mapping.put("ice", List.of("minecraft:snowy_plains", "minecraft:ice_spikes"));
        mapping.put("沼", List.of("minecraft:swamp", "minecraft:mangrove_swamp"));
        mapping.put("swamp", List.of("minecraft:swamp", "minecraft:mangrove_swamp"));
        mapping.put("山", List.of("minecraft:windswept_hills", "minecraft:jagged_peaks"));
        mapping.put("mountain", List.of("minecraft:windswept_hills", "minecraft:jagged_peaks"));
        mapping.put("魂", List.of("minecraft:deep_dark", "minecraft:soul_sand_valley"));
        mapping.put("soul", List.of("minecraft:soul_sand_valley", "minecraft:deep_dark"));
        mapping.put("玄", List.of("minecraft:snowy_plains"));
        return Map.copyOf(mapping);
    }

    public record BiomeInferenceInput(
        String benmingGuDescriptor,
        Set<DaoType> benmingPriorityDaoTypes,
        Map<DaoType, Double> daoMarkWeights,
        long fallbackSeed
    ) {

        public BiomeInferenceInput {
            benmingGuDescriptor = benmingGuDescriptor == null ? "" : benmingGuDescriptor;
            benmingPriorityDaoTypes = normalizeDaoHints(benmingPriorityDaoTypes);
            daoMarkWeights = normalizeDaoMarkWeights(daoMarkWeights);
        }

        private static Set<DaoType> normalizeDaoHints(Set<DaoType> source) {
            if (source == null || source.isEmpty()) {
                return Set.of();
            }
            Set<DaoType> normalized = EnumSet.noneOf(DaoType.class);
            for (DaoType daoType : source) {
                if (daoType != null) {
                    normalized.add(daoType);
                }
            }
            return Set.copyOf(normalized);
        }

        private static Map<DaoType, Double> normalizeDaoMarkWeights(Map<DaoType, Double> source) {
            if (source == null || source.isEmpty()) {
                return Map.of();
            }
            Map<DaoType, Double> normalized = new HashMap<>();
            for (Map.Entry<DaoType, Double> entry : source.entrySet()) {
                if (entry.getKey() == null || entry.getValue() == null) {
                    continue;
                }
                normalized.put(entry.getKey(), Math.max(0.0D, entry.getValue()));
            }
            return Map.copyOf(normalized);
        }
    }

    public record VanillaBiomeDescriptor(
        String biomeId,
        Set<String> tagIds,
        double baseTemperature,
        boolean hasPrecipitation
    ) {

        public VanillaBiomeDescriptor {
            biomeId = normalizeBiomeId(biomeId);
            tagIds = normalizeTagIds(tagIds);
            if (!Double.isFinite(baseTemperature)) {
                baseTemperature = 0.0D;
            }
        }

        public Set<String> normalizedTags() {
            return tagIds;
        }

        private static String normalizeBiomeId(String id) {
            if (id == null || id.isBlank()) {
                return "minecraft:plains";
            }
            return id.toLowerCase(Locale.ROOT);
        }

        private static Set<String> normalizeTagIds(Set<String> source) {
            if (source == null || source.isEmpty()) {
                return Set.of();
            }
            Set<String> normalized = new java.util.TreeSet<>();
            for (String tag : source) {
                if (tag == null || tag.isBlank()) {
                    continue;
                }
                String value = tag.toLowerCase(Locale.ROOT);
                if (value.startsWith("#")) {
                    value = value.substring(1);
                }
                normalized.add(value);
            }
            return Set.copyOf(normalized);
        }
    }

    public record BiomePreference(
        String biomeId,
        double totalScore,
        double benmingScore,
        double daoMarkScore,
        double metadataScore
    ) {
    }

    public record BiomeInferenceResult(
        List<BiomePreference> rankedPreferences,
        Optional<String> fallbackBiomeId,
        BiomeFallbackPolicy fallbackPolicy
    ) {
    }

    public enum BiomeFallbackPolicy {
        NONE,

        BENMING_RELATED,

        STABLE_HASH_POOL,

        DEFAULT_PLAINS
    }

    private enum SemanticToken {
        HOT,
        COLD,
        WET,
        DRY,
        AQUATIC,
        OCEAN,
        DEEP_OCEAN,
        RIVER,
        MOUNTAIN,
        HILL,
        PLAINS,
        FOREST,
        JUNGLE,
        SWAMP,
        DESERT,
        BADLANDS,
        SNOWY,
        ICY,
        CAVE,
        WINDSWEPT,
        LUSH,
        RARE,
        MAGICAL,
        MODIFIED,
        SPOOKY,
        WASTELAND,
        DEAD,
        FLORAL,
        OVERWORLD,
        NETHER,
        END,
        VOID
    }

    private record DaoSemanticRule(Set<SemanticToken> preferred, Set<SemanticToken> avoided) {
    }
}
