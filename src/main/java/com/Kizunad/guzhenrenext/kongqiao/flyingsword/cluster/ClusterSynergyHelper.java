package com.Kizunad.guzhenrenext.kongqiao.flyingsword.cluster;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * 飞剑集群共鸣计算辅助类。
 * <p>
 * 该类专注“读取当前活跃飞剑列表 -> 产出可消费的共鸣结果”，
 * 不直接修改实体状态，便于后续在伤害、UI、网络同步等链路复用同一份结果。
 * </p>
 */
public final class ClusterSynergyHelper {

    /**
     * 基础规则：同名飞剑达到该阈值即触发共鸣。
     */
    public static final int SAME_NAME_THRESHOLD = 3;

    /**
     * 基础规则：触发共鸣时提供的攻击增益（+10%）。
     */
    public static final float SYNERGY_ATTACK_BONUS = 0.10F;

    /**
     * 预留的效果键：同名飞剑攻击增益。
     */
    public static final String EFFECT_KEY_SAME_NAME_ATTACK = "same_name_attack_bonus";

    private ClusterSynergyHelper() {}

    /**
     * 计算当前活跃飞剑的共鸣结果。
     * <p>
     * 当前基础版仅实现：当任意“同名飞剑”数量达到 3 把及以上时，
     * 产出一次 +10% 攻击增益。
     * </p>
     * <p>
     * 说明：基础版采用“命中阈值即一次增益，不按组叠加”，
     * 后续若设计需要可在不改调用方协议的前提下扩展为多效果叠加。
     * </p>
     *
     * @param activeSwords 当前活跃飞剑
     * @return 共鸣结果（可直接消费，也可用于 UI/同步）
     */
    public static ClusterSynergyResult evaluate(List<FlyingSwordEntity> activeSwords) {
        if (activeSwords == null || activeSwords.isEmpty()) {
            return ClusterSynergyResult.empty();
        }

        Map<String, Integer> nameCounter = new HashMap<>();
        for (FlyingSwordEntity sword : activeSwords) {
            if (sword == null) {
                continue;
            }
            String identifier = resolveSwordIdentifier(sword);
            if (identifier.isBlank()) {
                continue;
            }
            nameCounter.merge(identifier, 1, Integer::sum);
        }

        for (Map.Entry<String, Integer> entry : nameCounter.entrySet()) {
            Integer count = entry.getValue();
            if (count == null || count < SAME_NAME_THRESHOLD) {
                continue;
            }

            Map<String, Float> effects = new HashMap<>();
            effects.put(EFFECT_KEY_SAME_NAME_ATTACK, SYNERGY_ATTACK_BONUS);
            List<SynergyTrigger> triggers = new ArrayList<>();
            triggers.add(
                new SynergyTrigger(
                    EFFECT_KEY_SAME_NAME_ATTACK,
                    entry.getKey(),
                    count
                )
            );
            return new ClusterSynergyResult(1.0F + SYNERGY_ATTACK_BONUS, effects, triggers);
        }

        return ClusterSynergyResult.empty();
    }

    /**
     * 解析飞剑“同名”标识。
     * <p>
     * 目前使用展示物品的注册名（namespace:path）作为稳定标识。
     * 选择该方案是为了最小侵入：无需改动既有存档结构与实体同步字段。
     * </p>
     *
     * @param sword 飞剑实体
     * @return 同名判定标识，无法解析时返回空字符串
     */
    private static String resolveSwordIdentifier(FlyingSwordEntity sword) {
        if (sword == null) {
            return "";
        }
        ItemStack stack = sword.getDisplayItemStack();
        if (stack == null || stack.isEmpty()) {
            return "";
        }
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (key == null) {
            return "";
        }
        return key.toString();
    }

    /**
     * 共鸣结果：可扩展的消费结构。
     * <p>
     * - attackMultiplier：伤害链路可直接消费的乘区结果。
     * - effects：效果键到数值的映射，供 UI/网络同步/后续规则扩展。
     * - triggers：触发详情，便于排查与调试。
     * </p>
     */
    public record ClusterSynergyResult(
        float attackMultiplier,
        Map<String, Float> effects,
        List<SynergyTrigger> triggers
    ) {

        /**
         * 空结果工厂，统一“未触发共鸣”语义。
         */
        public static ClusterSynergyResult empty() {
            return new ClusterSynergyResult(1.0F, Map.of(), List.of());
        }
    }

    /**
     * 单条触发详情。
     *
     * @param effectKey 共鸣效果键
     * @param sourceIdentifier 触发来源标识（同名标识）
     * @param matchedCount 触发时命中数量
     */
    public record SynergyTrigger(
        String effectKey,
        String sourceIdentifier,
        int matchedCount
    ) {}
}
