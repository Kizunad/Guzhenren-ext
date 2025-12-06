package com.Kizunad.customNPCs.menu;

import java.util.List;
import net.minecraft.world.inventory.Slot;

/**
 * 提供材料输入槽位的容器接口，供材料转换服务读取/清空。
 */
public interface MaterialSlotProvider {

    /**
     * 返回可用于材料转换的槽位列表。
     */
    List<Slot> getMaterialSlots();
}
