package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bingxuedao.common;

import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * 冰雪道主动：召唤白相仙蛇（临时召唤物）。
 */
public class BingXueDaoActiveSummonSerpentEffect implements IGuEffect {

    public static final String KEY_SUMMON_UUID =
        "GuzhenrenExtBingXue_SummonSerpentUuid";
    public static final String KEY_SUMMON_UNTIL_TICK =
        "GuzhenrenExtBingXue_SummonSerpentUntilTick";
    public static final String KEY_ENTITY_UNTIL_TICK =
        "GuzhenrenExtBingXue_SummonUntilTick";

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_DURATION_TICKS = "duration_ticks";

    private static final int DEFAULT_COOLDOWN_TICKS = 600;
    private static final int DEFAULT_DURATION_TICKS = 20 * 45;

    private static final ResourceLocation SERPENT_TYPE_ID =
        ResourceLocation.parse("guzhenren:bai_xiang_xian_she");

    private final String usageId;
    private final String nbtCooldownKey;

    public BingXueDaoActiveSummonSerpentEffect(
        final String usageId,
        final String nbtCooldownKey
    ) {
        this.usageId = usageId;
        this.nbtCooldownKey = nbtCooldownKey;
    }

    @Override
    public String getUsageId() {
        return usageId;
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

        final int remain = GuEffectCooldownHelper.getRemainingTicks(
            user,
            nbtCooldownKey
        );
        if (remain > 0) {
            player.displayClientMessage(
                Component.literal("冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        final int durationTicks = Math.max(
            1,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_DURATION_TICKS,
                DEFAULT_DURATION_TICKS
            )
        );

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final ServerLevel level = player.serverLevel();
        if (!BuiltInRegistries.ENTITY_TYPE.containsKey(SERPENT_TYPE_ID)) {
            player.displayClientMessage(
                Component.literal("召唤失败：未找到白相仙蛇实体类型。"),
                true
            );
            return false;
        }

        despawnExisting(player);

        final EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(SERPENT_TYPE_ID);
        final Entity entity = type.create(level);
        if (entity == null) {
            player.displayClientMessage(
                Component.literal("召唤失败：实体创建失败。"),
                true
            );
            return false;
        }

        final Vec3 forward = player.getLookAngle().normalize();
        final Vec3 spawnPos = player.position().add(forward.scale(2.0));
        entity.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, player.getYRot(), 0.0F);

        if (entity instanceof TamableAnimal tamable) {
            tamable.tame(player);
            tamable.setPersistenceRequired();
        }

        entity.getPersistentData().putInt(
            KEY_ENTITY_UNTIL_TICK,
            player.tickCount + durationTicks
        );
        level.addFreshEntity(entity);

        player.getPersistentData().putUUID(KEY_SUMMON_UUID, entity.getUUID());
        player.getPersistentData().putInt(
            KEY_SUMMON_UNTIL_TICK,
            player.tickCount + durationTicks
        );

        final int cooldownTicks = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_COOLDOWN_TICKS,
                DEFAULT_COOLDOWN_TICKS
            )
        );
        if (cooldownTicks > 0) {
            GuEffectCooldownHelper.setCooldownUntilTick(
                user,
                nbtCooldownKey,
                user.tickCount + cooldownTicks
            );
        }

        return true;
    }

    private static void despawnExisting(final ServerPlayer player) {
        if (player == null) {
            return;
        }
        if (!player.getPersistentData().hasUUID(KEY_SUMMON_UUID)) {
            return;
        }
        final ServerLevel level = player.serverLevel();
        final Entity existing = level.getEntity(
            player.getPersistentData().getUUID(KEY_SUMMON_UUID)
        );
        if (existing != null && existing.isAlive()) {
            existing.discard();
        }
        player.getPersistentData().remove(KEY_SUMMON_UUID);
        player.getPersistentData().remove(KEY_SUMMON_UNTIL_TICK);
    }
}
