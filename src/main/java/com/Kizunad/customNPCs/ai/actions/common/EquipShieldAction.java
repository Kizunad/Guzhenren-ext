package com.Kizunad.customNPCs.ai.actions.common;

import com.Kizunad.customNPCs.ai.actions.AbstractStandardAction;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.inventory.NpcInventory;
import com.Kizunad.customNPCs.ai.llm.LlmPromptRegistry;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 将盾牌装备到副手，避免物品复制：从副手/主手/背包中取盾并处理旧物品回收。
 */
public class EquipShieldAction extends AbstractStandardAction {

    public static final String LLM_USAGE_DESC =
        "EquipShieldAction: move a shield into offhand (from inventory or mainhand), avoiding item duplication.";

    static {
        LlmPromptRegistry.register(LLM_USAGE_DESC);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(
        EquipShieldAction.class
    );

    public EquipShieldAction() {
        super("EquipShieldAction");
    }

    @Override
    protected ActionStatus tickInternal(INpcMind mind, Mob mob) {
        if (mind == null) {
            LOGGER.warn("[EquipShieldAction] mind 为空，无法执行");
            return ActionStatus.FAILURE;
        }

        // 已经有盾牌在副手，视为完成
        if (mob.getItemInHand(InteractionHand.OFF_HAND).is(Items.SHIELD)) {
            return ActionStatus.SUCCESS;
        }

        // 优先将主手盾牌移到副手
        if (mob.getItemInHand(InteractionHand.MAIN_HAND).is(Items.SHIELD)) {
            return moveFromMainHand(mind, mob);
        }

        return moveFromInventory(mind, mob);
    }

    private ActionStatus moveFromMainHand(INpcMind mind, Mob mob) {
        ItemStack offhandOld = mob.getItemInHand(InteractionHand.OFF_HAND);
        ItemStack shield = mob.getItemInHand(InteractionHand.MAIN_HAND);

        mob.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        mob.setItemInHand(InteractionHand.OFF_HAND, shield);
        recycle(mind.getInventory(), mob, offhandOld);

        LOGGER.info("[EquipShieldAction] 将主手盾牌移动到副手");
        return ActionStatus.SUCCESS;
    }

    private ActionStatus moveFromInventory(INpcMind mind, Mob mob) {
        NpcInventory inventory = mind.getInventory();
        int slot = inventory.findFirstSlot(stack -> stack.is(Items.SHIELD));
        if (slot < 0) {
            LOGGER.debug("[EquipShieldAction] 背包无盾牌");
            return ActionStatus.FAILURE;
        }

        ItemStack stack = inventory.removeItem(slot);
        if (stack.isEmpty()) {
            LOGGER.warn("[EquipShieldAction] 目标槽位空，放弃");
            return ActionStatus.FAILURE;
        }

        // 取出 1 面盾，其余回库存
        ItemStack equip = stack.split(1);
        recycle(inventory, mob, stack);

        ItemStack offhandOld = mob.getItemInHand(InteractionHand.OFF_HAND);
        mob.setItemInHand(InteractionHand.OFF_HAND, equip);
        recycle(inventory, mob, offhandOld);

        LOGGER.info(
            "[EquipShieldAction] 从背包装备盾牌到副手，来源槽位 {}",
            slot
        );
        return ActionStatus.SUCCESS;
    }

    private void recycle(NpcInventory inventory, Mob mob, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemStack leftover = inventory.addItem(stack);
        if (!leftover.isEmpty()) {
            mob.spawnAtLocation(leftover);
            LOGGER.warn(
                "[EquipShieldAction] 背包已满，掉落物品: {}",
                leftover.getHoverName().getString()
            );
        }
    }

    @Override
    public boolean canInterrupt() {
        return true;
    }
}
