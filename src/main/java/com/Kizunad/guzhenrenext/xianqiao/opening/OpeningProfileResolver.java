package com.Kizunad.guzhenrenext.xianqiao.opening;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.TreeMap;
import net.guzhenren.network.GuzhenrenModVariables;

/** 开窍画像解析器。 */
public final class OpeningProfileResolver {

    private static final int SCORE_MIN = 0;

    private static final int SCORE_MAX = 100;

    private static final int SCORE_SCALE = 100;

    private static final int APTITUDE_COMPONENT_COUNT = 5;

    private static final int BENMING_GU_FALLBACK = 0;

    private static final String DEFAULT_DAO_MARK = "generic";

    private static final String DAO_HEN_PREFIX = "daohen_";

    private static final String DA_HEN_PREFIX = "dahen_";

    private static final double QIYUN_MAX_MIN = 20.0;

    private static final double QIYUN_MAX_MAX = 40.0;

    private static final double APTITUDE_ZHENYUAN_TARGET = 1000.0;

    private static final double APTITUDE_SHOUYUAN_TARGET = 1000.0;

    private static final double APTITUDE_JINGLI_TARGET = 1000.0;

    private static final double APTITUDE_HUNPO_TARGET = 1000.0;

    private static final double APTITUDE_TIZHI_TARGET = 100.0;

    private static final double HUMAN_QI_TARGET = 100.0;

    private static final double EARTH_QI_TARGET = 100.0;

    private final AscensionThreeQiEvaluator evaluator;

    public OpeningProfileResolver() {
        this(new AscensionThreeQiEvaluator());
    }

    public OpeningProfileResolver(AscensionThreeQiEvaluator evaluator) {
        this.evaluator = Objects.requireNonNull(evaluator, "evaluator");
    }

    public ResolvedOpeningProfile resolve(OpeningProfileResolverInput input) {
        Objects.requireNonNull(input, "input");
        int zhuanshu = toNonNegativeInt(input.zhuanshu());
        int jieduan = toNonNegativeInt(input.jieduan());
        int kongqiao = toNonNegativeInt(input.kongqiao());

        BenmingGuResolution benmingGuResolution = resolveBenmingGu(input.benminggu());
        DaoMarkResolution daoMarkResolution = resolveDaoMark(input.daoHen());
        AptitudeResolution aptitudeResolution = resolveAptitude(input);
        HumanQiResolution humanQiResolution = resolveHumanQi(input);
        EarthQiResolution earthQiResolution = resolveEarthQi(input);

        int heavenScore = resolveHeavenScore(input.qiyun(), input.qiyunShangxian());
        int humanScore = toScore(humanQiResolution.humanQiValue(), HUMAN_QI_TARGET);
        int earthScore = toScore(earthQiResolution.earthQiValue(), EARTH_QI_TARGET);
        int balanceScore = resolveBalanceScore(heavenScore, earthScore, humanScore);

        AscensionConditionSnapshot snapshot = new AscensionConditionSnapshot(
            zhuanshu,
            jieduan,
            heavenScore,
            earthScore,
            humanScore,
            balanceScore,
            input.ascensionAttemptInitiated(),
            input.snapshotFrozen()
        );
        AscensionThreeQiEvaluation evaluation = evaluator.evaluate(snapshot);

        return new ResolvedOpeningProfile(
            snapshot,
            evaluation,
            zhuanshu,
            jieduan,
            kongqiao,
            benmingGuResolution.benmingGuCode(),
            benmingGuResolution.benmingGuState(),
            daoMarkResolution.dominantDaoMark(),
            daoMarkResolution.dominantDaoMarkValue(),
            daoMarkResolution.totalDaoMarkValue(),
            daoMarkResolution.daoMarkState(),
            aptitudeResolution.aptitudeScore(),
            aptitudeResolution.aptitudeState(),
            humanQiResolution.humanQiSource(),
            earthQiResolution.fallbackApplied()
        );
    }

