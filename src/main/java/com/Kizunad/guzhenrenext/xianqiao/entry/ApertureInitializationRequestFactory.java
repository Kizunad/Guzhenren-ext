package com.Kizunad.guzhenrenext.xianqiao.entry;

import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.opening.AscensionConditionSnapshot;
import com.Kizunad.guzhenrenext.xianqiao.opening.AscensionReadinessStage;
import com.Kizunad.guzhenrenext.xianqiao.opening.AscensionThreeQiEvaluation;
import com.Kizunad.guzhenrenext.xianqiao.opening.OpeningProfileResolver;
import com.Kizunad.guzhenrenext.xianqiao.opening.ResolvedOpeningProfile;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import net.guzhenren.network.GuzhenrenModVariables;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class ApertureInitializationRequestFactory {

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

    private final OpeningProfileResolver profileResolver;

    public ApertureInitializationRequestFactory() {
        this(new OpeningProfileResolver());
    }

    public ApertureInitializationRequestFactory(OpeningProfileResolver profileResolver) {
        this.profileResolver = Objects.requireNonNull(profileResolver, "profileResolver");
    }

    public ApertureInitializationRequest createFromPlayer(
        ServerPlayer player,
        ApertureEntryChannel entryChannel
    ) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(entryChannel, "entryChannel");

        GuzhenrenModVariables.PlayerVariables variables = player.getData(GuzhenrenModVariables.PLAYER_VARIABLES);
        OptionalDouble earthQi = readOptionalDoubleField(variables, EARTH_QI_FIELD_CANDIDATES);
        OptionalDouble humanQi = readOptionalDoubleField(variables, HUMAN_QI_FIELD_CANDIDATES);
        AscensionAttemptSeam ascensionAttemptSeam = resolveAscensionAttemptSeam(variables);
        ResolvedOpeningProfile profile = profileResolver.resolve(
            OpeningProfileResolver.fromPlayerVariables(
                variables,
                earthQi,
                humanQi,
                ascensionAttemptSeam.ascensionAttemptInitiated(),
                ascensionAttemptSeam.snapshotFrozen()
            )
        );
        AscensionConditionSnapshot snapshot = profile.ascensionConditionSnapshot();
        AscensionThreeQiEvaluation evaluation = profile.threeQiEvaluation();
        boolean confirmed = evaluation.stage() == AscensionReadinessStage.CONFIRMED;

        return new ApertureInitializationRequest(
            player.getUUID(),
            entryChannel,
            ApertureEntryFlowContract.stagedFlow(),
            snapshot.heavenScore(),
            snapshot.earthScore(),
            snapshot.humanScore(),
            snapshot.balanceScore(),
            evaluation.rankFivePeak(),
            evaluation.eachQiAtLeast60(),
            evaluation.balanceAtLeast75(),
            confirmed,
            snapshot.snapshotFrozen(),
            isAlreadyInitialized(player)
        );
    }

    private static boolean isAlreadyInitialized(ServerPlayer player) {
        ServerLevel apertureLevel = player.server.getLevel(ApertureInitializationRuntimeExecutor.apertureDimension());
        if (apertureLevel == null) {
            return false;
        }
        return ApertureWorldData.get(apertureLevel).isApertureInitialized(player.getUUID());
    }

    /**
     * 从玩家变量解析升仙尝试语义接缝。
     * <p>
     * 规则说明：
     * </p>
     * <ul>
     *     <li>优先读取显式字段：{@code ascensionAttemptInitiated}/{@code snapshotFrozen}；</li>
     *     <li>若上游尚未提供显式字段，则回退读取蛊真人现有 {@code shiqiao} 位作为最小玩家态缝隙；</li>
     *     <li>当“主动发起”不成立时，冻结态会被强制归零，避免出现“未发起却已冻结”的伪 CONFIRMED 输入。</li>
     * </ul>
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

    private record AscensionAttemptSeam(
        boolean ascensionAttemptInitiated,
        boolean snapshotFrozen
    ) {}
}
