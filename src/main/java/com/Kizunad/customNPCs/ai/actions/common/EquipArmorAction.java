package com.Kizunad.customNPCs.ai.actions.common;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.actions.AbstractStandardAction;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.inventory.NpcInventory;
import com.Kizunad.customNPCs.ai.util.ArmorEvaluationUtil;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 装备背包中更优盔甲的动作。
 */
@SuppressWarnings("checkstyle:MagicNumber")
public class EquipArmorAction extends AbstractStandardAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(EquipArmorAction.class);

    private final ArmorEvaluationUtil.ArmorPreference preference;

    public EquipArmorAction() {
        this(ArmorEvaluationUtil.ArmorPreference.defaults());
    }

    public EquipArmorAction(ArmorEvaluationUtil.ArmorPreference preference) {
        super("EquipArmorAction");
        this.preference = preference;
    }

    @Override
    protected ActionStatus tickInternal(INpcMind mind, Mob mob) {
        if (mind == null) {
            LOGGER.warn("[EquipArmorAction] mind 为空，无法执行");
            return ActionStatus.FAILURE;
        }
        NpcInventory inventory = mind.getInventory();
        ArmorEvaluationUtil.ArmorUpgrade upgrade = ArmorEvaluationUtil.findBestUpgrade(
            inventory,
            mob,
            preference
        );

        if (upgrade == null) {
            LOGGER.debug("[EquipArmorAction] 未发现更优盔甲，跳过");
            return ActionStatus.FAILURE;
        }

        ItemStack candidate = inventory.removeItem(upgrade.inventorySlot());
        if (candidate.isEmpty()) {
            LOGGER.warn(
                "[EquipArmorAction] 目标槽位 {} 已为空，无法装备",
                upgrade.inventorySlot()
            );
            return ActionStatus.FAILURE;
        }

        // 盔甲通常不可堆叠，仍保证仅装备 1 个并回收余量
        ItemStack equipStack = candidate.copyWithCount(1);
        if (candidate.getCount() > 1) {
            ItemStack remainder = candidate.copy();
            remainder.shrink(1);
            ItemStack leftover = inventory.addItem(remainder);
            if (!leftover.isEmpty()) {
                mob.spawnAtLocation(leftover);
                LOGGER.warn(
                    "[EquipArmorAction] 背包已满，部分余量掉落: {}",
                    leftover.getHoverName().getString()
                );
            }
        }

        EquipmentSlot slot = upgrade.slot();
        ItemStack previous = mob.getItemBySlot(slot);
        mob.setItemSlot(slot, equipStack);

        if (!previous.isEmpty()) {
            ItemStack leftover = inventory.addItem(previous);
            if (!leftover.isEmpty()) {
                mob.spawnAtLocation(leftover);
                LOGGER.warn(
                    "[EquipArmorAction] 背包已满，旧装备掉落: {}",
                    leftover.getHoverName().getString()
                );
            }
        }

        mind.getMemory().rememberLongTerm(WorldStateKeys.ARMOR_OPTIMIZED, true);
        mind
            .getMemory()
            .rememberShortTerm(
                WorldStateKeys.ARMOR_BETTER_AVAILABLE,
                false,
                200
            );

        LOGGER.info(
            "[EquipArmorAction] 装备更优盔甲 -> 槽位 {} | 提升 {}",
            slot,
            String.format("%.2f", upgrade.improvement())
        );

        return ActionStatus.SUCCESS;
    }

    @Override
    public boolean canInterrupt() {
        return true;
    }

    @Override
    public String getName() {
        return "equip_armor";
    }
}
