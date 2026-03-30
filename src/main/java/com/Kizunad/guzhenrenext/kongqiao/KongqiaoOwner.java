package com.Kizunad.guzhenrenext.kongqiao;

import com.Kizunad.guzhenrenext.kongqiao.inventory.AttackInventory;
import com.Kizunad.guzhenrenext.kongqiao.inventory.GuchongFeedInventory;
import com.Kizunad.guzhenrenext.kongqiao.inventory.KongqiaoInventory;
import java.util.UUID;

/**
 * 空窍系统的基础持有者抽象。
 * <p>
 * 该接口只暴露空窍基础载体必须具备的背包、归属与脏标记能力。
 * 它对应 {@link KongqiaoLifecycleStateContract} 中的基础承载层，而不是完整生命周期真相源。
 * 玩家、NPC 甚至其他实体都可以实现该接口，以复用空窍/攻击背包逻辑。
 * </p>
 */
public interface KongqiaoOwner {
    KongqiaoInventory getKongqiaoInventory();

    AttackInventory getAttackInventory();

    GuchongFeedInventory getFeedInventory();

    /**
     * 按需同步到客户端/存档时调用。
     */
    default void markKongqiaoDirty() {}

    /**
     * 标识归属，用于日志与调试。
     */
    UUID getKongqiaoId();

    /**
     * 当前是否为客户端。
     */
    boolean isClientSide();
}
