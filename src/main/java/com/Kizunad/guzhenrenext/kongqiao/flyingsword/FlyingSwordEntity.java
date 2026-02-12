package com.Kizunad.guzhenrenext.kongqiao.flyingsword;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ai.SwordAIDriver;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ai.SwordAIMode;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.calculator.FlyingSwordAttributes;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.calculator.SwordSpiritData;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.growth.SwordExpCalculator;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.growth.SwordGrowthData;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.quality.SwordQuality;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * 飞剑实体（模块化重构版）。
 * <p>
 * 职责：
 * <ul>
 *     <li>实体生命周期管理</li>
 *     <li>SynchedEntityData 同步（Owner/AIMode/DisplayItem）</li>
 *     <li>NBT 序列化/反序列化</li>
 * </ul>
 * </p>
 * <p>
 * AI/运动逻辑已提取到：
 * <ul>
 *     <li>{@link SwordAIDriver} - AI 驱动入口</li>
 *     <li>{@link com.Kizunad.guzhenrenext.kongqiao.flyingsword.motion.SwordMotionDriver} - 运动驱动</li>
 *     <li>{@link com.Kizunad.guzhenrenext.kongqiao.flyingsword.ai.SwordCombatOps} - 战斗逻辑</li>
 *     <li>{@link com.Kizunad.guzhenrenext.kongqiao.flyingsword.ai.SwordTargeting} - 目标获取</li>
 * </ul>
 * </p>
 */
public class FlyingSwordEntity extends PathfinderMob implements OwnableEntity {

    // ===== 同步数据 =====

    private static final EntityDataAccessor<Optional<UUID>> OWNER =
        SynchedEntityData.defineId(
            FlyingSwordEntity.class,
            EntityDataSerializers.OPTIONAL_UUID
        );

    private static final EntityDataAccessor<Integer> AI_MODE =
        SynchedEntityData.defineId(
            FlyingSwordEntity.class,
            EntityDataSerializers.INT
        );

    private static final EntityDataAccessor<ItemStack> DISPLAY_ITEM_STACK =
        SynchedEntityData.defineId(
            FlyingSwordEntity.class,
            EntityDataSerializers.ITEM_STACK
        );

    private static final EntityDataAccessor<Integer> QUALITY_TIER =
        SynchedEntityData.defineId(
            FlyingSwordEntity.class,
            EntityDataSerializers.INT
        );

    private static final EntityDataAccessor<Integer> SWORD_LEVEL =
        SynchedEntityData.defineId(
            FlyingSwordEntity.class,
            EntityDataSerializers.INT
        );

    private static final EntityDataAccessor<Integer> SWORD_EXP =
        SynchedEntityData.defineId(
            FlyingSwordEntity.class,
            EntityDataSerializers.INT
        );

    private static final EntityDataAccessor<Integer> AFFINITY =
        SynchedEntityData.defineId(
            FlyingSwordEntity.class,
            EntityDataSerializers.INT
        );

    private static final int PET_COOLDOWN_TICKS = 1200;

    private static final int FEED_AFFINITY_GAIN = 10;

    private static final int PET_AFFINITY_GAIN = 5;

    private static final int HEART_PARTICLE_COUNT = 5;

    private static final double PARTICLE_SPREAD = 0.5D;

    private static final float INTERACTION_SOUND_VOLUME = 1.0F;

    private static final float INTERACTION_SOUND_PITCH = 1.0F;

    // ===== 运行时缓存 =====

    @Nullable
    private LivingEntity cachedOwner;

    @Nullable
    private LivingEntity cachedTarget;

    // ===== 飞剑属性 =====

    private final FlyingSwordAttributes swordAttributes =
        new FlyingSwordAttributes();

    private long lastPetTime;

    // ===== 构造 =====

