package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.tierThree;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * 三转犬魄蛊：主动【气味追踪】。
 * <p>
 * 这是一个“UI 辅助”型能力：开启后会在玩家视野中生成三条彩色粒子路径（类似药水粒子连成线），分别指向：
 * <ul>
 *   <li>红色：最近的敌对生物</li>
 *   <li>金色：最近的宝箱或稀有矿物</li>
 *   <li>绿色：最近的玩家</li>
 * </ul>
 * 限制与代价：
 * <ul>
 *   <li>持续消耗真元；真元不足时自动中断。</li>
 *   <li>默认无法追踪隐身单位；若目标被“发光”标记（例如鬼眼蛊的侦察效果），则允许追踪。</li>
 * </ul>
 * </p>
 */
public class QuanPuGuScentTrackingEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:quanpugu_active_scent_tracking";

    private static final String NBT_ACTIVE =
        "guzhenrenext_quanpugu_scent_tracking_active";

    private static final double DEFAULT_RADIUS = 20.0;
    private static final double DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND = 60.0;

    private static final double DEFAULT_PARTICLE_SPACING = 0.6;
    private static final float DEFAULT_PARTICLE_SCALE = 0.75F;
    private static final double DEFAULT_PARTICLE_JITTER = 0.05;

    private static final int DEFAULT_MAX_STEPS = 48;
    private static final int MAX_RADIUS_CLAMP = 48;

    private static final double HALF = 0.5;
    private static final double TREASURE_Y_OFFSET = 0.5;
    private static final double MIN_PATH_LENGTH = 0.01;

    private static final Vector3f COLOR_RED = new Vector3f(1.0F, 0.15F, 0.15F);
    private static final Vector3f COLOR_GOLD = new Vector3f(1.0F, 0.85F, 0.2F);
    private static final Vector3f COLOR_GREEN = new Vector3f(0.2F, 1.0F, 0.2F);

    private record PathSpec(
        DustParticleOptions particle,
        double spacing,
        double jitter,
        int maxSteps
    ) {}

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public boolean onActivate(
        LivingEntity user,
        ItemStack stack,
        NianTouData.Usage usageInfo
    ) {
        if (user.level().isClientSide()) {
            return false;
        }
        if (!(user instanceof Player player)) {
            return false;
        }
        if (!(user.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        boolean active = isActive(player);
        setActive(player, !active);

        if (!active) {
            // 立刻做一次资源校验：避免“开启但下一秒立刻中断”的体验割裂。
            double baseCost = getMetaDouble(
                usageInfo,
                "zhenyuan_base_cost_per_second",
                DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND
            );
            double realCost = ZhenYuanHelper.calculateGuCost(
                player,
                baseCost * DaoHenCalculator.calculateSelfMultiplier(
                    player,
                    DaoHenHelper.DaoType.NU_DAO
                )
            );
            if (realCost > 0 && !ZhenYuanHelper.hasEnough(player, realCost)) {
                setActive(player, false);
                player.sendSystemMessage(
                    Component.literal("真元不足，无法维持【气味追踪】。")
                );
                return false;
            }
            player.sendSystemMessage(Component.literal("已开启【气味追踪】。"));
        } else {
            player.sendSystemMessage(Component.literal("已关闭【气味追踪】。"));
        }

        // 主动开关类技能：这里返回 true 表示状态切换成功。
        // 持续消耗与粒子渲染在 onSecond 中完成。
        return true;
    }

    @Override
    public void onSecond(
        LivingEntity user,
        ItemStack stack,
        NianTouData.Usage usageInfo
    ) {
        if (user.level().isClientSide()) {
            return;
        }
        if (!(user instanceof Player player)) {
            return;
        }
        if (!isActive(player)) {
            return;
        }
        if (!(user.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            player,
            DaoHenHelper.DaoType.NU_DAO
        );

        double baseCost = getMetaDouble(
            usageInfo,
            "zhenyuan_base_cost_per_second",
            DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND
        );
        double realCost = ZhenYuanHelper.calculateGuCost(
            player,
            baseCost * selfMultiplier
        );
        if (realCost > 0 && !ZhenYuanHelper.hasEnough(player, realCost)) {
            setActive(player, false);
            player.sendSystemMessage(
                Component.literal("真元不足，【气味追踪】已中断。")
            );
            return;
        }
        if (realCost > 0) {
            ZhenYuanHelper.modify(player, -realCost);
        }

        double baseRadius = getMetaDouble(usageInfo, "radius", DEFAULT_RADIUS);
        double radius = baseRadius * selfMultiplier;

        Vec3 origin = player.getEyePosition(1.0F);
        AABB area = player.getBoundingBox().inflate(radius);

        LivingEntity nearestEnemy = findNearestEnemy(serverLevel, player, area);
        BlockPos nearestTreasure = findNearestTreasureOrOre(
            serverLevel,
            player.blockPosition(),
            Mth.clamp((int) Math.ceil(radius), 1, MAX_RADIUS_CLAMP)
        );
        Player nearestPlayer = findNearestPlayer(serverLevel, player, area);

        double spacing = getMetaDouble(
            usageInfo,
            "particle_spacing",
            DEFAULT_PARTICLE_SPACING
        );
        double jitter = getMetaDouble(
            usageInfo,
            "particle_jitter",
            DEFAULT_PARTICLE_JITTER
        );
        int maxSteps = getMetaInt(usageInfo, "max_steps", DEFAULT_MAX_STEPS);

        float scale = (float) getMetaDouble(
            usageInfo,
            "particle_scale",
            DEFAULT_PARTICLE_SCALE
        );
        PathSpec redPath = new PathSpec(
            new DustParticleOptions(COLOR_RED, scale),
            spacing,
            jitter,
            maxSteps
        );
        PathSpec goldPath = new PathSpec(
            new DustParticleOptions(COLOR_GOLD, scale),
            spacing,
            jitter,
            maxSteps
        );
        PathSpec greenPath = new PathSpec(
            new DustParticleOptions(COLOR_GREEN, scale),
            spacing,
            jitter,
            maxSteps
        );

        if (nearestEnemy != null) {
            spawnPath(
                serverLevel,
                serverPlayer,
                origin,
                nearestEnemy.getEyePosition(1.0F),
                redPath
            );
        }

        if (nearestTreasure != null) {
            Vec3 target = Vec3
                .atCenterOf(nearestTreasure)
                .add(0, TREASURE_Y_OFFSET, 0);
            spawnPath(
                serverLevel,
                serverPlayer,
                origin,
                target,
                goldPath
            );
        }

        if (nearestPlayer != null) {
            spawnPath(
                serverLevel,
                serverPlayer,
                origin,
                nearestPlayer.getEyePosition(1.0F),
                greenPath
            );
        }
    }

    @Override
    public void onUnequip(
        LivingEntity user,
        ItemStack stack,
        NianTouData.Usage usageInfo
    ) {
        if (user instanceof Player player) {
            setActive(player, false);
        }
    }

    private static boolean isActive(Player player) {
        return player.getPersistentData().getBoolean(NBT_ACTIVE);
    }

    private static void setActive(Player player, boolean active) {
        player.getPersistentData().putBoolean(NBT_ACTIVE, active);
    }

    private static LivingEntity findNearestEnemy(
        ServerLevel level,
        Player player,
        AABB area
    ) {
        List<LivingEntity> entities = level.getEntitiesOfClass(
            LivingEntity.class,
            area,
            e ->
                e.isAlive()
                    && e != player
                    && e instanceof Enemy
                    && isTrackableEntity(e)
        );
        return entities
            .stream()
            .min(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
            .orElse(null);
    }

    private static Player findNearestPlayer(
        ServerLevel level,
        Player player,
        AABB area
    ) {
        List<Player> players = level.getEntitiesOfClass(
            Player.class,
            area,
            p -> p.isAlive() && p != player && isTrackableEntity(p)
        );
        return players
            .stream()
            .min(Comparator.comparingDouble(p -> p.distanceToSqr(player)))
            .orElse(null);
    }

    private static boolean isTrackableEntity(LivingEntity entity) {
        // 默认：隐身不可追踪；若已被发光标记（例如鬼眼蛊），则允许追踪。
        if (entity.hasEffect(MobEffects.INVISIBILITY)) {
            return entity.hasEffect(MobEffects.GLOWING);
        }
        return true;
    }

    private static BlockPos findNearestTreasureOrOre(
        ServerLevel level,
        BlockPos center,
        int maxRadius
    ) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int r = 1; r <= maxRadius; r++) {
            int min = -r;
            int max = r;

            for (int dx = min; dx <= max; dx++) {
                for (int dy = min; dy <= max; dy++) {
                    for (int dz = min; dz <= max; dz++) {
                        // 只扫描“外壳”，避免每秒扫描完整立方体造成不必要的性能开销。
                        int chebyshev =
                            Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
                        if (chebyshev != r) {
                            continue;
                        }

                        mutable.set(
                            center.getX() + dx,
                            center.getY() + dy,
                            center.getZ() + dz
                        );

                        if (!level.isInWorldBounds(mutable)) {
                            continue;
                        }

                        BlockState state = level.getBlockState(mutable);
                        if (isTreasureBlock(state) || isRareOreBlock(state)) {
                            return mutable.immutable();
                        }
                    }
                }
            }
        }

        return null;
    }

    private static boolean isTreasureBlock(BlockState state) {
        // 只追踪“宝箱类”容器，避免把炉子/工作台等也标记为“宝藏”。
        return state.is(Blocks.CHEST)
            || state.is(Blocks.TRAPPED_CHEST)
            || state.is(Blocks.BARREL)
            || state.is(Blocks.ENDER_CHEST);
    }

    private static boolean isRareOreBlock(BlockState state) {
        // “稀有矿物”偏向高价值资源，避免泛化到所有矿石导致信息过载。
        return state.is(Blocks.DIAMOND_ORE)
            || state.is(Blocks.DEEPSLATE_DIAMOND_ORE)
            || state.is(Blocks.EMERALD_ORE)
            || state.is(Blocks.DEEPSLATE_EMERALD_ORE)
            || state.is(Blocks.ANCIENT_DEBRIS);
    }

    private static void spawnPath(
        ServerLevel level,
        ServerPlayer receiver,
        Vec3 from,
        Vec3 to,
        PathSpec spec
    ) {
        Vec3 delta = to.subtract(from);
        double length = delta.length();
        if (length < MIN_PATH_LENGTH) {
            return;
        }

        int steps = Mth.clamp(
            (int) Math.ceil(length / spec.spacing()),
            1,
            spec.maxSteps()
        );
        Vec3 direction = delta.scale(1.0 / steps);

        for (int i = 0; i <= steps; i++) {
            Vec3 pos = from.add(direction.scale(i));

            double jx = (level.random.nextDouble() - HALF) * spec.jitter();
            double jy = (level.random.nextDouble() - HALF) * spec.jitter();
            double jz = (level.random.nextDouble() - HALF) * spec.jitter();

            level.sendParticles(
                receiver,
                spec.particle(),
                true,
                pos.x + jx,
                pos.y + jy,
                pos.z + jz,
                1,
                0,
                0,
                0,
                0
            );
        }
    }

    private static double getMetaDouble(
        NianTouData.Usage usage,
        String key,
        double defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Double.parseDouble(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {
                // 念头配置属于数据驱动：无效值回退默认，避免影响服务器稳定性。
            }
        }
        return defaultValue;
    }

    private static int getMetaInt(
        NianTouData.Usage usage,
        String key,
        int defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Integer.parseInt(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {
                // 念头配置属于数据驱动：无效值回退默认，避免影响服务器稳定性。
            }
        }
        return defaultValue;
    }
}
