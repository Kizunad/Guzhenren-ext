package com.Kizunad.guzhenrenext.xianqiao.service;

import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoType;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/**
 * 仙窍初始化采样策略聚合器。
 * <p>统一管理默认道途、候选优先链、2x2 槽位布局与随机回退，避免调用层散落硬编码。</p>
 */
public final class ApertureInitialTerrainStrategy {

    private static final DaoType DEFAULT_DAO_TYPE = DaoType.EARTH;
    private static final int SAMPLE_OFFSET = 16;

    private static final List<ResourceKey<Biome>> DEFAULT_EARTH_PRIORITY = List.of(
        Biomes.DESERT,
        Biomes.SAVANNA,
        Biomes.PLAINS,
        Biomes.BADLANDS
    );

    private static final List<ResourceKey<Biome>> RANDOM_FALLBACK_POOL = List.of(
        Biomes.DESERT,
        Biomes.SAVANNA,
        Biomes.PLAINS,
        Biomes.BADLANDS,
        Biomes.FOREST,
        Biomes.TAIGA,
        Biomes.MEADOW,
        Biomes.SWAMP
    );

    private static final List<SlotTemplate> SLOT_TEMPLATES = List.of(
        new SlotTemplate("西北", -SAMPLE_OFFSET, -SAMPLE_OFFSET),
        new SlotTemplate("东北", 0, -SAMPLE_OFFSET),
        new SlotTemplate("西南", -SAMPLE_OFFSET, 0),
        new SlotTemplate("东南", 0, 0)
    );

    private static final Map<DaoType, List<ResourceKey<Biome>>> PRIORITY_CHAIN_BY_DAO = new EnumMap<>(DaoType.class);

    private static PlayerDaoTypeResolver playerDaoTypeResolver = player -> DEFAULT_DAO_TYPE;

    static {
        PRIORITY_CHAIN_BY_DAO.put(DEFAULT_DAO_TYPE, DEFAULT_EARTH_PRIORITY);
    }

    private ApertureInitialTerrainStrategy() {
    }

    public static InitialSamplingPlan buildPlan(ServerPlayer player, BlockPos center) {
        DaoType daoType = resolveDaoType(player);
        List<ResourceKey<Biome>> priorityChain = getPriorityChain(daoType);
        List<SamplingTask> tasks = new ArrayList<>(SLOT_TEMPLATES.size());
        for (SlotTemplate slotTemplate : SLOT_TEMPLATES) {
            tasks.add(
                new SamplingTask(
                    slotTemplate.directionName(),
                    center.offset(slotTemplate.offsetX(), 0, slotTemplate.offsetZ()),
                    priorityChain
                )
            );
        }
        return new InitialSamplingPlan(daoType, List.copyOf(tasks));
    }

    public static List<ResourceKey<Biome>> getPriorityChain(DaoType daoType) {
        List<ResourceKey<Biome>> configured = PRIORITY_CHAIN_BY_DAO.get(daoType);
        if (configured == null || configured.isEmpty()) {
            return DEFAULT_EARTH_PRIORITY;
        }
        return configured;
    }

    public static void setPriorityChain(DaoType daoType, List<ResourceKey<Biome>> priorityChain) {
        Objects.requireNonNull(daoType, "daoType");
        Objects.requireNonNull(priorityChain, "priorityChain");
        if (priorityChain.isEmpty()) {
            throw new IllegalArgumentException("priorityChain 不能为空");
        }
        PRIORITY_CHAIN_BY_DAO.put(daoType, List.copyOf(priorityChain));
    }

    public static void setPlayerDaoTypeResolver(PlayerDaoTypeResolver resolver) {
        playerDaoTypeResolver = Objects.requireNonNull(resolver, "resolver");
    }

    @Nullable
    public static ResourceKey<Biome> pickRandomFallbackBiome(
        RandomSource random,
        List<ResourceKey<Biome>> triedBiomes
    ) {
        Set<ResourceKey<Biome>> triedSet = new HashSet<>(triedBiomes);
        List<ResourceKey<Biome>> candidates = new ArrayList<>();
        for (ResourceKey<Biome> biomeKey : RANDOM_FALLBACK_POOL) {
            if (!triedSet.contains(biomeKey)) {
                candidates.add(biomeKey);
            }
        }
        if (candidates.isEmpty()) {
            candidates = new ArrayList<>(RANDOM_FALLBACK_POOL);
        }
        if (candidates.isEmpty()) {
            return null;
        }
        int randomIndex = random.nextInt(candidates.size());
        return candidates.get(randomIndex);
    }

    private static DaoType resolveDaoType(ServerPlayer player) {
        DaoType resolved = playerDaoTypeResolver.resolve(player);
        return resolved == null ? DEFAULT_DAO_TYPE : resolved;
    }

    /**
     * 玩家道途判定扩展点。
     * <p>当前默认 resolver 固定返回土道，后续可在不改命令层代码的前提下替换实现。</p>
     */
    @FunctionalInterface
    public interface PlayerDaoTypeResolver {

        @Nullable
        DaoType resolve(ServerPlayer player);
    }

    public record InitialSamplingPlan(DaoType daoType, List<SamplingTask> tasks) {
    }

    public record SamplingTask(String directionName, BlockPos targetAnchor, List<ResourceKey<Biome>> priorityBiomes) {
    }

    private record SlotTemplate(String directionName, int offsetX, int offsetZ) {
    }
}
