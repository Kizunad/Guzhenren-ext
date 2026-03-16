package com.Kizunad.guzhenrenext.xianqiao.alchemy.effect;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ItemStackCustomDataHelper;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoMarkApi;
import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoMarkDiffusionService;
import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoType;
import com.Kizunad.guzhenrenext.xianqiao.tribulation.TribulationState;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class DeepPillEffectState {

    private static final String PLAYER_VARIABLES_TAG = "guzhenren:player_variables";
    private static final String VAR_ZUO_DA_ZHEN_YUAN = "zuida_zhenyuan";
    private static final String VAR_ZHEN_YUAN = "zhenyuan";
    private static final String VAR_JIE_DUAN = "jieduan";
    private static final String VAR_ZHONG_ZU = "zhongzu";

    public static final String KEY_FORCE_BREAKTHROUGH_USED = "GuzhenrenExtForceBreakthroughUsed";
    public static final String KEY_NEAR_DEATH_TOKEN = "GuzhenrenExtNearDeathToken";
    public static final String KEY_REVERSAL_END = "GuzhenrenExtReversalTimeEnd";
    public static final String KEY_REVERSAL_WEAK_END = "GuzhenrenExtReversalWeakEnd";
    public static final String KEY_TRIBULATION_DAMP_END = "GuzhenrenExtTribulationDampEnd";
    public static final String KEY_BEAST_DOMINATION_END = "GuzhenrenExtBeastDominationEnd";
    public static final String KEY_WORLD_SUPPRESSION_END = "GuzhenrenExtWorldSuppressionEnd";
    public static final String KEY_ENLIGHTENMENT_END = "GuzhenrenExtEnlightenmentEnd";
    public static final String KEY_BODY_RESHAPE_USED = "GuzhenrenExtBodyReshapeUsed";
    public static final String KEY_DAO_RESET_COUNT = "GuzhenrenExtDaoResetCount";
    public static final String KEY_POWER_DISPERSE_COUNT = "GuzhenrenExtPowerDisperseCount";

    public static final String USAGE_FORCE_BREAKTHROUGH_PENALTY =
        "guzhenrenext:deep_pill_force_breakthrough_penalty";
    public static final String USAGE_ENLIGHTENMENT_CAPACITY =
        "guzhenrenext:deep_pill_enlightenment_capacity";

    private static final int TICKS_PER_SECOND = 20;
    private static final int SECONDS_PER_MINUTE = 60;
    private static final long REVERSAL_DURATION_TICKS = (long) TICKS_PER_SECOND * SECONDS_PER_MINUTE * 10;
    private static final long REVERSAL_WEAK_DURATION_TICKS = (long) TICKS_PER_SECOND * SECONDS_PER_MINUTE * 60;
    private static final long TRIBULATION_DAMP_DURATION_TICKS = (long) TICKS_PER_SECOND * SECONDS_PER_MINUTE * 3;
    private static final long BEAST_DOMINATION_DURATION_TICKS = (long) TICKS_PER_SECOND * SECONDS_PER_MINUTE * 5;
    private static final long WORLD_SUPPRESSION_DURATION_TICKS = (long) TICKS_PER_SECOND * SECONDS_PER_MINUTE * 3;
    private static final long ENLIGHTENMENT_DURATION_TICKS = (long) TICKS_PER_SECOND * SECONDS_PER_MINUTE * 10;
    private static final double FORCE_BREAKTHROUGH_CAP_RATIO = 0.10D;
    private static final double FORCE_BREAKTHROUGH_MIN_PENALTY = 1.0D;
    private static final double POWER_DISPERSE_SHARD_RATIO = 0.25D;
    private static final int POWER_DISPERSE_MIN_SHARDS = 1;
    private static final double ENLIGHTENMENT_CAPACITY_BONUS = 50.0D;
    private static final double ENLIGHTENMENT_NIANTOU_MULTIPLIER = 2.0D;
    private static final double BODY_RESHAPE_RACE_MIN = 1.0D;
    private static final double BODY_RESHAPE_RACE_MAX = 4.0D;
    private static final int TRIBULATION_REDUCTION_RATIO_PERCENT = 70;
    private static final int AURA_RESET_SAMPLE_AMOUNT = 180;
    private static final int AURA_DISTRIBUTION_AMOUNT = 220;
    private static final double POWER_DISPERSE_STAGE_WEIGHT = 100.0D;
    private static final float REVERSAL_TIME_SPEED_BOOST = 4.0F;
    private static final float REVERSAL_TIME_SPEED_WEAK = 0.25F;
    private static final float NORMAL_TIME_SPEED = 1.0F;
    private static final float FLOAT_COMPARE_EPSILON = 0.001F;
    private static final int FULL_PERCENTAGE = 100;

    private DeepPillEffectState() {
    }

    public static void grantNearDeathToken(ServerPlayer player) {
        player.getPersistentData().putBoolean(KEY_NEAR_DEATH_TOKEN, true);
    }

    public static boolean consumeNearDeathToken(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.getBoolean(KEY_NEAR_DEATH_TOKEN)) {
            return false;
        }
        data.remove(KEY_NEAR_DEATH_TOKEN);
        return true;
    }

    public static boolean applyForceBreakthrough(ServerPlayer player) {
        if (player.getPersistentData().getBoolean(KEY_FORCE_BREAKTHROUGH_USED)) {
            return false;
        }
        CompoundTag variables = getOrCreatePlayerVariablesTag(player);
        double maxZhenyuan = Math.max(0.0D, variables.getDouble(VAR_ZUO_DA_ZHEN_YUAN));
        double penalty = Math.max(FORCE_BREAKTHROUGH_MIN_PENALTY, maxZhenyuan * FORCE_BREAKTHROUGH_CAP_RATIO);
        applyForceBreakthroughPenalty(player, variables, penalty);
        variables.putDouble(VAR_JIE_DUAN, Math.max(0.0D, variables.getDouble(VAR_JIE_DUAN) + 1.0D));
        double fixedMaxZhenyuan = Math.max(0.0D, variables.getDouble(VAR_ZUO_DA_ZHEN_YUAN));
        double fixedZhenyuan = Math.max(0.0D, Math.min(variables.getDouble(VAR_ZHEN_YUAN), fixedMaxZhenyuan));
        variables.putDouble(VAR_ZHEN_YUAN, fixedZhenyuan);
        player.getPersistentData().putBoolean(KEY_FORCE_BREAKTHROUGH_USED, true);
        return true;
    }

    public static boolean reshuffleDaoMarks(ServerPlayer player) {
        Optional<ApertureInfoContext> context = resolveApertureContext(player);
        if (context.isEmpty()) {
            return false;
        }
        try {
            for (DaoHenHelper.DaoType daoType : DAO_RESET_TYPES) {
                DaoHenHelper.setDaoHen(player, daoType, 0.0D);
            }
            BlockPos center = context.get().apertureInfo.center();
            DaoType dominantDao = resolveDominantAuraType(context.get().level, center).orElse(DaoType.TIME);
            DaoHenHelper.DaoType mapped = mapAuraToGuzhenrenDaoType(dominantDao);
            for (DaoHenHelper.DaoType daoType : DAO_RESET_TYPES) {
                double value = daoType == mapped ? AURA_DISTRIBUTION_AMOUNT : AURA_RESET_SAMPLE_AMOUNT;
                DaoHenHelper.setDaoHen(player, daoType, value);
            }
        } catch (LinkageError ignored) {
        }
        player.getPersistentData().putInt(
            KEY_DAO_RESET_COUNT,
            player.getPersistentData().getInt(KEY_DAO_RESET_COUNT) + 1
        );
        return true;
    }

    public static boolean activateTimeReversal(ServerPlayer player) {
        Optional<ApertureInfoContext> context = resolveApertureContext(player);
        if (context.isEmpty()) {
            return false;
        }
        long gameTime = context.get().level.getGameTime();
        player.getPersistentData().putLong(KEY_REVERSAL_END, gameTime + REVERSAL_DURATION_TICKS);
        player.getPersistentData().putLong(KEY_REVERSAL_WEAK_END, gameTime + REVERSAL_WEAK_DURATION_TICKS);
        return true;
    }

    public static boolean triggerDampenedTribulation(ServerPlayer player) {
        Optional<ApertureInfoContext> context = resolveApertureContext(player);
        if (context.isEmpty()) {
            return false;
        }
        long now = context.get().level.getGameTime();
        player.getPersistentData().putLong(KEY_TRIBULATION_DAMP_END, now + TRIBULATION_DAMP_DURATION_TICKS);
        context.get().worldData.updateTribulationTick(player.getUUID(), now);
        return true;
    }

    public static int disperseCultivation(ServerPlayer player, ItemStack stack) {
        CompoundTag variables = getOrCreatePlayerVariablesTag(player);
        double condensedPower = Math.max(0.0D, variables.getDouble(VAR_ZHEN_YUAN))
            + Math.max(0.0D, variables.getDouble(VAR_JIE_DUAN) * POWER_DISPERSE_STAGE_WEIGHT);
        variables.putDouble(VAR_ZHEN_YUAN, 0.0D);
        variables.putDouble(VAR_JIE_DUAN, 0.0D);
        int shards = Math.max(POWER_DISPERSE_MIN_SHARDS, (int) Math.floor(condensedPower * POWER_DISPERSE_SHARD_RATIO));
        CompoundTag customDataTag = ItemStackCustomDataHelper.copyCustomDataTag(stack);
        customDataTag.putInt("guzhenrenext_dao_shard_count", shards);
        ItemStackCustomDataHelper.setCustomDataTag(stack, customDataTag);
        player.getPersistentData().putInt(
            KEY_POWER_DISPERSE_COUNT,
            player.getPersistentData().getInt(KEY_POWER_DISPERSE_COUNT) + 1
        );
        return shards;
    }

    public static void activateBeastDomination(ServerPlayer player, long gameTime) {
        player.getPersistentData().putLong(KEY_BEAST_DOMINATION_END, gameTime + BEAST_DOMINATION_DURATION_TICKS);
    }

    public static void activateWorldSuppression(ServerPlayer player, long gameTime) {
        player.getPersistentData().putLong(KEY_WORLD_SUPPRESSION_END, gameTime + WORLD_SUPPRESSION_DURATION_TICKS);
    }

    public static void activateEnlightenment(ServerPlayer player, long gameTime) {
        player.getPersistentData().putLong(KEY_ENLIGHTENMENT_END, gameTime + ENLIGHTENMENT_DURATION_TICKS);
        applyEnlightenmentCapacityBonus(player);
    }

    public static boolean applyBodyReshape(ServerPlayer player) {
        if (player.getPersistentData().getBoolean(KEY_BODY_RESHAPE_USED)) {
            return false;
        }
        CompoundTag variables = getOrCreatePlayerVariablesTag(player);
        double currentRace = variables.getDouble(VAR_ZHONG_ZU);
        double targetRace = Math.max(BODY_RESHAPE_RACE_MIN, Math.min(BODY_RESHAPE_RACE_MAX, currentRace + 1.0D));
        if (Double.compare(currentRace, targetRace) == 0) {
            return false;
        }
        variables.putDouble(VAR_ZHONG_ZU, targetRace);
        player.getPersistentData().putBoolean(KEY_BODY_RESHAPE_USED, true);
        return true;
    }

    public static void markForceBreakthroughUsed(ServerPlayer player) {
        player.getPersistentData().putBoolean(KEY_FORCE_BREAKTHROUGH_USED, true);
    }

    public static void markDaoResetUsed(ServerPlayer player) {
        player.getPersistentData().putInt(
            KEY_DAO_RESET_COUNT,
            player.getPersistentData().getInt(KEY_DAO_RESET_COUNT) + 1
        );
    }

    public static int recordPowerDisperseFallback(ServerPlayer player, ItemStack stack) {
        int shards = POWER_DISPERSE_MIN_SHARDS;
        CompoundTag customDataTag = ItemStackCustomDataHelper.copyCustomDataTag(stack);
        customDataTag.putInt("guzhenrenext_dao_shard_count", shards);
        ItemStackCustomDataHelper.setCustomDataTag(stack, customDataTag);
        player.getPersistentData().putInt(
            KEY_POWER_DISPERSE_COUNT,
            player.getPersistentData().getInt(KEY_POWER_DISPERSE_COUNT) + 1
        );
        return shards;
    }

    public static void markEnlightenmentWindow(ServerPlayer player, long gameTime) {
        player.getPersistentData().putLong(KEY_ENLIGHTENMENT_END, gameTime + ENLIGHTENMENT_DURATION_TICKS);
    }

    public static void markBodyReshapeUsed(ServerPlayer player) {
        player.getPersistentData().putBoolean(KEY_BODY_RESHAPE_USED, true);
    }

    public static void tickPlayer(ServerPlayer player, long gameTime) {
        CompoundTag data = player.getPersistentData();
        processReversal(player, data, gameTime);
        processEnlightenment(player, data, gameTime);
    }

    public static void tickApertureWorld(ServerLevel apertureLevel) {
        long gameTime = apertureLevel.getGameTime();
        for (ServerPlayer player : apertureLevel.getServer().getPlayerList().getPlayers()) {
            if (!player.level().dimension().equals(DaoMarkDiffusionService.APERTURE_DIMENSION)) {
                continue;
            }
            Optional<ApertureInfoContext> context = resolveApertureContext(player);
            if (context.isEmpty()) {
                continue;
            }
            processTribulationDamp(context.get().worldData, player, gameTime);
            processWorldSuppression(context.get().worldData, player, gameTime);
        }
    }

    public static boolean isEnlightenmentActive(Player player, long gameTime) {
        return player.getPersistentData().getLong(KEY_ENLIGHTENMENT_END) > gameTime;
    }

    public static double getEnlightenmentNianTouMultiplier(Player player, long gameTime) {
        return isEnlightenmentActive(player, gameTime) ? ENLIGHTENMENT_NIANTOU_MULTIPLIER : 1.0D;
    }

    public static boolean isWorldSuppressionActive(Player player, long gameTime) {
        return player.getPersistentData().getLong(KEY_WORLD_SUPPRESSION_END) > gameTime;
    }

    public static CompoundTag dumpDebugState(ServerPlayer player) {
        CompoundTag source = player.getPersistentData();
        CompoundTag result = new CompoundTag();
        result.putBoolean(KEY_NEAR_DEATH_TOKEN, source.getBoolean(KEY_NEAR_DEATH_TOKEN));
        result.putBoolean(KEY_FORCE_BREAKTHROUGH_USED, source.getBoolean(KEY_FORCE_BREAKTHROUGH_USED));
        result.putBoolean(KEY_BODY_RESHAPE_USED, source.getBoolean(KEY_BODY_RESHAPE_USED));
        result.putInt(KEY_DAO_RESET_COUNT, source.getInt(KEY_DAO_RESET_COUNT));
        result.putInt(KEY_POWER_DISPERSE_COUNT, source.getInt(KEY_POWER_DISPERSE_COUNT));
        result.putLong(KEY_REVERSAL_END, source.getLong(KEY_REVERSAL_END));
        result.putLong(KEY_REVERSAL_WEAK_END, source.getLong(KEY_REVERSAL_WEAK_END));
        result.putLong(KEY_TRIBULATION_DAMP_END, source.getLong(KEY_TRIBULATION_DAMP_END));
        result.putLong(KEY_BEAST_DOMINATION_END, source.getLong(KEY_BEAST_DOMINATION_END));
        result.putLong(KEY_WORLD_SUPPRESSION_END, source.getLong(KEY_WORLD_SUPPRESSION_END));
        result.putLong(KEY_ENLIGHTENMENT_END, source.getLong(KEY_ENLIGHTENMENT_END));
        return result;
    }

    private static void processReversal(ServerPlayer player, CompoundTag data, long gameTime) {
        Optional<ApertureInfoContext> context = resolveApertureContext(player);
        if (context.isEmpty()) {
            return;
        }
        ApertureInfo info = context.get().apertureInfo;
        if (data.getLong(KEY_REVERSAL_END) > gameTime) {
            context.get().worldData.updateTimeSpeed(player.getUUID(), REVERSAL_TIME_SPEED_BOOST);
            return;
        }
        if (data.getLong(KEY_REVERSAL_WEAK_END) > gameTime) {
            context.get().worldData.updateTimeSpeed(player.getUUID(), REVERSAL_TIME_SPEED_WEAK);
        } else if (Math.abs(info.timeSpeed() - NORMAL_TIME_SPEED) > FLOAT_COMPARE_EPSILON) {
            context.get().worldData.updateTimeSpeed(player.getUUID(), NORMAL_TIME_SPEED);
        }
    }

    private static void processTribulationDamp(ApertureWorldData worldData, ServerPlayer player, long gameTime) {
        if (player.getPersistentData().getLong(KEY_TRIBULATION_DAMP_END) <= gameTime) {
            return;
        }
        long reducedWindow = gameTime
            + (long) TribulationState.STRIKE.durationTicks() * TRIBULATION_REDUCTION_RATIO_PERCENT / FULL_PERCENTAGE;
        worldData.updateTribulationTick(player.getUUID(), reducedWindow);
    }

    private static void processWorldSuppression(ApertureWorldData worldData, ServerPlayer player, long gameTime) {
        boolean suppressing = player.getPersistentData().getLong(KEY_WORLD_SUPPRESSION_END) > gameTime;
        worldData.updateFrozen(player.getUUID(), suppressing);
    }

    private static void processEnlightenment(ServerPlayer player, CompoundTag data, long gameTime) {
        if (data.getLong(KEY_ENLIGHTENMENT_END) > gameTime) {
            double multiplier = getEnlightenmentNianTouMultiplier(player, gameTime);
            double delta = NianTouHelper.getAmount(player) * (multiplier - 1.0D);
            if (delta > 0.0D) {
                NianTouHelper.modify(player, delta / TICKS_PER_SECOND);
            }
            return;
        }
        removeEnlightenmentCapacityBonus(player);
    }

    private static CompoundTag getOrCreatePlayerVariablesTag(ServerPlayer player) {
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains(PLAYER_VARIABLES_TAG, CompoundTag.TAG_COMPOUND)) {
            persistentData.put(PLAYER_VARIABLES_TAG, new CompoundTag());
        }
        return persistentData.getCompound(PLAYER_VARIABLES_TAG);
    }

    private static void applyForceBreakthroughPenalty(ServerPlayer player, CompoundTag variables, double penalty) {
        try {
            GuzhenrenVariableModifierService.setAdditiveModifier(
                player,
                GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN,
                USAGE_FORCE_BREAKTHROUGH_PENALTY,
                -penalty
            );
        } catch (LinkageError ignored) {
            double nextMax = Math.max(0.0D, variables.getDouble(VAR_ZUO_DA_ZHEN_YUAN) - penalty);
            variables.putDouble(VAR_ZUO_DA_ZHEN_YUAN, nextMax);
        }
    }

    private static void applyEnlightenmentCapacityBonus(ServerPlayer player) {
        try {
            GuzhenrenVariableModifierService.setAdditiveModifier(
                player,
                GuzhenrenVariableModifierService.VAR_NIANTOU_CAPACITY,
                USAGE_ENLIGHTENMENT_CAPACITY,
                ENLIGHTENMENT_CAPACITY_BONUS
            );
        } catch (LinkageError ignored) {
            return;
        }
    }

    private static void removeEnlightenmentCapacityBonus(ServerPlayer player) {
        try {
            GuzhenrenVariableModifierService.removeModifier(
                player,
                GuzhenrenVariableModifierService.VAR_NIANTOU_CAPACITY,
                USAGE_ENLIGHTENMENT_CAPACITY
            );
        } catch (LinkageError ignored) {
            return;
        }
    }

    private static Optional<ApertureInfoContext> resolveApertureContext(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return Optional.empty();
        }
        ServerLevel apertureLevel = server.getLevel(DaoMarkDiffusionService.APERTURE_DIMENSION);
        if (apertureLevel == null) {
            apertureLevel = player.serverLevel();
        }
        ApertureWorldData worldData = ApertureWorldData.get(apertureLevel);
        ApertureInfo apertureInfo = worldData.getAperture(player.getUUID());
        if (apertureInfo == null) {
            return Optional.empty();
        }
        return Optional.of(new ApertureInfoContext(apertureLevel, worldData, apertureInfo));
    }

    private static Optional<DaoType> resolveDominantAuraType(Level level, BlockPos center) {
        return Arrays.stream(DaoType.values())
            .max(Comparator.comparingInt(type -> DaoMarkApi.getAura(level, center, type)));
    }

    private static DaoHenHelper.DaoType mapAuraToGuzhenrenDaoType(DaoType auraType) {
        return switch (auraType) {
            case FIRE -> DaoHenHelper.DaoType.HUO_DAO;
            case WATER -> DaoHenHelper.DaoType.SHUI_DAO;
            case EARTH -> DaoHenHelper.DaoType.TU_DAO;
            case WOOD -> DaoHenHelper.DaoType.MU_DAO;
            case METAL -> DaoHenHelper.DaoType.JIN_DAO;
            case LIGHTNING -> DaoHenHelper.DaoType.LEI_DAO;
            case WIND -> DaoHenHelper.DaoType.FENG_DAO;
            case ICE -> DaoHenHelper.DaoType.BING_DAO;
            case DEATH -> DaoHenHelper.DaoType.AN_DAO;
            case LIFE -> DaoHenHelper.DaoType.REN_DAO;
            case TIME -> DaoHenHelper.DaoType.ZHOU_DAO;
            case SPACE -> DaoHenHelper.DaoType.YU_DAO;
            case POISON -> DaoHenHelper.DaoType.DU_DAO;
            case SOUL -> DaoHenHelper.DaoType.HUN_DAO;
            case SWORD -> DaoHenHelper.DaoType.JIAN_DAO;
            case BLOOD -> DaoHenHelper.DaoType.XUE_DAO;
            case STRENGTH -> DaoHenHelper.DaoType.LI_DAO;
            case RULE -> DaoHenHelper.DaoType.LV_DAO;
            case WISDOM -> DaoHenHelper.DaoType.ZHI_DAO;
            case DARK -> DaoHenHelper.DaoType.AN_DAO;
            case LIGHT -> DaoHenHelper.DaoType.GUANG_DAO;
            case CLOUD -> DaoHenHelper.DaoType.YUN_DAO;
            case STAR -> DaoHenHelper.DaoType.XING_DAO;
            case MOON -> DaoHenHelper.DaoType.YUE_DAO;
            case TRANSFORMATION -> DaoHenHelper.DaoType.BIAN_HUA_DAO;
            case DREAM -> DaoHenHelper.DaoType.MENG_DAO;
            case EMOTION -> DaoHenHelper.DaoType.XIN_DAO;
            case LUCK -> DaoHenHelper.DaoType.QI_DAO;
            case FATE -> DaoHenHelper.DaoType.TIAN_DAO;
        };
    }

    private record ApertureInfoContext(ServerLevel level, ApertureWorldData worldData, ApertureInfo apertureInfo) {
    }

    private static final DaoHenHelper.DaoType[] DAO_RESET_TYPES = {
        DaoHenHelper.DaoType.HUO_DAO,
        DaoHenHelper.DaoType.SHUI_DAO,
        DaoHenHelper.DaoType.TU_DAO,
        DaoHenHelper.DaoType.MU_DAO,
        DaoHenHelper.DaoType.JIN_DAO,
        DaoHenHelper.DaoType.LEI_DAO,
        DaoHenHelper.DaoType.FENG_DAO,
        DaoHenHelper.DaoType.BING_DAO,
        DaoHenHelper.DaoType.REN_DAO,
        DaoHenHelper.DaoType.HUN_DAO,
        DaoHenHelper.DaoType.JIAN_DAO,
        DaoHenHelper.DaoType.XUE_DAO,
        DaoHenHelper.DaoType.LI_DAO,
        DaoHenHelper.DaoType.LV_DAO,
        DaoHenHelper.DaoType.ZHI_DAO,
        DaoHenHelper.DaoType.AN_DAO,
        DaoHenHelper.DaoType.GUANG_DAO,
        DaoHenHelper.DaoType.YUN_DAO,
        DaoHenHelper.DaoType.XING_DAO,
        DaoHenHelper.DaoType.YUE_DAO,
        DaoHenHelper.DaoType.BIAN_HUA_DAO,
        DaoHenHelper.DaoType.MENG_DAO,
        DaoHenHelper.DaoType.XIN_DAO,
        DaoHenHelper.DaoType.QI_DAO,
        DaoHenHelper.DaoType.TIAN_DAO,
        DaoHenHelper.DaoType.ZHOU_DAO,
        DaoHenHelper.DaoType.YU_DAO,
        DaoHenHelper.DaoType.DU_DAO,
        DaoHenHelper.DaoType.NU_DAO,
        DaoHenHelper.DaoType.LIAN_DAO
    };
}
