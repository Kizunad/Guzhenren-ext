package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeConfig;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基地污染服务 - 处理污染相关的统一逻辑。
 * <p>
 * 当前仅在“节点被破坏”场景中增加污染值，未来可扩展到其他污染来源。
 * </p>
 */
public final class BastionPollutionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BastionPollutionService.class);

    private BastionPollutionService() {
        // 工具类
    }

    /**
     * 节点被破坏时调用，按配置增加基地污染值。
     * <p>
     * 逻辑流程：
     * <ol>
     *     <li>读取基地类型污染配置，若未启用则直接返回。</li>
     *     <li>按照配置的 nodeDestructionPollutionGain 叠加污染值。</li>
     *     <li>通过 {@link BastionSavedData#updateBastion(BastionData)} 持久化更新。</li>
     * </ol>
     * </p>
     *
     * @param level   服务端世界
     * @param bastion 目标基地
     * @param pos     被破坏的节点位置（用于日志定位）
     */
    public static void onNodeDestroyed(ServerLevel level, BastionData bastion, BlockPos pos) {
        if (level == null || bastion == null || pos == null) {
            return;
        }

        BastionTypeConfig typeConfig = BastionTypeManager.getOrDefault(bastion.bastionType());
        BastionTypeConfig.PollutionConfig pollution = typeConfig.pollution();

        if (pollution == null || !pollution.enabled()) {
            LOGGER.debug("污染系统未启用，跳过节点破坏污染处理，基地 {}", bastion.id());
            return;
        }

        double gain = Math.max(0.0, pollution.nodeDestructionPollutionGain());
        if (gain <= 0.0) {
            LOGGER.debug("节点破坏污染增量 <= 0，跳过处理，基地 {}", bastion.id());
            return;
        }

        double current = bastion.pollution();
        BastionData updated = bastion.withPollution(current + gain);

        BastionSavedData savedData = BastionSavedData.get(level);
        savedData.updateBastion(updated);

        LOGGER.info("节点被破坏：基地 {} 污染 {} -> {}，增量 {}，位置 {}",
            bastion.id(),
            current,
            updated.pollution(),
            gain,
            pos.toShortString());
    }
}
