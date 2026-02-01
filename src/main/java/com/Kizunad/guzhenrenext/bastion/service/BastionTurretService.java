package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeConfig;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeManager;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/**
 * 炮台节点服务。
 * <p>
 * Round 24：最小实现，直接对范围内敌对目标造成伤害并扣除资源。
 * </p>
 */
public final class BastionTurretService {

    private BastionTurretService() {
    }

    /**
     * 每 tick 处理炮台攻击。
     *
     * @param level     服务器世界
     * @param savedData SavedData
     * @param bastion   基地数据
     * @param gameTime  当前游戏时间
     */
    public static void tick(ServerLevel level, BastionSavedData savedData, BastionData bastion, long gameTime) {
        BastionTypeConfig typeConfig = BastionTypeManager.getOrDefault(bastion.bastionType());
        BastionTypeConfig.TurretConfig turret = typeConfig.turret();

        if (turret == null || !turret.enabled()) {
            return;
        }

        long nextAllowed = savedData.getNextTurretTryTick(bastion.id());
        if (gameTime < nextAllowed) {
            return;
        }
        savedData.setNextTurretTryTick(bastion.id(), gameTime + Math.max(1, turret.cooldownTicks()));

        java.util.Set<BlockPos> anchors = savedData.getAnchors(bastion.id());
        if (anchors == null || anchors.isEmpty()) {
            return;
        }

        int shots = 0;
        int maxCount = Math.max(0, turret.maxCount());
        double costPerShot = Math.max(0.0, turret.costPerShot());
        double damage = Math.max(0.0, turret.damage());
        int range = Math.max(1, turret.range());
        int cooldown = Math.max(1, turret.cooldownTicks());

        BastionData current = bastion;
        for (BlockPos anchorPos : anchors) {
            if (maxCount > 0 && shots >= maxCount) {
                break;
            }
            if (current.resourcePool() < costPerShot) {
                break;
            }

            AABB box = new AABB(anchorPos).inflate(range);
            LivingEntity target = level
                .getEntitiesOfClass(LivingEntity.class, box, e -> isHostileToBastion(e, bastion.id()))
                .stream()
                .findFirst()
                .orElse(null);
            if (target == null) {
                continue;
            }

            // 造成直接伤害
            target.hurt(level.damageSources().generic(), (float) damage);

            // 扣资源并落盘
            BastionData updated = current.withResourcePool(Math.max(0.0, current.resourcePool() - costPerShot));
            savedData.updateBastion(updated);
            current = updated;

            shots++;
        }
    }

    private static boolean isHostileToBastion(LivingEntity entity, UUID bastionId) {
        if (entity == null || !entity.isAlive()) {
            return false;
        }
        if (entity instanceof Player player) {
            return !player.isCreative();
        }
        // 非玩家：不属于该基地的守卫视为敌对（简化）。
        return true;
    }
}
