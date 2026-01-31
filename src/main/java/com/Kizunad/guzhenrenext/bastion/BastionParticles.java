package com.Kizunad.guzhenrenext.bastion;

import com.Kizunad.guzhenrenext.bastion.block.BastionAnchorBlock;
import com.Kizunad.guzhenrenext.bastion.block.BastionCoreBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

/**
 * 基地粒子效果工具类。
 * <p>
 * 提供基地核心、节点及状态变化的粒子效果。
 * </p>
 */
public final class BastionParticles {

    private BastionParticles() {
        // 工具类
    }

    // ===== 粒子配置常量 =====

    /** 核心环绕粒子数量。 */
    private static final int CORE_AMBIENT_COUNT = 3;

    /** 节点环境粒子数量。 */
    private static final int NODE_AMBIENT_COUNT = 1;

    /** 粒子生成偏移范围。 */
    private static final double PARTICLE_OFFSET = 0.5;

    /** 粒子速度范围。 */
    private static final double PARTICLE_SPEED = 0.02;

    /** 封印粒子颜色（灰色）。 */
    private static final Vector3f SEALED_COLOR = new Vector3f(0.5f, 0.5f, 0.5f);

    /** 粒子大小。 */
    private static final float PARTICLE_SIZE = 1.0f;

    /** 核心环境粒子扩散范围系数。 */
    private static final double CORE_AMBIENT_SPREAD_FACTOR = 2.0;

    /** 节点环境粒子大小系数。 */
    private static final float NODE_AMBIENT_SIZE_FACTOR = 0.7f;

    /** 节点环境粒子垂直扩散系数。 */
    private static final double NODE_AMBIENT_Y_SPREAD = 0.5;

    /** 节点环境粒子上升速度。 */
    private static final double NODE_AMBIENT_Y_SPEED = 0.01;

    /** 颜色分量提取常量。 */
    private static final int COLOR_SHIFT_RED = 16;
    private static final int COLOR_SHIFT_GREEN = 8;
    private static final int COLOR_MASK = 0xFF;
    private static final float COLOR_NORMALIZE = 255.0f;

    // ===== 销毁粒子常量 =====

    /** 销毁爆炸粒子数量。 */
    private static final int DESTROY_EXPLOSION_COUNT = 5;
    /** 销毁烟雾粒子数量。 */
    private static final int DESTROY_SMOKE_COUNT = 30;
    /** 销毁烟雾扩散速度。 */
    private static final double DESTROY_SMOKE_SPEED = 0.05;
    /** 销毁碎片粒子数量。 */
    private static final int DESTROY_CLOUD_COUNT = 20;
    /** 销毁碎片扩散速度。 */
    private static final double DESTROY_CLOUD_SPEED = 0.1;

    // ===== 节点扩张粒子常量 =====

    /** 节点扩张粒子数量。 */
    private static final int NODE_EXPAND_DUST_COUNT = 10;
    /** 节点扩张粒子大小系数。 */
    private static final float NODE_EXPAND_SIZE_FACTOR = 0.8f;
    /** 节点扩张 Sculk 粒子数量。 */
    private static final int NODE_EXPAND_SCULK_COUNT = 5;
    /** 节点扩张小扩散范围。 */
    private static final double NODE_EXPAND_SMALL_SPREAD = 0.3;
    /** 节点扩张小扩散速度。 */
    private static final double NODE_EXPAND_SMALL_SPEED = 0.01;

    // ===== 节点衰减粒子常量 =====

    /** 节点衰减灰烬粒子数量。 */
    private static final int NODE_DECAY_ASH_COUNT = 15;
    /** 节点衰减烟雾粒子数量。 */
    private static final int NODE_DECAY_SMOKE_COUNT = 5;

    // ===== 封印粒子常量 =====

