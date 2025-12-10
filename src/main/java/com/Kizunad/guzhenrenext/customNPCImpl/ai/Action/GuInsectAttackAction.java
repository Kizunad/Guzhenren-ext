package com.Kizunad.guzhenrenext.customNPCImpl.ai.Action;

import com.Kizunad.customNPCs.ai.actions.registry.AttackCompatRegistry;
import com.Kizunad.customNPCs.ai.inventory.NpcInventory;
import com.Kizunad.guzhenrenext.customNPCImpl.util.GuInsectUtil;
import com.Kizunad.guzhenrenext.guzhenrenBridge.generated.itemUse.GuzhenrenItemDispatcher;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
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
    private static final long DEFAULT_COOLDOWN_TICKS = 40L;

    private final NpcCooldownTracker cooldownTracker;

    public GuInsectAttackAction() {
        this(DEFAULT_COOLDOWN_TICKS);
    }

    public GuInsectAttackAction(long cooldownTicks) {
        this.cooldownTracker = new NpcCooldownTracker(cooldownTicks);
    }

    @Override
    public AttackCompatRegistry.AttackDecision handle(
        AttackCompatRegistry.AttackContext context
    ) {
        if (cooldownTracker.shouldThrottle(context.getAttacker())) {
            return AttackCompatRegistry.AttackDecision.CONTINUE;
        }
        if (!ensureAttackGuEquipped(context)) {
            return AttackCompatRegistry.AttackDecision.CONTINUE;
        }
        if (triggerGuItemUse(context)) {
            cooldownTracker.markUsed(context.getAttacker());
            return AttackCompatRegistry.AttackDecision.HANDLED;
        }
        return AttackCompatRegistry.AttackDecision.CONTINUE;
    }

    private boolean ensureAttackGuEquipped(
        AttackCompatRegistry.AttackContext context
    ) {
        NpcInventory inventory = context.getMind().getInventory();
        List<Integer> validSlots = new ArrayList<>();
        for (int i = 0; i < inventory.getMainSize(); i++) {
            if (GuInsectUtil.isAttackGu(inventory.getItem(i))) {
                validSlots.add(i);
            }
        }

        if (!validSlots.isEmpty()) {
            int slot = validSlots.get(new Random().nextInt(validSlots.size()));
            ItemStack guStack = inventory.getItem(slot);
            ItemStack previous = context.getAttacker().getMainHandItem().copy();
            context
                .getAttacker()
                .setItemInHand(InteractionHand.MAIN_HAND, guStack);
            inventory.setItem(slot, previous);
            context.updateWeapon(context.getAttacker().getMainHandItem());

            LOGGER.info(
                "[GuInsectAttackAction] 随机切换主手蛊虫以执行攻击: {}",
                guStack.getHoverName().getString()
            );
            return true;
        }

        if (GuInsectUtil.isAttackGu(context.getWeapon())) {
            return true;
        }

        return false;
    }

    private boolean triggerGuItemUse(
        AttackCompatRegistry.AttackContext context
    ) {
        ItemStack weapon = context.getWeapon();
        if (weapon.isEmpty() || !GuInsectUtil.isAttackGu(weapon)) {
            return false;
        }

        // 强制瞄准目标眼部，防止攻击错位
        if (context.getTarget() != null) {
            context.getAttacker().lookAt(
                EntityAnchorArgument.Anchor.EYES, 
                context.getTarget().getEyePosition()
            );
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
}
