package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.actions.AbstractStandardAction;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.base.MoveToOwnerAction;
import com.Kizunad.customNPCs.ai.actions.common.AttackAction;
import com.Kizunad.customNPCs.ai.actions.common.RangedAttackItemAction;
import com.Kizunad.customNPCs.ai.actions.config.ActionConfig;
import com.Kizunad.customNPCs.ai.actions.util.NavigationUtil;
import com.Kizunad.customNPCs.ai.decision.NpcCommandType;
import com.Kizunad.customNPCs.ai.llm.LlmPromptRegistry;
import com.Kizunad.customNPCs.ai.util.EntityRelationUtil;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import java.util.Comparator;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.phys.AABB;

/**
 * 指令目标：根据主人下达的 FOLLOW/SIT/WORK/GUARD/DISMISS 执行动作。
 * <p>
 * 设计要点：
 * - 固定高优先级（90），低于生存，确保指令覆盖日常行为。
 * - FOLLOW 复用 MoveToOwnerAction；GUARD 参考 Hunt 的筛选与战斗策略，守护主人 32 格范围。
 * - SIT/WORK 暂时作为占位，保持原地待命，等待后续拓展。
 */
public class CommandGoal implements com.Kizunad.customNPCs.ai.decision.IGoal {

    private static final float COMMAND_PRIORITY = 90.0f;
    private static final double MAX_COMMAND_DISTANCE_SQR = 64.0D * 64.0D;
    private static final double FOLLOW_HOLD_DISTANCE = 3.5D;
    private static final double GUARD_RADIUS = 32.0D;
    private static final double GUARD_HEALTH_RATIO = 0.8D;
    private static final double GUARD_MAX_HEALTH_FACTOR = 1.5D;
    private static final int GUARD_ATTACK_TIMEOUT_TICKS = 200;
    private static final double GUARD_RANGED_MIN_DIST = 4.0D;

    private final double attackRange;
    private final int attackCooldownTicks;
    private NpcCommandType activeCommand = NpcCommandType.NONE;
    private MoveToOwnerAction followAction;
    private AbstractStandardAction guardAction;
    private UUID guardTargetUuid;

    static {
        LlmPromptRegistry.register(
            "CommandGoal: obey owner commands (FOLLOW/SIT/WORK/GUARD). " +
            "Guard clears threats within 32 blocks of owner using Hunt-like filters."
        );
    }

