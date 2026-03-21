package com.Kizunad.guzhenrenext.kongqiao.flyingsword.training;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordStateAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.calculator.FlyingSwordAttributes;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.BenmingSwordReadonlyModifierHelper;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.BenmingSwordResourceTransaction;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.CultivationSnapshot;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.resonance.FlyingSwordResonanceType;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ItemStackCustomDataHelper;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

public final class FlyingSwordTrainingService {

    private FlyingSwordTrainingService() {}

    private static final int SLOT_SWORD = 0;
    private static final int SLOT_FUEL = 1;
    private static final int EXP_PER_TICK = 1;
    private static final int AFFINITY_PROC_DENOMINATOR = 20;
    private static final int AFFINITY_GAIN = 1;
    private static final int BENMING_BONUS_EXP = 1;
    private static final int BENMING_BONUS_AFFINITY = 1;
    private static final double BENMING_BONUS_RESONANCE = 0.01D;
    private static final double BENMING_MAX_RESONANCE = 1.0D;
    private static final int OFFENSE_ROUTE_BONUS_EXP = 2;
    private static final int DEFENSE_ROUTE_BONUS_EXP = 0;
    private static final int SPIRIT_ROUTE_BONUS_EXP = 1;
    private static final int DEVOUR_ROUTE_BONUS_EXP = 0;
    private static final int OFFENSE_ROUTE_BONUS_AFFINITY = 0;
    private static final int DEFENSE_ROUTE_BONUS_AFFINITY = 0;
    private static final int SPIRIT_ROUTE_BONUS_AFFINITY = 3;
    private static final int DEVOUR_ROUTE_BONUS_AFFINITY = 4;
    private static final double OFFENSE_ROUTE_RESONANCE_MULTIPLIER = 0.8D;
    private static final double DEFENSE_ROUTE_RESONANCE_MULTIPLIER = 1.25D;
    private static final double SPIRIT_ROUTE_RESONANCE_MULTIPLIER = 1.0D;
    private static final double DEVOUR_ROUTE_RESONANCE_MULTIPLIER = 1.35D;
    private static final double BENMING_BONUS_ZHENYUAN_COST = 4.0D;
    private static final double BENMING_BONUS_NIANTOU_COST = 2.0D;
    private static final double BENMING_BONUS_HUNPO_COST = 1.0D;
    private static final BenmingSwordResourceTransaction.Request BENMING_BONUS_REQUEST =
        new BenmingSwordResourceTransaction.Request(
            BENMING_BONUS_ZHENYUAN_COST,
            BENMING_BONUS_NIANTOU_COST,
            BENMING_BONUS_HUNPO_COST
        );
    private static final BenmingTrainingRewardProfile LEGACY_BENMING_REWARD_PROFILE =
        new BenmingTrainingRewardProfile(
            BENMING_BONUS_EXP,
            BENMING_BONUS_AFFINITY,
            SPIRIT_ROUTE_RESONANCE_MULTIPLIER
        );
    private static final Map<FlyingSwordResonanceType, BenmingTrainingRewardProfile>
        BENMING_ROUTE_REWARD_PROFILE_MAP = createBenmingRouteRewardProfileMap();
    private static ReadonlyRewardModifierProvider readonlyRewardModifierProvider =
        player -> {
            final CultivationSnapshot snapshot = CultivationSnapshot.capture(player);
            return BenmingSwordReadonlyModifierHelper.fromSnapshot(snapshot);
        };
    private static BonusResourceGate bonusResourceGate = player ->
        BenmingSwordResourceTransaction.tryConsume(player, BENMING_BONUS_REQUEST).success();

    @FunctionalInterface
    public interface BonusResourceGate {

        boolean tryConsume(ServerPlayer player);
    }

    @FunctionalInterface
    public interface ReadonlyRewardModifierProvider {

        BenmingSwordReadonlyModifierHelper.ReadonlyModifier resolve(ServerPlayer player);
    }

