package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.hundao.tierTwo;

import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 二转大魂蛊：主动【吞魂炼魄】。
 * <p>
 * 按“魂魄材料”物品ID（如 {@code guzhenren:yizhuanhunpo}）映射产出魂魄：
 * <ul>
 *   <li>从玩家背包中选择一份魂材吞下（默认取产出最高的那种）。</li>
 *   <li>消耗少量真元，转化为魂魄；粒子对周围玩家可见。</li>
 *   <li>映射与数值可通过 metadata 的 {@code hunpo_value_<itemid>} 覆盖。</li>
 * </ul>
 * </p>
 */
public class DaHunGuHunPoMaterialIngestEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:dahuongu_active_ingest_hunpo_material";

    private static final String NBT_COOLDOWN_UNTIL_TICK =
        "DaHunGuHunPoMaterialIngestCooldownUntilTick";

    private static final TagKey<Item> HUNPO_MATERIAL_TAG = TagKey.create(
        Registries.ITEM,
        ResourceLocation.parse("guzhenren:hunpo")
    );

    private static final int TICKS_PER_SECOND = 20;
    private static final int MAX_HUNPO_VALUE_ENTRIES = 64;

    private static final int DEFAULT_COOLDOWN_TICKS = 40;
    private static final int DEFAULT_CONSUME_COUNT = 1;

    private static final int PARTICLE_COUNT = 14;
    private static final double PARTICLE_SPREAD = 0.35;
    private static final double PARTICLE_SPEED = 0.02;
    private static final double PARTICLE_Y_FACTOR = 0.6;

    private static final Map<ResourceLocation, Double> DEFAULT_HUNPO_VALUES =
        Map.of(
            ResourceLocation.parse("guzhenren:fanrenhunpo"),
            8.0,
            ResourceLocation.parse("guzhenren:yizhuanhunpo"),
            20.0,
            ResourceLocation.parse("guzhenren:erzhuanhunpo"),
            45.0,
            ResourceLocation.parse("guzhenren:sanzhunhunpo"),
            90.0,
            ResourceLocation.parse("guzhenren:sizhuanhunpo"),
            180.0,
            ResourceLocation.parse("guzhenren:wuzhuanhunpo"),
            360.0
        );

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public boolean onActivate(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user.level().isClientSide()) {
            return false;
        }
        if (!(user instanceof ServerPlayer player)) {
            return false;
        }
        if (!(user.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        final int currentTick = user.tickCount;
        final int cooldownUntil = getCooldownUntilTick(user);
        if (cooldownUntil > currentTick) {
            final int remain = cooldownUntil - currentTick;
            player.displayClientMessage(
                Component.literal(
                    "吞魂炼魄冷却中，剩余 " + (remain / TICKS_PER_SECOND) + " 秒"
                ),
                true
            );
            return false;
        }

        final int consumeCount = Math.max(
            1,
            UsageMetadataHelper.getInt(usageInfo, "consume_count", DEFAULT_CONSUME_COUNT)
        );
        final Candidate candidate = findBestCandidate(player, usageInfo);
        if (candidate == null) {
            player.displayClientMessage(
                Component.literal("背包中无可吞炼的魂魄材料。"),
                true
            );
            return false;
        }

        final int actualConsume = Math.min(consumeCount, candidate.stack().getCount());
        if (actualConsume <= 0) {
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final int cooldownTicks = getMetaInt(
            usageInfo,
            "cooldown_ticks",
            DEFAULT_COOLDOWN_TICKS
        );
        if (cooldownTicks > 0) {
            setCooldownUntilTick(user, currentTick + cooldownTicks);
        }

        final double perItemValue = candidate.hunpoValue();
        final double totalGain = Math.max(0.0, perItemValue * actualConsume);

        candidate.stack().shrink(actualConsume);
        if (totalGain > 0.0) {
            HunPoHelper.modify(user, totalGain);
        }

        spawnParticles(serverLevel, user);
        player.displayClientMessage(
            Component.literal(
                "吞炼 "
                    + candidate.itemId()
                    + " x"
                    + actualConsume
                    + "，魂魄 +"
                    + formatOneDecimal(totalGain)
            ),
            true
        );
        return true;
    }

    private static Candidate findBestCandidate(
        final ServerPlayer player,
        final NianTouData.Usage usageInfo
    ) {
        final Map<ResourceLocation, Double> values = buildHunpoValueTable(usageInfo);

        Candidate best = null;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            final ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty() || !stack.is(HUNPO_MATERIAL_TAG)) {
                continue;
            }
            final ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(
                stack.getItem()
            );

            final double value = values.getOrDefault(itemId, 0.0);
            if (value <= 0.0) {
                continue;
            }
            if (best == null || value > best.hunpoValue()) {
                best = new Candidate(itemId.toString(), stack, value);
            }
        }
        return best;
    }

    private static Map<ResourceLocation, Double> buildHunpoValueTable(
        final NianTouData.Usage usageInfo
    ) {
        final Map<ResourceLocation, Double> values = new HashMap<>(
            DEFAULT_HUNPO_VALUES
        );
        if (usageInfo.metadata() == null || usageInfo.metadata().isEmpty()) {
            return values;
        }

        // 配置格式：hunpo_item_<i> + hunpo_value_<i>（满足 snake_case 校验，值内可含冒号）
        // 例：hunpo_item_0=guzhenren:yizhuanhunpo, hunpo_value_0=20.0
        for (int i = 0; i < MAX_HUNPO_VALUE_ENTRIES; i++) {
            final String itemKey = "hunpo_item_" + i;
            final String valueKey = "hunpo_value_" + i;
            if (!usageInfo.metadata().containsKey(itemKey)
                || !usageInfo.metadata().containsKey(valueKey)) {
                continue;
            }
            final ResourceLocation rl = ResourceLocation.tryParse(
                usageInfo.metadata().get(itemKey)
            );
            if (rl == null) {
                continue;
            }
            try {
                final double value = Double.parseDouble(
                    usageInfo.metadata().get(valueKey)
                );
                if (value > 0.0) {
                    values.put(rl, value);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return values;
    }

    private static void spawnParticles(
        final ServerLevel level,
        final LivingEntity user
    ) {
        level.sendParticles(
            ParticleTypes.SOUL,
            user.getX(),
            user.getY() + user.getBbHeight() * PARTICLE_Y_FACTOR,
            user.getZ(),
            PARTICLE_COUNT,
            PARTICLE_SPREAD,
            PARTICLE_SPREAD,
            PARTICLE_SPREAD,
            PARTICLE_SPEED
        );
    }

    private static String formatOneDecimal(final double value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private static int getCooldownUntilTick(final LivingEntity user) {
        return user.getPersistentData().getInt(NBT_COOLDOWN_UNTIL_TICK);
    }

    private static void setCooldownUntilTick(
        final LivingEntity user,
        final int untilTick
    ) {
        user.getPersistentData().putInt(NBT_COOLDOWN_UNTIL_TICK, untilTick);
    }

    private static int getMetaInt(
        final NianTouData.Usage usage,
        final String key,
        final int defaultValue
    ) {
        return UsageMetadataHelper.getInt(usage, key, defaultValue);
    }

    private record Candidate(String itemId, ItemStack stack, double hunpoValue) {}
}
