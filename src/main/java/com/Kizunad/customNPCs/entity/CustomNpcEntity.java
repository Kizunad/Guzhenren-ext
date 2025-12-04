package com.Kizunad.customNPCs.entity;

import com.Kizunad.customNPCs.CustomNPCsMod;
import com.Kizunad.customNPCs.ai.config.NpcAttributeDefaults;
import com.Kizunad.customNPCs.ai.status.StatusProviderRegistry;
import com.Kizunad.customNPCs.util.SkinPool;
import com.Kizunad.customNPCs.network.OpenInteractGuiPayload;
import com.Kizunad.customNPCs.network.dto.DialogueOption;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.common.NeoForgeMod;

/**
 * 专属自定义 NPC 实体，运行自研 AI（NpcMind + Sensors + Actions）。
 * 仅保留必要的导航能力，移除原版 AI 干扰。
 */
public class CustomNpcEntity extends PathfinderMob {

    public static final String MIND_TAG = "customnpcs:mind_allowed";
    private static final String EXPERIENCE_TAG = "customnpcs:experience";
    private static final String STRENGTH_TAG = "customnpcs:strength_bonus";
    private static final String HEALTH_TAG = "customnpcs:health_bonus";
    private static final String SPEED_TAG = "customnpcs:speed_bonus";
    private static final String DEFENSE_TAG = "customnpcs:defense_bonus";
    private static final String SENSOR_TAG = "customnpcs:sensor_bonus";
    private static final String MATERIAL_TAG = "customnpcs:material";
    private static final String SKIN_TAG = "customnpcs:skin";
    private static final ResourceLocation DEFAULT_SKIN =
        SkinPool.getRandomSkin();
    private static final ResourceLocation LEGACY_DEFAULT_SKIN =
        ResourceLocation.fromNamespaceAndPath(
            CustomNPCsMod.MODID,
            "textures/entity/custom_npc.png"
        );
    private static final EntityDataAccessor<Integer> EXPERIENCE =
        SynchedEntityData.defineId(
            CustomNpcEntity.class,
            EntityDataSerializers.INT
        );
    private static final EntityDataAccessor<Float> STRENGTH_BONUS =
        SynchedEntityData.defineId(
            CustomNpcEntity.class,
            EntityDataSerializers.FLOAT
        );
    private static final EntityDataAccessor<Float> HEALTH_BONUS =
        SynchedEntityData.defineId(
            CustomNpcEntity.class,
            EntityDataSerializers.FLOAT
        );
    private static final EntityDataAccessor<Float> SPEED_BONUS =
        SynchedEntityData.defineId(
            CustomNpcEntity.class,
            EntityDataSerializers.FLOAT
        );
    private static final EntityDataAccessor<Float> DEFENSE_BONUS =
        SynchedEntityData.defineId(
            CustomNpcEntity.class,
            EntityDataSerializers.FLOAT
        );
    private static final EntityDataAccessor<Float> SENSOR_BONUS =
        SynchedEntityData.defineId(
            CustomNpcEntity.class,
            EntityDataSerializers.FLOAT
        );
    private static final EntityDataAccessor<Float> MATERIAL =
        SynchedEntityData.defineId(
            CustomNpcEntity.class,
            EntityDataSerializers.FLOAT
        );
    private static final EntityDataAccessor<String> SKIN_TEXTURE =
        SynchedEntityData.defineId(
            CustomNpcEntity.class,
            EntityDataSerializers.STRING
        );
    private static final int FLYING_MAX_TURN = 10;
    private static final int SWIM_MAX_TURN = 85;
    private static final int SWIM_VERTICAL_RATE = 10;
    private static final double SWIM_MOVE_EPSILON_SQ = 2.5E-7D;
    private static final float RAD_TO_DEG = 180.0F / (float) Math.PI;
    private static final float SWIM_YAW_OFFSET_DEG = 90.0F;
    private static final float SWIM_MAX_PITCH_DEG = 85.0F;
    // 适中速度/加速度，避免高频上下抖动
    private static final float SWIM_BASE_SPEED = 0.3F;
    private static final float SWIM_ACCELERATION = 0.05F;
    private static final boolean SWIM_HAS_GRAVITY = false;
    private static final float WATER_SPEED_MODIFIER = 0.01F;
    private static final double WATER_MOMENTUM_RETENTION = 0.9D;
    private static final double WATER_PATH_PUSH_SCALE = 0.15D;
    private static final float WATER_YAW_LERP_FACTOR = 0.1F;

