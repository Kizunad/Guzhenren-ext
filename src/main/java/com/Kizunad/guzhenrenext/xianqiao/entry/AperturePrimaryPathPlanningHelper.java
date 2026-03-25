package com.Kizunad.guzhenrenext.xianqiao.entry;

import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoType;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureOpeningSnapshot;
import com.Kizunad.guzhenrenext.xianqiao.opening.ApertureBootstrapExecutor;
import com.Kizunad.guzhenrenext.xianqiao.opening.AscensionConditionSnapshot;
import com.Kizunad.guzhenrenext.xianqiao.opening.BiomeInferenceService;
import com.Kizunad.guzhenrenext.xianqiao.opening.InitialTerrainPlan;
import com.Kizunad.guzhenrenext.xianqiao.opening.OpeningLayoutPlanner;
import com.Kizunad.guzhenrenext.xianqiao.opening.OpeningProfileResolver;
import com.Kizunad.guzhenrenext.xianqiao.opening.ResolvedOpeningProfile;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import net.guzhenren.network.GuzhenrenModVariables;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;

public final class AperturePrimaryPathPlanningHelper {

    private static final String[] EARTH_QI_FIELD_CANDIDATES = {
        "di_yu",
        "earthQi",
    };

    private static final String[] HUMAN_QI_FIELD_CANDIDATES = {
        "humanQi",
    };

    private static final String[] ASCENSION_ATTEMPT_FIELD_CANDIDATES = {
        "ascensionAttemptInitiated",
        "shiqiao",
    };

    private static final String[] SNAPSHOT_FROZEN_FIELD_CANDIDATES = {
        "snapshotFrozen",
        "shiqiao",
    };

    private static final int LAYOUT_VERSION = 1;

    private static final long HASH_SEED = 1125899906842597L;

    private static final long HASH_MULTIPLIER = 1315423911L;

    private static final String BENMING_UNKNOWN_DESCRIPTOR =
        "unknown_benming|state:unknown_fallback|benming_code:0";

    private static final List<BenmingSemanticBucket> BENMING_SEMANTIC_BUCKETS = List.of(
        new BenmingSemanticBucket("desert", "沙漠", "fire"),
        new BenmingSemanticBucket("ocean", "海", "water"),
        new BenmingSemanticBucket("forest", "林", "wood"),
        new BenmingSemanticBucket("icefield", "冰原", "ice"),
        new BenmingSemanticBucket("swamp", "沼泽", "poison"),
        new BenmingSemanticBucket("mountain", "山岳", "earth"),
        new BenmingSemanticBucket("soulsand", "魂沼", "soul")
    );

    private static final Map<String, DaoType> DAO_MARK_ALIAS_MAP = createDaoMarkAliasMap();

    private final OpeningProfileResolver profileResolver;

    private final BiomeInferenceService biomeInferenceService;

    private final OpeningLayoutPlanner openingLayoutPlanner;

    public AperturePrimaryPathPlanningHelper() {
        this(
            new OpeningProfileResolver(),
            new BiomeInferenceService(),
            new OpeningLayoutPlanner()
        );
    }

    public AperturePrimaryPathPlanningHelper(
        OpeningProfileResolver profileResolver,
        BiomeInferenceService biomeInferenceService,
        OpeningLayoutPlanner openingLayoutPlanner
    ) {
        this.profileResolver = Objects.requireNonNull(profileResolver, "profileResolver");
        this.biomeInferenceService = Objects.requireNonNull(biomeInferenceService, "biomeInferenceService");
        this.openingLayoutPlanner = Objects.requireNonNull(openingLayoutPlanner, "openingLayoutPlanner");
    }

