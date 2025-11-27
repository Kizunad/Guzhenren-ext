package com.Kizunad.customNPCs.ai.actions.common;

import com.Kizunad.customNPCs.ai.actions.AbstractStandardAction;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.interfaces.IAttackAction;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * 攻击动作 - 攻击指定目标实体
 * <p>
 * 功能：
 * - 检查目标距离
 * - 执行攻击动画（swing）
 * - 造成伤害（doHurtTarget）
 * - 处理攻击冷却
 * <p>
 * 参数：
 * - targetUuid: 目标实体的 UUID
 * - attackRange: 攻击距离阈值（默认 3.0 blocks）
 * - cooldownTicks: 攻击冷却时长（默认 20 ticks）
 * - maxAttempts: 最大攻击尝试次数
 */
@SuppressWarnings("checkstyle:MagicNumber")
public class AttackAction extends AbstractStandardAction implements IAttackAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttackAction.class);

    // 从配置获取默认值

    // ==================== 参数 ====================
    /**
     * 攻击距离阈值（blocks）
     */
    private final double attackRange;

    /**
     * 攻击冷却时长（ticks）
     */
    private final int cooldownTicks;

    /**
     * 最大攻击尝试时长（ticks）
     */
    private final int maxAttemptTicks;

    // ==================== 状态 ====================
    /**
     * 攻击冷却计数器
     */
    private int cooldownCounter;

    /**
     * 是否已成功命中
     */
    private boolean hasHit;

    /**
     * 尝试攻击的 tick 数
     */
    private int attemptTicks;

    /**
     * 创建攻击动作（使用默认值）
     * @param targetUuid 目标实体 UUID
     */
    public AttackAction(UUID targetUuid) {
        this(
            targetUuid,
            CONFIG.getAttackRange(),
            CONFIG.getAttackCooldownTicks(),
            CONFIG.getMaxAttackAttemptTicks()
        );
    }

    /**
     * 创建攻击动作（完整参数）
     * @param targetUuid 目标实体 UUID
     * @param attackRange 攻击距离
     * @param cooldownTicks 攻击冷却时长
     * @param maxAttemptTicks 最大尝试时长
     */
    public AttackAction(
        UUID targetUuid,
        double attackRange,
        int cooldownTicks,
        int maxAttemptTicks
    ) {
        super("AttackAction", targetUuid, maxAttemptTicks, 0, attackRange);
        this.attackRange = attackRange;
        this.cooldownTicks = cooldownTicks;
        this.maxAttemptTicks = maxAttemptTicks;
        this.cooldownCounter = 0;
        this.hasHit = false;
        this.attemptTicks = 0;
    }

    @Override
    protected ActionStatus tickInternal(INpcMind mind, Mob mob) {
        // 获取目标实体
        Entity targetEntity = resolveEntity(mob.level());
        if (targetEntity == null) {
            LOGGER.warn("[AttackAction] 目标实体 {} 不存在", targetUuid);
            return ActionStatus.FAILURE;
        }

        // 检查目标是否存活
        if (!targetEntity.isAlive()) {
            LOGGER.info("[AttackAction] 目标已死亡");
            return ActionStatus.SUCCESS; // 目标已死亡视为成功
        }

        // 检查目标是否切换维度
        if (!targetEntity.level().equals(mob.level())) {
            LOGGER.warn("[AttackAction] 目标切换维度");
            return ActionStatus.FAILURE;
        }

        // 检查距离
        Vec3 mobPos = mob.position();
        Vec3 targetPos = targetEntity.position();
        double distance = mobPos.distanceTo(targetPos);
        
        if (!isInRange(mobPos, targetPos, attackRange)) {
            if (CONFIG.isDebugLoggingEnabled()) {
                LOGGER.debug(
                    "[AttackAction] 目标超出攻击范围 | 距离: {}, 阈值: {}",
                    distance,
                    attackRange
                );
            }
            return ActionStatus.FAILURE; // 距离过远，由规划器重新生成 [MoveTo, Attack] 计划
        }

        // 处理冷却
        if (cooldownCounter > 0) {
            cooldownCounter--;
            return ActionStatus.RUNNING;
        }

        // 执行攻击
        performAttack(mob, targetEntity);

        // 设置冷却
        cooldownCounter = cooldownTicks;
        hasHit = true;

        // 成功条件：命中一次即为成功
        LOGGER.info("[AttackAction] 攻击成功，目标: {}", targetEntity.getName().getString());
        return ActionStatus.SUCCESS;
    }

    /**
     * 执行攻击逻辑
     * @param mob 攻击者
     * @param target 目标
     */
    private void performAttack(Mob mob, Entity target) {
        // 播放挥手动画
        mob.swing(InteractionHand.MAIN_HAND);

        // 造成伤害
        if (target instanceof LivingEntity livingTarget) {
            mob.doHurtTarget(livingTarget);
            LOGGER.debug(
                "[AttackAction] 攻击 {} | 剩余生命值: {}",
                target.getName().getString(),
                livingTarget.getHealth()
            );
        }
    }

    @Override
    protected void onStart(INpcMind mind, LivingEntity entity) {
        this.cooldownCounter = 0;
        this.hasHit = false;
        this.attemptTicks = 0;
        LOGGER.info("[AttackAction] 开始攻击目标 {}", targetUuid);
    }

    @Override
    protected void onStop(INpcMind mind, LivingEntity entity) {
        LOGGER.info(
            "[AttackAction] 停止攻击 | 已命中: {} | 尝试 {} ticks",
            hasHit,
            attemptTicks
        );
    }

    @Override
    public boolean canInterrupt() {
        // 攻击动作不可中断（优先级高）
        return false;
    }

    @Override
    public UUID getTargetUuid() {
        return targetUuid;
    }

    @Override
    public boolean hasHit() {
        return hasHit;
    }
}
