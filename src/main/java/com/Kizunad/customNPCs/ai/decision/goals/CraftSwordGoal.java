package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.interaction.MaterialValueManager;
import com.Kizunad.customNPCs.ai.llm.LlmPromptRegistry;
import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogLevel;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import java.util.Set;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 闲时制作备用木剑：背包没有剑时消耗材料合成一把基础木剑。
 */
public class CraftSwordGoal implements IGoal {

    public static final String LLM_USAGE_DESC =
        "CraftSwordGoal: when safe and inventory lacks any sword, craft a wooden sword using material points.";

    static {
        LlmPromptRegistry.register(LLM_USAGE_DESC);
    }

    private static final float PRIORITY = 0.2F; // 与盾牌制作相近，略高于制作盔甲
    private static final int COOLDOWN_TICKS = 200;
    private static final Set<Item> SWORDS = Set.of(
        Items.WOODEN_SWORD,
        Items.STONE_SWORD,
        Items.GOLDEN_SWORD,
        Items.IRON_SWORD,
        Items.DIAMOND_SWORD,
        Items.NETHERITE_SWORD
    );

    private long nextAllowedGameTime;
    private boolean finished;

    @Override
    public String getName() {
        return "craft_sword";
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

        boolean crafted = craftSword(npc, mind);
        finished = true;

        if (crafted) {
            MindLog.decision(
                MindLogLevel.INFO,
                "CraftSwordGoal 制作木剑成功，消耗 {} 材料，剩余 {}",
                getBaseSwordCost(),
                npc.getMaterial()
            );
        } else {
            MindLog.decision(
                MindLogLevel.WARN,
                "CraftSwordGoal 材料不足或成本配置缺失，跳过"
            );
        }
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        // 制作逻辑在 start 中一次性完成
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
        return finished || hasSwordInInventory(mind);
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
        float cost = getBaseSwordCost();
        if (cost <= 0.0F || npc.getMaterial() < cost) {
            return false;
        }
        return !hasSwordInInventory(mind);
    }

    private boolean hasSwordInInventory(INpcMind mind) {
        return mind.getInventory().anyMatch(stack -> isSword(stack.getItem()));
    }

    private boolean isSword(Item item) {
        return SWORDS.contains(item);
    }

    private boolean craftSword(CustomNpcEntity npc, INpcMind mind) {
        float cost = getBaseSwordCost();
        if (
            cost <= 0.0F ||
            npc.getMaterial() < cost ||
            hasSwordInInventory(mind)
        ) {
            return false;
        }

        ItemStack stack = new ItemStack(Items.WOODEN_SWORD);
        ItemStack remaining = mind.getInventory().addItem(stack);
        npc.setMaterial(npc.getMaterial() - cost);

        if (!remaining.isEmpty()) {
            npc.spawnAtLocation(remaining);
            MindLog.execution(
                MindLogLevel.WARN,
                "CraftSwordGoal 背包已满，木剑已掉落"
            );
        }
        return true;
    }

    private float getBaseSwordCost() {
        return MaterialValueManager.getInstance().getMaterialValue(
            new ItemStack(Items.WOODEN_SWORD)
        );
    }
}