    public PlanningBundle createFromPlayer(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        GuzhenrenModVariables.PlayerVariables variables = player.getData(GuzhenrenModVariables.PLAYER_VARIABLES);
        ResolvedOpeningProfile resolvedOpeningProfile = resolveOpeningProfile(variables);
        List<BiomeInferenceService.VanillaBiomeDescriptor> vanillaBiomes = collectVanillaBiomeDescriptors(
            player.server.overworld()
        );
        Map<DaoType, Double> daoMarkWeights = resolveDaoMarkWeights(variables, resolvedOpeningProfile);
        BiomeInferenceService.BiomeInferenceInput biomeInferenceInput = new BiomeInferenceService.BiomeInferenceInput(
            resolveBenmingDescriptor(resolvedOpeningProfile),
            resolveBenmingPriorityDaoTypes(resolvedOpeningProfile),
            daoMarkWeights,
            resolveFallbackSeed(player.getUUID(), resolvedOpeningProfile, daoMarkWeights)
        );
        BiomeInferenceService.BiomeInferenceResult biomeInferenceResult = biomeInferenceService.infer(
            biomeInferenceInput,
            vanillaBiomes
        );
        InitialTerrainPlan initialTerrainPlan = openingLayoutPlanner.plan(
            resolvedOpeningProfile,
            biomeInferenceResult
        );
        ApertureOpeningSnapshot openingSnapshot = toOpeningSnapshot(
            resolvedOpeningProfile.ascensionConditionSnapshot()
        );
        long planSeed = resolvePlanSeed(
            player.getUUID(),
            resolvedOpeningProfile,
            biomeInferenceResult,
            initialTerrainPlan,
            daoMarkWeights
        );
        ApertureBootstrapExecutor.BootstrapInput bootstrapInput = new ApertureBootstrapExecutor.BootstrapInput(
            openingSnapshot,
            initialTerrainPlan,
            LAYOUT_VERSION,
            planSeed
        );
        return new PlanningBundle(
            resolvedOpeningProfile,
            biomeInferenceResult,
            initialTerrainPlan,
            openingSnapshot,
            bootstrapInput
        );
    }

    private ResolvedOpeningProfile resolveOpeningProfile(GuzhenrenModVariables.PlayerVariables variables) {
        OptionalDouble earthQi = readOptionalDoubleField(variables, EARTH_QI_FIELD_CANDIDATES);
        OptionalDouble humanQi = readOptionalDoubleField(variables, HUMAN_QI_FIELD_CANDIDATES);
        AscensionAttemptSeam ascensionAttemptSeam = resolveAscensionAttemptSeam(variables);
        return profileResolver.resolve(
            OpeningProfileResolver.fromPlayerVariables(
                variables,
                earthQi,
                humanQi,
                ascensionAttemptSeam.ascensionAttemptInitiated(),
                ascensionAttemptSeam.snapshotFrozen()
            )
        );
    }

    private static List<BiomeInferenceService.VanillaBiomeDescriptor> collectVanillaBiomeDescriptors(
        ServerLevel overworldLevel
    ) {
        Objects.requireNonNull(overworldLevel, "overworldLevel");
        return overworldLevel.registryAccess()
            .lookupOrThrow(Registries.BIOME)
            .listElements()
            .map(AperturePrimaryPathPlanningHelper::toVanillaBiomeDescriptor)
            .toList();
    }

    private static BiomeInferenceService.VanillaBiomeDescriptor toVanillaBiomeDescriptor(
        Holder.Reference<Biome> biomeReference
    ) {
        Biome biome = biomeReference.value();
        Set<String> tagIds = biomeReference.tags()
            .map(tagKey -> tagKey.location().toString())
            .collect(Collectors.toCollection(TreeSet::new));
        return new BiomeInferenceService.VanillaBiomeDescriptor(
            biomeReference.key().location().toString(),
            tagIds,
            biome.getBaseTemperature(),
            biome.hasPrecipitation()
        );
    }

    private static Map<DaoType, Double> resolveDaoMarkWeights(
        GuzhenrenModVariables.PlayerVariables variables,
        ResolvedOpeningProfile resolvedOpeningProfile
    ) {
        Map<DaoType, Double> daoMarkWeights = new LinkedHashMap<>();
        if (variables != null) {
            for (Field field : variables.getClass().getFields()) {
                String normalizedKey = normalizeDaoHenFieldName(field.getName());
                if (normalizedKey == null) {
                    continue;
                }
                Optional<DaoType> daoType = resolveDaoType(normalizedKey);
                if (daoType.isEmpty()) {
                    continue;
                }
                double value = readNonNegativeNumberField(variables, field);
                if (value <= 0.0D) {
                    continue;
                }
                daoMarkWeights.merge(daoType.get(), value, Math::max);
            }
        }
        if (!daoMarkWeights.isEmpty()) {
            return Map.copyOf(daoMarkWeights);
        }
        Optional<DaoType> dominantDaoType = resolveDaoType(resolvedOpeningProfile.dominantDaoMark());
        if (dominantDaoType.isPresent() && resolvedOpeningProfile.totalDaoMarkValue() > 0.0D) {
            daoMarkWeights.put(dominantDaoType.get(), resolvedOpeningProfile.totalDaoMarkValue());
        }
        return Map.copyOf(daoMarkWeights);
    }

