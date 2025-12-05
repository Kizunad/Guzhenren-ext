package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.inventory.NpcInventory;
import com.Kizunad.customNPCs.ai.llm.LlmPromptRegistry;
import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogLevel;
import com.Kizunad.customNPCs.ai.util.ArmorEnchantmentUtil;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * 盔甲基础附魔：消耗材料为背包盔甲随机附加若干 I 级附魔。
 * <p>
 * 特性：
 * - 支持保护、荆棘、耐久、爆炸/火焰/弹射物保护多重共存。
 * - 每次对单件盔甲随机挑选若干缺失附魔，逐一扣除材料。
 * - 仅在安全、冷却结束且材料足够时触发。
 * </p>
 */
public class EnchantArmorGoal implements IGoal {

    public static final String LLM_USAGE_DESC =
        "EnchantArmorGoal: spend material to add random level-I armor enchantments (protection, thorns, " +
        "unbreaking, blast/fire/projectile protection) allowing multiple protections.";

    static {
        LlmPromptRegistry.register(LLM_USAGE_DESC);
    }

    private static final float PRIORITY = 0.20F;
    private static final int COOLDOWN_TICKS = 200;
    private static final float MATERIAL_COST_PER_ENCHANT = 4.0F;
    private static final int TARGET_LEVEL = 1;

    private long nextAllowedGameTime;
    private boolean finished;

    @Override
    public String getName() {
        return "enchant_armor";
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

        NpcInventory inventory = mind.getInventory();
        HolderGetter<Enchantment> enchantments = entity
            .level()
            .registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT);
        float material = npc.getMaterial();
        int applied = 0;
        RandomSource random = entity.getRandom();

        for (int i = 0; i < inventory.size(); i++) {
            if (material < MATERIAL_COST_PER_ENCHANT) {
                break;
            }
            ItemStack stack = inventory.getItem(i);
            if (!ArmorEnchantmentUtil.isEnchantableArmor(stack)) {
                continue;
            }
            List<Holder<Enchantment>> missing = ArmorEnchantmentUtil.findMissingEnchantments(
                stack,
                enchantments
            );
            if (missing.isEmpty()) {
                continue;
            }

            int maxAffordable = Math.min(
                missing.size(),
                (int) (material / MATERIAL_COST_PER_ENCHANT)
            );
            if (maxAffordable <= 0) {
                continue;
            }

            int toApply = 1 + random.nextInt(maxAffordable);
            for (int j = 0; j < toApply; j++) {
                int pick = random.nextInt(missing.size());
                Holder<Enchantment> enchantment = missing.remove(pick);
                stack.enchant(enchantment, TARGET_LEVEL);
                material -= MATERIAL_COST_PER_ENCHANT;
                applied++;

                if (material < MATERIAL_COST_PER_ENCHANT || missing.isEmpty()) {
                    break;
                }
            }
        }

        npc.setMaterial(material);
        finished = true;

        if (applied > 0) {
            float cost = applied * MATERIAL_COST_PER_ENCHANT;
            MindLog.decision(
                MindLogLevel.INFO,
                "EnchantArmorGoal 为盔甲新增基础附魔 x{}，消耗 {} 材料，剩余 {}",
                applied,
                cost,
                material
            );
        } else {
            MindLog.decision(
                MindLogLevel.WARN,
                "EnchantArmorGoal 未找到可附魔盔甲或材料不足"
            );
        }
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        // 一次性执行，无需额外逻辑
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
        return finished ||
            npc.getMaterial() < MATERIAL_COST_PER_ENCHANT ||
            !hasEnchantableArmor(mind, entity);
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
        return npc.getMaterial() >= MATERIAL_COST_PER_ENCHANT && hasEnchantableArmor(
            mind,
            entity
        );
    }

    private boolean hasEnchantableArmor(INpcMind mind, LivingEntity entity) {
        HolderGetter<Enchantment> enchantments = entity
            .level()
            .registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT);
        NpcInventory inventory = mind.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (
                ArmorEnchantmentUtil.isEnchantableArmor(stack) &&
                !ArmorEnchantmentUtil
                    .findMissingEnchantments(stack, enchantments)
                    .isEmpty()
            ) {
                return true;
            }
        }
        return false;
    }
}
