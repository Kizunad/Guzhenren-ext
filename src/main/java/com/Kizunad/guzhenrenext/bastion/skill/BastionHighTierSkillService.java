package com.Kizunad.guzhenrenext.bastion.skill;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeConfig;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeManager;
import com.Kizunad.guzhenrenext.bastion.entity.BastionGuardianData;
import com.Kizunad.guzhenrenext.bastion.service.BastionTalentEffectService;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoDataManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

/**
 * 基地高转技能/特效运行服务。
 * <p>
 * 设计目标：复用 {@link ShazhaoEffectRegistry} 中的实现，并根据 BastionTypeConfig 的
 * high_tier.skills / special_effects 配置，驱动对应的运行时效果。
 * </p>
 * <p>
 * 约束：
 * <ul>
 *   <li>仅在服务端执行。</li>
 *   <li>被动效果：当玩家在基地领域内时，每秒调用一次。</li>
 *   <li>主动技能：在 FULL tick 中以固定节流触发（每基地每秒最多一次）。</li>
 * </ul>
 * </p>
 */
public final class BastionHighTierSkillService {

    /**
     * 技能冷却键：基地 ID + 技能 ID。
     */
    private record SkillKey(UUID bastionId, ResourceLocation skillId) {
    }

    private BastionHighTierSkillService() {
    }

    /**
     * 主动技能触发节流：每个基地每秒最多触发一次。
     */
    private static final long ACTIVE_TRIGGER_INTERVAL_TICKS = 20L;

    /**
     * 记录每个基地最近一次主动技能触发的游戏时间。
     */
    private static final Map<UUID, Long> LAST_ACTIVE_TRIGGER_TICK =
        new ConcurrentHashMap<>();

    /**
     * 被动效果触发节流：同一基地在同一 tick 内仅执行一次。
     * <p>
     * 注意：该节流用于避免多个玩家同时在领域内时重复执行“基地被动效果”。
     * </p>
     */
    private static final Map<UUID, Long> LAST_PASSIVE_TRIGGER_TICK =
        new ConcurrentHashMap<>();

    /**
     * 记录每个基地每个技能的冷却截止 tick。
     */
    private static final Map<SkillKey, Long> COOLDOWN_UNTIL_TICK =
        new ConcurrentHashMap<>();

    /**
     * 运行被动效果：仅对“玩家在领域内”的场景调用。
     */
    public static void runPassiveEffects(
        final ServerLevel level,
        final BastionData bastion,
        final long gameTime,
        final List<ServerPlayer> playersInDomain
    ) {
        if (level == null || bastion == null) {
            return;
        }
        if (playersInDomain == null || playersInDomain.isEmpty()) {
            return;
        }

        final BastionTypeConfig typeConfig = BastionTypeManager.getOrDefault(
            bastion.bastionType()
        );
        if (typeConfig.highTier().isEmpty()) {
            return;
        }
        final BastionTypeConfig.HighTierConfig highTier = typeConfig.highTier().get();

        final double bonusMultiplier = resolveBonusMultiplier(highTier, bastion.tier(), bastion);

        // 收集守卫（用于某些特效：例如命运丝线强化守卫）
        final List<Mob> guardians = collectGuardians(level, bastion);

        final BastionSkillContext baseContext = new BastionSkillContext(
            level,
            bastion,
            gameTime,
            bonusMultiplier,
            playersInDomain,
            guardians,
            Map.of(),
            RandomSource.create()
        );

        // 1) 先跑 special_effects（被动）
        for (String effectIdText : highTier.specialEffects()) {
            if (effectIdText == null || effectIdText.isBlank()) {
                continue;
            }
            runBastionSecond(baseContext, effectIdText, Map.of());
        }

        // 2) 再跑 high_tier.skills 里声明的 passive
        for (BastionTypeConfig.SkillEntry entry : highTier.getSkillsForTier(bastion.tier())) {
            if (!isPassive(entry)) {
                continue;
            }
            runBastionSecond(baseContext, entry.skillId(), entry.metadata());
        }
    }

