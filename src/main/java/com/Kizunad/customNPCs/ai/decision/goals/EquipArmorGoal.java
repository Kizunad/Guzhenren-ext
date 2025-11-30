package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.actions.config.ActionConfig;
import com.Kizunad.customNPCs.ai.actions.goap.GoapEquipArmorAction;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.ai.util.ArmorEvaluationUtil;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;

/**
 * 自动装备更优盔甲的 GOAP 目标。
 * <p>
 * 该目标会评估背包中的盔甲,并在发现更优的盔甲时自动装备。
 * 优先级基于盔甲改进程度动态计算。
 * </p>
 */
public class EquipArmorGoal extends PlanBasedGoal {

    /** 默认基础优先级 */
    private static final float DEFAULT_BASE_PRIORITY = 0.6f;
    
    /** 改进度缩放因子,用于将盔甲改进度转换为优先级增量 */
    private static final double IMPROVEMENT_SCALE_FACTOR = 0.05;

    /** 基础优先级 */
    private final float basePriority;
    
    /** 欲望权重,用于调整目标的重要性 */
    private final double desireWeight;

    /**
     * 使用默认配置创建装备盔甲目标。
     * 使用默认基础优先级和配置文件中的欲望权重。
     */
    public EquipArmorGoal() {
        this(DEFAULT_BASE_PRIORITY, ActionConfig.getInstance().getArmorDesireWeight());
    }

    /**
     * 使用指定基础优先级创建装备盔甲目标。
     *
     * @param basePriority 基础优先级
     */
    public EquipArmorGoal(float basePriority) {
        this(basePriority, 1.0);
    }

    /**
     * 使用完整参数创建装备盔甲目标。
     *
     * @param basePriority 基础优先级
     * @param desireWeight 欲望权重
     */
    public EquipArmorGoal(float basePriority, double desireWeight) {
        this.basePriority = basePriority;
        this.desireWeight = desireWeight;
    }

    /**
     * 计算装备盔甲目标的优先级。
     * <p>
     * 优先级基于盔甲改进程度计算:
     * priority = min(1.0, basePriority + improvement * IMPROVEMENT_SCALE_FACTOR)
     * </p>
     *
     * @param mind NPC的思维接口
     * @param entity NPC实体
     * @return 优先级值,范围[0.0, 1.0]
     */
    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        ArmorEvaluationUtil.ArmorUpgrade upgrade = ArmorEvaluationUtil.findBestUpgrade(
            mind.getInventory(),
            entity
        );
        if (upgrade == null) {
            return 0.0f;
        }
        double improvement = upgrade.improvement();
        double scaled = Math.min(1.0, basePriority + improvement * IMPROVEMENT_SCALE_FACTOR);
        return (float) scaled;
    }

    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        return ArmorEvaluationUtil.hasBetterArmor(mind.getInventory(), entity);
    }

    @Override
    public WorldState getDesiredState(INpcMind mind, LivingEntity entity) {
        WorldState desired = new WorldState();
        desired.setState(WorldStateKeys.ARMOR_OPTIMIZED, true);
        return desired;
    }

    @Override
    protected WorldState getCurrentState(INpcMind mind, LivingEntity entity) {
        WorldState current = new WorldState();
        current.setState(
            WorldStateKeys.ARMOR_BETTER_AVAILABLE,
            ArmorEvaluationUtil.hasBetterArmor(mind.getInventory(), entity)
        );
        current.setState(
            WorldStateKeys.ARMOR_SCORE,
            ArmorEvaluationUtil.totalEquippedScore(entity)
        );
        return current;
    }

    @Override
    public List<IGoapAction> getAvailableActions(
        INpcMind mind,
        LivingEntity entity
    ) {
        ArmorEvaluationUtil.ArmorUpgrade upgrade = ArmorEvaluationUtil.findBestUpgrade(
            mind.getInventory(),
            entity
        );
        double improvement = upgrade != null ? upgrade.improvement() : 0.0;
        return Collections.singletonList(
            new GoapEquipArmorAction(improvement, desireWeight)
        );
    }

    @Override
    public String getName() {
        return "equip_armor";
    }
}
