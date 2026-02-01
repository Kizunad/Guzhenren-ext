package com.Kizunad.guzhenrenext.bastion.item;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.BastionState;
import java.util.Comparator;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 侦查道具：右键后扫描附近基地并输出信息，可选标记目标。
 * <p>
 * 设计目标：
 * <ul>
 *     <li>仅在服务端执行核心逻辑，避免客户端侧崩溃。</li>
 *     <li>使用 BastionSavedData 搜索附近基地，默认半径 128 格。</li>
 *     <li>向玩家发送结构化消息：距离/方位、转数、状态、道途、威胁。</li>
 *     <li>命中时播放提示音，避免消息刷屏。</li>
 *     <li>可选标记：对准的实体添加发光效果，便于协同。</li>
 * </ul>
 * </p>
 */
public class BastionScoutItem extends Item {

    /** 默认扫描半径（方块单位）。 */
    private static final int DEFAULT_SCAN_RADIUS = 128;

    /** 发光效果时长（tick，约 10 秒）。 */
    private static final int GLOW_DURATION_TICKS = 200;

    /** 发光效果等级。 */
    private static final int GLOW_AMPLIFIER = 0;

    /** 视线标记最大距离（方块单位）。 */
    private static final double MARK_RANGE = 32.0d;

    /** 视线判定余量。 */
    private static final double HIT_EPSILON = 1.0e-5d;

    /** 视线包围盒扩张量。 */
    private static final double TARGET_INFLATE = 0.5d;

    /** 视线射线判定阈值（平方距离）。 */
    private static final double TARGET_HIT_THRESHOLD = 0.25d;

    /** 目标中心偏移（方块中心）。 */
    private static final double POSITION_CENTER_OFFSET = 0.5d;

    /** 前向角阈值。 */
    private static final double ANGLE_FRONT = 22.5d;

    /** 侧前角阈值。 */
    private static final double ANGLE_FRONT_SIDE = 67.5d;

    /** 侧向角阈值。 */
    private static final double ANGLE_SIDE = 112.5d;

    /** 侧后角阈值。 */
    private static final double ANGLE_BACK_SIDE = 157.5d;

    /** 上下判定高度差。 */
    private static final double VERTICAL_THRESHOLD = 2.0d;

    /** 成功提示音音量。 */
    private static final float PING_VOLUME = 1.0f;

    /** 成功提示音音高。 */
    private static final float PING_PITCH = 1.2f;

    /** 失败提示音音量。 */
    private static final float FAIL_VOLUME = 0.8f;

    /** 失败提示音音高。 */
    private static final float FAIL_PITCH = 0.8f;

    public BastionScoutItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        ServerLevel serverLevel = (ServerLevel) level;
        ServerPlayer serverPlayer = (ServerPlayer) player;
        BastionSavedData savedData = BastionSavedData.get(serverLevel);
        long gameTime = serverLevel.getGameTime();

        BastionData nearest = findNearestActiveBastion(serverPlayer, savedData, gameTime);
        if (nearest == null) {
            serverPlayer.displayClientMessage(Component.translatable("message.guzhenrenext.bastion_scout.none"), true);
            playFailSound(serverLevel, serverPlayer);
            return InteractionResultHolder.success(stack);
        }

        sendBastionInfo(serverPlayer, nearest, gameTime);
        applyGlowToLookTarget(serverLevel, serverPlayer);
        playPingSound(serverLevel, serverPlayer);