    /**
     * 运行被动效果（自动收集领域内玩家）。
     * <p>
     * 该方法包含“每基地每 tick 仅执行一次”的节流逻辑，建议外部调用优先使用此入口。
     * </p>
     */
    public static void runPassiveEffects(
        final ServerLevel level,
        final BastionData bastion,
        final long gameTime
    ) {
        if (level == null || bastion == null) {
            return;
        }

        final Long lastTick = LAST_PASSIVE_TRIGGER_TICK.get(bastion.id());
        if (lastTick != null && lastTick == gameTime) {
            return;
        }

        final List<ServerPlayer> targets = collectPlayersInDomain(level, bastion);
        if (targets.isEmpty()) {
            return;
        }

        LAST_PASSIVE_TRIGGER_TICK.put(bastion.id(), gameTime);
        runPassiveEffects(level, bastion, gameTime, targets);
    }

    /**
     * 运行主动技能：由 BastionTicker 在 FULL tick 中驱动。
     */
    public static void runActiveSkills(
        final ServerLevel level,
        final BastionData bastion,
        final long gameTime
    ) {
        if (level == null || bastion == null) {
            return;
        }

        final BastionTypeConfig typeConfig = BastionTypeManager.getOrDefault(
            bastion.bastionType()
        );
        if (typeConfig.highTier().isEmpty()) {
            return;
        }

        // 主动技能节流：每基地每秒最多触发一次
        final Long lastTick = LAST_ACTIVE_TRIGGER_TICK.get(bastion.id());
        if (lastTick != null && gameTime - lastTick < ACTIVE_TRIGGER_INTERVAL_TICKS) {
            return;
        }
        LAST_ACTIVE_TRIGGER_TICK.put(bastion.id(), gameTime);

        final BastionTypeConfig.HighTierConfig highTier = typeConfig.highTier().get();
        final double bonusMultiplier = resolveBonusMultiplier(highTier, bastion.tier(), bastion);

        final List<ServerPlayer> targets = collectPlayersInDomain(level, bastion);
        if (targets.isEmpty()) {
            return;
        }
        final List<Mob> guardians = collectGuardians(level, bastion);

        // 从高到低扫描 active 技能（优先触发最高阶）
        final List<BastionTypeConfig.SkillEntry> skills =
            new ArrayList<>(highTier.getSkillsForTier(bastion.tier()));
        skills.sort((a, b) -> Integer.compare(b.minTier(), a.minTier()));

        for (BastionTypeConfig.SkillEntry entry : skills) {
            if (!isActive(entry)) {
                continue;
            }
            if (tryRunBastionActivate(level, bastion, gameTime, bonusMultiplier, targets, guardians, entry)) {
                break;
            }
        }
    }

    /**
     * 在基地移除时清理运行时状态。
     */
    public static void onBastionRemoved(final UUID bastionId) {
        if (bastionId == null) {
            return;
        }
        LAST_ACTIVE_TRIGGER_TICK.remove(bastionId);
        LAST_PASSIVE_TRIGGER_TICK.remove(bastionId);
        COOLDOWN_UNTIL_TICK.keySet().removeIf(key -> key.bastionId().equals(bastionId));
    }

    // ===== 内部：效果派发 =====

    private static void runBastionSecond(
        final BastionSkillContext baseContext,
        final String effectIdText,
        final Map<String, String> metadata
    ) {
        if (baseContext == null || baseContext.level() == null || baseContext.bastion() == null) {
            return;
        }

        final ResourceLocation id;
        try {
            id = ResourceLocation.parse(effectIdText);
        } catch (IllegalArgumentException e) {
            return;
        }

        final IShazhaoEffect effect = ShazhaoEffectRegistry.get(id);
        if (!(effect instanceof IBastionSkillEffect bastionEffect)) {
            return;
        }

        final BastionSkillContext ctx = new BastionSkillContext(
            baseContext.level(),
            baseContext.bastion(),
            baseContext.gameTime(),
            baseContext.bonusMultiplier(),
            baseContext.targets(),
            baseContext.guardians(),
            metadata == null ? Map.of() : metadata,
            RandomSource.create()
        );
        bastionEffect.onBastionSecond(ctx);
    }

