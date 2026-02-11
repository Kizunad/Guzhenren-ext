package com.Kizunad.guzhenrenext.xianqiao.daomark;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 道痕扩散服务。
 * <p>
 * 每 {@value #DIFFUSION_INTERVAL_TICKS} tick 在仙窍维度中执行一次扩散：
 * 高浓度 Section 向 6 个相邻 Section（上下左右前后）转移灵气。
 * 仅处理玩家附近已加载的 Chunk，保证性能。
 * </p>
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class DaoMarkDiffusionService {

    /** 仙窍维度 ResourceKey。 */
    public static final ResourceKey<Level> APERTURE_DIMENSION = ResourceKey.create(
        Registries.DIMENSION,
        ResourceLocation.fromNamespaceAndPath("guzhenrenext", "aperture_world")
    );

    /** 扩散间隔（tick 数）。 */
    private static final int DIFFUSION_INTERVAL_TICKS = 20;

    /** 扩散速率：每次转移量 = (源 - 邻居) * DIFFUSION_RATE。 */
    private static final float DIFFUSION_RATE = 0.05f;

    /** 扩散最小阈值：浓度低于此值时不向外扩散（控制最大传播半径约 3 Sections）。 */
    private static final int DIFFUSION_THRESHOLD = 10;

    /** 玩家附近需要处理扩散的 Chunk 半径（以 Chunk 为单位）。 */
    private static final int CHUNK_SCAN_RADIUS = 4;

    /** 相邻 Section 的偏移：上下左右前后各 1 Section。 */
    private static final int[][] NEIGHBOR_OFFSETS = {
        {0, 1, 0}, {0, -1, 0},   // 上下（同 chunk 内不同 section）
        {1, 0, 0}, {-1, 0, 0},   // 东西（相邻 chunk）
        {0, 0, 1}, {0, 0, -1}    // 南北（相邻 chunk）
    };

    private DaoMarkDiffusionService() {
    }

    /**
     * 每 tick 检查是否需要执行扩散。
     *
     * @param event 服务端 tick 事件
     */
    @SubscribeEvent
    public static void onServerTick(final ServerTickEvent.Post event) {
        ServerLevel level = event.getServer().getLevel(APERTURE_DIMENSION);
        if (level == null) {
            return;
        }
        if (level.getGameTime() % DIFFUSION_INTERVAL_TICKS != 0) {
            return;
        }
        diffuseAllChunks(level);
    }

    /**
     * 收集仙窍维度中玩家附近的已加载 Chunk 并执行扩散。
     */
    private static void diffuseAllChunks(ServerLevel level) {
        Set<ChunkPos> processed = new HashSet<>();
        for (ServerPlayer player : level.players()) {
            int playerChunkX = player.chunkPosition().x;
            int playerChunkZ = player.chunkPosition().z;
            for (int dx = -CHUNK_SCAN_RADIUS; dx <= CHUNK_SCAN_RADIUS; dx++) {
                for (int dz = -CHUNK_SCAN_RADIUS; dz <= CHUNK_SCAN_RADIUS; dz++) {
                    int cx = playerChunkX + dx;
                    int cz = playerChunkZ + dz;
                    ChunkPos cPos = new ChunkPos(cx, cz);
                    if (!processed.add(cPos)) {
                        continue;
                    }
                    if (!level.hasChunk(cx, cz)) {
                        continue;
                    }
                    LevelChunk chunk = level.getChunk(cx, cz);
                    if (!chunk.hasData(XianqiaoAttachments.DAO_MARK.get())) {
                        continue;
                    }
                    DaoChunkAttachment attachment = chunk.getData(XianqiaoAttachments.DAO_MARK.get());
                    diffuseChunkSections(level, cPos, attachment);
                }
            }
        }
    }

    /**
     * 对单个 Chunk 内有数据的每个 Section 向 6 个方向扩散。
     */
    private static void diffuseChunkSections(
        ServerLevel level, ChunkPos chunkPos, DaoChunkAttachment attachment
    ) {
        for (Map.Entry<Integer, DaoSectionData> entry : attachment.getSections().entrySet()) {
            int sectionY = entry.getKey();
            DaoSectionData source = entry.getValue();
            diffuseSection(level, chunkPos, sectionY, source);
        }
    }

    /**
     * 将一个 Section 的灵气向 6 个相邻 Section 扩散。
     */
    private static void diffuseSection(
        ServerLevel level, ChunkPos chunkPos, int sectionY, DaoSectionData source
    ) {
        for (int[] offset : NEIGHBOR_OFFSETS) {
            diffuseToNeighbor(level, chunkPos, sectionY, source, offset);
        }
    }

    /**
     * 将源 Section 的灵气向单个邻居方向扩散。
     */
    private static void diffuseToNeighbor(
        ServerLevel level, ChunkPos chunkPos, int sectionY,
        DaoSectionData source, int[] offset
    ) {
        int targetChunkX = chunkPos.x + offset[0];
        int targetSectionY = sectionY + offset[1];
        int targetChunkZ = chunkPos.z + offset[2];

        if (!level.hasChunk(targetChunkX, targetChunkZ)) {
            return;
        }
        LevelChunk targetChunk = level.getChunk(targetChunkX, targetChunkZ);
        DaoChunkAttachment targetAttachment = targetChunk.getData(XianqiaoAttachments.DAO_MARK.get());
        DaoSectionData neighbor = targetAttachment.getOrCreate(targetSectionY);

        for (DaoType type : DaoType.values()) {
            int sourceVal = source.getAura(type);
            int neighborVal = neighbor.getAura(type);
            int diff = sourceVal - neighborVal;
            if (diff <= 0 || sourceVal < DIFFUSION_THRESHOLD) {
                continue;
            }
            int transfer = (int) (diff * DIFFUSION_RATE);
            if (transfer <= 0) {
                continue;
            }
            source.setAura(type, sourceVal - transfer);
            neighbor.addAura(type, transfer);
        }
    }
}
