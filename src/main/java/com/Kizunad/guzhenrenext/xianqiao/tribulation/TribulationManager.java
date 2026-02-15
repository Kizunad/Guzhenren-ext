package com.Kizunad.guzhenrenext.xianqiao.tribulation;

import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoMarkApi;
import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoType;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;

/**
 * 单个仙窍的灾劫状态机管理器。
 * <p>
 * 该类按阶段推进灾劫流程：
 * 前兆（OMEN）→ 雷劫（STRIKE）→ 灾兽入侵（INVASION）→ 结算（SETTLEMENT）。
 * 管理器为瞬态对象，由外层 TickHandler 按玩家 UUID 管理生命周期。
 * </p>
 */
public class TribulationManager {

    /** 下次灾劫基础间隔：20 天。 */
    private static final long BASE_INTERVAL_TICKS = 480000L;

    /** 前兆阶段灵气扰动间隔。 */
    private static final int OMEN_PERTURB_INTERVAL = 20;

    /** 每次前兆扰动采样点数量。 */
    private static final int OMEN_PERTURB_SAMPLES = 8;

    /** 前兆扰动最小值。 */
    private static final int OMEN_PERTURB_MIN = 20;

    /** 前兆扰动随机幅度。 */
    private static final int OMEN_PERTURB_RANGE = 41;

    /** 雷劫落雷间隔。 */
    private static final int STRIKE_INTERVAL = 40;

    /** 雷劫阶段每次落雷的评分伤害。 */
    private static final float STRIKE_DAMAGE_PER_HIT = 1.0F;

    /** 入侵阶段首次生成的灾兽数量。 */
    private static final int INVASION_SPAWN_COUNT = 6;

    /** 灾兽边缘生成点，距离仙窍边界的内缩区块偏移。 */
    private static final int INVASION_EDGE_CHUNK_OFFSET = 1;

    /** 入侵阶段每只存活灾兽每 tick 造成的抽象损坏。 */
    private static final float INVASION_DAMAGE_PER_MOB_PER_TICK = 0.01F;

    private static final double BLOCK_CENTER_OFFSET = 0.5D;

    private static final float FULL_CIRCLE_DEGREES = 360.0F;

    /** 灾兽死亡后扩散道痕的固定强度。 */
    private static final int KILL_AURA_AMOUNT = 100;

    /** 损坏比超过该阈值判定为失败。 */
    private static final float FAILURE_DAMAGE_RATIO = 0.8F;

    /** 奖励判定分数阈值。 */
    private static final float REWARD_SCORE_THRESHOLD = 0.7F;

    /** 将 damageAccumulated 归一化为 damageRatio 的分母。 */
    private static final float DAMAGE_NORMALIZATION = 100.0F;

    /** 成功防御后的边界奖励值（每方向扩展区块数）。 */
    private static final int REWARD_BOUNDARY_CHUNK_DELTA = 1;

    /** 一个区块包含的方块边长。 */
    private static final int CHUNK_SIZE_BLOCKS = 16;

    /** 失败后核心灵气惩罚值。 */
    private static final int FAILURE_AURA_PENALTY = 120;

    /** 成功后核心灵气奖励值。 */
    private static final int SUCCESS_AURA_REWARD = 160;

    /** 雷劫结算奖励道类型。 */
    private static final DaoType REWARD_DAO = DaoType.TIME;

    /** 灾兽死亡爆散道痕道类型。 */
    private static final DaoType KILL_SPLASH_DAO = DaoType.LIGHTNING;

    /** 缓存全部道类型，避免重复 values() 分配。 */
    private static final DaoType[] DAO_TYPES = DaoType.values();

    /** 持有者 UUID（与一个仙窍绑定）。 */
    private final UUID owner;

    /** 当前状态。 */
    private TribulationState state = TribulationState.IDLE;

    /** 在当前状态已经停留的 tick 数。 */
    private int ticksInState;

    /** 累积抽象损坏值。 */
    private float damageAccumulated;