    private static String resolveBenmingDescriptor(ResolvedOpeningProfile resolvedOpeningProfile) {
        Objects.requireNonNull(resolvedOpeningProfile, "resolvedOpeningProfile");
        if (resolvedOpeningProfile.benmingGuState() == ResolvedOpeningProfile.BenmingGuState.UNKNOWN_FALLBACK) {
            return BENMING_UNKNOWN_DESCRIPTOR;
        }
        int benmingCode = Math.max(0, resolvedOpeningProfile.benmingGuCode());
        if (benmingCode <= 0) {
            return BENMING_UNKNOWN_DESCRIPTOR;
        }
        BenmingSemanticBucket semanticBucket = BENMING_SEMANTIC_BUCKETS.get(
            Math.floorMod(benmingCode, BENMING_SEMANTIC_BUCKETS.size())
        );
        String dominantDaoMark = normalizeAliasKey(resolvedOpeningProfile.dominantDaoMark());
        return "benming_code:" + benmingCode
            + "|state:resolved"
            + "|semantic:" + semanticBucket.semanticKey()
            + "|" + semanticBucket.chineseHint()
            + "|" + semanticBucket.daoHintKeyword()
            + "|dao:" + dominantDaoMark;
    }

    private static Set<DaoType> resolveBenmingPriorityDaoTypes(ResolvedOpeningProfile resolvedOpeningProfile) {
        Optional<DaoType> dominantDaoType = resolveDaoType(resolvedOpeningProfile.dominantDaoMark());
        return dominantDaoType.map(Set::of).orElseGet(Set::of);
    }

    private static ApertureOpeningSnapshot toOpeningSnapshot(AscensionConditionSnapshot ascensionConditionSnapshot) {
        return new ApertureOpeningSnapshot(
            ascensionConditionSnapshot.zhuanshu(),
            ascensionConditionSnapshot.jieduan(),
            ascensionConditionSnapshot.heavenScore(),
            ascensionConditionSnapshot.earthScore(),
            ascensionConditionSnapshot.humanScore(),
            ascensionConditionSnapshot.balanceScore(),
            ascensionConditionSnapshot.ascensionAttemptInitiated(),
            ascensionConditionSnapshot.snapshotFrozen()
        );
    }

    private static long resolveFallbackSeed(
        UUID playerId,
        ResolvedOpeningProfile resolvedOpeningProfile,
        Map<DaoType, Double> daoMarkWeights
    ) {
        long hash = seedWithPlayer(playerId);
        hash = mixInt(hash, resolvedOpeningProfile.zhuanshu());
        hash = mixInt(hash, resolvedOpeningProfile.jieduan());
        hash = mixInt(hash, resolvedOpeningProfile.kongqiao());
        hash = mixString(hash, resolvedOpeningProfile.dominantDaoMark());
        hash = mixDouble(hash, resolvedOpeningProfile.totalDaoMarkValue());
        return mixDaoMarkWeights(hash, daoMarkWeights);
    }

    private static long resolvePlanSeed(
        UUID playerId,
        ResolvedOpeningProfile resolvedOpeningProfile,
        BiomeInferenceService.BiomeInferenceResult biomeInferenceResult,
        InitialTerrainPlan initialTerrainPlan,
        Map<DaoType, Double> daoMarkWeights
    ) {
        long hash = resolveFallbackSeed(playerId, resolvedOpeningProfile, daoMarkWeights);
        AscensionConditionSnapshot snapshot = resolvedOpeningProfile.ascensionConditionSnapshot();
        hash = mixInt(hash, snapshot.heavenScore());
        hash = mixInt(hash, snapshot.earthScore());
        hash = mixInt(hash, snapshot.humanScore());
        hash = mixInt(hash, snapshot.balanceScore());
        hash = mixInt(hash, resolvedOpeningProfile.aptitudeScore());
        hash = mixString(hash, initialTerrainPlan.layoutTier().name());
        hash = mixInt(hash, initialTerrainPlan.layoutSize());
        hash = mixString(hash, biomeInferenceResult.fallbackPolicy().name());
        hash = mixString(hash, biomeInferenceResult.fallbackBiomeId().orElse(""));
        for (BiomeInferenceService.BiomePreference preference : biomeInferenceResult.rankedPreferences()) {
            hash = mixString(hash, preference.biomeId());
        }
        return hash;
    }

    private static long seedWithPlayer(UUID playerId) {
        long hash = HASH_SEED;
        hash = mixLong(hash, playerId.getMostSignificantBits());
        return mixLong(hash, playerId.getLeastSignificantBits());
    }

    private static long mixDaoMarkWeights(long hash, Map<DaoType, Double> daoMarkWeights) {
        for (Map.Entry<DaoType, Double> entry : daoMarkWeights.entrySet()) {
            hash = mixString(hash, entry.getKey().name());
            hash = mixDouble(hash, entry.getValue());
        }
        return hash;
    }

