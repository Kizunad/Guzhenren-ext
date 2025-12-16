package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierThree;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = GuzhenrenExt.MODID)
public class BingPoDamageHandler {

    // 默认配置 (实际应从 metadata 读取，此处为简化方案)
    private static final int DEFAULT_FREEZE_TICKS = 140; // 7秒冻结进度 (MC 满冻结约需 140-200 ticks)
    private static final float DEFAULT_DAMAGE_REDUCTION = 0.3f; // 30% 减伤
    private static final int FREEZE_PARTICLE_COUNT = 5;
    private static final double FREEZE_PARTICLE_SPREAD = 0.2;
    private static final double FREEZE_PARTICLE_SPEED = 0.05;
    private static final float BREAK_SOUND_VOLUME = 0.5f;
    private static final float BREAK_SOUND_PITCH = 1.5f;

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // 1. 检查激活
        ActivePassives actives = KongqiaoAttachments.getActivePassives(player);
        if (actives == null || !actives.isActive(BingPoGuEffect.USAGE_ID)) {
            return;
        }

        // 2. 获取攻击源
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            return;
        }

        // 3. 执行逻辑
        float originalDamage = event.getOriginalDamage();
        if (originalDamage <= 0) {
            return;
        }

        double freezeMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            player,
            DaoHenHelper.DaoType.HUN_DAO
        );
        int freezeTicks = (int) Math.round(DEFAULT_FREEZE_TICKS * freezeMultiplier);

        // A. 反噬：给予冻结
        // MC 的冻结机制：canFreeze() 检查，setTicksFrozen() 设置。
        // 普通生物默认上限 140? 无论如何，我们直接加。
        if (attacker.canFreeze()) {
            int currentFrozen = attacker.getTicksFrozen();
            attacker.setTicksFrozen(currentFrozen + freezeTicks);
            
            // 视觉反馈：攻击者身上冒出雪花
            if (attacker.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                    ParticleTypes.SNOWFLAKE,
                    attacker.getX(),
                    attacker.getEyeY(),
                    attacker.getZ(),
                    FREEZE_PARTICLE_COUNT,
                    FREEZE_PARTICLE_SPREAD,
                    FREEZE_PARTICLE_SPREAD,
                    FREEZE_PARTICLE_SPREAD,
                    FREEZE_PARTICLE_SPEED
                );
            }
        }

        // B. 减伤：若攻击者已冻结
        // isFullyFrozen() 是 ticksFrozen >= 140 (通常)。这里只要有寒意 (>0) 或者是完全冻结？
        // 为了体现“死寂”的克制，设定为：只要正在受冻 (ticksFrozen > 0)，就减伤。
        // 或者更强力一点：冻结程度越高，减伤越多？简化起见，固定减伤。
        if (attacker.getTicksFrozen() > 0 || attacker.isFullyFrozen()) {
            float newDamage = originalDamage * (1.0f - DEFAULT_DAMAGE_REDUCTION);
            event.setNewDamage(newDamage);
            
            // 听觉反馈：冰块碎裂的清脆声 (代表攻击被冰棱抵消)
            player.level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.GLASS_BREAK,
                SoundSource.PLAYERS,
                BREAK_SOUND_VOLUME,
                BREAK_SOUND_PITCH
            );
        }
    }
}
