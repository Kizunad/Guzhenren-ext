package com.Kizunad.guzhenrenext.bastion.territory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.ChunkPos;

/**
 * 仙窍领地数据模型。
 * <p>
 * 存储维度内所有区块的领地归属与道痕分布。
 * 替代原本基于 Set&lt;BlockPos&gt; 的菌毯存储。
 * </p>
 */
public class ApertureTerritory {

    /**
     * chunk 索引 key 的编解码器。
     * <p>
     * NBT 的 CompoundTag 规定 key 只能是 String，不能直接使用 Long 作为 map key。
     * 因此这里采用“十进制字符串 &lt;-&gt; Long”的映射方案，保持和 ChunkPos.toLong() 旧索引风格一致。
     * </p>
     */
    private static final Codec<Long> CHUNK_KEY_CODEC = Codec.STRING.comapFlatMap(
        key -> {
            try {
                return DataResult.success(Long.parseLong(key));
            } catch (NumberFormatException ex) {
                return DataResult.error(() -> "Invalid chunk key (not long): " + key);
            }
        },
        Object::toString
    );

    /**
     * 区块归属索引：key=ChunkPos.toLong，value=基地 UUID。
     * 表示该 chunk 的“主权”归属于哪个基地。
     */
    private final Map<Long, UUID> chunkOwners = new HashMap<>();

    /**
     * 区块道痕数据：key=ChunkPos.toLong，value=道痕数据。
     * 记录每个 chunk 的道痕强度。
     */
    private final Map<Long, DaoMarkData> chunkDaoMarks = new HashMap<>();

    /** 编解码器（用于序列化整个领地数据）。 */
    public static final Codec<ApertureTerritory> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.unboundedMap(CHUNK_KEY_CODEC, UUIDUtil.CODEC).fieldOf("chunk_owners")
                .forGetter(t -> t.chunkOwners),
            Codec.unboundedMap(CHUNK_KEY_CODEC, DaoMarkData.CODEC).fieldOf("chunk_dao_marks")
                .forGetter(t -> t.chunkDaoMarks)
        ).apply(instance, ApertureTerritory::new)
    );

    public ApertureTerritory() {
    }

    // 私有构造用于 Codec
    private ApertureTerritory(Map<Long, UUID> owners, Map<Long, DaoMarkData> daoMarks) {
        this.chunkOwners.putAll(owners);
        this.chunkDaoMarks.putAll(daoMarks);
    }

    /**
     * 获取指定 chunk 的归属基地。
     */
    @Nullable
    public UUID getOwner(long chunkPosLong) {
        return chunkOwners.get(chunkPosLong);
    }
    
    /**
     * 获取指定 chunk 的归属基地。
     */
    @Nullable
    public UUID getOwner(ChunkPos pos) {
        return getOwner(pos.toLong());
    }

    /**
     * 设置 chunk 归属。
     */
    public void setOwner(long chunkPosLong, UUID bastionId) {
        if (bastionId == null) {
            chunkOwners.remove(chunkPosLong);
        } else {
            chunkOwners.put(chunkPosLong, bastionId);
        }
    }
    
    /**
     * 设置 chunk 归属。
     */
    public void setOwner(ChunkPos pos, UUID bastionId) {
        setOwner(pos.toLong(), bastionId);
    }

    /**
     * 获取指定 chunk 的道痕数据（从不返回 null，无数据返回 EMPTY）。
     */
    public DaoMarkData getDaoMarks(long chunkPosLong) {
        return chunkDaoMarks.getOrDefault(chunkPosLong, DaoMarkData.EMPTY);
    }
    
    /**
     * 获取指定 chunk 的道痕数据。
     */
    public DaoMarkData getDaoMarks(ChunkPos pos) {
        return getDaoMarks(pos.toLong());
    }

    /**
     * 设置道痕数据。
     */
    public void setDaoMarks(long chunkPosLong, DaoMarkData data) {
        if (data == null || data.isEmpty()) {
            chunkDaoMarks.remove(chunkPosLong);
        } else {
            chunkDaoMarks.put(chunkPosLong, data);
        }
    }
    
    /**
     * 设置道痕数据。
     */
    public void setDaoMarks(ChunkPos pos, DaoMarkData data) {
        setDaoMarks(pos.toLong(), data);
    }
    
    /**
     * 获取所有有归属的 chunks。
     */
    public Map<Long, UUID> getAllOwners() {
        return Collections.unmodifiableMap(chunkOwners);
    }

    /**
     * 获取所有有道痕的 chunks。
     */
    public Map<Long, DaoMarkData> getAllDaoMarks() {
        return Collections.unmodifiableMap(chunkDaoMarks);
    }


    /**
     * 移除指定基地的所有领土（用于基地销毁）。
     */
    public void removeTerritoryByBastionId(UUID bastionId) {
        if (bastionId == null) {
            return;
        }
        // 移除归属
        chunkOwners.entrySet().removeIf(entry -> bastionId.equals(entry.getValue()));
        // 注意：道痕可能需要保留一段时间（衰减），但如果是强制销毁，这里先不处理道痕清理逻辑，
        // 或者由上层逻辑决定是否清理道痕。
        // MVP: 简单地保留道痕数据，或者也一并清除？
        // 考虑到 "道痕消散期"，道痕数据可能应该独立于 owner 存在。
        // 但如果基地彻底没了，owner 没了，道痕自然也该慢慢没。
        // 这里仅移除 owner 索引，道痕数据留给 tick 逻辑去衰减。
    }

    public boolean isEmpty() {
        return chunkOwners.isEmpty() && chunkDaoMarks.isEmpty();
    }
}
