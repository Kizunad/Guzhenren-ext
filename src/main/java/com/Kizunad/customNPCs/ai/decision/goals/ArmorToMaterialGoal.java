package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.inventory.NpcInventory;
import com.Kizunad.customNPCs.ai.llm.LlmPromptRegistry;
import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogLevel;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import com.Kizunad.customNPCs.ai.interaction.MaterialValueManager;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 将背包内多余的盔甲拆解为材料点：每个部位仅保留一件最高等级，其余直接转化为材料。
 */
public class ArmorToMaterialGoal implements IGoal {

    public static final String LLM_USAGE_DESC =
        "ArmorToMaterialGoal: keep only one best armor per slot in inventory, convert redundant armor into "
            + "material points.";

    static {
        LlmPromptRegistry.register(LLM_USAGE_DESC);
    }

    private static final float PRIORITY = 0.12F; // 低于收集/制作
    private static final int COOLDOWN_TICKS = 200;
    private static final int TIER_LEATHER = 0;
    private static final int TIER_GOLD = 1;
    private static final int TIER_CHAIN = 2;
    private static final int TIER_IRON = 3;
    private static final int TIER_DIAMOND = 4;
    private static final int TIER_NETHERITE = 5;
    private static final Map<Item, Integer> ARMOR_TIER = buildArmorTier();

    private long nextAllowedGameTime;
    private boolean finished;

