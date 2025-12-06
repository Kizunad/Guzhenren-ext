package com.Kizunad.customNPCs.ai.actions.common;

import com.Kizunad.customNPCs.ai.actions.AbstractStandardAction;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.util.NavigationUtil;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import java.util.UUID;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 上马动作：让 NPC 靠近指定坐骑并调用 {@link Mob#startRiding(Entity)}。
 * <p>
 * 逻辑足够朴素——阶段一只负责导航+骑乘，不关心自主寻找坐骑；
 * 后续感知/决策接入时可以直接引用该动作。
 */
public class MountAction extends AbstractStandardAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(MountAction.class);

    private static final double DEFAULT_ACCEPTABLE_DISTANCE = 2.0D;
    private static final double DEFAULT_APPROACH_SPEED = 1.15D;
    private static final float LOOK_MAX_ROTATION = 45.0F;

    /** 最终靠近距离 */
    private final double acceptableDistance;
    /** 导航速度（倍速） */
    private final double approachSpeed;
    /** 导航节流冷却 */
    private int pathUpdateCooldown;

    public MountAction(UUID mountUuid) {
        this(mountUuid, DEFAULT_ACCEPTABLE_DISTANCE, DEFAULT_APPROACH_SPEED);
    }

    public MountAction(UUID mountUuid, double acceptableDistance, double approachSpeed) {
        super("MountAction", mountUuid);
        this.acceptableDistance = acceptableDistance;
        this.approachSpeed = approachSpeed;
        this.pathUpdateCooldown = 0;
    }

    @Override
    protected void onStart(INpcMind mind, LivingEntity entity) {
        this.pathUpdateCooldown = 0;
    }

    @Override
    protected void onStop(INpcMind mind, LivingEntity entity) {
        if (entity instanceof Mob mob) {
            mob.getNavigation().stop();
        }
    }

    @Override
    protected ActionStatus tickInternal(INpcMind mind, Mob mob) {
        Entity mountEntity = resolveEntity(mob.level());
        if (mountEntity == null) {
            LOGGER.warn("[MountAction] 找不到目标坐骑: {}", targetUuid);
            return ActionStatus.FAILURE;
        }

        if (!mountEntity.isAlive()) {
            LOGGER.info("[MountAction] 坐骑已失效: {}", mountEntity);
            return ActionStatus.FAILURE;
        }

        if (mountEntity.level() != mob.level()) {
            LOGGER.warn("[MountAction] 坐骑与 NPC 不在同一世界");
            return ActionStatus.FAILURE;
        }

        if (!isSupportedMount(mountEntity)) {
            LOGGER.warn(
                "[MountAction] 目标 {} 不是支持的坐骑类型",
                mountEntity.getName().getString()
            );
            return ActionStatus.FAILURE;
        }

        Entity currentVehicle = mob.getVehicle();
        if (currentVehicle == mountEntity) {
            return ActionStatus.SUCCESS; // 已经骑上目标
        }

        if (currentVehicle != null && currentVehicle != mountEntity) {
            mob.stopRiding(); // 正在其他载具上，先下马
        }

        if (!mountEntity.getPassengers().isEmpty()) {
            LOGGER.info("[MountAction] 坐骑 {} 已被占用", mountEntity.getUUID());
            return ActionStatus.FAILURE;
        }

        double acceptableDistanceSq = acceptableDistance * acceptableDistance;
        double distanceSq = mob.distanceToSqr(mountEntity);
        if (distanceSq > acceptableDistanceSq) {
            // 目标可能在移动，使用粘性导航保持追踪
            pathUpdateCooldown = NavigationUtil.stickyNavigateToEntity(
                mob,
                mountEntity,
                approachSpeed,
                pathUpdateCooldown
            );
            return ActionStatus.RUNNING;
        }

        mob.getNavigation().stop();
        mob.getLookControl().setLookAt(mountEntity, LOOK_MAX_ROTATION, LOOK_MAX_ROTATION);
        boolean mounted = mob.startRiding(mountEntity, true);
        if (!mounted) {
            LOGGER.warn(
                "[MountAction] startRiding 失败: {} -> {}",
                mob.getUUID(),
                mountEntity.getUUID()
            );
            return ActionStatus.FAILURE;
        }

        return ActionStatus.SUCCESS;
    }

    /**
     * 仅允许文档中规划的坐骑类型，避免意外骑乘不受控实体。
     */
    private boolean isSupportedMount(Entity entity) {
        return entity instanceof AbstractHorse ||
        entity instanceof Boat ||
        entity instanceof AbstractMinecart;
    }

    @Override
    public boolean canInterrupt() {
        // 上马仅是短暂动作，保持可中断以便战斗/躲避立即接管
        return true;
    }
}