    /** 封印尘埃粒子大小系数。 */
    private static final float SEAL_SIZE_FACTOR = 1.5f;
    /** 封印尘埃粒子数量。 */
    private static final int SEAL_DUST_COUNT = 20;
    /** 封印尘埃扩散范围（水平）。 */
    private static final double SEAL_DUST_SPREAD_XZ = 1.0;
    /** 封印尘埃扩散范围（垂直）。 */
    private static final double SEAL_DUST_SPREAD_Y = 0.5;
    /** 封印尘埃速度。 */
    private static final double SEAL_DUST_SPEED = 0.05;
    /** 封印附魔粒子数量。 */
    private static final int SEAL_ENCHANT_COUNT = 30;
    /** 封印附魔粒子扩散。 */
    private static final double SEAL_ENCHANT_SPREAD = 0.5;
    /** 封印附魔粒子速度。 */
    private static final double SEAL_ENCHANT_SPEED = 0.1;
    /** 封印粒子高度偏移1。 */
    private static final double SEAL_HEIGHT_OFFSET_1 = 1.0;
    /** 封印粒子高度偏移1.5。 */
    private static final double SEAL_HEIGHT_OFFSET_1_5 = 1.5;

    // ===== 占领粒子常量 =====

    /** 占领尘埃粒子大小系数。 */
    private static final float CAPTURE_SIZE_FACTOR = 2.0f;
    /** 占领尘埃粒子数量。 */
    private static final int CAPTURE_DUST_COUNT = 50;
    /** 占领尘埃扩散范围（水平）。 */
    private static final double CAPTURE_DUST_SPREAD_XZ = 2.0;
    /** 占领尘埃速度。 */
    private static final double CAPTURE_DUST_SPEED = 0.1;
    /** 占领图腾粒子数量。 */
    private static final int CAPTURE_TOTEM_COUNT = 30;
    /** 占领图腾粒子扩散。 */
    private static final double CAPTURE_TOTEM_SPREAD_XZ = 1.0;
    /** 占领图腾粒子扩散（垂直）。 */
    private static final double CAPTURE_TOTEM_SPREAD_Y = 0.5;
    /** 占领图腾粒子速度。 */
    private static final double CAPTURE_TOTEM_SPEED = 0.3;

    // ===== 升级粒子常量 =====

    /** 升级尘埃粒子大小系数。 */
    private static final float EVOLVE_SIZE_FACTOR = 1.8f;
    /** 升级尘埃粒子数量。 */
    private static final int EVOLVE_DUST_COUNT = 40;
    /** 升级尘埃扩散范围（水平）。 */
    private static final double EVOLVE_DUST_SPREAD_XZ = 0.5;
    /** 升级尘埃扩散范围（垂直）。 */
    private static final double EVOLVE_DUST_SPREAD_Y = 2.0;
    /** 升级尘埃粒子速度。 */
    private static final double EVOLVE_DUST_SPEED = 0.05;
    /** 升级高兴村民粒子数量。 */
    private static final int EVOLVE_HAPPY_COUNT = 20;
    /** 升级粒子高度偏移。 */
    private static final double EVOLVE_HEIGHT_OFFSET = 2.0;
    /** 升级高兴粒子速度。 */
    private static final double EVOLVE_HAPPY_SPEED = 0.1;

    // ===== 祭献粒子常量 =====

    /** 祭献尘埃粒子数量。 */
    private static final int SACRIFICE_DUST_COUNT = 15;
    /** 祭献尘埃粒子大小系数。 */
    private static final float SACRIFICE_SIZE_FACTOR = 1.2f;
    /** 祭献灵魂粒子数量。 */
    private static final int SACRIFICE_SOUL_COUNT = 10;
    /** 祭献轨迹分段数。 */
    private static final int SACRIFICE_TRAIL_SEGMENTS = 8;
    /** 祭献轨迹粒子数。 */
    private static final int SACRIFICE_TRAIL_PARTICLES = 3;
    /** 真元粒子颜色 - 红色分量。 */
    private static final float ZHENYUAN_COLOR_R = 1.0f;
    /** 真元粒子颜色 - 绿色分量（金色）。 */
    private static final float ZHENYUAN_COLOR_G = 0.85f;
    /** 真元粒子颜色 - 蓝色分量（金色）。 */
    private static final float ZHENYUAN_COLOR_B = 0.3f;

