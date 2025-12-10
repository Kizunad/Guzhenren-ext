package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.interaction.MaterialValueManager;
import com.Kizunad.customNPCs.ai.inventory.InventoryWhitelist;
import com.Kizunad.customNPCs.ai.inventory.NpcInventory;
import com.Kizunad.customNPCs.ai.llm.LlmPromptRegistry;
import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogLevel;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 背包压缩：背包 80% 填充时，将非白名单物品转为材料点，释放空间。
 */
public class CompressInventoryGoal implements IGoal {

    public static final String LLM_USAGE_DESC =
        "CompressInventoryGoal: when inventory is 50%+ full, convert non-whitelisted items into material points; " +
        "skip totems, potions, armor, tools, bows/crossbows, arrows.";

    static {
        LlmPromptRegistry.register(LLM_USAGE_DESC);
    }

    private static final float PRIORITY = 0.32F; // 中高优先级，避免背包爆满
    private static final float TRIGGER_RATIO = 0.5F;
    private static final int COOLDOWN_TICKS = 200;

    private long nextAllowedGameTime;
    private boolean finished;

    @Override
    public String getName() {
        return "compress_inventory";
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

        CompressResult result = compressInventory(npc, mind.getInventory());
        finished = true;

        if (result.removed() > 0) {
            MindLog.decision(
                MindLogLevel.INFO,
                "CompressInventoryGoal 压缩背包，移除 {} 堆，材料 +{}，当前材料 {}",
                result.removed(),
                result.gained(),
                npc.getMaterial()
            );
        } else {
            MindLog.decision(
                MindLogLevel.DEBUG,
                "CompressInventoryGoal 无可压缩物品"
            );
        }
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        // 一次性处理
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        // 无需清理
    }

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        if (!(entity instanceof CustomNpcEntity)) {
            return true;
        }
        return finished || !shouldCompress(mind.getInventory());
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
        return shouldCompress(mind.getInventory());
    }

    private boolean shouldCompress(NpcInventory inventory) {
        return (
            inventory.getFillRatio() >= TRIGGER_RATIO &&
            hasCompressibleItem(inventory)
        );
    }

    private boolean hasCompressibleItem(NpcInventory inventory) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty() || InventoryWhitelist.isWhitelisted(stack)) {
                continue;
            }
            float value = MaterialValueManager.getInstance().getMaterialValue(
                stack
            );
            if (value > 0.0F) {
                return true;
            }
        }
        return false;
    }

    private CompressResult compressInventory(
        CustomNpcEntity npc,
        NpcInventory inventory
    ) {
        int removed = 0;
        float gained = 0.0F;

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty() || InventoryWhitelist.isWhitelisted(stack)) {
                continue;
            }
            float value = MaterialValueManager.getInstance().getMaterialValue(
                stack
            );
            if (value <= 0.0F) {
                continue;
            }

            inventory.setItem(i, ItemStack.EMPTY);
            float gain = value * stack.getCount();
            npc.addMaterial(gain);
            removed++;
            gained += gain;
            MindLog.execution(
                MindLogLevel.INFO,
                "CompressInventoryGoal 拆解槽位 {}，返还材料 {}",
                i,
                gain
            );
        }
        return new CompressResult(removed, gained);
    }

    private record CompressResult(int removed, float gained) {}
}
