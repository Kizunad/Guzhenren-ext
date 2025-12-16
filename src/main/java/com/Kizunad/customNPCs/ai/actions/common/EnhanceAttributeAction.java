package com.Kizunad.customNPCs.ai.actions.common;

import com.Kizunad.customNPCs.ai.actions.AbstractStandardAction;
import com.Kizunad.customNPCs.ai.actions.ActionResult;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.config.NpcAttributeDefaults;
import com.Kizunad.customNPCs.ai.llm.LlmPromptRegistry;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 提升属性动作：消耗经验点，按统一比例增强指定方向（力量/生命/速度/防御/感应）。
 * <p>
 * 经验转换与归一化：
 * - 10 经验 = 0.1 TOKEN（固定消费一个档位），TOKEN 按基准属性的同一百分比换算，避免基础值差异导致收益失衡。
 * - 未指定方向时随机选取一个方向，并使用较低幅度（随机系数）以降低波动。
 */
public class EnhanceAttributeAction extends AbstractStandardAction {

    public static final String LLM_USAGE_DESC =
        "EnhanceAttributeAction: spend 10 experience to grant 0.2x base-value bonus to one attribute " +
        "(strength/health/speed/defense/sensor); optional direction, otherwise pick random with reduced gain.";

    static {
        LlmPromptRegistry.register(LLM_USAGE_DESC);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(
        EnhanceAttributeAction.class
    );
    private static final int EXPERIENCE_COST = 10;
    private static final float TOKEN_PER_EXPERIENCE = 0.01F; // 10 exp -> 0.1 token
    private static final float PERCENT_PER_TOKEN = 0.2F; // 每个 token 增加基准属性的 20%
    private static final float MIN_DELTA = 0.01F;
    private static final float RANDOM_FACTOR_MIN = 0.35F;
    private static final float RANDOM_FACTOR_MAX = 0.65F;
    private static final float SPEED_FACTOR = 0.05f;
    private static final float DEFENSE_FACTOR = 0.05f;
    private static final float ATTACK_FACTOR = 0.5f;
    private static final float HEALTH_FACTOR = 50.0f;

    private final AttributeDirection preferredDirection;
    private AttributeDirection resolvedDirection;
    private float resolvedRandomFactor = 1.0F;
    private String lastFailureReason = "";

    public EnhanceAttributeAction(AttributeDirection direction) {
        super(
            "EnhanceAttributeAction",
            null,
            CONFIG.getDefaultTimeoutTicks(),
            CONFIG.getDefaultMaxRetries(),
            CONFIG.getDefaultNavRange()
        );
        this.preferredDirection = direction;
    }

    @Override
    protected ActionStatus tickInternal(INpcMind mind, Mob mob) {
        if (!(mob instanceof CustomNpcEntity npc)) {
            lastFailureReason = "仅支持 CustomNpcEntity";
            return ActionStatus.FAILURE;
        }

        if (npc.getExperience() < EXPERIENCE_COST) {
            lastFailureReason = "经验不足，至少需要 10 点";
            return ActionStatus.FAILURE;
        }

        resolveDirection(npc);
        float tokens = EXPERIENCE_COST * TOKEN_PER_EXPERIENCE;
        float delta = computeDelta(tokens, resolvedDirection);
        if (preferredDirection == null) {
            delta *= resolvedRandomFactor; // 未指定方向时降低增益
        }

        applyDelta(npc, resolvedDirection, delta);
        npc.addExperience(-EXPERIENCE_COST);

        LOGGER.info(
            "[EnhanceAttributeAction] {} 增加 {} | 消耗经验 {}",
            resolvedDirection,
            delta,
            EXPERIENCE_COST
        );
        return ActionStatus.SUCCESS;
    }

    @Override
    public ActionResult tickWithReason(INpcMind mind, LivingEntity entity) {
        ActionStatus status = tick(mind, entity);
        if (status == ActionStatus.FAILURE) {
            return new ActionResult(status, lastFailureReason);
        }
        return new ActionResult(status);
    }

    @Override
    protected void onStop(INpcMind mind, LivingEntity entity) {
        lastFailureReason = "";
    }

    @Override
    public boolean canInterrupt() {
        return true;
    }

    private void resolveDirection(CustomNpcEntity npc) {
        if (resolvedDirection != null) {
            return;
        }
        if (preferredDirection != null) {
            resolvedDirection = preferredDirection;
            resolvedRandomFactor = 1.0F;
            return;
        }
        AttributeDirection[] dirs = AttributeDirection.values();
        int idx = npc.getRandom().nextInt(dirs.length);
        resolvedDirection = dirs[idx];
        resolvedRandomFactor = Mth.nextFloat(
            npc.getRandom(),
            RANDOM_FACTOR_MIN,
            RANDOM_FACTOR_MAX
        );
    }

    private float computeDelta(float tokens, AttributeDirection direction) {
        double base = switch (direction) {
            case STRENGTH -> NpcAttributeDefaults.ATTACK_DAMAGE;
            case HEALTH -> NpcAttributeDefaults.MAX_HEALTH;
            case SPEED -> NpcAttributeDefaults.MOVEMENT_SPEED;
            case DEFENSE -> NpcAttributeDefaults.ARMOR;
            case SENSOR -> NpcAttributeDefaults.FOLLOW_RANGE;
        };
        double scaled = base * tokens * PERCENT_PER_TOKEN;
        return Math.max(MIN_DELTA, (float) scaled);
    }

    private void applyDelta(
        CustomNpcEntity npc,
        AttributeDirection direction,
        float delta
    ) {
        switch (direction) {
            case STRENGTH -> npc.addStrengthBonus(delta * ATTACK_FACTOR);
            case HEALTH -> npc.addHealthBonus(delta * HEALTH_FACTOR);
            case SPEED -> npc.addSpeedBonus(delta * SPEED_FACTOR);
            case DEFENSE -> npc.addDefenseBonus(delta * DEFENSE_FACTOR);
            case SENSOR -> npc.addSensorBonus(delta);
            default -> {}
        }
    }

    /**
     * 属性方向枚举。
     */
    public enum AttributeDirection {
        STRENGTH,
        HEALTH,
        SPEED,
        DEFENSE,
        SENSOR,
    }
}
