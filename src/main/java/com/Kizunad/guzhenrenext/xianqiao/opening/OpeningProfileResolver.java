package com.Kizunad.guzhenrenext.xianqiao.opening;

import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionAttemptStage;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import net.guzhenren.network.GuzhenrenModVariables;
import net.minecraft.world.entity.LivingEntity;

/**
 * 开局画像解析器。
 * <p>
 * 输入来源可以是玩家实时变量、也可以是测试构造的原始数值表；输出统一为冻结快照。
 * </p>
 */
public final class OpeningProfileResolver {

    private static final double EPSILON = 1.0E-9D;
    private static final double DEFAULT_HUMAN_QI_TARGET = 100.0D;
    private static final double DEFAULT_EARTH_QI_TARGET = 100.0D;
    private static final double TIZHI_SCORE_MAX = 100.0D;
    private static final double PERCENT_SCALE = 100.0D;
    private static final double RANK_FIVE_VALUE = 5.0D;
    private static final int REQUIRED_CORE_FIELD_COUNT = 5;
    private static final double FALLBACK_HUMAN_QI_WEIGHT_JINGLI = 0.4D;
    private static final double FALLBACK_HUMAN_QI_WEIGHT_HUNPO = 0.4D;
    private static final double FALLBACK_HUMAN_QI_WEIGHT_TIZHI = 0.2D;

    private static final String KEY_BENMING_GU = "benminggu";
    private static final String KEY_DAOHEN_TOTAL = "daohen_zong";
    private static final String KEY_MAX_ZHENYUAN = "zuida_zhenyuan";
    private static final String KEY_SHOUYUAN = "shouyuan";
    private static final String KEY_JINGLI = "jingli";
    private static final String KEY_MAX_JINGLI = "zuida_jingli";
    private static final String KEY_HUNPO = "hunpo";
    private static final String KEY_MAX_HUNPO = "zuida_hunpo";
    private static final String KEY_TIZHI = "tizhi";
    private static final String KEY_ZHUANSHU = "zhuanshu";
    private static final String KEY_JIEDUAN = "jieduan";
    private static final String KEY_KONGQIAO = "kongqiao";
    private static final String KEY_QIYUN = "qiyun";
    private static final String KEY_QIYUN_MAX = "qiyun_shangxian";
    private static final String KEY_RENQI = "renqi";
    private static final String KEY_HUMAN_QI = "humanQi";
    private static final String KEY_EARTH_QI = "earthQi";
    private static final String KEY_DI_YU = "di_yu";
    private static final String KEY_HUMAN_QI_TARGET = "humanQiTarget";
    private static final String KEY_EARTH_QI_TARGET = "earthQiTarget";

    private final AscensionThreeQiEvaluator threeQiEvaluator;

    public OpeningProfileResolver() {
        this(new AscensionThreeQiEvaluator());
    }

    public OpeningProfileResolver(AscensionThreeQiEvaluator threeQiEvaluator) {
        if (threeQiEvaluator == null) {
            throw new IllegalArgumentException("threeQiEvaluator 不能为空");
        }
        this.threeQiEvaluator = threeQiEvaluator;
    }

    /**
     * 从玩家实体读取实时变量并冻结成画像。
     */
    public ResolvedOpeningProfile resolveFromPlayer(
        LivingEntity player,
        double earthQi,
        boolean playerInitiated
    ) {
        if (player == null) {
            throw new IllegalArgumentException("player 不能为空");
        }
        GuzhenrenModVariables.PlayerVariables vars = player.getData(GuzhenrenModVariables.PLAYER_VARIABLES);
        return resolveFromPlayerVariables(vars, earthQi, playerInitiated);
    }

    public ResolvedOpeningProfile resolveFromPlayer(
        LivingEntity player,
        boolean playerInitiated
    ) {
        if (player == null) {
            throw new IllegalArgumentException("player 不能为空");
        }
        GuzhenrenModVariables.PlayerVariables vars = player.getData(GuzhenrenModVariables.PLAYER_VARIABLES);
        return resolveFromPlayerVariables(vars, playerInitiated);
    }

    /**
     * 从 PlayerVariables 对象读取并冻结。
     */
    public ResolvedOpeningProfile resolveFromPlayerVariables(
        Object playerVariables,
        double earthQi,
        boolean playerInitiated
    ) {
        if (playerVariables == null) {
            throw new IllegalArgumentException("playerVariables 不能为空");
        }
        return resolve(new ResolveInput(extractNumericVariables(playerVariables), earthQi, playerInitiated));
    }

