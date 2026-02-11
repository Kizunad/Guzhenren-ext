package com.Kizunad.guzhenrenext.xianqiao.spirit;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * 地灵实体。
 * <p>
 * 设计目标：
 * 1) 作为仙窍管理入口，主人右键可打开管理菜单；
 * 2) 提供轻量巡逻 AI（浮水、随机游走、注视玩家）；
 * 3) 拥有认主信息并持久化到 NBT。
 * </p>
 */
public class LandSpiritEntity extends PathfinderMob {

    /** NBT：主人 UUID。 */
    private static final String KEY_OWNER_UUID = "OwnerUUID";

    /**
     * 仙窍维度键。
     */
    private static final ResourceKey<Level> APERTURE_DIMENSION = ResourceKey.create(
        Registries.DIMENSION,
        ResourceLocation.fromNamespaceAndPath(GuzhenrenExt.MODID, "aperture_world")
    );

    /** 巡逻速度。 */
    private static final double STROLL_SPEED = 0.6D;

    /** 巡逻触发间隔。 */
    private static final int STROLL_INTERVAL = 120;

    /** 注视玩家半径。 */
    private static final float LOOK_RANGE = 8.0F;

    /** 地灵基础生命值（实际由于无敌不会被消耗）。 */
    private static final double MAX_HEALTH = 40.0D;

    /** 地灵移动速度属性。 */
    private static final double MOVE_SPEED = 0.25D;

    /** 地灵跟随范围属性。 */
    private static final double FOLLOW_RANGE = 16.0D;

    /** 主人 UUID（仅服务端权威）。 */
    @Nullable
    private UUID ownerUUID;

    public LandSpiritEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    /**
     * 创建地灵属性。
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, MAX_HEALTH)
            .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
            .add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RandomStrollGoal(this, STROLL_SPEED, STROLL_INTERVAL));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, LOOK_RANGE));
    }

    /**
     * 设置主人 UUID。
     *
     * @param ownerUUID 主人 UUID
     */
    public void setOwnerUUID(@Nullable UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    /**
     * 获取主人 UUID。
     */
    @Nullable
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    /**
     * 判断指定玩家是否为主人。
     */
    public boolean isOwner(Player player) {
        return ownerUUID != null && ownerUUID.equals(player.getUUID());
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.CONSUME;
        }

        if (!isOwner(serverPlayer)) {
            serverPlayer.sendSystemMessage(Component.translatable("message.guzhenrenext.land_spirit.not_owner"));
            return InteractionResult.CONSUME;
        }

        ServerLevel apertureLevel = serverPlayer.server.getLevel(APERTURE_DIMENSION);
        if (apertureLevel == null) {
            serverPlayer.sendSystemMessage(Component.literal("仙窍维度未加载，暂时无法打开管理界面。"));
            return InteractionResult.CONSUME;
        }

        UUID owner = ownerUUID;
        if (owner == null) {
            serverPlayer.sendSystemMessage(Component.literal("地灵尚未完成认主，无法打开管理界面。"));
            return InteractionResult.CONSUME;
        }

        ApertureWorldData.get(apertureLevel).getOrAllocate(owner);
        MenuProvider provider = new SimpleMenuProvider(
            (containerId, inventory, ignoredPlayer) -> new LandSpiritMenu(containerId, inventory, owner, apertureLevel),
            Component.translatable("screen.guzhenrenext.spirit_management.title")
        );
        serverPlayer.openMenu(provider);
        return InteractionResult.CONSUME;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (ownerUUID != null) {
            compound.putUUID(KEY_OWNER_UUID, ownerUUID);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.hasUUID(KEY_OWNER_UUID)) {
            ownerUUID = compound.getUUID(KEY_OWNER_UUID);
        } else {
            ownerUUID = null;
        }
    }
}
