package com.Kizunad.guzhenrenext.network;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoData;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.inventory.KongqiaoInventory;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierThree.LingHunGuSkyStepEffect;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouDataManager;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouUnlockChecker;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 羚魂蛊踏空：二段跳请求包（客户端 -> 服务端）。
 * <p>
 * 客户端只负责提交“按下跳跃键”的请求；服务端在此处进行权威校验：
 * 1) 玩家空窍内确实存在并解锁了踏空用途；
 * 2) 该被动在 TweakConfig 中未被关闭；
 * 3) 当前腾空状态允许执行二段跳，且未消耗过本次二段跳。
 * </p>
 */
public record ServerboundLingHunGuDoubleJumpPayload() implements CustomPacketPayload {

    public static final Type<ServerboundLingHunGuDoubleJumpPayload> TYPE =
        new Type<>(
            ResourceLocation.fromNamespaceAndPath(
                GuzhenrenExt.MODID,
                "linghungu_double_jump"
            )
        );

    public static final StreamCodec<ByteBuf, ServerboundLingHunGuDoubleJumpPayload> STREAM_CODEC =
        StreamCodec.unit(new ServerboundLingHunGuDoubleJumpPayload());

    private static final double DEFAULT_JUMP_Y_VELOCITY = 0.42;
    private static final double DEFAULT_FORWARD_BOOST = 0.05;
    private static final int DEFAULT_PARTICLE_COUNT = 10;
    private static final double DEFAULT_PARTICLE_SPREAD = 0.15;
    private static final double PARTICLE_Y_OFFSET = 0.1;
    private static final double PARTICLE_SPEED = 0.01;
    private static final double FORWARD_EPSILON_SQR = 0.0001;

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
        final ServerboundLingHunGuDoubleJumpPayload payload,
        final IPayloadContext context
    ) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!isSkyStepEnabled(player)) {
                return;
            }
            if (!canDoubleJump(player)) {
                return;
            }

            final NianTouData.Usage usageInfo = findUsageInfo(
                player,
                LingHunGuSkyStepEffect.USAGE_ID
            );
            if (usageInfo == null) {
                return;
            }

            final double yVelocity = getMetaDouble(
                usageInfo,
                "double_jump_y_velocity",
                DEFAULT_JUMP_Y_VELOCITY
            );
            final double forwardBoost = getMetaDouble(
                usageInfo,
                "double_jump_forward_boost",
                DEFAULT_FORWARD_BOOST
            );
            applyDoubleJump(player, yVelocity, forwardBoost);
            LingHunGuSkyStepEffect.setDoubleJumpUsed(player, true);

            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                    ParticleTypes.CLOUD,
                    player.getX(),
                    player.getY() + PARTICLE_Y_OFFSET,
                    player.getZ(),
                    getMetaInt(
                        usageInfo,
                        "double_jump_particle_count",
                        DEFAULT_PARTICLE_COUNT
                    ),
                    DEFAULT_PARTICLE_SPREAD,
                    DEFAULT_PARTICLE_SPREAD,
                    DEFAULT_PARTICLE_SPREAD,
                    PARTICLE_SPEED
                );
            }
        });
    }

    private static boolean isSkyStepEnabled(final ServerPlayer player) {
        final TweakConfig config = KongqiaoAttachments.getTweakConfig(player);
        if (config != null && !config.isPassiveEnabled(LingHunGuSkyStepEffect.USAGE_ID)) {
            return false;
        }
        return findUsageInfo(player, LingHunGuSkyStepEffect.USAGE_ID) != null;
    }

    private static boolean canDoubleJump(final Player player) {
        if (player.onGround()
            || player.isInWaterOrBubble()
            || player.onClimbable()
            || player.isFallFlying()
            || player.getAbilities().flying
        ) {
            return false;
        }
        return !LingHunGuSkyStepEffect.isDoubleJumpUsed(player);
    }

    private static void applyDoubleJump(
        final Player player,
        final double yVelocity,
        final double forwardBoost
    ) {
        final Vec3 motion = player.getDeltaMovement();
        final double newY = Math.max(motion.y, yVelocity);

        Vec3 look = player.getLookAngle();
        Vec3 forward = new Vec3(look.x, 0.0, look.z);
        if (forward.lengthSqr() > FORWARD_EPSILON_SQR) {
            forward = forward.normalize().scale(forwardBoost);
        } else {
            forward = Vec3.ZERO;
        }

        player.setDeltaMovement(
            motion.x + forward.x,
            newY,
            motion.z + forward.z
        );
        player.fallDistance = 0.0F;
        player.hasImpulse = true;
    }

    private static NianTouData.Usage findUsageInfo(
        final ServerPlayer player,
        final String usageId
    ) {
        final KongqiaoData data = KongqiaoAttachments.getData(player);
        if (data == null) {
            return null;
        }
        final KongqiaoInventory inventory = data.getKongqiaoInventory();
        if (inventory == null) {
            return null;
        }

        final int unlockedSlots = inventory.getSettings().getUnlockedSlots();
        for (int i = 0; i < unlockedSlots; i++) {
            final ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            final NianTouData itemData = NianTouDataManager.getData(stack);
            if (itemData == null || itemData.usages() == null) {
                continue;
            }
            for (NianTouData.Usage usage : itemData.usages()) {
                if (!usageId.equals(usage.usageID())) {
                    continue;
                }
                if (!NianTouUnlockChecker.isUsageUnlocked(player, stack, usageId)) {
                    continue;
                }
                return usage;
            }
        }
        return null;
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

    private static int getMetaInt(
        final NianTouData.Usage usage,
        final String key,
        final int defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Integer.parseInt(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }
}