    // ===== 环境粒子（随机刻） =====

    /**
     * 生成核心方块的环境粒子。
     * <p>
     * 在方块随机刻时调用，产生道途颜色的尘埃粒子环绕效果。
     * </p>
     *
     * @param state  方块状态
     * @param level  世界
     * @param pos    方块位置
     * @param random 随机源
     */
    public static void spawnCoreAmbientParticles(
            BlockState state,
            Level level,
            BlockPos pos,
            RandomSource random) {
        if (!(state.getBlock() instanceof BastionCoreBlock)) {
            return;
        }

        BastionDao dao = state.getValue(BastionCoreBlock.DAO);
        Vector3f color = getDaoColor(dao);
        DustParticleOptions dust = new DustParticleOptions(color, PARTICLE_SIZE);

        for (int i = 0; i < CORE_AMBIENT_COUNT; i++) {
            double x = pos.getX() + PARTICLE_OFFSET
                + (random.nextDouble() - PARTICLE_OFFSET) * CORE_AMBIENT_SPREAD_FACTOR;
            double y = pos.getY() + PARTICLE_OFFSET + random.nextDouble();
            double z = pos.getZ() + PARTICLE_OFFSET
                + (random.nextDouble() - PARTICLE_OFFSET) * CORE_AMBIENT_SPREAD_FACTOR;

            level.addParticle(dust, x, y, z,
                (random.nextDouble() - PARTICLE_OFFSET) * PARTICLE_SPEED,
                random.nextDouble() * PARTICLE_SPEED,
                (random.nextDouble() - PARTICLE_OFFSET) * PARTICLE_SPEED);
        }
    }

    /**
     * 生成节点方块的环境粒子。
     *
     * @param state  方块状态
     * @param level  世界
     * @param pos    方块位置
     * @param random 随机源
     */
    public static void spawnNodeAmbientParticles(
            BlockState state,
            Level level,
            BlockPos pos,
            RandomSource random) {
        if (!(state.getBlock() instanceof BastionAnchorBlock)) {
            return;
        }

        BastionDao dao = state.getValue(BastionAnchorBlock.DAO);
        Vector3f color = getDaoColor(dao);
        DustParticleOptions dust = new DustParticleOptions(color, PARTICLE_SIZE * NODE_AMBIENT_SIZE_FACTOR);

        for (int i = 0; i < NODE_AMBIENT_COUNT; i++) {
            double x = pos.getX() + PARTICLE_OFFSET + (random.nextDouble() - PARTICLE_OFFSET);
            double y = pos.getY() + PARTICLE_OFFSET + random.nextDouble() * NODE_AMBIENT_Y_SPREAD;
            double z = pos.getZ() + PARTICLE_OFFSET + (random.nextDouble() - PARTICLE_OFFSET);

            level.addParticle(dust, x, y, z, 0, NODE_AMBIENT_Y_SPEED, 0);
        }
    }

    /**
     * 生成 Anchor 被拆除时的粒子效果。
     * <p>
     * 目前复用节点扩张粒子，保持资源依赖最小化。
     * </p>
     */
    public static void spawnAnchorDestroyedParticles(ServerLevel level, BlockPos pos, BastionDao dao) {
        spawnNodeExpandParticles(level, pos, dao);
    }

    // ===== 事件粒子（服务端） =====