    private record BenmingTrainingRewardProfile(
        int bonusExp,
        int bonusAffinity,
        double resonanceMultiplier
    ) {}

    public static void tick(ServerPlayer player) {
        if (player == null) {
            return;
        }
        FlyingSwordTrainingAttachment training =
            KongqiaoAttachments.getFlyingSwordTraining(player);
        if (training == null) {
            return;
        }
        tickInternal(training, player);
    }

    public static void tickInternal(
        FlyingSwordTrainingAttachment training,
        ServerPlayer player
    ) {
        if (training == null) {
            return;
        }

        ItemStack swordStack = training.getInputSlots().getStackInSlot(SLOT_SWORD);
        if (!isTrainingSword(swordStack)) {
            return;
        }

        int fuelTime = training.getFuelTime();
        if (fuelTime <= 0 && !tryRefillFuel(training)) {
            return;
        }

        if (training.getFuelTime() <= 0) {
            return;
        }

        training.setFuelTime(training.getFuelTime() - 1);

        CompoundTag root = ItemStackCustomDataHelper.copyCustomDataTag(swordStack);
        FlyingSwordAttributes attributes = FlyingSwordAttributes.fromNBT(
            root.getCompound("Attributes")
        );
        attributes.addExperience(EXP_PER_TICK);
        training.addAccumulatedExp(EXP_PER_TICK);

        if (
            player != null &&
            player.getRandom().nextInt(AFFINITY_PROC_DENOMINATOR) == 0
        ) {
            attributes.getSpiritData().addAffinity(AFFINITY_GAIN);
        }

        if (isBenmingSwordOwnedByPlayer(attributes, player)) {
            if (bonusResourceGate.tryConsume(player)) {
                applyBenmingRewardProfile(training, attributes, player);
            }
        }

        root.put("Attributes", attributes.toNBT());
        ItemStackCustomDataHelper.setCustomDataTag(swordStack, root);
        training.getInputSlots().setStackInSlot(SLOT_SWORD, swordStack);
    }

    private static void applyBenmingRewardProfile(
        final FlyingSwordTrainingAttachment training,
        final FlyingSwordAttributes attributes,
        final ServerPlayer player
    ) {
        final BenmingTrainingRewardProfile rewardProfile =
            resolveBenmingRewardProfile(player);
        if (rewardProfile.bonusExp() > 0) {
            attributes.addExperience(rewardProfile.bonusExp());
            training.addAccumulatedExp(rewardProfile.bonusExp());
        }
        if (rewardProfile.bonusAffinity() > 0) {
            attributes.getSpiritData().addAffinity(rewardProfile.bonusAffinity());
        }

        final double readonlyRewardResonanceGain = applyReadonlyRewardMultiplier(
            BENMING_BONUS_RESONANCE * rewardProfile.resonanceMultiplier(),
            player
        );
        final double nextResonance = Math.min(
            BENMING_MAX_RESONANCE,
            attributes.getBond().getResonance() + readonlyRewardResonanceGain
        );
        attributes.getBond().setResonance(nextResonance);
    }

    private static BenmingTrainingRewardProfile resolveBenmingRewardProfile(
        final ServerPlayer player
    ) {
        if (player == null) {
            return LEGACY_BENMING_REWARD_PROFILE;
        }
        final FlyingSwordStateAttachment state =
            KongqiaoAttachments.getFlyingSwordState(player);
        if (state == null) {
            return LEGACY_BENMING_REWARD_PROFILE;
        }
        return FlyingSwordResonanceType.resolve(state.getResonanceType())
            .map(type ->
                BENMING_ROUTE_REWARD_PROFILE_MAP.getOrDefault(
                    type,
                    LEGACY_BENMING_REWARD_PROFILE
                ))
            .orElse(LEGACY_BENMING_REWARD_PROFILE);
    }

