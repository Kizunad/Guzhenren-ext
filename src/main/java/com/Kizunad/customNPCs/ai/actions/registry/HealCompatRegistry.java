package com.Kizunad.customNPCs.ai.actions.registry;

import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 治疗兼容注册表。
 * <p>
 * 允许外部模组在默认治疗流程前插入自定义逻辑，
 * 例如使用特殊的治疗蛊虫或自定义药剂。
 * </p>
 */
public final class HealCompatRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        HealCompatRegistry.class
    );
    private static final List<HealCompatHandler> HANDLERS =
        new CopyOnWriteArrayList<>();

    private HealCompatRegistry() {}

    @FunctionalInterface
    public interface HealCompatHandler {
        HealDecision handle(HealContext context);
    }

    public enum HealDecision {
        /**
         * 继续执行默认的治疗物品选择与使用流程。
         */
        CONTINUE,
        /**
         * 兼容逻辑已处理，无需继续默认流程。
         */
        HANDLED,
    }

    public static final class HealContext {
        private final INpcMind mind;
        private final Mob patient;
        private final float healthRatio;
        private HealCandidate candidate;

        public HealContext(INpcMind mind, Mob patient, float healthRatio) {
            this.mind = mind;
            this.patient = patient;
            this.healthRatio = healthRatio;
        }

        public INpcMind getMind() {
            return mind;
        }

        public Mob getPatient() {
            return patient;
        }

        public float getHealthRatio() {
            return healthRatio;
        }

        /**
         * 提供一个自定义的治疗物品候选。
         * @param stack 要使用的物品堆
         * @param sourceSlot 槽位标识（-1 主手，-2 副手，>=0 背包槽位）
         */
        public void propose(ItemStack stack, int sourceSlot) {
            if (stack == null || stack.isEmpty()) {
                return;
            }
            this.candidate = new HealCandidate(stack.copy(), sourceSlot);
        }

        public HealCandidate getCandidate() {
            return candidate;
        }
    }

    public record HealCandidate(ItemStack stack, int sourceSlot) {}

    /**
     * 注册治疗兼容处理器。
     */
    public static void register(HealCompatHandler handler) {
        HANDLERS.add(handler);
    }

    /**
     * 依次调用处理器，若返回 HANDLED 则跳过默认治疗逻辑。
     */
    public static HealDecision dispatch(HealContext context) {
        HealDecision decision = HealDecision.CONTINUE;
        for (HealCompatHandler handler : HANDLERS) {
            try {
                decision = handler.handle(context);
            } catch (Exception e) {
                LOGGER.error("[HealCompatRegistry] 兼容处理器异常", e);
                continue;
            }
            if (decision == HealDecision.HANDLED) {
                return HealDecision.HANDLED;
            }
        }
        return decision;
    }
}