    /** 已生成灾兽数量。 */
    private int enemiesSpawned;

    /** 已判定清除灾兽数量。 */
    private int enemiesKilled;

    /** 当前入侵阶段仍在追踪的灾兽 UUID 集。 */
    private final Set<UUID> invasionEntities = new HashSet<>();

    /** 灾兽最近一次已知位置。 */
    private final Map<UUID, BlockPos> invasionEntityLastPos = new HashMap<>();

    /** 该管理器是否已经完成整轮灾劫。 */
    private boolean finished;

    public TribulationManager(UUID owner) {
        this.owner = owner;
    }

    /**
     * 主 tick 逻辑。
     *
     * @param level        仙窍维度服务端世界
     * @param apertureInfo 仙窍信息快照
     */
    public void tick(ServerLevel level, ApertureInfo apertureInfo) {
        if (state == TribulationState.IDLE || finished) {
            return;
        }

        switch (state) {
            case OMEN -> tickOmen(level, apertureInfo);
            case STRIKE -> tickStrike(level, apertureInfo);
            case INVASION -> tickInvasion(level, apertureInfo);
            case SETTLEMENT -> tickSettlement(level, apertureInfo);
            default -> {
                // IDLE 在入口已提前返回，这里不处理。
            }
        }

        if (state == TribulationState.SETTLEMENT || state == TribulationState.IDLE) {
            return;
        }

        ticksInState++;
        int duration = state.durationTicks();
        if (duration > 0 && ticksInState >= duration) {
            advanceState();
        }
    }

    /**
     * 从空闲态启动灾劫。
     */
    public void startTribulation() {
        if (state != TribulationState.IDLE) {
            return;
        }
        state = TribulationState.OMEN;
        ticksInState = 0;
        damageAccumulated = 0.0F;
        enemiesSpawned = 0;
        enemiesKilled = 0;
        invasionEntities.clear();
        invasionEntityLastPos.clear();
        finished = false;
    }

    /**
     * 推进到下一状态。
     */
    public void advanceState() {
        state = switch (state) {
            case IDLE -> TribulationState.OMEN;
            case OMEN -> TribulationState.STRIKE;
            case STRIKE -> TribulationState.INVASION;
            case INVASION -> TribulationState.SETTLEMENT;
            case SETTLEMENT -> TribulationState.IDLE;
        };
        ticksInState = 0;
    }

    /**
     * 前兆阶段：定时随机扰动道痕。
     */
    private void tickOmen(ServerLevel level, ApertureInfo apertureInfo) {
        if (ticksInState % OMEN_PERTURB_INTERVAL != 0) {
            return;
        }
        RandomSource random = level.getRandom();
        for (int i = 0; i < OMEN_PERTURB_SAMPLES; i++) {
            BlockPos target = randomPosInAperture(random, apertureInfo);
            DaoType type = DAO_TYPES[random.nextInt(DAO_TYPES.length)];
            int amount = OMEN_PERTURB_MIN + random.nextInt(OMEN_PERTURB_RANGE);
            if (random.nextBoolean()) {
                DaoMarkApi.addAura(level, target, type, amount);
            } else {
                DaoMarkApi.consumeAura(level, target, type, amount);
            }
        }
    }

    /**
     * 雷劫阶段：每隔固定 tick 在仙窍范围内落雷。
     */
    private void tickStrike(ServerLevel level, ApertureInfo apertureInfo) {
        if (ticksInState % STRIKE_INTERVAL != 0) {
            return;
        }
        RandomSource random = level.getRandom();
        BlockPos target = randomPosInAperture(random, apertureInfo);
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
        if (lightning != null) {
            lightning.moveTo(target.getX() + BLOCK_CENTER_OFFSET, target.getY(), target.getZ() + BLOCK_CENTER_OFFSET);
            lightning.setVisualOnly(false);
            level.addFreshEntity(lightning);
        }
        damageAccumulated += STRIKE_DAMAGE_PER_HIT;
    }