    private static boolean tryRunBastionActivate(
        final ServerLevel level,
        final BastionData bastion,
        final long gameTime,
        final double bonusMultiplier,
        final List<ServerPlayer> targets,
        final List<Mob> guardians,
        final BastionTypeConfig.SkillEntry entry
    ) {
        if (entry == null || entry.skillId() == null || entry.skillId().isBlank()) {
            return false;
        }

        // 解析技能 ID
        final ResourceLocation id;
        try {
            id = ResourceLocation.parse(entry.skillId());
        } catch (IllegalArgumentException e) {
            return false;
        }

        // 合并 metadata（优先 ShazhaoData 默认值，再被 bastion_type.skill.metadata 覆盖）
        final Map<String, String> metadata = resolveMetadata(entry, id);

        // 技能冷却检查：按基地 + 技能维度
        final long cooldownTicks = getCooldownTicks(metadata, id);
        if (cooldownTicks > 0L) {
            final Long untilTick = COOLDOWN_UNTIL_TICK.get(new SkillKey(bastion.id(), id));
            if (untilTick != null && untilTick > gameTime) {
                return false;
            }
        }

        final IShazhaoEffect effect = ShazhaoEffectRegistry.get(id);
        if (!(effect instanceof IBastionSkillEffect bastionEffect)) {
            return false;
        }

        final BastionSkillContext ctx = new BastionSkillContext(
            level,
            bastion,
            gameTime,
            bonusMultiplier,
            targets,
            guardians,
            metadata,
            RandomSource.create()
        );

        final boolean success = bastionEffect.onBastionActivate(ctx);
        if (success && cooldownTicks > 0L) {
            COOLDOWN_UNTIL_TICK.put(
                new SkillKey(bastion.id(), id),
                gameTime + cooldownTicks
            );
        }
        return success;
    }

    // ===== 内部：收集对象 =====

