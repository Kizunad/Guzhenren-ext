package com.Kizunad.guzhenrenext.kongqiao.flyingsword;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ai.SwordAIDriver;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ai.SwordAIMode;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.calculator.FlyingSwordAttributes;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.growth.SwordExpCalculator;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.growth.SwordGrowthData;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.quality.SwordQuality;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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

    // ===== 运行时缓存 =====

    @Nullable
    private LivingEntity cachedOwner;

    @Nullable
    private LivingEntity cachedTarget;

    // ===== 飞剑属性 =====

    private final FlyingSwordAttributes swordAttributes =
        new FlyingSwordAttributes();

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
        }
    }

    /**
     * 获取当前品质。
     */
    public SwordQuality getQuality() {
        return swordAttributes.getQuality();
    }

    /**
     * 获取当前等级。
     */
    public int getSwordLevel() {
        return swordAttributes.getLevel();
    }

    /**
     * 获取当前经验。
     */
    public int getSwordExperience() {
        return swordAttributes.getExperience();
    }

    /**
     * 添加经验（自动升级）。
     *
     * @param amount 经验量
     * @return 升级结果
     */
    public SwordGrowthData.ExpAddResult addExperience(int amount) {
        return swordAttributes.addExperience(amount);
    }

    /**
     * 尝试突破到下一品质。
     *
     * @return 突破结果
     */
    public SwordExpCalculator.BreakthroughResult tryBreakthrough() {
        return swordAttributes.tryBreakthrough();
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
    }
}