    /**
     * 入侵阶段：首次刷怪并持续统计存活灾兽与清除进度。
     */
    private void tickInvasion(ServerLevel level, ApertureInfo apertureInfo) {
        if (ticksInState == 0) {
            spawnInvasionEnemies(level, apertureInfo);
        }

        reconcileInvasionEntities(level);
        if (!invasionEntities.isEmpty()) {
            damageAccumulated += invasionEntities.size() * INVASION_DAMAGE_PER_MOB_PER_TICK;
        }

        if (enemiesSpawned > 0 && invasionEntities.isEmpty()) {
            advanceState();
        }
    }

    /**
     * 结算阶段：按 damageRatio 计算奖惩并设置下一次灾劫。
     */
    private void tickSettlement(ServerLevel level, ApertureInfo apertureInfo) {
        float damageRatio = Mth.clamp(damageAccumulated / DAMAGE_NORMALIZATION, 0.0F, 1.0F);
        float score = 1.0F - damageRatio;

        ApertureWorldData worldData = ApertureWorldData.get(level);
        if (score >= REWARD_SCORE_THRESHOLD) {
            worldData.expandBoundaryByChunkDelta(owner, REWARD_BOUNDARY_CHUNK_DELTA);
            DaoMarkApi.addAura(level, apertureInfo.center(), REWARD_DAO, SUCCESS_AURA_REWARD);
        } else if (damageRatio > FAILURE_DAMAGE_RATIO) {
            DaoMarkApi.consumeAura(level, apertureInfo.center(), REWARD_DAO, FAILURE_AURA_PENALTY);
        }

        long nextTick = level.getGameTime() + BASE_INTERVAL_TICKS;
        worldData.updateTribulationTick(owner, nextTick);

        advanceState();
        finished = true;
    }

    /**
     * 在仙窍边缘刷入固定数量灾兽。
     */
    private void spawnInvasionEnemies(ServerLevel level, ApertureInfo apertureInfo) {
        RandomSource random = level.getRandom();
        for (int i = 0; i < INVASION_SPAWN_COUNT; i++) {
            BlockPos spawnPos = randomEdgePosInAperture(random, apertureInfo, INVASION_EDGE_CHUNK_OFFSET);

            EntityType<? extends Mob> type = random.nextBoolean() ? EntityType.ZOMBIE : EntityType.SKELETON;
            Mob mob = type.create(level);
            if (mob == null) {
                continue;
            }
            mob.moveTo(spawnPos.getX() + BLOCK_CENTER_OFFSET, spawnPos.getY(), spawnPos.getZ() + BLOCK_CENTER_OFFSET,
                random.nextFloat() * FULL_CIRCLE_DEGREES, 0.0F);
            mob.setPersistenceRequired();
            if (level.addFreshEntity(mob)) {
                UUID uuid = mob.getUUID();
                invasionEntities.add(uuid);
                invasionEntityLastPos.put(uuid, mob.blockPosition());
                enemiesSpawned++;
            }
        }
    }

