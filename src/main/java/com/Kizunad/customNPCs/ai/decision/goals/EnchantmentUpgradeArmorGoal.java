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
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * 盔甲附魔升级：消耗材料随机选择一个未满级的目标附魔并提升 1 级。
 * <p>
 * 特性：
 * - 缺失时可新增 I 级保护/荆棘/耐久/爆炸保护/火焰保护/弹射物保护。
 * - 不会选择已达上限的附魔；如果全部满级则跳过。
 * - 允许多重保护系共存，使用材料点做经济约束。
 * </p>
 */
public class EnchantmentUpgradeArmorGoal implements IGoal {

    public static final String LLM_USAGE_DESC =
        "EnchantmentUpgradeArmorGoal: spend material to randomly upgrade one allowed armor enchantment by +1; " +
        "skips maxed entries and can add missing level-1 protection/thorns/unbreaking/blast/fire/projectile " +
        "protection.";

    static {
        LlmPromptRegistry.register(LLM_USAGE_DESC);
    }

    private static final float PRIORITY = 0.23F;
    private static final int COOLDOWN_TICKS = 200;
    private static final float MATERIAL_COST_PER_UPGRADE = 6.0F;

    private long nextAllowedGameTime;
    private boolean finished;

    @Override
    public String getName() {
        return "enchantment_upgrade_armor";
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

        float material = npc.getMaterial();
        if (material < MATERIAL_COST_PER_UPGRADE) {
            finished = true;
            MindLog.decision(
                MindLogLevel.WARN,
                "EnchantmentUpgradeArmorGoal 材料不足，无法升级附魔"
            );
            return;
        }

        HolderGetter<Enchantment> enchantments = entity
            .level()
            .registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT);
        List<UpgradeOption> options = collectUpgradeableOptions(
            mind.getInventory(),
            enchantments
        );
        if (options.isEmpty()) {
            finished = true;
            MindLog.decision(
                MindLogLevel.WARN,
                "EnchantmentUpgradeArmorGoal 无可升级或新增的附魔"
            );
            return;
        }

        RandomSource random = entity.getRandom();
        UpgradeOption option = options.get(random.nextInt(options.size()));
        int targetLevel = Math.min(option.currentLevel() + 1, option.maxLevel());
        option.stack().enchant(option.enchantment(), targetLevel);

        npc.setMaterial(material - MATERIAL_COST_PER_UPGRADE);
        finished = true;

        MindLog.decision(
            MindLogLevel.INFO,
            "EnchantmentUpgradeArmorGoal 将 {} 提升至等级 {}，消耗 {} 材料，剩余 {}",
            describeEnchantment(option.enchantment()),
            targetLevel,
            MATERIAL_COST_PER_UPGRADE,
            npc.getMaterial()
        );
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
        if (!(entity instanceof CustomNpcEntity npc)) {
            return true;
        }
        return finished ||
            npc.getMaterial() < MATERIAL_COST_PER_UPGRADE ||
            !hasUpgradeableOption(mind.getInventory(), entity);
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
        return npc.getMaterial() >= MATERIAL_COST_PER_UPGRADE && hasUpgradeableOption(
            mind.getInventory(),
            entity
        );
    }

    private List<UpgradeOption> collectUpgradeableOptions(
        NpcInventory inventory,
        HolderGetter<Enchantment> enchantments
    ) {
        List<UpgradeOption> options = new ArrayList<>();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!ArmorEnchantmentUtil.isEnchantableArmor(stack)) {
                continue;
            }
            List<ArmorEnchantmentUtil.EnchantmentLevel> upgrades = ArmorEnchantmentUtil.collectUpgradeableEnchantments(
                stack,
                enchantments
            );
            for (ArmorEnchantmentUtil.EnchantmentLevel level : upgrades) {
                options.add(
                    new UpgradeOption(
                        i,
                        stack,
                        level.enchantment(),
                        level.level(),
                        level.maxLevel()
                    )
                );
            }
        }
        return options;
    }

    private boolean hasUpgradeableOption(
        NpcInventory inventory,
        LivingEntity entity
    ) {
        HolderGetter<Enchantment> enchantments = entity
            .level()
            .registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT);
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!ArmorEnchantmentUtil.isEnchantableArmor(stack)) {
                continue;
            }
            if (
                !ArmorEnchantmentUtil
                    .collectUpgradeableEnchantments(stack, enchantments)
                    .isEmpty()
            ) {
                return true;
            }
        }
        return false;
    }

    private String describeEnchantment(Holder<Enchantment> enchantment) {
        return enchantment
            .unwrapKey()
            .map(key -> key.location().toString())
            .orElse(enchantment.value().toString());
    }

    private record UpgradeOption(
        int slot,
        ItemStack stack,
        Holder<Enchantment> enchantment,
        int currentLevel,
        int maxLevel
    ) {}
}
