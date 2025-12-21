package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.common;

import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.kongqiao.service.XueDaoRuntimeService;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * 血道主动：召唤血河蠎（临时召唤物）。
 * <p>
 * 说明：召唤物到期后由 {@link XueDaoRuntimeService} 负责清理，避免永久堆积。
 * </p>
 */
public class XueDaoActiveSummonBloodPythonEffect implements IGuEffect {

    public static final String KEY_ENTITY_UNTIL_TICK =
        "GuzhenrenExtXueDao_SummonUntilTick";

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_DURATION_TICKS = "duration_ticks";

    private static final int DEFAULT_COOLDOWN_TICKS = 20 * 60;
    private static final int DEFAULT_DURATION_TICKS = 20 * 40;

    private static final ResourceLocation PYTHON_TYPE_ID =
        ResourceLocation.parse("guzhenren:xue_he_mang");

    private final String usageId;
    private final String nbtCooldownKey;

    public XueDaoActiveSummonBloodPythonEffect(
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
        if (!BuiltInRegistries.ENTITY_TYPE.containsKey(PYTHON_TYPE_ID)) {
            player.displayClientMessage(
                Component.literal("召唤失败：未找到血河蠎实体类型。"),
                true
            );
            return false;
        }

        XueDaoRuntimeService.despawnExistingPython(player);

        final EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(PYTHON_TYPE_ID);
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

        entity.getPersistentData().putInt(
            KEY_ENTITY_UNTIL_TICK,
            player.tickCount + durationTicks
        );
        level.addFreshEntity(entity);

        XueDaoRuntimeService.bindPython(player, entity.getUUID(), player.tickCount + durationTicks);

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
}

