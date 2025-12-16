package com.Kizunad.guzhenrenext.kongqiao.attachment;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * 空窍数据附件注册表。
 */
public final class KongqiaoAttachments {

    private KongqiaoAttachments() {}

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(
        NeoForgeRegistries.ATTACHMENT_TYPES,
        GuzhenrenExt.MODID
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<KongqiaoData>> KONGQIAO =
        ATTACHMENT_TYPES.register(
            "kongqiao",
            () -> AttachmentType.serializable(KongqiaoData::new).build()
        );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<NianTouUnlocks>> NIANTOU_UNLOCKS =
        ATTACHMENT_TYPES.register(
            "niantou_unlocks",
            () -> AttachmentType.serializable(NianTouUnlocks::new).build()
        );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ActivePassives>> ACTIVE_PASSIVES =
        ATTACHMENT_TYPES.register(
            "active_passives",
            () -> AttachmentType.serializable(ActivePassives::new).copyOnDeath().build()
        );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<TweakConfig>> TWEAK_CONFIG =
        ATTACHMENT_TYPES.register(
            "tweak_config",
            () -> AttachmentType.serializable(TweakConfig::new).copyOnDeath().build()
        );

    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }

    public static KongqiaoData getData(Entity entity) {
        if (entity == null || !entity.hasData(KONGQIAO.get())) {
            return null;
        }
        return entity.getData(KONGQIAO.get());
    }

    public static NianTouUnlocks getUnlocks(Entity entity) {
        if (entity == null || !entity.hasData(NIANTOU_UNLOCKS.get())) {
            return null;
        }
        return entity.getData(NIANTOU_UNLOCKS.get());
    }

    public static ActivePassives getActivePassives(Entity entity) {
        if (entity == null) {
            return null;
        }
        if (!entity.hasData(ACTIVE_PASSIVES.get())) {
            entity.setData(ACTIVE_PASSIVES.get(), new ActivePassives());
        }
        return entity.getData(ACTIVE_PASSIVES.get());
    }

    public static TweakConfig getTweakConfig(Entity entity) {
        if (entity == null) {
            return null;
        }
        if (!entity.hasData(TWEAK_CONFIG.get())) {
            entity.setData(TWEAK_CONFIG.get(), new TweakConfig());
        }
        return entity.getData(TWEAK_CONFIG.get());
    }
}
