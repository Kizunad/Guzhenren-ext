package com.Kizunad.guzhenrenext.guzhenrenBridge;

import net.guzhenren.network.GuzhenrenModVariables;
import net.minecraft.world.entity.LivingEntity;

public final class NianTouHelper {

    private NianTouHelper() {}

	public static double getAmount(LivingEntity entity) {
		if (entity == null) {
			return 0.0;
		}
		try {
			return getVariables(entity).niantou;
		} catch (Exception e) {
            return 0.0;
        }
    }

	public static void modify(LivingEntity entity, double amount) {
		if (entity == null) {
			return;
		}
		try {
			var vars = getVariables(entity);
			double original = vars.niantou;
            // 假设没有严格上限，或者上限由 niantou_rongliang 控制？暂且只做扣除
            double newValue = Math.max(0, original + amount);
            
            if (Double.compare(original, newValue) != 0) {
                vars.niantou = newValue;
                PlayerVariablesSyncHelper.markSyncDirty(vars);
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private static GuzhenrenModVariables.PlayerVariables getVariables(LivingEntity entity) {
        return entity.getData(GuzhenrenModVariables.PLAYER_VARIABLES);
    }
}
