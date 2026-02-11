package com.Kizunad.guzhenrenext.xianqiao.daomark;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * 仙窍道痕系统的 Chunk Attachment 注册表。
 */
public final class XianqiaoAttachments {

    private XianqiaoAttachments() {
    }

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(
        NeoForgeRegistries.ATTACHMENT_TYPES,
        GuzhenrenExt.MODID
    );

    /** 挂载在 LevelChunk 上的道痕数据附件。 */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<DaoChunkAttachment>> DAO_MARK =
        ATTACHMENT_TYPES.register(
            "dao_mark",
            () -> AttachmentType.serializable(DaoChunkAttachment::new).build()
        );

    /**
     * 将附件类型注册到模组事件总线。
     *
     * @param bus 模组事件总线
     */
    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }
}
