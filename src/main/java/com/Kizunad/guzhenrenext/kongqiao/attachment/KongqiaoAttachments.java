package com.Kizunad.guzhenrenext.kongqiao.attachment;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.parasite.ParasiteUpgradeAttachment;
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

    // ===== 飞剑系统（独立附件，不写入 KongqiaoData） =====

    public static final DeferredHolder<
        AttachmentType<?>,
        AttachmentType<com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordStorageAttachment>
    > FLYING_SWORD_STORAGE =
        ATTACHMENT_TYPES.register(
            "flying_sword_storage",
            () ->
                AttachmentType.serializable(
                        com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordStorageAttachment::new
                    )
                    .copyOnDeath()
                    .build()
        );

    public static final DeferredHolder<
        AttachmentType<?>,
        AttachmentType<com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordSelectionAttachment>
    > FLYING_SWORD_SELECTION =
        ATTACHMENT_TYPES.register(
            "flying_sword_selection",
            () ->
                AttachmentType.serializable(
                        com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordSelectionAttachment::new
                    )
                    .copyOnDeath()
                    .build()
        );

    public static final DeferredHolder<
        AttachmentType<?>,
        AttachmentType<com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordCooldownAttachment>
    > FLYING_SWORD_COOLDOWNS =
        ATTACHMENT_TYPES.register(
            "flying_sword_cooldowns",
            () ->
                AttachmentType.serializable(
                        com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordCooldownAttachment::new
                    )
                    .build()
        );

    public static final DeferredHolder<
        AttachmentType<?>,
        AttachmentType<com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordPreferencesAttachment>
    > FLYING_SWORD_PREFERENCES =
        ATTACHMENT_TYPES.register(
            "flying_sword_preferences",
            () ->
                AttachmentType.serializable(
                        com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordPreferencesAttachment::new
                    )
                    .copyOnDeath()
                    .build()
        );

    public static final DeferredHolder<
        AttachmentType<?>,
        AttachmentType<com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordRuntimeAttachment>
    > FLYING_SWORD_RUNTIME =
        ATTACHMENT_TYPES.register(
            "flying_sword_runtime",
            () ->
                AttachmentType.serializable(
                        com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordRuntimeAttachment::new
                    )
                    .build()
        );

    public static final DeferredHolder<
        AttachmentType<?>,
        AttachmentType<com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordStateAttachment>
    > FLYING_SWORD_STATE =
        ATTACHMENT_TYPES.register(
            "flying_sword_state",
            () ->
                AttachmentType.serializable(
                        com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordStateAttachment::new
                    )
                    .build()
        );

    public static final DeferredHolder<
        AttachmentType<?>,
        AttachmentType<com.Kizunad.guzhenrenext.kongqiao.flyingsword.forge.FlyingSwordForgeAttachment>
    > FLYING_SWORD_FORGE =
        ATTACHMENT_TYPES.register(
            "flying_sword_forge",
            () ->
                AttachmentType.serializable(
                        com.Kizunad.guzhenrenext.kongqiao.flyingsword.forge.FlyingSwordForgeAttachment::new
                    )
                    .copyOnDeath()
                    .build()
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

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ParasiteUpgradeAttachment>> PARASITE_UPGRADES =
        ATTACHMENT_TYPES.register(
            "parasite_upgrades",
            () -> AttachmentType.serializable(ParasiteUpgradeAttachment::new).copyOnDeath().build()
        );

    public static final DeferredHolder<
        AttachmentType<?>,
        AttachmentType<GuzhenrenVariableModifiers>
    > GUZHENREN_VARIABLE_MODIFIERS =
        ATTACHMENT_TYPES.register(
            "guzhenren_variable_modifiers",
            () -> AttachmentType.serializable(GuzhenrenVariableModifiers::new).copyOnDeath().build()
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

    public static com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment
        .FlyingSwordStorageAttachment getFlyingSwordStorage(Entity entity) {
        if (entity == null) {
            return null;
        }
        if (!entity.hasData(FLYING_SWORD_STORAGE.get())) {
            entity.setData(
                FLYING_SWORD_STORAGE.get(),
                new com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment
                    .FlyingSwordStorageAttachment()
            );
        }
        return entity.getData(FLYING_SWORD_STORAGE.get());
    }

    public static com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment
        .FlyingSwordSelectionAttachment getFlyingSwordSelection(Entity entity) {
        if (entity == null) {
            return null;
        }
        if (!entity.hasData(FLYING_SWORD_SELECTION.get())) {
            entity.setData(
                FLYING_SWORD_SELECTION.get(),
                new com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment
                    .FlyingSwordSelectionAttachment()
            );
        }
        return entity.getData(FLYING_SWORD_SELECTION.get());
    }

    public static com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment
        .FlyingSwordCooldownAttachment getFlyingSwordCooldowns(Entity entity) {
        if (entity == null) {
            return null;
        }
        if (!entity.hasData(FLYING_SWORD_COOLDOWNS.get())) {
            entity.setData(
                FLYING_SWORD_COOLDOWNS.get(),
                new com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment
                    .FlyingSwordCooldownAttachment()
            );
        }
        return entity.getData(FLYING_SWORD_COOLDOWNS.get());
    }

    public static com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment
        .FlyingSwordPreferencesAttachment getFlyingSwordPreferences(Entity entity) {
        if (entity == null) {
            return null;
        }
        if (!entity.hasData(FLYING_SWORD_PREFERENCES.get())) {
            entity.setData(
                FLYING_SWORD_PREFERENCES.get(),
                new com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment
                    .FlyingSwordPreferencesAttachment()
            );
        }
        return entity.getData(FLYING_SWORD_PREFERENCES.get());
    }

    public static com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment
        .FlyingSwordRuntimeAttachment getFlyingSwordRuntime(Entity entity) {
        if (entity == null) {
            return null;
        }
        if (!entity.hasData(FLYING_SWORD_RUNTIME.get())) {
            entity.setData(
                FLYING_SWORD_RUNTIME.get(),
                new com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment
                    .FlyingSwordRuntimeAttachment()
            );
        }
        return entity.getData(FLYING_SWORD_RUNTIME.get());
    }

    public static com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment
        .FlyingSwordStateAttachment getFlyingSwordState(Entity entity) {
        if (entity == null) {
            return null;
        }
        if (!entity.hasData(FLYING_SWORD_STATE.get())) {
            entity.setData(
                FLYING_SWORD_STATE.get(),
                new com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment
                    .FlyingSwordStateAttachment()
            );
        }
        return entity.getData(FLYING_SWORD_STATE.get());
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

    public static GuzhenrenVariableModifiers getGuzhenrenVariableModifiers(
        Entity entity
    ) {
        if (entity == null) {
            return null;
        }
        if (!entity.hasData(GUZHENREN_VARIABLE_MODIFIERS.get())) {
            entity.setData(
                GUZHENREN_VARIABLE_MODIFIERS.get(),
                new GuzhenrenVariableModifiers()
            );
        }
        return entity.getData(GUZHENREN_VARIABLE_MODIFIERS.get());
    }

    public static ParasiteUpgradeAttachment getParasiteUpgrades(Entity entity) {
        if (entity == null) {
            return null;
        }
        if (!entity.hasData(PARASITE_UPGRADES.get())) {
            entity.setData(PARASITE_UPGRADES.get(), new ParasiteUpgradeAttachment());
        }
        return entity.getData(PARASITE_UPGRADES.get());
    }

    public static com.Kizunad.guzhenrenext.kongqiao.flyingsword.forge
        .FlyingSwordForgeAttachment getFlyingSwordForge(Entity entity) {
        if (entity == null) {
            return null;
        }
        if (!entity.hasData(FLYING_SWORD_FORGE.get())) {
            entity.setData(
                FLYING_SWORD_FORGE.get(),
                new com.Kizunad.guzhenrenext.kongqiao.flyingsword.forge
                    .FlyingSwordForgeAttachment()
            );
        }
        return entity.getData(FLYING_SWORD_FORGE.get());
    }
}
