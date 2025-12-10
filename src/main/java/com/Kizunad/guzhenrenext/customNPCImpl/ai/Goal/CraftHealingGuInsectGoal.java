package com.Kizunad.guzhenrenext.customNPCImpl.ai.Goal;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.interaction.MaterialValueManager;
import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogLevel;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import com.Kizunad.guzhenrenext.customNPCImpl.util.GuCultivationHelper;
import com.Kizunad.guzhenrenext.customNPCImpl.util.GuInsectUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.guzhenren.network.GuzhenrenModVariables;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 制作治疗型蛊虫（*_1 标签）。
 * <p>
 * 根据转数选择对应治疗蛊虫标签，维持至少一只备用。
 * </p>
 */
public class CraftHealingGuInsectGoal extends AbstractGuzhenrenGoal {

    private static final float PRIORITY = 0.21F;
    private static final int COOLDOWN_TICKS = 2000;
    private static final int MIN_HEAL_GU_STOCK = 10;
    private static final float FALLBACK_MATERIAL_COST = 8.0F;
    private static final double TURN_COST_MULTIPLIER_BASE = 2.0D;
    private static final Map<Integer, TagKey<Item>> TURN_TAGS = Map.of(
        1,
        tag("guzhenren:gushiguchong1_1"),
        2,
        tag("guzhenren:gushiguchong2_1"),
        3,
        tag("guzhenren:gushiguchong3_1"),
        4,
        tag("guzhenren:gushiguchong4_1"),
        5,
        tag("guzhenren:gushiguchong5_1")
    );

    private long nextAllowedGameTime;
    private boolean finished;

    public CraftHealingGuInsectGoal() {
        super("craft_healing_gu_insect");
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

        CraftOption option = findCraftOption(entity, mind);
        if (option == null) {
            finished = true;
            return;
        }
        if (!canStore(mind, option.item())) {
            finished = true;
            MindLog.decision(
                MindLogLevel.WARN,
                "CraftHealingGuInsectGoal 背包已满，跳过制作蛊虫: {}",
                BuiltInRegistries.ITEM.getKey(option.item())
            );
            return;
        }
        float material = npc.getMaterial();
        if (material < option.cost()) {
            finished = true;
            MindLog.decision(
                MindLogLevel.WARN,
                "CraftHealingGuInsectGoal 材料不足，需 {} 点，当前 {}",
                option.cost(),
                material
            );
            return;
        }

        ItemStack stack = new ItemStack(option.item());
        ItemStack remaining = mind.getInventory().addItem(stack);
        npc.setMaterial(material - option.cost());
        finished = true;

        if (!remaining.isEmpty()) {
            npc.spawnAtLocation(remaining);
            MindLog.execution(
                MindLogLevel.WARN,
                "CraftHealingGuInsectGoal 背包已满，蛊虫已掉落: {}",
                remaining.getHoverName().getString()
            );
        }

        MindLog.decision(
            MindLogLevel.INFO,
            "CraftHealingGuInsectGoal 制作蛊虫成功: {}，消耗 {} 材料，剩余 {}，标签 {}",
            BuiltInRegistries.ITEM.getKey(option.item()),
            option.cost(),
            npc.getMaterial(),
            option.tag().location()
        );
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {}

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {}

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        return finished || hasEnoughStock(mind);
    }

    private boolean canEngage(INpcMind mind, LivingEntity entity) {
        if (!(entity instanceof CustomNpcEntity npc)) {
            return false;
        }
        if (entity.level().isClientSide()) {
            return false;
        }
        if (!isGuMaster(entity)) {
            return false;
        }
        if (entity.level().getGameTime() < nextAllowedGameTime) {
            return false;
        }
        if (mind.getMemory().hasMemory(WorldStateKeys.IN_DANGER)) {
            return false;
        }
        if (hasEnoughStock(mind)) {
            return false;
        }
        CraftOption option = findCraftOption(entity, mind);
        if (option == null) {
            return false;
        }
        if (npc.getMaterial() < option.cost()) {
            return false;
        }
        return canStore(mind, option.item());
    }

    private boolean hasEnoughStock(INpcMind mind) {
        int count = 0;
        for (int i = 0; i < mind.getInventory().getMainSize(); i++) {
            ItemStack stack = mind.getInventory().getItem(i);
            if (GuInsectUtil.isHealGu(stack)) {
                count++;
                if (count >= MIN_HEAL_GU_STOCK) {
                    return true;
                }
            }
        }
        return false;
    }

    private CraftOption findCraftOption(LivingEntity entity, INpcMind mind) {
        GuzhenrenModVariables.PlayerVariables vars =
            GuCultivationHelper.getVariables(entity);
        int turn = clampTurn(vars);
        TagKey<Item> tag = TURN_TAGS.get(turn);
        if (tag == null) {
            return null;
        }

        List<Item> candidates = getItemsByTag(tag);
        if (candidates.isEmpty()) {
            MindLog.decision(
                MindLogLevel.WARN,
                "CraftHealingGuInsectGoal 未找到标签 {} 对应物品",
                tag.location()
            );
            return null;
        }

        MaterialValueManager valueManager = MaterialValueManager.getInstance();
        List<CraftOption> options = new ArrayList<>();
        double multiplier = Math.pow(
            TURN_COST_MULTIPLIER_BASE,
            Math.max(0, turn - 1)
        );
        for (Item item : candidates) {
            ItemStack stack = new ItemStack(item);
            float rawCost = valueManager.getMaterialValue(stack);
            float effectiveCost = (float) ((rawCost > 0.0F
                    ? rawCost
                    : FALLBACK_MATERIAL_COST) *
                multiplier);
            options.add(new CraftOption(item, tag, effectiveCost));
        }

        options.sort(Comparator.comparing(CraftOption::cost));
        return options.isEmpty() ? null : options.getFirst();
    }

    private List<Item> getItemsByTag(TagKey<Item> tag) {
        return BuiltInRegistries.ITEM.getTag(tag)
            .map(holderSet ->
                holderSet
                    .stream()
                    .map(Holder::value)
                    .filter(Objects::nonNull)
                    .toList()
            )
            .orElse(List.of());
    }

    private int clampTurn(GuzhenrenModVariables.PlayerVariables vars) {
        double raw = vars == null ? 1.0D : vars.zhuanshu;
        int turn = (int) Math.floor(Math.max(1.0D, raw));
        return Math.min(turn, TURN_TAGS.size());
    }

    private static TagKey<Item> tag(String id) {
        return TagKey.create(Registries.ITEM, ResourceLocation.parse(id));
    }

    private boolean canStore(INpcMind mind, Item item) {
        ItemStack probe = new ItemStack(item);
        var inventory = mind.getInventory();
        for (int i = 0; i < inventory.getMainSize(); i++) {
            ItemStack slot = inventory.getItem(i);
            if (slot.isEmpty()) {
                return true;
            }
            if (
                ItemStack.isSameItemSameComponents(slot, probe) &&
                slot.getCount() < slot.getMaxStackSize()
            ) {
                return true;
            }
        }
        return false;
    }

    private record CraftOption(Item item, TagKey<Item> tag, float cost) {}
}