    private static long mixString(long hash, String value) {
        return mixLong(hash, Objects.toString(value, "").hashCode());
    }

    private static long mixDouble(long hash, double value) {
        return mixLong(hash, Double.doubleToLongBits(value));
    }

    private static long mixInt(long hash, int value) {
        return mixLong(hash, value);
    }

    private static long mixLong(long hash, long value) {
        return hash * HASH_MULTIPLIER + value;
    }

    private static OptionalDouble readOptionalDoubleField(
        GuzhenrenModVariables.PlayerVariables variables,
        String[] fieldCandidates
    ) {
        if (variables == null || fieldCandidates == null) {
            return OptionalDouble.empty();
        }
        for (String fieldName : fieldCandidates) {
            OptionalDouble resolved = tryReadField(variables, fieldName);
            if (resolved.isPresent()) {
                return resolved;
            }
        }
        return OptionalDouble.empty();
    }

    private static OptionalDouble tryReadField(
        GuzhenrenModVariables.PlayerVariables variables,
        String fieldName
    ) {
        if (fieldName == null || fieldName.isBlank()) {
            return OptionalDouble.empty();
        }
        try {
            Field field = variables.getClass().getField(fieldName);
            Object raw = field.get(variables);
            if (!(raw instanceof Number number)) {
                return OptionalDouble.empty();
            }
            double value = number.doubleValue();
            if (!Double.isFinite(value) || value <= 0.0D) {
                return OptionalDouble.empty();
            }
            return OptionalDouble.of(value);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            return OptionalDouble.empty();
        }
    }

    /**
     * 读取升仙尝试输入语义。
     * <p>
     * 为避免入口层把 CONFIRMED 硬编码伪造为 true/true，优先读取显式状态位，
     * 若上游尚未提供则回退到现有 shiqiao 字段；未主动发起时冻结态必须归零。
     * </p>
     */
    private static AscensionAttemptSeam resolveAscensionAttemptSeam(
        GuzhenrenModVariables.PlayerVariables variables
    ) {
        boolean ascensionAttemptInitiated = readOptionalBooleanField(
            variables,
            ASCENSION_ATTEMPT_FIELD_CANDIDATES
        ).orElse(false);
        boolean snapshotFrozen = readOptionalBooleanField(
            variables,
            SNAPSHOT_FROZEN_FIELD_CANDIDATES
        ).orElse(ascensionAttemptInitiated);
        if (!ascensionAttemptInitiated) {
            snapshotFrozen = false;
        }
        return new AscensionAttemptSeam(ascensionAttemptInitiated, snapshotFrozen);
    }

    private static Optional<Boolean> readOptionalBooleanField(
        GuzhenrenModVariables.PlayerVariables variables,
        String[] fieldCandidates
    ) {
        if (variables == null || fieldCandidates == null) {
            return Optional.empty();
        }
        for (String fieldName : fieldCandidates) {
            Optional<Boolean> resolved = tryReadBooleanField(variables, fieldName);
            if (resolved.isPresent()) {
                return resolved;
            }
        }
        return Optional.empty();
    }

    private static Optional<Boolean> tryReadBooleanField(
        GuzhenrenModVariables.PlayerVariables variables,
        String fieldName
    ) {
        if (fieldName == null || fieldName.isBlank()) {
            return Optional.empty();
        }
        try {
            Field field = variables.getClass().getField(fieldName);
            Object raw = field.get(variables);
            if (raw instanceof Boolean value) {
                return Optional.of(value);
            }
            if (raw instanceof Number number) {
                return Optional.of(number.doubleValue() > 0.0D);
            }
            return Optional.empty();
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            return Optional.empty();
        }
    }

    private static double readNonNegativeNumberField(
        GuzhenrenModVariables.PlayerVariables variables,
        Field field
    ) {
        try {
            Object raw = field.get(variables);
            if (!(raw instanceof Number number)) {
                return 0.0D;
            }
            double value = number.doubleValue();
            if (!Double.isFinite(value) || value <= 0.0D) {
                return 0.0D;
            }
            return value;
        } catch (IllegalAccessException ignored) {
            return 0.0D;
        }
    }

    private static String normalizeDaoHenFieldName(String fieldName) {
        if (fieldName == null) {
            return null;
        }
        if (fieldName.startsWith("daohen_")) {
            return fieldName.substring("daohen_".length());
        }
        if (fieldName.startsWith("dahen_")) {
            return fieldName.substring("dahen_".length());
        }
        return null;
    }

