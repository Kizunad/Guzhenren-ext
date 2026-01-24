package com.Kizunad.guzhenrenext.bastion;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 基地系统音效注册表。
 * <p>
 * 注册基地交互、状态变化等相关音效。
 * </p>
 */
public final class BastionSounds {

    private BastionSounds() {
        // 工具类
    }

    /** 音效注册器。 */
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
        DeferredRegister.create(Registries.SOUND_EVENT, GuzhenrenExt.MODID);

    // ===== 基地交互音效 =====

    /** 封印成功音效。 */
    public static final DeferredHolder<SoundEvent, SoundEvent> BASTION_SEAL =
        registerSound("bastion.seal");

    /** 占领成功音效。 */
    public static final DeferredHolder<SoundEvent, SoundEvent> BASTION_CAPTURE =
        registerSound("bastion.capture");

    /** 攻击基地音效。 */
    public static final DeferredHolder<SoundEvent, SoundEvent> BASTION_ATTACK =
        registerSound("bastion.attack");

    /** 基地警报音效（触发防御时）。 */
    public static final DeferredHolder<SoundEvent, SoundEvent> BASTION_ALARM =
        registerSound("bastion.alarm");

    /** 资源祭献音效。 */
    public static final DeferredHolder<SoundEvent, SoundEvent> BASTION_SACRIFICE =
        registerSound("bastion.sacrifice");

    // ===== 基地状态变化音效 =====

    /** 基地升级音效。 */
    public static final DeferredHolder<SoundEvent, SoundEvent> BASTION_EVOLVE =
        registerSound("bastion.evolve");

    /** 基地销毁音效。 */
    public static final DeferredHolder<SoundEvent, SoundEvent> BASTION_DESTROY =
        registerSound("bastion.destroy");

    /** 封印解除音效。 */
    public static final DeferredHolder<SoundEvent, SoundEvent> BASTION_UNSEAL =
        registerSound("bastion.unseal");

    // ===== 节点音效 =====

    /** 节点扩张音效。 */
    public static final DeferredHolder<SoundEvent, SoundEvent> NODE_EXPAND =
        registerSound("bastion.node_expand");

    /** 节点衰减音效。 */
    public static final DeferredHolder<SoundEvent, SoundEvent> NODE_DECAY =
        registerSound("bastion.node_decay");

    // ===== 环境音效 =====

    /** 基地环境音效（核心附近播放）。 */
    public static final DeferredHolder<SoundEvent, SoundEvent> BASTION_AMBIENT =
        registerSound("bastion.ambient");

    // ===== 注册方法 =====

    /**
     * 注册模组事件总线。
     *
     * @param modEventBus 模组事件总线
     */
    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }

    /**
     * 创建并注册音效事件。
     */
    private static DeferredHolder<SoundEvent, SoundEvent> registerSound(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(GuzhenrenExt.MODID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
