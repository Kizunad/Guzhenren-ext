package com.Kizunad.customNPCs.ai.actions.common;

import com.Kizunad.customNPCs.ai.actions.AbstractStandardAction;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.llm.LlmPromptRegistry;
import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogLevel;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 收集周围方块换算为制造材料点数的动作，不破坏方块，仅基于方块密度计算。
 */
public class GatherMaterialAction extends AbstractStandardAction {

    public static final String LLM_USAGE_DESC =
        "GatherMaterialAction: scan nearby breakable blocks (~3 radius), convert them into material points " +
        "for crafting (stored in customnpcs:material).";

    static {
        LlmPromptRegistry.register(LLM_USAGE_DESC);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(
        GatherMaterialAction.class
    );
    private static final int SEARCH_RADIUS = 3;
    private static final int MAX_BLOCKS = 64;
    private static final float BASE_GAIN = 0.5F;
    private static final float HARDNESS_FACTOR = 0.25F;

    public GatherMaterialAction() {
        super("GatherMaterialAction");
    }

    @Override
    protected ActionStatus tickInternal(INpcMind mind, Mob mob) {
        if (!(mob instanceof CustomNpcEntity npc)) {
            LOGGER.warn("[GatherMaterialAction] 目标实体不是 CustomNpcEntity");
            return ActionStatus.FAILURE;
        }
        if (!(mob.level() instanceof ServerLevel level)) {
            LOGGER.warn("[GatherMaterialAction] 非服务端环境，跳过收集");
            return ActionStatus.FAILURE;
        }

        float gained = collectNearbyMaterial(level, mob, mob.blockPosition());
        if (gained <= 0.0F) {
            MindLog.execution(
                MindLogLevel.WARN,
                "[GatherMaterialAction] 周围无可用方块，收集失败"
            );
            return ActionStatus.FAILURE;
        }

        npc.addMaterial(gained);
        MindLog.execution(
            MindLogLevel.INFO,
            "[GatherMaterialAction] 收集材料 +{}，当前材料 {}",
            gained,
            npc.getMaterial()
        );
        return ActionStatus.SUCCESS;
    }

    /**
     * 遍历半径内方块，按硬度和数量换算材料点数，跳过空气/液体/不可破坏方块。
     * mobGriefing 关闭时仅扫描收集点数，开启时额外破坏方块避免重复刷取。
     */
    private float collectNearbyMaterial(
        ServerLevel level,
        Mob mob,
        BlockPos origin
    ) {
        // Gamerule 仅允许被破坏方块时破坏，其余逻辑为仅收集材料
        boolean canGrief = level
            .getGameRules()
            .getBoolean(GameRules.RULE_MOBGRIEFING);

        BlockPos min = origin.offset(
            -SEARCH_RADIUS,
            -SEARCH_RADIUS,
            -SEARCH_RADIUS
        );

        BlockPos max = origin.offset(
            SEARCH_RADIUS,
            SEARCH_RADIUS,
            SEARCH_RADIUS
        );
        int processed = 0;
        float total = 0.0F;

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (pos.equals(origin)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || !state.getFluidState().isEmpty()) {
                continue;
            }
            if (level.getBlockEntity(pos) != null) {
                continue;
            }
            float hardness = state.getDestroySpeed(level, pos);
            if (hardness < 0.0F) {
                continue;
            }

            if (canGrief) {
                // 破坏方块避免无限刷取材料
                boolean destroyed = level.destroyBlock(pos, false, mob);
                if (!destroyed) {
                    LOGGER.debug(
                        "[GatherMaterialAction] 方块 {} 未被破坏，跳过",
                        state.getBlock()
                    );
                    continue;
                }
            } else {
                LOGGER.trace(
                    "[GatherMaterialAction] mobGriefing 关闭，仅扫描方块 {}",
                    state.getBlock()
                );
            }

            total += BASE_GAIN + hardness * HARDNESS_FACTOR;
            processed++;
            if (processed >= MAX_BLOCKS) {
                break;
            }
        }
        return total;
    }

    @Override
    public boolean canInterrupt() {
        return true;
    }
}
