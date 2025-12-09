package com.Kizunad.guzhenrenext.customNPCImpl.lifecycle;

import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import net.guzhenren.network.GuzhenrenModVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NPC 资源秒级回复逻辑。
 * <p>
 * 将此处理器注册到 {@link NpcSecondTicker}，每秒为 NPC 回复真元、魂魄、念头和精力。
 * 回复量基于最大值的一定百分比。
 * </p>
 */
public final class NpcResourceRegeneration {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        NpcResourceRegeneration.class
    );
    private static boolean registered = false;

    // 回复百分比（每秒）
    private static final double REGEN_PERCENTAGE = 0.01; // 1% 每秒

    private NpcResourceRegeneration() {}

    /**
     * 注册资源回复处理器。
     * 应在模组初始化阶段调用一次。
     */
    public static void register() {
        if (registered) {
            return;
        }
        NpcSecondTicker.addHandler(
            NpcResourceRegeneration::performRegeneration
        );
        registered = true;
        LOGGER.debug("NpcResourceRegeneration registered to NpcSecondTicker.");
    }

    private static void performRegeneration(CustomNpcEntity npc) {
        try {
            // 获取蛊真人模组的玩家变量
            GuzhenrenModVariables.PlayerVariables variables = npc.getData(
                GuzhenrenModVariables.PLAYER_VARIABLES
            );

            // 真元 (zhenyuan)
            double currentZhenYuan = variables.zhenyuan;
            double maxZhenYuan = variables.zuida_zhenyuan;
            if (currentZhenYuan < maxZhenYuan) {
                variables.zhenyuan = Math.min(
                    maxZhenYuan,
                    currentZhenYuan + maxZhenYuan * REGEN_PERCENTAGE
                );
            }

            // 魂魄 (hunpo)
            double currentHunPo = variables.hunpo;
            double maxHunPo = variables.zuida_hunpo;
            if (currentHunPo < maxHunPo) {
                variables.hunpo = Math.min(
                    maxHunPo,
                    currentHunPo + maxHunPo * REGEN_PERCENTAGE
                );
            }

            // 精力 (jingli)
            double currentJingLi = variables.jingli;
            double maxJingLi = variables.zuida_jingli;
            if (currentJingLi < maxJingLi) {
                variables.jingli = Math.min(
                    maxJingLi,
                    currentJingLi + maxJingLi * REGEN_PERCENTAGE
                );
            }

            // 念头 (niantou)
            double currentNianTou = variables.niantou;
            double maxNianTou = variables.niantou_rongliang; // "niantou_rongliang" is max for niantou
            if (currentNianTou < maxNianTou) {
                variables.niantou = Math.min(
                    maxNianTou,
                    currentNianTou + maxNianTou * REGEN_PERCENTAGE
                );
            }

            // 标记变量已修改，确保同步
            variables.markSyncDirty();
        } catch (Exception e) {
            LOGGER.warn(
                "Failed to regenerate Guzhenren resources for NPC {}: {}",
                npc.getName().getString(),
                e.getMessage()
            );
        }
    }
}