    /**
     * 生成封印成功粒子效果。
     *
     * @param level 服务端世界
     * @param pos   位置
     */
    public static void spawnSealParticles(ServerLevel level, BlockPos pos) {
        DustParticleOptions dust = new DustParticleOptions(SEALED_COLOR, PARTICLE_SIZE * SEAL_SIZE_FACTOR);

        level.sendParticles(dust,
            pos.getX() + PARTICLE_OFFSET, pos.getY() + SEAL_HEIGHT_OFFSET_1, pos.getZ() + PARTICLE_OFFSET,
            SEAL_DUST_COUNT,
            SEAL_DUST_SPREAD_XZ, SEAL_DUST_SPREAD_Y, SEAL_DUST_SPREAD_XZ,
            SEAL_DUST_SPEED);

        // 附加魔法粒子
        level.sendParticles(ParticleTypes.ENCHANT,
            pos.getX() + PARTICLE_OFFSET, pos.getY() + SEAL_HEIGHT_OFFSET_1_5, pos.getZ() + PARTICLE_OFFSET,
            SEAL_ENCHANT_COUNT,
            SEAL_ENCHANT_SPREAD, SEAL_ENCHANT_SPREAD, SEAL_ENCHANT_SPREAD,
            SEAL_ENCHANT_SPEED);
    }