    public ResolvedOpeningProfile resolveFromPlayerVariables(
        Object playerVariables,
        boolean playerInitiated
    ) {
        if (playerVariables == null) {
            throw new IllegalArgumentException("playerVariables 不能为空");
        }
        Map<String, Double> rawVariables = extractNumericVariables(playerVariables);
        return resolve(new ResolveInput(rawVariables, resolveEarthQi(rawVariables), playerInitiated));
    }

    /**
     * 从原始数值表解析（用于测试或离线回放）。
     */
    public ResolvedOpeningProfile resolveFromRawVariables(
        Map<String, Double> rawVariables,
        double earthQi,
        boolean playerInitiated
    ) {
        return resolve(new ResolveInput(rawVariables, earthQi, playerInitiated));
    }

    public ResolvedOpeningProfile resolveFromRawVariables(
        Map<String, Double> rawVariables,
        boolean playerInitiated
    ) {
        Map<String, Double> normalizedRawVariables = normalizeRawVariables(rawVariables);
        return resolve(
            new ResolveInput(
                normalizedRawVariables,
                resolveEarthQi(normalizedRawVariables),
                playerInitiated
            )
        );
    }

    public ResolvedOpeningProfile resolveFromSnapshot(AscensionConditionSnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("snapshot 不能为空");
        }
        AscensionThreeQiEvaluator.ThreeQiEvaluation evaluation = threeQiEvaluator.evaluate(snapshot);
        AscensionAttemptStage suggestedStage = resolveSuggestedStage(snapshot, evaluation);
        return new ResolvedOpeningProfile(snapshot, evaluation, suggestedStage);
    }

    public ResolvedOpeningProfile resolve(ResolveInput input) {
        if (input == null) {
            throw new IllegalArgumentException("input 不能为空");
        }

        Map<String, Double> raw = normalizeRawVariables(input.rawVariables());
        double benmingGuRawValue = getValue(raw, KEY_BENMING_GU);
        AscensionConditionSnapshot.BenmingGuFallbackState benmingState = resolveBenmingState(benmingGuRawValue);
        String benmingToken = resolveBenmingToken(benmingGuRawValue, benmingState);

        Map<String, Double> daoMarks = resolveDaoMarks(raw);
        double daoMarkTotalFromPlayer = getValue(raw, KEY_DAOHEN_TOTAL);
        double daoMarkResolvedTotal = sumValues(daoMarks);
        AscensionConditionSnapshot.DaoMarkCoverageState daoMarkCoverageState = resolveDaoMarkCoverage(
            daoMarks,
            daoMarkTotalFromPlayer,
            daoMarkResolvedTotal
        );

        double maxZhenyuan = getValue(raw, KEY_MAX_ZHENYUAN);
        double shouyuan = getValue(raw, KEY_SHOUYUAN);
        double jingli = getValue(raw, KEY_JINGLI);
        double maxJingli = getValue(raw, KEY_MAX_JINGLI);
        double hunpo = getValue(raw, KEY_HUNPO);
        double maxHunpo = getValue(raw, KEY_MAX_HUNPO);
        double tizhi = getValue(raw, KEY_TIZHI);
        double zhuanshu = getValue(raw, KEY_ZHUANSHU);
        double jieduan = getValue(raw, KEY_JIEDUAN);
        double kongqiao = getValue(raw, KEY_KONGQIAO);
        double qiyun = getValue(raw, KEY_QIYUN);
        double qiyunMax = getValue(raw, KEY_QIYUN_MAX);
        double renqi = getValue(raw, KEY_RENQI);

        double humanQiTarget = resolveTarget(getValue(raw, KEY_HUMAN_QI_TARGET), DEFAULT_HUMAN_QI_TARGET);
        double earthQiTarget = resolveTarget(getValue(raw, KEY_EARTH_QI_TARGET), DEFAULT_EARTH_QI_TARGET);
        double fallbackHumanQi = resolveFallbackHumanQi(raw, jingli, maxJingli, hunpo, maxHunpo, tizhi);

        AscensionConditionSnapshot.AptitudeResourceState aptitudeResourceState = resolveAptitudeResourceState(
            maxZhenyuan,
            shouyuan,
            maxJingli,
            maxHunpo,
            tizhi,
            jingli,
            hunpo
        );

        AscensionConditionSnapshot snapshot = new AscensionConditionSnapshot(
            benmingGuRawValue,
            benmingState,
            benmingToken,
            daoMarks,
            daoMarkCoverageState,
            daoMarkTotalFromPlayer,
            daoMarkResolvedTotal,
            aptitudeResourceState,
            maxZhenyuan,
            shouyuan,
            jingli,
            maxJingli,
            hunpo,
            maxHunpo,
            tizhi,
            zhuanshu,
            jieduan,
            kongqiao,
            qiyun,
            qiyunMax,
            renqi,
            fallbackHumanQi,
            sanitizeNonNegativeFinite(input.earthQi()),
            humanQiTarget,
            earthQiTarget,
            input.playerInitiated()
        );

        AscensionThreeQiEvaluator.ThreeQiEvaluation evaluation = threeQiEvaluator.evaluate(snapshot);
        AscensionAttemptStage suggestedStage = resolveSuggestedStage(snapshot, evaluation);
        return new ResolvedOpeningProfile(snapshot, evaluation, suggestedStage);
    }

    private static AscensionAttemptStage resolveSuggestedStage(
        AscensionConditionSnapshot snapshot,
        AscensionThreeQiEvaluator.ThreeQiEvaluation evaluation
    ) {
        if (evaluation.canEnterConfirmed()) {
            return AscensionAttemptStage.CONFIRMED;
        }
        if (evaluation.readyToConfirm()) {
            return AscensionAttemptStage.READY_TO_CONFIRM;
        }
        if (evaluation.fiveTurnPeak()) {
            return AscensionAttemptStage.QI_OBSERVATION_AND_HARMONIZATION;
        }
        if (isRankFive(snapshot.zhuanshu())) {
            return AscensionAttemptStage.ASCENSION_PREPARATION_UNLOCKED;
        }
        return AscensionAttemptStage.CULTIVATION_PROGRESS;
    }

    private static boolean isRankFive(double zhuanshu) {
        return Math.abs(sanitizeNonNegativeFinite(zhuanshu) - RANK_FIVE_VALUE) <= EPSILON;
    }

    private static AscensionConditionSnapshot.BenmingGuFallbackState resolveBenmingState(double benmingGuRawValue) {
        if (benmingGuRawValue <= EPSILON) {
            return AscensionConditionSnapshot.BenmingGuFallbackState.MISSING;
        }
        double rounded = Math.rint(benmingGuRawValue);
        if (Math.abs(benmingGuRawValue - rounded) <= EPSILON) {
            return AscensionConditionSnapshot.BenmingGuFallbackState.RESOLVED;
        }
        return AscensionConditionSnapshot.BenmingGuFallbackState.UNKNOWN;
    }

    private static String resolveBenmingToken(
        double benmingGuRawValue,
        AscensionConditionSnapshot.BenmingGuFallbackState state
    ) {
        if (state == AscensionConditionSnapshot.BenmingGuFallbackState.RESOLVED) {
            long id = Math.round(benmingGuRawValue);
            return "benminggu:" + id;
        }
        return "unknown";
    }

    private static Map<String, Double> resolveDaoMarks(Map<String, Double> rawVariables) {
        TreeMap<String, Double> daoMarks = new TreeMap<>();
        for (Map.Entry<String, Double> entry : rawVariables.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith("daohen_") && !key.startsWith("dahen_")) {
                continue;
            }
            if (KEY_DAOHEN_TOTAL.equals(key)) {
                continue;
            }
            String canonical = canonicalDaoKey(key);
            if (canonical.isBlank()) {
                continue;
            }
            double value = sanitizeNonNegativeFinite(entry.getValue());
            if (value <= EPSILON) {
                continue;
            }
            daoMarks.put(canonical, value);
        }
        return Map.copyOf(new LinkedHashMap<>(daoMarks));
    }

    private static String canonicalDaoKey(String rawKey) {
        if (rawKey.startsWith("daohen_")) {
            return rawKey.substring("daohen_".length());
        }
        if (rawKey.startsWith("dahen_")) {
            return rawKey.substring("dahen_".length());
        }
        return "";
    }

    private static double sumValues(Map<String, Double> values) {
        double total = 0.0D;
        for (Double value : values.values()) {
            total += sanitizeNonNegativeFinite(value);
        }
        return total;
    }

    private static AscensionConditionSnapshot.DaoMarkCoverageState resolveDaoMarkCoverage(
        Map<String, Double> daoMarks,
        double daoMarkTotalFromPlayer,
        double daoMarkResolvedTotal
    ) {
        boolean hasEntries = !daoMarks.isEmpty();
        boolean hasTotal = daoMarkTotalFromPlayer > EPSILON;
        boolean hasResolvedTotal = daoMarkResolvedTotal > EPSILON;
        if (!hasEntries && !hasTotal) {
            return AscensionConditionSnapshot.DaoMarkCoverageState.MISSING;
        }
        if (!hasEntries || !hasTotal || !hasResolvedTotal) {
            return AscensionConditionSnapshot.DaoMarkCoverageState.PARTIAL;
        }
        return AscensionConditionSnapshot.DaoMarkCoverageState.COMPLETE;
    }

    private static AscensionConditionSnapshot.AptitudeResourceState resolveAptitudeResourceState(
        double maxZhenyuan,
        double shouyuan,
        double maxJingli,
        double maxHunpo,
        double tizhi,
        double jingli,
        double hunpo
    ) {
        int missingCoreCount = 0;
        if (maxZhenyuan <= EPSILON) {
            missingCoreCount++;
        }
        if (shouyuan <= EPSILON) {
            missingCoreCount++;
        }
        if (maxJingli <= EPSILON) {
            missingCoreCount++;
        }
        if (maxHunpo <= EPSILON) {
            missingCoreCount++;
        }
        if (tizhi <= EPSILON) {
            missingCoreCount++;
        }
        if (missingCoreCount >= REQUIRED_CORE_FIELD_COUNT && jingli <= EPSILON && hunpo <= EPSILON) {
            return AscensionConditionSnapshot.AptitudeResourceState.ALL_ZERO_OR_MISSING;
        }
        if (missingCoreCount > 0) {
            return AscensionConditionSnapshot.AptitudeResourceState.PARTIAL_MISSING;
        }
        return AscensionConditionSnapshot.AptitudeResourceState.HEALTHY;
    }

    private static double resolveFallbackHumanQi(
        Map<String, Double> raw,
        double jingli,
        double maxJingli,
        double hunpo,
        double maxHunpo,
        double tizhi
    ) {
        double configuredHumanQi = getValue(raw, KEY_HUMAN_QI);
        if (configuredHumanQi > EPSILON) {
            return configuredHumanQi;
        }

        double jingliScore = ratio(jingli, maxJingli) * PERCENT_SCALE;
        double hunpoScore = ratio(hunpo, maxHunpo) * PERCENT_SCALE;
        double tizhiScore = clamp(tizhi, 0.0D, TIZHI_SCORE_MAX);
        return (jingliScore * FALLBACK_HUMAN_QI_WEIGHT_JINGLI)
            + (hunpoScore * FALLBACK_HUMAN_QI_WEIGHT_HUNPO)
            + (tizhiScore * FALLBACK_HUMAN_QI_WEIGHT_TIZHI);
    }

    private static double ratio(double current, double max) {
        if (max <= EPSILON) {
            return 0.0D;
        }
        return clamp(current / max, 0.0D, 1.0D);
    }

    private static Map<String, Double> extractNumericVariables(Object playerVariables) {
        TreeMap<String, Double> values = new TreeMap<>();
        Field[] fields = playerVariables.getClass().getFields();
        for (Field field : fields) {
            try {
                Object rawValue = field.get(playerVariables);
                if (rawValue instanceof Number number) {
                    values.put(field.getName(), sanitizeNonNegativeFinite(number.doubleValue()));
                }
            } catch (IllegalAccessException ignored) {
            }
        }
        return Map.copyOf(new LinkedHashMap<>(values));
    }

    private static Map<String, Double> normalizeRawVariables(Map<String, Double> raw) {
        if (raw == null || raw.isEmpty()) {
            return Map.of();
        }
        TreeMap<String, Double> sorted = new TreeMap<>();
        for (Map.Entry<String, Double> entry : raw.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            sorted.put(entry.getKey(), sanitizeNonNegativeFinite(entry.getValue()));
        }
        return Map.copyOf(new LinkedHashMap<>(sorted));
    }

    private static double getValue(Map<String, Double> values, String key) {
        Double value = values.get(key);
        return sanitizeNonNegativeFinite(value);
    }

    private static double resolveEarthQi(Map<String, Double> rawVariables) {
        if (rawVariables.containsKey(KEY_EARTH_QI)) {
            return getValue(rawVariables, KEY_EARTH_QI);
        }
        return getValue(rawVariables, KEY_DI_YU);
    }

    private static double resolveTarget(double value, double defaultTarget) {
        if (value <= EPSILON) {
            return defaultTarget;
        }
        return value;
    }

    private static double sanitizeNonNegativeFinite(double value) {
        if (!Double.isFinite(value) || value < 0.0D) {
            return 0.0D;
        }
        return value;
    }

    private static double sanitizeNonNegativeFinite(Double value) {
        if (value == null || !Double.isFinite(value) || value < 0.0D) {
            return 0.0D;
        }
        return value;
    }

    private static double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    public record ResolveInput(Map<String, Double> rawVariables, double earthQi, boolean playerInitiated) {

        public ResolveInput {
            if (rawVariables == null) {
                rawVariables = Map.of();
            }
            rawVariables = Map.copyOf(new LinkedHashMap<>(new TreeMap<>(rawVariables)));
        }
    }
}