    private static Optional<DaoType> resolveDaoType(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            return Optional.empty();
        }
        String normalizedKey = normalizeAliasKey(rawKey);
        return Optional.ofNullable(DAO_MARK_ALIAS_MAP.get(normalizedKey));
    }

    private static String normalizeAliasKey(String rawKey) {
        return rawKey.trim().toLowerCase(Locale.ROOT).replace("道", "");
    }

    private static Map<String, DaoType> createDaoMarkAliasMap() {
        Map<String, DaoType> aliases = new LinkedHashMap<>();
        registerDaoAliases(aliases, DaoType.FIRE, "fire", "huo", "火");
        registerDaoAliases(aliases, DaoType.WATER, "water", "shui", "水", "aqua");
        registerDaoAliases(aliases, DaoType.EARTH, "earth", "tu", "土", "rock");
        registerDaoAliases(aliases, DaoType.WOOD, "wood", "mu", "木", "forest");
        registerDaoAliases(aliases, DaoType.METAL, "metal", "jin", "金");
        registerDaoAliases(aliases, DaoType.LIGHTNING, "lightning", "lei", "雷");
        registerDaoAliases(aliases, DaoType.WIND, "wind", "feng", "风");
        registerDaoAliases(aliases, DaoType.ICE, "ice", "bing", "冰", "snow");
        registerDaoAliases(aliases, DaoType.DEATH, "death", "si", "死");
        registerDaoAliases(aliases, DaoType.LIFE, "life", "sheng", "生");
        registerDaoAliases(aliases, DaoType.TIME, "time", "shi", "时");
        registerDaoAliases(aliases, DaoType.SPACE, "space", "kong", "空");
        registerDaoAliases(aliases, DaoType.POISON, "poison", "du", "毒");
        registerDaoAliases(aliases, DaoType.SOUL, "soul", "hun", "魂");
        registerDaoAliases(aliases, DaoType.SWORD, "sword", "jian", "剑");
        registerDaoAliases(aliases, DaoType.BLOOD, "blood", "xue", "血");
        registerDaoAliases(aliases, DaoType.STRENGTH, "strength", "li", "力");
        registerDaoAliases(aliases, DaoType.RULE, "rule", "guize", "规则");
        registerDaoAliases(aliases, DaoType.WISDOM, "wisdom", "zhi", "智");
        registerDaoAliases(aliases, DaoType.DARK, "dark", "an", "暗");
        registerDaoAliases(aliases, DaoType.LIGHT, "light", "guang", "光");
        registerDaoAliases(aliases, DaoType.CLOUD, "cloud", "yun", "云");
        registerDaoAliases(aliases, DaoType.STAR, "star", "xing", "星");
        registerDaoAliases(aliases, DaoType.MOON, "moon", "yue", "月");
        registerDaoAliases(aliases, DaoType.TRANSFORMATION, "transformation", "bianhua", "变化");
        registerDaoAliases(aliases, DaoType.DREAM, "dream", "meng", "梦");
        registerDaoAliases(aliases, DaoType.EMOTION, "emotion", "qing", "情");
        registerDaoAliases(aliases, DaoType.LUCK, "luck", "yunqi", "运");
        registerDaoAliases(aliases, DaoType.FATE, "fate", "ming", "命");
        return Map.copyOf(aliases);
    }

    private static void registerDaoAliases(
        Map<String, DaoType> aliases,
        DaoType daoType,
        String... customAliases
    ) {
        aliases.put(normalizeAliasKey(daoType.name()), daoType);
        aliases.put(normalizeAliasKey(daoType.getDisplayName()), daoType);
        for (String alias : customAliases) {
            aliases.put(normalizeAliasKey(alias), daoType);
        }
    }

    private record BenmingSemanticBucket(
        String semanticKey,
        String chineseHint,
        String daoHintKeyword
    ) {
    }

    private record AscensionAttemptSeam(
        boolean ascensionAttemptInitiated,
        boolean snapshotFrozen
    ) {
    }

    public record PlanningBundle(
        ResolvedOpeningProfile resolvedOpeningProfile,
        BiomeInferenceService.BiomeInferenceResult biomeInferenceResult,
        InitialTerrainPlan initialTerrainPlan,
        ApertureOpeningSnapshot openingSnapshot,
        ApertureBootstrapExecutor.BootstrapInput bootstrapInput
    ) {

        public PlanningBundle {
            Objects.requireNonNull(resolvedOpeningProfile, "resolvedOpeningProfile");
            Objects.requireNonNull(biomeInferenceResult, "biomeInferenceResult");
            Objects.requireNonNull(initialTerrainPlan, "initialTerrainPlan");
            Objects.requireNonNull(openingSnapshot, "openingSnapshot");
            Objects.requireNonNull(bootstrapInput, "bootstrapInput");
        }
    }
}