    public CommandGoal() {
        ActionConfig config = ActionConfig.getInstance();
        this.attackRange = config.getAttackRange();
        this.attackCooldownTicks = config.getAttackCooldownTicks();
    }

    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        return canRun(mind, entity) ? COMMAND_PRIORITY : 0.0f;
    }

    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        NpcCommandType command = resolveCommand(mind);
        if (command == null || command == NpcCommandType.NONE) {
            return false;
        }
        return resolveOwner(mind, entity) != null;
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        this.activeCommand = resolveCommand(mind);
        stopActions(mind, entity);
        startForCommand(mind, entity, this.activeCommand);
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        NpcCommandType current = resolveCommand(mind);
        if (current != activeCommand) {
            stopActions(mind, entity);
            activeCommand = current;
            startForCommand(mind, entity, activeCommand);
        }

        if (activeCommand == null || activeCommand == NpcCommandType.NONE) {
            return;
        }

        switch (activeCommand) {
            case FOLLOW -> tickFollow(mind, entity);
            case SIT -> tickSit(entity);
            case WORK -> tickWork(entity);
            case GUARD -> tickGuard(mind, entity);
            default -> {
            }
        }
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        stopActions(mind, entity);
        activeCommand = NpcCommandType.NONE;
    }

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        UUID ownerId = mind
            .getMemory()
            .getMemory(WorldStateKeys.OWNER_UUID, UUID.class);
        if (ownerId == null) {
            return true;
        }
        return resolveCommand(mind) == NpcCommandType.NONE;
    }

    @Override
    public String getName() {
        return "command";
    }

    private void startForCommand(
        INpcMind mind,
        LivingEntity entity,
        NpcCommandType command
    ) {
        if (command == null) {
            return;
        }
        switch (command) {
            case FOLLOW -> startFollow(mind, entity);
            case SIT, WORK -> stopNavigation(entity);
            case GUARD -> startGuard(mind, entity);
            default -> {
            }
        }
    }

    private void startFollow(INpcMind mind, LivingEntity entity) {
        stopActions(mind, entity);
        this.followAction = new MoveToOwnerAction();
        this.followAction.start(mind, entity);
    }

    private void tickFollow(INpcMind mind, LivingEntity entity) {
        if (!(entity instanceof Mob mob)) {
            return;
        }
        if (followAction == null) {
            if (!shouldMoveToOwner(mind, mob)) {
                stopNavigation(mob);
                return;
            }
            startFollow(mind, entity);
            return;
        }
        ActionStatus status = followAction.tick(mind, mob);
        if (status == ActionStatus.SUCCESS || status == ActionStatus.FAILURE) {
            followAction.stop(mind, mob);
            followAction = null;
        }
    }

    private void tickSit(LivingEntity entity) {
        if (entity instanceof Mob mob) {
            stopNavigation(mob);
        }
    }

    private void tickWork(LivingEntity entity) {
        if (entity instanceof Mob mob) {
            // 占位：工作指令未定义，先保持原地待机
            stopNavigation(mob);
        }
    }

    private void startGuard(INpcMind mind, LivingEntity entity) {
        stopActions(mind, entity);
        guardTargetUuid = null;
    }

    private boolean shouldMoveToOwner(INpcMind mind, Mob mob) {
        LivingEntity owner = resolveOwner(mind, mob);
        if (owner == null) {
            return false;
        }
        return mob.distanceTo(owner) > FOLLOW_HOLD_DISTANCE;
    }

    private void tickGuard(INpcMind mind, LivingEntity entity) {
        if (!(entity instanceof Mob mob)) {
            return;
        }
        LivingEntity owner = resolveOwner(mind, mob);
        if (owner == null) {
            stopActions(mind, entity);
            return;
        }
        LivingEntity target = ensureGuardTarget(mob, owner);
        if (target == null) {
            stopGuardAction(mind, mob);
            return;
        }
        if (guardAction == null) {
            createGuardAction(mob, target);
            if (guardAction != null) {
                guardAction.start(mind, mob);
            }
        }
        if (guardAction == null) {
            return;
        }
        ActionStatus status = guardAction.tick(mind, mob);
        if (
            status == ActionStatus.SUCCESS ||
            status == ActionStatus.FAILURE
        ) {
            guardAction.stop(mind, mob);
            guardAction = null;
        }
    }

    private void createGuardAction(Mob mob, LivingEntity target) {
        if (shouldUseRanged(mob, target)) {
            guardAction = new RangedAttackItemAction(target.getUUID());
        } else {
            guardAction =
                new AttackAction(
                    target.getUUID(),
                    attackRange,
                    attackCooldownTicks,
                    GUARD_ATTACK_TIMEOUT_TICKS
                );
        }
    }

    private LivingEntity ensureGuardTarget(Mob mob, LivingEntity owner) {
        LivingEntity current = resolveGuardTarget(mob);
        if (isValidTarget(mob, owner, current)) {
            return current;
        }
        LivingEntity ownerAggro = resolveOwnerAttackTarget(owner);
        if (isValidTarget(mob, owner, ownerAggro)) {
            guardTargetUuid = ownerAggro.getUUID();
            return ownerAggro;
        }
        LivingEntity selected = selectThreat(mob, owner);
        guardTargetUuid = selected == null ? null : selected.getUUID();
        return selected;
    }

    private LivingEntity resolveGuardTarget(Mob mob) {
        if (
            guardTargetUuid == null ||
            !(mob.level() instanceof ServerLevel serverLevel)
        ) {
            return null;
        }
        Entity resolved = serverLevel.getEntity(guardTargetUuid);
        return resolved instanceof LivingEntity living ? living : null;
    }

    private LivingEntity selectThreat(Mob mob, LivingEntity owner) {
        if (!(mob.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        AABB box = owner.getBoundingBox().inflate(GUARD_RADIUS);
        return serverLevel
            .getEntitiesOfClass(LivingEntity.class, box, target ->
                isCandidate(mob, owner, target)
            )
            .stream()
            .filter(target ->
                NavigationUtil.canReachEntity(
                    mob,
                    target,
                    GUARD_RADIUS + 1.0D
                )
            )
            .min(Comparator.comparingDouble(owner::distanceToSqr))
            .orElse(null);
    }

    private boolean isCandidate(
        LivingEntity mob,
        LivingEntity owner,
        LivingEntity target
    ) {
        if (target == null || target == mob || target == owner) {
            return false;
        }
        if (!target.isAlive() || target.isInvisible()) {
            return false;
        }
        if (
            target instanceof Player player &&
            (player.isCreative() || player.isSpectator())
        ) {
            return false;
        }
        if (EntityRelationUtil.isAlly(mob, target)) {
            return false;
        }
        if (owner.distanceTo(target) > GUARD_RADIUS) {
            return false;
        }
        return isLowRiskTarget(mob, target);
    }

    private boolean isLowRiskTarget(LivingEntity mob, LivingEntity target) {
        double max = target.getMaxHealth();
        if (max <= 0.0D) {
            return false;
        }
        double ratio = target.getHealth() / max;
        boolean healthAdvantage =
            target.getHealth() <= mob.getMaxHealth() * GUARD_MAX_HEALTH_FACTOR;
        return ratio <= GUARD_HEALTH_RATIO || healthAdvantage;
    }

    private LivingEntity resolveOwnerAttackTarget(LivingEntity owner) {
        LivingEntity lastHurt = owner.getLastHurtMob();
        if (lastHurt != null) {
            return lastHurt;
        }
        Entity direct = owner.getControllingPassenger();
        if (direct instanceof LivingEntity living) {
            return living.getLastHurtMob();
        }
        return null;
    }

    private boolean shouldUseRanged(Mob mob, LivingEntity target) {
        double distSqr = mob.distanceToSqr(target);
        if (distSqr < GUARD_RANGED_MIN_DIST * GUARD_RANGED_MIN_DIST) {
            return false;
        }
        ItemStack main = mob.getMainHandItem();
        ItemStack off = mob.getOffhandItem();
        boolean hasRangedWeapon =
            isProjectileWeapon(main) || isProjectileWeapon(off);
        if (!hasRangedWeapon) {
            return false;
        }
        return hasAmmoForWeapon(mob, main) || hasAmmoForWeapon(mob, off);
    }

    private boolean isProjectileWeapon(ItemStack stack) {
        return (
            !stack.isEmpty() && stack.getItem() instanceof ProjectileWeaponItem
        );
    }

    private boolean hasAmmoForWeapon(Mob mob, ItemStack weapon) {
        if (!isProjectileWeapon(weapon)) {
            return false;
        }
        ProjectileWeaponItem gun = (ProjectileWeaponItem) weapon.getItem();
        Predicate<ItemStack> ammoPredicate = gun.getAllSupportedProjectiles();
        ItemStack other = weapon == mob.getMainHandItem()
            ? mob.getOffhandItem()
            : mob.getMainHandItem();
        if (ammoPredicate.test(other)) {
            return true;
        }
        return !mob.getProjectile(weapon).isEmpty();
    }

    private LivingEntity resolveOwner(INpcMind mind, LivingEntity entity) {
        UUID ownerId = mind
            .getMemory()
            .getMemory(WorldStateKeys.OWNER_UUID, UUID.class);
        if (ownerId == null || !(entity.level() instanceof ServerLevel level)) {
            return null;
        }
        ServerPlayer owner = level
            .getServer()
            .getPlayerList()
            .getPlayer(ownerId);
        if (owner != null && owner.distanceToSqr(entity) <= MAX_COMMAND_DISTANCE_SQR) {
            return owner;
        }
        Entity resolved = level.getEntity(ownerId);
        return resolved instanceof LivingEntity living &&
                living.distanceToSqr(entity) <= MAX_COMMAND_DISTANCE_SQR
            ? living
            : null;
    }

    private NpcCommandType resolveCommand(INpcMind mind) {
        String commandName = mind
            .getMemory()
            .getMemory(WorldStateKeys.CURRENT_COMMAND, String.class);
        if (commandName == null) {
            return NpcCommandType.NONE;
        }
        try {
            return NpcCommandType.valueOf(commandName);
        } catch (IllegalArgumentException e) {
            return NpcCommandType.NONE;
        }
    }

    private boolean isValidTarget(
        Mob mob,
        LivingEntity owner,
        LivingEntity target
    ) {
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (owner.distanceTo(target) > GUARD_RADIUS) {
            return false;
        }
        if (EntityRelationUtil.isAlly(mob, target)) {
            return false;
        }
        return isLowRiskTarget(mob, target);
    }

    private void stopNavigation(LivingEntity entity) {
        if (entity instanceof Mob mob) {
            mob.getNavigation().stop();
        }
    }

    private void stopActions(INpcMind mind, LivingEntity entity) {
        if (followAction != null) {
            followAction.stop(mind, entity);
            followAction = null;
        }
        stopGuardAction(mind, entity);
    }

    private void stopGuardAction(INpcMind mind, LivingEntity entity) {
        if (guardAction != null) {
            guardAction.stop(mind, entity);
            guardAction = null;
        }
        guardTargetUuid = null;
    }
}
