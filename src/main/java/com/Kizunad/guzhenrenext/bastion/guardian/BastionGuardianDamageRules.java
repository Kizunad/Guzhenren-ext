package com.Kizunad.guzhenrenext.bastion.guardian;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * 基地守卫伤害规则兜底（服务端）。
 * <p>
 * 由于“守卫 vs 守卫”可能存在间接伤害（投射物/爆炸/特殊攻击），
 * 仅靠 AI 目标过滤不足以完全阻止互殴。
 * 因此在伤害事件层做硬裁决：不满足交战条件则把伤害设为 0。</p>
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class BastionGuardianDamageRules {

    private BastionGuardianDamageRules() {
    }

    @SubscribeEvent
    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) {
            return;
        }
        if (!(victim.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Entity attacker = BastionGuardianCombatRules.resolveAttacker(event.getSource());
        if (attacker == null) {
            return;
        }
        if (!BastionGuardianCombatRules.isGuardian(attacker) || !BastionGuardianCombatRules.isGuardian(victim)) {
            return;
        }
        if (BastionGuardianCombatRules.canGuardianDamage(serverLevel, attacker, victim)) {
            return;
        }

        // NeoForge 的 Pre 事件允许改写伤害值：这里直接置 0，实现“硬免疫”。
        event.setNewDamage(DamageConstants.ZERO_DAMAGE);
    }

    /**
     * 常量分组，避免 MagicNumber。
     */
    private static final class DamageConstants {
        static final float ZERO_DAMAGE = 0.0f;

        private DamageConstants() {
        }
    }
}
