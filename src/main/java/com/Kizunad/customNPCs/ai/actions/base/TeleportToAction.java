package com.Kizunad.customNPCs.ai.actions.base;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.IAction;
import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogLevel;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 传送动作 - 当常规移动长期失败或卡住时使用的兜底手段
 * <p>
 * 功能：
 * - 将 NPC 直接传送到指定坐标或目标实体当前位置
 * - 对坐标进行世界边界与高度修正，避免非法坐标
 * - 仅在服务端执行，客户端请求会直接失败
 */
public class TeleportToAction implements IAction {

    private static final double MAX_COORDINATE = 30000000.0; // Minecraft 世界边界
    private static final double SAFE_Y_OFFSET = 0.5; // 避免传送后落入方块内部

    private final Vec3 targetPos;
    private final Entity targetEntity;
    private boolean hasTeleported;

    /**
     * 创建传送到指定坐标的动作
     * @param targetPos 目标坐标
     */
    public TeleportToAction(Vec3 targetPos) {
        this(targetPos, null);
    }

    /**
     * 创建传送到目标实体当前位置的动作
     * @param targetEntity 目标实体
     */
    public TeleportToAction(Entity targetEntity) {
        this(null, targetEntity);
    }

    private TeleportToAction(Vec3 targetPos, Entity targetEntity) {
        this.targetPos = targetPos;
        this.targetEntity = targetEntity;
        this.hasTeleported = false;
    }

    @Override
    public ActionStatus tick(INpcMind mind, LivingEntity entity) {
        if (hasTeleported) {
            return ActionStatus.SUCCESS;
        }

        Vec3 destination = resolveDestination();
        if (destination == null) {
            MindLog.execution(MindLogLevel.WARN, "传送失败：目标无效");
            return ActionStatus.FAILURE;
        }

        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            MindLog.execution(
                MindLogLevel.WARN,
                "传送失败：仅允许在服务端执行，当前 level 类型: {}",
                entity.level().getClass().getSimpleName()
            );
            return ActionStatus.FAILURE;
        }

        Vec3 clamped = clampToWorld(destination, serverLevel);
        entity.teleportTo(clamped.x, clamped.y, clamped.z);
        hasTeleported = true;

        MindLog.execution(
            MindLogLevel.WARN,
            "兜底传送执行：从 {} 传送到 {}",
            entity.position(),
            clamped
        );
        return ActionStatus.SUCCESS;
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        hasTeleported = false;
        MindLog.execution(
            MindLogLevel.INFO,
            "传送动作开始，目标: {}",
            targetEntity != null
                ? targetEntity.getName().getString()
                : targetPos
        );
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        MindLog.execution(
            MindLogLevel.INFO,
            "传送动作结束，状态: {}",
            hasTeleported ? "已传送" : "未执行传送"
        );
    }

    @Override
    public boolean canInterrupt() {
        // 传送属于快速兜底操作，执行期间可被更高优先级动作中断
        return true;
    }

    @Override
    public String getName() {
        if (targetEntity != null) {
            return "teleport_to_entity_" + targetEntity.getId();
        }
        return "teleport_to_pos";
    }

    /**
     * 解析当前应使用的目标坐标
     * @return 目标坐标；若无法解析则返回 null
     */
    private Vec3 resolveDestination() {
        if (targetEntity != null) {
            if (targetEntity.isAlive()) {
                return targetEntity.position();
            }
            return null;
        }
        return targetPos;
    }

    /**
     * 将目标坐标限制在世界可用范围内
     * @param destination 原始目标坐标
     * @param level 当前服务端世界
     * @return 修正后的安全坐标
     */
    private Vec3 clampToWorld(Vec3 destination, ServerLevel level) {
        double minY = level.getMinBuildHeight();
        double maxY = level.getMaxBuildHeight() - 1;
        double clampedX = Mth.clamp(
            destination.x,
            -MAX_COORDINATE,
            MAX_COORDINATE
        );
        double clampedY = Mth.clamp(destination.y + SAFE_Y_OFFSET, minY, maxY);
        double clampedZ = Mth.clamp(
            destination.z,
            -MAX_COORDINATE,
            MAX_COORDINATE
        );
        return new Vec3(clampedX, clampedY, clampedZ);
    }
}
