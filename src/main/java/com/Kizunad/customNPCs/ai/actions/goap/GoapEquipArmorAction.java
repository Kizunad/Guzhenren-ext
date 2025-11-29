package com.Kizunad.customNPCs.ai.actions.goap;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.common.EquipArmorAction;
import com.Kizunad.customNPCs.ai.actions.config.ActionConfig;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.ai.util.ArmorEvaluationUtil;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.LivingEntity;

/**
 * GOAP 装备盔甲动作包装。
 */
@SuppressWarnings("checkstyle:MagicNumber")
public class GoapEquipArmorAction implements IGoapAction {

    private static final float BASE_COST = 2.5f;

    private final EquipArmorAction wrappedAction;
    private final WorldState preconditions;
    private final WorldState effects;
    private final float cost;

    /**
     * @param improvementScore 盔甲升级带来的评分提升，用于调整代价
     * @param desireWeight 越高表示越愿意为更大提升付出行动
     */
    public GoapEquipArmorAction(double improvementScore, double desireWeight) {
        this(
            new EquipArmorAction(ArmorEvaluationUtil.ArmorPreference.defaults()),
            improvementScore,
            desireWeight
        );
    }

    public GoapEquipArmorAction(double improvementScore) {
        this(
            improvementScore,
            ActionConfig.getInstance().getArmorDesireWeight()
        );
    }

    public GoapEquipArmorAction() {
        this(
            0.0,
            ActionConfig.getInstance().getArmorDesireWeight()
        );
    }

    public GoapEquipArmorAction(
        EquipArmorAction wrappedAction,
        double improvementScore,
        double desireWeight
    ) {
        this.wrappedAction = wrappedAction;
        this.preconditions = new WorldState();
        this.preconditions.setState(WorldStateKeys.ARMOR_BETTER_AVAILABLE, true);

        this.effects = new WorldState();
        this.effects.setState(WorldStateKeys.ARMOR_OPTIMIZED, true);

        float adjusted = (float) (BASE_COST - improvementScore * desireWeight);
        this.cost = Math.max(1.0f, adjusted);
    }

    @Override
    public WorldState getPreconditions() {
        return preconditions;
    }

    @Override
    public WorldState getEffects() {
        return effects;
    }

    @Override
    public float getCost() {
        return cost;
    }

    @Override
    public ActionStatus tick(INpcMind mind, LivingEntity entity) {
        return wrappedAction.tick(mind, entity);
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        wrappedAction.start(mind, entity);
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        wrappedAction.stop(mind, entity);
    }

    @Override
    public boolean canInterrupt() {
        return wrappedAction.canInterrupt();
    }

    @Override
    public String getName() {
        return "goap_equip_armor";
    }
}
