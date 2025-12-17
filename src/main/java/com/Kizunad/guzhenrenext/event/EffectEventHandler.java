package com.Kizunad.guzhenrenext.event;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.registry.ModMobEffects;
import com.Kizunad.guzhenrenext.util.ModConstants; // Import ModConstants
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;

@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public class EffectEventHandler {

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        // 如果实体拥有“撕裂”效果，治疗量减半
        if (event.getEntity().hasEffect(ModMobEffects.TEAR)) {
            event.setAmount(event.getAmount() * ModConstants.TEAR_HEALING_REDUCTION_FACTOR);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            // 检查攻击者是否有“疯狗”状态
            if (attacker.hasEffect(ModMobEffects.MAD_DOG)) {
                // 获取奴道道痕
                double nuDaoMarks = DaoHenHelper.getDaoHen(attacker, DaoHenHelper.DaoType.NU_DAO);
                
                // 基础持续时间 (5秒)
                // 加成：每 1点道痕 +2 tick
                int duration = ModConstants.MAD_DOG_BASE_DURATION_TICKS
                    + (int) (nuDaoMarks * ModConstants.MAD_DOG_NUDOA_DURATION_PER_MARK);
                
                // 施加撕裂效果
                event.getEntity().addEffect(new MobEffectInstance(
                    ModMobEffects.TEAR,
                    duration,
                    0 // 初始等级 0
                ));
            }
        }
    }
}