    public static OpeningProfileResolverInput fromPlayerVariables(
        GuzhenrenModVariables.PlayerVariables variables,
        OptionalDouble earthQi,
        OptionalDouble humanQi,
        boolean ascensionAttemptInitiated,
        boolean snapshotFrozen
    ) {
        if (variables == null) {
            return BasicResolverInput.empty(
                earthQi,
                humanQi,
                ascensionAttemptInitiated,
                snapshotFrozen
            );
        }
        Map<String, Double> daoHen = readDaoHenFields(variables);
        return BasicResolverInput.builder()
            .zhuanshu(variables.zhuanshu)
            .jieduan(variables.jieduan)
            .kongqiao(variables.kongqiao)
            .benminggu(variables.benminggu)
            .zuidaZhenyuan(variables.zuida_zhenyuan)
            .shouyuan(variables.shouyuan)
            .jingli(variables.jingli)
            .zuidaJingli(variables.zuida_jingli)
            .hunpo(variables.hunpo)
            .zuidaHunpo(variables.zuida_hunpo)
            .tizhi(variables.tizhi)
            .qiyun(variables.qiyun)
            .qiyunShangxian(variables.qiyun_shangxian)
            .renqi(toPresentPositiveOptional(variables.renqi))
            .humanQi(sanitizeOptional(humanQi))
            .earthQi(sanitizeOptional(earthQi))
            .daoHen(daoHen)
            .ascensionAttemptInitiated(ascensionAttemptInitiated)
            .snapshotFrozen(snapshotFrozen)
            .build();
    }

    private static OptionalDouble toPresentPositiveOptional(double value) {
        if (!Double.isFinite(value) || value <= 0.0) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(value);
    }

    private static OptionalDouble sanitizeOptional(OptionalDouble value) {
        if (value == null || value.isEmpty()) {
            return OptionalDouble.empty();
        }
        double raw = value.getAsDouble();
        if (!Double.isFinite(raw) || raw <= 0.0) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(raw);
    }

