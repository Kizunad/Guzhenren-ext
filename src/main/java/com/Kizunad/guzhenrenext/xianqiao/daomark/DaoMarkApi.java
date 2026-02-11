package com.Kizunad.guzhenrenext.xianqiao.daomark;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * 道痕/灵气场公共 API（纯静态工具类）。
 * <p>
 * 提供基于 {@link BlockPos} 的灵气读取、增加、消耗操作，
 * 内部自动定位到对应 Chunk 和 Section。
 * </p>
 */
public final class DaoMarkApi {

    private DaoMarkApi() {
    }

    /**
     * 获取指定位置、指定道类型的灵气浓度。
     *
     * @param level 世界
     * @param pos   方块坐标
     * @param type  道类型
     * @return 灵气浓度；如果所在 Chunk 未加载或无数据则返回 0
     */
    public static int getAura(Level level, BlockPos pos, DaoType type) {
        LevelChunk chunk = getLoadedChunk(level, pos);
        if (chunk == null) {
            return 0;
        }
        if (!chunk.hasData(XianqiaoAttachments.DAO_MARK.get())) {
            return 0;
        }
        DaoChunkAttachment attachment = chunk.getData(XianqiaoAttachments.DAO_MARK.get());
        int sectionY = SectionPos.blockToSectionCoord(pos.getY());
        DaoSectionData section = attachment.get(sectionY);
        if (section == null) {
            return 0;
        }
        return section.getAura(type);
    }

    /**
     * 在指定位置增加灵气浓度。
     *
     * @param level  世界
     * @param pos    方块坐标
     * @param type   道类型
     * @param amount 增加量
     */
    public static void addAura(Level level, BlockPos pos, DaoType type, int amount) {
        LevelChunk chunk = getLoadedChunk(level, pos);
        if (chunk == null) {
            return;
        }
        DaoChunkAttachment attachment = chunk.getData(XianqiaoAttachments.DAO_MARK.get());
        int sectionY = SectionPos.blockToSectionCoord(pos.getY());
        DaoSectionData section = attachment.getOrCreate(sectionY);
        section.addAura(type, amount);
    }

    /**
     * 消耗指定位置的灵气。
     *
     * @param level  世界
     * @param pos    方块坐标
     * @param type   道类型
     * @param amount 消耗量
     * @return 如果灵气充足则扣除并返回 true，否则返回 false
     */
    public static boolean consumeAura(Level level, BlockPos pos, DaoType type, int amount) {
        LevelChunk chunk = getLoadedChunk(level, pos);
        if (chunk == null) {
            return false;
        }
        if (!chunk.hasData(XianqiaoAttachments.DAO_MARK.get())) {
            return false;
        }
        DaoChunkAttachment attachment = chunk.getData(XianqiaoAttachments.DAO_MARK.get());
        int sectionY = SectionPos.blockToSectionCoord(pos.getY());
        DaoSectionData section = attachment.get(sectionY);
        if (section == null) {
            return false;
        }
        return section.consumeAura(type, amount);
    }

    /**
     * 获取 pos 所在的已加载 LevelChunk；未加载则返回 null。
     */
    private static LevelChunk getLoadedChunk(Level level, BlockPos pos) {
        int chunkX = SectionPos.blockToSectionCoord(pos.getX());
        int chunkZ = SectionPos.blockToSectionCoord(pos.getZ());
        if (!level.hasChunk(chunkX, chunkZ)) {
            return null;
        }
        return level.getChunkSource().getChunkNow(chunkX, chunkZ);
    }
}
