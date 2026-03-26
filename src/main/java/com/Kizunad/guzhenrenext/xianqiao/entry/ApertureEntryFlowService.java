package com.Kizunad.guzhenrenext.xianqiao.entry;

import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionAttemptEntryChannel;
import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionAttemptServiceContract;
import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionAttemptStage;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ReturnPosition;
import com.Kizunad.guzhenrenext.xianqiao.opening.ResolvedOpeningProfile;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public final class ApertureEntryFlowService {

    private static final double BLOCK_CENTER_OFFSET = 0.5D;

    public EntryResult enter(
        ServerPlayer player,
        AscensionAttemptEntryChannel entryChannel,
        EntryHooks entryHooks
    ) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(entryChannel, "entryChannel");
        Objects.requireNonNull(entryHooks, "entryHooks");
        String servicePathId = AscensionAttemptServiceContract.resolveTransactionService(entryChannel);
        ServerLevel apertureLevel = player.server.getLevel(entryHooks.apertureDimension());
        if (apertureLevel == null) {
            return EntryResult.failure(Component.literal("仙窍天地尚未显化。"), servicePathId);
        }

        ResolvedOpeningProfile attemptProfile = entryHooks.resolveAttemptProfile(player, true);
        if (!attemptProfile.threeQiEvaluation().canEnterConfirmed()) {
            return EntryResult.failure(resolveBlockedMessage(attemptProfile), servicePathId);
        }

        ApertureWorldData worldData = ApertureWorldData.get(apertureLevel);
        UUID owner = player.getUUID();
        ApertureInfo apertureInfo = worldData.getOrAllocate(owner);
        entryHooks.initializeApertureIfNeeded(apertureLevel, worldData, player, apertureInfo);
        apertureInfo = worldData.getOrAllocate(owner);
        entryHooks.ensureLandSpiritExists(apertureLevel, player, apertureInfo.center());

        ServerLevel currentLevel = player.serverLevel();
        worldData.setReturnPosition(
            owner,
            new ReturnPosition(
                currentLevel.dimension().location().toString(),
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYRot(),
                player.getXRot()
            )
        );

        BlockPos center = apertureInfo.center();
        player.teleportTo(
            apertureLevel,
            center.getX() + BLOCK_CENTER_OFFSET,
            center.getY(),
            center.getZ() + BLOCK_CENTER_OFFSET,
            player.getYRot(),
            player.getXRot()
        );
        return EntryResult.success(servicePathId);
    }

    private static Component resolveBlockedMessage(ResolvedOpeningProfile profile) {
        AscensionAttemptStage suggestedStage = profile.suggestedStage();
        if (suggestedStage == AscensionAttemptStage.CULTIVATION_PROGRESS) {
            return Component.literal("未达五转巅峰，尚不可冲关。");
        }
        if (
            suggestedStage == AscensionAttemptStage.ASCENSION_PREPARATION_UNLOCKED
                || suggestedStage == AscensionAttemptStage.QI_OBSERVATION_AND_HARMONIZATION
        ) {
            return Component.literal("天地人三气尚未圆融，尚不可冲关。");
        }
        if (suggestedStage == AscensionAttemptStage.READY_TO_CONFIRM) {
            return Component.literal("升仙气机未定，尚不可冲关。");
        }
        return Component.literal("当前火候未足，尚不可冲关。");
    }

    public interface EntryHooks {

        ResourceKey<Level> apertureDimension();

        ResolvedOpeningProfile resolveAttemptProfile(ServerPlayer player, boolean playerInitiated);

        void initializeApertureIfNeeded(
            ServerLevel level,
            ApertureWorldData worldData,
            ServerPlayer player,
            ApertureInfo apertureInfo
        );

        void ensureLandSpiritExists(ServerLevel level, ServerPlayer player, BlockPos center);
    }

    public record EntryResult(boolean success, Component failureMessage, String servicePathId) {

        public EntryResult {
            servicePathId = Objects.requireNonNull(servicePathId, "servicePathId");
        }

        public static EntryResult failure(Component message, String servicePathId) {
            return new EntryResult(false, Objects.requireNonNull(message, "message"), servicePathId);
        }

        public static EntryResult success(String servicePathId) {
            return new EntryResult(true, null, servicePathId);
        }
    }
}
