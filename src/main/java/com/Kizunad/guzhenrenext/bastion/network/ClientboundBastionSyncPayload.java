package com.Kizunad.guzhenrenext.bastion.network;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.bastion.BastionDao;
import com.Kizunad.guzhenrenext.bastion.BastionState;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 基地同步包（服务端 -> 客户端）。
 * <p>
 * 同步基地的核心信息供客户端渲染使用：
 * <ul>
 *   <li>基地位置和范围（边界渲染）</li>
 *   <li>道途类型（颜色渲染）</li>
 *   <li>状态（视觉效果切换）</li>
 * </ul>
 * </p>
 *
 * @param bastionId           基地唯一标识符
 * @param coreX               核心 X 坐标
 * @param coreY               核心 Y 坐标
 * @param coreZ               核心 Z 坐标
 * @param daoOrdinal          道途类型的 ordinal 值
 * @param tier                当前转数
 * @param radius              节点扩张半径（growthRadius）
 * @param auraRadius          光环影响半径（用于边界渲染和效果判定）
 * @param stateOrdinal        持久化状态的 ordinal 值（ACTIVE/DESTROYED）
 * @param sealedUntilGameTime 封印解除的游戏时间（0 表示未封印）
 */
public record ClientboundBastionSyncPayload(
        UUID bastionId,
        int coreX,
        int coreY,
        int coreZ,
        int daoOrdinal,
        int tier,
        int radius,
        int auraRadius,
        int stateOrdinal,
        long sealedUntilGameTime
) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
        GuzhenrenExt.MODID,
        "bastion_sync"
    );

    public static final Type<ClientboundBastionSyncPayload> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundBastionSyncPayload> STREAM_CODEC =
        StreamCodec.of(
            (buf, payload) -> {
                buf.writeUUID(payload.bastionId);
                buf.writeInt(payload.coreX);
                buf.writeInt(payload.coreY);
                buf.writeInt(payload.coreZ);
                buf.writeVarInt(payload.daoOrdinal);
                buf.writeVarInt(payload.tier);
                buf.writeVarInt(payload.radius);
                buf.writeVarInt(payload.auraRadius);
                buf.writeVarInt(payload.stateOrdinal);
                buf.writeLong(payload.sealedUntilGameTime);
            },
            buf -> new ClientboundBastionSyncPayload(
                buf.readUUID(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readLong()
            )
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 从核心坐标创建 BlockPos。
     *
     * @return 核心方块坐标
     */
    public BlockPos getCorePos() {
        return new BlockPos(coreX, coreY, coreZ);
    }

    /**
     * 获取道途类型。
     *
     * @return BastionDao 枚举值
     */
    public BastionDao getDao() {
        BastionDao[] values = BastionDao.values();
        if (daoOrdinal >= 0 && daoOrdinal < values.length) {
            return values[daoOrdinal];
        }
        return BastionDao.ZHI_DAO; // fallback
    }

    /**
     * 获取基地状态。
     *
     * @return BastionState 枚举值
     */
    public BastionState getState() {
        BastionState[] values = BastionState.values();
        if (stateOrdinal >= 0 && stateOrdinal < values.length) {
            return values[stateOrdinal];
        }
        return BastionState.ACTIVE; // fallback
    }

    /**
     * 获取考虑封印时间后的有效状态。
     * <p>
     * 客户端使用此方法派生真实状态，优先级：DESTROYED > SEALED > ACTIVE。
     * </p>
     *
     * @param currentGameTime 当前游戏时间（客户端可从 Minecraft.getInstance().level.getGameTime() 获取）
     * @return 有效 BastionState
     */
    public BastionState getEffectiveState(long currentGameTime) {
        return BastionState.getEffectiveState(getState(), sealedUntilGameTime, currentGameTime);
    }

    /**
     * 客户端处理：将基地数据注册到客户端缓存。
     */
    public static void handle(ClientboundBastionSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                Class<?> clazz = Class.forName(
                    "com.Kizunad.guzhenrenext.bastion.client.BastionClientCache"
                );
                clazz.getMethod(
                    "register",
                    ClientboundBastionSyncPayload.class
                ).invoke(null, payload);
            } catch (Exception ignored) {
                // Dedicated Server 环境或客户端类未加载时忽略
            }
        });
    }
}
