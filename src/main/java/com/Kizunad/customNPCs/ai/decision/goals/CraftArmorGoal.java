package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.llm.LlmPromptRegistry;
import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogLevel;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 制作备用皮革盔甲套装：当背包缺少对应部位的盔甲时，自动合成一套备用（装备 + 可出售）。
 */
public class CraftArmorGoal implements IGoal {

    public static final String LLM_USAGE_DESC =
        "CraftArmorGoal: when safe and backpack lacks spare leather armor pieces, craft a full leather set.";

    static {
        LlmPromptRegistry.register(LLM_USAGE_DESC);
    }

    private static final float PRIORITY = 0.18F; // 略高于 gather_material/idle
    private static final int COOLDOWN_TICKS = 200; // 10s 冷却，避免刷物品
    private static final float MATERIAL_COST_PER_PIECE = 3.0F;
    private static final List<Item> LEATHER_SET = List.of(
        Items.LEATHER_HELMET,
        Items.LEATHER_CHESTPLATE,
        Items.LEATHER_LEGGINGS,
        Items.LEATHER_BOOTS
    );

    private long nextAllowedGameTime;
    private boolean finished;

    @Override
    public String getName() {
        return "craft_armor";
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

        int crafted = craftMissingPieces(npc, mind);
        finished = true;

        if (crafted > 0) {
            MindLog.decision(
                MindLogLevel.INFO,
                "CraftArmorGoal 制作皮革盔甲 x{}，消耗 {} 材料，当前材料点 {}",
                crafted,
                crafted * MATERIAL_COST_PER_PIECE,
                npc.getMaterial()
            );
        } else {
            MindLog.decision(
                MindLogLevel.WARN,
                "CraftArmorGoal 材料不足，无法制作缺失盔甲"
            );
        }
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        // 无需额外逻辑，制作在 start 中一次性完成
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        // 无需清理
    }

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        if (!(entity instanceof CustomNpcEntity npc)) {
            return true;
        }
        return finished || !hasMissingPieces(mind);
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
        return hasMissingPieces(mind);
    }

    private boolean hasMissingPieces(INpcMind mind) {
        var inventory = mind.getInventory();
        for (Item piece : LEATHER_SET) {
            if (!hasInInventory(inventory, piece)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasInInventory(
        com.Kizunad.customNPCs.ai.inventory.NpcInventory inventory,
        Item item
    ) {
        return inventory.anyMatch(stack -> stack.is(item));
    }

    private int craftMissingPieces(CustomNpcEntity npc, INpcMind mind) {
        var inventory = mind.getInventory();
        int crafted = 0;
        float material = npc.getMaterial();

        for (Item piece : LEATHER_SET) {
            if (hasInInventory(inventory, piece)) {
                continue;
            }
            if (material < MATERIAL_COST_PER_PIECE) {
                break;
            }
            ItemStack stack = new ItemStack(piece);
            ItemStack remaining = inventory.addItem(stack);
            material -= MATERIAL_COST_PER_PIECE;
            npc.setMaterial(material);

            if (!remaining.isEmpty()) {
                // 背包满则掉落在地，避免吞物
                npc.spawnAtLocation(remaining);
                MindLog.execution(
                    MindLogLevel.WARN,
                    "CraftArmorGoal 背包空间不足，盔甲已掉落: {}",
                    remaining.getHoverName().getString()
                );
            }
            crafted++;
        }

        return crafted;
    }
}