    /**
     * 收集光环范围内的玩家。
     * <p>
     * 使用 auraRadius 而非 growthRadius，确保高转技能效果范围与光环一致。
     * </p>
     */
    private static List<ServerPlayer> collectPlayersInDomain(
        final ServerLevel level,
        final BastionData bastion
    ) {
        final List<ServerPlayer> targets = new ArrayList<>();
        final BlockPos core = bastion.corePos();
        final int radius = Math.max(1, bastion.getAuraRadius());
        final AABB box = new AABB(
            core.getX() - radius,
            core.getY() - radius,
            core.getZ() - radius,
            core.getX() + radius,
            core.getY() + radius,
            core.getZ() + radius
        );
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, box)) {
            if (player.blockPosition().distSqr(core) <= (long) radius * radius) {
                // 排除基地友方玩家（主人/接管者），高转技能只对敌对玩家生效
                if (!bastion.isFriendlyTo(player.getUUID())) {
                    targets.add(player);
                }
            }
        }
        return targets;
    }

    private static List<Mob> collectGuardians(
        final ServerLevel level,
        final BastionData bastion
    ) {
        final List<Mob> guardians = new ArrayList<>();
        final BlockPos core = bastion.corePos();
        final int radius = Math.max(16, bastion.growthRadius());
        final int height = 16;
        final AABB box = new AABB(
            core.getX() - radius,
            core.getY() - height,
            core.getZ() - radius,
            core.getX() + radius,
            core.getY() + height,
            core.getZ() + radius
        );

        for (Mob mob : level.getEntitiesOfClass(Mob.class, box)) {
            if (!BastionGuardianData.isGuardian(mob)) {
                continue;
            }
            if (!BastionGuardianData.belongsToBastion(mob, bastion.id())) {
                continue;
            }
            guardians.add(mob);
        }

        return guardians;
    }

    private static boolean isPassive(final BastionTypeConfig.SkillEntry entry) {
        return entry != null && "passive".equalsIgnoreCase(entry.category());
    }

    private static boolean isActive(final BastionTypeConfig.SkillEntry entry) {
        return entry != null && "active".equalsIgnoreCase(entry.category());
    }

    /**
     * 解析技能加成倍率。
     * <p>
     * 结合配置中的 bonusMultiplier 与基地主道途对应天赋效果，
     * 将最终倍率用于被动效果与主动技能的统一加成计算。
     * </p>
     */
    private static double resolveBonusMultiplier(
        final BastionTypeConfig.HighTierConfig highTier,
        final int tier,
        final BastionData bastion
    ) {
        if (highTier == null) {
            return 1.0;
        }
        final BastionTypeConfig.TierThreshold threshold = highTier.getThresholdForTier(tier);
        double baseMultiplier = 1.0;
        if (threshold != null) {
            baseMultiplier = Math.max(0.0, threshold.bonusMultiplier());
        }

        // 没有基地上下文时，仅返回配置倍率。
        if (bastion == null || bastion.primaryDao() == null) {
            return baseMultiplier;
        }

        // 力道：力压群雄（狂暴额外伤害）
        final double overwhelmBonus = BastionTalentEffectService.getLiDaoOverwhelmMultiplier(bastion);
        // 智道：念头效率（影响技能效果）
        final double niantouBonus = BastionTalentEffectService.getZhiDaoNiantouEfficiencyMultiplier(bastion);
        // 魂道：灵魂折磨（死亡光环伤害）
        final double tormentBonus = BastionTalentEffectService.getHunDaoTormentMultiplier(bastion);
        // 木道：共生协同（守卫与植物共生输出）
        final double synergyBonus = BastionTalentEffectService.getMuDaoGuardianSynergyMultiplier(bastion);

        // 根据基地主道途选择对应的天赋倍率。
        double talentMultiplier = 1.0;
        switch (bastion.primaryDao()) {
            case LI_DAO -> talentMultiplier = overwhelmBonus;
            case ZHI_DAO -> talentMultiplier = niantouBonus;
            case HUN_DAO -> talentMultiplier = tormentBonus;
            case MU_DAO -> talentMultiplier = synergyBonus;
            default -> {
                // 其他道途暂不叠加额外技能倍率，保持默认 1.0。
            }
        }

        return baseMultiplier * talentMultiplier;
    }

    private static Map<String, String> resolveMetadata(
        final BastionTypeConfig.SkillEntry entry,
        final ResourceLocation skillId
    ) {
        final Map<String, String> override =
            entry.metadata() == null ? Map.of() : entry.metadata();
        if (skillId == null) {
            return override;
        }

        final ShazhaoData data = ShazhaoDataManager.get(skillId);
        if (data == null) {
            return override;
        }
        if (override.isEmpty()) {
            return data.metadata();
        }

        final Map<String, String> merged = new HashMap<>(data.metadata());
        merged.putAll(override);
        return merged;
    }

    /**
     * 读取技能冷却（tick）。
     * <p>
     * 支持两种 key：
     * <ul>
     *   <li>"cooldown"：bastion_type.skill.metadata 的简写</li>
     *   <li>"{skillId}_cooldown"：与 ShazhaoData.metadata 兼容的完整 key</li>
     * </ul>
     * </p>
     */
    private static long getCooldownTicks(
        final Map<String, String> metadata,
        final ResourceLocation skillId
    ) {
        if (metadata == null || metadata.isEmpty() || skillId == null) {
            return 0L;
        }

        final long fromShort = parseLong(metadata.get("cooldown"), 0L);
        if (fromShort > 0L) {
            return fromShort;
        }
        final String fullKey = skillId + "_cooldown";
        return Math.max(0L, parseLong(metadata.get(fullKey), 0L));
    }

    private static long parseLong(final String text, final long defaultValue) {
        if (text == null || text.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }
}
