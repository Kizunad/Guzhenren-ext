package com.Kizunad.guzhenrenext.bastion.event;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.entity.BastionGuardianData;
import com.Kizunad.guzhenrenext.bastion.service.BastionModifierService;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * 基地词缀效果事件处理器。
 * 将 BastionModifierService 中的词缀效果集成到守卫的伤害/死亡事件中。
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class BastionModifierEventHandler {

    private BastionModifierEventHandler() {
    }

    /**
     * 处理守卫伤害前事件：
     * <ul>
     *     <li>守卫受伤：应用 HARDENED 减伤</li>
     *     <li>守卫攻击玩家：应用 CLOAKED 首击加成</li>
     * </ul>
     *
     * @param event 伤害事件（Pre，可改写伤害值）
     */
    @SubscribeEvent
    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        LivingEntity victim = event.getEntity();

        // 场景 1：守卫受伤（入伤）
        if (victim instanceof Mob guardianVictim && BastionGuardianData.isGuardian(guardianVictim)
            && guardianVictim.level() instanceof ServerLevel victimLevel) {
            BastionData victimBastion = getBastionData(victimLevel, guardianVictim);
            if (victimBastion != null) {
                float modifiedIncomingDamage = BastionModifierService.modifyIncomingDamage(
                    guardianVictim,
                    victimBastion,
                    event.getNewDamage()
                );
                event.setNewDamage(modifiedIncomingDamage);
            }
        }

        // 场景 2：守卫攻击玩家（出伤）
        if (!(victim instanceof ServerPlayer)) {
            return;
        }
        Entity attackerEntity = event.getSource().getEntity();
        if (!(attackerEntity instanceof Mob guardian)) {
            return;
        }
        if (!BastionGuardianData.isGuardian(guardian)) {
            return;
        }
        if (!(guardian.level() instanceof ServerLevel attackerLevel)) {
            return;
        }

        BastionData bastion = getBastionData(attackerLevel, guardian);
        if (bastion == null) {
            return;
        }

        float modifiedDamage = BastionModifierService.modifyOutgoingDamage(
            guardian,
            bastion,
            event.getNewDamage()
        );
        event.setNewDamage(modifiedDamage);
    }

    /**
     * 守卫死亡时触发词缀死亡效果。
     *
     * @param event 死亡事件
     */
    @SubscribeEvent
    public static void onGuardianDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim instanceof Mob guardian)) {
            return;
        }
        if (!(guardian.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!BastionGuardianData.isGuardian(guardian)) {
            return;
        }

        BastionData bastion = getBastionData(serverLevel, guardian);
        if (bastion == null) {
            return;
        }

        BastionModifierService.onGuardianDeath(guardian, bastion, serverLevel);
    }

    /**
     * 从守卫实体获取所属基地数据。
     *
     * @param level  服务端世界
     * @param entity 实体
     * @return 基地数据；无法解析时返回 null
     */
    private static BastionData getBastionData(ServerLevel level, Entity entity) {
        if (level == null || entity == null) {
            return null;
        }
        UUID bastionId = BastionGuardianData.getBastionId(entity);
        if (bastionId == null) {
            return null;
        }
        return BastionSavedData.get(level).getBastion(bastionId);
    }
}
