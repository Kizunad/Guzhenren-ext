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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 压缩盔甲库存：当背包内某部位盔甲超过两件时，只保留最高品质的两件，其余拆解为材料点。
 */
public class CompressArmorGoal implements IGoal {

    public static final String LLM_USAGE_DESC =
        "CompressArmorGoal: keep at most two best armor pieces per slot in inventory, "
            + "recycle the rest into material points.";

    static {
        LlmPromptRegistry.register(LLM_USAGE_DESC);
    }

    private static final float PRIORITY = 0.16F;
    private static final int COOLDOWN_TICKS = 200;
    private static final int MAX_ARMOR_PER_SLOT = 2;
    private static final Comparator<ArmorEntry> ENTRY_ORDER = Comparator
        .comparingInt(ArmorEntry::tier)
        .reversed()
        .thenComparingInt(ArmorEntry::damage)
        .thenComparingInt(ArmorEntry::slotIndex);

    private long nextAllowedGameTime;
    private boolean finished;

    @Override
    public String getName() {
        return "compress_armor";
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

        CompressResult result = compressArmor(npc, mind.getInventory());
        finished = true;

        if (result.removed() > 0) {
            MindLog.decision(
                MindLogLevel.INFO,
                "CompressArmorGoal 拆解多余盔甲 {} 件，返还材料 {}，当前材料 {}",
                result.removed(),
                result.gained(),
                npc.getMaterial()
            );
        } else {
            MindLog.decision(
                MindLogLevel.DEBUG,
                "CompressArmorGoal 未发现多余盔甲"
            );
        }
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        // 一次性操作，无需逐 tick 逻辑
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        // 无额外清理
    }

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        if (!(entity instanceof CustomNpcEntity)) {
            return true;
        }
        return finished || !hasOverflowArmor(mind.getInventory());
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
        return hasOverflowArmor(mind.getInventory());
    }

    private boolean hasOverflowArmor(NpcInventory inventory) {
        Map<EquipmentSlot, Integer> counts = new EnumMap<>(EquipmentSlot.class);
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getItem(i);
            EquipmentSlot slot = ArmorTierHelper.slotOf(stack);
            int tier = ArmorTierHelper.tierOf(stack);
            if (slot == null || tier < 0) {
                continue;
            }
            int next = counts.getOrDefault(slot, 0) + 1;
            if (next > MAX_ARMOR_PER_SLOT) {
                return true;
            }
            counts.put(slot, next);
        }
        return false;
    }

    private CompressResult compressArmor(
        CustomNpcEntity npc,
        NpcInventory inventory
    ) {
        Map<EquipmentSlot, List<ArmorEntry>> grouped = groupBySlot(inventory);
        int removed = 0;
        float gained = 0.0F;

        for (List<ArmorEntry> entries : grouped.values()) {
            entries.sort(ENTRY_ORDER);
            for (int i = MAX_ARMOR_PER_SLOT; i < entries.size(); i++) {
                ArmorEntry extra = entries.get(i);
                float gain = dismantle(
                    npc,
                    inventory,
                    extra.slotIndex(),
                    extra.stack()
                );
                removed++;
                gained += gain;
            }
        }
        return new CompressResult(removed, gained);
    }

    private Map<EquipmentSlot, List<ArmorEntry>> groupBySlot(
        NpcInventory inventory
    ) {
        Map<EquipmentSlot, List<ArmorEntry>> grouped = new EnumMap<>(
            EquipmentSlot.class
        );
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getItem(i);
            EquipmentSlot slot = ArmorTierHelper.slotOf(stack);
            int tier = ArmorTierHelper.tierOf(stack);
            if (slot == null || tier < 0) {
                continue;
            }
            grouped
                .computeIfAbsent(slot, key -> new ArrayList<>())
                .add(new ArmorEntry(i, stack, tier, stack.getDamageValue()));
        }
        return grouped;
    }

    private float dismantle(
        CustomNpcEntity npc,
        NpcInventory inventory,
        int slotIndex,
        ItemStack stack
    ) {
        float gain = MaterialValueManager
            .getInstance()
            .getMaterialValue(stack);
        inventory.setItem(slotIndex, ItemStack.EMPTY);
        if (gain > 0.0F) {
            npc.addMaterial(gain);
        }
        MindLog.execution(
            MindLogLevel.INFO,
            "CompressArmorGoal 拆解槽位 {}，转化材料 {}",
            slotIndex,
            gain
        );
        return gain;
    }

    private record ArmorEntry(
        int slotIndex,
        ItemStack stack,
        int tier,
        int damage
    ) {}

    private record CompressResult(int removed, float gained) {}
}