    private static Map<FlyingSwordResonanceType, BenmingTrainingRewardProfile>
        createBenmingRouteRewardProfileMap() {
        final EnumMap<FlyingSwordResonanceType, BenmingTrainingRewardProfile> map =
            new EnumMap<>(FlyingSwordResonanceType.class);

        map.put(
            FlyingSwordResonanceType.OFFENSE,
            new BenmingTrainingRewardProfile(
                OFFENSE_ROUTE_BONUS_EXP,
                OFFENSE_ROUTE_BONUS_AFFINITY,
                OFFENSE_ROUTE_RESONANCE_MULTIPLIER
            )
        );

        map.put(
            FlyingSwordResonanceType.DEFENSE,
            new BenmingTrainingRewardProfile(
                DEFENSE_ROUTE_BONUS_EXP,
                DEFENSE_ROUTE_BONUS_AFFINITY,
                DEFENSE_ROUTE_RESONANCE_MULTIPLIER
            )
        );

        map.put(
            FlyingSwordResonanceType.SPIRIT,
            new BenmingTrainingRewardProfile(
                SPIRIT_ROUTE_BONUS_EXP,
                SPIRIT_ROUTE_BONUS_AFFINITY,
                SPIRIT_ROUTE_RESONANCE_MULTIPLIER
            )
        );

        map.put(
            FlyingSwordResonanceType.DEVOUR,
            new BenmingTrainingRewardProfile(
                DEVOUR_ROUTE_BONUS_EXP,
                DEVOUR_ROUTE_BONUS_AFFINITY,
                DEVOUR_ROUTE_RESONANCE_MULTIPLIER
            )
        );

        return Map.copyOf(map);
    }

    private static boolean tryRefillFuel(FlyingSwordTrainingAttachment training) {
        ItemStack fuelStack = training.getInputSlots().getStackInSlot(SLOT_FUEL);
        int fuelTicks = FuelHelper.getFuelTime(fuelStack);
        if (fuelTicks <= 0) {
            training.clearFuelState();
            return false;
        }

        fuelStack.shrink(1);
        training.getInputSlots().setStackInSlot(SLOT_FUEL, fuelStack);
        training.setFuelTime(fuelTicks);
        training.setMaxFuelTime(fuelTicks);
        return true;
    }

    private static boolean isTrainingSword(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() instanceof SwordItem;
    }

    private static boolean isBenmingSwordOwnedByPlayer(
        FlyingSwordAttributes attributes,
        ServerPlayer player
    ) {
        if (attributes == null || player == null) {
            return false;
        }
        String ownerUuid = attributes.getBond().getOwnerUuid();
        return ownerUuid != null && ownerUuid.equals(player.getUUID().toString());
    }

    private static double applyReadonlyRewardMultiplier(
        final double baseReward,
        final ServerPlayer player
    ) {
        if (player == null || baseReward <= 0.0D) {
            return 0.0D;
        }
        final BenmingSwordReadonlyModifierHelper.ReadonlyModifier modifier =
            readonlyRewardModifierProvider.resolve(player);
        return modifier.applyToReward(baseReward);
    }

    public static void installReadonlyRewardModifierProviderForTest(
        final ReadonlyRewardModifierProvider provider
    ) {
        if (provider == null) {
            resetReadonlyRewardModifierProviderForTest();
            return;
        }
        readonlyRewardModifierProvider = provider;
    }

    public static void resetReadonlyRewardModifierProviderForTest() {
        readonlyRewardModifierProvider = player -> {
            final CultivationSnapshot snapshot = CultivationSnapshot.capture(player);
            return BenmingSwordReadonlyModifierHelper.fromSnapshot(snapshot);
        };
    }

    public static void installBonusResourceGateForTest(BonusResourceGate gate) {
        if (gate == null) {
            resetBonusResourceGateForTest();
            return;
        }
        bonusResourceGate = gate;
    }

    public static void resetBonusResourceGateForTest() {
        bonusResourceGate = player ->
            BenmingSwordResourceTransaction.tryConsume(player, BENMING_BONUS_REQUEST).success();
    }
}
