package com.Kizunad.guzhenrenext.customNPCImpl.ai.Action;

import com.Kizunad.customNPCs.ai.actions.registry.AttackCompatRegistry;
import com.Kizunad.customNPCs.ai.inventory.NpcInventory;
import com.Kizunad.guzhenrenext.customNPCImpl.util.GuInsectUtil;
import com.Kizunad.guzhenrenext.guzhenrenBridge.generated.itemUse.GuzhenrenItemDispatcher;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 攻击型蛊虫兼容处理。
 * <p>
 * - 若主手没有攻击蛊虫，则自动从背包调换
 * - 保持对 {@link com.Kizunad.customNPCs.ai.actions.common.AttackAction}
 *   的透明：继续执行默认攻击逻辑
 */
public class GuInsectAttackAction
    implements AttackCompatRegistry.AttackCompatHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        GuInsectAttackAction.class
    );

    @Override
    public AttackCompatRegistry.AttackDecision handle(
        AttackCompatRegistry.AttackContext context
    ) {
        if (!ensureAttackGuEquipped(context)) {
            return AttackCompatRegistry.AttackDecision.CONTINUE;
        }
        if (triggerGuItemUse(context)) {
            return AttackCompatRegistry.AttackDecision.HANDLED;
        }
        return AttackCompatRegistry.AttackDecision.CONTINUE;
    }

    private boolean ensureAttackGuEquipped(
        AttackCompatRegistry.AttackContext context
    ) {
        if (GuInsectUtil.isAttackGu(context.getWeapon())) {
            return true;
        }

        NpcInventory inventory = context.getMind().getInventory();
        int slot = findAttackGuSlot(inventory);
        if (slot == -1) {
            return false;
        }

        ItemStack guStack = inventory.getItem(slot);
        ItemStack previous = context.getAttacker().getMainHandItem().copy();
        context
            .getAttacker()
            .setItemInHand(InteractionHand.MAIN_HAND, guStack);
        inventory.setItem(slot, previous);
        context.updateWeapon(context.getAttacker().getMainHandItem());

        LOGGER.info(
            "[GuInsectAttackAction] 切换主手蛊虫以执行攻击: {}",
            guStack.getHoverName().getString()
        );
        return true;
    }

    private boolean triggerGuItemUse(
        AttackCompatRegistry.AttackContext context
    ) {
        ItemStack weapon = context.getWeapon();
        if (weapon.isEmpty() || !GuInsectUtil.isAttackGu(weapon)) {
            return false;
        }
        try {
            boolean success =
                GuzhenrenItemDispatcher.dispatch(context.getAttacker(), weapon);
            if (success) {
                LOGGER.debug(
                    "[GuInsectAttackAction] Dispatcher 执行成功: {}",
                    weapon.getHoverName().getString()
                );
            }
            return success;
        } catch (Exception e) {
            LOGGER.error("[GuInsectAttackAction] 调用蛊虫分发表失败", e);
            return false;
        }
    }

    private int findAttackGuSlot(NpcInventory inventory) {
        for (int i = 0; i < inventory.getMainSize(); i++) {
            if (GuInsectUtil.isAttackGu(inventory.getItem(i))) {
                return i;
            }
        }
        return -1;
    }
}
