package com.Kizunad.guzhenrenext.xianqiao.entry;

import com.Kizunad.guzhenrenext.xianqiao.opening.AscensionConditionSnapshot;
import com.Kizunad.guzhenrenext.xianqiao.opening.OpeningProfileResolver;
import com.Kizunad.guzhenrenext.xianqiao.opening.ResolvedOpeningProfile;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

public final class AperturePrimaryPathPlanningHelper {

    private static final long HASH_SEED = 1125899906842597L;

    private static final long HASH_MULTIPLIER = 31L;

    private static final String BENMING_UNKNOWN_DESCRIPTOR =
        "unknown_benming|state:unknown_fallback|benming_code:0";

    private static final BenmingSemanticBucket[] BENMING_SEMANTIC_BUCKETS = {
        new BenmingSemanticBucket("desert", "沙漠", "fire"),
        new BenmingSemanticBucket("ocean", "海", "water"),
        new BenmingSemanticBucket("forest", "林", "wood"),
        new BenmingSemanticBucket("icefield", "冰原", "ice"),
        new BenmingSemanticBucket("swamp", "沼泽", "poison"),
        new BenmingSemanticBucket("mountain", "山岳", "earth"),
        new BenmingSemanticBucket("soulsand", "魂沼", "soul"),
    };

    private final OpeningProfileResolver profileResolver;

    public AperturePrimaryPathPlanningHelper() {
        this(new OpeningProfileResolver());
    }

    public AperturePrimaryPathPlanningHelper(OpeningProfileResolver profileResolver) {
        this.profileResolver = Objects.requireNonNull(profileResolver, "profileResolver");
    }

    public PlanningBundle createFromPlayer(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        ResolvedOpeningProfile resolvedOpeningProfile = profileResolver.resolveFromPlayer(player, true);
        return new PlanningBundle(
            resolvedOpeningProfile,
            resolveBenmingDescriptor(resolvedOpeningProfile),
            resolveDeterministicSeed(player.getUUID(), resolvedOpeningProfile)
        );
    }

    private static String resolveBenmingDescriptor(ResolvedOpeningProfile resolvedOpeningProfile) {
        Objects.requireNonNull(resolvedOpeningProfile, "resolvedOpeningProfile");
        AscensionConditionSnapshot snapshot = resolvedOpeningProfile.conditionSnapshot();
        if (snapshot.benmingGuFallbackState() != AscensionConditionSnapshot.BenmingGuFallbackState.RESOLVED) {
            return BENMING_UNKNOWN_DESCRIPTOR;
        }
        int benmingCode = (int) Math.round(snapshot.benmingGuRawValue());
        if (benmingCode <= 0) {
            return BENMING_UNKNOWN_DESCRIPTOR;
        }
        BenmingSemanticBucket semanticBucket = BENMING_SEMANTIC_BUCKETS[
            Math.floorMod(benmingCode, BENMING_SEMANTIC_BUCKETS.length)
        ];
        String dominantDaoMark = resolveDominantDaoMark(snapshot.daoMarks());
        return "benming_code:" + benmingCode
            + "|state:resolved"
            + "|semantic:" + semanticBucket.semanticKey()
            + "|" + semanticBucket.chineseHint()
            + "|" + semanticBucket.daoHintKeyword()
            + "|dao:" + dominantDaoMark;
    }

    private static String resolveDominantDaoMark(Map<String, Double> daoMarks) {
        if (daoMarks == null || daoMarks.isEmpty()) {
            return "none";
        }
        String dominantKey = "none";
        double dominantValue = Double.NEGATIVE_INFINITY;
        for (Map.Entry<String, Double> entry : daoMarks.entrySet()) {
            String candidateKey = entry.getKey();
            if (candidateKey == null || candidateKey.isBlank()) {
                continue;
            }
            double candidateValue = entry.getValue() == null ? 0.0D : entry.getValue().doubleValue();
            if (candidateValue > dominantValue) {
                dominantKey = candidateKey;
                dominantValue = candidateValue;
                continue;
            }
            if (candidateValue == dominantValue && candidateKey.compareTo(dominantKey) < 0) {
                dominantKey = candidateKey;
            }
        }
        return dominantKey;
    }

    private static long resolveDeterministicSeed(UUID playerId, ResolvedOpeningProfile resolvedOpeningProfile) {
        long seed = HASH_SEED;
        seed = (seed * HASH_MULTIPLIER) + playerId.getMostSignificantBits();
        seed = (seed * HASH_MULTIPLIER) + playerId.getLeastSignificantBits();
        seed = (seed * HASH_MULTIPLIER)
            + Double.doubleToLongBits(resolvedOpeningProfile.conditionSnapshot().benmingGuRawValue());
        seed = (seed * HASH_MULTIPLIER) + resolvedOpeningProfile.conditionSnapshot().daoMarks().hashCode();
        return seed;
    }

    public record PlanningBundle(
        ResolvedOpeningProfile resolvedOpeningProfile,
        String benmingDescriptor,
        long deterministicSeed
    ) {

        public PlanningBundle {
            resolvedOpeningProfile = Objects.requireNonNull(resolvedOpeningProfile, "resolvedOpeningProfile");
            benmingDescriptor = Objects.requireNonNull(benmingDescriptor, "benmingDescriptor");
        }
    }

    private record BenmingSemanticBucket(String semanticKey, String chineseHint, String daoHintKeyword) {
    }
}
