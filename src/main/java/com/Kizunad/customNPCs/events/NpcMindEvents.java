package com.Kizunad.customNPCs.events;

import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.DamageTypeTags;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * NpcMind 事件处理器
 */
public class NpcMindEvents {

    private static final float FIRE_REDUCTION = 0.6f;
    private static final float FALL_REDUCTION = 0.7f;

    /**
     * 在实体 tick 时，调用 NpcMind.tick()
     */
    @SubscribeEvent
    public void onEntityTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        // 只处理服务端
        if (entity.level().isClientSide()) {
            return;
        }

        // 获取 NpcMind Data Attachment
        if (entity.hasData(NpcMindAttachment.NPC_MIND)) {
            var mind = entity.getData(NpcMindAttachment.NPC_MIND);
            mind.tick((ServerLevel) entity.level(), entity);
        }
    }

    /**
     * 针对自定义 NPC 的基础伤害抗性：降低火焰/跌落伤害，提升生存能力。
     */
    @SubscribeEvent
    public void onLivingDamagePre(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity living)) {
            return;
        }
        if (!living.hasData(NpcMindAttachment.NPC_MIND)) {
            return;
        }
        DamageSource source = event.getSource();
        float amount = event.getOriginalDamage();

        // 火焰伤害减免
        if (source.is(DamageTypeTags.IS_FIRE)) {
            event.setNewDamage(amount * FIRE_REDUCTION);
            return;
        }

        // 跌落伤害减免
        if (source.is(DamageTypeTags.IS_FALL)) {
            event.setNewDamage(amount * FALL_REDUCTION);
        }
    }
}