    @Override
    public String getName() {
        return "armor_to_material";
    }

    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        return canEngage(mind, entity) ? PRIORITY : 0.0F;
    }

    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        return canEngage(mind, entity);
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        finished = false;
        if (!(entity instanceof CustomNpcEntity npc)) {
            finished = true;
            return;
        }
        nextAllowedGameTime = entity.level().getGameTime() + COOLDOWN_TICKS;

        int salvaged = salvageExtraArmor(npc, mind.getInventory());
        finished = true;

        if (salvaged > 0) {
            MindLog.decision(
                MindLogLevel.INFO,
                "ArmorToMaterialGoal 拆解多余盔甲 x{}，当前材料 {}",
                salvaged,
                npc.getMaterial()
            );
        } else {
            MindLog.decision(
                MindLogLevel.DEBUG,
                "ArmorToMaterialGoal 无多余盔甲可拆解"
            );
        }
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        // 一次性处理
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        // 无清理
    }

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        if (!(entity instanceof CustomNpcEntity)) {
            return true;
        }
        return finished || !hasExtraArmor(mind.getInventory());
    }

    private boolean canEngage(INpcMind mind, LivingEntity entity) {
        if (!(entity instanceof CustomNpcEntity)) {
            return false;
        }
        if (entity.level().isClientSide()) {
            return false;
        }
        if (entity.level().getGameTime() < nextAllowedGameTime) {
            return false;
        }
        if (mind.getMemory().hasMemory(WorldStateKeys.IN_DANGER)) {
            return false;
        }
        return hasExtraArmor(mind.getInventory());
    }

    /**
     * 仅背包判定（不动已装备的盔甲）。
     */
    private boolean hasExtraArmor(NpcInventory inventory) {
        Map<EquipmentSlot, Integer> bestTier = computeBestTier(inventory);
        Map<EquipmentSlot, Integer> kept = new EnumMap<>(EquipmentSlot.class);

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getItem(i);
            EquipmentSlot slot = slotOf(stack);
            int tier = tierOf(stack);
            if (slot == null || tier < 0) {
                continue;
            }
            int best = bestTier.getOrDefault(slot, -1);
            if (tier < best) {
                return true;
            }
            if (tier == best) {
                int keptCount = kept.getOrDefault(slot, 0);
                if (keptCount >= 1) {
                    return true;
                }
                kept.put(slot, keptCount + 1);
            }
        }
        return false;
    }

    private int salvageExtraArmor(CustomNpcEntity npc, NpcInventory inventory) {
        Map<EquipmentSlot, Integer> bestTier = computeBestTier(inventory);
        Map<EquipmentSlot, Integer> kept = new EnumMap<>(EquipmentSlot.class);
        int salvaged = 0;

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getItem(i);
            EquipmentSlot slot = slotOf(stack);
            int tier = tierOf(stack);
            if (slot == null || tier < 0) {
                continue;
            }

            int best = bestTier.getOrDefault(slot, -1);
            if (tier < best) {
                salvaged += dismantle(npc, inventory, i, stack);
                continue;
            }

            if (tier == best) {
                int keptCount = kept.getOrDefault(slot, 0);
                if (keptCount >= 1) {
                    salvaged += dismantle(npc, inventory, i, stack);
                } else {
                    kept.put(slot, keptCount + 1);
                }
            }
        }
        return salvaged;
    }

    private int dismantle(
        CustomNpcEntity npc,
        NpcInventory inventory,
        int slotIndex,
        ItemStack stack
    ) {
        inventory.setItem(slotIndex, ItemStack.EMPTY);
        float gain = MaterialValueManager
            .getInstance()
            .getMaterialValue(stack);
        npc.addMaterial(gain);
        MindLog.execution(
            MindLogLevel.INFO,
            "ArmorToMaterialGoal 拆解盔甲槽位 {}，返还材料 {}",
            slotIndex,
            gain
        );
        return 1;
    }

    private Map<EquipmentSlot, Integer> computeBestTier(NpcInventory inventory) {
        Map<EquipmentSlot, Integer> bestTier = new EnumMap<>(EquipmentSlot.class);
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getItem(i);
            EquipmentSlot slot = slotOf(stack);
            int tier = tierOf(stack);
            if (slot == null || tier < 0) {
                continue;
            }
            int current = bestTier.getOrDefault(slot, -1);
            if (tier > current) {
                bestTier.put(slot, tier);
            }
        }
        return bestTier;
    }

    private EquipmentSlot slotOf(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem armor) {
            return armor.getEquipmentSlot();
        }
        return null;
    }

    private int tierOf(ItemStack stack) {
        return ARMOR_TIER.getOrDefault(stack.getItem(), -1);
    }

    private static Map<Item, Integer> buildArmorTier() {
        Map<Item, Integer> map = new HashMap<>();
        // 皮革
        map.put(Items.LEATHER_HELMET, TIER_LEATHER);
        map.put(Items.LEATHER_CHESTPLATE, TIER_LEATHER);
        map.put(Items.LEATHER_LEGGINGS, TIER_LEATHER);
        map.put(Items.LEATHER_BOOTS, TIER_LEATHER);
        // 金
        map.put(Items.GOLDEN_HELMET, TIER_GOLD);
        map.put(Items.GOLDEN_CHESTPLATE, TIER_GOLD);
        map.put(Items.GOLDEN_LEGGINGS, TIER_GOLD);
        map.put(Items.GOLDEN_BOOTS, TIER_GOLD);
        // 锁链
        map.put(Items.CHAINMAIL_HELMET, TIER_CHAIN);
        map.put(Items.CHAINMAIL_CHESTPLATE, TIER_CHAIN);
        map.put(Items.CHAINMAIL_LEGGINGS, TIER_CHAIN);
        map.put(Items.CHAINMAIL_BOOTS, TIER_CHAIN);
        // 铁
        map.put(Items.IRON_HELMET, TIER_IRON);
        map.put(Items.IRON_CHESTPLATE, TIER_IRON);
        map.put(Items.IRON_LEGGINGS, TIER_IRON);
        map.put(Items.IRON_BOOTS, TIER_IRON);
        // 钻石
        map.put(Items.DIAMOND_HELMET, TIER_DIAMOND);
        map.put(Items.DIAMOND_CHESTPLATE, TIER_DIAMOND);
        map.put(Items.DIAMOND_LEGGINGS, TIER_DIAMOND);
        map.put(Items.DIAMOND_BOOTS, TIER_DIAMOND);
        // 下界合金
        map.put(Items.NETHERITE_HELMET, TIER_NETHERITE);
        map.put(Items.NETHERITE_CHESTPLATE, TIER_NETHERITE);
        map.put(Items.NETHERITE_LEGGINGS, TIER_NETHERITE);
        map.put(Items.NETHERITE_BOOTS, TIER_NETHERITE);
        return map;
    }
}
