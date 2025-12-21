package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common;

import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import net.minecraft.world.entity.LivingEntity;

public final class JianDaoBoostHelper {

    public static final String KEY_JIAN_XIN_MULTIPLIER =
        "GuzhenrenExtJianDaoJianXinMultiplier";

    public static final String KEY_SUI_REN_UNTIL_TICK =
        "GuzhenrenExtJianDaoSuiRenUntilTick";
    public static final String KEY_SUI_REN_MULTIPLIER =
        "GuzhenrenExtJianDaoSuiRenMultiplier";

    private static final double DEFAULT_MULTIPLIER = 1.0;
    private static final double MIN_MULTIPLIER = 1.0;
    private static final double MAX_MULTIPLIER = 10.0;

    private JianDaoBoostHelper() {}

    public static double getJianXinMultiplier(final LivingEntity user) {
        if (user == null) {
            return DEFAULT_MULTIPLIER;
        }
        final double raw = user.getPersistentData().getDouble(
            KEY_JIAN_XIN_MULTIPLIER
        );
        if (raw <= 0.0) {
            return DEFAULT_MULTIPLIER;
        }
        return UsageMetadataHelper.clamp(raw, MIN_MULTIPLIER, MAX_MULTIPLIER);
    }

    public static void setJianXinMultiplier(
        final LivingEntity user,
        final double multiplier
    ) {
        if (user == null) {
            return;
        }
        final double safe = UsageMetadataHelper.clamp(
            multiplier,
            MIN_MULTIPLIER,
            MAX_MULTIPLIER
        );
        user.getPersistentData().putDouble(KEY_JIAN_XIN_MULTIPLIER, safe);
    }

    public static void clearJianXinMultiplier(final LivingEntity user) {
        if (user == null) {
            return;
        }
        user.getPersistentData().remove(KEY_JIAN_XIN_MULTIPLIER);
    }

    public static void activateSuiRen(
        final LivingEntity user,
        final int durationTicks,
        final double multiplier
    ) {
        if (user == null) {
            return;
        }
        final int safeDuration = Math.max(0, durationTicks);
        final int untilTick = user.tickCount + safeDuration;
        user.getPersistentData().putInt(KEY_SUI_REN_UNTIL_TICK, untilTick);
        user.getPersistentData().putDouble(
            KEY_SUI_REN_MULTIPLIER,
            UsageMetadataHelper.clamp(multiplier, MIN_MULTIPLIER, MAX_MULTIPLIER)
        );
    }

    public static double consumeSuiRenMultiplierIfActive(final LivingEntity user) {
        if (user == null) {
            return DEFAULT_MULTIPLIER;
        }
        final int untilTick = user.getPersistentData().getInt(
            KEY_SUI_REN_UNTIL_TICK
        );
        if (untilTick <= 0 || user.tickCount > untilTick) {
            clearSuiRen(user);
            return DEFAULT_MULTIPLIER;
        }
        final double raw = user.getPersistentData().getDouble(
            KEY_SUI_REN_MULTIPLIER
        );
        clearSuiRen(user);
        if (raw <= 0.0) {
            return DEFAULT_MULTIPLIER;
        }
        return UsageMetadataHelper.clamp(raw, MIN_MULTIPLIER, MAX_MULTIPLIER);
    }

    public static void clearSuiRen(final LivingEntity user) {
        if (user == null) {
            return;
        }
        user.getPersistentData().remove(KEY_SUI_REN_UNTIL_TICK);
        user.getPersistentData().remove(KEY_SUI_REN_MULTIPLIER);
    }
}

