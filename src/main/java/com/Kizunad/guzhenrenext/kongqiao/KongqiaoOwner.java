package com.Kizunad.guzhenrenext.kongqiao;

import com.Kizunad.guzhenrenext.kongqiao.inventory.AttackInventory;
import com.Kizunad.guzhenrenext.kongqiao.inventory.GuchongFeedInventory;
import com.Kizunad.guzhenrenext.kongqiao.inventory.KongqiaoInventory;
import java.util.UUID;

/**
 * 空窍系统的持有者抽象。
 * <p>
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
