package com.Kizunad.customNPCs.ai.actions.common;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.actions.AbstractStandardAction;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * 远程武器攻击动作 - 使用弓、弩等远程物品对目标进行射击
 * <p>
 * 强调使用物品进行攻击，而非自主技能（如魔法、吐息等）。
 * <p>
 * 功能特性：
 * - 弹药检查（箭矢、火球等）
 * - 持续瞄准目标
 * - 弩/弓的充能机制差异处理
 * - 写入世界状态供 GOAP 规划
 * <p>
 * 前置条件：
 * - 手持 ProjectileWeaponItem（弓/弩）
 * - 目标在有效距离窗口内（4-12 格）
 * - 有可用弹药
 */
public class RangedAttackItemAction extends AbstractStandardAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        RangedAttackItemAction.class
    );

    // ==================== 距离配置 ====================
    /**
     * 最小射程（格）- 太近容易被近战反击
     */
    private static final double MIN_RANGE = 4.0d;

    /**
     * 最大射程（格）- 太远命中率低
     */
    private static final double MAX_RANGE = 12.0d;

    // ==================== 充能配置 ====================
    /**
     * 弓的最小充能时间（ticks）- 确保有足够的伤害
     */
    private static final int BOW_MIN_CHARGE_TICKS = 20;

    /**
     * 最大转头速度（度/tick）
     */
    private static final float MAX_HEAD_ROTATION = 30.0F;

    /**
     * 世界状态记忆时长（ticks）
     */
    private static final int STATE_MEMORY_DURATION = 20;

    // ==================== 状态字段 ====================
    /**
     * 是否已射击
     */
    private boolean fired;

    /**
     * 充能计数（拉弓/装弩时间）
     */
    private int chargeTicks;

    /**
     * 持武器的手（主手/副手）
     */
    private InteractionHand weaponHand;

    /**
     * 构造函数
     * @param targetUuid 目标实体 UUID
     */
    public RangedAttackItemAction(UUID targetUuid) {
        super("RangedAttackItemAction", targetUuid);
        this.fired = false;
        this.chargeTicks = 0;
        this.weaponHand = null;
    }

    @Override
    protected void onStart(INpcMind mind, LivingEntity entity) {
        // 重置状态
        this.fired = false;
        this.chargeTicks = 0;
        this.weaponHand = null;
    }

    @Override
    protected ActionStatus tickInternal(INpcMind mind, Mob mob) {
        // ==================== Step 1: 目标验证 ====================
        Entity targetEntity = resolveEntity(mob.level());
        if (!(targetEntity instanceof LivingEntity livingTarget)) {
            LOGGER.warn("[RangedAttackItemAction] 目标不存在或非生物");
            return ActionStatus.FAILURE;
        }

        // ==================== Step 2: 距离检查 ====================
        double distance = mob.position().distanceTo(livingTarget.position());
        if (distance < MIN_RANGE || distance > MAX_RANGE) {
            if (CONFIG.isDebugLoggingEnabled()) {
                LOGGER.debug(
                    "[RangedAttackItemAction] 距离不在窗口内: {} ({}-{})",
                    distance,
                    MIN_RANGE,
                    MAX_RANGE
                );
            }
            return ActionStatus.FAILURE;
        }

        // ==================== Step 3: 武器检查 ====================
        ItemStack main = mob.getMainHandItem();
        ItemStack off = mob.getOffhandItem();

        // 检查主手或副手是否有远程武器
        if (!isProjectileWeapon(main) && !isProjectileWeapon(off)) {
            LOGGER.debug("[RangedAttackItemAction] 无远程武器，无法射击");
            return ActionStatus.FAILURE;
        }

        // 选择持有远程武器的手
        if (weaponHand == null) {
            weaponHand = isProjectileWeapon(main)
                ? InteractionHand.MAIN_HAND
                : InteractionHand.OFF_HAND;
        }

        ItemStack weapon = mob.getItemInHand(weaponHand);
        if (!isProjectileWeapon(weapon)) {
            LOGGER.debug("[RangedAttackItemAction] 选中的手不再持有远程武器");
            return ActionStatus.FAILURE;
        }

        // ==================== Step 4: 弹药检查 ====================
        ItemStack ammo = mob.getProjectile(weapon);
        if (ammo.isEmpty()) {
            LOGGER.warn("[RangedAttackItemAction] 无弹药，无法射击");
            // 写入世界状态：无弹药
            mind.getMemory().rememberShortTerm(
                WorldStateKeys.HAS_RANGED_WEAPON,
                false,
                STATE_MEMORY_DURATION
            );
            return ActionStatus.FAILURE;
        }

        // ==================== Step 5: 持续瞄准目标 ====================
        mob.getLookControl().setLookAt(
            livingTarget,
            MAX_HEAD_ROTATION,  // 最大水平转速
            MAX_HEAD_ROTATION   // 最大俯仰转速
        );

        // ==================== Step 6: 充能与射击 ====================
        // 弩的特殊处理
        if (weapon.getItem() instanceof CrossbowItem) {
            return handleCrossbowAttack(mind, mob, weapon, livingTarget, distance);
        }

        // 弓的处理
        if (weapon.getItem() instanceof BowItem) {
            return handleBowAttack(mind, mob, weapon, livingTarget, distance);
        }

        // 其他远程武器（使用默认逻辑）
        return handleGenericProjectileWeapon(mind, mob, weapon, livingTarget, distance);
    }

    /**
     * 处理弩的攻击逻辑
     */
    private ActionStatus handleCrossbowAttack(
        INpcMind mind,
        Mob mob,
        ItemStack weapon,
        LivingEntity target,
        double distance
    ) {
        // 检查弩是否已装填
        if (CrossbowItem.isCharged(weapon)) {
            // 已装填，直接射击
            mob.releaseUsingItem();
            fired = true;

            LOGGER.info(
                "[RangedAttackItemAction] 弩射击 {} (dist={})",
                target.getName().getString(),
                String.format("%.1f", distance)
            );

            // 写入世界状态
            writeAttackState(mind);
            return ActionStatus.SUCCESS;
        }

        // 未装填，开始装填
        if (!mob.isUsingItem()) {
            mob.startUsingItem(weaponHand);
            chargeTicks = 0;
            LOGGER.debug("[RangedAttackItemAction] 开始装填弩");
        }

        chargeTicks++;

        // 弩的装填时间通常是 25 ticks
        int useDuration = weapon.getUseDuration(mob);
        if (chargeTicks >= useDuration) {
            // 装填完成
            LOGGER.debug("[RangedAttackItemAction] 弩装填完成");
            return ActionStatus.RUNNING;
        }

        return ActionStatus.RUNNING;
    }

    /**
     * 处理弓的攻击逻辑
     */
    private ActionStatus handleBowAttack(
        INpcMind mind,
        Mob mob,
        ItemStack weapon,
        LivingEntity target,
        double distance
    ) {
        // 如果还没开始使用物品，开始拉弓
        if (!mob.isUsingItem()) {
            mob.startUsingItem(weaponHand);
            chargeTicks = 0;
            LOGGER.debug("[RangedAttackItemAction] 开始拉弓");
            return ActionStatus.RUNNING;
        }

        // 充能计数
        chargeTicks++;

        // 弓需要至少 20 ticks 才能充满能量
        if (chargeTicks >= BOW_MIN_CHARGE_TICKS) {
            // 释放射击
            mob.releaseUsingItem();
            fired = true;

            LOGGER.info(
                "[RangedAttackItemAction] 弓射击 {} (dist={}, charge={})",
                target.getName().getString(),
                String.format("%.1f", distance),
                chargeTicks
            );

            // 写入世界状态
            writeAttackState(mind);
            return ActionStatus.SUCCESS;
        }

        return ActionStatus.RUNNING;
    }

    /**
     * 处理通用远程武器（如自定义武器）
     */
    private ActionStatus handleGenericProjectileWeapon(
        INpcMind mind,
        Mob mob,
        ItemStack weapon,
        LivingEntity target,
        double distance
    ) {
        // 使用默认逻辑（与弓类似）
        if (!mob.isUsingItem()) {
            mob.startUsingItem(weaponHand);
            chargeTicks = 0;
            return ActionStatus.RUNNING;
        }

        chargeTicks++;
        int useDuration = weapon.getUseDuration(mob);

        // 使用物品持续时间的 90% 作为充能完成标准
        if (chargeTicks >= Math.max(BOW_MIN_CHARGE_TICKS, useDuration - 2)) {
            mob.releaseUsingItem();
            fired = true;

            LOGGER.info(
                "[RangedAttackItemAction] 远程射击 {} (dist={})",
                target.getName().getString(),
                String.format("%.1f", distance)
            );

            writeAttackState(mind);
            return ActionStatus.SUCCESS;
        }

        return ActionStatus.RUNNING;
    }

    /**
     * 写入攻击相关的世界状态
     */
    private void writeAttackState(INpcMind mind) {
        // 写入目标受到伤害状态
        mind.getMemory().rememberShortTerm(
            WorldStateKeys.TARGET_DAMAGED,
            true,
            STATE_MEMORY_DURATION
        );

        // 写入有远程武器状态（用于后续决策）
        mind.getMemory().rememberShortTerm(
            WorldStateKeys.HAS_RANGED_WEAPON,
            true,
            STATE_MEMORY_DURATION
        );
    }

    @Override
    protected void onStop(INpcMind mind, LivingEntity entity) {
        // 如果正在使用物品，停止使用
        if (entity.isUsingItem()) {
            entity.stopUsingItem();
            LOGGER.debug("[RangedAttackItemAction] 停止使用武器");
        }
    }

    /**
     * 检查是否为远程武器
     */
    private boolean isProjectileWeapon(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ProjectileWeaponItem;
    }

    @Override
    public boolean canInterrupt() {
        // 允许被 CRITICAL 事件中断（如受到攻击时切换到逃跑）
        return true;
    }
}
