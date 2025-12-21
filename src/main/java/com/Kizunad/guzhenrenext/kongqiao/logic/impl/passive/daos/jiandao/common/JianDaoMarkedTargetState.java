package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common;

import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;

public final class JianDaoMarkedTargetState {

    public static final String KEY_MARKED_TARGET_UUID =
        "GuzhenrenExtJianDaoMarkedTargetUuid";
    public static final String KEY_MARKED_TARGET_UNTIL_TICK =
        "GuzhenrenExtJianDaoMarkedTargetUntilTick";

    private JianDaoMarkedTargetState() {}

    public static void setMarkedTarget(
        final LivingEntity user,
        final UUID targetUuid,
        final int untilTick
    ) {
        if (user == null || targetUuid == null) {
            return;
        }
        user.getPersistentData().putUUID(KEY_MARKED_TARGET_UUID, targetUuid);
        user.getPersistentData().putInt(KEY_MARKED_TARGET_UNTIL_TICK, untilTick);
    }

    public static UUID getMarkedTargetUuid(final LivingEntity user) {
        if (user == null) {
            return null;
        }
        if (!user.getPersistentData().hasUUID(KEY_MARKED_TARGET_UUID)) {
            return null;
        }
        final int untilTick = user.getPersistentData().getInt(
            KEY_MARKED_TARGET_UNTIL_TICK
        );
        if (untilTick <= 0 || user.tickCount > untilTick) {
            clearMarkedTarget(user);
            return null;
        }
        return user.getPersistentData().getUUID(KEY_MARKED_TARGET_UUID);
    }

    public static void clearMarkedTarget(final LivingEntity user) {
        if (user == null) {
            return;
        }
        user.getPersistentData().remove(KEY_MARKED_TARGET_UUID);
        user.getPersistentData().remove(KEY_MARKED_TARGET_UNTIL_TICK);
    }
}