    private static final int WATER_TELEPORT_COOLDOWN_TICKS = 100;
    private static final int LAND_SEARCH_RADIUS = 10;
    private static final int LAND_SEARCH_VERTICAL_RANGE = 4;
    private static final int TOTEM_REGEN_DURATION_TICKS = 900;
    private static final int TOTEM_REGEN_AMPLIFIER = 1;
    private static final int TOTEM_ABSORPTION_DURATION_TICKS = 100;
    private static final int TOTEM_ABSORPTION_AMPLIFIER = 1;
    private static final int TOTEM_FIRE_RESIST_DURATION_TICKS = 800;
    private static final int TOTEM_FIRE_RESIST_AMPLIFIER = 0;
    private static final byte TOTEM_USE_EVENT = 35;
    private static final double INTERACT_DISTANCE = 20.0D;
    private static final double INTERACT_DISTANCE_SQR =
        INTERACT_DISTANCE * INTERACT_DISTANCE;

    private int waterTeleportCooldown = 0;
    /**
     * 开关：是否允许在水中时自动寻找陆地并传送。
     * 默认为 true。修改此字段可控制行为。
     */
    public boolean allowWaterTeleport = false;

    public enum NavigationMode {
        GROUND,
        FLYING,
        WATER,
        AMPHIBIOUS,
        WALL,
    }

    private NavigationMode navigationMode = NavigationMode.AMPHIBIOUS;
    private ResourceLocation cachedSkinTexture = null;

    public CustomNpcEntity(
        EntityType<? extends CustomNpcEntity> type,
        Level level
    ) {
        // 默认使用两栖导航，确保水下/水面/陆地都能正常寻路和攻击
        this(type, level, NavigationMode.AMPHIBIOUS);
    }

    public CustomNpcEntity(
        EntityType<? extends CustomNpcEntity> type,
        Level level,
        NavigationMode mode
    ) {
        super(type, level);
        this.navigationMode = mode;
        this.setPersistenceRequired();
        this.getTags().add(MIND_TAG); // 触发 NpcMindAttachment 自动挂载
        this.setCanPickUpLoot(true);
        // 允许水域寻路并消除水域惩罚，避免落水后导航停滞
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 0.0F);
        // 服务端构造时立即分配皮肤，后续读取存档会覆盖随机值，避免指令/代码生成时未触发 finalizeSpawn 造成纹理缺失。
        this.ensureSkinTextureAssigned();