    private static Map<String, Double> readDaoHenFields(
        GuzhenrenModVariables.PlayerVariables variables
    ) {
        Map<String, Double> daoHen = new TreeMap<>();
        Field[] fields = variables.getClass().getFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            if (!isDaoHenField(fieldName)) {
                continue;
            }
            try {
                Object raw = field.get(variables);
                if (!(raw instanceof Number number)) {
                    continue;
                }
                double value = sanitizeNonNegative(number.doubleValue());
                if (value <= 0.0) {
                    continue;
                }
                String normalizedKey = normalizeDaoHenKey(fieldName);
                daoHen.merge(normalizedKey, value, Math::max);
            } catch (IllegalAccessException ignored) {
                continue;
            }
        }
        return Collections.unmodifiableMap(daoHen);
    }

    private static boolean isDaoHenField(String fieldName) {
        return fieldName.startsWith(DAO_HEN_PREFIX)
            || fieldName.startsWith(DA_HEN_PREFIX);
    }

    private static String normalizeDaoHenKey(String fieldName) {
        if (fieldName.startsWith(DAO_HEN_PREFIX)) {
            return fieldName.substring(DAO_HEN_PREFIX.length());
        }
        if (fieldName.startsWith(DA_HEN_PREFIX)) {
            return fieldName.substring(DA_HEN_PREFIX.length());
        }
        return fieldName;
    }

    private static int resolveHeavenScore(double qiyun, double qiyunShangxian) {
        double heavenValue = sanitizeNonNegative(qiyun);
        double qiyunMax = sanitizeNonNegative(qiyunShangxian);
        if (qiyunMax <= 0.0) {
            qiyunMax = QIYUN_MAX_MIN;
        }
        double normalizedQiyunMax = clampDouble(
            qiyunMax,
            QIYUN_MAX_MIN,
            QIYUN_MAX_MAX
        );
        return toScore(heavenValue, normalizedQiyunMax);
    }

    private static HumanQiResolution resolveHumanQi(OpeningProfileResolverInput input) {
        OptionalDouble renqi = sanitizeOptional(input.renqi());
        if (renqi.isPresent()) {
            return new HumanQiResolution(
                renqi.getAsDouble(),
                ResolvedOpeningProfile.HumanQiSource.REN_QI
            );
        }
        OptionalDouble humanQi = sanitizeOptional(input.humanQi());
        if (humanQi.isPresent()) {
            return new HumanQiResolution(
                humanQi.getAsDouble(),
                ResolvedOpeningProfile.HumanQiSource.HUMAN_QI_FALLBACK
            );
        }
        return new HumanQiResolution(
            0.0,
            ResolvedOpeningProfile.HumanQiSource.MISSING_FALLBACK
        );
    }

    private static EarthQiResolution resolveEarthQi(OpeningProfileResolverInput input) {
        OptionalDouble earthQi = sanitizeOptional(input.earthQi());
        if (earthQi.isPresent()) {
            return new EarthQiResolution(earthQi.getAsDouble(), false);
        }
        return new EarthQiResolution(0.0, true);
    }

    private static AptitudeResolution resolveAptitude(OpeningProfileResolverInput input) {
        double zhenyuan = sanitizeNonNegative(input.zuidaZhenyuan());
        double shouyuan = sanitizeNonNegative(input.shouyuan());
        double jingli = Math.max(
            sanitizeNonNegative(input.jingli()),
            sanitizeNonNegative(input.zuidaJingli())
        );
        double hunpo = Math.max(
            sanitizeNonNegative(input.hunpo()),
            sanitizeNonNegative(input.zuidaHunpo())
        );
        double tizhi = sanitizeNonNegative(input.tizhi());

        boolean allZero = zhenyuan <= 0.0
            && shouyuan <= 0.0
            && jingli <= 0.0
            && hunpo <= 0.0
            && tizhi <= 0.0;
        if (allZero) {
            return new AptitudeResolution(
                0,
                ResolvedOpeningProfile.AptitudeState.ALL_ZERO_FALLBACK
            );
        }

        int zhenyuanScore = toScore(zhenyuan, APTITUDE_ZHENYUAN_TARGET);
        int shouyuanScore = toScore(shouyuan, APTITUDE_SHOUYUAN_TARGET);
        int jingliScore = toScore(jingli, APTITUDE_JINGLI_TARGET);
        int hunpoScore = toScore(hunpo, APTITUDE_HUNPO_TARGET);
        int tizhiScore = toScore(tizhi, APTITUDE_TIZHI_TARGET);
        int aptitudeScore = (zhenyuanScore
            + shouyuanScore
            + jingliScore
            + hunpoScore
            + tizhiScore) / APTITUDE_COMPONENT_COUNT;
        return new AptitudeResolution(
            aptitudeScore,
            ResolvedOpeningProfile.AptitudeState.RESOLVED
        );
    }

    private static BenmingGuResolution resolveBenmingGu(double benminggu) {
        int benmingCode = toPositiveInt(benminggu);
        if (benmingCode <= 0) {
            return new BenmingGuResolution(
                BENMING_GU_FALLBACK,
                ResolvedOpeningProfile.BenmingGuState.UNKNOWN_FALLBACK
            );
        }
        return new BenmingGuResolution(
            benmingCode,
            ResolvedOpeningProfile.BenmingGuState.RESOLVED
        );
    }

    private static DaoMarkResolution resolveDaoMark(Map<String, Double> daoHen) {
        if (daoHen == null || daoHen.isEmpty()) {
            return DaoMarkResolution.sparseFallback();
        }

        String dominant = DEFAULT_DAO_MARK;
        double dominantValue = 0.0;
        double total = 0.0;
        Map<String, Double> sorted = new TreeMap<>(daoHen);
        for (Map.Entry<String, Double> entry : sorted.entrySet()) {
            double value = sanitizeNonNegative(entry.getValue());
            if (value <= 0.0) {
                continue;
            }
            String key = normalizeDaoMark(entry.getKey());
            total += value;
            if (value > dominantValue) {
                dominant = key;
                dominantValue = value;
                continue;
            }
            if (Double.compare(value, dominantValue) == 0
                && key.compareTo(dominant) < 0) {
                dominant = key;
            }
        }

        if (total <= 0.0 || dominantValue <= 0.0) {
            return DaoMarkResolution.sparseFallback();
        }
        return new DaoMarkResolution(
            dominant,
            dominantValue,
            total,
            ResolvedOpeningProfile.DaoMarkState.RESOLVED
        );
    }

    private static String normalizeDaoMark(String key) {
        if (key == null || key.isBlank()) {
            return DEFAULT_DAO_MARK;
        }
        return key;
    }

    private static int resolveBalanceScore(
        int heavenScore,
        int earthScore,
        int humanScore
    ) {
        int max = Math.max(heavenScore, Math.max(earthScore, humanScore));
        if (max <= 0) {
            return 0;
        }
        int min = Math.min(heavenScore, Math.min(earthScore, humanScore));
        double ratio = ((double) min / (double) max) * SCORE_SCALE;
        return clampScore((int) Math.floor(ratio));
    }

    private static int toScore(double value, double target) {
        if (target <= 0.0) {
            return 0;
        }
        double ratio = sanitizeNonNegative(value) / target;
        double scaled = ratio * SCORE_SCALE;
        return clampScore((int) Math.floor(scaled));
    }

    private static int toPositiveInt(double value) {
        if (!Double.isFinite(value) || value <= 0.0) {
            return 0;
        }
        return (int) Math.floor(value);
    }

    private static int toNonNegativeInt(double value) {
        if (!Double.isFinite(value) || value <= 0.0) {
            return 0;
        }
        return (int) Math.floor(value);
    }

    private static double sanitizeNonNegative(double value) {
        if (!Double.isFinite(value) || value <= 0.0) {
            return 0.0;
        }
        return value;
    }

    private static int clampScore(int score) {
        return Math.max(SCORE_MIN, Math.min(SCORE_MAX, score));
    }

    private static double clampDouble(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private record BenmingGuResolution(
        int benmingGuCode,
        ResolvedOpeningProfile.BenmingGuState benmingGuState
    ) {}

    private record DaoMarkResolution(
        String dominantDaoMark,
        double dominantDaoMarkValue,
        double totalDaoMarkValue,
        ResolvedOpeningProfile.DaoMarkState daoMarkState
    ) {

        private static DaoMarkResolution sparseFallback() {
            return new DaoMarkResolution(
                DEFAULT_DAO_MARK,
                0.0,
                0.0,
                ResolvedOpeningProfile.DaoMarkState.SPARSE_FALLBACK
            );
        }
    }

    private record AptitudeResolution(
        int aptitudeScore,
        ResolvedOpeningProfile.AptitudeState aptitudeState
    ) {}

    private record HumanQiResolution(
        double humanQiValue,
        ResolvedOpeningProfile.HumanQiSource humanQiSource
    ) {}

    private record EarthQiResolution(double earthQiValue, boolean fallbackApplied) {}

    /** 解析输入最小抽象。 */
    public interface OpeningProfileResolverInput {
        double zhuanshu();

        double jieduan();

        double kongqiao();

        double benminggu();

        double zuidaZhenyuan();

        double shouyuan();

        double jingli();

        double zuidaJingli();

        double hunpo();

        double zuidaHunpo();

        double tizhi();

        double qiyun();

        double qiyunShangxian();

        OptionalDouble renqi();

        OptionalDouble humanQi();

        OptionalDouble earthQi();

        Map<String, Double> daoHen();

        boolean ascensionAttemptInitiated();

        boolean snapshotFrozen();
    }

    public static final class BasicResolverInput implements OpeningProfileResolverInput {

        private final double zhuanshu;

        private final double jieduan;

        private final double kongqiao;

        private final double benminggu;

        private final double zuidaZhenyuan;

        private final double shouyuan;

        private final double jingli;

        private final double zuidaJingli;

        private final double hunpo;

        private final double zuidaHunpo;

        private final double tizhi;

        private final double qiyun;

        private final double qiyunShangxian;

        private final OptionalDouble renqi;

        private final OptionalDouble humanQi;

        private final OptionalDouble earthQi;

        private final Map<String, Double> daoHen;

        private final boolean ascensionAttemptInitiated;

        private final boolean snapshotFrozen;

        private BasicResolverInput(Builder builder) {
            this.zhuanshu = builder.zhuanshu;
            this.jieduan = builder.jieduan;
            this.kongqiao = builder.kongqiao;
            this.benminggu = builder.benminggu;
            this.zuidaZhenyuan = builder.zuidaZhenyuan;
            this.shouyuan = builder.shouyuan;
            this.jingli = builder.jingli;
            this.zuidaJingli = builder.zuidaJingli;
            this.hunpo = builder.hunpo;
            this.zuidaHunpo = builder.zuidaHunpo;
            this.tizhi = builder.tizhi;
            this.qiyun = builder.qiyun;
            this.qiyunShangxian = builder.qiyunShangxian;
            this.renqi = sanitizeOptional(builder.renqi);
            this.humanQi = sanitizeOptional(builder.humanQi);
            this.earthQi = sanitizeOptional(builder.earthQi);
            this.daoHen = freezeDaoHenMap(builder.daoHen);
            this.ascensionAttemptInitiated = builder.ascensionAttemptInitiated;
            this.snapshotFrozen = builder.snapshotFrozen;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static BasicResolverInput empty(
            OptionalDouble earthQi,
            OptionalDouble humanQi,
            boolean ascensionAttemptInitiated,
            boolean snapshotFrozen
        ) {
            return builder()
                .humanQi(humanQi)
                .earthQi(earthQi)
                .ascensionAttemptInitiated(ascensionAttemptInitiated)
                .snapshotFrozen(snapshotFrozen)
                .build();
        }

        private static Map<String, Double> freezeDaoHenMap(Map<String, Double> source) {
            if (source == null || source.isEmpty()) {
                return Collections.emptyMap();
            }
            Map<String, Double> sanitized = new LinkedHashMap<>();
            Map<String, Double> sorted = new TreeMap<>(source);
            for (Map.Entry<String, Double> entry : sorted.entrySet()) {
                String key = normalizeDaoMark(entry.getKey());
                double value = sanitizeNonNegative(entry.getValue());
                if (value <= 0.0) {
                    continue;
                }
                sanitized.put(key, value);
            }
            return Collections.unmodifiableMap(sanitized);
        }

        @Override
        public double zhuanshu() {
            return zhuanshu;
        }

        @Override
        public double jieduan() {
            return jieduan;
        }

        @Override
        public double kongqiao() {
            return kongqiao;
        }

        @Override
        public double benminggu() {
            return benminggu;
        }

        @Override
        public double zuidaZhenyuan() {
            return zuidaZhenyuan;
        }

        @Override
        public double shouyuan() {
            return shouyuan;
        }

        @Override
        public double jingli() {
            return jingli;
        }

        @Override
        public double zuidaJingli() {
            return zuidaJingli;
        }

        @Override
        public double hunpo() {
            return hunpo;
        }

        @Override
        public double zuidaHunpo() {
            return zuidaHunpo;
        }

        @Override
        public double tizhi() {
            return tizhi;
        }

        @Override
        public double qiyun() {
            return qiyun;
        }

        @Override
        public double qiyunShangxian() {
            return qiyunShangxian;
        }

        @Override
        public OptionalDouble renqi() {
            return renqi;
        }

        @Override
        public OptionalDouble humanQi() {
            return humanQi;
        }

        @Override
        public OptionalDouble earthQi() {
            return earthQi;
        }

        @Override
        public Map<String, Double> daoHen() {
            return daoHen;
        }

        @Override
        public boolean ascensionAttemptInitiated() {
            return ascensionAttemptInitiated;
        }

        @Override
        public boolean snapshotFrozen() {
            return snapshotFrozen;
        }

        /** 输入构建器。 */
        public static final class Builder {

            private double zhuanshu;

            private double jieduan;

            private double kongqiao;

            private double benminggu;

            private double zuidaZhenyuan;

            private double shouyuan;

            private double jingli;

            private double zuidaJingli;

            private double hunpo;

            private double zuidaHunpo;

            private double tizhi;

            private double qiyun;

            private double qiyunShangxian;

            private OptionalDouble renqi = OptionalDouble.empty();

            private OptionalDouble humanQi = OptionalDouble.empty();

            private OptionalDouble earthQi = OptionalDouble.empty();

            private Map<String, Double> daoHen = Collections.emptyMap();

            private boolean ascensionAttemptInitiated;

            private boolean snapshotFrozen;

            private Builder() {}

            public Builder zhuanshu(double value) {
                this.zhuanshu = value;
                return this;
            }

            public Builder jieduan(double value) {
                this.jieduan = value;
                return this;
            }

            public Builder kongqiao(double value) {
                this.kongqiao = value;
                return this;
            }

            public Builder benminggu(double value) {
                this.benminggu = value;
                return this;
            }

            public Builder zuidaZhenyuan(double value) {
                this.zuidaZhenyuan = value;
                return this;
            }

            public Builder shouyuan(double value) {
                this.shouyuan = value;
                return this;
            }

            public Builder jingli(double value) {
                this.jingli = value;
                return this;
            }

            public Builder zuidaJingli(double value) {
                this.zuidaJingli = value;
                return this;
            }

            public Builder hunpo(double value) {
                this.hunpo = value;
                return this;
            }

            public Builder zuidaHunpo(double value) {
                this.zuidaHunpo = value;
                return this;
            }

            public Builder tizhi(double value) {
                this.tizhi = value;
                return this;
            }

            public Builder qiyun(double value) {
                this.qiyun = value;
                return this;
            }

            public Builder qiyunShangxian(double value) {
                this.qiyunShangxian = value;
                return this;
            }

            public Builder renqi(OptionalDouble value) {
                this.renqi = value == null ? OptionalDouble.empty() : value;
                return this;
            }

            public Builder humanQi(OptionalDouble value) {
                this.humanQi = value == null ? OptionalDouble.empty() : value;
                return this;
            }

            public Builder earthQi(OptionalDouble value) {
                this.earthQi = value == null ? OptionalDouble.empty() : value;
                return this;
            }

            public Builder daoHen(Map<String, Double> value) {
                this.daoHen = value == null ? Collections.emptyMap() : value;
                return this;
            }

            public Builder ascensionAttemptInitiated(boolean value) {
                this.ascensionAttemptInitiated = value;
                return this;
            }

            public Builder snapshotFrozen(boolean value) {
                this.snapshotFrozen = value;
                return this;
            }

            public BasicResolverInput build() {
                return new BasicResolverInput(this);
            }
        }
    }
}
