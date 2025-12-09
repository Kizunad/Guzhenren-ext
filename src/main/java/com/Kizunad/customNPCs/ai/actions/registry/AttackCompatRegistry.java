package com.Kizunad.customNPCs.ai.actions.registry;

import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 攻击兼容注册表。
 * <p>
 * 允许其他模组在 {@link com.Kizunad.customNPCs.ai.actions.common.AttackAction}
 * 执行前注入自定义逻辑，例如使用特殊武器、触发额外判定等。
 */
public final class AttackCompatRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        AttackCompatRegistry.class
    );
    private static final List<AttackCompatHandler> HANDLERS =
        new CopyOnWriteArrayList<>();

    private AttackCompatRegistry() {}

    @FunctionalInterface
    public interface AttackCompatHandler {
        AttackDecision handle(AttackContext context);
    }

    public enum AttackDecision {
        /**
         * 继续执行默认近战逻辑。
         */
        CONTINUE,
        /**
         * 兼容逻辑已完全处理，跳过默认 {@link net.minecraft.world.entity.Mob#doHurtTarget}.
         */
        HANDLED,
    }

    public static final class AttackContext {
        private final INpcMind mind;
        private final Mob attacker;
        private final LivingEntity target;
        private ItemStack weapon;

        public AttackContext(
            INpcMind mind,
            Mob attacker,
            LivingEntity target,
            ItemStack weapon
        ) {
            this.mind = mind;
            this.attacker = attacker;
            this.target = target;
            this.weapon = weapon;
        }

        public INpcMind getMind() {
            return mind;
        }

        public Mob getAttacker() {
            return attacker;
        }

        public LivingEntity getTarget() {
            return target;
        }

        public ItemStack getWeapon() {
            return weapon;
        }

        /**
         * 更新上下文记录的武器引用，方便后续处理器获知最新主手。
         */
        public void updateWeapon(ItemStack newWeapon) {
            this.weapon = newWeapon;
        }
    }

    /**
     * 注册攻击兼容处理器。
     */
    public static void register(AttackCompatHandler handler) {
        HANDLERS.add(handler);
    }

    /**
     * 依次调用处理器，若有处理器返回 {@link AttackDecision#HANDLED} 则停止并跳过默认攻击。
     * @return 返回最终的处理结果
     */
    public static AttackDecision dispatch(AttackContext context) {
        AttackDecision decision = AttackDecision.CONTINUE;
        for (AttackCompatHandler handler : HANDLERS) {
            try {
                decision = handler.handle(context);
            } catch (Exception e) {
                LOGGER.error("[AttackCompatRegistry] 兼容处理器异常", e);
                continue;
            }
            if (decision == AttackDecision.HANDLED) {
                return AttackDecision.HANDLED;
            }
        }
        return decision;
    }
}
