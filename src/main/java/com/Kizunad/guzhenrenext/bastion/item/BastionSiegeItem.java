package com.Kizunad.guzhenrenext.bastion.item;

import com.Kizunad.guzhenrenext.bastion.BastionBlocks;
import com.Kizunad.guzhenrenext.bastion.entity.BastionGuardianData;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * 攻城道具：分为爆破与破甲两种，用于打击基地。
 * <p>
 * 设计目标：
 * <ul>
 *     <li>爆破：仅对基地结构方块生效，直接破坏目标，消耗自身。</li>
 *     <li>破甲：为附近基地守卫施加虚弱与缓慢，降低短时间战斗力。</li>
 *     <li>只在服务端执行核心逻辑，避免客户端崩溃。</li>
 *     <li>行长不超过 120，注释中文且详细。</li>
 * </ul>
 * </p>
 */
public class BastionSiegeItem extends Item {

    /** 爆破最大检测距离（射线长度，方块单位）。 */
    private static final double BREACH_RANGE = 20.0d;

    /** 破甲半径（立方体包围盒半径，方块单位）。 */
    private static final double ARMOR_PIERCE_RADIUS = 16.0d;

    /** 用于方块中心偏移的常量。 */
    private static final double BLOCK_CENTER_OFFSET = 0.5d;

    /** 成功爆破时的音量。 */
    private static final float BREACH_SOUND_VOLUME = 0.8f;

    /** 破甲提示音音量。 */
    private static final float ARMOR_PIERCE_SOUND_VOLUME = 0.8f;

    /** 破甲效果时长（tick，约 10 秒）。 */
    private static final int ARMOR_PIERCE_DURATION = 200;

    /** 虚弱等级（0 基础，1 表示虚弱 II）。 */
    private static final int ARMOR_PIERCE_WEAKNESS_AMPLIFIER = 1;

    /** 缓慢等级（0 表示缓慢 I）。 */
    private static final int ARMOR_PIERCE_SLOW_AMPLIFIER = 0;

    /** 基地相关方块集合，用于判定爆破目标合法性。 */
    private static final Set<Block> BASTION_BLOCKS = Set.of(
        BastionBlocks.BASTION_CORE.get(),
        BastionBlocks.BASTION_ANCHOR.get(),
        BastionBlocks.BASTION_NODE.get(),
        BastionBlocks.BASTION_ENERGY_NODE.get(),
        BastionBlocks.BASTION_AURA_NODE.get(),
        BastionBlocks.BASTION_GUARDIAN_HATCHERY.get(),
        BastionBlocks.BASTION_TURRET.get(),
        BastionBlocks.BASTION_TRAP.get(),
        BastionBlocks.BASTION_CHITIN_SHELL.get(),
        BastionBlocks.BASTION_ANTI_FIRE_SHELL.get(),
        BastionBlocks.BASTION_ANTI_EXPLOSION_SHELL.get(),
        BastionBlocks.BASTION_REVERSAL_ARRAY.get()
    );

    /** 道具类型：爆破或破甲。 */
    public enum SiegeType {
        BREACH,
        ARMOR_PIERCE
    }

    private final SiegeType type;

    public BastionSiegeItem(SiegeType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        ServerLevel serverLevel = (ServerLevel) level;
        ServerPlayer serverPlayer = (ServerPlayer) player;

        return switch (type) {
            case BREACH -> handleBreach(serverLevel, serverPlayer, stack);
            case ARMOR_PIERCE -> handleArmorPierce(serverLevel, serverPlayer, stack);
        };
    }

    /**
     * 爆破逻辑：仅破坏基地相关方块，成功后消耗道具。
     */
    private InteractionResultHolder<ItemStack> handleBreach(
        ServerLevel level,
        ServerPlayer player,
        ItemStack stack
    ) {
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);

        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }

        BlockPos pos = hitResult.getBlockPos();

        if (player.distanceToSqr(
            pos.getX() + BLOCK_CENTER_OFFSET,
            pos.getY() + BLOCK_CENTER_OFFSET,
            pos.getZ() + BLOCK_CENTER_OFFSET
        )
            > BREACH_RANGE * BREACH_RANGE) {
            return InteractionResultHolder.pass(stack);
        }

        BlockState state = level.getBlockState(pos);
        if (!isBastionStructureBlock(state.getBlock())) {
            return InteractionResultHolder.pass(stack);
        }

        boolean destroyed = level.destroyBlock(pos, true, player);
        if (destroyed) {
            level.playSound(
                null,
                pos,
                SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.PLAYERS,
                BREACH_SOUND_VOLUME,
                1.0f
            );
            stack.shrink(1);
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    /**
     * 破甲逻辑：为附近守卫添加虚弱与缓慢，成功时消耗道具。
     */
    private InteractionResultHolder<ItemStack> handleArmorPierce(
        ServerLevel level,
        ServerPlayer player,
        ItemStack stack
    ) {
        AABB box = AABB.unitCubeFromLowerCorner(player.position())
            .inflate(ARMOR_PIERCE_RADIUS);

        int affected = 0;
        for (Mob mob : level.getEntitiesOfClass(Mob.class, box)) {
            if (!BastionGuardianData.isGuardian(mob)) {
                continue;
            }

            mob.addEffect(new MobEffectInstance(
                MobEffects.WEAKNESS,
                ARMOR_PIERCE_DURATION,
                ARMOR_PIERCE_WEAKNESS_AMPLIFIER,
                false,
                true
            ));
            mob.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                ARMOR_PIERCE_DURATION,
                ARMOR_PIERCE_SLOW_AMPLIFIER,
                false,
                true
            ));
            affected++;
        }

        if (affected > 0) {
            level.playSound(
                null,
                player.blockPosition(),
                SoundEvents.BEACON_DEACTIVATE,
                SoundSource.PLAYERS,
                1.0f,
                ARMOR_PIERCE_SOUND_VOLUME
            );
            stack.shrink(1);
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    /**
     * 判定方块是否属于基地结构。
     */
    private boolean isBastionStructureBlock(Block block) {
        return BASTION_BLOCKS.contains(block);
    }
}
