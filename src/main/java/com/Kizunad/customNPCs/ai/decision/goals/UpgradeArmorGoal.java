package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.inventory.NpcInventory;
import com.Kizunad.customNPCs.ai.llm.LlmPromptRegistry;
import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogLevel;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 升级背包盔甲：按照固定路线消耗材料将盔甲升级。
 * 路线：皮革 -> 黄金 -> 锁链 -> 铁 -> 钻石 -> 下界合金。
 */
public class UpgradeArmorGoal implements IGoal {

    public static final String LLM_USAGE_DESC =
        "UpgradeArmorGoal: upgrade inventory armor (leather→gold→chain→iron→diamond→netherite) using material points.";

    static {
        LlmPromptRegistry.register(LLM_USAGE_DESC);
    }

    private static final float PRIORITY = 0.21F; // 略高于 craft_armor
    private static final int COOLDOWN_TICKS = 200;
    private static final float COST_LEATHER_TO_GOLD = 4.0F;
    private static final float COST_GOLD_TO_CHAIN = 6.0F;
    private static final float COST_CHAIN_TO_IRON = 8.0F;
    private static final float COST_IRON_TO_DIAMOND = 12.0F;
    private static final float COST_DIAMOND_TO_NETHERITE = 16.0F;
    private static final Map<Item, UpgradeTarget> UPGRADE_CHAIN = buildChain();

    private long nextAllowedGameTime;
    private boolean finished;

    @Override
    public String getName() {
        return "upgrade_armor";
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

        int upgraded = upgradeInventoryArmor(npc, mind.getInventory());
        finished = true;

        if (upgraded > 0) {
            MindLog.decision(
                MindLogLevel.INFO,
                "UpgradeArmorGoal 升级盔甲 x{}，剩余材料 {}",
                upgraded,
                npc.getMaterial()
            );
        } else {
            MindLog.decision(
                MindLogLevel.WARN,
                "UpgradeArmorGoal 未找到可升级的盔甲或材料不足"
            );
        }
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        // 升级在 start 中完成
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        // 无需额外清理
    }

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        if (!(entity instanceof CustomNpcEntity)) {
            return true;
        }
        return finished || !hasUpgradeableArmor(mind);
    }

    private boolean canEngage(INpcMind mind, LivingEntity entity) {
        if (!(entity instanceof CustomNpcEntity npc)) {
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
        return hasUpgradeableArmor(mind) && npc.getMaterial() >= minRequiredCost();
    }

    private boolean hasUpgradeableArmor(INpcMind mind) {
        NpcInventory inventory = mind.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (UPGRADE_CHAIN.containsKey(stack.getItem())) {
                return true;
            }
        }
        return false;
    }

    private int upgradeInventoryArmor(CustomNpcEntity npc, NpcInventory inventory) {
        int upgraded = 0;
        float material = npc.getMaterial();

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getItem(i);
            UpgradeTarget target = UPGRADE_CHAIN.get(stack.getItem());
            if (target == null) {
                continue;
            }
            if (material < target.cost()) {
                continue;
            }

            inventory.setItem(i, new ItemStack(target.next()));
            material -= target.cost();
            upgraded++;
        }

        npc.setMaterial(material);
        return upgraded;
    }

    private float minRequiredCost() {
        return UPGRADE_CHAIN
            .values()
            .stream()
            .map(UpgradeTarget::cost)
            .min(Float::compare)
            .orElse(Float.MAX_VALUE);
    }

    private static Map<Item, UpgradeTarget> buildChain() {
        Map<Item, UpgradeTarget> map = new HashMap<>();
        register(map, Items.LEATHER_HELMET, Items.GOLDEN_HELMET, COST_LEATHER_TO_GOLD);
        register(map, Items.GOLDEN_HELMET, Items.CHAINMAIL_HELMET, COST_GOLD_TO_CHAIN);
        register(map, Items.CHAINMAIL_HELMET, Items.IRON_HELMET, COST_CHAIN_TO_IRON);
        register(map, Items.IRON_HELMET, Items.DIAMOND_HELMET, COST_IRON_TO_DIAMOND);
        register(map, Items.DIAMOND_HELMET, Items.NETHERITE_HELMET, COST_DIAMOND_TO_NETHERITE);

        register(map, Items.LEATHER_CHESTPLATE, Items.GOLDEN_CHESTPLATE, COST_LEATHER_TO_GOLD);
        register(map, Items.GOLDEN_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, COST_GOLD_TO_CHAIN);
        register(map, Items.CHAINMAIL_CHESTPLATE, Items.IRON_CHESTPLATE, COST_CHAIN_TO_IRON);
        register(map, Items.IRON_CHESTPLATE, Items.DIAMOND_CHESTPLATE, COST_IRON_TO_DIAMOND);
        register(map, Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE, COST_DIAMOND_TO_NETHERITE);

        register(map, Items.LEATHER_LEGGINGS, Items.GOLDEN_LEGGINGS, COST_LEATHER_TO_GOLD);
        register(map, Items.GOLDEN_LEGGINGS, Items.CHAINMAIL_LEGGINGS, COST_GOLD_TO_CHAIN);
        register(map, Items.CHAINMAIL_LEGGINGS, Items.IRON_LEGGINGS, COST_CHAIN_TO_IRON);
        register(map, Items.IRON_LEGGINGS, Items.DIAMOND_LEGGINGS, COST_IRON_TO_DIAMOND);
        register(map, Items.DIAMOND_LEGGINGS, Items.NETHERITE_LEGGINGS, COST_DIAMOND_TO_NETHERITE);

        register(map, Items.LEATHER_BOOTS, Items.GOLDEN_BOOTS, COST_LEATHER_TO_GOLD);
        register(map, Items.GOLDEN_BOOTS, Items.CHAINMAIL_BOOTS, COST_GOLD_TO_CHAIN);
        register(map, Items.CHAINMAIL_BOOTS, Items.IRON_BOOTS, COST_CHAIN_TO_IRON);
        register(map, Items.IRON_BOOTS, Items.DIAMOND_BOOTS, COST_IRON_TO_DIAMOND);
        register(map, Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS, COST_DIAMOND_TO_NETHERITE);
        return map;
    }

    private static void register(
        Map<Item, UpgradeTarget> map,
        Item from,
        Item to,
        float cost
    ) {
        map.put(from, new UpgradeTarget(to, cost));
    }

    private record UpgradeTarget(Item next, float cost) {}
}
