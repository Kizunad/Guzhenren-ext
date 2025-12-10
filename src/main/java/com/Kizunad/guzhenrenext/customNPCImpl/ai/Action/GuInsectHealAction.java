package com.Kizunad.guzhenrenext.customNPCImpl.ai.Action;

import com.Kizunad.customNPCs.ai.actions.registry.HealCompatRegistry;
import com.Kizunad.customNPCs.ai.inventory.NpcInventory;
import com.Kizunad.guzhenrenext.customNPCImpl.util.GuInsectUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 治疗蛊虫兼容处理。
 * <p>
 * 在 HealGoal 前置分发阶段寻找治疗类蛊虫（*_1 标签），
 * 若找到则将其作为治疗物候选交给 HealGoal 使用。
 * </p>
 */
public class GuInsectHealAction implements HealCompatRegistry.HealCompatHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        GuInsectHealAction.class
    );
    private static final long DEFAULT_COOLDOWN_TICKS = 40L;
    private static final int MAIN_HAND_SLOT = -1;
    private static final int OFF_HAND_SLOT = -2;
    private final NpcCooldownTracker cooldownTracker;

    public GuInsectHealAction() {
        this(DEFAULT_COOLDOWN_TICKS);
    }

    public GuInsectHealAction(long cooldownTicks) {
        this.cooldownTracker = new NpcCooldownTracker(cooldownTicks);
    }

    @Override
    public HealCompatRegistry.HealDecision handle(
        HealCompatRegistry.HealContext context
    ) {
        var patient = context.getPatient();
        if (patient == null || patient.level().isClientSide()) {
            return HealCompatRegistry.HealDecision.CONTINUE;
        }
        if (cooldownTracker.shouldThrottle(patient)) {
            return HealCompatRegistry.HealDecision.CONTINUE;
        }

        // 先检查主手/副手，避免额外搬运
        ItemStack main = patient.getItemInHand(InteractionHand.MAIN_HAND);
        boolean proposed = false;
        if (GuInsectUtil.isHealGu(main)) {
            context.propose(main, MAIN_HAND_SLOT);
            proposed = true;
        } else {
            ItemStack off = patient.getItemInHand(InteractionHand.OFF_HAND);
            if (GuInsectUtil.isHealGu(off)) {
                context.propose(off, OFF_HAND_SLOT);
                proposed = true;
            } else {
                NpcInventory inventory = context.getMind().getInventory();
                for (int i = 0; i < inventory.getMainSize(); i++) {
                    ItemStack stack = inventory.getItem(i);
                    if (GuInsectUtil.isHealGu(stack)) {
                        context.propose(stack, i);
                        LOGGER.debug(
                            "[GuInsectHealAction] 发现治疗蛊虫，槽位 {}: {}",
                            i,
                            stack.getHoverName().getString()
                        );
                        proposed = true;
                        break;
                    }
                }
            }
        }
        if (proposed) {
            cooldownTracker.markUsed(patient);
        }

        return HealCompatRegistry.HealDecision.CONTINUE;
    }
}
