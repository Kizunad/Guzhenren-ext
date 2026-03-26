package com.Kizunad.guzhenrenext.xianqiao.entry;

import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionAttemptEntryChannel;
import com.Kizunad.guzhenrenext.xianqiao.command.ApertureCommand;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import java.util.Objects;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public final class ApertureEntryRuntime {

    private static final ResourceKey<Level> APERTURE_DIMENSION = ResourceKey.create(
        Registries.DIMENSION,
        ResourceLocation.fromNamespaceAndPath("guzhenrenext", "aperture_world")
    );

    private ApertureEntryRuntime() {
    }

    public static ApertureInitializationResult trigger(
        ServerPlayer player,
        ApertureEntryChannel entryChannel
    ) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(entryChannel, "entryChannel");

        boolean alreadyInitialized = isAlreadyInitialized(player);
        ApertureEntryFlowService.EntryResult result = ApertureCommand.executeUnifiedAscensionEntry(
            player,
            toAscensionEntryChannel(entryChannel)
        );
        if (!result.success()) {
            return new ApertureInitializationResult(
                resolveFailureStatus(result.failureMessage()),
                toMessage(result.failureMessage()),
                false
            );
        }
        return new ApertureInitializationResult(
            alreadyInitialized
                ? ApertureInitializationResult.Status.ALREADY_INITIALIZED
                : ApertureInitializationResult.Status.INITIALIZATION_EXECUTED,
            "",
            true
        );
    }

    private static AscensionAttemptEntryChannel toAscensionEntryChannel(ApertureEntryChannel entryChannel) {
        return switch (entryChannel) {
            case LEGACY_COMMAND -> AscensionAttemptEntryChannel.LEGACY_COMMAND_ADAPTER;
            case HUB_V1_GAMEPLAY -> AscensionAttemptEntryChannel.PLAYER_INITIATED_ENTRY;
        };
    }

    private static boolean isAlreadyInitialized(ServerPlayer player) {
        ServerLevel apertureLevel = player.server.getLevel(APERTURE_DIMENSION);
        if (apertureLevel == null) {
            return false;
        }
        return ApertureWorldData.get(apertureLevel).isApertureInitialized(player.getUUID());
    }

    private static ApertureInitializationResult.Status resolveFailureStatus(Component message) {
        String text = toMessage(message);
        if (text.contains("五转巅峰")) {
            return ApertureInitializationResult.Status.REJECTED_NOT_RANK_FIVE_PEAK;
        }
        if (text.contains("三气")) {
            return ApertureInitializationResult.Status.REJECTED_THREE_QI_NOT_READY;
        }
        if (text.contains("气机未定")) {
            return ApertureInitializationResult.Status.REJECTED_ASCENSION_ATTEMPT_NOT_CONFIRMED;
        }
        return ApertureInitializationResult.Status.FAILED_RUNTIME;
    }

    private static String toMessage(Component message) {
        if (message == null) {
            return "";
        }
        return message.getString();
    }
}
