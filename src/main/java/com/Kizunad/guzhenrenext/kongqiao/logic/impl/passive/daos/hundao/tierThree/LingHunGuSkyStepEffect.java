package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierThree;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 羚魂蛊：被动【踏空】。
 * <p>
 * 设计目标：
 * 1) 二段跳：允许玩家在空中追加一次跳跃（由客户端输入触发，服务端权威执行）。
 * 2) 地形适应：在雪地/冰面/灵魂沙上行走更“平”，体感上不被拖慢与滑行。
 * </p>
 * <p>
 * 风道道痕（daohen_fengdao）用于提升“身法”相关的强度（跳跃与地形适应均属于自身效果）。
 * </p>
 */
public class LingHunGuSkyStepEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:linghungu_passive_sky_step";

    /**
     * 玩家 PersistentData：是否已消耗过本次腾空中的“二段跳”。
     * <p>
     * 只在玩家有该用途解锁且被动启用时维护，避免污染全局逻辑。
     * </p>
     */
    public static final String NBT_DOUBLE_JUMP_USED = "LingHunGuDoubleJumpUsed";

    private static final ResourceLocation SURFACE_SPEED_MODIFIER_ID =
        ResourceLocation.parse("guzhenrenext:linghungu_surface_speed");

    private static final double DEFAULT_SLOW_SURFACE_SPEED_BONUS = 1.5;
    private static final double DEFAULT_ICE_SLIP_DAMPEN = 0.65;
    private static final double MIN_ICE_DAMPEN = 0.20;
    private static final double MAX_ICE_DAMPEN = 0.90;

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public void onTick(LivingEntity user, ItemStack stack, NianTouData.Usage usageInfo) {
        if (user.level().isClientSide()) {
            return;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(user);
        if (config != null && !config.isPassiveEnabled(USAGE_ID)) {
            removeSurfaceSpeedModifier(user);
            resetDoubleJump(user);
            return;
        }

        if (!(user instanceof Player player)) {
            return;
        }

        // 1) 触地/入水/攀爬/鞘翅飞行时刷新二段跳次数
        if (shouldResetDoubleJump(player)) {
            setDoubleJumpUsed(player, false);
        }

        // 2) 地形适应：灵魂沙/雪地等减速表面给予速度补偿；冰面给予滑行衰减
        if (!player.onGround()) {
            removeSurfaceSpeedModifier(user);
            return;
        }

        final BlockPos onPos = player.getOnPos();
        final BlockState onState = player.level().getBlockState(onPos);
        final double fengDaoMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            player,
            DaoHenHelper.DaoType.FENG_DAO
        );

        if (isSlowSurface(onState)) {
            final double bonus = getMetaDouble(
                usageInfo,
                "slow_surface_speed_bonus",
                DEFAULT_SLOW_SURFACE_SPEED_BONUS
            );
            applySurfaceSpeedModifier(user, bonus * fengDaoMultiplier);
        } else {
            removeSurfaceSpeedModifier(user);
        }

        if (isIceSurface(onState)) {
            final double baseDampen = getMetaDouble(
                usageInfo,
                "ice_slip_dampen",
                DEFAULT_ICE_SLIP_DAMPEN
            );
            final double dampen = clamp(
                baseDampen / Math.max(1.0, fengDaoMultiplier),
                MIN_ICE_DAMPEN,
                MAX_ICE_DAMPEN
            );
            final Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x * dampen, motion.y, motion.z * dampen);
        }
    }

    @Override
    public void onUnequip(LivingEntity user, ItemStack stack, NianTouData.Usage usageInfo) {
        removeSurfaceSpeedModifier(user);
        resetDoubleJump(user);
    }

    private static boolean shouldResetDoubleJump(final Player player) {
        return player.onGround()
            || player.isInWaterOrBubble()
            || player.onClimbable()
            || player.isFallFlying()
            || player.getAbilities().flying;
    }

    private static boolean isSlowSurface(final BlockState state) {
        // “雪地”在不同包中可能对应雪层/细雪；这里选择对实际会拖慢的方块做补偿。
        return state.is(Blocks.SOUL_SAND)
            || state.is(Blocks.SOUL_SOIL)
            || state.is(Blocks.POWDER_SNOW);
    }

    private static boolean isIceSurface(final BlockState state) {
        return state.is(Blocks.ICE)
            || state.is(Blocks.PACKED_ICE)
            || state.is(Blocks.BLUE_ICE)
            || state.is(Blocks.FROSTED_ICE);
    }

    private static void applySurfaceSpeedModifier(final LivingEntity user, final double amount) {
        final AttributeInstance attr = user.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr == null) {
            return;
        }
        final AttributeModifier existing = attr.getModifier(SURFACE_SPEED_MODIFIER_ID);
        if (existing == null || Double.compare(existing.amount(), amount) != 0) {
            if (existing != null) {
                attr.removeModifier(SURFACE_SPEED_MODIFIER_ID);
            }
            attr.addTransientModifier(
                new AttributeModifier(
                    SURFACE_SPEED_MODIFIER_ID,
                    amount,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                )
            );
        }
    }

    private static void removeSurfaceSpeedModifier(final LivingEntity user) {
        final AttributeInstance attr = user.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr == null) {
            return;
        }
        if (attr.getModifier(SURFACE_SPEED_MODIFIER_ID) != null) {
            attr.removeModifier(SURFACE_SPEED_MODIFIER_ID);
        }
    }

    private static void resetDoubleJump(final LivingEntity user) {
        if (user instanceof Player player) {
            setDoubleJumpUsed(player, false);
        }
    }

    public static boolean isDoubleJumpUsed(final Player player) {
        return player.getPersistentData().getBoolean(NBT_DOUBLE_JUMP_USED);
    }

    public static void setDoubleJumpUsed(final Player player, final boolean used) {
        player.getPersistentData().putBoolean(NBT_DOUBLE_JUMP_USED, used);
    }

    private static double clamp(final double value, final double min, final double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private static double getMetaDouble(
        final NianTouData.Usage usage,
        final String key,
        final double defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Double.parseDouble(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }
}