    /**
     * 对已追踪灾兽做存活对账，并处理死亡后的道痕爆散。
     */
    private void reconcileInvasionEntities(ServerLevel level) {
        Iterator<UUID> iterator = invasionEntities.iterator();
        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            Entity entity = level.getEntity(uuid);
            if (entity == null) {
                enemiesKilled++;
                invasionEntityLastPos.remove(uuid);
                iterator.remove();
                continue;
            }

            if (entity.isAlive()) {
                invasionEntityLastPos.put(uuid, entity.blockPosition());
                continue;
            }

            BlockPos deathPos = invasionEntityLastPos.getOrDefault(uuid, entity.blockPosition());
            DaoMarkApi.addAura(level, deathPos, KILL_SPLASH_DAO, KILL_AURA_AMOUNT);
            enemiesKilled++;
            invasionEntityLastPos.remove(uuid);
            iterator.remove();
        }
    }

    /**
     * 在仙窍区块边界内随机生成一个位置（y 固定为核心层）。
     */
    private static BlockPos randomPosInAperture(RandomSource random, ApertureInfo info) {
        int randomChunkX = Mth.nextInt(random, info.minChunkX(), info.maxChunkX());
        int randomChunkZ = Mth.nextInt(random, info.minChunkZ(), info.maxChunkZ());
        int randomBlockX = random.nextInt(CHUNK_SIZE_BLOCKS);
        int randomBlockZ = random.nextInt(CHUNK_SIZE_BLOCKS);
        int x = randomChunkX * CHUNK_SIZE_BLOCKS + randomBlockX;
        int z = randomChunkZ * CHUNK_SIZE_BLOCKS + randomBlockZ;
        return new BlockPos(x, info.center().getY() + 1, z);
    }

    /**
     * 在仙窍边缘区块带随机生成一个位置（y 固定为核心层）。
     * <p>
     * 该方法不再使用“中心点 + 半径”的圆形/方形距离公式，
     * 而是直接在 min/max chunk 语义下抽样边缘区块。
     * </p>
     *
     * @param random 随机源
     * @param info 仙窍边界信息
     * @param edgeChunkOffset 向内收缩的区块偏移量（0 表示最外层）
     * @return 位于边缘区块带内的随机方块位置
     */
    private static BlockPos randomEdgePosInAperture(RandomSource random, ApertureInfo info, int edgeChunkOffset) {
        int normalizedOffset = Math.max(0, edgeChunkOffset);
        int minChunkX = info.minChunkX() + normalizedOffset;
        int maxChunkX = info.maxChunkX() - normalizedOffset;
        int minChunkZ = info.minChunkZ() + normalizedOffset;
        int maxChunkZ = info.maxChunkZ() - normalizedOffset;

        if (minChunkX > maxChunkX || minChunkZ > maxChunkZ) {
            return randomPosInAperture(random, info);
        }

        int width = maxChunkX - minChunkX + 1;
        int depth = maxChunkZ - minChunkZ + 1;
        int perimeter = width * 2 + Math.max(0, depth - 2) * 2;
        if (perimeter <= 0) {
            return randomPosInAperture(random, info);
        }

        int index = random.nextInt(perimeter);
        int chunkX;
        int chunkZ;
        if (index < width) {
            chunkX = minChunkX + index;
            chunkZ = minChunkZ;
        } else if (index < width + Math.max(0, depth - 2)) {
            int offset = index - width;
            chunkX = maxChunkX;
            chunkZ = minChunkZ + 1 + offset;
        } else if (index < width * 2 + Math.max(0, depth - 2)) {
            int offset = index - (width + Math.max(0, depth - 2));
            chunkX = maxChunkX - offset;
            chunkZ = maxChunkZ;
        } else {
            int offset = index - (width * 2 + Math.max(0, depth - 2));
            chunkX = minChunkX;
            chunkZ = maxChunkZ - 1 - offset;
        }

        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
        int x = chunkPos.getMinBlockX() + random.nextInt(CHUNK_SIZE_BLOCKS);
        int z = chunkPos.getMinBlockZ() + random.nextInt(CHUNK_SIZE_BLOCKS);
        return new BlockPos(x, info.center().getY() + 1, z);
    }

    /**
     * @return 当前状态
     */
    public TribulationState getState() {
        return state;
    }

    /**
     * @return 当前状态内已停留 tick 数
     */
    public int getTicksInState() {
        return ticksInState;
    }

    /**
     * @return 累积损坏值
     */
    public float getDamageAccumulated() {
        return damageAccumulated;
    }

    /**
     * @return 已生成灾兽数
     */
    public int getEnemiesSpawned() {
        return enemiesSpawned;
    }

    /**
     * @return 已清除灾兽数
     */
    public int getEnemiesKilled() {
        return enemiesKilled;
    }

    /**
     * @return 当前管理器是否完成整轮灾劫
     */
    public boolean isFinished() {
        return finished;
    }
}
