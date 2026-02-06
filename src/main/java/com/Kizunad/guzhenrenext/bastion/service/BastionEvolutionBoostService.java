package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * 基地进化加速服务：消耗资源池加速进化进度。
 */
public final class BastionEvolutionBoostService {

    /** 每次加速消耗的资源量。 */
    private static final int BOOST_COST = 500;
    /** 每次加速增加的进化进度。 */
    private static final double BOOST_PROGRESS = 0.05D;
    /** 最大进化进度。 */
    private static final double MAX_PROGRESS = 1.0D;
    /** 百分比换算基数。 */
    private static final int PERCENT_BASE = 100;

    private BastionEvolutionBoostService() {
    }

    /**
     * 尝试加速基地进化。
     */
    public static boolean tryBoostEvolution(
        ServerLevel level,
        BastionData bastion,
        ServerPlayer player
    ) {
        if (bastion.resourcePool() < BOOST_COST) {
            player.sendSystemMessage(Component.translatable(
                "message.guzhenrenext.evolution_boost.insufficient",
                BOOST_COST
            ));
            return false;
        }

        if (bastion.evolutionProgress() >= MAX_PROGRESS) {
            player.sendSystemMessage(Component.translatable("message.guzhenrenext.evolution_boost.max_progress"));
            return false;
        }

        BastionSavedData savedData = BastionSavedData.get(level);
        BastionData updated = bastion
            .withResourcePool(bastion.resourcePool() - BOOST_COST)
            .withEvolution(Math.min(bastion.evolutionProgress() + BOOST_PROGRESS, MAX_PROGRESS), bastion.tier());
        savedData.updateBastion(updated);

        player.sendSystemMessage(Component.translatable(
            "message.guzhenrenext.evolution_boost.success",
            (int) (updated.evolutionProgress() * PERCENT_BASE)
        ));
        return true;
    }
}