        return InteractionResultHolder.success(stack);
    }

    /**
     * 寻找指定玩家附近最近且有效的基地。
     *
     * @param player    玩家
     * @param data      存档数据
     * @param gameTime  当前游戏时间
     * @return 最近的基地，若无则返回 null
     */
    @Nullable
    private BastionData findNearestActiveBastion(ServerPlayer player, BastionSavedData data, long gameTime) {
        BlockPos playerPos = player.blockPosition();

        return data.getAllBastions().stream()
            .filter(bastion -> bastion.dimension() == player.serverLevel().dimension())
            .filter(bastion -> bastion.state() != BastionState.DESTROYED)
            .filter(bastion -> playerPos.distSqr(bastion.corePos()) <= (long) DEFAULT_SCAN_RADIUS * DEFAULT_SCAN_RADIUS)
            .min(Comparator.comparingDouble(bastion -> playerPos.distSqr(bastion.corePos())))
            .orElse(null);
    }

    /**
     * 将基地信息发送给玩家。
     *
     * @param player   玩家
     * @param bastion  基地数据
     * @param gameTime 当前游戏时间
     */
    private void sendBastionInfo(ServerPlayer player, BastionData bastion, long gameTime) {
        BlockPos playerPos = player.blockPosition();
        BlockPos corePos = bastion.corePos();
        double distance = Math.sqrt(playerPos.distSqr(corePos));
        String direction = formatDirection(player, corePos);
        BastionState effectiveState = bastion.getEffectiveState(gameTime);

        MutableComponent title = Component.translatable("message.guzhenrenext.bastion_scout.title");
        MutableComponent line1 = Component.translatable(
            "message.guzhenrenext.bastion_scout.pos",
            corePos.getX(),
            corePos.getY(),
            corePos.getZ()
        ).withStyle(ChatFormatting.GOLD);
        MutableComponent line2 = Component.translatable(
            "message.guzhenrenext.bastion_scout.dir",
            direction,
            String.format(Locale.ROOT, "%.1f", distance)
        ).withStyle(ChatFormatting.YELLOW);
        MutableComponent line3 = Component.translatable(
            "message.guzhenrenext.bastion_scout.state",
            effectiveState.getSerializedName()
        ).withStyle(formatStateColor(effectiveState));
        MutableComponent line4 = Component.translatable(
            "message.guzhenrenext.bastion_scout.tier",
            bastion.tier()
        ).withStyle(ChatFormatting.AQUA);
        MutableComponent line5 = Component.translatable(
            "message.guzhenrenext.bastion_scout.dao",
            bastion.primaryDao().name()
        ).withStyle(ChatFormatting.LIGHT_PURPLE);
        MutableComponent line6 = Component.translatable(
            "message.guzhenrenext.bastion_scout.threat",
            bastion.threatMeter()
        ).withStyle(ChatFormatting.RED);

        player.displayClientMessage(title, true);
        player.displayClientMessage(line1, true);
        player.displayClientMessage(line2, true);
        player.displayClientMessage(line3, true);
        player.displayClientMessage(line4, true);
        player.displayClientMessage(line5, true);
        player.displayClientMessage(line6, true);
    }

    /**
     * 对玩家视线目标应用发光效果，用于标记。
     *
     * @param level  服务器世界
     * @param player 玩家
     */
    private void applyGlowToLookTarget(ServerLevel level, ServerPlayer player) {
        LivingEntity target = findLookedAtEntity(level, player);
        if (target == null) {
            return;
        }

        target.addEffect(new MobEffectInstance(
            MobEffects.GLOWING,
            GLOW_DURATION_TICKS,
            GLOW_AMPLIFIER,
            false,
            true
        ));
    }

    /**
     * 尝试找到玩家正在凝视的实体（有限距离）。
     *
     * @param level  世界
     * @param player 玩家
     * @return 实体或 null
     */
    @Nullable
    private LivingEntity findLookedAtEntity(ServerLevel level, ServerPlayer player) {
        double reach = MARK_RANGE;
        Vec3 eye = player.getEyePosition();
        Vec3 view = player.getViewVector(1.0f);
        Vec3 end = eye.add(view.scale(reach));
        BlockHitResult blockHit = level.clip(new ClipContext(
            eye,
            end,
            ClipContext.Block.OUTLINE,
            ClipContext.Fluid.NONE,
            player
        ));
        double maxDistance = blockHit.getType() == HitResult.Type.BLOCK
            ? blockHit.getLocation().distanceTo(eye)
            : reach;
        Vec3 extendedEnd = eye.add(view.scale(maxDistance));

        LivingEntity nearest = null;
        double nearestDist = maxDistance;
        for (LivingEntity entity : level.getEntitiesOfClass(
            LivingEntity.class,
            player.getBoundingBox().expandTowards(view.scale(reach)).inflate(TARGET_INFLATE)
        )) {
            if (entity.getType() == EntityType.PLAYER && entity == player) {
                continue;
            }
            Vec3 toEntity = entity.getEyePosition().subtract(eye);
            double projection = toEntity.dot(view);
            if (projection < 0 || projection > maxDistance) {
                continue;
            }
            Vec3 closest = eye.add(view.scale(projection));
            double distance = entity.getBoundingBox().inflate(TARGET_INFLATE).distanceToSqr(closest);
            if (distance <= TARGET_HIT_THRESHOLD && projection < nearestDist) {
                nearestDist = projection;
                nearest = entity;
            }
        }
        return nearest;
    }

    /**
     * 方向格式化（八方向 + 上下）。
     */
    private String formatDirection(Player player, BlockPos target) {
        Vec3 look = player.getViewVector(1.0f);
        double dx = target.getX() + POSITION_CENTER_OFFSET - player.getX();
        double dy = target.getY() + POSITION_CENTER_OFFSET - player.getEyeY();
        double dz = target.getZ() + POSITION_CENTER_OFFSET - player.getZ();
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        double dot = (dx * look.x + dz * look.z) / Math.max(HIT_EPSILON, horizontalDist);
        double angle = Math.toDegrees(Math.acos(Mth.clamp(dot, -1.0, 1.0)));

        String horizontal;
        if (angle < ANGLE_FRONT) {
            horizontal = "前方";
        } else if (angle < ANGLE_FRONT_SIDE) {
            horizontal = dz >= 0 ? "右前" : "左前";
        } else if (angle < ANGLE_SIDE) {
            horizontal = dz >= 0 ? "右侧" : "左侧";
        } else if (angle < ANGLE_BACK_SIDE) {
            horizontal = dz >= 0 ? "右后" : "左后";
        } else {
            horizontal = "后方";
        }

        String vertical;
        if (Math.abs(dy) < VERTICAL_THRESHOLD) {
            vertical = "水平";
        } else {
            vertical = dy > 0 ? "上方" : "下方";
        }

        return horizontal + "/" + vertical;
    }

    /**
     * 根据基地状态返回颜色。
     */
    private ChatFormatting formatStateColor(BastionState state) {
        return switch (state) {
            case ACTIVE -> ChatFormatting.GREEN;
            case SEALED -> ChatFormatting.BLUE;
            case DESTROYED -> ChatFormatting.DARK_GRAY;
            default -> ChatFormatting.WHITE;
        };
    }

    /**
     * 播放成功提示音。
     */
    private void playPingSound(ServerLevel level, ServerPlayer player) {
        level.playSound(
            null,
            player.blockPosition(),
            SoundEvents.NOTE_BLOCK_PLING.value(),
            SoundSource.PLAYERS,
            PING_VOLUME,
            PING_PITCH
        );
    }

    /**
     * 播放失败提示音。
     */
    private void playFailSound(ServerLevel level, ServerPlayer player) {
        level.playSound(
            null,
            player.blockPosition(),
            SoundEvents.VILLAGER_NO,
            SoundSource.PLAYERS,
            FAIL_VOLUME,
            FAIL_PITCH
        );
    }
}
