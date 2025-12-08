package com.Kizunad.guzhenrenext.customNPCImpl.ai.Action;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.guzhenrenext.guzhenrenBridge.generated.itemUse.GuzhenrenItemDispatcher;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 蛊真人物品使用 Action。
 * <p>
 * 尝试使用指定的物品（必须是支持的蛊虫）。
 * 这是一个瞬时动作，执行一次即完成。
 */
public class GuzhenrenItemUseAction extends AbstractGuzhenrenAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(GuzhenrenItemUseAction.class);
    private final ItemStack itemStack;

    public GuzhenrenItemUseAction(ItemStack itemStack) {
        super("GuzhenrenItemUseAction");
        this.itemStack = itemStack;
    }

    @Override
    protected ActionStatus tickInternal(INpcMind mind, Mob mob) {
        if (itemStack == null || itemStack.isEmpty()) {
            LOGGER.warn("[GuzhenrenItemUseAction] 物品为空，动作失败。");
            return ActionStatus.FAILURE;
        }

        LOGGER.debug("[GuzhenrenItemUseAction] 尝试使用物品: {}", itemStack.getDisplayName().getString());

        boolean success = false;
        try {
            success = GuzhenrenItemDispatcher.dispatch(mob, itemStack);
        } catch (Exception e) {
            LOGGER.error("[GuzhenrenItemUseAction] 执行分发逻辑时发生异常", e);
            return ActionStatus.FAILURE;
        }

        if (success) {
            LOGGER.debug("[GuzhenrenItemUseAction] 物品使用成功。");
            return ActionStatus.SUCCESS;
        } else {
            LOGGER.warn("[GuzhenrenItemUseAction] 物品不支持或执行条件未满足: {}", itemStack.getItem().getClass().getSimpleName());
            return ActionStatus.FAILURE;
        }
    }

    @Override
    public boolean canInterrupt() {
        return false; // 瞬时动作，不建议中断（其实瞬间就完成了）
    }
    
    @Override
    public String toString() {
        return "GuzhenrenItemUseAction{item=" + (itemStack != null ? itemStack.getItem() : "null") + "}";
    }
}