        /*
         * NOTE: FlyingMoveControl
         * 默认 MoveControl 是地面逻辑，假设有重力、地面摩擦，会尝试贴地，无法
         * 正常悬停/升降。飞行实体需要绕过重力/地面摩擦并支持垂直/三维调整，因此要用 FlyingMoveControl 来正确
         * 应用速度和角度。
         */
        if (navigationMode == NavigationMode.FLYING) {
            this.moveControl = new FlyingMoveControl(
                this,
                FLYING_MAX_TURN,
                false
            );
        }
        // 覆盖 super 中创建的默认导航，按模式替换
        this.navigation = createNavigation(level);
    }

    @Override
    public net.minecraft.world.InteractionResult mobInteract(
        net.minecraft.world.entity.player.Player player,
        net.minecraft.world.InteractionHand hand
    ) {
        if (hand == net.minecraft.world.InteractionHand.MAIN_HAND) {
            if (!this.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
                openInteractGui(serverPlayer);
            }
            return net.minecraft.world.InteractionResult.sidedSuccess(
                this.level().isClientSide
            );
        }
        return super.mobInteract(player, hand);
    }

    private void openInteractGui(ServerPlayer serverPlayer) {
        if (
            !this.isAlive() ||
            this.isRemoved() ||
            this.distanceToSqr(serverPlayer) > INTERACT_DISTANCE_SQR
        ) {
            return;
        }
        var statuses = StatusProviderRegistry.collect(this);
        var options = java.util.Collections.<DialogueOption>emptyList();
        boolean isOwner = false;
        PacketDistributor.sendToPlayer(
            serverPlayer,
            new OpenInteractGuiPayload(
                this.getId(),
                this.getDisplayName(),
                this.getHealth(),
                this.getMaxHealth(),
                isOwner,
                statuses,
                options
            )
        );
    }

    @Override
    @javax.annotation.Nullable
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
        net.minecraft.world.level.ServerLevelAccessor level,
        net.minecraft.world.DifficultyInstance difficulty,
        net.minecraft.world.entity.MobSpawnType reason,
        @javax.annotation.Nullable net.minecraft.world.entity.SpawnGroupData spawnData
    ) {
        spawnData = super.finalizeSpawn(level, difficulty, reason, spawnData);
        com.Kizunad.customNPCs.registry.NpcSpawnRegistry.onSpawn(
            this,
            level,
            difficulty,
            reason,
            spawnData
        );
        // 保底分配皮肤（自然生成流程中仍走 finalizeSpawn）。
        this.ensureSkinTextureAssigned();
        return spawnData;
    }

    @Override
    public void tick() {
        super.tick();
        // 两栖/水生：在水中重置氧气，避免缺氧导致上浮，保证水下攻击
        boolean inWater = this.isInWater();
        if (
            (this.navigationMode == NavigationMode.WATER ||
                this.navigationMode == NavigationMode.AMPHIBIOUS) &&
            inWater
        ) {
            this.setAirSupply(this.getMaxAirSupply());
            this.setSwimming(true); // 强制游泳姿态
            
            // 手动水中推进逻辑：
            // 因为使用了 GroundPathNavigation，在水中 MoveControl 几乎失效。
            // 这里直接检测是否有路径，如果有，强行推向下一个节点。
            if (!this.level().isClientSide && !this.navigation.isDone()) {
                net.minecraft.world.level.pathfinder.Path path = this.navigation.getPath();
                if (path != null) {
                    net.minecraft.world.phys.Vec3 nextPos = path.getNextEntityPos(this);
                    net.minecraft.world.phys.Vec3 dir = nextPos.subtract(this.position()).normalize();
                    // 给予一个向前的推力，速度适中
                    double speed = this.getAttributeValue(Attributes.MOVEMENT_SPEED) * WATER_PATH_PUSH_SCALE;
                    this.setDeltaMovement(this.getDeltaMovement().add(dir.scale(speed)));
                    
                    // 简单的朝向调整
                    double dx = nextPos.x - this.getX();
                    double dz = nextPos.z - this.getZ();
                    float targetYaw =
                        (float) (Mth.atan2(dz, dx) * RAD_TO_DEG) -
                        SWIM_YAW_OFFSET_DEG;
                    this.setYRot(
                        this.getYRot() +
                        Mth.wrapDegrees(targetYaw - this.getYRot()) * WATER_YAW_LERP_FACTOR
                    );
                    this.yBodyRot = this.getYRot();
                }
            }
            
        } else {
            this.setSwimming(false);
        }

        if (
            !this.level().isClientSide &&
            this.isInWater() &&
            this.allowWaterTeleport
        ) {
            if (this.waterTeleportCooldown > 0) {
                this.waterTeleportCooldown--;
            } else {
                // 尝试寻找陆地
                net.minecraft.core.BlockPos land = findNearestLand(
                    (ServerLevel) this.level(),
                    this.blockPosition()
                );
                if (land != null) {
                    net.minecraft.world.phys.Vec3 target =
                        net.minecraft.world.phys.Vec3.atCenterOf(land);
                    this.teleportTo(target.x, target.y, target.z);
                    this.waterTeleportCooldown = WATER_TELEPORT_COOLDOWN_TICKS; // 传送成功后冷却
                } else {
                    this.waterTeleportCooldown = WATER_TELEPORT_COOLDOWN_TICKS; // 找不到陆地也冷却，回归原有行为
                }
            }
        } else {
            this.waterTeleportCooldown = 0;
        }

        com.Kizunad.customNPCs.registry.NpcTickRegistry.onTick(this);
    }

    private net.minecraft.core.BlockPos findNearestLand(
        ServerLevel level,
        net.minecraft.core.BlockPos origin
    ) {
        net.minecraft.core.BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        int radius = LAND_SEARCH_RADIUS;
        int vRange = LAND_SEARCH_VERTICAL_RANGE;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -vRange; dy <= vRange; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    net.minecraft.core.BlockPos pos = origin.offset(dx, dy, dz);
                    if (isLandable(level, pos)) {
                        double dist = origin.distSqr(pos);
                        if (dist < bestDist) {
                            bestDist = dist;
                            best = pos.immutable();
                        }
                    }
                }
            }
        }
        return best;
    }

    private boolean isLandable(
        ServerLevel level,
        net.minecraft.core.BlockPos pos
    ) {
        if (!level.hasChunkAt(pos)) {
            return false;
        }
        net.minecraft.world.level.block.state.BlockState feet =
            level.getBlockState(pos);
        net.minecraft.world.level.block.state.BlockState above =
            level.getBlockState(pos.above());
        if (
            !feet.getFluidState().isEmpty() || !above.getFluidState().isEmpty()
        ) {
            return false;
        }
        if (!feet.getCollisionShape(level, pos).isEmpty()) {
            return false;
        }
        net.minecraft.core.BlockPos belowPos = pos.below();
        net.minecraft.world.level.block.state.BlockState below =
            level.getBlockState(belowPos);
        return below.isFaceSturdy(
            level,
            belowPos,
            net.minecraft.core.Direction.UP
        );
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        // 经验值：后续成长系统基础数据，默认 0
        builder.define(EXPERIENCE, 0);
        // 成长增益：暂存升级点，后续映射到 Attribute
        // Strength -> ATTACK_DAMAGE
        builder.define(STRENGTH_BONUS, 0.0F);
        // Health -> MAX_HEALTH
        builder.define(HEALTH_BONUS, 0.0F);
        // Speed -> ATTACK_SPEED/MOVEMENT_SPEED/FLYING_SPEED/SWIM_SPEED
        //       -> STEP_HEIGHT/JUMP_STRENGTH/SNEAKING_SPEED
        builder.define(SPEED_BONUS, 0.0F);
        // Defense -> ARMOR/KNOCKBACK_RESISTANCE
        builder.define(DEFENSE_BONUS, 0.0F);
        // Sensor -> FOLLOW_RANGE
        builder.define(SENSOR_BONUS, 0.0F);
        // 材料储备：用于制造/建造等行为的基础点数
        builder.define(MATERIAL, 0.0F);
        // 默认皮肤：使用占位纹理，未分配随机皮肤时兜底
        builder.define(SKIN_TEXTURE, DEFAULT_SKIN.toString());
    }

    @Override
    protected void registerGoals() {
        // 自定义 AI 全由 NpcMind 驱动，清空原版 Goals 避免干扰
        // 对地面/攀墙实体保留漂浮，避免落水即溺亡；两栖/水生无需强制上浮
        NavigationMode mode = this.navigationMode == null
            ? NavigationMode.GROUND
            : this.navigationMode;
        if (mode == NavigationMode.GROUND || mode == NavigationMode.WALL) {
            this.goalSelector.addGoal(0, new FloatGoal(this));
        }
    }

    @Override
    protected void pickUpItem(
        net.minecraft.world.entity.item.ItemEntity itemEntity
    ) {
        var mindHolder = this.getData(
            com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment.NPC_MIND
        );
        if (mindHolder != null) {
            var mind = mindHolder;
            var inventory = mind.getInventory();
            ItemStack entityStack = itemEntity.getItem();
            int originalCount = entityStack.getCount();
            ItemStack remaining = inventory.addItem(entityStack);
            if (remaining.isEmpty()) {
                this.take(itemEntity, originalCount);
                itemEntity.discard();
                return;
            }
            itemEntity.setItem(remaining);
        }
        super.pickUpItem(itemEntity);
    }

    @Override
    protected void dropCustomDeathLoot(
        net.minecraft.server.level.ServerLevel level,
        net.minecraft.world.damagesource.DamageSource source,
        boolean causedByPlayer
    ) {
        super.dropCustomDeathLoot(level, source, causedByPlayer);
        if (
            this.hasData(
                com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment.NPC_MIND
            )
        ) {
            var mind = this.getData(
                com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment.NPC_MIND
            );
            var inventory = mind.getInventory();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack stack = inventory.removeItemNoUpdate(i);
                if (!stack.isEmpty()) {
                    this.spawnAtLocation(stack);
                }
            }
        }

        // 确保装备槽（主手/副手/护甲）也掉落，避免仅背包物品被清空
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HAND || slot.isArmor()) {
                ItemStack equipped = this.getItemBySlot(slot);
                if (!equipped.isEmpty()) {
                    this.spawnAtLocation(equipped.copy());
                    this.setItemSlot(slot, ItemStack.EMPTY);
                }
            }
        }
    }

    @Override
    public void die(DamageSource source) {
        if (tryUseTotemFromInventory(source)) {
            return;
        }
        super.die(source);
    }

    /**
     * 尝试从背包消耗不死图腾，复刻原版效果。
     * @param source 伤害来源
     * @return 成功触发图腾则返回 true
     */
    private boolean tryUseTotemFromInventory(DamageSource source) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        var mind = this.getData(
            com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment.NPC_MIND
        );
        if (mind == null) {
            return false;
        }
        var inventory = mind.getInventory();
        int totemSlot = inventory.findFirstSlot(
            stack -> stack.is(Items.TOTEM_OF_UNDYING)
        );
        if (totemSlot < 0) {
            return false;
        }
        ItemStack totemStack = inventory.getItem(totemSlot);
        totemStack.shrink(1);
        if (totemStack.isEmpty()) {
            inventory.setItem(totemSlot, ItemStack.EMPTY);
        }
        this.setHealth(1.0F);
        this.removeAllEffects();
        this.addEffect(
            new MobEffectInstance(
                MobEffects.REGENERATION,
                TOTEM_REGEN_DURATION_TICKS,
                TOTEM_REGEN_AMPLIFIER
            )
        );
        this.addEffect(
            new MobEffectInstance(
                MobEffects.ABSORPTION,
                TOTEM_ABSORPTION_DURATION_TICKS,
                TOTEM_ABSORPTION_AMPLIFIER
            )
        );
        this.addEffect(
            new MobEffectInstance(
                MobEffects.FIRE_RESISTANCE,
                TOTEM_FIRE_RESIST_DURATION_TICKS,
                TOTEM_FIRE_RESIST_AMPLIFIER
            )
        );
        this.level().broadcastEntityEvent(this, TOTEM_USE_EVENT);
        return true;
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity victim) {
        boolean result = super.killedEntity(level, victim);
        // 击杀奖励：基础 1 点，叠加目标最大生命值
        if (!level.isClientSide) {
            int gain = 1 + Mth.floor(victim.getMaxHealth());
            this.addExperience(gain);
            // 额外奖励：尝试发放一块熟猪排，有空位才放入
            var mind = this.getData(
                com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment.NPC_MIND
            );
            if (mind != null) {
                var inventory = mind.getInventory();
                ItemStack leftover = inventory.addItem(
                    new ItemStack(Items.COOKED_PORKCHOP)
                );
                if (!leftover.isEmpty()) {
                    // 背包无空间，按需求不做额外处理
                }
            }
        }
        return result;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        NavigationMode mode = navigationMode;
        if (mode == null) {
            mode = NavigationMode.GROUND;
            this.navigationMode = mode;
        }
        PathNavigation created;
        if (mode == NavigationMode.FLYING) {
            created = new FlyingPathNavigation(this, level);
        } else if (mode == NavigationMode.WALL) {
            created = new WallClimberNavigation(this, level);
        } else {
            // 地面、水生、两栖统一使用地面导航，依靠手动逻辑处理水中移动
            created = new GroundPathNavigation(this, level);
        }
        
        // 允许在水中漂浮/游泳
        boolean shouldFloat = true;
        created.setCanFloat(shouldFloat);
        return created;
    }

    @Override
    public void travel(net.minecraft.world.phys.Vec3 pTravelVector) {
        if (
            this.isEffectiveAi() &&
            this.isInWater() &&
            (this.navigationMode == NavigationMode.WATER ||
                this.navigationMode == NavigationMode.AMPHIBIOUS)
        ) {
            this.moveRelative(WATER_SPEED_MODIFIER, pTravelVector);
            this.move(
                net.minecraft.world.entity.MoverType.SELF,
                this.getDeltaMovement()
            );
            this.setDeltaMovement(
                this.getDeltaMovement().scale(WATER_MOMENTUM_RETENTION)
            );
        } else {
            super.travel(pTravelVector);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(EXPERIENCE_TAG, this.getExperience());
        tag.putFloat(STRENGTH_TAG, this.getStrengthBonus());
        tag.putFloat(HEALTH_TAG, this.getHealthBonus());
        tag.putFloat(SPEED_TAG, this.getSpeedBonus());
        tag.putFloat(DEFENSE_TAG, this.getDefenseBonus());
        tag.putFloat(SENSOR_TAG, this.getSensorBonus());
        tag.putFloat(MATERIAL_TAG, this.getMaterial());
        tag.putString(SKIN_TAG, this.getSkinTexture().toString());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains(EXPERIENCE_TAG)) {
            this.setExperience(tag.getInt(EXPERIENCE_TAG));
        }
        if (tag.contains(STRENGTH_TAG)) {
            this.setStrengthBonus(tag.getFloat(STRENGTH_TAG));
        }
        if (tag.contains(HEALTH_TAG)) {
            this.setHealthBonus(tag.getFloat(HEALTH_TAG));
        }
        if (tag.contains(SPEED_TAG)) {
            this.setSpeedBonus(tag.getFloat(SPEED_TAG));
        }
        if (tag.contains(DEFENSE_TAG)) {
            this.setDefenseBonus(tag.getFloat(DEFENSE_TAG));
        }
        if (tag.contains(SENSOR_TAG)) {
            this.setSensorBonus(tag.getFloat(SENSOR_TAG));
        }
        if (tag.contains(MATERIAL_TAG)) {
            this.setMaterial(tag.getFloat(MATERIAL_TAG));
        }
        if (tag.contains(SKIN_TAG)) {
            ResourceLocation skin = ResourceLocation.tryParse(
                tag.getString(SKIN_TAG)
            );
            if (skin != null) {
                // 兼容旧存档/占位纹理，自动切换到随机皮肤
                if (skin.equals(LEGACY_DEFAULT_SKIN)) {
                    this.setSkinTexture(SkinPool.getRandomSkin());
                } else {
                    this.setSkinTexture(skin);
                }
            }
        }
    }

    /**
     * 当前皮肤纹理，已在服务端抽取并同步。
     */
    public ResourceLocation getSkinTexture() {
        String stored = this.entityData.get(SKIN_TEXTURE);
        if (
            this.cachedSkinTexture != null &&
            stored.equals(this.cachedSkinTexture.toString())
        ) {
            return this.cachedSkinTexture;
        }
        ResourceLocation skin = ResourceLocation.tryParse(stored);
        if (LEGACY_DEFAULT_SKIN.toString().equals(stored)) {
            skin = SkinPool.getRandomSkin();
            this.setSkinTexture(skin);
        }
        this.cachedSkinTexture = skin == null ? DEFAULT_SKIN : skin;
        return this.cachedSkinTexture;
    }

    /**
     * 设置皮肤纹理，传入 null 时回退到默认皮肤。
     */
    public void setSkinTexture(
        @javax.annotation.Nullable ResourceLocation skin
    ) {
        ResourceLocation target = skin == null ? DEFAULT_SKIN : skin;
        this.cachedSkinTexture = target;
        this.entityData.set(SKIN_TEXTURE, target.toString());
    }

    /**
     * 确保服务端已有皮肤纹理，默认占位时抽取随机皮肤。
     */
    private void ensureSkinTextureAssigned() {
        if (this.level().isClientSide()) {
            return;
        }
        String current = this.entityData.get(SKIN_TEXTURE);
        if (
            DEFAULT_SKIN.toString().equals(current) ||
            LEGACY_DEFAULT_SKIN.toString().equals(current)
        ) {
            this.setSkinTexture(SkinPool.getRandomSkin());
        }
    }

    /**
     * 当前经验值。
     */
    public int getExperience() {
        return this.entityData.get(EXPERIENCE);
    }

    /**
     * 设置经验值，负数会被归零。
     */
    public void setExperience(int experience) {
        this.entityData.set(EXPERIENCE, Math.max(0, experience));
    }

    /**
     * 增量修改经验值，可用于掉落/奖励等场景。
     */
    public void addExperience(int experience) {
        if (experience == 0) {
            return;
        }
        this.setExperience(this.getExperience() + experience);
    }

    /**
     * 力量增益（映射 ATTACK_DAMAGE）。
     */
    public float getStrengthBonus() {
        return this.entityData.get(STRENGTH_BONUS);
    }

    public void setStrengthBonus(float value) {
        this.entityData.set(STRENGTH_BONUS, clampNonNegative(value));
        this.refreshGrowthAttributes();
    }

    public void addStrengthBonus(float delta) {
        if (delta == 0) {
            return;
        }
        this.setStrengthBonus(this.getStrengthBonus() + delta);
    }

    /**
     * 生命增益（映射 MAX_HEALTH）。
     */
    public float getHealthBonus() {
        return this.entityData.get(HEALTH_BONUS);
    }

    public void setHealthBonus(float value) {
        this.entityData.set(HEALTH_BONUS, clampNonNegative(value));
        this.refreshGrowthAttributes();
    }

    public void addHealthBonus(float delta) {
        if (delta == 0) {
            return;
        }
        this.setHealthBonus(this.getHealthBonus() + delta);
    }

    /**
     * 速度增益（映射 ATTACK_SPEED、MOVE/FLY/SWIM、STEP_HEIGHT、JUMP_STRENGTH、
     * SNEAKING_SPEED）。
     */
    public float getSpeedBonus() {
        return this.entityData.get(SPEED_BONUS);
    }

    public void setSpeedBonus(float value) {
        this.entityData.set(SPEED_BONUS, clampNonNegative(value));
        this.refreshGrowthAttributes();
    }

    public void addSpeedBonus(float delta) {
        if (delta == 0) {
            return;
        }
        this.setSpeedBonus(this.getSpeedBonus() + delta);
    }

    /**
     * 防御增益（映射 ARMOR、KNOCKBACK_RESISTANCE）。
     */
    public float getDefenseBonus() {
        return this.entityData.get(DEFENSE_BONUS);
    }

    public void setDefenseBonus(float value) {
        this.entityData.set(DEFENSE_BONUS, clampNonNegative(value));
        this.refreshGrowthAttributes();
    }

    public void addDefenseBonus(float delta) {
        if (delta == 0) {
            return;
        }
        this.setDefenseBonus(this.getDefenseBonus() + delta);
    }

    /**
     * 感应增益（映射 FOLLOW_RANGE）。
     */
    public float getSensorBonus() {
        return this.entityData.get(SENSOR_BONUS);
    }

    public void setSensorBonus(float value) {
        this.entityData.set(SENSOR_BONUS, clampNonNegative(value));
        this.refreshGrowthAttributes();
    }

    public void addSensorBonus(float delta) {
        if (delta == 0) {
            return;
        }
        this.setSensorBonus(this.getSensorBonus() + delta);
    }

    /**
     * 可用于制造/建造的材料点数。
     */
    public float getMaterial() {
        return this.entityData.get(MATERIAL);
    }

    /**
     * 设置材料点数，自动裁剪为非负数。
     */
    public void setMaterial(float value) {
        this.entityData.set(MATERIAL, clampNonNegative(value));
    }

    /**
     * 增量调整材料点数。
     */
    public void addMaterial(float delta) {
        if (delta == 0) {
            return;
        }
        this.setMaterial(this.getMaterial() + delta);
    }

    private float clampNonNegative(float value) {
        return Math.max(0.0F, value);
    }

    /**
     * 将增益数据同步到实体 Attribute，已在各 setter 内部自动调用。
     */
    private void refreshGrowthAttributes() {
        if (this.level().isClientSide()) {
            return;
        }
        double strengthBonus = this.getStrengthBonus();
        double healthBonus = this.getHealthBonus();
        double speedBonus = this.getSpeedBonus();
        double defenseBonus = this.getDefenseBonus();
        double sensorBonus = this.getSensorBonus();

        setAttributeBase(
            Attributes.ATTACK_DAMAGE,
            NpcAttributeDefaults.ATTACK_DAMAGE + strengthBonus
        );

        setAttributeBase(
            Attributes.MAX_HEALTH,
            NpcAttributeDefaults.MAX_HEALTH + healthBonus
        );

        setAttributeBase(
            Attributes.ATTACK_SPEED,
            NpcAttributeDefaults.ATTACK_SPEED + speedBonus
        );
        setAttributeBase(
            Attributes.MOVEMENT_SPEED,
            NpcAttributeDefaults.MOVEMENT_SPEED + speedBonus
        );
        setAttributeBase(
            Attributes.FLYING_SPEED,
            NpcAttributeDefaults.FLYING_SPEED + speedBonus
        );
        setAttributeBase(
            NeoForgeMod.SWIM_SPEED,
            NpcAttributeDefaults.SWIM_SPEED + speedBonus
        );
        setAttributeBase(
            Attributes.STEP_HEIGHT,
            NpcAttributeDefaults.STEP_HEIGHT + speedBonus
        );
        setAttributeBase(
            Attributes.JUMP_STRENGTH,
            NpcAttributeDefaults.JUMP_STRENGTH + speedBonus
        );
        setAttributeBase(
            Attributes.SNEAKING_SPEED,
            NpcAttributeDefaults.SNEAKING_SPEED + speedBonus
        );

        setAttributeBase(
            Attributes.ARMOR,
            NpcAttributeDefaults.ARMOR + defenseBonus
        );
        setAttributeBase(
            Attributes.KNOCKBACK_RESISTANCE,
            NpcAttributeDefaults.KNOCKBACK_RESISTANCE + defenseBonus
        );

        setAttributeBase(
            Attributes.FOLLOW_RANGE,
            NpcAttributeDefaults.FOLLOW_RANGE + sensorBonus
        );
    }

    private void setAttributeBase(
        Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
        double value
    ) {
        var instance = this.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    /**
     * 定义实体默认属性（生命/攻击/移速/护甲），供属性注册事件使用。
     */
    public static AttributeSupplier.Builder createAttributes() {
        AttributeSupplier.Builder builder = NpcAttributeDefaults.apply(
            AttributeSupplier.builder()
        );
        NpcAttributeDefaults.applyOptionalAttributes(builder);
        return builder;
    }

    /**
     * 便于后续自定义分类或生成规则时复用。
     */
    public static MobCategory getCategory() {
        return MobCategory.CREATURE;
    }
}
