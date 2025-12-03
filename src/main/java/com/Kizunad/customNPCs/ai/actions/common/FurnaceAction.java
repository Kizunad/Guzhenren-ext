package com.Kizunad.customNPCs.ai.actions.common;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.AbstractStandardAction;
import com.Kizunad.customNPCs.ai.inventory.NpcInventory;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

/**
 * 无需方块交互的烧炼动作。
 * <p>
 * 逻辑：从 NPC 背包中取出一份可烹饪的食材与燃料，按配方耗时完成后将产物放回背包。
 * 仅处理可吃的食物，避免将矿物等误烧。
 */
public class FurnaceAction extends AbstractStandardAction {

    private static final int DEFAULT_COOK_TIME = 200;
    private static final int COOK_TIMEOUT_MULTIPLIER = 3;

    private CookCandidate candidate;
    private ItemStack consumedInput = ItemStack.EMPTY;
    private ItemStack consumedFuel = ItemStack.EMPTY;
    private ItemStack produced = ItemStack.EMPTY;
    private int progressTicks;
    private boolean finished;

    public FurnaceAction() {
        super(
            "furnace_cook",
            null,
            DEFAULT_COOK_TIME * COOK_TIMEOUT_MULTIPLIER +
            CONFIG.getTimeoutBufferTicks(),
            0,
            0
        );
    }

    @Override
    protected ActionStatus tickInternal(INpcMind mind, Mob mob) {
        if (!(mob.level() instanceof ServerLevel serverLevel)) {
            return ActionStatus.FAILURE;
        }
        if (candidate == null) {
            if (!prepare(mind, serverLevel)) {
                com.Kizunad.customNPCs.ai.logging.MindLog.execution(
                    com.Kizunad.customNPCs.ai.logging.MindLogLevel.WARN,
                    "烹饪准备失败，背包可能缺少燃料或可烧炼食材"
                );
                return ActionStatus.FAILURE;
            }
        }

        finished = true;
        ItemStack remaining = mind.getInventory().addItem(produced.copy());
        if (!remaining.isEmpty()) {
            mob.spawnAtLocation(remaining);
        }
        ItemStack container = consumedFuel.getCraftingRemainingItem();
        if (!container.isEmpty()) {
            ItemStack leftover = mind.getInventory().addItem(container);
            if (!leftover.isEmpty()) {
                mob.spawnAtLocation(leftover);
            }
        }
        return ActionStatus.SUCCESS;
    }

    @Override
    protected void onStart(INpcMind mind, LivingEntity entity) {
        progressTicks = 0;
        finished = false;
        candidate = null;
        consumedInput = ItemStack.EMPTY;
        consumedFuel = ItemStack.EMPTY;
        produced = ItemStack.EMPTY;
    }

    @Override
    protected void onStop(INpcMind mind, LivingEntity entity) {
        if (finished || !(entity instanceof Mob mob)) {
            return;
        }
        // 中断或失败时归还已取出的物品，避免无故消耗
        returnStack(mind, mob, consumedInput);
        returnStack(mind, mob, consumedFuel);
    }

    @Override
    public boolean canInterrupt() {
        return true;
    }

    /**
     * 扫描背包，查找可烹饪食材与燃料。
     *
     * @param inventory NPC 背包
     * @param level 世界（必须为服务端）
     * @return 候选项
     */
    public static Optional<CookCandidate> findCookCandidate(
        NpcInventory inventory,
        Level level
    ) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return Optional.empty();
        }

        int fuelSlot = findFuelSlot(inventory, -1);
        if (fuelSlot < 0) {
            return Optional.empty();
        }

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            Optional<RecipeHolder<? extends AbstractCookingRecipe>> recipe = getCookingRecipe(
                serverLevel,
                stack
            );
            if (recipe.isEmpty()) {
                continue;
            }
            ItemStack result = recipe
                .get()
                .value()
                .getResultItem(serverLevel.registryAccess());
            if (result.getItem().getFoodProperties(result, null) == null) {
                continue;
            }
            int cookTime = recipe.get().value().getCookingTime();
            if (cookTime <= 0) {
                cookTime = DEFAULT_COOK_TIME;
            }
            int resolvedFuelSlot = fuelSlot;
            if (fuelSlot == i) {
                resolvedFuelSlot = findFuelSlot(inventory, i);
                if (resolvedFuelSlot < 0) {
                    continue;
                }
            }
            return Optional.of(
                new CookCandidate(i, resolvedFuelSlot, cookTime, result.copy())
            );
        }

        return Optional.empty();
    }

    private boolean prepare(INpcMind mind, ServerLevel level) {
        Optional<CookCandidate> optional = findCookCandidate(
            mind.getInventory(),
            level
        );
        if (optional.isEmpty()) {
            return false;
        }
        candidate = optional.get();
        consumedInput = mind.getInventory().removeItem(candidate.inputSlot(), 1);
        consumedFuel = mind.getInventory().removeItem(candidate.fuelSlot(), 1);
        produced = candidate.output();

        com.Kizunad.customNPCs.ai.logging.MindLog.execution(
            com.Kizunad.customNPCs.ai.logging.MindLogLevel.INFO,
            "烹饪准备完成 input:{} fuel:{} output:{} cookTime:{}",
            consumedInput,
            consumedFuel,
            produced,
            candidate.cookTime()
        );

        if (consumedInput.isEmpty() || consumedFuel.isEmpty()) {
            com.Kizunad.customNPCs.ai.logging.MindLog.execution(
                com.Kizunad.customNPCs.ai.logging.MindLogLevel.WARN,
                "烹饪准备失败，物品取出为空 input:{} fuel:{}",
                consumedInput,
                consumedFuel
            );
            return false;
        }
        if (produced.isEmpty()) {
            com.Kizunad.customNPCs.ai.logging.MindLog.execution(
                com.Kizunad.customNPCs.ai.logging.MindLogLevel.WARN,
                "烹饪准备失败，产物为空"
            );
            return false;
        }

        progressTicks = 0;
        return true;
    }

    @SuppressWarnings("unchecked")
    private static Optional<RecipeHolder<? extends AbstractCookingRecipe>> getCookingRecipe(
        ServerLevel level,
        ItemStack stack
    ) {
        RecipeManager recipeManager = level.getRecipeManager();
        return recipeManager
            .getAllRecipesFor(RecipeType.SMELTING)
            .stream()
            .filter(holder -> !holder.value().getIngredients().isEmpty())
            .filter(holder ->
                holder.value().getIngredients().get(0).test(stack)
            )
            .findFirst()
            .map(holder -> (RecipeHolder<? extends AbstractCookingRecipe>) holder);
    }

    private static int findFuelSlot(NpcInventory inventory, int exclude) {
        for (int i = 0; i < inventory.size(); i++) {
            if (i == exclude) {
                continue;
            }
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && AbstractFurnaceBlockEntity.isFuel(stack)) {
                return i;
            }
        }
        return -1;
    }

    private void returnStack(INpcMind mind, Mob mob, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemStack leftover = mind.getInventory().addItem(stack.copy());
        if (!leftover.isEmpty()) {
            mob.spawnAtLocation(leftover);
        }
    }

    /**
     * 烹饪候选信息。
     * inputSlot: 原料所在槽位
     * fuelSlot: 燃料槽位
     * cookTime: 配方耗时
     * output: 烹饪结果
     */
    public record CookCandidate(
        int inputSlot,
        int fuelSlot,
        int cookTime,
        ItemStack output
    ) {}
}
