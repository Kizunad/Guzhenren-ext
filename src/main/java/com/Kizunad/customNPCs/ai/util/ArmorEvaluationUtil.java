package com.Kizunad.customNPCs.ai.util;

import com.Kizunad.customNPCs.ai.inventory.NpcInventory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

/**
 * 盔甲评分与择优工具。
 * <p>
 * 评分因素：
 * - 防御值、韧性
 * - 击退抗性
 * - 附魔总值（正向）与诅咒（负向）
 * - 耐久比例（轻权重）
 */
@SuppressWarnings("checkstyle:MagicNumber")
public final class ArmorEvaluationUtil {

    public record ArmorPreference(
        double armorWeight,
        double toughnessWeight,
        double knockbackWeight,
        double enchantWeight,
        double cursePenaltyWeight,
        double durabilityWeight
    ) {
        public static ArmorPreference defaults() {
            return new ArmorPreference(1.0, 0.65, 10.0, 0.35, 2.0, 0.1);
        }
    }

    public record ArmorUpgrade(
        int inventorySlot,
        EquipmentSlot slot,
        ItemStack stack,
        double improvement,
        double targetScore,
        double currentScore
    ) {}

    private ArmorEvaluationUtil() {}

    /**
     * 计算盔甲评分。
     * @param stack 盔甲物品
     * @param preference 权重配置
     * @return 综合评分（非盔甲为0）
     */
    public static double calculateArmorScore(
        ItemStack stack,
        ArmorPreference preference
    ) {
        if (!(stack.getItem() instanceof ArmorItem armorItem)) {
            return 0.0;
        }

        double score = 0.0;
        score += armorItem.getDefense() * preference.armorWeight();
        score += armorItem.getToughness() * preference.toughnessWeight();

        // 以防未来需要扩展击退/附魔: 当前版本简化为防御+韧性+耐久

        // 耐久度（比例）
        if (stack.isDamageableItem()) {
            double durabilityRatio =
                (stack.getMaxDamage() - stack.getDamageValue()) /
                (double) stack.getMaxDamage();
            score += durabilityRatio * preference.durabilityWeight();
        }

        return score;
    }

    public static double calculateArmorScore(ItemStack stack) {
        return calculateArmorScore(stack, ArmorPreference.defaults());
    }

    /**
     * 查找背包中最优的盔甲升级方案。
     */
    public static ArmorUpgrade findBestUpgrade(
        NpcInventory inventory,
        LivingEntity entity
    ) {
        return findBestUpgrade(inventory, entity, ArmorPreference.defaults());
    }

    public static ArmorUpgrade findBestUpgrade(
        NpcInventory inventory,
        LivingEntity entity,
        ArmorPreference preference
    ) {
        double bestImprovement = 0.0;
        ArmorUpgrade best = null;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmor()) {
                continue;
            }
            double currentScore = calculateArmorScore(
                entity.getItemBySlot(slot),
                preference
            );

            for (int i = 0; i < inventory.size(); i++) {
                ItemStack candidate = inventory.getItem(i);
                if (!(candidate.getItem() instanceof ArmorItem armorItem)) {
                    continue;
                }
                if (armorItem.getEquipmentSlot() != slot) {
                    continue;
                }

                double candidateScore = calculateArmorScore(
                    candidate,
                    preference
                );
                double improvement = candidateScore - currentScore;
                if (improvement > bestImprovement + 1.0e-3) {
                    bestImprovement = improvement;
                    best = new ArmorUpgrade(
                        i,
                        slot,
                        candidate,
                        improvement,
                        candidateScore,
                        currentScore
                    );
                }
            }
        }

        return best;
    }

    public static boolean hasBetterArmor(NpcInventory inventory, LivingEntity entity) {
        return findBestUpgrade(inventory, entity) != null;
    }

    public static double totalEquippedScore(LivingEntity entity) {
        double total = 0.0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmor()) {
                continue;
            }
            total += calculateArmorScore(entity.getItemBySlot(slot));
        }
        return total;
    }
}
