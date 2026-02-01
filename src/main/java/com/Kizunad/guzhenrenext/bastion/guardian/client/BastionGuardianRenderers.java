package com.Kizunad.guzhenrenext.bastion.guardian.client;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.bastion.guardian.BastionGuardianEntities;
import net.minecraft.client.renderer.entity.EvokerRenderer;
import net.minecraft.client.renderer.entity.IllusionerRenderer;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.client.renderer.entity.PhantomRenderer;
import net.minecraft.client.renderer.entity.PillagerRenderer;
import net.minecraft.client.renderer.entity.RavagerRenderer;
import net.minecraft.client.renderer.entity.VexRenderer;
import net.minecraft.client.renderer.entity.BlazeRenderer;
import net.minecraft.client.renderer.entity.HoglinRenderer;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.VindicatorRenderer;
import net.minecraft.client.renderer.entity.WardenRenderer;
import net.minecraft.client.renderer.entity.WitchRenderer;
import net.minecraft.client.renderer.entity.WitherSkeletonRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * 基地守卫渲染注册（客户端）。
 * <p>
 * 将自定义 EntityType 绑定到原版 Renderer，从而复用原版模型/动画。</p>
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BastionGuardianRenderers {

    private BastionGuardianRenderers() {
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(BastionGuardianEntities.BASTION_WITCH.get(), WitchRenderer::new);
        event.registerEntityRenderer(BastionGuardianEntities.BASTION_EVOKER.get(), EvokerRenderer::new);
        event.registerEntityRenderer(BastionGuardianEntities.BASTION_ILLUSIONER.get(), IllusionerRenderer::new);

        event.registerEntityRenderer(BastionGuardianEntities.BASTION_PHANTOM.get(), PhantomRenderer::new);
        event.registerEntityRenderer(BastionGuardianEntities.BASTION_VEX.get(), VexRenderer::new);
        event.registerEntityRenderer(
            BastionGuardianEntities.BASTION_WITHER_SKELETON.get(),
            WitherSkeletonRenderer::new
        );

        event.registerEntityRenderer(BastionGuardianEntities.BASTION_VINDICATOR.get(), VindicatorRenderer::new);
        event.registerEntityRenderer(BastionGuardianEntities.BASTION_SHIELD_GUARDIAN.get(), VindicatorRenderer::new);
        event.registerEntityRenderer(BastionGuardianEntities.BASTION_BERSERKER_GUARDIAN.get(), VindicatorRenderer::new);
        event.registerEntityRenderer(BastionGuardianEntities.BASTION_PILLAGER.get(), PillagerRenderer::new);
        event.registerEntityRenderer(BastionGuardianEntities.BASTION_ARCHER_GUARDIAN.get(), SkeletonRenderer::new);
        event.registerEntityRenderer(BastionGuardianEntities.BASTION_CASTER_GUARDIAN.get(), BlazeRenderer::new);
        event.registerEntityRenderer(BastionGuardianEntities.BASTION_IRON_GOLEM.get(), IronGolemRenderer::new);

        event.registerEntityRenderer(BastionGuardianEntities.BASTION_RAVAGER.get(), RavagerRenderer::new);
        event.registerEntityRenderer(BastionGuardianEntities.BASTION_HOGLIN.get(), HoglinRenderer::new);
        event.registerEntityRenderer(BastionGuardianEntities.BASTION_WARDEN.get(), WardenRenderer::new);
    }
}
