package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.PollutionStage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 基地污染视觉服务。
 * <p>
 * 根据污染阶段在基地核心附近生成不同粒子，用于向玩家传达污染程度。
 * 仅在服务端触发，通过 {@link ServerLevel#sendParticles} 向客户端同步。
 * </p>
 */
public final class BastionPollutionVisualService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BastionPollutionVisualService.class);

    /**
     * 核心附近粒子生成配置。
     */
    private static final class ParticleConfig {
        /** 基础水平偏移范围（方块，中心为 0.5,0.5）。 */
        static final double BASE_HORIZONTAL_SPREAD = 0.6D;
        /** 垂直偏移范围。 */
        static final double VERTICAL_SPREAD = 0.4D;
        /** 方块中心偏移量。 */
        static final double BLOCK_CENTER_OFFSET = 0.5D;
        /** 随机半幅，用于将随机值平移到 [-0.5, 0.5] 区间。 */
        static final double RANDOM_HALF_RANGE = 0.5D;
        /** 轻度污染粒子数量。 */
        static final int LIGHT_COUNT = 4;
        /** 中度污染粒子数量。 */
        static final int MEDIUM_COUNT = 8;
        /** 失控污染粒子数量。 */
        static final int CRITICAL_COUNT = 16;

        private ParticleConfig() {
        }
    }

    private BastionPollutionVisualService() {
    }

    /**
     * 根据污染阶段生成对应粒子效果。
     *
     * @param level   服务器世界
     * @param bastion 基地数据
     */
    public static void spawnPollutionParticles(ServerLevel level, BastionData bastion) {
        if (level == null || bastion == null) {
            LOGGER.warn("spawnPollutionParticles called with null params: level={}, bastion={}", level, bastion);
            return;
        }

        PollutionStage stage = bastion.getPollutionStage();
        if (stage == PollutionStage.NONE) {
            return; // 无污染，不生成粒子
        }

        BlockPos corePos = bastion.corePos();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        int count;
        var particleType = switch (stage) {
            case LIGHT -> {
                count = ParticleConfig.LIGHT_COUNT;
                yield ParticleTypes.SMOKE;
            }
            case MEDIUM -> {
                count = ParticleConfig.MEDIUM_COUNT;
                yield ParticleTypes.MYCELIUM;
            }
            case CRITICAL -> {
                count = ParticleConfig.CRITICAL_COUNT;
                yield ParticleTypes.WITCH;
            }
            default -> {
                count = 0;
                yield null;
            }
        };

        if (particleType == null || count <= 0) {
            return;
        }

        double centerX = corePos.getX() + ParticleConfig.BLOCK_CENTER_OFFSET;
        double centerY = corePos.getY() + ParticleConfig.BLOCK_CENTER_OFFSET;
        double centerZ = corePos.getZ() + ParticleConfig.BLOCK_CENTER_OFFSET;

        for (int i = 0; i < count; i++) {
            double offsetX = (random.nextDouble() - ParticleConfig.RANDOM_HALF_RANGE)
                * ParticleConfig.BASE_HORIZONTAL_SPREAD * 2;
            double offsetY = random.nextDouble() * ParticleConfig.VERTICAL_SPREAD;
            double offsetZ = (random.nextDouble() - ParticleConfig.RANDOM_HALF_RANGE)
                * ParticleConfig.BASE_HORIZONTAL_SPREAD * 2;

            level.sendParticles(
                particleType,
                centerX + offsetX,
                centerY + offsetY,
                centerZ + offsetZ,
                1,
                0.0D,
                0.0D,
                0.0D,
                0.0D
            );
        }
    }
}
