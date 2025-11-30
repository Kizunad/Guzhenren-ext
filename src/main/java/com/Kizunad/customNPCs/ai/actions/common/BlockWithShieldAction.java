package com.Kizunad.customNPCs.ai.actions.common;

import com.Kizunad.customNPCs.ai.actions.AbstractStandardAction;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 格挡动作 - 使用盾牌短时间举盾，减少伤害并为撤退/反击争取时间。
 */
public class BlockWithShieldAction extends AbstractStandardAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        BlockWithShieldAction.class
    );
    private static final int DEFAULT_BLOCK_DURATION_TICKS = 40;

    private final int blockDurationTicks;
    private int remainingTicks;
    private InteractionHand blockingHand;

    /**
     * 默认构造函数，使用默认格挡持续时间。
     */
    public BlockWithShieldAction() {
        this(DEFAULT_BLOCK_DURATION_TICKS);
    }

    /**
     * 构造函数，指定格挡持续时间。
     * @param blockDurationTicks 格挡持续时间（单位：ticks）
     */
    public BlockWithShieldAction(int blockDurationTicks) {
        super("BlockWithShieldAction");
        this.blockDurationTicks = blockDurationTicks;
    }

    @Override
    /**
     * 开始格挡动作时的初始化。
     * @param mind NPC 的思维模块
     * @param entity NPC 实体
     */
    protected void onStart(INpcMind mind, LivingEntity entity) {
        this.remainingTicks = blockDurationTicks;
        this.blockingHand = findShieldHand(entity);
        if (blockingHand == null) {
            LOGGER.warn("[BlockWithShieldAction] 无可用盾牌，无法格挡");
        }
    }

    @Override
    /**
     * 每 tick 执行一次格挡逻辑。
     * @param mind NPC 的思维模块
     * @param mob NPC 实体
     * @return 动作状态
     */
    protected ActionStatus tickInternal(INpcMind mind, Mob mob) {
        if (blockingHand == null) {
            return ActionStatus.FAILURE;
        }

        ItemStack stack = mob.getItemInHand(blockingHand);
        if (!stack.is(Items.SHIELD)) {
            LOGGER.debug("[BlockWithShieldAction] 盾牌已不存在，终止格挡");
            return ActionStatus.FAILURE;
        }

        if (!mob.isUsingItem()) {
            mob.startUsingItem(blockingHand);
        }

        remainingTicks--;
        if (remainingTicks <= 0) {
            return ActionStatus.SUCCESS;
        }
        return ActionStatus.RUNNING;
    }

    @Override
    /**
     * 结束格挡动作时的清理。
     * @param mind NPC 的思维模块
     * @param entity NPC 实体
     */
    protected void onStop(INpcMind mind, LivingEntity entity) {
        if (entity.isUsingItem()) {
            entity.stopUsingItem();
        }
    }

    @Override
    /**
     * 是否可以被打断。格挡动作可以被打断。
     * @return true
     */
    public boolean canInterrupt() {
        return true;
    }

    /**
     * 在实体手中寻找盾牌。
     * @param entity 实体
     * @return 盾牌所在手，若无返回 null
     */
    private InteractionHand findShieldHand(LivingEntity entity) {
        if (entity.getMainHandItem().is(Items.SHIELD)) {
            return InteractionHand.MAIN_HAND;
        }
        if (entity.getOffhandItem().is(Items.SHIELD)) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }
}
