package com.Kizunad.customNPCs.ai.actions.base;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.IAction;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 注视动作 - 让 NPC 注视指定目标
 * <p>
 * 支持：
 * - 注视固定坐标
 * - 注视移动实体（动态跟踪）
 */
public class LookAtAction implements IAction {

    private final Vec3 targetPos;
    private final Entity targetEntity;
    private final int duration; // 持续时间（ticks）
    private int currentTicks;

    /**
     * 创建注视坐标的动作
     * @param target 目标坐标
     * @param duration 持续时间（ticks）
     */
    public LookAtAction(Vec3 target, int duration) {
        this.targetPos = target;
        this.targetEntity = null;
        this.duration = duration;
        this.currentTicks = 0;
    }

    /**
     * 创建注视实体的动作
     * @param target 目标实体
     * @param duration 持续时间（ticks）
     */
    public LookAtAction(Entity target, int duration) {
        this.targetPos = null;
        this.targetEntity = target;
        this.duration = duration;
        this.currentTicks = 0;
    }

    @Override
    public ActionStatus tick(INpcMind mind, LivingEntity entity) {
        currentTicks++;

        // 检查实体是否为 Mob（只有 Mob 才有 LookControl）
        if (!(entity instanceof net.minecraft.world.entity.Mob mob)) {
            return ActionStatus.FAILURE;
        }

        // 获取目标位置
        Vec3 lookTarget;
        if (targetEntity != null && targetEntity.isAlive()) {
            lookTarget = targetEntity.getEyePosition();
        } else if (targetPos != null) {
            lookTarget = targetPos;
        } else {
            // 目标无效，失败
            return ActionStatus.FAILURE;
        }

        // 使用 LookControl 注视目标
        mob
            .getLookControl()
            .setLookAt(lookTarget.x, lookTarget.y, lookTarget.z);

        // 检查是否完成
        if (currentTicks >= duration) {
            return ActionStatus.SUCCESS;
        }

        return ActionStatus.RUNNING;
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        currentTicks = 0;
        String targetName = targetEntity != null
            ? targetEntity.getName().getString()
            : targetPos.toString();
        System.out.println(
            "[LookAtAction] 开始注视: " +
                targetName +
                "，持续 " +
                duration +
                " ticks"
        );
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        String targetName = targetEntity != null
            ? targetEntity.getName().getString()
            : (targetPos != null ? targetPos.toString() : "null");
        System.out.println("[LookAtAction] 停止注视: " + targetName);
    }

    @Override
    public boolean canInterrupt() {
        // 注视动作可以被中断
        return true;
    }

    @Override
    public String getName() {
        if (targetEntity != null) {
            return "look_at_entity_" + targetEntity.getId();
        } else {
            return "look_at_pos";
        }
    }
}
