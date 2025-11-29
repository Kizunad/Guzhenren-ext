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
 */
@SuppressWarnings("checkstyle:MagicNumber")
public class EquipArmorGoal extends PlanBasedGoal {

    private final float basePriority;
    private final double desireWeight;

    public EquipArmorGoal() {
        this(0.6f, ActionConfig.getInstance().getArmorDesireWeight());
    }

    public EquipArmorGoal(float basePriority) {
        this(basePriority, 1.0);
    }

    public EquipArmorGoal(float basePriority, double desireWeight) {
        this.basePriority = basePriority;
        this.desireWeight = desireWeight;
    }

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
        double scaled = Math.min(1.0, basePriority + improvement * 0.05);
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
