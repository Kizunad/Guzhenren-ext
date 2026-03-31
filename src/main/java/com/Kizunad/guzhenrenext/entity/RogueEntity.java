package com.Kizunad.guzhenrenext.entity;

import com.Kizunad.guzhenrenext.entity.ai.GoapMindCore;
import com.Kizunad.guzhenrenext.entity.mind.EntityMemory;
import com.Kizunad.guzhenrenext.entity.mind.EntityPersonality;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

/**
 * 散修 NPC 实体基类。
 * <p>
 * 该实体只负责承载 AI 心智、性格与记忆等状态，不在这里硬编码具体行为树或任务逻辑。
 * 具体行为由 GOAP 核心在运行时根据外部注册的目标、动作与感知器驱动。
 * </p>
 */
public class RogueEntity extends PathfinderMob {

    private static final String KEY_FACTION_ID = "faction_id";

    private static final String KEY_PERSONALITY = "personality";

    private static final String KEY_MEMORY = "memory";

    /**
     * GOAP 心智核心。
     */
    private final GoapMindCore mindCore;

    /**
     * 性格模块，负责情绪与驱动力状态。
     */
    private final EntityPersonality personality;

    /**
     * 记忆模块，负责短期记忆与关系标签。
     */
    private final EntityMemory memory;

    /**
     * 势力 UUID。
     * <p>
     * 为 null 时表示当前实体为无归属散修。
     * </p>
     */
    @Nullable
    private UUID factionId;

    public RogueEntity(EntityType<? extends RogueEntity> type, Level level) {
        super(type, level);
        this.mindCore = new GoapMindCore();
        this.personality = new EntityPersonality();
        this.memory = new EntityMemory();
        this.factionId = null;
    }

    @Override
    public void tick() {
        super.tick();
        mindCore.tick(level().getGameTime());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (factionId != null) {
            tag.putUUID(KEY_FACTION_ID, factionId);
        }
        tag.put(KEY_PERSONALITY, personality.save());
        tag.put(KEY_MEMORY, memory.save());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        factionId = tag.hasUUID(KEY_FACTION_ID) ? tag.getUUID(KEY_FACTION_ID) : null;

        if (tag.contains(KEY_PERSONALITY, Tag.TAG_COMPOUND)) {
            personality.load(tag.getCompound(KEY_PERSONALITY));
        }

        if (tag.contains(KEY_MEMORY, Tag.TAG_COMPOUND)) {
            memory.load(tag.getCompound(KEY_MEMORY));
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes();
    }

    public GoapMindCore getGoapMindCore() {
        return mindCore;
    }

    public EntityPersonality getPersonality() {
        return personality;
    }

    public EntityMemory getMemory() {
        return memory;
    }

    @Nullable
    public UUID getFactionId() {
        return factionId;
    }

    public void setFactionId(@Nullable UUID factionId) {
        this.factionId = factionId;
    }
}
