package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.interaction.MaterialValueManager;
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
 * 升级背包中的剑：按固定路线消耗材料提升剑阶。
 */
public class UpgradeSwordGoal implements IGoal {

    public static final String LLM_USAGE_DESC =
        "UpgradeSwordGoal: upgrade inventory swords (wood→stone→iron→diamond→netherite; gold→iron) using " +
        "material points.";

    static {
        LlmPromptRegistry.register(LLM_USAGE_DESC);
    }

    private static final float PRIORITY = 0.21F; // 与盔甲升级一致
    private static final int COOLDOWN_TICKS = 200;
    private static final Map<Item, Item> UPGRADE_CHAIN = buildChain();

    private long nextAllowedGameTime;
    private boolean finished;

    @Override
    public String getName() {
        return "upgrade_sword";
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

        int upgraded = upgradeInventorySwords(npc, mind.getInventory());
        finished = true;

        if (upgraded > 0) {
            MindLog.decision(
                MindLogLevel.INFO,
                "UpgradeSwordGoal 升级剑 x{}，剩余材料 {}",
                upgraded,
                npc.getMaterial()
            );
        } else {
            MindLog.decision(
                MindLogLevel.WARN,
                "UpgradeSwordGoal 未找到可升级的剑或材料不足"
            );
        }
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        // 升级逻辑在 start 中一次性完成
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
        return finished || !hasUpgradeableSword(mind);
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
        float minCost = minRequiredCost();
        if (minCost == Float.MAX_VALUE || npc.getMaterial() < minCost) {
            return false;
        }
        return hasUpgradeableSword(mind);
    }

    private boolean hasUpgradeableSword(INpcMind mind) {
        NpcInventory inventory = mind.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getItem(i);
            Item next = UPGRADE_CHAIN.get(stack.getItem());
            if (next != null) {
                return true;
            }
        }
        return false;
    }

    private int upgradeInventorySwords(
        CustomNpcEntity npc,
        NpcInventory inventory
    ) {
        int upgraded = 0;
        float material = npc.getMaterial();

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getItem(i);
            Item next = UPGRADE_CHAIN.get(stack.getItem());
            if (next == null) {
                continue;
            }
            float cost = getMaterialCost(next);
            if (cost <= 0.0F || material < cost) {
                continue;
            }

            inventory.setItem(i, new ItemStack(next));
            material -= cost;
            upgraded++;
        }

        npc.setMaterial(material);
        return upgraded;
    }

    private float minRequiredCost() {
        float min = Float.MAX_VALUE;
        for (Item target : UPGRADE_CHAIN.values()) {
            float cost = getMaterialCost(target);
            if (cost > 0.0F && cost < min) {
                min = cost;
            }
        }
        return min;
    }

    private float getMaterialCost(Item item) {
        return MaterialValueManager.getInstance().getMaterialValue(
            new ItemStack(item)
        );
    }

    private static Map<Item, Item> buildChain() {
        Map<Item, Item> map = new HashMap<>();
        map.put(Items.WOODEN_SWORD, Items.STONE_SWORD);
        map.put(Items.STONE_SWORD, Items.IRON_SWORD);
        map.put(Items.GOLDEN_SWORD, Items.IRON_SWORD);
        map.put(Items.IRON_SWORD, Items.DIAMOND_SWORD);
        map.put(Items.DIAMOND_SWORD, Items.NETHERITE_SWORD);
        return map;
    }
}