    public FlyingSwordEntity(
        EntityType<? extends PathfinderMob> type,
        Level level
    ) {
        super(type, level);
        this.setNoGravity(true);
        this.noCulling = true;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, FlyingSwordConstants.ENTITY_MAX_HEALTH)
            .add(Attributes.MOVEMENT_SPEED, 0.0D)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
            .add(
                Attributes.FOLLOW_RANGE,
                FlyingSwordConstants.ENTITY_FOLLOW_RANGE
            );
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER, Optional.empty());
        builder.define(AI_MODE, SwordAIMode.ORBIT.ordinal());
        builder.define(DISPLAY_ITEM_STACK, new ItemStack(Items.IRON_SWORD));
        builder.define(QUALITY_TIER, 0);
        builder.define(SWORD_LEVEL, 1);
        builder.define(SWORD_EXP, 0);
        builder.define(AFFINITY, 0);
    }

    @Override
    protected void registerGoals() {
        // 不使用传统 Goal 系统，AI 由 SwordAIDriver 驱动
    }

    // ===== Owner =====

    public void setOwner(@Nullable LivingEntity owner) {
        if (owner == null) {
            this.entityData.set(OWNER, Optional.empty());
            this.cachedOwner = null;
            return;
        }
        this.entityData.set(OWNER, Optional.of(owner.getUUID()));
        this.cachedOwner = owner;
    }

    @Override
    @Nullable
    public UUID getOwnerUUID() {
        return entityData.get(OWNER).orElse(null);
    }

    @Nullable
    @Override
    public LivingEntity getOwner() {
        // 优先使用缓存
        if (cachedOwner != null && !cachedOwner.isRemoved()) {
            return cachedOwner;
        }

        UUID id = getOwnerUUID();
        if (id == null) {
            cachedOwner = null;
            return null;
        }

        // 扫描附近实体查找主人
        for (Entity entity : level().getEntities(
            this,
            getBoundingBox().inflate(FlyingSwordConstants.OWNER_SCAN_RANGE)
        )) {
            if (!(entity instanceof LivingEntity living)) {
                continue;
            }
            if (id.equals(living.getUUID())) {
                cachedOwner = living;
                return cachedOwner;
            }
        }

        cachedOwner = null;
        return null;
    }

    public boolean isOwnedBy(@Nullable LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        Optional<UUID> id = entityData.get(OWNER);
        return id.isPresent() && id.get().equals(entity.getUUID());
    }

    // ===== AI Mode =====

    /**
     * 获取 AI 模式（枚举）。
     */
    public SwordAIMode getAIModeEnum() {
        int ordinal = entityData.get(AI_MODE);
        return SwordAIMode.fromOrdinal(ordinal);
    }

    /**
     * 获取 AI 模式（兼容旧接口）。
     * @deprecated 使用 {@link #getAIModeEnum()}
     */
    @Deprecated
    public AIMode getAIMode() {
        return AIMode.values()[Math.min(
            getAIModeEnum().ordinal(),
            AIMode.values().length - 1
        )];
    }

    /**
     * 设置 AI 模式。
     */
    public void setAIMode(SwordAIMode mode) {
        if (mode == null) {
            mode = SwordAIMode.ORBIT;
        }
        entityData.set(AI_MODE, mode.ordinal());
    }

    /**
     * 设置 AI 模式（兼容旧接口）。
     * @deprecated 使用 {@link #setAIMode(SwordAIMode)}
     */
    @Deprecated
    public void setAIMode(AIMode mode) {
        if (mode == null) {
            mode = AIMode.ORBIT;
        }
        entityData.set(AI_MODE, mode.ordinal());
    }

    /**
     * 兼容旧代码的 AIMode 枚举。
     * @deprecated 使用 {@link SwordAIMode}
     */
    @Deprecated
    public enum AIMode {
        ORBIT,
        GUARD,
        HUNT,
        HOVER,
        RECALL,
    }

    // ===== Display Item =====

    public ItemStack getDisplayItemStack() {
        return entityData.get(DISPLAY_ITEM_STACK);
    }

    public void setDisplayItemStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            entityData.set(DISPLAY_ITEM_STACK, new ItemStack(Items.IRON_SWORD));
            return;
        }
        entityData.set(DISPLAY_ITEM_STACK, stack.copy());
    }

    // ===== Sword Attributes =====

    public FlyingSwordAttributes getSwordAttributes() {
        return swordAttributes;
    }

    /**
     * 将 swordAttributes 的品质/等级/经验写入 entityData（仅服务端调用）。
     * <p>
     * 通过 SynchedEntityData 自动同步到客户端，
     * 客户端在 {@link #onSyncedDataUpdated(EntityDataAccessor)} 中更新本地镜像。
     * </p>
     */
    public void syncAttributesToEntityData() {
        if (level().isClientSide()) {
            return;
        }
        entityData.set(QUALITY_TIER, swordAttributes.getQuality().ordinal());
        entityData.set(SWORD_LEVEL, swordAttributes.getLevel());
        entityData.set(SWORD_EXP, swordAttributes.getExperience());
        entityData.set(AFFINITY, swordAttributes.getSpiritData().getAffinity());
    }

    public void syncSpiritDataToEntityData() {
        if (level().isClientSide()) {
            return;
        }
        entityData.set(AFFINITY, swordAttributes.getSpiritData().getAffinity());
    }

    // ===== Cached Target =====

    @Nullable
    public LivingEntity getCachedTarget() {
        return cachedTarget;
    }

    public void setCachedTarget(@Nullable LivingEntity target) {
        this.cachedTarget = target;
    }

    // ===== Tick =====

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            return;
        }

        LivingEntity owner = getOwner();
        if (owner == null || owner.isRemoved()) {
            discard();
            return;
        }

        // 委托给 AI 驱动
        LivingEntity newTarget = SwordAIDriver.tickAI(
            this,
            owner,
            cachedTarget
        );
        this.cachedTarget = newTarget;
    }

    @Override
    public InteractionResult mobInteract(
        Player player,
        InteractionHand hand
    ) {
        if (level().isClientSide() || !isOwnedBy(player)) {
            return super.mobInteract(player, hand);
        }

        SwordSpiritData spirit = swordAttributes.getSpiritData();
        ItemStack stack = player.getItemInHand(hand);

        if (!stack.isEmpty()) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            spirit.addAffinity(FEED_AFFINITY_GAIN);
            syncSpiritDataToEntityData();

            playSound(
                SoundEvents.GENERIC_EAT,
                INTERACTION_SOUND_VOLUME,
                INTERACTION_SOUND_PITCH
            );
            spawnHeartParticles();

            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown()) {
            long currentTime = level().getGameTime();
            if (currentTime - lastPetTime >= PET_COOLDOWN_TICKS) {
                lastPetTime = currentTime;
                spirit.addAffinity(PET_AFFINITY_GAIN);
                syncSpiritDataToEntityData();

                playSound(
                    SoundEvents.WOLF_WHINE,
                    INTERACTION_SOUND_VOLUME,
                    INTERACTION_SOUND_PITCH
                );
                spawnHeartParticles();

                return InteractionResult.SUCCESS;
            }
        }

        return super.mobInteract(player, hand);
    }

    private void spawnHeartParticles() {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.HEART,
                getX(),
                getY() + getBbHeight(),
                getZ(),
                HEART_PARTICLE_COUNT,
                PARTICLE_SPREAD,
                PARTICLE_SPREAD,
                PARTICLE_SPREAD,
                0.0D
            );
        }
    }

    // ===== Despawn Control =====

    @Override
    public boolean shouldDespawnInPeaceful() {
        return false;
    }

    // ===== NBT =====

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        // Owner
        entityData.get(OWNER).ifPresent(uuid -> tag.putUUID("Owner", uuid));

        // AI Mode
        tag.putInt("AIMode", entityData.get(AI_MODE));

        // Display Item
        try {
            tag.put(
                "DisplayItem",
                getDisplayItemStack().save(registryAccess())
            );
        } catch (Exception ignored) {}

        // Sword Attributes（使用新的品质/等级系统）
        tag.put("Attributes", swordAttributes.toNBT());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        // Owner
        if (tag.hasUUID("Owner")) {
            entityData.set(OWNER, Optional.of(tag.getUUID("Owner")));
        }

        // AI Mode
        if (tag.contains("AIMode")) {
            entityData.set(AI_MODE, tag.getInt("AIMode"));
        }

        // Display Item
        if (tag.contains("DisplayItem")) {
            try {
                ItemStack stack = ItemStack.parseOptional(
                    registryAccess(),
                    tag.getCompound("DisplayItem")
                );
                if (!stack.isEmpty()) {
                    entityData.set(DISPLAY_ITEM_STACK, stack);
                }
            } catch (Exception ignored) {}
        }

        // Sword Attributes（使用新的品质/等级系统）
        if (tag.contains("Attributes")) {
            swordAttributes.readFromNBT(tag.getCompound("Attributes"));
            syncAttributesToEntityData();
        }
    }

    /**
     * 获取当前品质。
     */
    public SwordQuality getQuality() {
        if (level().isClientSide()) {
            int tier = entityData.get(QUALITY_TIER);
            return SwordQuality.fromTier(tier);
        }
        return swordAttributes.getQuality();
    }

    /**
     * 获取当前等级。
     */
    public int getSwordLevel() {
        if (level().isClientSide()) {
            return entityData.get(SWORD_LEVEL);
        }
        return swordAttributes.getLevel();
    }

    /**
     * 获取当前经验。
     */
    public int getSwordExperience() {
        if (level().isClientSide()) {
            return entityData.get(SWORD_EXP);
        }
        return swordAttributes.getExperience();
    }

    public int getAffinity() {
        return entityData.get(AFFINITY);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (!level().isClientSide()) {
            return;
        }
        if (key.equals(QUALITY_TIER)) {
            swordAttributes.getGrowthData().setQualityRaw(
                SwordQuality.fromTier(entityData.get(QUALITY_TIER))
            );
            swordAttributes.recalculateFromGrowth();
        } else if (key.equals(SWORD_LEVEL)) {
            swordAttributes.getGrowthData().setLevelRaw(
                entityData.get(SWORD_LEVEL)
            );
            swordAttributes.recalculateFromGrowth();
        } else if (key.equals(SWORD_EXP)) {
            swordAttributes.getGrowthData().setExperienceRaw(
                entityData.get(SWORD_EXP)
            );
        }
    }

    /**
     * 添加经验（自动升级）。
     *
     * @param amount 经验量
     * @return 升级结果
     */
    public SwordGrowthData.ExpAddResult addExperience(int amount) {
        SwordGrowthData.ExpAddResult result = swordAttributes.addExperience(
            amount
        );
        syncAttributesToEntityData();
        return result;
    }

    /**
     * 尝试突破到下一品质。
     *
     * @return 突破结果
     */
    public SwordExpCalculator.BreakthroughResult tryBreakthrough() {
        SwordExpCalculator.BreakthroughResult result = swordAttributes.tryBreakthrough();
        syncAttributesToEntityData();
        return result;
    }

    /**
     * 获取属性摘要（用于调试/显示）。
     */
    public String getAttributesSummary() {
        return swordAttributes.getSummary();
    }

    // ===== 兼容旧代码的 NBT 方法 =====

    /**
     * @deprecated 使用 {@link FlyingSwordAttributes#toNBT()}
     */
    @Deprecated
    public CompoundTag writeAttributesToTag() {
        return swordAttributes.toNBT();
    }

    /**
     * @deprecated 使用 {@link FlyingSwordAttributes#readFromNBT(CompoundTag)}
     */
    @Deprecated
    public void readAttributesFromTag(CompoundTag tag) {
        swordAttributes.readFromNBT(tag);
        syncAttributesToEntityData();
    }
}