    /**
     * 生成占领成功粒子效果。
     *
     * @param level 服务端世界
     * @param pos   位置
     * @param dao   道途类型
     */
    public static void spawnCaptureParticles(ServerLevel level, BlockPos pos, BastionDao dao) {
        Vector3f color = getDaoColor(dao);
        DustParticleOptions dust = new DustParticleOptions(color, PARTICLE_SIZE * CAPTURE_SIZE_FACTOR);

        // 胜利粒子爆发
        level.sendParticles(dust,
            pos.getX() + PARTICLE_OFFSET, pos.getY() + SEAL_HEIGHT_OFFSET_1, pos.getZ() + PARTICLE_OFFSET,
            CAPTURE_DUST_COUNT,
            CAPTURE_DUST_SPREAD_XZ, SEAL_HEIGHT_OFFSET_1, CAPTURE_DUST_SPREAD_XZ,
            CAPTURE_DUST_SPEED);

        // 烟花效果
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
            pos.getX() + PARTICLE_OFFSET, pos.getY() + SEAL_HEIGHT_OFFSET_1, pos.getZ() + PARTICLE_OFFSET,
            CAPTURE_TOTEM_COUNT,
            CAPTURE_TOTEM_SPREAD_XZ, CAPTURE_TOTEM_SPREAD_Y, CAPTURE_TOTEM_SPREAD_XZ,
            CAPTURE_TOTEM_SPEED);
    }

    /**
     * 生成升级粒子效果。
     *
     * @param level 服务端世界
     * @param pos   位置
     * @param dao   道途类型
     */
    public static void spawnEvolveParticles(ServerLevel level, BlockPos pos, BastionDao dao) {
        Vector3f color = getDaoColor(dao);
        DustParticleOptions dust = new DustParticleOptions(color, PARTICLE_SIZE * EVOLVE_SIZE_FACTOR);

        // 上升粒子柱
        level.sendParticles(dust,
            pos.getX() + PARTICLE_OFFSET, pos.getY() + PARTICLE_OFFSET, pos.getZ() + PARTICLE_OFFSET,
            EVOLVE_DUST_COUNT,
            EVOLVE_DUST_SPREAD_XZ, EVOLVE_DUST_SPREAD_Y, EVOLVE_DUST_SPREAD_XZ,
            EVOLVE_DUST_SPEED);

        // 经验球效果
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
            pos.getX() + PARTICLE_OFFSET, pos.getY() + EVOLVE_HEIGHT_OFFSET, pos.getZ() + PARTICLE_OFFSET,
            EVOLVE_HAPPY_COUNT,
            CAPTURE_TOTEM_SPREAD_XZ, CAPTURE_TOTEM_SPREAD_Y, CAPTURE_TOTEM_SPREAD_XZ,
            EVOLVE_HAPPY_SPEED);
    }

    /**
     * 生成威胁事件通用粒子效果。
     * <p>
     * 当前版本复用节点扩张粒子作为“能量波动”提示。
     * </p>
     *
     * @param level 服务端世界
     * @param pos   位置
     * @param dao   道途类型
     */
    public static void spawnThreatParticles(ServerLevel level, BlockPos pos, BastionDao dao) {
        // 复用已有粒子，避免增加新的资源依赖
        spawnNodeExpandParticles(level, pos, dao);
    }

    /**
     * 生成扩张涌动粒子效果。
     * <p>
     * 当前版本复用升级粒子，体现“基地能量突然上涌”。
     * </p>
     *
     * @param level 服务端世界
     * @param pos   位置
     * @param dao   道途类型
     */
    public static void spawnExpansionSurgeParticles(ServerLevel level, BlockPos pos, BastionDao dao) {
        spawnEvolveParticles(level, pos, dao);
    }

    /**
     * 生成销毁粒子效果。
     *
     * @param level 服务端世界
     * @param pos   位置
     */
    public static void spawnDestroyParticles(ServerLevel level, BlockPos pos) {
        // 爆炸粒子
        level.sendParticles(ParticleTypes.EXPLOSION,
            pos.getX() + PARTICLE_OFFSET, pos.getY() + PARTICLE_OFFSET, pos.getZ() + PARTICLE_OFFSET,
            DESTROY_EXPLOSION_COUNT,
            PARTICLE_OFFSET, PARTICLE_OFFSET, PARTICLE_OFFSET,
            0.0);

        // 烟雾
        level.sendParticles(ParticleTypes.LARGE_SMOKE,
            pos.getX() + PARTICLE_OFFSET, pos.getY() + PARTICLE_OFFSET, pos.getZ() + PARTICLE_OFFSET,
            DESTROY_SMOKE_COUNT,
            1.0, 1.0, 1.0,
            DESTROY_SMOKE_SPEED);

        // 碎片
        level.sendParticles(ParticleTypes.CLOUD,
            pos.getX() + PARTICLE_OFFSET, pos.getY() + PARTICLE_OFFSET, pos.getZ() + PARTICLE_OFFSET,
            DESTROY_CLOUD_COUNT,
            1.0, PARTICLE_OFFSET, 1.0,
            DESTROY_CLOUD_SPEED);
    }

    /**
     * 生成节点扩张粒子效果。
     *
     * @param level 服务端世界
     * @param pos   位置
     * @param dao   道途类型
     */
    public static void spawnNodeExpandParticles(ServerLevel level, BlockPos pos, BastionDao dao) {
        Vector3f color = getDaoColor(dao);
        DustParticleOptions dust = new DustParticleOptions(color, PARTICLE_SIZE * NODE_EXPAND_SIZE_FACTOR);

        level.sendParticles(dust,
            pos.getX() + PARTICLE_OFFSET, pos.getY() + PARTICLE_OFFSET, pos.getZ() + PARTICLE_OFFSET,
            NODE_EXPAND_DUST_COUNT,
            PARTICLE_OFFSET, PARTICLE_OFFSET, PARTICLE_OFFSET,
            PARTICLE_SPEED);

        // Sculk 生长效果
        level.sendParticles(ParticleTypes.SCULK_CHARGE_POP,
            pos.getX() + PARTICLE_OFFSET, pos.getY() + PARTICLE_OFFSET, pos.getZ() + PARTICLE_OFFSET,
            NODE_EXPAND_SCULK_COUNT,
            NODE_EXPAND_SMALL_SPREAD, NODE_EXPAND_SMALL_SPREAD, NODE_EXPAND_SMALL_SPREAD,
            NODE_EXPAND_SMALL_SPEED);
    }

    /**
     * 生成节点衰减粒子效果。
     *
     * @param level 服务端世界
     * @param pos   位置
     */
    public static void spawnNodeDecayParticles(ServerLevel level, BlockPos pos) {
        level.sendParticles(ParticleTypes.ASH,
            pos.getX() + PARTICLE_OFFSET, pos.getY() + PARTICLE_OFFSET, pos.getZ() + PARTICLE_OFFSET,
            NODE_DECAY_ASH_COUNT,
            PARTICLE_OFFSET, PARTICLE_OFFSET, PARTICLE_OFFSET,
            PARTICLE_SPEED);

        level.sendParticles(ParticleTypes.SMOKE,
            pos.getX() + PARTICLE_OFFSET, pos.getY() + PARTICLE_OFFSET, pos.getZ() + PARTICLE_OFFSET,
            NODE_DECAY_SMOKE_COUNT,
            NODE_EXPAND_SMALL_SPREAD, NODE_EXPAND_SMALL_SPREAD, NODE_EXPAND_SMALL_SPREAD,
            NODE_EXPAND_SMALL_SPEED);
    }

    /**
     * 生成资源祭献粒子效果。
     * <p>
     * 从玩家位置向基地核心生成流动的能量轨迹。
     * </p>
     *
     * @param level     服务端世界
     * @param corePos   基地核心位置
     * @param playerPos 玩家位置
     */
    public static void spawnSacrificeParticles(ServerLevel level, BlockPos corePos, BlockPos playerPos) {
        // 金色真元粒子颜色
        Vector3f zhenyuanColor = new Vector3f(ZHENYUAN_COLOR_R, ZHENYUAN_COLOR_G, ZHENYUAN_COLOR_B);
        DustParticleOptions dust = new DustParticleOptions(zhenyuanColor, PARTICLE_SIZE * SACRIFICE_SIZE_FACTOR);

        // 在核心位置生成聚集效果
        level.sendParticles(dust,
            corePos.getX() + PARTICLE_OFFSET, corePos.getY() + SEAL_HEIGHT_OFFSET_1, corePos.getZ() + PARTICLE_OFFSET,
            SACRIFICE_DUST_COUNT,
            PARTICLE_OFFSET, PARTICLE_OFFSET, PARTICLE_OFFSET,
            PARTICLE_SPEED);

        // 灵魂粒子效果
        level.sendParticles(ParticleTypes.SOUL,
            corePos.getX() + PARTICLE_OFFSET, corePos.getY() + SEAL_HEIGHT_OFFSET_1, corePos.getZ() + PARTICLE_OFFSET,
            SACRIFICE_SOUL_COUNT,
            PARTICLE_OFFSET, PARTICLE_OFFSET, PARTICLE_OFFSET,
            SEAL_DUST_SPEED);

        // 从玩家到核心的能量轨迹
        double dx = corePos.getX() - playerPos.getX();
        double dy = corePos.getY() - playerPos.getY();
        double dz = corePos.getZ() - playerPos.getZ();

        for (int i = 0; i < SACRIFICE_TRAIL_SEGMENTS; i++) {
            double t = (double) i / SACRIFICE_TRAIL_SEGMENTS;
            double x = playerPos.getX() + PARTICLE_OFFSET + dx * t;
            double y = playerPos.getY() + SEAL_HEIGHT_OFFSET_1 + dy * t;
            double z = playerPos.getZ() + PARTICLE_OFFSET + dz * t;

            level.sendParticles(dust, x, y, z,
                SACRIFICE_TRAIL_PARTICLES,
                NODE_EXPAND_SMALL_SPREAD, NODE_EXPAND_SMALL_SPREAD, NODE_EXPAND_SMALL_SPREAD,
                NODE_EXPAND_SMALL_SPEED);
        }
    }

    // ===== 内部方法 =====

    /**
     * 将道途颜色转换为粒子颜色向量。
     */
    private static Vector3f getDaoColor(BastionDao dao) {
        int color = dao.getColor();
        float r = ((color >> COLOR_SHIFT_RED) & COLOR_MASK) / COLOR_NORMALIZE;
        float g = ((color >> COLOR_SHIFT_GREEN) & COLOR_MASK) / COLOR_NORMALIZE;
        float b = (color & COLOR_MASK) / COLOR_NORMALIZE;
        return new Vector3f(r, g, b);
    }
}
